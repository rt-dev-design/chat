package dev.runtian.helpcommunity.mainpart.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import dev.runtian.helpcommunity.commons.exception.BusinessException;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.general.BaseResponse;
import dev.runtian.helpcommunity.commons.general.DeleteRequest;
import dev.runtian.helpcommunity.commons.general.ErrorCode;
import dev.runtian.helpcommunity.commons.general.ResultUtils;
import dev.runtian.helpcommunity.commons.post.ImageVO;
import dev.runtian.helpcommunity.commons.user.User;
import dev.runtian.helpcommunity.innerapi.post.ImageService;
import dev.runtian.helpcommunity.innerapi.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口，操作服务器对象文件资源，控件类，bean
 * 在 MVC 中注册和映射到 /api/image/**
 * 调用用户业务，本地对象信息业务，以及腾讯云对象存储业务管理
 */
@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Resource
    private UserService userService;

    @Resource
    private ImageService imageService;

    /**
     * 图片文件上传端点 uploadImageFile
     *
     * multipartFile 这个参数的注解会要求请求的 Content-Type
     *                      为 Multipart，且参数名称为 file
     * uploadFileRequest 告诉服务器这个文件是有关什么业务类型的
     *                          请求中的除了 file 的另一个参数的名字自动对应到 uploadFileRequest 中的 biz
     * request servlet request, user
     *
     * 上传完的图片视图，含云端 url
     * 或者抛异常
     */
    @PostMapping("/upload")
    public BaseResponse<ImageVO> uploadImage(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request
    ) {
        imageService.validateImageFileUploadRequest(multipartFile, uploadFileRequest);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(imageService.uploadImage(multipartFile, uploadFileRequest, loginUser));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteImage(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request
    ) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        boolean b = imageService.deleteById(deleteRequest.getId(), user);
        return ResultUtils.success(b);
    }

}
