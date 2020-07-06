import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class myFrame extends JFrame {

    private int[] nextMove;
    public boolean turn = false;
    JButton[][] buttons;
    final int[] first5 = {0};
    public myFrame(){
        JPanel panel = new JPanel();
        panel.setSize(500,500);
        GridLayout gridLayout = new GridLayout(12,12);
        panel.setLayout(gridLayout);
        buttons = new JButton[12][12];

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                final int finalJ = j;
                final int finalI = i;
                final JButton button = new JButton();
                buttons[i][j]=button;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if(first5[0] <5) {
                            nextMove = new int[]{finalI, finalJ};
                            buttons[finalI][finalJ].setBackground(Color.green);
                            System.out.println(Arrays.toString(nextMove));
                            saveFirstMoves(nextMove,first5[0]);
                            first5[0]++;
                        }else{
                            if(turn){
                                nextMove = new int[]{finalI, finalJ};
                                tryMove(nextMove,button);
                            }else
                                makePopup("Its not your turn I think (says a never wrong computer)");

                        }
                    }
                });
//                gridLayout.addLayoutComponent(i+","+j,button);
                button.setBackground(Color.WHITE);
                panel.add(button);
            }

        }
//        panel.setLayout(gridLayout);
        this.add(panel);
    }
    int[][] moves = new int[5][2];
    private void saveFirstMoves(int[] nextMove, int i) {
        moves[i]=nextMove;
    }
    public byte[] getFirstMoves(){

        byte[] firstMoves = new byte[10];
        for (int i = 0; i < moves.length; i++) {
            for (int j = 0; j < moves[i].length; j++) {
                firstMoves[i*moves[0].length+j]= (byte) moves[i][j];
            }
        }
        return firstMoves;
    }

    void makePopup(String msg){
        JOptionPane.showMessageDialog(null,msg);
    }

    private void tryMove(int[] nextMove, JButton button) {
        Main.makeMove(nextMove);
    }


}
