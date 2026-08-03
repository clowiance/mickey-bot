package dev.clowiance.mickey.infrastructure;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Path;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseMigrationTest {

    @TempDir
    Path tempDir;

    @Test
    void createsCurrentSchemaOnEmptyDatabase() throws Exception {
        SQLiteDataSource dataSource = dataSource("empty.db");

        migrate(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            assertThat(hasTable(connection, "managed_study_groups")).isTrue();
            assertThat(hasColumn(connection, "scheduled_broadcasts", "status")).isTrue();
            assertThat(hasColumn(connection, "warns", "chat_id")).isTrue();
            assertThat(hasIndex(connection, "idx_broadcast_due")).isTrue();
        }
    }

    @Test
    void upgradesLegacySchemaWithoutLosingRows() throws Exception {
        SQLiteDataSource dataSource = dataSource("legacy.db");
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE scheduled_broadcasts "
                    + "(id INTEGER PRIMARY KEY, chat_id BIGINT, send_at TIMESTAMP, sent BOOLEAN, text VARCHAR(255))");
            statement.execute("INSERT INTO scheduled_broadcasts(id, chat_id, sent, text) VALUES (1, 10, 0, 'hello')");
            statement.execute("CREATE TABLE warns (id INTEGER PRIMARY KEY, active BOOLEAN NOT NULL, "
                    + "admin_id BIGINT NOT NULL, created_at TIMESTAMP NOT NULL, reason VARCHAR(255) NOT NULL, "
                    + "user_id BIGINT NOT NULL)");
            statement.execute("CREATE TABLE punishment_logs (id INTEGER PRIMARY KEY, admin_id BIGINT, "
                    + "created_at TIMESTAMP, reason VARCHAR(255), type VARCHAR(255), user_id BIGINT, username VARCHAR(255))");
            statement.execute("CREATE TABLE managed_study_groups (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "category VARCHAR(30) NOT NULL, specialty VARCHAR(250) NOT NULL, course VARCHAR(100), "
                    + "invite_link VARCHAR(200) NOT NULL UNIQUE, note VARCHAR(500), active BOOLEAN NOT NULL, "
                    + "updated_at TIMESTAMP NOT NULL)");
            statement.execute("INSERT INTO managed_study_groups(category, specialty, invite_link, active, updated_at) "
                    + "VALUES ('UNIVERSITY', 'ІПЗ', 'https://t.me/+university', 1, CURRENT_TIMESTAMP)");
            statement.execute("INSERT INTO managed_study_groups(category, specialty, invite_link, active, updated_at) "
                    + "VALUES ('COLLEGE', 'Коледж', 'https://t.me/+college', 1, CURRENT_TIMESTAMP)");
        }

        migrate(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            assertThat(hasColumn(connection, "scheduled_broadcasts", "attempt_count")).isTrue();
            assertThat(hasColumn(connection, "punishment_logs", "chat_id")).isTrue();
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT status, text FROM scheduled_broadcasts WHERE id = 1")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("status")).isEqualTo("PENDING");
                assertThat(result.getString("text")).isEqualTo("hello");
            }
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT category, specialty FROM managed_study_groups")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("category")).isEqualTo("UNIVERSITY");
                assertThat(result.getString("specialty")).isEqualTo("ІПЗ");
                assertThat(result.next()).isFalse();
            }
        }
    }

    private SQLiteDataSource dataSource(String fileName) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tempDir.resolve(fileName));
        return dataSource;
    }

    private void migrate(SQLiteDataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    private boolean hasTable(Connection connection, String table) throws Exception {
        try (var result = connection.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean hasColumn(Connection connection, String table, String column) throws Exception {
        try (var result = connection.getMetaData().getColumns(null, null, table, column)) {
            return result.next();
        }
    }

    private boolean hasIndex(Connection connection, String index) throws Exception {
        try (var statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='index' AND name=?")) {
            statement.setString(1, index);
            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
