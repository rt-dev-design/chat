package dev.runtian.helpcommunity.mainpart.controller;

import cn.hutool.core.io.FileUtil;
import dev.runtian.helpcommunity.commons.constant.FileConstant;
import dev.runtian.helpcommunity.commons.enums.FileUploadBizEnum;
import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.exception.ThrowUtils;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.post.Image;
import dev.runtian.helpcommunity.commons.post.ImageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.post.ImageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import dev.runtian.helpcommunity.mainpart.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

@RestController
@RequestMapping("/file")
@Slf4j
public class GeneralFileController {
    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request
    ) {
        validateFileUploadRequest(multipartFile, uploadFileRequest);
        User loginUser = userService.getLoginUser(request);
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        Long userId = loginUser.getId();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        File file = null;
        try {
            // 上传文件到云端，返回可访问地址
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            String url = FileConstant.COS_HOST + filepath;
            return ResultUtils.success(url);
        }
        catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    public void validateFileUploadRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
        if (multipartFile == null || uploadFileRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传时请求参数缺失");

        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(uploadFileRequest.getBiz());
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求的上传业务类型错误");
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
}
