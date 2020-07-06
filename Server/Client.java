import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;

public class Client {
    public int lives;
    public int turn = 0;
    public InetAddress address;
    BigInteger b_public;
     BigInteger secret;
     ArrayList<byte[]> moves;
     ArrayList<byte[]> shipLocation;
     String name;


    public Client(BigInteger sharedSecretKey, String name) {
        secret = sharedSecretKey;
        this.name=name;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    BigInteger getSecret(){
        return secret;
    }
     void setB(BigInteger b){
        b_public=b;
    }
     BigInteger getB(){
        return b_public;
    }

    public void addMove(byte convertedDatum, byte convertedDatum1) {
        if(moves==null)
            moves = new ArrayList<byte[]>();
        moves.add(new byte[]{convertedDatum,convertedDatum1});
    }
    public void addShip(byte convertedDatum, byte convertedDatum1) {
        if(shipLocation==null)
            shipLocation = new ArrayList<byte[]>();
        shipLocation.add(new byte[]{convertedDatum,convertedDatum1});
    }

    public ArrayList<byte[]> getMoves() {
        return moves;
    }

    public byte turnUpdate() {
        if(turn==0)
            turn= MainServer.PLAYER_COUNT;
        turn--;
        return (byte) turn;
    }
}
