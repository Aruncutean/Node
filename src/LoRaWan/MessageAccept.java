package LoRaWan;

import Criptare.Aes128;

public class MessageAccept {

    /*
Header |   AppNonce | NetID   | DevAddr  | DLSettings | RXDelay | CFList   |   MIC
1 byte |  3 bytes   | 3 bytes | 4 bytes  |   1 byte   |  1 byte | 16 bytes | 4 bytes

  */


    private byte[] nwkSKey;
    private byte[] appSKey;
    private byte[] devAddr;
    private byte[] appKey;

    public MessageAccept(byte[] appKey) {
        this.nwkSKey = new byte[16];
        this.appSKey = new byte[16];
        this.devAddr = new byte[4];
        this.appKey = appKey;
    }


    public boolean processingMessage(byte[] bytes, byte[] devNonce) {
        if (bytes[0] == 0x20) {
            byte[] message = new byte[bytes.length - 1];
            for (int i = 0; i < bytes.length - 1; i++) {
                message[i] = bytes[i + 1];
            }

            byte[] messageDecrypt = null;
            try {
                messageDecrypt = Aes128.decrypt(message, appKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (messageDecrypt != null) {
                byte[] messageFormat = new byte[message.length - 3];
                messageFormat[0] = bytes[0];
                for (int i = 1; i < messageFormat.length; i++) {
                    messageFormat[i] = messageDecrypt[i - 1];
                }

                byte[] micCalculate = null;
                try {
                    micCalculate = Aes128.mic(messageFormat, messageFormat.length, appKey);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                int micState = 0;

                for (int i = 0; i < 4; i++) {
                    if (micCalculate[i] == messageDecrypt[messageDecrypt.length - 4 + i]) {
                        micState = micState + 1;
                    }
                }

                if (micState == 4) {

                    nwkSKey[0] = 0x01;

                    //Load AppNonce
                    for (int i = 0; i < 3; i++) {
                        nwkSKey[i + 1] = messageFormat[i + 1];
                    }

                    //Load NetId
                    for (int i = 0; i < 3; i++) {
                        nwkSKey[i + 4] = messageFormat[i + 4];
                    }


                    //saveDevdata
                    for (int i = 0; i < 4; i++) {
                        devAddr[3 - i] = messageFormat[i + 7];
                    }

                    nwkSKey[7] = devNonce[0];
                    nwkSKey[8] = devNonce[1];
                    for (int i = 9; i <= 15; i++) {
                        nwkSKey[i] = 0x00;
                    }

                    for (int i = 0; i < 16; i++) {
                        appSKey[i] = nwkSKey[i];
                    }
                    appSKey[0] = 0x02;
                    try {
                        nwkSKey = Aes128.decrypt(nwkSKey, appKey);
                        appSKey = Aes128.decrypt(appSKey, appKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public byte[] getAppSKey() {
        return appSKey;
    }

    public byte[] getNwkSKey() {
        return nwkSKey;
    }

    public byte[] getDevAddr() {
        return devAddr;
    }
}
