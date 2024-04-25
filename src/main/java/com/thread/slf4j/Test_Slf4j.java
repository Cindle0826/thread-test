package com.thread.slf4j;

import lombok.extern.slf4j.Slf4j;

//@Slf4j(topic = "Test123")
@Slf4j
public class Test_Slf4j {
    public static void main(String[] args) {
        log.info("Hello1,{},{}", 123, 456);
        log.error("Hello2, {}", 123);
        log.warn("Hello3");
        log.debug("Hello4");
        log.trace("Hello5");
        log.error("error exception", new RuntimeException("test error"));
    }
}
