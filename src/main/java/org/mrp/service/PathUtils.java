package org.mrp.service;

import java.util.regex.Pattern;

public class PathUtils {
    public static Pattern createPatternFromTemplate(String pathTemplate) {
        // Replace {id} with regex group
        String patternString = pathTemplate.replace("{id}", "(\\d+)");

        // escape special characters
        patternString = Pattern.quote(patternString);

        // Remove \Q and \E markers (only get pattern)
        patternString = patternString.substring(2, patternString.length() - 2);

        return Pattern.compile("^" + patternString + "$");
    }
}