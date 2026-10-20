package com.xyzbroker.cashoutapp.data.mapper

import com.xyzbroker.cashoutapp.data.remote.dto.CashOutRequestDto
import com.xyzbroker.cashoutapp.data.remote.dto.CashOutResponseDto
import com.xyzbroker.cashoutapp.data.remote.dto.DestinationDto
import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Sends exactly two decimals (250.00). UNNECESSARY throws rather than round an amount with more. */
fun CashOutRequest.toDto(): CashOutRequestDto = CashOutRequestDto(
    amount = amount.setScale(2, RoundingMode.UNNECESSARY),
    destination = DestinationDto(beneficiaryName = beneficiaryName, iban = iban),
)

/**
 * A 200 body is either a receipt or the legacy refusal. Anything else in a 200 is not proof that
 * nothing moved, so it maps to an error whose outcome is unknown.
 */
fun CashOutResponseDto.toCashOutResult(): Result<CashOutReceipt> = when (isSuccess) {
    true if !reference.isNullOrBlank() -> Result.Success(
        CashOutReceipt(
            reference = reference,
            amount = amount?.toBigDecimalOrNull(),
            currency = currency,
            estimatedArrival = estimatedArrival?.toLocalDateOrNull(),
        ),
    )

    false if errorCode == CashOutError.WithdrawalFailed.code -> Result.Error(
        CashOutError.WithdrawalFailed
    )

    false -> Result.Error(CashOutError.Unknown(errorCode))
    else -> Result.Error(CashOutError.NoResponse)
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (e: DateTimeParseException) {
    null
}
