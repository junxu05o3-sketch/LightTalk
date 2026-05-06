package com.lighttalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LightTalk 后端服务启动类
 *
 * @SpringBootApplication 包含以下注解：
 * - @Configuration: 标识为配置类
 * - @EnableAutoConfiguration: 启用 Spring Boot 自动配置
 * - @ComponentScan: 自动扫描当前包及子包下的组件
 */
@SpringBootApplication
public class LightTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LightTalkApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   LightTalk Backend Started Successfully");
        System.out.println("   API Doc: http://localhost:8080/api/doc.html");
        System.out.println("==========================================");
    }
}
