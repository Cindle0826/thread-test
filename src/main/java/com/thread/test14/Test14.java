package com.thread.test14;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * AQS AbstractQueuedSynchronizer 是阻塞式鎖和相關的同步器工具的框架
 */
@Slf4j(topic = "c.Test14")
public class Test14 {
    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(() -> {
            lock.lock();
            try {
                log.debug("locking ...");
            } finally {
                log.debug("unlocking ...");
                lock.unlock();
            }
        }).start();

        new Thread(() -> {
            lock.lock();
            try {
                log.debug("locking ...");
            } finally {
                log.debug("unlocking ...");
                lock.unlock();
            }
        }).start();
    }
}

class MyLock implements Lock {

    // 獨佔鎖
    class MySync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, 1)) {
                // 加鎖成功, 並設置 owner 為當前執行緒
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // 為了保持有緒性
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        // 是否持有獨佔鎖
        @Override
        protected boolean tryReleaseShared(int arg) {
            return getState() == 1;
        }

        public Condition newCondition() {
            return new ConditionObject();
        }
    }

    private MySync sync = new MySync();

    // 加鎖 (不成功會進入等待)
    @Override
    public void lock() {
        sync.acquire(1);
    }

    // 加鎖, 可打斷
    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    // 嘗試加鎖 (一次)
    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
