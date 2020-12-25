package com.iconloop.iitpvault;

import com.iconloop.iitpvault.config.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
//@Import({DataSourceConfig.class})
public class IitpVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(IitpVaultApplication.class, args);
	}

}
