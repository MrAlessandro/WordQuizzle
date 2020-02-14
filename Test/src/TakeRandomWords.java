import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class TakeRandomWords
{
    public static void main(String[] args) throws IOException
    {
        Path dictionaryPath = Paths.get("/home", "alessandro", "Development", "WordQuizzle", "WordQuizzleServer", "resources", "Dictionary");
        RandomAccessFile dictionary = new RandomAccessFile(dictionaryPath.toString(), "r");
        long length = dictionary.length();

        Random randomSeek = new Random();
        long seek = Math.abs(randomSeek.nextLong() % length);
        dictionary.seek(length);
        dictionary.readLine();
        System.out.println(dictionary.readLine());

    }
}
