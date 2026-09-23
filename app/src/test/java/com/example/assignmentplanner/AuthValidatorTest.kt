package com.example.assignmentplanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validateEmail_correctEmail_returnsTrue() {
        assertTrue(AuthValidator.isValidEmail("student@uct.ac.za"))
    }

    @Test
    fun validateEmail_incorrectEmail_returnsFalse() {
        assertFalse(AuthValidator.isValidEmail("studentuct.ac.za"))
        assertFalse(AuthValidator.isValidEmail(""))
    }

    @Test
    fun validatePassword_correctPassword_returnsTrue() {
        assertTrue(AuthValidator.isValidPassword("SecurePass123"))
    }

    @Test
    fun validatePassword_shortPassword_returnsFalse() {
        assertFalse(AuthValidator.isValidPassword("Pass1"))
    }
    
    @Test
    fun validatePassword_noDigit_returnsFalse() {
        assertFalse(AuthValidator.isValidPassword("PasswordNoDigit"))
    }
}
