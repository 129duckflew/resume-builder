package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.User;
import com.resume.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final UserRepository userRepository;
    private final HttpClient httpClient;

    public AiService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String rewrite(Resume resume, Long userId, String instruction) {
        String apiKey = getApiKey(userId);
        String prompt = """
                You are a professional resume editor. Rewrite the following resume section
                according to the instruction. Keep it concise and factual.
                Return ONLY the rewritten content, no explanation.

                Instruction: %s

                Content:
                %s
                """.formatted(instruction, resume.getContent());

        return callLlm(apiKey, prompt);
    }

    public String suggest(Resume resume, Long userId, String jobDescription) {
        String apiKey = getApiKey(userId);
        String prompt = """
                Based on this resume content, suggest improvements tailored to the
                following job description. Focus on matching keywords and highlighting
                relevant experience. Return suggestions as bullet points.

                Job Description:
                %s

                Resume:
                %s
                """.formatted(jobDescription, resume.getContent());

        return callLlm(apiKey, prompt);
    }

    private String getApiKey(Long userId) {
        return userRepository.findById(userId)
                .map(User::getApiKey)
                .filter(k -> !k.isEmpty())
                .orElseThrow(() -> new RuntimeException("API key not configured. Set it in Settings."));
    }

    private String callLlm(String apiKey, String prompt) {
        try {
            String body = """
                    {
                        "model": "gpt-4o-mini",
                        "messages": [{"role": "user", "content": %s}],
                        "temperature": 0.7,
                        "max_tokens": 1024
                    }
                    """.formatted(escapeJson(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("LLM API returned {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("AI service error: " + response.statusCode());
            }

            var json = objectMapper.readTree(response.body());
            return json.get("choices").get(0).get("message").get("content").asText();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("AI service call failed: " + e.getMessage());
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String escapeJson(String s) {
        try {
            return objectMapper.writeValueAsString(s);
        } catch (Exception e) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
        }
    }
}
