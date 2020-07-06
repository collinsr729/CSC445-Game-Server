import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainServer {
    static DatagramSocket socket = null;
//    private static InetAddress turn;
    public static final int PLAYER_COUNT = 3;

    public static void main(String[] args) {
        DatagramPacket packet = new DatagramPacket(new byte[11],11);

        try {
            socket = new DatagramSocket(2791);
            socket.setSoTimeout(5000);

        while(map.size()<PLAYER_COUNT) {
            try {
                socket.receive(packet);
                handleJoin(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("join-timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        } catch (SocketException e) {
            System.out.println("total fail");
        }

        sendAll("starting".getBytes());

        Game game = new Game(map.values());
        int ships=0;
        while(!game.spotsSet()) {
            try {
                socket.receive(packet);
                handleSpots(packet);
            }  catch (SocketTimeoutException e) {
                System.out.println("spots-timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Spots picked");

        while (!game.isOver) {
            game.run(socket);
        }
        String endGame = "end"+game.getLoser();
        sendAll(endGame.getBytes());
        sendAll(endGame.getBytes());
    }


    private static void sendAll(byte[] bytes) {//sends all message with a turn attached
        int turn=0;
        for(InetAddress i : map.keySet()) {
            byte[] newBytes = new byte[bytes.length+1];
            for (int j = 0; j < bytes.length; j++) {
                newBytes[j]= bytes[j];
            }
            map.get(i).turn = turn;
            newBytes[bytes.length]= (byte) turn;
            turn++;
            try {
//                System.out.println(Arrays.toString(newBytes));
//                System.out.println(Arrays.toString(DH.convert(newBytes,map.get(i).secret)));
                socket.send(new DatagramPacket(DH.convert(newBytes,map.get(i).secret),newBytes.length,i,2791));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Sent "+ Arrays.toString(bytes));
    }

    static HashMap<InetAddress, Client> map = new HashMap<InetAddress,Client>();
//    static HashMap<InetAddress, BigInteger> map = new HashMap<InetAddress, BigInteger>();
//    static HashMap<InetAddress, BigInteger> mapOfBPublic = new HashMap<InetAddress, BigInteger>();

    private static void handleJoin(DatagramPacket packet){
        byte[] packetData = packet.getData();
        InetAddress packetAddr = packet.getAddress();
        if(packetData[0]== (byte)'W') {
            if (!map.containsKey(packetAddr)) {
                byte[] bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    bytes[i] = packetData[i + 1];
                }
                String name = "";
                for (int i = 9; i < packetData.length; i++) {
                    name += (char) packetData[i];
                }
                System.out.println("Adding "+name+":"+packetAddr.getHostAddress());
                DH dh = new DH(BigInteger.valueOf('&'));
                BigInteger[] bigs = dh.generateRandomKeys(BigInteger.valueOf(77));
//                DH.getSharedSecretKey(bigs[0],BigInteger.valueOf(77),new BigInteger(bytes));
                map.put(packetAddr, new Client(DH.getSharedSecretKey(bigs[0], BigInteger.valueOf(77), new BigInteger(bytes)),name));
                map.get(packetAddr).setB(new BigInteger(bytes));
                map.get(packetAddr).setAddress(packetAddr);
                map.get(packetAddr).lives=5;
                System.out.println(Arrays.toString(bigs) + ":" + new BigInteger(bytes));
                sendWRQResponse(bigs[1], packet);
            }
        }
    }

    private static void handleSpots(DatagramPacket packet) {
        if(map.containsKey(packet.getAddress())) {
            byte[] convertedData = DH.convert(packet.getData(),map.get(packet.getAddress()).secret);
            Client currentClient = map.get(packet.getAddress());
            System.out.println("Adding "+currentClient.name+" spots");
            for (int i = 1; i < convertedData.length; i += 2) {
                currentClient.addShip(convertedData[i], convertedData[i + 1]);
            }
        }
    }

    private static void sendWRQResponse(BigInteger bigInteger,DatagramPacket p) {
        byte[] b = new byte[10];
        b[0]='O';
        b[1]='K';
        byte[] bigsByte = bigInteger.toByteArray();
        int num = b.length-bigsByte.length;
        for (int i = 2; i < num; i++) {
            b[i] = 0;
        }
        for (int i = num; i < b.length; i++) {
            b[i]=bigsByte[i-num];
        }
        try {
            socket.send(new DatagramPacket(b,b.length,p.getAddress(),p.getPort()));
        } catch (IOException e) {
            System.out.println("couldnt respond to a WRQ");
        }
    }
    static HashMap<InetAddress,ArrayList<byte[]>> moves;
    private static void handleMove(InetAddress address, byte[] packetData) {
        if(0==map.get(address).turn){
            moves.get(address).add(new byte[]{packetData[1],packetData[2]});
            try {
                socket.send(new DatagramPacket("true".getBytes(),4,address,2791));
            } catch (IOException e) {
                System.out.println("couldnt send true");
                try {
                    socket.send(new DatagramPacket("true".getBytes(),4,address,2791));
                } catch (IOException ee) {
                    System.out.println("couldnt send true");
                }
            }
        }else
            try {
                socket.send(new DatagramPacket("false".getBytes(),5,address,2791));
            } catch (IOException e) {
                System.out.println("couldnt send false");
            }

    }

    private static boolean validateSender(byte[] packetData, BigInteger b_public) {
        byte[] bytes = new byte[8];
        for (int i = 1; i < 9; i++) {
            bytes[i-1] = packetData[i];
        }
        return b_public.intValue()==new BigInteger(bytes).intValue();
    }

}

