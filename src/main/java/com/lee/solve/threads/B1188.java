package com.lee.solve.threads;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @title: 设计有限阻塞队列
 * @Date: 2023-03-20 11:48
 * @Description: 实现一个拥有如下方法的线程安全有限阻塞队列：
 * <p>
 * BoundedBlockingQueue(int capacity) 构造方法初始化队列，其中capacity代表队列长度上限。
 * void enqueue(int element) 在队首增加一个element. 如果队列满，调用线程被阻塞直到队列非满。
 * int dequeue() 返回队尾元素并从队列中将其删除. 如果队列为空，调用线程被阻塞直到队列非空。
 * int size() 返回当前队列元素个数。
 * 你的实现将会被多线程同时访问进行测试。每一个线程要么是一个只调用enqueue方法的生产者线程，要么是一个只调用dequeue方法的消费者线程。size方法将会在每一个测试用例之后进行调用。
 * <p>
 * 请不要使用内置的有限阻塞队列实现，否则面试将不会通过。
 * <p>
 * <p>
 * <p>
 * 示例 1:
 * <p>
 * 输入:
 * 1
 * 1
 * ["BoundedBlockingQueue","enqueue","dequeue","dequeue","enqueue","enqueue","enqueue","enqueue","dequeue"]
 * [[2],[1],[],[],[0],[2],[3],[4],[]]
 * <p>
 * 输出:
 * [1,0,2,2]
 * <p>
 * 解释:
 * 生产者线程数目 = 1
 * 消费者线程数目 = 1
 * <p>
 * BoundedBlockingQueue queue = new BoundedBlockingQueue(2);   // 使用capacity = 2初始化队列。
 * <p>
 * queue.enqueue(1);   // 生产者线程将 1 插入队列。
 * queue.dequeue();    // 消费者线程调用 dequeue 并返回 1 。
 * queue.dequeue();    // 由于队列为空，消费者线程被阻塞。
 * queue.enqueue(0);   // 生产者线程将 0 插入队列。消费者线程被解除阻塞同时将 0 弹出队列并返回。
 * queue.enqueue(2);   // 生产者线程将 2 插入队列。
 * queue.enqueue(3);   // 生产者线程将 3 插入队列。
 * queue.enqueue(4);   // 生产者线程由于队列长度已达到上限 2 而被阻塞。
 * queue.dequeue();    // 消费者线程将 2 从队列弹出并返回。生产者线程解除阻塞同时将4插入队列。
 * queue.size();       // 队列中还有 2 个元素。size()方法在每组测试用例最后调用。
 * <p>
 * <p>
 * 示例 2:
 * <p>
 * 输入:
 * 3
 * 4
 * ["BoundedBlockingQueue","enqueue","enqueue","enqueue","dequeue","dequeue","dequeue","enqueue"]
 * [[3],[1],[0],[2],[],[],[],[3]]
 * <p>
 * 输出:
 * [1,0,2,1]
 * <p>
 * 解释:
 * 生产者线程数目 = 3
 * 消费者线程数目 = 4
 * <p>
 * BoundedBlockingQueue queue = new BoundedBlockingQueue(3);   // 使用capacity = 3初始化队列。
 * <p>
 * queue.enqueue(1);   // 生产者线程 P1 将 1 插入队列。
 * queue.enqueue(0);   // 生产者线程 P2 将 0 插入队列。
 * queue.enqueue(2);   // 生产者线程 P3 将2插入队列。
 * queue.dequeue();    // 消费者线程 C1 调用 dequeue。
 * queue.dequeue();    // 消费者线程 C2 调用 dequeue。
 * queue.dequeue();    // 消费者线程 C3 调用 dequeue。
 * queue.enqueue(3);   // 其中一个生产者线程将3插入队列。
 * queue.size();       // 队列中还有 1 个元素。
 * <p>
 * 由于生产者/消费者线程的数目可能大于 1 ，我们并不知道线程如何被操作系统调度，即使输入看上去隐含了顺序。因此任意一种输出[1,0,2]或[1,2,0]或[0,1,2]或[0,2,1]或[2,0,1]或[2,1,0]都可被接受。
 * <p>
 * <p>
 * 提示:
 * <p>
 * 1 <= Number of Prdoucers <= 8
 * 1 <= Number of Consumers <= 8
 * 1 <= size <= 30
 * 0 <= element <= 20
 * enqueue的调用次数 大于等于  dequeue 的调用次数。
 * enque, deque 和 size 最多被调用 40 次
 */
public class B1188 {

    class Node {
        Node prev;
        Node next;
        int data;

        public Node(int data) {
            this.data = data;
            prev = null;
            next = null;
        }

        public Node() {
            prev = null;
            next = null;
        }
    }

    Node head;
    Node tail;


    private volatile int capacity;
    private AtomicInteger en_num = new AtomicInteger(0);
    private AtomicInteger de_num = new AtomicInteger(0);
    private AtomicInteger num = new AtomicInteger(0);
    Semaphore en = new Semaphore(0);
    Semaphore dl = new Semaphore(0);

    public B1188(int capacity) {
        this.capacity = capacity;
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.prev = head;
    }

    Object o = new Object();

    public void enqueue(int element) throws InterruptedException {
        while (num.get() >= capacity) {
            en_num.getAndIncrement();
            en.acquire();
        }
        Node node = new Node(element);
        synchronized (o) {
            node.next = head.next;
            head.next.prev = node;
            node.prev = head;
            head.next = node;
            if (de_num.get() > 0) {
                dl.release();
            }
            num.getAndIncrement();
        }


    }

    public int dequeue() throws InterruptedException {
        while (num.get() <= 0) {
            de_num.getAndIncrement();
            dl.acquire();
        }
        int a = tail.prev.data;
        // tail.prev.prev.next = tail;
        // tail.prev.prev = tail.prev;
        synchronized (o) {
            tail = tail.prev;
            tail.next = null;
            tail.data = 0;
            if (en_num.get() > 0) {
                en.release();
            }
            num.getAndDecrement();
        }


        return a;
    }

    public int size() {
        if (num.get() >= capacity) {
            return capacity;
        } else if (num.get() <= 0) {
            return 0;
        } else {
            return num.get();
        }

    }
}
