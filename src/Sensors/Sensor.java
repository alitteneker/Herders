package Sensors;
import Simulation.World;
import Animats.Animat;

public abstract class Sensor {
    public int num_results;

    public abstract float[] sense(Animat ind, World world);
}
