package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.CrmViewModel
import com.example.util.JalaliCalendar

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CrmViewModel) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("داشبورد تحلیلی و مدیریتی (KPI)", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.generateAiInsights() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh AI")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_dashboard_hero_1783577974047),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            item {
                Text("شاخص‌های کلیدی عملکرد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                KpiGrid(records)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("تحلیل هوشمند (AI Insights)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (aiInsights.isEmpty()) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(aiInsights, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Text("نمودار نتایج تماس", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                CallResultChart(records)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun KpiGrid(records: List<com.example.data.CrmRecord>) {
    val total = records.size
    val answered = records.count { it.result != "بدون پاسخ" }
    val followUps = records.count { it.result == "نیاز به پیگیری" }
    val followUpsDone = records.count { it.followUpStatus == "DONE" && it.result == "نیاز به پیگیری" }
    val engagements = records.count { it.result == "موافقت با همکاری" }
    val proformas = records.count { it.proformaSent }
    val success = records.count { it.salesStatus == "SOLD" }
    val pipeline = records.filter { it.salesStatus == "PENDING" && it.result == "موافقت با همکاری" }.sumOf { it.amount }
    
    val today = JalaliCalendar.getCurrentJalaliDate().toString()
    val todayCalls = records.count { JalaliCalendar.formatTimestamp(it.timestamp) == today }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("کل تماس‌ها", total.toString(), Modifier.weight(1f))
            KpiCard("تماس‌های امروز", todayCalls.toString(), Modifier.weight(1f), color = MaterialTheme.colorScheme.primaryContainer)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("نرخ پاسخگویی", if (total > 0) "${(answered * 100 / total)}%" else "0%", Modifier.weight(1f))
            KpiCard("نرخ تبدیل تماس", if (total > 0) "${(engagements * 100 / total)}%" else "0%", Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard("پیگیری‌های موثر", if (followUps > 0) "${(followUpsDone * 100 / followUps)}%" else "0%", Modifier.weight(1f))
            KpiCard("نرخ بستن فروش", if (proformas > 0) "${(success * 100 / proformas)}%" else "0%", Modifier.weight(1f))
        }
        KpiCard("ارزش Pipeline (ریال)", String.format("%,.0f", pipeline), Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer)
    }
}

@Composable
fun KpiCard(title: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CallResultChart(records: List<com.example.data.CrmRecord>) {
    val results = listOf("بدون پاسخ", "نیاز به پیگیری", "عدم نیاز", "قرارداد با رقبا", "موافقت با همکاری")
    val counts = results.map { res -> records.count { it.result == res } }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        results.forEachIndexed { index, label ->
            val count = counts[index]
            val progress = count.toFloat() / maxCount
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(label, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall)
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            size = size
                        )
                        drawRect(
                            color = barColor,
                            size = Size(size.width * progress, size.height)
                        )
                    }
                }
                Text(count.toString(), modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
