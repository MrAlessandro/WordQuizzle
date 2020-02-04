package server.printer;

import server.constants.ServerConstants;

import java.io.*;

public class Printer
{
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private PrintStream output = System.out;

    public Printer(String name)
    {
        if (!(name.equals("Main")) && ServerConstants.LOG_IN_FILE)
        {
            OutputStream outStream;
            try {
                File logFile = new File(ServerConstants.SERVER_LOG_FILES_PATH.toString() + "/" + name + ".log");
                if (!logFile.exists())
                    logFile.createNewFile();
                outStream = new FileOutputStream(logFile);
            } catch (IOException e) {
                throw new Error("Log files inconsistency");
            }
            this.output = new PrintStream(outStream, true);
        }
    }

    public void print(Object o)
    {
        output.print(o.toString());
    }

    public void println(Object o)
    {
        output.println(o.toString());
    }

    public void printGreen(Object o)
    {
        output.print(ANSI_GREEN + o.toString() + ANSI_RESET);
    }

    public void printRed(Object o)
    {
        output.print(ANSI_RED + o.toString() + ANSI_RESET);
    }

    public void printYellow(Object o)
    {
        output.print(ANSI_YELLOW + o.toString() + ANSI_RESET);
    }

    public void printCyan(Object o)
    {
        output.print(ANSI_CYAN + o.toString() + ANSI_RESET);
    }

    public void printlnGreen(Object o)
    {
        output.println(ANSI_GREEN + o.toString() + ANSI_RESET);
    }

    public void printlnRed(Object o)
    {
        output.println(ANSI_RED + o.toString() + ANSI_RESET);
    }

    public void printlnYellow(Object o)
    {
        output.println(ANSI_YELLOW + o.toString() + ANSI_RESET);
    }

    public void printlnCyan(Object o)
    {
        output.print(ANSI_CYAN + o.toString() + ANSI_RESET);
    }
}
