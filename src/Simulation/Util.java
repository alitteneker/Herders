package Simulation;

public class Util {
    public static float distance( float x, float y ) {
        return sqrt( x * x + y * y );
    }
    public static float distance( float x1, float y1, float x2, float y2 ) {
        return distance( x1-x2, y1-y2 );
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
}
