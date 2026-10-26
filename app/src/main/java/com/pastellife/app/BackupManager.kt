package com.pastellife.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.withTransaction
import com.pastellife.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun BackupManager(database: AppDatabase) {
    val context = LocalContext.current
    val dao = remember { database.appDao() }
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var confirmRestore by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            status = try {
                withContext(Dispatchers.IO) {
                    val json = buildBackup(dao)
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                        ?: error("Could not open the selected file.")
                }
                "Backup exported. Keep the file somewhere safe."
            } catch (e: Exception) { "Backup failed: " + (e.message ?: "unknown error") }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            status = try {
                withContext(Dispatchers.IO) {
                    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Could not read the selected file.")
                    restoreBackup(database, JSONObject(raw))
                }
                "Backup restored on this device."
            } catch (e: Exception) { "Restore failed: " + (e.message ?: "invalid backup") }
        }
    }

    Surface(color = Color(0xFFECE5F7), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Backup & restore", style = MaterialTheme.typography.titleMedium, color = Color(0xFF33313A))
            Text("Export a copy of your local data as a JSON file, or restore a backup. Restoring replaces the data currently on this device.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF55515E))
            Button(onClick = { exportLauncher.launch("pastellife-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("Export backup") }
            OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.fillMaxWidth()) { Text("Restore backup") }
            if (status.isNotBlank()) Text(status, color = Color(0xFF397A55), style = MaterialTheme.typography.bodySmall)
            if (confirmRestore) {
                AlertDialog(
                    onDismissRequest = { confirmRestore = false },
                    title = { Text("Replace current data?") },
                    text = { Text("Restoring a backup replaces all tasks, accounts, transactions, contacts, debts and savings goals on this phone. Export a backup first if you need to keep the current data.") },
                    confirmButton = { TextButton(onClick = { confirmRestore = false; importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }) { Text("Continue") } },
                    dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel") } }
                )
            }
        }
    }
}

private suspend fun buildBackup(dao: AppDao): String {
    val root = JSONObject().put("format", "PastelLifeBackup").put("version", 1)
    root.put("tasks", JSONArray().also { a -> dao.backupTaskEntity().forEach { t -> a.put(JSONObject()
        .put("id",t.id).put("title",t.title).put("isDone",t.isDone).put("kind",t.kind).put("priority",t.priority)
        .put("dueDate",t.dueDate).put("repeatRule",t.repeatRule).put("trackedSeconds",t.trackedSeconds)
        .put("timerStartedAt",t.timerStartedAt ?: JSONObject.NULL).put("notes",t.notes).put("createdAt",t.createdAt)) } })
    root.put("accounts", JSONArray().also { a -> dao.backupAccountEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("name",x.name).put("openingBalance",x.openingBalance).put("currentBalance",x.currentBalance).put("accountType",x.accountType)) } })
    root.put("transactions", JSONArray().also { a -> dao.backupTransactionEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("accountId",x.accountId).put("amount",x.amount).put("type",x.type).put("category",x.category).put("note",x.note).put("createdAt",x.createdAt)) } })
    root.put("contacts", JSONArray().also { a -> dao.backupContactEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("name",x.name).put("phone",x.phone).put("notes",x.notes)) } })
    root.put("debts", JSONArray().also { a -> dao.backupDebtEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("contactId",x.contactId).put("originalAmount",x.originalAmount).put("remainingAmount",x.remainingAmount).put("direction",x.direction).put("dueDate",x.dueDate).put("note",x.note)) } })
    root.put("payments", JSONArray().also { a -> dao.backupDebtPaymentEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("debtId",x.debtId).put("amount",x.amount).put("direction",x.direction).put("note",x.note).put("createdAt",x.createdAt)) } })
    root.put("savingsGoals", JSONArray().also { a -> dao.backupSavingsGoalEntity().forEach { x -> a.put(JSONObject().put("id",x.id).put("title",x.title).put("targetAmount",x.targetAmount).put("currentAmount",x.currentAmount).put("dueDate",x.dueDate).put("note",x.note)) } })
    return root.toString(2)
}

private suspend fun restoreBackup(database: AppDatabase, root: JSONObject) {
    require(root.optString("format") == "PastelLifeBackup") { "This is not a PastelLife backup." }
    val dao = database.appDao()
    fun array(name: String) = root.optJSONArray(name) ?: JSONArray()
    fun objects(name: String, parse: (JSONObject) -> Unit) {
        val a = array(name)
        for (i in 0 until a.length()) a.optJSONObject(i)?.let(parse)
    }
    val tasks = mutableListOf<TaskEntity>()
    objects("tasks") { o -> tasks += TaskEntity(o.optLong("id"),o.optString("title"),o.optBoolean("isDone"),o.optString("kind","Personal"),o.optString("priority","Medium"),o.optString("dueDate"),o.optString("repeatRule","None"),o.optLong("trackedSeconds"),if(o.isNull("timerStartedAt")) null else o.optLong("timerStartedAt"),o.optString("notes"),o.optLong("createdAt",System.currentTimeMillis())) }
    val accounts = mutableListOf<AccountEntity>()
    objects("accounts") { o -> accounts += AccountEntity(o.optLong("id"),o.optString("name"),o.optLong("openingBalance"),o.optLong("currentBalance",o.optLong("openingBalance")),o.optString("accountType","Cash")) }
    val transactions = mutableListOf<TransactionEntity>()
    objects("transactions") { o -> transactions += TransactionEntity(o.optLong("id"),o.optLong("accountId"),o.optLong("amount"),o.optString("type"),o.optString("category"),o.optString("note"),o.optLong("createdAt",System.currentTimeMillis())) }
    val contacts = mutableListOf<ContactEntity>()
    objects("contacts") { o -> contacts += ContactEntity(o.optLong("id"),o.optString("name"),o.optString("phone"),o.optString("notes")) }
    val debts = mutableListOf<DebtEntity>()
    objects("debts") { o -> debts += DebtEntity(o.optLong("id"),o.optLong("contactId"),o.optLong("originalAmount"),o.optLong("remainingAmount"),o.optString("direction"),o.optString("dueDate"),o.optString("note")) }
    val payments = mutableListOf<DebtPaymentEntity>()
    objects("payments") { o -> payments += DebtPaymentEntity(o.optLong("id"),o.optLong("debtId"),o.optLong("amount"),o.optString("direction"),o.optString("note"),o.optLong("createdAt",System.currentTimeMillis())) }
    val goals = mutableListOf<SavingsGoalEntity>()
    objects("savingsGoals") { o -> goals += SavingsGoalEntity(o.optLong("id"),o.optString("title"),o.optLong("targetAmount"),o.optLong("currentAmount"),o.optString("dueDate"),o.optString("note")) }

    database.withTransaction {
        dao.clearTaskEntity(); dao.clearAccountEntity(); dao.clearTransactionEntity(); dao.clearContactEntity()
        dao.clearDebtEntity(); dao.clearDebtPaymentEntity(); dao.clearSavingsGoalEntity()
        if(tasks.isNotEmpty()) dao.restoreTaskEntity(tasks)
        if(accounts.isNotEmpty()) dao.restoreAccountEntity(accounts)
        if(transactions.isNotEmpty()) dao.restoreTransactionEntity(transactions)
        if(contacts.isNotEmpty()) dao.restoreContactEntity(contacts)
        if(debts.isNotEmpty()) dao.restoreDebtEntity(debts)
        if(payments.isNotEmpty()) dao.restoreDebtPaymentEntity(payments)
        if(goals.isNotEmpty()) dao.restoreSavingsGoalEntity(goals)
    }
}
