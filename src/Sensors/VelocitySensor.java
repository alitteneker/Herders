package Sensors;
import Animats.Animat;
import Simulation.World;

public class VelocitySensor extends Sensor {

    int num_results = 2;
    public float[] sense(Animat parent, World world) {
        float real_max = parent.getMaxVelocity();
        return new float[] { parent.vel_x / real_max, parent.vel_y / real_max };
    }
}
