package org.adoxx.pn.simulation.data;

import java.util.HashMap;
import org.adoxx.pn.simulation.data.measures.Measures;

public class TraceCollector {

    public HashMap<Trace, Measures> traceMeasurementsMap = new HashMap<Trace, Measures>();
    
    public void addTrace(Trace trace, Measures measures) throws Exception{
        
        Measures oldMeasures = traceMeasurementsMap.get(trace);
        if(oldMeasures == null){
            traceMeasurementsMap.put(trace, measures);
        } else {
            oldMeasures.numberOfExecutions++;
        }
    }
}
