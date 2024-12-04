package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Trace;
import org.adoxx.pn.simulation.data.measures.TraceDouble;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class CostMaxMinTotCalculator implements SimulationListenerI {

    private CostCalculator costCalculator = null;
    private TraceGeneration traceGeneration = null;

    private TraceDouble maxCost = new TraceDouble(null, -1);
    private TraceDouble minCost = new TraceDouble(null, Double.MAX_VALUE);
    
    double totalCost = 0;
    
    public CostMaxMinTotCalculator(CostCalculator costCalculator, TraceGeneration traceGeneration) {
        this.costCalculator = costCalculator;
        this.traceGeneration = traceGeneration;
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        Trace trace = traceGeneration.getUniqueEndedTrace();//.getTrace();
        if(costCalculator.getCost() > maxCost.value){
            maxCost.value = costCalculator.getCost();
            maxCost.trace = trace;
        }
        
        if(costCalculator.getCost() < minCost.value){
            minCost.value = costCalculator.getCost();
            minCost.trace = trace;
        }
        
        totalCost += costCalculator.getCost();
    }
    
    public TraceDouble getMaxCosts(){ return maxCost; }
    public TraceDouble getMinCosts(){ return minCost; }
    public double getTotalCost(){ return totalCost; }
}
