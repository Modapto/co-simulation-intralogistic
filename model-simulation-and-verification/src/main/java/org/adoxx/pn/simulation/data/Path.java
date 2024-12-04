package org.adoxx.pn.simulation.data;

import java.util.HashMap;

import org.adoxx.pn.P;
import org.adoxx.pn.T;

public class Path {
    
    public class Counter{
        public int counter = 1;
        public Counter(){}
        public Counter(int start){counter=start;}
    }

    public long id=0;
    public HashMap<T, Counter> transitionMap = new HashMap<T, Counter>();
    public HashMap<T, Counter> transitionOrderMap = new HashMap<T, Counter>();
    
    public void addTransition(T transition){
        Counter oldCounter = transitionMap.get(transition);
        if(oldCounter!=null)
            oldCounter.counter++;
        else
            transitionMap.put(transition, new Counter());
        
        if(transitionOrderMap.get(transition)==null)
            transitionOrderMap.put(transition, new Counter(countSequenceId(transition)));
    }
    
    private int countSequenceId(T transition){
        int counter = 0;
        for(P prevPlace:transition.previousList)
            for(T prevTransition:prevPlace.previousList){
                Counter prevCounter = transitionOrderMap.get(prevTransition);
                if(prevCounter!=null)
                    if(prevCounter.counter>counter)
                        counter = prevCounter.counter;
            }
        return counter+1;
    }
    
    public boolean pathEquals(Path path){
        if(!this.transitionMap.keySet().equals(path.transitionMap.keySet()))
            return false;
        for(T key:this.transitionMap.keySet())
            if(path.transitionMap.get(key).counter!=this.transitionMap.get(key).counter)
                return false;
        return true;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Path))
            return o==this;
        
        Path path = (Path)o;
        return pathEquals(path);
    }
    
    @Override
    public int hashCode(){
        int hash = 0;
        for(T key:this.transitionMap.keySet()){
            hash = (hash) ^ key.hashCode();
        }
        return hash;
    }
}
