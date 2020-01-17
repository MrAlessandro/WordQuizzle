package Users;

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
    private String Password;
    private byte[] Salt;
    private static final int Iterations = 1000;

    protected Password(char[] passwd)
    {
        char[] chars = passwd;

        try
        {
            this.Salt = generateSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, this.Salt, Iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            this.Password = toHex(hash);
            Arrays.fill(chars, '\0');
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            e.printStackTrace();
        }

    }

    protected Password(String passwd, byte[] salt)
    {
        this.Password = passwd;
        this.Salt = salt;
    }

    protected boolean checkPassword(char[] toCheck)
    {
        byte[] stored = fromHex(this.Password);

        try
        {
            PBEKeySpec spec = new PBEKeySpec(toCheck, this.Salt, Iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = stored.length ^ testHash.length;
            for(int i = 0; i < stored.length /*&& i < testHash.length*/; i++)
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


    protected String getEncodedPassword()
    {
        return this.Password;
    }

    private static byte[] generateSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
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

    protected JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        retValue.put("Password", this.Password);
        retValue.put("Salt", this.Salt.toString());

        return retValue;
    }
}
