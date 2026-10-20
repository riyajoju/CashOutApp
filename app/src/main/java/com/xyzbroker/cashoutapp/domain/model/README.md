# domain/model

Use exact amounts with explicit currency; avoid Double. Endpoint-specific CashOutError and outcomes are in domain/result_handlimg.

Implemented: AccountBalance.kt (currency, balance, equity, freeMargin as BigDecimal; available is the smallest of the three floored to the cent with RoundingMode.FLOOR).

Planned: Money.kt and CashOutRequest.kt.
