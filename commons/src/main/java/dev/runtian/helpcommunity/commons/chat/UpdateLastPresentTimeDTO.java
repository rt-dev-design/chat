package dev.runtian.helpcommunity.commons.chat;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLastPresentTimeDTO implements Serializable {

    private Long id;

    /**
     * 更新这个用户的最后活动时间
     */
    private Long thisUsersId;

    private Long theOtherUsersId;

    private static final long serialVersionUID = 1L;
}
