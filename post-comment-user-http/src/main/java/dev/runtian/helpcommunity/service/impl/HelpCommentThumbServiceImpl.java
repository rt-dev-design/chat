package dev.runtian.helpcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.model.entity.*;
import dev.runtian.helpcommunity.service.HelpCommentService;
import dev.runtian.helpcommunity.service.HelpCommentThumbService;
import dev.runtian.helpcommunity.mapper.HelpCommentThumbMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author rt
* @description 针对表【help_comment_thumb(评论点赞)】的数据库操作Service实现
* @createDate 2024-02-23 19:34:17
*/
@Service
public class HelpCommentThumbServiceImpl
        extends ServiceImpl<HelpCommentThumbMapper, HelpCommentThumb>
        implements HelpCommentThumbService {

    @Resource
    private HelpCommentService helpCommentService;

    @Override
    public int doHelpCommentThumb(long helpCommentId, User loginUser) {
        // 判断评论是否存在，并获取实体
        HelpComment helpComment = helpCommentService.getById(helpCommentId);
        if (helpComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        // 单个 Tomcat 服务器中，单个用户串行点赞
        // 事务方法就是从 AopContext 里拿来的，是代理的业务 bean, 不是单纯的业务 bean
        // 直接调用的方法没有被代理，出错不会回滚事务
        // 锁就是 synchronized 的参数，是一个和当前单个用户有关的对象
        HelpCommentThumbService helpCommentThumbService = (HelpCommentThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return helpCommentThumbService.doHelpCommentThumbInner(userId, helpCommentId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doHelpCommentThumbInner(long userId, long helpCommentId) {
        // 查询以确定点赞的存在性
        HelpCommentThumb helpCommentThumb = new HelpCommentThumb();
        helpCommentThumb.setUserId(userId);
        helpCommentThumb.setHelpCommentId(helpCommentId);
        QueryWrapper<HelpCommentThumb> thumbQueryWrapper = new QueryWrapper<>(helpCommentThumb);
        HelpCommentThumb oldHelpCommentThumb = this.getOne(thumbQueryWrapper);
        boolean result;

        if (oldHelpCommentThumb != null) {
            // 已点赞，删数据项
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 删除成功则点赞数 - 1
                /**
                 * UPDATE helpComment
                 * SET thumbNum = thumbNum - 1
                 * WHERE helpCommentId = ${id}
                 *     AND thumbNum > 0
                 */
                result = helpCommentService.update()
                        .eq("id", helpCommentId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                // 成功则返回有效增量 -1
                return result ? -1 : 0;
            } else {
                // 删除失败实际上就是取消点赞失败
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞，插数据项
            result = this.save(helpCommentThumb);
            if (result) {
                // 插入成功，点赞数 + 1
                /**
                 * UPDATE helpComment
                 * SET thumbNum = thumbNum + 1
                 * WHERE helpCommentId = id
                 */
                result = helpCommentService.update()
                        .eq("id", helpCommentId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                // 成功则返回有效增量 1
                return result ? 1 : 0;
            } else {
                // 插入失败实际上就是点赞失败
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}




