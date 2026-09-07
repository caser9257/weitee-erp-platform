package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_STATUS_INVALID;

@Service
@Validated
@Slf4j
public class ErpBomServiceImpl implements ErpBomService {

    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpBomItemSubstituteMapper erpBomItemSubstituteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBomStatus(Long id, Integer status) {
        validateBomExists(id);
        if (!ErpBomStatusEnum.isValid(status)) {
            throw exception(BOM_STATUS_INVALID);
        }
        // 仅生命周期审批回调可到达（Controller 已下线直改入口）；同状态重复回调幂等跳过
        ErpBomDO existed = validateBomExists(id);
        if (existed.getStatus().equals(status)) {
            log.info("[updateBomStatus] 状态未变化，幂等跳过，id={}, status={}", id, status);
            return;
        }
        erpBomMapper.updateById(new ErpBomDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearDisableApprovalProcess(Long id) {
        validateBomExists(id);
        erpBomMapper.update(null, new LambdaUpdateWrapper<ErpBomDO>()
                .eq(ErpBomDO::getId, id)
                .set(ErpBomDO::getProcessInstanceId, (String) null));
    }

    @Override
    public ErpBomDO getBom(Long id) {
        return erpBomMapper.selectById(id);
    }

    @Override
    public PageResult<ErpBomDO> getBomPage(ErpBomPageReqVO pageReqVO) {
        return erpBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpBomItemDO> getBomItemList(Long bomId) {
        return erpBomItemMapper.selectListByBomId(bomId);
    }

    @Override
    public List<ErpBomDO> getEffectiveBomList(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return List.of();
        }
        return erpBomMapper.selectListByProductIds(productIds);
    }

    @Override
    public List<ErpBomItemDO> getBomItemListByBomIds(Collection<Long> bomIds) {
        if (CollUtil.isEmpty(bomIds)) {
            return List.of();
        }
        return erpBomItemMapper.selectListByBomIds(bomIds);
    }

    @Override
    public List<ErpBomItemSubstituteDO> getBomItemSubstituteList(java.util.Collection<Long> bomItemIds) {
        return erpBomItemSubstituteMapper.selectListByBomItemIds(bomItemIds);
    }

    @Override
    public ErpBomDO getEffectiveBom(Long productId) {
        return erpBomMapper.selectEffectiveByProductId(productId);
    }

    private ErpBomDO validateBomExists(Long id) {
        ErpBomDO bom = erpBomMapper.selectById(id);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        return bom;
    }

}
