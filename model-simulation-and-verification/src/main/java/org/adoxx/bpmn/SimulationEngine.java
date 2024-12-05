package org.adoxx.bpmn;

import java.util.Date;

import org.adoxx.pn.PetriNet;

import org.adoxx.pn.input.ImporterManager;
import org.adoxx.pn.simulation.Simulation;
import org.adoxx.pn.simulation.data.Path;
import org.adoxx.pn.simulation.data.PathCollector;
import org.adoxx.pn.simulation.data.SimulationResults;
import org.adoxx.pn.simulation.data.Trace;
import org.adoxx.pn.simulation.data.TraceCollector;
import org.adoxx.pn.simulation.data.measures.Measures;
import org.adoxx.pn.simulation.data.measures.TotalMeasures;
import org.adoxx.pn.simulation.handlers.impl.ActivitiesTotCounter;
import org.adoxx.pn.simulation.handlers.impl.CostCalculator;
import org.adoxx.pn.simulation.handlers.impl.CostMaxMinTotCalculator;
import org.adoxx.pn.simulation.handlers.impl.DeadlockCounter;
import org.adoxx.pn.simulation.handlers.impl.DeadlockIdentifier;
import org.adoxx.pn.simulation.handlers.impl.ExecutionTimeCalculator;
import org.adoxx.pn.simulation.handlers.impl.ExecutionTimeMaxMinTotCalculator;
import org.adoxx.pn.simulation.handlers.impl.LivelockCounter;
import org.adoxx.pn.simulation.handlers.impl.LivelockIdentifier;
import org.adoxx.pn.simulation.handlers.impl.PathGeneration;
import org.adoxx.pn.simulation.handlers.impl.TerminateEventCounter;
import org.adoxx.pn.simulation.handlers.impl.TerminateEventHandler;
import org.adoxx.pn.simulation.handlers.impl.TraceGeneration;
import org.adoxx.pn.simulation.handlers.impl.TransitionSelectorAdoxx;
import org.adoxx.pn.simulation.handlers.impl.TransitionSelectorDefault;
import org.adoxx.pn.simulation.handlers.impl.WaitingTimeCalculator;
import org.adoxx.pn.simulation.handlers.impl.WaitingTimeMaxMinCalculator;
import org.adoxx.utils.Utils;

public class SimulationEngine {
    
    SimulationResults[] simulationResultsList = new SimulationResults[0];;
        
    ImporterManager importManager = null;
    Simulation simulation = null;
    PathGeneration pathGeneration = null;
    TraceGeneration traceGeneration = null;
    CostCalculator costCalculator = null;
    CostMaxMinTotCalculator costMaxMinTotCalculator = null;
    ExecutionTimeCalculator executionTimeCalculator = null;
    ExecutionTimeMaxMinTotCalculator executionTimeMaxMinTotCalculator = null;
    WaitingTimeCalculator waitingTimeCalculator = null;
    WaitingTimeMaxMinCalculator waitingTimeMaxMinCalculator = null;
    ActivitiesTotCounter activitiesTotCounter = null;
    TerminateEventHandler terminateEventHandler = null;
    TerminateEventCounter terminateEventCounter = null;
    DeadlockIdentifier deadlockIdentifier = null;
    DeadlockCounter deadlockCounter = null;
    LivelockIdentifier livelockIdentifier = null;
    LivelockCounter livelockCounter = null;
    TransitionSelectorAdoxx transitionSelectorAdoxx = null;
    TransitionSelectorDefault transitionSelectorDefault = null;
    
    public SimulationEngine(){
        initialize();
    }
    
