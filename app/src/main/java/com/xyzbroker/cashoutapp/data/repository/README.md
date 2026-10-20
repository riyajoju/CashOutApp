# data/repository

Implement domain repository interfaces using the remote service.

Implemented: CashOutRepositoryImpl.kt for the single sandbox account 100245.
- getAccountBalance: getSummary through safeApiCall; a summary missing a figure is Error(NoResponse).
- submitCashOut(request, idempotencyKey): sends the caller's key unchanged and returns Result<CashOutReceipt>. The caller creates one random UUID per intention and reuses it on retries.
