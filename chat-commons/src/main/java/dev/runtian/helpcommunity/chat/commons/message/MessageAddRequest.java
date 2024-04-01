package dev.runtian.helpcommunity.chat.commons.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageAddRequest implements Serializable {

    private String type;

    private String content;

    private Long senderId;

    private Long receiverId;
    
    private Long chatId;

    private static final long serialVersionUID = 1L;
}
