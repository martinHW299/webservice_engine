package com.boctool.webservice_engine.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

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

    public static String convertTextToMd5(String query){
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
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = parameters.get(paramName);

            if (paramValue != null) {
                String paramValueStr;

                if (paramValue instanceof Number) {
                    paramValueStr = paramValue.toString();
                } else {
                    paramValueStr = "'" + paramValue.toString() + "'";
                }

                matcher.appendReplacement(builder, paramValueStr);
            } else {
                throw new RuntimeException("Parameter " + paramName + " not found in provided parameters");
            }
        }

        matcher.appendTail(builder);
        return builder.toString();
    }


    public static String normalizeSql(String sql) {
        if (sql== null) {
            return null;
        }
        sql = sql.replace(";", "");
        return sql.trim().toLowerCase();
    }

}
