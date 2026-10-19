# Build configuration

`buildSrc/src/main/kotlin/configuration/AppConfig.kt` centralizes app version and SDK values. `app/build.gradle.kts` imports these constants.

| Setting | Current value |
| --- | --- |
| Version code | 1 |
| Version name | 1.0.0 |
| Minimum SDK | 24 |
| Target SDK | 36 |
| Compile SDK | 37 |

No maximum SDK restriction is configured. Target SDK and maximum SDK are different settings.

The environment dimension currently defines `uat` and `prod` flavors, with matching application ID/version suffixes and BASE_URL BuildConfig fields. The current UAT name differs from the initially discussed SIT name; it has been preserved as configured. URLs remain the configured xyzbroker.com values pending sandbox integration.

Compose and BuildConfig generation are enabled. Java source and target compatibility are 17. Debug coverage is enabled; release minification and resource shrinking are enabled.

`buildSrc/build.gradle.kts` applies kotlin-dsl and configures Maven Central and the Gradle Plugin Portal. Gradle discovers buildSrc automatically; it is not included as an app module in settings.gradle.kts.

The previous Gradle verification attempt was blocked by sandbox access to the Gradle wrapper cache. A successful Gradle sync/build has not been verified in this chat.
