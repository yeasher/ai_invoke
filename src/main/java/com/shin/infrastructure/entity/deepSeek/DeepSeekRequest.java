package com.shin.infrastructure.entity.deepSeek;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author shiner
 * @since 2025/6/13
 */
@Data
@Builder
public class DeepSeekRequest {

    private String model;
    private List<DeepSeekMessage> messages;
    private Boolean stream;

}
