package dev.runtian.helpcommunity.mainpart.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.commons.constant.CommonConstant;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.post.*;
import dev.runtian.helpcommunity.commons.postfavour.PostFavour;
import dev.runtian.helpcommunity.commons.postthumb.PostThumb;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.commons.user.UserVO;
import dev.runtian.helpcommunity.commons.utils.SqlUtils;
import dev.runtian.helpcommunity.innerapi.post.ImageService;
import dev.runtian.helpcommunity.innerapi.post.PostService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import dev.runtian.helpcommunity.mainpart.esdao.PostEsDTO;
import dev.runtian.helpcommunity.mainpart.manager.CosManager;
import dev.runtian.helpcommunity.mainpart.mapper.ImageMapper;
import dev.runtian.helpcommunity.mainpart.mapper.PostFavourMapper;
import dev.runtian.helpcommunity.mainpart.mapper.PostMapper;
import dev.runtian.helpcommunity.mainpart.mapper.PostThumbMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子服务实现类，bean
 * 注入了需要用到的其他服务、请求模板、管理器以及 DAO 层 beans
 * 实现了服务接口 PostService 中的方法
 * 继承了 ServiceImpl, 其实现了 IService
 * 这个继承体系受 MyBatis-Plus 框架的影响
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private CosManager cosManager;

    @Resource
    private PostMapper postMapper;

    /**
     * 检验贴子合法性的通用方法 validPost
     * 一层层校验解构数据校验，每个校验是先判空，然后校验业务谓词
     * 校验不通过则抛异常
     *
     * this
     * @param post 要求传入贴子实体实例
     * @param add 是否为 insert(add) 之前的校验
     *
     * 成功返回或者抛异常
     */
    @Override
    public void validPost(Post post, boolean add) {
        // 一层层校验解构数据然后校验，先判空，然后校验业务谓词
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题或内容不能为空");
        }
        if (title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
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
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        // 1、一般性的、语言语法方面的检验，构造结果
        // 这里防止 NPE
        // 若 HTTP 请求体本身就是空的，指的是连"{}"都没有，则在控件层以上的框架部分就会抛异常了，不会走到这里
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        // 2、业务性的检验和构造结果
        // 提取 PostQueryRequest 里面的 9 个域
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 逐个判空，并且拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
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

    /**
     * 向ES发起查询/搜索请求获取数据的业务方法searchFromEs
     *
     * this
     * postQueryRequest 查询请求DTO实例，提供搜索条件
     *
     * 贴子实体页面
     */
    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        // 排除空指针，并将查询请求的所有数据域取出来
        if (postQueryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // 注意，在 es 中，起始页为 0，所以这里需要 -1
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();

        // 构造ES请求条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 关于各个方法：https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html

        // 选出filter和排除must not的意思是“必须这样”，“尽量查出更多”的原则不适用
        // termQuery是只直接拿数据域作比较，而不作拆词和匹配

        // 选出没有被删除的数据，且这一项不参与评分
        // id, notId (排除id为notId的项，且不评分), userId 同理
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }


        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollUtil.isNotEmpty(orTagList)) {
            // 构造子查询条件
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            // 加到父条件中
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);


        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    /**
     * 将实体转换为视图的方法 getPostVO
     * 其核心在于
     * 将实体中的外键转化为另一个实体或者视图，通过关联查询的方法
     * 判断登录状态并据此填充视图中的相关数据域
     *
     * 参数：this(service和mapper), post, request(请求和用户信息)
     * 返回：VO
     */
    @Override
    @Transactional
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        // 1. 将实体和视图中一样的数据域先复制
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 2. 关联查询用户信息，并将视图中的用户 VO 复制好
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 3. 若用户已登录，获取用户对该贴点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        // 4. 获取贴子图片列表
        postVO.setImages(imageMapper.selectList(new QueryWrapper<Image>().eq("postId", postId)).stream()
                .map(ImageService::getImageVO)
                .collect(Collectors.toList()));

        return postVO;
    }

    /**
     * 将实体页面转换为视图页面的方法 getPostVOPage
     * 本质是对于实体列表页面中各个贴子的某些域所关联的表进行关联查询
     * 对于每个需要关联的域，将全部值取出来做一次查询
     *
     * 参数：this(service和mapper), postPage, request(请求和用户信息)
     * 返回：PostVO page
     */
    @Override
    public Page<PostVO> getPostVOPage(
            Page<Post> postPage,
            HttpServletRequest request
    ) {
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        List<Post> postList = postPage.getRecords();
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 对页面中的各个贴子，关联查询发帖用户信息并构造视图，即 Post userId(发送贴子的用户) -> PostVO userId + userVO
        Set<Long> userIdSet = postList.stream()
                .map(Post::getUserId)
                .collect(Collectors.toSet());
        // 将 user 的 list 转换为 <userId, user> 的 map
        Map<Long, List<User>> userIdUserListMap =
                userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 判断是否已登录，获取当前用户对页面中各个贴子点赞、收藏状态

        Set<Long> postIdSet = postList.stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        User loginUser = userService.getLoginUserPermitNull(request);
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        if (loginUser != null) {
            // 对于页面中的所有贴子和当前用户，获取点赞情况
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb ->
                    postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 对于页面中的所有贴子和当前用户，获取收藏情况
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour ->
                    postIdHasFavourMap.put(postFavour.getPostId(), true));
        }

        // 4. 对页面中的各个贴子，关联查询图片信息并构造视图，即 Post userId(发送贴子的用户) -> PostVO userId + userVO
        Map<Long, List<Image>> postIdImagesMap = imageMapper.selectList(
                new QueryWrapper<Image>()
                        .in("postId", postIdSet)
                        .ne("isDelete", 1)
                )
                .stream()
                .collect(Collectors.groupingBy(Image::getPostId));
        Map<Long, List<ImageVO>> postIdImageVOsMap = new HashMap<>();
        postIdImagesMap.forEach((k, v) -> {
            postIdImageVOsMap.put(k, v.stream().map(ImageService::getImageVO).collect(Collectors.toList()));
        });
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            postVO.setImages(postIdImageVOsMap.getOrDefault(post.getId(), null));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    @Transactional
    public boolean deletePostAndImagesByPostId(long id) {
        QueryWrapper<Image> qw = new QueryWrapper<>();
        qw.eq("postId", id);
        List<Image> imageList = imageMapper.selectList(qw);


        if (CollUtil.isNotEmpty(imageList)) {
            List<Long> imageIdList = imageList.stream().map(Image::getId).collect(Collectors.toList());
            int delRet = imageMapper.deleteBatchIds(imageIdList);
            if (delRet != imageIdList.size()) throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除图片失败");
            cosManager.deleteMultipleObjects(imageList.stream().map(Image::getUrl).collect(Collectors.toList()));
        }

        boolean delCommentRes = this.removeById(id);
        ThrowUtils.throwIf(!delCommentRes, ErrorCode.OPERATION_ERROR, "删除贴子失败");
        return true;
    }

    @Override
    @Transactional
    public boolean deletePostWithoutDeletingOtherThings(long id) {
        boolean delRes = this.removeById(id);
        ThrowUtils.throwIf(!delRes, ErrorCode.OPERATION_ERROR, "删除贴子失败");
        return true;
    }

    @Override
    public Page<Post> selectDeletedPostsByPage(IPage<Post> page, Wrapper<Post> queryWrapper) {
        return postMapper.selectDeletedPostsByPage(page, queryWrapper);
    }

    @Override
    public int restoreDeletedPost(long id) {
        int res = postMapper.restoreDeletedPost(id);
        ThrowUtils.throwIf(res != 1, new BusinessException(ErrorCode.OPERATION_ERROR));
        return res;
    }
}
