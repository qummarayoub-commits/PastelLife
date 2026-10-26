package com.pastellife.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculationsTest {
    @Test fun availableBalanceAddsAccountBalances() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Cash", openingBalance = 1000, currentBalance = 800),
            AccountEntity(id = 2, name = "Bank", openingBalance = 5000, currentBalance = 4500)
        )
        assertEquals(5300L, FinanceCalculations.availableBalance(accounts))
    }

    @Test fun netPositionIncludesReceivablesAndSubtractsPayables() {
        val accounts = listOf(AccountEntity(id = 1, name = "Cash", openingBalance = 1000, currentBalance = 1000))
        val debts = listOf(
            DebtEntity(id = 1, contactId = 1, originalAmount = 500, remainingAmount = 300, direction = "RECEIVABLE"),
            DebtEntity(id = 2, contactId = 2, originalAmount = 200, remainingAmount = 100, direction = "PAYABLE")
        )
        assertEquals(1200L, FinanceCalculations.netFinancialPosition(accounts, debts))
    }

    @Test fun transfersDoNotCountAsIncomeOrExpense() {
        val transactions = listOf(
            TransactionEntity(accountId = 1, amount = 1000, type = "Income", category = "Salary"),
            TransactionEntity(accountId = 1, amount = 200, type = "Expense", category = "Food"),
            TransactionEntity(accountId = 1, amount = 500, type = "Transfer", category = "Transfer out"),
            TransactionEntity(accountId = 2, amount = 500, type = "Transfer", category = "Transfer in")
        )
        assertEquals(800L, FinanceCalculations.netSavings(transactions))
    }
}