    private void initialize(){
        importManager = new ImporterManager();
        pathGeneration = new PathGeneration();
        traceGeneration = new TraceGeneration(pathGeneration);
        costCalculator = new CostCalculator();
        costMaxMinTotCalculator = new CostMaxMinTotCalculator(costCalculator, traceGeneration);
        executionTimeCalculator = new ExecutionTimeCalculator();
        executionTimeMaxMinTotCalculator = new ExecutionTimeMaxMinTotCalculator(executionTimeCalculator, traceGeneration);
        waitingTimeCalculator = new WaitingTimeCalculator(executionTimeCalculator);
        waitingTimeMaxMinCalculator = new WaitingTimeMaxMinCalculator(waitingTimeCalculator, traceGeneration, pathGeneration);
        activitiesTotCounter = new ActivitiesTotCounter();
        terminateEventHandler = new TerminateEventHandler();
        terminateEventCounter = new TerminateEventCounter(terminateEventHandler);
        deadlockIdentifier = new DeadlockIdentifier();
        deadlockCounter = new DeadlockCounter(deadlockIdentifier, pathGeneration);
        livelockIdentifier = new LivelockIdentifier();
        livelockCounter = new LivelockCounter(livelockIdentifier, pathGeneration);
        transitionSelectorAdoxx = new TransitionSelectorAdoxx();
        transitionSelectorDefault = new TransitionSelectorDefault();
        
        simulation = new Simulation();
        //Follow the definition order.. IT'S IMPORTANT!! (is the order by which they will be called during the simulation)
        simulation.setTransitionSelector(transitionSelectorAdoxx);
        //simulation.setTransitionSelector(transitionSelectorDefault);
        simulation.addSimulationListener(pathGeneration);
        simulation.addSimulationListener(traceGeneration);
        simulation.addSimulationListener(costCalculator);
        simulation.addSimulationListener(costMaxMinTotCalculator);
        simulation.addSimulationListener(executionTimeCalculator);
        simulation.addSimulationListener(executionTimeMaxMinTotCalculator);
        simulation.addSimulationListener(waitingTimeCalculator);
        simulation.addSimulationListener(waitingTimeMaxMinCalculator);
        simulation.addSimulationListener(activitiesTotCounter);
        simulation.addSimulationListener(terminateEventHandler);
        simulation.addSimulationListener(terminateEventCounter);
        simulation.addSimulationListener(deadlockIdentifier);
        simulation.addSimulationListener(deadlockCounter);
        simulation.addSimulationListener(livelockIdentifier);
        simulation.addSimulationListener(livelockCounter);
    }
    
    public ImporterManager getImporterManager(){
        return importManager;
    }
    
    public Simulation getSimulation(){
        return simulation;
    }
    
    public SimulationResults[] getSimulationResults(){
        return simulationResultsList;
    }
    
