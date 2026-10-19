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
