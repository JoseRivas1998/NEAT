package com.tcg.neat;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Tester {

    public static void main(String[] args) {
        NeuralNetwork nn = new NeuralNetwork(2, 1);
        List<ConnectionHistory> connectionHistories = new ArrayList<>();
        JPanel panel = new NeuralNetPanel(nn);
        JFrame frame = new JFrame("Oval Sample");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setVisible(true);
        new Thread(() -> {
            while(frame.isShowing()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nn.mutate(connectionHistories);
                nn.feedForward(new double[]{1, 1}, operand -> operand);
                panel.repaint();
            }
        }).start();
    }

}