    public void executePathAnalysis(String model, int numExecutions) throws Exception{
        
        simulationResultsList = new SimulationResults[0];
        
        long importStartTime = new Date().getTime();
        PetriNet[] pnList = importManager.generateFromModel(model);
        long importEndTime = new Date().getTime() - importStartTime;
        Utils.log("Last model import time: " + Utils.convertoMillisecondsToStringDateTime(importEndTime), Utils.LogType.INFO);
        
        simulationResultsList = new SimulationResults[pnList.length];
        
        for(int i=0;i<pnList.length;i++){
            initialize();
            
            PetriNet pn = pnList[i];
            long startingSimulationTime = new Date().getTime();
            
            simulation.setPetriNet(pn);
            transitionSelectorAdoxx.setPetriNet(pn);
            
            TraceCollector traceCollector = new TraceCollector();
            PathCollector pathCollector = new PathCollector();
            
            for(int executionCounter=0;executionCounter<numExecutions;executionCounter++){
                simulation.singleRunEvent();
                
                Path path = pathGeneration.getUniqueEndedPath();
                Trace trace = traceGeneration.getUniqueEndedTrace();
                
                Measures traceMeasures = new Measures();
                traceMeasures.executionTime = executionTimeCalculator.getExecutionTime();
                traceMeasures.costs = costCalculator.getCost();
                traceMeasures.waitingTimeMap = waitingTimeCalculator.getWaitingTimeMap();
                traceMeasures.totalWaitingTime = waitingTimeCalculator.getTotalWaitingTime();
                traceMeasures.totalMsgWaitingTime = waitingTimeCalculator.getMessagesTotalWaitingTime();
                traceMeasures.averageWaitingTime = waitingTimeCalculator.getTotalWaitingTimeAverage();
                traceMeasures.averageMsgWaitingTime = waitingTimeCalculator.getMessagesTotalWaitingTimeAverage();
                traceMeasures.standardDeviationWaitingTime = waitingTimeCalculator.getTotalWaitingTimeStandardDeviation();
                traceMeasures.standardDeviationMsgWaitingTime = waitingTimeCalculator.getMessagesWaitingTimeStandardDeviation();
                
                Measures pathMeasures = new Measures();
                pathMeasures.terminateEventHappens = terminateEventHandler.isHappened();
                pathMeasures.deadlockHappens = deadlockIdentifier.isDeadlocked();
                pathMeasures.livelockHappens = livelockIdentifier.isLivelocked();
                
                traceCollector.addTrace(trace, traceMeasures);               
                pathCollector.addPath(path, trace, pathMeasures);
            }
            
            
            TotalMeasures totalMeasures = new TotalMeasures();
            totalMeasures.totalRuns = numExecutions;
            
            pathCollector.updatePathProbability(totalMeasures);
            
            totalMeasures.maxExecutionTime = executionTimeMaxMinTotCalculator.getMaxExecutionTime();
            totalMeasures.minExecutionTime = executionTimeMaxMinTotCalculator.getMinExecutionTime();
            totalMeasures.totalExecutionTime = executionTimeMaxMinTotCalculator.getTotalExecutionTime();
            
            totalMeasures.maxCosts = costMaxMinTotCalculator.getMaxCosts();
            totalMeasures.minCosts = costMaxMinTotCalculator.getMinCosts();           
            totalMeasures.totalCost = costMaxMinTotCalculator.getTotalCost();
            
            totalMeasures.maxWaitingTime = waitingTimeMaxMinCalculator.getMaxWaitingTime();
            totalMeasures.minWaitingTime = waitingTimeMaxMinCalculator.getMinWaitingTime();
            
            totalMeasures.maxMsgWaitingTime = waitingTimeMaxMinCalculator.getMsgMaxWaitingTime();
            totalMeasures.minMsgWaitingTime = waitingTimeMaxMinCalculator.getMsgMinWaitingTime();
            
            totalMeasures.minWaitingTimeStandardDeviation = waitingTimeMaxMinCalculator.getMinWaitingTimeStandardDeviation();
            totalMeasures.minMsgWaitingTimeStandardDeviation = waitingTimeMaxMinCalculator.getMinMsgWaitingTimeStandardDeviation();
            
            totalMeasures.minWaitingTimeWithMinStandardDeviation = waitingTimeMaxMinCalculator.getMinWaitingTimeWithMinStandardDeviation();
            totalMeasures.minMsgWaitingTimeWithMinStandardDeviation = waitingTimeMaxMinCalculator.getMinMsgWaitingTimeWithMinStandardDeviation();
            
            totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath = waitingTimeMaxMinCalculator.getMinWaitingTimeWithMinStandardDeviationForEveryPath();
            totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath = waitingTimeMaxMinCalculator.getMinMsgWaitingTimeWithMinStandardDeviationForEveryPath();
            
            totalMeasures.totalTerminatedTraces = terminateEventCounter.getCounter();
            totalMeasures.totalDeadlockedTraces = deadlockCounter.getTraceCounter();
            totalMeasures.totalDeadlockedPaths = deadlockCounter.getPathCounter();
            totalMeasures.totalLivelockedTraces = livelockCounter.getTraceCounter();
            totalMeasures.totalLivelockedPaths = livelockCounter.getPathCounter();
            
            totalMeasures.totalEnlapsedTime = (new Date().getTime()) - startingSimulationTime;
            
            totalMeasures.activitiesMeasures = activitiesTotCounter.getActivitiesMeasures();
            
            SimulationResults simulationResults = new SimulationResults();
            simulationResultsList[i] = simulationResults;
            simulationResults.model = model;
            simulationResults.petriNet = pn;
            simulationResults.pathCollector = pathCollector;
            simulationResults.traceCollector = traceCollector;
            simulationResults.totalMeasures = totalMeasures;
        }
    }

    public String getXMLResults(boolean fullXml) throws Exception{
        String ret = "<SimulationResults>";
        for(SimulationResults simulationResults:simulationResultsList){
            if(fullXml)
                ret += simulationResults.generateSimulationResultsXML();
            else
                ret += simulationResults.generateSimulationResultsXMLSlim();
        }
        ret += "</SimulationResults>";
        return ret;
    }
    
    
    /*
    public static void main(String[] args) {
        try {
            String bpmnUrl = "D:\\SIMULATOR\\TEST MODELS\\testtest.pnml";
            String bpmnModel = new String(org.adoxx.utils.IOUtils.readFile(bpmnUrl));
            SimulationEngine sm = new SimulationEngine();
            long startTime = new Date().getTime();
            sm.executePathAnalysis(bpmnModel, 1000); //100000
            
            System.out.println(sm.getXMLResults(false));
            
            //System.out.println("Tot exec time:"+sm.getSimulationResults()[0].totalMeasures.totalExecutionTime);
            //System.out.println("Tot terminated:"+sm.getSimulationResults()[0].totalMeasures.totalTerminatedTraces);
            //System.out.println("Tot deadlock:"+sm.getSimulationResults()[0].totalMeasures.totalDeadlockedTraces);

            System.out.println("Tot enlapsed time:"+Utils.convertoMillisecondsToStringDateTime(sm.getSimulationResults()[0].totalMeasures.totalEnlapsedTime));
            System.out.println("Tot time: "+(Utils.convertoMillisecondsToStringDateTime(new Date().getTime()-startTime)));
        }catch(Exception ex){ex.printStackTrace();}
    }
    */
}
