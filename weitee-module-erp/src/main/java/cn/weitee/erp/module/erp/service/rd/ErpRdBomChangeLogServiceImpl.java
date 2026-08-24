package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomChangeLogRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomChangeLogDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomChangeLogMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ErpRdBomChangeLogServiceImpl implements ErpRdBomChangeLogService {

    @Resource
    private ErpRdBomChangeLogMapper changeLogMapper;

    @Override
    public void logChange(Long bomId, String changeType, String changeDetail) {
        ErpRdBomChangeLogDO log = new ErpRdBomChangeLogDO();
        log.setBomId(bomId);
        log.setChangeType(changeType);
        log.setChangeDetail(changeDetail);
        changeLogMapper.insert(log);
    }

    @Override
    public List<ErpRdBomChangeLogRespVO> getChangeLogList(Long bomId) {
        List<ErpRdBomChangeLogDO> list = changeLogMapper.selectListByBomId(bomId);
        return BeanUtils.toBean(list, ErpRdBomChangeLogRespVO.class);
    }

}
