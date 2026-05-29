param(
  [string]$JdbcUrl='jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true',
  [string]$Username='root',
  [string]$Password='123456'
)
$ErrorActionPreference='Stop'
$driverJar='D:\ruoyi-vue-pro\tmp\m2repo\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar'
$cp='D:\ruoyi-vue-pro\tmp;' + $driverJar
@'
import java.sql.*;
public class Q {
  public static void main(String[] args) throws Exception {
    try (Connection c = DriverManager.getConnection(args[0], args[1], args[2]); Statement s = c.createStatement()) {
      ResultSet rs = s.executeQuery("select count(*) from system_menu where deleted=b'0'");
      while (rs.next()) System.out.println("active_menu_count="+rs.getInt(1));
      rs = s.executeQuery("select count(*) from system_role_menu where deleted=b'0'");
      while (rs.next()) System.out.println("active_role_menu_count="+rs.getInt(1));
      rs = s.executeQuery("select id, code, status, deleted from system_role where code='super_admin'");
      while (rs.next()) System.out.println("super_admin role id="+rs.getLong(1)+" code="+rs.getString(2)+" status="+rs.getInt(3)+" deleted="+rs.getString(4));
    }
  }
}
