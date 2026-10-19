# Project instructions

- Follow the single-module architecture documented in docs/ARCHITECTURE.md.
- Use lowercase di, data, domain, and presentation packages.
- Keep domain independent of Android and data/presentation types.
- Define cash-out UI state in CashOutViewModel.kt; keep screen composables stateless.
- For every project change, update the relevant Markdown documentation and docs/CHANGELOG.md in the
  same task. Update structure, build settings, implementation status, and verification details when
  affected. Clearly distinguish planned files from implemented files.
- A successful cash-out submit means request received/processing, not paid. Preserve that
  distinction in code and documentation.
