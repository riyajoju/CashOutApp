# domain/usecase

Business operations. The service remains authoritative for withdrawal eligibility.

Implemented:
- GetCashOutDetailsUseCase.kt: loads the account balance.
- ValidateCashOutAmountUseCase.kt: rejects an empty amount (Empty), a malformed amount (Invalid; only ASCII digits with an optional dot and at most two decimals are accepted), an amount under the 50.00 minimum, or above available. Returns AmountValidation; never rounds.
- SubmitCashOutUseCase.kt: submits a CashOutRequest with the caller's idempotency key.
