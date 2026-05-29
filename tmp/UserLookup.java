import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserLookup {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"
                + "?useSSL=false&serverTimezone=Asia/Shanghai"
                + "&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
            String sql = "SELECT id, username, nickname, remark, dept_id, tenant_id "
                    + "FROM system_users "
                    + "WHERE deleted = b'0' AND (nickname LIKE ? OR remark LIKE ? OR username LIKE ?) "
                    + "ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String keyword = "%采购人员%";
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ps.setString(3, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("id=%d username=%s nickname=%s remark=%s deptId=%s tenantId=%s%n",
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("nickname"),
                                rs.getString("remark"),
                                rs.getString("dept_id"),
                                rs.getString("tenant_id"));
                    }
                }
            }
        }
    }
}
