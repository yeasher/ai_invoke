package com.shin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author shiner
 * @since 2025/6/13
 */
@SpringBootApplication
public class AiInvokeStarter {
    public static void main(String[] args) {
        try {
            SpringApplication.run(AiInvokeStarter.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}