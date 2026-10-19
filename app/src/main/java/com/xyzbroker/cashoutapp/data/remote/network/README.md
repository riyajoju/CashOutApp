# data/remote/network

Retrofit, Gson converter, OkHttp, and HttpLoggingInterceptor dependencies are installed. OkHttp interceptors support request headers without another dependency.

Client setup, logging configuration, and header/auth interceptors are pending. Implement them once the sandbox contract and required headers are available.

common/NetworkHandler.kt defines the sealed HttpError, ConnectionError, UnexpectedError, and Success cases. HTTP failures preserve a raw errorCode; endpoint-specific reasons remain in domain/result_handlimg. Exception mapping remains pending.
