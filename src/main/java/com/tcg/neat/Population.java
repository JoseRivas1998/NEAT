package com.tcg.neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class Population {

    protected List<Player> population;

    private List<ConnectionHistory> innovationHistory;
    private List<Species> species;

    private int generation;

    public Population(int size) {
        population = new ArrayList<>();
        innovationHistory = new ArrayList<>();
        species = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Player p = getPlayer();
            p.brain.mutate(innovationHistory);
            population.add(p);
        }
        this.generation = 0;
    }

    protected abstract Player getPlayer();

    public void naturalSelection() {
        speciate();
        calculateFitness();
        sortSpecies();
        cullSpecies();
        killStaleSpecies();
        killBadSpecies();

        double averageSum = avgFitnessSum();
        List<Player> newPopulation = new ArrayList<>();
        for (Species s : species) {
            newPopulation.add(s.bestPlayer.copyWithoutFitness());
            int numberOfChildren = (int)(s.getAverageFitness() / averageSum * population.size()) - 1;
            for (int i = 0; i < numberOfChildren; i++) {
                newPopulation.add(s.reproduce(innovationHistory));
            }
        }

        while(newPopulation.size() < population.size()) {
            newPopulation.add(species.get(0).reproduce(innovationHistory));
        }
        population = newPopulation;
        generation++;

    }

    public int getGeneration() {
        return generation;
    }

    private void speciate() {
        species.forEach(Species::clearPlayers);
        for (Player player : population) {
            boolean found = false;
            for (int i = 0; i < species.size() && !found; i++) {
                Species s = species.get(i);
                if(s.belongsInSpecies(player)) {
                    s.addPlayer(player);
                    found = true;
                }
            }
            if(!found) {
                species.add(new Species(player));
            }
        }
    }

    private void calculateFitness() {
        population.forEach(Player::calculateFitness);
    }

    private void sortSpecies() {
        species.forEach(Species::sortPlayers);
        species.sort(Comparator.reverseOrder());
    }

    private void killStaleSpecies() {
        int numSpecies = 0;
        Iterator<Species> speciesIterator = species.iterator();
        while(speciesIterator.hasNext()) {
            Species s = speciesIterator.next();
            if(numSpecies > 2) {
                if(s.getStaleness() >= 15) {
                    speciesIterator.remove();
                }
            }
            numSpecies++;
        }
    }

    private void killBadSpecies() {
        double averageSum = avgFitnessSum();

        Iterator<Species> speciesIterator = species.iterator();
        int numSpecies = 0;
        while(speciesIterator.hasNext()) {
            Species s = speciesIterator.next();
            if(numSpecies > 1) {
                if(s.getAverageFitness() / averageSum * population.size() < 1) {
                    speciesIterator.remove();
                }
            }
            numSpecies++;
        }

    }

    private double avgFitnessSum() {
        return species.stream()
                .mapToDouble(Species::getAverageFitness)
                .sum();
    }

    private void cullSpecies() {
        for (Species s : species) {
            s.cull();
            s.sharingFitness();
            s.calculateAverageFitness();
        }
    }

    public Player getBestEver() {
        return population
                .stream()
                .max(Player::compareTo)
                .get();
    }

}
