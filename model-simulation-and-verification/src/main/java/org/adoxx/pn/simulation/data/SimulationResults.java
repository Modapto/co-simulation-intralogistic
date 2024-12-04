package org.adoxx.pn.simulation.data;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.measures.Measures;
import org.adoxx.pn.simulation.data.measures.TotalMeasures;
import org.adoxx.pn.simulation.data.measures.TraceLongDouble;
import org.adoxx.utils.Utils;
import org.adoxx.utils.XMLUtils;

public class SimulationResults {

    public String model = "";
    public PetriNet petriNet = null;
    public PathCollector pathCollector = null;
    public TraceCollector traceCollector = null;
    public TotalMeasures totalMeasures = null;
    
    public String generateSimulationResultsXML() throws Exception{
        if(petriNet == null || pathCollector == null || traceCollector == null || totalMeasures == null)
            throw new Exception("Simulation results not ready");
            
        long oneDay = Utils.convertAdoxxDateTimeToMilliseconds("00:000:08:00:00");

        String ret = "<PathAnalysis modelId=\""+petriNet.getName()+"\" modelName=\""+XMLUtils.escapeXMLField(petriNet.additionalInfoList.get("model_name"))+"\">";
        
        ret += "<GeneralMeasures>";
        ret += "<EnlapsedTime>"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.totalEnlapsedTime)+"</EnlapsedTime>";
        
