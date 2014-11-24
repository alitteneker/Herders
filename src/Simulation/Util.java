package Simulation;

public class Util {
    public static boolean isNaN(float val) {
        return val != val;
    }
    public static float distanceSquared( float x, float y) {
        return x * x + y * y;
    }
    public static float distance( float x, float y ) {
        return sqrt( x * x + y * y );
    }
    public static float distanceSquared( float x1, float y1, float x2, float y2 ) {
        return distanceSquared( x1-x2, y1-y2 );
    }
    public static float distance( float x1, float y1, float x2, float y2 ) {
        return distance( x1-x2, y1-y2 );
    }
    public static float distance( Vector2f a, Vector2f b) {
        return distance( a.getX(), a.getY(), b.getX(), b.getY() );
    }
    public static float distanceSquared( Vector2f a, Vector2f b ) {
        return distanceSquared( a.getX(), a.getY(), b.getX(), b.getY() );
    }
    public static float distance( float[] point ) {
        float sum = 0;
        for( int i = 0; i < point.length; ++i )
            sum += point[i] * point[i];
        return sqrt(sum);
    }
    public static float[] minMaxLength( float[] point, float min, float max ) {
        float dist = distance( point );
        if( dist >= min && dist <= max )
            return point;
        float scale = ( dist > max ? max : min ) / dist;
        for( int i = 0; i < point.length; ++i )
            point[i] *= scale;
        return point;
    }
    public static float dotProduct( float[] a, float[] b ) {
        float ret = 0;
        int i = -1, len = Math.min(a.length, b.length);
        while( ++i < len )
            ret += a[i] * b[i];
        return ret;
    }
    public static float invert(float val) {
        if( val != 0 )
            val = 1f / val;
        return val;
    }
    public static float min(float a, float b) {
        return a > b ? b : a;
    }
    public static float max(float a, float b) {
        return a > b ? a : b;
    }
    public static float abs(float a) {
        return a < 0 ? -a : a;
    }
    public static float square( float val ) {
        return val*val;
    }
    public static int round(float val) {
        float r = val % 1;
        return (int)(r >= 0.5f ? val - r + 1 : val - r);
    }
    public static float sqrt(float val) {
        return (float)Math.sqrt(val);
    }
    public static float sin(float theta) {
        return (float)Math.sin(theta);
    }
    public static float cos(float theta) {
        return (float)Math.cos(theta);
    }
    public static float atan2(float x, float y) {
        return (float)Math.atan2(x, y);
    }
    public static float[] polarToCartesian(float r, float theta) {
        return new float[] { r * sin(theta), r * cos(theta) };
    }
    public static float[] cartesianToPolar(float x, float y) {
        return new float[] { distance(x, y), atan2(x, y) };
    }
    public static String getString(float[] data) {
        return join(" ",data);
    }
    public static String join( String separator, float[] data) {
        String ret = "";
        for( int i = 0; i < data.length; ++i ) {
            if( i > 0 )
                ret += separator;
            ret += data[i];
        }
        return ret;
    }
}
