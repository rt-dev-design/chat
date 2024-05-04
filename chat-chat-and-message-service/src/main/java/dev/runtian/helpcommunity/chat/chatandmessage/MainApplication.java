package dev.runtian.helpcommunity.chat.chatandmessage;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 主类，项目入口
 */
@SpringBootApplication
@MapperScan("dev.runtian.helpcommunity.chat.chatandmessage.mapper")
@EnableDubbo
@EnableTransactionManagement
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
