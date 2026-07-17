package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonTeal
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SubtleGray
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen navigation enum
enum class ActiveTab {
    DASHBOARD,
    AI_CHAT,
    TASKS,
    NOTES,
    HABITS,
    DOCUMENTS
}

fun getPriorityColor(priority: String): Color {
    return when (priority) {
        "URGENT" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFFBBF24)
        "LOW" -> Color(0xFF3B82F6)
        else -> Color.Gray
    }
}

fun getPriorityText(priority: String): String {
    return when (priority) {
        "URGENT" -> "قصوى"
        "HIGH" -> "عالية"
        "MEDIUM" -> "متوسطة"
        "LOW" -> "منخفضة"
        else -> "عادية"
    }
}

fun getTaskEmoji(title: String): String {
    val titleLower = title.lowercase()
    return when {
        titleLower.contains("غسيل") || titleLower.contains("سيارة") || titleLower.contains("سياره") -> "🚗"
        titleLower.contains("شراء") || titleLower.contains("تسوق") || titleLower.contains("متجر") || titleLower.contains("أشتري") -> "🛒"
        titleLower.contains("اجتماع") || titleLower.contains("رئيس") || titleLower.contains("عمل") || titleLower.contains("مقابلة") -> "💼"
        titleLower.contains("دراسة") || titleLower.contains("مذاكرة") || titleLower.contains("كتاب") || titleLower.contains("مشروع") -> "📚"
        titleLower.contains("رياضة") || titleLower.contains("جيم") || titleLower.contains("تمرين") || titleLower.contains("ركض") -> "🏋️"
        titleLower.contains("طبيب") || titleLower.contains("مستشفى") || titleLower.contains("دواء") || titleLower.contains("صحة") -> "🏥"
        titleLower.contains("سفر") || titleLower.contains("رحلة") || titleLower.contains("طائرة") -> "✈️"
        titleLower.contains("أكل") || titleLower.contains("طعام") || titleLower.contains("عشاء") || titleLower.contains("غداء") || titleLower.contains("فطور") -> "🍽️"
        titleLower.contains("برمجة") || titleLower.contains("تطبيق") || titleLower.contains("كود") || titleLower.contains("حاسوب") -> "💻"
        titleLower.contains("اتصال") || titleLower.contains("هاتف") || titleLower.contains("كلم") -> "📞"
        else -> "🫧"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var activeTab by remember { mutableStateOf(ActiveTab.DASHBOARD) }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NeonTeal, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Second Brain AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings Action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(ActiveTab.DASHBOARD, Icons.Default.Home, "الرئيسية"),
                    Triple(ActiveTab.AI_CHAT, Icons.Default.Chat, "المساعد الذكي"),
                    Triple(ActiveTab.TASKS, Icons.Default.CheckCircle, "المهام"),
                    Triple(ActiveTab.NOTES, Icons.Default.Edit, "الملاحظات"),
                    Triple(ActiveTab.HABITS, Icons.Default.Star, "العادات"),
                    Triple(ActiveTab.DOCUMENTS, Icons.Default.FileOpen, "المستندات")
                )

                tabs.forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonTeal,
                            selectedTextColor = NeonTeal,
                            unselectedIconColor = SubtleGray,
                            unselectedTextColor = SubtleGray,
                            indicatorColor = NeonTeal.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                ActiveTab.DASHBOARD -> DashboardScreen(viewModel)
                ActiveTab.AI_CHAT -> AiChatScreen(viewModel)
                ActiveTab.TASKS -> TaskScreen(viewModel)
                ActiveTab.NOTES -> NotesScreen(viewModel)
                ActiveTab.HABITS -> HabitsScreen(viewModel)
                ActiveTab.DOCUMENTS -> DocumentsScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرحباً بك، مصطفى 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مساعدك الشخصي جاهز لمساعدتك في تنظيم أفكارك ويومك.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SubtleGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonTeal.copy(alpha = 0.4f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧠", fontSize = 32.sp)
                    }
                }
            }
        }

        // Pomodoro Focus Timer Card
        item {
            PomodoroWidget(viewModel)
        }

        // Floating Task Bubbles Card
        item {
            FloatingTaskBubbles(viewModel = viewModel, tasks = tasks)
        }

        // Quick Stats / Highlights Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "العادات النشطة",
                    value = "${habits.size}",
                    emoji = "⚡",
                    color = PurpleAccent
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "مشاريعك",
                    value = "${projects.size}",
                    emoji = "💼",
                    color = NeonBlue
                )
            }
        }

        // Section Title
        item {
            Text(
                text = "المهام الهامة العاجلة 📌",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val todoTasks = tasks.filter { it.status != "DONE" }
        if (todoTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد مهام نشطة حالياً. كل شيء منجز! 🎉",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SubtleGray
                        )
                    }
                }
            }
        } else {
            items(todoTasks.take(3)) { task ->
                TaskRowItem(task, onStatusChange = { newStatus ->
                    viewModel.updateTaskStatus(task, newStatus)
                }, onDelete = {
                    viewModel.deleteTask(task)
                })
            }
        }
    }
}

