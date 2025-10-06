package com.mymicroservice.gateway.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorItem {

    private String message;
    private String timestamp;
    private String url;
    private int statusCode;

    private Map<String, String> fieldErrors;
}
