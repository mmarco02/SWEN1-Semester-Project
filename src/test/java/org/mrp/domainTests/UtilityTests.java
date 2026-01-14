package org.mrp.domainTests;

import org.junit.jupiter.api.Test;
import org.mrp.service.utils.HashUtils;
import org.mrp.service.utils.PathUtils;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UtilityTests {

    @Test
    void testHashUtilsHashAndVerifySuccess() throws NoSuchAlgorithmException {
        String password = "mySecretPassword123";

        HashUtils.HashResult hashResult = HashUtils.hashWithSalt(password);
        boolean isValid = HashUtils.verify(password, hashResult.hash(), hashResult.salt());

        assertNotNull(hashResult.hash());
        assertNotNull(hashResult.salt());
        assertTrue(isValid);
    }

    @Test
    void testHashUtilsVerifyFailsWithWrongPassword() throws NoSuchAlgorithmException {
        String correctPassword = "correct123";
        String wrongPassword = "wrong456";

        HashUtils.HashResult hashResult = HashUtils.hashWithSalt(correctPassword);

        boolean isValid = HashUtils.verify(wrongPassword, hashResult.hash(), hashResult.salt());

        assertFalse(isValid);
    }

    @Test
    void testHashUtilsHashProducesDifferentResultsForSameInput() throws NoSuchAlgorithmException {
        String password = "samePassword";

        HashUtils.HashResult result1 = HashUtils.hashWithSalt(password);
        HashUtils.HashResult result2 = HashUtils.hashWithSalt(password);

        assertNotEquals(result1.hash(), result2.hash());
        assertNotEquals(result1.salt(), result2.salt());
    }

    @Test
    void testPathUtilsCreatePatternFromTemplate() {
        String template = "/api/users/{id}/profile";

        Pattern pattern = PathUtils.createPatternFromTemplate(template);

        assertNotNull(pattern);

        assertTrue(pattern.matcher("/api/users/123/profile").matches());
        assertTrue(pattern.matcher("/api/users/123/profile/").matches()); //with trailing slash
        assertFalse(pattern.matcher("/api/users/abc/profile").matches()); //non-numeric ID
        assertFalse(pattern.matcher("/api/users/123").matches()); //incomplete path
    }

    @Test
    void testPathUtilsPatternWithMultipleIds() {
        String template = "/api/entries/{id}/ratings/{id}";

        Pattern pattern = PathUtils.createPatternFromTemplate(template);

        assertTrue(pattern.matcher("/api/entries/100/ratings/50").matches());
        assertTrue(pattern.matcher("/api/entries/1/ratings/2/").matches());
        assertFalse(pattern.matcher("/api/entries/abc/ratings/def").matches());
    }
}