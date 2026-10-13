package com.example.ui.screens.kanban

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStage
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.BakriViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun KanbanScreen(
    viewModel: BakriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val tasks by viewModel.tasks.collectAsState()

    var selectedStageFilter by remember { mutableStateOf<TaskStage?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // State for Add Task Dialog
    var taskTitleInput by remember { mutableStateOf("") }
    var taskClientInput by remember { mutableStateOf("") }
    var taskStageInput by remember { mutableStateOf(TaskStage.DESIGN) }
    var taskDeadlineMs by remember { mutableStateOf<Long?>(null) }

    // Motivational Peak-Hour Quotes for Graphic Designers
    val designerQuotes = remember {
        listOf(
            "💡 نصيحة المصمم: احرص دائماً على تحويل الخطوط إلى منحنيات (Convert to Curves / Outlines) قبل إرسال الملف لماكينة الطباعة لتجنب تشوه النصوص.",
            "🎨 إلهام ورشة العمل: الألوان بصيغة CMYK تضمن تطابق درجات ألوان الفلكس والبنر مع شاشتك بنسبة 98%.",
            "🚀 ذروة الإنتاج: التدقيق الإملائي السريع قبل القص النهائي يوفر خامات مطبوعة ومبالغ مالية!",
            "✨ سر الحرفة: إضافة هوامش قص (Bleed) بمقدار 2 سم حول التصميم يضمن تركيباً متقناً على هياكل الحديد والخشب."
        )
    }
    val currentQuote = remember { designerQuotes.random() }

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
                        text = "📋 لوحة الإنتاج والمهام (كانبان)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "متابعة مراحل التنفيذ (تصميم، طباعة، تجهيز، تسليم) وضبط منبهات المواعيد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddTaskDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                        contentColor = if (isDark) Color(0xFF00363A) else Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مهمة جديدة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Motivational Designer Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0x339333EA) else Color(0x207C3AED))
                    .border(1.dp, NeonViolet.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = currentQuote,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Stage Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    StageFilterChip(
                        label = "جميع المراحل (${tasks.size})",
                        isSelected = selectedStageFilter == null,
                        onClick = { selectedStageFilter = null }
                    )
                }
                items(TaskStage.values()) { stage ->
                    val count = tasks.count { it.stage == stage && !it.isCompleted }
                    StageFilterChip(
                        label = "${stage.labelAr} ($count)",
                        isSelected = selectedStageFilter == stage,
                        onClick = { selectedStageFilter = stage }
                    )
                }
            }
        }

        // Filtered tasks list
        val displayedTasks = tasks.filter { task ->
            selectedStageFilter == null || task.stage == selectedStageFilter
        }

        if (displayedTasks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد مهام في هذه المرحلة",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "جميع أوامر الطباعة والتصاميم مكتملة وجاهزة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedTasks, key = { it.id }) { task ->
                TaskCardItem(
                    task = task,
                    viewModel = viewModel,
                    onScheduleAlarm = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, year, month, day ->
                            TimePickerDialog(context, { _, hour, min ->
                                val targetCal = Calendar.getInstance().apply {
                                    set(year, month, day, hour, min, 0)
                                }
                                viewModel.scheduleTaskAlarm(context, task, targetCal.timeInMillis)
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Dialog: Add New Task
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = {
                Text(
                    text = "إضافة مهمة إنتاج جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassTextField(
                        value = taskTitleInput,
                        onValueChange = { taskTitleInput = it },
                        label = "عنوان المهمة / العمل",
                        placeholder = "مثال: تصميم شعار بنر عيادة الشفاء"
                    )
                    GlassTextField(
                        value = taskClientInput,
                        onValueChange = { taskClientInput = it },
                        label = "اسم العميل",
                        placeholder = "مثال: د. أحمد"
                    )

                    Text(
                        text = "مرحلة البداية:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskStage.values().forEach { stg ->
                            val isSel = taskStageInput == stg
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) (if (isDark) Color(0x3300F2FE) else Color(0x200284C7))
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) (if (isDark) NeonCyan else Color(0xFF0284C7)) else Color(0x30888888),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { taskStageInput = stg }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stg.labelAr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSel) (if (isDark) NeonCyan else Color(0xFF0284C7)) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTask(taskTitleInput, taskClientInput, taskStageInput, taskDeadlineMs)
                        taskTitleInput = ""
                        taskClientInput = ""
                        taskDeadlineMs = null
                        showAddTaskDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إضافة المهمة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TaskCardItem(
    task: TaskEntity,
    viewModel: BakriViewModel,
    onScheduleAlarm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val stageIcon = when (task.stage) {
        TaskStage.DESIGN -> Icons.Default.Brush
        TaskStage.PRINTING -> Icons.Default.Print
        TaskStage.FINISHING -> Icons.Default.PrecisionManufacturing
        TaskStage.DELIVERY -> Icons.Default.LocalShipping
    }

    GlassCard(cornerRadius = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x3300F2FE) else Color(0x200284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = stageIcon,
                        contentDescription = null,
                        tint = if (isDark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (task.clientName.isNotBlank()) {
                        Text(
                            text = "العميل: ${task.clientName} • المرحلة: ${task.stage.labelAr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Checkbox completed toggle
            IconButton(
                onClick = { viewModel.toggleTaskCompleted(task) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (task.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action controls: Move stage back/forward & Schedule Alarm
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Schedule Alarm Button
            OutlinedButton(
                onClick = onScheduleAlarm,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("منبه الموعد ⏰", style = MaterialTheme.typography.labelSmall)
            }

            // Stage Navigation Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val stages = TaskStage.values()
                val currentIndex = stages.indexOf(task.stage)

                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.updateTaskStage(task, stages[currentIndex - 1]) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("← ${stages[currentIndex - 1].labelAr}", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (currentIndex < stages.size - 1) {
                    Button(
                        onClick = { viewModel.updateTaskStage(task, stages[currentIndex + 1]) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) NeonCyan else Color(0xFF0284C7),
                            contentColor = if (isDark) Color(0xFF00363A) else Color.White
                        )
                    ) {
                        Text("${stages[currentIndex + 1].labelAr} →", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun StageFilterChip(
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
            .padding(horizontal = 12.dp, vertical = 7.dp),
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
