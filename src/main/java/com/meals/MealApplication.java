package com.meals;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@MapperScan(basePackages = "com.meals.mapper")//这里加了这个扫描，所有mapper就不需要加注解了
@Slf4j
@SpringBootApplication
@ServletComponentScan//开启组件扫描，扫描到过滤器
@EnableTransactionManagement
@EnableCaching //开启SpringCache缓存注解功能
public class MealApplication {
    public static void main(String[] args) {
        SpringApplication.run(MealApplication.class,args);
        log.info("项目已启动...");
    }
}
