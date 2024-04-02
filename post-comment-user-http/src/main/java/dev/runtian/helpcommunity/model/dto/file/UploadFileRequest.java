package dev.runtian.helpcommunity.model.dto.file;

import java.io.Serializable;
import lombok.Data;

/**
 * 文件上传请求
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务类型
     * 这里实际上是使用了 dev.runtian.helpcommunity.model.enums.FileUploadBizEnum
     * 中的常量值，但是为了方便让框架将请求参数/体映射到 dto 中，使用了 String
     */
    private String biz;

    private Long id;

    private static final long serialVersionUID = 1L;
}