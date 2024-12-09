package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {
    //配置项appplication,yml会通过这个配置属性类加载
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
