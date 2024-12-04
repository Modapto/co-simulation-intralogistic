package org.adoxx.pn.simulation.data.measures;

import java.util.HashMap;

import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Path;

public class TotalMeasures {
    
    public HashMap<T, Measures> activitiesMeasures = new HashMap<T, Measures>();
    
    public PathDouble bestPathProbability = new PathDouble(null, -1);
    
    public TraceLong maxExecutionTime = new TraceLong(null, -1);
    public TraceLong minExecutionTime = new TraceLong(null, -1);
    public long totalExecutionTime = -1;
    
    public TraceDouble maxCosts = new TraceDouble(null, -1);
    public TraceDouble minCosts = new TraceDouble(null, -1);
    public double totalCost = -1;
    
    public TraceLong maxWaitingTime = new TraceLong(null, -1);
    public TraceLong minWaitingTime = new TraceLong(null, -1);
    
    public TraceLong maxMsgWaitingTime = new TraceLong(null, -1);
    public TraceLong minMsgWaitingTime = new TraceLong(null, -1);
    
    public TraceDouble minWaitingTimeStandardDeviation = new TraceDouble(null, -1);
    public TraceDouble minMsgWaitingTimeStandardDeviation = new TraceDouble(null, -1);
    
    public TraceDouble minWaitingTimeWithMinStandardDeviation = new TraceDouble(null, -1);
    public TraceDouble minMsgWaitingTimeWithMinStandardDeviation = new TraceDouble(null, -1);
    
    public HashMap<Path, TraceLongDouble> minWaitingTimeWithMinStandardDeviationForEveryPath = new HashMap<Path, TraceLongDouble>();
    public HashMap<Path, TraceLongDouble> minMsgWaitingTimeWithMinStandardDeviationForEveryPath = new HashMap<Path, TraceLongDouble>();
    
    public int totalRuns = -1;
    public int totalTerminatedTraces = -1;
    public int totalDeadlockedTraces = -1;
    public int totalDeadlockedPaths = -1;
    public int totalLivelockedTraces = -1;
    public int totalLivelockedPaths = -1;
    
    public long totalEnlapsedTime = -1;
    
    public double getAverageExecutionTime(){ return ((double)totalExecutionTime) / totalRuns; }
    public double getAverageCosts(){ return totalCost / totalRuns; }
    public int getNumberOfExecutionIn(long time){ return (getAverageExecutionTime()==0)?0:(int)(((double)time/1000)/((int)(getAverageExecutionTime()/1000))); } //convert also from milliseconds to seconds
}
