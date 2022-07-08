package LoRa;

import com.pi4j.wiringpi.Gpio;

public class LoRa {

    private static LoRa loRa = null;

    private int[][] LoRa_Frequency = new int[][]{//[868.1 - 867.9] MHz
            {0xD9, 0x06, 0x8B}, //Channel [0], 868.1 MHz / 61.035 Hz = 14222987 = 0xD9068B
            {0xD9, 0x13, 0x58}, //Channel [1], 868.3 MHz / 61.035 Hz = 14226264 = 0xD91358
            {0xD9, 0x20, 0x24}, //Channel [2], 868.5 MHz / 61.035 Hz = 14229540 = 0xD92024
            {0xD8, 0xC6, 0x8B}, //Channel [3], 867.1 MHz / 61.035 Hz = 14206603 = 0xD8C68B
            {0xD8, 0xD3, 0x58}, //Channel [4], 867.3 MHz / 61.035 Hz = 14209880 = 0xD8D358
            {0xD8, 0xE0, 0x24}, //Channel [5], 867.5 MHz / 61.035 Hz = 14213156 = 0xD8E024
            {0xD8, 0xEC, 0xF1}, //Channel [6], 867.7 MHz / 61.035 Hz = 14216433 = 0xD8ECF1
            {0xD8, 0xF9, 0xBE}, //Channel [7], 867.9 MHz / 61.035 Hz = 14219710 = 0xD8F9BE
            {0xD9, 0x61, 0xBE}, // RX2 Receive channel 869.525 MHz / 61.035 Hz = 14246334 = 0xD961BE
    };


    private final Spi spi;
    private final int ss;
    private final int reset;
    private final int dio0;
    private final int dio1;

    private LoRa() {
        spi = new Spi();
        this.ss = 6;
        this.reset = 0;
        this.dio0 = 7;
        this.dio1 = 16;
    }

    public static LoRa getInstance() {
        if (loRa == null) {
            loRa = new LoRa();
        }
        return loRa;
    }

    public void changeSFBW(int sf, int bw) {
        spi.write(Reg.RFM_REG_MODEM_CONFIG2.getVal(), (sf << 4) | 0b0100);
        spi.write(Reg.RFM_REG_MODEM_CONFIG1.getVal(), (bw << 4) | 0x02);

        if (sf > 10) {
            spi.write(Reg.RFM_REG_MODEM_CONFIG3.getVal(), 0b1100);
        } else {
            spi.write(Reg.RFM_REG_MODEM_CONFIG3.getVal(), 0b0100);
        }
    }

    public void changeDatarate(int datarate) {
        switch (datarate) {
            case 0x00:  // SF12BW125
                changeSFBW(12, 0x07);
                break;
            case 0x01:  // SF11BW125
                changeSFBW(11, 0x07);
                break;
            case 0x02:  // SF10BW125
                changeSFBW(10, 0x07);
                break;
            case 0x03:  // SF9BW125
                changeSFBW(9, 0x07);
                break;
            case 0x04:  // SF8BW125
                changeSFBW(8, 0x07);
                break;
            case 0x05:  // SF7BW125
                changeSFBW(7, 0x07);
                break;
            case 0x06:  // SF7BW250
                changeSFBW(7, 0x08);
                break;
        }
    }


    public void changeChannel(int channel) {
        if (channel <= 0x08) {
            for (int i = 0; i < 3; ++i) {
                spi.write(Reg.RFM_REG_FR_MSB.getVal() + i, LoRa_Frequency[channel][i]);
            }
        }
    }

    public boolean init() {
        Gpio.wiringPiSetup();
        Gpio.pinMode(ss, Gpio.OUTPUT);
        Gpio.pinMode(reset, Gpio.OUTPUT);
        Gpio.pinMode(dio0, Gpio.INPUT);
        Gpio.digitalWrite(ss, true);


        Gpio.digitalWrite(reset, false);
        Gpio.delay(100);
        Gpio.digitalWrite(reset, true);
        Gpio.delay(100);

        int ver = spi.read(0x42);
        if (ver != 18) {
            return false;
        }
        Gpio.delay(50);
        spi.write(Reg.RFM_REG_OP_MODE.getVal(), Reg.RFM_MODE_LORA.getVal());
        switchMode(Reg.RFM_MODE_STANDBY.getVal());
        changeChannel(0);

        setTxPower(20);

        spi.write(Reg.RFM_REG_LNA.getVal(), 0x23);

        changeDatarate(0x00);

        spi.write(Reg.RFM_REG_SYM_TIMEOUT_LSB.getVal(), 0x25);

        spi.write(Reg.RFM_REG_PREAMBLE_MSB.getVal(), 0x00);
        spi.write(Reg.RFM_REG_PREAMBLE_LSB.getVal(), 0x08);

        spi.write(Reg.RFM_REG_SYNC_WORD.getVal(), 0x34);

        spi.write(0x0E, 0x80);

        spi.write(0x0F, 0x00);

        return true;
    }

