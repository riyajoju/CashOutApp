# Build configuration

`buildSrc/src/main/kotlin/configuration/AppConfig.kt` centralizes app version and SDK values. `app/build.gradle.kts` imports these constants.

| Setting | Current value |
| --- | --- |
| Version code | 1 |
| Version name | 1.0.0 |
| Minimum SDK | 26 |
| Target SDK | 36 |
| Compile SDK | 37 |

No maximum SDK restriction is configured. Target SDK and maximum SDK are different settings.

The environment dimension currently defines `uat` and `prod` flavors, with matching application ID/version suffixes and BASE_URL BuildConfig fields. The current UAT name differs from the initially discussed SIT name; it has been preserved as configured. uat points at the sandbox (http://10.0.2.2:8080/). prod uses the placeholder https://api.example.com/, because production is HTTPS only.

Compose and BuildConfig generation are enabled. Java source and target compatibility are 17. Debug coverage is enabled; release minification and resource shrinking are enabled.

`buildSrc/build.gradle.kts` applies kotlin-dsl and configures Maven Central and the Gradle Plugin Portal. Gradle discovers buildSrc automatically; it is not included as an app module in settings.gradle.kts.

Verification: ./gradlew :app:assembleUatDebug :app:assembleProdDebug --console=plain passed after the networking/Hilt setup. Both debug variants compiled and packaged successfully.

## Networking and dependency injection

Versions are centralized in gradle/libs.versions.toml:

| Dependency | Version |
| --- | --- |
| Retrofit and Gson converter | 3.0.0 |
| OkHttp and logging interceptor | 4.12.0 |
| Hilt runtime, compiler, Gradle plugin | 2.60.1 |
| KSP plugin | 2.3.9 |

The root build declares Hilt/KSP plugins with apply false; app applies them and uses ksp for hilt-compiler. AGP built-in Kotlin remains enabled. CashOutApplication is annotated with @HiltAndroidApp and registered in the manifest; MainActivity is annotated with @AndroidEntryPoint. INTERNET permission is declared.

Retrofit 3.0.0 uses OkHttp 4.12; the explicit client and logging dependencies use the same version. Gson is the selected JSON converter. Headers use OkHttp's existing interceptor API and need no separate headers dependency. Client instances, logging policies, header/auth interceptors, API contracts, and DI provider modules remain pending.

References: [Retrofit release](https://github.com/square/retrofit/releases/tag/3.0.0), [Hilt setup](https://dagger.dev/hilt/gradle-setup), [Dagger KSP](https://dagger.dev/dev-guide/ksp.html).

## Sandbox token and cleartext

The bearer token is not in source. app/build.gradle.kts reads `sandbox.apiToken` from the git-ignored local.properties, falling back to the `SANDBOX_API_TOKEN` environment variable, and exposes it as `BuildConfig.API_TOKEN`. local.properties.example shows the expected keys with placeholders. Without a token the app still builds, but every request gets 401 UNAUTHENTICATED. The token is compiled into the APK, which is acceptable for the sandbox only; production would use real login.

Cleartext HTTP is not enabled in the main manifest. The uat source set adds a network security config (app/src/uat/res/xml/network_security_config.xml) that permits cleartext only for 10.0.2.2 and localhost. prod permits no cleartext.

okhttp mockwebserver 4.12.0 (matching OkHttp) is a test-only dependency.

Verification: :app:assembleUatDebug and :app:assembleProdDebug passed. The merged uatDebug manifest references the network security config; the prodDebug manifest has no cleartext setting.
