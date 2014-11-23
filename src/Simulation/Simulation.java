package Simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Simulation {
    public static int count_rounds = 0;
    public static String log_location = "simulation.txt";
    public static World world;
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
