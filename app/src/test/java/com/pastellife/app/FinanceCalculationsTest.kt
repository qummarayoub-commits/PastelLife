package com.pastellife.app

import com.pastellife.app.data.AccountEntity
import com.pastellife.app.data.DebtEntity
import com.pastellife.app.data.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculationsTest {
    @Test
    fun availableBalanceAddsCurrentAccountBalances() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Cash", openingBalance = 1000, currentBalance = 800),
            AccountEntity(id = 2, name = "Bank", openingBalance = 5000, currentBalance = 4200)
        )
        assertEquals(5000L, FinanceCalculations.availableBalance(accounts))
    }

    @Test
    fun netFinancialPositionAddsReceivablesAndSubtractsPayables() {
        val accounts = listOf(AccountEntity(id = 1, name = "Cash", openingBalance = 10000, currentBalance = 10000))
        val debts = listOf(
            DebtEntity(id = 1, contactId = 1, originalAmount = 3000, remainingAmount = 2000, direction = "RECEIVABLE"),
            DebtEntity(id = 2, contactId = 2, originalAmount = 1500, remainingAmount = 500, direction = "PAYABLE"),
            DebtEntity(id = 3, contactId = 3, originalAmount = 700, remainingAmount = 700, direction = "SETTLED_OR_UNKNOWN")
        )
        assertEquals(11500L, FinanceCalculations.netFinancialPosition(accounts, debts))
    }

    @Test
    fun netSavingsIgnoresTransfersAndDebtRepayments() {
        val transactions = listOf(
            TransactionEntity(accountId = 1, amount = 5000, type = "Income", category = "Salary"),
            TransactionEntity(accountId = 1, amount = 1200, type = "Expense", category = "Food"),
            TransactionEntity(accountId = 1, amount = 900, type = "Transfer", category = "Transfer to Bank"),
            TransactionEntity(accountId = 1, amount = 300, type = "Debt", category = "Repayment received")
        )
        assertEquals(3800L, FinanceCalculations.netSavings(transactions))
    }

    @Test
    fun emptyAccountsAndDebtsHaveZeroPosition() {
        assertEquals(0L, FinanceCalculations.availableBalance(emptyList()))
        assertEquals(0L, FinanceCalculations.netFinancialPosition(emptyList(), emptyList()))
    }
}