        ret += "<AverageCosts>"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.getAverageCosts())+"</AverageCosts>";
        ret += "<MaxCosts traceId=\"t."+totalMeasures.maxCosts.trace.id+"\">"+totalMeasures.maxCosts.value+"</MaxCosts>";
        ret += "<MinCosts traceId=\"t."+totalMeasures.minCosts.trace.id+"\">"+totalMeasures.minCosts.value+"</MinCosts>";
        ret += "<TotalCosts>"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.totalCost)+"</TotalCosts>";
        
        ret += "<AverageExecutionsInOneDay>"+totalMeasures.getNumberOfExecutionIn(oneDay)+"</AverageExecutionsInOneDay>";
        
        ret += "<AverageExecutionTime>"+Utils.convertoMillisecondsToStringDateTime((int)totalMeasures.getAverageExecutionTime())+"</AverageExecutionTime>";
        ret += "<MaxExecutionTime traceId=\"t."+totalMeasures.maxExecutionTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxExecutionTime.value)+"</MaxExecutionTime>";
        ret += "<MinExecutionTime traceId=\"t."+totalMeasures.minExecutionTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minExecutionTime.value)+"</MinExecutionTime>";
        ret += "<TotalExecutionTime>"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.totalExecutionTime)+"</TotalExecutionTime>";
        
        ret += "<MaxWaitingTime traceId=\"t."+totalMeasures.maxWaitingTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxWaitingTime.value)+"</MaxWaitingTime>";
        ret += "<MinWaitingTime traceId=\"t."+totalMeasures.minWaitingTimeWithMinStandardDeviation.trace.id+"\" standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minWaitingTimeWithMinStandardDeviation.value)+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minWaitingTime.value)+"</MinWaitingTime>";
        if(totalMeasures.maxMsgWaitingTime.trace!=null){
            ret += "<MaxMessageWaitingTime traceId=\"t."+totalMeasures.maxMsgWaitingTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxMsgWaitingTime.value)+"</MaxMessageWaitingTime>";
            ret += "<MinMessageWaitingTime traceId=\"t."+totalMeasures.minMsgWaitingTimeWithMinStandardDeviation.trace.id+"\" standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minMsgWaitingTimeWithMinStandardDeviation.value)+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minMsgWaitingTime.value)+"</MinMessageWaitingTime>";
        }else{
            ret += "<MaxMessageWaitingTime traceId=\"\"></MaxMessageWaitingTime>";
            ret += "<MinMessageWaitingTime traceId=\"\" standardDeviation=\"\"></MinMessageWaitingTime>"; 
        }
        ret += "<MostProbablePath pathId=\"p."+totalMeasures.bestPathProbability.path.id+"\">"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.bestPathProbability.value)+"</MostProbablePath>";
        
        ret += "<TotalEndTerminateEvents>"+totalMeasures.totalTerminatedTraces+"</TotalEndTerminateEvents>";
        ret += "<TotalDeadlockedTraces>"+totalMeasures.totalDeadlockedTraces+"</TotalDeadlockedTraces>";
        ret += "<TotalDeadlockedPaths>"+totalMeasures.totalDeadlockedPaths+"</TotalDeadlockedPaths>";
        ret += "<TotalLivelockedTraces>"+totalMeasures.totalLivelockedTraces+"</TotalLivelockedTraces>";
        ret += "<TotalLivelockedPaths>"+totalMeasures.totalLivelockedPaths+"</TotalLivelockedPaths>";
        
        ret += "<TotalRuns>"+totalMeasures.totalRuns+"</TotalRuns>";
        ret += "<TotalTraces>"+traceCollector.traceMeasurementsMap.size()+"</TotalTraces>";
        ret += "<TotalPaths>"+pathCollector.pathMeasurementsMap.size()+"</TotalPaths>";
        ret += "</GeneralMeasures>";
        
        ret += "<ObjectMeasures>";
        for(Entry<T, Measures> entryActivitiesMeasures : totalMeasures.activitiesMeasures.entrySet()){
            ret += "<Object id=\""+entryActivitiesMeasures.getKey().description+"\" "
                    + "name=\""+XMLUtils.escapeXMLField(entryActivitiesMeasures.getKey().additionalInfoList.get("name"))+"\" "
                    + "numberOfExecutions=\""+entryActivitiesMeasures.getValue().numberOfExecutions+"\" "
                    + "costs=\""+entryActivitiesMeasures.getValue().costs+"\" "
                    + "executionTime=\""+Utils.convertoMillisecondsToStringDateTime(entryActivitiesMeasures.getValue().executionTime)+"\" "
                    + "totCosts=\""+(entryActivitiesMeasures.getValue().costs*entryActivitiesMeasures.getValue().numberOfExecutions)+"\" "
                    + "totExecutionTime=\""+Utils.convertoMillisecondsToStringDateTime(entryActivitiesMeasures.getValue().executionTime*entryActivitiesMeasures.getValue().numberOfExecutions)+"\" />";
        }
        ret += "</ObjectMeasures>";
        
        ret += "<Paths>";
        for(Entry<Path, Measures> entryPathMeasures:pathCollector.pathMeasurementsMap.entrySet()){
            ret += "<Path id=\"p."+entryPathMeasures.getKey().id+"\" "
                    + "numberOfExecutions=\""+entryPathMeasures.getValue().numberOfExecutions+"\" "
                    + "pathProbability=\""+String.format(Locale.ENGLISH, "%.2f", entryPathMeasures.getValue().getProbability(totalMeasures.totalRuns))+"\" "
                    + "numberOfTraces=\""+pathCollector.pathTracesMap.get(entryPathMeasures.getKey()).size()+"\" "
                    + "deadlocked=\""+entryPathMeasures.getValue().deadlockHappens+"\" "
                    + "livelocked=\""+entryPathMeasures.getValue().livelockHappens+"\" "
                    + "terminateEvent=\""+entryPathMeasures.getValue().terminateEventHappens+"\" "
                    + "minWaitingTime=\""+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueLong)+"\" "
                    + "minWaitingTimeSD=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueDouble)+"\" "
                    + "minWaitingTimeTraceId=\"t."+totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).trace.id+"\" "
                    + ((totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey())!=null)?(
                        "minMsgWaitingTime=\""+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueLong)+"\" "
                        + "minMsgWaitingTimeSD=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueDouble)+"\" "
                        + "minMsgWaitingTimeTraceId=\"t."+totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).trace.id+"\" "
                    ):(
                        "minMsgWaitingTime=\"\" "
                        + "minMsgWaitingTimeSD=\"\" "
                        + "minMsgWaitingTimeTraceId=\"\" "
                    ))
                    + ">";
            ret += "<Set>";
            for(Entry<T, org.adoxx.pn.simulation.data.Path.Counter> entry:entryPathMeasures.getKey().transitionMap.entrySet()){
                //ret += "<Object numExecutions=\""+entry.getValue().counter+"\">"+entry.getKey().description+"</Object>";
                ret += "<Object numExecutions=\""+entry.getValue().counter+"\" step=\""+entryPathMeasures.getKey().transitionOrderMap.get(entry.getKey()).counter+"\">"+entry.getKey().description+"</Object>";
            }
            ret += "</Set>";
            ret += "</Path>";
        }
        ret += "</Paths>";
        
        ret += "<Traces>";
        for(Entry<org.adoxx.pn.simulation.data.Trace, Measures> entryTraceMeasures:traceCollector.traceMeasurementsMap.entrySet()){
            ret += "<Trace id=\"t."+entryTraceMeasures.getKey().id+"\" pathId=\"p."+entryTraceMeasures.getKey().path.id+"\" numberOfExecutions=\""+entryTraceMeasures.getValue().numberOfExecutions+"\" executionTime=\""+Utils.convertoMillisecondsToStringDateTime(entryTraceMeasures.getValue().executionTime)+"\" costs=\""+entryTraceMeasures.getValue().costs+"\" traceProbability=\""+String.format(Locale.ENGLISH, "%.2f", entryTraceMeasures.getValue().getProbability(totalMeasures.totalRuns))+"\">";
            ret += "<Steps>";
            org.adoxx.pn.simulation.data.Trace.TraceNode currentTraceNode = entryTraceMeasures.getKey().startingNode;
            while(currentTraceNode!=null){
                String objId = currentTraceNode.executedTransition.description;
                ret += "<Object>"+objId+"</Object>";
                currentTraceNode = currentTraceNode.nextNode;
            }
            ret += "</Steps>";

            ret += "<WaitingTimes total=\""+Utils.convertoMillisecondsToStringDateTime(entryTraceMeasures.getValue().totalWaitingTime)+"\" totalOnMessages=\""+Utils.convertoMillisecondsToStringDateTime(entryTraceMeasures.getValue().totalMsgWaitingTime)+"\" average=\""+Utils.convertoMillisecondsToStringDateTime((long)entryTraceMeasures.getValue().averageWaitingTime)+"\" averageOnMessages=\""+Utils.convertoMillisecondsToStringDateTime((long)entryTraceMeasures.getValue().averageMsgWaitingTime)+"\" standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)entryTraceMeasures.getValue().standardDeviationWaitingTime)+"\" standardDeviationOnMessages=\""+Utils.convertoMillisecondsToStringDateTime((long)entryTraceMeasures.getValue().standardDeviationMsgWaitingTime)+"\">";
            for(Entry<P, org.adoxx.pn.simulation.data.measures.WaitingTime> entrySet:entryTraceMeasures.getValue().waitingTimeMap.entrySet())
                if(entrySet.getValue().getWaitingTime()>0)
                    ret += "<Object objId=\""+entrySet.getKey().description+"\">"+Utils.convertoMillisecondsToStringDateTime(entrySet.getValue().getWaitingTime())+"</Object>";
            ret += "</WaitingTimes>";
            ret += "</Trace>";
        }
        ret += "</Traces>";
        
        ret += "</PathAnalysis>";
        return ret;
    }
    
    public String generateSimulationResultsXMLSlim() throws Exception{
        if(petriNet == null || pathCollector == null || traceCollector == null || totalMeasures == null)
            throw new Exception("Simulation results not ready");
        
        long oneDay = Utils.convertAdoxxDateTimeToMilliseconds("00:000:08:00:00");

        HashSet<Trace> importantTraceList = new HashSet<Trace>();
        importantTraceList.add(totalMeasures.maxCosts.trace);
        importantTraceList.add(totalMeasures.minCosts.trace);
        importantTraceList.add(totalMeasures.maxExecutionTime.trace);
        importantTraceList.add(totalMeasures.minExecutionTime.trace);
        importantTraceList.add(totalMeasures.maxWaitingTime.trace);
        importantTraceList.add(totalMeasures.minWaitingTimeWithMinStandardDeviation.trace);
        if(totalMeasures.maxMsgWaitingTime.trace!=null){
            importantTraceList.add(totalMeasures.maxMsgWaitingTime.trace);
            importantTraceList.add(totalMeasures.minMsgWaitingTimeWithMinStandardDeviation.trace);
        }
        for(Entry<Path, TraceLongDouble> entry: totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.entrySet()){
            importantTraceList.add(entry.getValue().trace);
        }
        for(Entry<Path, TraceLongDouble> entry: totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.entrySet()){
            importantTraceList.add(entry.getValue().trace);
        }
        
        
        String ret = "<PathAnalysis modelId=\""+petriNet.getName()+"\" modelName=\""+XMLUtils.escapeXMLField(petriNet.additionalInfoList.get("model_name"))+"\">";
        
        ret += "<GeneralMeasures>";
        ret += "<EnlapsedTime>"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.totalEnlapsedTime)+"</EnlapsedTime>";
        
        ret += "<AverageCosts>"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.getAverageCosts())+"</AverageCosts>";
        ret += "<MaxCosts traceId=\"t."+totalMeasures.maxCosts.trace.id+"\">"+totalMeasures.maxCosts.value+"</MaxCosts>";
        ret += "<MinCosts traceId=\"t."+totalMeasures.minCosts.trace.id+"\">"+totalMeasures.minCosts.value+"</MinCosts>";
        ret += "<TotalCosts>"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.totalCost)+"</TotalCosts>";
        
        ret += "<AverageExecutionsInOneDay>"+totalMeasures.getNumberOfExecutionIn(oneDay)+"</AverageExecutionsInOneDay>";
        
        ret += "<AverageExecutionTime>"+Utils.convertoMillisecondsToStringDateTime((int)totalMeasures.getAverageExecutionTime())+"</AverageExecutionTime>";
        ret += "<MaxExecutionTime traceId=\"t."+totalMeasures.maxExecutionTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxExecutionTime.value)+"</MaxExecutionTime>";
        ret += "<MinExecutionTime traceId=\"t."+totalMeasures.minExecutionTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minExecutionTime.value)+"</MinExecutionTime>";
        ret += "<TotalExecutionTime>"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.totalExecutionTime)+"</TotalExecutionTime>";
        
        ret += "<MaxWaitingTime traceId=\"t."+totalMeasures.maxWaitingTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxWaitingTime.value)+"</MaxWaitingTime>";
        ret += "<MinWaitingTime traceId=\"t."+totalMeasures.minWaitingTimeWithMinStandardDeviation.trace.id+"\" standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minWaitingTimeWithMinStandardDeviation.value)+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minWaitingTime.value)+"</MinWaitingTime>";
        if(totalMeasures.maxMsgWaitingTime.trace!=null){
            ret += "<MaxMessageWaitingTime traceId=\"t."+totalMeasures.maxMsgWaitingTime.trace.id+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.maxMsgWaitingTime.value)+"</MaxMessageWaitingTime>";
            ret += "<MinMessageWaitingTime traceId=\"t."+totalMeasures.minMsgWaitingTimeWithMinStandardDeviation.trace.id+"\" standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minMsgWaitingTimeWithMinStandardDeviation.value)+"\">"+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minMsgWaitingTime.value)+"</MinMessageWaitingTime>";
        }else{
            ret += "<MaxMessageWaitingTime traceId=\"\"></MaxMessageWaitingTime>";
            ret += "<MinMessageWaitingTime traceId=\"\" standardDeviation=\"\"></MinMessageWaitingTime>"; 
        }
        ret += "<MostProbablePath pathId=\"p."+totalMeasures.bestPathProbability.path.id+"\">"+String.format(Locale.ENGLISH, "%.2f", totalMeasures.bestPathProbability.value)+"</MostProbablePath>";
        
        ret += "<TotalEndTerminateEvents>"+totalMeasures.totalTerminatedTraces+"</TotalEndTerminateEvents>";
        ret += "<TotalDeadlockedTraces>"+totalMeasures.totalDeadlockedTraces+"</TotalDeadlockedTraces>";
        ret += "<TotalDeadlockedPaths>"+totalMeasures.totalDeadlockedPaths+"</TotalDeadlockedPaths>";
        ret += "<TotalLivelockedTraces>"+totalMeasures.totalLivelockedTraces+"</TotalLivelockedTraces>";
        ret += "<TotalLivelockedPaths>"+totalMeasures.totalLivelockedPaths+"</TotalLivelockedPaths>";

        ret += "<TotalRuns>"+totalMeasures.totalRuns+"</TotalRuns>";
        ret += "<TotalTraces>"+traceCollector.traceMeasurementsMap.size()+"</TotalTraces>";
        ret += "<TotalPaths>"+pathCollector.pathMeasurementsMap.size()+"</TotalPaths>";
        ret += "</GeneralMeasures>";
        
        
        ret += "<ObjectMeasures>";
        for(Entry<T, Measures> entryActivitiesMeasures : totalMeasures.activitiesMeasures.entrySet()){
            ret += "<Object id=\""+entryActivitiesMeasures.getKey().description+"\" "
                    + "name=\""+XMLUtils.escapeXMLField(entryActivitiesMeasures.getKey().additionalInfoList.get("name"))+"\" "
                    + "numberOfExecutions=\""+entryActivitiesMeasures.getValue().numberOfExecutions+"\" "
                    + "costs=\""+entryActivitiesMeasures.getValue().costs+"\" "
                    + "executionTime=\""+Utils.convertoMillisecondsToStringDateTime(entryActivitiesMeasures.getValue().executionTime)+"\" "
                    + "totCosts=\""+(entryActivitiesMeasures.getValue().costs*entryActivitiesMeasures.getValue().numberOfExecutions)+"\" "
                    + "totExecutionTime=\""+Utils.convertoMillisecondsToStringDateTime(entryActivitiesMeasures.getValue().executionTime*entryActivitiesMeasures.getValue().numberOfExecutions)+"\" />";
        }
        ret += "</ObjectMeasures>";
        
        
        ret += "<Paths>";
        for(Entry<Path, Measures> entryPathMeasures:pathCollector.pathMeasurementsMap.entrySet()){
            ret += "<Path id=\"p."+entryPathMeasures.getKey().id+"\" "
                    + "numberOfExecutions=\""+entryPathMeasures.getValue().numberOfExecutions+"\" "
                    + "pathProbability=\""+String.format(Locale.ENGLISH, "%.2f", entryPathMeasures.getValue().getProbability(totalMeasures.totalRuns))+"\" "
                    + "numberOfTraces=\""+pathCollector.pathTracesMap.get(entryPathMeasures.getKey()).size()+"\" "
                    + "deadlocked=\""+entryPathMeasures.getValue().deadlockHappens+"\" "
                    + "livelocked=\""+entryPathMeasures.getValue().livelockHappens+"\" "
                    + "terminateEvent=\""+entryPathMeasures.getValue().terminateEventHappens+"\" "
                    + "minWaitingTime=\""+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueLong)+"\" "
                    + "minWaitingTimeSD=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueDouble)+"\" "
                    + "minWaitingTimeTraceId=\"t."+totalMeasures.minWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).trace.id+"\" "
                    + ((totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey())!=null)?(
                        "minMsgWaitingTime=\""+Utils.convertoMillisecondsToStringDateTime(totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueLong)+"\" "
                        + "minMsgWaitingTimeSD=\""+Utils.convertoMillisecondsToStringDateTime((long)totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).valueDouble)+"\" "
                        + "minMsgWaitingTimeTraceId=\"t."+totalMeasures.minMsgWaitingTimeWithMinStandardDeviationForEveryPath.get(entryPathMeasures.getKey()).trace.id+"\" "
                    ):(
                        "minMsgWaitingTime=\"\" "
                        + "minMsgWaitingTimeSD=\"\" "
                        + "minMsgWaitingTimeTraceId=\"\" "
                    ))
                    + ">";
            ret += "<Set>";
            for(Entry<T, org.adoxx.pn.simulation.data.Path.Counter> entry:entryPathMeasures.getKey().transitionMap.entrySet()){
                //ret += "<Object numExecutions=\""+entry.getValue().counter+"\">"+entry.getKey().description+"</Object>";
                ret += "<Object numExecutions=\""+entry.getValue().counter+"\" step=\""+entryPathMeasures.getKey().transitionOrderMap.get(entry.getKey()).counter+"\">"+entry.getKey().description+"</Object>";
            }
            ret += "</Set>";
            ret += "</Path>";
        }
        ret += "</Paths>";
        
        
        ret += "<Traces>";
        for(Trace importantTrace: importantTraceList){
            Measures traceMeasure = traceCollector.traceMeasurementsMap.get(importantTrace);
            ret += "<Trace id=\"t."+importantTrace.id+"\" "
                    + "pathId=\"p."+importantTrace.path.id+"\" "
                    + "numberOfExecutions=\""+traceMeasure.numberOfExecutions+"\" "
                    + "executionTime=\""+Utils.convertoMillisecondsToStringDateTime(traceMeasure.executionTime)+"\" "
                    + "costs=\""+traceMeasure.costs+"\" "
                    + "traceProbability=\""+String.format(Locale.ENGLISH, "%.2f", traceMeasure.getProbability(totalMeasures.totalRuns))+"\">";
            ret += "<Steps>";
            org.adoxx.pn.simulation.data.Trace.TraceNode currentTraceNode = importantTrace.startingNode;
            while(currentTraceNode!=null){
                String objId = currentTraceNode.executedTransition.description;
                ret += "<Object>"+objId+"</Object>";
                currentTraceNode = currentTraceNode.nextNode;
            }
            ret += "</Steps>";
            ret += "<WaitingTimes total=\""+Utils.convertoMillisecondsToStringDateTime(traceMeasure.totalWaitingTime)+"\" "
                    + "totalOnMessages=\""+Utils.convertoMillisecondsToStringDateTime(traceMeasure.totalMsgWaitingTime)+"\" "
                    + "average=\""+Utils.convertoMillisecondsToStringDateTime((long)traceMeasure.averageWaitingTime)+"\" "
                    + "averageOnMessages=\""+Utils.convertoMillisecondsToStringDateTime((long)traceMeasure.averageMsgWaitingTime)+"\" "
                    + "standardDeviation=\""+Utils.convertoMillisecondsToStringDateTime((long)traceMeasure.standardDeviationWaitingTime)+"\" "
                    + "standardDeviationOnMessages=\""+Utils.convertoMillisecondsToStringDateTime((long)traceMeasure.standardDeviationMsgWaitingTime)+"\">";
            for(Entry<P, org.adoxx.pn.simulation.data.measures.WaitingTime> entrySet:traceMeasure.waitingTimeMap.entrySet())
                if(entrySet.getValue().getWaitingTime()>0)
                    ret += "<Object objId=\""+entrySet.getKey().description+"\">"+Utils.convertoMillisecondsToStringDateTime(entrySet.getValue().getWaitingTime())+"</Object>";
            ret += "</WaitingTimes>";
            ret += "</Trace>";
        }
        ret += "</Traces>";
        

        ret += "</PathAnalysis>";
        return ret;
    }
}
