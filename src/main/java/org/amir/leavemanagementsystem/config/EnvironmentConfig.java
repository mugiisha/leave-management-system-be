package org.amir.leavemanagementsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:.env")
public class EnvironmentConfig {
    // This class is just a marker to load the .env file
    // Spring will automatically load properties from the .env file
} 