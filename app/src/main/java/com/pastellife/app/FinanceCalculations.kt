package com.pastellife.app

import com.pastellife.app.data.AccountEntity
import com.pastellife.app.data.DebtEntity

/**
 * Pure financial totals. Transfers are excluded because only account balances and
 * debt outstanding amounts are used; income/expense totals come from transaction type.
 */
object FinanceCalculations {
    fun availableBalance(accounts: List<AccountEntity>): Long =
        accounts.sumOf { it.currentBalance }

    fun netFinancialPosition(accounts: List<AccountEntity>, debts: List<DebtEntity>): Long {
        val receivables = debts.filter { it.direction == "RECEIVABLE" }.sumOf { it.remainingAmount }
        val payables = debts.filter { it.direction == "PAYABLE" }.sumOf { it.remainingAmount }
        return availableBalance(accounts) + receivables - payables
    }
}
