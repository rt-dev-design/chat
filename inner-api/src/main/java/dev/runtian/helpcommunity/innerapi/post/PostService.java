package dev.runtian.helpcommunity.innerapi.post;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.post.PostQueryRequest;
import dev.runtian.helpcommunity.commons.post.PostVO;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务接口，声明了实现类需要实现的方法
 * 1、继承了 MyBatisPlus 的 IService 接口
 * 其中有很多好用的默认方法，比如：
 * boolean save(T entity), boolean saveBatch(Collection<T> entityList), saveOrUpdate
 * boolean removeById(Serializable id), 有重载的传入 entity 或 Wrapper 的版本
 * boolean update(T entity), 还可以多传入 Wrapper
 * long count(Wrapper<T> queryWrapper)
 * List<T> list(Wrapper<T> queryWrapper), Wrapper可以不传
 * <E extends IPage<T>> E page(E page, Wrapper<T> queryWrapper)
 * page 方法由 MyBatisPlus 的 IService 接口提供：
 * 参数0 this, 参数1 page 分页信息, 参数2 queryWrapper 查询语句的其他信息
 * 返回 Page<T>, 数据在 records 里，类型是 T，这个返回值是个 DTO, 由 MyBatisPlus 提供，包含分页信息
 * 2、根据具体业务声明了一些方法
 */
public interface PostService extends IService<Post> {

    /**
     * 校验贴子，用于创建或者更新之前
     *
     * @param post 贴子实体对象，封装了将要被校验的参数
     * @param add 是否是一个创建操作，创建操作要求特殊校验
     */
    void validPost(Post post, boolean add);

    /**
     * 从查询请求体获取查询条件
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 获取帖子视图封装
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子视图封装
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    boolean deletePostAndImagesByPostId(long id);

    boolean deletePostWithoutDeletingOtherThings(long id);

    Page<Post> selectDeletedPostsByPage(
            IPage<Post> page,
            @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper
    );

    int restoreDeletedPost(long id);
}
