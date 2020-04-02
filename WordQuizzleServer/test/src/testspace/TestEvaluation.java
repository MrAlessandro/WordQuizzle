package testspace;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TestEvaluation
{
    public static void main(String[] args) throws InterruptedException
    {
        Middle middle = new Middle();

        middle.register(new Consumer<Values>() {
            @Override
            public void accept(Values values) {
                System.out.println("a is: " +  values.getA());
                System.out.println("b is: " +  values.getB());
            }
        });

        Thread.sleep(2000);
    }

    static class Values
    {
        private int a = 0;
        private int b = 0;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    static class Middle
    {
        private ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);

        public void register(Consumer<Values> passed)
        {
            Consumer<Values> middleConsumer = new Consumer<Values>() {
                @Override
                public void accept(Values values) {
                    System.out.println("Middle");
                    passed.accept(values);
                }
            };

            Leaf leaf = new Leaf(middleConsumer);
            pool.schedule(leaf, 1, TimeUnit.SECONDS);
        }
    }

    static class Leaf implements Runnable
    {
        public Consumer<Values> task;

        public Leaf(Consumer<Values> task)
        {
            this.task = task;
        }

        @Override
        public void run()
        {
            Values values = new Values();
            values.setA(5);
            values.setB(5);
            System.out.println("Leaf");
            task.accept(values);
        }
    }
}
