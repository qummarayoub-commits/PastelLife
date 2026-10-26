package com.pastellife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Canvas = Color(0xFFFFFAF7)
private val Ink = Color(0xFF33313A)
private val Pink = Color(0xFFF8DDE5)
private val Mint = Color(0xFFDDF2E7)
private val Yellow = Color(0xFFFFEFC2)
private val Blue = Color(0xFFDDEBFA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF95647B), background = Canvas, surface = Color.White)) { PastelLifeApp() } }
    }
}

@Composable
private fun PastelLifeApp() {
    var tab by remember { mutableStateOf("Home") }
    var taskText by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(listOf("Plan the week", "Review monthly spending", "Drink enough water")) }
    var income by remember { mutableStateOf("0") }
    var expense by remember { mutableStateOf("0") }
    val available = (income.toDoubleOrNull() ?: 0.0) - (expense.toDoubleOrNull() ?: 0.0)
    Scaffold(containerColor = Canvas, bottomBar = {
        NavigationBar(containerColor = Color.White) {
            listOf("Home", "Tasks", "Finance").forEach { item ->
                NavigationBarItem(selected = tab == item, onClick = { tab = item },
                    icon = { Icon(when(item) { "Tasks" -> Icons.Default.TaskAlt; "Finance" -> Icons.Default.AccountBalanceWallet; else -> Icons.Default.Home }, contentDescription = item) },
                    label = { Text(item) })
            }
        }
    }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 26.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("PastelLife", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("A little more organised, every day.", color = Ink.copy(alpha = .65f), fontSize = 14.sp)
            }
            when (tab) {
                "Home" -> {
                    item { Surface(color = Pink, shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(22.dp)) {
                            Text("Your available balance", color = Ink.copy(alpha = .75f))
                            Text("PKR ${"%,.0f".format(available)}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Based on amounts entered in Finance", fontSize = 12.sp, color = Ink.copy(alpha = .65f))
                        }
                    } }
                    item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard("Tasks left", tasks.size.toString(), Mint, Modifier.weight(1f))
                        MetricCard("This month", "PKR ${"%,.0f".format(available)}", Yellow, Modifier.weight(1f))
                    } }
                    item { Text("Today's focus", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink) }
                    items(tasks) { task -> TaskRow(task) { tasks = tasks.filterNot { it == task } } }
                }
                "Tasks" -> {
                    item { Text("Your tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink) }
                    item { OutlinedTextField(value = taskText, onValueChange = { taskText = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Add a task") }, shape = RoundedCornerShape(18.dp), trailingIcon = {
                            TextButton(onClick = { if (taskText.isNotBlank()) { tasks = tasks + taskText.trim(); taskText = "" } }) { Text("Add") }
                        }) }
                    items(tasks) { task -> TaskRow(task) { tasks = tasks.filterNot { it == task } } }
                    if (tasks.isEmpty()) item { Text("You're all caught up. Add a task whenever you're ready!", color = Ink.copy(alpha = .7f)) }
                }
                else -> {
                    item { Text("Finance overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink) }
                    item { Surface(color = Mint, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Quick entry (PKR)", fontWeight = FontWeight.SemiBold, color = Ink)
                            OutlinedTextField(value = income, onValueChange = { income = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Income total") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = expense, onValueChange = { expense = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Expense total") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Text("Net recorded: PKR ${"%,.0f".format(available)}", fontWeight = FontWeight.Bold, color = Ink)
                            Text("Starter demo only: these quick-entry values are not saved after closing the app.", fontSize = 12.sp, color = Ink.copy(alpha = .7f))
                        }
                    } }
                    item { MetricCard("Planned next", "Accounts, transfers, contacts & history", Blue, Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color, shape = RoundedCornerShape(22.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontSize = 13.sp, color = Ink.copy(alpha = .75f))
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}

@Composable
private fun TaskRow(title: String, onDone: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Complete task", tint = Color(0xFF77A98D))
            Spacer(Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f), color = Ink)
            TextButton(onClick = onDone) { Text("Done") }
        }
    }
}
