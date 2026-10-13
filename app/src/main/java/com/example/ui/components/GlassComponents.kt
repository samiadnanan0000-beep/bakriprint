package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyMode
import com.example.data.model.OrderStatus
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ChipAmberBg
import com.example.ui.theme.ChipCyanBg
import com.example.ui.theme.ChipGreenBg
import com.example.ui.theme.ChipVioletBg
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.AppTab

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.DefaultCorner,
    elevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .liquidGlass(cornerRadius = cornerRadius, isDark = isDark, elevation = elevation)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassTopBar(
    activeTab: AppTab,
    themeMode: AppThemeMode,
    currencyMode: CurrencyMode,
    onToggleTheme: () -> Unit,
    onToggleCurrency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(cornerRadius = 20.dp, isDark = isDark, elevation = 8.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Tagline
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassTokens.glowBrush()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Bakri Print Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "بكري برنت",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "استوديو الإعلان والديكور • ${activeTab.labelAr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action controls: Currency toggle & Theme toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Currency Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                    .border(
                        1.dp,
                        if (isDark) Color(0x6600F2FE) else Color(0x400284C7),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleCurrency() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalAtm,
                        contentDescription = "Currency",
                        tint = if (isDark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currencyMode.symbolAr,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) NeonCyan else Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Theme Switcher Button
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x26FFFFFF) else Color(0x15000000))
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = if (isDark) Color(0xFFFFD54F) else Color(0xFF1E293B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GlassBottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val tabs = listOf(
        Triple(AppTab.CALCULATOR, "الحاسبة", Icons.Default.Calculate),
        Triple(AppTab.ORDERS, "الطلبات", Icons.Default.ReceiptLong),
        Triple(AppTab.VENDORS, "المطابع", Icons.Default.Print),
        Triple(AppTab.KANBAN, "المهام", Icons.Default.ViewKanban),
        Triple(AppTab.SETTINGS, "الإعدادات", Icons.Default.Settings)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(cornerRadius = 24.dp, isDark = isDark, elevation = 12.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (tab, title, icon) ->
            val isSelected = tab == selectedTab
            val activeColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (isDark) NeonCyan else Color(0xFF0284C7)
                } else {
                    if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                },
                label = "tab_color"
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTabSelected(tab) }
                    .background(
                        if (isSelected) {
                            if (isDark) Color(0x2600F2FE) else Color(0x1F0284C7)
                        } else Color.Transparent
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = activeColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = activeColor
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        },
        trailingIcon = trailingText?.let {
            { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp)) }
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (isDark) Color(0x40162033) else Color(0x30E2E8F0),
            unfocusedContainerColor = if (isDark) Color(0x20162033) else Color(0x15E2E8F0),
            focusedBorderColor = if (isDark) NeonCyan else Color(0xFF0284C7),
            unfocusedBorderColor = if (isDark) Color(0x3394A3B8) else Color(0x40CBD5E1),
            focusedLabelColor = if (isDark) NeonCyan else Color(0xFF0284C7),
            cursorColor = if (isDark) NeonCyan else Color(0xFF0284C7)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Pair(ChipAmberBg, Color(0xFFF59E0B))
        OrderStatus.IN_PRINTING -> Pair(ChipVioletBg, NeonViolet)
        OrderStatus.READY -> Pair(ChipCyanBg, NeonCyan)
        OrderStatus.COMPLETED -> Pair(ChipGreenBg, EmeraldSuccess)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.labelAr,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}
