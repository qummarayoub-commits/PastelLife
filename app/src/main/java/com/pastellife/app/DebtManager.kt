package com.pastellife.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pastellife.app.data.*
import kotlinx.coroutines.launch

private val DebtInk = Color(0xFF33313A)
private val DebtMint = Color(0xFFDDF2E7)
private val DebtBlue = Color(0xFFDDEBFA)
private fun debtMoney(value: Long) = "PKR " + java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "PK")).format(value)

@Composable
fun DebtManager(dao: AppDao) {
    val scope = rememberCoroutineScope()
    val contacts by dao.observeContacts().collectAsState(initial = emptyList())
    val accounts by dao.observeAccounts().collectAsState(initial = emptyList())
    val debts by dao.observeDebts().collectAsState(initial = emptyList())
    val payments by dao.observeAllPayments().collectAsState(initial = emptyList())
    var chosenContact by remember { mutableStateOf<Long?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var direction by remember { mutableStateOf("RECEIVABLE") }
    var debtAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var debtNote by remember { mutableStateOf("") }
    var paymentDebtId by remember { mutableStateOf<Long?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentNote by remember { mutableStateOf("") }
    var paymentMenu by remember { mutableStateOf(false) }
    var accountMenuOpen by remember { mutableStateOf(false) }
    var paymentAccountId by remember { mutableStateOf<Long?>(null) }
    var status by remember { mutableStateOf("") }
    val selectedContact = contacts.firstOrNull { it.id == chosenContact } ?: contacts.firstOrNull()
    val selectedDebt = debts.firstOrNull { it.id == paymentDebtId } ?: debts.firstOrNull { it.remainingAmount > 0L }
    val paymentAccount = accounts.firstOrNull { it.id == paymentAccountId } ?: accounts.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Receivables & payables", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DebtInk)
        Text("Money owed to you is a receivable. Money you owe is a payable.", fontSize = 12.sp, color = DebtInk.copy(alpha=.65f))
        if (contacts.isEmpty()) {
            Surface(color = DebtBlue, shape = RoundedCornerShape(16.dp)) {
                Text("Add a contact above before recording money owed.", Modifier.padding(14.dp), color = DebtInk)
            }
        } else {
            Surface(color = DebtBlue, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Add an amount owed", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DebtInk)
                    Box {
                        OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Contact: " + (selectedContact?.name ?: "Choose contact"))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            contacts.forEach { person -> DropdownMenuItem(text = { Text(person.name) }, onClick = { chosenContact = person.id; menuOpen = false }) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = direction == "RECEIVABLE", onClick = { direction = "RECEIVABLE" }, label = { Text("They owe me") })
                        FilterChip(selected = direction == "PAYABLE", onClick = { direction = "PAYABLE" }, label = { Text("I owe them") })
                    }
                    OutlinedTextField(value = debtAmount, onValueChange = { debtAmount = it.filter(Char::isDigit) }, label = { Text("Amount (PKR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due date (YYYY-MM-DD, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = debtNote, onValueChange = { debtNote = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        val value = debtAmount.toLongOrNull()
                        if (selectedContact == null || value == null || value <= 0L) status = "Choose a contact and enter an amount greater than zero."
                        else scope.launch {
                            dao.addDebt(DebtEntity(contactId = selectedContact.id, originalAmount = value, remainingAmount = value, direction = direction, dueDate = dueDate.trim(), note = debtNote.trim()))
                            debtAmount = ""; dueDate = ""; debtNote = ""; status = "Amount owed saved."
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Save amount owed") }
                }
            }
        }

        Text("Open balances", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DebtInk)
        if (debts.isEmpty()) Text("No receivables or payables recorded yet.", color = DebtInk.copy(alpha=.65f), fontSize = 13.sp)
        debts.forEach { debt ->
            val person = contacts.firstOrNull { it.id == debt.contactId }?.name ?: "Unknown contact"
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(person, fontWeight = FontWeight.SemiBold, color = DebtInk)
                    Text(if (debt.direction == "RECEIVABLE") "They owe you" else "You owe them", fontSize = 12.sp, color = DebtInk.copy(alpha=.65f))
                    Text("Remaining: " + debtMoney(debt.remainingAmount), fontWeight = FontWeight.Bold, color = DebtInk)
                    if (debt.dueDate.isNotBlank()) Text("Due: " + debt.dueDate, fontSize = 12.sp, color = DebtInk.copy(alpha=.65f))
                    if (debt.note.isNotBlank()) Text(debt.note, fontSize = 12.sp, color = DebtInk.copy(alpha=.65f))
                }
            }
        }

        if (debts.any { it.remainingAmount > 0L }) {
            Surface(color = DebtMint, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Record partial repayment", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DebtInk)
                    Box {
                        OutlinedButton(onClick = { paymentMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedDebt?.let { d0 -> (contacts.firstOrNull { it.id == d0.contactId }?.name ?: "Contact") + " — " + debtMoney(d0.remainingAmount) } ?: "Choose balance")
                        }
                        DropdownMenu(expanded = paymentMenu, onDismissRequest = { paymentMenu = false }) {
                            debts.filter { it.remainingAmount > 0L }.forEach { d0 ->
                                DropdownMenuItem(text = { Text((contacts.firstOrNull { it.id == d0.contactId }?.name ?: "Contact") + " — " + debtMoney(d0.remainingAmount)) }, onClick = { paymentDebtId = d0.id; paymentMenu = false })
                            }
                        }
                    }
                    Box {
                        OutlinedButton(onClick = { accountMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Account: " + (paymentAccount?.name ?: "Choose account"))
                        }
                        DropdownMenu(expanded = accountMenuOpen, onDismissRequest = { accountMenuOpen = false }) {
                            accounts.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { paymentAccountId = account.id; accountMenuOpen = false }) }
                        }
                    }
                    OutlinedTextField(value = paymentAmount, onValueChange = { paymentAmount = it.filter(Char::isDigit) }, label = { Text("Payment amount (PKR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = paymentNote, onValueChange = { paymentNote = it }, label = { Text("Payment note (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        val target = debts.firstOrNull { it.id == (paymentDebtId ?: selectedDebt?.id) }
                        val value = paymentAmount.toLongOrNull()
                        if (target == null || value == null || value <= 0L || value > target.remainingAmount) status = "Enter a payment up to the remaining balance."
                        else if (paymentAccount == null) status = "Create an account before recording a repayment."
                        else if (target.direction == "PAYABLE" && paymentAccount.currentBalance < value) status = "The selected account has insufficient balance."
                        else scope.launch {
                            val received = target.direction == "RECEIVABLE"
                            val person = contacts.firstOrNull { it.id == target.contactId }?.name ?: "contact"
                            val updatedAccount = paymentAccount.copy(currentBalance = paymentAccount.currentBalance + (if (received) value else -value))
                            val transaction = TransactionEntity(accountId = paymentAccount.id, amount = value, type = "Debt", category = if (received) "Repayment received from " + person else "Repayment paid to " + person, note = paymentNote.trim())
                            dao.recordDebtPaymentWithAccount(
                                DebtPaymentEntity(debtId = target.id, amount = value, direction = if (received) "RECEIVED" else "PAID", note = paymentNote.trim()),
                                target.copy(remainingAmount = target.remainingAmount - value),
                                updatedAccount,
                                transaction
                            )
                            paymentAmount = ""; paymentNote = ""; status = "Repayment saved and account balance updated."
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Save repayment") }
                }
            }
        }
        Text("Payment history", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DebtInk)
        if (payments.isEmpty()) Text("Partial and full repayments will appear here.", color = DebtInk.copy(alpha=.65f), fontSize = 13.sp)
        payments.forEach { payment ->
            val debt = debts.firstOrNull { it.id == payment.debtId }
            val person = contacts.firstOrNull { it.id == debt?.contactId }?.name ?: "Contact"
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(person, fontWeight = FontWeight.Medium, color = DebtInk); Text(payment.direction, fontSize = 12.sp, color = DebtInk.copy(alpha=.6f)); if(payment.note.isNotBlank()) Text(payment.note, fontSize=12.sp,color=DebtInk.copy(alpha=.6f)); Text(java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date(payment.createdAt)),fontSize=11.sp,color=DebtInk.copy(alpha=.5f)) }
                    Text(debtMoney(payment.amount), fontWeight = FontWeight.SemiBold, color = DebtInk)
                }
            }
        }
        if (status.isNotBlank()) Text(status, color = Color(0xFF397A55), fontSize = 12.sp)
    }
}
