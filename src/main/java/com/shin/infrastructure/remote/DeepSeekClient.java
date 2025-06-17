package com.shin.infrastructure.remote;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.shin.infrastructure.entity.deepSeek.DeepSeekMessage;
import com.shin.infrastructure.entity.deepSeek.DeepSeekRequest;
import com.shin.infrastructure.entity.deepSeek.DeepSeekResponse;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shiner
 * @since 2025/6/13
 */
@Component
public class DeepSeekClient {

    @Autowired
    private WebClient webClient;
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String CHAR_URL = "/chat/completions";
    private static final String model = "deepseek-chat";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String promptEn = "You are an expert content creator deeply familiar with the Xiaohongshu (RED) platform's style. Craft compelling content aligned with the platform's best practices based on user requirements. Focus intensely on the core value and unique highlights of the product/experience. Use authentic, relatable, and highly persuasive storytelling approaches (e.g., personal experience narratives, scenario-based problem-solving, practical tips sharing, emotional resonance).\n" +
            "\n" +
            "Ensure content drives high engagement (encouraging comments, likes, saves) with clear structure:\n" +
            "\n" +
            "Attention-grabbing headline/opening\n" +
            "\n" +
            "Well-organized main body\n" +
            "\n" +
            "Strong call-to-action/interaction prompt\n" +
            "\n" +
            "Naturally integrate relevant trending hashtags. Adopt a warm, conversational tone as if chatting with close friends – avoid overly formal language or traditional ad vibes. Enhance appeal with strategic emoji use \uD83D\uDE0D and expressive phrasing (e.g., \"OMG!\", \"Seriously game-changing!\").\n" +
            "\n" +
            "Ultimate goal: Spark target audience interest, create lasting impact, and motivate action (e.g., clicks, purchases, trials).";
    private final String promptCn = "你是一位深谙小红书平台调性的内容创作专家。请根据用户需求，创作一篇符合小红书风格的内容。内容需聚焦产品/体验的核心价值和独特亮点，采用真实、生活化且极具种草力的表达方式（如个人体验故事、场景化痛点解决、干货分享、情感共鸣等）。确保内容具备高互动性（可引发评论、点赞、收藏），结构清晰（包含吸睛标题/开头、主体内容、互动引导），并自然融入相关热门话题标签。语言风格需亲切自然，如同闺蜜分享，避免过度书面化和硬广感，鼓励使用emoji、感叹词增强感染力。最终内容需能有效激发目标用户兴趣和行动欲望，并留下深刻印象。最主要的是，使用中文回答并按照markdown格式返回。";

    public String getResponse(String apiKey, String questionStr) throws IOException {
        List<DeepSeekMessage> messages = new ArrayList<>();
        // 身份
        DeepSeekMessage messageSystem = DeepSeekMessage.builder()
                .role("system")
                .content(promptEn).build();
        // 问题
        DeepSeekMessage messageUser = DeepSeekMessage.builder()
                .role("user")
                .content(questionStr).build();
        // 填充数据
        messages.add(messageSystem);
        messages.add(messageUser);
        DeepSeekRequest requestBody = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .build();
        String requestJsonStr = gson.toJson(requestBody);

        // 创建HTTP请求
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(mediaType, requestJsonStr))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        // 发送请求并处理响应
        OkHttpClient httpClient = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String string = response.body().string();
            DeepSeekResponse deepSeekResponse = JSONObject.parseObject(string, new TypeReference<DeepSeekResponse>(){});
            return deepSeekResponse.getChoices().get(0).getMessage().getContent();
        }
    }

    public void getResponseStream(String apiKey, String questionStr, SseEmitter emitter) {
        List<DeepSeekMessage> messages = new ArrayList<>();
        // 身份
        DeepSeekMessage messageSystem = DeepSeekMessage.builder()
                .role("system")
                .content(promptCn).build();
        // 问题
        DeepSeekMessage messageUser = DeepSeekMessage.builder()
                .role("user")
                .content(questionStr).build();
        // 填充数据
        messages.add(messageSystem);
        messages.add(messageUser);
        DeepSeekRequest requestBody = DeepSeekRequest.builder()
                .model(model)
                .messages(messages)
                .stream(true)
                .build();
        StringBuilder completeResponse = new StringBuilder();
        webClient.post()
                .uri(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .body(BodyInserters.fromValue(requestBody))
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    System.out.println(chunk);
                    try {
                        if (chunk.contains("[DONE]")) {
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            emitter.complete();
//                        } else if (chunk.startsWith("data:")) {
                        } else {
                            String json = chunk.replaceFirst("data: *", "");
                            String token = parseTokenFromJson(json);
                            completeResponse.append(token);
                            emitter.send(SseEmitter.event().data(token));
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(() -> {
                    System.out.println(completeResponse.toString());
                })
                .doOnError(emitter::completeWithError)
                .subscribe();
    }

    private String parseTokenFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            return node.path("choices").get(0).path("delta").path("content").asText("");
        } catch (Exception e) {
            return "";
        }
    }
}
