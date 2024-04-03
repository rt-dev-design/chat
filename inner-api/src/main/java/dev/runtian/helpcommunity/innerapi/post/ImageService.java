package dev.runtian.helpcommunity.innerapi.post;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.commons.file.UploadFileRequest;
import dev.runtian.helpcommunity.commons.post.Image;
import dev.runtian.helpcommunity.commons.post.ImageVO;
import dev.runtian.helpcommunity.commons.user.User;
import org.springframework.beans.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

/**
* @author rt
* @description 针对表【image(贴子包含的图片的信息)】的数据库操作Service
* @createDate 2024-02-22 12:08:52
*/
public interface ImageService extends IService<Image> {
    static ImageVO getImageVO(Image image) {
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(image, imageVO);
        return imageVO;
    }

    void validateImageFileUploadRequest(MultipartFile multipartFile, UploadFileRequest uploadFileRequest);

    ImageVO uploadImage(MultipartFile multipartFile, UploadFileRequest uploadFileRequest, User loginUser);

    boolean deleteById(long id, User user);
}
