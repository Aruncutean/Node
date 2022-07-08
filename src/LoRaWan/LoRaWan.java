package LoRaWan;

import Criptare.Aes128;
import Criptare.Utils;
import LoRa.LoRa;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LoRaWan {

    private byte[] devEUI;
    private byte[] appEui;
    private byte[] appKey;

    //Message SendRequest
    private byte[] devNonce = new byte[2];

    //Message Accept
    MessageAccept messageAccept;
    MessageSend messageSend;
    MessageReceive messageReceive;


    public LoRaWan() {
        devEUI = new byte[8];
        appEui = new byte[8];
        appKey = new byte[16];
    }

    public void setAppEui(String appEui) {
        try {
            this.appEui = Hex.decodeHex(appEui.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    public void setDevEUI(String devEUI) {
        try {
            this.devEUI = Hex.decodeHex(devEUI.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    public void setAppKey(String appKey) {
        try {
            this.appKey = Hex.decodeHex(appKey.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest() {
        MessageRequest messageRequest = new MessageRequest();
        devNonce = messageRequest.sendRequest(devEUI, appEui, appKey);
    }


    public boolean joinAccept() {
        LoRa loRa = LoRa.getInstance();
        if (loRa.singleReceive()) {
            this.messageAccept = new MessageAccept(appKey);
            if (this.messageAccept.processingMessage(loRa.getPackage(), devNonce)) {
                messageSend = new MessageSend();
                messageReceive = new MessageReceive();
                return true;
            }
        }
        return false;
    }

    public void sendData(String data) {
        messageSend.setFromAcceptMessage(messageAccept);
        messageSend.sendData(data);
    }


    public void receiveData() {
        LoRa loRa = LoRa.getInstance();
        if (loRa.singleReceive()) {
            byte[] bytes = loRa.getPackage();
            messageReceive.setFromAcceptMessage(messageAccept);
            messageReceive.receiveMessage(bytes);
        }

    }
}
