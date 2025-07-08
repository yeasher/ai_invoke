package com.shin.controller;

import com.shin.infrastructure.remote.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author shiner
 * @since 2025/6/13
 */
@Slf4j
@RestController
@RequestMapping("/ai/invoke")
public class AIInvokeController {

    @Value("${ai.deepSeek.api.key}")
    private String deepSeekApiKey;
    @Autowired
    private DeepSeekClient deepSeekClient;

    @PostMapping("/deepSeek/chat")
    public String deepSeekChat(@RequestBody String questionStr) {
        String response = null;
        try {
            response = deepSeekClient.getResponse(deepSeekApiKey, questionStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @GetMapping(value = "/deepSeek/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter deepSeekChatStream(@RequestParam("prompt") String prompt, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");//跨域问题
        SseEmitter sseEmitter = new SseEmitter();
        deepSeekClient.getResponseStream1(deepSeekApiKey, prompt, sseEmitter);
        return sseEmitter;
    }

    @GetMapping(value = "/deepSeek/chat/stream2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter deepSeekChatStream2(@RequestParam("prompt") String prompt, @RequestParam("rule") String rule, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");//跨域问题
        SseEmitter sseEmitter = new SseEmitter();
        deepSeekClient.getResponseStreamBySetSystem(deepSeekApiKey, prompt, rule, sseEmitter);
        return sseEmitter;
    }


}
