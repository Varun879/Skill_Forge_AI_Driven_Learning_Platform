package com.skillforge.exception;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int                 status;
    private String              error;
    private String              message;
    private String              path;
    private Instant             timestamp;
    private Map<String, String> fieldErrors;

    private ErrorResponse(Builder b) {
        this.status      = b.status;
        this.error       = b.error;
        this.message     = b.message;
        this.path        = b.path;
        this.timestamp   = b.timestamp;
        this.fieldErrors = b.fieldErrors;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int                 status;
        private String              error;
        private String              message;
        private String              path;
        private Instant             timestamp;
        private Map<String, String> fieldErrors;

        public Builder status(int v)                      { this.status = v;      return this; }
        public Builder error(String v)                    { this.error = v;       return this; }
        public Builder message(String v)                  { this.message = v;     return this; }
        public Builder path(String v)                     { this.path = v;        return this; }
        public Builder timestamp(Instant v)               { this.timestamp = v;   return this; }
        public Builder fieldErrors(Map<String, String> v) { this.fieldErrors = v; return this; }
        public ErrorResponse build()                      { return new ErrorResponse(this); }
    }

    public int                 getStatus()      { return status; }
    public String              getError()       { return error; }
    public String              getMessage()     { return message; }
    public String              getPath()        { return path; }
    public Instant             getTimestamp()   { return timestamp; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
