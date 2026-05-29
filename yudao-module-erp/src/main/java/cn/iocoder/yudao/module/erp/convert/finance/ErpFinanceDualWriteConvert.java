package cn.iocoder.yudao.module.erp.convert.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ERP 双写日志 Convert
 */
@Component
public class ErpFinanceDualWriteConvert {

    public ErpFinanceDualWriteLogRespVO convert(ErpFinanceDualWriteLogDO logDO) {
        return BeanUtils.toBean(logDO, ErpFinanceDualWriteLogRespVO.class);
    }

    public PageResult<ErpFinanceDualWriteLogRespVO> convertPage(PageResult<ErpFinanceDualWriteLogDO> pageResult) {
        List<ErpFinanceDualWriteLogRespVO> list = BeanUtils.toBean(pageResult.getList(), ErpFinanceDualWriteLogRespVO.class);
        return new PageResult<>(list, pageResult.getTotal());
    }
}
