package com.buxuesong.account.infrastructure.general.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * 数据库初始化组件，在应用启动时执行 SQL 脚本
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始初始化数据库...");

        try {
            // 加载 SQL 脚本
            Resource resource = resourceLoader.getResource("classpath:db/init.sql");
            if (resource.exists()) {
                // 读取 SQL 脚本内容
                String sqlScript = new BufferedReader(
                    new InputStreamReader(resource.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));

                // 执行 SQL 脚本
                logger.info("执行数据库初始化脚本");
                jdbcTemplate.execute(sqlScript);
                logger.info("数据库初始化完成");
            } else {
                logger.warn("数据库初始化脚本不存在: classpath:db/init.sql");
            }
        } catch (Exception e) {
            logger.error("数据库初始化失败", e);
        }
    }
}
