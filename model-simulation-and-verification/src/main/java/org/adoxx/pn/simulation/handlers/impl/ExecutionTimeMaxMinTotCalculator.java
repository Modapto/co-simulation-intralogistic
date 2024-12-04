package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Trace;
import org.adoxx.pn.simulation.data.measures.TraceLong;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class ExecutionTimeMaxMinTotCalculator implements SimulationListenerI {
    
    private ExecutionTimeCalculator executionTimeCalculator = null;
    private TraceGeneration traceGeneration = null;
    
    private TraceLong minExecutionTime = new TraceLong(null, Long.MAX_VALUE);
    private TraceLong maxExecutionTime = new TraceLong(null, -1);
    
    long totalExecutionTime = 0;

    public ExecutionTimeMaxMinTotCalculator(ExecutionTimeCalculator executionTimeCalculator, TraceGeneration traceGeneration) {
        this.executionTimeCalculator = executionTimeCalculator;
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
        Trace trace = traceGeneration.getUniqueEndedTrace();
        if(executionTimeCalculator.getExecutionTime() > maxExecutionTime.value){
            maxExecutionTime.value = executionTimeCalculator.getExecutionTime();
            maxExecutionTime.trace = trace;
        }
        
        if(executionTimeCalculator.getExecutionTime() < minExecutionTime.value){
            minExecutionTime.value = executionTimeCalculator.getExecutionTime();
            minExecutionTime.trace = trace;
        }
        
        totalExecutionTime += executionTimeCalculator.getExecutionTime();
    }
    
    public TraceLong getMaxExecutionTime(){ return maxExecutionTime; }
    public TraceLong getMinExecutionTime(){ return minExecutionTime; }
    public long getTotalExecutionTime(){ return totalExecutionTime; }
}
