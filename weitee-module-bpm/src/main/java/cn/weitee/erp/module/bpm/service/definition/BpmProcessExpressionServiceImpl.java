package cn.weitee.erp.module.bpm.service.definition;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionPageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionSaveReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.definition.BpmProcessExpressionDO;
import cn.weitee.erp.module.bpm.dal.mysql.definition.BpmProcessExpressionMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.PROCESS_EXPRESSION_NOT_EXISTS;

/**
 * BPM 流程表达式 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class BpmProcessExpressionServiceImpl implements BpmProcessExpressionService {

    @Resource
    private BpmProcessExpressionMapper bpmProcessExpressionMapper;

    @Override
    public Long createProcessExpression(BpmProcessExpressionSaveReqVO createReqVO) {
        // 插入
        BpmProcessExpressionDO processExpression = BeanUtils.toBean(createReqVO, BpmProcessExpressionDO.class);
        bpmProcessExpressionMapper.insert(processExpression);
        // 返回
        return processExpression.getId();
    }

    @Override
    public void updateProcessExpression(BpmProcessExpressionSaveReqVO updateReqVO) {
        // 校验存在
        validateProcessExpressionExists(updateReqVO.getId());
        // 更新
        BpmProcessExpressionDO updateObj = BeanUtils.toBean(updateReqVO, BpmProcessExpressionDO.class);
        bpmProcessExpressionMapper.updateById(updateObj);
    }

    @Override
    public void deleteProcessExpression(Long id) {
        // 校验存在
        validateProcessExpressionExists(id);
        // 删除
        bpmProcessExpressionMapper.deleteById(id);
    }

    private void validateProcessExpressionExists(Long id) {
        if (bpmProcessExpressionMapper.selectById(id) == null) {
            throw exception(PROCESS_EXPRESSION_NOT_EXISTS);
        }
    }

    @Override
    public BpmProcessExpressionDO getProcessExpression(Long id) {
        return bpmProcessExpressionMapper.selectById(id);
    }

    @Override
    public PageResult<BpmProcessExpressionDO> getProcessExpressionPage(BpmProcessExpressionPageReqVO pageReqVO) {
        return bpmProcessExpressionMapper.selectPage(pageReqVO);
    }

}