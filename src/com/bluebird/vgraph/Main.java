package com.bluebird.vgraph;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private static Runnable drawer;
    private final XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
    private final CategoryAxis timeAxis = new CategoryAxis();
    private final NumberAxis displacementAxis = new NumberAxis();
    // private final HashMap<XYChart.Data<String, Number>, ArrayList<Integer>> connectionList = new HashMap<>();
    private final ArrayList<Bar> bars = new ArrayList<>();
    private final BorderPane root = new BorderPane();
    private final StackPane histogramPane = new StackPane();
    private final VBox visibilityGraphPane = new VBox();
    private final HBox switchPane = new HBox();
    private final HBox visibiltyCanvas = new HBox();
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private ObservableList<XYChart.Data<String, Number>> dataArrayList = FXCollections.observableArrayList();
    private LineChart<String, Number> lineChart;
    private BarChart<String, Number> barChart;

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        prepareSwitchPane();
        root.setBottom(switchPane);
        prepareHistogramPane();

        primaryStage.setTitle("Visibility Graph");
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

        showHistogram();
    }

    private void prepareSwitchPane() {
        ToggleButton edit = new ToggleButton("Edit values");
        ToggleButton reset = new ToggleButton("Create new graph");
        ToggleButton histogram = new ToggleButton("Show histogram");
        ToggleButton visibility = new ToggleButton("Show visibilty graph");
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.selectToggle(histogram);
        toggleGroup.getToggles().addAll(edit, reset, histogram, visibility);
        histogram.setOnAction(e -> showHistogram());
        visibility.setOnAction(e -> showVisibilityGraph());

        switchPane.setAlignment(Pos.CENTER);
        switchPane.getChildren().addAll(edit, reset, histogram, visibility);
    }

    private void prepareHistogramPane() {
        displacementAxis.setLowerBound(0);

        barChart = new BarChart<>(timeAxis, displacementAxis);
        lineChart = new LineChart<>(timeAxis, displacementAxis);

        populateData(25);
        // populateAbsoluteData();

        timeAxis.setAnimated(true);
        displacementAxis.setAnimated(true);
        barChart.setAnimated(true);
        lineChart.setAnimated(true);

        barChart.getData().add(barSeries);
        barChart.setOpacity(.5);
        lineChart.setLegendVisible(false);
        barChart.setLegendVisible(false);

        histogramPane.getChildren().addAll(lineChart, barChart);
        histogramPane.setAlignment(Pos.CENTER);
        prepareDrawer();
    }

    private void prepareVisibilityPane() {
        visibilityGraphPane.setAlignment(Pos.CENTER);
    }

    private void showHistogram() {
        root.setCenter(histogramPane);
        startDrawing();
    }

    private void showVisibilityGraph() {
        root.setCenter(visibilityGraphPane);
    }

    private void prepareDrawer() {
        drawer = () -> {
            try {
                Thread.sleep(1000);
                final SimpleBooleanProperty vis = new SimpleBooleanProperty(false);
                vis.addListener((observableValue, aBoolean, t1) -> {
                    if (t1) {
                        backgroundExecutor.execute(() -> {
                            try {
                                Thread.sleep(1000);
                                calculateVisibilityEdges();
                                showEdges();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
                Platform.runLater(() -> {
                    if (!barSeries.getData().equals(dataArrayList)) {
                        barChart.getData().clear();
                        barSeries.getData().clear();
                        lineChart.getData().clear();

                        barChart.getData().add(barSeries);

                        barSeries.setData(dataArrayList);
                        vis.setValue(true);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void startDrawing() {
        backgroundExecutor.execute(drawer);
    }

    private void calculateVisibilityEdges() {
        bars.clear();
        ObservableList<XYChart.Data<String, Number>> dataList = dataArrayList;
        int total = dataList.size();
        int count = 0;
        while (count < total) {
            XYChart.Data<String, Number> present = dataList.get(count);
            Bar b = new Bar(present);
            int temp = count + 1;
            boolean isVisible;
            double ta = Double.parseDouble(present.getXValue());
            double ya = present.getYValue().doubleValue();
            //   System.out.println("Checking for (" + ta + "," + ya + ") "+count);
            XYChart.Data<String, Number> intermediate = present;
            while (temp < total) {
                XYChart.Data<String, Number> target = dataList.get(temp);
                double tb = Double.parseDouble(target.getXValue());
                double yb = target.getYValue().doubleValue();
                if (temp - 1 == count) {
                    b.connectTo(temp);
                    intermediate = target;
                } else {
                    double tc = Double.parseDouble(intermediate.getXValue());
                    double yc = intermediate.getYValue().doubleValue();
                    double div = (ya - yb) * ((tb - tc) / (tb - ta));

                    isVisible = (yc < (div + yb));
                    if (isVisible) {
                        b.connectTo(temp);
                        //    System.out.println("\tConnected to : ("+tb+","+yb+") "+temp);
                        //     System.out.println(count+"->"+temp);
                        if (yb > intermediate.getYValue().doubleValue()) {
                            //  System.out.println("Reassigned im to " + target.getXValue() + "," + target.getYValue());
                            intermediate = target;
                        }
                    }

                }
                temp++;
            }
            bars.add(b);
            count++;
        }
    }

    private void showEdges() {
        final int[] count = {0};
        bars.forEach(bar -> {
            Double ta = bar.getXValue();
            Double ya = bar.getYValue();
            bar.getConnectionList().forEach(connection -> {
                //       System.out.println(count[0] +"->"+connection);
                Bar to = bars.get(connection);
                Double tb = to.getXValue();
                Double yb = to.getYValue();
                backgroundExecutor.execute(() -> drawEdge(ta, ya, tb, yb));
            });
            count[0]++;
        });
    }

    private void drawEdge(double ta, double ya, double tb, double yb) {
        XYChart.Series<String, Number> edge = new XYChart.Series<>();
        edge.getData().add(new XYChart.Data<>((ta + "").replace(".0", ""), ya));
        edge.getData().add(new XYChart.Data<>((tb + "").replace(".0", ""), yb));
        //   System.out.println("(" + ta + "," + ya + ")->(" + tb + "," + yb + ")");
        Platform.runLater(() -> lineChart.getData().add(edge));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void populateAbsoluteData() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("25", 35);
        map.put("30", 20);
        map.put("35", 15);
        map.put("40", 34);
        map.put("45", 35);
        map.put("50", 20);
        map.put("55", 15);
        map.put("60", 34);
        map.put("65", 35);
        map.put("70", 20);
        map.put("75", 15);
        map.put("80", 34);
        map.put("85", 35);
        map.forEach((s, integer) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(s, integer);
            dataArrayList.add(data);
        });
        dataArrayList.sort(Comparator.comparing(XYChart.Data::getXValue));
    }

    private void populateData(int count) {
        Random r = new Random();
        HashSet<String> strings = new HashSet<>();
        while (count > 0) {
            Integer y = r.nextInt(100);
            Integer t = r.nextInt(100);
            while (strings.contains(t + "") || t < 10)
                t = r.nextInt(100);
            while (y < 10)
                y = r.nextInt(100);
            strings.add(t + "");
            XYChart.Data<String, Number> data = new XYChart.Data<>(t.toString(), y);
            dataArrayList.add(data);
            count--;
        }
        dataArrayList.sort(Comparator.comparing(XYChart.Data::getXValue));
        timeAxis.setCategories(FXCollections.observableArrayList(strings).sorted());
        calculateVisibilityEdges();
    }

    private void halfCircle() {
        Arc arc = new Arc(50, 50, 25, 25, 0, 180);
        arc.setType(ArcType.OPEN);
        arc.setStrokeWidth(10);
        arc.setStroke(Color.CORAL);
        arc.setStrokeType(StrokeType.INSIDE);
        arc.setFill(null);
    }
}
