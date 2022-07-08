package LoRa;

import com.pi4j.wiringpi.Gpio;

public class Spi {
    public Spi() {
        int fb = com.pi4j.wiringpi.Spi.wiringPiSPISetup(0, 4000000);
        if (fb == 1) {
            System.out.println("Nu merge");
        }
    }

    public  void write(int register, int data) {
        byte packet[] = new byte[2];
        packet[0] = (byte) (register | 0x80);
        packet[1] = (byte)data;
        Gpio.digitalWrite(6, false);
        com.pi4j.wiringpi.Spi.wiringPiSPIDataRW(0, packet, 2);
        Gpio.digitalWrite(6, true);
    }

    public  byte  read(int  register) {
        byte packet[] = new byte[2];
        Gpio.digitalWrite(6, false);
        packet[0] = (byte) (register & 0x7F);
        packet[1] = 0x00;
        com.pi4j.wiringpi.Spi.wiringPiSPIDataRW(0, packet, 2);
        Gpio.digitalWrite(6, true);
        return packet[1];
    }

}
