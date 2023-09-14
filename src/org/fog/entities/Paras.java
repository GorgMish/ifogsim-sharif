package org.fog.entities;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Random;


public class Paras {
//    public static int simTime = 600;
//    public static int simTime = 1800;
    public static int simTime = 3600;
    public final static int BESTEFFORT = 1;

    public final static int CLOUDFOG = 0;
    public final static int ClOUDONLY = 1;
    public final static int FOGONLY = 2;
    public final static int LFHC = 3;
    ////Simulation
    public static boolean calcLatency = true;
    public static boolean offloading = false;
    public static boolean mobility = true;
    public static boolean CLOUD = false;
    public static boolean schedulingDynamically = true;

    public static int resType = LFHC;
    public static int offloadingType = BESTEFFORT;

    public static String resTypeStr;

    public static Random randomGenerator;

    // Debug and Print
    public static boolean locDebug = true;
    public static boolean IoTlocDebug = true;
    public static boolean debug = false;
    public static String pathOfRun;
    public static int runNum = 0;
    ////TOPOLOGY

    // general
    public static int numOfAreas = 1;
    public static int numOfRooms = 1; //4
    public static int numOfPatients = 4; //4
    public static int numOfLightSensorPerPatient = 4;  //4
    public static int numOfHeavySensorPerPatient = 1;
//    public static int numOfLightSensorPerPatient = 8;  //4
//    public static int numOfHeavySensorPerPatient = 2;
//    public static int numOfLightSensorPerPatient = 12;  //4
//    public static int numOfHeavySensorPerPatient = 3;
    public static int numOfMobSensorPerArea = 1;
    public static double speed = 3 * 100000000;
    public static double زcoefPower = 0;
    public static long seed = 1;

    public static int nInLog = 4;
    public static double probOfNewMobileFog = 0.5;
    public static double newFogInterInterval = 2000.012;
    public static ArrayList<Pair<Double,Double>> directions = new ArrayList<Pair<Double,Double>>() {{ add(new Pair<Double,Double>(0.0, 0.0));add(new Pair <Double,Double> (1.0, 0.0));add(new Pair <Double,Double> (-1.0, 0.0));add(new Pair <Double,Double> (0.0, 1.0));add(new Pair<Double,Double>(0.0, -1.0));add(new Pair<Double,Double>(1.0, 1.0));add(new Pair<Double,Double>(-1.0, 1.0));add(new Pair<Double,Double>(1.0, -1.0));add(new Pair<Double,Double>(-1.0, -1.0)); }};
    public static ArrayList<Pair <Double,Double> > roomLocs = new ArrayList<Pair <Double,Double> > (){{add(new Pair <Double,Double> (0.0, 0.0));add(new Pair <Double,Double> (0.0, 15.0));add(new Pair <Double,Double> (15.0, 0.0));add(new Pair <Double,Double> (15.0, 15.0));add(new Pair <Double,Double> (7.5, 7.5));}};
//    public static ArrayList<Double> patientScheduleTime = new ArrayList<Double>() {{add(2500.0);add(3000.0);add(3500.0);}};
    public static ArrayList<Pair<Double, Double>> patientScheduleTime = new ArrayList<Pair<Double, Double>>() {{
        add(new Pair<Double, Double>(0.2, 0.1));
        add(new Pair<Double, Double>(0.25, 0.05));
        add(new Pair<Double, Double>(0.25, 0.15));
    }};

    public static ArrayList<Pair<Double, Double>> fogScheduleTime = new ArrayList<Pair<Double, Double>>() {{
        add(new Pair<Double, Double>(0.4, 0.15));
        add(new Pair<Double, Double>(0.5, 0.20));
    }};

    public static ArrayList<Integer> IoTinRooms = new ArrayList<Integer>();
    public static ArrayList<Integer> patientInArea = new ArrayList<Integer>();
    // mobile IoT
    public static boolean IoTMob = false;
    public static boolean all_IoT_mob = false;

    // Mobility Intervals
    public static double fogMobilityTimeInterval = 1;
    public static double patientMobilityTimeInterval = 1;
//    public static double newPatientInterInterval = 500.015;

    // computation
    public static double calcNumTasksInterval = 1.00;
    public static final String ENERGY_CONSUMPTION_TXT = "energyConsumption.txt";
    public static final String TASK_NUMBER_TXT = "tasksNumber.txt";

