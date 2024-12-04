package org.adoxx.pn.simulation.data;

import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.T;

public class Trace {

    public class TraceNode{
        public TraceNode previousNode = null;
        public TraceNode nextNode = null;
        public T executedTransition = null;
        public TraceNode(T transition){executedTransition = transition;}
        public ArrayList<P> getConsumedPlaces(){ return executedTransition.previousList; }
        public ArrayList<P> getFilledPlaces(){ return executedTransition.nextList; }
    }
    
    public TraceNode startingNode = null;
    public TraceNode lastNode = null;
    public Path path = null;
    public long id=0;
    
    public TraceNode addTransition(T transition){
        if(startingNode==null){
            startingNode = new TraceNode(transition);
            return startingNode;
        }
        TraceNode previousLastNode = lastNode;
        if(previousLastNode==null)
            previousLastNode = startingNode;
        lastNode = new TraceNode(transition);
        lastNode.previousNode = previousLastNode;
        previousLastNode.nextNode = lastNode;
        return lastNode;
    }
    
    public boolean stepEquals(Trace trace){
        TraceNode currentNode = startingNode;
        TraceNode otherNode = trace.startingNode;
        while(currentNode!=null){
            if(otherNode==null)
                return false;
            if(!currentNode.executedTransition.equals(otherNode.executedTransition))
                return false;
            currentNode = currentNode.nextNode;
            otherNode = otherNode.nextNode;
        }
        if(currentNode==null && otherNode!=null)
            return false;
        return true;
    }
    

    @Override
    public int hashCode(){
        int hash = 0;
        TraceNode currentNode = startingNode;
        if(currentNode!=null){
            hash = currentNode.executedTransition.hashCode();
            currentNode = currentNode.nextNode;
        }
        while(currentNode!=null){
            hash = (hash) ^ currentNode.executedTransition.hashCode();
            currentNode = currentNode.nextNode;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Trace))
            return o==this;
        
        Trace trace = (Trace)o;
        return stepEquals(trace);
    }
    
    @Override
    public String toString(){
        TraceNode currentNode = startingNode;
        String ret = "";
        while(currentNode!=null){
            ret += currentNode.executedTransition.name + "->";
            currentNode = currentNode.nextNode;
        }
        ret = ret.substring(0, ret.length()-2);
        return ret;
    }
}
