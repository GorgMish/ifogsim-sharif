package org.fog.placement;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.fog.entities.analysisStruct;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.*;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.test.perfeval.Example1;
import org.fog.utils.*;
import org.fog.utils.distribution.DeterministicDistribution;

public class Controller extends SimEntity {
	
	public static boolean ONLY_CLOUD = false;
		
	private List<FogDevice> fogDevices;
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	private List<FogDevice> fogLayer1;

	private Map<String, Double> preEngCons;
	private Map<String, Integer> preLightTasks;
	private Map<String, Integer> preHeavyTasks;

	public List<FogDevice> getIoTMobs() {
		return IoTMobs;
	}

	public void setIoTMobs(List<FogDevice> ioTMobs) {
		IoTMobs = ioTMobs;
	}

	private List<FogDevice> IoTMobs;
	private List<Patient> patientMobs;


	private HashMap<Double, Double> analysis;
	
	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;

	private Map<String, ModulePlacement> appModulePlacementPolicy;
	private boolean runFinished;

	public Controller(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, List<FogDevice> fogLayer1, List<FogDevice> IoTMobs) {
		super(name);
		this.applications = new HashMap<String, Application>();
		this.preEngCons = new HashMap<String, Double>();
		this.preLightTasks = new HashMap<String, Integer>();
		this.preHeavyTasks = new HashMap<String, Integer>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setAppModulePlacementPolicy(new HashMap<String, ModulePlacement>());
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setActuators(actuators);
		setSensors(sensors);
		connectWithLatencies();
		setFogLayer1(fogLayer1);
		setIoTMobs(IoTMobs);
		this.patientMobs = new ArrayList<Patient>();
		CloudSim.runFinished = false;
		//TODO
//		gatewaySelection();
//		formClusters();
	}
	public void setFogLayer1(List<FogDevice> fogLayer1) {
		this.fogLayer1 = fogLayer1;
	}

	public List<FogDevice> getFogLayer1() {
		return fogLayer1;
	}

	private void gatewaySelection() {
// TODO Auto-generated method stub
		for(int i=0;i<getFogDevices().size();i++){
			FogDevice fogDevice = getFogDevices().get(i);
			int parentID=-1;
			if(fogDevice.getParentId()==-1) {
				double minDistance = Config.MAX_NUMBER;
				for(int j=0;j<getFogDevices().size();j++){
					FogDevice anUpperDevice = getFogDevices().get(j);
					if(fogDevice.getLevel()+1==anUpperDevice.getLevel()){
						double distance = calculateDistance(fogDevice,anUpperDevice);
						if(distance<minDistance){
							minDistance = distance;
							parentID = anUpperDevice.getId();}
					}
				}
			}
			fogDevice.setParentId(parentID);
		}
	}

	private void gatewayLayer1() {
// TODO Auto-generated method stub
		for(int i=0;i<getFogDevices().size();i++){
			FogDevice fogDevice = getFogDevices().get(i);
			int parentID=-1;
			if(fogDevice.getParentId()==-1) {
				double minDistance = Config.MAX_NUMBER;
				for(int j=0;j<getFogDevices().size();j++){
					FogDevice anUpperDevice = getFogDevices().get(j);
					if(fogDevice.getLevel()+1==anUpperDevice.getLevel()){
						double distance = calculateDistance(fogDevice,anUpperDevice);
						if(distance<minDistance){
							minDistance = distance;
							parentID = anUpperDevice.getId();}
					}
				}
			}
			fogDevice.setParentId(parentID);
		}
	}

