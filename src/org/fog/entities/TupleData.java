package org.fog.entities;

import org.apache.commons.math3.util.Pair;

public class TupleData {
    private Pair<Double, Double> locationXY;
    private int ID;
    private int Type;
    private double Deadline;

    public TupleData() {

    }

    public TupleData(Pair<Double, Double> locationXY, int ID, int type, double deadline) {
        this.locationXY = locationXY;
        this.ID = ID;
        Type = type;
        Deadline = deadline;
    }

    public Pair<Double, Double> getLocationXY() {
        return locationXY;
    }

    public int getID() {
        return ID;
    }

    public int getType() {
        return Type;
    }

    public double getDeadline() {
        return Deadline;
    }

    public void setLocationXY(Pair<Double, Double> locationXY) {
        this.locationXY = locationXY;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setType(int type) {
        Type = type;
    }

    public void setDeadline(double deadline) {
        Deadline = deadline;
    }


}
