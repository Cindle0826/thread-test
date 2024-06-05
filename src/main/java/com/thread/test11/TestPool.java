package com.thread.test11;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.TestPool")
public class TestPool {
    public static void main(String[] args) {
        /*
            為什麼執行 take 方法結束後執行緒會等待，而 poll 方法則不會繼續等待，
            因為 take 方法是阻塞的，如果沒有獲取到任務會一直等待，而 poll 方法是帶超時的，如果超時了就會返回 null
         */
        ThreadPool threadPool = new ThreadPool(1,
                1000, TimeUnit.MILLISECONDS, 1,
                (queue, task) -> {
                    // 1. 死等
//                    queue.put(task);
                    // 2. 帶超時等待
//                    queue.offer(task, 1500, TimeUnit.MILLISECONDS);
                    // 3. 放棄任務執行
//                    log.debug("放棄 {}", task);
                    // 4. 讓調用者拋出異常
//                    throw new RuntimeException("任務執行失敗" + task);
                    // 5. 讓調用者自己執行任務
                    task.run();
                }
        );
        for (int i = 0; i < 4; i++) {
            int j = i;
            threadPool.execute(() -> {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.debug("{}", j);
            });
        }

    }
}

@FunctionalInterface
interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}

@Slf4j(topic = "c.ThreadPool")
class ThreadPool {
    // 任務隊列
    private BlockingQueue<Runnable> taskQueue;

    // 執行緒集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心執行緒數量
    private int coreSize;

    // 獲取任務的超時時間
    private long timeout;

    private TimeUnit timeUnit;

    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int queueCapacity, RejectPolicy<Runnable> rejectPolicy ) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockingQueue<>(queueCapacity);
        this.rejectPolicy = rejectPolicy;
    }

    /**
     * 當執行的時候發現在 work 的執行緒數量小於核心數量就去新增執行緒，反之將任務交給 taskQueue 隊列
     * @param task
     */
    public void execute(Runnable task) {
        // 當任務沒有超過 coreSize 時，直接交給 worker 執行
        // 如果任務數超過 coreSize 時，交給 taskQueue 隊列
        synchronized (workers) {
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                log.debug("新增 worker {}, task {}", worker, task);
                workers.add(worker);
                worker.start();
            } else {
                taskQueue.put(task);
                // 隊列滿的時候
                // 1. 死等
                // 2. 帶超時等待
                // 3. 放棄任務執行
                // 4. 讓調用者拋出異常
                // 5. 讓調用者自己執行任務
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            // 執行任務
            // 1. 當 task 不為空, 執行任務
            // 2. 當 task 執行完畢, 再接著從任務隊列獲取任務並執行
            while (task != null || (task = taskQueue.poll(timeout, timeUnit)) != null) {
                try {
                    log.debug("正在執行...{}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (workers) {
                log.debug("移除 worker{}", this);
                workers.remove(this);
            }
        }
    }
}


@Slf4j(topic = "c.BlockingQueue")
class BlockingQueue<T> {

    // 1. 任務隊列
    private Deque<T> queue = new ArrayDeque<>();

    // 2. 鎖
    private ReentrantLock lock = new ReentrantLock();

    // 3. 生產者條件變量
    private Condition fullWaitSet = lock.newCondition();

    // 4. 生產者條件變量
    private Condition emptyWaitSet = lock.newCondition();

    // 5. 容量
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // 帶超時的阻塞隊列
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 將超時時間轉換為納秒
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    // 返回剩餘時間
                    if (nanos <= 0) {
                        return null;
                    }
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                    log.error("error : {} ", e.getMessage());
                }
            }
            T t = queue.removeFirst();
            // 唤醒生產者
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞獲取，如果一直等待可能會一直阻塞，所以必須要一個超時的時候獲取 poll 方法
    public T take() {
        lock.lock();
        try {
            // 如果隊列他是空的時候就必須要進行阻塞
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 元素獲取完後從隊列移除
            T t = queue.removeFirst();
            return t;
        } finally {
            // 確保無論如何都能釋放鎖，不然或造成其他人無法進入鎖
            lock.unlock();
        }
    }

    // 帶超時時間阻塞添加
    public boolean offer(T t, long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            // 如果隊列滿了時就必須要進行阻塞
            while (queue.size() == capacity) {
                try {
                    log.debug("等待任務加入到隊列中 {}", t);
                    if (nanos <= 0) {
                        return false;
                    }
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任務到隊列中 {}", t);
            // 元素添加到隊列
            queue.addLast(t);
            // 唤醒消費者
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞添加
    public void put(T t) {
        lock.lock();
        try {
            // 如果隊列滿了時就必須要進行阻塞
            while (queue.size() == capacity) {
                try {
                    log.debug("等待任務加入到隊列中 {}", t);
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任務到隊列中 {}", t);
            // 元素添加到隊列
            queue.addLast(t);
            // 唤醒消費者
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            // 判斷隊列是否滿了
            if (queue.size() == capacity) {
                rejectPolicy.reject(this, task);
            } else { // 有空閒
                log.debug("加入任務隊列 {}", task);
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
