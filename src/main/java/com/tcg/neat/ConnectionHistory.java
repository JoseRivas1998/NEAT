package com.tcg.neat;

import java.util.ArrayList;
import java.util.List;

public final class ConnectionHistory {

    private int input;
    private int output;
    int innovationNumber;
    private List<Integer> innovationNumbers;

    public ConnectionHistory(int input, int output, int innovationNumber, List<Integer> innovationNumbers) {
        this.input = input;
        this.output = output;
        this.innovationNumber = innovationNumber;
        this.innovationNumbers = new ArrayList<>(innovationNumbers);
    }

    public boolean matches(NeuralNetwork nn, Neuron input, Neuron output) {
        boolean result;
        if (nn.allGenes() == innovationNumbers.size()) {
            result = false;
        } else {
            List<Integer> nnInnovations = nn.innovationNumbers();
            result = input.getId() == output.getId();
            for (int i = 0; i < nnInnovations.size() && result; i++) {
                if(!innovationNumbers.contains(nnInnovations.get(i))) {
                    result = false;
                }
            }
        }
        return result;
    }

}
