package LoRaWan;

public class Message {

    private byte buffer[];
    private byte devAddr[];
    private byte direction;
    private byte frameCounter;
    private byte frameControl;
    private int size;
    private byte message[];

    public Message() {
        buffer = new byte[256];
    }

    public byte[] getMessageSend() {
        byte[] message = new byte[size];
        for (int i = 0; i < size; i++) {
            message[i] = buffer[i];
        }
        return message;
    }


    public byte[] getMessage() {
        return message;
    }

    public void setHeader(byte header) {
        buffer[0] = header;
    }

    public void setMic(byte[] mic) {
        for (int i = 0; i < 4; i++) {
            buffer[size + i] = mic[i];
        }
        size = size + 4;
    }


    public void setBuffer(byte[] buffer) {
        size = 9;
        for (int i = 0; i < buffer.length; i++) {
            this.buffer[size + i] = buffer[i];
        }
        size = size + buffer.length;

    }

    public void setDevAddr(byte[] devAddr) {
        this.devAddr = devAddr;
        buffer[1] = devAddr[3];
        buffer[2] = devAddr[2];
        buffer[3] = devAddr[1];
        buffer[4] = devAddr[0];
    }

    public void setDirection(byte direction) {
        buffer[5] = direction;
        this.direction = direction;
    }

    public void setFrameControl(byte frameControl) {
        this.frameControl = frameControl;
    }

    public void setFrameCounter(byte frameCounter) {
        this.frameCounter = frameCounter;
        buffer[6] = (byte) (frameCounter & 0x00FF);
        buffer[7] = (byte) ((frameCounter >> 8) & 0x00FF);

    }

    public void setModeMessage() {
        buffer[8] = 1;
    }

    public void setMessage(byte message[]) {
        this.message = message;
    }


    public void setSize(int size) {
        this.size = size;
    }

    public byte getFrameControl() {
        return frameControl;
    }

    public byte getFrameCounter() {
        return frameCounter;
    }

    public byte[] getDevAddr() {
        return devAddr;
    }

    public byte getDirection() {
        return direction;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getSize() {
        return size;
    }
}
