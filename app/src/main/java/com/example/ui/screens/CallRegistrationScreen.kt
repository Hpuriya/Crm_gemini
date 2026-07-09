package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CrmRecord
import com.example.util.JalaliCalendar
import com.example.viewmodel.CrmViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallRegistrationScreen(viewModel: CrmViewModel) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ثبت تماس و دیتابیس اصلی", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("کل تماس‌های ثبت شده: ${records.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(records) { record ->
                RecordItem(record)
            }
        }

        if (showDialog) {
            AddRecordDialog(
                viewModel = viewModel,
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun RecordItem(record: CrmRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.contactId, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(JalaliCalendar.formatTimestamp(record.timestamp), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${record.companyName} - ${record.customerName}", fontWeight = FontWeight.Medium)
            Text(record.contactNumber, style = MaterialTheme.typography.bodyMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("نتیجه: ", fontWeight = FontWeight.Bold)
                Text(record.result)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("اقدام بعدی: ", fontWeight = FontWeight.Bold)
                Text(record.nextAction, color = MaterialTheme.colorScheme.secondary)
            }
            if (record.description.isNotEmpty()) {
                Text("توضیحات: ${record.description}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(viewModel: CrmViewModel, onDismiss: () -> Unit) {
    var companyName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("بدون پاسخ") }
    var description by remember { mutableStateOf("") }
    
    var isNumberDuplicate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val results = listOf("بدون پاسخ", "نیاز به پیگیری", "عدم نیاز", "قرارداد با رقبا", "موافقت با همکاری")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت تماس جدید") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("نام شرکت") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = companyName.isEmpty()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("نام مشتری") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { 
                        contactNumber = it
                        scope.launch {
                            isNumberDuplicate = viewModel.allRecords.value.any { r -> r.contactNumber == it }
                        }
                    },
                    label = { Text("شماره تماس") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        if (isNumberDuplicate) Icon(Icons.Default.Error, "Duplicate", tint = Color.Red)
                        else if (contactNumber.isNotEmpty()) Icon(Icons.Default.CheckCircle, "OK", tint = Color.Green)
                    },
                    supportingText = { if (isNumberDuplicate) Text("این شماره قبلاً ثبت شده است!", color = Color.Red) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = result,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نتیجه تماس") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        results.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    result = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotEmpty() && contactNumber.isNotEmpty()) {
                        viewModel.addRecord(companyName, customerName, contactNumber, result, description)
                        onDismiss()
                    }
                },
                enabled = companyName.isNotEmpty() && contactNumber.isNotEmpty()
            ) {
                Text("ثبت نهایی")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
