package compare_sync_async_executors;

import record.Quotation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;

public class ExecuterTasks {
    final static Random random = new Random();

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        run();
    }

    public static void run() throws ExecutionException, InterruptedException {


        List<Callable<Quotation>> quotationTask = createCallableList();

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Instant begin = Instant.now();

        List<Future> futures = new ArrayList<>();
        quotationTask.forEach(task -> {
            Future<Quotation> future = executorService.submit(task);
            futures.add(future);

        });

        List<Quotation> quotations = new ArrayList<>();
        for (Future<Quotation> future : futures) {
            Quotation quotation = future.get();
            quotations.add(quotation);
        }


        Quotation bestQuotation = quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);

        System.out.println("Best quotation [ES ] = " + bestQuotation +
                " (" + duration.toMillis() + "ms)");

        executorService.shutdown();
        ;
    }

    private static List<Callable<Quotation>> createCallableList() {
        Callable<Quotation> callableA = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server A", random.nextInt(40, 60));

        };


        Callable<Quotation> callableB = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(35, 70));
        };

        Callable<Quotation> callableC = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(45, 80));
        };

        List<Callable<Quotation>> quotationTask = List.of(callableA, callableB, callableC);
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
