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
    public volatile int iteration, num_active, num_alive;
    public volatile float timestep;
    public float fps = 0;
    public int thread_UID = 0;
    public ArrayList<ThreadSplitter> threads;
    
    // mark this as false for kill on next iterations
    public volatile boolean keep_going = true;
    public volatile int phase_control = -1;

    public World( PApplet applet ) {
        this.applet = applet;
        threads = new ArrayList<ThreadSplitter>();
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
    public void startThreads() {
        for( int i = 0; i < threads.size(); ++i )
            threads.get(i).kill = true;
        threads.clear();
                
        ThreadGroup tg = new ThreadGroup("ThreadSplitters_" + (thread_UID++));
        final int processors = Math.max( 1, Math.round( 1f * Runtime.getRuntime().availableProcessors() ) );
        
        for( int i = 0; i < processors; ++i ) {
            ThreadSplitter th = new ThreadSplitter( this, tg, "TS_"+thread_UID+"_"+i);
            threads.add(th);
            th.start();
        }
    }
    public void updateThreadLimits() {
        for( int i = 0; i < num_active; ++i ) {
            Animat a = animats.get(i);
            if( a.decomposed ) {
                animats.remove(a);
                animats.add(a);
                --num_active;
                if( i < num_alive - 1 )
                    --num_alive;
                --i;
            }
            else if( !a.alive && i < num_alive - 1 ) {
                animats.remove(a);
                animats.add( --num_alive, a );
                --i;
            }
        }
        int thread_size = threads.size(),
            total_collision_checks = num_active * ( num_active - 1 ) / 2,
            each_alive = num_alive / thread_size,
            each_coll_check = total_collision_checks / thread_size;
        for( int i = 0; i < thread_size; ++i ) {
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
    }
    public void runLogicThreads() {
        updateThreadLimits();
        ++iteration;

        final int thread_size = threads.size();
        for( int mode = 0; mode < ThreadSplitter.NUM_MODES; ++mode ) {
            phase_control = mode;
            
            int active_count = 1;
            while( active_count > 0 ) {
                try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); };
                active_count = 0;
                for( int ind = 0; ind < thread_size; ++ind ) {
                    ThreadSplitter th = threads.get(ind);
                    if( th.sim_mode == mode && th.iteration == iteration )
                        ++active_count;
                }
            }
        }
        phase_control = -1;
    }
    public void run() {
        iteration = 0;
        timestep = 1;
        num_active = num_alive = animats.size();
        long timer = System.currentTimeMillis();
        int last_iter_update = 0;

        startThreads();
        while( bothStillAlive() && keep_going ) {
            
            runLogicThreads();
            
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
        applet.fill(245, 201, 152);
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
        int sim_mode = 0;
        int minAlive, maxAlive, minViable, checkViable;
        int iteration = 1;
        boolean kill = false;
        
        public ThreadSplitter(World world, ThreadGroup group, String name) {
            super( group, name );
            this.world = world;
        }
        public void run() {
            while( world.keep_going && !kill ) {
                if( world.iteration < this.iteration || world.phase_control < sim_mode ) {
                    try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); };
                    continue;
                }
                    
                if( sim_mode == MODE_CONTROL )
                    control( world.iteration, world.timestep );
                if( sim_mode == MODE_MOVE )
                    move( world.timestep );
                if( sim_mode == MODE_COLLIDE )
                    checkForCollisions();
                
                ++sim_mode;
                if( sim_mode == NUM_MODES ) {
                    sim_mode = 0;
                    ++iteration;
                }
            }
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
