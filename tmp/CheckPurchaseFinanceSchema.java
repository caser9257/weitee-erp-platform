import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckPurchaseFinanceSchema {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"
            + "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            + "&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            printTable(connection, "erp_ap_statement");
            printTable(connection, "erp_ap_statement_item");
            printTable(connection, "erp_finance_payment_allocate");
            printColumn(connection, "erp_finance_payment_item", "ap_statement_id");
        }
    }

    private static void printTable(Connection connection, String tableName) throws Exception {
        String sql = "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "ruoyi-vue-pro");
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                System.out.println("table." + tableName + "=" + (resultSet.getInt(1) > 0 ? "exists" : "missing"));
            }
        }
    }

    private static void printColumn(Connection connection, String tableName, String columnName) throws Exception {
        String sql = "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "ruoyi-vue-pro");
            statement.setString(2, tableName);
            statement.setString(3, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                System.out.println("column." + tableName + "." + columnName + "=" + (resultSet.getInt(1) > 0 ? "exists" : "missing"));
            }
        }
    }
}
