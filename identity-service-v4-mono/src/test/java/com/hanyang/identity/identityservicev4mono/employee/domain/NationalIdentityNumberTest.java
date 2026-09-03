package com.hanyang.identity.identityservicev4mono.employee.domain;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NationalIdentityNumberTest {

    @Test
    void acceptsVietnamNationalIdCardWithExactlyTwelveDigits() {
        NationalIdentityNumber number = NationalIdentityNumber.of(
                " vn ",
                NationalIdentityType.NATIONAL_ID_CARD,
                " 001204012345 "
        );

        assertEquals("001204012345", number.value());
        assertEquals("2345", number.lastFour());
    }

    @Test
    void rejectsVietnamNationalIdWithInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                NationalIdentityNumber.of(
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "00120401234A"
                )
        );
    }

    @Test
    void rejectsUnsupportedCountryUntilItsValidationPolicyExists() {
        assertThrows(IllegalArgumentException.class, () ->
                NationalIdentityNumber.of(
                        "KR",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "1234567890123"
                )
        );
    }
}