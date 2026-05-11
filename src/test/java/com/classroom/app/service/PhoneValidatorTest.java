package com.classroom.app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneValidatorTest {
    @Test
    void shouldValidateE164LikePhone() {
        assertTrue(PhoneValidator.isValid("+8801712345678"));
        assertTrue(PhoneValidator.isValid("14155552671"));
        assertFalse(PhoneValidator.isValid("01712"));
        assertFalse(PhoneValidator.isValid("abc"));
    }
}
