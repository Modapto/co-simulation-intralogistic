package org.adoxx.pn.simulation.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.adoxx.pn.simulation.data.measures.Measures;
import org.adoxx.pn.simulation.data.measures.TotalMeasures;

public class PathCollector {

    public HashMap<Path, Measures> pathMeasurementsMap = new HashMap<Path, Measures>();
    public HashMap<Path, Set<Trace>> pathTracesMap = new HashMap<Path, Set<Trace>>();
    
    public void addPath(Path path, Trace trace, Measures measures) throws Exception{
        
        Measures oldMeasures = pathMeasurementsMap.get(path);
        if(oldMeasures == null){
            pathMeasurementsMap.put(path, measures);
        } else {
            oldMeasures.numberOfExecutions++;
        }
        
        Set<Trace> oldTraceSet = pathTracesMap.get(path);
        if(oldTraceSet != null){
            oldTraceSet.add(trace);
        }else{
            HashSet<Trace> traceSet = new HashSet<Trace>();
            traceSet.add(trace);
            pathTracesMap.put(path, traceSet);
        }
    }
    
    public void updatePathProbability(TotalMeasures totalMeasures){
        for(Entry<Path, Measures> entry:pathMeasurementsMap.entrySet()){
            Measures measures = entry.getValue();
            org.adoxx.pn.simulation.data.Path path = entry.getKey();
            double probability = measures.getProbability(totalMeasures.totalRuns);
            if(probability > totalMeasures.bestPathProbability.value){
                totalMeasures.bestPathProbability.value = probability;
                totalMeasures.bestPathProbability.path = path;
            }
        }
    }
    
}
