import java.util.Arrays;

public class TestError
{
    public static void main(String[] args)
    {
        Thread.UncaughtExceptionHandler errorsHandler;
        errorsHandler = (thread, throwable) -> {
            System.out.println("FATAL ERROR FROM THREAD " + thread.getName());
            System.out.println(Arrays.toString(throwable.getStackTrace()));
            System.exit(1);
        };
        Thread.currentThread().setUncaughtExceptionHandler(errorsHandler);
        throw new Error("Ciao");
    }
}
