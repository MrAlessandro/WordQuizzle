package commons.loggers;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private PrintStream output = System.out;
    private boolean colored;

    public Logger(boolean colored)
    {
        this.colored = colored;
    }

    public Logger(boolean colored, String name, String logFilesPath) throws IOException
    {
        this.colored = colored;

        File logFile = new File(logFilesPath + name + ".log");
        if (logFile.exists())
            logFile.delete();

        logFile.createNewFile();

        this.output = new PrintStream(new FileOutputStream(logFile), true);

    }

    public PrintStream getPrintStream()
    {
        return this.output;
    }

    public void print(Object o)
    {
        Date actual = new Date();

        if (colored)
            output.print(ANSI_BLUE + "[" + formatDate(actual.getTime()) + "] " + ANSI_RESET + o.toString());
        else
            output.print("[" + formatDate(actual.getTime()) + "] " + o.toString());

        output.flush();
    }

    public void println(Object o)
    {
        Date actual = new Date();

        if (colored)
            output.println(ANSI_BLUE + "[" + formatDate(actual.getTime()) + "] " + ANSI_RESET + o.toString());
        else
            output.println("[" + formatDate(actual.getTime()) + "] " + o.toString());

        output.flush();
    }

    public void printlnGreen(Object o)
    {
        if (colored)
            output.println(ANSI_GREEN + o.toString() + ANSI_RESET);
        else
            output.println(o.toString());

        output.flush();
    }

    public void printlnRed(Object o)
    {
        if (colored)
            output.println(ANSI_RED + o.toString() + ANSI_RESET);
        else
            output.println(o.toString());

        output.flush();
    }

    public void printlnYellow(Object o)
    {
        if (colored)
            output.println(ANSI_YELLOW + o.toString() + ANSI_RESET);
        else
            output.println(o.toString());

        output.flush();
    }

    public void printlnCyan(Object o)
    {
        if (colored)
            output.println(ANSI_CYAN + o.toString() + ANSI_RESET);
        else
            output.println(o.toString());
        output.flush();
    }

    private String formatDate(long milli)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date actualDate = new Date(milli);
        return date_format.format(actualDate);
    }
}
