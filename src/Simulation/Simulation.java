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
import Animats.WolfNeuralNetwork;
import processing.core.PApplet;

public class Simulation {
    public static int count_rounds = 0;
    public static String log_location = "simulation.txt";
    public static World world;
    public static PApplet applet;
    public static SimulationThread th;
    
    public void runSimulation( PApplet app ) {
        applet = app;        
        world = new World(applet);
        world.addAnimats(seedWolves(20));
        world.addAnimats(seedSheep(400));
        
        final ThreadGroup tg = new ThreadGroup("Simulation");
        th = new SimulationThread(this, tg, "Sim");
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
        int gene_count = Wolf.getWeightCount(),
            sensor_count = WolfNeuralNetwork.num_sensor_vals,
            hidden_count = WolfNeuralNetwork.num_hidden_nodes;
        final float range = 0.25f;
        Random gen = new Random();
        for( int i = 0; i < count; ++i) {
            float[] data = new float[gene_count];
            
            // seed the hidden node weights
            for( int j = 0; j < 7; ++j ) {
                if( j < 5 ) {
                    int index = j * ( 3 * sensor_count + 4 );
                    data[ index ]                    = -1;
                    data[ index + 1 ]                = 1;
                    data[ index + sensor_count + 2 ] = -1;
                    data[ index + sensor_count + 3 ] = 1;
                    data[ index + ( 2 * sensor_count ) ]     = 0.25f;
                    data[ index + ( 2 * sensor_count ) + 1 ] = 0.25f;
                    data[ index + ( 2 * sensor_count ) + 2 ] = 0.25f;
                    data[ index + ( 2 * sensor_count ) + 3 ] = 0.25f;
                }
                else if(j == 5) {
                    data[ j * 4 + 15 * sensor_count     ] = 1;
                    data[ j * 4 + 16 * sensor_count + 1 ] = 1;
                    data[ j * 4 + 17 * sensor_count     ] = 0.5f;
                    data[ j * 4 + 17 * sensor_count + 1 ] = 0.5f;
                }
                else {
                    data[ 18 * sensor_count ] = 1;
                }
            }
            int offset = sensor_count * hidden_count;
            // sheep, wolves, obstacles, marsh, carcass, velocity
            float[] guess = new float[] { 5, -2f, -1, 2f, 5, 0 };
            for( int j = 0; j < hidden_count; ++j ) {
                int type = j == 18 ? 2 : j % 3, 
                    index = j + ( type * hidden_count ) + offset;
                if( type == 2 )
                    data[index] = j/3 == 4 ? 10 : 0;
                else
                    data[index] = guess[j/3];
                data[ index ] += range * ( gen.nextFloat() * 2 - 1 );
            }
            ret.add( new Wolf( new Genome(data) ) );
        }
        return ret;
    }
    public static ArrayList<Animat> seedSheep(int count) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        Random gen = new Random();
        for( int i = 0; i < count; ++i ) {
            float[] data = new float[]{ 600f, 50f, 10f, 1000f };
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
}
