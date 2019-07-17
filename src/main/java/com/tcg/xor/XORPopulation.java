package com.tcg.xor;

import com.tcg.neat.Player;
import com.tcg.neat.Population;

public class XORPopulation extends Population {

    public XORPopulation(int size) {
        super(size);
    }

    public double step() {
        double totalError = 0;
        for (Player player : population) {
            ((XORPlayer) player).doXOR();
            totalError += ((XORPlayer) player).getError();
        }
        naturalSelection();
        return totalError / population.size();
    }

    @Override
    protected Player getPlayer() {
        return new XORPlayer();
    }
}
