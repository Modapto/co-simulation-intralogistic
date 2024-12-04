package org.adoxx.pn.simulation.data.measures;

public class WaitingTime {
    public long arrivalTime = 0;
    public long executionTime = 0;
    public WaitingTime(long arrivalTime){this.arrivalTime = arrivalTime;}
    public long getWaitingTime(){return executionTime - arrivalTime;}
}
