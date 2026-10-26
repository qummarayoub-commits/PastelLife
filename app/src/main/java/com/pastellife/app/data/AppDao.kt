package com.pastellife.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>
    @Insert suspend fun addTask(task: TaskEntity): Long
    @Update suspend fun updateTask(task: TaskEntity)
    @Query("DELETE FROM tasks WHERE id = :id") suspend fun deleteTask(id: Long)

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun observeAccounts(): Flow<List<AccountEntity>>
    @Query("SELECT COUNT(*) FROM accounts") suspend fun countAccounts(): Int
    @Insert suspend fun addAccount(account: AccountEntity): Long
    @Update suspend fun updateAccount(account: AccountEntity)

    @Transaction
    suspend fun recordTransactionAndBalance(transaction: TransactionEntity, updatedAccount: AccountEntity) {
        addTransaction(transaction)
        updateAccount(updatedAccount)
    }

    @Transaction
    suspend fun recordTransfer(fromAccount: AccountEntity, toAccount: AccountEntity, amount: Long) {
        addTransaction(TransactionEntity(accountId = fromAccount.id, amount = amount, type = "Transfer", category = "Transfer to " + toAccount.name))
        addTransaction(TransactionEntity(accountId = toAccount.id, amount = amount, type = "Transfer", category = "Transfer from " + fromAccount.name))
        updateAccount(fromAccount.copy(currentBalance = fromAccount.currentBalance - amount))
        updateAccount(toAccount.copy(currentBalance = toAccount.currentBalance + amount))
    }

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>
    @Insert suspend fun addTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    fun observeContacts(): Flow<List<ContactEntity>>
    @Insert suspend fun addContact(contact: ContactEntity): Long

    @Query("SELECT * FROM debts ORDER BY id DESC")
    fun observeDebts(): Flow<List<DebtEntity>>
    @Insert suspend fun addDebt(debt: DebtEntity): Long
    @Update suspend fun updateDebt(debt: DebtEntity)

    @Transaction
    suspend fun recordDebtPaymentAndBalance(payment: DebtPaymentEntity, updatedDebt: DebtEntity) {
        addDebtPayment(payment)
        updateDebt(updatedDebt)
    }

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY createdAt DESC")
    fun observePayments(debtId: Long): Flow<List<DebtPaymentEntity>>
    @Insert suspend fun addDebtPayment(payment: DebtPaymentEntity): Long

    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun observeSavingsGoals(): Flow<List<SavingsGoalEntity>>
    @Insert suspend fun addSavingsGoal(goal: SavingsGoalEntity): Long
    @Update suspend fun updateSavingsGoal(goal: SavingsGoalEntity)
    @Query("DELETE FROM savings_goals WHERE id = :id") suspend fun deleteSavingsGoal(id: Long)
    @Query("SELECT * FROM tasks") suspend fun backupTaskEntity(): List<TaskEntity>
    @Query("DELETE FROM tasks") suspend fun clearTaskEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreTaskEntity(items: List<TaskEntity>)

    @Query("SELECT * FROM accounts") suspend fun backupAccountEntity(): List<AccountEntity>
    @Query("DELETE FROM accounts") suspend fun clearAccountEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAccountEntity(items: List<AccountEntity>)

    @Query("SELECT * FROM transactions") suspend fun backupTransactionEntity(): List<TransactionEntity>
    @Query("DELETE FROM transactions") suspend fun clearTransactionEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreTransactionEntity(items: List<TransactionEntity>)

    @Query("SELECT * FROM contacts") suspend fun backupContactEntity(): List<ContactEntity>
    @Query("DELETE FROM contacts") suspend fun clearContactEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreContactEntity(items: List<ContactEntity>)

    @Query("SELECT * FROM debts") suspend fun backupDebtEntity(): List<DebtEntity>
    @Query("DELETE FROM debts") suspend fun clearDebtEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreDebtEntity(items: List<DebtEntity>)

    @Query("SELECT * FROM debt_payments") suspend fun backupDebtPaymentEntity(): List<DebtPaymentEntity>
    @Query("DELETE FROM debt_payments") suspend fun clearDebtPaymentEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreDebtPaymentEntity(items: List<DebtPaymentEntity>)

    @Query("SELECT * FROM savings_goals") suspend fun backupSavingsGoalEntity(): List<SavingsGoalEntity>
    @Query("DELETE FROM savings_goals") suspend fun clearSavingsGoalEntity()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreSavingsGoalEntity(items: List<SavingsGoalEntity>)

}
