import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BomPreviewProbe {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"
                + "?useSSL=false&serverTimezone=Asia/Shanghai"
                + "&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true"
                + "&rewriteBatchedStatements=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
            printProduct(conn, 960001L);
            printEffectiveBom(conn, 960001L);
            printBomItems(conn, 960200L);
            printSuccessCandidates(conn);
        }
    }

    private static void printProduct(Connection conn, long productId) throws Exception {
        String sql = "SELECT id, name, purchase_price, status, deleted, tenant_id "
                + "FROM erp_product WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("=== product ===");
                while (rs.next()) {
                    System.out.printf("id=%d, name=%s, purchasePrice=%s, status=%s, deleted=%s, tenantId=%s%n",
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("purchase_price"),
                            rs.getString("status"),
                            rs.getString("deleted"),
                            rs.getString("tenant_id"));
                }
            }
        }
    }

    private static void printEffectiveBom(Connection conn, long productId) throws Exception {
        String sql = "SELECT id, bom_code, product_id, version, status, deleted "
                + "FROM erp_bom WHERE product_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("=== bom ===");
                while (rs.next()) {
                    System.out.printf("id=%d, bomCode=%s, productId=%d, version=%s, status=%s, deleted=%s%n",
                            rs.getLong("id"),
                            rs.getString("bom_code"),
                            rs.getLong("product_id"),
                            rs.getString("version"),
                            rs.getString("status"),
                            rs.getString("deleted"));
                }
            }
        }
    }

    private static void printBomItems(Connection conn, long bomId) throws Exception {
        String sql = "SELECT i.id, i.bom_id, i.material_id, p.name AS material_name, p.purchase_price, "
                + "i.material_type, i.usage_qty, i.supply_owner, i.deleted "
                + "FROM erp_bom_item i "
                + "LEFT JOIN erp_product p ON p.id = i.material_id "
                + "WHERE i.bom_id = ? ORDER BY i.id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bomId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("=== bom items ===");
                while (rs.next()) {
                    System.out.printf("id=%d, bomId=%d, materialId=%d, materialName=%s, purchasePrice=%s, materialType=%s, usageQty=%s, supplyOwner=%s, deleted=%s%n",
                            rs.getLong("id"),
                            rs.getLong("bom_id"),
                            rs.getLong("material_id"),
                            rs.getString("material_name"),
                            rs.getBigDecimal("purchase_price"),
                            rs.getString("material_type"),
                            rs.getBigDecimal("usage_qty"),
                            rs.getString("supply_owner"),
                            rs.getString("deleted"));
                }
            }
        }
    }

    private static void printSuccessCandidates(Connection conn) throws Exception {
        String sql = "SELECT p.id, p.name, p.purchase_price, b.id AS bom_id, b.version, "
                + "SUM(CASE WHEN i.supply_owner = 'CUSTOMER' THEN 0 "
                + "         WHEN child.purchase_price IS NOT NULL THEN i.usage_qty * child.purchase_price "
                + "         ELSE NULL END) AS calc_price, "
                + "SUM(CASE WHEN i.supply_owner = 'CUSTOMER' THEN 0 "
                + "         WHEN child.purchase_price IS NULL THEN 1 ELSE 0 END) AS missing_count "
                + "FROM erp_product p "
                + "JOIN erp_bom b ON b.product_id = p.id AND b.deleted = 0 AND b.status = 1 "
                + "JOIN erp_bom_item i ON i.bom_id = b.id AND i.deleted = 0 "
                + "LEFT JOIN erp_bom child_bom ON child_bom.product_id = i.material_id AND child_bom.deleted = 0 AND child_bom.status = 1 "
                + "LEFT JOIN erp_product child ON child.id = i.material_id "
                + "WHERE p.deleted = 0 "
                + "GROUP BY p.id, p.name, p.purchase_price, b.id, b.version "
                + "HAVING missing_count = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== success candidates (flat scan) ===");
            while (rs.next()) {
                BigDecimal calcPrice = rs.getBigDecimal("calc_price");
                System.out.printf("productId=%d, name=%s, purchasePrice=%s, bomId=%d, version=%s, calcPrice=%s%n",
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("purchase_price"),
                        rs.getLong("bom_id"),
                        rs.getString("version"),
                        calcPrice);
            }
        }
    }
}
