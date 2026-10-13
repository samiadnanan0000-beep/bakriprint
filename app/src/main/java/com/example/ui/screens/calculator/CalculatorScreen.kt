package com.example.ui.screens.calculator

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.BakriViewModel
import java.util.Locale

@Composable
fun CalculatorScreen(
    viewModel: BakriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val widthStr by viewModel.calcWidth.collectAsState()
    val heightStr by viewModel.calcHeight.collectAsState()
    val qtyStr by viewModel.calcQuantity.collectAsState()
    val selectedMaterial by viewModel.calcSelectedMaterial.collectAsState()
    val isDesignReady by viewModel.calcIsDesignReady.collectAsState()
    val discountStr by viewModel.calcDiscount.collectAsState()
    val depositStr by viewModel.calcDeposit.collectAsState()
    val defaultDesignFee by viewModel.defaultDesignFee.collectAsState()
    val materials by viewModel.materials.collectAsState()

    // Active material fallback
    val activeMaterial = selectedMaterial ?: materials.firstOrNull()
    val result = viewModel.calculateCurrentOrderValues()

    var showSaveDialog by remember { mutableStateOf(false) }
    var clientNameInput by remember { mutableStateOf("") }
    var clientPhoneInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Title & Description
            Text(
                text = "📐 حاسبة المقاسات والطباعة الرقمية",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "حساب دقيق للمساحات بالمتر المربع (م²) والتكلفة الإجمالية فورياً",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Dimensions inputs
        item {
            GlassCard {
                Text(
                    text = "أبعاد اللوحة والكمية",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = widthStr,
                        onValueChange = { viewModel.calcWidth.value = it },
                        label = "العرض (م)",
                        placeholder = "3.0",
                        trailingText = "متر",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    GlassTextField(
                        value = heightStr,
                        onValueChange = { viewModel.calcHeight.value = it },
                        label = "الارتفاع (م)",
                        placeholder = "1.2",
                        trailingText = "متر",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    GlassTextField(
                        value = qtyStr,
                        onValueChange = { viewModel.calcQuantity.value = it },
                        label = "الكمية",
                        placeholder = "1",
                        trailingText = "قطع",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Area Pill Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                        .border(
                            1.dp,
                            if (isDark) Color(0x6600F2FE) else Color(0x400284C7),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "إجمالي المساحة بالمتر المربع:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f م²", result.totalAreaSqm),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) NeonCyan else Color(0xFF0284C7)
                            )
                        )
                    }
                }
            }
        }

        // 2. Material selector
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اختر خامة الطباعة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${materials.size} خامات متاحة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(materials) { mat ->
                        val isSelected = activeMaterial?.id == mat.id
                        val borderColor = if (isSelected) {
                            if (isDark) NeonCyan else Color(0xFF0284C7)
                        } else Color.Transparent

                        val bgCardColor = if (isSelected) {
                            if (isDark) Color(0x3300F2FE) else Color(0x260284C7)
                        } else {
                            if (isDark) Color(0x20162033) else Color(0x15E2E8F0)
                        }

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgCardColor)
                                .border(1.2.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable { viewModel.selectMaterial(mat) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isSelected) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mat.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mat.nameAr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${viewModel.formatMoney(mat.pricePerSqmYer)} / م²",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) NeonCyan else Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }

        // 3. Design Ready Switch & Financial adjustments
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDesignReady) Icons.Default.CheckCircle else Icons.Default.Brush,
                                contentDescription = null,
                                tint = if (isDesignReady) EmeraldSuccess else NeonViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDesignReady) "التصميم جاهز للطباعة" else "بحاجة لتصميم وتجهيز",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isDesignReady) "لا توجد رسوم تصميم مضافة (0 ر.ي)" else "إضافة رسوم التصميم تلقائياً (${viewModel.formatMoney(defaultDesignFee)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isDesignReady,
                        onCheckedChange = { viewModel.toggleDesignReady(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldSuccess,
                            checkedTrackColor = Color(0x3310B981),
                            uncheckedThumbColor = NeonViolet,
                            uncheckedTrackColor = Color(0x339333EA)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Discount and Paid Deposit inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = discountStr,
                        onValueChange = { viewModel.calcDiscount.value = it },
                        label = "خصم خاص",
                        placeholder = "0",
                        trailingText = "ر.ي",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    GlassTextField(
                        value = depositStr,
                        onValueChange = { viewModel.calcDeposit.value = it },
                        label = "العربون المدفوع",
                        placeholder = "0",
                        trailingText = "ر.ي",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Live Financial Breakdown & Result Summary
        item {
            GlassCard(
                cornerRadius = 24.dp,
                elevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل الفاتورة والحساب",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "حساب فوري",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) NeonCyan else Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Line items
                BillRow("تكلفة الطباعة (${result.materialName}):", viewModel.formatMoney(result.totalAreaSqm * result.unitPriceYer))
                BillRow("رسوم التصميم والتجهيز:", viewModel.formatMoney(result.designFeeYer), isHighlight = !result.isDesignReady)
                if (result.discountYer > 0) {
                    BillRow("الخصم الممنوح:", "- ${viewModel.formatMoney(result.discountYer)}", color = EmeraldSuccess)
                }
                BillRow("الإجمالي المطلوب:", viewModel.formatMoney(result.totalDueYer), isBold = true)
                BillRow("العربون المدفوع:", viewModel.formatMoney(result.paidDepositYer), color = EmeraldSuccess)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Remaining Balance Highlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المتبقي عند الاستلام:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = viewModel.formatMoney(result.remainingBalanceYer),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (result.remainingBalanceYer > 0) (if (isDark) NeonCyan else Color(0xFF0284C7)) else EmeraldSuccess
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Buttons: Save as Client Order & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                            contentColor = if (isDark) Color(0xFF00363A) else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ كطلب عميل", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = {
                            val invoiceSummary = """
                                📐 بكري برنت - تفاصيل التسعيرة:
                                • الخامة: ${result.materialName}
                                • المقاس: ${result.widthM} × ${result.heightM} م (${result.quantity} قطع)
                                • المساحة: ${String.format(Locale.US, "%.2f", result.totalAreaSqm)} م²
                                • حالة التصميم: ${if (result.isDesignReady) "جاهز للطباعة" else "يشمل تصميم"}
                                • الإجمالي: ${viewModel.formatMoney(result.totalDueYer)}
                                • المتبقي: ${viewModel.formatMoney(result.remainingBalanceYer)}
                            """.trimIndent()
                            viewModel.sendWhatsApp(context, "", invoiceSummary)
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة واتساب", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }

    // Modal Dialog: Save as Client Order
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "تسجيل طلب عميل جديد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "الخدمة: ${result.materialName} (${result.widthM}×${result.heightM}م)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    GlassTextField(
                        value = clientNameInput,
                        onValueChange = { clientNameInput = it },
                        label = "اسم العميل / الشركة",
                        placeholder = "مثال: محلات النور التجارية",
                        leadingIcon = Icons.Default.Person
                    )

                    GlassTextField(
                        value = clientPhoneInput,
                        onValueChange = { clientPhoneInput = it },
                        label = "رقم هاتف العميل (للتنبيهات)",
                        placeholder = "77XXXXXXX أو 73XXXXXXX",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    GlassTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = "ملاحظات التشطيب والتجهيز",
                        placeholder = "مثال: تخريم زوايا، لحام أطراف، لميع",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createOrderFromCalculator(
                            clientName = clientNameInput,
                            clientPhone = clientPhoneInput,
                            notes = notesInput
                        )
                        showSaveDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ وبدء الإنتاج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun BillRow(
    title: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (color != Color.Unspecified) color
            else if (isHighlight) NeonViolet
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
