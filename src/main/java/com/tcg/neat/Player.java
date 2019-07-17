package com.tcg.neat;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public abstract class Player implements Comparable<Player> {

    protected double fitness;
    NeuralNetwork brain;

    public Player(int inputs, int outputs) {
        fitness = 0;
        brain = new NeuralNetwork(inputs, outputs);
    }

    public double[] engageNeuralNetwork(double[] inputs, DoubleUnaryOperator activationFunction) {
        return brain.feedForward(inputs, activationFunction);
    }

    public void calculateFitness() {}

    public double getFitness() {
        return fitness;
    }

    void divideFitness(double amount) {
        this.fitness /= amount;
    }

    public double[] feedForward(double[] inputs, DoubleUnaryOperator activationFunction) {
        return brain.feedForward(inputs, activationFunction);
    }

    public void mutate(List<ConnectionHistory> connectionHistory) {
        brain.mutate(connectionHistory);
    }

    protected abstract Player newPlayer();

    protected abstract Player copySelf();

    public Player copy() {
        Player player = copyWithoutFitness();
        player.fitness = this.fitness;
        return player;
    }

    public Player copyWithoutFitness() {
        Player player = copySelf();
        player.brain = brain.copy();
        return player;
    }

    public static Player crossOver(Player p1, Player p2) {
        Player player = p1.newPlayer();
        if(p1.getFitness() > p2.getFitness()) {
            player.brain = NeuralNetwork.crossOver(p1.brain, p2.brain);
        } else {
            player.brain = NeuralNetwork.crossOver(p2.brain, p1.brain);
        }
        return player;
    }

    @Override
    public int compareTo(Player o) {
        return Double.compare(this.fitness, o.fitness);
    }
}
