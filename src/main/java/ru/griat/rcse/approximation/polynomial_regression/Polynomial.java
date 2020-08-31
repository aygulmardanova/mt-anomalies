package ru.griat.rcse.approximation.polynomial_regression;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;

public class Polynomial extends PolynomialFunction {

    public Polynomial(double[] c) throws NullArgumentException, NoDataException {
        super(c);
    }

    @Override
    public Polynomial derivative() {
        return new Polynomial(this.polynomialDerivative().getCoefficients());
    }

    @Override
    public String toString() {
        if      (degree() == -1) return "0";
        else if (degree() ==  0) return String.format("%.2f", getCoefficients()[0]);
        else if (degree() ==  1) return String.format("%.2f %s + %.2f", getCoefficients()[1], "t", getCoefficients()[0]).replace("+ -", "- ");
        StringBuilder s = new StringBuilder(String.format("%.2f %s^%d", getCoefficients()[degree()], "t", degree()));
        for (int i = degree() - 1; i >= 0; i--) {
            if      (getCoefficients()[i] == 0) continue;
            else s.append(String.format(" + %.2f ", getCoefficients()[i]));
            if      (i == 1) s.append("t");
            else if (i >  1) s.append("t^").append(i);
        }
        return s.toString().replace("+ -", "- ");
    }
}
