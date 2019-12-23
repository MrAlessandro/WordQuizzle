import java.io.IOException;

public class WordQuizzle
{
    public static void main(String[] args) throws IOException {
        UserNet Net = UserNet.getNet();

        Net.restoreNet();

        Net.printNet();

    }
}
