package cn.weitee.erp.module.system.service.dict;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.enums.BatchEditModeEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataBatchUpdateReqVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataBatchUpdateResultVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictDataDO;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictTypeDO;
import cn.weitee.erp.module.system.dal.mysql.dict.DictDataMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.system.enums.ErrorCodeConstants.*;

/**
 * 字典数据 Service 实现类
 *
 * @author ruoyi
 */
@Service
@Slf4j
public class DictDataServiceImpl implements DictDataService {

    /**
     * 排序 dictType > sort
     */
    private static final Comparator<DictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(DictDataDO::getDictType)
            .thenComparingInt(DictDataDO::getSort);

    private static final int DICT_DATA_LABEL_MAX_LENGTH = 100;

    private static final Set<String> DICT_DATA_COLOR_TYPES = new HashSet<>(Arrays.asList(
            "default", "primary", "success", "info", "warning", "danger"));

    @Resource
    private DictTypeService dictTypeService;

    @Resource
    private DictDataMapper dictDataMapper;

    @Override
    public List<DictDataDO> getDictDataList(Integer status, String dictType) {
        List<DictDataDO> list = dictDataMapper.selectListByStatusAndDictType(status, dictType);
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

    @Override
    public PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO pageReqVO) {
        return dictDataMapper.selectPage(pageReqVO);
    }

