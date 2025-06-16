package com.shin.infrastructure.entity.deepSeek;

import lombok.Builder;
import lombok.Data;

/**
 * @author shiner
 * @since 2025/6/13
 */
@Data
@Builder
public class DeepSeekMessage {

    /**
     * Possible values: [assistant]
     *
     * 生成这条消息的角色。
     */
    private String role;
    /**
     * 该 completion 的内容。
     */
    private String content;
}
