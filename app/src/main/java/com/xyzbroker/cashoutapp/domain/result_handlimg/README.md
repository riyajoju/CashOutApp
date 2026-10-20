# domain/result_handlimg

CashOutError.kt defines endpoint-specific error reasons and extra fields. Result.kt is a generic Result<out T> with Success(data) and Error(reason: CashOutError?); import it explicitly because it shadows kotlin.Result. SubmitOutcome.kt defines how a submit ended: Received (processing, not paid), Refused (nothing debited), NotSent, Unknown (money may have moved; retry with the same key), and NeedsSupport (money may have moved; do not retry). This package is independent of data and networking frameworks. See docs/ERROR_HANDLING.md for semantics.
