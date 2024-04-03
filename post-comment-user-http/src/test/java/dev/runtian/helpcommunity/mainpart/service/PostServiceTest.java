package dev.runtian.helpcommunity.mainpart.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.annotation.Resource;

import dev.runtian.helpcommunity.commons.post.Post;
import dev.runtian.helpcommunity.commons.post.PostQueryRequest;
import dev.runtian.helpcommunity.innerapi.post.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子服务测试
 *
 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostService postService;

    @Test
    void searchFromEs() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setUserId(1L);
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        Assertions.assertNotNull(postPage);
    }

}