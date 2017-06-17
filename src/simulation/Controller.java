package simulation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simulationmodels.*;
import util.CanvasPane;
import util.SimpleShapePainter;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;


public class Controller implements Initializable{

    private @FXML
    StackPane stackPane;

    private @FXML
    GridPane gridPane;

    private AnchorPane anchorPane;
    private TrafficManager manager;
    private CarModelGenerator carModelGenerator;
    private TrafficLightsController lightsController;

    private TextField textFieldW;
    private TextField textFieldNW;
    private TextField textFieldSW;
    private TextField textFieldNE;
    private TextField textFieldSE;
    private TextField textFieldE;

    private Slider sliderW;
    private Slider sliderNW;
    private Slider sliderNE;
    private Slider sliderSW;
    private Slider sliderSE;
    private Slider sliderE;

    private Button buttonPlay;
    private Button buttonStop;

    private CrossroadsView crossroadsViewWest;
    private CrossroadsView crossroadsViewEast;

    private RoadModel roadWW;
    private RoadModel roadNW;
    private RoadModel roadEW;
    private RoadModel roadSW;
    private RoadModel roadWE;
    private RoadModel roadNE;
    private RoadModel roadEE;
    private RoadModel roadSE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /**
         * Create the Canvas and add it to the window
         */
        CanvasPane canvasPane = new CanvasPane(800,900);
        Canvas canvas = canvasPane.getCanvas();
        anchorPane = new AnchorPane();
        stackPane.getChildren().addAll(canvasPane,anchorPane);

        setUpCarGenerationControl();

        /**
         * Prepare background
         */
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(Color.CHOCOLATE);
        context.fillRect(0,0,800,600);

        /**
         * Draw the crossroads
         */
        drawCrossroads(context);

        /**
         * Add traffic lights to the crossroads
         */
        setUpRoadModelsAndTrafficLights(canvasPane);

        /**
         * Set up the car generation
         */
        CopyOnWriteArrayList<CarModel> carModels = new CopyOnWriteArrayList<>();
        carModelGenerator = new CarModelGenerator(anchorPane, carModels);
        setUpCarModelGenerator();

        /**
         * Set up car generation sliders listeners
         */
        setUpSlidersListeners();

