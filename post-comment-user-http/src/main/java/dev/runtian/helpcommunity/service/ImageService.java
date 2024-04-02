package dev.runtian.helpcommunity.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.runtian.helpcommunity.model.dto.file.UploadFileRequest;
import dev.runtian.helpcommunity.model.entity.Image;
import dev.runtian.helpcommunity.model.entity.User;
import dev.runtian.helpcommunity.model.vo.ImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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
