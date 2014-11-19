package Sensors;

import Animats.Animat;
import Animats.Sheep;
import Animats.Wolf;
import Simulation.Util;
import Simulation.World;

public class GradientSensor extends Sensor {

    int num_results = 4;
    int type;
    public static final int TYPE_SHEEP = 1, TYPE_WOLVES = 2, TYPE_OBSTACLES = 3, TYPE_MARSH = 4, TYPE_CARCASS = 5;

    public GradientSensor(int type) {
        super();
        this.type = type;
    }
    public float[] sense(Animat ind, World world) {
        float[] results = new float[num_results];
        results[0] = measureAt( ind.pos_x - ind.radius, ind.pos_y, ind, world );
        results[1] = measureAt( ind.pos_x + ind.radius, ind.pos_y, ind, world );
        results[2] = measureAt( ind.pos_x, ind.pos_y - ind.radius, ind, world );
        results[3] = measureAt( ind.pos_x, ind.pos_y + ind.radius, ind, world );
        return results;
    }
    protected float measureAt(float x, float y, Animat ind, World world ) {
        float ret = 0;
        if( type == TYPE_SHEEP || type == TYPE_WOLVES || type == TYPE_CARCASS ) {
            for( int i = 0; i < world.animats.size(); ++i ) {
                Animat check = world.animats.get(i);
                if( check.decomposed )
                    continue;
                if( ( check instanceof Sheep && ( type == TYPE_SHEEP || type == TYPE_CARCASS ) && ( type == TYPE_SHEEP ) == check.alive )
                        || ( check instanceof Wolf && type == TYPE_WOLVES ) )
                    ret += Util.invert( Util.distance(x, y, check.pos_x, check.pos_y) );
            }
        }
        else if( type == TYPE_OBSTACLES )
            ret = Util.distance(x, y) / world.radius;
        else if( type == TYPE_MARSH )
            ret = Util.invert( Util.distance(x, y, world.marsh_pos_x, world.marsh_pos_y) );

        return ret;
    }
}
