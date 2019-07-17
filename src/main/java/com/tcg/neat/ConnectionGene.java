package com.tcg.neat;

import java.util.Random;

public final class ConnectionGene {

    final Neuron input;
    final Neuron output;
    private double weight;
    private boolean enabled;
    private int innovationNumber; // TODO figure this guy out

    public ConnectionGene(Neuron input, Neuron output, double weight, boolean enabled, int innovationNumber) {
        this.input = input;
        this.output = output;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
    }

    public ConnectionGene(Neuron input, Neuron output, Random random, boolean enabled, int innovationNumber) {
        this.input = input;
        this.output = output;
        this.weight = 2.0 * random.nextDouble() - 1;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
    }

    public boolean isNeuronOutput(Neuron neuron) {
        return output.equals(neuron);
    }

    public boolean isNeronInput(Neuron neuron) {
        return input.equals(neuron);
    }

    public void mutateWeight(Random random) {
        if(random.nextDouble() < 0.9) {
            this.weight += (2 * random.nextGaussian() - 1) / 50.0;
            if(this.weight > 1) {
                this.weight = 1;
            }
            if(this.weight < -1) {
                this.weight = -1;
            }
        } else {
            this.weight = 2 * random.nextDouble() - 1;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        this.enabled = false;
    }

    public double getWeight() {
        return this.weight;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    @Override
    public String toString() {
        return input + "->" + output;
    }
}
