package Simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import Animats.Animat;
import Animats.Genome;
import Animats.Sheep;
import Animats.Wolf;
import processing.core.PApplet;

public class Simulation {
    public static int count_rounds = 0;
    public static String log_location = "simulation.txt";
    public static World world;
    public static PApplet applet;
    public void runSimulation( PApplet app ) {
        applet = app;        
        world = new World(applet);
        world.addAnimats(seedWolves(40));
        world.addAnimats(seedSheep(200));
        
        ThreadGroup tg = new ThreadGroup("Simulation");
        SimulationThread th = new SimulationThread(tg, "Sim");
        th.start();
    }
    public static void draw() {
        world.draw();
    }
    public static void drawMetadata() {
        world.drawMetadata();
    }
    public static ArrayList<Animat> seedWolves(int count) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        int gene_count = Wolf.getWeightCount();
        float range = 10f;
        Random gen = new Random();
        for( int i = 0; i < count; ++i) {
            float[] data = new float[gene_count];
            for( int j = 0; j < data.length; ++j ) {
                data[j] = range * ( gen.nextFloat() * 2 - 1 );
            }
            ret.add(new Wolf(new Genome(data)));
        }
        return ret;
    }
    public static ArrayList<Animat> seedSheep(int count) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        Random gen = new Random();
        for( int i = 0; i < count; ++i ) {
            float[] data = new float[]{ 1f, 1f, 1f, 3f };
            for( int j = 0; j < data.length; ++j )
                data[j] += 0.05f * Util.minMax((float)gen.nextGaussian(), -1f, 1f);
            ret.add( new Sheep( new Genome(data) ) );
        }
        return ret;
    }
    public static boolean printlnToLog( String data ) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true)));
            out.println(data);
            out.close();
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    class SimulationThread extends Thread {
        public SimulationThread(ThreadGroup group, String name) {
            super(group, name);
        }
        public void run() {
            boolean evolve_sheep = false;
            float wolf_fitness = 0;
            
            long timer = System.currentTimeMillis();
            while( wolf_fitness < ( world.iteration > 0 ? world.iteration : 1 ) ) {
                ++count_rounds;
                world.run();
                
                // cross the existing population, weighted by fitness
                reproducePopulations(world, evolve_sheep);
                wolf_fitness = Genome.last_avg_fitness;
                System.out.println( "Round " + count_rounds
                        + " wolf fitness is " + wolf_fitness
                        + " after " + ( System.currentTimeMillis()-timer )
                        + "ms and " + world.iteration + " iterations." );
                timer = System.currentTimeMillis();
            }

            applet.exit();
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
    }
}
