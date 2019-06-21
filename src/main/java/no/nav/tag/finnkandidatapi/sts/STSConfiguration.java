package no.nav.tag.finnkandidatapi.sts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class STSConfiguration {

    @Value("${SERVICEBRUKER_BRUKERNAVN}")
    private String brukernavn;

    @Value("${SERVICEBRUKER_PASSORD}")
    private String passord;

    @Bean
    public RestTemplate stsBasicAuthRestTemplate() {
        return new RestTemplateBuilder()
                .basicAuthentication(brukernavn, passord)
                .build();
    }

}
