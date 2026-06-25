package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.CUSTOMER_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

/**
 * ERP 客户 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ErpCustomerServiceImpl implements ErpCustomerService {

    @Resource
    private ErpCustomerMapper erpCustomerMapper;

    @Override
    public Long createCustomer(ErpCustomerSaveReqVO createReqVO) {
        // 插入
        ErpCustomerDO customer = BeanUtils.toBean(createReqVO, ErpCustomerDO.class);
        erpCustomerMapper.insert(customer);
        // 返回
        return customer.getId();
    }

    @Override
    public void updateCustomer(ErpCustomerSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());
        // 更新
        ErpCustomerDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomerDO.class);
        erpCustomerMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);
        // 删除
        erpCustomerMapper.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
        if (erpCustomerMapper.selectById(id) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomerDO getCustomer(Long id) {
        return erpCustomerMapper.selectById(id);
    }

    @Override
    public ErpCustomerDO validateCustomer(Long id) {
        ErpCustomerDO customer = erpCustomerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(customer.getStatus())) {
            throw exception(CUSTOMER_NOT_ENABLE, customer.getName());
        }
        return customer;
    }

    @Override
    public List<ErpCustomerDO> getCustomerList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return erpCustomerMapper.selectByIds(ids);
    }

    @Override
    public PageResult<ErpCustomerDO> getCustomerPage(ErpCustomerPageReqVO pageReqVO) {
        return erpCustomerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpCustomerDO> getCustomerListByStatus(Integer status) {
        return erpCustomerMapper.selectListByStatus(status);
    }

}
