package com.example.ui.screens.orders

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ClientSmsMessage
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.BakriViewModel
import com.example.viewmodel.MessageTemplateType
import com.example.viewmodel.totalDueYer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: BakriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val orders by viewModel.orders.collectAsState()
    val smsMessages by viewModel.smsMessages.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    // Dialog state for SMS dispatch
    var activeOrderForSms by remember { mutableStateOf<OrderEntity?>(null) }
    var smsCustomText by remember { mutableStateOf("") }

    // Dialog state for WhatsApp dispatch with templates
    var activeOrderForWhatsApp by remember { mutableStateOf<OrderEntity?>(null) }
    var selectedTemplateType by remember { mutableStateOf(MessageTemplateType.ORDER_READY) }
    var whatsAppCustomText by remember { mutableStateOf("") }

    // Permission launcher for SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.SEND_SMS] == true
        if (granted && activeOrderForSms != null) {
            viewModel.sendDirectSms(context, activeOrderForSms!!.clientPhone, smsCustomText)
            activeOrderForSms = null
        }
    }

    // Filtered orders list
    val filteredOrders = orders.filter { order ->
        val matchesQuery = searchQuery.isBlank() ||
                order.clientName.contains(searchQuery, ignoreCase = true) ||
                order.clientPhone.contains(searchQuery) ||
                order.serviceType.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == null || order.status == selectedStatusFilter
        matchesQuery && matchesStatus
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📦 متابعة طلبات العملاء والمراسلة",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "إرسال إشعارات استلام وجاهزية المطبوعات عبر واتساب المباشر وترويسة المكتب",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar & Filter chips
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    GlassTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "البحث في الطلبات",
                        placeholder = "اسم العميل، رقم الهاتف، نوع الخامة...",
                        leadingIcon = Icons.Default.Search
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                label = "الكل (${orders.size})",
                                isSelected = selectedStatusFilter == null,
                                onClick = { selectedStatusFilter = null }
                            )
                        }
                        items(OrderStatus.values()) { status ->
                            val count = orders.count { it.status == status }
                            FilterChip(
                                label = "${status.labelAr} ($count)",
                                isSelected = selectedStatusFilter == status,
                                onClick = { selectedStatusFilter = status }
                            )
                        }
                    }
                }
            }
        }

        // Empty state
        if (filteredOrders.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                            text = "لا توجد طلبات مسجلة حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "قم بإنشاء طلب جديد من نافذة الحاسبة لتتبع حالته هنا",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                OrderCardItem(
                    order = order,
                    viewModel = viewModel,
                    allSmsList = smsMessages,
                    onOpenSmsDialog = { orderToMessage, defaultMessage ->
                        activeOrderForSms = orderToMessage
                        smsCustomText = defaultMessage
                    },
                    onOpenWhatsAppDialog = { orderToMessage ->
                        activeOrderForWhatsApp = orderToMessage
                        selectedTemplateType = if (orderToMessage.status == OrderStatus.READY) {
                            MessageTemplateType.ORDER_READY
                        } else {
                            MessageTemplateType.ORDER_RECEIVED
                        }
                        whatsAppCustomText = viewModel.generateClientMessage(selectedTemplateType, orderToMessage)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal WhatsApp Dispatch Dialog with Templates
    activeOrderForWhatsApp?.let { order ->
        AlertDialog(
            onDismissRequest = { activeOrderForWhatsApp = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إرسال رسالة واتساب للعميل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "العميل: ${order.clientName} (${order.clientPhone})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "اختر قالب الرسالة:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Template selection pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MessageTemplateType.values().forEach { tmpl ->
                            val isSel = selectedTemplateType == tmpl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) (if (isDark) Color(0x3310B981) else Color(0x2010B981))
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) EmeraldSuccess else Color(0x30888888),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTemplateType = tmpl
                                        whatsAppCustomText = viewModel.generateClientMessage(tmpl, order)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tmpl.titleAr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSel) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    GlassTextField(
                        value = whatsAppCustomText,
                        onValueChange = { whatsAppCustomText = it },
                        label = "نص الرسالة المجهز (يشمل اسم المكتب)",
                        singleLine = false,
                        modifier = Modifier.height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendWhatsApp(context, order.clientPhone, whatsAppCustomText)
                        activeOrderForWhatsApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح واتساب وإرسال")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeOrderForWhatsApp = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal SMS Dispatch Dialog
    activeOrderForSms?.let { order ->
        AlertDialog(
            onDismissRequest = { activeOrderForSms = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = if (isDark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إرسال SMS مباشر عبر الشريحة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "المرسل إليه: ${order.clientName} (${order.clientPhone})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "سيتم الإرسال فوراً من شريحة الهاتف بدون الحاجة لإنترنت:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    GlassTextField(
                        value = smsCustomText,
                        onValueChange = { smsCustomText = it },
                        label = "نص الرسالة",
                        singleLine = false,
                        modifier = Modifier.height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hasSmsPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasSmsPermission) {
                            viewModel.sendDirectSms(context, order.clientPhone, smsCustomText)
                            activeOrderForSms = null
                        } else {
                            smsPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                        contentColor = if (isDark) Color(0xFF00363A) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إرسال الآن ✉️")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeOrderForSms = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun OrderCardItem(
    order: OrderEntity,
    viewModel: BakriViewModel,
    allSmsList: List<ClientSmsMessage>,
    onOpenSmsDialog: (OrderEntity, String) -> Unit,
    onOpenWhatsAppDialog: (OrderEntity) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var isExpanded by remember { mutableStateOf(false) }

    // Client SMS replies matching this phone
    val phoneSuffix = if (order.clientPhone.length >= 7) order.clientPhone.takeLast(7) else order.clientPhone
    val matchingSms = allSmsList.filter { it.senderPhone.contains(phoneSuffix) }

    val defaultMessage = viewModel.generateClientMessage(
        if (order.status == OrderStatus.READY) MessageTemplateType.ORDER_READY else MessageTemplateType.ORDER_RECEIVED,
        order
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Client Name, Status Badge, Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.clientName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${order.serviceType} • ${order.widthM} × ${order.heightM} م (${order.quantity} قطع)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = order.status)
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x20162033) else Color(0x15E2E8F0))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الإجمالي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(order.totalDueYer()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "المدفوع",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(order.paidDepositYer),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldSuccess
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "المتبقي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatMoney(order.remainingBalanceYer),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (order.remainingBalanceYer > 0) (if (isDark) NeonCyan else Color(0xFF0284C7)) else EmeraldSuccess
                        )
                    )
                }
            }

            // Action Buttons Row: Dual Messenger & Mark Ready Trigger
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Direct SMS Button
                OutlinedButton(
                    onClick = { onOpenSmsDialog(order, defaultMessage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                // WhatsApp Button with template launcher
                OutlinedButton(
                    onClick = { onOpenWhatsAppDialog(order) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Forum, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                // "Mark as Ready" Trigger Button
                if (order.status != OrderStatus.READY && order.status != OrderStatus.COMPLETED) {
                    Button(
                        onClick = {
                            viewModel.updateOrderStatus(order, OrderStatus.READY)
                            // Trigger immediate WhatsApp template dialog with READY template
                            onOpenWhatsAppDialog(order)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                            contentColor = if (isDark) Color(0xFF00363A) else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("جاهز للاستلام", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else if (order.status == OrderStatus.READY) {
                    Button(
                        onClick = { viewModel.updateOrderStatus(order, OrderStatus.COMPLETED) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تم التسليم", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Expanded Details: Stage changer, Notes, and Incoming Client SMS
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Switcher Pills
                    Text(
                        text = "تغيير حالة الطلب:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OrderStatus.values().forEach { st ->
                            val isCurr = order.status == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCurr) (if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurr) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color(0x30888888),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateOrderStatus(order, st) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st.labelAr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isCurr) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (order.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ملاحظات: ${order.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Incoming SMS Replies matching client phone
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ردود العميل الواردة (SMS):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${matchingSms.size} رسائل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (matchingSms.isEmpty()) {
                        Text(
                            text = "لم يتم استلام أي ردود SMS بعد من هذا الرقم",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            matchingSms.take(3).forEach { sms ->
                                val dateStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(sms.timestamp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0x3310B981) else Color(0x2010B981))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = sms.messageBody,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // Delete button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.deleteOrder(order) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف الطلب", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isSelected) {
        if (isDark) Color(0x3300F2FE) else Color(0x260284C7)
    } else {
        if (isDark) Color(0x15FFFFFF) else Color(0x10000000)
    }
    val textColor = if (isSelected) {
        if (isDark) NeonCyan else Color(0xFF0284C7)
    } else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                0.8.dp,
                if (isSelected) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
    }
}
