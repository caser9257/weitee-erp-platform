package cn.iocoder.yudao.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import cn.iocoder.yudao.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.iocoder.yudao.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;

@Service
@Validated
public class ErpRdBomServiceImpl implements ErpRdBomService {

    private static final Pattern RD_BOM_VERSION_PATTERN = Pattern.compile("^[Vv](\\d+)(?:\\.\\d+)?$");

    @Resource
    private ErpRdBomMapper erpRdBomMapper;
    @Resource
    private ErpRdBomItemMapper erpRdBomItemMapper;
    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpBomItemSubstituteMapper erpBomItemSubstituteMapper;
    @Resource
    private ErpRdBomItemSubstituteMapper erpRdBomItemSubstituteMapper;
    @Resource
    private ErpProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRdBom(ErpRdBomSaveReqVO createReqVO) {
        validateBomItems(createReqVO.getItems());
        productService.validProductList(List.of(createReqVO.getProductId()));
        ErpRdBomDO rdBom = BeanUtils.toBean(createReqVO, ErpRdBomDO.class)
                .setVersion(null)
                .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus());
        erpRdBomMapper.insert(rdBom);
        saveRdBomItems(rdBom.getId(), createReqVO.getItems());
        return rdBom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRdBom(ErpRdBomSaveReqVO updateReqVO) {
        ErpRdBomDO existed = validateRdBomExists(updateReqVO.getId());
        validateBomItems(updateReqVO.getItems());
        productService.validProductList(List.of(updateReqVO.getProductId()));
        erpRdBomMapper.updateById(BeanUtils.toBean(updateReqVO, ErpRdBomDO.class)
                .setVersion(existed.getVersion())
                .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus())
                .setPublishedBomId(existed.getPublishedBomId())
                .setLastPublishedTime(existed.getLastPublishedTime()));
        List<ErpRdBomItemDO> existedItems = erpRdBomItemMapper.selectListByBomId(updateReqVO.getId());
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedItems, ErpRdBomItemDO::getId));
        erpRdBomItemMapper.deleteByBomId(updateReqVO.getId());
        saveRdBomItems(updateReqVO.getId(), updateReqVO.getItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRdBom(Long id) {
        validateRdBomExists(id);
        List<ErpRdBomItemDO> itemList = erpRdBomItemMapper.selectListByBomId(id);
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(itemList, ErpRdBomItemDO::getId));
        erpRdBomMapper.deleteById(id);
        erpRdBomItemMapper.deleteByBomId(id);
    }

    @Override
    public ErpRdBomDO getRdBom(Long id) {
        return erpRdBomMapper.selectById(id);
    }

    @Override
    public PageResult<ErpRdBomDO> getRdBomPage(ErpRdBomPageReqVO pageReqVO) {
        return erpRdBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpRdBomItemDO> getRdBomItemList(Long bomId) {
        return erpRdBomItemMapper.selectListByBomId(bomId);
    }

    @Override
    public List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(java.util.Collection<Long> bomItemIds) {
        return erpRdBomItemSubstituteMapper.selectListByBomItemIds(bomItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishRdBom(Long id) {
        ErpRdBomDO rdBom = validateRdBomExists(id);
        List<ErpRdBomItemDO> rdBomItems = erpRdBomItemMapper.selectListByBomId(id);
        if (CollUtil.isEmpty(rdBomItems)) {
            throw exception(BOM_ITEM_EMPTY);
        }
        String nextVersion = generateNextVersion(rdBom.getProductId());
        rdBom.setVersion(nextVersion);

        ErpBomDO manufacturingBom = rdBom.getPublishedBomId() == null ? null : erpBomMapper.selectById(rdBom.getPublishedBomId());
        boolean createNewManufacturingBom = manufacturingBom == null
                || ErpBomStatusEnum.ENABLE.getStatus().equals(manufacturingBom.getStatus());

        Long manufacturingBomId;
        if (createNewManufacturingBom) {
            ErpBomDO newManufacturingBom = new ErpBomDO()
                    .setBomCode(rdBom.getBomCode())
                    .setProductId(rdBom.getProductId())
                    .setVersion(rdBom.getVersion())
                    .setStatus(ErpBomStatusEnum.DISABLE.getStatus())
                    .setSourceRdBomId(rdBom.getId())
                    .setRemark(rdBom.getRemark());
            erpBomMapper.insert(newManufacturingBom);
            manufacturingBomId = newManufacturingBom.getId();
        } else {
            manufacturingBomId = manufacturingBom.getId();
            List<ErpBomItemDO> existedManufacturingBomItems = erpBomItemMapper.selectListByBomId(manufacturingBomId);
            erpBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedManufacturingBomItems, ErpBomItemDO::getId));
            erpBomMapper.updateById(new ErpBomDO()
                    .setId(manufacturingBomId)
                    .setBomCode(rdBom.getBomCode())
                    .setProductId(rdBom.getProductId())
                    .setVersion(rdBom.getVersion())
                    .setStatus(ErpBomStatusEnum.DISABLE.getStatus())
                    .setSourceRdBomId(rdBom.getId())
                    .setRemark(rdBom.getRemark()));
            erpBomItemMapper.deleteByBomId(manufacturingBomId);
        }

        List<ErpBomItemDO> manufacturingBomItems = rdBomItems.stream()
                .map(item -> new ErpBomItemDO()
                        .setBomId(manufacturingBomId)
                        .setMaterialId(item.getMaterialId())
                        .setMaterialType(item.getMaterialType())
                        .setUnitId(item.getUnitId())
                        .setUsageQty(item.getUsageQty())
                        .setLossRate(item.getLossRate())
                        .setLeadTimeDay(item.getLeadTimeDay())
                        .setSort(item.getSort())
                        .setRemark(item.getRemark()))
                .toList();
        erpBomItemMapper.insertBatch(manufacturingBomItems);
        copyRdBomItemSubstitutes(rdBomItems, manufacturingBomItems);

        erpRdBomMapper.updateById(new ErpRdBomDO()
                .setId(id)
                .setVersion(nextVersion)
                .setStatus(ErpRdBomStatusEnum.PUBLISHED.getStatus())
                .setPublishedBomId(manufacturingBomId)
                .setLastPublishedTime(LocalDateTime.now()));
    }

    private String generateNextVersion(Long productId) {
        List<ErpRdBomDO> existedBomList = erpRdBomMapper.selectList(ErpRdBomDO::getProductId, productId);
        int nextMajorVersion = existedBomList.stream()
                .map(ErpRdBomDO::getVersion)
                .map(this::extractMajorVersion)
                .filter(version -> version > 0)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "V" + nextMajorVersion + ".0";
    }

    private int extractMajorVersion(String version) {
        if (version == null || version.isBlank()) {
            return 0;
        }
        Matcher matcher = RD_BOM_VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private void saveRdBomItems(Long bomId, List<ErpRdBomSaveReqVO.Item> items) {
        List<ErpProductDO> products = productService.validProductList(convertSet(items, ErpRdBomSaveReqVO.Item::getMaterialId));
        Map<Long, ErpProductDO> productMap = convertMap(products, ErpProductDO::getId);
        List<ErpRdBomItemDO> itemDOs = BeanUtils.toBean(items, ErpRdBomItemDO.class,
                item -> item.setBomId(bomId).setUnitId(productMap.get(item.getMaterialId()).getUnitId()));
        erpRdBomItemMapper.insertBatch(itemDOs);
        saveRdBomItemSubstitutes(itemDOs, items);
    }

    private void saveRdBomItemSubstitutes(List<ErpRdBomItemDO> itemDOs, List<ErpRdBomSaveReqVO.Item> items) {
        List<ErpRdBomItemSubstituteDO> substituteDOs = new java.util.ArrayList<>();
        for (int i = 0; i < itemDOs.size(); i++) {
            ErpRdBomItemDO itemDO = itemDOs.get(i);
            List<ErpRdBomSaveReqVO.Item.Substitute> substitutes = items.get(i).getSubstitutes();
            if (CollUtil.isEmpty(substitutes)) {
                continue;
            }
            productService.validProductList(convertSet(substitutes, ErpRdBomSaveReqVO.Item.Substitute::getSubstituteMaterialId));
            substituteDOs.addAll(BeanUtils.toBean(substitutes, ErpRdBomItemSubstituteDO.class,
                    substitute -> {
                        substitute.setId(null);
                        substitute.setBomItemId(itemDO.getId());
                    }));
        }
        if (CollUtil.isNotEmpty(substituteDOs)) {
            erpRdBomItemSubstituteMapper.insertBatch(substituteDOs);
        }
    }

    private void copyRdBomItemSubstitutes(List<ErpRdBomItemDO> rdBomItems, List<ErpBomItemDO> manufacturingBomItems) {
        List<ErpRdBomItemSubstituteDO> rdBomItemSubstitutes =
                erpRdBomItemSubstituteMapper.selectListByBomItemIds(convertSet(rdBomItems, ErpRdBomItemDO::getId));
        if (CollUtil.isEmpty(rdBomItemSubstitutes)) {
            return;
        }
        Map<Long, Long> rdBomItemIdMap = new java.util.HashMap<>();
        for (int i = 0; i < rdBomItems.size(); i++) {
            rdBomItemIdMap.put(rdBomItems.get(i).getId(), manufacturingBomItems.get(i).getId());
        }
        List<ErpBomItemSubstituteDO> manufacturingItemSubstitutes = BeanUtils.toBean(rdBomItemSubstitutes,
                ErpBomItemSubstituteDO.class, substitute -> {
                    substitute.setId(null);
                    substitute.setBomItemId(rdBomItemIdMap.get(substitute.getBomItemId()));
                });
        erpBomItemSubstituteMapper.insertBatch(manufacturingItemSubstitutes);
    }

    private void validateBomItems(List<?> items) {
        if (CollUtil.isEmpty(items)) {
            throw exception(BOM_ITEM_EMPTY);
        }
    }

    private ErpRdBomDO validateRdBomExists(Long id) {
        ErpRdBomDO rdBom = erpRdBomMapper.selectById(id);
        if (rdBom == null) {
            throw exception(RD_BOM_NOT_EXISTS);
        }
        return rdBom;
    }

}
