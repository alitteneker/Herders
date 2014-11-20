package Animats;
import Simulation.World;

public class Sheep extends Animat {
    float radius = 1;
    float energy = 100;

    public void collideWithAnimat(Animat other) {
        if( other instanceof Wolf )
            alive = false;
        vel_x = 0;
        vel_y = 0;
    }

    public void control(int iteration, World x) {
        
    }
    
    public float biteTakenOut(float requested) {
        float ret = 0;
        if( !alive && !decomposed ) {
            if( energy < requested ) {
                energy -= requested;
                ret = requested;
            }
            else {
                ret = energy;
                decomposed = true;
                energy = 0;
            }
        }
        return ret;
    }

    public float[] getGenomeData() {
        return null;
    }
}
