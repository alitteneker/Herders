package Animats;

import java.util.Random;
import Simulation.Util;

public class Genome {
    public float[] data;
    public static final float MUTATION_RANGE = 0.5f;

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
                        Util.min(a.data[i],b.data[i]) + ( gen.nextFloat() * Util.abs(a.data[i] - b.data[i]) )
                        + ( ((float)gen.nextGaussian()) * MUTATION_RANGE );
            }
            children[c] = new Genome(child);
        }
        return children;
    }
}
