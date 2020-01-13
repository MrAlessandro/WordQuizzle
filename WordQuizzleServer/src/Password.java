import org.json.simple.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

class Password
{
    private String Password;
    private byte[] Salt;

    protected Password(char[] passwd)
    {
        char[] chars = passwd;

        try
        {
            this.Salt = generateSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, this.Salt, 1000, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            this.Password = toHex(this.Salt) + ":" + toHex(hash);
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
