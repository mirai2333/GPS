package sample;

import yzj.NMEAParser;
import yzj.ReadTools;
import yzj.getPortException;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class DataDao {
    private static BlockingQueue<String> rmcQueue;
    private static ReadTools rt = new ReadTools();
    private static Thread readThread = new Thread(rt);

    public static void getData() throws getPortException {
        if (!readThread.isAlive())
            readThread.start();
        rt.openSerialPort();

    }

    public static void setPortName(String portName){
        rt.setSerialPortName(portName);
        rt.getSerialPortIdentifier();
    }


    public static boolean isNull(){
        return rt.isNullPort();
    }

    public static Date getDate(){
        return NMEAParser.getDate();
    }
    public static String getLatitude(){
        return NMEAParser.getLatitude();
    }
    public static String getLongitude(){
        return NMEAParser.getLongitude();
    }

    public static String getSatelliteNumber() {
        return NMEAParser.getSatelliteNumber();
    }

    public static String getElevation() {
        return NMEAParser.getElevation();
    }

    public static void ParserRmcMessage(String message){
        NMEAParser.RMCParser(message);
    }
    public static void ParserGgaMessage(String message){
        NMEAParser.GGAParser(message);
    }
    public static void closePort(){
        rt.closePort();
    }

}
