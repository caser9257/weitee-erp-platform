import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ApplyMysqlSql {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: <jdbcUrl> <username> <password> <sqlFile>");
        }
        String jdbcUrl = args[0];
        String username = args[1];
        String password = args[2];
        String sqlFile = args[3];

        String content = Files.readString(Path.of(sqlFile), StandardCharsets.UTF_8);
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }
        content = content.replaceAll("(?s)/\\*.*?\\*/", "\n");
        String normalizedSql = content.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("--"))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        String[] statements = normalizedSql.split(";");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            for (String statement : statements) {
                String sql = statement.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                try (Statement jdbcStatement = connection.createStatement()) {
                    boolean hasResultSet = jdbcStatement.execute(sql);
                    System.out.println("EXECUTED: " + sql);
                    if (hasResultSet) {
                        try (ResultSet resultSet = jdbcStatement.getResultSet()) {
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            int columnCount = metaData.getColumnCount();
                            while (resultSet.next()) {
                                StringBuilder row = new StringBuilder("ROW:");
                                for (int i = 1; i <= columnCount; i++) {
                                    row.append(' ')
                                            .append(metaData.getColumnLabel(i))
                                            .append('=')
                                            .append(resultSet.getString(i));
                                }
                                System.out.println(row);
                            }
                        }
                    }
                }
            }
        }
    }
}
