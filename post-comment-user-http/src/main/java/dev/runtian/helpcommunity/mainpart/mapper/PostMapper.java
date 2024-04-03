package dev.runtian.helpcommunity.mainpart.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.post.Post;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作接口
 * 这个接口用于声明一些方法，一一对应到 XML 中的 SQL, 并由 MyBatis 提供实现
 * 接口继承了 MyBatis-Plus 的 BaseMapper, 其中声明了很多常用操作的方法，MyBatis-Plus 将提供方法的实现
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

    Page<Post> selectDeletedPostsByPage(
            IPage<Post> page,
            @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper
    );

    int restoreDeletedPost(long id);
}




