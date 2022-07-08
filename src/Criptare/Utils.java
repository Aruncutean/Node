package Criptare;

import LoRaWan.Message;

public class Utils {

    public static void encryptPayload(Message message, byte[] data, byte[] key) {

        byte[] blockA = new byte[16];

        int numberOfBlocks = (data.length / 16);
        int ibz = (data.length % 16);

        if (ibz != 0) {
            numberOfBlocks++;
        }

        for (int i = 0; i < numberOfBlocks; i++) {
            blockA[0] = 0x01;
            blockA[1] = 0x00;
            blockA[2] = 0x00;
            blockA[3] = 0x00;
            blockA[4] = 0x00;

            blockA[5] = message.getDirection();

            blockA[6] = message.getDevAddr()[3];
            blockA[7] = message.getDevAddr()[2];
            blockA[8] = message.getDevAddr()[1];
            blockA[9] = message.getDevAddr()[0];

            blockA[10] = (byte) (message.getFrameCounter() & 0x00FF);
            blockA[11] = (byte) ((message.getFrameCounter() >> 8) & 0x00FF);

            blockA[12] = 0x00;
            blockA[13] = 0x00;
            blockA[14] = 0x00;

            blockA[15] = (byte) (i + 1);
            try {
                blockA = Aes128.encrypt(blockA, key);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i != (numberOfBlocks - 1)) {
                for (int j = 0; j < 16; j++) {
                    data[(i * 16) + j] ^= blockA[j];
                }
            } else {
                if (ibz == 0) {
                    ibz = 16;
                }
                for (int j = 0; j < ibz; j++) {
                    data[(i * 16) + j] ^= blockA[j];
                }

            }
        }

        message.setBuffer(data);
        message.setMessage(data);
    }

    public static byte[] constructDataMic(Message message, byte[] key) {
        byte[] micData = new byte[16 + message.getSize()];

        byte blockB[] = new byte[16];
        blockB[0] = 0x49;
        blockB[1] = 0x00;
        blockB[2] = 0x00;
        blockB[3] = 0x00;
        blockB[4] = 0x00;

        blockB[5] = message.getDirection();

        blockB[6] = message.getDevAddr()[3];
        blockB[7] = message.getDevAddr()[2];
        blockB[8] = message.getDevAddr()[1];
        blockB[9] = message.getDevAddr()[0];

        blockB[10] = (byte) (message.getFrameCounter() & 0x00FF);
        blockB[11] = (byte) ((message.getFrameCounter() >> 8) & 0x00FF);

        blockB[12] = 0x00; //Frame counter upper bytes
        blockB[13] = 0x00;

        blockB[14] = 0x00;
        blockB[15] = (byte) message.getSize();

        for (int i = 0; i < 16; i++) {
            micData[i] = blockB[i];
        }

        for (int i = 0; i < message.getSize(); i++) {
            micData[i + 16] = message.getBuffer()[i];
        }

        byte[] micCalculate = null;
        try {
            micCalculate = Aes128.mic(micData, 16 + message.getSize(), key);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return micCalculate;
    }


}
