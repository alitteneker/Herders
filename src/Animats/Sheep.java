package Animats;

import Simulation.Vector2f;
import Simulation.World;

public class Sheep extends Animat {
    
    float energy = 100;
    float cohesion_weight, separation_weight, alignment_weight, avoidance_weight;
    final float cohesion_distance = 250, separation_distance = 10, alignment_distance = 50, avoidance_distance = 50;
    final float max_vel = 2;
    final float vel_friction = 0.05f, repulsion_factor = 200f;

    public Sheep( Genome gnome ) {
        super( gnome );
        radius = 1;
        color = applet.color(54, 51, 119);
    }

    public void collideWithAnimat(Animat other) {
        if( other instanceof Wolf ) {
            alive = false;
            color = applet.color(255, 0, 0);
        }
        velocity.set(0, 0);
    }

    public void control( int iteration, World world ) {
        acceleration.set(0, 0);
        Vector2f cohesion = new Vector2f(),
                separation = new Vector2f(),
                alignment = new Vector2f(),
                avoidance = new Vector2f();
        int cohesion_count = 0, separation_count = 0, alignment_count = 0, avoidance_count = 0;
        float dist;
        int len = world.animats.size();
        for( int i = 0; i < len; ++i ) {
            Animat a = world.animats.get(i);
            if( this.equals(a) || a.decomposed )
                continue;
            Vector2f direction = a.position.subtract(position);
            dist = direction.getLength();
            direction.normalize();
            if( a instanceof Sheep ) {
                if( dist <= cohesion_distance ) {
                    cohesion.addEquals( direction.getScaled( 1f / ( dist + repulsion_factor ) ) );
                    ++cohesion_count;
                }
                if( dist <= separation_distance ) {
                    separation.addEquals( direction.getScaled(-1f/dist) );
                    ++separation_count;
                }
                if( dist <= alignment_distance ) {
                    alignment.addEquals(a.velocity);
                    ++alignment_count;
                }
            }
            else if( dist <= avoidance_distance ) {
                avoidance.addEquals(direction.getScaled(-1f/dist));
                ++avoidance_count;
            }
        }
        dist = world.radius - position.getLength();
        if( dist <= avoidance_distance ) {
            avoidance.addEquals( position.getNormalized(), -1f/dist );
            ++avoidance_count;
        }
        
        if( cohesion_count > 0 ) {
            cohesion.scale( cohesion_weight / (float)cohesion_count );
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
        
        acceleration.setMaxLength( max_vel );
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
        color = applet.color(54, 51, 119);
    }

    public float getFitness( int end_iteration ) {
        return alive ? ((float)end_iteration) : death_time;
    }
}
