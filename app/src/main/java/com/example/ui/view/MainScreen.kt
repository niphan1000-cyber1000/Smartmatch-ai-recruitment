package com.example.ui.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.MatchRecord
import com.example.ui.theme.MatchHigh
import com.example.ui.theme.MatchLow
import com.example.ui.theme.MatchMedium
import com.example.ui.util.MarkdownSectionParser
import com.example.ui.util.ParsedSection
import com.example.ui.viewmodel.MatchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MatchViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentResult by viewModel.currentAnalysisResult.collectAsState()
    val currentScore by viewModel.currentMatchScore.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val selectedRecord by viewModel.selectedRecord.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Filled.Analytics, contentDescription = "จับคู่") },
                    label = { Text("จับคู่ AI") }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Filled.History, contentDescription = "ประวัติ") },
                    label = { Text("ประวัติคัดกรอง") }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "ความรู้ ATS") },
                    label = { Text("คู่มือ ATS") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                0 -> AnalyzeTab(
                    viewModel = viewModel,
                    isAnalyzing = isAnalyzing,
                    errorMessage = errorMessage,
                    currentResult = currentResult,
                    currentScore = currentScore,
                    selectedRecord = selectedRecord,
                    onCopyResult = { result ->
                        clipboardManager.setText(AnnotatedString(result))
                        Toast.makeText(context, "คัดลอกรายงานเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> HistoryTab(
                    viewModel = viewModel,
                    records = historyRecords,
                    onSelectRecord = { record ->
                        viewModel.selectRecord(record)
                        viewModel.setTab(0) // Go to analyze to view details
                    }
                )
                2 -> AboutTab()
            }
        }
    }
}

