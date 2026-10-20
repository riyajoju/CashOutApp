# domain/repository

Repository interfaces consumed by use cases and implemented by data. Do not expose Retrofit or DTO types.

Implemented: CashOutRepository.kt with getAccountBalance(): Result<AccountBalance>.

Implemented: submitCashOut(request: CashOutRequest, idempotencyKey: String): Result<CashOutReceipt>.
