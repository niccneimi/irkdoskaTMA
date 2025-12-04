package com.irkdoska.irkdoska.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.irkdoska.irkdoska.exception.ExpiredTmaException;

@Component
public class TmaCore {

    private static final Logger logger = LoggerFactory.getLogger(TmaCore.class);
    private static final Set<Long> ADMIN_IDS = Set.of(718802381L, 7724264827L, 1899914568L);

    public static class HmacSha256 {
        public static byte[] hmacSha256(String data, String key) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes("UTF-8"));
            return hmacBytes;
        }

        public static byte[] hmacSha256KeyBytes(String data, byte[] key) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes("UTF-8"));
            return hmacBytes;
        }

        public static String bytesToHex(byte[] hmacBytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    public TmaUserPrincipal getPrincipalFromTma(String initRawData) throws Exception {
        String urlDecodedInitRawData = URLDecoder.decode(initRawData, StandardCharsets.UTF_8);
        String userJsonString = "";
        for (String item : urlDecodedInitRawData.split("&")) {
            if (item.split("=")[0].equals("user")) {
                userJsonString = item.split("=")[1];
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(userJsonString);
        Long telegramId = rootNode.get("id").asLong();
        String firstName = rootNode.get("first_name").asText("");
        String lastName = rootNode.get("last_name").asText("");
        JsonNode usernameNode = rootNode.get("username");
        String username = (usernameNode != null) ? usernameNode.asText("") : "none_username";

        String role = "USER";
        if (ADMIN_IDS.contains(telegramId)) {
            role = "ADMIN";
        }

        TmaUserPrincipal principal = new TmaUserPrincipal(initRawData, telegramId, firstName, lastName, username, role);
        return principal;
    }

    public boolean validate(String tgBotToken, String initRawData) throws Exception {
        String urlDecodedInitRawData = URLDecoder.decode(initRawData, StandardCharsets.UTF_8);
        HashMap<String, String> pairs = new HashMap<>();
        String hash = "";
        for (String item : urlDecodedInitRawData.split("&")) {
            int idx = item.indexOf('=');
            if (idx == -1)
                continue;

            String key = item.substring(0, idx);
            String value = item.substring(idx + 1);

            if ("hash".equals(key)) {
                hash = value;
            } else {
                pairs.put(key, value);

                if ("auth_date".equals(key)) {
                    try {
                        long authDate = Long.parseLong(value);
                        if (authDate + 3600 < Instant.now().getEpochSecond()) {
                            throw new ExpiredTmaException("Token expired", initRawData);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Invalid auth_date format", e);
                    }
                }
            }
        }

        Map<String, String> sortedByKey = new TreeMap<>(pairs);
        String result = sortedByKey.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
        byte[] firstSugnature = HmacSha256.hmacSha256(tgBotToken, "WebAppData");
        String secondSugnature = HmacSha256.bytesToHex(HmacSha256.hmacSha256KeyBytes(result, firstSugnature));
        if (hash.equals(secondSugnature)) {
            return true;
        }
        return false;
    }

}
