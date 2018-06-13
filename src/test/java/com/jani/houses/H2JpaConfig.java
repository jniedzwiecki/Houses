package com.jani.houses;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.jani.houses")
@PropertySource("classpath:application.yml")
@EnableTransactionManagement
class H2JpaConfig {
}
