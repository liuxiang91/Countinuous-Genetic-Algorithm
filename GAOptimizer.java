
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;


/**
 * A genetic algorithm optimizer for continuous multivariate functions
 * see http://www.ie.itcr.ac.cr/rpereira/mat_ant/Genetic%20Algorithms/ch2.pdf
 * and http://www.ie.itcr.ac.cr/rpereira/mat_ant/Genetic%20Algorithms/ch3.pdf
 * @author Xiang Liu 
 * liuxiang@umich.edu
 */
public class GAOptimizer {

    int popSize;
    int nKeep;
    int mIter;
    int dim;
    double mu;
    RealFunction f;
    String log = "";
    LinkedList<PVPair> maxHistory;
    LinkedList<Double> avgHistory;
    LinkedList<PVPair> history;
    LinkedList<PVPair> pop;
    ArrayList<ArrayList> initPool;
    double[] probTable;

    public static void main(String[] args) {

    }

    public GAOptimizer() {
    }

    public void initialize() {
        double sum = 0;
        probTable = new double[nKeep];
        for (int i = 0; i < nKeep; i++) {
            sum += prob(i + 1);
            probTable[i] = sum;
        }
    }

    /**
     * Genetic Algorithm Optimizer
     *
     * @param f a real function implementing the RealFunction interface
     * @param inputPop LinkedList of PVPairs to specify the initial population
     * @param nPop Number of candidates in the population
     * @param nKeep Number of top candidates to keep after selection
     * @param mIter Max number of iteration
     * @param mu mutation factor
     * @param convergeceGap convergence criterion (absolute difference)
     * @return PVPair of the optimal solution
     */
    public PVPair optimize(RealFunction f, LinkedList<PVPair> inputPop, int nPop, int nKeep, int mIter, double mu, double convergeceGap) {
        // I. Initialize
        maxHistory = new LinkedList();
        avgHistory = new LinkedList();
        this.f = f;
        dim = f.getDim();
        popSize = nPop;
        this.nKeep = nKeep;
        this.mIter = mIter;
        this.mu = mu;
        int iter = 1;
        initialize();

        // II. Generate initial population
        if (inputPop == null) { // if no initla population specified
            pop = genInitPop(popSize);  // randomly generate an initial population
        } else {
            pop = inputPop; // set inital population to the input one
        }

        // III. Iterative steps of the Genetic Algorithm
        while (iter <= mIter) {
            // 1. Select the best candidates and remove the rest
            Collections.sort(pop, Collections.reverseOrder());
            while (pop.size() > nKeep) {
                pop.remove(nKeep);
            }

            // 2. Reproduce new offsprings to fill the population
            reproduce();
            Collections.sort(pop, Collections.reverseOrder());

            // Recoed average and best 
            double avg = 0;
            for (PVPair p : pop) {
                avg += p.getV();
            }
            avg = avg / popSize;
            avgHistory.add(Double.valueOf(avg));
            maxHistory.add(pop.get(0));
            System.out.println("Generation " + iter + ", best = " + pop.get(0) + ", avg = " + avg);

            // 3. Check for stopping criterion/cirteria
            if (isConverged(convergeceGap)) {
                return Collections.max(maxHistory);
            }

            // 4. Mutate
            mutate();
            iter++;
        }
        return Collections.max(maxHistory);
    }

    /**
     * Check the absolute difference between the average values of the current
     * generation and the previous generation
     *
     * @param convergeceGap convergence criterion absolute difference
     * @return True if converged
     */
    private boolean isConverged(double convergeceGap) {
        if (avgHistory.size() > 1) {
            if (Math.abs(avgHistory.getLast() - avgHistory.get(avgHistory.size() - 2)) <= convergeceGap) {
                return true;
            }
        }
        return false;

    }

