package Animats;
import processing.core.PApplet;
import Simulation.Vector2f;
import Simulation.World;

public abstract class Animat {
    public Vector2f position = new Vector2f(), velocity = new Vector2f(), acceleration = new Vector2f();
    public volatile float radius, max_vel = 4, max_vel_scale = 1;
    public int death_time;
    public volatile boolean alive = true, decomposed = false;
    public Genome genome;
    public int color;
    
    public Animat( Genome gnome ) {
        genome = gnome;
        initFromGenome();
    }
    public abstract void initFromGenome();
    public abstract float getFitness( int end_iteration );
    public float getMaxVelocity() {
        return max_vel * max_vel_scale;
    }
    public void move( float timestep ) {
        if( !alive )
            return;

        velocity.addEquals( acceleration, timestep );
        velocity.setMaxLength( max_vel * max_vel_scale );
        position.addEquals(velocity, timestep);
    }
    public void draw(PApplet applet) {
        float px = position.getX(), py = position.getY(),
                max_vel = getMaxVelocity(), sca = max_vel != 0 ? radius / max_vel : 0,
                vx = velocity.getX() * sca, vy = velocity.getY() * sca;
        applet.fill(color);
        applet.ellipseMode(PApplet.RADIUS);
        applet.ellipse(position.getX(), position.getY(), radius, radius);
        applet.stroke(0f);
        applet.line(px, py, px + vx, py + vy);
    }
    public void collideWithWorld() {
        velocity.set( 0, 0 );
    }
    public abstract void collideWithAnimat( Animat other );
    public abstract void control( int iteration, World x );
    public abstract float[] getGenomeData();
}
