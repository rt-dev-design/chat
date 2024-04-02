package dev.runtian.helpcommunity.controller;

import dev.runtian.helpcommunity.common.BaseResponse;
import dev.runtian.helpcommunity.common.DeleteRequest;
import dev.runtian.helpcommunity.common.ErrorCode;
import dev.runtian.helpcommunity.common.ResultUtils;
import dev.runtian.helpcommunity.exception.BusinessException;
import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.vo.CommentImageVO;
import dev.runtian.helpcommunity.service.CommentImageService;
import dev.runtian.helpcommunity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 文件接口，操作服务器对象文件资源，控件类，bean
 * 在 MVC 中注册和映射到 /api/comment_image/**
 * 调用用户业务，本地对象信息业务，以及腾讯云对象存储业务管理
 */
@RestController
@RequestMapping("/comment_image")
@Slf4j
public class CommentImageController {

    @Resource
    private UserService userService;

    @Resource
    private CommentImageService commentImageService;

    @PostMapping("/upload")
    public BaseResponse<CommentImageVO> uploadCommentImage(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request
    ) {
        commentImageService.validateCommentImageFileUploadRequest(multipartFile, uploadFileRequest);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(commentImageService.uploadCommentImage(multipartFile, uploadFileRequest, loginUser));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCommentImage(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        boolean b = commentImageService.deleteById(deleteRequest.getId(), user);
        return ResultUtils.success(b);
    }

}
