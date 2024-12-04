package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashMap;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Path;
import org.adoxx.pn.simulation.data.Trace;
import org.adoxx.pn.simulation.data.measures.TraceDouble;
import org.adoxx.pn.simulation.data.measures.TraceLong;
import org.adoxx.pn.simulation.data.measures.TraceLongDouble;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class WaitingTimeMaxMinCalculator implements SimulationListenerI {

    private WaitingTimeCalculator waitingTimeCalculator = null;
    private TraceGeneration traceGeneration = null;
    private PathGeneration pathGeneration = null;
    
    private TraceLong maxWaitingTime = new TraceLong(null, -1);
    private TraceLong minWaitingTime = new TraceLong(null, Long.MAX_VALUE);
    
    private TraceLong maxMsgWaitingTime = new TraceLong(null, -1);
    private TraceLong minMsgWaitingTime = new TraceLong(null, Long.MAX_VALUE);
    
    private TraceDouble minWaitingTimeStandardDeviation = new TraceDouble(null, Double.MAX_VALUE);
    private TraceDouble minMsgWaitingTimeStandardDeviation = new TraceDouble(null, Double.MAX_VALUE);
    
    private TraceDouble minStandardDeviationOfMinWaitingTime = new TraceDouble(null, Double.MAX_VALUE);
    private TraceDouble minMsgStandardDeviationOfMinWaitingTime = new TraceDouble(null, Double.MAX_VALUE);

    private HashMap<Path, TraceLongDouble> minWaitingTimeWithMinStandardDeviationForEveryPath = new HashMap<Path, TraceLongDouble>();
    private HashMap<Path, TraceLongDouble> minMsgWaitingTimeWithMinStandardDeviationForEveryPath = new HashMap<Path, TraceLongDouble>();
    
    public WaitingTimeMaxMinCalculator(WaitingTimeCalculator waitingTimeCalculator, TraceGeneration traceGeneration, PathGeneration pathGeneration) {
        this.waitingTimeCalculator = waitingTimeCalculator;
        this.traceGeneration = traceGeneration;
        this.pathGeneration = pathGeneration;
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        long currentTotalWaitingTime = waitingTimeCalculator.getTotalWaitingTime();
        long currentTotalMsgWaitingTime = waitingTimeCalculator.getMessagesTotalWaitingTime();
        double currentTotalWaitingTimeStandardDeviation = waitingTimeCalculator.getTotalWaitingTimeStandardDeviation();
        double currentTotalMsgWaitingTimeStandardDeviation = waitingTimeCalculator.getMessagesWaitingTimeStandardDeviation();
        boolean containMessages = waitingTimeCalculator.getMessagePlaceCount()!=0;
        
        Trace trace = traceGeneration.getUniqueEndedTrace();
        Path path = pathGeneration.getUniqueEndedPath();
        
        if(currentTotalWaitingTime > maxWaitingTime.value){
            maxWaitingTime.value = currentTotalWaitingTime;
            maxWaitingTime.trace = trace;
        }
        
        if(currentTotalWaitingTime <= minWaitingTime.value && currentTotalWaitingTimeStandardDeviation <= minStandardDeviationOfMinWaitingTime.value){
            minWaitingTime.value = currentTotalWaitingTime;
            minWaitingTime.trace = trace;
            
            minStandardDeviationOfMinWaitingTime.value = currentTotalWaitingTimeStandardDeviation;
            minStandardDeviationOfMinWaitingTime.trace = trace;
        }

        TraceLongDouble currentPathTotalWaitingTimeAndSD = minWaitingTimeWithMinStandardDeviationForEveryPath.get(path);
        if(currentPathTotalWaitingTimeAndSD == null){
            minWaitingTimeWithMinStandardDeviationForEveryPath.put(path, new TraceLongDouble(trace, currentTotalWaitingTime, currentTotalWaitingTimeStandardDeviation));
        }else{
            if(currentTotalWaitingTime <= currentPathTotalWaitingTimeAndSD.valueLong && currentTotalWaitingTimeStandardDeviation <= currentPathTotalWaitingTimeAndSD.valueDouble){
                currentPathTotalWaitingTimeAndSD.trace = trace;
                currentPathTotalWaitingTimeAndSD.valueLong = currentTotalWaitingTime;
                currentPathTotalWaitingTimeAndSD.valueDouble = currentTotalWaitingTimeStandardDeviation;
            }
        }
        
        if(currentTotalWaitingTimeStandardDeviation < minWaitingTimeStandardDeviation.value){
            minWaitingTimeStandardDeviation.value = currentTotalWaitingTimeStandardDeviation;
            minWaitingTimeStandardDeviation.trace = trace;
        }
        
        if(containMessages){
            if(currentTotalMsgWaitingTime > maxMsgWaitingTime.value){
                maxMsgWaitingTime.value = currentTotalMsgWaitingTime;
                maxMsgWaitingTime.trace = trace;
            }
            
            if(currentTotalMsgWaitingTime <= minMsgWaitingTime.value && currentTotalMsgWaitingTimeStandardDeviation <= minMsgStandardDeviationOfMinWaitingTime.value){
                minMsgWaitingTime.value = currentTotalMsgWaitingTime;
                minMsgWaitingTime.trace = trace;
                
                minMsgStandardDeviationOfMinWaitingTime.value = currentTotalMsgWaitingTimeStandardDeviation;
                minMsgStandardDeviationOfMinWaitingTime.trace = trace;
            }
            
            TraceLongDouble currentPathTotalMsgWaitingTimeAndSD = minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(path);
            if(currentPathTotalMsgWaitingTimeAndSD == null){
                minMsgWaitingTimeWithMinStandardDeviationForEveryPath.put(path, new TraceLongDouble(trace, currentTotalMsgWaitingTime, currentTotalMsgWaitingTimeStandardDeviation));
            }else{
                if(currentTotalMsgWaitingTime <= currentPathTotalMsgWaitingTimeAndSD.valueLong && currentTotalMsgWaitingTimeStandardDeviation <= currentPathTotalMsgWaitingTimeAndSD.valueDouble){
                    currentPathTotalMsgWaitingTimeAndSD.trace = trace;
                    currentPathTotalMsgWaitingTimeAndSD.valueLong = currentTotalMsgWaitingTime;
                    currentPathTotalMsgWaitingTimeAndSD.valueDouble = currentTotalMsgWaitingTimeStandardDeviation;
                }
            }
            
            if(currentTotalMsgWaitingTimeStandardDeviation < minMsgWaitingTimeStandardDeviation.value){
                minMsgWaitingTimeStandardDeviation.value = currentTotalMsgWaitingTimeStandardDeviation;
                minMsgWaitingTimeStandardDeviation.trace = trace;
            }
        }
    }
    
    public TraceLong getMaxWaitingTime(){ return maxWaitingTime; }
    public TraceLong getMinWaitingTime(){ return minWaitingTime; }
    
    public TraceLong getMsgMaxWaitingTime(){ return maxMsgWaitingTime; }
    public TraceLong getMsgMinWaitingTime(){ return minMsgWaitingTime; }
    
    public TraceDouble getMinWaitingTimeStandardDeviation(){ return minWaitingTimeStandardDeviation; }
    public TraceDouble getMinMsgWaitingTimeStandardDeviation(){ return minMsgWaitingTimeStandardDeviation; }
    
    public TraceDouble getMinWaitingTimeWithMinStandardDeviation(){ return minStandardDeviationOfMinWaitingTime; }
    public TraceDouble getMinMsgWaitingTimeWithMinStandardDeviation(){ return minMsgStandardDeviationOfMinWaitingTime; }
    
    public HashMap<Path, TraceLongDouble> getMinWaitingTimeWithMinStandardDeviationForEveryPath(){ return minWaitingTimeWithMinStandardDeviationForEveryPath; }
    public HashMap<Path, TraceLongDouble> getMinMsgWaitingTimeWithMinStandardDeviationForEveryPath(){ return minMsgWaitingTimeWithMinStandardDeviationForEveryPath; }
}
