package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashMap;
import java.util.Map.Entry;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.measures.WaitingTime;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class WaitingTimeCalculator implements SimulationListenerI {
    
    private HashMap<P, WaitingTime> waitingTimeMap = null;
    private ExecutionTimeCalculator executionTimeCalculator = null;
    
    public WaitingTimeCalculator(ExecutionTimeCalculator executionTimeCalculator){
        this.executionTimeCalculator = executionTimeCalculator;
    }
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        waitingTimeMap = new HashMap<P, WaitingTime>();
        for(P initialPlace:petriNet.getStartList())
            waitingTimeMap.put(initialPlace, new WaitingTime(0));
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {//FIXME: arc weight
        for(P consumedPlace:transitionFired.previousList){
            WaitingTime placeWT = waitingTimeMap.get(consumedPlace);
            if(placeWT == null)
                continue;
                //throw new Exception("Impossible to identify the waiting time map for the object " + consumedPlace.name + " during the firing of the transition " + transitionFired.name);
            placeWT.executionTime = executionTimeCalculator.getExecutionTimeBeforeTransition();
            if(placeWT.getWaitingTime()==0)
                waitingTimeMap.remove(consumedPlace);
        }
        
        for(P filledPlace:transitionFired.nextList){
            if(!waitingTimeMap.containsKey(filledPlace)){
                waitingTimeMap.put(filledPlace, new WaitingTime(executionTimeCalculator.getExecutionTime()));
            }else{
                //in this case i'm in a loop. nothing to do in order to wait till the beginning
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        //fix the waiting time for the end places that are never executed and generate negative values
        for(Entry<P, WaitingTime> entry: ((HashMap<P, WaitingTime>)waitingTimeMap.clone()).entrySet())
            if(entry.getValue().arrivalTime > entry.getValue().executionTime)
                waitingTimeMap.remove(entry.getKey());
    }
    
    public HashMap<P, WaitingTime> getWaitingTimeMap() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        return waitingTimeMap;
    }

    public long getTotalWaitingTime() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        long tot = 0;
        for(Entry<P, WaitingTime> entry: waitingTimeMap.entrySet())
            tot += entry.getValue().getWaitingTime();
        return tot;
    }
    
    public long getMessagesTotalWaitingTime() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        long tot = 0;
        for(Entry<P, WaitingTime> entry: waitingTimeMap.entrySet())
            if(entry.getKey().excludeFromDeadlockCheck)
                tot += entry.getValue().getWaitingTime();
        return tot;
    }
    
    public int getMessagePlaceCount() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        int count = 0;
        for(Entry<P, WaitingTime> entry: waitingTimeMap.entrySet())
            if(entry.getKey().excludeFromDeadlockCheck)
                count++;
        return count;
    }
    
    public double getTotalWaitingTimeAverage() throws Exception{
        if(waitingTimeMap.size()==0)
            return 0;
        return ((double)getTotalWaitingTime()) / waitingTimeMap.size();
    }
    
    public double getMessagesTotalWaitingTimeAverage() throws Exception{
        int numMessagePlace = getMessagePlaceCount();
        if(numMessagePlace==0)
            return 0;
        return ((double)getMessagesTotalWaitingTime()) / numMessagePlace;
    }
    
    public double getTotalWaitingTimeStandardDeviation() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        
        if(waitingTimeMap.size()==0)
            return 0;
        
        double sum = 0;
        double avg = getTotalWaitingTimeAverage();
        for(Entry<P, WaitingTime> entry: waitingTimeMap.entrySet())
            sum += Math.pow(((double)entry.getValue().getWaitingTime())-avg, 2);
        double sd = sum / (waitingTimeMap.size());
        sd = Math.sqrt(sd);
        return sd;
    }
    
    public double getMessagesWaitingTimeStandardDeviation() throws Exception{
        if(waitingTimeMap == null)
            throw new Exception("Simulation has not been started. The waitingTimeMap is currently null");
        
        int numMsgPlaces = getMessagePlaceCount();
        
        if(numMsgPlaces==0)
            return 0;
        
        double sum = 0;
        double avg = getMessagesTotalWaitingTimeAverage();
        for(Entry<P, WaitingTime> entry: waitingTimeMap.entrySet())
            if(entry.getKey().excludeFromDeadlockCheck)
                sum += Math.pow(((double)entry.getValue().getWaitingTime())-avg, 2);
        double sd = sum / (numMsgPlaces);
        sd = Math.sqrt(sd);
        return sd;
    }
}
