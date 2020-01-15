package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 01 - Jan - 2017.
 */
public class Result {

    public enum Code {
        OK,
        ERROR,
        AUTH_REQUIRED,
        GIT_AUTH_REQUIRED,
        NOT_AUTHORIZED,
        CANCEL
    }

    private Code code;
    private Object value;
    private Object valueExt;

    /**
     * Construct result
     * @param code code
     * @param value value
     * @param valueExt extended value
     */
    public Result(Code code, Object value, Object valueExt) {
        this.code = code;
        this.value = value;
        this.valueExt = valueExt;
    }

    public Result(Code code, Object value) {
        this.code = code;
        this.value = value;
    }

    public Result(Object value) {
        this.code = Code.OK;
        this.value = value;
    }

    public Object getValueExt() {
        return valueExt;
    }

    public void setValueExt(Object valueExt) {
        this.valueExt = valueExt;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