@Composable
fun PomodoroWidget(viewModel: MainViewModel) {
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val completedSessions by viewModel.pomodoroSessionsCompleted.collectAsStateWithLifecycle()

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val progress = secondsLeft.toFloat() / (25 * 60)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مؤقت التركيز (Pomodoro) ⏱️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = NeonBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "جلسات: $completedSessions",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Clock and Timer representation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.dp,
                    color = NeonBlue,
                    trackColor = NeonBlue.copy(alpha = 0.1f)
                )
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.togglePomodoro() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFEF4444) else NeonBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRunning) "إيقاف مؤقت ⏸️" else "ابدأ التركيز ▶️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.resetPomodoro() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SubtleGray)
                ) {
                    Text(text = "إعادة ضبط 🔄", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun FloatingTaskBubbles(
    viewModel: MainViewModel,
    tasks: List<Task>
) {
    val activeTasks = tasks.filter { it.status != "DONE" }.take(6)
    var selectedTaskForPopup by remember { mutableStateOf<Task?>(null) }
    var burstingTaskId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "فقاعات المهام النشطة 🫧",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (activeTasks.isNotEmpty()) {
                Text(
                    text = "اضغط لتفجيرها! 💥",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeTasks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(NeonTeal.copy(alpha = 0.3f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧠", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "عقلك الثاني صافٍ تماماً! 🫧",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "لا توجد مهام نشطة حالياً. أضف مهمة جديدة لتطفو هنا.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleGray
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeTasks.forEachIndexed { index, task ->
                    val duration = 1200 + (index * 250)
                    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float_$index")
                    val floatY by infiniteTransition.animateFloat(
                        initialValue = -5f,
                        targetValue = 5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "y_offset"
                    )
                    
                    val isBursting = burstingTaskId == task.id
                    val scale by animateFloatAsState(
                        targetValue = if (isBursting) 0f else 1f,
                        animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing),
                        label = "burst_scale"
                    )

                    if (scale > 0.01f) {
                        val emoji = getTaskEmoji(task.title)
                        val priorityColor = getPriorityColor(task.priority)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = floatY
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = scale
                                }
                                .clickable { selectedTaskForPopup = task }
                                .width(72.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                priorityColor.copy(alpha = 0.35f),
                                                priorityColor.copy(alpha = 0.05f)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            1.5.dp,
                                            Brush.linearGradient(
                                                listOf(priorityColor, Color.White.copy(alpha = 0.4f))
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.25f),
                                        startAngle = 180f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        size = this.size * 0.85f,
                                        topLeft = androidx.compose.ui.geometry.Offset(this.size.width * 0.08f, this.size.height * 0.08f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 2.dp.toPx(),
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                    )
                                }

                                Text(
                                    text = emoji,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    selectedTaskForPopup?.let { task ->
        val emoji = getTaskEmoji(task.title)
        val priorityColor = getPriorityColor(task.priority)

        AlertDialog(
            onDismissRequest = { selectedTaskForPopup = null },
            icon = { Text(emoji, fontSize = 40.sp) },
            title = {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.description.ifBlank { "لا يوجد وصف لهذه المهمة." },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "الأولوية: ${getPriorityText(task.priority)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        burstingTaskId = task.id
                        scope.launch {
                            delay(320)
                            viewModel.updateTaskStatus(task, "DONE")
                            selectedTaskForPopup = null
                            burstingTaskId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إنجاز وتفجير الفقاعة! 🫧💥", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTaskForPopup = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إبقاء المهمة تطفو 🎈")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    emoji: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = SubtleGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }
    }
}

// ==========================================
// 2. AI CHAT SCREEN
// ==========================================
@Composable
fun AiChatScreen(viewModel: MainViewModel) {
    val chats by viewModel.allChats.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chats.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المساعد الذكي (Second Brain AI) ✨",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.clearChats() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Chat",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Bubble List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🧠", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "مرحباً! أنا عقلك الثاني الذكي.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اسألني أي شيء، سأساعدك في كتابة بريد، تنظيم مهامك، أو تلخيص أفكارك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chats) { chat ->
                        val isUser = chat.role == "USER"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) NeonTeal else MaterialTheme.colorScheme.surface
                                ),
                                border = if (!isUser) BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f)) else null,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = chat.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("اكتب رسالة...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            FloatingActionButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendChatMessage(inputMessage)
                        inputMessage = ""
                    }
                },
                containerColor = NeonTeal,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

// ==========================================
// 3. TASKS SCREEN
// ==========================================
@Composable
fun TaskScreen(viewModel: MainViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("MEDIUM") }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "قائمة المهام والمشاريع 📋",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddProjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ مشروع", color = Color.White)
                }
                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ مهمة", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Projects scrolling layout
        if (projects.isNotEmpty()) {
            Text(
                text = "مشاريعك النشطة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                projects.forEach { project ->
                    Card(
                        modifier = Modifier.width(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { viewModel.deleteProject(project) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = project.description.ifBlank { "لا يوجد وصف." },
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = project.progress,
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonBlue
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tasks List
        Text(
            text = "قائمة المهام",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeonTeal
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مهام حالياً. أضف مهمة للبدء!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtleGray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskRowItem(task, onStatusChange = { newStatus ->
                        viewModel.updateTaskStatus(task, newStatus)
                    }, onDelete = {
                        viewModel.deleteTask(task)
                    })
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("إضافة مهمة جديدة ✨") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("الوصف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Priority selector
                    Text("مستوى الأهمية:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("URGENT" to "عاجل جداً", "HIGH" to "عالي", "MEDIUM" to "متوسط", "LOW" to "منخفض").forEach { (valStr, text) ->
                            val selected = taskPriority == valStr
                            FilterChip(
                                selected = selected,
                                onClick = { taskPriority = valStr },
                                label = { Text(text) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(taskTitle, taskDesc, taskPriority)
                            taskTitle = ""
                            taskDesc = ""
                            taskPriority = "MEDIUM"
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Text("إضافة المهمة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            title = { Text("إضافة مشروع جديد 💼") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("اسم المشروع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projectDesc,
                        onValueChange = { projectDesc = it },
                        label = { Text("وصف المشروع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectName.isNotBlank()) {
                            viewModel.addProject(projectName, projectDesc, 0.2f)
                            projectName = ""
                            projectDesc = ""
                            showAddProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("إضافة المشروع", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProjectDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun TaskRowItem(
    task: Task,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == "DONE"
    val priorityColor = getPriorityColor(task.priority)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { checked ->
                    onStatusChange(if (checked) "DONE" else "TODO")
                },
                colors = CheckboxDefaults.colors(checkedColor = NeonTeal)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) SubtleGray else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Priority Indicator Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = getPriorityText(task.priority),
                    color = priorityColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==========================================
// 4. NOTES & IDEAS SCREEN
// ==========================================
@Composable
fun NotesScreen(viewModel: MainViewModel) {
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val ideas by viewModel.allIdeas.collectAsStateWithLifecycle()
    var currentTabIsNotes by remember { mutableStateOf(true) }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteCategory by remember { mutableStateOf("عام") }

    var showAddIdeaDialog by remember { mutableStateOf(false) }
    var ideaTitle by remember { mutableStateOf("") }
    var ideaDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentTabIsNotes) "الملاحظات الشخصية 📝" else "بنك الأفكار الذكي 💡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (currentTabIsNotes) {
                            showAddNoteDialog = true
                        } else {
                            showAddIdeaDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (currentTabIsNotes) "+ ملاحظة" else "+ فكرة", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tab
        TabRow(
            selectedTabIndex = if (currentTabIsNotes) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = NeonTeal
        ) {
            Tab(
                selected = currentTabIsNotes,
                onClick = { currentTabIsNotes = true },
                text = { Text("ملاحظات") }
            )
            Tab(
                selected = !currentTabIsNotes,
                onClick = { currentTabIsNotes = false },
                text = { Text("الأفكار والذكاء") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentTabIsNotes) {
            // Notes list
            if (notes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("لا توجد ملاحظات مضافة بعد. 📝", color = SubtleGray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleGray,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NeonTeal.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = note.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonTeal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Ideas List
            if (ideas.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("شارك أفكارك الأولى لتطويرها بالذكاء الاصطناعي! 💡", color = SubtleGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ideas) { idea ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = idea.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.deleteIdea(idea) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete idea",
                                            tint = Color.Red.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Text(
                                    text = idea.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SubtleGray
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                if (idea.aiSuggestions.isNotBlank()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = PurpleAccent.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "AI suggestions",
                                                    tint = PurpleAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "تطوير الفكرة بالذكاء الاصطناعي:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PurpleAccent
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = idea.aiSuggestions,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.generateAiIdeaSuggestions(idea) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Develop",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تطوير الفكرة بالذكاء الاصطناعي ✨", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("كتابة ملاحظة جديدة 📝") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("المحتوى") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = noteCategory,
                        onValueChange = { noteCategory = it },
                        label = { Text("التصنيف (مثال: دراسة، عمل، رياضة)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteContent, noteCategory)
                            noteTitle = ""
                            noteContent = ""
                            noteCategory = "عام"
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Text("حفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Idea Dialog
    if (showAddIdeaDialog) {
        AlertDialog(
            onDismissRequest = { showAddIdeaDialog = false },
            title = { Text("حفظ فكرة مبتكرة 💡") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ideaTitle,
                        onValueChange = { ideaTitle = it },
                        label = { Text("عنوان الفكرة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ideaDesc,
                        onValueChange = { ideaDesc = it },
                        label = { Text("شرح الفكرة") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ideaTitle.isNotBlank()) {
                            viewModel.addIdea(ideaTitle, ideaDesc, "عام")
                            ideaTitle = ""
                            ideaDesc = ""
                            showAddIdeaDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Text("حفظ الفكرة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddIdeaDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// ==========================================
// 5. HABITS & GOALS SCREEN
// ==========================================
@Composable
fun HabitsScreen(viewModel: MainViewModel) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitName by remember { mutableStateOf("") }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalName by remember { mutableStateOf("") }
    var goalPeriod by remember { mutableStateOf("DAILY") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "العادات والأهداف اليومية ⚡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ هدف", color = Color.White)
                }
                Button(
                    onClick = { showAddHabitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ عادة", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goals Tracker
        if (goals.isNotEmpty()) {
            Text(
                text = "الأهداف الحالية",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                goals.forEach { goal ->
                    Card(
                        modifier = Modifier.width(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { viewModel.deleteGoal(goal) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "المدى: ${goal.period}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = goal.progress,
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonBlue
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Habits List
        Text(
            text = "قائمتك لمتابعة العادات التكرارية",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeonTeal
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم تتم إضافة أي عادات بعد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtleGray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits) { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "معدل الالتزام: ${(habit.complianceRate * 100).toInt()}% • تكرار: ${habit.frequency}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleGray
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NeonTeal.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "سلسلة: 🔥 ${habit.streak}",
                                        color = NeonTeal,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.checkInHabit(habit) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NeonTeal, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Complete habit",
                                        tint = Color.Black
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete habit",
                                        tint = Color.Red.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = { Text("إضافة عادة جديدة ⚡") },
            text = {
                Column {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("اسم العادة (مثال: ركض 3 كم 🏃‍♂️)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            viewModel.addHabit(habitName, "DAILY")
                            habitName = ""
                            showAddHabitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Text("إضافة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("إضافة هدف طموح 🎯") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("ما هو هدفك؟") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("الفترة الزمنية للهدف:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY" to "يومي", "WEEKLY" to "أسبوعي", "MONTHLY" to "شهري").forEach { (valStr, text) ->
                            val selected = goalPeriod == valStr
                            FilterChip(
                                selected = selected,
                                onClick = { goalPeriod = valStr },
                                label = { Text(text) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalName.isNotBlank()) {
                            viewModel.addGoal(goalName, goalPeriod, 100f)
                            goalName = ""
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("إضافة الهدف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// ==========================================
// 6. DOCUMENTS & OCR SCREEN
// ==========================================
@Composable
fun DocumentsScreen(viewModel: MainViewModel) {
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    var selectedDocForSummary by remember { mutableStateOf<Document?>(null) }
    var uploadDocName by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ملخصات الذكاء الاصطناعي والمستندات 📂",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showUploadDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ رفع مستند", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📄", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لم يتم رفع أي مستندات أو PDF بعد.", color = SubtleGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📄", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = doc.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "النوع: ${doc.mimeType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SubtleGray
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { selectedDocForSummary = doc },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("قراءة التلخيص", color = Color.White)
                                }

                                IconButton(onClick = { viewModel.deleteDocument(doc) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete document",
                                        tint = Color.Red.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload Document Dialog
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("محاكاة رفع مستند PDF / Word 📂") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uploadDocName,
                        onValueChange = { uploadDocName = it },
                        label = { Text("اسم الملف (مثال: بحث الذكاء الاصطناعي.pdf)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadDocName.isNotBlank()) {
                            viewModel.addDocument(uploadDocName, "application/pdf")
                            uploadDocName = ""
                            showUploadDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Text("رفع وتلخيص", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Summary Result Dialog
    selectedDocForSummary?.let { doc ->
        AlertDialog(
            onDismissRequest = { selectedDocForSummary = null },
            icon = { Text("📄", fontSize = 40.sp) },
            title = { Text(text = "ملخص مستند: ${doc.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = doc.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider()
                    Text(
                        text = "تم التلخيص وتحليل المستند تلقائياً بالكامل بواسطة المساعد الذكي Second Brain AI ✨",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDocForSummary = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("فهمت 🤝", color = Color.White)
                }
            }
        )
    }
}
