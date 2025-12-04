package org.mrp.service;

import java.util.regex.Pattern;

public class PathUtils {
    public static Pattern createPatternFromTemplate(String pathTemplate) {
        String escaped = Pattern.quote(pathTemplate);

        escaped = escaped.replace("\\{id\\}", "(\\\\d+)");

        return Pattern.compile("^" + escaped + "$");
    }
}
