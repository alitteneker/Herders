package Simulation;
import java.util.ArrayList;

import Animats.Animat;
import Animats.Sheep;
import Animats.Wolf;
import processing.core.*;

public class World {
    PApplet applet;

    public ArrayList<Animat> animats = new ArrayList<Animat>();
    public float radius = 500;
    public float marsh_pos_x = 0, marsh_pos_y = 250, marsh_radius = 25;

    public World( PApplet applet ) {
        this.applet = applet;
        initialize();
    }
    public void initialize() {
        // add some sheep and wolves
        // need to be positioned so that they are not colliding
    }
    public int countAliveWolves() {
        int ret = 0;
        for( int i = 0; i < animats.size(); ++i )
            if( animats.get(i) instanceof Wolf && animats.get(i).alive )
                ++ret;
        return ret;
    }
    public void run() {

        int iteration = 0, i, j, size = animats.size();
        float dist, radius_diff, diff_x, diff_y;

        while( countAliveWolves() > 0 ) {
            iteration++;

            // let all animats run their control logic
            // TODO: multithread?
            for( i = 0; i < size; ++i ) {
                Animat a = animats.get(i);
                if( a instanceof Sheep) {
                    if( Util.distance(a.pos_x, a.pos_y, marsh_pos_x, marsh_pos_y) <= marsh_radius + a.radius )
                        a.max_vel_scale = 0.25f;
                    else if( a.max_vel_scale < 1 )
                        a.max_vel_scale = 1;
                }
                a.control(iteration, this);
            }

            // let all animats move, then search for and resolve all collisions with the environment
            for( i = 0; i < size; ++i ) {
                Animat a = animats.get(i);
                a.move();

                // resolve any collisions with the edge of the world
                dist = (float) Math.sqrt( ( a.pos_x * a.pos_x ) + ( a.pos_y * a.pos_y ) );
                if( dist + a.radius - radius >= 0 ) {
                    radius_diff = ( radius - a.radius - 1 ) / dist;
                    a.pos_x *= radius_diff;
                    a.pos_y *= radius_diff;
                    a.collideWithWorld();
                }
            }
            
            // look for and resolve any collisions between animats
            for( i = 0; i < size; ++i ) {
                Animat a = animats.get(i);
                if( a.decomposed )
                    continue;
                for( j = i + 1; j < size; ++j ) {
                    Animat b = animats.get(j);
                    if( i == j || b.decomposed )
                        continue;

                    diff_x = a.pos_x - b.pos_x;
                    diff_y = a.pos_y - b.pos_y;
                    dist = (float) Math.sqrt( ( diff_x * diff_x ) + ( diff_y * diff_y ) );
                    radius_diff = ( a.radius + b.radius + 1 ) - dist;
                    if( radius_diff > 0 ) {
                        radius_diff /= dist;
                        diff_x *= radius_diff;
                        diff_y *= radius_diff;
                        
                        a.pos_x += diff_x/2;
                        a.pos_y += diff_y/2;
                        b.pos_x -= diff_x/2;
                        b.pos_y -= diff_y/2;

                        a.collideWithAnimat(b);
                        b.collideWithAnimat(a);
                    }
                }
            }
        }
    }
}
