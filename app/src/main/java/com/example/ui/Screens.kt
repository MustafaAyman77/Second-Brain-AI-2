package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.CosmicGradientEnd
import com.example.ui.theme.CosmicGradientStart
import com.example.ui.theme.NeonTeal
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.VoiceProcessingState
import com.example.viewmodel.ProposedAction
import com.example.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Navigation Destinations
enum class Screen(val title: String, val icon: ImageVector) {
    Dashboard("الرئيسية", Icons.Default.Dashboard),
    Chat("المساعد الذكي", Icons.Default.Chat),
    Tasks("المهام والمشاريع", Icons.Default.CheckCircle),
    Notes("الملاحظات الذكية", Icons.Default.Book),
    Calendar("التقويم الذكي", Icons.Default.DateRange),
    Documents("المستندات و OCR", Icons.Default.Description),
    Habits("التركيز والعادات", Icons.Default.Timer),
    EmailAssistant("مساعد البريد", Icons.Default.Email),
    Memory("الذاكرة الذكية", Icons.Default.Psychology),
    Settings("الإعدادات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primaryContainer, CosmicGradientEnd)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, NeonTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧠",
                                fontSize = 32.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.userName.collectAsState().value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = viewModel.userBio.collectAsState().value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = if (viewModel.userPlan.collectAsState().value == "Premium") "عضوية بريميوم ✦" else "العضوية المجانية",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Drawer items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(Screen.values()) { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentScreen.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة")
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.Settings }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "الملف الشخصي")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(Screen.Dashboard, Screen.Chat, Screen.Tasks, Screen.Notes, Screen.Calendar)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.background, CosmicGradientEnd)
                        )
                    )
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "screen_transition"
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.Dashboard -> DashboardScreen(viewModel, onNavigate = { currentScreen = it })
                        Screen.Chat -> ChatScreen(viewModel)
                        Screen.Tasks -> TasksScreen(viewModel)
                        Screen.Notes -> NotesScreen(viewModel)
                        Screen.Calendar -> CalendarScreen(viewModel)
                        Screen.Documents -> DocumentsScreen(viewModel)
                        Screen.Habits -> HabitsFocusScreen(viewModel)
                        Screen.EmailAssistant -> EmailAssistantScreen(viewModel)
                        Screen.Memory -> MemoryScreen(viewModel)
                        Screen.Settings -> SettingsScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: MainViewModel, onNavigate: (Screen) -> Unit) {
    val userName by viewModel.userName.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val completedTasks = tasks.count { it.status == "DONE" }
    val totalTasks = tasks.size
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    val upcomingEvents = calendarEvents.filter { it.startTime > System.currentTimeMillis() }.take(2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CosmicGradientStart, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧠",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "مرحباً بك، $userName",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "مستعد لتنظيم عقولنا وأفكارنا اليوم؟",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Indicator
                        Text(
                            text = "إنجاز المهام اليومية: $completedTasks/$totalTasks",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = NeonTeal,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Floating Task Bubbles Section
        item {
            FloatingTaskBubbles(viewModel = viewModel, tasks = tasks, onNavigate = onNavigate)
        }

        // Action Buttons Row (Quick Features)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "مساعد البريد",
                    icon = Icons.Default.Email,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.EmailAssistant) }
                )
                DashboardActionCard(
                    title = "تلخيص و OCR",
                    icon = Icons.Default.Description,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Documents) }
                )
                DashboardActionCard(
                    title = "العادات والتركيز",
                    icon = Icons.Default.Timer,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Habits) }
                )
            }
        }

        // Voice Command Quick Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سجل مهمة بصوتك 🎙️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اضغط للمحاكاة الصوتية وتحويل صوتك لمهمة تلقائياً بذكاء.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Button(
                        onClick = { onNavigate(Screen.Chat) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("افتح الدردشة")
                    }
                }
            }
        }

        // Daily Top Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المهام الأكثر أهمية ⭐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(Screen.Tasks) }) {
                    Text("عرض الكل")
                }
            }
        }

        val priorityTasks = tasks.filter { it.priority == "URGENT" || it.priority == "HIGH" }.take(3)
        if (priorityTasks.isEmpty()) {
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "لا توجد مهام عاجلة متبقية! أنت منظم بامتياز.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        } else {
            items(priorityTasks) { task ->
                TaskRowItem(task, onStatusChanged = { viewModel.updateTaskStatus(task, it) })
            }
        }

        // Calendar Highlights
        item {
            Text(
                text = "التقويم والمواعيد القادمة 🗓️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (upcomingEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "لا توجد فعاليات قادمة مبرمجة في التقويم حالياً.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(upcomingEvents) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🕒", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "موعد: ${SimpleDateFormat("EEEE h:mm a", Locale("ar")).format(Date(event.startTime))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionCard(title: String, icon: ImageVector, backgroundColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
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

@Composable
fun FloatingTaskBubbles(
    viewModel: MainViewModel,
    tasks: List<Task>,
    onNavigate: (Screen) -> Unit
) {
    val activeTasks = tasks.filter { it.status != "DONE" }.take(6)
    var selectedTaskForPopup by remember { mutableStateOf<Task?>(null) }
    var burstingTaskId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
            // Serene "Clear Mind" Bubble when there are no tasks
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
                            text = "لا توجد مهام نشطة حالياً. استمتع بيومك أو أضف مهمة جديدة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                    // Independent organic floating bounce anims
                    val duration = 1200 + (index * 250) // 1200ms, 1450ms, 1700ms, etc.
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
                            // The glossy physical Bubble!
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
                                // Glossy reflection arc/crescent
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

    // Expanded Bubble Details Dialog
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

                    // Priority Badge
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
                            kotlinx.coroutines.delay(320) // Let pop animation finish
                            viewModel.updateTaskStatus(task, "DONE")
                            selectedTaskForPopup = null
                            burstingTaskId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إنجاز وتفجير الفقاعة! 🫧💥", fontWeight = FontWeight.Bold)
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

// ==========================================
// 2. AI CHAT SCREEN & INTENT ACTION CARDS
// ==========================================
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val sessions by viewModel.chatSessions.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val proposedAction by viewModel.proposedAction.collectAsState()
    val voiceState by viewModel.voiceProcessingState.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Mock Voice Simulation overlay / state
    var showVoiceMockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Sessions Quick Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.createNewChatSession() },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "محادثة جديدة", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("دردشة جديدة", fontSize = 11.sp)
            }

            sessions.forEach { session ->
                val isSelected = session.id == viewModel.currentSessionId.collectAsState().value
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectChatSession(session.id) },
                    label = { Text(session.title, fontSize = 11.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "مساعد عقولنا الذكي ⚡",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اكتب لي ما ترغب به (مثل: 'أضف مهمة تسوق ملابس غدا الساعة 4' أو 'احفظ ملاحظة بأني أحب تناول الشاي الأخضر صباحا' لتحديث ذاكرتي). سأقوم بالتحليل الأوتوماتيكي والحل بلمح البصر!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (msg.content.startsWith("[VOICE:")) {
                                VoiceRecordPlayBubble(content = msg.content, isUser = isUser)
                            } else {
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("المساعد يفكر ويكتب...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Proposed Action Card inside chat list if available!
            if (proposedAction != null) {
                item {
                    ProposedActionCard(
                        action = proposedAction!!,
                        onConfirm = { viewModel.executeProposedAction() },
                        onDismiss = { viewModel.dismissProposedAction() }
                    )
                }
            }

            // Voice Simulation Card
            when (voiceState) {
                is VoiceProcessingState.Idle -> {}
                else -> {
                    item {
                        VoiceProcessingCard(
                            state = voiceState,
                            onConfirm = { viewModel.confirmVoiceTask(it) },
                            onCancel = { viewModel.cancelVoiceTask() }
                        )
                    }
                }
            }
        }

        // Chat Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice simulation trigger
                IconButton(
                    onClick = { showVoiceMockDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "أمر صوتي", tint = MaterialTheme.colorScheme.secondary)
                }

                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("اكتب رسالة أو أمر هنا...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("chat_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White)
                }
            }
        }
    }

    // Voice Simulation Dialog
    if (showVoiceMockDialog) {
        var mockPhraseInput by remember { mutableStateOf("") }
        var isRecordingVoice by remember { mutableStateOf(false) }
        var recordingTimeSeconds by remember { mutableStateOf(0) }
        var barHeights by remember { mutableStateOf(listOf(10, 15, 8, 20, 12, 16, 10, 24, 14, 18)) }

        val prepopulatedPhrases = listOf(
            "سجل مهمة غسيل السيارة غدا الساعة 4",
            "أضف اجتماع مع رئيس العمل غدا الساعة 9 صباحا بأولوية قصوى",
            "ذكرني أشتري شاي أخضر وزنجبيل من المتجر الليلة",
            "احفظ ملاحظة دراسية هامة بأن الموعد الأخير للمشروع الأسبوع القادم"
        )

        // Live voice recording waveform simulation
        if (isRecordingVoice) {
            LaunchedEffect(Unit) {
                while (isRecordingVoice) {
                    barHeights = barHeights.map { (6..28).random() }
                    kotlinx.coroutines.delay(120)
                }
            }
            LaunchedEffect(Unit) {
                while (isRecordingVoice) {
                    kotlinx.coroutines.delay(1000)
                    recordingTimeSeconds += 1
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showVoiceMockDialog = false },
            title = { 
                Text(
                    text = if (isRecordingVoice) "🎙️ جاري تسجيل الريكورد..." else "محاكاة تسجيل الصوت والأوامر 🎙️", 
                    fontWeight = FontWeight.Bold, 
                    textAlign = TextAlign.Right, 
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isRecordingVoice) {
                        Text(
                            text = "اكتب النص الذي تود التحدث به أو اختر أحد الأمثلة السريعة، ثم اضغط على زر التسجيل لمحاكاة ريكورد حقيقي تفاعلي!",
                            style = MaterialTheme.typography.bodySmall
                        )

                        TextField(
                            value = mockPhraseInput,
                            onValueChange = { mockPhraseInput = it },
                            placeholder = { Text("اكتب النص الذي ستنطق به...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("أمثلة سريعة لنطقها:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        prepopulatedPhrases.forEach { phrase ->
                            OutlinedButton(
                                onClick = { mockPhraseInput = phrase },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(phrase, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    } else {
                        // Dynamic active recording interface
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Pulsing mic
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "تسجيل نشط", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }

                            // Dynamic Live Waveform Bouncer!
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                barHeights.forEach { height ->
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(height.dp)
                                            .background(NeonTeal, RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            // Timer display
                            Text(
                                text = "0:${String.format("%02d", recordingTimeSeconds)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "\" ${mockPhraseInput.ifBlank { "صوت عام..." }} \"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!isRecordingVoice) {
                    Button(
                        onClick = {
                            isRecordingVoice = true
                            recordingTimeSeconds = 0
                        }
                    ) {
                        Text("🎙️ ابدأ التسجيل الآن")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Send as Voice Record directly to Chat
                        Button(
                            onClick = {
                                showVoiceMockDialog = false
                                isRecordingVoice = false
                                val spoken = mockPhraseInput.ifBlank { "سجل لي مهمة غسيل السيارة" }
                                val dur = if (recordingTimeSeconds > 0) recordingTimeSeconds else 6
                                viewModel.sendVoiceChatMessage(spoken, dur)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                        ) {
                            Text("💬 إرسال كـ ريكورد")
                        }

                        // Extract directly as a Task card
                        Button(
                            onClick = {
                                showVoiceMockDialog = false
                                isRecordingVoice = false
                                viewModel.startVoiceRecording()
                                viewModel.stopVoiceRecordingAndProcess(mockPhraseInput.ifBlank { null })
                            }
                        ) {
                            Text("⚡ استخراج مهمة")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showVoiceMockDialog = false
                        isRecordingVoice = false
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun VoiceRecordPlayBubble(
    content: String,
    isUser: Boolean
) {
    val regex = "\\[VOICE:(\\d+)\\]\\s*(.*)".toRegex()
    val match = regex.find(content)
    val (durationSeconds, transcribedText) = if (match != null) {
        val dur = match.groupValues[1].toIntOrNull() ?: 10
        val txt = match.groupValues[2]
        Pair(dur, txt)
    } else {
        Pair(10, content)
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressSeconds by remember { mutableStateOf(0) }
    var showTranscript by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentProgressSeconds < durationSeconds) {
                kotlinx.coroutines.delay(1000)
                currentProgressSeconds += 1
            }
            isPlaying = false
            currentProgressSeconds = 0
        }
    }

    val totalDurationStr = "0:${String.format("%02d", durationSeconds)}"
    val currentDurationStr = "0:${String.format("%02d", currentProgressSeconds)}"

    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Play/Pause button
            IconButton(
                onClick = {
                    if (isPlaying) {
                        isPlaying = false
                    } else {
                        if (currentProgressSeconds >= durationSeconds) {
                            currentProgressSeconds = 0
                        }
                        isPlaying = true
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(primaryColor.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Waveform bars representation
            val waveBars = listOf(14, 24, 18, 12, 28, 22, 16, 26, 12, 20, 15, 24, 18, 10, 22, 14)
            val playedRatio = if (durationSeconds > 0) currentProgressSeconds.toFloat() / durationSeconds else 0f
            val barsToHighlight = (waveBars.size * playedRatio).toInt()

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                waveBars.forEachIndexed { index, height ->
                    val isHighlighted = index <= barsToHighlight
                    val barColor = if (isHighlighted) {
                        if (isUser) Color.White else NeonTeal
                    } else {
                        if (isUser) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height.dp)
                            .background(barColor, RoundedCornerShape(2.dp))
                    )
                }
            }

            Text(
                text = if (isPlaying) currentDurationStr else totalDurationStr,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showTranscript = !showTranscript }
        ) {
            Text(
                text = if (showTranscript) "🔽 إخفاء النص المفرغ" else "📝 عرض النص المفرغ",
                style = MaterialTheme.typography.bodySmall,
                color = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        if (showTranscript) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = transcribedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ProposedActionCard(action: ProposedAction, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, NeonTeal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔮", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "إجراء ذكي مكتشف تلقائياً!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                    Text(
                        text = "الذكاء الاصطناعي يقترح تنفيذ العملية التالية:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "النوع: " + when(action.type) {
                            "CREATE_TASK" -> "إنشاء مهمة جديدة"
                            "CREATE_NOTE" -> "حفظ ملاحظة"
                            "CREATE_EVENT" -> "جدولة حدث في التقويم"
                            "CREATE_HABIT" -> "تتبع عادة جديدة"
                            else -> action.type
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "العنوان: ${action.title ?: ""}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (!action.description.isNullOrBlank()) {
                        Text(text = "الوصف: ${action.description}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!action.content.isNullOrBlank()) {
                        Text(text = "المحتوى: ${action.content}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!action.priority.isNullOrBlank()) {
                        Text(text = "الأولوية: ${action.priority}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("تجاهل")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = CosmicGradientEnd)
                ) {
                    Text("تأكيد وتنفيذ الإجراء ✅")
                }
            }
        }
    }
}

@Composable
fun VoiceProcessingCard(state: VoiceProcessingState, onConfirm: (Task) -> Unit, onCancel: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                is VoiceProcessingState.Recording -> {
                    Text("🎙️ جاري تسجيل الصوت...", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is VoiceProcessingState.Transcribing -> {
                    Text("📝 جاري تحويل الصوت إلى نص مقروء...", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
                is VoiceProcessingState.Extracting -> {
                    Text("🧠 جاري استخراج معلومات المهمة بالذكاء الاصطناعي...", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "\"${state.text}\"", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is VoiceProcessingState.Success -> {
                    val task = state.extractedTask
                    Text("✅ تم الاستخراج بنجاح!", fontWeight = FontWeight.Bold, color = NeonTeal)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "العنوان المستخرج: ${task.title}", fontWeight = FontWeight.Bold)
                            Text(text = "الأهمية: ${task.priority}", style = MaterialTheme.typography.bodySmall)
                            if (task.dueDate != null) {
                                Text(
                                    text = "تاريخ الاستحقاق: ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(task.dueDate))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onCancel) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onConfirm(task) }) { Text("حفظ المهمة") }
                    }
                }
                is VoiceProcessingState.Error -> {
                    Text("❌ خطأ: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onCancel) { Text("رجوع") }
                }
                else -> {}
            }
        }
    }
}

// ==========================================
// 3. TASK & PROJECT SCREEN
// ==========================================
@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val projects by viewModel.projects.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Tasks, 1 = Projects

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("المهام", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("المشاريع", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                // Tasks List
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("قائمة المهام اليومية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("add_task_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "أضف مهمة")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("أضف مهمة")
                        }
                    }

                    if (tasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("قائمة المهام فارغة حالياً.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tasks) { task ->
                                TaskRowItem(
                                    task = task,
                                    onStatusChanged = { viewModel.updateTaskStatus(task, it) },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Projects List
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تتبع المشاريع الكبرى", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = { showAddProjectDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "أضف مشروع")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشروع جديد")
                        }
                    }

                    if (projects.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد مشاريع نشطة حالياً.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(projects) { project ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.deleteProject(project) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Text(
                                            text = project.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Progress
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "التقدم المكتمل", style = MaterialTheme.typography.bodySmall)
                                            Text(text = "${project.progressPercent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = project.progressPercent / 100f,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape),
                                            color = NeonTeal
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

    // Add Task Dialog
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("MEDIUM") }
        var selectedProjectId by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("إضافة مهمة جديدة", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان المهمة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())

                    Text("الأولوية والأهمية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW" to "منخفضة", "MEDIUM" to "متوسطة", "HIGH" to "عالية", "URGENT" to "قصوى").forEach { (key, label) ->
                            ElevatedFilterChip(
                                selected = priority == key,
                                onClick = { priority = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (projects.isNotEmpty()) {
                        Text("ربط بالمشروع (اختياري):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            projects.forEach { proj ->
                                ElevatedFilterChip(
                                    selected = selectedProjectId == proj.id,
                                    onClick = { selectedProjectId = if (selectedProjectId == proj.id) null else proj.id },
                                    label = { Text(proj.title) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addTask(title, desc, priority, null, selectedProjectId)
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("حفظ المهمة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            title = { Text("إنشاء مشروع جديد", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان المشروع") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addProject(title, desc, null, null)
                            showAddProjectDialog = false
                        }
                    }
                ) {
                    Text("إنشاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProjectDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun TaskRowItem(task: Task, onStatusChanged: (String) -> Unit, onDelete: (() -> Unit)? = null) {
    val priorityColor = when (task.priority) {
        "URGENT" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFFBBF24)
        "LOW" -> Color(0xFF3B82F6)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Checkbox(
                checked = task.status == "DONE",
                onCheckedChange = { isChecked ->
                    onStatusChanged(if (isChecked) "DONE" else "TODO")
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.status == "DONE") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف المهمة", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ==========================================
// 4. SMART NOTES SCREEN
// ==========================================
@Composable
fun NotesScreen(viewModel: MainViewModel) {
    val notes by viewModel.notes.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مفكرة الملاحظات الذكية 📝", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddNoteDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
                Spacer(modifier = Modifier.width(4.dp))
                Text("ملاحظة جديدة")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info card about AI categorization
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("الملاحظات تحفظ وتُصنف أوتوماتيكياً بالذكاء الاصطناعي مع توليد وسوم دلالية ذكية!", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notes.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("مفكرتك فارغة تماماً. ابدأ بتسجيل أول ملاحظة!")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedNote = note },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = note.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteNote(note) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = note.content, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
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
            title = { Text("تدوين ملاحظة جديدة", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("العنوان") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("المحتوى والتفاصيل...") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNoteWithAICategorization(title, content)
                            title = ""
                            content = ""
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("حفظ وتصنيف بالذكاء ⚡")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // View / Edit Note Detail Dialog
    if (selectedNote != null) {
        AlertDialog(
            onDismissRequest = { selectedNote = null },
            title = { Text(selectedNote!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "التصنيف المقترح: ${selectedNote!!.category}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = selectedNote!!.content, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedNote = null }) { Text("إغلاق") }
            }
        )
    }
}

// ==========================================
// 5. SMART CALENDAR SCREEN
// ==========================================
@Composable
fun CalendarScreen(viewModel: MainViewModel) {
    val events by viewModel.calendarEvents.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var eventTitle by remember { mutableStateOf("") }
    var showAddEventDialog by remember { mutableStateOf(false) }

    var aiSuggestTimeResult by remember { mutableStateOf("") }
    var showAISuggestTimeDialog by remember { mutableStateOf(false) }
    var isAILoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("التقويم الذكي 🗓️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showAISuggestTimeDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("اقتراح وقت AI ✨")
                }
                Button(onClick = { showAddEventDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar timeline / event list
        if (events.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد مواعيد مجدولة حالياً.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الوقت: ${SimpleDateFormat("EEEE h:mm a", Locale("ar")).format(Date(event.startTime))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (event.location.isNotBlank()) {
                                    Text(text = "المكان: ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteCalendarEvent(event) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        var location by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("جدولة موعد جديد", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eventTitle, onValueChange = { eventTitle = it }, label = { Text("عنوان الموعد") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("المكان") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank()) {
                            viewModel.addCalendarEvent(
                                title = eventTitle,
                                description = "",
                                startTime = System.currentTimeMillis() + 3600000, // +1 Hour default
                                endTime = System.currentTimeMillis() + 7200000,
                                location = location
                            )
                            eventTitle = ""
                            showAddEventDialog = false
                        }
                    }
                ) {
                    Text("جدولة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // AI Suggest Meeting Time Dialog
    if (showAISuggestTimeDialog) {
        var durationMinutes by remember { mutableStateOf("60") }
        var attendeesInfo by remember { mutableStateOf("أنا متفرغ مساءً، والزميل متفرغ بين 4 و 6 مساءً") }

        AlertDialog(
            onDismissRequest = { showAISuggestTimeDialog = false },
            title = { Text("اقتراح أفضل وقت بالذكاء 🧠", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يقوم المساعد بتحليل انشغالات التقويم الحالي واقتراح أفضل وقت مناسب تلقائياً حسب تفضيلاتك وتفرغ زملائك!", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(value = durationMinutes, onValueChange = { durationMinutes = it }, label = { Text("مدة الاجتماع (بالدقائق)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = attendeesInfo, onValueChange = { attendeesInfo = it }, label = { Text("تفاصيل تفرغ الحضور والقيود") }, modifier = Modifier.fillMaxWidth())

                    if (isAILoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (aiSuggestTimeResult.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        ) {
                            Text(text = aiSuggestTimeResult, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isAILoading = true
                        val prompt = """
                            اقترح أفضل وقت لعقد اجتماع مدته $durationMinutes دقيقة بناءً على المعطيات التالية وتفضيلات التفرغ:
                            المعطيات: $attendeesInfo
                            
                            يرجى تقديم مقترحين محددين (التاريخ والوقت) باللغة العربية بأسلوب راقٍ ومقنع ومفسر لسبب الاختيار.
                        """.trimIndent()

                        coroutineScope.launch {
                            aiSuggestTimeResult = RetrofitClient.callGemini(prompt = prompt, temperature = 0.5f)
                            isAILoading = false
                        }
                    }
                ) {
                    Text("توليد الاقتراح ⚡")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAISuggestTimeDialog = false
                    aiSuggestTimeResult = ""
                }) { Text("إغلاق") }
            }
        )
    }
}

// ==========================================
// 6. DOCUMENTS & OCR SCREEN
// ==========================================
@Composable
fun DocumentsScreen(viewModel: MainViewModel) {
    val documents by viewModel.documents.collectAsState()
    val isDocLoading by viewModel.isDocLoading.collectAsState()
    val summary by viewModel.docSummary.collectAsState()
    val extractedTasks by viewModel.docTasks.collectAsState()

    var docTitle by remember { mutableStateOf("") }
    var docContentInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("مستندات الذكاء الاصطناعي و OCR 📄", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إدخال مستند جديد أو مسح ضوئي محاكى", fontWeight = FontWeight.Bold)

                OutlinedTextField(value = docTitle, onValueChange = { docTitle = it }, label = { Text("عنوان المستند / الورقة الممسوحة") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = docContentInput,
                    onValueChange = { docContentInput = it },
                    label = { Text("محتوى النص للمستند أو النص المستخرج بالمسح الضوئي...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            docTitle = "عقد الاتفاقية السنوي"
                            docContentInput = """
                                نصوص العقد السنوي للخدمات التقنية لسنة 2026.
                                المهام المطلوبة والمسؤوليات:
                                - يجب تسليم تقرير الصيانة قبل تاريخ 20 يوليو 2026.
                                - يلتزم المطور بمراجعة الأكواد الأمنية وإرسال التعديل الأسبوع القادم.
                                - يتكفل الطرف الأول بتسليم الدفعات المالية فور تأكيد الإنجاز.
                            """.trimIndent()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("محاكاة مسح مستند 📑", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (docTitle.isNotBlank() && docContentInput.isNotBlank()) {
                                viewModel.processDocument(docTitle, docContentInput, "TEXT")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تحليل واستخراج ⚡", fontSize = 11.sp)
                    }
                }
            }
        }

        if (isDocLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("جاري قراءة وتلخيص المستند واستخراج المهام...")
                }
            }
        }

        if (summary.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, NeonTeal)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("نتائج التحليل والتلخيص بالذكاء الاصطناعي ✦", fontWeight = FontWeight.Bold, color = NeonTeal)
                    Text(text = summary, style = MaterialTheme.typography.bodyMedium)

                    if (extractedTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("المهام المقترحة المستخرجة والمضافة للمهام تلقائياً:", fontWeight = FontWeight.Bold)
                        extractedTasks.forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📌", modifier = Modifier.padding(end = 8.dp))
                                Text(text = task, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Saved docs log
        if (documents.isNotEmpty()) {
            Text("أرشيف المستندات المحللة سابقاً:", fontWeight = FontWeight.Bold)
            documents.forEach { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = doc.title, fontWeight = FontWeight.Bold)
                        Text(text = "ملخص: ${doc.summary}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. HABITS, GOALS & POMODORO SCREEN
// ==========================================
@Composable
fun HabitsFocusScreen(viewModel: MainViewModel) {
    val habits by viewModel.habits.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val pomodoroSecondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val pomodoroIsActive by viewModel.pomodoroIsActive.collectAsState()
    val pomodoroMode by viewModel.pomodoroMode.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    var selectedSection by remember { mutableStateOf(0) } // 0 = Pomodoro, 1 = Habits, 2 = Goals

    val minutes = pomodoroSecondsLeft / 60
    val seconds = pomodoroSecondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSection, containerColor = MaterialTheme.colorScheme.background) {
            Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }) {
                Text("مؤقت بومودورو ⏱️", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }) {
                Text("متابعة العادات 🔥", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }) {
                Text("إدارة الأهداف 🎯", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            when (selectedSection) {
                0 -> {
                    // Pomodoro Timer Screen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pomodoroMode == "WORK") "وقت العمل والتركيز 💻" else "وقت الاستراحة والراحة ☕",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (pomodoroMode == "WORK") MaterialTheme.colorScheme.primary else NeonTeal
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Circular Progress Timer
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CosmicGradientStart, CosmicGradientEnd)
                                    )
                                )
                                .border(4.dp, if (pomodoroIsActive) NeonTeal else Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = timeFormatted,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (pomodoroMode == "WORK") "بومودورو 25 دقيقة" else "استراحة 5 دقائق",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    if (pomodoroIsActive) viewModel.pausePomodoro()
                                    else viewModel.startPomodoro()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text(if (pomodoroIsActive) "إيقاف مؤقت" else "ابدأ التركيز")
                            }

                            OutlinedButton(
                                onClick = { viewModel.resetPomodoro() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("إعادة ضبط")
                            }
                        }
                    }
                }
                1 -> {
                    // Habits Tracker Screen
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("بناء ومتابعة العادات اليومية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddHabitDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "أضف عادة")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (habits.isEmpty()) {
                            Text("لم تقم بإضافة أي عادات لتتبعها بعد.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(habits) { habit ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(text = "سلسلة الالتزام الحالية: 🔥 ${habit.streakCount} أيام", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Button(onClick = { viewModel.checkInHabit(habit) }) {
                                                    Text("تم اليوم ✓")
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Goals Screen
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("أهدافي الإستراتيجية 🎯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddGoalDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "أضف هدف")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (goals.isEmpty()) {
                            Text("لا توجد أهداف مبرمجة حالياً.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(goals) { goal ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(text = goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                    Text(text = "نوع الهدف: " + when(goal.type) {
                                                        "DAILY" -> "يومي"
                                                        "WEEKLY" -> "أسبوعي"
                                                        "MONTHLY" -> "شهري"
                                                        "YEARLY" -> "سنوي"
                                                        else -> goal.type
                                                    }, style = MaterialTheme.typography.bodySmall)
                                                }
                                                Row {
                                                    IconButton(onClick = { viewModel.updateGoalProgress(goal, 10) }) {
                                                        Icon(Icons.Default.AddCircle, contentDescription = "زيادة التقدم", tint = NeonTeal)
                                                    }
                                                    IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            val progressPercent = (goal.currentValue.toFloat() / goal.targetValue * 100).toInt()
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "التقدم المحقق: ${goal.currentValue}/${goal.targetValue}", style = MaterialTheme.typography.bodySmall)
                                                Text(text = "$progressPercent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = goal.currentValue.toFloat() / goal.targetValue,
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                color = NeonTeal
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

    // Add Habit Dialog
    if (showAddHabitDialog) {
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = { Text("بناء عادة جديدة", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("اسم العادة (مثلاً: القراءة 30 دقيقة)") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addHabit(title, "DAILY")
                            showAddHabitDialog = false
                        }
                    }
                ) {
                    Text("إضافة العادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetVal by remember { mutableStateOf("100") }
        var type by remember { mutableStateOf("DAILY") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("تحديد هدف جديد", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الهدف") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = targetVal, onValueChange = { targetVal = it }, label = { Text("قيمة الهدف المستهدفة (مثلاً 100)") }, modifier = Modifier.fillMaxWidth())

                    Text("دورة تحقيق الهدف:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DAILY" to "يومي", "WEEKLY" to "أسبوعي", "MONTHLY" to "شهري", "YEARLY" to "سنوي").forEach { (key, label) ->
                            ElevatedFilterChip(
                                selected = type == key,
                                onClick = { type = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addGoal(title, type, targetVal.toIntOrNull() ?: 100)
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("حفظ الهدف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

// ==========================================
// 8. EMAIL ASSISTANT SCREEN
// ==========================================
@Composable
fun EmailAssistantScreen(viewModel: MainViewModel) {
    val draft by viewModel.emailDraft.collectAsState()
    val isLoading by viewModel.emailLoading.collectAsState()

    var contextInput by remember { mutableStateOf("") }
    var toneSelected by remember { mutableStateOf("مهني ومحترف") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("مساعد البريد الإلكتروني الذكي ✉️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("توليد وصياغة رسالة بريد إلكتروني", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = contextInput,
                    onValueChange = { contextInput = it },
                    label = { Text("اكتب تفاصيل السياق أو موضوع البريد الإلكتروني...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Text("نبرة وصيغة البريد:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("مهني ومحترف", "ودي ولطيف", "رسمي وجاد", "اعتذار وتوضيح").forEach { tone ->
                        ElevatedFilterChip(
                            selected = toneSelected == tone,
                            onClick = { toneSelected = tone },
                            label = { Text(tone) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (contextInput.isNotBlank()) {
                                viewModel.draftEmail(contextInput, toneSelected)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("صياغة البريد ⚡")
                    }

                    Button(
                        onClick = {
                            if (contextInput.isNotBlank()) {
                                viewModel.summarizeEmail(contextInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تلخيص بريد وارد 📑")
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (draft.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("المسودة المولدة:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { viewModel.sendChatMessage("انسخ هذا البريد: $draft") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ للدردشة")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(text = draft, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. AI MEMORY SCREEN
// ==========================================
@Composable
fun MemoryScreen(viewModel: MainViewModel) {
    val memories by viewModel.aiMemories.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("الذاكرة الذكية للمساعد 🧠", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "هنا يقوم المساعد باستخراج الحقائق والتفضيلات عنك تلقائياً من المحادثات السابقة ليتذكرها دوماً ويخصص لك تجربة فريدة بالكامل!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (memories.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💡", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("الذاكرة فارغة حالياً. بمجرد التحدث مع المساعد ومشاركة تفضيلاتك ستظهر هنا!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(memories) { memory ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (memory.category) {
                                        "person" -> "👤"
                                        "project" -> "🚀"
                                        "habit" -> "🔥"
                                        else -> "💡"
                                    },
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = memory.key, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = memory.value, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { viewModel.deleteMemory(memory.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الحقيقة", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. PROFILE & SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val name by viewModel.userName.collectAsState()
    val bio by viewModel.userBio.collectAsState()
    val plan by viewModel.userPlan.collectAsState()

    var editName by remember { mutableStateOf(name) }
    var editBio by remember { mutableStateOf(bio) }
    var editPlan by remember { mutableStateOf(plan) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("الملف الشخصي والإعدادات ⚙️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تعديل بيانات الملف الشخصي", fontWeight = FontWeight.Bold)

                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("الاسم الكريم") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("نبذة بسيطة") }, modifier = Modifier.fillMaxWidth())

                Text("مستوى خطة الاشتراك:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Free" to "مجانية", "Premium" to "بريميوم ✦").forEach { (key, label) ->
                        ElevatedFilterChip(
                            selected = editPlan == key,
                            onClick = { editPlan = key },
                            label = { Text(label) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editBio, editPlan)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ التغييرات ✅")
                }
            }
        }

        // About / Tech Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("معلومات التطبيق التقنية 📱", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("اسم التطبيق: عقل ثانٍ بالذكاء الاصطناعي - Second Brain AI", style = MaterialTheme.typography.bodySmall)
                Text("قاعدة البيانات المحلية: SQLite - Room DB", style = MaterialTheme.typography.bodySmall)
                Text("محرك الذكاء الاصطناعي: Google Gemini 3.5 Flash", style = MaterialTheme.typography.bodySmall)
                Text("الإصدار: 1.0.0 (بناء 2026)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
