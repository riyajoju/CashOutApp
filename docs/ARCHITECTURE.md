# Architecture

One Android `app` module, with four architecture packages under `com.xyzbroker.cashoutapp`. Package names use lowercase Kotlin conventions, including `di`.

```text
app/src/main/java/com/xyzbroker/cashoutapp/
├── CashOutApplication.kt
├── MainActivity.kt
├── di/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   ├── dto/
│   │   └── network/
│   │       └── common/
│   ├── mapper/
│   └── repository/
├── domain/
│   ├── model/
│   ├── result_handlimg/
│   ├── repository/
│   └── usecase/
└── presentation/
    ├── theme/
    └── cashout/
        └── components/
```

Each scaffold package contains a README describing its responsibility and planned files. Theme Kotlin files have been moved into presentation/theme, with their packages and activity import updated. No placeholder Kotlin implementations have been added.

## Dependencies

Presentation → Domain ← Data. DI wires implementations to interfaces. Domain does not import Android, Retrofit, DTOs, or presentation types. These boundaries are conventions within the single module, not Gradle-enforced module boundaries.

## Presentation contract

CashOutViewModel.kt will contain both the ViewModel and CashOutUiState definition. The ViewModel owns MutableStateFlow internally and exposes read-only StateFlow. CashOutRoute collects it with lifecycle awareness and passes state and event callbacks into stateless CashOutScreen and component composables.

Expected states: initial loading, editable form, submitting, request received, and error. Submission success must not imply payment completion. Prevent repeated submit taps while a request is in progress. Follow the sandbox API contract for idempotency, retry behavior, balance refresh, and validation.

## Planned classes

- DI: NetworkModule and RepositoryModule (implemented).
- Data: CashOutApi (implemented), request/response DTOs (ProblemDto, AccountResponseDto, CashOutRequestDto, and CashOutResponseDto implemented), mappers (AccountMapper, CashOutErrorMapper, and CashOutMapper implemented), CashOutRepositoryImpl (balance and submit implemented).
- Domain: Result, CashOutError, AccountBalance, CashOutRequest, CashOutReceipt, CashOutRepository, GetCashOutDetailsUseCase, ValidateCashOutAmountUseCase, and SubmitCashOutUseCase (implemented); Money (planned).
- Presentation: CashOutRoute and CashOutScreen (in CashOutScreen.kt) and CashOutViewModel with CashOutUiState (implemented: balance, form, submit, outcomes); AmountInput and CashOutConfirmation (planned).

These classes are planned until their implementation is explicitly recorded in the change log.

## Dependency injection setup

Hilt and its KSP compiler are configured. CashOutApplication initializes Hilt and MainActivity is an Android entry point. NetworkModule provides Retrofit, OkHttp, the Authorization header (token from BuildConfig.API_TOKEN), debug logging with Authorization redaction, and CashOutApi. Repository bindings remain planned.

## Domain errors

CashOutError.kt defines endpoint-specific reasons in domain/result_handlimg. NetworkResult.kt defines the sealed transport result in data/remote/network/common, and SafeApiCall.kt classifies Retrofit calls into it. ProblemDto.kt in data/remote/dto holds the parsed error body. None of these depend on CashOutError, and domain does not depend on them. See [error handling](ERROR_HANDLING.md) for all supplied codes, extra fields, and ambiguous submit outcomes. data/mapper maps NetworkResult to the domain Result<T>, with every failure as a CashOutError (see [error handling](ERROR_HANDLING.md)). The domain repository interface uses only domain types (CashOutRequest, CashOutReceipt, AccountBalance).
