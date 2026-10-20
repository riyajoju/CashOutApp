# data/repository

Implement domain repository interfaces using the remote service.

Implemented: CashOutRepositoryImpl.kt. getAccountBalance calls getSummary through safeApiCall for the single sandbox account 100245 and maps the body with AccountMapper. Any failure, or a summary missing a figure, is Result.Error() with no reason, because reading the balance moves no money and any failure can be retried.

Planned: submit, using a submit mapper and a caller-supplied idempotency key.
