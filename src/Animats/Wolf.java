package Animats;
import Simulation.Util;
import Simulation.World;


public class Wolf extends Animat {

    public float energy = 2000;
    WolfNeuralNetwork NN;
    
    final float friction_scale = 0.05f;
    
    // CONSTANTS
    public static final float max_accel          = 2.0f;
    public static final float d_kill_energy      = -2.0f;
    public static final float d_eat_energy       = 1.0f;
    public static final float d_bad_bite_energy  = -5.0f;
    public static final float d_idle_energy      = -0.5f;
    public static final float d_accel_energy     = -0.1f;
    public static final float eat_motor_thresh   = 1.0f;
    public static final float min_eat_dist       = 10.0f;
    public static final float max_energy         = 4000.0f;
    public static final float d_world_collision_energy  = -3.0f;
    public static final float d_animat_collision_energy = -1.0f;
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
        else
            energy += d_animat_collision_energy;
        velocity.set(0, 0);
    }
    public void collideWithWorld() {
        super.collideWithWorld();
        energy += d_world_collision_energy;
    }
    public void control( int iteration, World world ) {
        if( !alive )
            return;

        NN.calculate( world );
        acceleration.set( NN.getMotorX(), NN.getMotorY() );
        float eat_motor = NN.getMotorEat();
        
        float d_energy = d_idle_energy;
        acceleration.setMaxLength(max_accel);
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
        }
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
        if( prey != null ) {
            return prey.biteTakenOut(d_eat_energy);
        }
        else {
            return d_bad_bite_energy;
        }
    }
    
    public void move( float timestep ) {
        velocity.scale( ( 1 - friction_scale ) * timestep );
        super.move( timestep );
    }

    public float[] getGenomeData() {
        return NN.getGenomeData();
    }
    public static int getWeightCount() {
        return WolfNeuralNetwork.getWeightCount();
    }
    public float getFitness( int end_iteration ) {
        float ret = alive ? end_iteration : death_time;
        ret += ( energy / max_energy ) * end_iteration;
        return ret;
    }
    public void draw() {
        if( alive )
            super.draw();
    }
}
