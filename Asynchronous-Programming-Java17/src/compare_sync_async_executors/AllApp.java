package compare_sync_async_executors;

import java.util.concurrent.ExecutionException;

public class AllApp {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        SynchronousTasks.run();
        ExecuterTasks.run();
        AsyncTasks.run();

        //SAMPLE OUTPUT
       // Best quotation [SYNC ] = Quotation[server=Server A, amount=53] (293ms)
       // Best quotation [ES ] = Quotation[server=Server B, amount=45] (121ms)
       // Best quotation [ASYNC ] = Quotation[server=Server A, amount=45] (118ms)

    }
}
