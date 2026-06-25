package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.ContractImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 合同导入服务接口
 *
 * @author system
 */
public interface ErpContractImportService {

    /**
     * 下载导入模板
     *
     * @return 模板文件字节数组
     */
    byte[] downloadTemplate();

    /**
     * 导入合同
     *
     * @param file 导入文件
     * @return 导入结果
     */
    ContractImportResultVO importContracts(MultipartFile file);

}
