package com.example.ui.screens.settings

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaterialEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.BakriViewModel

@Composable
fun SettingsScreen(
    viewModel: BakriViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val themeMode by viewModel.themeMode.collectAsState()
    val defaultDesignFee by viewModel.defaultDesignFee.collectAsState()
    val exchangeRate by viewModel.usdExchangeRate.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()

    // Security & Office Preferences
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val officeName by viewModel.officeName.collectAsState()

    val templateReceived by viewModel.templateReceived.collectAsState()
    val templateReady by viewModel.templateReady.collectAsState()
    val templateInvoice by viewModel.templateInvoice.collectAsState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    var showOfficeNameDialog by remember { mutableStateOf(false) }
    var officeNameInput by remember { mutableStateOf(officeName) }

    var showTemplateDialog by remember { mutableStateOf<String?>(null) } // "received", "ready", "invoice"
    var templateInputText by remember { mutableStateOf("") }

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var materialToEdit by remember { mutableStateOf<MaterialEntity?>(null) }

    // Dialog state for add material
    var newMatNameAr by remember { mutableStateOf("") }
    var newMatPriceStr by remember { mutableStateOf("") }
    var newMatCategory by remember { mutableStateOf("") }

    // State for default fee & exchange rate edits
    var showFeeDialog by remember { mutableStateOf(false) }
    var editFeeInput by remember { mutableStateOf(defaultDesignFee.toInt().toString()) }

    var showRateDialog by remember { mutableStateOf(false) }
    var editRateInput by remember { mutableStateOf(exchangeRate.toInt().toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚙️ الإعدادات والأمان وهوية المكتب",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "إدارة قفل التطبيق بالبيومتري، قوالب رسائل الواتساب، والأسعار",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Security & Biometrics Section (حماية التطبيق بالرمز والبيومتري)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حماية التطبيق والبيومتري (Security)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PIN Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل رمز الدخول (PIN Code)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "طلب إدخال رمز أمان للدخول إلى بيانات التطبيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setPinEnabled(enabled)
                                if (enabled) {
                                    showChangePinDialog = true
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                        )
                    }

                    if (isPinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رمز PIN الحالي مفعل",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )

                            OutlinedButton(
                                onClick = {
                                    newPinInput = ""
                                    showChangePinDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تغيير رمز PIN", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Biometric (Fingerprint / Face) Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الفتح بالبصمة أو الوجه (Biometric)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "استخدام البصمة لفتح التطبيق بسرعة عبر BiometricPrompt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { viewModel.setBiometricEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                            )
                        }
                    }
                }
            }
        }

        // 2. Office Profile & Messaging Templates (إعدادات اسم المكتب والرسائل الموجهة للعملاء)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "هوية المكتب وقوالب رسائل العملاء",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Office Name Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x20162033) else Color(0x15E2E8F0))
                            .clickable {
                                officeNameInput = officeName
                                showOfficeNameDialog = true
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "اسم المكتب / الاستوديو (الترويسة)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = officeName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) NeonCyan else Color(0xFF0284C7)
                            )
                        }

                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Office Name", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "قوالب رسائل الواتساب والـ SMS المجهزة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Template 1: Order Received
                    TemplateRowItem(
                        title = "1. إشعار استلام الطلب",
                        snippet = templateReceived,
                        onEdit = {
                            templateInputText = templateReceived
                            showTemplateDialog = "received"
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Template 2: Ready for Pickup
                    TemplateRowItem(
                        title = "2. إشعار جاهزية المطبوعات للتسليم",
                        snippet = templateReady,
                        onEdit = {
                            templateInputText = templateReady
                            showTemplateDialog = "ready"
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Template 3: Invoice & Balance
                    TemplateRowItem(
                        title = "3. تفاصيل الحساب والفاتورة",
                        snippet = templateInvoice,
                        onEdit = {
                            templateInputText = templateInvoice
                            showTemplateDialog = "invoice"
                        }
                    )
                }
            }
        }

        // 3. Financial KPI Summary Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المؤشرات المالية الإجمالية",
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
                                text = "إجمالي مبيعات الطلبات:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatMoney(summary.totalBillingsYer),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "المبالغ المحصلة (كاش):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatMoney(summary.totalCollectedYer),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldSuccess
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مستحقات معلقة لدى العملاء:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatMoney(summary.pendingClientBalancesYer),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) NeonCyan else Color(0xFF0284C7)
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ديون المطابع الوسيطة:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatMoney(summary.totalVendorDebtsYer),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Theme Preferences
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مظهر التطبيق (Theme & Day/Night Mode)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "التبديل بين الوضع الليلي والنهاري أو تتبع وضع النظام تلقائياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            title = "تلقائي النظام",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = themeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            title = "داكن (Night)",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            title = "نهاري (Day)",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Pricing & Exchange Rates
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إعدادات التسعير والعملات",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Default Design Fee row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x20162033) else Color(0x15E2E8F0))
                            .clickable { showFeeDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "رسوم التصميم الافتراضية",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تطبق عند إلغاء زر (التصميم جاهز)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewModel.formatMoney(defaultDesignFee),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonViolet
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // USD Exchange Rate row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x20162033) else Color(0x15E2E8F0))
                            .clickable { showRateDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سعر صرف الدولار (USD / YER)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "للتحويل الفوري بين الريال والدولار",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1$ = ${exchangeRate.toInt()} ر.ي",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldSuccess
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 6. Materials Pricing Catalog
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "قائمة أسعار الخامات والمطبوعات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تعديل سعر المتر المربع (م²) أو إضافة خامات",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddMaterialDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة خامة", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        materials.forEach { mat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x20162033) else Color(0x10000000))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mat.nameAr,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "التصنيف: ${mat.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${viewModel.formatMoney(mat.pricePerSqmYer)} / م²",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) NeonCyan else Color(0xFF0284C7)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { materialToEdit = mat },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal: Change PIN Dialog
    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("تعيين رمز الدخول (PIN)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل رمز أمان مكون من 4 أرقام:")
                    GlassTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = "رمز PIN (4 أرقام)",
                        placeholder = "مثال: 1234",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPinInput.length == 4) {
                        viewModel.setPinCode(newPinInput)
                        viewModel.setPinEnabled(true)
                        showChangePinDialog = false
                    }
                }) { Text("حفظ الرمز") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Edit Office Name Dialog
    if (showOfficeNameDialog) {
        AlertDialog(
            onDismissRequest = { showOfficeNameDialog = false },
            title = { Text("تعديل اسم المكتب / الاستوديو") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يستخدم هذا الاسم في ترويسة الرسائل الموجهة للعملاء عبر واتساب والـ SMS:")
                    GlassTextField(
                        value = officeNameInput,
                        onValueChange = { officeNameInput = it },
                        label = "اسم المكتب",
                        placeholder = "مثال: بكري برنت للدعاية والإعلان"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (officeNameInput.isNotBlank()) {
                        viewModel.setOfficeName(officeNameInput)
                        showOfficeNameDialog = false
                    }
                }) { Text("حفظ الاسم") }
            },
            dismissButton = {
                TextButton(onClick = { showOfficeNameDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Edit Template Dialog
    showTemplateDialog?.let { type ->
        val title = when (type) {
            "received" -> "تعديل قالب: إشعار استلام الطلب"
            "ready" -> "تعديل قالب: إشعار جاهزية المطبوعات"
            else -> "تعديل قالب: تفاصيل الحساب والفاتورة"
        }

        AlertDialog(
            onDismissRequest = { showTemplateDialog = null },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الكلمات المفتاحية المتاحة:\n{OFFICE} اسم المكتب، {CLIENT} العميل، {SERVICE} الخدمة، {DIMENSIONS} المقاسات، {TOTAL} الإجمالي، {BALANCE} المتبقي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    GlassTextField(
                        value = templateInputText,
                        onValueChange = { templateInputText = it },
                        label = "نص القالب",
                        singleLine = false,
                        modifier = Modifier.height(130.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    when (type) {
                        "received" -> viewModel.setTemplateReceived(templateInputText)
                        "ready" -> viewModel.setTemplateReady(templateInputText)
                        "invoice" -> viewModel.setTemplateInvoice(templateInputText)
                    }
                    showTemplateDialog = null
                }) { Text("حفظ القالب") }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = null }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Edit Design Fee
    if (showFeeDialog) {
        AlertDialog(
            onDismissRequest = { showFeeDialog = false },
            title = { Text("تعديل رسوم التصميم الافتراضية") },
            text = {
                GlassTextField(
                    value = editFeeInput,
                    onValueChange = { editFeeInput = it },
                    label = "رسوم التصميم (ر.ي)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val fee = editFeeInput.toDoubleOrNull() ?: 2000.0
                    viewModel.setDefaultDesignFee(fee)
                    showFeeDialog = false
                }) { Text("تحديث") }
            },
            dismissButton = {
                TextButton(onClick = { showFeeDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Edit Exchange Rate
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("تعديل سعر صرف الدولار") },
            text = {
                GlassTextField(
                    value = editRateInput,
                    onValueChange = { editRateInput = it },
                    label = "سعر الدولار مقابل الريال (YER)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val rate = editRateInput.toDoubleOrNull() ?: 530.0
                    viewModel.setUsdExchangeRate(rate)
                    showRateDialog = false
                }) { Text("تحديث") }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Add Material
    if (showAddMaterialDialog) {
        AlertDialog(
            onDismissRequest = { showAddMaterialDialog = false },
            title = { Text("إضافة خامة طباعة جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTextField(
                        value = newMatNameAr,
                        onValueChange = { newMatNameAr = it },
                        label = "اسم الخامة باللغة العربية",
                        placeholder = "مثال: استكر حراري للسيارات"
                    )
                    GlassTextField(
                        value = newMatPriceStr,
                        onValueChange = { newMatPriceStr = it },
                        label = "سعر المتر المربع (ر.ي)",
                        placeholder = "مثال: 3000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    GlassTextField(
                        value = newMatCategory,
                        onValueChange = { newMatCategory = it },
                        label = "التصنيف",
                        placeholder = "استكرات، بنرات، فلكس، لوحات..."
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val price = newMatPriceStr.toDoubleOrNull() ?: 0.0
                    if (newMatNameAr.isNotBlank() && price > 0) {
                        viewModel.addMaterial(newMatNameAr, price, newMatCategory.ifBlank { "عام" })
                        newMatNameAr = ""
                        newMatPriceStr = ""
                        newMatCategory = ""
                        showAddMaterialDialog = false
                    }
                }) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMaterialDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Modal: Edit Material Price
    materialToEdit?.let { mat ->
        var editPriceStr by remember { mutableStateOf(mat.pricePerSqmYer.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { materialToEdit = null },
            title = { Text("تعديل سعر خامة: ${mat.nameAr}") },
            text = {
                GlassTextField(
                    value = editPriceStr,
                    onValueChange = { editPriceStr = it },
                    label = "سعر المتر المربع (ر.ي)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val price = editPriceStr.toDoubleOrNull() ?: mat.pricePerSqmYer
                    viewModel.updateMaterial(mat.copy(pricePerSqmYer = price))
                    materialToEdit = null
                }) { Text("حفظ السعر") }
            },
            dismissButton = {
                TextButton(onClick = { materialToEdit = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun TemplateRowItem(
    title: String,
    snippet: String,
    onEdit: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) Color(0x20162033) else Color(0x10000000))
            .clickable { onEdit() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = snippet,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Template", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}
