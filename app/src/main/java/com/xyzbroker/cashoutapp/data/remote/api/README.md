# data/remote/api

Retrofit service interfaces.

Implemented: CashOutApi.kt with getSummary(accountNumber) and submitCashOut(accountNumber, idempotencyKey, body). Both return Response<T> so callers wrap them in safeApiCall. The Authorization header is added by the OkHttp client in NetworkModule.

Planned for the live call only: cash outs list and stream endpoints.
