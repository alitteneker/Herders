package Simulation;
import java.util.ArrayList;

import Animats.Animat;
import Animats.Sheep;
import Animats.Wolf;
import processing.core.*;

public class World {
    PApplet applet;

    public ArrayList<Animat> animats = new ArrayList<Animat>();
    public float radius = 500, marsh_radius = 75;
    public Vector2f marsh_position = new Vector2f( 0, 250 ), wolfstart = new Vector2f(-250, 0), sheepstart = new Vector2f(250, 0);
    public int iteration, num_active, num_alive;
    public float timestep;
    public float fps = 0;
    public int thread_UID = 0;

    public World( PApplet applet ) {
        this.applet = applet;
    }
    public boolean bothStillAlive() {
        int retW = 0, retS = 0;
        for( int i = 0; i < animats.size(); ++i ) {
            Animat a = animats.get(i);
            if( a.alive && a instanceof Wolf )
                ++retW;
            else if( a.alive && a instanceof Sheep )
                ++retS;
        }
        return retW > 0 && retS > 0;
    }
    public ArrayList<Animat> getExisting( boolean wolves ) {
        ArrayList<Animat> ret = new ArrayList<Animat>();
        int len = animats.size();
        for( int i = 0; i < len; ++i ) {
            Animat a = animats.get(i);
            if( wolves == ( a instanceof Wolf ) )
                ret.add(a);
        }
        return ret;
    }
    public void addAnimats( ArrayList<Animat> population ) {
        int len = population.size();
        Vector2f start = population.get(0) instanceof Wolf ? wolfstart : sheepstart,
                curr = new Vector2f();
        float delta = 3 * population.get(0).radius + 3, range = Util.sqrt(len) * delta, limit = start.getX() + ( range / 2 );
        curr.set(start.getX() - range/2, start.getY() - range/2);
        for( int i = 0; i < len; ++i ) {
            Animat a = population.get(i);
            curr.addEquals(delta, 0);
            if( curr.getX() > limit )
                curr.set(start.getX(), curr.getY() + delta);
            a.position.set(curr);
            animats.add(a);
        }
    }
    public ArrayList<ThreadSplitter> prepThreads(int mode, int processors, ThreadGroup tg) {
        ArrayList<ThreadSplitter> threads = new ArrayList<ThreadSplitter>();
        int i;
        for( i = 0; i < num_active; ++i ) {
            Animat a = animats.get(i);
            if( a.decomposed ) {
                animats.add(animats.remove(i--));
                --num_active;
                if( i < num_alive - 1 )
                    --num_alive;
            }
            else if( !a.alive && i < num_alive - 1 )
                animats.add( --num_alive, animats.remove( i-- ) );
        }

        for ( i = 0; i < processors; ++i )
            threads.add( new ThreadSplitter( mode, this, tg, "TS_"+thread_UID+"_"+i) );
        
        int thread_size = threads.size(),
                total_collision_checks = num_active * ( num_active - 1 ) / 2,
                each_alive = num_alive / thread_size,
                each_coll_check = total_collision_checks / thread_size;
        for( i = 0; i < thread_size; ++i ) {
            ThreadSplitter t = threads.get(i);
            t.minAlive = i * each_alive;
            t.minViable = i * each_coll_check;
            if( i == thread_size - 1 ) {
                t.maxAlive = num_alive;
                t.checkViable = total_collision_checks - ( i * each_coll_check );
            }
            else {
                t.maxAlive = ( i + 1 ) * each_alive;
                t.checkViable = each_coll_check;
            }
        }
        
        return threads;
    }
    public void runThreads() {
        final int processors = Runtime.getRuntime().availableProcessors();
        ThreadGroup tg = new ThreadGroup( "ThreadSplitters" + thread_UID++ );
        float PROC_SCALE = 1f;
        int TIMEOUT = 5;
        
        for( int mode = 0; mode < ThreadSplitter.NUM_MODES; ++mode ) {
            ArrayList<ThreadSplitter> threads = prepThreads(mode, (int)Util.max(1, processors * PROC_SCALE), tg);
            for( int i = 0; i < threads.size(); ) {
                if( tg.activeCount() < processors ) {
                    ThreadSplitter th = threads.get( i++ );
                    th.start();
                }
                else
                    try { Thread.sleep(TIMEOUT); }
                        catch (InterruptedException e) { e.printStackTrace(); }
            }
            while( tg.activeCount() > 0 ) {
                try { Thread.sleep(TIMEOUT); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }
    public void run() {
        iteration = 0;
        timestep = 1;
        num_active = num_alive = animats.size();
        long timer = System.currentTimeMillis();
        int last_iter_update = 0;

        while( bothStillAlive() ) {
            iteration++;
            runThreads();
            if( (System.currentTimeMillis() - timer) > 40) {
                applet.redraw();
                timer = System.currentTimeMillis() - timer;
                fps = (float)( iteration - last_iter_update ) / ( (float)timer / 1000f );
                last_iter_update = iteration;
                timer = System.currentTimeMillis();
            }
        }
    }
    
    public void drawMetadata() {
        applet.textSize(32);
        applet.fill(19,155,209);
        applet.text("IPS: "+fps,35,35);
    }
    
    public void draw() {
        // fill in background
        applet.background(0);
        applet.ellipseMode(PApplet.RADIUS);

        // draw range of world
        applet.fill(255);
        applet.ellipse(0, 0, radius, radius);
        
        // draw marsh
        applet.fill(170, 123, 57);
        applet.ellipse(marsh_position.getX(), marsh_position.getY(), marsh_radius, marsh_radius);
        
        // draw animats
        for( int i = 0; i < animats.size(); ++i ) {
            Animat a = animats.get(i);
            if( a != null && !a.decomposed )
                a.draw();
        }
    }
    
    class ThreadSplitter extends Thread {
        World world;
        static final int MODE_CONTROL = 0, MODE_MOVE = 1, MODE_COLLIDE = 2, NUM_MODES = 3;
        int sim_mode;
        int minAlive, maxAlive, minViable, checkViable;
        
        public ThreadSplitter(int mode, World world, ThreadGroup group, String name) {
            super( group, name );
            this.sim_mode = mode;
            this.world = world;
        }
        public void run() {
            if( sim_mode == MODE_CONTROL )
                control( world.iteration, world.timestep );
            if( sim_mode == MODE_MOVE )
                move( world.timestep );
            if( sim_mode == MODE_COLLIDE )
                checkForCollisions();
        }
        
        public void control( int iteration, float timestep ) {
            for( int i = minAlive; i < maxAlive; ++i ) {
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
            for( int i = minAlive; i < maxAlive; ++i ) {
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
        
        // resolve any collision between animats
        public void checkForCollisions() {
            int i = 0, j = 1, count = 0, next, check = 0;
            boolean setJ = false;
            float dist, radius_diff;
            Vector2f diff = new Vector2f();
            
            while( check < minViable ) {
                next = ( world.num_active - i ) * ( world.num_active - i - 1 );
                if( next + check > minViable ) {
                    j = i + ( minViable - check );
                    break;
                }
                check += next;
                ++i;
            }
                
            for( ; i < world.num_active; ++i ) {

                Animat a = animats.get(i);
                if( a.decomposed )
                    continue;
                
                for( j = setJ ? i + 1 : j; j < world.num_active; ++j ) {
                    
                    Animat b = animats.get(j);
                    if( b.decomposed  || ( !a.alive && !b.alive ) )
                        continue;
                    
                    if( ++count > checkViable )
                        return;

                    diff.set( a.position.getX() - b.position.getX(), a.position.getY() - b.position.getY() );
                    dist = diff.getLengthSquared();
                    if( Util.square( a.radius + b.radius + 1 ) - dist > 0 ) {
                        dist = Util.sqrt(dist);
                        radius_diff = a.radius + b.radius + 1 - dist;
                        diff.scale( radius_diff / dist / 2 );
                        a.position.addEquals( diff );
                        b.position.subtractEquals( diff );

                        a.collideWithAnimat(b);
                        b.collideWithAnimat(a);
                    }
                }
                setJ = true;
            }
        }
    }
}
