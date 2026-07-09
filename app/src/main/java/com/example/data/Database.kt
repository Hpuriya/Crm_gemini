package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "crm_records")
data class CrmRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val companyName: String,
    val customerName: String,
    val contactNumber: String,
    val result: String,
    val nextAction: String,
    val description: String,
    
    // Follow-up specific
    val followUpDate: Long? = null,
    val followUpStatus: String = "PENDING", // PENDING, DONE
    
    // Sales specific
    val proformaSent: Boolean = false,
    val proformaSentDate: Long? = null,
    val amount: Double = 0.0,
    val salesStatus: String = "PENDING" // PENDING, SOLD, CANCELED
)

@Dao
interface CrmDao {
    @Query("SELECT * FROM crm_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CrmRecord>>

    @Query("SELECT * FROM crm_records WHERE contactNumber = :number LIMIT 1")
    suspend fun getRecordByNumber(number: String): CrmRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CrmRecord): Long

    @Update
    suspend fun updateRecord(record: CrmRecord)

    @Query("SELECT COUNT(*) FROM crm_records")
    suspend fun getCount(): Int

    @Query("SELECT * FROM crm_records WHERE result = 'نیاز به پیگیری'")
    fun getFollowUps(): Flow<List<CrmRecord>>

    @Query("SELECT * FROM crm_records WHERE result = 'موافقت با همکاری' OR salesStatus != 'PENDING'")
    fun getSalesOpportunities(): Flow<List<CrmRecord>>
}

@Database(entities = [CrmRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crmDao(): CrmDao
}
