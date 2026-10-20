package com.xyzbroker.cashoutapp.domain.usecase

import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.repository.CashOutRepository
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import javax.inject.Inject

class SubmitCashOutUseCase @Inject constructor(
    private val repository: CashOutRepository,
) {
    suspend operator fun invoke(
        request: CashOutRequest,
        idempotencyKey: String
    ): Result<CashOutReceipt> =
        repository.submitCashOut(request, idempotencyKey)
}
