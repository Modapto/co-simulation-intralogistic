package org.adoxx.pn.simulation.data.measures;

import org.adoxx.pn.simulation.data.Trace;

public class TraceLongDouble {

    public TraceLongDouble(Trace trace, long valueLong, double valueDouble){ this.trace=trace; this.valueLong=valueLong; this.valueDouble = valueDouble; }
    public Trace trace = null;
    public long valueLong = -1;
    public double valueDouble = -1;
}
