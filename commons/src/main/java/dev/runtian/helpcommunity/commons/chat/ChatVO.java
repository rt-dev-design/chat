package dev.runtian.helpcommunity.commons.chat;

import dev.runtian.helpcommunity.commons.model.vo.UserVO;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatVO implements Serializable {
    private Long id;

    private UserVO theOtherUser;

    private Date lastMessageTime;

    private String lastMessage;

    private Boolean thereAreNewMessages;

    private static final long serialVersionUID = 1L;
}
