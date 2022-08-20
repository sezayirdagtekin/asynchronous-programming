package any_of_task_factory_method;

import record.Weather;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AnyOfNotCompletedTaskExample {


    public static void main(String[] args) {

        Supplier<Weather> fetchWeatherA =
                () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server A", "Sunny");
                };
        Supplier<Weather> fetchWeatherB =
                () -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server B", "Mostly Sunny");
                };

        CompletableFuture<Weather> taskA = CompletableFuture.supplyAsync(fetchWeatherA);
        CompletableFuture<Weather> taskB = CompletableFuture.supplyAsync(fetchWeatherB);

        CompletableFuture.anyOf(taskA, taskB)
                .thenAccept(System.out::println)
                .join();

        System.out.println("taskA = " + taskA);
        System.out.println("taskB = " + taskB);

        //SAMPLE OUTPUT
        //taskA = java.util.concurrent.CompletableFuture@378bf509[Not completed]
        //taskB = java.util.concurrent.CompletableFuture@5fd0d5ae[Completed normally]
    }
}
