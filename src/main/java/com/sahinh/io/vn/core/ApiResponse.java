package com.sahinh.io.vn.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** Chuẩn JSON trả về: {code, message, data}. */
public class ApiResponse {
    public static Map<String, Object> ok(Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", 200);
        r.put("message", "SUCCESS");
        r.put("data", data);
        return r;
    }

    public static Map<String, Object> ok(String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", 200);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("message", message);
        r.put("data", null);
        return r;
    }
}