    public static long lightMIPS = 100;
    public static long heavyMIPS = 800;
//    public static long lightMIPS = 1;
//    public static long heavyMIPS = 200;

    public static long fog1MIPS = 2800;
    public static long fog2MIPS = 4480;
//    public static long fog1MIPS = 5000;
//    public static long fog2MIPS = 8000;
//    public static long fog1MIPS = 3000;
//    public static long fog2MIPS = 5000;
//    public static long fog1MIPS = 10000;
//    public static long fog2MIPS = 15000;
//    public static long cloudMIPS = 50000;
    public static long cloudMIPS = 44800;

    // power
    public static double baseMIPS = fog1MIPS;


    public static double powerCloudActive = (cloudMIPS/fog1MIPS) * 83.433;
    public static double powerCloudIdle = (cloudMIPS/fog1MIPS) * 83.433;
//    public static double powerCloudActive = ((double)cloudMIPS/baseMIPS) * 103.339;
//    public static double powerCloudIdle = 0.0;
    public static double powerL2Active = (fog2MIPS/fog1MIPS) * 107.339;
    public static double powerL2Idle = (fog2MIPS/fog1MIPS) * 83.433;
//    public static double powerL2Active = ((double)fog2MIPS/baseMIPS) * 103.339;
//    public static double powerL2Idle = 0.0;

    public static double powerL1Active = (fog1MIPS/fog1MIPS) * 107.339;
    public static double powerL1Idle = (fog1MIPS/fog1MIPS) * 83.433;
//    public static double powerL1Idle = (fog1MIPS/3000.0) * 83.25;
//    public static double powerL1Idle = 0.0;

    public static double initialPowHour = 18;
    public static double batteryThreshold = 5;
    public static double RSSIthreshold = -80;
    public static double deadline = 5.0;

    // requests
    public static double lightReqMI = 100;
    public static double heavyReqMI = 1500;
    public static double lightReqByte = 100;
    public static double heavyReqByte = 80000;

    public static double lightReqPow = 100;
    public static double heavyReqPow = 400;

    public static double lightEmitInterval = 2;
    public static double heavyEmitInterval  = 10;

    // request to main fog
    public static double lightReqToSec = 4;
    public static double heavyReqToSec = 4;

    // avg runtime
    public static double lightFog1ReqAvgRun = 33;
    public static double heavyFog1ReqAvgRun = 500;
    public static double lightFog2ReqAvgRun = 10;
    public static double heavyFog2ReqAvgRun = 150;
    public static double lightCloudReqAvgRun = 2;
    public static double heavyCloudReqAvgRun = 30;

    // topology
    public static double areaMargin = 5;
    public static double ariaSide = 15;
    public static double fog2Dis = 2000;
    public static double cloudDis = 8000000;

    // links
    public static long bwFromLightKB = 250;
    public static long bwFromHeavyMB = 54;
    public static long bwToActKB = 250;
    public static long bwBetFogMB = 100;
    public static long bwBetLayerGB = 10;


    // const
    public final static long GB = 1000000000;
    public final static long MB = 1000000;
    public final static long KB = 1000;
    public final static long HtoS = 3600;


}














