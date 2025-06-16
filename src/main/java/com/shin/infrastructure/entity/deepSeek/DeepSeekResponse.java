package com.shin.infrastructure.entity.deepSeek;

import lombok.Data;

import java.util.List;

/**
 * @author shiner
 * @since 2025/6/13
 *
 */
@Data
public class DeepSeekResponse {

    /*
     * {
     *     "id": "c60f7879-d486-4739-848a-8952eba4d675",
     *     "object": "chat.completion",
     *     "created": 1749805103,
     *     "model": "deepseek-chat",
     *     "choices": [
     *         {
     *             "index": 0,
     *             "message": {
     *                 "role": "assistant",
     *                 "content": "。。。。。。。。"
     *             },
     *             "logprobs": null,
     *             "finish_reason": "stop"
     *         }
     *     ],
     *     "usage": {
     *         "prompt_tokens": 244,
     *         "completion_tokens": 525,
     *         "total_tokens": 769,
     *         "prompt_tokens_details": {
     *             "cached_tokens": 0
     *         },
     *         "prompt_cache_hit_tokens": 0,
     *         "prompt_cache_miss_tokens": 244
     *     },
     *     "system_fingerprint": "fp_8802369eaa_prod0425fp8"
     * }
     */

    /**
     * 该对话的唯一标识符。
     */
    private String id;
    /**
     * Possible values: [chat.completion]
     *
     * 对象的类型, 其值为 chat.completion。
     */
    private String object;
    /**
     * 创建聊天完成时的 Unix 时间戳（以秒为单位）。
     */
    private Integer created;
    /**
     * 生成该 completion 的模型名。
     */
    private String model;
    /**
     * 模型生成的 completion 的选择列表。
     */
    private List<Choice> choices;

    /**
     * 该对话补全请求的用量信息。
     */
    private Usage usage;

    /**
     * 系统指纹(模型运行时用户的配置)
     */
    private String system_fingerprint;

    @Data
    public static class Choice{
        /**
         * 该 completion 在模型生成的 completion 的选择列表中的索引。
         */
        private String index;
        /**
         * 模型生成的 completion 消息。
         */
        private DeepSeekMessage message;
        /**
         * 该 choice 的对数概率信息。
         */
        private String logprobs;
        /**
         * Possible values: [stop, length, content_filter, tool_calls, insufficient_system_resource]
         *
         * 模型停止生成 token 的原因。
         *
         * stop：模型自然停止生成，或遇到 stop 序列中列出的字符串。
         *
         * length ：输出长度达到了模型上下文长度限制，或达到了 max_tokens 的限制。
         *
         * content_filter：输出内容因触发过滤策略而被过滤。
         *
         * insufficient_system_resource：系统推理资源不足，生成被打断。
         */
        private String finish_reason;

    }

    @Data
    public static class Usage{
        /**
         * 用户 prompt 所包含的 token 数。该值等于 prompt_cache_hit_tokens + prompt_cache_miss_tokens
         */
        private Integer prompt_tokens;
        /**
         * 模型 completion 产生的 token 数。
         */
        private Integer completion_tokens;
        /**
         * 该请求中，所有 token 的数量（prompt + completion）。
         */
        private Integer total_tokens;
        /**
         * completion tokens 的详细信息。
         */
        private List<PromptTokensDetail> prompt_tokens_details;
        /**
         * 用户 prompt 中，命中上下文缓存的 token 数。
         */
        private Integer prompt_cache_hit_tokens;
        /**
         * 用户 prompt 中，未命中上下文缓存的 token 数。
         */
        private Integer prompt_cache_miss_tokens;

        @Data
        public static class PromptTokensDetail{
            /**
             * 推理模型所产生的思维链 token 数量
             */
            private Integer cached_tokens;
        }

    }


}
