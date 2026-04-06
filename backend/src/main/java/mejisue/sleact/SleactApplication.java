package mejisue.sleact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "mejisue.sleact")
@SpringBootApplication
public class SleactApplication {

	public static void main(String[] args) {
		SpringApplication.run(SleactApplication.class, args);
	}

}
