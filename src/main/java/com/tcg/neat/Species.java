package com.tcg.neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Species implements Comparable<Species> {

    private List<Player> players;
    Player bestPlayer;
    private NeuralNetwork representative;

    private double bestFitness;
    private int staleness;
    private double averageFitness;

    // Coefficients for testing compatibility
    private static final double C1 = 1;
    private static final double C2 = 1;
    private static final double C3 = 0.5;
    private static final double THRESHOLD = 3;

    private static final double DUPLICATION_CHANCE = 0.15;

    public Species(Player player) {
        players = new ArrayList<>();
        bestPlayer = player.copy();
        representative = bestPlayer.brain.copy();
        bestFitness = bestPlayer.getFitness();
        averageFitness = 0;
        staleness = 0;
    }

    void addPlayer(Player player) {
        players.add(player);
    }

    public boolean belongsInSpecies(Player player) {

        double normalizer;

        int numRepGenes = representative.connectionGenes().size();
        int numOtherGenes = player.brain.connectionGenes().size();

        if(numRepGenes > numOtherGenes) {
            normalizer = (numRepGenes > 20) ? numRepGenes - 20 : 1;
        } else {
            normalizer = (numOtherGenes > 20) ? numOtherGenes - 20 : 1;
        }

        int excess = NeuralNetwork.excessGenes(representative, player.brain);
        int disjoint = NeuralNetwork.disjointGenes(representative, player.brain);
        double avgWeightDiff = weightDiff(player.brain);

        double compatibility = ((C1 * excess) / normalizer) + ((C2 * disjoint) / normalizer) + (C3 * avgWeightDiff);

        return compatibility <= THRESHOLD;
    }

    private double weightDiff(NeuralNetwork nn) {
        int numShared = 0;
        double totalWeightDiff = 0.0;
        for (ConnectionGene repGene : representative.connectionGenes()) {
            boolean found = false;
            for (int i = 0; i < nn.connectionGenes().size() && !found; i++) {
                ConnectionGene otherGene = nn.connectionGenes().get(i);
                if(repGene.getInnovationNumber() == otherGene.getInnovationNumber()) {
                    found = true;
                    numShared++;
                    totalWeightDiff += Math.abs(repGene.getWeight() - otherGene.getWeight());
                }
            }
        }
        return totalWeightDiff / numShared;
    }

    void sortPlayers() {
        players.sort(Comparator.reverseOrder());


        if(players.size() == 0) {
            staleness = 990;
        } else if(players.get(0).getFitness() > bestFitness) {
            staleness = 0;
            bestPlayer = players.get(0).copy();
            representative = bestPlayer.brain.copy();
            bestFitness = bestPlayer.getFitness();
        } else {
            staleness++;
        }

    }

    void calculateAverageFitness() {
        averageFitness = fitnessSum() / players.size();
    }

    double fitnessSum() {
        return players.stream()
                .mapToDouble(Player::getFitness)
                .sum();
    }

    private Player selectParent() {
        double random = Math.random() * fitnessSum();
        double runningSum = 0;
        for (Player player : players) {
            runningSum += player.getFitness();
            if(runningSum > random) {
                return player;
            }
        }
        return players.get(0);
    }

    public Player reproduce(List<ConnectionHistory> connectionHistory) {
        Player offspring;
        if(Math.random() < DUPLICATION_CHANCE) {
            offspring = selectParent().copy();
        } else {
            Player p1 = selectParent();
            Player p2 = selectParent();
            offspring = Player.crossOver(p1, p2);
        }
        offspring.mutate(connectionHistory);
        return offspring;
    }

    public void sharingFitness() {
        players.forEach(player -> player.divideFitness(players.size()));
    }

    void cull() {
        int halfSize = players.size() / 2;
        players.subList(halfSize, players.size()).clear();
    }

    void clearPlayers() {
        this.players.clear();
    }

    int getStaleness() {
        return this.staleness;
    }

    double getAverageFitness() {
        return this.averageFitness;
    }

    @Override
    public int compareTo(Species o) {
        return this.bestPlayer.compareTo(o.bestPlayer);
    }
}
