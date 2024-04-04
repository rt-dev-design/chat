package dev.runtian.helpcommunity.commons.chat;

import dev.runtian.helpcommunity.commons.general.PageRequest;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatQueryRequest extends PageRequest implements Serializable {

    /**
     * chatId
     */
    private Long id;

    /**
     * 最近一条消息的时间
     */
    private Date lastMessageTime;



    /**
     * 用户x最后出现时间
     */
    private Date usexLastPresentTime;

    /**
     * 用户y最后出现时间
     */
    private Date useryLastPresentTime;

    // 以下是3种不同的查询

    /**
     * 用户x的id，需要包含该用户，且必须是用户x
     */
    private Long userxId;

    /**
     * 用户y的id，需要包含该用户，且必须是用户y
     */
    private Long useryId;

    /**
     * 需要包含这1个用户，既可以是userx也可以是usery
     */
    private Long userId;

    /**
     * 需要包含以下2个用户，谁是x谁是y不重要
     */
    private Long thisUsersId;

    private Long theOtherUsersId;

    private static final long serialVersionUID = 1L;
}
