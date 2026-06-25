package cn.weitee.erp.module.system.api.dict;

import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictDataDO;
import cn.weitee.erp.module.system.service.dict.DictDataService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 字典数据 API 实现类
 *
 * @author WeTai
 */
@Service
public class DictDataApiImpl implements DictDataApi {

    @Resource
    private DictDataService dictDataService;

    @Override
    public void validateDictDataList(String dictType, Collection<String> values) {
        dictDataService.validateDictDataList(dictType, values);
    }

    @Override
    public List<DictDataRespDTO> getDictDataList(String dictType) {
        List<DictDataDO> list = dictDataService.getDictDataListByDictType(dictType);
        return BeanUtils.toBean(list, DictDataRespDTO.class);
    }

}
