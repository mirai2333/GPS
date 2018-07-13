package yzj;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 按照NMEA通信协议解析数据帧
 * @author 杨子江
 */
public class NMEAParser {
    private static String dateMsg = null;
    private static String timeMsg = null;

    private static String latitude = null;
    private static String latitudeData = null;

    private static String longitude = null;
    private static String longitudeData = null;

    private static String satelliteNumber = null;
    private static String elevation = null;


    private static boolean dataAvailable = false;
    private static int year,month,date,hourOfDay,minute,second;


    /**
     * 传入RMC数据帧并解析，将解析好的数据存入成员变量。
     * @param rmcMessage
     */
    public static void RMCParser(String rmcMessage){
        String[] msg = rmcMessage.split(",");
        if (msg[2].equals("A")){
            dataAvailable = true;
            latitudeData = msg[3];
            latitude = msg[4];
            longitudeData = msg[5];
            longitude = msg[6];
            dateMsg = msg[9];
            timeMsg = msg[1];
        }
    }

    public static void GGAParser(String ggaMessage){
        String[] msg = ggaMessage.split(",");
        satelliteNumber = msg[7];
        elevation = msg[9];
    }

    private static Calendar timeParser(){
        String utc = dateMsg+timeMsg;
        date = Integer.valueOf(utc.substring(0,2));
        month = Integer.valueOf(utc.substring(2,4));
        year = Integer.valueOf(utc.substring(4,6))+2000;
        hourOfDay = Integer.valueOf(utc.substring(6,8));
        minute = Integer.valueOf(utc.substring(8,10));
        second = Integer.valueOf(utc.substring(10,12));
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
        calendar.set(year,month-1,date,hourOfDay,minute,second);
        return calendar;
    }

    public static Date getDate(){
        if (dataAvailable)
            return timeParser().getTime();
        return null;
    }
    public static String getLatitude(){
        if (dataAvailable)
            return latitude+"--"+latitudeData.substring(0,2)+"°"+latitudeData.substring(2,latitudeData.length())+"′";
        return null;
    }
    public static String getLongitude(){
        if (dataAvailable)
            return longitude+"--"+longitudeData.substring(0,3)+"°"+longitudeData.substring(3,longitudeData.length())+"′";
        return null;
    }

    public static String getSatelliteNumber() {
        return satelliteNumber;
    }

    public static String getElevation() {
        return elevation;
    }
}
