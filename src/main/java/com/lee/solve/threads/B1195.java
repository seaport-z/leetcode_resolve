package com.lee.solve.threads;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * @title: 交替打印字符串
 * @Date: 2023-03-20 11:48
 * @Description:
 * 编写一个可以从 1 到 n 输出代表这个数字的字符串的程序，但是：
 *
 * 如果这个数字可以被 3 整除，输出 "fizz"。
 * 如果这个数字可以被 5 整除，输出 "buzz"。
 * 如果这个数字可以同时被 3 和 5 整除，输出 "fizzbuzz"。
 * 例如，当 n = 15，输出： 1, 2, fizz, 4, buzz, fizz, 7, 8, fizz, buzz, 11, fizz, 13, 14, fizzbuzz。
 *
 * 假设有这么一个类：
 *
 * class FizzBuzz {
 *   public FizzBuzz(int n) { ... }               // constructor
 *   public void fizz(printFizz) { ... }          // only output "fizz"
 *   public void buzz(printBuzz) { ... }          // only output "buzz"
 *   public void fizzbuzz(printFizzBuzz) { ... }  // only output "fizzbuzz"
 *   public void number(printNumber) { ... }      // only output the numbers
 * }
 * 请你实现一个有四个线程的多线程版  FizzBuzz， 同一个 FizzBuzz 实例会被如下四个线程使用：
 *
 * 线程A将调用 fizz() 来判断是否能被 3 整除，如果可以，则输出 fizz。
 * 线程B将调用 buzz() 来判断是否能被 5 整除，如果可以，则输出 buzz。
 * 线程C将调用 fizzbuzz() 来判断是否同时能被 3 和 5 整除，如果可以，则输出 fizzbuzz。
 * 线程D将调用 number() 来实现输出既不能被 3 整除也不能被 5 整除的数字。
 *
 *
 * 提示：
 *
 * 本题已经提供了打印字符串的相关方法，如 printFizz() 等，具体方法名请参考答题模板中的注释部分。
 */
public class B1195 {

    private AtomicInteger i = new AtomicInteger(0);
    private int n;
    private int x,y,z;
    Semaphore[] es = new Semaphore[4];


    public B1195(int n) {
        this.n = n;
        es[0] = new Semaphore(1);
        es[1] = new Semaphore(0);
        es[2] = new Semaphore(0);
        es[3] = new Semaphore(0);
        x = ((n/3)*3)%5==0?((n/3)*3)-3:((n/3)*3);
        y = ((n/5)*5)%3==0?((n/5)*5)-5:((n/5)*5);
        z = (n/15)*15;
    }
    /**
     * number 抢占，条件符合，直接输出，i++;不符合，通知fizz/buzz/fizzbuzz并等待;
     * <p>
     * fizz 抢占，直接输出，i++，并通知number继续执行
     */

    // printFizz.run() outputs "fizz".
    public void fizz(Runnable printFizz) throws InterruptedException {
        while (i.get() < x) {
            es[1].acquire();
            printFizz.run();
            es[0].release();
        }

    }

    // printBuzz.run() outputs "buzz".
    public void buzz(Runnable printBuzz) throws InterruptedException {
        while (i.get() < y) {
            es[2].acquire();
            printBuzz.run();
            es[0].release();
        }
    }

    // printFizzBuzz.run() outputs "fizzbuzz".
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        while (i.get() < z) {
            es[3].acquire();
            printFizzBuzz.run();
            es[0].release();
        }
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void number(IntConsumer printNumber) throws InterruptedException {
        while (i.get() < n && i.compareAndSet(i.get(), i.get() + 1)) {
            es[0].acquire();
            if (i.get() % 3 != 0 && i.get() % 5 != 0) {
                printNumber.accept(i.get());
                es[0].release();
            }
            if (i.get() % 3 == 0 && i.get() % 5 != 0) {
                es[1].release();
            }
            if (i.get() % 3 != 0 && i.get() % 5 == 0) {
                es[2].release();
            }
            if (i.get() % 3 == 0 && i.get() % 5 == 0) {
                es[3].release();
            }
        }
    }
}
