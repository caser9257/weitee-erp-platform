import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcSqlRunner {

    private static final String URL = System.getenv("JDBC_URL") != null ? System.getenv("JDBC_URL")
            : "jdbc:mysql://127.0.0.1:3306/weitee-erp"
            + "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            + "&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true";
    private static final String USERNAME = System.getenv("JDBC_USERNAME") != null ? System.getenv("JDBC_USERNAME") : "root";
    private static final String PASSWORD = System.getenv("JDBC_PASSWORD") != null ? System.getenv("JDBC_PASSWORD") : "";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: JdbcSqlRunner <sql-file>");
        }
        Path sqlFile = Path.of(args[0]).toAbsolutePath().normalize();
        String content = Files.readString(sqlFile, StandardCharsets.UTF_8);
        List<String> statements = splitStatements(content);
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            int executed = 0;
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                statement.execute(trimmed);
                executed++;
                System.out.println("executed[" + executed + "] " + summarize(trimmed));
            }
            System.out.println("done=true");
            System.out.println("statement_count=" + executed);
        }
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    current.append(c);
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '-' && next == '-' && isSqlLineCommentStart(sql, i)) {
                    inLineComment = true;
                    i++;
                    continue;
                }
                if (c == '#') {
                    inLineComment = true;
                    continue;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                if (inSingleQuote && next == '\'') {
                    current.append(c).append(next);
                    i++;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                current.append(c);
                continue;
            }
            if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
                continue;
            }
            if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
                current.append(c);
                continue;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote && !inBacktick) {
                statements.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private static boolean isSqlLineCommentStart(String sql, int index) {
        int nextIndex = index + 2;
        if (nextIndex >= sql.length()) {
            return true;
        }
        char next = sql.charAt(nextIndex);
        return Character.isWhitespace(next);
    }

    private static String summarize(String sql) {
        String compact = sql.replaceAll("\\s+", " ").trim();
        return compact.length() <= 120 ? compact : compact.substring(0, 117) + "...";
    }
}