    public void setTxPower(int level) {
        if (level > 17) {
            if (level > 20) {
                level = 20;
            }
            level -= 3;

            spi.write(Reg.RFM_REG_PA_DAC.getVal(), 0x87);
            setOCP(140);
        } else {
            if (level < 2) {
                level = 2;
            }
            spi.write(Reg.RFM_REG_PA_DAC.getVal(), 0x84);
            setOCP(100);
        }
        spi.write(Reg.RFM_REG_PA_CONFIG.getVal(), 0x80 | (level - 2));  //PA Boost mask

    }


    public void setOCP(int mA) {
        int ocpTrim = 27;
        if (mA <= 120) {
            ocpTrim = (mA - 45) / 5;
        } else if (mA <= 240) {
            ocpTrim = (mA + 30) / 10;
        }

        spi.write(Reg.RFM_REG_OCP.getVal(), 0x20 | (0x1F & ocpTrim));
    }

    public void sendPackage(byte[] message) {
        int i;
        int txLocation = 0x00;

        switchMode(Reg.RFM_MODE_STANDBY.getVal());
        spi.write(Reg.RFM_REG_DIO_MAPPING1.getVal(), 0x40);

        //Set IQ to normal values
        spi.write(Reg.RFM_REG_INVERT_IQ.getVal(), 0x27);
        spi.write(Reg.RFM_REG_INVERT_IQ2.getVal(), 0x1D);

        //Set payload length to the right length
        spi.write(Reg.RFM_REG_PAYLOAD_LENGTH.getVal(), message.length);

        //Get location of Tx part of FiFo
        txLocation = spi.read(0x0E);

        //Set SPI pointer to start of Tx part in FiFo
        spi.write(Reg.RFM_REG_FIFO_ADDR_PTR.getVal(), txLocation);

        //Write Payload to FiFo
        for (i = 0; i < message.length; i++) {
            spi.write(Reg.RFM_REG_FIFO.getVal(), message[i]);
        }

        //Switch RFM to Tx
        spi.write(Reg.RFM_REG_OP_MODE.getVal(), 0x83);

        //Wait for TxDone
        int k = 0;
        while (Gpio.digitalRead(dio0) == 0) {
            Gpio.delay(5);
            if (i++ > 1000) {
                break;
            }
        }

        //Clear interrupt
        spi.write(Reg.RFM_REG_IRQ_FLAGS.getVal(), 0x08);
    }

    public boolean singleReceive() {
        boolean status = false;
        spi.write(Reg.RFM_REG_DIO_MAPPING1.getVal(), 0x00);

        spi.write(Reg.RFM_REG_INVERT_IQ.getVal(), 0x67);
        spi.write(Reg.RFM_REG_INVERT_IQ2.getVal(), 0x19);

        switchMode(Reg.RFM_MODE_RXCONT.getVal());

        while ((Gpio.digitalRead(dio0) == 0) && (Gpio.digitalRead(dio1) == 0)) ;

        if (Gpio.digitalRead(dio1) == 1) {
            status = false;
        }

        if (Gpio.digitalRead(dio0) == 1) {
            status = true;
        }
        return status;
    }

    public byte[] getPackage() {

        //   StringBuilder stringBuilder = new StringBuilder();
        int RFM_Interrupts = spi.read(0x12);


        int RFM_Package_Location = spi.read(0x10);
        int size = spi.read(0x13);

        spi.write(Reg.RFM_REG_FIFO_ADDR_PTR.getVal(), RFM_Package_Location);

        byte[] bytes = new byte[size];
        for (int i = 0x00; i < size; i++) {
            bytes[i] = spi.read(Reg.RFM_REG_FIFO.getVal());
        }

        spi.write(Reg.RFM_REG_IRQ_FLAGS.getVal(), RFM_Interrupts);

        return bytes;
    }

    public void switchMode(int mode) {
        mode = mode | 0x80;
        spi.write(Reg.RFM_REG_OP_MODE.getVal(), mode);

    }

    public int getRssi() {
        return spi.read(Reg.RFM_REG_LAST_RSSI.getVal());
    }


    public int getDio0() {
        return dio0;
    }
}
