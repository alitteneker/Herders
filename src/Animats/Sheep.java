package Animats;
import Simulation.Util;
import Simulation.Vector2f;
import Simulation.World;

public class Sheep extends Animat {
    
    float radius = 1;
    float energy = 100;
    float cohesion_weight, separation_weight, alignment_weight, avoidance_weight;
    float cohesion_distance = 50, separation_distance = 50, alignment_distance = 50, avoidance_distance = 50;
    float max_acceleration = 2;
    static final float vel_friction = 0.04f;

    public Sheep( Genome gnome ) {
        super( gnome );
    }

    public void collideWithAnimat(Animat other) {
        if( other instanceof Wolf )
            alive = false;
        velocity.set(0, 0);
    }

    public void control( int iteration, World world ) {
        acceleration.set( 0, 0 );
        Vector2f cohesion = new Vector2f(), separation = new Vector2f(), alignment = new Vector2f(), avoidance = new Vector2f();
        int cohesion_count = 0, separation_count = 0, alignment_count = 0, avoidance_count = 0;
        int len = world.animats.size();
        float dist;
        for( int i = 0; i < len; ++i ) {
            Animat a = world.animats.get(i);
            if( this.equals(a) || a.decomposed )
                continue;
            dist = Util.distance( position, a.position );
            if( a instanceof Sheep ) {
                if( dist <= cohesion_distance ) {
                    cohesion.addEquals(a.position);
                    ++cohesion_count;
                }
                if( dist <= separation_distance ) {
                    separation.addEquals( ( position.getX() - a.position.getX() ) / ( dist * dist ),
                            ( position.getY() - a.position.getY() ) / ( dist * dist ) );
                    ++separation_count;
                }
                if( dist <= alignment_distance ) {
                    alignment.addEquals(a.velocity);
                    ++alignment_count;
                }
            }
            else {
                if( dist <= avoidance_distance ) {
                    avoidance.addEquals( ( position.getX() - a.position.getX() ) / ( dist * dist ),
                            ( position.getY() - a.position.getY() ) / ( dist * dist ) );
                    ++avoidance_count;
                }
            }
        }
        dist = world.radius - position.getLength();
        if( dist <= avoidance_distance ) {
            avoidance.subtractEquals( position, 1 / ( dist * dist ) );
            ++avoidance_count;
        }
        
        if( cohesion_count > 0 ) {
            cohesion.scale( 1 / (float)cohesion_count );
            cohesion.subtractEquals( position );
            cohesion.scale( cohesion_weight );
            acceleration.addEquals( cohesion );
        }
        if( separation_count > 0 ) {
            separation.scale( separation_weight / (float)separation_count );
            acceleration.addEquals( separation );
        }
        if( alignment_count > 0 ) {
            alignment.scale( 1 / (float)alignment_count );
            alignment.subtractEquals( velocity );
            alignment.scale( alignment_weight );
            acceleration.addEquals(alignment);
        }
        if( avoidance_count > 0 ) {
            avoidance.scale( avoidance_weight / (float)avoidance_count );
            acceleration.addEquals( avoidance );
        }
        
        acceleration.setMaxLength( max_acceleration );
    }
    
    public void move( float timestep ) {
        velocity.scale( 1 - vel_friction * timestep );
        super.move( timestep );
    }
    
    public float biteTakenOut(float requested) {
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

    public float[] getGenomeData() {
        // TODO
        return null;
    }

    public void initFromGenome() {
        cohesion_weight = genome.data[0];
        separation_weight = genome.data[1];
        alignment_weight = genome.data[2];
        avoidance_weight = genome.data[3];
    }

    public float getFitness( int end_iteration ) {
        return (float)death_time/(float)end_iteration;
    }
}
