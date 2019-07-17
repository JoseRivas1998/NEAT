package com.tcg.neat;

import org.json.JSONObject;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

public final class Neuron {

    private final int id;
    int layer;
    private double inputSum;

    public Neuron(int id) {
        this.id = id;
        this.layer = 0;
        this.inputSum = 0;
    }

    public void setInputSum(double sum) {
        this.inputSum = sum;
    }

    public void addToInputSum(double value) {
        inputSum += value;
    }

    public double applyActivationFunction(DoubleUnaryOperator activationFunction) {
        return inputSum = activationFunction.applyAsDouble(inputSum);
    }

    public double getInputSum() {
        return inputSum;
    }

    public boolean isInput() {
        return layer == 1;
    }

    public boolean isLayer(int layer) {
        return this.layer == layer;
    }

    public int getLayer() {
        return layer;
    }

    public int getId() {
        return this.id;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.id);
        jsonObject.put("layer", this.layer);
        return jsonObject;
    }

    public static Neuron fromJSON(JSONObject jsonObject) {
        Neuron neuron = new Neuron(jsonObject.getInt("id"));
        neuron.layer = jsonObject.getInt("layer");
        return neuron;
    }

    public Neuron copy() {
        return Neuron.fromJSON(this.toJSON());
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if(obj == null || obj.getClass() != this.getClass()) {
            result = false;
        } else if(obj == this) {
            result = true;
        } else {
            Neuron other = (Neuron) obj;
            result = other.id == this.id && other.layer == this.layer;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.layer);
    }
}
