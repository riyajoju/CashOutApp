# data/remote/network/common

- NetworkResult.kt: sealed transport result with Success(data, replayed), HttpError(status, problem, retryAfterSeconds), NoUsableResponse(cause), and NotSent(cause). It has no CashOutError dependency.
- SafeApiCall.kt: safeApiCall wraps a Retrofit call and classifies it into NetworkResult. It reads the error body once, parses ProblemDto (null for non-JSON such as gateway HTML), reads Retry-After (delta-seconds only) and Idempotent-Replayed, and rethrows CancellationException.

Only connect failures with no earlier attempt (ConnectException, UnknownHostException, NoRouteToHostException without suppressed exceptions) are NotSent. Every other I/O failure, and any unparseable 2xx body, is NoUsableResponse because the server may have acted on the request.
