package Senzor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class Humidity {
    private I2CBus bus;
    private I2CDevice device;

    public Humidity() {

        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            device = bus.getDevice(0x44);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        }

    }


    public float getTemp() {
        byte[] config = new byte[2];
        config[0] = (byte) 0x2C;
        config[1] = (byte) 0x06;
        try {
            device.write(config, 0, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Read 6 bytes of data
        // temp msb, temp lsb, temp CRC, humidity msb, humidity lsb, humidity CRC
        byte[] data = new byte[6];
        try {
            device.read(data, 0, 6);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert the data
        float cTemp = (float) (((((data[0] & 0xFF) * 256) + (data[1] & 0xFF)) * 175.0) / 65535.0 - 45.0);
        return cTemp;
    }


    public float getHumidity() {
        byte[] config = new byte[2];
        config[0] = (byte) 0x2C;
        config[1] = (byte) 0x06;
        try {
            device.write(config, 0, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Read 6 bytes of data
        // temp msb, temp lsb, temp CRC, humidity msb, humidity lsb, humidity CRC
        byte[] data = new byte[6];
        try {
            device.read(data, 0, 6);
        } catch (IOException e) {
            e.printStackTrace();
        }
        float humidity = (float) (((((data[3] & 0xFF) * 256) + (data[4] & 0xFF)) * 100.0) / 65535.0);

        return humidity;
    }
}
