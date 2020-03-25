package server.loggers;

import server.settings.ServerConstants;

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

    public static void setUp()
    {
        if (ServerConstants.LOG_FILES)
        {
            File logFilesDirectory = new File(ServerConstants.LOG_FILES_PATH);
            logFilesDirectory.mkdir();
        }
    }

    public Logger(String name)
    {
        if (!(name.equals("Main")) && ServerConstants.LOG_FILES)
        {
            OutputStream outStream;
            try
            {
                File logFile = new File(ServerConstants.LOG_FILES_PATH + name + ".log");
                if (logFile.exists())
                    logFile.delete();

                logFile.createNewFile();

                outStream = new FileOutputStream(logFile);

                this.output = new PrintStream(outStream, true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void print(Object o)
    {
        Date actual = new Date();
        output.print(ANSI_BLUE + "[" + formatDate(actual.getTime()) + "] " + ANSI_RESET + o.toString());
        output.flush();
    }

    public void println(Object o)
    {
        Date actual = new Date();
        output.println(ANSI_BLUE + "[" + formatDate(actual.getTime()) + "] " + ANSI_RESET + o.toString());
        output.flush();
    }

    public void printGreen(Object o)
    {
        output.print(ANSI_GREEN + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printRed(Object o)
    {
        output.print(ANSI_RED + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printYellow(Object o)
    {
        output.print(ANSI_YELLOW + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printCyan(Object o)
    {
        output.print(ANSI_CYAN + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printlnGreen(Object o)
    {
        output.println(ANSI_GREEN + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printlnRed(Object o)
    {
        output.println(ANSI_RED + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printlnYellow(Object o)
    {
        output.println(ANSI_YELLOW + o.toString() + ANSI_RESET);
        output.flush();
    }

    public void printlnCyan(Object o)
    {
        output.println(ANSI_CYAN + o.toString() + ANSI_RESET);
        output.flush();
    }

    private String formatDate(long millisecs)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date actualDate = new Date(millisecs);
        return date_format.format(actualDate);
    }
}
