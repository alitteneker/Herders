package Animats;
import Simulation.World;


public class Wolf extends Animat {
    float radius = 5;
    public float energy = 5000;
    WolfNeuralNetwork NN;
    Genome gnome;
    
    // CONSTANTS
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
        super();
        this.gnome = gnome;
        NN = new WolfNeuralNetwork(this, gnome);
    }
    public void collideWithAnimat(Animat other) {
        if( other instanceof Sheep )
            energy += d_kill_energy;
        vel_x = 0;
        vel_y = 0;
    }
    public void control( int iteration, World world ) {
        if( !alive )
            return;

        NN.calculate( world );
        float accel_x = NN.getMotorX(), accel_y = NN.getMotorY(), eat_motor = NN.getMotorEat();
        
        float d_energy = d_idle_energy;
        d_energy += Math.sqrt( accel_x * accel_x + accel_y * accel_y ) * d_accel_energy;
        if( eat_motor >= eat_motor_thresh )
            d_energy += takeBite( world );
        energy += d_energy;
        
        if( energy > max_energy ) {
            energy = max_energy;
            max_vel_scale *= d_scale_vel_over_max_energy;
        }
        if( energy <= 0 ) {
            alive = decomposed = false;
            death_time = iteration;
        }
        else {
            vel_x += accel_x;
            vel_y += accel_y;
        }
    }

    public float takeBite( World world ) {
        Sheep prey = null;
        float min_dist = world.radius * 2, dist_squared;
        for( int i = 0; i < world.animats.size(); ++i ) {
            Animat check = world.animats.get(i);
            if( check.alive || check.decomposed || check instanceof Wolf )
                continue;
            dist_squared = (float) ( Math.pow( pos_x - check.pos_x, 2 ) + Math.pow( pos_y - check.pos_y, 2 ) );
            if( dist_squared < min_eat_dist * min_eat_dist && dist_squared < min_dist ) {
                min_dist = dist_squared;
                prey = (Sheep)check;
            }
        }
        if( prey != null )
            return prey.biteTakenOut(d_eat_energy);
        else
            return d_bad_bite_energy;
    }
}
