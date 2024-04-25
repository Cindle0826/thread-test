package com.thread.test2;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class Test2 {
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

class ThreadSafe {

    public void method1(long loopNumber) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; i++) {
            method2(list);
            method3(list);
        }
    }

    public void method2(ArrayList list) {
        list.add("1");
    }

    public void method3(ArrayList list) {

        list.remove(0);
    }
}

/**
 * 因為子類覆寫了方法，並在方法建立一個新的 thread
 * 導致再執行的時候新的 thread 執行時間有可能比主 thread 更快
 * 解決辦法 父類別要對方法私有化 private
 */
class ThreadUnSafe extends  ThreadSafe {
    @Override
    public void method3(ArrayList list) {
        new Thread(() -> list.remove(0)).start();
    }
}
