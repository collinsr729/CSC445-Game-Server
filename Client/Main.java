import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private static InetAddress address;
    private static DatagramSocket socket;
    private static BigInteger[] bigs;
    private static BigInteger secret;
    private static BigInteger b_public;
    private static myFrame frame;
    public static ReentrantLock lock;

    public static void main(String[] args) {
        lock = new ReentrantLock();
	// write your code here
        frame = new myFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500,520);
        try {
            address = (InetAddress.getByName("gee.cs.oswego.edu"));
            socket = new DatagramSocket(2791);

            System.out.println("Whats your name? (2 digit)");
            Scanner in = new Scanner(System.in);
            String name = in.nextLine().substring(0, 2);
            sendWRQ(name);

            DatagramPacket wrqACK = new DatagramPacket(new byte[10], 10);
            socket.receive(wrqACK);
            validateWACK(wrqACK);

            frame.setVisible(true);
            while (!waitForGameStart()) {
            }
            frame.makePopup("Enter your starting positions if you haven't (5 ships)");

            makeSpacket();//sends moves to server


        }catch (IOException e) {

        }
            DatagramPacket inPacket = new DatagramPacket(new byte[10], 10);
            boolean gameOver = false;
            String winner = "me";
            while (!gameOver) {

                try {
                    socket.receive(inPacket);
                    byte[] convertedData = DH.convert(inPacket.getData(), secret);
                    if(convertedData[0]==(byte)'e'
                    && convertedData[1]==(byte)'n'
                    && convertedData[2]==(byte)'d'){
                        gameOver=true;
                        winner = (char)convertedData[3]+""+(char)convertedData[4];
                    }else if(convertedData[0]==(byte)'M'){
                        String s1 = "",s2="",x="",y="";
                        for (int i = 1; i < convertedData.length; i++) {
                            if(i<3)
                                s1+=(char)convertedData[i];
                            else if(i<6)
                                s2 += (char)convertedData[i];
                            else if(i==6)
                                x+=(char)convertedData[i];
                            else if(i==7)
                                y+=(char)convertedData[i];
                            else if(i==8)
                                frame.turn=(0==(int)convertedData[i]);
                        }
                        if(s2.contains("h")){
                            System.out.println(s1+" hit a ship at "+x+","+y);
                            frame.buttons[Integer.parseInt(x)][Integer.parseInt(y)].setBackground(Color.red);
                        }else {
                            System.out.println(s1 + " hit water at " + x + "," + y);
                            frame.buttons[Integer.parseInt(x)][Integer.parseInt(y)].setBackground(Color.blue);
                        }
                        if(frame.turn){
                            frame.makePopup("Your turn");
                        }
                    }
                }catch (SocketTimeoutException e) {
                    System.out.println("TIMEOUT reseting socket");
                    socket.close();
                    socket=null;
                    try {
                        socket = new DatagramSocket(2791);
                    } catch (SocketException socketException) {
                        System.out.println("couldnt reset socket");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }//while ! gameOver
        System.out.println("Game Over... "+winner+" loses!!");
    }

    private static void makeSpacket() {
//        int last = frame.first5[0];
        while((frame.first5[0] <5)) {
            System.out.print("");
        }
        byte[] bytes = frame.getFirstMoves();

        System.out.println("got moves, sending");

        byte[] b = new byte[11];
        b[0]=(byte)'S';
        for (int i = 1; i < b.length; i++) {
            b[i]=bytes[i-1];
        }
        try {
            socket.send(new DatagramPacket(DH.convert(b,secret),b.length,address,2791));
        } catch (IOException e) {
            System.out.println("couldn't send s-packet");
        }
    }

    private static boolean waitForGameStart() {
        byte[] recStart = new byte[9];
        try {
            socket.setSoTimeout(2000);
            socket.receive(new DatagramPacket(recStart,recStart.length));
            byte[] converted = DH.convert(recStart,secret);
            System.out.println(Arrays.toString(converted));
            String s = byteToString(converted);
            if(s.contains("starting")){
                frame.turn=((int)converted[8]==0);
                System.out.println(frame.turn);
                System.out.println(converted[8]);
                return true;
            }
        } catch (IOException e) {
            System.out.println("waiting");

        }
        return false;
    }

    private static String byteToString(byte[] recStart) {
        String s = "";
        for (byte b : recStart){
            s+= (char)b;
        }
        return s;
    }

    private static void validateWACK(DatagramPacket wrqACK) {
        byte[] ackData = wrqACK.getData();
        System.out.println("ack:"+Arrays.toString(ackData));
        byte[] bytes = new byte[8];
        if(ackData[0]=='O'&&ackData[1]=='K'){
            for (int i = 0; i < 8; i++) {
                bytes[i]=ackData[i+2];
            }
            bigs[1] = new BigInteger(bytes);
            secret = DH.getSharedSecretKey(bigs[0],BigInteger.valueOf(77),bigs[1]);
            System.out.println(Arrays.toString(bigs));
        }
    }

    private static void sendWRQ(String name) {
        DH dh = new DH(BigInteger.valueOf('&'));
        bigs = dh.generateRandomKeys(BigInteger.valueOf(77));
        b_public=bigs[1];
        byte[] b = new byte[11];
        b[0]='W';
        byte[] bigsByte = bigs[1].toByteArray();
        int num = 9-bigsByte.length;

        for(int i = 1;i<b.length;i++){
            if(i<num)
            b[i]=0;
            else if(i<9)
                b[i] = bigsByte[i-num];
            else{
                b[i]= (byte) name.charAt(i-9);
            }
        }
        System.out.println(Arrays.toString(bigs));
        try {
            socket.send(new DatagramPacket(b, b.length, address, 2791));
        }catch (IOException e){
            System.out.println("didnt send wrq");
        }
    }

    public static void makeMove(int[] move){
        byte[] b = new byte[11];
        byte[] bBytes = b_public.toByteArray();
        int num = 9-bBytes.length;
        System.out.println("were sending a move, see if the server cares");
        for (int i = 0; i < b.length; i++) {
            if(i<1)
                b[i]='M';
            else if(i<num)
                b[i]= 0;
            else if(i<9)
                b[i]=bBytes[i-num];
            else
                b[i]= (byte) move[i-9];
        }

        System.out.println("were sending a move, see if the server cares");
        try {
            socket.send(new DatagramPacket(DH.convert(b,secret),b.length,address,2791));
        } catch (IOException e) {
            System.out.println("couldnt send move... sad");
        }
    }
}
