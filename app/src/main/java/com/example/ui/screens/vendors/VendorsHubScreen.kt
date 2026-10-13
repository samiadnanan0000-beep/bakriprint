package com.example.ui.screens.vendors

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PaymentMethod
import com.example.data.model.RemittanceCompany
import com.example.data.model.VendorEntity
import com.example.data.model.VendorJobEntity
import com.example.data.model.VoucherEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.BakriViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VendorsHubScreen(
    viewModel: BakriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val vendors by viewModel.vendors.collectAsState()

    var showAddVendorDialog by remember { mutableStateOf(false) }
    var vendorToDelete by remember { mutableStateOf<VendorEntity?>(null) }
    var vendorToSettle by remember { mutableStateOf<VendorEntity?>(null) }
    var vendorToAddJob by remember { mutableStateOf<VendorEntity?>(null) }
    var viewingImagePath by remember { mutableStateOf<String?>(null) }

    // State for New Vendor Dialog
    var newVendorName by remember { mutableStateOf("") }
    var newVendorPhone by remember { mutableStateOf("") }
    var newVendorLocation by remember { mutableStateOf("") }
    var newVendorNotes by remember { mutableStateOf("") }

    // State for Add Executed Job Dialog
    var jobTitleInput by remember { mutableStateOf("") }
    var jobMaterialInput by remember { mutableStateOf("") }
    var jobDimensionsInput by remember { mutableStateOf("") }
    var jobCostInput by remember { mutableStateOf("") }
    var jobNotesInput by remember { mutableStateOf("") }
    var jobPhotoLocalPath by remember { mutableStateOf<String?>(null) }

    // State for Payment Voucher Settlement Dialog
    var voucherAmountInput by remember { mutableStateOf("") }
    var voucherMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var voucherRemittanceCompany by remember { mutableStateOf(RemittanceCompany.AL_KURAIMI) }
    var voucherRemittanceNumber by remember { mutableStateOf("") }
    var voucherNotesInput by remember { mutableStateOf("") }
    var voucherReceiptLocalPath by remember { mutableStateOf<String?>(null) }

    // Photo Picker for Executed Job Photos
    val jobPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val jobsDir = File(context.filesDir, "vendor_jobs").apply { mkdirs() }
                        val file = File(jobsDir, "job_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            jobPhotoLocalPath = file.absolutePath
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Photo Picker for Wire Remittance Vouchers
    val voucherPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
                        val file = File(receiptsDir, "voucher_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            voucherReceiptLocalPath = file.absolutePath
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🏭 إدارة المطابع وسجل الأعمال",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "توثيق الأعمال المنفذة، رفع صور النماذج، وسندات سداد الديون",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddVendorDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                        contentColor = if (isDark) Color(0xFF00363A) else Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مطبعة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Summary of total debts across all vendors
        item {
            val totalDebts = vendors.sumOf { it.remainingDebtYer }
            val totalPaid = vendors.sumOf { it.totalPaidYer }
            GlassCard {
                Text(
                    text = "ملخص حسابات المطابع الشريكة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "إجمالي ديون المطابع:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatMoney(totalDebts),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (totalDebts > 0) Color(0xFFEF4444) else EmeraldSuccess
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "إجمالي السداد الموثق:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatMoney(totalPaid),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                        )
                    }
                }
            }
        }

        // Empty state or vendors list
        if (vendors.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لم تتم إضافة أي مطابع وسيطة بعد",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "أضف مطابع البنر، UV، الأعلام أو القص بالليزر لتسجيل الأعمال وسندات السداد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(vendors, key = { it.id }) { vendor ->
                VendorCardItem(
                    vendor = vendor,
                    viewModel = viewModel,
                    onAddJob = {
                        vendorToAddJob = vendor
                        jobTitleInput = ""
                        jobMaterialInput = ""
                        jobDimensionsInput = ""
                        jobCostInput = ""
                        jobNotesInput = ""
                        jobPhotoLocalPath = null
                    },
                    onSettlePayment = {
                        vendorToSettle = vendor
                        voucherReceiptLocalPath = null
                        voucherAmountInput = ""
                        voucherRemittanceNumber = ""
                        voucherNotesInput = ""
                    },
                    onDeleteVendor = { vendorToDelete = vendor },
                    onViewImage = { path -> viewingImagePath = path }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Dialog: Add New Vendor (CRUD)
    if (showAddVendorDialog) {
        AlertDialog(
            onDismissRequest = { showAddVendorDialog = false },
            title = {
                Text(
                    text = "إضافة مطبعة وسيطة جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassTextField(
                        value = newVendorName,
                        onValueChange = { newVendorName = it },
                        label = "اسم المطبعة",
                        placeholder = "مثال: مطابع النصر الحديثة"
                    )
                    GlassTextField(
                        value = newVendorPhone,
                        onValueChange = { newVendorPhone = it },
                        label = "رقم الهاتف / المسؤول",
                        placeholder = "77XXXXXXX",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    GlassTextField(
                        value = newVendorLocation,
                        onValueChange = { newVendorLocation = it },
                        label = "العنوان والموقع",
                        placeholder = "مثال: شارع التحرير - صنعاء",
                        leadingIcon = Icons.Default.LocationOn
                    )
                    GlassTextField(
                        value = newVendorNotes,
                        onValueChange = { newVendorNotes = it },
                        label = "ملاحظات وتخصص المطبعة",
                        placeholder = "مثال: طباعة أقمشة وأعلام، ماكينة فلكس 5 متر",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addVendor(newVendorName, newVendorPhone, newVendorLocation, newVendorNotes)
                        newVendorName = ""
                        newVendorPhone = ""
                        newVendorLocation = ""
                        newVendorNotes = ""
                        showAddVendorDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ المطبعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVendorDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal Dialog: Delete Vendor Confirmation
    vendorToDelete?.let { vendor ->
        AlertDialog(
            onDismissRequest = { vendorToDelete = null },
            title = { Text("تأكيد حذف المطبعة") },
            text = {
                Text("هل أنت متأكد من حذف المطبعة \"${vendor.name}\"؟ سيتم حذف كافة سجلات الأعمال وسندات الصرف المرتبطة بها.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVendor(vendor)
                        vendorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("نعم، حذف المطبعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { vendorToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal Dialog: Add Executed Job (سجل وتفاصيل العمل المنفذ مع صورة من المعرض)
    vendorToAddJob?.let { vendor ->
        AlertDialog(
            onDismissRequest = { vendorToAddJob = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل عمل منفذ: ${vendor.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassTextField(
                        value = jobTitleInput,
                        onValueChange = { jobTitleInput = it },
                        label = "نوع العمل المنفذ",
                        placeholder = "مثال: لوحة فلكس واجهة صيدلية، بنر معرض"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassTextField(
                            value = jobMaterialInput,
                            onValueChange = { jobMaterialInput = it },
                            label = "الخامة المستخدمة",
                            placeholder = "فلكس 440g، فينيل، أكريليك...",
                            modifier = Modifier.weight(1f)
                        )

                        GlassTextField(
                            value = jobDimensionsInput,
                            onValueChange = { jobDimensionsInput = it },
                            label = "المقاسات",
                            placeholder = "مثال: 4 × 2 م",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GlassTextField(
                        value = jobCostInput,
                        onValueChange = { jobCostInput = it },
                        label = "تكلفة العمل المنفذ (ر.ي)",
                        placeholder = "مثال: 25000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    GlassTextField(
                        value = jobNotesInput,
                        onValueChange = { jobNotesInput = it },
                        label = "ملاحظات التنفيذ",
                        placeholder = "تخريم، تركيب إضاءة ليد، لحام أطراف..."
                    )

                    // Photo Picker Button
                    OutlinedButton(
                        onClick = {
                            jobPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (jobPhotoLocalPath != null) "تم إرفاق صورة العمل المنفذ ✓" else "إرفاق صورة العمل المنفذ من المعرض",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = jobCostInput.toDoubleOrNull() ?: 0.0
                        viewModel.addVendorJob(
                            vendorId = vendor.id,
                            jobTitle = jobTitleInput,
                            material = jobMaterialInput,
                            dimensions = jobDimensionsInput,
                            costYer = cost,
                            executionNotes = jobNotesInput,
                            imagePath = jobPhotoLocalPath
                        )
                        vendorToAddJob = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ العمل في السجل")
                }
            },
            dismissButton = {
                TextButton(onClick = { vendorToAddJob = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal Dialog: Payment Settlement Voucher
    vendorToSettle?.let { vendor ->
        AlertDialog(
            onDismissRequest = { vendorToSettle = null },
            title = {
                Text(
                    text = "سند صرف وسداد مديونية: ${vendor.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "المديونية الحالية للمطبعة: ${viewModel.formatMoney(vendor.remainingDebtYer)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    GlassTextField(
                        value = voucherAmountInput,
                        onValueChange = { voucherAmountInput = it },
                        label = "المبلغ المسدد (ر.ي)",
                        placeholder = "مثال: 50000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Method Toggle: Cash vs. Wire Transfer
                    Text(
                        text = "طريقة الصرف:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val isSel = voucherMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) (if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color(0x30888888),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { voucherMethod = method }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method.labelAr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSel) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Wire Transfer Details
                    if (voucherMethod == PaymentMethod.REMITTANCE) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "شبكة التحويل:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RemittanceCompany.values().forEach { comp ->
                                val isCompSel = voucherRemittanceCompany == comp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCompSel) Color(0x339333EA) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isCompSel) NeonViolet else Color(0x30888888),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { voucherRemittanceCompany = comp }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comp.labelAr,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isCompSel) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isCompSel) NeonViolet else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        GlassTextField(
                            value = voucherRemittanceNumber,
                            onValueChange = { voucherRemittanceNumber = it },
                            label = "رقم الحوالة / السند",
                            placeholder = "رقم إشعار الإيداع"
                        )

                        OutlinedButton(
                            onClick = {
                                voucherPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (voucherReceiptLocalPath != null) "تم إرفاق صورة السند ✓" else "إرفاق صورة إشعار الحوالة",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    GlassTextField(
                        value = voucherNotesInput,
                        onValueChange = { voucherNotesInput = it },
                        label = "ملاحظات إضافية",
                        placeholder = "دفعة حساب، تصفية شهر..."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = voucherAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.recordVoucherSettlement(
                                vendorId = vendor.id,
                                amountYer = amount,
                                method = voucherMethod,
                                remittanceCompany = if (voucherMethod == PaymentMethod.REMITTANCE) voucherRemittanceCompany else null,
                                remittanceNumber = voucherRemittanceNumber,
                                receiptImagePath = voucherReceiptLocalPath,
                                notes = voucherNotesInput
                            )
                            vendorToSettle = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل السند وتحديث الرصيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { vendorToSettle = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Full Image Viewer Dialog
    viewingImagePath?.let { path ->
        AlertDialog(
            onDismissRequest = { viewingImagePath = null },
            title = {
                Text(
                    text = "عرض الصورة المرفقة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Full Image View",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingImagePath = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun VendorCardItem(
    vendor: VendorEntity,
    viewModel: BakriViewModel,
    onAddJob: () -> Unit,
    onSettlePayment: () -> Unit,
    onDeleteVendor: () -> Unit,
    onViewImage: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Executed Jobs, 1: Vouchers
    val vouchers by viewModel.getVouchersForVendor(vendor.id).collectAsState(initial = emptyList())
    val jobs by viewModel.getJobsForVendor(vendor.id).collectAsState(initial = emptyList())

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Vendor Name, Phone, Location & Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vendor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (vendor.location.isNotBlank()) {
                        Text(
                            text = "📍 ${vendor.location} • 📞 ${vendor.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteVendor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Vendor",
                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Ledger Mini Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x20162033) else Color(0x15E2E8F0))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إجمالي التكاليف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(vendor.totalJobsCostYer),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "المسدد لهم",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(vendor.totalPaidYer),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldSuccess
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "المتبقي لهم (دين)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(vendor.remainingDebtYer),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (vendor.remainingDebtYer > 0) Color(0xFFEF4444) else EmeraldSuccess
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action Buttons: Add Executed Job & Settle Payment Voucher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddJob,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة عمل منفذ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onSettlePayment,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                        contentColor = if (isDark) Color(0xFF00363A) else Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سند صرف وحوالة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Expanded Section: Executed Jobs Portfolio & Vouchers
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab selector between Executed Jobs and Vouchers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) (if (isDark) Color(0x3300F2FE) else Color(0x200284C7)) else Color.Transparent)
                                .border(1.dp, if (selectedTab == 0) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color(0x30888888), RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الأعمال المنفذة (${jobs.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTab == 0) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) (if (isDark) Color(0x3300F2FE) else Color(0x200284C7)) else Color.Transparent)
                                .border(1.dp, if (selectedTab == 1) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color(0x30888888), RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "سندات الصرف (${vouchers.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTab == 1) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedTab == 0) {
                        // Executed Jobs List (Portfolio)
                        if (jobs.isEmpty()) {
                            Text(
                                text = "لا توجد أعمال منفذة مسجلة لهذه المطبعة حتى الآن. انقر على \"إضافة عمل منفذ\" لإرفاق صورة وتفاصيل العمل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                jobs.forEach { job ->
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(job.createdAt))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Color(0x20162033) else Color(0x10000000))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Job Photo Thumbnail
                                        if (job.imagePath != null) {
                                            AsyncImage(
                                                model = File(job.imagePath),
                                                contentDescription = "Job Photo",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onViewImage(job.imagePath) },
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = job.jobTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "الخامة: ${job.material} • المقاس: ${job.dimensions}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (job.executionNotes.isNotBlank()) {
                                                Text(
                                                    text = "ملاحظات: ${job.executionNotes}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "التكلفة: ${viewModel.formatMoney(job.costYer)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isDark) NeonCyan else Color(0xFF0284C7)
                                                )
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteVendorJob(job) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Job",
                                                tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Vouchers List
                        if (vouchers.isEmpty()) {
                            Text(
                                text = "لا توجد سندات صرف مسجلة لهذه المطبعة حتى الآن",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                vouchers.forEach { voucher ->
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(voucher.createdAt))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0x20162033) else Color(0x10000000))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = viewModel.formatMoney(voucher.amountYer),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = EmeraldSuccess
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• ${voucher.method.labelAr}${voucher.remittanceCompany?.let { " (${it.labelAr})" } ?: ""}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = dateStr + if (voucher.remittanceNumber.isNotBlank()) " | رقم: ${voucher.remittanceNumber}" else "",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (voucher.receiptImagePath != null) {
                                            IconButton(
                                                onClick = { onViewImage(voucher.receiptImagePath) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "View Receipt",
                                                    tint = if (isDark) NeonCyan else Color(0xFF0284C7),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
