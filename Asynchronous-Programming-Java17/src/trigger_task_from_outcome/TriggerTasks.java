package trigger_task_from_outcome;

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


        List<Supplier<Quotation>> quotationTask = createSuppliers();

        Instant begin = Instant.now();

        List<CompletableFuture<Quotation>> futures = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTask) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }
        //ArrayList is not thread safe but ConcurrentLinkedDeque thread safe
        Collection<Quotation> quotations = new ConcurrentLinkedDeque<>();
        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
        for (CompletableFuture<Quotation> future : futures) {

            //This is blocking code removed
            // Quotation quotation = future.join();
            CompletableFuture<Void> completableFuture = future.thenAccept(quotation -> quotations.add(quotation));
            completableFutureList.add(completableFuture);

        }

        //This is not best way to complete before main thread is die
        //Thread.sleep(2000);

        //This is better
        for (CompletableFuture cf : completableFutureList) {
            cf.join();
        }

        System.out.println("quotations:" + quotations);

        Quotation bestQuotation = quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);

        System.out.println("Best quotation [ASYNC ] = " + bestQuotation +
                " (" + duration.toMillis() + "ms)");


    }

    private static List<Supplier<Quotation>> createSuppliers() {
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
