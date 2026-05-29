import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InspectProjectWarningTree {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"
                + "?useSSL=false&serverTimezone=Asia/Shanghai"
                + "&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
            printQuery(conn, "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE id = 930107");
            printQuery(conn, "SELECT id, name, path, component, parent_id, type, status, visible, sort FROM system_menu WHERE parent_id = (SELECT id FROM system_menu WHERE path = '/pmo' AND deleted = b'0' ORDER BY id LIMIT 1) ORDER BY sort, id");
            printQuery(conn, "SELECT COUNT(*) AS cnt FROM system_role_menu WHERE role_id = 1 AND menu_id = 930107 AND deleted = b'0'");
        }
    }

    private static void printQuery(Connection conn, String sql) throws Exception {
        System.out.println("SQL: " + sql);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        row.append(" | ");
                    }
                    row.append(rs.getMetaData().getColumnLabel(i)).append('=').append(rs.getString(i));
                }
                System.out.println(row);
            }
        }
    }
}
