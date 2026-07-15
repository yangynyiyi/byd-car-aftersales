package com.byd.aftersales.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class IdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private IdGenerator() {
    }

    public static String generate(String prefix) {
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return prefix + LocalDateTime.now().format(FORMATTER) + random;
    }
}
