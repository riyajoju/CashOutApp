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
