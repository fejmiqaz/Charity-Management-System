package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsernameService {
    private final UserAccountRepository accounts;
    private final JdbcTemplate jdbc;

    static String part(String value) {
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return normalized.isEmpty() ? "user" : normalized.substring(0, Math.min(100, normalized.length()));
    }

    public String generate(String name, String surname) {
        return part(name) + "." + part(surname);
    }

    @Transactional
    public void migrate() {
        // Hibernate update does not remove the unique constraint from the earlier implementation.
        var constraints = jdbc.queryForList("""
            SELECT DISTINCT tc.constraint_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage k
              ON tc.constraint_name = k.constraint_name AND tc.table_schema = k.table_schema
             AND tc.table_name = k.table_name
            WHERE LOWER(tc.table_name) = 'user_accounts' AND LOWER(k.column_name) = 'username'
              AND tc.constraint_type = 'UNIQUE' AND tc.table_schema = CURRENT_SCHEMA
            """, String.class);
        for (String constraint : constraints)
            jdbc.execute("ALTER TABLE user_accounts DROP CONSTRAINT \"" + constraint.replace("\"", "\"\"") + "\"");
        for (var account : accounts.findAll()) {
            if (account.getUsername() == null) {
                String[] parts = account.getName().trim().split("\\s+", 2);
                account.setUsername(generate(parts[0], parts.length > 1 ? parts[1] : "user"));
            } else {
                account.setUsername(account.getUsername().replaceFirst("\\.\\d+$", ""));
            }
        }
        accounts.flush();
    }
}
