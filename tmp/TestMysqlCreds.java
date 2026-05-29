import java.sql.*;
public class TestMysqlCreds {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    String[] passwords = {"123456", "Root@123456"};
    String url = "jdbc:mysql://127.0.0.1:3306/?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
    for (String password : passwords) {
      try (Connection conn = DriverManager.getConnection(url, "root", password);
           Statement st = conn.createStatement()) {
        System.out.println("PASSWORD_OK=" + password);
        ResultSet rs = st.executeQuery("show databases");
        while (rs.next()) {
          System.out.println("DB=" + rs.getString(1));
        }
        return;
      } catch (Exception ex) {
        System.out.println("PASSWORD_FAIL=" + password + " => " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
      }
    }
  }
}