	static Map<Integer, Integer> clusterInfo = new HashMap<Integer,Integer>();
	static Map<Integer, List<Integer>> clusters = new HashMap<Integer, List<Integer>>();
	private void formClusters() {
		for(FogDevice fd: getFogDevices()){
			clusterInfo.put(fd.getId(), -1);
		}
		int clusterId = 0;
		for(int i=0;i<getFogDevices().size();i++){
			FogDevice fd1 = getFogDevices().get(i);
			for(int j=0;j<getFogDevices().size();j++) {
				FogDevice fd2 = getFogDevices().get(j);
				if(fd1.getId()!=fd2.getId() && fd1.getParentId()==fd2.getParentId() && calculateDistance(fd1, fd2)<Config.CLUSTER_DISTANCE && fd1.getLevel()==fd2.getLevel())
				{
					int fd1ClusteriD = clusterInfo.get(fd1.getId());
					int fd2ClusteriD = clusterInfo.get(fd2.getId());
					if(fd1ClusteriD==-1 && fd2ClusteriD==-1){
						clusterId++;
						clusterInfo.put(fd1.getId(), clusterId);
						clusterInfo.put(fd2.getId(), clusterId);
					}
					else if(fd1ClusteriD==-1)
						clusterInfo.put(fd1.getId(), clusterInfo.get(fd2.getId()));
					else if(fd2ClusteriD==-1)
						clusterInfo.put(fd2.getId(), clusterInfo.get(fd1.getId()));
				}
			}
		}
		for(int id:clusterInfo.keySet()){
			if(!clusters.containsKey(clusterInfo.get(id))){
				List<Integer>clusterMembers = new ArrayList<Integer>();
				clusterMembers.add(id);
				clusters.put(clusterInfo.get(id), clusterMembers);
			}
			else
			{
				List<Integer>clusterMembers = clusters.get(clusterInfo.get(id));
				clusterMembers.add(id);
				clusters.put(clusterInfo.get(id), clusterMembers);
			}
		}
		for(int id:clusters.keySet())
			System.out.println(id+" "+clusters.get(id));
	}

	private double calculateDistance(FogDevice fogDevice, FogDevice anUpperDevice) {
// TODO Auto-generated method stub
		return Math.sqrt(Math.pow(fogDevice.getxCoordinate() - anUpperDevice.getxCoordinate(), 2.00) +
				Math.pow(fogDevice.getyCoordinate() - anUpperDevice.getyCoordinate(), 2.00));
	}

