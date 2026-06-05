package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.bom.ErpBomPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.bom.ErpBomSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.BOM_STATUS_INVALID;

@Service
@Validated
public class ErpBomServiceImpl implements ErpBomService {

    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpBomItemSubstituteMapper erpBomItemSubstituteMapper;
    @Resource
    private ErpProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBom(ErpBomSaveReqVO createReqVO) {
        validateBomItems(createReqVO.getItems());
        productService.validProductList(List.of(createReqVO.getProductId()));
        ErpBomDO bom = BeanUtils.toBean(createReqVO, ErpBomDO.class)
                .setStatus(ErpBomStatusEnum.DISABLE.getStatus());
        erpBomMapper.insert(bom);
        saveBomItems(bom.getId(), createReqVO.getItems());
        return bom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBom(ErpBomSaveReqVO updateReqVO) {
        ErpBomDO existedBom = validateBomExists(updateReqVO.getId());
        validateBomItems(updateReqVO.getItems());
        productService.validProductList(List.of(updateReqVO.getProductId()));
        erpBomMapper.updateById(BeanUtils.toBean(updateReqVO, ErpBomDO.class)
                .setStatus(existedBom.getStatus())
                .setSourceRdBomId(existedBom.getSourceRdBomId()));
        List<ErpBomItemDO> existedItems = erpBomItemMapper.selectListByBomId(updateReqVO.getId());
        erpBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedItems, ErpBomItemDO::getId));
        erpBomItemMapper.deleteByBomId(updateReqVO.getId());
        saveBomItems(updateReqVO.getId(), updateReqVO.getItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBomStatus(Long id, Integer status) {
        validateBomExists(id);
        if (!ErpBomStatusEnum.isValid(status)) {
            throw exception(BOM_STATUS_INVALID);
        }
        erpBomMapper.updateById(new ErpBomDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long id) {
        validateBomExists(id);
        List<ErpBomItemDO> itemList = erpBomItemMapper.selectListByBomId(id);
        erpBomItemSubstituteMapper.deleteByBomItemIds(convertSet(itemList, ErpBomItemDO::getId));
        erpBomMapper.deleteById(id);
        erpBomItemMapper.deleteByBomId(id);
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

    private void saveBomItems(Long bomId, List<ErpBomSaveReqVO.Item> items) {
        List<ErpProductDO> products = productService.validProductList(convertSet(items, ErpBomSaveReqVO.Item::getMaterialId));
        Map<Long, ErpProductDO> productMap = convertMap(products, ErpProductDO::getId);
        List<ErpBomItemDO> itemDOs = BeanUtils.toBean(items, ErpBomItemDO.class,
                item -> item.setBomId(bomId).setUnitId(productMap.get(item.getMaterialId()).getUnitId()));
        erpBomItemMapper.insertBatch(itemDOs);
        saveBomItemSubstitutes(itemDOs, items);
    }

    private void saveBomItemSubstitutes(List<ErpBomItemDO> itemDOs, List<ErpBomSaveReqVO.Item> items) {
        List<ErpBomItemSubstituteDO> substituteDOs = new ArrayList<>();
        for (int i = 0; i < itemDOs.size(); i++) {
            ErpBomItemDO itemDO = itemDOs.get(i);
            List<ErpBomSaveReqVO.Item.Substitute> substitutes = items.get(i).getSubstitutes();
            if (substitutes == null || substitutes.isEmpty()) {
                continue;
            }
            productService.validProductList(convertSet(substitutes, ErpBomSaveReqVO.Item.Substitute::getSubstituteMaterialId));
            substituteDOs.addAll(BeanUtils.toBean(substitutes, ErpBomItemSubstituteDO.class, substitute -> {
                substitute.setId(null);
                substitute.setBomItemId(itemDO.getId());
            }));
        }
        if (!substituteDOs.isEmpty()) {
            erpBomItemSubstituteMapper.insertBatch(substituteDOs);
        }
    }

    private void validateBomItems(List<ErpBomSaveReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            throw exception(BOM_ITEM_EMPTY);
        }
    }

    private ErpBomDO validateBomExists(Long id) {
        ErpBomDO bom = erpBomMapper.selectById(id);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        return bom;
    }

}
