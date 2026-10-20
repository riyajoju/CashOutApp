# domain/result_handlimg

CashOutError.kt defines failure reasons: one case per service code, plus NotSent (never reached the service) and NoResponse (no usable answer). isSubmitOutcomeUnknown marks reasons where, after a submit, money may have moved. Result.kt is a generic Result<out T> with Success(data) and Error(reason: CashOutError); import it explicitly because it shadows kotlin.Result. This package is independent of data and networking frameworks. See docs/ERROR_HANDLING.md.
