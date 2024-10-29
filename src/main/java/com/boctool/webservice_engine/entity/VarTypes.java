package com.boctool.webservice_engine.entity;

import java.util.Set;

public enum VarTypes {
    CHAR("char"),
    INTEGER("integer"),
    DATE("date"),
    DATETIME("datetime"),
    ARRAY_CHAR("array_char"),
    ARRAY_INTEGER("array_integer"),
    FUNCTION("function");

    private final String type;

    VarTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
