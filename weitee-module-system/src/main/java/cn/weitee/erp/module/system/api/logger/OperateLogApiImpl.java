package cn.weitee.erp.module.system.api.logger;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import cn.weitee.erp.module.system.api.logger.dto.OperateLogPageReqDTO;
import cn.weitee.erp.module.system.api.logger.dto.OperateLogRespDTO;
import cn.weitee.erp.module.system.dal.dataobject.logger.OperateLogDO;
import cn.weitee.erp.module.system.service.logger.OperateLogService;
import com.fhs.core.trans.anno.TransMethodResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 操作日志 API 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogService.createOperateLog(createReqDTO);
    }

    @Override
    @TransMethodResult
    public PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        PageResult<OperateLogDO> operateLogPage = operateLogService.getOperateLogPage(pageReqDTO);
        return BeanUtils.toBean(operateLogPage, OperateLogRespDTO.class);
    }

}
