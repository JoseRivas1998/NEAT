package com.tcg.xor;

import com.tcg.neat.ActivationFunction;
import com.tcg.neat.Player;

import java.util.Arrays;

public class XORMain {

    public static void main(String[] args) {
        System.out.println("Generating initial population");
        XORPopulation pop = new XORPopulation(1000);
        System.out.println("Done");
        for (int i = 0; i < 100; i++) {
            int gen = pop.getGeneration();
            pop.step();
            ((XORPlayer) pop.getBestEver()).doXOR();
            double best = ((XORPlayer) pop.getBestEver()).getError();
            System.out.printf("%d: %.2f\n", gen, best);
        }
        double[][] inputs = {
                {1.0, 1.0},
                {1.0, 0.0},
                {0.0, 1.0},
                {0.0, 0.0}
        };
        Player best = pop.getBestEver();
        for (double[] input : inputs) {
            System.out.print(Arrays.toString(input) + ": ");
            double output = best.feedForward(input, ActivationFunction.SIGMOID.activationFunction)[0];
            System.out.println(output);
        }

    }

}