        /**
         * Set up the play and pause buttons listeners
         */
        setUpButtonListeners(carModels);

    }

    private void setUpButtonListeners(CopyOnWriteArrayList<CarModel> carModels) {
        List<TrafficLightsModel> lightsModels = new ArrayList<>();
        lightsModels.add(roadNW.getTrafficLightsModelEndB());
        lightsModels.add(roadSW.getTrafficLightsModelEndA());
        lightsModels.add(roadEW.getTrafficLightsModelEndA());
        lightsModels.add(roadWW.getTrafficLightsModelEndB());
        lightsModels.add(roadNE.getTrafficLightsModelEndB());
        lightsModels.add(roadSE.getTrafficLightsModelEndA());
        lightsModels.add(roadEE.getTrafficLightsModelEndA());
        lightsModels.add(roadWE.getTrafficLightsModelEndB());

        ArrayList<RoadModel> roadModels = new ArrayList<>();
        roadModels.add(roadEW);
        roadModels.add(roadNW);
        roadModels.add(roadSW);
        roadModels.add(roadWW);

        roadModels.add(roadEE);
        roadModels.add(roadNE);
        roadModels.add(roadSE);
        roadModels.add(roadWE);

        buttonPlay.setOnAction(event -> runSimulation(roadModels, carModels, lightsModels));
        buttonStop.setOnAction(event -> this.stopSimulation());
    }

    private void setUpSlidersListeners() {
        sliderNW.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/ sliderNW.getValue());
                carModelGenerator.setTimeBetweenCars(0, t);
                textFieldNW.setText(Integer.toString((int)sliderNW.getValue()));
            }
        });

        sliderSW.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/ sliderSW.getValue());
                carModelGenerator.setTimeBetweenCars(2, t);
                textFieldSW.setText(Integer.toString((int)sliderSW.getValue()));
            }
        });

        sliderNE.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/ sliderNE.getValue());
                carModelGenerator.setTimeBetweenCars(4, t);
                textFieldNE.setText(Integer.toString((int)sliderNE.getValue()));
            }
        });

        sliderSE.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/ sliderSE.getValue());
                carModelGenerator.setTimeBetweenCars(5, t);
                textFieldSE.setText(Integer.toString((int)sliderSE.getValue()));
            }
        });

        sliderW.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/sliderW.getValue());
                carModelGenerator.setTimeBetweenCars(1, t);
                textFieldW.setText(Integer.toString((int)sliderW.getValue()));
            }
        });

        sliderE.valueChangingProperty().addListener((observable, wasChanging, isNowChanging) -> {
            if (!isNowChanging) {
                int t = (int)(60000/sliderE.getValue());
                carModelGenerator.setTimeBetweenCars(3, t);
                textFieldE.setText(Integer.toString((int)sliderE.getValue()));
            }
        });
    }

    private void setUpCarModelGenerator() {
        /**
         * Traffic coming from the north west road
         */
        List<Point3D> roadNWRoutes = new LinkedList<>();
        roadNWRoutes.add(new Point3D(0,3*roadNW.getRoadView().getRoadLength(),40));
        carModelGenerator.addRoadTraffic(new Point2D(
                roadNW.getRoadView().getLeftUpperCorner().getX() + roadNW.getRoadView().getLaneWidth()/3,
                roadNW.getRoadView().getLeftUpperCorner().getY()),
                (int)(60000/ sliderNW.getValue()),roadNWRoutes,roadNW.getRoadView().getLaneWidth()/3, roadNW.getRoadView().getLaneWidth()/3);
        /**
         * Traffic coming from the west road
         */
        List<Point3D> roadWRoutes = new LinkedList<>();
        roadWRoutes.add(new Point3D(5*roadWW.getRoadView().getRoadLength(),0,40));
        carModelGenerator.addRoadTraffic(new Point2D(
                        roadWW.getRoadView().getLeftUpperCorner().getX(),
                        roadWW.getRoadView().getLeftUpperCorner().getY() + roadWW.getRoadView().getLaneWidth()+roadWW.getRoadView().getLaneWidth()/3),
                (int)(60000/sliderW.getValue()),roadWRoutes,roadWW.getRoadView().getLaneWidth()/3, roadWW.getRoadView().getLaneWidth()/3);
        /**
         * Traffic coming from the south west road
         */
        List<Point3D> roadSWRoutes = new LinkedList<>();
        roadSWRoutes.add(new Point3D(0,-3*roadSW.getRoadView().getRoadLength(),40));
        carModelGenerator.addRoadTraffic(new Point2D(
                        roadSW.getRoadView().getLeftUpperCorner().getX()+roadSW.getRoadView().getLaneWidth()+roadSW.getRoadView().getLaneWidth()/3,
                        roadSW.getRoadView().getLeftUpperCorner().getY()+roadSW.getRoadView().getRoadLength()),
                (int)(60000/ sliderSW.getValue()),roadSWRoutes,roadSW.getRoadView().getLaneWidth()/3, roadSW.getRoadView().getLaneWidth()/3);

        /**
         * Traffic coming from the east road
         */
        List<Point3D> roadERoutes = new LinkedList<>();
        roadERoutes.add(new Point3D(-5*roadEE.getRoadView().getRoadLength(),0,40));
        carModelGenerator.addRoadTraffic(new Point2D(
                        roadEE.getRoadView().getLeftUpperCorner().getX()+roadEE.getRoadView().getRoadLength(),
                        roadEE.getRoadView().getLeftUpperCorner().getY()+roadEE.getRoadView().getLaneWidth()/3),
                (int)(60000/sliderE.getValue()), roadERoutes, roadEE.getRoadView().getLaneWidth()/3, roadEE.getRoadView().getLaneWidth()/3);
        /**
         * Traffic coming from the north east road
         */
        List<Point3D> roadNERoutes = new LinkedList<>();
        roadNERoutes.add(new Point3D(0,3*roadNE.getRoadView().getRoadLength(),40));
        carModelGenerator.addRoadTraffic(new Point2D(
                        roadNE.getRoadView().getLeftUpperCorner().getX() + roadNE.getRoadView().getLaneWidth()/3,
                        roadNE.getRoadView().getLeftUpperCorner().getY()),
                (int)(60000/ sliderNE.getValue()),roadNERoutes,roadNE.getRoadView().getLaneWidth()/3, roadNE.getRoadView().getLaneWidth()/3);
        /**
         * Traffic coming from the south east road
         */
        List<Point3D> roadSERoutes = new LinkedList<>();
        roadSERoutes.add(new Point3D(0,-3*roadSE.getRoadView().getRoadLength(),40));
        carModelGenerator.addRoadTraffic(new Point2D(
                        roadSE.getRoadView().getLeftUpperCorner().getX()+roadSE.getRoadView().getLaneWidth()+roadSE.getRoadView().getLaneWidth()/3,
                        roadSE.getRoadView().getLeftUpperCorner().getY()+roadSE.getRoadView().getRoadLength()),
                (int)(60000/ sliderSE.getValue()),roadSERoutes,roadSE.getRoadView().getLaneWidth()/3, roadSE.getRoadView().getLaneWidth()/3);
    }

    private void setUpRoadModelsAndTrafficLights(CanvasPane canvasPane) {
        roadNW = new RoadModel(crossroadsViewWest.getRoadNORTH());
        roadNW.addLightsEndB(5000,3000);

        roadEW = new RoadModel(crossroadsViewWest.getRoadEAST());
        roadEW.addLightsEndA(5000,3000);
        roadEW.getTrafficLightsModelEndA().setOffset(3000);

        roadSW = new RoadModel(crossroadsViewWest.getRoadSOUTH());
        roadSW.addLightsEndA(5000,3000);

        roadWW = new RoadModel(crossroadsViewWest.getRoadWEST());
        roadWW.addLightsEndB(5000,3000);
        roadWW.getTrafficLightsModelEndB().setOffset(3000);

        roadNE = new RoadModel(crossroadsViewEast.getRoadNORTH());
        roadNE.addLightsEndB(5000,3000);

        roadEE = new RoadModel(crossroadsViewEast.getRoadEAST());
        roadEE.addLightsEndA(5000,3000);
        roadEE.getTrafficLightsModelEndA().setOffset(3000);

        roadSE = new RoadModel(crossroadsViewEast.getRoadSOUTH());
        roadSE.addLightsEndA(5000,3000);

        roadWE = new RoadModel(crossroadsViewEast.getRoadWEST());
        roadWE.addLightsEndB(5000,3000);
        roadWE.getTrafficLightsModelEndB().setOffset(3000);

        canvasPane.getChildren().add(roadNW.getTrafficLightsModelEndB().getTrafficLightsView());

        canvasPane.getChildren().add(roadEW.getTrafficLightsModelEndA().getTrafficLightsView());

        canvasPane.getChildren().add(roadSW.getTrafficLightsModelEndA().getTrafficLightsView());

        canvasPane.getChildren().add(roadWW.getTrafficLightsModelEndB().getTrafficLightsView());

        canvasPane.getChildren().add(roadNE.getTrafficLightsModelEndB().getTrafficLightsView());

        canvasPane.getChildren().add(roadEE.getTrafficLightsModelEndA().getTrafficLightsView());

        canvasPane.getChildren().add(roadSE.getTrafficLightsModelEndA().getTrafficLightsView());

        canvasPane.getChildren().add(roadWE.getTrafficLightsModelEndB().getTrafficLightsView());
    }

    private void drawCrossroads(GraphicsContext context) {
        crossroadsViewWest = new CrossroadsView(new Point2D(380.0/2,280.0/2),
                2,40.0/2,260/2,360/2,250/2,350/2);
        SimpleShapePainter.drawShape(crossroadsViewWest,context);

        crossroadsViewEast = new CrossroadsView(new Point2D(585,280.0/2),
                2,40.0/2,260/2,360/2,250/2,350/2);
        SimpleShapePainter.drawShape(crossroadsViewEast,context);
    }

    private void setUpCarGenerationControl() {
        Label textW = new Label("Left crossroads - road west");
        Label textNW = new Label("Left crossroads - road north");
        Label textSW = new Label("Left crossroads - road south");
        Label textNE = new Label("Right crossroads - road north");
        Label textSE = new Label("Right crossroads - road south");
        Label textE = new Label("Right crossroads - road east");

        textFieldW = new TextField();
        textFieldW.setEditable(false);
        textFieldW.setMaxWidth(50);

        textFieldNW = new TextField();
        textFieldNW.setEditable(false);
        textFieldNW.setMaxWidth(50);

        textFieldSW = new TextField();
        textFieldSW.setEditable(false);
        textFieldSW.setMaxWidth(50);

        textFieldNE = new TextField();
        textFieldNE.setEditable(false);
        textFieldNE.setMaxWidth(50);

        textFieldSE = new TextField();
        textFieldSE.setEditable(false);
        textFieldSE.setMaxWidth(50);

        textFieldE = new TextField();
        textFieldE.setEditable(false);
        textFieldE.setMaxWidth(50);

        /**
         * Car frequency sliders - range from 5 cars per minute to 60 cars per minute
         */
        sliderW = new Slider(5, 60, 10);
        sliderNW = new Slider(5, 60, 10);
        sliderSW = new Slider(5, 60, 10);
        sliderNE = new Slider(5, 60, 10);
        sliderSE = new Slider(5, 60, 10);
        sliderE = new Slider(5, 60, 10);

        textFieldW.setText(Integer.toString((int)sliderW.getValue()));
        textFieldNW.setText(Integer.toString((int)sliderNW.getValue()));
        textFieldSW.setText(Integer.toString((int)sliderSW.getValue()));
        textFieldNE.setText(Integer.toString((int)sliderNE.getValue()));
        textFieldSE.setText(Integer.toString((int)sliderSE.getValue()));
        textFieldE.setText(Integer.toString((int)sliderE.getValue()));

        buttonPlay = new Button("Play");
        buttonStop = new Button("Stop");

        gridPane.setMinSize(400, 200);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.setAlignment(Pos.CENTER);

        Label generationControlPanelLabel = new Label("Car generation frequency on each road [car/minute]");
        generationControlPanelLabel.setStyle("-fx-font-weight: bolder");
        gridPane.add(generationControlPanelLabel, 0,0,3,1);

        gridPane.add(textW, 0, 1);
        gridPane.add(sliderW, 1, 1);
        gridPane.add(textFieldW, 2,1);

        gridPane.add(textNW, 0, 2);
        gridPane.add(sliderNW, 1, 2);
        gridPane.add(textFieldNW, 2, 2);

        gridPane.add(textSW, 0, 3);
        gridPane.add(sliderSW, 1, 3);
        gridPane.add(textFieldSW,2, 3);

        gridPane.add(textNE, 0, 4);
        gridPane.add(sliderNE, 1, 4);
        gridPane.add(textFieldNE, 2, 4);

        gridPane.add(textSE, 0, 5);
        gridPane.add(sliderSE, 1, 5);
        gridPane.add(textFieldSE, 2, 5);

        gridPane.add(textE, 0, 6);
        gridPane.add(sliderE, 1, 6);
        gridPane.add(textFieldE, 2,6);

        gridPane.add(buttonPlay, 0, 8);
        gridPane.add(buttonStop, 1, 8);
    }

    private void stopSimulation() {
        if (manager != null) {
            manager.stopManager();
        }

        if (carModelGenerator != null) {
            try {
                carModelGenerator.stopGenerator();
            } catch (InterruptedException e) {
                System.err.println("Couldn't stop the car generator!");
                e.printStackTrace();
                System.exit(1);
            }
        }

        if (lightsController != null) {
            try {
                lightsController.stopLights();
            } catch (InterruptedException e) {
                System.err.println("Couldn't stop the lights modelling!");
                e.printStackTrace();
                System.exit(1);
            }
        }

        Platform.runLater(() -> anchorPane.getChildren().clear());
    }

    private void runSimulation(ArrayList<RoadModel> roadModels, CopyOnWriteArrayList<CarModel> carModels, List<TrafficLightsModel> lightsModels) {
        lightsController = new TrafficLightsController(lightsModels);
        lightsController.startLights();
        carModelGenerator.startGenerator();
        manager = new TrafficManager(carModels, roadModels, anchorPane);
        manager.startManager();
    }

    public void setUpOnCloseRequest(Stage stage) {
        stage.setOnCloseRequest(event -> {
            stopSimulation();
            stage.close();
            System.exit(0);
        });
    }
}
