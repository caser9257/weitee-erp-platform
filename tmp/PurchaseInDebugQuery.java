import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PurchaseInDebugQuery {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"
                + "?useSSL=false&serverTimezone=Asia/Shanghai"
                + "&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true"
                + "&rewriteBatchedStatements=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
            printPurchaseIn(conn);
            printPurchaseInItems(conn);
            printPurchaseOrder(conn);
        }
    }

    private static void printPurchaseIn(Connection conn) throws Exception {
        String sql = "SELECT id, no, status, qa_status, process_instance_id, order_id, "
                + "create_time, in_time, creator, deleted "
                + "FROM erp_purchase_in ORDER BY id DESC LIMIT 20";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== erp_purchase_in ===");
            while (rs.next()) {
                System.out.println(
                        rs.getLong("id")
                                + " | no=" + rs.getString("no")
                                + " | status=" + rs.getString("status")
                                + " | qa=" + rs.getString("qa_status")
                                + " | process=" + rs.getString("process_instance_id")
                                + " | orderId=" + rs.getString("order_id")
                                + " | createTime=" + rs.getString("create_time")
                                + " | inTime=" + rs.getString("in_time")
                                + " | creator=" + rs.getString("creator")
                                + " | deleted=" + rs.getString("deleted"));
            }
        }
    }

    private static void printPurchaseInItems(Connection conn) throws Exception {
        String sql = "SELECT id, in_id, order_item_id, product_id, warehouse_id, count, "
                + "qa_pass_count, qa_reject_count, qa_remark "
                + "FROM erp_purchase_in_items ORDER BY in_id DESC, id DESC LIMIT 50";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== erp_purchase_in_items ===");
            while (rs.next()) {
                System.out.println(
                        rs.getLong("id")
                                + " | inId=" + rs.getString("in_id")
                                + " | orderItemId=" + rs.getString("order_item_id")
                                + " | productId=" + rs.getString("product_id")
                                + " | warehouseId=" + rs.getString("warehouse_id")
                                + " | count=" + rs.getString("count")
                                + " | pass=" + rs.getString("qa_pass_count")
                                + " | reject=" + rs.getString("qa_reject_count")
                                + " | qaRemark=" + rs.getString("qa_remark"));
            }
        }
    }

    private static void printPurchaseOrder(Connection conn) throws Exception {
        String sql = "SELECT id, no, status, process_instance_id, in_count, return_count, "
                + "total_count, creator, create_time, deleted "
                + "FROM erp_purchase_order ORDER BY id DESC LIMIT 20";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== erp_purchase_order ===");
            while (rs.next()) {
                System.out.println(
                        rs.getLong("id")
                                + " | no=" + rs.getString("no")
                                + " | status=" + rs.getString("status")
                                + " | process=" + rs.getString("process_instance_id")
                                + " | inCount=" + rs.getString("in_count")
                                + " | returnCount=" + rs.getString("return_count")
                                + " | totalCount=" + rs.getString("total_count")
                                + " | creator=" + rs.getString("creator")
                                + " | createTime=" + rs.getString("create_time")
                                + " | deleted=" + rs.getString("deleted"));
            }
        }
    }
}
