package no.nav.tag.finnkandidatapi.config;

import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Fysisk;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class JdbcConvertersConfig extends JdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new Converter<String, ArrayList<Fysisk>>() {
                    @Nullable
                    @Override
                    public ArrayList<Fysisk> convert(String in) {
                        return stringTilListe(in);
                    }
                },
                new Converter<ArrayList<Fysisk>, String>() {
                    @Nullable
                    @Override
                    public String convert(ArrayList<Fysisk> in) {
                        return listeTilString(in);
                    }
                }));
    }

    private ArrayList<Fysisk> stringTilListe(String string) {
        return Stream.of(string.split(","))
                .map(Fysisk::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String listeTilString(ArrayList<Fysisk> list) {
        String[] stringlist = list.stream()
                .map(Enum::name)
                .toArray(String[]::new);
        return String.join(",", stringlist);
    }
}
