package com.xyzbroker.cashoutapp.data.repository

import com.xyzbroker.cashoutapp.data.mapper.toAccountBalanceResult
import com.xyzbroker.cashoutapp.data.mapper.toCashOutResult
import com.xyzbroker.cashoutapp.data.mapper.toDto
import com.xyzbroker.cashoutapp.data.mapper.toResult
import com.xyzbroker.cashoutapp.data.remote.api.CashOutApi
import com.xyzbroker.cashoutapp.data.remote.network.common.safeApiCall
import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.repository.CashOutRepository
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import javax.inject.Inject

class CashOutRepositoryImpl @Inject constructor(
    private val api: CashOutApi,
) : CashOutRepository {

    override suspend fun getAccountBalance(): Result<AccountBalance> {
        val response = safeApiCall { api.getSummary(ACCOUNT_NUMBER) }
        return response.toResult(onSuccess = { summary -> summary.toAccountBalanceResult() })
    }

    override suspend fun submitCashOut(
        request: CashOutRequest,
        idempotencyKey: String,
    ): Result<CashOutReceipt> {
        val response = safeApiCall {
            api.submitCashOut(
                accountNumber = ACCOUNT_NUMBER,
                idempotencyKey = idempotencyKey,
                body = request.toDto(),
            )
        }
        return response.toResult(onSuccess = { body -> body.toCashOutResult() })
    }

    private companion object {
        // The sandbox has one account; login is out of scope.
        const val ACCOUNT_NUMBER = "100245"
    }
}