//
//
//
//
//        Log.disable();
//        int num_user = 1; // number of cloud users
//        Calendar calendar = Calendar.getInstance();
//        boolean trace_flag = false; // mean trace events
//        long seed = 1;
//        Paras.randomGenerator = new Random(seed);
//        CloudSim.init(num_user, calendar, trace_flag);
//
//        String appId = "ٍExample1"; // identifier of the application
//
//        FogBroker broker = new FogBroker("broker");
//
//        Application application = createApplication(appId, broker.getId());
//        application.setUserId(broker.getId());
//
//        createFogDevices(broker.getId(), appId);
//
//        Controller controller = null; //todo
//
//        ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
//
//        if (Paras.CLOUD) {
//        moduleMapping.addModuleToDevice("lightProcess", "Cloud");
//        moduleMapping.addModuleToDevice("heavyProcess", "Cloud");
//        // if the mode of deployment is cloud-based
////                moduleMapping.addModuleToDevice("preProcess", "cloud");
////                moduleMapping.addModuleToDevice("fogProcess", "cloud"); // placing all instances of Object Detector module in the Cloud
////                moduleMapping.addModuleToDevice("moreProcess", "cloud"); // placing all instances of Object Tracker module in the Cloud
////                moduleMapping.addModuleToDevice("massAnalysis", "cloud");
//        } else{
//        for (FogDevice device : fogDevices) {
//        if (!device.getName().contains("Sensor")) { // names of all Smart Cameras start with 'm'
////                    moduleMapping.addModuleToDevice("motion_detector", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
//        moduleMapping.addModuleToDevice("lightProcess", device.getName());
//        moduleMapping.addModuleToDevice("heavyProcess", device.getName());
//        }
////                moduleMapping.addModuleToDevice("motion_detector", device.getName());
//        }
//        }
//
//        controller = new Controller("master-controller", fogDevices, sensors,
//        actuators);
////            if (Paras.mobility)
////			    controller.setMobilityMap(mobilityMap);
//        controller.submitApplication(application,
//        (Paras.CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
//        : (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));
//
//        TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
//
//        CloudSim.startSimulation();
//
//        CloudSim.stopSimulation();
//
//        Log.printLine("VRGame finished!");





//
//            Log.disable();
//                    int num_user = 1; // number of cloud users
//                    Calendar calendar = Calendar.getInstance();
//                    boolean trace_flag = false; // mean trace events
//                    long seed = 1;
//                    Paras.randomGenerator = new Random(seed);
//                    CloudSim.init(num_user, calendar, trace_flag);
//
//                    String appId = "ٍExample1"; // identifier of the application
//
//                    FogBroker broker = new FogBroker("broker");
//
//                    Application application = createApplication(appId, broker.getId());
//                    application.setUserId(broker.getId());
//
//                    createFogDevices(broker.getId(), appId);
//
//                    Controller controller = null; //todo
//
//                    switch (Paras.resType){
//                    case Paras.CLOUDFOG:
//                    Paras.CLOUD = false;
//                    Paras.offloading = true;
//                    Paras.resTypeStr = "CLOUDFOG";
//                    break;
//                    case Paras.ClOUDONLY:
//                    Paras.CLOUD = true;
//                    Paras.offloading = false;
//                    Paras.resTypeStr = "ClOUDONLY";
//                    break;
//                    case Paras.FOGONLY:
//                    Paras.CLOUD = false;
//                    Paras.offloading = true;
//                    Paras.resTypeStr = "FOGONLY";
//                    break;
//                    case Paras.LFHC:
//                    Paras.CLOUD = false;
//                    Paras.offloading = true;
//                    Paras.resTypeStr = "LFHC";
//                    break;
//                    }
//                    ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
//
//                    if (Paras.CLOUD) {
//                    moduleMapping.addModuleToDevice("lightProcess", "Cloud");
//                    moduleMapping.addModuleToDevice("heavyProcess", "Cloud");
//                    // if the mode of deployment is cloud-based
////                moduleMapping.addModuleToDevice("preProcess", "cloud");
////                moduleMapping.addModuleToDevice("fogProcess", "cloud"); // placing all instances of Object Detector module in the Cloud
////                moduleMapping.addModuleToDevice("moreProcess", "cloud"); // placing all instances of Object Tracker module in the Cloud
////                moduleMapping.addModuleToDevice("massAnalysis", "cloud");
//                    } else{
//                    for (FogDevice device : fogDevices) {
//                    if (!device.getName().contains("Sensor")) { // names of all Smart Cameras start with 'm'
////                    moduleMapping.addModuleToDevice("motion_detector", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
//                    moduleMapping.addModuleToDevice("lightProcess", device.getName());
//                    moduleMapping.addModuleToDevice("heavyProcess", device.getName());
//                    }
////                moduleMapping.addModuleToDevice("motion_detector", device.getName());
//                    }
//                    }
//
//                    controller = new Controller("master-controller", fogDevices, sensors,
//                    actuators);
////            if (Paras.mobility)
////			    controller.setMobilityMap(mobilityMap);
//                    controller.submitApplication(application,
//                    (Paras.CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
//                    : (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));
//
//                    TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
//
//                    CloudSim.startSimulation();
//
//                    CloudSim.stopSimulation();
//
//                    Log.printLine("VRGame finished!");

