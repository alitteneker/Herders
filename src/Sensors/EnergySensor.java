package Sensors;

import Animats.Animat;
import Animats.Wolf;
import Simulation.World;

public class EnergySensor extends Sensor {

    public EnergySensor() {
        num_results = 1;
    }
    public float[] sense(Animat ind, World world) {
        return new float[] { ((Wolf)ind).energy / Wolf.max_energy };
    }
}
