package com.bluebird.vgraph;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Main extends Application {

    private final XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
    private final CategoryAxis timeAxis = new CategoryAxis();
    private ObservableList<XYChart.Data<String, Number>> dataArrayList = FXCollections.observableArrayList();
    private LineChart<String, Number> lineChart;
    private BarChart<String, Number> barChart;

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane bp = new StackPane();
        bp.setMinSize(500, 500);

        NumberAxis displacement = new NumberAxis();
        displacement.setLowerBound(0);

        barChart = new BarChart<>(timeAxis, displacement);
        lineChart = new LineChart<>(timeAxis, displacement);

        populateData(20);
        // populateAbsoluteData();

        timeAxis.setAnimated(true);
        displacement.setAnimated(true);
        barChart.setAnimated(true);
        lineChart.setAnimated(true);

        barChart.getData().add(barSeries);
        barChart.setOpacity(.5);
        lineChart.setLegendVisible(false);
        barChart.setLegendVisible(false);

        bp.getChildren().addAll(lineChart, barChart);

        primaryStage.setTitle("Visibility Graph");
        primaryStage.setScene(new Scene(bp, 500, 500));
        primaryStage.show();

        startDrawing();
    }

    private void startDrawing() {
        Runnable r = () -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> barSeries.setData(dataArrayList));
                Thread.sleep(1000);
                drawVisibility();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    private void drawVisibility() {
        ObservableList<XYChart.Data<String, Number>> dataList = barChart.getData().get(0).getData();
        int total = dataList.size();
        int count = 0;
        while (count < total) {
            XYChart.Data<String, Number> present = dataList.get(count);
            int temp = count + 1;
            boolean isVisible;
            double ta = Double.parseDouble(present.getXValue());
            double ya = present.getYValue().doubleValue();
            System.out.println("Checking for (" + ta + "," + ya + ")");
            XYChart.Data<String, Number> intermediate = present;
            while (temp < total) {
                XYChart.Data<String, Number> target = dataList.get(temp);
                double tb = Double.parseDouble(target.getXValue());
                double yb = target.getYValue().doubleValue();
                if (temp - 1 == count || tb == ta) {
                    drawEdge(ta, ya, tb, yb);
                    if (temp - 1 == count)
                        intermediate = target;
                } else {
                    double tc = Double.parseDouble(intermediate.getXValue());
                    double yc = intermediate.getYValue().doubleValue();
                    double div = (ya - yb) * ((tb - tc) / (tb - ta));

                    isVisible = (yc < (div + yb));
                    if (isVisible) {
                        drawEdge(ta, ya, tb, yb);
                        if (yb > intermediate.getYValue().doubleValue()) {
                            System.out.println("Reassigned im to " + target.getXValue() + "," + target.getYValue());
                            intermediate = target;
                        }
                    }

                }
                temp++;
            }
            count++;
        }
    }

    private void drawEdge(double ta, double ya, double tb, double yb) {
        XYChart.Series<String, Number> edge = new XYChart.Series<>();
        edge.getData().add(new XYChart.Data<>((ta + "").replace(".0", ""), ya));
        edge.getData().add(new XYChart.Data<>((tb + "").replace(".0", ""), yb));
        System.out.println("(" + ta + "," + ya + ")->(" + tb + "," + yb + ")");
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
    }
}
