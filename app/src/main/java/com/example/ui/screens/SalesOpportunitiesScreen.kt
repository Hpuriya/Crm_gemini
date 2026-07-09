package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOpportunitiesScreen(viewModel: CrmViewModel) {
    val salesOpportunities by viewModel.salesOpportunities.collectAsStateWithLifecycle()
    var editingRecord by remember { mutableStateOf<CrmRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("پیش‌فاکتور و فرصت فروش", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val totalPipeline = salesOpportunities.filter { it.salesStatus == "PENDING" }.sumOf { it.amount }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ارزش کل فرصت‌های باز (Pipeline)", style = MaterialTheme.typography.titleMedium)
                    Text("${String.format("%,.0f", totalPipeline)} ریال", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
            }

            if (salesOpportunities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("فرصت فروشی ثبت نشده است.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(salesOpportunities) { record ->
                        SalesOpportunityItem(record, onEdit = { editingRecord = it })
                    }
                }
            }
        }

        if (editingRecord != null) {
            EditSalesDialog(
                record = editingRecord!!,
                onDismiss = { editingRecord = null },
                onUpdate = { 
                    viewModel.updateRecord(it)
                    editingRecord = null
                }
            )
        }
    }
}

@Composable
fun SalesOpportunityItem(record: CrmRecord, onEdit: (CrmRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.contactId, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onEdit(record) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            Text(record.customerName, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("وضعیت پیش‌فاکتور:", style = MaterialTheme.typography.bodySmall)
                    Text(if (record.proformaSent) "ارسال شده" else "ارسال نشده", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("مبلغ تقریبی:", style = MaterialTheme.typography.bodySmall)
                    Text("${String.format("%,.0f", record.amount)} ریال", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("وضعیت نهایی: ", style = MaterialTheme.typography.bodySmall)
                Text(
                    when(record.salesStatus) {
                        "SOLD" -> "فروخته شد"
                        "CANCELED" -> "لغو شد"
                        else -> "در جریان"
                    },
                    color = when(record.salesStatus) {
                        "SOLD" -> Color(0xFF2E7D32)
                        "CANCELED" -> Color(0xFFC62828)
                        else -> Color(0xFFF57F17)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSalesDialog(record: CrmRecord, onDismiss: () -> Unit, onUpdate: (CrmRecord) -> Unit) {
    var amount by remember { mutableStateOf(record.amount.toString()) }
    var proformaSent by remember { mutableStateOf(record.proformaSent) }
    var salesStatus by remember { mutableStateOf(record.salesStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش فرصت فروش") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("مبلغ تقریبی (ریال)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = proformaSent, onCheckedChange = { proformaSent = it })
                    Text("پیش‌فاکتور ارسال شد")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("وضعیت نهایی:", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(
                        selected = salesStatus == "PENDING",
                        onClick = { salesStatus = "PENDING" },
                        label = { Text("در جریان") }
                    )
                    FilterChip(
                        selected = salesStatus == "SOLD",
                        onClick = { salesStatus = "SOLD" },
                        label = { Text("فروش موفق") }
                    )
                    FilterChip(
                        selected = salesStatus == "CANCELED",
                        onClick = { salesStatus = "CANCELED" },
                        label = { Text("لغو شده") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdate(record.copy(
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    proformaSent = proformaSent,
                    salesStatus = salesStatus,
                    proformaSentDate = if (proformaSent && !record.proformaSent) System.currentTimeMillis() else record.proformaSentDate
                ))
            }) {
                Text("بروزرسانی")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
