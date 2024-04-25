package com.thread.test5;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

@Slf4j(topic = "Test5")
public class Test5 {
    public static void main(String[] args){
        MessageQueue mq = new MessageQueue(2);

        for (int i = 0; i < 3; i++) {
            int id = i;
            new Thread(() -> {
                mq.put(new Message(id, "message" + id));
            }, "producer-" + i).start();
        }

        new Thread(() -> {
            for (;;) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Message take = mq.take();
                log.info("result message {}", take);
            }
        }, "consumer").start();
    }
}

@Slf4j(topic = "MQ")
class MessageQueue {
    // 存放容器
    private final LinkedList<Message> list = new LinkedList<>();
    // 容量
    private int capcity;

    public MessageQueue(int capcity) {
        this.capcity = capcity;
    }

    /**
     * 如果對列為空就等待，取得消息後再發通知給儲存對列
     *
     * @return 消息
     */
    public Message take() {
        synchronized (list) {
            while (list.isEmpty()) {
                try {
                    log.debug("對列為空 ...");
                    list.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Message message = list.removeFirst();
            log.debug("已取出消息 {}", message);
            list.notifyAll();
            return message;
        }
    }

    /**
     * 如果對列滿了就不能儲存，如果儲存了一個就發通知獲取
     *
     * @param message 存入消息
     */
    public void put(Message message) {
        synchronized (list) {
            while (list.size() == capcity) {
                try {
                    log.debug("對列已滿");
                    list.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.debug("已生產消息 {}", message);
            list.addLast(message);
            list.notifyAll();
        }
    }

}

final class Message {
    private int id;
    private Object value;

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}
