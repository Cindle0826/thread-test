package com.thread.test6;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "test6")
public class Test6 {
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;
    static ReentrantLock ROOM = new ReentrantLock();

    static Condition waitCigarette = ROOM.newCondition();
    static Condition waitTakeoutSet = ROOM.newCondition();

    public static void main(String[] args) {
        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("有菸嗎? {}", hasCigarette);
                while (!hasCigarette) {
                    log.debug("沒有菸，那我先休息一下 !");
                    try {
                        waitCigarette.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                log.debug("可以開始工作了");
            } finally {
                ROOM.unlock();
            }
        }, "yun").start();

        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("外賣來了嗎? {}", hasTakeout);
                while (!hasTakeout) {
                    log.debug("沒外賣，那我先休息一下 !");
                    try {
                        waitTakeoutSet.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                log.debug("可以開始工作了");
            } finally {
                ROOM.unlock();
            }
        }, "fr").start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("外賣到了，叫醒!");
                hasTakeout = true;
                waitTakeoutSet.signal();
            } finally {
                ROOM.unlock();
            }
        }, "送外賣的").start();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("菸到了，叫醒!");
                hasCigarette = true;
                waitCigarette.signal();
            } finally {
                ROOM.unlock();
            }
        }, "送菸的").start();
    }
}
