package Sensors;
import Animats.Animat;
import Simulation.World;

public class VelocitySensor extends Sensor {

    public VelocitySensor() {
        num_results = 2;
    }
    public float[] sense(Animat parent, World world) {
        float real_max = parent.getMaxVelocity();
        return new float[] { parent.velocity.getX() / real_max, parent.velocity.getY() / real_max };
    }
}
