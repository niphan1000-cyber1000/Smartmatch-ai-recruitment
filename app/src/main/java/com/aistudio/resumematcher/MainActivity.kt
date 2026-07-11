package com.aistudio.resumematcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aistudio.resumematcher.data.local.AppDatabase
import com.aistudio.resumematcher.data.repository.MatchRepository
import com.aistudio.resumematcher.ui.theme.MyApplicationTheme
import com.aistudio.resumematcher.ui.view.MainScreen
import com.aistudio.resumematcher.ui.viewmodel.MatchViewModel
import com.aistudio.resumematcher.ui.viewmodel.MatchViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { MatchRepository(database.matchRecordDao()) }
    private val viewModel: MatchViewModel by viewModels {
        MatchViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

