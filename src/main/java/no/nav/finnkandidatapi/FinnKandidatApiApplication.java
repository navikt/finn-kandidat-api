package no.nav.finnkandidatapi;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {
        "springfox.documentation.swagger.web.ApiResourceController",
        "org.springframework"
})
public class FinnKandidatApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinnKandidatApiApplication.class, args);
    }

}
