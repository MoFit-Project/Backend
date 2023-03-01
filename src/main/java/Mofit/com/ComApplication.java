package Mofit.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

//
@EnableWebSecurity
@EnableCaching
@SpringBootApplication
public class ComApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComApplication.class, args);
	}

}
