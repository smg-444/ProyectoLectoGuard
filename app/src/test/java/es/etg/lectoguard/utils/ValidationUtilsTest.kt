package es.etg.lectoguard.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `valid email returns true`() {
        assertTrue(ValidationUtils.isValidEmail("test@email.com"))
    }

    @Test
    fun `invalid email returns false`() {
        assertFalse(ValidationUtils.isValidEmail("bademail"))
    }

    @Test
    fun `valid password returns true`() {
        assertTrue(ValidationUtils.isValidPassword("123456"))
    }

    @Test
    fun `short password returns false`() {
        assertFalse(ValidationUtils.isValidPassword("123"))
    }
} 