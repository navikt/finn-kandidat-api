package no.nav.tag.finnkandidatapi;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = {
		"springfox.documentation.swagger.web.ApiResourceController",
		"org.springframework"
})
public class FinnKandidatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinnKandidatApiApplication.class, args);
	}

}
