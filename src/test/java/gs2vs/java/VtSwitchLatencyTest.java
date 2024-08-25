package gs2vs.java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * 测试两个虚拟线程使用Future获取结果场景的延迟情况。
 * - 测试用例：（MBP2019）运行10k次
 *   - 10-20ms, ~5times
 *   - 1~10ms, ~150 times
 *   - 绝大部分在6~9us和100~200us的区间
 */
public class VtSwitchLatencyTest {
    static volatile long[] stat = new long[10000];

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================begin first batch data");

        for (int i = 0; i < 100; i++) {
            waitVirtualThread(stat, i);
        }

        Thread.sleep(6000);
        System.out.println("================completed first batch data");

        for (int i = 0; i < 10000; i++) {
            if(stat[i] > 0){
                System.out.printf("index: %d, value: %d\n", i, stat[i]);
            }
        }

        System.out.println("================begin first 10k(for warmup)");

        long[] stat1 = new long[10000];
        for (int i = 0; i < 10000; i++) {
            waitVirtualThread(stat1, i);
        }

        Thread.sleep(6000);
        System.out.println("================completed first 10k(for warmup)");

        System.out.println("================begin first 10k");

        long[] stat2 = new long[10000];
        for (int i = 0; i < 10000; i++) {
            waitVirtualThread(stat2, i);
        }

        Thread.sleep(6000);
        System.out.println("================seconds 10k result");
        long count2 = 0;
        for (int i = 0; i < 10000; i++) {
            if(stat2[i] > 0){
                System.out.printf("index: %d, value: %d\n", i, stat2[i]);
                count2 ++;
            }
        }
        System.out.printf("stat2: %d\n", count2);

        System.out.println("================begin 100 result");

        long[] stat3 = new long[10000];
        for (int i = 0; i < 100; i++) {
            waitVirtualThread(stat3, i);
        }


        Thread.sleep(6000);
        System.out.println("================latest 100 result");
        long count = 0;
        for (int i = 0; i < 10000; i++) {
            if(stat3[i] > 0){
                System.out.printf("index: %d, value: %d\n", i, stat3[i]);
                count ++;
            }
        }
        System.out.printf("stat3: %d\n", count);

        Thread.currentThread().join();
    }

    public static void waitVirtualThread(long[] stat, int i) {
        ThreadFactory factory = Thread.ofVirtual().factory();
        factory.newThread(() -> {
            CompletableFuture future = new CompletableFuture();
            factory.newThread(() -> {
                try {
                    Thread.sleep(3000);
                    future.complete(System.nanoTime());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            try {
                long l = (long) future.get();
                long cost = System.nanoTime() - l;
//                if(cost > 10_000_000L) {
//                    System.out.println("LARGE_THEN_10MS: " + cost);
//                } else
//                    if(cost > 1_000_000L) {
                if(cost > 3_000L) {
                    stat[i] = cost;
//                    System.out.println("LARGE_THEN_1MS:" + cost);
                } else {
//                    System.out.println(cost);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

}