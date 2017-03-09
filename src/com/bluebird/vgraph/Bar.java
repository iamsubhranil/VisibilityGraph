/*
    Created By : iamsubhranil
    Date : 9/3/17
    Time : 11:32 PM
    Package : com.bluebird.vgraph
    Project : VisibiltyGraph
*/
package com.bluebird.vgraph;

import javafx.scene.chart.XYChart;

import java.util.ArrayList;

public class Bar {

    private final XYChart.Data<String, Number> data;
    private final ArrayList<Integer> connectionList = new ArrayList<>();

    public Bar(XYChart.Data<String, Number> data) {
        this.data = data;
    }

    public void connectTo(int num) {
        connectionList.add(num);
    }

    public Double getXValue() {
        return Double.parseDouble(data.getXValue());
    }

    public Double getYValue() {
        return data.getYValue().doubleValue();
    }

    public XYChart.Data<String, Number> getData() {
        return data;
    }

    public ArrayList<Integer> getConnectionList() {
        return connectionList;
    }
}
