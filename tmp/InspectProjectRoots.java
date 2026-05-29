import java.sql.*;
public class InspectProjectRoots {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
    try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
      String[] queries = {
        "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE deleted = b'0' AND path IN ('/project','/pmo') ORDER BY id",
        "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE deleted = b'0' AND parent_id = (SELECT id FROM system_menu WHERE path = '/project' AND deleted = b'0' ORDER BY id LIMIT 1) ORDER BY sort, id",
        "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE deleted = b'0' AND parent_id = (SELECT id FROM system_menu WHERE path = '/pmo' AND deleted = b'0' ORDER BY id LIMIT 1) ORDER BY sort, id",
        "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE deleted = b'0' AND parent_id = (SELECT id FROM system_menu WHERE id = 931410 AND deleted = b'0' ORDER BY id LIMIT 1) ORDER BY sort, id"
      };
      for (String sql : queries) {
        System.out.println("SQL: " + sql);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
          ResultSetMetaData md = rs.getMetaData();
          int cols = md.getColumnCount();
          while (rs.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
              if (i > 1) row.append(" | ");
              row.append(md.getColumnLabel(i)).append('=').append(rs.getString(i));
            }
            System.out.println(row);
          }
        }
      }
    }
  }
}
