package cn.weitee.erp.module.erp.service.product;

public interface ErpProductBpmService {

    String submitProduct(Long userId, Long productId);

    void cancelProductApproval(Long userId, Long productId, String reason);

}
