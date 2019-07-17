package com.tcg.neat;

import java.util.function.DoubleUnaryOperator;

public enum  ActivationFunction {
    SIGN(Math::signum),
    SIGMOID(operand -> 1.0 / (1.0 + Math.exp(-4.9 * operand)))
    ;
    public final DoubleUnaryOperator activationFunction;

    ActivationFunction(DoubleUnaryOperator activationFunction) {
        this.activationFunction = activationFunction;
    }
}
