package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.service.mrp.support.ErpBomPricingPreviewResult;

public interface ErpBomPricingService {

    ErpBomPricingPreviewResult previewBomPricing(Long productId);

}
