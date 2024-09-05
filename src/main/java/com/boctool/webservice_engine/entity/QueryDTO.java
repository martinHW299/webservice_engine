package com.boctool.webservice_engine.entity;

import java.util.Map;

public class QueryDTO {
    String sql;
    Map<String, String> parameters;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
