package any_of_task_factory_method;

import record.Weather;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReadingOneOfSeveralTasks {
    final static Random random = new Random();

    public static void main(String[] args) throws InterruptedException {

        run();
    }

    public static void run() throws InterruptedException {

        List<Supplier<Weather>>  weatherTasks=  buildWeather();

        List<CompletableFuture<Weather>>  futures= new ArrayList<>();
        for(Supplier<Weather> task:weatherTasks){
            CompletableFuture<Weather> future= CompletableFuture.supplyAsync(task);
            futures.add(future);

        }

        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(futures.toArray(CompletableFuture[]::new));

        anyFuture.thenAccept(System.out::println).join();

    }

    private static List<Supplier<Weather>> buildWeather() {
        Supplier<Weather> weatherA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new  Weather("Server A", "Sunny");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        };


        Supplier<Weather> weatherB = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Weather("Server B", "Mostly Sunny");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        };

        Supplier<Weather> weatherC = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Weather("Server C", "Almost Sunny");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        };

        List<Supplier<Weather>> WeatherTask = List.of(weatherA, weatherB, weatherC);
        return WeatherTask;
    }

    private static Function<Callable<Weather>, Weather> getWeatherFunction() {
        return task -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
