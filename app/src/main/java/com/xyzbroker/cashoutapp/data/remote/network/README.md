# data/remote/network

Retrofit, Gson converter, OkHttp, and HttpLoggingInterceptor dependencies are installed. di/NetworkModule.kt provides the OkHttp client (authorization interceptor, Authorization log redaction, 30 s timeouts) and Retrofit.

common/NetworkResult.kt and common/SafeApiCall.kt implement the generic transport result and its classification. Endpoint-specific reasons live in domain/result_handlimg and are mapped in data/mapper (pending).
