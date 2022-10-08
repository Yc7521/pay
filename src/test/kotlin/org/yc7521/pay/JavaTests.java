package org.yc7521.pay;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

class IntWrapper {
  int value = 0;
}

@SpringBootTest
public class JavaTests {
  @Test
  void test() throws ExecutionException, InterruptedException {
    Object lock = new Object();
    IntWrapper sum = new IntWrapper();

    ScheduledThreadPoolExecutor executor =
      new ScheduledThreadPoolExecutor(8, new ThreadFactory() {
        int count = 0;

        @Override
        public Thread newThread(
          @NotNull Runnable r) {
          return new Thread(r, "ThreadPoolExecutor Thread " + (++count));
        }
      });

    ArrayList<Future<?>> tasks = new ArrayList<>();
    // 创建任务 (X10)
    for (int i = 0; i < 10; i++) {
      int finalI = i;
      tasks.add(executor.submit(() -> {
        // 累加 100 次
        // T1 T2
        synchronized (lock) {
          for (int j = 0; j < 100; j++) {
            sum.value = sum.value + 1;
            Thread.sleep(10);
          }
        }
        return finalI;
      }));
    }

    for (Future<?> task : tasks) {
      System.out.println(task.get());
    }

    System.out.println("sum.value: " + sum.value);
  }
}
