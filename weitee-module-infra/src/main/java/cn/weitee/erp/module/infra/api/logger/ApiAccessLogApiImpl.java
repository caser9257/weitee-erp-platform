package cn.weitee.erp.module.infra.api.logger;

import cn.weitee.erp.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import cn.weitee.erp.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import cn.weitee.erp.module.infra.service.logger.ApiAccessLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * API 访问日志的 API 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}
