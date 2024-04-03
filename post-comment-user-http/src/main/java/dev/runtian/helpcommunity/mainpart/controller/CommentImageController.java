package dev.runtian.helpcommunity.mainpart.controller;

import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.DeleteRequest;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.helpcommnet.CommentImageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.helpcomment.CommentImageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
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
