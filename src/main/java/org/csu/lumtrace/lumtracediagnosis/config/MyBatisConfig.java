package org.csu.lumtrace.lumtracediagnosis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 扫描配置
 */
@Configuration
@MapperScan("org.csu.lumtrace.lumtracediagnosis.mapper")
public class MyBatisConfig {
}
