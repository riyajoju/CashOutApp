# Error handling

CashOutError reasons, including NotSent and NoResponse for failures without a usable answer, live in domain/result_handlimg/CashOutError.kt. The generic transport result lives in data/remote/network/common:

| NetworkResult case | When | Submit meaning |
| --- | --- | --- |
| Success(data, replayed) | 2xx with a parsed body; replayed reflects Idempotent-Replayed: true | Inspect body: isSuccess true is accepted, WITHDRAWAL_FAILED is a refusal |
| HttpError(status, problem, retryAfterSeconds) | Non-2xx. problem is the parsed ProblemDto, or null for non-JSON (gateway HTML). retryAfterSeconds comes from the Retry-After header (delta-seconds only) | Depends on problem.code; see below |
| NoUsableResponse(cause) | Read timeout, reset, any I/O failure after a possible send, unparseable or empty 2xx body, other unexpected exceptions | Outcome unknown; money may have moved |
| NotSent(cause) | ConnectException, UnknownHostException, NoRouteToHostException with no suppressed exceptions (no earlier OkHttp attempt) | Request never reached the server |

safeApiCall (SafeApiCall.kt) performs this classification, reads the error body once, and rethrows CancellationException. It is a result type, not an Exception: success is returned, not thrown. It carries no customer-facing text. ProblemDto.detail is for logs only.

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

## Mapping to Result and CashOutError (data/mapper)

Every operation returns the domain Result<T>: Success(data) or Error(reason: CashOutError). CashOutErrorMapper.kt converts transport failures for any endpoint; CashOutMapper.kt converts the submit 200 body.

| Input | Result |
| --- | --- |
| 2xx, isSuccess true with a reference | Success(CashOutReceipt). Request is processing, not paid. An unreadable amount or date becomes null rather than hiding the receipt |
| 2xx, isSuccess false with WITHDRAWAL_FAILED | Error(WithdrawalFailed) |
| 2xx, isSuccess false with any other errorCode | Error(Unknown(errorCode)) |
| 2xx, isSuccess missing, or true without a reference | Error(NoResponse) |
| HttpError with a problem body | Error(problem code mapped one to one; RATE_LIMITED prefers the Retry-After header) |
| HttpError without a problem body (gateway HTML, empty body) | Error(NoResponse) |
| NoUsableResponse | Error(NoResponse) |
| NotSent | Error(NotSent) |

CashOutError.isSubmitOutcomeUnknown is true for NoResponse, InternalError, Unknown, and WithdrawalNeedsSupport. After a submit, these mean money may have moved: keep the idempotency key, never report them as paid or failed, and do not retry WithdrawalNeedsSupport. Replayed successes map the same as first-time ones.

The earlier SubmitOutcome type was removed in favor of Result plus these CashOutError cases. correlationId is not carried into the domain yet.

## Mapping contract

- Branch on problem.code only, never on detail. Keep status available for codes the app does not recognize.
- Success is transport success only. Map errorCode WITHDRAWAL_FAILED to CashOutError.WithdrawalFailed.
- For RATE_LIMITED, prefer the Retry-After header value and fall back to problem.retryAfterSeconds.
- On submit, treat these as an unknown outcome: NoUsableResponse; HttpError with problem == null (e.g. 502/503/504 gateway HTML); INTERNAL_ERROR; WITHDRAWAL_NEEDS_SUPPORT (show supportReference; needs human support); any unrecognized code. Do not retry these with a new idempotency key.
- ACCOUNT_RESTRICTED, BELOW_MINIMUM, INSUFFICIENT_FUNDS, RATE_LIMITED, VALIDATION_ERROR, and legacy WITHDRAWAL_FAILED mean nothing was debited per the supplied contract.
- NotSent means the request never reached the server. The mapper may report it as not sent, or conservatively as unknown; retrying with the same key is safe either way.
- OkHttp's retryOnConnectionFailure may resend a request automatically. This is safe because it reuses the same Request, including its Idempotency-Key.

Implemented: NetworkResult, ProblemDto, safeApiCall, CashOutApi, the summary, request, and response DTOs, CashOutErrorMapper, CashOutMapper, and repository submit. Pending: idempotency-key handling in the ViewModel, UI handling, and unit tests for safeApiCall and the mapper (both test files were removed).

## Sandbox behavior observed in the mock server source

- An amount below 50.00 (but above zero) returns 422 BELOW_MINIMUM with minimum, and that outcome is stored against the key. Zero, negative, string, more than two decimals, or exponent form return 422 VALIDATION_ERROR with amount INVALID_AMOUNT.
- A missing Idempotency-Key is rejected before the body is validated, so a request with both problems gets IDEMPOTENCY_KEY_REQUIRED. API.md says VALIDATION_ERROR is checked before the key is looked at; that holds for the key lookup, not for its presence.
- The idempotency fingerprint uses the trimmed beneficiary name and the IBAN with spaces removed and uppercased, so formatting-only differences replay instead of returning IDEMPOTENCY_KEY_REUSED.
- The amount is checked against the raw JSON text, matching the first "amount" key.

