package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CrmRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CrmViewModel
import com.example.viewmodel.CrmViewModelFactory

class MainActivity : ComponentActivity() {
  private lateinit var database: AppDatabase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    database = Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "crm_database"
    ).build()
    
    val repository = CrmRepository(database.crmDao())
    val factory = CrmViewModelFactory(repository)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: CrmViewModel = viewModel(factory = factory)
        MainScreen(viewModel = viewModel)
      }
    }
  }
}
