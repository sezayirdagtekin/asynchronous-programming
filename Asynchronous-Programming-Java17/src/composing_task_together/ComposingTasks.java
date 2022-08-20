package composing_task_together;

import record.Quotation;
import record.TravelPage;
import record.Weather;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class ComposingTasks {
    final static Random random = new Random();

    public static void main(String[] args) throws InterruptedException {

        run();
    }

    public static void run() throws InterruptedException {


        List<Supplier<Quotation>> quotationTasks = buildQuotation();
        List<Supplier<Weather>> weatherTasks = buildWeather();


        List<CompletableFuture<Weather>> futuresWeather = new ArrayList<>();
        for (Supplier<Weather> task : weatherTasks) {
            CompletableFuture<Weather> future = CompletableFuture.supplyAsync(task);
            futuresWeather.add(future);

        }

        CompletableFuture<Weather> weatherCF = CompletableFuture.anyOf(futuresWeather.toArray(CompletableFuture[]::new))
                .thenApply(weather -> (Weather) weather);


        List<CompletableFuture<Quotation>> futuresQuotation = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {

            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futuresQuotation.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futuresQuotation.toArray(CompletableFuture[]::new));


        CompletableFuture<Quotation> bestQuotationCF = allOf.thenApply(v -> futuresQuotation.stream()
                .map(CompletableFuture::join)
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow());


        //Combine
        //bestQuotation.thenCombine(anyWeather,(q,w)-> new TravelPage(q,w));
        CompletableFuture<TravelPage> completableFutureTravelPage = bestQuotationCF.thenCombine(weatherCF, TravelPage::new);

        completableFutureTravelPage.thenAccept(System.out::println).join();


        //Compose has better performance . but it is not readable
        CompletableFuture<Void> done =
                bestQuotationCF.thenCompose(
                                quotation ->
                                        weatherCF.thenApply(weather -> new TravelPage(quotation, weather)))
                        .thenAccept(System.out::println);
        done.join();

        //SAMPE OUTPUT:
        //TravelPage[quotation=Quotation[server=Server B, amount=36], weather=Weather[server=Server B, weather=Mostly Sunny]]
        //TravelPage[quotation=Quotation[server=Server B, amount=36], weather=Weather[server=Server B, weather=Mostly Sunny]]


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


    private static List<Supplier<Weather>> buildWeather() {
        Supplier<Weather> weatherA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
                return new Weather("Server A", "Sunny");
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
