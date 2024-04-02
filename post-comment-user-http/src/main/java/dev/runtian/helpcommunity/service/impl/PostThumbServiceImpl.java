package dev.runtian.helpcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.mapper.PostThumbMapper;
import dev.runtian.helpcommunity.model.entity.Post;
import dev.runtian.helpcommunity.model.entity.PostThumb;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.service.PostService;
import dev.runtian.helpcommunity.service.PostThumbService;
import javax.annotation.Resource;

import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户点赞帖子服务，业务实现类，bean
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
        implements PostThumbService {

    @Resource
    private PostService postService;

    /**
     * 用户点赞贴子的业务的代理方法 doPostThumb
     * 做业务校验和多线程同步，通过调用其他方法完成实际业务
     *
     * this(IService, ServiceImpl, mappers, service)
     * postId
     * loginUser
     *
     * 返回贴子点赞数的变化量
     * 或者抛异常
     */
    @Override
    public int doPostThumb(long postId, User loginUser) {
        // 判断贴子是否存在，并获取实体
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        // 单个 Tomcat 服务器中，单个用户串行点赞
        // 事务方法就是从 AopContext 里拿来的，是代理的业务 bean, 不是单纯的业务 bean
        // 直接调用的方法没有被代理，出错不会回滚事务
        // 锁就是 synchronized 的参数，是一个和当前单个用户有关的对象
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postThumbService.doPostThumbInner(userId, postId);
        }
    }

    /**
     * 用户点赞贴子的业务的事务方法 doPostThumbInner
     * 完成实际业务，主要是执行了数据库事务
     * 这个事务是
     * 一次对 post_thumb 的 insert if not exists and delete if exists
     * 以及一次对 post 表中 thumbNum 的 update
     *
     * this(IService, ServiceImpl, mappers, service)
     * postId
     * userId
     *
     * 返回贴子点赞数的变化量
     * 或者抛异常
     * 并且回滚数据库事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostThumbInner(long userId, long postId) {
        // 查询以确定点赞的存在性
        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb oldPostThumb = this.getOne(thumbQueryWrapper);
        // 返回值
        boolean result;
        if (oldPostThumb != null) {
            // 已点赞，删数据项
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 删除成功则点赞数 - 1
                /**
                 * UPDATE post
                 * SET thumbNum = thumbNum - 1
                 * WHERE postId = ${id}
                 *     AND thumbNum > 0
                 */
                result = postService.update()
                        .eq("id", postId)
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
            result = this.save(postThumb);
            if (result) {
                // 插入成功，点赞数 + 1
                /**
                 * UPDATE post
                 * SET thumbNum = thumbNum + 1
                 * WHERE postId = id
                 */
                result = postService.update()
                        .eq("id", postId)
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




