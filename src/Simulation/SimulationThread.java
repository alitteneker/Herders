package Simulation;

import java.util.ArrayList;
import java.util.Collections;

import Animats.Animat;
import Animats.Genome;
import Animats.Sheep;

public class SimulationThread extends Thread {
    Simulation sim;
    public SimulationThread(Simulation sim, ThreadGroup group, String name) {
        super(group, name);
        this.sim = sim;
    }
    public void run() {
        System.out.println("Simulation thread started.");
        
        boolean evolve_sheep = false;
        float wolf_fitness = 0;
        
        long timer = System.currentTimeMillis();
        while( Simulation.world.keep_going && wolf_fitness < ( Simulation.world.iteration > 0 ? Simulation.world.iteration : 1 ) ) {
            ++Simulation.count_rounds;
            Simulation.world.run();
            
            // cross the existing population, weighted by fitness
            reproducePopulations(Simulation.world, evolve_sheep);
            wolf_fitness = Genome.last_avg_fitness;
            System.out.println( "Round " + Simulation.count_rounds
                    + " avg wolf fitness is " + wolf_fitness
                    + " (max " + Genome.last_max_fitness + ", stdev " + Genome.last_stdev_fitness
                    + ") after " + ( System.currentTimeMillis()-timer )
                    + "ms and " + Simulation.world.iteration + " iterations." );
            timer = System.currentTimeMillis();
        }

        System.out.println("Simulation thread shutting down.");
        Simulation.applet.exit();
    }
    public void reproducePopulations(World world, boolean evolve_sheep) {
        ArrayList<Animat> sheep = world.getExisting(false);
        if( evolve_sheep )
            sheep = Genome.cross( sheep, world.iteration );
        else {
            for( int i = 0; i < sheep.size(); ++i )
                ((Sheep)sheep.get(i)).reinitialize();
        }
        
        ArrayList<Animat> wolves = Genome.cross( world.getExisting(true),  world.iteration );
        
        world.animats.clear();
        
        // give each a quick shuffle just to keep things interesting
        Collections.shuffle(wolves);
        Collections.shuffle(sheep);
        
        world.addAnimats(wolves);
        world.addAnimats(sheep);
        
        // TODO: output both lists to file
    }
    public void markStop() {
        Simulation.world.keep_going = false;
    }
}
