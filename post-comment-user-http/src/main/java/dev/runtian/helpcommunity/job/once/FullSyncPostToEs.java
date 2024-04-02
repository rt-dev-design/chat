package dev.runtian.helpcommunity.job.once;

import dev.runtian.helpcommunity.esdao.PostEsDao;
import dev.runtian.helpcommunity.model.dto.post.PostEsDTO;
import dev.runtian.helpcommunity.model.entity.Post;
import dev.runtian.helpcommunity.service.PostService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 全量同步帖子到 es 的方法类，bean
 * 作为 CommandLineRunner 在系统初始化完毕后执行一次，并在这个生命周期里只执行一次
 *
 */
@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 同步2个数据源的方法
     * 将 MySQL 中的数据一次性全部读入，然后存入 ElasticSearch
     *
     * args 由框架传入的命令行参数，在这里用不到
     *
     *
     */
    @Override
    public void run(String... args) {
        List<Post> postList = postService.list();
        if (CollUtil.isEmpty(postList)) {
            return;
        }

        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("FullSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("FullSyncPostToEs end, total {}", total);
    }
}
