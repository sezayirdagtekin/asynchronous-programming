package compare;

import record.Quotation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncTasks {
    final static Random random = new Random();

    public static void main(String[] args)  {

        run();
    }

    public static void run()  {


        List<Supplier<Quotation>> quotationTask = createSuppliers();

        Instant begin = Instant.now();

        List<CompletableFuture<Quotation>> futures = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTask) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }

        List<Quotation> quotations = new ArrayList<>();
        for (CompletableFuture<Quotation> future : futures) {

            //Quotation quotation = future.get();
            //same as above not  throws ExecutionException, InterruptedException
            Quotation quotation = future.join();
            quotations.add(quotation);

        }

        //Comparator<Quotation> compare= (a,b)-> Integer.compare(a.amount(),b.amount());
        // Comparator<Quotation> compare= (a,b)-> a.amount()- b.amount();
      Quotation bestQuotation= quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

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
