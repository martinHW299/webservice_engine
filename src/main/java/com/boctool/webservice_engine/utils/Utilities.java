package com.boctool.webservice_engine.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static final Set<String> ALLOWED_TYPES = Set.of("char", "integer", "date", "datetime");

    public static void validateInputTypeParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String paramType = entry.getValue().toLowerCase();
            if (!ALLOWED_TYPES.contains(paramType)) {
                throw new IllegalArgumentException("Invalid parameter type: " + paramType + " for key: " + entry.getKey());
            }
        }
    }

    public static String determineQueryType(String query) {
        String trimmedQuery = query.trim().toUpperCase();
        if (trimmedQuery.startsWith("SELECT")) {
            return "SELECT";
        } else if (trimmedQuery.startsWith("INSERT")) {
            return "INSERT";
        } else if (trimmedQuery.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (trimmedQuery.startsWith("DELETE")) {
            return "DELETE";
        } else {
            return "UNKNOWN";
        }
    }

    public static String convertQueryToMD5(String query){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(query.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static String replaceParameters(String query, Map<String, Object> parameters) {
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = pattern.matcher(query);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = parameters.get(paramName);

            if (paramValue != null) {
                String paramValueStr = paramValue.toString();
                matcher.appendReplacement(buffer, "'" + paramValueStr + "'");
            } else {
                throw new RuntimeException("Parameter " + paramName + " not found in provided parameters");
            }
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String normalizeSql(String sql) {
        if (sql== null) {
            return null;
        }
        sql = sql.replace(";", "");
        return sql.trim().toLowerCase();
    }

}
