package no.nav.tag.finnkandidatapi.databaseTest;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface FinnKandidatRepository extends CrudRepository<FinnKandidat, Integer> {
    @Query("SELECT 'ok'")
    public String healthcheck();
}
