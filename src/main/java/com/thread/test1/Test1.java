package com.thread.test1;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class Test1 {
    static final int THREAD_NUMBER = 2;
    static final int LOOP_NUMBER = 200;

    public static void main(String[] args) {
        log.debug("{}", "start");
        ThreadUnSafe tu = new ThreadUnSafe();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            new Thread(() -> {
                tu.method1(LOOP_NUMBER);
            }, "Thread-" + THREAD_NUMBER).start();
        }
        log.info("{}", "end");
    }
}

class ThreadUnSafe {

    ArrayList<String> list = new ArrayList<>();

    public void method1(long loopNumber) {
        for (int i = 0; i < loopNumber; i++) {
            method2();
            method3();
        }
    }

    private void method2() {
        list.add("1");
    }

    private void method3() {
        list.remove(0);
    }
}
