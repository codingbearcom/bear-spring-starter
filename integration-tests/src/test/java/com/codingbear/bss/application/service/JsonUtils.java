/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.service;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.junit.jupiter.api.Assertions.fail;

public class JsonUtils {
    private JsonUtils() {
        // prevent instantiation
    }

    /**
     * Asserts equality of two JSON objects given as String. Indentation, whitespaces and order of elements is ignored.
     *
     * @param expected Expected JSON string
     * @param actual   Actual JSON string
     */
    public static void assertEqualsJson(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);

        } catch (AssertionError | JSONException e) {
            fail("Expected: " + expected + "\nBut Found: " + actual);
        }
    }
}
