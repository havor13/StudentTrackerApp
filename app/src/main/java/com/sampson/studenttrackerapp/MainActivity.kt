package com.sampson.studenttrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sampson.studenttrackerapp.ui.theme.StudentTrackerAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// DataStore setup
val ComponentActivity.dataStore by preferencesDataStore(name = "student_prefs")
val STUDENT_LIST_KEY = stringPreferencesKey("student_list")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentTrackerAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { StudentTrackerScreen(navController, this@MainActivity) }
                    composable("second") { SecondScreen(this@MainActivity) }
                }
            }
        }
    }
}

@Composable
fun StudentTrackerScreen(navController: androidx.navigation.NavHostController, activity: ComponentActivity) {
    var text by remember { mutableStateOf("") }
    var students = remember { mutableStateListOf<String>() }

    // Load saved student list from DataStore
    LaunchedEffect(Unit) {
        val prefs = activity.dataStore.data.first()
        val savedList = prefs[STUDENT_LIST_KEY] ?: ""
        if (savedList.isNotEmpty()) {
            students.clear()
            students.addAll(savedList.split(","))
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter student name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    students.add(text)
                    activity.lifecycleScope.launch {
                        activity.dataStore.edit { prefs ->
                            prefs[STUDENT_LIST_KEY] = students.joinToString(",")
                        }
                    }
                    text = ""
                }
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text("Save Student")
        }

        Text("Stored Students:", modifier = Modifier.padding(top = 16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(students) { student ->
                Text(student, modifier = Modifier.padding(4.dp))
            }
        }

        Button(
            onClick = { navController.navigate("second") },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("Go to Second Screen")
        }
    }
}

@Composable
fun SecondScreen(activity: ComponentActivity) {
    var students by remember { mutableStateOf(listOf<String>()) }

    // Load student list again for continuity
    LaunchedEffect(Unit) {
        val prefs = activity.dataStore.data.first()
        val savedList = prefs[STUDENT_LIST_KEY] ?: ""
        students = if (savedList.isNotEmpty()) savedList.split(",") else emptyList()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome to the second screen!")
        Text("Stored students:", modifier = Modifier.padding(top = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(students) { student ->
                Text(student, modifier = Modifier.padding(4.dp))
            }
        }
    }
}
