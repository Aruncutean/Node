package Criptare;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class Aes128 {
    public static byte[] encrypt(byte[] Data, byte[] key1) throws Exception {
        Key key = new SecretKeySpec(key1, "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(Data);
    }

    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "SunJCE");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    public static byte[] mic(byte[] Data,int size, byte[] key1) throws Exception {
        AESEngine aes = new AESEngine();
        CMac cmac = new CMac(aes);
        KeyParameter key = new KeyParameter(key1);
        cmac.init(key);
        cmac.update(Data, 0, size);
        byte[] mac = new byte[cmac.getMacSize()];
        cmac.doFinal(mac, 0);
        return mac;
    }
}
