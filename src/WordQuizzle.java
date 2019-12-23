class WordQuizzle
{
    public static void main(String[] args)
    {
        UserNet Net = UserNet.getNet();

        Net.restoreNet();

        Net.printNet();

        Net.backUpNet();

    }
}
