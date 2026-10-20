# data/mapper

Map API DTOs into domain models and domain input into API requests.

Implemented:
- AccountMapper.kt: AccountResponseDto.toAccountBalanceResult, Success(AccountBalance) or Error(NoResponse) if any figure is missing.
- CashOutErrorMapper.kt: NetworkResult<T>.toResult(onSuccess) for any endpoint (HttpError with a problem body to its CashOutError, no problem body or NoUsableResponse to NoResponse, NotSent to NotSent), and ProblemDto.toCashOutError.
- CashOutMapper.kt: CashOutRequest.toDto (amount scale 2, never rounded) and CashOutResponseDto.toCashOutResult (receipt, legacy WITHDRAWAL_FAILED, other refusal codes as Unknown, anything else as NoResponse).
