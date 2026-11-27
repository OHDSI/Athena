package com.odysseusinc.athena.repositories.athena;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RegistrationDictionaryRepository {

    public record Location(long id, String name, String isoCode) {}
    public record ProfessionalType(long id, String name) {}

    // Apply the limit to search hits, then include the selected option once even if it falls outside that page.
    private static final String SUGGEST = """
            (SELECT id, name, iso_code FROM options
             WHERE name ILIKE :pattern ORDER BY name, id LIMIT :limit)
            UNION
            SELECT id, name, iso_code FROM options
            WHERE CAST(id AS text) = :selected OR upper(iso_code) = upper(:selected)
            ORDER BY name, id
            """;

    private final JdbcClient jdbc;

    public RegistrationDictionaryRepository(@Qualifier("dataSourceAthenaDB") DataSource dataSource) {
        jdbc = JdbcClient.create(dataSource);
    }

    public List<ProfessionalType> professionalTypes() {
        return jdbc.sql("SELECT id, name FROM professional_types ORDER BY name, id")
                .query(ProfessionalType.class).list();
    }

    public List<Location> countries(String query, int limit, String includeId) {
        return suggest(jdbc.sql("""
                WITH options AS (SELECT id, name, alfa2 AS iso_code FROM countries)
                """ + SUGGEST), query, limit, includeId);
    }

    public List<Location> provinces(String countryId, String query, int limit, String includeId) {
        return suggest(jdbc.sql("""
                WITH options AS (
                    SELECT p.id, p.name, p.iso_code
                    FROM states_provinces p JOIN countries c ON c.id = p.country_id
                    WHERE CAST(c.id AS text) = :country OR upper(c.alfa2) = upper(:country)
                )
                """ + SUGGEST).param("country", countryId), query, limit, includeId);
    }

    private List<Location> suggest(JdbcClient.StatementSpec statement, String query, int limit, String includeId) {
        // PostgreSQL ILIKE uses backslash as its default escape character.
        String literal = query == null ? "" : query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return statement.param("pattern", "%" + literal + "%")
                .param("limit", limit)
                .param("selected", includeId)
                .query(Location.class).list();
    }
}
