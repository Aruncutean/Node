package LoRa;

public class LoRaMode {
    private final int REG_OP_MODE = 0x01;
    private final int MODE_LORA = 0x80;
    private final int MODE_SLEEP = 0x00;
    private final int MODE_MASK = 0x07;
    private final int MODE_STANDBY = 0x01;
    private final int MODE_FSTX = 0x02;
    private final int MODE_TX = 0x03;
    private final int MODE_RX_CONTINUOUS = 0x05;
    private final int MODE_RX_SINGLE = 0x06;

    private Spi spi;

    public LoRaMode(Spi spi) {
        this.spi = spi;
    }

    public boolean isTxMode() {
        if ((spi.read(REG_OP_MODE) & MODE_TX) == MODE_TX) {
            return true;
        }
        return false;
    }

    public void setLoraMode() {
        spi.write(REG_OP_MODE, MODE_LORA);
    }

    public void setMODE_SLEEP() {
        spi.write(REG_OP_MODE, MODE_SLEEP);
    }

    public void setSleepMode() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_SLEEP);
    }


    public void setTxMode() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_TX);
    }


    public void setStandbyMode() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_STANDBY);
    }

    public void setRxContinuousMode() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_RX_CONTINUOUS);
    }

    public void setRxSingleMode() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_RX_SINGLE);
    }

    public void setModeFSTX() {
        spi.write(REG_OP_MODE, MODE_LORA | MODE_FSTX);
    }

}
