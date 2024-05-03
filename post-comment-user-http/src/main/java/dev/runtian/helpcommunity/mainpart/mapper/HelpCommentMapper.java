package dev.runtian.helpcommunity.mainpart.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.runtian.helpcommunity.commons.helpcommnet.HelpComment;
import dev.runtian.helpcommunity.commons.post.Post;
import org.apache.ibatis.annotations.Param;

/**
* @author rt
* @description 针对表【help_comment(评论)】的数据库操作Mapper
* @createDate 2024-02-22 16:57:01
* @Entity dev.runtian.helpcommunity.model.entity.HelpComment
*/
public interface HelpCommentMapper extends BaseMapper<HelpComment> {
    Page<HelpComment> selectDeletedCommentsByPage(
            IPage<HelpComment> page,
            @Param(Constants.WRAPPER) Wrapper<HelpComment> queryWrapper
    );
}




