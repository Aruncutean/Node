import LoRa.LoRa;
import LoRaWan.LoRaWan;
import Senzor.Humidity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.wiringpi.Gpio;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static boolean isAccepted = false;
    static float airQuality;
    static float battery;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) {
        System.out.println("Run");

        final Serial serial = SerialFactory.createInstance();
        try {
            serial.open(Serial.DEFAULT_COM_PORT, 9600);
            serial.addListener(new SerialDataEventListener() {
                @Override
                public void dataReceived(SerialDataEvent event) {
                    try {
                        String request = event.getAsciiString();
                        if (!request.isEmpty()) {
                           // System.out.println(request);
                            String[] arr = request.split("/");
                            try {

                                battery = Float.parseFloat(arr[0]);
                                airQuality = Float.parseFloat(arr[1]);
                            }catch (NumberFormatException e)
                            {

                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            serial.write("da");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Humidity humidity = new Humidity();

        String devEUI = "3EA63435A61D7B5C";
        String appEui = "3EA63435A61D7B5D";
        String appKey = "9EF97DEF12C7772417FF9FB1496B70B5";


        LoRa loRa = LoRa.getInstance();

        if (!loRa.init()) {
            System.out.println("LoRa Not init");
        } else {
            System.out.println("LoRa init");
            LoRaWan loRaWan = new LoRaWan();
            loRaWan.setAppEui(appEui);
            loRaWan.setAppKey(appKey);
            loRaWan.setDevEUI(devEUI);

            Runnable runnableAccept = new Runnable() {
                @Override
                public void run() {

                    if (loRaWan.joinAccept()) {
                        isAccepted = true;
                        System.out.println("Message Accept");
                    }
                }
            };
            Runnable runnableDataRecv = new Runnable() {
                @Override
                public void run() {
                    loRaWan.receiveData();
                }
            };

            do {
              //  System.out.println(df.format(humidity.getTemp()));
                if (!isAccepted) {
                    loRaWan.sendRequest();
                    System.out.println("Cerere  { Data: " + LocalDate.now()+" Time: "+ LocalTime.now()+"}");
                }
                long t = System.currentTimeMillis();
                long end = t + 10000;
                while (System.currentTimeMillis() < end) {
                    if (loRaWan.joinAccept()) {
                        isAccepted = true;
                        System.out.println("Message Accept  { Data: " + LocalDate.now()+" Time: "+ LocalTime.now()+"}");
                        break;
                    }
                }
            } while (!isAccepted);
            if (isAccepted) {
                while (true) {

                    if (Gpio.digitalRead(loRa.getDio0()) == Gpio.LOW) {
                        loRaWan.sendData("temp:" + df.format(humidity.getTemp()) + ";" + "hum:" + df.format(humidity.getHumidity()) + ";" + "airQ:" + airQuality + ";" + "bat:" + battery);
                        System.out.println("Message "+  "temp:" + df.format(humidity.getTemp()) + ";" + "hum:" + df.format(humidity.getHumidity()) + ";" + "airQ:" + airQuality + ";" + "bat:" + battery );
                        System.out.println("Send Data { Data: " + LocalDate.now()+" Time: "+ LocalTime.now()+"}");
                    }
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                    executor.scheduleAtFixedRate(runnableDataRecv, 0, 10, TimeUnit.SECONDS);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
