package com.buxuesong.account.infrastructure.general.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 环境变量配置类，用于加载.env文件中的环境变量 在Spring应用启动时自动加载环境变量到Spring环境中
 */
public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DotEnvConfig.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        loadEnvFile();
    }

    private void loadEnvFile() {
        logger.info("开始加载.env文件...");

        // 获取项目根目录下的.env文件路径
        Path envPath = Paths.get(".env");

        if (!Files.exists(envPath)) {
            logger.warn(".env文件不存在，跳过环境变量加载");
            return;
        }

        Map<String, Object> envProperties = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行和注释行
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // 解析键值对
                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();

                    // 移除值两端的引号（如果存在）
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    envProperties.put(key, value);
                    System.setProperty(key, value);
                    count++;
                    logger.debug("加载环境变量: {} = {}", key, value);
                }
            }

            logger.info("成功加载{}个环境变量", count);

        } catch (IOException e) {
            logger.error("读取.env文件失败", e);
        }
    }
}