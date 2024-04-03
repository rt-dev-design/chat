package dev.runtian.helpcommunity.mainpart.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.postfavour.PostFavour;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.post.PostFavourService;
import dev.runtian.helpcommunity.innerapi.post.PostService;
import dev.runtian.helpcommunity.mainpart.mapper.PostFavourMapper;

import javax.annotation.Resource;

import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户收藏帖子服务实现，服务实现类，bean
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;

    /**
     * 用户收藏贴子的业务的代理方法 doPostFavour
     * 做业务校验和多线程同步，通过调用其他方法完成实际业务
     *
     * this(IService, Service, ServiceImpl, mappers, other services)
     * postId
     * loginUser
     *
     * 返回贴子收藏数的变化量
     * 或者抛异常
     */
    @Override
    public int doPostFavour(long postId, User loginUser) {
        // 判断贴子是否存在，否则抛异常，是则往下
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        // 每个用户串行进行帖子收藏
        // 要有和此用户有关的锁，包裹住事务方法
        // String.intern() 方法获取字符串在池中的内部表示对象，常用于获取和用户有关的锁
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    /**
     * 获取我收藏的帖子的列表的业务方法 listFavourPostByPage
     * 做一点业务校验，然后调用 mapper 进行关联查询
     * 这里的数据库表的关系是 post, post_favour(postId 和 userId 对)
     * 所以要对这 2 个表的 join on postId 做查询
     * 这里是调用 mapper 直接传 sql 做，因为涉及按不同数据域和排序顺序的分页，很难从业务去模拟 join，或者说效率会很低
     *
     * this(IService, Service, ServiceImpl, mappers, other services)
     * page 分页信息，传给 mapper
     * queryWrapper 查询条件/sql片段，传给 mapper
     * favourUserId 查询哪个用户的收藏
     *
     * 返回收藏贴子页面数据
     * 或者抛异常
     */
    @Override
    public Page<Post> listFavourPostByPage(
            IPage<Post> page,
            Wrapper<Post> queryWrapper,
            long favourUserId
    ) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(page, queryWrapper, favourUserId);
    }

    /**
     * 用户收藏贴子的业务的事务方法 doPostFavourInner
     * 完成实际业务，主要是执行了数据库事务
     * 这个事务是
     * 一次对 post_favour 的 insert if not exists and delete if exists
     * 以及一次对 post 表中 favourNum 的 update
     *
     * this(IService, ServiceImpl, mappers, service)
     * postId
     * userId
     *
     * 返回贴子收藏数的变化量
     * 或者抛异常
     * 并且回滚数据库事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostFavourInner(long userId, long postId) {
        // 查询是否已经收藏
        PostFavour postFavour = new PostFavour();
        postFavour.setUserId(userId);
        postFavour.setPostId(postId);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour oldPostFavour = this.getOne(postFavourQueryWrapper);
        boolean result;
        if (oldPostFavour != null) {
            // 已收藏，则删除
            result = this.remove(postFavourQueryWrapper);
            // 删除成功则取消成功，否则失败
            if (result) {
                // 帖子收藏数 - 1
                /**
                 * UPDATE post
                 * SET favourNum = favourNum - 1
                 * WHERE id = ${postId}
                 *     AND favourNum > 0
                 */
                result = postService.update()
                        .eq("id", postId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum - 1")
                        .update();
                // 若成功则返回有效增量-1
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未收藏，则插入
            result = this.save(postFavour);
            // 插入成功则收藏成功，否则失败
            if (result) {
                // 帖子收藏数 + 1
                /**
                 * UPDATE post
                 * SET favourNum = favourNum + 1
                 * WHERE id = ${postId}
                 */
                result = postService.update()
                        .eq("id", postId)
                        .setSql("favourNum = favourNum + 1")
                        .update();
                // 若成功则返回有效增量1
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

}




