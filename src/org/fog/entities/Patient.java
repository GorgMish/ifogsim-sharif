package org.fog.entities;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Patient  {
	private int area;
	public String name;
	public List<FogDevice> fogDevices;
	protected Pair<Double, Double> locationXY;
	protected String type;
	public Double depTime = 0.0;
	public double getxCoordinate() {
		return locationXY.getFirst();}
	public void setxCoordinate(double xCoordinate) {
		this.locationXY = new Pair<>(xCoordinate, locationXY.getSecond());
	}
	public void setyCoordinate(double yCoordinate) {
		this.locationXY = new Pair<>(locationXY.getFirst(), yCoordinate);
	}
	public double getyCoordinate() {
		return locationXY.getSecond();}
	public void setLocationXY(Pair<Double, Double> newXY){
		this.locationXY = new Pair<>(newXY);

		for (FogDevice fog :
				this.fogDevices) {
			fog.setLocationXY(this.locationXY);
		}
	}
	public Pair<Double, Double> getLocationXY(){
		return locationXY;
	}

	public Patient(int area) {
		this.area = area;
		this.fogDevices = new ArrayList<FogDevice>();
	}

	public File relatedFile;
	public BufferedWriter bufferWriter;

	public double calcDistance(Pair<Double, Double> l1, Pair<Double, Double> l2){
		double distance = Math.sqrt(
				Math.pow(l1.getFirst()-l2.getFirst(), 2) + Math.pow(l1.getSecond()-l2.getSecond(), 2));
		return distance;
	}

	public void removeFogDevices(List<FogDevice> devicesToRemove) {
		fogDevices.removeAll(devicesToRemove);
	}

	public void addFogDevices(List<FogDevice> devicesToAdd) {
		for (FogDevice device : devicesToAdd) {
			this.addFogDevice(device);
		}
	}

	public void removeFogDevice(FogDevice deviceToRemove) {
		fogDevices.remove(deviceToRemove);
	}

	public void addFogDevice(FogDevice deviceToAdd) {
		fogDevices.add(deviceToAdd);
		deviceToAdd.setLocationXY(this.locationXY);
	}


	public int updateIoTStatus() {
		Pair<Double, Double> fogLoc, mainFogLoc;
		double x = this.getLocationXY().getFirst();
		double y = this.getLocationXY().getSecond();
		if(this.depTime <= CloudSim.clock())
			return -1;
		if(Paras.IoTlocDebug) {
			try {
				BufferedWriter bf = new BufferedWriter(new FileWriter(this.relatedFile, true));;
				bf.write(this.name + ":  " + x+" "+y+"\n");
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (Math.abs(x - 7.5) > 7.5 || Math.abs(y - 7.5) > 7.5){
			return -1;
//			if (Math.abs(x - 7.5) > 12.5 || Math.abs(y - 7.5) > 12.5)
//				return -1;
//			else
//				return 0;
		} else{
			return 0;
		}

	}

}