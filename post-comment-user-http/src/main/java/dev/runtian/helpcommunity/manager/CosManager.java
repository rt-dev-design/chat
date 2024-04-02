package dev.runtian.helpcommunity.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsResult;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.config.CosClientConfig;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import dev.runtian.helpcommunity.constant.FileConstant;
import dev.runtian.helpcommunity.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    public void deleteObject(String url) {
        String key = url.substring(FileConstant.COS_HOST.length() + 1);
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    public void deleteMultipleObjects(List<String> urls) {
        String[] keys = new String[urls.size()];
        urls.stream().map(e -> e.substring(FileConstant.COS_HOST.length() + 1)).collect(Collectors.toList()).toArray(keys);
        DeleteObjectsResult result = cosClient.deleteObjects(new DeleteObjectsRequest(cosClientConfig.getBucket()).withKeys(keys));
        if (result.getDeletedObjects().size() != urls.size())
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
    }
}
