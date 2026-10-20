package com.xyzbroker.cashoutapp.domain.usecase

import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.repository.CashOutRepository
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import javax.inject.Inject

class GetCashOutDetailsUseCase @Inject constructor(
    private val repository: CashOutRepository,
) {
    suspend operator fun invoke(): Result<AccountBalance> = repository.getAccountBalance()
}
