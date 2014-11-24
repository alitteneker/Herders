package Simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        world.addAnimats(seedWolves(20));
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
        Random gen = new Random();
        for( int i = 0; i < count; ++i) {
            float[] data = new float[gene_count];
            for( int j = 0; j < data.length; ++j ) {
                data[j] = gen.nextFloat() * 2 - 1;
            }
            ret.add(new Wolf(new Genome(data)));
        }
        return ret;
    }
    public static ArrayList<Animat> seedSheep(int count) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        int gene_count = Sheep.getWeightCount();
        Random gen = new Random();
        for( int i = 0; i < count; ++i ) {
            float[] data = new float[gene_count];
            for( int j = 0; j < data.length; ++j ) {
                if( j == 2 )
                    data[j] = 0.9f + ( ( gen.nextFloat()-0.5f ) * 0.1f );
                else
                    data[j] = gen.nextFloat();
            }
            ret.add(new Sheep(new Genome(data)));
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
            float wolf_fitness = 0;
            
            while( wolf_fitness < 0.5f ) {
                ++count_rounds;
                world.run();
                
                // cross the existing population, weighted by fitness
                ArrayList<Animat> sheep = Genome.cross(world.getExisting(false), world.iteration);
                ArrayList<Animat> wolves = Genome.cross(world.getExisting(true), world.iteration);
                wolf_fitness = Genome.last_avg_fitness;
                System.out.println( "Round " + count_rounds
                        + " wolf fitness is " + wolf_fitness
                        + " after " + world.iteration + " iterations." );
                if( wolf_fitness >= 0.5f ) {
                    break;
                }
                world.animats.clear();
                world.addAnimats(sheep);
                world.addAnimats(wolves);
            }

            applet.exit();
        }
    }
}
