package org.adoxx.pn.simulation.data.measures;

import java.util.HashMap;

import org.adoxx.pn.P;

public class Measures {

    public int numberOfExecutions = 1;
    public long executionTime = 0;
    public double costs = 0;
    public HashMap<P, WaitingTime> waitingTimeMap = new HashMap<P, WaitingTime>();
    public long totalWaitingTime = 0;
    public long totalMsgWaitingTime = 0;
    public double averageWaitingTime = 0;
    public double averageMsgWaitingTime = 0;
    public double standardDeviationWaitingTime = 0;
    public double standardDeviationMsgWaitingTime = 0;
    
    public boolean terminateEventHappens = false;
    public boolean deadlockHappens = false;
    public boolean livelockHappens = false;
    
    public double getProbability(double total){ return ((double)numberOfExecutions / total) * 100; }
    
    //FIXME: spostare le misure di deviazionestandard ecc qui?
}
