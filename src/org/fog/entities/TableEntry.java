package org.fog.entities;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TableEntry{
    private int id;
    private Pair<Double, Double> locationXY;
    public Double depTime = 0.0;
    private long mips;
    private boolean mobility;
    private boolean redzoneStatus;
    private double linkBW;
    private double estWaitingTime;
    private double estExecutionTime;
    private double transDelay;
    private double propDelay;
    private double channelStatusRSSI;
    private double batteryLife;
    private double distance;
    private int lightReqRem;
    private int heavyReqRem;
    private int runningReq;
    private Map<Integer, Tuple> lightReqRemTuples;
    private Map<Integer, Tuple> heavyReqRemTuples;

    public Map<Integer, Tuple> getLightReqRemTuples() {
        return lightReqRemTuples;
    }

    public void setLightReqRemTuples(Map<Integer, Tuple> lightReqRemTuples) {
        this.lightReqRemTuples = lightReqRemTuples;
    }

    public Map<Integer, Tuple> getHeavyReqRemTuples() {
        return heavyReqRemTuples;
    }

    public void setHeavyReqRemTuples(Map<Integer, Tuple> heavyReqRemTuples) {
        this.heavyReqRemTuples = heavyReqRemTuples;
    }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pair<Double, Double> getLocationXY() {
        return locationXY;
    }

    public void setLocationXY(Pair<Double, Double> locationXY) {
        this.locationXY = locationXY;
    }

    public long getMips() {
        return mips;
    }

    public void setMips(long mips) {
        this.mips = mips;
    }

    public boolean isMobility() {
        return mobility;
    }

    public void setMobility(boolean mobility) {
        this.mobility = mobility;
    }
    public boolean isRedzoneStatus() {
        return redzoneStatus;
    }

    public void setRedzoneStatus(boolean redzoneStatus) {
        this.redzoneStatus = redzoneStatus;
    }
    public double getEstWaitingTime() {
        return estWaitingTime;
    }

    public void setEstWaitingTime(double estWaitingTime) {
        this.estWaitingTime = estWaitingTime;
    }


    public double getLinkBW() {
        return linkBW;
    }

    public void setLinkBW(double linkBW) {
        this.linkBW = linkBW;
    }

    public double getEstExecutionTime() {
        return estExecutionTime;
    }

    public void setEstExecutionTime(double estExecutionTime) {
        this.estExecutionTime = estExecutionTime;
    }

    public double getTransDelay() {
        return transDelay;
    }

    public void setTransDelay(double transDelay) {
        this.transDelay = transDelay;
    }

    public double getPropDelay() {
        return propDelay;
    }

    public void setPropDelay(double propDelay) {
        this.propDelay = propDelay;
    }

    public double getChannelStatusRSSI() {
        return channelStatusRSSI;
    }

    public void setChannelStatusRSSI(double channelStatusRSSI) {
        this.channelStatusRSSI = channelStatusRSSI;
    }

    public double getBatteryLife() {
        return batteryLife;
    }

    public void setBatteryLife(double batteryLife) {
        this.batteryLife = batteryLife;
    }

//    public TableEntry(int id, Pair<Double, Double> locationXY, long mips, boolean mobility, double linkBW, double estWaitingTime, double estExecutionTime, double transDelay, double propDelay, double channelStatusRSSI, double batteryLife, double distance, int lightReqRem, int heavyReqRem, int runningReq) {
//        this.id = id;
//        this.locationXY = locationXY;
//        this.mips = mips;
//        this.mobility = mobility;
//        this.linkBW = linkBW;
//        this.estWaitingTime = estWaitingTime;
//        this.estExecutionTime = estExecutionTime;
//        this.transDelay = transDelay;
//        this.propDelay = propDelay;
//        this.channelStatusRSSI = channelStatusRSSI;
//        this.batteryLife = batteryLife;
//        this.distance = distance;
//        this.lightReqRem = lightReqRem;
//        this.heavyReqRem = heavyReqRem;
//        this.runningReq = runningReq;
//    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getLightReqRem() {
        return lightReqRem;
    }

    public void setLightReqRem(int lightReqRem) {
        this.lightReqRem = lightReqRem;
    }

    public int getHeavyReqRem() {
        return heavyReqRem;
    }

    public void setHeavyReqRem(int heavyReqRem) {
        this.heavyReqRem = heavyReqRem;
    }

    public int getRunningReq() {
        return runningReq;
    }

    public void setRunningReq(int runningReq) {
        this.runningReq = runningReq;
    }

    public void incLightReqRem(){
        this.lightReqRem++;
    }
    public void decLightReqRem(){
        this.lightReqRem--;
    }
    public void incHeavyReqRem(){
        this.heavyReqRem++;
    }
    public void decHeavyReqRem(){
        this.heavyReqRem--;
    }

    public TableEntry (int id, Pair<Double, Double> locationXY, long mips, boolean mobility,  double linkBW, double estWaitingTime, double transDelay, double propDelay, double channelStatusRSSI, double batteryLife, double distance, int lightReqRem, int heavyReqRem, int runningReq){
        this.id = id;
        this.locationXY = locationXY;
        this.mips = mips;
        this.linkBW = linkBW;
        this.mobility = mobility;
        this.estWaitingTime = estWaitingTime;
        this.transDelay = transDelay;
        this.propDelay = propDelay;
        this.channelStatusRSSI = channelStatusRSSI;
        this.batteryLife = batteryLife;
        this.distance = distance;
        this.lightReqRem = lightReqRem;
        this.heavyReqRem = heavyReqRem;
        this.runningReq = runningReq;

        this.setHeavyReqRemTuples(new HashMap<Integer, Tuple>());
        this.setLightReqRemTuples(new HashMap<Integer, Tuple>());
    }

    public int updateStatus() {
        Pair<Double, Double> fogLoc, mainFogLoc;
        if(this.depTime <= CloudSim.clock())
            return -1;
        if (Math.abs(this.getLocationXY().getFirst() - 7.5) > 7.5 || Math.abs(this.getLocationXY().getSecond() - 7.5) > 7.5){
            this.redzoneStatus = true;
            if(Paras.locDebug) {
                try {
                    BufferedWriter bf = new BufferedWriter(new FileWriter(CloudSim.getEntity(this.id).relatedFile, true));
                    bf.write(this.getLocationXY().getFirst()+" "+this.getLocationXY().getSecond()+"\n");
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//                System.out.println("time: "+ CloudSim.clock()+" - redzone: " + CloudSim.getEntityName(this.id)+" loc: "+ this.getLocationXY()+ " battery: "+this.getBatteryLife());
            if (Math.abs(this.getLocationXY().getFirst() - 7.5) > 12.5 || Math.abs(this.getLocationXY().getSecond() - 7.5) > 12.5)
                return -1;
            else
                return 1;
        } else{
            this.redzoneStatus = false;
            if(Paras.locDebug) {
                try {
                    BufferedWriter bf = new BufferedWriter(new FileWriter(CloudSim.getEntity(this.id).relatedFile, true));
                    bf.write(this.getLocationXY().getFirst()+" "+this.getLocationXY().getSecond()+"\n");
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//                System.out.println("time: "+ CloudSim.clock()+" - safe: " + CloudSim.getEntityName(this.id)+" loc: "+ this.getLocationXY()+ " battery: "+this.getBatteryLife());
            return 0;
        }

    }
}


