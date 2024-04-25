package com.thread.test3;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j()
public class Test3 {
    final static Object lock = new Object();
    private static boolean isSmoke = false;
    private static boolean isRest = false;

    public static void main(String[] args) {
        new Thread(() -> {
            log.debug("有菸了嗎 ? {} ", isSmoke);
            synchronized (lock) {
                while (!isSmoke) {
                    log.debug("休息一下 ~");
                    try {
//                        lock.wait(1000);
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (isSmoke) {
                log.debug("可以工作了");
            } else {
                log.debug("好的我繼續休息");
            }
        }).start();

        new Thread(() -> {
            synchronized (lock) {
                if (!isRest) {
                    log.debug("還不能休息");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (isRest) {
                log.debug("可以休息了");
            } else {
                log.debug("還不能休息");
            }
        }).start();

        for (int i = 0; i < 5; i++) {
            synchronized (lock) {
                new Thread(() -> {
                    log.debug("工作中 Thread");
                }).start();
            }
        }

        new Thread(() -> {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (lock) {
                isRest = true;
                log.debug("可以開始工作了");
                lock.notifyAll();
            }
        }).start();
    }
}