    @Override
    public DictDataDO getDictData(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public Long createDictData(DictDataSaveReqVO createReqVO) {
        // 校验字典类型有效
        validateDictTypeExists(createReqVO.getDictType());
        // 校验字典数据的值的唯一性
        validateDictDataValueUnique(null, createReqVO.getDictType(), createReqVO.getValue());

        // 插入字典类型
        DictDataDO dictData = BeanUtils.toBean(createReqVO, DictDataDO.class);
        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    @Override
    public void updateDictData(DictDataSaveReqVO updateReqVO) {
        // 校验自己存在
        validateDictDataExists(updateReqVO.getId());
        // 校验字典类型有效
        validateDictTypeExists(updateReqVO.getDictType());
        // 校验字典数据的值的唯一性
        validateDictDataValueUnique(updateReqVO.getId(), updateReqVO.getDictType(), updateReqVO.getValue());

        // 更新字典类型
        DictDataDO updateObj = BeanUtils.toBean(updateReqVO, DictDataDO.class);
        dictDataMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataBatchUpdateResultVO updateDictDataBatch(DictDataBatchUpdateReqVO reqVO) {
        List<Long> uniqueIds = new java.util.ArrayList<>(new LinkedHashSet<>(reqVO.getIds()));
        List<DictDataDO> dictDataList = dictDataMapper.selectByIds(uniqueIds);
        Map<Long, DictDataDO> dictDataMap = CollectionUtils.convertMap(dictDataList, DictDataDO::getId);
        for (Long id : uniqueIds) {
            if (!dictDataMap.containsKey(id)) {
                throw exception(DICT_DATA_NOT_EXISTS);
            }
        }

        DictDataBatchFieldUpdater updater = createBatchUpdater(reqVO);
        uniqueIds.forEach(id -> {
            DictDataDO updateObj = dictDataMap.get(id);
            updater.apply(updateObj);
            dictDataMapper.updateById(updateObj);
        });

        DictDataBatchUpdateResultVO result = new DictDataBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(java.util.Collections.emptyList());
        return result;
    }

    @Override
    public void deleteDictData(Long id) {
        // 校验是否存在
        validateDictDataExists(id);

        // 删除字典数据
        dictDataMapper.deleteById(id);
    }

    @Override
    public void deleteDictDataList(List<Long> ids) {
        dictDataMapper.deleteByIds(ids);
    }

    @Override
    public long getDictDataCountByDictType(String dictType) {
        return dictDataMapper.selectCountByDictType(dictType);
    }

    @VisibleForTesting
    public void validateDictDataValueUnique(Long id, String dictType, String value) {
        DictDataDO dictData = dictDataMapper.selectByDictTypeAndValue(dictType, value);
        if (dictData == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典数据
        if (id == null) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
        if (!dictData.getId().equals(id)) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

    @VisibleForTesting
    public void validateDictDataExists(Long id) {
        if (id == null) {
            return;
        }
        DictDataDO dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw exception(DICT_DATA_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    public void validateDictTypeExists(String type) {
        DictTypeDO dictType = dictTypeService.getDictType(type);
        if (dictType == null) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dictType.getStatus())) {
            throw exception(DICT_TYPE_NOT_ENABLE);
        }
    }

    @Override
    public void validateDictDataList(String dictType, Collection<String> values) {
        if (CollUtil.isEmpty(values)) {
            return;
        }
        Map<String, DictDataDO> dictDataMap = CollectionUtils.convertMap(
                dictDataMapper.selectByDictTypeAndValues(dictType, values), DictDataDO::getValue);
        // 校验
        values.forEach(value -> {
            DictDataDO dictData = dictDataMap.get(value);
            if (dictData == null) {
                throw exception(DICT_DATA_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dictData.getStatus())) {
                throw exception(DICT_DATA_NOT_ENABLE, dictData.getLabel());
            }
        });
    }

    @Override
    public DictDataDO getDictData(String dictType, String value) {
        return dictDataMapper.selectByDictTypeAndValue(dictType, value);
    }

    @Override
    public DictDataDO parseDictData(String dictType, String label) {
        return dictDataMapper.selectByDictTypeAndLabel(dictType, label);
    }

    @VisibleForTesting
    DictDataBatchFieldUpdater createBatchUpdater(DictDataBatchUpdateReqVO reqVO) {
        if (!BatchEditModeEnum.OVERWRITE.getValue().equals(reqVO.getMode())) {
            throw exception(DICT_DATA_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        switch (reqVO.getFieldKey()) {
            case "label":
                return dictData -> {
                    if (reqVO.getValue().length() > DICT_DATA_LABEL_MAX_LENGTH) {
                        throw exception(DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, reqVO.getFieldKey(), reqVO.getValue());
                    }
                    dictData.setLabel(reqVO.getValue());
                };
            case "sort":
                return dictData -> dictData.setSort(parsePositiveIntegerValue(reqVO.getFieldKey(), reqVO.getValue()));
            case "status":
                return dictData -> dictData.setStatus(parseIntegerValue(reqVO.getFieldKey(), reqVO.getValue(), CommonStatusEnum.ARRAYS));
            case "colorType":
                return dictData -> {
                    if (!DICT_DATA_COLOR_TYPES.contains(reqVO.getValue())) {
                        throw exception(DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, reqVO.getFieldKey(), reqVO.getValue());
                    }
                    dictData.setColorType(reqVO.getValue());
                };
            case "remark":
                return dictData -> dictData.setRemark(reqVO.getValue());
            default:
                throw exception(DICT_DATA_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
    }

    @VisibleForTesting
    Integer parseIntegerValue(String fieldKey, String value, Integer... allowedValues) {
        Integer parsedValue;
        try {
            parsedValue = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw exception(DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        if (allowedValues != null && allowedValues.length > 0) {
            for (Integer allowedValue : allowedValues) {
                if (allowedValue != null && allowedValue.equals(parsedValue)) {
                    return parsedValue;
                }
            }
            throw exception(DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        return parsedValue;
    }

    @VisibleForTesting
    Integer parsePositiveIntegerValue(String fieldKey, String value) {
        Integer parsedValue = parseIntegerValue(fieldKey, value);
        if (parsedValue < 0) {
            throw exception(DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        return parsedValue;
    }

    @FunctionalInterface
    @VisibleForTesting
    interface DictDataBatchFieldUpdater {
        void apply(DictDataDO dictData);
    }

    @Override
    public List<DictDataDO> getDictDataListByDictType(String dictType) {
        List<DictDataDO> list = dictDataMapper.selectList(DictDataDO::getDictType, dictType);
        list.sort(Comparator.comparing(DictDataDO::getSort));
        return list;
    }

}
