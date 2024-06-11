package com.thread.test12;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j(topic = "c.test")
public class TestScheduleExecutors {
    // 如何讓每周四 18:00:00 定時執行任務?
    public static void main(String[] args) {
        // 獲取當前時間
        LocalDateTime now = LocalDateTime.now();
        log.info(now.toString());
        // 獲取週四時間
        LocalDateTime time = now.withHour(18).withMinute(0).withSecond(0).withNano(0).with(DayOfWeek.THURSDAY);
        // 如果 當前時間 > 本周周四, 必須找到下周周四
        if (now.compareTo(time) > 0) {
            time = time.plusWeeks(1);
        }
        log.info(time.toString());
        long initialDelay = Duration.between(now, time).toMillis();
        long period = 1000 * 60 * 60 * 24 * 7;
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.scheduleAtFixedRate(() -> {
            log.info("running...");
        }, initialDelay, period, java.util.concurrent.TimeUnit.MILLISECONDS);

    }
}
