package LoRaWan;

import Criptare.Utils;
import LoRa.LoRa;

public class MessageSend {

    private int direction;
    private int frameControl;
    private int frameCounter;

    // de la mesajul de accept
    private byte[] nwkSKey;
    private byte[] appSKey;
    private byte[] devAddr;

    public MessageSend() {
        this.direction = 0;
        this.frameControl = 0;
        this.frameCounter = 0;

    }

    public void sendData(String data) {

        Message message = new Message();

        message.setHeader((byte) 0x40);
        message.setDevAddr(devAddr);
        message.setDirection((byte) direction);
        message.setFrameControl((byte) frameControl);
        message.setFrameCounter((byte) frameCounter);

        if (data.length() > 0) {
            message.setModeMessage();
            Utils.encryptPayload(message, data.getBytes(), appSKey);
        }

        byte[] micCalculate = Utils.constructDataMic(message, nwkSKey);
        message.setMic(micCalculate);

        LoRa loRa = LoRa.getInstance();
        loRa.sendPackage(message.getMessageSend());
        frameCounter++;

    }

    public void setFromAcceptMessage(MessageAccept message) {
        this.devAddr = message.getDevAddr();
        this.nwkSKey = message.getNwkSKey();
        this.appSKey = message.getAppSKey();
    }

}
