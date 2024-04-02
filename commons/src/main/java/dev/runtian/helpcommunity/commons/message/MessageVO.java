package dev.runtian.helpcommunity.commons.message;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO  implements Serializable {

    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 所属对话
     */
    private Long chatId;

    /**
     * 消息发送者id
     */
    private Long senderId;

    private static final long serialVersionUID = 1L;
}
