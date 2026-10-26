package com.pastellife.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pastellife.app.data.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private fun goalMoney(v: Long) = "PKR " + NumberFormat.getNumberInstance(Locale("en", "PK")).format(v)

@Composable
fun SavingsGoalsManager(dao: AppDao) {
    val scope = rememberCoroutineScope()
    val goals by dao.observeSavingsGoals().collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var contribution by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val selected = goals.firstOrNull { it.id == selectedId } ?: goals.firstOrNull { it.currentAmount < it.targetAmount }

    Surface(color = Color(0xFFFFEFC2), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Savings goals", style = MaterialTheme.typography.titleMedium, color = Color(0xFF33313A))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = target, onValueChange = { target = it.filter(Char::isDigit) }, label = { Text("Target amount (PKR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Target date (YYYY-MM-DD, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note or saving method") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                val value = target.toLongOrNull()
                if (title.isBlank() || value == null || value <= 0L) status = "Enter a goal name and target amount."
                else scope.launch { dao.addSavingsGoal(SavingsGoalEntity(title = title.trim(), targetAmount = value, dueDate = dueDate.trim(), note = note.trim())); title = ""; target = ""; dueDate = ""; note = ""; status = "Savings goal saved." }
            }, modifier = Modifier.fillMaxWidth()) { Text("Create goal") }
        }
    }
    if (goals.isNotEmpty()) {
        Text("Your goals", style = MaterialTheme.typography.titleMedium, color = Color(0xFF33313A))
        goals.forEach { goal ->
            val progress = (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(goal.title, color = Color(0xFF33313A), style = MaterialTheme.typography.titleSmall)
                    Text(goalMoney(goal.currentAmount) + " of " + goalMoney(goal.targetAmount), color = Color(0xFF55515E))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    if (goal.dueDate.isNotBlank()) Text("Target date: " + goal.dueDate, color = Color(0xFF55515E), style = MaterialTheme.typography.bodySmall)
                    if (goal.note.isNotBlank()) Text(goal.note, color = Color(0xFF55515E), style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { scope.launch { dao.deleteSavingsGoal(goal.id) } }) { Text("Delete") }
                    }
                }
            }
        }
        Surface(color = Color(0xFFDDF2E7), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Add to a goal", style = MaterialTheme.typography.titleSmall, color = Color(0xFF33313A))
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) { Text(selected?.title ?: "Choose goal") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        goals.filter { it.currentAmount < it.targetAmount }.forEach { goal ->
                            DropdownMenuItem(text = { Text(goal.title) }, onClick = { selectedId = goal.id; menuOpen = false })
                        }
                    }
                }
                OutlinedTextField(value = contribution, onValueChange = { contribution = it.filter(Char::isDigit) }, label = { Text("Contribution (PKR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    val value = contribution.toLongOrNull()
                    if (selected == null || value == null || value <= 0L) status = "Choose a goal and enter a positive amount."
                    else scope.launch { dao.updateSavingsGoal(selected.copy(currentAmount = (selected.currentAmount + value).coerceAtMost(selected.targetAmount))); contribution = ""; status = "Goal contribution saved." }
                }, modifier = Modifier.fillMaxWidth()) { Text("Save contribution") }
            }
        }
    }
    if (status.isNotBlank()) Text(status, color = Color(0xFF397A55), style = MaterialTheme.typography.bodySmall)
}
