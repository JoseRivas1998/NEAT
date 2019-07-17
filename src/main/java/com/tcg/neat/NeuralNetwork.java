package com.tcg.neat;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class NeuralNetwork {

    private enum RandomSingleton {
        INSTANCE;

        Random random;

        RandomSingleton() {
            this.random = new Random();
        }
    }

    private static final int INPUT_LAYER = 1;
    private List<Neuron> neurons;
    private List<ConnectionGene> connections;

    private int layers;

    private int inputs;
    private int outputs;

    private int biasId;

    private static final double WEIGHT_MUTATION_CHANCE = 0.8;
    private static final double CONNECTION_MUTATION_CHANCE = 0.05;
    private static final double NEURON_MUTATION_CHANCE = 0.01;

    private int innovationNumber;

    public NeuralNetwork(int inputs, int outputs) {
        neurons = new ArrayList<>();
        connections = new ArrayList<>();
        this.inputs = inputs;
        this.outputs = outputs;
        for (int i = 0; i < inputs; i++) {
            Neuron input = addNeuron();
            input.layer = INPUT_LAYER;
        }
        for (int i = 0; i < outputs; i++) {
            Neuron output = addNeuron();
            output.layer = INPUT_LAYER + 1;

        }
        Neuron bias = addNeuron();
        bias.layer = INPUT_LAYER;
        biasId = bias.getId();
        layers = 2;
        innovationNumber = 1;
    }

    public double[] feedForward(double[] inputs, DoubleUnaryOperator activationFunction) {
        if(inputs.length != this.inputs) {
            throw new IllegalArgumentException("Input array length does not match input nodes.");
        }
        List<Neuron> inputNeurons = inputNeurons();
        for (int i = 0; i < inputs.length; i++) {
            inputNeurons.get(i).setInputSum(inputs[i]);
        }
        getNeuronById(biasId).setInputSum(1.0);
        for(int layer = INPUT_LAYER; layer <= layers; layer++) {
            List<Neuron> layerNeurons = getLayer(layer);
            for (Neuron layerNeuron : layerNeurons) {
                if(layer != INPUT_LAYER) {
                    layerNeuron.applyActivationFunction(activationFunction);
                }
                List<ConnectionGene> outputGenes = getOutputGenes(layerNeuron);
                for (ConnectionGene outputGene : outputGenes) {
                    outputGene.output.addToInputSum(layerNeuron.getInputSum() * outputGene.getWeight());
                }
            }
        }
        double[] outputs = new double[this.outputs];
        List<Neuron> outputNeurons = outputNeurons();

        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = outputNeurons.get(i).getInputSum();
        }

        return outputs;
    }

    private Neuron addNeuron() {
        Neuron n = new Neuron(neurons.size() + 1);
        neurons.add(n);
        return n;
    }

    private List<Neuron> inputNeurons() {
        return getLayer(neuron -> neuron.layer == INPUT_LAYER && neuron.getId() != biasId);
    }

    private List<Neuron> outputNeurons() {
        return getLayer(layers);
    }

    List<Neuron> getLayer(int layer) {
        return getLayer(neuron -> neuron.getLayer() == layer);
    }

    private List<Neuron> getLayer(Predicate<Neuron> filter) {
        return neurons
                .stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(Neuron::getId))
                .collect(Collectors.toList());
    }

    void mutate(List<ConnectionHistory> connectionHistory) {
        if (connections.size() == 0) {
            mutateConnection(connectionHistory);
            return;
        }

        if (RandomSingleton.INSTANCE.random.nextDouble() < WEIGHT_MUTATION_CHANCE) {
            connections
                    .stream()
                    .filter(ConnectionGene::isEnabled)
                    .forEach(connectionGene -> connectionGene.mutateWeight(RandomSingleton.INSTANCE.random));
        }

        if (RandomSingleton.INSTANCE.random.nextDouble() < CONNECTION_MUTATION_CHANCE) {
            mutateConnection(connectionHistory);
        }

        if (RandomSingleton.INSTANCE.random.nextDouble() < NEURON_MUTATION_CHANCE) {
            mutateNeuron(connectionHistory);
        }

    }

    private void mutateNeuron(List<ConnectionHistory> connectionHistory) {
        int geneIndex;
        int numTries = 0;
        do {
            numTries++;
            geneIndex = RandomSingleton.INSTANCE.random.nextInt(connections.size());
        } while (!connections.get(geneIndex).isEnabled() && numTries < 50);
        ConnectionGene connectionGene = connections.get(geneIndex);
        int nextLayer = connectionGene.input.layer + 1;
        if (nextLayer == connectionGene.output.layer) {
            neurons.stream()
                    .filter(neuron -> neuron.layer >= nextLayer)
                    .forEach(neuron -> {
                        neuron.layer++;
                    });
            layers++;
        }
        connectionGene.disable();
        Neuron newNeuron = addNeuron();
        newNeuron.layer = nextLayer;
        addConnection(connectionGene.input, newNeuron, connectionHistory);
        addConnection(newNeuron, connectionGene.output, connectionHistory);
    }

    private void mutateConnection(List<ConnectionHistory> connectionHistory) {
        if(fullyConnected()) {
            return;
        }
        Neuron n1;
        Neuron n2;
        do {
            n1 = neurons.get(RandomSingleton.INSTANCE.random.nextInt(neurons.size()));
            n2 = neurons.get(RandomSingleton.INSTANCE.random.nextInt(neurons.size()));
        } while (!validConnection(n1, n2));

        if (n1.layer < n2.layer) {
            addConnection(n1, n2, connectionHistory);
        } else {
            addConnection(n2, n1, connectionHistory);
        }
    }

    private boolean validConnection(Neuron n1, Neuron n2) {
        return !(n1.layer == n2.layer || neuronsConnected(n1, n2));
    }

    private boolean neuronsConnected(Neuron n1, Neuron n2) {
        return connections
                .stream()
                .filter(ConnectionGene::isEnabled)
                .anyMatch(gene -> (gene.input == n1 && gene.output == n2) || (gene.input == n2 && gene.output == n1));
    }

    private boolean isNeuronFullyConnected(Neuron neuron) {
        return isNeuronOutput(neuron) || neurons.stream()
                .filter(neuron1 -> neuron1.layer >= neuron.layer + 1)
                .allMatch(neuron1 -> neuronsConnected(neuron, neuron1));
    }

    private boolean fullyConnected() {
        return neurons
                .stream()
                .allMatch(this::isNeuronFullyConnected);
    }

    private List<ConnectionGene> getOutputGenes(Neuron input) {
        return connections
                .stream()
                .filter(connectionGene -> connectionGene.input.equals(input))
                .collect(Collectors.toList());
    }

    boolean isNeuronOutput(Neuron n) {
        return n.layer == layers;
    }

    private Neuron getNeuronById(int id) {
        boolean found = false;
        Neuron n = null;
        for (int i = 0; i < neurons.size(); i++) {
            if (neurons.get(i).getId() == id) {
                found = true;
                n = neurons.get(i);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Neuron not found");
        }
        return n;
    }

    private void addConnection(Neuron input, Neuron output, List<ConnectionHistory> connectionHistory) {
        int innovationNumber = generateInnovationNumber(input, output, connectionHistory);
        connections.add(new ConnectionGene(input, output, RandomSingleton.INSTANCE.random, true, innovationNumber));
    }

    private void addConnection(int input, int output, double weight, boolean enabled, int innovationNumber) {
        Neuron inNeuron = getNeuronById(input);
        Neuron outNeuron = getNeuronById(output);
        connections.add(new ConnectionGene(inNeuron, outNeuron, weight, enabled, innovationNumber));
    }

    private void addConnection(ConnectionGene connectionGene) {
        addConnection(connectionGene.input.getId(), connectionGene.output.getId(), connectionGene.getWeight(), connectionGene.isEnabled(), connectionGene.getInnovationNumber());
    }

    private int generateInnovationNumber(Neuron input, Neuron output, List<ConnectionHistory> connectionHistory) {
        boolean found = false;
        int result = innovationNumber;
        for(int i = 0; i < connectionHistory.size() && !found; i++) {
            if(connectionHistory.get(i).matches(this, input, output)) {
                found = true;
                result = connectionHistory.get(i).innovationNumber;
            }
        }

        if(!found) {
            connectionHistory.add(new ConnectionHistory(input.getId(), output.getId(), result, innovationNumbers()));
            innovationNumber++;
        }
        return result;
    }

    public int layerSize(int layer) {
        return (int) neurons
                .stream()
                .filter(neuron -> neuron.isLayer(layer))
                .count();
    }

    List<ConnectionGene> connectionGenes() {
        return new ArrayList<>(connections);
    }

    int layers() {
        return this.layers;
    }

    int allGenes() {
        return this.connections.size();
    }

    List<Integer> innovationNumbers() {
        return connections
                .stream()
                .map(ConnectionGene::getInnovationNumber)
                .collect(Collectors.toList());
    }

    public int numInputs() {
        return this.inputs;
    }

    public int numOutputs() {
        return this.outputs;
    }

    private static int maxSharedInnovation(NeuralNetwork n1, NeuralNetwork n2) {
        int maxSharedInnovation = 0;
        for(ConnectionGene n1Connection : n1.connections) {
            boolean found = false;
            for (int i = 0; i < n2.connections.size() && !found; i++) {
                ConnectionGene n2Connection = n2.connections.get(i);
                if(n1Connection.getInnovationNumber() == n2Connection.getInnovationNumber()) {
                    found = true;
                    maxSharedInnovation = Math.max(maxSharedInnovation, n1Connection.getInnovationNumber());
                }
            }
        }
        return maxSharedInnovation;
    }

    private static List<ConnectionGene> getUnlikeConnections(NeuralNetwork n1, NeuralNetwork n2) {
        List<ConnectionGene> unlikeConnections = new ArrayList<>();

        for(ConnectionGene n1Connection : n1.connections) {
            boolean found = false;
            for (int i = 0; i < n2.connections.size() && !found; i++) {
                ConnectionGene n2Connection = n2.connections.get(i);
                if(n1Connection.getInnovationNumber() == n2Connection.getInnovationNumber()) {
                    found = true;
                }
            }
            if(!found) {
                unlikeConnections.add(n1Connection);
            }
        }

        for(ConnectionGene n2Connection : n2.connections) {
            boolean found = false;
            for(int i = 0; i < n1.connections.size() && !found; i++) {
                ConnectionGene n1Connection = n1.connections.get(i);
                if(n1Connection.getInnovationNumber() == n2Connection.getInnovationNumber()) {
                    found = true;
                }
            }
            if(!found) {
                unlikeConnections.add(n2Connection);
            }
        }

        return unlikeConnections;
    }

    public static int excessGenes(NeuralNetwork n1, NeuralNetwork n2) {
        int maxInnovation = maxSharedInnovation(n1, n2);
        List<ConnectionGene> unlikeGenes = getUnlikeConnections(n1, n2);
        return (int) unlikeGenes
                .stream()
                .filter(connectionGene -> connectionGene.getInnovationNumber() > maxInnovation)
                .count();
    }

    public static int disjointGenes(NeuralNetwork n1, NeuralNetwork n2) {
        int maxInnovation = maxSharedInnovation(n1, n2);
        List<ConnectionGene> unlikeGenes = getUnlikeConnections(n1, n2);
        return (int) unlikeGenes
                .stream()
                .filter(connectionGene -> connectionGene.getInnovationNumber() < maxInnovation)
                .count();
    }

    public NeuralNetwork copy() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(this.inputs, this.outputs);
        neuralNetwork.neurons.clear();
        neuralNetwork.connections.clear();
        for (Neuron neuron : neurons) {
            neuralNetwork.neurons.add(neuron.copy());
        }
        for (ConnectionGene connection : connections) {
            neuralNetwork.addConnection(connection);
        }
        return neuralNetwork;
    }

    static NeuralNetwork crossOver(NeuralNetwork moreFitParent, NeuralNetwork lessFitParent) {
        NeuralNetwork neuralNetwork = new NeuralNetwork(moreFitParent.inputs, moreFitParent.outputs);
        neuralNetwork.neurons.clear();
        moreFitParent.neurons
                .forEach(neuron -> neuralNetwork.neurons.add(neuron.copy()));

        for (ConnectionGene connection : moreFitParent.connections) {
            Optional<ConnectionGene> overlap = lessFitParent
                    .connections
                    .stream()
                    .filter(connectionGene -> connectionGene.getInnovationNumber() == connection.getInnovationNumber())
                    .findAny();
            if(overlap.isPresent()) {
                neuralNetwork.addConnection(Math.random() > 0.5 ? connection : overlap.get());
            } else {
                neuralNetwork.addConnection(connection);
            }
        }

        return neuralNetwork;
    }

}
