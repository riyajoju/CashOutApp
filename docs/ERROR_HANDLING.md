# Error handling

Endpoint-specific CashOutError reasons live in domain/result_handlimg/CashOutError.kt. Generic NetworkHandler<T> lives in data/remote/network/common/NetworkHandler.kt and has Success(data), HttpError, ConnectionError, and UnexpectedError cases. It is a result type, not an Exception: success is returned, not thrown. HttpError preserves status, raw errorCode, optional message, and cause without depending on CashOutError. DTO parsing and domain mapping remain pending.

| HTTP | Code | Domain reason / extra fields |
| --- | --- | --- |
| 400 | IDEMPOTENCY_KEY_REQUIRED | IdempotencyKeyRequired |
| 400 / 422 | VALIDATION_ERROR | ValidationError(fieldErrors) |
| 401 | UNAUTHENTICATED | Unauthenticated |
| 403 | ACCOUNT_RESTRICTED | AccountRestricted |
| 404 | ACCOUNT_NOT_FOUND | AccountNotFound |
| 404 | NOT_FOUND | NotFound |
| 500 | INTERNAL_ERROR | InternalError; submit outcome unknown |
| 409 | WITHDRAWAL_NEEDS_SUPPORT | WithdrawalNeedsSupport(supportReference); human support required |
| 422 | BELOW_MINIMUM | BelowMinimum(minimum) |
| 422 | INSUFFICIENT_FUNDS | InsufficientFunds(maxWithdrawable) |
| 422 | IDEMPOTENCY_KEY_REUSED | IdempotencyKeyReused |
| 429 | RATE_LIMITED | RateLimited(retryAfterSeconds) |
| Any non-success | Missing/unrecognized code | Unknown(rawCode) |
| 200 | errorCode=WITHDRAWAL_FAILED | CashOutError.WithdrawalFailed; legacy refusal |

Extra monetary values use BigDecimal to preserve precision; no currency or units are inferred. Optional extra fields are nullable so an incomplete error body does not erase the original failure. Field errors default to an empty map.

## Mapping contract for the upcoming data implementation

- Parse status and business code independently. Preserve unknown codes and HTTP status.
- An HTTP 200 body is transported as NetworkHandler.Success. Endpoint-specific mapping must inspect errorCode and map WITHDRAWAL_FAILED to CashOutError.WithdrawalFailed; transport success does not imply business success.
- For RATE_LIMITED, consume retryAfterSeconds and the Retry-After header according to the API contract; a header parser is not implemented yet.
- Map network I/O failures to ConnectionError. Preserve coroutine cancellation by rethrowing CancellationException rather than wrapping it.
- Use UnexpectedError for other unexpected failures. A malformed non-success body should retain HttpError with Unknown, rather than lose its HTTP status.
- INTERNAL_ERROR on submit, lost connections, and timeouts do not prove that nothing was debited. WITHDRAWAL_NEEDS_SUPPORT explicitly requires human intervention; money may or may not have moved. Do not automatically retry these as a new request/key.
- ACCOUNT_RESTRICTED, BELOW_MINIMUM, INSUFFICIENT_FUNDS, RATE_LIMITED, and legacy WITHDRAWAL_FAILED explicitly mean nothing was debited per the supplied contract. Do not infer debit outcome for other codes without more contract details.

This change defines domain error reasons and network exception types only. API DTOs, error-body parsing, exception mapping, retry behavior, and UI handling remain pending.
