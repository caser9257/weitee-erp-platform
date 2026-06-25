package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehousePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpWarehouseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_BATCH_UPDATE_FIELD_NOT_SUPPORT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_NOT_EXISTS;

/**
 * ERP 仓库 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ErpWarehouseServiceImpl implements ErpWarehouseService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final String BATCH_FIELD_ADDRESS = "address";
    private static final String BATCH_FIELD_CATEGORY_ID = "categoryId";
    private static final String BATCH_FIELD_PRINCIPAL = "principal";
    private static final String BATCH_FIELD_REMARK = "remark";
    private static final String BATCH_FIELD_STATUS = "status";
    private static final String BATCH_FIELD_SORT = "sort";

    @Resource
    private ErpWarehouseMapper erpWarehouseMapper;

    @Resource
    private ErpWarehouseCategoryService warehouseCategoryService;

    @Override
    public Long createWarehouse(ErpWarehouseSaveReqVO createReqVO) {
        validateWarehouseCategoryExists(createReqVO.getCategoryId());
        // 插入
        ErpWarehouseDO warehouse = BeanUtils.toBean(createReqVO, ErpWarehouseDO.class);
        erpWarehouseMapper.insert(warehouse);
        // 返回
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(ErpWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateWarehouseExists(updateReqVO.getId());
        validateWarehouseCategoryExists(updateReqVO.getCategoryId());
        // 更新
        ErpWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, ErpWarehouseDO.class);
        erpWarehouseMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpWarehouseBatchUpdateResultVO updateWarehouseBatch(ErpWarehouseBatchUpdateReqVO reqVO) {
        if (!BATCH_EDIT_MODE_OVERWRITE.equals(reqVO.getMode())) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        String fieldKey = normalizeBatchFieldKey(reqVO.getFieldKey());
        Object value = parseBatchValue(fieldKey, reqVO.getValue());
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(reqVO.getIds()));
        List<ErpWarehouseDO> warehouses = erpWarehouseMapper.selectByIds(uniqueIds);
        Map<Long, ErpWarehouseDO> warehouseMap = convertMap(warehouses, ErpWarehouseDO::getId);
        for (Long id : uniqueIds) {
            if (!warehouseMap.containsKey(id)) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }

        uniqueIds.forEach(id -> {
            ErpWarehouseDO updateObj = new ErpWarehouseDO().setId(id);
            applyBatchValue(updateObj, fieldKey, value);
            erpWarehouseMapper.updateById(updateObj);
        });

        ErpWarehouseBatchUpdateResultVO result = new ErpWarehouseBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus) {
        // 1. 校验存在
        validateWarehouseExists(id);

        // 2.1 如果开启，则需要关闭所有其它的默认
        if (defaultStatus) {
            ErpWarehouseDO warehouse = erpWarehouseMapper.selectByDefaultStatus();
            if (warehouse != null) {
                erpWarehouseMapper.updateById(new ErpWarehouseDO().setId(warehouse.getId()).setDefaultStatus(false));
            }
        }
        // 2.2 更新对应的默认状态
        erpWarehouseMapper.updateById(new ErpWarehouseDO().setId(id).setDefaultStatus(defaultStatus));
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
        // 删除
        erpWarehouseMapper.deleteById(id);
    }

    private void validateWarehouseExists(Long id) {
        if (erpWarehouseMapper.selectById(id) == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
    }

    @Override
    public ErpWarehouseDO getWarehouse(Long id) {
        return erpWarehouseMapper.selectById(id);
    }

    @Override
    public List<ErpWarehouseDO> validWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpWarehouseDO> list = erpWarehouseMapper.selectByIds(ids);
        Map<Long, ErpWarehouseDO> warehouseMap = convertMap(list, ErpWarehouseDO::getId);
        for (Long id : ids) {
            ErpWarehouseDO warehouse = warehouseMap.get(id);
            if (warehouseMap.get(id) == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(warehouse.getStatus())) {
                throw exception(WAREHOUSE_NOT_ENABLE, warehouse.getName());
            }
        }
        return list;
    }

    @Override
    public List<ErpWarehouseDO> getWarehouseListByStatus(Integer status) {
        return erpWarehouseMapper.selectListByStatus(status);
    }

    @Override
    public List<ErpWarehouseDO> getWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpWarehouseMapper.selectByIds(ids);
    }

    @Override
    public List<ErpWarehouseDO> getWarehouseListByCategoryId(Long categoryId) {
        return erpWarehouseMapper.selectListByCategoryId(categoryId);
    }

    @Override
    public PageResult<ErpWarehouseDO> getWarehousePage(ErpWarehousePageReqVO pageReqVO) {
        return erpWarehouseMapper.selectPage(pageReqVO);
    }

    private String normalizeBatchFieldKey(String fieldKey) {
        if (!BATCH_FIELD_ADDRESS.equals(fieldKey)
                && !BATCH_FIELD_CATEGORY_ID.equals(fieldKey)
                && !BATCH_FIELD_PRINCIPAL.equals(fieldKey)
                && !BATCH_FIELD_REMARK.equals(fieldKey)
                && !BATCH_FIELD_STATUS.equals(fieldKey)
                && !BATCH_FIELD_SORT.equals(fieldKey)) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_NOT_SUPPORT, fieldKey);
        }
        return fieldKey;
    }

    private Object parseBatchValue(String fieldKey, String value) {
        String trimmedValue = value == null ? null : value.trim();
        if (BATCH_FIELD_CATEGORY_ID.equals(fieldKey)) {
            return parseCategoryIdValue(fieldKey, trimmedValue);
        }
        if (BATCH_FIELD_STATUS.equals(fieldKey)) {
            return parseStatusValue(fieldKey, trimmedValue);
        }
        if (BATCH_FIELD_SORT.equals(fieldKey)) {
            return parseSortValue(fieldKey, trimmedValue);
        }
        if (trimmedValue == null || trimmedValue.isEmpty()) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        return trimmedValue;
    }

    private Integer parseStatusValue(String fieldKey, String value) {
        try {
            Integer status = Integer.valueOf(value);
            if (!CommonStatusEnum.isEnable(status) && !CommonStatusEnum.isDisable(status)) {
                throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return status;
        } catch (NumberFormatException ex) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private Long parseSortValue(String fieldKey, String value) {
        try {
            Long sort = Long.valueOf(value);
            if (sort < 0) {
                throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return sort;
        } catch (NumberFormatException ex) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private Long parseCategoryIdValue(String fieldKey, String value) {
        try {
            Long categoryId = Long.valueOf(value);
            validateWarehouseCategoryExists(categoryId);
            return categoryId;
        } catch (NumberFormatException ex) {
            throw exception(WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private void applyBatchValue(ErpWarehouseDO updateObj, String fieldKey, Object value) {
        if (BATCH_FIELD_ADDRESS.equals(fieldKey)) {
            updateObj.setAddress((String) value);
            return;
        }
        if (BATCH_FIELD_CATEGORY_ID.equals(fieldKey)) {
            updateObj.setCategoryId((Long) value);
            return;
        }
        if (BATCH_FIELD_PRINCIPAL.equals(fieldKey)) {
            updateObj.setPrincipal((String) value);
            return;
        }
        if (BATCH_FIELD_REMARK.equals(fieldKey)) {
            updateObj.setRemark((String) value);
            return;
        }
        if (BATCH_FIELD_STATUS.equals(fieldKey)) {
            updateObj.setStatus((Integer) value);
            return;
        }
        if (BATCH_FIELD_SORT.equals(fieldKey)) {
            updateObj.setSort((Long) value);
        }
    }

    private void validateWarehouseCategoryExists(Long categoryId) {
        if (categoryId == null || warehouseCategoryService.getWarehouseCategory(categoryId) == null) {
            throw exception(WAREHOUSE_CATEGORY_NOT_EXISTS);
        }
    }

}
