package com.xyzbroker.cashoutapp.data.mapper

import com.xyzbroker.cashoutapp.data.remote.dto.AccountResponseDto
import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result

/** A summary missing any figure is unusable: an available amount must never be computed from partial data. */
fun AccountResponseDto.toAccountBalanceResult(): Result<AccountBalance> {
    if (currency == null || balance == null || equity == null || freeMargin == null) {
        return Result.Error(CashOutError.NoResponse)
    }
    return Result.Success(
        AccountBalance(
            currency = currency,
            balance = balance,
            equity = equity,
            freeMargin = freeMargin,
        ),
    )
}
