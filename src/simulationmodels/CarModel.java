package simulationmodels;

import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import util.NWSE;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Piotrek on 16.04.2017.
 */
public class CarModel extends Rectangle{

    private Queue<Transition> transitionsList;
    private Queue<NWSE> directionsList;
    private Transition currentTransition = null;
    private Boolean done = false;
    private Boolean stopped = true;
    private static final double bumperBuffer = 10.0;

    public CarModel(double x, double y, double width, double height) {
        super(x, y, width, height);
        transitionsList = new ArrayDeque<>();
        directionsList = new ArrayDeque<>();
    }

    public void addTransition(double moveX, double moveY, double speed){
        //Create directions queue
        NWSE direction = null;
        if(moveX > 0)
            direction = NWSE.E;
        if(moveX < 0)
            direction = NWSE.W;
        if(moveY > 0)
            direction = NWSE.S;
        if(moveY < 0)
            direction = NWSE.N;

        this.directionsList.add(direction);

        //Crete trnslations queue
        TranslateTransition t = new TranslateTransition();
        t.setNode(this);
        t.setByX(moveX);
        t.setByY(moveY);


        double distance = Math.sqrt(moveX*moveX + moveY*moveY);
        double duration = distance/speed;

        t.setDuration(Duration.seconds(duration));
        t.setCycleCount(1);
        t.setOnFinished(e ->{
            //one transition finished
            System.out.println("finished");
            this.transitionsList.poll();
            this.directionsList.poll();
            this.start();
        });

        this.transitionsList.add(t);


    }

    public void start() {
        this.stopped = false;
        if(!this.transitionsList.isEmpty()) {
            this.transitionsList.peek().play();
        }else{
            this.done = true;
        }
    }
    public void stop(){
        this.stopped = true;
        if(!this.transitionsList.isEmpty()) {
            this.transitionsList.peek().pause();
        }
    }

    public Boolean getStopped() {
        return stopped;
    }

    public double getBumperX(){
        switch (this.directionsList.peek()) {
            case E: {
                return this.getBoundsInParent().getMaxX()+1.0;
            }
            case W: {
                return this.getBoundsInParent().getMinX() - 1.0;
            }
            case N: {
                return this.getBoundsInParent().getMinX();
            }
            case S: {
                return this.getBoundsInParent().getMinX();
            }

        }
        return this.getBoundsInParent().getMinX();
    }

    public double getBumperY(){

        switch (this.directionsList.peek()) {
            case E: {
                return this.getBoundsInParent().getMinY();
            }
            case W: {
                return this.getBoundsInParent().getMinY();
            }
            case N: {
                return this.getBoundsInParent().getMinY() - 1.0;
            }
            case S: {
                return this.getBoundsInParent().getMaxY() + 1.0;
            }

        }
        return this.getBoundsInParent().getMinY();
    }

    public double getBumberWidth(){

        switch (this.directionsList.peek()) {
            case E: {
                return bumperBuffer;
            }
            case W: {
                return bumperBuffer;
            }
            case N: {
                return this.getBoundsInParent().getWidth();
            }
            case S: {
                return this.getBoundsInParent().getWidth();
            }

        }
        return bumperBuffer;
    }

    public double getBumperHeight() {

        switch (this.directionsList.peek()) {
            case E: {
                return this.getBoundsInParent().getHeight();
            }
            case W: {
                return this.getBoundsInParent().getHeight();
            }
            case N: {
                return bumperBuffer;
            }
            case S: {
                return bumperBuffer;
            }

        }
        return bumperBuffer;
    }

    public NWSE getDirection() {
        return this.directionsList.peek();
    }
}