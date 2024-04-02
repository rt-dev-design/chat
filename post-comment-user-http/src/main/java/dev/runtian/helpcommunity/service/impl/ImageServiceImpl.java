package dev.runtian.helpcommunity.service.impl;


import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.common.ResultUtils;
import dev.runtian.helpcommunity.constant.FileConstant;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.exception.ThrowUtils;
import dev.runtian.helpcommunity.manager.CosManager;
import dev.runtian.helpcommunity.mapper.ImageMapper;
import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.entity.Image;
import dev.runtian.helpcommunity.model.entity.Post;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.enums.FileUploadBizEnum;
import dev.runtian.helpcommunity.model.enums.UserRoleEnum;
import dev.runtian.helpcommunity.model.vo.ImageVO;
import dev.runtian.helpcommunity.service.ImageService;
import dev.runtian.helpcommunity.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
* @author rt
*/
@Service
@Slf4j
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService {

    @Resource
    private CosManager cosManager;

    @Resource
    private PostService postService;


    /**
     * 校验图片文件上传请求合法性的方法 validateImageFileUploadRequest
     *
     * multipartFile 文件的引用
     * uploadFileRequest 上传文件的附加信息
     *
     * 只要有校验不通过则抛异常
     */
    @Override
    public void validateImageFileUploadRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
        if (multipartFile == null || uploadFileRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = uploadFileRequest.getId();
        if (FileUploadBizEnum.POST.equals(fileUploadBizEnum) || FileUploadBizEnum.COMMENT.equals(fileUploadBizEnum)) {
            if (id == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        if (fileSize > ONE_M * 10L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
        }

        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageVO uploadImage(
            MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            User loginUser
    ) {
        // 生成临时文件名和文件信息对象，并创建临时文件，并且用于上传云端
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        Long userId = loginUser.getId();
        Long postId = uploadFileRequest.getId();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        File file = null;
        try {
            // 上传文件到云端
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            String url = FileConstant.COS_HOST + filepath;
            Image image = Image.builder()
                    .url(url).postId(postId)
                    .build();
            //setImageEntityWidthHeight(file, image);
            boolean result = this.save(image);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "内部错误，图片上传失败");

            // 返回可访问地址
            return ImageService.getImageVO(image);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    private void setImageEntityWidthHeight(File imageFile, Image imageEntity)
            throws IOException {
        String fileSuffix = FileUtil.getSuffix(imageFile);
        if (Arrays.asList("jpeg", "jpg", "png").contains(fileSuffix)) {
            BufferedImage img = ImageIO.read(imageFile);
            int width = img.getWidth();
            int height = img.getHeight();
            imageEntity.setWidth(width);
            imageEntity.setHeight(height);
            return;
        }

        ImageReader reader = null;
        try (FileImageInputStream inputStream = new FileImageInputStream(imageFile)) {
            Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName(fileSuffix.toUpperCase());
            if (!iter.hasNext()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "No " + fileSuffix + " reader found.");
            }
            reader = iter.next();
            reader.setInput(inputStream);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            imageEntity.setWidth(width);
            imageEntity.setHeight(height);
            System.out.printf("The size of the WebP image is %d x %d pixels.", width, height);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }

    }


    @Override
    @Transactional
    public boolean deleteById(long id, User user) {
        if (id <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        // 图片必须存在，且必须由管理员或者用户本身删除
        Image image = this.baseMapper.selectOne(new QueryWrapper<Image>().eq("id", id));
        if (image == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        Post post = postService.getOne(new QueryWrapper<Post>().eq("id", image.getPostId()));
        if (post == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        if (!user.getId().equals(post.getUserId()) && !UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        cosManager.deleteObject(image.getUrl());
        int result = this.baseMapper.deleteById(id);
        if (result != 1) throw new BusinessException(ErrorCode.OPERATION_ERROR);
        return true;
    }


}




