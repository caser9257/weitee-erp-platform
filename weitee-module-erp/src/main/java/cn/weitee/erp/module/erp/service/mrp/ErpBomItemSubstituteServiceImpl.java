package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstituteRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Validated
public class ErpBomItemSubstituteServiceImpl implements ErpBomItemSubstituteService {

    @Resource
    private ErpBomItemSubstituteMapper erpBomItemSubstituteMapper;
    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpProductService productService;

    @Override
    public PageResult<ErpBomItemSubstituteRespVO> getSubstitutePage(ErpBomItemSubstitutePageReqVO pageReqVO) {
        boolean hasFilter = pageReqVO.getBomId() != null || pageReqVO.getMaterialId() != null
                || CollUtil.isNotEmpty(pageReqVO.getBomItemIds());
        List<Long> resolvedBomItemIds = resolveBomItemIds(pageReqVO);
        if (hasFilter && CollUtil.isEmpty(resolvedBomItemIds)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        pageReqVO.setBomItemIds(resolvedBomItemIds);
        PageResult<ErpBomItemSubstituteDO> pageResult = erpBomItemSubstituteMapper.selectPage(pageReqVO);
        return new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<Long> resolveBomItemIds(ErpBomItemSubstitutePageReqVO pageReqVO) {
        Set<Long> resolvedIds = new LinkedHashSet<>();
        if (CollUtil.isNotEmpty(pageReqVO.getBomItemIds())) {
            resolvedIds.addAll(pageReqVO.getBomItemIds());
        }
        if (pageReqVO.getBomId() != null || pageReqVO.getMaterialId() != null) {
            List<ErpBomItemDO> bomItems = erpBomItemMapper.selectList(new LambdaQueryWrapperX<ErpBomItemDO>()
                    .eqIfPresent(ErpBomItemDO::getBomId, pageReqVO.getBomId())
                    .eqIfPresent(ErpBomItemDO::getMaterialId, pageReqVO.getMaterialId()));
            Set<Long> matchedIds = convertSet(bomItems, ErpBomItemDO::getId);
            if (resolvedIds.isEmpty()) {
                resolvedIds.addAll(matchedIds);
            } else {
                resolvedIds.retainAll(matchedIds);
            }
        }
        return resolvedIds.isEmpty() ? null : new ArrayList<>(resolvedIds);
    }

    private List<ErpBomItemSubstituteRespVO> buildRespVOList(List<ErpBomItemSubstituteDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> bomItemIds = convertSet(list, ErpBomItemSubstituteDO::getBomItemId);
        Map<Long, ErpBomItemDO> bomItemMap = bomItemIds.isEmpty()
                ? Collections.emptyMap()
                : convertMap(erpBomItemMapper.selectByIds(bomItemIds), ErpBomItemDO::getId);
        Set<Long> bomIds = convertSet(bomItemMap.values(), ErpBomItemDO::getBomId);
        Map<Long, ErpBomDO> bomMap = bomIds.isEmpty()
                ? Collections.emptyMap()
                : convertMap(erpBomMapper.selectByIds(bomIds), ErpBomDO::getId);

        Set<Long> productIds = new LinkedHashSet<>();
        productIds.addAll(convertSet(bomMap.values(), ErpBomDO::getProductId));
        productIds.addAll(convertSet(bomItemMap.values(), ErpBomItemDO::getMaterialId));
        productIds.addAll(convertSet(list, ErpBomItemSubstituteDO::getSubstituteMaterialId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(productIds);

        return BeanUtils.toBean(list, ErpBomItemSubstituteRespVO.class, item -> {
            ErpBomItemDO bomItem = bomItemMap.get(item.getBomItemId());
            if (bomItem == null) {
                return;
            }
            item.setBomId(bomItem.getBomId());
            item.setMaterialId(bomItem.getMaterialId());
            ErpBomDO bom = bomMap.get(bomItem.getBomId());
            if (bom != null) {
                item.setBomCode(bom.getBomCode());
                ErpProductRespVO bomProduct = productMap.get(bom.getProductId());
                if (bomProduct != null) {
                    item.setProductId(bom.getProductId());
                    item.setProductName(bomProduct.getName());
                }
            }
            ErpProductRespVO material = productMap.get(bomItem.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
            }
            ErpProductRespVO substitute = productMap.get(item.getSubstituteMaterialId());
            if (substitute != null) {
                item.setSubstituteMaterialName(substitute.getName());
            }
        });
    }

}
