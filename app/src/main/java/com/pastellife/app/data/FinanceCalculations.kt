package com.pastellife.app.data

object FinanceCalculations {
    fun availableBalance(accounts: List<AccountEntity>): Long =
        accounts.sumOf { it.currentBalance }

    fun netFinancialPosition(accounts: List<AccountEntity>, debts: List<DebtEntity>): Long {
        val receivables = debts.filter { it.direction == "RECEIVABLE" }.sumOf { it.remainingAmount }
        val payables = debts.filter { it.direction == "PAYABLE" }.sumOf { it.remainingAmount }
        return availableBalance(accounts) + receivables - payables
    }

    fun netSavings(transactions: List<TransactionEntity>): Long {
        val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        return income - expenses
    }
}