    /**
     * fill the population pool with new offsprings
     */
    private void reproduce() {
        while (pop.size() < popSize) {
            // Randomly pick two parents
            int i1 = pickIndex();
            int i2 = pickIndex();
            // Generate offsprings
            PVPair[] off = genOffspring(pop.get(i1), pop.get(i2));
            // Add the two offsprings to population
            pop.add(off[0]);
            pop.add(off[1]);
        }
        pop.removeLast(); // remove excessive offspring

    }

    /**
     *
     * @param p1 parent 1
     * @param p2 parent 2
     * @return two offsprings
     */
    private PVPair[] genOffspring(PVPair p1, PVPair p2) {
        int pivot = (int) Math.round(Math.random() * (dim - 1));
        double[] o1 = new double[dim];
        double[] o2 = new double[dim];
        for (int i = 0; i < pivot; i++) {
            o1[i] = p1.getP()[i];
            o2[i] = p2.getP()[i];
        }
        double b = Math.random();
        o1[pivot] = p1.getP()[pivot] - b * (p1.getP()[pivot] - p2.getP()[pivot]);
        o2[pivot] = p2.getP()[pivot] + b * (p1.getP()[pivot] - p2.getP()[pivot]);
        for (int i = pivot + 1; i < dim; i++) {
            o1[i] = p2.getP()[i];
            o2[i] = p1.getP()[i];
        }
        o1 = f.makeLegal(o1);
        o2 = f.makeLegal(o2);
        PVPair off1 = new PVPair(o1, f.value(o1));
        PVPair off2 = new PVPair(o2, f.value(o2));
        return new PVPair[]{off1, off2};

    }

    private void mutate() {
        int nMut = (int) Math.ceil((popSize - 1) * dim * mu);
        for (int i = 1; i <= nMut; i++) {
            int nrow = (int) Math.ceil(Math.random() * (popSize - 1));
            int ncol = (int) Math.round(Math.random() * (dim - 1));
            PVPair old = pop.get(nrow);
            double[] x = old.getP();
            x[ncol] = Math.random() * (f.getDomUB() - f.getDomLB()) + f.getDomLB();
            x=f.makeLegal(x);
            PVPair mut = new PVPair(x, f.value(x));
            pop.set(nrow, mut);
        }
    }

    /**
     * Randomly pick parent index using Weighted Random Pairing see
     * http://www.ie.itcr.ac.cr/rpereira/mat_ant/Genetic%20Algorithms/ch2.pdf
     * page 39
     *
     *
     * @return
     */
    private int pickIndex() {
        double p = Math.random();
        int i = 0;
        double prev = 0;
        while (!(p >= prev && p < probTable[i])) {
            i++;
            prev = probTable[i - 1];
        }
        return i;
    }

    /**
     * rank weighing probability
     *
     * @param index
     * @return
     */
    private double prob(int index) {
        return ((double) (nKeep - index + 1)) / ((double) ((1 + nKeep) * nKeep / 2));
    }

    /**
     * Randomly generate initial population see
     * http://www.ie.itcr.ac.cr/rpereira/mat_ant/Genetic%20Algorithms/ch2.pdf
     * page 39
     *
     * @param popSize
     * @return
     */
    private LinkedList<PVPair> genInitPop(int popSize) {
        LinkedList<PVPair> pop = new LinkedList<PVPair>();
        while (pop.size() < popSize) {
            double[] soln = randSoln();
            pop.add(new PVPair(soln, f.value(soln)));
        }
        return pop;

    }

    /**
     * Randomly generate one solution
     *
     * @return
     */
    private double[] randSoln() {
        double[] soln = new double[dim];
        do {
            for (int i = 0; i <= dim - 1; i++) {
                soln[i] = Math.random() * (f.getDomUB() - f.getDomLB()) + f.getDomLB();
            }
            soln = f.makeLegal(soln);
        } while (!f.isLegalSoln(soln)); // IMPORTANT!!!! MAKE SURE SOLUTION IS FEASIBLE HERE
        return soln;

    }
}
