package dev.runtian.helpcommunity.commons.general;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;
}
