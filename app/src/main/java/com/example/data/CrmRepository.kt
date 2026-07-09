package com.example.data

import kotlinx.coroutines.flow.Flow

class CrmRepository(private val crmDao: CrmDao) {
    val allRecords: Flow<List<CrmRecord>> = crmDao.getAllRecords()
    val followUps: Flow<List<CrmRecord>> = crmDao.getFollowUps()
    val salesOpportunities: Flow<List<CrmRecord>> = crmDao.getSalesOpportunities()

    suspend fun insertRecord(record: CrmRecord) = crmDao.insertRecord(record)
    suspend fun updateRecord(record: CrmRecord) = crmDao.updateRecord(record)
    suspend fun getRecordByNumber(number: String) = crmDao.getRecordByNumber(number)
    suspend fun getNextId() = crmDao.getCount() + 1
}
