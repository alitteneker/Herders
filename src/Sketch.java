import processing.core.*;

public class Sketch extends PApplet {

    private static final long serialVersionUID = 1L;
    Simulation.Simulation sim;
    public void setup() {
        size(1000,1000);
        noLoop();
        sim = new Simulation.Simulation();
        sim.runSimulation(this);
    }

    public void draw() {
        pushMatrix();
        scale(0.6f);
        Simulation.Simulation.draw();
        popMatrix();
    }
}