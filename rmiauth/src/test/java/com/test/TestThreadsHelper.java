
package com.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Вспомогательный объект для создания потоков для тестов JUnit.<br>
 * <br>
 * Сохраняет возникшие исключения, ведет подсчет {@link CountDownLatch} по завершении
 * работы потоков и имеет возможность переключения на последовательное исполнение
 * потоков.
 *
 * @author Sergey Ponomarev<sergey.ponomarev@vistar.su>
 */
public abstract class TestThreadsHelper {

    /** Счетчик завершения работы потоков. */
    private final CountDownLatch cdLatch;
    /** Должны ли потоки выполняться последовательно один за другим. */
    private final boolean bSequential;
    /** Lock, который осуществляет последовательное выполнение потоков, если оно включено. */
    final ReentrantLock sequentialLock;
    /** Рабочий поток. */
    private final Thread[] threads;
    /** Сгенерированное в процессе работы потока исключение или null, если исключений не было. */
    private final AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();

    /**
     * Конструктор. <br>
     * <br>
     * Вызывает <tt>TestThreadHelper(numThreads, false)</tt>.
     *
     * @param numThreads Количество потоков.
     */
    public TestThreadsHelper(int numThreads) {
        this(numThreads, false);
    }

    /**
     * Конструктор. Создает объект, работающий в режиме последовательного выполнения
     * потоков вместе с другим объектом {@link TestThreadsHelper}.
     *
     * @param numThreads Количество потоков.
     * @param bSequential Должны ли потоки выполняться последовательно один за другим.
     * @param sequentialShare Объект, с потоками которого необходимо синхронизировать
     *      работу своих потоков при последовательном выполнении.
     */
    public TestThreadsHelper(int numThreads, boolean bSequential,
            TestThreadsHelper sequentialShare) {
        this(numThreads, bSequential, sequentialShare.sequentialLock);
    }

    /**
     * Конструктор. <br>
     * <br>
     *
     * @param numThreads Количество потоков.
     * @param bSequential Должны ли потоки выполняться последовательно один за другим.
     */
    public TestThreadsHelper(int numThreads, boolean bSequential) {
        this(numThreads, bSequential, new ReentrantLock(true));
    }

    /**
     * Приватный конструктор, вызывается всеми остальными.
     *
     * @param numThreads Количество потоков.
     * @param bSequential Должны ли потоки выполняться последовательно один за другим.
     * @param sequentialLock Блокировка последовательного выполенния потоков.
     */
    private TestThreadsHelper(int numThreads, boolean bSequential, ReentrantLock sequentialLock) {
        if (numThreads < 0) {
            throw new IllegalArgumentException("numThreads = " + numThreads);
        }
        if (sequentialLock == null) {
            throw new NullPointerException("sequentialLock");
        }

        this.bSequential = bSequential;
        this.sequentialLock = sequentialLock;
        this.cdLatch = new CountDownLatch(numThreads);
        this.threads = new Thread[numThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new TestThreadRunnable(i));
        }
    }

    /** Запускает потоки. */
    public final TestThreadsHelper startThreads() {
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        return this;
    }

    /**
     * @return Массив рабочих потоков, никогда не null.
     */
    public final Thread[] getThreads() {
        return threads;
    }

    /**
     * Проверяет, было ли в процессе работы потоков сгенерировано исключение,
     * и если да, то генерирует его.
     *
     * @throws Throwable Если в процессе работы потоков сгенерировано исключение,
     *      оно будет сгенерировано в вызывающем потоке еще раз.
     */
    public final TestThreadsHelper checkThrowables() throws Throwable { // CHECKSTYLE.LINE.OFF
        Throwable t = throwable.get();
        if (t != null) {
            throw t;
        }
        return this;
    }

    /**
     * Ждет завершения работы потоков.
     *
     * @throws InterruptedException В случае если текущий поток был прерван.
     */
    public final TestThreadsHelper awaitThreads() throws InterruptedException {
        cdLatch.await();
        return this;
    }

    /**
     * Рабочий метод потока, аналог {@link Runnable#run()}.
     *
     * @param nThread Номер потока. Нумерация начинается с 0.
     *
     * @throws Exception .
     */
    protected abstract void run(int nThread) throws Exception;

    /**
     * {@link Runnable}, который выполняет вспомогательные действия для обеспечения
     * функциональности объекта.
     */
    private class TestThreadRunnable implements Runnable {

        /** Номер потока, нумерация начинается с 0. */
        private final int nThread;

        /**
         * Конструктор.
         *
         * @param nThread Номер потока, нумерация начинается с 0.
         */
        public TestThreadRunnable(int nThread) {
            this.nThread = nThread;
        }

        @Override
        public void run() {
            if (bSequential) {
                sequentialLock.lock();
            }

            try {
                TestThreadsHelper.this.run(nThread);
            } catch (Throwable t) {
                Logger.getLogger(TestThreadsHelper.class.getName()).log(
                        Level.WARNING, "Throwable catched in thread " + Thread.currentThread(),
                        t);
                throwable.compareAndSet(null, t);
            } finally {
                if (bSequential) {
                    sequentialLock.unlock();
                }
            }

            cdLatch.countDown();
        }
    }
}
