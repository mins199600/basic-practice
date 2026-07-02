package com.practice.logincrud;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class LoginCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginCrudApplication.class, args);
    }

    @Component
    static class DebugDbUrlPrinter {
        @Value("${spring.datasource.url}")
        private String url;

        @PostConstruct
        void print() {
            System.out.println("=== spring.datasource.url = " + url);
            System.out.println("java.security.krb5.conf = " + System.getProperty("java.security.krb5.conf"));
            System.out.println("java.security.krb5realm = " + System.getProperty("java.security.krb5.realm"));
            System.out.println("java.security.auth.login.config = " + System.getProperty("java.security.auth.login.config"));
        }
    }
}
