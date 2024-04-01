package dev.runtian.helpcommunity.chat.commons.message;

import dev.runtian.helpcommunity.chat.commons.general.PageRequest;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueryRequest extends PageRequest implements Serializable {
    private Long chatId;

    private Long senderId;

    private Long receiverId;

    private Date beforeTime;

    private Long id;

    private String type;

    private String content;

    private static final long serialVersionUID = 1L;
}
