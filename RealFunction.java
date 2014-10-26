/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author xiangliu
 */
public interface RealFunction {
    public double value (double[] x); // return the value at point x
    public int getDim(); // return the dimension of the input
    public double getDomLB(); // return the upper bound of the domain
    public double getDomUB(); // return lower bound of the domain
    public boolean isLegalSoln(double[] x); // check if x is a legal solution
    public double[] makeLegal(double[] x); // make x a legal solution and return it.
}
