# Change log

## 2026-10-19

- Added buildSrc with configuration/AppConfig.kt and connected version/SDK constants to the app build script.
- Recorded current configuration: version 1.0.0 (code 1), minimum SDK 24, target SDK 36, compile SDK 37.
- Preserved existing uat/prod flavors and BuildConfig setup.
- Created di, data, domain, and presentation architecture packages with documented subpackages.
- Moved Compose theme files from ui/theme into presentation/theme and updated the activity import.
- Documented the agreed ViewModel-owned state and stateless Compose contract.
- Added root README, architecture/build documentation, and persistent documentation maintenance instructions in AGENTS.md.
- Cash-out feature implementation is still pending. Structural checks passed; no Gradle build was run for the package/documentation scaffold. Earlier buildSrc verification was blocked by Gradle cache permissions.

### Networking dependencies and Hilt bootstrap

- Added Retrofit 3.0.0, Gson converter 3.0.0, OkHttp 4.12.0, and logging-interceptor 4.12.0 using the version catalog.
- Added Hilt 2.60.1 and KSP 2.3.9 plugins/runtime/compiler.
- Created CashOutApplication with @HiltAndroidApp, registered it in the manifest, and annotated MainActivity with @AndroidEntryPoint.
- Added INTERNET permission. Network clients, headers, logging configuration, and provider modules remain pending.
- Validation: ./gradlew :app:assembleUatDebug :app:assembleProdDebug --console=plain passed, including KSP generation, Kotlin compilation, Hilt component generation, and APK packaging for both flavors.

## 2026-10-20

- Recorded the existing user-added NetworkModule with authorization interception and Authorization log redaction; left its implementation unchanged.
- Added domain/model/CashOutException.kt: sealed HTTP, connection, unexpected, and legacy refusal exceptions, with typed business codes and extra fields.
- Preserved HTTP status and unknown codes; modeled exact monetary extras with BigDecimal.
- Documented all provided sandbox errors and unknown submit outcomes in ERROR_HANDLING.md. Parsing/mapping remains pending.
- Validation: ./gradlew :app:compileUatDebugKotlin :app:compileProdDebugKotlin --console=plain passed for both flavors.

### Error type placement correction

- Kept endpoint-specific CashOutError in domain/model/CashOutError.kt.
- Moved and renamed CashOutException to data/remote/network/NetworkHandler.kt, preserving its cases and fields.
- Removed the old CashOutException.kt file and updated architecture and error documentation. Domain has no dependency on NetworkHandler.
- Validation: :app:compileUatDebugKotlin and :app:compileProdDebugKotlin passed.

### Error package organization

- Moved NetworkHandler.kt into data/remote/network/common.
- Moved CashOutError.kt into domain/result_handlimg, using the requested folder spelling.
- Updated Kotlin packages, imports, architecture tree, and package/error documentation. Behavior is unchanged.
- Validation: offline Kotlin compilation passed for uatDebug and prodDebug.

### Generic network success

- Replaced the network WithdrawalFailed case with generic Success<T>(data).
- Made NetworkHandler<T> a sealed result rather than an Exception, so successes are returned rather than thrown.
- Removed its CashOutError dependency; HttpError carries a raw errorCode for later domain mapping.
- Added CashOutError.WithdrawalFailed in domain for the legacy HTTP 200 business refusal. API response mapping remains pending.
- Updated error-handling and architecture documentation.
- Validation: Kotlin compilation passed for uatDebug and prodDebug.

### NetworkResult replaces NetworkHandler

- Replaced data/remote/network/common/NetworkHandler.kt with NetworkResult.kt: Success(data, replayed), HttpError(status, problem, retryAfterSeconds), NoUsableResponse(cause), and NotSent(cause). Removed ConnectionError, UnexpectedError, and default English messages.
- HttpError now carries the parsed ProblemDto, so the mapper keeps fieldErrors, minimum, maxWithdrawable, supportReference, retryAfterSeconds, and correlationId. Gateway HTML yields problem = null.
- Connection failures are split. Only connect failures with no earlier attempt are NotSent. Timeouts, resets, and unparseable 2xx bodies are NoUsableResponse, because a submit may have been processed.
- Added data/remote/dto/ProblemDto.kt (all nullable, money as BigDecimal).
- Added data/remote/network/common/SafeApiCall.kt: classifies Retrofit calls, reads the error body once, reads the Retry-After (delta-seconds) and Idempotent-Replayed headers, and rethrows CancellationException.
- Added an R8 keep rule for the dto package, because Gson maps DTOs by reflection and release builds are minified.
- Updated ERROR_HANDLING.md, ARCHITECTURE.md, README.md, and the package READMEs.
- Validation: SafeApiCallTest (11 tests: replay header, problem body and exact money, fieldErrors, Retry-After, gateway HTML, NotSent vs NoUsableResponse, unparseable body, cancellation) passed via :app:testUatDebugUnitTest. :app:compileProdDebugKotlin passed. Release minification was not built.

