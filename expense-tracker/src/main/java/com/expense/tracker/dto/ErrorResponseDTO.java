package com.expense.tracker.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseDTO {
    private int            status;
    private String         error;
    private String         message;
    private String         path;
    private LocalDateTime timestamp;

    private List<FieldErrorDTO> fieldErrors;


    public static class FieldErrorDTO {
        private String field;
        private String message;

        public FieldErrorDTO(String field, String message) {
            this.field   = field;
            this.message = message;
        }

        public String getField()   { return field; }
        public String getMessage() { return message; }
    }


    public ErrorResponseDTO() {
        this.timestamp   = LocalDateTime.now();
        this.fieldErrors = List.of();
    }

    public int getStatus()                          { return status; }
    public void setStatus(int status)               { this.status = status; }

    public String getError()                        { return error; }
    public void setError(String error)              { this.error = error; }

    public String getMessage()                      { return message; }
    public void setMessage(String message)          { this.message = message; }

    public String getPath()                         { return path; }
    public void setPath(String path)                { this.path = path; }

    public LocalDateTime getTimestamp()                     { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp)       { this.timestamp = timestamp; }

    public List<FieldErrorDTO> getFieldErrors()                     { return fieldErrors; }
    public void setFieldErrors(List<FieldErrorDTO> fieldErrors)     { this.fieldErrors = fieldErrors; }
}
