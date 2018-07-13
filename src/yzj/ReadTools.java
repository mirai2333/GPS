package yzj;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 读取串口数据
 * @author 杨子江
 */
public class ReadTools implements SerialPortEventListener,Runnable{

    //GPS对应的串行端口名称
    private String serialPortName;
    //GPS对应的串行端口管理器引用
    private CommPortIdentifier serialPortIdentifier = null;
    //打开端口后显示的占用名称，默认为理学院
    private String portOwnerName = "理学院";
    //打开端口的间隔时间，默认2秒
    private int timeIntervals = 0;
    //串口数据波特率，默认为9600
    private int baudRate = 9600;
    //绑定到端口的输入流
    private InputStream inputStream = null;
    //端口数据读取缓存
    byte[] readBuffer=new byte[1024];
    //串行端口的引用
    private SerialPort serialPort;
    //数据队列
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();
    //RMC信号队列
    public static BlockingQueue<String> RMCQueue = new ArrayBlockingQueue<String>(1);
    public static BlockingQueue<String> GGAQueue = new ArrayBlockingQueue<String>(1);

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    public void setPortOwnerName(String portOwnerName) {
        this.portOwnerName = portOwnerName;
    }

    public void setTimeIntervals(int timeIntervals) {
        this.timeIntervals = timeIntervals;
    }
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }


    /**
     * 获取到给定端口号对应的端口管理对象的引用
     * @return 包含所有串行端口的列表
     */
    public List<CommPortIdentifier> getSerialPortIdentifier() {
        CommPortIdentifier portId;
        List<CommPortIdentifier> list = new ArrayList<CommPortIdentifier>();
        Enumeration en = CommPortIdentifier.getPortIdentifiers();
        while (en.hasMoreElements()) {
            portId = (CommPortIdentifier) en.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                list.add(portId);
                if (portId.getName().equals(serialPortName)) {
                    serialPortIdentifier = portId;
                }
            }
        }
        return list;
    }

    /**
     * 打开端口，设置好监听器，开始接收数据，并将数据存储到同步队列中
     */
    public void openSerialPort() throws getPortException{
        try {
            if (serialPortIdentifier == null)
                throw new getPortException("端口获取异常，没有获取到端口");
            serialPort=(SerialPort) serialPortIdentifier.open(portOwnerName, timeIntervals);
            inputStream = serialPort.getInputStream();
            serialPort.addEventListener(this::serialEvent);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1 , SerialPort.PARITY_NONE);
        } catch (PortInUseException e) {
            System.out.println("端口被占用");
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println("IO流获取异常");
            System.out.println(e.toString());
        } catch (TooManyListenersException e) {
            System.out.println("监听器太多异常");
            System.out.println(e.toString());
        } catch (UnsupportedCommOperationException e) {
            System.out.println("参数设置异常");
            System.out.println(e.toString());
        }
    }

    public void closePort(){
        if (serialPortIdentifier == null)
            return;
        serialPort.close();
    }

    public boolean isNullPort(){
        return serialPortIdentifier==null;
    }
    //监听器接口实现
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
            try {
                int len = 0;
                while ( inputStream.available() > 0 ){
                    len = inputStream.read(readBuffer);
                    msgQueue.add(new String(readBuffer,0,len));
                }
            }catch (IOException e){
                System.out.println("监听器读取异常");
            }
        }
    }

    /**
     * 新线程用来获取同步队列中的数据，找到需要的数据帧，例如RMC数据帧
     */
    public void run() {
        String[] msgs;
        try {
            System.out.println("---任务处理线程运行了---");
            while (true) {
                // 如果堵塞队列中存在数据就将其输出
                msgs = msgQueue.take().split(System.getProperty("line.separator"));
                for (String msg : msgs) {
                    if (msg.contains("RMC")) {
                        RMCQueue.clear();
                        RMCQueue.add(msg);
                    }
                    if (msg.contains("GGA")){
                        GGAQueue.clear();
                        GGAQueue.add(msg);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
