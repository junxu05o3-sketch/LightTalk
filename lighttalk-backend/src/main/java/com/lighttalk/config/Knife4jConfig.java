package com.lighttalk.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Knife4j 接口文档配置类
 * 访问地址: http://localhost:8080/api/doc.html
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class Knife4jConfig {

    /**
     * 配置 API 文档
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 扫描 Controller 所在包
                .apis(RequestHandlerSelectors.basePackage("com.lighttalk.controller"))
                .paths(PathSelectors.any())
                .build()
                // 添加 JWT 认证
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * API 基本信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("LightTalk API 文档")
                .description("LightTalk 实时聊天系统后端接口文档")
                .version("1.0.0")
                .contact(new Contact("LightTalk", "", ""))
                .build();
    }

    /**
     * 安全方案：JWT Token
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> schemes = new ArrayList<>();
        // 设置 Authorization 请求头
        schemes.add(new ApiKey("Authorization", "Authorization", "header"));
        return schemes;
    }

    /**
     * 安全上下文
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> contexts = new ArrayList<>();
        contexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(operationContext -> true)
                .build());
        return contexts;
    }

    /**
     * 默认安全引用
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> references = new ArrayList<>();
        references.add(new SecurityReference("Authorization", authorizationScopes));
        return references;
    }
}
