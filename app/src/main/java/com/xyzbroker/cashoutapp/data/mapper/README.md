# data/mapper

Map API DTOs into domain models and domain input into API requests.

Implemented: AccountMapper.kt (AccountResponseDto to AccountBalance; null if any figure is missing).

Planned: a submit mapper (NetworkResult<CashOutResponseDto> to SubmitOutcome, and ProblemDto to CashOutError). An earlier SubmitCashOutMapper.kt was removed and will be re-added.
