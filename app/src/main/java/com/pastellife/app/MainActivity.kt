package com.pastellife.app
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pastellife.app.data.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.concurrent.TimeUnit

private val Canvas=Color(0xFFFFFAF7); private val Ink=Color(0xFF33313A)
private val Pink=Color(0xFFF8DDE5); private val Mint=Color(0xFFDDF2E7)
private val Yellow=Color(0xFFFFEFC2); private val Blue=Color(0xFFDDEBFA)
private fun money(v:Long)="PKR "+NumberFormat.getNumberInstance(Locale("en","PK")).format(v)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  setContent { MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF95647B),background=Canvas,surface=Color.White)){ PastelLifeApp() } }
 }
}
@Composable private fun PastelLifeApp(){
 val context= LocalContext.current; val dao= remember { AppDatabase.get(context).appDao() }; val scope= rememberCoroutineScope()
 val tasks by dao.observeTasks().collectAsState(initial= emptyList())
 val accounts by dao.observeAccounts().collectAsState(initial= emptyList())
 val transactions by dao.observeTransactions().collectAsState(initial= emptyList())
 val contacts by dao.observeContacts().collectAsState(initial= emptyList())
 val attachments by dao.observeTaskAttachments().collectAsState(initial= emptyList())
 val debts by dao.observeDebts().collectAsState(initial= emptyList())
 var tab by remember { mutableStateOf("Home") }; var task by remember { mutableStateOf("") }; var taskNotes by remember { mutableStateOf("") }; var projectName by remember { mutableStateOf("") }; var taskDueDate by remember { mutableStateOf("") }; var repeatRule by remember { mutableStateOf("None") }; var taskKind by remember { mutableStateOf("Personal") }; var taskPriority by remember { mutableStateOf("Medium") }
 var accountName by remember { mutableStateOf("") }; var opening by remember { mutableStateOf("") }; var accountType by remember { mutableStateOf("Cash") }; var editingAccountId by remember { mutableStateOf<Long?>(null) }; var editAccountName by remember { mutableStateOf("") }; var editAccountType by remember { mutableStateOf("Cash") }; var selectedAccountId by remember { mutableStateOf<Long?>(null) }; var accountMenuOpen by remember { mutableStateOf(false) }
 var amount by remember { mutableStateOf("") }; var category by remember { mutableStateOf("General") }
 var note by remember { mutableStateOf("") }; var txType by remember { mutableStateOf("Expense") }
 var contact by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var contactNotes by remember { mutableStateOf("") }; var editingContactId by remember { mutableStateOf<Long?>(null) }; var editContactName by remember { mutableStateOf("") }; var editContactPhone by remember { mutableStateOf("") }; var editContactNotes by remember { mutableStateOf("") }
 var message by remember { mutableStateOf("") }; var attachmentTaskId by remember { mutableStateOf<Long?>(null) }; var editingTaskId by remember { mutableStateOf<Long?>(null) }; var editTaskTitle by remember { mutableStateOf("") }; var editTaskNotes by remember { mutableStateOf("") }; var editTaskDueDate by remember { mutableStateOf("") }
 val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
 val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
  if(uri != null && attachmentTaskId != null) {
   runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
   val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor -> if(cursor.moveToFirst()) cursor.getString(0) else null } ?: uri.lastPathSegment ?: "Attachment"
   val targetId = attachmentTaskId!!
   scope.launch { dao.addTaskAttachment(TaskAttachmentEntity(taskId=targetId,uri=uri.toString(),displayName=displayName)); message="Attachment added." }
  }
 }
 LaunchedEffect(Unit){
  if(Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
   notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
  }
  val now = System.currentTimeMillis()
  val next = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,9); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0); if(timeInMillis <= now) add(Calendar.DAY_OF_YEAR,1) }
  val delay = (next.timeInMillis - now).coerceAtLeast(0L)
  val request = PeriodicWorkRequestBuilder<DailyTaskReminderWorker>(24,TimeUnit.HOURS).setInitialDelay(delay,TimeUnit.MILLISECONDS).build()
  WorkManager.getInstance(context).enqueueUniquePeriodicWork("pastellife-daily-reminder",ExistingPeriodicWorkPolicy.UPDATE,request)
 }
 val balance=FinanceCalculations.availableBalance(accounts); val income=transactions.filter{it.type=="Income"}.sumOf{it.amount}
 val expenses=transactions.filter{it.type=="Expense"}.sumOf{it.amount}
 val monthStart=Calendar.getInstance().apply{set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 val weekStart=Calendar.getInstance().apply{set(Calendar.DAY_OF_WEEK,firstDayOfWeek);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 val monthTx=transactions.filter{it.createdAt>=monthStart}
 val weekTx=transactions.filter{it.createdAt>=weekStart}
 val monthIncome=monthTx.filter{it.type=="Income"}.sumOf{it.amount}
 val monthExpenses=monthTx.filter{it.type=="Expense"}.sumOf{it.amount}
 val weekIncome=weekTx.filter{it.type=="Income"}.sumOf{it.amount}
 val weekExpenses=weekTx.filter{it.type=="Expense"}.sumOf{it.amount}
 val savings=FinanceCalculations.netSavings(monthTx)
 val netPosition=FinanceCalculations.netFinancialPosition(accounts,debts)
 Scaffold(containerColor=Canvas,bottomBar={ NavigationBar(containerColor=Color.White){
  listOf("Home","Tasks","Calendar","Finance","Contacts","Reports").forEach{ label-> NavigationBarItem(selected=tab==label,onClick={tab=label;message=""},icon={
   Icon(when(label){"Tasks"->Icons.Default.TaskAlt;"Calendar"->Icons.Default.DateRange;"Finance"->Icons.Default.AccountBalanceWallet;"Contacts"->Icons.Default.People;"Reports"->Icons.Default.Assessment;else->Icons.Default.Home},contentDescription=label)
  },label={Text(label)})}
 }}){ pad->
  LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal=20.dp),contentPadding=PaddingValues(top=24.dp,bottom=28.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   item{Text("PastelLife",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Ink);Text("A little more organised, every day.",color=Ink.copy(alpha=.65f))}
   if(message.isNotBlank())item{Text(message,color=Color(0xFF397A55),fontSize=13.sp)}
   when(tab){
    "Home"->{
     item{CardBlock(Pink){Text("Available balance",color=Ink.copy(alpha=.7f));Text(money(balance),fontSize=29.sp,fontWeight=FontWeight.Bold,color=Ink);Text("Across "+accounts.size+" account(s)",fontSize=12.sp,color=Ink.copy(alpha=.6f))}}
     item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Tasks to do",tasks.count{!it.isDone}.toString(),Mint,Modifier.weight(1f));Metric("Net savings",money(savings),Yellow,Modifier.weight(1f))}}
     item{Metric("Net financial position",money(netPosition),Blue,Modifier.fillMaxWidth())}
     item{Text("Today's focus",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     if(tasks.none{!it.isDone})item{Hint("No tasks yet. Add your first task in Tasks.")}
     items(tasks.filter{!it.isDone}.take(5),key={it.id}){t->TaskLine(t.title){scope.launch{completeTaskAndRepeat(context,dao,t)}}}
     item{Text("Recent transactions",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     if(transactions.isEmpty())item{Hint("Your income and expenses will appear here.")}
     items(transactions.take(4),key={it.id}){tx->TxLine(tx,accounts.firstOrNull{account->account.id==tx.accountId}?.name ?: "Account")}
    }
    "Tasks"->{
     item{Text("Your tasks",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)}
     item{Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
      OutlinedTextField(value=task,onValueChange={task=it},modifier=Modifier.fillMaxWidth(),label={Text("What do you need to do?")},singleLine=true,shape=RoundedCornerShape(18.dp))
      OutlinedTextField(value=taskNotes,onValueChange={taskNotes=it},modifier=Modifier.fillMaxWidth(),label={Text("Notes (optional)")},minLines=2)
      if(taskKind=="Project") OutlinedTextField(value=projectName,onValueChange={projectName=it},modifier=Modifier.fillMaxWidth(),label={Text("Project name")},singleLine=true)
      OutlinedTextField(value=taskDueDate,onValueChange={taskDueDate=it},modifier=Modifier.fillMaxWidth(),label={Text("Due date (YYYY-MM-DD, optional)")},singleLine=true)
      Text("Category",color=Ink,fontWeight=FontWeight.Medium)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Personal","Work","Project").forEach{kind->FilterChip(selected=taskKind==kind,onClick={taskKind=kind},label={Text(kind)})}}
      Text("Priority",color=Ink,fontWeight=FontWeight.Medium)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Low","Medium","High").forEach{priority->FilterChip(selected=taskPriority==priority,onClick={taskPriority=priority},label={Text(priority)})}}
      Text("Repeat",color=Ink,fontWeight=FontWeight.Medium)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("None","Daily","Weekly").forEach{rule->FilterChip(selected=repeatRule==rule,onClick={repeatRule=rule},label={Text(rule)})}}
      Button(onClick={if(task.isNotBlank()){val title=task.trim();scope.launch{val taskId=dao.addTask(TaskEntity(title=title,kind=taskKind,priority=taskPriority,dueDate=taskDueDate.trim(),repeatRule=repeatRule,notes=taskNotes.trim(),projectName=if(taskKind=="Project") projectName.trim().ifBlank{null} else null));TaskReminderScheduler.schedule(context,taskId,title,taskDueDate.trim());task="";taskNotes="";projectName="";taskDueDate="";repeatRule="None";taskKind="Personal";taskPriority="Medium";message="Task saved on this device."}}else message="Enter a task first."},modifier=Modifier.fillMaxWidth()){Text("Add task")}
     }}
     if(tasks.isEmpty())item{Hint("Your task list is empty.")}
     items(tasks,key={it.id}){t->Surface(color=Color.White,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
      Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(checked=t.isDone,onCheckedChange={v->scope.launch{if(v) completeTaskAndRepeat(context,dao,t) else {dao.updateTask(t.copy(isDone=false));TaskReminderScheduler.schedule(context,t.id,t.title,t.dueDate)}}})
       Column(Modifier.weight(1f)){
        Text(t.title,color=Ink,fontWeight=FontWeight.Medium)
        if(t.notes.isNotBlank()) Text(t.notes,fontSize=12.sp,color=Ink.copy(alpha=.65f))
        Text(if(t.isDone)"Completed" else "To do",fontSize=12.sp,color=Ink.copy(alpha=.6f))
        Text(t.kind+" • "+t.priority+" priority"+(t.projectName?.let{" • "+it} ?: ""),fontSize=12.sp,color=Ink.copy(alpha=.65f))
        if(t.dueDate.isNotBlank()) Text("Due: "+t.dueDate,fontSize=12.sp,color=Ink.copy(alpha=.6f))
        Text(if(t.timerStartedAt!=null) "Timer running • tracked "+durationLabel(t.trackedSeconds) else "Tracked: "+durationLabel(t.trackedSeconds),fontSize=12.sp,color=Ink.copy(alpha=.7f))
        TextButton(onClick={attachmentTaskId=t.id;attachmentPicker.launch(arrayOf("*/*"))}){Text("Attach file")}
        attachments.filter{it.taskId==t.id}.forEach{att->
         Row(verticalAlignment=Alignment.CenterVertically){
          TextButton(onClick={runCatching{val fileUri=Uri.parse(att.uri);val mime=context.contentResolver.getType(fileUri) ?: "*/*";context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(fileUri,mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}.onFailure{message="Could not open attachment."}}){Text(att.displayName,maxLines=1)}
          TextButton(onClick={scope.launch{dao.deleteTaskAttachment(att.id)}}){Text("Remove")}
         }
        }
       }
       TextButton(onClick={editingTaskId=t.id;editTaskTitle=t.title;editTaskNotes=t.notes;editTaskDueDate=t.dueDate}){Text("Edit")}
       TextButton(onClick={scope.launch{
        val now=System.currentTimeMillis()
        if(t.timerStartedAt==null) dao.updateTask(t.copy(timerStartedAt=now))
        else dao.updateTask(t.copy(timerStartedAt=null,trackedSeconds=t.trackedSeconds+((now-t.timerStartedAt)/1000L).coerceAtLeast(0L)))
       }}){Text(if(t.timerStartedAt==null)"Start" else "Stop")}
       TextButton(onClick={scope.launch{TaskReminderScheduler.cancel(context,t.id);dao.deleteTaskAndAttachments(t.id)}}){Text("Delete")}
      }
      if(editingTaskId==t.id) AlertDialog(
       onDismissRequest={editingTaskId=null},
       title={Text("Edit task")},
       text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(value=editTaskTitle,onValueChange={editTaskTitle=it},label={Text("Task title")},modifier=Modifier.fillMaxWidth(),singleLine=true)
        OutlinedTextField(value=editTaskNotes,onValueChange={editTaskNotes=it},label={Text("Notes")},modifier=Modifier.fillMaxWidth(),minLines=2)
        OutlinedTextField(value=editTaskDueDate,onValueChange={editTaskDueDate=it},label={Text("Due date (YYYY-MM-DD)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
       }},
       confirmButton={TextButton(onClick={val title=editTaskTitle.trim();if(title.isBlank())message="Task title cannot be empty." else scope.launch{dao.updateTask(t.copy(title=title,notes=editTaskNotes.trim(),dueDate=editTaskDueDate.trim()));TaskReminderScheduler.schedule(context,t.id,title,editTaskDueDate.trim());editingTaskId=null;message="Task updated."}}){Text("Save changes")}},
       dismissButton={TextButton(onClick={editingTaskId=null}){Text("Cancel")}}
      )
     }}
    }
    "Calendar"->{
     item{Text("Calendar & due dates",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)}
     item{CardBlock(Blue){Text("Agenda view",fontWeight=FontWeight.SemiBold,color=Ink);Text("Tasks with due dates are listed below. Enter dates as YYYY-MM-DD when adding a task.",fontSize=12.sp,color=Ink.copy(alpha=.7f))}}
     val scheduled=tasks.filter{it.dueDate.isNotBlank()}.sortedBy{it.dueDate}
     if(scheduled.isEmpty()) item{Hint("No dated tasks yet. Add a due date in the Tasks tab.")}
     items(scheduled,key={it.id}){t->CardBlock(Color.White){
      Text(t.dueDate,fontWeight=FontWeight.SemiBold,color=Color(0xFF95647B))
      Text(t.title,color=Ink)
      Text((if(t.isDone)"Completed" else "Pending")+(if(t.repeatRule!="None")" • "+t.repeatRule+" repeat" else ""),fontSize=12.sp,color=Ink.copy(alpha=.65f))
      if(!t.isDone) TextButton(onClick={scope.launch{completeTaskAndRepeat(context,dao,t)}}){Text("Complete")}
     }}
    }
    "Finance"->{
     item{Text("Finance overview",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)}
     item{CardBlock(Pink){Text("Available balance",color=Ink.copy(alpha=.7f));Text(money(balance),fontSize=27.sp,fontWeight=FontWeight.Bold,color=Ink);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Income",money(income),Mint,Modifier.weight(1f));Metric("Expenses",money(expenses),Yellow,Modifier.weight(1f))};Text("Net savings: "+money(savings),fontWeight=FontWeight.SemiBold,color=Ink);Text("Net financial position: "+money(netPosition),fontWeight=FontWeight.SemiBold,color=Ink)}}
     item{Text("Accounts",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     if(accounts.isEmpty()) item{Hint("Create your first account and enter its opening balance to start tracking finances.")}
     items(accounts,key={it.id}){a->
      Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
       CardBlock(Color.White){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.AccountBalanceWallet,null,tint=Color(0xFF95647B));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(a.name,fontWeight=FontWeight.SemiBold,color=Ink);Text(a.accountType,fontSize=12.sp,color=Ink.copy(alpha=.6f))};Column(horizontalAlignment=Alignment.End){Text(money(a.currentBalance),fontWeight=FontWeight.Bold,color=Ink);TextButton(onClick={editingAccountId=a.id;editAccountName=a.name;editAccountType=a.accountType}){Text("Edit")}}}}
       if(editingAccountId==a.id) AlertDialog(
        onDismissRequest={editingAccountId=null},
        title={Text("Edit account")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
         OutlinedTextField(value=editAccountName,onValueChange={editAccountName=it},label={Text("Account name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
         Text("Account type",color=Ink)
         Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Cash","Bank","Mobile wallet","Custom").forEach{kind->FilterChip(selected=editAccountType==kind,onClick={editAccountType=kind},label={Text(kind)})}}
         Text("Opening and current balances are kept unchanged.",fontSize=12.sp,color=Ink.copy(alpha=.65f))
        }},
        confirmButton={TextButton(onClick={val name=editAccountName.trim();if(name.isBlank())message="Account name cannot be empty." else scope.launch{dao.updateAccount(a.copy(name=name,accountType=editAccountType));editingAccountId=null;message="Account updated."}}){Text("Save changes")}},
        dismissButton={TextButton(onClick={editingAccountId=null}){Text("Cancel")}}
       )
      }
     }
     item{CardBlock(Blue){Text("Add account",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)
      OutlinedTextField(value=accountName,onValueChange={accountName=it},label={Text("Account name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      OutlinedTextField(value=opening,onValueChange={opening=it.filter{c->c.isDigit()}},label={Text("Opening balance (PKR)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      Text("Account type",color=Ink,fontWeight=FontWeight.Medium)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Cash","Bank","Mobile wallet","Custom").forEach{kind->FilterChip(selected=accountType==kind,onClick={accountType=kind},label={Text(kind)})}}
      Button(onClick={val n=accountName.trim();val b=opening.toLongOrNull();if(n.isNotBlank()&&b!=null)scope.launch{dao.addAccount(AccountEntity(name=n,openingBalance=b,accountType=accountType));accountName="";opening="";accountType="Cash";message="Account added."}else message="Enter account name and opening balance."},modifier=Modifier.fillMaxWidth()){Text("Save account")}
     }}
     item{CardBlock(Mint){Text("Record transaction",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=txType=="Income",onClick={txType="Income"},label={Text("Income")});FilterChip(selected=txType=="Expense",onClick={txType="Expense"},label={Text("Expense")})}
      Box {
       val chosenAccount=accounts.firstOrNull{it.id==selectedAccountId} ?: accounts.firstOrNull()
       OutlinedButton(onClick={accountMenuOpen=true},modifier=Modifier.fillMaxWidth()){Text("Account: "+(chosenAccount?.name ?: "Choose account"))}
       DropdownMenu(expanded=accountMenuOpen,onDismissRequest={accountMenuOpen=false}){accounts.forEach{acc->DropdownMenuItem(text={Text(acc.name)},onClick={selectedAccountId=acc.id;accountMenuOpen=false})}}
      }
      OutlinedTextField(value=amount,onValueChange={amount=it.filter{c->c.isDigit()}},label={Text("Amount (PKR)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      OutlinedTextField(value=category,onValueChange={category=it},label={Text("Category")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      OutlinedTextField(value=note,onValueChange={note=it},label={Text("Note (optional)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      Button(onClick={val v=amount.toLongOrNull();val a=accounts.firstOrNull{it.id==selectedAccountId} ?: accounts.firstOrNull();when{v==null||v<=0L||a==null->message="Enter a positive amount and ensure an account exists.";txType=="Expense"&&a.currentBalance<v->message="Insufficient balance in the selected account.";else->scope.launch{
       dao.recordTransactionAndBalance(
        TransactionEntity(accountId=a!!.id,amount=v!!,type=txType,category=category.ifBlank{"General"}.trim(),note=note.trim()),
        a.copy(currentBalance=a.currentBalance+(if(txType=="Income")v else -v))
       )
       amount="";note="";message=txType+" saved."
      }}},modifier=Modifier.fillMaxWidth()){Text("Save transaction")}
      Text("Saved locally on this phone. Cloud sync is not enabled yet.",fontSize=12.sp,color=Ink.copy(alpha=.7f))
     }}
     item{SavingsGoalsManager(dao)}
     item{BackupManager(AppDatabase.get(context))}
     item{TransferManager(dao)}
     item{Text("Transaction history",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     if(transactions.isEmpty())item{Hint("No transactions recorded yet.")}
     items(transactions,key={it.id}){tx->TxLine(tx,accounts.firstOrNull{account->account.id==tx.accountId}?.name ?: "Account")}
    }
    "Reports"->{
     item{Text("Reports",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)}
     item{Text("Monthly overview",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Income",money(monthIncome),Mint,Modifier.weight(1f));Metric("Expenses",money(monthExpenses),Yellow,Modifier.weight(1f))}}
     item{CardBlock(Pink){Text("Monthly net savings",color=Ink.copy(alpha=.7f));Text(money(FinanceCalculations.netSavings(monthTx)),fontSize=26.sp,fontWeight=FontWeight.Bold,color=Ink);Text("Income minus expenses recorded this month. Transfers are excluded.",fontSize=12.sp,color=Ink.copy(alpha=.7f))}}
     item{Text("This week",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Income",money(weekIncome),Mint,Modifier.weight(1f));Metric("Expenses",money(weekExpenses),Yellow,Modifier.weight(1f))}}
     item{Metric("Weekly net",money(FinanceCalculations.netSavings(weekTx)),Blue,Modifier.fillMaxWidth())}
     item{Text("Tasks",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Completed",tasks.count{it.isDone}.toString(),Mint,Modifier.weight(1f));Metric("Remaining",tasks.count{!it.isDone}.toString(),Yellow,Modifier.weight(1f))}}
     item{Text("Accounts total",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)}
     item{Metric("Available balance",money(balance),Blue,Modifier.fillMaxWidth())}
    }
    "Contacts"->{
     item{Text("Contacts",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)}
     item{CardBlock(Yellow){Text("Add a contact",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=Ink)
      OutlinedTextField(value=contact,onValueChange={contact=it},label={Text("Name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      OutlinedTextField(value=phone,onValueChange={phone=it},label={Text("Phone (optional)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      OutlinedTextField(value=contactNotes,onValueChange={contactNotes=it},label={Text("Notes (optional)")},modifier=Modifier.fillMaxWidth(),minLines=2)
      Button(onClick={if(contact.isNotBlank())scope.launch{dao.addContact(ContactEntity(name=contact.trim(),phone=phone.trim(),notes=contactNotes.trim()));contact="";phone="";contactNotes="";message="Contact saved."}else message="Please enter a contact name."},modifier=Modifier.fillMaxWidth()){Text("Save contact")}
     }}
     if(contacts.isEmpty())item{Hint("Add contacts here, then record receivables, payables and partial repayments below.")}
     items(contacts,key={it.id}){c->
      Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
       CardBlock(Color.White){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.People,null,tint=Color(0xFF95647B));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(c.name,fontWeight=FontWeight.SemiBold,color=Ink);if(c.phone.isNotBlank())Text(c.phone,fontSize=13.sp,color=Ink.copy(alpha=.65f));if(c.notes.isNotBlank())Text(c.notes,fontSize=12.sp,color=Ink.copy(alpha=.6f))};TextButton(onClick={editingContactId=c.id;editContactName=c.name;editContactPhone=c.phone;editContactNotes=c.notes}){Text("Edit")}}}
       if(editingContactId==c.id) AlertDialog(
        onDismissRequest={editingContactId=null},
        title={Text("Edit contact")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
         OutlinedTextField(value=editContactName,onValueChange={editContactName=it},label={Text("Name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
         OutlinedTextField(value=editContactPhone,onValueChange={editContactPhone=it},label={Text("Phone")},modifier=Modifier.fillMaxWidth(),singleLine=true)
         OutlinedTextField(value=editContactNotes,onValueChange={editContactNotes=it},label={Text("Notes")},modifier=Modifier.fillMaxWidth(),minLines=2)
        }},
        confirmButton={TextButton(onClick={val name=editContactName.trim();if(name.isBlank())message="Contact name cannot be empty." else scope.launch{dao.updateContact(c.copy(name=name,phone=editContactPhone.trim(),notes=editContactNotes.trim()));editingContactId=null;message="Contact updated."}}){Text("Save changes")}},
        dismissButton={TextButton(onClick={editingContactId=null}){Text("Cancel")}}
       )
      }
     }
     item { DebtManager(dao) }
    }
   }
  }
 }
}
private fun durationLabel(seconds: Long): String { val hours=seconds/3600; val minutes=(seconds%3600)/60; val secs=seconds%60; return if(hours>0) "${hours}h ${minutes}m" else if(minutes>0) "${minutes}m ${secs}s" else "${secs}s" }
private suspend fun completeTaskAndRepeat(context: Context, dao: AppDao, task: TaskEntity) {
 val now = System.currentTimeMillis()
 val finishedSeconds = task.trackedSeconds + if(task.timerStartedAt != null) ((now-task.timerStartedAt)/1000L).coerceAtLeast(0L) else 0L
 dao.updateTask(task.copy(isDone=true,timerStartedAt=null,trackedSeconds=finishedSeconds))
 TaskReminderScheduler.cancel(context, task.id)
 if(task.repeatRule!="None") {
  val cal=Calendar.getInstance()
  if(task.dueDate.isNotBlank()) { val parsed=runCatching { SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(task.dueDate) }.getOrNull(); if(parsed!=null && parsed.time>now) cal.time=parsed }
  cal.add(Calendar.DAY_OF_YEAR,if(task.repeatRule=="Daily") 1 else 7)
  val nextDate=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.time)
  val nextId = dao.addTask(task.copy(id=0,isDone=false,dueDate=nextDate,trackedSeconds=0,timerStartedAt=null,createdAt=now))
  TaskReminderScheduler.schedule(context, nextId, task.title, nextDate)
 }
}
@Composable private fun CardBlock(color:Color,content:@Composable ColumnScope.()->Unit){Surface(color=color,shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp),content=content)}}
@Composable private fun Metric(title:String,value:String,color:Color,modifier:Modifier=Modifier){CardBlock(color){Text(title,fontSize=12.sp,color=Ink.copy(alpha=.7f));Text(value,fontSize=16.sp,fontWeight=FontWeight.Bold,color=Ink)}}
@Composable private fun TaskLine(title:String,done:()->Unit){Surface(color=Color.White,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CheckCircle,null,tint=Color(0xFF77A98D));Spacer(Modifier.width(10.dp));Text(title,Modifier.weight(1f),color=Ink);TextButton(onClick=done){Text("Done")}}}}
@Composable private fun TxLine(tx:TransactionEntity, accountName:String){val sign=when(tx.type){"Income"->"+";"Expense"->"−";else->"↔ "};val txColor=when(tx.type){"Income"->Color(0xFF397A55);"Expense"->Color(0xFFB64B5A);else->Color(0xFF7963A8)};Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(tx.category,fontWeight=FontWeight.SemiBold,color=Ink);Text(accountName,fontSize=12.sp,color=Ink.copy(alpha=.65f));if(tx.note.isNotBlank())Text(tx.note,fontSize=12.sp,color=Ink.copy(alpha=.6f));Text(tx.type,fontSize=12.sp,color=Ink.copy(alpha=.6f));Text(SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.US).format(java.util.Date(tx.createdAt)),fontSize=11.sp,color=Ink.copy(alpha=.5f))};Text(sign+money(tx.amount),color=txColor,fontWeight=FontWeight.Bold)}}}
@Composable private fun Hint(text:String){Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){Text(text,Modifier.padding(16.dp),color=Ink.copy(alpha=.65f),fontSize=13.sp)}}
