package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CrmRecord
import com.example.util.JalaliCalendar
import com.example.viewmodel.CrmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpsScreen(viewModel: CrmViewModel) {
    val followUps by viewModel.followUps.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("پیگیری‌ها (Follow-ups)", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (followUps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("موردی برای پیگیری وجود ندارد.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(followUps) { record ->
                    FollowUpItem(record, onUpdate = { viewModel.updateRecord(it) })
                }
            }
        }
    }
}

@Composable
fun FollowUpItem(record: CrmRecord, onUpdate: (CrmRecord) -> Unit) {
    val isDelayed = record.timestamp < System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Simple logic: more than 24h old

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isDelayed && record.followUpStatus == "PENDING") {
            CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        } else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.contactId, fontWeight = FontWeight.Bold)
                if (isDelayed && record.followUpStatus == "PENDING") {
                    Icon(Icons.Default.Warning, contentDescription = "Delayed", tint = Color.Red)
                }
            }
            Text("${record.customerName} - ${record.contactNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("اقدام بعدی: ${record.nextAction}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("وضعیت: ", style = MaterialTheme.typography.bodySmall)
                Text(
                    if (record.followUpStatus == "DONE") "انجام شده" else "در انتظار",
                    color = if (record.followUpStatus == "DONE") Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }

            if (record.followUpStatus == "PENDING") {
                Button(
                    onClick = { onUpdate(record.copy(followUpStatus = "DONE")) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("علامت‌گذاری به عنوان انجام شده")
                }
            }
        }
    }
}
