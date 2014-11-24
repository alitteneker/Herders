package Animats;
import Simulation.Util;
import Simulation.World;


public class Wolf extends Animat {

    public float energy = 5000;
    WolfNeuralNetwork NN;
    
    // CONSTANTS
    public static final float max_accel         = 2;
    public static final float d_kill_energy     = -5;
    public static final float d_eat_energy      = 10;
    public static final float d_bad_bite_energy = -5;
    public static final float d_idle_energy     = -2;
    public static final float d_accel_energy    = -1;
    public static final float eat_motor_thresh  = 1;
    public static final float min_eat_dist      = 10;
    public static final float max_energy        = 10000;
    public static final float d_scale_vel_over_max_energy = 0.8f;

    public Wolf( Genome gnome ) {
        super( gnome );
        radius = 5;
        color = applet.color(170, 143, 57);
    }
    public void initFromGenome() {
        NN = new WolfNeuralNetwork(this, genome);
    }
    public void collideWithAnimat(Animat other) {
        if( other instanceof Sheep )
            energy += d_kill_energy;
        velocity.set(0, 0);
    }
    public void control( int iteration, World world ) {
        if( !alive )
            return;

        NN.calculate( world );
        acceleration.set( NN.getMotorX(), NN.getMotorY() );
        float eat_motor = NN.getMotorEat();
        
        float d_energy = d_idle_energy;
        d_energy += acceleration.getLength() * d_accel_energy;
        if( eat_motor >= eat_motor_thresh )
            d_energy += takeBite( world );
        energy += d_energy;
        
        if( energy > max_energy ) {
            energy = max_energy;
            max_vel_scale *= d_scale_vel_over_max_energy;
        }
        if( energy <= 0 ) {
            energy = 0;
            alive = decomposed = false;
            death_time = iteration;
            System.out.println("Wolf death!");
        }
        else
            acceleration.setMaxLength(max_accel);
    }

    public float takeBite( World world ) {
        Sheep prey = null;
        float min_dist = world.radius * 2, dist_mask = min_eat_dist * min_eat_dist, dist_squared;
        for( int i = 0; i < world.animats.size(); ++i ) {
            Animat check = world.animats.get(i);
            if( check.alive || check.decomposed || check instanceof Wolf )
                continue;
            dist_squared = Util.distanceSquared( position, check.position );
            if( dist_squared < dist_mask && dist_squared < min_dist ) {
                min_dist = dist_squared;
                prey = (Sheep)check;
            }
        }
        if( prey != null )
            return prey.biteTakenOut(d_eat_energy);
        else
            return d_bad_bite_energy;
    }

    public float[] getGenomeData() {
        return NN.getGenomeData();
    }
    public static int getWeightCount() {
        return WolfNeuralNetwork.getWeightCount();
    }
    public float getFitness( int end_iteration ) {
        return ( ( (float)death_time/(float)end_iteration ) + ( energy / max_energy ) ) / 2f;
    }
}
