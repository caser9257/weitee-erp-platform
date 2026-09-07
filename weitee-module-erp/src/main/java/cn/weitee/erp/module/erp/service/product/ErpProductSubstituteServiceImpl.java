package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductSubstituteMapper;
import cn.weitee.erp.module.erp.enums.product.ErpProductSubstituteTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpProductSubstituteServiceImpl implements ErpProductSubstituteService {

    @Resource
    private ErpProductSubstituteMapper substituteMapper;
    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpProductService productService;

    @Override
    public List<ErpProductSubstituteRespVO> getListByProductId(Long productId) {
        List<ErpProductSubstituteDO> list = substituteMapper.selectListByProductId(productId);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> subIds = list.stream().map(ErpProductSubstituteDO::getSubstituteProductId).collect(Collectors.toSet());
        List<ErpProductDO> products = productMapper.selectBatchIds(new ArrayList<>(subIds));
        Map<Long, ErpProductDO> productMap = products.stream().collect(Collectors.toMap(ErpProductDO::getId, p -> p));
        return BeanUtils.toBean(list, ErpProductSubstituteRespVO.class, vo -> {
            ErpProductDO p = productMap.get(vo.getSubstituteProductId());
            if (p != null) {
                vo.setSubstituteProductName(p.getName());
                vo.setSubstituteMaterialCode(p.getMaterialCode());
                vo.setSubstituteProductStandard(p.getStandard());
            }
        });
    }

    @Override
    public Map<Long, List<ErpProductSubstituteRespVO>> getListMapByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ErpProductSubstituteDO> list = substituteMapper.selectListByProductIds(productIds);
        if (list.isEmpty()) {
            return Map.of();
        }
        Set<Long> subIds = list.stream().map(ErpProductSubstituteDO::getSubstituteProductId).collect(Collectors.toSet());
        List<ErpProductDO> products = productMapper.selectBatchIds(new ArrayList<>(subIds));
        Map<Long, ErpProductDO> productMap = products.stream().collect(Collectors.toMap(ErpProductDO::getId, p -> p));
        Map<Long, List<ErpProductSubstituteRespVO>> result = new HashMap<>();
        for (ErpProductSubstituteDO d : list) {
            ErpProductSubstituteRespVO vo = BeanUtils.toBean(d, ErpProductSubstituteRespVO.class);
            ErpProductDO p = productMap.get(d.getSubstituteProductId());
            if (p != null) {
                vo.setSubstituteProductName(p.getName());
                vo.setSubstituteMaterialCode(p.getMaterialCode());
                vo.setSubstituteProductStandard(p.getStandard());
            }
            result.computeIfAbsent(d.getProductId(), k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    @Override
    public Map<Long, Boolean> getHasSubstituteMap(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ErpProductSubstituteDO> list = substituteMapper.selectListByProductIds(productIds);
        Set<Long> hasIds = list.stream().map(ErpProductSubstituteDO::getProductId).collect(Collectors.toSet());
        Map<Long, Boolean> map = new HashMap<>();
        for (Long pid : productIds) {
            map.put(pid, hasIds.contains(pid));
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubstitute(ErpProductSubstituteSaveReqVO reqVO) {
        validateSubstituteRow(reqVO);
        // 校验同一对主-替唯一
        List<ErpProductSubstituteDO> exists = substituteMapper.selectListByProductId(reqVO.getProductId());
        boolean duplicated = exists.stream().anyMatch(d ->
                d.getSubstituteProductId().equals(reqVO.getSubstituteProductId()));
        if (duplicated) {
            throw exception(PRODUCT_SUBSTITUTE_EXISTS);
        }
        ErpProductSubstituteDO d = BeanUtils.toBean(reqVO, ErpProductSubstituteDO.class);
        fillDefaults(d);
        substituteMapper.insert(d);
        return d.getId();
    }

    @Override
    public void deleteSubstitute(Long productId, Long substituteProductId) {
        substituteMapper.deleteByProductIdAndSubstituteId(productId, substituteProductId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSubstitutes(Long productId, List<ErpProductSubstituteSaveReqVO> list) {
        // 1. 校验主物料存在
        ErpProductDO product = productMapper.selectById(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        List<ErpProductSubstituteSaveReqVO> rows = list == null ? List.of() : list;
        // 2. 校验每行
        Set<Long> seenSubIds = new HashSet<>();
        for (ErpProductSubstituteSaveReqVO row : rows) {
            validateSubstituteRow(row);
            if (!seenSubIds.add(row.getSubstituteProductId())) {
                throw exception(PRODUCT_SUBSTITUTE_REPEATED);
            }
        }
        // 3. diff 更新：已存在行按 id 更新，新行新增，未传入行删除
        List<ErpProductSubstituteDO> existingList = substituteMapper.selectListByProductId(productId);
        Map<Long, ErpProductSubstituteDO> existingMap = existingList.stream()
                .collect(Collectors.toMap(ErpProductSubstituteDO::getId, d -> d));
        Set<Long> incomingIds = rows.stream()
                .map(ErpProductSubstituteSaveReqVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 3.1 删除未在传入列表中的已有行
        for (ErpProductSubstituteDO d : existingList) {
            if (!incomingIds.contains(d.getId())) {
                substituteMapper.deleteById(d.getId());
            }
        }
        // 3.2 更新或新增
        for (ErpProductSubstituteSaveReqVO row : rows) {
            if (row.getId() != null) {
                ErpProductSubstituteDO d = existingMap.get(row.getId());
                if (d != null) {
                    ErpProductSubstituteDO updated = BeanUtils.toBean(row, ErpProductSubstituteDO.class);
                    fillDefaults(updated);
                    substituteMapper.updateById(updated);
                } else {
                    // id 不存在的行视为新增
                    ErpProductSubstituteDO d2 = BeanUtils.toBean(row, ErpProductSubstituteDO.class);
                    fillDefaults(d2);
                    substituteMapper.insert(d2);
                }
            } else {
                ErpProductSubstituteDO d = BeanUtils.toBean(row, ErpProductSubstituteDO.class);
                fillDefaults(d);
                substituteMapper.insert(d);
            }
        }
    }

    private void validateSubstituteRow(ErpProductSubstituteSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getProductId(), reqVO.getSubstituteProductId())) {
            throw exception(PRODUCT_SUBSTITUTE_SELF);
        }
        ErpProductDO product = productMapper.selectById(reqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        ErpProductDO sub = productMapper.selectById(reqVO.getSubstituteProductId());
        if (sub == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void fillDefaults(ErpProductSubstituteDO d) {
        if (d.getPriority() == null) {
            d.setPriority(1);
        }
        if (d.getReplaceRatio() == null) {
            d.setReplaceRatio(BigDecimal.ONE);
        }
        if (d.getSubstituteType() == null) {
            d.setSubstituteType(ErpProductSubstituteTypeEnum.GLOBAL.getType());
        }
        if (d.getStatus() == null) {
            d.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
    }

}
