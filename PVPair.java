/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Xiang
 */
import java.util.*;

public class PVPair implements Comparable<PVPair> {

    private double v;
    private double[] p;

    public PVPair() {
        this.v = -1;
        this.p = null;
    }

    public PVPair(double[] p, double v) {
        this.v = v;
        this.p = p;
    }

    public double getV() {
        return v;
    }

    public double[] getP() {
        return p;
    }

    public void setV(double v) {
        this.v = v;
    }

    public void setP(double[] p) {
        this.p = p;
    }

    public int compareTo(PVPair o) {
        if (this.v > o.v) {
            return 1;
        } else if (this.v < o.v) {
            return -1;
        } else {
            return 0;
        }

    }

    public String toString() {
        String s = "";
        s += String.format("%.7f", v);
        s += " @ ";
        for (int i = 0; i < p.length - 1; i++) {
            s += String.format("%.5f", p[i]);
            s += ", ";
        }
        s += String.format("%.5f", p[p.length - 1]);
        return s;
    }

    public LinkedList getArr() {
        LinkedList r = new LinkedList();
        r.add(v);
        for (double d : p) {
            r.add(d);
        }
        return r;
    }
}