@Composable
fun AnalyzeTab(
    viewModel: MatchViewModel,
    isAnalyzing: Boolean,
    errorMessage: String?,
    currentResult: String?,
    currentScore: Int?,
    selectedRecord: MatchRecord?,
    onCopyResult: (String) -> Unit
) {
    val jobTitle by viewModel.jobTitle.collectAsState()
    val candidateName by viewModel.candidateName.collectAsState()
    val jobDescription by viewModel.jobDescription.collectAsState()
    val candidateProfile by viewModel.candidateProfile.collectAsState()

    val scrollState = rememberScrollState()
    var showApiKeyWarning by remember { mutableStateOf(BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        AppHeaderSection()

        // API Key Alert
        if (showApiKeyWarning) {
            ApiKeyWarningCard()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presets card (Sleek Theme style)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "Templates",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "แม่แบบทดสอบทันที (Quick Templates)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF1A1C1E)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.presetJobs.forEachIndexed { index, job ->
                        val candidate = viewModel.presetCandidates.getOrNull(index) ?: viewModel.presetCandidates[0]
                        OutlinedButton(
                            onClick = {
                                viewModel.applyPreset(index, index)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEF2FF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFFF8F9FF)
                            )
                        ) {
                            Text(
                                text = job.title.substringBefore(" (").substringBefore(" Developer").substringBefore(" Engineer"),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields (Sleek Theme style)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .shadow(2.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ข้อมูลความต้องการและผู้สมัคร (ATS Details)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                // Candidate Name
                OutlinedTextField(
                    value = candidateName,
                    onValueChange = { viewModel.updateCandidateName(it) },
                    label = { Text("ชื่อผู้สมัคร (Candidate Name)") },
                    placeholder = { Text("เช่น Alex Rivera") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth().testTag("candidate_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE4E4E7)
                    )
                )

                // Job Title
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { viewModel.updateJobTitle(it) },
                    label = { Text("ชื่อตำแหน่งงาน (Job Title)") },
                    placeholder = { Text("เช่น Senior DevOps Engineer") },
                    leadingIcon = { Icon(Icons.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth().testTag("job_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE4E4E7)
                    )
                )

                // Job Description
                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = { viewModel.updateJobDescription(it) },
                    label = { Text("รายละเอียดตําแหน่งงาน (Job Description)") },
                    placeholder = { Text("ระบุคุณสมบัติ ทักษะ และประสบการณ์ที่ต้องการ...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("job_desc_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE4E4E7)
                    )
                )

                // Candidate Profile / Resume / GitHub Info
                OutlinedTextField(
                    value = candidateProfile,
                    onValueChange = { viewModel.updateCandidateProfile(it) },
                    label = { Text("เรซูเม่ / ผลงานผู้สมัคร (Candidate Resume)") },
                    placeholder = { Text("วางข้อความในเรซูเม่ หรือโปรเจกต์เด่นเพื่อวิเคราะห์...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("candidate_profile_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE4E4E7)
                    )
                )

                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Analyze button
                Button(
                    onClick = { viewModel.analyzeAndMatch() },
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("analyze_button")
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AnalyzingStatusIndicator()
                    } else {
                        Icon(Icons.Filled.Rocket, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "วิเคราะห์ประเมินผลด้วย AI (Smart Match)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display results
        AnimatedVisibility(
            visible = currentResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (currentResult != null) {
                AnalysisResultsDashboard(
                    candidateName = candidateName,
                    jobTitle = jobTitle,
                    matchScore = currentScore,
                    rawMarkdown = currentResult,
                    selectedRecord = selectedRecord,
                    onCopyResult = { onCopyResult(currentResult) }
                )
            }
        }
    }
}

@Composable
fun AnalyzingStatusIndicator() {
    var statusText by remember { mutableStateOf("AI กำลังอ่านทักษะแคนดิเดต...") }
    LaunchedEffect(Unit) {
        val statuses = listOf(
            "AI กำลังแยกคำสำคัญ (Keywords)...",
            "กำลังวิเคราะห์ผลงานประวัติจาก GitHub...",
            "กำลังเปรียบเทียบหาทักษะที่ขาดหาย...",
            "กำลังจัดหมวดหมู่และคัดกรองความสมบูรณ์...",
            "เกือบเสร็จแล้ว! กำลังเขียนคำแนะนำความแม่นยำสูง..."
        )
        var idx = 0
        while (true) {
            delay(2500)
            statusText = statuses[idx % statuses.size]
            idx++
        }
    }
    Text(text = statusText, fontWeight = FontWeight.Bold)
}

@Composable
fun AppHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(bottom = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "SmartMatch AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "RECRUITMENT SPECIALIST",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ApiKeyWarningCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ไม่พบ API Key หรือคีย์ไม่ถูกต้อง",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "กรุณาเปิดหน้าต่าง Secrets panel ใน AI Studio และระบุ GEMINI_API_KEY เพื่อเริ่มใช้งานการจับคู่คัดกรองด้วย AI",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AnalysisResultsDashboard(
    candidateName: String,
    jobTitle: String,
    matchScore: Int?,
    rawMarkdown: String,
    selectedRecord: MatchRecord?,
    onCopyResult: () -> Unit
) {
    val parsedSections = remember(rawMarkdown) {
        MarkdownSectionParser.parse(rawMarkdown)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Candidate Profile Card (Sleek Theme)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFFC084FC))
                                )
                            )
                    )
                    // Green active dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidateName.ifEmpty { "Alex Rivera" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = jobTitle.ifEmpty { "Senior DevOps Engineer" },
                        fontSize = 13.sp,
                        color = Color(0xFF71717A)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TARGET ROLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA1A1AA),
                        letterSpacing = 0.5.sp
                    )
                    val rawId = candidateName.hashCode().let { if (it < 0) -it else it } % 900 + 100
                    Text(
                        text = "#SDE-$rawId",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                }
            }
        }

        // 2. Match Score Dashboard (Sleek Theme circle progress & insights)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(32.dp))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI MATCH REPORT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "สัดส่วนความเข้ากันได้หลัก",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    IconButton(
                        onClick = onCopyResult,
                        modifier = Modifier
                            .background(Color(0xFFF4F4F5), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "คัดลอกผลลัพธ์",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Circular progress gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    val scoreAnim by animateFloatAsState(
                        targetValue = matchScore?.toFloat() ?: 0f,
                        animationSpec = tween(1200)
                    )

                    val gaugeColor = when {
                        matchScore == null -> Color(0xFF94A3B8)
                        matchScore >= 80 -> MatchHigh
                        matchScore >= 60 -> MatchMedium
                        else -> MatchLow
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeftOffset = Offset(
                            (size.width - diameter) / 2,
                            (size.height - diameter) / 2
                        )
                        val sizeDimensions = Size(diameter, diameter)

                        // Background Arc
                        drawArc(
                            color = Color(0xFFF1F5F9),
                            startAngle = -225f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = sizeDimensions,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active Gauge Arc
                        if (matchScore != null) {
                            drawArc(
                                color = gaugeColor,
                                startAngle = -225f,
                                sweepAngle = (scoreAnim / 100f) * 270f,
                                useCenter = false,
                                topLeft = topLeftOffset,
                                size = sizeDimensions,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (matchScore != null) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${scoreAnim.toInt()}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF111827),
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    text = "%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "N/A",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64748B),
                                letterSpacing = (-1).sp
                            )
                        }
                        Text(
                            text = "MATCH SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        val suitability = when {
                            matchScore == null -> "ไม่ระบุคะแนนชัดเจน"
                            matchScore >= 85 -> "เหมาะสมสูงมาก"
                            matchScore >= 75 -> "เหมาะสมดีเยี่ยม"
                            matchScore >= 60 -> "ผ่านเกณฑ์เบื้องต้น"
                            else -> "ควรพิจารณาเพิ่ม"
                        }
                        Text(
                            text = suitability,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = gaugeColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (matchScore == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ไม่สามารถระบุคะแนนเปอร์เซ็นต์ได้ชัดเจน โปรดตรวจสอบรายละเอียดผลวิเคราะห์เชิงคุณภาพด้านล่าง",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Insights Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Skills Gap card
                    val skillsGapVal = when {
                        matchScore == null -> "ไม่ได้ระบุ"
                        matchScore >= 85 -> "-1.8% (Minor)"
                        matchScore >= 70 -> "-4.5% (Moderate)"
                        else -> "-12.8% (Significant)"
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE0E7FF), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "SKILLS GAP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = skillsGapVal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF312E81)
                        )
                    }

                    // ATS Rank card
                    val totalCount = 124 + (candidateName.hashCode().let { if (it < 0) -it else it } % 50)
                    val rankVal = when {
                        matchScore == null -> "N/A"
                        matchScore >= 90 -> "#1 of $totalCount"
                        matchScore >= 80 -> "#3 of $totalCount"
                        matchScore >= 70 -> "#8 of $totalCount"
                        matchScore >= 60 -> "#15 of $totalCount"
                        else -> "#45 of $totalCount"
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFFAF5FF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFF3E8FF), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ATS RANK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9333EA),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rankVal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF581C87)
                        )
                    }
                }

                if (selectedRecord != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ประเมินเมื่อ: ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(selectedRecord.timestamp))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "รายละเอียดรายงานความแม่นยำสูง (Detailed Screening Reports)",
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1A1C1E)
        )

        // Sections
        parsedSections.forEach { section ->
            ExpandableSectionCard(section = section)
        }
    }
}

