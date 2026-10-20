package com.xyzbroker.cashoutapp.domain.repository

import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result

interface CashOutRepository {
    suspend fun getAccountBalance(): Result<AccountBalance>
    suspend fun submitCashOut(
        request: CashOutRequest,
        idempotencyKey: String,
    ): Result<CashOutReceipt>
}
