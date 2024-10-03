package com.boctool.webservice_engine.entity;

import java.util.Map;

public class ResponseDTO {
    private int code;
    private String status;
    private String message;
    private int rowCount;
    private Object data;
    private double runtime;

    public int getCode() {return code;}

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRowCount() {return rowCount;}

    public void setRowCount(int rowCount) {this.rowCount = rowCount;}

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public double getRuntime() {
        return runtime;
    }

    public void setRuntime(double runtime) {
        this.runtime = runtime;
    }
}
