package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * ERP 产品导入 Service 接口
 */
public interface ErpProductImportService {

    /**
     * 下载产品导入模板
     *
     * @return 模板文件字节数组
     */
    byte[] downloadTemplate();

    /**
     * 导入产品
     *
     * @param file           导入文件
     * @param updateSupport  是否支持覆盖更新（true：已存在条码自动覆盖；false：已存在则失败）
     * @return 导入结果
     */
    ErpProductImportResultVO importProducts(MultipartFile file, Boolean updateSupport);

}
