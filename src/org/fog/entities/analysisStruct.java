package org.fog.entities;

import java.util.HashMap;

public class analysisStruct {
    public static final String AE = "AddEvent";
    public static final String HIT = "h";
    public static final String LOCK = "l";
    public static final String UNLOCK = "un";
    public static final String SETCS = "s";
    public static final String EVICT = "e";
    public static final String CHP = "ch";
    public static final String RESET = "re";
    public static final String FILEPOPU = "FP";

    public String type;
    public Double time;
    public Double runTime;
    public int src;
    public int dst;
    public Double delay;
    public int tag;
    public String tupleType;
    static public HashMap<String, Integer> numberOfLightOffloading = new HashMap<String, Integer>();
    static public HashMap<String, Integer> numberOfHeavyOffloading = new HashMap<String, Integer>();

    public analysisStruct(String type, Double time, Double runTime,Integer src, Integer dst, Double delay, Integer tag, String tupleType){
        this.type = type;
        this.time = time;
        this.runTime = runTime;
        this.src = src;
        this.dst = dst;
        this.delay = delay;
        this.tag = tag;
        this.tupleType = tupleType;
    }
    analysisStruct(String type, Double time){
        this.type = type;
        this.time = time;
    }

//    public String getType() {
//        return type;
//    }
//    public String getRouterName() {
//        return routerName;
//    }
//    public Double getTime() {
//        return time;
//    }
//    public Integer getPacket() {
//        return packet;
//    }
//    public Integer getFile() {
//        return file;
//    }
//    public void setType(String type) {
//        this.type = type;
//    }
//    public void setTime(Double time) {
//        this.time = time;
//    }
//    public void setPacket(Integer packet) {
//        this.packet = packet;
//    }
//    public void setRouterName(String routerName) {
//        this.routerName = routerName;
//    }
//    public void setFile(Integer file) {
//        this.file = file;
//    }
}