### Submit outcome mapping

- Added domain/result_handlimg/SubmitOutcome.kt: Received (processing, not paid), Refused (nothing debited), NotSent, Unknown (money may have moved; retry with the same key), NeedsSupport (money may have moved; do not retry).
- Added data/remote/dto/CashOutResponseDto.kt for the submit 200 body (success and legacy refusal).
- Added data/mapper/SubmitCashOutMapper.kt. It branches on code only. INTERNAL_ERROR, gateway HTML, missing or unrecognized codes, isSuccess=false with an undocumented errorCode, and isSuccess=true without a reference all map to Unknown. Retry-After header takes precedence over the body's retryAfterSeconds.
- Reformatted the IOException branch in SafeApiCall.kt; behavior is unchanged.
- Kept the domain/result_handlimg package spelling as previously requested.
- Validation: :app:testUatDebugUnitTest passed (SubmitCashOutMapperTest 16 tests, SafeApiCallTest 11 tests). :app:compileProdDebugKotlin passed.

### Sandbox connection

- Added data/remote/api/CashOutApi.kt (getSummary, submitCashOut with the Idempotency-Key header) and NetworkModule.provideCashOutApi.
- Added AccountResponseDto.kt and CashOutRequestDto.kt (with DestinationDto).
- Moved the bearer token out of NetworkModule into BuildConfig.API_TOKEN, read from local.properties (sandbox.apiToken) or SANDBOX_API_TOKEN. Added local.properties.example with placeholders.
- Removed app-wide usesCleartextTraffic. The uat flavor now adds a network security config allowing cleartext for 10.0.2.2 and localhost only. The prod BASE_URL is now the HTTPS placeholder https://api.example.com/.
- Added okhttp mockwebserver 4.12.0 as a test dependency.
- Recorded sandbox behavior observed in the mock server source in ERROR_HANDLING.md. Corrected the minimum SDK in BUILD_CONFIGURATION.md to 26.
- Validation: :app:testUatDebugUnitTest passed (CashOutApiTest 3 tests: path, Idempotency-Key, Authorization, Content-Type, exact JSON with amount 250.00 and no exponent form, summary decimals kept; SubmitCashOutMapperTest 16; SafeApiCallTest 11). :app:assembleUatDebug and :app:assembleProdDebug passed. No run against the live sandbox yet, because Node is not installed on this machine.

### Available balance from the sandbox

- Added domain AccountBalance (available = smallest of balance, equity, freeMargin, floored to the cent), BalanceOutcome, CashOutRepository, and GetCashOutDetailsUseCase.
- Added data AccountMapper (returns null if any figure is missing), CashOutRepositoryImpl (getAccountBalance for account 100245 via safeApiCall), and di/RepositoryModule.
- Added presentation CashOutViewModel (CashOutUiState: Loading, Form, BalanceError) and CashOutScreen.kt (CashOutRoute and stateless CashOutScreen). MainActivity now shows CashOutRoute instead of the template greeting. Added the customer-facing strings to strings.xml.
- Added lifecycle-runtime-compose 2.11.0 and hilt-lifecycle-viewmodel-compose 1.3.0.
- SafeApiCallTest and SubmitCashOutMapperTest were deleted from the working tree; the tests will be re-added after the server connection is complete. CashOutApiTest remains.
- Validation: :app:assembleUatDebug passed. On the Medium_Phone_API_37.0 emulator against the local sandbox, the uatDebug app sent GET /v1/accounts/100245/summary (sandbox log: 200) and displayed Available $34,347.20 (balance 34412.65, equity 36120.5512, freeMargin 34347.20). The unit tests were not re-run for this change.

### Generic Result replaces BalanceOutcome

- Added domain/result_handlimg/Result.kt: sealed Result<out T> with Success(data) and Error(reason: CashOutError?), using Nothing for the error case. It shadows kotlin.Result and must be imported explicitly.
- Removed BalanceOutcome. CashOutRepository.getAccountBalance, GetCashOutDetailsUseCase, CashOutRepositoryImpl, and CashOutViewModel now use Result<AccountBalance>. SubmitOutcome is unchanged, because a submit can also end with an unknown outcome or needing support.
- data/mapper/SubmitCashOutMapper.kt had been removed from the working tree, so the repository no longer maps problem codes for the balance; every balance failure is Result.Error(). Docs now list the submit mapper and its test as planned.
- Validation: :app:assembleUatDebug and :app:testUatDebugUnitTest passed (CashOutApiTest 3, ExampleUnitTest 1). Reinstalled on the emulator: the sandbox logged GET /v1/accounts/100245/summary -> 200 and the screen showed Available $34,347.20.

