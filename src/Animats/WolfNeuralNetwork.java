package Animats;

import Sensors.EnergySensor;
import Sensors.GradientSensor;
import Sensors.Sensor;
import Sensors.VelocitySensor;
import Simulation.World;

public class WolfNeuralNetwork {
    // all wolves share the same sensors
    public static int num_sensor_vals;
    static Sensor[] sensors = makeSensors();
    public static Sensor[] makeSensors() {
        Sensor[] ret = new Sensor[7];

        ret[0] = new GradientSensor(GradientSensor.TYPE_SHEEP);
        ret[1] = new GradientSensor(GradientSensor.TYPE_WOLVES);
        ret[2] = new GradientSensor(GradientSensor.TYPE_OBSTACLES);
        ret[3] = new GradientSensor(GradientSensor.TYPE_MARSH);
        ret[4] = new GradientSensor(GradientSensor.TYPE_CARCASS);
        ret[5] = new VelocitySensor();
        ret[6] = new EnergySensor();
        
        num_sensor_vals = 0;
        for( int i = 0; i < ret.length; ++i )
            num_sensor_vals += ret[i].num_results;

        return ret;
    }

    public float[][] weights_level_1;
    public float[][] weights_level_2;
    public float[] motors;
    public static final float node_threshold = 0.01f;
    public static final int num_hidden_nodes = 19, num_motors = 3;
    Wolf parent;

    public WolfNeuralNetwork(Wolf par, Genome gnome) {
        parent = par;
        motors = new float[num_motors];
        setWeightsFromGenome( gnome );
    }
    public void calculate( World world ) {
        float[] sensor_data = getSensorData(world);
        float[] hidden = new float[num_hidden_nodes];
        int i, j;
        
        // calculate hidden values
        if( num_hidden_nodes != weights_level_1.length )
            return;
        for( i = 0; i < weights_level_1.length; ++i ) {
            if( sensor_data.length != weights_level_1[i].length )
                return;
            for( j = 0; j < weights_level_1[i].length; ++j )
                if( weights_level_1[i][j] != 0 )
                    hidden[i] += weights_level_1[i][j] * sensor_data[j];
            if( Math.abs(hidden[i]) < node_threshold )
                hidden[i] = 0;
        }
        
        // calculate motors from hidden
        if( num_motors != weights_level_2.length )
            return;
        for( i = 0; i < weights_level_2.length; ++i ) {
            motors[i] = 0;
            if( num_hidden_nodes != weights_level_2[i].length )
                return;
            for( j = 0; j < weights_level_2[i].length; ++j )
                if( weights_level_2[i][j] != 0 )
                    motors[i] += weights_level_2[i][j] * hidden[j];
        }
    }
    
    public static int getWeightCount() {
        return num_hidden_nodes * ( num_sensor_vals + num_motors );
    }

    // TODO: make weights symmetric up/down left/right on level 1
    public void setWeightsFromGenome( Genome gnome ) {
        int thresh = num_hidden_nodes * num_sensor_vals;
        weights_level_1 = new float[num_hidden_nodes][num_sensor_vals];
        weights_level_2 = new float[num_motors][num_hidden_nodes];
        for( int i = 0; i < gnome.data.length; ++i ) {
            if( i < thresh )
                weights_level_1[ i / num_sensor_vals ][ i % num_sensor_vals ] = gnome.data[i];
            else
                weights_level_2[ ( i - thresh ) / num_hidden_nodes ][ ( i - thresh ) % num_hidden_nodes ] = gnome.data[i];
        }
    }

    public float[] getGenomeData() {
        int i, j, ind = 0, count = getWeightCount();
        float[] ret = new float[count];
        for( i = 0; i < weights_level_1.length; ++i )
            for( j = 0; j < weights_level_1[i].length; ++j)
                ret[ind++] = weights_level_1[i][j];
        for( i = 0; i < weights_level_2.length; ++i )
            for( j = 0; j < weights_level_2[i].length; ++j)
                ret[ind++] = weights_level_2[i][j];
        return ret;
    }

    public float[] getSensorData( World world ) {
        float[] ret = new float[num_sensor_vals];
        int i, j, ind = 0;

        for( i = 0; i < sensors.length; ++i ) {
            float[] res = sensors[i].sense(parent, world);
            for( j = 0; j < sensors[i].num_results; ++j )
                ret[ind++] = res[j];
        }

        return ret;
    }
    public float getMotorX() {
        return motors[0];
    }
    public float getMotorY() {
        return motors[1];
    }
    public float getMotorEat() {
        return motors[2];
    }
}
