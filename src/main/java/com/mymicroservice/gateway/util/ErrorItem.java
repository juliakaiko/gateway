package com.mymicroservice.gateway.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorItem {

    private String message;
    private String timestamp;
    private String url;
    private int statusCode;
}
