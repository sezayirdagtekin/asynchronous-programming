package compare_sync_async_executors;

import record.Quotation;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SynchronousTasks {
    final static Random random = new Random();

    public static void main(String[] args) {

        run();
    }

    public static void run() {


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

        Instant begin = Instant.now();


        Quotation bestQuotation = quotationTask.stream().map(getQuotationFunction())
                .min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);

        System.out.println("Best quotation [SYNC ] = " + bestQuotation +
                " (" + duration.toMillis() + "ms)");


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
