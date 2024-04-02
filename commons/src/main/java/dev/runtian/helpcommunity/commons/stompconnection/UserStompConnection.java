package dev.runtian.helpcommunity.commons.stompconnection;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("UserStompConnection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class UserStompConnection  implements Serializable {

    @Id
    private Long id;
    private String ip;
    private Integer port;
    private Boolean online;

    private static final long serialVersionUID = 1L;
}
