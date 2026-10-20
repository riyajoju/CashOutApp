# data/remote/dto

Kotlin data classes for API requests and responses. Gson maps them by reflection; app/src/main/keepRules/rules.keep keeps this package under R8.

Implemented: ProblemDto.kt (service error body; every field nullable, money as BigDecimal; detail is for logs only).

Implemented: CashOutResponseDto.kt (submit 200 body; covers success and the legacy isSuccess=false refusal; amount and estimatedArrival kept as strings).

Implemented: AccountResponseDto.kt (summary; balance, equity, and freeMargin as BigDecimal) and CashOutRequestDto.kt with DestinationDto (amount as BigDecimal JSON number; it must already have scale 2; method defaults to bank; a null reason is omitted).
