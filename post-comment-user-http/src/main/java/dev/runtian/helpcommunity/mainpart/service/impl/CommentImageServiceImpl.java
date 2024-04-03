package dev.runtian.helpcommunity.mainpart.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.runtian.helpcommunity.commons.constant.FileConstant;
import dev.runtian.helpcommunity.commons.enums.FileUploadBizEnum;
import dev.runtian.helpcommunity.commons.enums.UserRoleEnum;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.helpcommnet.CommentImage;
import dev.runtian.helpcommunity.commons.helpcommnet.CommentImageVO;
import dev.runtian.helpcommunity.commons.helpcommnet.HelpComment;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.helpcomment.CommentImageService;
import dev.runtian.helpcommunity.innerapi.helpcomment.HelpCommentService;
import dev.runtian.helpcommunity.mainpart.manager.CosManager;
import dev.runtian.helpcommunity.mainpart.mapper.CommentImageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
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
* @description 针对表【comment_image(评论包含的图片的信息)】的数据库操作Service实现
* @createDate 2024-02-23 19:27:28
*/
@Service
@Slf4j
public class CommentImageServiceImpl extends ServiceImpl<CommentImageMapper, CommentImage>
    implements CommentImageService {

    @Resource
    private CosManager cosManager;

    @Resource
    private HelpCommentService commentService;



    @Override
    public void validateCommentImageFileUploadRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
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
    @Transactional
    public CommentImageVO uploadCommentImage(
            MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            User loginUser
    ) {
        // 生成临时文件名和文件信息对象，并创建临时文件，并且用于上传云端
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        Long userId = loginUser.getId();
        Long commentId = uploadFileRequest.getId();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        File file = null;
        try {
            // 上传文件到云端
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            String url = FileConstant.COS_HOST + filepath;
            CommentImage commentImage = CommentImage.builder()
                    .url(url).commentId(commentId)
                    .build();
            // setCommentImageEntityWidthHeight(file, commentImage);
            boolean result = this.save(commentImage);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "内部错误，图片上传失败");
            // 返回可访问地址
            return CommentImageService.getCommentImageVO(commentImage);
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

    private void setCommentImageEntityWidthHeight(File imageFile, CommentImage imageEntity)
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
        CommentImage commentImage = this.baseMapper.selectOne(new QueryWrapper<CommentImage>().eq("id", id));
        if (commentImage == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        HelpComment post = commentService.getOne(new QueryWrapper<HelpComment>().eq("id", commentImage.getCommentId()));
        if (post == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        if (!user.getId().equals(post.getUserId()) && !UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        cosManager.deleteObject(commentImage.getUrl());
        int result = this.baseMapper.deleteById(id);
        if (result != 1) throw new BusinessException(ErrorCode.OPERATION_ERROR);
        return true;
    }
}




