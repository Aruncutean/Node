package LoRaWan;

import Criptare.Aes128;
import LoRa.LoRa;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Locale;
import java.util.Random;

public class MessageRequest {

    private byte devNonce[];
    private LoRa loRa;

    public MessageRequest() {
        devNonce = new byte[2];
        this.loRa = LoRa.getInstance();
    }

    public byte[] sendRequest(byte[] devEUI,
                              byte[] appEui,
                              byte[] appKey) {

        byte[] message = new byte[23];
        message[0] = 0;
        for (int i = 0; i < 8; i++) {
            message[i + 1] = appEui[7 - i];
        }

        for (int i = 0; i < 8; i++) {
            message[i + 9] = devEUI[7 - i];

        }
        String setOfCharacters = "abcdef1234567";

        Random random = new Random();
        char[] bite1 = {setOfCharacters.charAt(random.nextInt((setOfCharacters.length()))),
                setOfCharacters.charAt(random.nextInt((setOfCharacters.length())))};
        char[] bite2 = {setOfCharacters.charAt(random.nextInt((setOfCharacters.length()))),
                setOfCharacters.charAt(random.nextInt((setOfCharacters.length())))};
        try {
            byte[] bite1B = Hex.decodeHex(bite1);
            byte[] bite2B = Hex.decodeHex(bite2);
            message[17] = bite1B[0];
            message[18] = bite2B[0];
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        devNonce[0] = message[17];
        devNonce[1] = message[18];

        Aes128 aes128 = new Aes128();
        byte[] micByte = null;
        try {
            micByte = aes128.mic(message, message.length - 4, appKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        message[19] = micByte[0];
        message[20] = micByte[1];
        message[21] = micByte[2];
        message[22] = micByte[3];

        loRa.sendPackage(message);

        return devNonce;
    }


}
