package com.pastellife.app.data

import androidx.room.Dao
import androidx.room.Insert
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

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY createdAt DESC")
    fun observePayments(debtId: Long): Flow<List<DebtPaymentEntity>>
    @Insert suspend fun addDebtPayment(payment: DebtPaymentEntity): Long
}
