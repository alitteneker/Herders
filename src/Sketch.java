import processing.core.*;

public class Sketch extends PApplet {

    private static final long serialVersionUID = 1L;
    Simulation.Simulation sim;
    public float scale = 0.6f, trX = 0, trY = -150;
    public void setup() {
        size(1000,1000);
        noLoop();
        sim = new Simulation.Simulation();
        sim.runSimulation(this);
    }

    public void draw() {
        pushMatrix();
        preTransform();
        Simulation.Simulation.draw();
        popMatrix();
        Simulation.Simulation.drawMetadata();
    }
    
    public void preTransform() {
        translate(500, 500);
        scale(scale);
        translate(trX, trY);
    }
    
    public void keyPressed() {
        float dscale = 1.1f, dtr = 10;
        if( key == CODED ) {
            if( keyCode == UP )
                trY += dtr;
            else if( keyCode == DOWN )
                trY -= dtr;
            else if( keyCode == RIGHT )
                trX -= dtr;
            else if( keyCode == LEFT )
                trX += dtr;
        }
        else if( key == '=' )
            scale *= dscale;
        else if( key == '-' )
            scale /= dscale;
    }
}