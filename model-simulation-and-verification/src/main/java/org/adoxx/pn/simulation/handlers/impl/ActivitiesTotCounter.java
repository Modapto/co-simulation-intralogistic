package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashMap;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.measures.Measures;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class ActivitiesTotCounter implements SimulationListenerI {

    HashMap<T, Measures> activityList = new HashMap<T, Measures>();
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        //FIXME: make more general
        if(transitionFired.additionalInfoList.containsKey("isEntryPoint")){
            Measures measures = activityList.get(transitionFired);
            if(measures==null){
                
                boolean hasCost = false;
                double cost = 0;
                for(P place: transitionFired.previousList)
                    if(place.additionalInfoList.containsKey("cost")){
                        cost += Double.parseDouble(place.additionalInfoList.get("cost"));
                        hasCost = true;
                    }
                boolean hasExecutionTime = false;
                long executionTime = 0;
                for(P place: transitionFired.previousList)
                    if(place.additionalInfoList.containsKey("executionTime")){
                        executionTime += Long.parseLong(place.additionalInfoList.get("executionTime"));
                        hasExecutionTime = true;
                    }
                
                if(hasCost || hasExecutionTime){
                    measures = new Measures();
                    measures.costs = cost;
                    measures.executionTime = executionTime;
                    activityList.put(transitionFired, measures);
                }
            } else {
                measures.numberOfExecutions++;
            }
        } else {
            //TODO: contare gli XOR o gli end ?
        }
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        
    }
    
    public HashMap<T, Measures> getActivitiesMeasures(){
        return activityList;
    }

}
