# presentation/cashout

CashOutUiState is defined in CashOutViewModel.kt. The ViewModel owns state, handles events, and calls use cases. The route collects state with lifecycle awareness. The Compose screen receives state and callbacks and stays stateless.

Implemented: CashOutViewModel.kt (states Loading, Form(balance), BalanceError; loadBalance on start and on retry) and CashOutScreen.kt (CashOutRoute using hiltViewModel and collectAsStateWithLifecycle; stateless CashOutScreen showing the title, Available amount formatted as currency, or an app-written error with Try again). Text comes from strings.xml.

Planned: amount, beneficiary name, and IBAN inputs; client-side amount validation; submitting, request received, and submit error states; components package (AmountInput, CashOutConfirmation).
