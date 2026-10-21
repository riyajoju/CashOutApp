# CashOutApp

Android cash-out sample for XYZ Broker, using a single app module and Clean Architecture packages.

A successful submission means the cash-out request was received and is being processed. The account is debited immediately; payment approval and payout happen later.

## Documentation

- [Submission notes: assumptions, open decisions, next steps](NOTES.md)
- [Architecture and project structure](docs/ARCHITECTURE.md)
- [Build configuration](docs/BUILD_CONFIGURATION.md)
- [Domain error handling](docs/ERROR_HANDLING.md)
- [Change log](docs/CHANGELOG.md)
- [AI conversation history and tools](ai-logs/README.md)

## Running against the sandbox

1. Install Node 18 or later.
2. Start the sandbox from the candidate kit: `cd mock-server && LOG=1 node server.mjs` (port 8080).
3. Add `sandbox.apiToken=<token from API.md>` to local.properties (see local.properties.example).
4. Run the uat debug variant on an emulator, which reaches the host at http://10.0.2.2:8080. On a device, run `adb reverse tcp:8080 tcp:8080` first.
5. Reset between experiments with `curl -X POST http://localhost:8080/_mock/reset`.

## Current status

Build configuration, architecture scaffolding, networking dependencies, and Hilt application setup are present. Hilt networking providers and authorization header redaction are present. Domain error reasons, the NetworkResult transport type, ProblemDto, and safeApiCall classification (with unit tests) are implemented. CashOutApi, the summary and submit DTOs, and the submit outcome mapper are implemented. The app loads the account summary, validates the amount on the device, submits with one idempotency key per intention, and shows an app-written message for every outcome. Unit tests for the mappers, validation, and retry behavior are planned.

Update the relevant documentation and change log whenever the project changes.

## Tests

See [major automated and manual test cases](docs/TESTING.md).

See [app icon resources and design](docs/APP_ICON.md).