	private FogDevice getFogDeviceById(int id){
		for(FogDevice fogDevice : getFogDevices()){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}
	
	private void connectWithLatencies(){
		for(FogDevice fogDevice : getFogDevices()){
			FogDevice parent = getFogDeviceById(fogDevice.getParentId());
			if(parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}
	
	@Override
	public void startEntity() {
		if (Paras.mobility) {
			scheduleMobility();

			for (int i = 0; i < Paras.fogScheduleTime.size(); i++){
				scheduleAddNewMobFog(Paras.fogScheduleTime.get(i));
			}

			for (int i = 0; i < Paras.patientScheduleTime.size(); i++){
				scheduleAddNewMobIoT(Paras.patientScheduleTime.get(i));
			}

			scheduleCalcNumTasks();
		}

		for(String appId : applications.keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);//todo
		
		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);
		
		for(FogDevice dev : getFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);//todo

	}

	private void scheduleAddNewMobFog(Pair<Double, Double> scheduleTimePair) {
		Double timeToAdd = scheduleTimePair.getFirst() * Paras.simTime;
		Double depTime = scheduleTimePair.getSecond() * Paras.simTime + timeToAdd;
//		double timeToAdd= Paras.newPatientInterInterval;
		send(getId(), timeToAdd, FogEvents.ADD_NEW_MOBILE_FOG, depTime);
	}

	private void scheduleAddNewMobIoT(Pair<Double, Double> scheduleTimePair) {
		Double timeToAdd = scheduleTimePair.getFirst() * Paras.simTime;
		Double depTime = scheduleTimePair.getSecond() * Paras.simTime + timeToAdd;
//		double timeToAdd= Paras.newPatientInterInterval;
		send(getId(), timeToAdd, FogEvents.ADD_NEW_MOBILE_IOT, depTime);
	}

	private void scheduleCalcNumTasks() {
		double timeToAdd= Paras.calcNumTasksInterval;
		send(getId(), timeToAdd, FogEvents.CALC_NUMBER_OF_TASKS, null);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.CHANGE_MOBFOGS_LOCATION:
			manageFogMobility();
			break;
		case FogEvents.CHANGE_MOBIOTS_LOCATION:
			managePatientMobility();
			break;
		case FogEvents.ADD_NEW_MOBILE_FOG:
			addNewMobileFog((Double) ev.getData());
			break;
		case FogEvents.ADD_NEW_MOBILE_IOT:
			addNewMobilePatient((Double) ev.getData());
			break;
		case FogEvents.CALC_NUMBER_OF_TASKS:
			calc_number_of_tasks();
			break;
		case FogEvents.STOP_SIMULATION: //todo result
			CloudSim.stopSimulation();
//			printShowConfig();
			printTimeDetails();
			printPowerDetails();
			printCostDetails();

			printNetworkUsageDetails();
			printAnalysis();
			CloudSim.runFinished = true;
//			analysisFile();
//			System.exit(0);
			break;
		}
	}

	private void printShowConfig() {


		System.out.println("\n **** config of this result ***** \n");
		System.out.println(" ## 1 topology details ##");
		System.out.println("# of rooms : "+ Paras.numOfRooms);
		System.out.println("# of patients per room : "+ Paras.numOfPatients);
		System.out.println("# of light sensors: "+ Paras.numOfLightSensorPerPatient);
		System.out.println("# of heavy sensors: "+ Paras.numOfHeavySensorPerPatient);

		System.out.println(" ## 2 MIPS of devices ##");
		System.out.println("Cloud : "+ Paras.cloudMIPS);
		System.out.println("Layer2: "+ Paras.fog2MIPS);
		System.out.println("Layer2: "+ Paras.fog1MIPS);

		System.out.println(" ## 3 MI of tasks ##");
		System.out.println("light : "+ Paras.lightReqMI);
		System.out.println("heavy : "+ Paras.heavyReqMI);

		System.out.println(" ### 4 other details ###");
		System.out.println("deadline : "+ Paras.deadline);
		System.out.println("battery threshold : "+ Paras.batteryThreshold);
		System.out.println("RSSI threshold : "+ Paras.RSSIthreshold);

		System.out.println(" #################");
	}

	private void printAnalysis() {
		System.out.println("\n*** OFFLOADING DETAILS *** ");
		System.out.println("# OF LIGHTS OFFLOADED");
		analysisStruct.numberOfLightOffloading.forEach((k,v)->System.out.println(k + " : "+v));
		System.out.println("# OF HEAVIES OFFLOADED");
		analysisStruct.numberOfHeavyOffloading.forEach((k,v)->System.out.println(k + " : "+v));

		System.out.println("\n ========= END =========== \n \n \n \n");
	}

	private void addNewMobileFog(Double depTime) {
		int areaNum = 0;
		FogDevice mainFog, mobileFog = createMobDevice("MobileFog-"+Example1.numMob, Paras.fog1MIPS, 4000, Paras.bwBetFogMB * Paras.MB, Paras.bwBetFogMB * Paras.MB, 1, 0.0, Paras.powerL1Active, Paras.powerL1Idle);
		mainFog = (FogDevice)CloudSim.getEntity("MainFog" + "-A" +areaNum);
		fogDevices.add(mobileFog);
		mobileFog.setMobile();

//        mobileFog.setParentId(mainFog.getId());
		mobileFog.setUplinkLatency(0);
		mobileFog.setPeerHeadId(mainFog.getId());
		mainFog.getListOfPeers().add(mobileFog.getId());
		mobileFog.setLocationXY(new Pair<Double, Double>(12.0, 7.5));
		mobileFog.relatedFile = new File(Paras.pathOfRun+"\\run"+Paras.runNum+"__mob"+Example1.numMob+".txt");

		try {
			mobileFog.relatedFile.createNewFile();
			mobileFog.bufferWriter = new BufferedWriter(new FileWriter(mobileFog.relatedFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Example1.incMobs();


//		fogLayer1s.add(mobileFog);
//		if(Paras.debug)
			System.out.println("time: "+CloudSim.clock()+" - Fog-device added: " + mobileFog.getName());

		TableEntry entry = new TableEntry(mobileFog.getId(), mobileFog.getLocationXY(), Paras.fog1MIPS, true, Paras.bwBetFogMB * Paras.MB,0.0, 0.0, 0.0, 0.0, Paras.initialPowHour*Paras.HtoS, 0.0, 0, 0, 0);
		entry.depTime = depTime;
		mainFog.offloadTable.put(mobileFog.getName(), entry);
		mainFog.getInterfaces().put(mobileFog.getId(), new Interface(Paras.bwBetFogMB * Paras.MB));

//		scheduleAddNewMobFog();

		sendNow(mobileFog.getId(), FogEvents.RESOURCE_MGMT);
        sendNow(mobileFog.getId(), FogEvents.ACTIVE_APP_UPDATE, applications.values().iterator().next());
        sendNow(mobileFog.getId(), FogEvents.APP_SUBMIT, applications.values().iterator().next());
        sendNow(mobileFog.getId(), FogEvents.LAUNCH_MODULE, applications.values().iterator().next().getModuleByName("lightProcess"));
        sendNow(mobileFog.getId(), FogEvents.LAUNCH_MODULE, applications.values().iterator().next().getModuleByName("heavyProcess"));
	}

	private void calc_number_of_tasks() {
		calcPowerDetails();
		scheduleCalcNumTasks();
	}

	private void addNewMobilePatient(Double depTime) {
//		int areaNum = Paras.randomGenerator.nextInt(Paras.numOfRooms);
		int areaNum = 0;

		Patient patient = new Patient(areaNum);
		patient.name = "patient-A" + areaNum + "-mob-" + Paras.patientInArea.get(areaNum);
		patient.setLocationXY(Paras.roomLocs.get(4));
		patient.depTime = depTime;
		int numPatient = Paras.patientInArea.get(areaNum);
		int k = numPatient;
		Paras.patientInArea.set(areaNum, k+1);
		for (int i = 0; i < Paras.numOfLightSensorPerPatient; i++) {
			String mobileId = "LightSensor-" + k + "-mob-" + i + "-A" +areaNum;
			FogDevice Sensor = addSensor(mobileId,"lightRAW", 2, "Example1", ((FogDevice)CloudSim.getEntity("MainFog" + "-A" +areaNum)).getId(), Paras.lightMIPS, 1000, Paras.bwFromLightKB * Paras.KB, Paras.bwFromLightKB * Paras.KB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
//			if(Paras.IoTinRooms.size()<=k)
//				Paras.IoTinRooms.add(k,0);
			Sensor.setUplinkLatency(0); // latency of connection between camera and router is 2 ms

			fogDevices.add(Sensor);
			patient.addFogDevice(Sensor);
			Sensor.setPatient(patient);

//			sendNow(Sensor.getId(), FogEvents.RESOURCE_MGMT);
//			sendNow(Sensor.getId(), FogEvents.ACTIVE_APP_UPDATE, applications.values().iterator().next());
//			sendNow(Sensor.getId(), FogEvents.APP_SUBMIT, applications.values().iterator().next());

//			Paras.IoTinRooms.set(k,Paras.IoTinRooms.get(k)+1);
		}
		for (int i = 0; i < Paras.numOfHeavySensorPerPatient; i++) {
			String mobileId = "HeavySensor-" + k + "-mob-" + i+ "-A" +areaNum;
			FogDevice Sensor = addSensor(mobileId,"heavyRAW", 2, "Example1", ((FogDevice)CloudSim.getEntity("MainFog" + "-A" +areaNum)).getId(), Paras.heavyMIPS, 1000, Paras.bwFromHeavyMB * Paras.MB, Paras.bwFromHeavyMB * Paras.MB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
			Sensor.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
			fogDevices.add(Sensor);
			patient.addFogDevice(Sensor);
			Sensor.setPatient(patient);
		}
		String mobileId = "Actuator-" + k+ "-mob-A" +areaNum;
		FogDevice Actuator = addActuator(mobileId, "ACTUATOR", 2, "Example1", ((FogDevice)CloudSim.getEntity("MainFog" + "-A" +areaNum)).getId(), 10, 100, 10, Paras.bwFromHeavyMB * Paras.MB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
		Actuator.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
		fogDevices.add(Actuator);
		patient.addFogDevice(Actuator);
		Actuator.setPatient(patient);


//		String mobileId = "LightSensor-" + areaNum + "-" + Paras.IoTinRooms.get(area);
//		Paras.IoTinRooms.set(areaNum,Paras.IoTinRooms.get(area)+1);
//
//		FogDevice Sensor = addSensor(mobileId,"lightRAW", 2, "Example1", ((FogDevice)CloudSim.getEntity("MainFog")).getId(), Paras.lightMIPS, 1000, Paras.bwFromLightKB * Paras.KB, Paras.bwFromLightKB * Paras.KB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
//		Sensor.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
//		Sensor.setLocationXY(Paras.roomLocs.get(room));
//		fogDevices.add(Sensor);
//		IoTMobs.add(Sensor);
//
//		((FogDevice)CloudSim.getEntity("MainFog")).getChildrenIds().add(Sensor.getId());

		if(Paras.IoTlocDebug) {
			patient.relatedFile = new File(Paras.pathOfRun+"\\run"+Paras.runNum+"-"+k+"__mob_patient-A"+ areaNum + "-" + ".txt");

			try {
				patient.relatedFile.createNewFile();
				patient.bufferWriter = new BufferedWriter(new FileWriter(patient.relatedFile, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		patientMobs.add(patient);
//		System.out.println("time: "+CloudSim.clock()+" - IoT-device added: " + Sensor.getName());
		System.out.println("time: "+CloudSim.clock()+" - patient added: " + patient.name);

//		if (CloudSim.clock() < 1100)
//			scheduleAddNewMobIoT();


//		sendNow(mobileFog.getId(), FogEvents.LAUNCH_MODULE, applications.values().iterator().next().getModuleByName("lightProcess"));
//		sendNow(mobileFog.getId(), FogEvents.LAUNCH_MODULE, applications.values().iterator().next().getModuleByName("heavyProcess"));
	}

	private FogDevice addSensor(String id, String tupleType, int userId, String appId, int parentId, long mips, int ram, long dwBw, long upBw) {
		FogDevice Embedded = createMobDevice("embSen-" + id, mips, ram, dwBw, upBw, 3, 0, 5, 4);
		Embedded.setParentId(parentId);
		Sensor sensor = new Sensor("sen-" + id, tupleType, userId, appId, new DeterministicDistribution(Paras.lightEmitInterval)); // inter-transmission time of camera (sensor) follows a deterministic distribution
		sensors.add(sensor);
		sensor.setApp(applications.values().iterator().next());
		sensor.setGatewayDeviceId(Embedded.getId());
		sensor.setLatency(0.0);  // latency of connection between camera (sensor) and the parent Smart Camera is 1 ms
		return Embedded;
	}

	private FogDevice addActuator(String id, String actuatorType, int userId, String appId, int parentId, long mips, int ram, long dwBw, long upBw) {
		FogDevice Embedded = createMobDevice("embAct-" + id, mips, ram, dwBw, upBw, 3, 0, 5, 4);
		Embedded.setParentId(parentId);
		Actuator alert = new Actuator("alert-"+id, userId, appId, actuatorType);
		actuators.add(alert);
		alert.setGatewayDeviceId(Embedded.getId());
		alert.setLatency(0.0);  // latency of connection between PTZ Control and the parent Smart Camera is 1 msreturn Embeded;
		return Embedded;
	}

	private static FogDevice createMobDevice(String nodeName, long mips,
											 int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),//todo
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
		);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource//todo
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
		// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics,
					new AppModuleAllocationPolicy(hostList), storageList, 0.1, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogdevice.setLevel(level);
		return fogdevice;
	}

	private void analysisFile() {
		//todo:analysis
		File out;
		BufferedWriter outTofile = null;
		out = new File("analysis.txt");
		DecimalFormat df = new DecimalFormat("0.00");
		try {
			outTofile = new BufferedWriter(new FileWriter(out));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int iii = 0;
		for(analysisStruct i:CloudSim.listOfAnalysis){
//			outTofile.write(iii++ + ".  "  + i.getType() + " time:" + i.getTime() + " place:" + i.getRouterName() + " file:" + i.getFile() + " packet:" + i.getPacket() + "\r\n");
			try {
				if (i.tag == 55||i.tag == 68||i.tag == 20)
					continue;
				outTofile.write( "tag :"+ i.tag + "  time :"+ df.format(i.time)+"  rTime :"+ df.format(i.runTime)+"  src :"+ i.src+"  dst :"+ i.dst+"  delay :"+ df.format(i.delay) + " tuple :" + i.tupleType +"\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			outTofile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printNetworkUsageDetails() {
		System.out.println("Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);
	}

	private FogDevice getCloud(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().equals("Cloud") || dev.getName().equals("cloud"))
				return dev;
		return null;
	}
	
	private void printCostDetails(){
		System.out.println("Cost of execution in cloud = "+getCloud().getTotalCost());
	}
	
	private void printPowerDetails() {
		for(FogDevice fogDevice : getFogDevices()){
			if (fogDevice.getName().contains("emb"))
				continue;
			System.out.println(fogDevice.getName() + " : Energy Consumed = "+fogDevice.getEnergyConsumption());
		}
	}

	private void calcPowerDetails() {
		calcTaskAnalysis();
		for(FogDevice fogDevice : getFogDevices()){
			String fogName = fogDevice.getName();
			Double preEngConsTemp = 0.0;
			if (fogName.contains("emb"))
				continue;
			if(preEngCons.containsKey(fogName)){
				preEngConsTemp = preEngCons.get(fogName);
			}
			Double accEngCons = fogDevice.getEnergyConsumption();
			Double curEngCons = accEngCons - preEngConsTemp;
			preEngCons.put(fogName, accEngCons);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paras.pathOfRun+"\\run"+Paras.runNum+"-"+Paras.ENERGY_CONSUMPTION_TXT, true))) {
				writer.write(fogDevice.getName() + ": "+curEngCons);
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void calcTaskAnalysis() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paras.pathOfRun+"\\run"+Paras.runNum+"-"+Paras.TASK_NUMBER_TXT, true))) {
			writer.write("time: "+CloudSim.clock());
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		writer.write(fogDevice.getName() + ": "+curEngCons);
		analysisStruct.numberOfLightOffloading.forEach((k,v)-> {
			Integer preTasksTemp = 0;
			if(preLightTasks.containsKey(k)){
				preTasksTemp = preLightTasks.get(k);
			}
			Integer curTasksNum = v - preTasksTemp;
			preLightTasks.put(k, v);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paras.pathOfRun+"\\run"+Paras.runNum+"-"+Paras.TASK_NUMBER_TXT, true))) {
				writer.write(k + "_Light: "+curTasksNum);
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		analysisStruct.numberOfHeavyOffloading.forEach((k,v)-> {
			Integer preTasksTemp = 0;
			if(preHeavyTasks.containsKey(k)){
				preTasksTemp = preHeavyTasks.get(k);
			}
			Integer curTasksNum = v - preTasksTemp;
			preHeavyTasks.put(k, v);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paras.pathOfRun+"\\run"+Paras.runNum+"-"+Paras.TASK_NUMBER_TXT, true))) {
				writer.write(k + "_Heavy: "+curTasksNum);
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	private String getStringForLoopId(int loopId){
		for(String appId : getApplications().keySet()){
			Application app = getApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}
	private void printTimeDetails() {
		System.out.println("=========================================");
		System.out.println("============== RESULTS ==================");
		System.out.println("==============  " + Paras.resTypeStr + "   ==================");

		System.out.println("=========================================");
		System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
		System.out.println("=========================================");
		System.out.println("APPLICATION LOOP DELAYS");
		System.out.println("=========================================");
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
			/*double average = 0, count = 0;
			for(int tupleId : TimeKeeper.getInstance().getLoopIdToTupleIds().get(loopId)){
				Double startTime = 	TimeKeeper.getInstance().getEmitTimes().get(tupleId);
				Double endTime = 	TimeKeeper.getInstance().getEndTimes().get(tupleId);
				if(startTime == null || endTime == null)
					break;
				average += endTime-startTime;
				count += 1;
			}
			System.out.println(getStringForLoopId(loopId) + " ---> "+(average/count));*/
			System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
		}
		System.out.println("=========================================");
		System.out.println("TUPLE CPU EXECUTION DELAY");
		System.out.println("=========================================");
		
		for(String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()){
			System.out.println(tupleType + " ---> "+TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}
		
		System.out.println("=========================================");
	}

	protected void manageResources(){
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
	}
	
	@Override
	public void shutdownEntity() {	
	}
	
	public void submitApplication(Application application, int delay, ModulePlacement modulePlacement){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
		getAppModulePlacementPolicy().put(application.getAppId(), modulePlacement);
		
		for(Sensor sensor : sensors){
			sensor.setApp(getApplications().get(sensor.getAppId())); //todo javab: bug : baghti ye sensor jadid ezafe mikonim ba hich eventi nemishe in ghetee kodo (set kardan app) run kard ke yanni bug :|
		}
		for(Actuator ac : actuators){
			ac.setApp(getApplications().get(ac.getAppId()));
		}
		
		for(AppEdge edge : application.getEdges()){
			if(edge.getEdgeType() == AppEdge.ACTUATOR){
				String moduleName = edge.getSource();
				for(Actuator actuator : getActuators()){
					if(actuator.getActuatorType().equalsIgnoreCase(edge.getDestination())) //todo:
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(), edge.getTupleType());
				}
			}
		}	
	}
// mobility
//	private static Map<Integer, Pair<Double, Integer>> mobilityMap;
//	public void setMobilityMap(Map<Integer, Pair<Double, Integer>> mobilityMap) {
//		Pair<Double, Integer> element = new Pair<Double, Integer>(1000.0, 9);
//		mobilityMap.put(7,element);
//		this.mobilityMap = mobilityMap;
//	}
	private void scheduleMobility(){
//		double timeToChange = Paras.fogMobilityTimeInterval;
		send(getId(), Paras.fogMobilityTimeInterval, FogEvents.CHANGE_MOBFOGS_LOCATION, null);
		send(getId(), Paras.patientMobilityTimeInterval, FogEvents.CHANGE_MOBIOTS_LOCATION, null);
	}
	private void manageFogMobility() {
		ArrayList <String> removeFogNames = new ArrayList<String>();
		for (int i=0; i < Paras.numOfAreas; i++){
			for (Map.Entry<String, TableEntry> entry : ((FogDevice) CloudSim.getEntity("MainFog" + "-A" +i)).offloadTable.entrySet()){
				if (entry.getValue().isMobility()){
					entry.getValue().setLocationXY(changeTheLocation( ((FogDevice) CloudSim.getEntity(entry.getKey()))));
					if( entry.getValue().updateStatus() == -1){
						CloudSim.removeEntityByName(entry.getKey());
						((FogDevice) CloudSim.getEntity("MainFog" + "-A" +i)).doOffloadingForRemTasks(entry.getKey());
						removeFogNames.add(entry.getKey());
						((FogDevice) CloudSim.getEntity("MainFog" + "-A" +i)).getListOfPeers().
								remove((Integer)entry.getValue().getId());
//					Example1.decMobs();
//					if(Paras.debug)
						System.out.println("time: "+CloudSim.clock()+" - device removed: " + entry.getKey());
					}
				}
			}
			((FogDevice) CloudSim.getEntity("MainFog" + "-A" +i)).offloadTable.keySet().removeAll(removeFogNames);
		}

		double timeToChange = Paras.fogMobilityTimeInterval;
		send(getId(), timeToChange, FogEvents.CHANGE_MOBFOGS_LOCATION, null);
	}

	private void managePatientMobility() {
		ArrayList<Patient> toRemove = new ArrayList<>();
		for (int j = 0; j < patientMobs.size(); j++){
			Patient patient = patientMobs.get(j);
			changeTheLocation(patient);

			if(patient.updateIoTStatus() == -1){
				List<FogDevice> patientIoTs = patient.fogDevices;
				for (int i = 0; i<patientIoTs.size(); i++) {
					((FogDevice) CloudSim.getEntity(patientIoTs.get(i).getParentId())).getChildrenIds().removeAll(Arrays.asList(patientIoTs.get(i).getId()));
					CloudSim.removeEntityByName(patientIoTs.get(i).getName());
				}
				toRemove.add(patient);
				System.out.println("time: " + CloudSim.clock() + " - patient removed: " + patient.name);

			}
			patientMobs.removeAll(toRemove);
		}

		double timeToChange = Paras.patientMobilityTimeInterval;
		send(getId(), timeToChange, FogEvents.CHANGE_MOBIOTS_LOCATION, null);
	}


	private Pair<Double, Double> changeTheLocation(FogDevice fog) {
		Pair<Double, Double> curLoc, nextLoc, direction;
		curLoc = fog.getLocationXY();
		direction = Paras.directions.get(Paras.randomGenerator.nextInt(Paras.directions.size()));
		direction = new Pair<Double,Double>(0.0, 0.0);
		nextLoc = new Pair<Double,Double>(curLoc.getFirst() + direction.getFirst(), curLoc.getSecond() + direction.getSecond());
		fog.setLocationXY(nextLoc);
		return nextLoc;
	}

	private Pair<Double, Double> changeTheLocation(Patient patient) {
		Pair<Double, Double> curLoc, nextLoc, direction;
		curLoc = patient.getLocationXY();
		direction = Paras.directions.get(Paras.randomGenerator.nextInt(Paras.directions.size()));
		direction = new Pair<Double,Double>(0.0, 0.0);
		nextLoc = new Pair<Double,Double>(curLoc.getFirst() + direction.getFirst(), curLoc.getSecond() + direction.getSecond());
		patient.setLocationXY(nextLoc);
		return nextLoc;
	}

	public void submitApplication(Application application, ModulePlacement modulePlacement){
		submitApplication(application, 0, modulePlacement);
	}
	
	
	private void processAppSubmit(SimEvent ev){
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}
	
	private void processAppSubmit(Application application){
		System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		
		ModulePlacement modulePlacement = getAppModulePlacementPolicy().get(application.getAppId());
		for(FogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);

		}
		
		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		for(Integer deviceId : deviceToModuleMap.keySet()){
			for(AppModule module : deviceToModuleMap.get(deviceId)){
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}


	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		for(Sensor sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}

	public Map<String, ModulePlacement> getAppModulePlacementPolicy() {
		return appModulePlacementPolicy;
	}

	public void setAppModulePlacementPolicy(Map<String, ModulePlacement> appModulePlacementPolicy) {
		this.appModulePlacementPolicy = appModulePlacementPolicy;
	}
}