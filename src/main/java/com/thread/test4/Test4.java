package com.thread.test4;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Test4")
public class Test4 {
    public static void main(String[] args) {
        GuardedObject<String> go = new GuardedObject();
        new Thread(() -> {
            log.debug("等待結果 ...");
            try {
                String result = go.get(2000);
                log.debug("獲得結果 {}", result);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                log.debug("執行下載 ...");
                Thread.sleep(3000);
                log.debug("下載中 ...");
                go.complete("Hello World !");
//                go.complete(null);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "t2").start();
    }
}

/**
 * 保護性暫停 設計模式
 */
@Slf4j(topic = "GuardedObject")
class GuardedObject<T> {
    private T response = null;

    /**
     *
     * @param mills 最多等待多久
     * @return 結果
     * @throws InterruptedException exception
     */
    public synchronized T get(long mills) throws InterruptedException {
        long begin = System.currentTimeMillis(); // 0 sec
        long parseTime = 0;
        log.debug("等待中 ...");
        while (response == null) {

            if (parseTime >= mills) {
                break;
            }
            this.wait(mills - parseTime); // 需要考慮虛假喚醒，其他 Thread 提前一秒喚醒 1 sec
            parseTime = System.currentTimeMillis() - begin; // 2 sec
        }

        return response;
    }

    public synchronized void complete(T obj) {
        response = obj;
        this.notifyAll();
        log.debug("下載完畢 !");
    }
}
