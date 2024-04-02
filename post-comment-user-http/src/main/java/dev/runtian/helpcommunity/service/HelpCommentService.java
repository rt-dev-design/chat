package dev.runtian.helpcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.model.dto.helpcommnet.HelpCommentQueryRequest;
import dev.runtian.helpcommunity.model.entity.HelpComment;
import dev.runtian.helpcommunity.model.vo.HelpCommentVO;

import javax.servlet.http.HttpServletRequest;

public interface HelpCommentService extends IService<HelpComment> {
    void validHelpComment(HelpComment helpComment, boolean add);

    /**
     * 从查询请求体获取查询条件
     */
    QueryWrapper<HelpComment> getQueryWrapper(HelpCommentQueryRequest helpCommentQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param helpCommentQueryRequest
     * @return
     */
    // Page<HelpComment> searchFromEs(HelpCommentQueryRequest helpCommentQueryRequest);

    /**
     * 获取帖子视图封装
     */
    HelpCommentVO getHelpCommentVO(HelpComment helpComment, HttpServletRequest request);

    /**
     * 分页获取帖子视图封装
     */
    Page<HelpCommentVO> getHelpCommentVOPage(Page<HelpComment> helpCommentPage, HttpServletRequest request);

    boolean deleteHelpCommentAndCommentImagesByHelpCommentId(long id);
}
