package Sensors;

import Animats.Animat;
import Animats.Sheep;
import Animats.Wolf;
import Simulation.Util;
import Simulation.World;

public class GradientSensor extends Sensor {

    public int type;
    public static final int TYPE_SHEEP = 1, TYPE_WOLVES = 2, TYPE_OBSTACLES = 3, TYPE_MARSH = 4, TYPE_CARCASS = 5;

    public GradientSensor(int type) {
        super();
        num_results = 4;
        this.type = type;
    }
    public float[] sense(Animat ind, World world) {
        float[] results = new float[num_results];
        float x = ind.position.getX(), y = ind.position.getY(), radius = ind.radius;
        results[0] = measureAt( x - radius, y,          ind, world );
        results[1] = measureAt( x + radius, y,          ind, world );
        results[2] = measureAt( x,          y - radius, ind, world );
        results[3] = measureAt( x,          y + radius, ind, world );
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
                    ret += Util.invert( Util.distance(x, y, check.position.getX(), check.position.getY()) );
            }
        }
        else if( type == TYPE_OBSTACLES )
            ret = Util.distance(x, y) / world.radius;
        else if( type == TYPE_MARSH )
            ret = Util.invert( 3 + Util.minMax( Util.distance(x, y, world.marsh_position.getX(), world.marsh_position.getY()) - world.marsh_radius, 0, 1000 ) );

        return ret;
    }
}
