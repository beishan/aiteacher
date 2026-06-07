package com.tutorassist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "onlyoffice")
public class OnlyOfficeConfig {

    /** OnlyOffice 内部访问地址（Docker 容器间通信） */
    private String url;

    /** OnlyOffice 浏览器访问地址 */
    private String publicUrl;

    /** JWT 密钥（与 OnlyOffice 容器一致） */
    private String jwtSecret;

    /** 后端浏览器可访问地址（用于回调和文件下载） */
    private String backendPublicUrl;
}
