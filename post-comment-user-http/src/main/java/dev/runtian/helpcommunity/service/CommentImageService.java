package dev.runtian.helpcommunity.service;

import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.entity.CommentImage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.model.entity.Image;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.vo.CommentImageVO;
import dev.runtian.helpcommunity.model.vo.ImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

/**
* @author rt
* @description 针对表【comment_image(评论包含的图片的信息)】的数据库操作Service
* @createDate 2024-02-23 19:27:28
*/
public interface CommentImageService extends IService<CommentImage> {

    static CommentImageVO getCommentImageVO(CommentImage commentImage) {
        CommentImageVO commentImageVO = new CommentImageVO();
        BeanUtils.copyProperties(commentImage, commentImageVO);
        return commentImageVO;
    }

    void validateCommentImageFileUploadRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest);

    CommentImageVO uploadCommentImage(MultipartFile multipartFile, UploadFileRequest uploadFileRequest, User loginUser);

    boolean deleteById(long id, User user);
}
