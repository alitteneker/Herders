package Animats;
import Simulation.Util;
import Simulation.Vector2f;
import Simulation.World;

public class Sheep extends Animat {
    
    float energy = 100;
    float cohesion_weight, separation_weight, alignment_weight, avoidance_weight;
    float cohesion_distance = 50, separation_distance = 50, alignment_distance = 50, avoidance_distance = 50;
    float max_vel = 2;
    static final float vel_friction = 0.0f, repulsion_scale = 0.5f;

    public Sheep( Genome gnome ) {
        super( gnome );
        radius = 1;
        color = applet.color(54, 51, 119);
    }

    public void collideWithAnimat(Animat other) {
        if( other instanceof Wolf ) {
            alive = false;
            color = applet.color(42, 78, 110);
        }
        velocity.set(0, 0);
    }

    public void control( int iteration, World world ) {
        Vector2f vel = new Vector2f( 0, 0 );
        Vector2f cohesion = new Vector2f(), separation = new Vector2f(), alignment = new Vector2f(), avoidance = new Vector2f();
        int cohesion_count = 0, separation_count = 0, alignment_count = 0, avoidance_count = 0;
        int len = world.animats.size();
        float dist;
        for( int i = 0; i < len; ++i ) {
            Animat a = world.animats.get(i);
            if( this.equals(a) || a.decomposed )
                continue;
            dist = Util.distanceSquared( position, a.position );
            if( a instanceof Sheep ) {
                if( dist <= cohesion_distance ) {
                    float div = Util.square( Util.sqrt(dist) + repulsion_scale );
                    cohesion.addEquals( ( a.position.getX() - position.getX() ) / div,
                            ( a.position.getY() - position.getY() ) / div );
                    ++cohesion_count;
                }
                if( dist <= separation_distance ) {
                    separation.addEquals( ( position.getX() - a.position.getX() ) / dist,
                            ( position.getY() - a.position.getY() ) / dist );
                    ++separation_count;
                }
                if( dist <= alignment_distance ) {
                    alignment.addEquals(a.velocity);
                    ++alignment_count;
                }
            }
            else if( dist <= avoidance_distance ) {
                avoidance.addEquals( ( position.getX() - a.position.getX() ) / dist,
                        ( position.getY() - a.position.getY() ) / dist);
                ++avoidance_count;
            }
        }
        dist = world.radius - position.getLength();
        if( dist <= avoidance_distance ) {
            avoidance.subtractEquals( position, 1 / ( dist * dist ) );
            ++avoidance_count;
        }
        
        if( cohesion_count > 0 ) {
            cohesion.scale( cohesion_weight / (float)cohesion_count );
            vel.addEquals( cohesion );
        }
        if( separation_count > 0 ) {
            separation.scale( separation_weight / (float)separation_count );
            vel.addEquals( separation );
        }
        if( alignment_count > 0 ) {
            alignment.scale( 1 / (float)alignment_count );
            alignment.subtractEquals( velocity );
            alignment.scale( alignment_weight );
            vel.addEquals(alignment);
        }
        if( avoidance_count > 0 ) {
            avoidance.scale( avoidance_weight / (float)avoidance_count );
            vel.addEquals( avoidance );
        }
        
        vel.setMaxLength( max_vel );
        acceleration.set( -velocity.getX(), -velocity.getY() );
        acceleration.addEquals(vel);
    }
    
    public void move( float timestep ) {
        velocity.scale( 1 - vel_friction * timestep );
        super.move( timestep );
    }
    
    public synchronized float biteTakenOut(float requested) {
        float ret = 0;
        if( !alive && !decomposed ) {
            if( energy < requested ) {
                energy -= requested;
                ret = requested;
            }
            else {
                ret = energy;
                decomposed = true;
                energy = 0;
            }
        }
        return ret;
    }

    public static int getWeightCount() {
        return 4;
    }

    public void initFromGenome() {
        cohesion_weight   = genome.data[0];
        separation_weight = genome.data[1];
        alignment_weight  = genome.data[2];
        avoidance_weight  = genome.data[3];
    }
    
    public void reinitialize() {
        alive = true;
        decomposed = false;
        energy = 100;
    }

    public float getFitness( int end_iteration ) {
        return alive ? ((float)end_iteration) : death_time;
    }
}
