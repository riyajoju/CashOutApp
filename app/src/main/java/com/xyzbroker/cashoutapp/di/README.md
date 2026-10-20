# di

Hilt is configured using KSP. CashOutApplication initializes Hilt and MainActivity is an Android entry point.

Implemented: NetworkModule.kt provides the logging interceptor, OkHttp client (Authorization from BuildConfig.API_TOKEN), Retrofit, and CashOutApi.

Planned: RepositoryModule.kt for repository bindings.
