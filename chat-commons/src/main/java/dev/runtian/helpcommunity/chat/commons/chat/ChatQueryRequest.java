package dev.runtian.helpcommunity.chat.commons.chat;

import dev.runtian.helpcommunity.chat.commons.general.PageRequest;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatQueryRequest extends PageRequest implements Serializable {

    private Long id;

    /**
     * 最近一条消息的时间
     */
    private Date lastMessageTime;

    /**
     * 用户x
     */
    private Long userxId;

    /**
     * 用户y
     */
    private Long useryId;

    /**
     * 用户x最后活跃时间
     */
    private Date usexLastPresentTime;

    /**
     * 用户y最后出现时间
     */
    private Date useryLastPresentTime;

    /**
     * 需要包含该用户，既可以是userx也可以是usery
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
