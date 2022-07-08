package LoRaWan;

import Criptare.Utils;

public class MessageReceive {

    // de la mesajul de accept
    private byte[] nwkSKey;
    private byte[] appSKey;
    private byte[] devAddr;

    public MessageReceive() {
        this.devAddr = new byte[4];
    }

    public void receiveMessage(byte[] bytes) {
        Message message = new Message();
        if (bytes[0] == 0x40 || bytes[0] == 0x60 || bytes[0] == 0x80 || bytes[0] == 0xA0) {
            message.setHeader(bytes[0]);

            devAddr[0] = bytes[4];
            devAddr[1] = bytes[3];
            devAddr[2] = bytes[2];
            devAddr[3] = bytes[1];
            message.setDevAddr(devAddr);
            message.setDirection((byte) 1);
            message.setFrameControl(bytes[5]);
            message.setFrameCounter((byte) ((bytes[7] << 8) + bytes[6]));

            byte[] mic = Utils.constructDataMic(message, nwkSKey);
            int micState = 0;
            for (int i = 0; i < 4; i++) {
                if (bytes[(bytes.length - 4) + i] == mic[i]) {
                    System.out.println(mic[i]);
                    micState++;
                }
            }

            if (micState == 4) {
                System.out.println("mic");
                int dateMessageLocation = 8 + (message.getFrameControl() & 0x0F);
                if (message.getFrameCounter() != dateMessageLocation) {
                    int port = bytes[8];
                    byte data[] = new byte[bytes.length - dateMessageLocation - 1];
                    dateMessageLocation = dateMessageLocation + 1;

                    for (int i = 0; i < data.length; i++) {
                        data[i] = bytes[dateMessageLocation + i];
                    }

                    if (port == 0x00) {
                        Utils.encryptPayload(message, data, nwkSKey);
                    } else {
                        Utils.encryptPayload(message, data, appSKey);
                    }
                }
            }
        }

    }

    public void setFromAcceptMessage(MessageAccept message) {
        this.nwkSKey = message.getNwkSKey();
        this.appSKey = message.getAppSKey();
    }


}
