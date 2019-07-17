package com.tcg.neat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NeuralNetPanel extends JPanel {

    private NeuralNetwork nn;

    public NeuralNetPanel(NeuralNetwork nn) {
        this.nn = nn;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NeuralNetPanel.this.repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        float layerWidth = (float) width / (nn.layers() + 1);
        Map<Neuron, Point> neuronPointMap = new HashMap<>();
        for(int i = 1; i <= nn.layers(); i++) {
            int layerSize = nn.layerSize(i);
            java.util.List<Neuron> layer = nn.getLayer(i);
            layer.sort(Comparator.comparingInt(Neuron::getLayer));
            float layerHeight = (float) height / (layerSize + 1);
            float x = i * layerWidth;
            for (int j = 0; j < layer.size(); j++) {
                Neuron neuron = layer.get(j);
                float y = (j + 1) * layerHeight;
                neuronPointMap.put(neuron, new Point(x, y));
            }
        }
        g.setColor(Color.BLACK);

        nn.connectionGenes().forEach(connectionGene -> {
            Point n1 = neuronPointMap.get(connectionGene.input);
            Point n2 = neuronPointMap.get(connectionGene.output);
            if(connectionGene.isEnabled()) {
                Stroke s = ((Graphics2D) g).getStroke();
                ((Graphics2D) g).setStroke(new BasicStroke((float) Math.abs(connectionGene.getWeight() * 2)));
                if(connectionGene.getWeight() > 0) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.GREEN);
                }
                g.drawLine((int) n1.x, (int) n1.y, (int) n2.x, (int) n2.y);
                ((Graphics2D) g).setStroke(s);
                g.setColor(Color.BLACK);
            }
        });
        for (Map.Entry<Neuron, Point> value : neuronPointMap.entrySet()) {
            Neuron n = value.getKey();
            Point p = value.getValue();
            if(n.isInput()) {
                g.setColor(Color.BLUE);
            } else if(nn.isNeuronOutput(n)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.BLACK);
            }
            g.fillOval((int) p.x, (int) p.y, 8, 8);
            g.drawString(Integer.toString(n.getId()), (int) p.x - 4, (int) p.y - 4);
        }

    }


    private final class Point {
        float x;
        float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
