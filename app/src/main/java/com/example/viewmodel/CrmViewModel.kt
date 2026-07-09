package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.CrmRecord
import com.example.data.CrmRepository
import com.example.util.JalaliCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CrmViewModel(private val repository: CrmRepository) : ViewModel() {

    val allRecords: StateFlow<List<CrmRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followUps: StateFlow<List<CrmRecord>> = repository.followUps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesOpportunities: StateFlow<List<CrmRecord>> = repository.salesOpportunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiInsights = MutableStateFlow<String>("")
    val aiInsights = _aiInsights.asStateFlow()

    fun addRecord(
        companyName: String,
        customerName: String,
        contactNumber: String,
        result: String,
        description: String
    ) {
        viewModelScope.launch {
            val nextId = repository.getNextId()
            val jalaliDate = JalaliCalendar.getCurrentJalaliDate()
            val contactId = "CRM-${jalaliDate.year}${jalaliDate.month.toString().padStart(2, '0')}${jalaliDate.day.toString().padStart(2, '0')}-$nextId"
            
            val nextAction = when (result) {
                "نیاز به پیگیری" -> "تماس مجدد"
                "موافقت با همکاری" -> "ارسال پیش‌فاکتور"
                "بدون پاسخ" -> "تماس مجدد در ساعت دیگر"
                else -> "بایگانی"
            }

            val record = CrmRecord(
                contactId = contactId,
                companyName = companyName,
                customerName = customerName,
                contactNumber = contactNumber,
                result = result,
                nextAction = nextAction,
                description = description
            )
            repository.insertRecord(record)
            generateAiInsights()
        }
    }

    fun updateRecord(record: CrmRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
            generateAiInsights()
        }
    }

    fun generateAiInsights() {
        viewModelScope.launch {
            val records = allRecords.value
            if (records.isEmpty()) return@launch
            
            val prompt = "Based on the following CRM call logs (Persian), identify the top 3 sales opportunities and provide a brief strategy for each. Respond in Persian. Logs: " +
                    records.take(10).joinToString { "${it.customerName} from ${it.companyName}: ${it.result}, ${it.description}" }

            try {
                val response = RetrofitClient.geminiService.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = GenerateContentRequest(listOf(Content(listOf(Part(prompt)))))
                )
                _aiInsights.value = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Insights not available."
            } catch (e: Exception) {
                _aiInsights.value = "Error generating AI insights: ${e.message}"
            }
        }
    }
}
