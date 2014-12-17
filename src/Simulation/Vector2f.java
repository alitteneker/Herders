package Simulation;

public class Vector2f {
    public float x, y;
    public Vector2f() {
        set(0,0);
    }
    public Vector2f(float x, float y) {
        set(x, y);
    }
    public Vector2f(Vector2f set) {
        set(set);
    }
    public synchronized float getX() {
        return x;
    }
    public synchronized float getY() {
        return y;
    }
    public float getLength() {
        return Util.distance(x, y);
    }
    public float getLengthSquared() {
        return Util.distanceSquared(x, y);
    }
    public synchronized void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void set(Vector2f set) {
        set( set.getX(), set.getY() );
    }
    public synchronized void scale( float scale ) {
        x *= scale;
        y *= scale;
    }
    public Vector2f getScaled( float scale ) {
        Vector2f ret = new Vector2f(this);
        ret.scale(scale);
        return ret;
    }
    public void normalize() {
        setLength(1);
    }
    public Vector2f getNormalized() {
        Vector2f ret = new Vector2f(this);
        ret.normalize();
        return ret;
    }
    public void setLength( float length ) {
        scale( length / getLength() );
    }
    public void setMaxLength( float max ) {
        float len = getLength();
        if( len > max )
            scale( max / len );
    }
    public void addEquals( float x, float y ) {
        set( this.x + x, this.y + y );
    }
    public void addEquals( Vector2f other ) {
        set( x + other.getX(), y + other.getY() );
    }
    public void addEquals( Vector2f other, float scale ) {
        addEquals( other.getX() * scale, other.getY() * scale );
    }
    public Vector2f add( Vector2f other ) {
        return new Vector2f( x + other.getX(), y + other.getY() );
    }
    public void subtractEquals( float x, float y ) {
        set( this.x + x, this.y + y );
    }
    public void subtractEquals( Vector2f other ) {
        set( x - other.getX(), y - other.getY() );
    }
    public void subtractEquals( Vector2f other, float scale ) {
        addEquals( other, -scale );
    }
    public Vector2f subtract( Vector2f other ) {
        return new Vector2f( x - other.getX(), y - other.getY() );
    }
}
