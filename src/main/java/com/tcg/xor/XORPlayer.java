package com.tcg.xor;

import com.tcg.neat.ActivationFunction;
import com.tcg.neat.Player;

public class XORPlayer extends Player {

    private double error;

    public XORPlayer() {
        super(2, 1);
    }

    public void doXOR() {
        double[][] inputs = {
                {1.0, 1.0},
                {1.0, 0.0},
                {0.0, 1.0},
                {0.0, 0.0}
        };

        double[] expectedOutputs = {0.0, 1.0, 1.0, 0.0};

        double[][] outputs = new double[inputs.length][];

        error = 0;
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = feedForward(inputs[i], ActivationFunction.SIGMOID.activationFunction);
            error += Math.abs(expectedOutputs[i] - outputs[i][0]);
        }


    }

    public double getError() {
        return error;
    }

    @Override
    public void calculateFitness() {
        this.fitness = 4 - error;
        this.fitness = this.fitness *this.fitness;
    }

    @Override
    protected Player newPlayer() {
        return new XORPlayer();
    }

    @Override
    protected Player copySelf() {
        return new XORPlayer();
    }

}
