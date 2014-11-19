package Animats;
import Simulation.World;


public abstract class Animat {
    public float pos_x, pos_y, vel_x, vel_y, radius;
    public float max_vel, max_vel_scale = 1;
    public int death_time;
    public boolean alive = true, decomposed = false;
    
    public Animat() {
        vel_x = 0;
        vel_y = 0;
    }
    public float getMaxVelocity() {
        return max_vel * max_vel_scale;
    }
    public void move() {
        if( !alive )
            return;

        float curr_vel = (float) Math.sqrt( vel_x * vel_x + vel_y * vel_y ),
                curr_max_vel = getMaxVelocity();
        if( curr_vel > curr_max_vel ) {
            curr_vel /= curr_max_vel;
            vel_x /= curr_vel;
            vel_y /= curr_vel;
        }

        pos_x += vel_x;
        pos_y += vel_y;
    }
    public void collideWithWorld() {
        vel_x = 0;
        vel_y = 0;
    }
    public abstract void collideWithAnimat( Animat other );
    public abstract void control( int iteration, World x );
}
