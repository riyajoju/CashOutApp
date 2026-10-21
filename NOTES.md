# Notes

## How to run

1. Start the sandbox: `node server.mjs`.
2. Add `sandbox.apiToken=<token from API.md>` to `local.properties` (see
   `local.properties.example`).
3. Run the **uatDebug** build on an emulator.

Tests: `./gradlew testUatDebugUnitTest`.

## Assumptions

- **Success means "received", not "paid".** The success screen says the request is being processed,
  and shows the reference and estimated arrival date.
- **If the app can't be sure what happened, it says so.** After a timeout, a 502 page,
  `INTERNAL_ERROR`
  or an unknown error code, the app says money *may* have left the account. It locks the form and
  shows **Check again**, which sends the same request with the same key, so the server returns its
  saved answer instead of charging again.
- **Needs support (`700.00`)** shows the support reference and no retry button.
- **One key per cash out.** The app creates a random key on the first tap and saves it with the
  form, so it survives rotation and the app being closed by Android. It is only cleared once the
  result is known.
- **Checked on the phone:** all fields are filled, and the amount is a number with at most 2
  decimals, at least $50.00, and not more than available. The IBAN and name are checked by the
  server, and its errors are shown under the right field.
- **No rounding, ever.** Money uses `BigDecimal`. Available is the smallest of balance, equity and
  free margin, always rounded **down** to the cent.
- **Timeout is 30 seconds**, longer than the sandbox's 15-second delay, so `300.00` waits and then
  succeeds.

## Decision I'm least sure of

- Showing "we couldn't confirm" for unknown error codes. It's safe for money, but sometimes the
  customer will be worried when nothing actually happened.
- Saving the IBAN and name on the phone so a retry sends exactly the same request. It's safer for
  money, but it keeps personal details in the app's saved state.

## With another hour

- Refresh the available amount after a refusal or a success.
- Send the `correlationId` on error screens to backend, for support.
- A countdown when rate limited, with the button disabled until then.
- UI tests for the screen.
- Keep an unfinished request even if the app is swiped away (save it to disk).
