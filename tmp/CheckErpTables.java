import java.sql.*;
public class CheckErpTables {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    String password = args[0];
    String db = args[1];
    String url = "jdbc:mysql://127.0.0.1:3306/" + db + "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
    try (Connection conn = DriverManager.getConnection(url, "root", password);
         Statement st = conn.createStatement()) {
      ResultSet rs = st.executeQuery("select table_name from information_schema.tables where table_schema='" + db + "' and table_name in ('system_users','system_user','erp_finance_ledger','erp_finance_subject','erp_ap_statement') order by table_name");
      while (rs.next()) {
        System.out.println("TABLE=" + rs.getString(1));
      }
    }
  }
}
