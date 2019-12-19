public class WordQuizzle
{
    public static void main(String[] args)
    {
        UserNet Net = UserNet.getNet();
        char[] passwd = new char[1];
        passwd[0] = 'c';


        Net.registerUser("Marco", passwd);
        Net.registerUser("Sofia", passwd);
        Net.registerUser("Andrea", passwd);
        Net.registerUser("Alessandro", passwd);
        Net.registerUser("Costanza", passwd);
        Net.registerUser("Martina", passwd);
        Net.registerUser("Alessio", passwd);

        try
        {
            Net.addFriendship("Marco", "Costanza");
            Net.addFriendship("Marco", "Sofia");
            Net.addFriendship("Martina", "Alessio");
            Net.addFriendship("Alessandro", "Sofia");
            Net.addFriendship("Andrea", "Costanza");
            Net.addFriendship("Alessandro", "Costanza");
            Net.addFriendship("Alessio", "Martina");
        }
        catch (InconsistentRelationshipException e)
        {
            e.printStackTrace();
        }

        Net.printNet();
    }
}
