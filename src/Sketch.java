import processing.core.*;

public class Sketch extends PApplet {

    private static final long serialVersionUID = 1L;
    Simulation.Simulation sim;
    DisposeHandler dh;
    public float scale = 0.5f, trX = 0, trY = -150;

    public void setup() {
        size(500, 500);
        noLoop();
        dh = new DisposeHandler(this);
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
        translate(250, 325);
        scale(scale);
        translate(trX, trY);
    }

    public void keyPressed() {
        float dscale = 1.1f, dtr = 10;
        if (key == CODED) {
            if (keyCode == UP)
                trY += dtr;
            else if (keyCode == DOWN)
                trY -= dtr;
            else if (keyCode == RIGHT)
                trX -= dtr;
            else if (keyCode == LEFT)
                trX += dtr;
        } else if (key == '=')
            scale *= dscale;
        else if (key == '-')
            scale /= dscale;
    }

    // attempt to kill the independent threads when the applet is destroyed
    public class DisposeHandler {
        DisposeHandler(PApplet pa) {
            pa.registerMethod("dispose", this);
        }
        public void dispose() {
            System.out.println("Attempting to kill threads.");
            Simulation.Simulation.th.markStop();
        }
    }
}