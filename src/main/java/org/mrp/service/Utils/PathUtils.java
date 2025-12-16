package org.mrp.service.Utils;

import java.util.regex.Pattern;

public class PathUtils {
    public static Pattern createPatternFromTemplate(String pathTemplate) {
        // replace {id} with regex group (\\d+)
        String patternString = pathTemplate
                .replace("{id}", "(\\d+)")
                // handle optional trailing slash
                .replace("/", "\\/");

        // add start and end anchors and make trailing slash optional
        patternString = "^" + patternString + "\\/?$";

        return Pattern.compile(patternString);
    }
}