# CashOutApp

Android cash-out sample for XYZ Broker, using a single app module and Clean Architecture packages.

A successful submission means the cash-out request was received and is being processed. The account is debited immediately; payment approval and payout happen later.

## Documentation

- [Architecture and project structure](docs/ARCHITECTURE.md)
- [Build configuration](docs/BUILD_CONFIGURATION.md)
- [Domain error handling](docs/ERROR_HANDLING.md)
- [Change log](docs/CHANGELOG.md)

## Current status

Build configuration, architecture scaffolding, networking dependencies, and Hilt application setup are present. The starter activity still renders the template greeting. Hilt networking providers and authorization header redaction are present. Domain error reasons and network exception types are implemented. Cash-out API/DTOs, repository, use cases, ViewModel, and screen remain planned.

Update the relevant documentation and change log whenever the project changes.
