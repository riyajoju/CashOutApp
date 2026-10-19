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
