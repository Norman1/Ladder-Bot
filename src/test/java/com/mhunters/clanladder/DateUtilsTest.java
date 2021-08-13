package com.mhunters.clanladder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class DateUtilsTest {

    @Test
    void parseDateTest() {
        LocalDateTime date = DateUtils.parseDate("12/30/2021 18:00:01");
        String outString = DateUtils.format(date);
        System.out.println(outString);

    }
}
