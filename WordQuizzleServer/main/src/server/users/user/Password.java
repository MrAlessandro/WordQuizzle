package server.users.user;

import org.json.simple.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

class Password
{
    private static final int ITERATIONS = 1000;

    private String password;
    private String salt;

    protected Password(char[] password)
    {
        try
        {
            byte[] salt = generateSalt();

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            this.password = toHex(hash);
            this.salt = toHex(salt);
            Arrays.fill(password, '\0');
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new RuntimeException("Generating password");
        }

    }

    protected Password(String password, String salt)
    {
        this.password = password;
        this.salt = salt;
    }

    protected Password(JSONObject serializedPassword)
    {
        this.password = (String) serializedPassword.get("Password");
        this.salt = (String) serializedPassword.get("Salt");
    }

    protected boolean checkPassword(char[] toCheck)
    {
        byte[] stored = fromHex(this.password);

        try
        {
            PBEKeySpec spec = new PBEKeySpec(toCheck, fromHex(this.salt), ITERATIONS, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = stored.length ^ testHash.length;
            for(int i = 0; i < stored.length && i < testHash.length; i++)
            {
                diff |= stored[i] ^ testHash[i];
            }

            Arrays.fill(toCheck, '\0');

            return diff == 0;
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private static byte[] generateSalt()
    {
        SecureRandom sr;

        try
        {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Generating randomizer");
        }

        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    protected static String toHex(byte[] source)
    {
        BigInteger bi = new BigInteger(1, source);
        String hex = bi.toString(16);
        int paddingLength = (source.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    protected static byte[] fromHex(String hex)
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    protected JSONObject serialize()
    {
        JSONObject retValue = new JSONObject();
        retValue.put("Password", this.password);
        retValue.put("Salt", this.salt);

        return retValue;
    }

    public String toString()
    {
        return this.password;
    }
}
