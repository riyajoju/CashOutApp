# presentation/cashout

CashOutUiState is defined in CashOutViewModel.kt. The ViewModel owns state, handles events, and calls use cases. The route collects state with lifecycle awareness. The Compose screen receives state and callbacks and stays stateless.

Implemented:
- CashOutViewModel.kt: CashOutUiState (Loading, BalanceError, Form), SubmitStatus (Received, Refused, Unknown), and FormField. The form fields and the idempotency key live in SavedStateHandle, so they survive configuration changes and process death. One random UUID is created per intention and reused on retries. The key is cleared on any known outcome; it is kept while the outcome is unknown, which locks the inputs and turns the button into Check again. A key found on restore means the last submit is still unknown. On success the receipt (reference, amount, currency, estimated arrival) is also saved, so after process death the Request received card is restored instead of a fillable form; Make another cash out clears it. WITHDRAWAL_NEEDS_SUPPORT locks the form with no retry. Before anything is sent, all fields are checked together: an empty amount, a blank beneficiary name, and a blank IBAN are reported at once (missingFields) and the API is not called. Server fieldErrors map to the matching field.
- CashOutScreen.kt: CashOutRoute and the stateless CashOutScreen with amount, beneficiary name, and IBAN fields, the minimum hint, the Cash out / Check again button, app-written messages for every CashOutError, and a receipt card (processing, not paid; reference and estimated arrival) with Make another cash out.

Planned: refresh the available amount after INSUFFICIENT_FUNDS; a countdown for RATE_LIMITED; components package (AmountInput, CashOutConfirmation).
