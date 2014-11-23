package Animats;

import java.util.ArrayList;
import java.util.Random;

import Simulation.Util;

public class Genome {
    public float[] data;
    public static final float MUTATION_RANGE = 0.2f;

    public Genome(float[] data) {
        this.data = data;
    }
    public Genome(Animat orig) {
        data = orig.getGenomeData();
    }
    public Genome cross(Genome other) {
        return cross(this, other);
    }
    public Genome[] cross(Genome other, int children) {
        return cross(this, other, children);
    }
    public static Genome cross( Genome a, Genome b ) {
        return cross(a,b,1)[0];
    }
    public static Genome[] cross(Genome a, Genome b, int num_children) {
        if( num_children <= 0 )
            return null;
        int c,i, len = Math.min(a.data.length, b.data.length);
        float[] child = new float[len];
        Genome[] children = new Genome[num_children];
        Random gen = new Random();
        for( c = 0; c < num_children; ++c ) {
            for( i = 0; i < len; ++i ) {
                child[i] =
                        ( Util.min(a.data[i],b.data[i]) + ( gen.nextFloat() * Util.abs(a.data[i] - b.data[i]) ) )
                        * ( 1.0f + ( ((float)gen.nextGaussian()) * MUTATION_RANGE ) );
            }
            children[c] = new Genome(child);
        }
        return children;
    }
    public static Animat cross(Animat a, Animat b) {
        Genome crossed = cross( a.genome, b.genome );
        if( a instanceof Wolf )
            return new Wolf(crossed);
        else
            return new Sheep(crossed);
    }
    public static ArrayList<Animat> cross(ArrayList<Animat> animats, int end_iteration) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        int len = animats.size(), i, max_ind = -1, inda, indb;
        boolean allwolves = true;
        float[] fitness = new float[len];
        float sum_fit = 0, sum_fit_going = 0, max_fit = -1, prog;
        for( i = 0; i < len; ++i ) {
            Animat a = animats.get(i);
            allwolves = allwolves && a instanceof Wolf;
            fitness[i] = a.getFitness(end_iteration);
            sum_fit += fitness[i];
            if( fitness[i] > max_fit ) {
                max_fit = fitness[i];
                max_ind = i;
            }
        }
        // record max fitness genome, and max and avg fitness genome level
        Simulation.Simulation.printlnToLog(
                Simulation.Simulation.count_rounds
                + " " + ( allwolves ? "Wolves" : "Sheep" )
                + " " + ( sum_fit / (float)len )
                + " " + max_fit
                + " " + Util.getString( animats.get(max_ind).genome.data ) );
        for( i = 0; i < len; ++i ) {
            prog = fitness[i] /= sum_fit;
            fitness[i] += sum_fit_going;
            sum_fit_going += prog;
        }
        for( i = 0; i < len; ++i ) {
            inda = indb = getWeightedIndex(fitness);
            while( inda == indb )
                indb = getWeightedIndex(fitness);
            ret.add( cross( animats.get(inda), animats.get(indb) ) );
        }
        return ret;
    }
    public static int getWeightedIndex(float[] fitness) {
        float val = (float)Math.random();
        for( int i = 0; i < fitness.length; ++i )
            if( val < fitness[i] )
                return i;
        return fitness.length - 1;
    }
}
