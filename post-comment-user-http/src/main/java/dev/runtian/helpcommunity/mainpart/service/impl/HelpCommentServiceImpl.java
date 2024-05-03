package dev.runtian.helpcommunity.mainpart.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.commons.constant.CommonConstant;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.helpcommnet.*;
import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.commons.user.UserVO;
import dev.runtian.helpcommunity.commons.utils.SqlUtils;
import dev.runtian.helpcommunity.innerapi.helpcomment.CommentImageService;
import dev.runtian.helpcommunity.innerapi.helpcomment.HelpCommentService;
import dev.runtian.helpcommunity.innerapi.post.PostService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import dev.runtian.helpcommunity.mainpart.manager.CosManager;
import dev.runtian.helpcommunity.mainpart.mapper.CommentImageMapper;
import dev.runtian.helpcommunity.mainpart.mapper.HelpCommentMapper;
import dev.runtian.helpcommunity.mainpart.mapper.HelpCommentThumbMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HelpCommentServiceImpl 
        extends ServiceImpl<HelpCommentMapper, HelpComment>
        implements HelpCommentService {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private HelpCommentThumbMapper helpCommentThumbMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private CommentImageMapper commentImageMapper;

    @Resource
    private CosManager cosManager;

    @Resource
    private HelpCommentMapper helpCommentMapper;

    /**
     * 检验评论合法性的通用方法 validHelpComment
     * 一层层校验解构数据校验，每个校验是先判空，然后校验业务谓词
     * 校验不通过则抛异常
     *
     * this
     * @param helpComment 要求传入评论实体实例
     * @param add 是否为 insert(add) 之前的校验
     *
     * 成功返回或者抛异常
     */
    @Override
    public void validHelpComment(HelpComment helpComment, boolean add) {
        // 一层层校验解构数据然后校验，先判空，然后校验业务谓词
        if (helpComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long postId = helpComment.getPostId();
        String content = helpComment.getContent();
        // 创建时，检查外键约束
        if (add) {
            ThrowUtils.throwIf(postService.count(new QueryWrapper<Post>().eq("id", postId)) != 1L, ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }
        if (content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 将请求体 DTO 转换成查询条件的方法 getQueryWrapper
     * 在控件和业务里复用
     * 对请求体里面的域进行逐个判空，不空的才加到查询里面，动态生成查询
     * 这样子前端就可以看情况传值，查询接口也可以更好地复用
     * 这里的 Wrapper 和分页无关
     */
    @Override
    public QueryWrapper<HelpComment> getQueryWrapper(HelpCommentQueryRequest helpCommentQueryRequest) {
        QueryWrapper<HelpComment> queryWrapper = new QueryWrapper<>();
        // 1、一般性的、语言语法方面的检验，构造结果
        // 这里防止 NPE
        // 若 HTTP 请求体本身就是空的，指的是连"{}"都没有，则在控件层以上的框架部分就会抛异常了，不会走到这里
        if (helpCommentQueryRequest == null) {
            return queryWrapper;
        }
        // 2、业务性的检验和构造结果
        // 提取 HelpCommentQueryRequest 里面的 9 个域
        String searchText = helpCommentQueryRequest.getSearchText();
        String sortField = helpCommentQueryRequest.getSortField();
        String sortOrder = helpCommentQueryRequest.getSortOrder();
        Long id = helpCommentQueryRequest.getId();
        String content = helpCommentQueryRequest.getContent();
        Long userId = helpCommentQueryRequest.getUserId();
        Long notId = helpCommentQueryRequest.getNotId();
        Long postId = helpCommentQueryRequest.getPostId();
        // 逐个判空，并且拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(postId), "postId", postId);
        if (StringUtils.isAnyBlank(sortField, sortOrder)) {
            queryWrapper.orderBy(
                    true,
                    false,
                    "createTime");
        } else {
            queryWrapper.orderBy(
                    SqlUtils.validSortField(sortField),
                    sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    sortField);
        }
        return queryWrapper;
    }

//    @Override
//    public Page<HelpComment> searchFromEs(HelpCommentQueryRequest helpCommentQueryRequest) {
//        Long id = helpCommentQueryRequest.getId();
//        Long notId = helpCommentQueryRequest.getNotId();
//        String searchText = helpCommentQueryRequest.getSearchText();
//        String title = helpCommentQueryRequest.getTitle();
//        String content = helpCommentQueryRequest.getContent();
//        List<String> tagList = helpCommentQueryRequest.getTags();
//        List<String> orTagList = helpCommentQueryRequest.getOrTags();
//        Long userId = helpCommentQueryRequest.getUserId();
//        // es 起始页为 0
//        long current = helpCommentQueryRequest.getCurrent() - 1;
//        long pageSize = helpCommentQueryRequest.getPageSize();
//        String sortField = helpCommentQueryRequest.getSortField();
//        String sortOrder = helpCommentQueryRequest.getSortOrder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        // 过滤
//        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
//        if (id != null) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
//        }
//        if (notId != null) {
//            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
//        }
//        if (userId != null) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
//        }
//        // 必须包含所有标签
//        if (CollUtil.isNotEmpty(tagList)) {
//            for (String tag : tagList) {
//                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
//            }
//        }
//        // 包含任何一个标签即可
//        if (CollUtil.isNotEmpty(orTagList)) {
//            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
//            for (String tag : orTagList) {
//                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
//            }
//            orTagBoolQueryBuilder.minimumShouldMatch(1);
//            boolQueryBuilder.filter(orTagBoolQueryBuilder);
//        }
//        // 按关键词检索
//        if (StringUtils.isNotBlank(searchText)) {
//            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
//            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
//            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
//            boolQueryBuilder.minimumShouldMatch(1);
//        }
//        // 按标题检索
//        if (StringUtils.isNotBlank(title)) {
//            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
//            boolQueryBuilder.minimumShouldMatch(1);
//        }
//        // 按内容检索
//        if (StringUtils.isNotBlank(content)) {
//            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
//            boolQueryBuilder.minimumShouldMatch(1);
//        }
//        // 排序
//        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
//        if (StringUtils.isNotBlank(sortField)) {
//            sortBuilder = SortBuilders.fieldSort(sortField);
//            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
//        }
//        // 分页
//        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
//        // 构造查询
//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
//                .withPageable(pageRequest).withSorts(sortBuilder).build();
//        SearchHits<HelpCommentEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, HelpCommentEsDTO.class);
//        Page<HelpComment> page = new Page<>();
//        page.setTotal(searchHits.getTotalHits());
//        List<HelpComment> resourceList = new ArrayList<>();
//        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
//        if (searchHits.hasSearchHits()) {
//            List<SearchHit<HelpCommentEsDTO>> searchHitList = searchHits.getSearchHits();
//            List<Long> helpCommentIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
//                    .collect(Collectors.toList());
//            List<HelpComment> helpCommentList = baseMapper.selectBatchIds(helpCommentIdList);
//            if (helpCommentList != null) {
//                Map<Long, List<HelpComment>> idHelpCommentMap = helpCommentList.stream().collect(Collectors.groupingBy(HelpComment::getId));
//                helpCommentIdList.forEach(helpCommentId -> {
//                    if (idHelpCommentMap.containsKey(helpCommentId)) {
//                        resourceList.add(idHelpCommentMap.get(helpCommentId).get(0));
//                    } else {
//                        // 从 es 清空 db 已物理删除的数据
//                        String delete = elasticsearchRestTemplate.delete(String.valueOf(helpCommentId), HelpCommentEsDTO.class);
//                        log.info("delete helpComment {}", delete);
//                    }
//                });
//            }
//        }
//        page.setRecords(resourceList);
//        return page;
//    }

    public static HelpComment voToEntity(HelpCommentVO helpCommentVO) {
        if (helpCommentVO == null) {
            return null;
        }
        HelpComment helpComment = new HelpComment();
        BeanUtils.copyProperties(helpCommentVO, helpComment);
        return helpComment;
    }

    public static HelpCommentVO entityToVo(HelpComment helpComment) {
        if (helpComment == null) {
            return null;
        }
        HelpCommentVO helpCommentVO = new HelpCommentVO();
        BeanUtils.copyProperties(helpComment, helpCommentVO);
        return helpCommentVO;
    }

    /**
     * 将实体转换为视图的方法 getHelpCommentVO
     * 其核心在于
     * 将实体中的外键转化为另一个实体或者视图，通过关联查询的方法
     * 判断登录状态并据此填充视图中的相关数据域
     *
     * 参数：this(service和mapper), helpComment, request(请求和用户信息)
     * 返回：VO
     */
    @Override
    public HelpCommentVO getHelpCommentVO(HelpComment helpComment, HttpServletRequest request) {
        // 1. 将实体和视图中一样的数据域先复制
        HelpCommentVO helpCommentVO = entityToVo(helpComment);
        long helpCommentId = helpComment.getId();
        // 2. 关联查询用户信息，并将视图中的用户 VO 复制好
        Long userId = helpComment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        helpCommentVO.setUser(userVO);
        // 3. 若用户已登录，获取用户对该贴点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<HelpCommentThumb> helpCommentThumbQueryWrapper = new QueryWrapper<>();
            helpCommentThumbQueryWrapper.in("helpCommentId", helpCommentId);
            helpCommentThumbQueryWrapper.eq("userId", loginUser.getId());
            HelpCommentThumb helpCommentThumb = helpCommentThumbMapper.selectOne(helpCommentThumbQueryWrapper);
            helpCommentVO.setHasThumb(helpCommentThumb != null);
        }
        // 4. 获取评论图片列表
        helpCommentVO.setCommentImages(commentImageMapper.selectList(new QueryWrapper<CommentImage>().eq("commentId", helpCommentId)).stream()
                .map(CommentImageService::getCommentImageVO)
                .collect(Collectors.toList()));
        return helpCommentVO;
    }

    /**
     * 将实体页面转换为视图页面的方法 getHelpCommentVOPage
     * 本质是对于实体列表页面中各个评论的某些域所关联的表进行关联查询
     * 对于每个需要关联的域，将全部值取出来做一次查询
     *
     * 参数：this(service和mapper), helpCommentPage, request(请求和用户信息)
     *
     * 返回：HelpCommentVO page
     */
    @Override
    public Page<HelpCommentVO> getHelpCommentVOPage(
            Page<HelpComment> helpCommentPage,
            HttpServletRequest request
    ) {
        Page<HelpCommentVO> helpCommentVOPage = new Page<>(helpCommentPage.getCurrent(), helpCommentPage.getSize(), helpCommentPage.getTotal());
        List<HelpComment> helpCommentList = helpCommentPage.getRecords();
        if (CollUtil.isEmpty(helpCommentList)) {
            return helpCommentVOPage;
        }
        // 1. 对页面中的各个评论，关联查询发帖用户信息并构造视图，即 HelpComment userId(发送评论的用户) -> HelpCommentVO userId + userVO
        Set<Long> userIdSet = helpCommentList.stream()
                .map(HelpComment::getUserId)
                .collect(Collectors.toSet());
        // 将 user 的 list 转换为 <userId, user> 的 map
        Map<Long, List<User>> userIdUserListMap =
                userService.listByIds(userIdSet).stream()
                        .collect(Collectors.groupingBy(User::getId));
        // 2. 判断是否已登录，获取当前用户对页面中各个评论点赞、收藏状态

        Set<Long> helpCommentIdSet = helpCommentList.stream()
                .map(HelpComment::getId)
                .collect(Collectors.toSet());

        User loginUser = userService.getLoginUserPermitNull(request);
        Map<Long, Boolean> helpCommentIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> helpCommentIdHasFavourMap = new HashMap<>();
        if (loginUser != null) {
            // 对于页面中的所有评论和当前用户，获取点赞情况
            QueryWrapper<HelpCommentThumb> helpCommentThumbQueryWrapper = new QueryWrapper<>();
            helpCommentThumbQueryWrapper.in("helpCommentId", helpCommentIdSet);
            helpCommentThumbQueryWrapper.eq("userId", loginUser.getId());
            List<HelpCommentThumb> helpCommentHelpCommentThumbList = helpCommentThumbMapper.selectList(helpCommentThumbQueryWrapper);
            helpCommentHelpCommentThumbList.forEach(helpCommentHelpCommentThumb ->
                    helpCommentIdHasThumbMap.put(helpCommentHelpCommentThumb.getHelpCommentId(), true));
        }

        // 4. 对页面中的各个评论，关联查询图片信息并构造视图，即 HelpComment userId(发送评论的用户) -> HelpCommentVO userId + userVO
        Map<Long, List<CommentImage>> helpCommentIdCommentImagesMap = commentImageMapper.selectList(
                        new QueryWrapper<CommentImage>()
                                .in("commentId", helpCommentIdSet)
                                .ne("isDelete", 1)
                )
                .stream()
                .collect(Collectors.groupingBy(CommentImage::getCommentId));
        Map<Long, List<CommentImageVO>> helpCommentIdCommentImageVOsMap = new HashMap<>();
        helpCommentIdCommentImagesMap.forEach((k, v) -> {
            helpCommentIdCommentImageVOsMap.put(k, v.stream().map(CommentImageService::getCommentImageVO).collect(Collectors.toList()));
        });
        // 填充信息
        List<HelpCommentVO> helpCommentVOList = helpCommentList.stream().map(helpComment -> {
            HelpCommentVO helpCommentVO = entityToVo(helpComment);
            Long userId = helpComment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            helpCommentVO.setUser(userService.getUserVO(user));
            helpCommentVO.setHasThumb(helpCommentIdHasThumbMap.getOrDefault(helpComment.getId(), false));
            helpCommentVO.setHasFavour(helpCommentIdHasFavourMap.getOrDefault(helpComment.getId(), false));
            helpCommentVO.setCommentImages(helpCommentIdCommentImageVOsMap.getOrDefault(helpComment.getId(), null));
            return helpCommentVO;
        }).collect(Collectors.toList());
        helpCommentVOPage.setRecords(helpCommentVOList);
        return helpCommentVOPage;
    }

    @Override
    public boolean deleteHelpCommentAndCommentImagesByHelpCommentId(long id) {
        QueryWrapper<CommentImage> qw = new QueryWrapper<>();
        qw.eq("id", id);
        List<CommentImage> commentImageList = commentImageMapper.selectList(qw);

        if (CollUtil.isNotEmpty(commentImageList)) {
            List<Long> commentImageIdList = commentImageList.stream().map(CommentImage::getId).collect(Collectors.toList());
            int delImgRes = commentImageMapper.deleteBatchIds(commentImageIdList);
            if (delImgRes != commentImageIdList.size()) throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除评论图片失败");
            cosManager.deleteMultipleObjects(commentImageList.stream().map(CommentImage::getUrl).collect(Collectors.toList()));
        }

        boolean delCommentRes = this.removeById(id);
        ThrowUtils.throwIf(!delCommentRes, ErrorCode.OPERATION_ERROR, "删除评论失败");
        return true;
    }

    @Override
    public Page<HelpComment> selectDeletedCommentByPage(IPage<HelpComment> page, Wrapper<HelpComment> queryWrapper) {
        return helpCommentMapper.selectDeletedCommentsByPage(page, queryWrapper);
    }
}




