package com.example.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientSmsMessage
import com.example.data.model.CurrencyMode
import com.example.data.model.MaterialEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RemittanceCompany
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStage
import com.example.data.model.VendorEntity
import com.example.data.model.VoucherEntity
import com.example.data.repository.BakriRepository
import com.example.receiver.DeadlineNotificationReceiver
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

enum class AppTab(val labelAr: String) {
    CALCULATOR("الحاسبة"),
    ORDERS("الطلبات"),
    VENDORS("المطابع"),
    KANBAN("المهام"),
    SETTINGS("الإعدادات")
}

data class FinancialSummary(
    val totalBillingsYer: Double = 0.0,
    val totalCollectedYer: Double = 0.0,
    val pendingClientBalancesYer: Double = 0.0,
    val totalVendorDebtsYer: Double = 0.0,
    val activeOrdersCount: Int = 0
)

enum class MessageTemplateType(val titleAr: String) {
    ORDER_RECEIVED("إشعار استلام الطلب"),
    ORDER_READY("إشعار جاهزية المطبوعات"),
    INVOICE_BALANCE("تفاصيل الحساب والفاتورة")
}

class BakriViewModel(
    private val repository: BakriRepository,
    private val preferences: com.example.data.local.AppPreferences = com.example.BakriApplication.instance.preferences
) : ViewModel() {

    // Security & Lock State
    val isPinEnabled = preferences.isPinEnabled
    val pinCode = preferences.pinCode
    val isBiometricEnabled = preferences.isBiometricEnabled
    val officeName = preferences.officeName

    private val _isAppUnlocked = MutableStateFlow(!preferences.isPinEnabled.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        if (isPinEnabled.value) {
            _isAppUnlocked.value = false
        }
    }

    fun setPinEnabled(enabled: Boolean) {
        preferences.setPinEnabled(enabled)
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun setPinCode(code: String) {
        preferences.setPinCode(code)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
    }

    fun setOfficeName(name: String) {
        preferences.setOfficeName(name)
    }

    // Message Templates
    val templateReceived = preferences.templateReceived
    val templateReady = preferences.templateReady
    val templateInvoice = preferences.templateInvoice

    fun setTemplateReceived(text: String) = preferences.setTemplateReceived(text)
    fun setTemplateReady(text: String) = preferences.setTemplateReady(text)
    fun setTemplateInvoice(text: String) = preferences.setTemplateInvoice(text)

    // Navigation & Global Preferences
    private val _currentTab = MutableStateFlow(AppTab.CALCULATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode

    private val _currencyMode = MutableStateFlow(CurrencyMode.YER)
    val currencyMode: StateFlow<CurrencyMode> = _currencyMode.asStateFlow()

    private val _usdExchangeRate = MutableStateFlow(530.0) // 1 USD = 530 YER (or configurable)
    val usdExchangeRate: StateFlow<Double> = _usdExchangeRate.asStateFlow()

    private val _defaultDesignFee = MutableStateFlow(2000.0)
    val defaultDesignFee: StateFlow<Double> = _defaultDesignFee.asStateFlow()

    // Toast/Snackbar Events
    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage = _eventMessage.asSharedFlow()

    // Reactive Data Sources
    val materials: StateFlow<List<MaterialEntity>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors: StateFlow<List<VendorEntity>> = repository.allVendors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsMessages: StateFlow<List<ClientSmsMessage>> = repository.allSmsMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Financial Overview Derived Flow
    val financialSummary: StateFlow<FinancialSummary> = combine(orders, vendors) { ordersList, vendorsList ->
        var billings = 0.0
        var collected = 0.0
        var pending = 0.0
        var activeCount = 0

        for (order in ordersList) {
            billings += order.totalDueYer()
            collected += order.paidDepositYer
            pending += order.remainingBalanceYer
            if (order.status != OrderStatus.COMPLETED) {
                activeCount++
            }
        }

        val totalDebts = vendorsList.sumOf { it.remainingDebtYer }

        FinancialSummary(
            totalBillingsYer = billings,
            totalCollectedYer = collected,
            pendingClientBalancesYer = pending,
            totalVendorDebtsYer = totalDebts,
            activeOrdersCount = activeCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // --- Calculator State ---
    val calcWidth = MutableStateFlow("3.0")
    val calcHeight = MutableStateFlow("1.2")
    val calcQuantity = MutableStateFlow("1")
    val calcSelectedMaterial = MutableStateFlow<MaterialEntity?>(null)
    val calcIsDesignReady = MutableStateFlow(false) // false = needs design (+2000 YER)
    val calcDiscount = MutableStateFlow("0")
    val calcDeposit = MutableStateFlow("0")

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun toggleCurrency() {
        _currencyMode.value = if (_currencyMode.value == CurrencyMode.YER) CurrencyMode.USD else CurrencyMode.YER
    }

    fun setUsdExchangeRate(rate: Double) {
        if (rate > 0) _usdExchangeRate.value = rate
    }

    fun setDefaultDesignFee(fee: Double) {
        if (fee >= 0) _defaultDesignFee.value = fee
    }

    // Helper to format currency
    fun formatMoney(amountYer: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (_currencyMode.value == CurrencyMode.USD) 2 else 0
        }
        return if (_currencyMode.value == CurrencyMode.USD) {
            val usd = amountYer / _usdExchangeRate.value
            "${numberFormat.format(usd)} ${CurrencyMode.USD.symbolAr}"
        } else {
            "${numberFormat.format(amountYer)} ${CurrencyMode.YER.symbolAr}"
        }
    }

    // Calculator Actions
    fun selectMaterial(material: MaterialEntity) {
        calcSelectedMaterial.value = material
    }

    fun toggleDesignReady(ready: Boolean) {
        calcIsDesignReady.value = ready
    }

    fun calculateCurrentOrderValues(): CalculatorResult {
        val w = calcWidth.value.toDoubleOrNull() ?: 0.0
        val h = calcHeight.value.toDoubleOrNull() ?: 0.0
        val qty = calcQuantity.value.toIntOrNull() ?: 1
        val area = (w * h * qty).coerceAtLeast(0.0)

        val material = calcSelectedMaterial.value ?: materials.value.firstOrNull()
        val unitPrice = material?.pricePerSqmYer ?: 1200.0
        val printCost = area * unitPrice

        val designFee = if (calcIsDesignReady.value) 0.0 else _defaultDesignFee.value
        val subtotal = printCost + designFee

        val discount = calcDiscount.value.toDoubleOrNull() ?: 0.0
        val totalDue = (subtotal - discount).coerceAtLeast(0.0)
        val deposit = calcDeposit.value.toDoubleOrNull() ?: 0.0
        val remaining = (totalDue - deposit).coerceAtLeast(0.0)

        return CalculatorResult(
            widthM = w,
            heightM = h,
            quantity = qty,
            totalAreaSqm = area,
            materialName = material?.nameAr ?: "فلكس بنر",
            unitPriceYer = unitPrice,
            isDesignReady = calcIsDesignReady.value,
            designFeeYer = designFee,
            subtotalYer = subtotal,
            discountYer = discount,
            totalDueYer = totalDue,
            paidDepositYer = deposit,
            remainingBalanceYer = remaining
        )
    }

    fun createOrderFromCalculator(
        clientName: String,
        clientPhone: String,
        notes: String
    ) {
        val result = calculateCurrentOrderValues()
        if (clientName.isBlank()) {
            emitMessage("يرجى إدخال اسم العميل")
            return
        }

        viewModelScope.launch {
            val order = OrderEntity(
                clientName = clientName.trim(),
                clientPhone = clientPhone.trim(),
                serviceType = result.materialName,
                widthM = result.widthM,
                heightM = result.heightM,
                quantity = result.quantity,
                totalAreaSqm = result.totalAreaSqm,
                isDesignReady = result.isDesignReady,
                designFeeYer = result.designFeeYer,
                unitPriceYer = result.unitPriceYer,
                subtotalYer = result.subtotalYer,
                discountYer = result.discountYer,
                paidDepositYer = result.paidDepositYer,
                remainingBalanceYer = result.remainingBalanceYer,
                status = OrderStatus.PENDING,
                notes = notes
            )
            val orderId = repository.insertOrder(order)

            // Also automatically add a task to Kanban
            repository.insertTask(
                TaskEntity(
                    orderId = orderId,
                    title = "طباعة ${result.materialName} (${result.widthM}×${result.heightM}م)",
                    clientName = clientName.trim(),
                    stage = if (result.isDesignReady) TaskStage.PRINTING else TaskStage.DESIGN,
                    priority = 1
                )
            )

            emitMessage("تم حفظ الطلب وإضافته لخط الإنتاج بنجاح ✨")
            _currentTab.value = AppTab.ORDERS
        }
    }

    // Orders Actions
    fun updateOrderStatus(order: OrderEntity, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, newStatus)
            emitMessage("تم تحديث حالة الطلب إلى: ${newStatus.labelAr}")
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            emitMessage("تم حذف الطلب")
        }
    }

    // Dual Messenger: Direct SIM SMS
    fun sendDirectSms(context: Context, phone: String, messageText: String) {
        if (phone.isBlank()) {
            Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phone, null, messageText, null, null)
            }
            Toast.makeText(context, "تم إرسال رسالة SMS عبر الشريحة بنجاح ✅", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Fallback to launching device SMS app
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                    putExtra("sms_body", messageText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(smsIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "تعذر الإرسال المباشر: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Dual Messenger: WhatsApp Intent
    fun sendWhatsApp(context: Context, phone: String, messageText: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val intlPhone = if (cleanPhone.startsWith("7") && cleanPhone.length == 9) {
            "967$cleanPhone"
        } else if (cleanPhone.startsWith("07") && cleanPhone.length == 10) {
            "967${cleanPhone.substring(1)}"
        } else {
            cleanPhone
        }

        try {
            val encoded = URLEncoder.encode(messageText, "UTF-8")
            val uri = Uri.parse("https://wa.me/$intlPhone?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق واتساب", Toast.LENGTH_SHORT).show()
        }
    }

    // Message Generation with Office Branding & Templates
    fun generateClientMessage(templateType: MessageTemplateType, order: OrderEntity): String {
        val rawTemplate = when (templateType) {
            MessageTemplateType.ORDER_RECEIVED -> templateReceived.value
            MessageTemplateType.ORDER_READY -> templateReady.value
            MessageTemplateType.INVOICE_BALANCE -> templateInvoice.value
        }
        return rawTemplate
            .replace("{OFFICE}", officeName.value)
            .replace("{CLIENT}", order.clientName)
            .replace("{SERVICE}", order.serviceType)
            .replace("{DIMENSIONS}", "${order.widthM} × ${order.heightM} م")
            .replace("{AREA}", String.format(Locale.US, "%.2f", order.totalAreaSqm))
            .replace("{TOTAL}", formatMoney(order.totalDueYer()))
            .replace("{PAID}", formatMoney(order.paidDepositYer))
            .replace("{BALANCE}", formatMoney(order.remainingBalanceYer))
    }

    fun sendWhatsAppWithTemplate(context: Context, phone: String, templateType: MessageTemplateType, order: OrderEntity) {
        val msg = generateClientMessage(templateType, order)
        sendWhatsApp(context, phone, msg)
    }

    // Printing Press Hub (Vendors CRUD)
    fun addVendor(name: String, phone: String, location: String, notes: String) {
        if (name.isBlank()) {
            emitMessage("يرجى إدخال اسم المطبعة")
            return
        }
        viewModelScope.launch {
            repository.insertVendor(
                VendorEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    location = location.trim(),
                    notes = notes.trim()
                )
            )
            emitMessage("تمت إضافة المطبعة إلى الدليل بنجاح")
        }
    }

    fun deleteVendor(vendor: VendorEntity) {
        viewModelScope.launch {
            repository.deleteVendor(vendor)
            emitMessage("تم حذف المطبعة وكافة سجلاتها")
        }
    }

    fun getJobsForVendor(vendorId: Long) = repository.getJobsForVendor(vendorId)

    fun addVendorJob(
        vendorId: Long,
        jobTitle: String,
        material: String,
        dimensions: String,
        costYer: Double,
        executionNotes: String,
        imagePath: String?
    ) {
        if (jobTitle.isBlank()) {
            emitMessage("يرجى كتابة نوع العمل المنفذ")
            return
        }
        viewModelScope.launch {
            repository.addVendorJob(
                com.example.data.model.VendorJobEntity(
                    vendorId = vendorId,
                    jobTitle = jobTitle.trim(),
                    material = material.trim(),
                    dimensions = dimensions.trim(),
                    costYer = costYer,
                    executionNotes = executionNotes.trim(),
                    imagePath = imagePath
                )
            )
            emitMessage("تم حفظ تفاصيل العمل وصورة التنفيذ بنجاح ✅")
        }
    }

    fun deleteVendorJob(job: com.example.data.model.VendorJobEntity) {
        viewModelScope.launch {
            repository.deleteVendorJob(job)
            emitMessage("تم حذف سجل العمل وتحديث رصيد المطبعة")
        }
    }

    fun addVendorJobCost(vendorId: Long, costYer: Double) {
        viewModelScope.launch {
            repository.addVendorJobCost(vendorId, costYer)
            emitMessage("تم تسجيل تكلفة أمر الطباعة في حساب المطبعة")
        }
    }

    fun recordVoucherSettlement(
        vendorId: Long,
        amountYer: Double,
        method: PaymentMethod,
        remittanceCompany: RemittanceCompany?,
        remittanceNumber: String,
        receiptImagePath: String?,
        notes: String
    ) {
        if (amountYer <= 0) {
            emitMessage("يرجى إدخال مبلغ صحيح للسند")
            return
        }
        viewModelScope.launch {
            repository.addVoucherAndSettleDebt(
                VoucherEntity(
                    vendorId = vendorId,
                    amountYer = amountYer,
                    method = method,
                    remittanceCompany = remittanceCompany,
                    remittanceNumber = remittanceNumber.trim(),
                    receiptImagePath = receiptImagePath,
                    notes = notes.trim()
                )
            )
            emitMessage("تم حفظ سند الصرف وتحديث رصيد المديونية بنجاح ✅")
        }
    }

    fun getVouchersForVendor(vendorId: Long) = repository.getVouchersForVendor(vendorId)

    // Kanban Actions
    fun updateTaskStage(task: TaskEntity, newStage: TaskStage) {
        viewModelScope.launch {
            repository.updateTaskStage(task.id, newStage)
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun addTask(title: String, clientName: String, stage: TaskStage, deadlineMs: Long?) {
        if (title.isBlank()) {
            emitMessage("يرجى كتابة عنوان المهمة")
            return
        }
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    clientName = clientName.trim(),
                    stage = stage,
                    deadlineEpochMs = deadlineMs
                )
            )
            emitMessage("تمت إضافة المهمة إلى لوحة الإنتاج")
        }
    }

    fun scheduleTaskAlarm(context: Context, task: TaskEntity, triggerEpochMs: Long) {
        if (triggerEpochMs <= System.currentTimeMillis()) {
            emitMessage("يرجى اختيار وقت في المستقبل")
            return
        }
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
                putExtra(DeadlineNotificationReceiver.EXTRA_TITLE, task.title)
                putExtra(DeadlineNotificationReceiver.EXTRA_CLIENT, task.clientName)
                putExtra(DeadlineNotificationReceiver.EXTRA_ID, task.id.toInt())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            emitMessage("تم جدولة تذكير بموعد التسليم ⏰")
        } catch (_: Exception) {
            emitMessage("تم حفظ الموعد")
        }
    }

    // Material Settings
    fun addMaterial(nameAr: String, pricePerSqmYer: Double, category: String) {
        viewModelScope.launch {
            repository.insertMaterial(
                MaterialEntity(
                    nameAr = nameAr.trim(),
                    nameEn = nameAr.trim(),
                    pricePerSqmYer = pricePerSqmYer,
                    category = category.trim()
                )
            )
            emitMessage("تمت إضافة الخامة إلى قائمة الأسعار")
        }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            repository.updateMaterial(material)
            emitMessage("تم تحديث سعر الخامة")
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
            emitMessage("تم حذف الخامة")
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _eventMessage.emit(msg)
        }
    }
}

data class CalculatorResult(
    val widthM: Double,
    val heightM: Double,
    val quantity: Int,
    val totalAreaSqm: Double,
    val materialName: String,
    val unitPriceYer: Double,
    val isDesignReady: Boolean,
    val designFeeYer: Double,
    val subtotalYer: Double,
    val discountYer: Double,
    val totalDueYer: Double,
    val paidDepositYer: Double,
    val remainingBalanceYer: Double
)

fun OrderEntity.totalDueYer(): Double = (subtotalYer - discountYer).coerceAtLeast(0.0)
