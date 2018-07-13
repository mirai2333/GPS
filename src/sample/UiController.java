package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import yzj.ReadTools;
import yzj.getPortException;

public class UiController implements Initializable {

    @FXML
    private Label gpsTime;
    @FXML
    private Label latitude;
    @FXML
    private Label longitude;
    @FXML
    private Label elevation;
    @FXML
    private Label satelliteNumber;

    @FXML
    private Label hint;
    @FXML
    private TextField portNum;
    @FXML
    private TextField timeSpace;

    private String rmcMessage;
    private String ggaMessage;
    private int timeSp = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ggaMessage = ReadTools.GGAQueue.take();
                        DataDao.ParserGgaMessage(ggaMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        rmcMessage = ReadTools.RMCQueue.take();
                        DataDao.ParserRmcMessage(rmcMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SimpleDateFormat simdate = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat simtime = new SimpleDateFormat("HH:mm:ss");
                    Date date = DataDao.getDate();
                    if (date != null){
                        String stime = simtime.format(date);
                        String sdate = simdate.format(date);
                        Runtime run = Runtime.getRuntime();
                        try {
                            run.exec("cmd /c date " + sdate);
                            run.exec("cmd /c time " + stime);
                            if (timeSp < 0)
                                timeSp = 10;
                            Thread.sleep(timeSp * 1000);
                            System.out.println(stime+"--"+sdate);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }).start();

    }

    // When user click on myButton
    // this method will be called.
    public void refresh(ActionEvent event) {

        if (DataDao.isNull()) {
            String portName = portNum.getText().trim().toUpperCase();
            DataDao.setPortName(portName);
            try {
                DataDao.getData();
            } catch (getPortException e) {
                hint.setText("端口号错误");
                System.out.println(e.toString());
            }
        }
        hint.setText("");
        if (rmcMessage == null || ggaMessage == null)
            return;
        DataDao.ParserRmcMessage(rmcMessage);
        DataDao.ParserGgaMessage(ggaMessage);

        Date now = DataDao.getDate();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTimeString = df.format(now);
        // Show in VIEW
        gpsTime.setText(dateTimeString);
        latitude.setText(DataDao.getLatitude());
        longitude.setText(DataDao.getLongitude());
        elevation.setText(DataDao.getElevation());
        satelliteNumber.setText(DataDao.getSatelliteNumber());
    }

    public void setTime(ActionEvent event) {
        timeSp = Integer.parseInt(timeSpace.getText());
        SimpleDateFormat simdate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simtime = new SimpleDateFormat("HH:mm:ss");
        Date date = DataDao.getDate();
        if (date == null)
            return;
        String stime = simtime.format(date);
        String sdate = simdate.format(date);
        Runtime run = Runtime.getRuntime();
        try {
            run.exec("cmd /c date " + sdate);
            run.exec("cmd /c time " + stime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}