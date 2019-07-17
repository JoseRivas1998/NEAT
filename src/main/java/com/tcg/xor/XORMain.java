package com.tcg.xor;

import com.tcg.neat.ActivationFunction;

import java.util.Arrays;

public class XORMain {

    public static void main(String[] args) {
        System.out.println("Generating initial population");
        XORPopulation pop = new XORPopulation(1000);
        System.out.println("Done");
        for (int i = 0; i < 100; i++) {
            int gen = pop.getGeneration();
            double avgError = pop.step();
            System.out.printf("Generation %d: %.5f", gen, avgError);
        }
        double[] outputs = pop.getBestEver().feedForward(new double[]{1.0, 0.0}, ActivationFunction.SIGMOID.activationFunction);
        System.out.println(Arrays.toString(outputs));
    }

}
