package run_serveral_task_asyn;

import record.Quotation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Supplier;

public class TriggerTasks {
    final static Random random = new Random();

    public static void main(String[] args) throws InterruptedException {

        run();
    }

    public static void run() throws InterruptedException {


        List<Supplier<Quotation>> quotationTasks = buildQuotation();

        List<CompletableFuture<Quotation>> quotationsCFS = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {

            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            quotationsCFS.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(quotationsCFS.toArray(CompletableFuture[]::new));


        Quotation bestQuotation = allOf.thenApply(v -> quotationsCFS.stream()
                .map(CompletableFuture::join)
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow()).join();


        System.out.println("Best quotation [ASYNC ] = " + bestQuotation);


    }

    private static List<Supplier<Quotation>> buildQuotation() {
        Supplier<Quotation> callableA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Quotation("Server A", random.nextInt(40, 60));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        };


        Supplier<Quotation> callableB = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Quotation("Server B", random.nextInt(35, 70));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        };

        Supplier<Quotation> callableC = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Quotation("Server B", random.nextInt(45, 80));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        };

        List<Supplier<Quotation>> quotationTask = List.of(callableA, callableB, callableC);
        return quotationTask;
    }

    private static Function<Callable<Quotation>, Quotation> getQuotationFunction() {
        return task -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
