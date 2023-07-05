package org.fog.test.perfeval;

import java.io.*;
import java.lang.reflect.Parameter;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import javax.print.attribute.standard.PageRanges;

import static jdk.xml.internal.JdkXmlUtils.getValue;

/**
 * Simulation setup for case study 2 - Intelligent Surveillance
 * @author Harshit Gupta
 *
 */
public class Example1 {

    static List<FogDevice> fogDevices;
    static List<FogDevice> fogLayer2s;
    static List<FogDevice> fogLayer1s;
    static List<FogDevice> IoTMobs;

    static Hashtable<Integer, List<FogDevice>> peerGateWays;
    static List<Sensor> sensors;
    static List<Actuator> actuators;



    private static HashMap<String, Integer> getIdByName = new HashMap<String, Integer>();

    static Map<Integer, Pair<Double, Integer>> mobilityMap = new HashMap<Integer, Pair<Double, Integer>>();
    static String mobilityDestination = "FogDevice-0";
    public static int numMob;
    static public void incMobs(){
        numMob++;
    }
    static public void decMobs(){
        numMob--;
    }

    public static int countER = 0;
    public static int dl = 0;
    static public void inc(){
        countER++;
    }



    public static void main(String[] args) {

        Log.printLine("Starting DCNS...");


        try{
            File directory=new File("C:\\Drivers\\Learning\\Research\\FogComputing\\Simulation\\iFogSim\\Res_Ex");
            int fileCount=directory.list().length;
            Paras.pathOfRun = "D:\\Learn\\Academic\\LabWorks\\FogComputing\\Simulation\\iFogSim\\Res_Ex\\Config "+fileCount;
            Paras.pathOfRun = "C:\\Drivers\\Learning\\Research\\FogComputing\\Simulation\\iFogSim\\Res_Ex\\Config "+fileCount;
            File f = new File(Paras.pathOfRun);
            f.mkdir();
            String fileName = "res_run";
            PrintStream out = new PrintStream(new FileOutputStream("D:\\Learn\\Academic\\LabWorks\\FogComputing\\Simulation\\iFogSim\\Res_Ex\\Config "+fileCount+"\\"+fileName+".txt", true));
            System.setOut(out);
        }catch(Exception e){System.out.println(e);}


        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events


            printShowConfig();

            for (int i = 0; i < 4; i++){
                resetSim();
                Paras.randomGenerator = new Random(Paras.seed);
                CloudSim.init(num_user, calendar, trace_flag);

                String appId = "Example1"; // identifier of the application
                FogBroker broker = new FogBroker("broker");

                Application application = createApplication(appId, broker.getId());
                application.setUserId(broker.getId());

                createFogDevices(broker.getId(), appId);

                Controller controller = null; //todo
                Paras.resType = i;
//                Paras.resType = 0;
                switch (Paras.resType){
                    case Paras.CLOUDFOG:
                        Paras.CLOUD = false;
                        Paras.offloading = true;
                        Paras.resTypeStr = "CLOUD_FOG";
                        break;
                    case Paras.ClOUDONLY:
                        Paras.CLOUD = true;
                        Paras.offloading = false;
                        Paras.resTypeStr = "ClOUDONLY";
                        break;
                    case Paras.FOGONLY:
                        Paras.CLOUD = false;
                        Paras.offloading = true;
                        Paras.resTypeStr = "FOGONLY";
                        break;
                    case Paras.LFHC:
                        Paras.CLOUD = false;
                        Paras.offloading = true;
                        Paras.resTypeStr = "LFHC";
                        break;
                }
                ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

                if (Paras.CLOUD) {
                    moduleMapping.addModuleToDevice("lightProcess", "Cloud");
                    moduleMapping.addModuleToDevice("heavyProcess", "Cloud");
                    // if the mode of deployment is cloud-based
//                moduleMapping.addModuleToDevice("preProcess", "cloud");
//                moduleMapping.addModuleToDevice("fogProcess", "cloud"); // placing all instances of Object Detector module in the Cloud
//                moduleMapping.addModuleToDevice("moreProcess", "cloud"); // placing all instances of Object Tracker module in the Cloud
//                moduleMapping.addModuleToDevice("massAnalysis", "cloud");
                } else{
                    for (FogDevice device : fogDevices) {
                        if (!device.getName().contains("Sensor")) { // names of all Smart Cameras start with 'm'
//                    moduleMapping.addModuleToDevice("motion_detector", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
                            moduleMapping.addModuleToDevice("lightProcess", device.getName());
                            moduleMapping.addModuleToDevice("heavyProcess", device.getName());
                        }
//                moduleMapping.addModuleToDevice("motion_detector", device.getName());
                    }
                }

                controller = new Controller("master-controller", fogDevices, sensors,
                        actuators, fogLayer1s, IoTMobs);
//            if (Paras.mobility)
//			    controller.setMobilityMap(mobilityMap);
                controller.submitApplication(application,
                        (Paras.CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                                : (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

                TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

                CloudSim.startSimulation();

                CloudSim.stopSimulation();

                Paras.runNum++;

                Log.printLine("VRGame finished!");

            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    private static void resetSim() {

        numMob = 0;
        fogDevices = new ArrayList<FogDevice>();
        fogLayer2s = new ArrayList<FogDevice>();
        fogLayer1s = new ArrayList<FogDevice>();
        IoTMobs = new ArrayList<FogDevice>();
        peerGateWays = new Hashtable<Integer, List<FogDevice>>();
        sensors = new ArrayList<Sensor>();
        actuators = new ArrayList<Actuator>();
        analysisStruct.numberOfLightOffloading = new HashMap<String, Integer>();
        analysisStruct.numberOfHeavyOffloading = new HashMap<String, Integer>();
        NetworkUsageMonitor.resetNU();
        TimeKeeper.resetTK();
    }

    private static void printShowConfig() {

        System.out.println("\n **** config of this result ***** \n");
        System.out.println(" ## 1 topology details ##");
        System.out.println("# of rooms : "+ Paras.numOfRooms);
        System.out.println("# of light sensors: "+ Paras.numOfLightSensorPerRoom);
        System.out.println("# of heavy sensors: "+ Paras.numOfHeavySensorPerRoom);

        System.out.println(" ## 2 MIPS of devices ##");
        System.out.println("Cloud : "+ Paras.cloudMIPS);
        System.out.println("Layer2: "+ Paras.fog2MIPS);
        System.out.println("Layer1: "+ Paras.fog1MIPS);

        System.out.println(" ## 3 MI of tasks ##");
        System.out.println("light : "+ Paras.lightReqMI);
        System.out.println("heavy : "+ Paras.heavyReqMI);

        System.out.println(" ### 4 other details ###");
        System.out.println("deadline : "+ Paras.deadline);
        System.out.println("battery threshold : "+ Paras.batteryThreshold);
        System.out.println("RSSI threshold : "+ Paras.RSSIthreshold);
        System.out.println("emit interval : "+ Paras.emitInterval);

        System.out.println(" ###########################\n");
    }


    private static FogDevice addLowLevelFogDevice(String id, int brokerId, String appId, int parentId) {
        FogDevice lowLevelFogDevice = createFogDevice("LowLevelFog-Device-" + id, 1000, 1000, 10000, 270, 2, 0, 87.53, 82.44);
        lowLevelFogDevice.setParentId(parentId);
        getIdByName.put(lowLevelFogDevice.getName(), lowLevelFogDevice.getId());
        if ((int) (Math.random() * 100) % 2 == 0) {
            Pair<Double, Integer> pair = new Pair<Double, Integer>(100.00, getIdByName.get(mobilityDestination));
            mobilityMap.put(lowLevelFogDevice.getId(), pair);
        }
        Sensor sensor = new Sensor("s-" + id, "Sensor", brokerId, appId, new DeterministicDistribution(5));
        sensors.add(sensor);
        Actuator actuator = new Actuator("a-" + id, brokerId, appId, "OutputData");
        actuators.add(actuator);
        sensor.setGatewayDeviceId(lowLevelFogDevice.getId());
        sensor.setLatency(6.0);
        actuator.setGatewayDeviceId(lowLevelFogDevice.getId());
        actuator.setLatency(1.0);
        return lowLevelFogDevice;
    }

    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createFogDevice("Cloud", Paras.cloudMIPS, 40000, 100, Paras.bwBetLayerGB * 1000000000, 0, 0.01, Paras.powerCloudActive, Paras.powerCloudIdle);
        cloud.setParentId(-1);
        cloud.setLocationXY(new Pair<Double, Double>(0.0, 30002007.0));
        fogDevices.add(cloud);


        FogDevice fogLayer2 = createFogDevice("FogLayer2", Paras.fog2MIPS, 4000, Paras.bwBetLayerGB * 1000000000, Paras.bwBetLayerGB * 1000000000, 1, 0.0, Paras.powerL2Active, Paras.powerL2Idle);
        fogLayer2.setParentId(cloud.getId());
        fogLayer2.setUplinkLatency(0); // latency of connection between proxy server and cloud is 100 ms
        fogLayer2.setLocationXY(new Pair<Double, Double>(0.0, 2007.0));
        fogDevices.add(fogLayer2);
        fogLayer2s.add(fogLayer2);

//        FogDevice Router2 = createFogDevice("Router2", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
//        Router2.setParentId(cloud.getId());
//        Router2.setUplinkLatency(100); // latency of connection between proxy server and cloud is 100 ms
//        fogDevices.add(Router2);
//        routers.add(Router2);

        for (int i = 0; i < fogLayer2s.size(); i++){
            addArea(i + "", userId, appId, fogLayer2s.get(i).getId());
        }
    }


    private static void addArea(String id, int userId, String appId, int parentId) {
        FogDevice mainFog = createFogDevice("MainFog", Paras.fog1MIPS, 4000, Paras.bwBetLayerGB * 1000000000, Paras.bwToActKB * Paras.KB, 1, 0.0, Paras.powerL1Active, Paras.powerL1Idle);
        fogDevices.add(mainFog);
        mainFog.setParentId(parentId);
        mainFog.setUplinkLatency(0);
//        mainFog.setPeerHeadId(mainFog.getId());
        mainFog.setLocationXY(new Pair<Double, Double>(7.5, 7.5));
        fogLayer1s.add(mainFog);

        mainFog.offloadTable = new HashMap<String, TableEntry>();

        TableEntry entry = new TableEntry(mainFog.getId(), mainFog.getLocationXY(), Paras.fog1MIPS, false, -1.0, 0.0, 0.0, 0.0, 0.0, 3600, 0.0, 0, 0, 0);
        mainFog.offloadTable.put(mainFog.getName(), entry);
//        peerGateWays.put(parentId, )

        FogDevice staticFog = createFogDevice("StaticFog", Paras.fog1MIPS, 4000, Paras.bwBetFogMB * Paras.MB, Paras.bwBetFogMB * 100000, 1, 0.0, Paras.powerL1Active, Paras.powerL1Idle);
        fogDevices.add(staticFog);
//        staticFog.setParentId(mainFog.getId());
        staticFog.setUplinkLatency(0);
        staticFog.setPeerHeadId(mainFog.getId());
        mainFog.getListOfPeers().add(staticFog.getId());
        staticFog.setLocationXY(new Pair<Double, Double>(2.5, 7.5));
        fogLayer1s.add(staticFog);

        entry = new TableEntry(staticFog.getId(), staticFog.getLocationXY(), Paras.fog1MIPS, false,Paras.bwBetFogMB * Paras.MB, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0);
        mainFog.offloadTable.put(staticFog.getName(), entry);
        mainFog.getInterfaces().put(staticFog.getId(), new Interface(Paras.bwBetFogMB * Paras.MB));

        FogDevice mobileFog = createFogDevice("MobileFog-"+numMob, Paras.fog1MIPS, 4000, Paras.bwBetFogMB * Paras.MB, Paras.bwBetFogMB * Paras.MB, 1, 0.0, Paras.powerL1Active, Paras.powerL1Idle);
        fogDevices.add(mobileFog);
        mobileFog.setMobile();
//        mobileFog.setParentId(mainFog.getId());
        mobileFog.setUplinkLatency(0);
        mobileFog.setPeerHeadId(mainFog.getId());
        mainFog.getListOfPeers().add(mobileFog.getId());
        mobileFog.setLocationXY(new Pair<Double, Double>(12.0, 7.5));
        fogLayer1s.add(mobileFog);
        mobileFog.relatedFile = new File(Paras.pathOfRun+"\\run"+Paras.runNum+"__mob"+Example1.numMob+".txt");
        try {
            mobileFog.bufferWriter = new BufferedWriter(new FileWriter(mobileFog.relatedFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        incMobs();

        entry = new TableEntry(mobileFog.getId(), mobileFog.getLocationXY(), Paras.fog1MIPS, true, Paras.bwBetFogMB * Paras.MB,0.0, 0.0, 0.0, 0.0, Paras.initialPowHour*Paras.HtoS, 0.0, 0, 0, 0);
        mainFog.offloadTable.put(mobileFog.getName(), entry);
        mainFog.getInterfaces().put(mobileFog.getId(), new Interface(Paras.bwBetFogMB * Paras.MB));

        ArrayList<FogDevice> listForPeer = new ArrayList<>();

        for (int k = 0; k < Paras.numOfRooms; k++){
            for (int i = 0, j = 0; i < Paras.numOfLightSensorPerRoom; i++, j++) {
                String mobileId = "LightSensor-" + k + "-" + i;
                FogDevice Sensor = addSensor(mobileId,"lightRAW", userId, appId, mainFog.getId(), Paras.lightMIPS, 1000, Paras.bwFromLightKB * Paras.KB, Paras.bwFromLightKB * Paras.KB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
                if(Paras.IoTinRooms.size()<=k)
                    Paras.IoTinRooms.add(k,0);
                Sensor.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
                Sensor.setLocationXY(Paras.roomLocs.get(k));
                fogDevices.add(Sensor);
                IoTMobs.add(Sensor);
                if(Paras.IoTlocDebug) {
                    Sensor.relatedFile = new File(Paras.pathOfRun+"\\run"+Paras.runNum+"__mob_IOT_"+ k + "-" + Paras.IoTinRooms.get(k)+".txt");

                    try {
                        Sensor.bufferWriter = new BufferedWriter(new FileWriter(Sensor.relatedFile, true));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Paras.IoTinRooms.set(k,Paras.IoTinRooms.get(k)+1);
//            if (j < 2){
//                listForPeer.add(Sensor);
//            }
            }
            for (int i = 0, j = 0; i < Paras.numOfHeavySensorPerRoom; i++, j++) {
                String mobileId = "HeavySensor-" + k + "-" + i;
                FogDevice Sensor = addSensor(mobileId,"heavyRAW", userId, appId, mainFog.getId(), Paras.heavyMIPS, 1000, Paras.bwFromHeavyMB * Paras.MB, Paras.bwFromHeavyMB * Paras.MB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
                Sensor.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
                Sensor.setLocationXY(Paras.roomLocs.get(k));
                fogDevices.add(Sensor);

//            if (j < 2){
//                listForPeer.add(Sensor);
//            }
            }
            String mobileId = "Actuator-" + k;
            FogDevice Actuator = addActuator(mobileId, "ACTUATOR", userId, appId, mainFog.getId(), 10, 100, 10, Paras.bwFromHeavyMB * Paras.MB); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
            Actuator.setUplinkLatency(0); // latency of connection between camera and router is 2 ms
            Actuator.setLocationXY(Paras.roomLocs.get(k));
            fogDevices.add(Actuator);
        }



        entry = new TableEntry(mainFog.getParentId(), new Pair<Double, Double>(7.5, 2007.5), Paras.fog2MIPS, false,Paras.bwBetLayerGB * Paras.GB, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0);
        mainFog.offloadTable.put("FogLayer2", entry);

        entry = new TableEntry(CloudSim.getEntityId("Cloud"), CloudSim.getEntity("Cloud").getLocationXY(), Paras.cloudMIPS, false, Paras.bwBetLayerGB * Paras.GB,0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0);
        mainFog.offloadTable.put("Cloud", entry);

//        makePeerSensor(listForPeer);
//        addActuator(id, userId, appId, gateWay1.getId());

        return ;
    }

//    private static void makePeerSensor(ArrayList<FogDevice> listForPeer) {
////        for (int i = 0; i < listForPeer.size(); i++){
////            for (int j = i + 1; j < listForPeer.size(); j++){
////                listForPeer.get(i).listOfPeers.add(listForPeer.get(j).getId());
////            }
////        }
//        List<Integer> temp = new ArrayList<>();
//        for (int i = 0; i < listForPeer.size(); i++)
//            temp.add(listForPeer.get(i).getId());
//        for (int i = 0; i < listForPeer.size(); i++)
//            listForPeer.get(i).listOfPeers = temp;
//    }

    private static FogDevice addSensor(String id, String tupleType, int userId, String appId, int parentId, long mips, int ram, long dwBw, long upBw) {
        FogDevice Embedded = createFogDevice("embSen-" + id, mips, ram, dwBw, upBw, 3, 0, 5, 4);
        Embedded.setParentId(parentId);
        Sensor sensor = new Sensor("sen-" + id, tupleType, userId, appId, new DeterministicDistribution(Paras.emitInterval)); // inter-transmission time of camera (sensor) follows a deterministic distribution
        sensors.add(sensor);
        sensor.setGatewayDeviceId(Embedded.getId());
        sensor.setLatency(0.0);  // latency of connection between camera (sensor) and the parent Smart Camera is 1 ms
        return Embedded;
    }

    private static FogDevice addActuator(String id, String actuatorType, int userId, String appId, int parentId, long mips, int ram, long dwBw, long upBw) {
        FogDevice Embedded = createFogDevice("embAct-" + id, mips, ram, dwBw, upBw, 3, 0, 5, 4);
        Embedded.setParentId(parentId);
        Actuator alert = new Actuator("alert-"+id, userId, appId, actuatorType);
        actuators.add(alert);
        alert.setGatewayDeviceId(Embedded.getId());
        alert.setLatency(0.0);  // latency of connection between PTZ Control and the parent Smart Camera is 1 msreturn Embeded;
        return Embedded;
    }


    /**
     * Creates a vanilla fog device
     *
     * @param nodeName    name of the device to be used in simulation
     * @param mips        MIPS
     * @param ram         RAM
     * @param upBw        uplink bandwidth
     * @param downBw      downlink bandwidth
     * @param level       hierarchy level of the device
     * @param ratePerMips cost rate per MIPS used
     * @param busyPower
     * @param idlePower
     * @return
     */
    private static FogDevice createFogDevice(String nodeName, long mips,
                                             int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating
        if (nodeName == "Cloud"){
            for (int i=1; i<5; i++){
                peList.add(new Pe(i, new PeProvisionerOverbooking(mips)));
            }
        }
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

    /**
     * Function to create the Intelligent Surveillance application in the DDF model.
     *
     * @param appId  unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {

        Application application = Application.createApplication(appId, userId);
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
//        application.addAppModule("object_detector", 10);
//        application.addAppModule("motion_detector", 10);
//        application.addAppModule("object_tracker", 10);
//        application.addAppModule("user_interface", 10);
//        application.addAppModule("preProcess", 10);
//        application.addAppModule("fogProcess", 10);
//        application.addAppModule("moreProcess", 10);
//        application.addAppModule("massAnalysis", 10);

        application.addAppModule("lightProcess", 10);
        application.addAppModule("heavyProcess", 50);
        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */   //todo
//        application.addAppEdge("CAMERA", "motion_detector", 1000, 20000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
//        application.addAppEdge("motion_detector", "object_detector", 2000, 2000, "MOTION_VIDEO_STREAM", Tuple.UP, AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
//        application.addAppEdge("object_detector", "user_interface", 500, 2000, "DETECTED_OBJECT", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
//        application.addAppEdge("object_detector", "object_tracker", 1000, 100, "OBJECT_LOCATION", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
//        application.addAppEdge("object_tracker", "PTZ_CONTROL", 100, 28, 100, "PTZ_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR); // adding edge from Object Tracker to PTZ CONTROL (actuator) carrying tuples of type PTZ_PARAMS

//        application.addAppEdge("SENSOR", "preProcess", 1000, 20000, "RAW", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
//        application.addAppEdge("preProcess", "fogProcess", 2000, 2000, "SEMI_RAW", Tuple.UP, AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
//        application.addAppEdge("fogProcess", "ACTUATOR", 500, 2000, "ALERT", Tuple.DOWN, AppEdge.ACTUATOR); // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
//        application.addAppEdge("fogProcess", "moreProcess", 1000, 100, "FOG_DATA", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
//        application.addAppEdge("moreProcess", "massAnalysis", 1000, 4000, "AREA_DATA", Tuple.UP, AppEdge.MODULE); // adding edge from Object Tracker to PTZ CONTROL (actuator) carrying tuples of type PTZ_PARAMS

        application.addAppEdge("lightRAW", "lightProcess", Paras.lightReqMI, Paras.lightReqByte, "lightRAW", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("heavyRAW", "heavyProcess", Paras.heavyReqMI, Paras.heavyReqByte, "heavyRAW", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("lightProcess", "ACTUATOR", 100, 10, "lightALERT", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge("heavyProcess", "ACTUATOR", 100, 10, "heavyALERT", Tuple.DOWN, AppEdge.ACTUATOR);

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
//        application.addTupleMapping("motion_detector", "CAMERA", "MOTION_VIDEO_STREAM", new FractionalSelectivity(0.5)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
//        application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "OBJECT_LOCATION", new FractionalSelectivity(1.0)); // 1.0 tuples of type OBJECT_LOCATION are emitted by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM
//        application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "DETECTED_OBJECT", new FractionalSelectivity(0.05)); // 0.05 tuples of type MOTION_VIDEO_STREAM are emitted by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM

//        application.addTupleMapping("preProcess", "RAW", "SEMI_RAW", new FractionalSelectivity(0.5));
//        application.addTupleMapping("fogProcess", "SEMI_RAW", "ALERT", new FractionalSelectivity(0.01));
//        application.addTupleMapping("fogProcess", "SEMI_RAW", "FOG_DATA", new FractionalSelectivity(0.5));
//        application.addTupleMapping("moreProcess", "FOG_DATA", "AREA_DATA", new FractionalSelectivity(0.5));

        application.addTupleMapping("lightProcess", "lightRAW", "lightALERT", new FractionalSelectivity(1));
        application.addTupleMapping("heavyProcess", "heavyRAW", "heavyALERT", new FractionalSelectivity(1));


        /*
         * Defining application loops (maybe incomplete loops) to monitor the latency of.
         * Here, we add two loops for monitoring : Motion Detector -> Object Detector -> Object Tracker and Object Tracker -> PTZ Control
         */
//        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
//            add("motion_detector");
//            add("object_detector");
//            add("object_tracker");
//        }});
//        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {{
//            add("object_tracker");
//            add("PTZ_CONTROL");
//        }});

        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {{
            add("heavyProcess");
            add("ACTUATOR");
        }});
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("lightRAW");
            add("lightProcess");
            add("ACTUATOR");
        }});
        final AppLoop loop3 = new AppLoop(new ArrayList<String>() {{
            add("heavyRAW");
            add("heavyProcess");
            add("ACTUATOR");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
            add(loop3);
        }};

        application.setLoops(loops);
        return application;
    }

    private static FogDevice addLowLevelFogDevice(String id, int brokerId, String appId) {
        FogDevice lowLevelFogDevice = createFogDevice("LowLevelFog-Device-" + id, 1000, 1000, 10000, 270, 2, 0, 87.53, 82.44);
        lowLevelFogDevice.setParentId(-1);
        lowLevelFogDevice.setxCoordinate(getValue(10.00));
        lowLevelFogDevice.setyCoordinate(getValue(15.00));
        getIdByName.put(lowLevelFogDevice.getName(), lowLevelFogDevice.getId());
        Sensor sensor = new Sensor("s-"+id, "Sensor", brokerId, appId, new DeterministicDistribution(getValue(5.00)));
        sensors.add(sensor);
        Actuator actuator = new Actuator("a-"+id, brokerId, appId, "OutputData");
        actuators.add(actuator);
        sensor.setGatewayDeviceId(lowLevelFogDevice.getId());
        sensor.setLatency(6.0);
        actuator.setGatewayDeviceId(lowLevelFogDevice.getId());
        actuator.setLatency(1.0);
        return lowLevelFogDevice;}
    private static double getValue(double min) {
        Random rn = new Random();
        return rn.nextDouble()*10 + min;
    }
}



