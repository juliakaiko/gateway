package com.mymicroservice.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayCustomProperties {

    private Public publicPaths;
    private Internal internalPaths;

    @Data
    public static class Public {
        private List<String> paths;
    }

    @Data
    public static class Internal {
        private List<String> paths;
    }
}
