package com.Alanya;

import java.io.Serializable;
import java.util.Map;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Map<String, String> data;
    private final ServerResponseType type;

    public ServerResponse(boolean success, String message, Map<String, String> data, ServerResponseType type) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getData() {
        return data;
    }
    
    public ServerResponseType getType() {
        return type;
    }
}