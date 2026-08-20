package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductSubstituteMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

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
    public Long createSubstitute(ErpProductSubstituteSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getProductId(), reqVO.getSubstituteProductId())) {
            throw new IllegalArgumentException("替代料不能为自身");
        }
        ErpProductDO product = productMapper.selectById(reqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        ErpProductDO sub = productMapper.selectById(reqVO.getSubstituteProductId());
        if (sub == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        ErpProductSubstituteDO d = BeanUtils.toBean(reqVO, ErpProductSubstituteDO.class);
        substituteMapper.insert(d);
        return d.getId();
    }

    @Override
    public void deleteSubstitute(Long productId, Long substituteProductId) {
        substituteMapper.deleteByProductIdAndSubstituteId(productId, substituteProductId);
    }

}
