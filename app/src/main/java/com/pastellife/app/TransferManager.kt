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

private fun transferMoney(value: Long) = "PKR " + NumberFormat.getNumberInstance(Locale("en", "PK")).format(value)

@Composable
fun TransferManager(dao: AppDao) {
    val scope = rememberCoroutineScope()
    val accounts by dao.observeAccounts().collectAsState(initial = emptyList())
    var fromId by remember { mutableStateOf<Long?>(null) }
    var toId by remember { mutableStateOf<Long?>(null) }
    var fromMenu by remember { mutableStateOf(false) }
    var toMenu by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val from = accounts.firstOrNull { it.id == fromId } ?: accounts.getOrNull(0)
    val to = accounts.firstOrNull { it.id == toId } ?: accounts.getOrNull(1)
    Surface(color = Color(0xFFECE5F7), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Transfer between your accounts", color = Color(0xFF33313A), style = MaterialTheme.typography.titleMedium)
            Text("Transfers change account balances but do not count as income or expenses.", color = Color(0xFF55515E), style = MaterialTheme.typography.bodySmall)
            Box {
                OutlinedButton(onClick = { fromMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("From: " + (from?.name ?: "Choose account")) }
                DropdownMenu(expanded = fromMenu, onDismissRequest = { fromMenu = false }) {
                    accounts.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { fromId = account.id; fromMenu = false }) }
                }
            }
            Box {
                OutlinedButton(onClick = { toMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("To: " + (to?.name ?: "Choose account")) }
                DropdownMenu(expanded = toMenu, onDismissRequest = { toMenu = false }) {
                    accounts.filter { it.id != from?.id }.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { toId = account.id; toMenu = false }) }
                }
            }
            OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter(Char::isDigit) }, label = { Text("Amount (PKR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                if (from == null || to == null || from.id == to.id || amount == null || amount <= 0L) status = "Choose two different accounts and enter a valid amount."
                else if (from.currentBalance < amount) status = "The source account has insufficient balance."
                else scope.launch {
                    dao.recordTransfer(from, to, amount)
                    amountText = ""; status = "Transfer saved."
                }
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Transfer") }
            if (status.isNotBlank()) Text(status, color = Color(0xFF397A55), style = MaterialTheme.typography.bodySmall)
        }
    }
}
