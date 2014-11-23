package Simulation;
import java.util.ArrayList;

import Animats.Animat;
import Animats.Sheep;
import Animats.Wolf;
import processing.core.*;

public class World {
    PApplet applet;

    public ArrayList<Animat> animats = new ArrayList<Animat>();
    public ThreadGroup tg;
    public ArrayList<ThreadSplitter> threads = new ArrayList<ThreadSplitter>();
    public float radius = 500, marsh_radius = 25;
    public Vector2f marsh_position = new Vector2f( 0, 250 );
    public int iteration;
    public float timestep;

    public World( PApplet applet ) {
        this.applet = applet;
        buildThreads();
        initialize();
    }
    public void buildThreads() {
        tg = new ThreadGroup( "ThreadSplitters" );
        final int processors = Runtime.getRuntime().availableProcessors();
        for ( int i = 0; i < processors; ++i )
            threads.add( new ThreadSplitter( this, tg, "TS"+i) );
    }
    public void cleanThreads() {
        // go through the animats, count the non-decomposed, and move the decomposed to the end of the list
        // use the range of viable animats to set the min/maxes of each thread (avoids having to deal with duplicates)
    }
    public void runThreads( int mode ) {
        final int processors = Runtime.getRuntime().availableProcessors();
        for( int i = 0; i < threads.size(); ) {
            if( tg.activeCount() < processors ) {
                ThreadSplitter th = threads.get( i++ );
                th.setMode(mode);
                th.start();
            }
            else
                try { Thread.sleep(100); }
                    catch (InterruptedException e) { e.printStackTrace(); }
        }
        while( tg.activeCount() > 0 ) {
            try { Thread.sleep(100); } 
                catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    public void initialize() {
        // position animats so that they are not colliding
    }
    public int countAlive( boolean justwolves ) {
        int ret = 0;
        for( int i = 0; i < animats.size(); ++i ) {
            Animat a = animats.get(i); 
            if( a.alive && ( !justwolves || ( justwolves && a instanceof Wolf ) ) )
                ++ret;
        }
        return ret;
    }
    public void run() {
        iteration = 0;
        timestep = 1;

        while( countAlive(true) > 0 ) {
            iteration++;

            cleanThreads();
            runThreads(ThreadSplitter.MODE_CONTROL);
            runThreads(ThreadSplitter.MODE_MOVE);
            runThreads(ThreadSplitter.MODE_COLLIDE);
            draw();
        }
    }
    
    public void draw() {
        // can't multithread this bit due to drawing limitations
        int size = animats.size();
        for( int i = 0; i < size; ++i ) {
            Animat a = animats.get(i);
            if( !a.decomposed )
                a.draw(applet);
        }
    }
    
    class ThreadSplitter extends Thread {
        World world;
        static final int MODE_CONTROL = 1, MODE_COLLIDE = 2, MODE_MOVE = 3;
        int mode;
        int minIndex, maxIndex;
        
        public ThreadSplitter(World world, ThreadGroup group, String name) {
            super( group, name );
        }
        public void setMode(int set) {
            mode = set;
        }
        public void run() {
            if( mode == MODE_CONTROL )
                control( world.iteration, world.timestep );
            if( mode == MODE_MOVE )
                move( world.timestep );
            if( mode == MODE_COLLIDE )
                checkForCollisions();
        }
        
        public void control( int iteration, float timestep ) {
            for( int i = minIndex; i < maxIndex; ++i ) {
                Animat a = animats.get(i);
                if( !a.alive )
                    continue;
                if( a instanceof Sheep) {
                    if( Util.distance(a.position, marsh_position) <= marsh_radius + a.radius )
                        a.max_vel_scale = 0.25f;
                    else if( a.max_vel_scale < 1 )
                        a.max_vel_scale = 1;
                }
                a.control(iteration, world);
            }
        }
        
        public void move( float timestep ) {
            float dist, radius_diff;
            for( int i = minIndex; i < maxIndex; ++i ) {
                Animat a = animats.get(i);
                if( !a.alive )
                    continue;
                a.move(timestep);

                // resolve any collisions with the edge of the world
                dist = a.position.getLength();
                if( dist + a.radius - radius >= 0 ) {
                    radius_diff = ( radius - a.radius - 1 ) / dist;
                    a.position.scale( radius_diff );
                    a.collideWithWorld();
                }
            }
        }
        
        public void checkForCollisions() {
            int minSq = minIndex * minIndex, maxSq = (maxIndex - 1) * (maxIndex - 1), minI = , maxI, minJ, maxJ;
            int i, j, size = animats.size();
            float dist, radius_diff;
            Vector2f diff = new Vector2f();
            for( i = 0; i < size; ++i ) {
                Animat a = animats.get(i);
                if( a.decomposed )
                    continue;
                for( j = i + 1; j < size; ++j ) {
                    Animat b = animats.get(j);
                    if( i == j || b.decomposed )
                        continue;

                    diff.set( a.position.getX() - b.position.getX(), a.position.getY() - b.position.getY() );
                    dist = diff.getLength();
                    radius_diff = ( a.radius + b.radius + 1 ) - dist;
                    if( radius_diff > 0 ) {
                        diff.scale( radius_diff / dist / 2 );
                        a.position.addEquals( diff );
                        b.position.subtractEquals( diff );

                        a.collideWithAnimat(b);
                        b.collideWithAnimat(a);
                    }
                }
            }
        }
    }
}
