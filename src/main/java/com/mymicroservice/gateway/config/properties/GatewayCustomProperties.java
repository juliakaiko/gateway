package com.mymicroservice.gateway.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayCustomProperties {

    private Public publicPaths;
    private Internal internalPaths;

    @Getter
    @Setter
    public static class Public {
        private List<String> paths;
    }

    @Getter
    @Setter
    public static class Internal {
        private List<String> paths;
    }
}
