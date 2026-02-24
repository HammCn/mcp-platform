package com.mcp.common.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID 生成工具类
 * 支持生成多种类型的 ID：UUID、雪花 ID、业务 ID 等
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public final class IdUtil {

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final long SEQUENCE_MASK = 0x0000000000000FFF; // 12 bits
    private static final long MAX_SEQUENCE = 4095L;
    
    private static final long START_TIMESTAMP = 1704067200000L; // 2024-01-01 00:00:00
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    private static final long workerId;
    private static final long datacenterId;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    static {
        workerId = getWorkerId();
        datacenterId = getDatacenterId();
    }

    private IdUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 生成 UUID（不带横杠）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID（带横杠）
     */
    public static String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成雪花 ID
     */
    public static synchronized long snowflakeId() {
        long timestamp = System.currentTimeMillis();
        
        if (timestamp < START_TIMESTAMP) {
            throw new RuntimeException("时钟回拨异常");
        }
        
        if (timestamp == getLastTimestamp()) {
            long sequence = (SEQUENCE.getAndIncrement() & SEQUENCE_MASK);
            if (sequence == 0) {
                timestamp = tilNextMillis(getLastTimestamp());
            }
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT) |
                    (datacenterId << DATACENTER_ID_SHIFT) |
                    (workerId << WORKER_ID_SHIFT) |
                    sequence;
        } else {
            SEQUENCE.set(0);
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT) |
                    (datacenterId << DATACENTER_ID_SHIFT) |
                    (workerId << WORKER_ID_SHIFT) |
                    (SEQUENCE.getAndIncrement() & SEQUENCE_MASK);
        }
    }

    /**
     * 生成雪花 ID（字符串格式）
     */
    public static String snowflakeIdStr() {
        return String.valueOf(snowflakeId());
    }

    /**
     * 生成业务 ID（时间戳 + 随机数）
     */
    public static String businessId(String prefix) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        long seq = SEQUENCE.getAndIncrement() % 10000;
        return String.format("%s%s%04d", prefix, timestamp, seq);
    }

    /**
     * 生成 API Key
     */
    public static String generateApiKey() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
        return "sk_" + randomPart;
    }

    /**
     * 生成请求 ID
     */
    public static String generateRequestId() {
        return "req_" + uuid().substring(0, 16);
    }

    /**
     * 生成短期 Token
     */
    public static String generateShortToken() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 生成随机字符串
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static long getLastTimestamp() {
        return START_TIMESTAMP;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static long getWorkerId() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] addr = address.getAddress();
            return (addr[addr.length - 1] & 0x1F);
        } catch (UnknownHostException e) {
            return ThreadLocalRandom.current().nextInt(32);
        }
    }

    private static long getDatacenterId() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] addr = address.getAddress();
            return ((addr.length > 1 ? addr[addr.length - 2] : 0) & 0x1F);
        } catch (UnknownHostException e) {
            return ThreadLocalRandom.current().nextInt(32);
        }
    }
}