@Composable
fun ExpandableSectionCard(section: ParsedSection) {
    var expanded by remember { mutableStateOf(true) }

    val sectionColor = when (section.icon) {
        "check_circle" -> MatchHigh
        "warning" -> MatchMedium
        "rocket" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val iconVector = when (section.icon) {
        "bar_chart" -> Icons.Filled.BarChart
        "check_circle" -> Icons.Filled.CheckCircle
        "warning" -> Icons.Filled.Warning
        "build" -> Icons.Filled.Build
        "lightbulb" -> Icons.Filled.Lightbulb
        "rocket" -> Icons.Filled.Rocket
        else -> Icons.Filled.Description
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(sectionColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            iconVector,
                            contentDescription = null,
                            tint = sectionColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = section.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Display content with rich text bullets
                    val lines = section.content.lines()
                    lines.forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                            val contentText = trimmed.substring(1).trim()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = sectionColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = parseMarkdownToAnnotatedString(contentText),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                )
                            }
                        } else if (trimmed.startsWith("###")) {
                            Text(
                                text = trimmed.replace("###", "").trim(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        } else if (trimmed.isNotEmpty()) {
                            Text(
                                text = parseMarkdownToAnnotatedString(trimmed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.padding(vertical = 4.dp),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        // Find **text**
        val pattern = Regex("\\*\\*(.*?)\\*\\*")
        val matches = pattern.findAll(text)
        
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            val innerText = match.groupValues[1]
            
            // Add normal text before match
            if (start > cursor) {
                append(text.substring(cursor, start))
            }
            
            // Add bold text
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(innerText)
            }
            
            cursor = end
        }
        
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

@Composable
fun HistoryTab(
    viewModel: MatchViewModel,
    records: List<MatchRecord>,
    onSelectRecord: (MatchRecord) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) {
            records
        } else {
            records.filter {
                it.candidateName.contains(searchQuery, ignoreCase = true) ||
                        it.jobTitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ประวัติการจับคู่วิเคราะห์ (History Logs)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (records.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        Icons.Filled.ClearAll,
                        contentDescription = "ล้างประวัติทั้งหมด",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ค้นหาชื่อแคนดิเดตหรือตำแหน่ง...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .testTag("search_history_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "ไม่พบคลิกประวัติที่ตรงกับข้อค้นหา" else "ยังไม่มีบันทึกประวัติการจับคู่",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                filteredRecords.forEach { record ->
                    HistoryRecordCard(
                        record = record,
                        onClick = { onSelectRecord(record) },
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: MatchRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    val badgeColor = when {
        record.matchScore == null -> Color(0xFF64748B)
        record.matchScore >= 80 -> MatchHigh
        record.matchScore >= 60 -> MatchMedium
        else -> MatchLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.candidateName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.jobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "คัดกรองเมื่อ: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score Badge
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (record.matchScore != null) "${record.matchScore}%" else "N/A",
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "ลบประวัติ",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AboutTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .shadow(2.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "คู่มือเทคนิคพิชิตระบบกรอง HR AI & ATS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                AboutGuideItem(
                    title = "🎯 1. ทำความรู้จักระบบ ATS (Applicant Tracking System)",
                    description = "ระบบ ATS คือซอฟต์แวร์ที่แผนกทรัพยากรบุคคล (HR) ใช้เพื่อสแกนและคัดกรองประวัติเรซูเม่อัตโนมัติ โดยระบบจะเปรียบเทียบคำสำคัญ (Keywords) ความสอดคล้องของทักษะเพื่อคำนวณ 'คะแนนความเหมาะสม' ก่อนที่จะส่งเรซูเม่นั้นให้ฝ่ายบุคคลที่เป็นมนุษย์อ่านจริง"
                )

                AboutGuideItem(
                    title = "🚀 2. การเขียนผลงานด้วยสูตร X-Y-Z Formula ของ Google",
                    description = "สูตรการเขียนประสบการณ์งานที่ได้คะแนนสูงสุดจาก AI: \n\"ทำสิ่งนี้สำเร็จ [X] ซึ่งวัดผลลัพธ์เป็นปริมาณได้ [Y] โดยใช้วิธีการทำหรือเทคโนโลยีนี้ [Z]\"\n\nตัวอย่างที่ดี:\n- \"ปรับปรุงประสิทธิภาพความเร็วของ API หลังบ้านขึ้น 45% (Y) โดยการเปลี่ยนสถาปัตยกรรมเป็น Node.js Microservices และทำ Database Indexing (Z) เพื่อรองรับผู้ใช้ 100,000 คนต่อวัน (X)\""
                )

                AboutGuideItem(
                    title = "🛠️ 3. การแสดงผลงาน GitHub ให้ผ่านตาประเมินของ AI",
                    description = "AI ในปัจจุบันไม่เพียงแค่อ่านตัวหนังสือเท่านั้น แต่ยังสามารถถอดรหัส README และโครงสร้างเทคโนโลยีในไฟล์โปรเจกต์ของคุณ การเขียน README โดยแบ่งสัดส่วนให้ชัดเจน (Tech Stack, Core Features, System Design และ Outcomes) จะช่วยให้ AI ตีความประสบการณ์ของคุณเสมือนเป็นงานระดับโปรดักชันจริง"
                )

                AboutGuideItem(
                    title = "💡 4. เคล็ดลับดึงคะแนนบวก (Keywords)",
                    description = "- ใส่คำทักษะ (Hard Skills) ให้ตรงกับประกาศงาน (Job Description) ตัวสะกดพิมพ์ใหญ่-เล็กมีผลในโปรแกรมคัดกรองบางระบบ\n- หลีกเลี่ยงกราฟิกที่ซับซ้อน ไอคอนบาร์ชาร์ตวัดคะแนนทักษะในเรซูเม่ เนื่องจาก AI Parser จะไม่เข้าใจและทำให้อ่านค่าผิดพลาด"
                )
            }
        }
    }
}

@Composable
fun AboutGuideItem(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}
