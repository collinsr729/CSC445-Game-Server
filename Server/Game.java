import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Game {
    public boolean isOver = false;
    public ArrayList<Client> clients;
    DatagramSocket socket;
    ArrayList<int[]> moves = new ArrayList<int[]>();
    String loser = "me";

    public boolean spotsSet(){
        for(Client c: clients)
            if(c.shipLocation==null)
                return false;
        return true;
    }

    public Game(Collection<Client> clients) {
        this.clients = new ArrayList<Client>();
        this.clients.addAll(clients);
        for (Client c : this.clients){
            c.lives=5;
        }
    }

    public void run(DatagramSocket socket) {
        DatagramPacket inPacket = new DatagramPacket(new byte[12],12);
        this.socket=socket;
        try {
            socket.receive(inPacket);
            handlePacket(inPacket);

        } catch (SocketTimeoutException e) {
            System.out.println("run-timeout");
        }  catch (IOException e) {
            e.printStackTrace();
        }
        checkGameOver();
    }

    private void checkGameOver() {
        for(Client c: clients)
            if(c.lives==0){
                isOver=true;
                loser=c.name;
            }
    }

    private void handlePacket(DatagramPacket inPacket) {
        Client c = findMatchedClient(inPacket);
        if(c!=null) {
            byte[] convertedData = DH.convert(inPacket.getData(), c.secret);
            if(convertedData[0]==(byte)'M'){
                int x = convertedData[9];
                int y = convertedData[10];
                System.out.println(x+","+y+":"+ Arrays.toString(convertedData));
                byte[] b;
                String b_str;
                if(makeMove(c,x,y)){
                    b_str = "M"+c.name+"hit"+x+""+y;
                }else
                    b_str = "M"+c.name+"mis"+x+""+y;
                b = b_str.getBytes();

                sendAll(b);
                System.out.println("sent "+Arrays.toString(b));
            }
        }
    }

    private void sendAll(byte[] b) {
        for(Client c: clients){
            byte[] bb = new byte[b.length+1];
            for (int i = 0; i < b.length; i++) {
                bb[i] = b[i];
            }
            if(b[0]==(byte)'M')
                bb[b.length] = c.turnUpdate();
            else
                bb[b.length]= (byte) c.turn;
            byte[] converted = DH.convert(bb,c.secret);
            InetAddress addr = (c.address);
//            System.out.println(Arrays.toString(converted));
            DatagramPacket toSend = new DatagramPacket(converted,converted.length,addr,2791);
            try {
                socket.send(toSend);
            } catch (IOException e) {
                System.out.println("Couldn't update "+c.name);
            }
        }
    }

    private boolean makeMove(Client c, int x, int y) {
        boolean ret = false;
        if(c.turn==0){
            moves.add(new int[]{x,y});
            for (Client cc: clients){
                for(byte[] b : cc.shipLocation)
                    if(b[0]==(byte)x&&b[1]==(byte)y) {
                        cc.lives--;
                        ret = true;
                    }
            }
        }
        return ret;
        //check turn, make move and update all turns, save all moves in an arraylist here to quick check something?
        //returns true or false if hit a ship
    }

    private Client findMatchedClient(DatagramPacket inPacket) {
        for(Client c : clients)
            if(validateSender(DH.convert(inPacket.getData(),c.secret),c.b_public))
             return c;
        return null;
    }
    private static boolean validateSender(byte[] packetData, BigInteger b_public) {
        byte[] bytes = new byte[8];
        for (int i = 1; i < 9; i++) {
            bytes[i-1] = packetData[i];
        }
        return b_public.intValue()==new BigInteger(bytes).intValue();
    }

    public String getLoser() {
        return loser;
    }
}
