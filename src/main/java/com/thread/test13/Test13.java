package com.thread.test13;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j(topic = "c.Test13")
public class Test13 {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool(4);
        System.out.println(pool.invoke(new MyTask(5)));
    }
}

@Slf4j(topic = "c.MyTask")
class MyTask extends RecursiveTask<Integer> {
    private final int n;

    public MyTask(int n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "MyTask{" +
                "n=" + n +
                '}';
    }

    @Override
    protected Integer compute() {
        // 終止條件
        if (n == 1) {
            log.debug("join() {}", n);
            return 1;
        }



        MyTask t1 = new MyTask(n - 1);
        t1.fork();
        log.debug("fork() {} + {}", n, t1);

        int result = n + t1.join();
        log.debug("fork() {} + {} = {}", n, t1, result);

        return  result;
    }
}
