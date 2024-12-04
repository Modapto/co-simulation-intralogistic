package org.adoxx.pn.simulation.handlers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;

public abstract class TransitionSelectorA implements TransitionSelectorI{

    protected class TransitionGroupInfos{
        P place = null;
        ArrayList<T> transitionGroup = null;
        public TransitionGroupInfos(P place, ArrayList<T> transitionGroup){ this.place = place; this.transitionGroup = transitionGroup; }
    }
    
    private Random random = new Random();
    protected PetriNet petriNet = null;
    
    abstract public T chooseTransition(ArrayList<T> transitionEnabledList) throws Exception;
    
    public TransitionSelectorA(PetriNet pn){
        this.petriNet = pn;
    }
    
    public TransitionSelectorA(){}
    
    public void setPetriNet(PetriNet pn){
        this.petriNet = pn;
    }
    
    //Group transitions through each place that enable them
    protected final HashMap<P, ArrayList<T>> calculateRelatedSets(ArrayList<T> transitionEnabledList){

        HashMap<P, ArrayList<T>> ret = new HashMap<P, ArrayList<T>>();
        for(T transitionEnabled:transitionEnabledList){
            for(P placeEnabler:transitionEnabled.previousList){
                if(ret.containsKey(placeEnabler)){
                    ret.get(placeEnabler).add(transitionEnabled);
                }else{
                    ArrayList<T> trList = new ArrayList<T>(1);
                    trList.add(transitionEnabled);
                    ret.put(placeEnabler, trList);
                }
            }
        }
        return ret;
    }
    
   //generate number between lowLimit and upLimit inclusive
    protected final int chooseFairPlay(int lowLimit, int upLimit){
        if(lowLimit==upLimit) 
            return upLimit;

        return random.nextInt(upLimit-lowLimit)+lowLimit;
    }
    
    protected final double chooseGaussian(double mean, double deviation){
        return random.nextGaussian()*deviation+mean;
    }
    
    protected double processProbabilityString(String pathProbabilityString) throws Exception{
        double pathProbability=0;
        if(pathProbabilityString!=null && !pathProbabilityString.isEmpty()){
            try{
                pathProbabilityString = pathProbabilityString.replace(",", ".");
                pathProbability = Double.parseDouble(pathProbabilityString);
            }catch(Exception ex){ 
                throw new Exception("The probability "+pathProbabilityString+" is not a correct Double number");
            }
        }
        return pathProbability;
    }
    
    protected final TransitionGroupInfos chooseTransitionEnabledGroup(HashMap<P, ArrayList<T>> transitionEnabledGroupList) throws Exception{
        //choice between parallel activities (fair choice)
        int choice = chooseFairPlay(0, transitionEnabledGroupList.size());
        int count=0;
        for (Entry<P, ArrayList<T>> entry : transitionEnabledGroupList.entrySet()){
            if(count==choice)
                return new TransitionGroupInfos(entry.getKey(), entry.getValue());
            count++;
        }
        throw new Exception("Impossible to get element " + choice + " from the group of size " + transitionEnabledGroupList.size());
    }
    
    protected final T chooseTransitionToFire(TransitionGroupInfos transitionGroupInfos) throws Exception{
        /*
        TODO: reduce dynamically the probability of a T when the places after the T contains arelady tokens. In this case is better to choice a T that is followed by empty places in order to distribute the load.
        */
        if(petriNet==null)
            throw new Exception("The Petri net has not been set correctly");
            
        T transitionChoosed = null;
        if(transitionGroupInfos.transitionGroup.size()==1){
            transitionChoosed = transitionGroupInfos.transitionGroup.get(0);
        }else{
            //Automatically calculate the path probability when empty
            double[] probArray = new double[transitionGroupInfos.transitionGroup.size()];
            double counterEmpty = 0;
            double counterSum = 0;
            for(int i=0;i<transitionGroupInfos.transitionGroup.size();i++){
                T transitionEnabled = transitionGroupInfos.transitionGroup.get(i);
                String pathProbabilityString = petriNet.getConnection(transitionGroupInfos.place, transitionEnabled).additionalInfoList.get("pathProbability");
                probArray[i] = processProbabilityString(pathProbabilityString);
                if(probArray[i]==0)
                    counterEmpty++;
                counterSum += probArray[i];
            }
            if(counterSum>1)
                throw new Exception("The sum of probabilities for the paths from object "+transitionGroupInfos.place.name+" can not be greather then 1");
            if(counterEmpty>0){
                double probToAssign = new BigDecimal(1).subtract(new BigDecimal(""+counterSum)).divide(new BigDecimal(""+counterEmpty), 5, RoundingMode.HALF_UP).doubleValue(); //is required to use bigdecimal either in some case return a wrong result
                for(int i=0;i<transitionGroupInfos.transitionGroup.size();i++)
                    if(probArray[i]==0)
                        probArray[i] = probToAssign;
            }

            //choice between concurrent activities
            double randomNum = (double)chooseFairPlay(1, 10001)/10000; //generate number between 0,00001 and 1 inclusive
            double pathProbabilityIncremental = 0;
            for(int i=0;i<probArray.length;i++){
                if(randomNum>pathProbabilityIncremental && randomNum<=(pathProbabilityIncremental+probArray[i]))
                    transitionChoosed = transitionGroupInfos.transitionGroup.get(i);
                pathProbabilityIncremental = pathProbabilityIncremental + probArray[i];
            }
            
            if(randomNum>pathProbabilityIncremental){//in case of periodic numbers like 0,33333 0,33333 and 0,33333 and random num 1 I need to manually choose the last transition
                transitionChoosed = transitionGroupInfos.transitionGroup.get(transitionGroupInfos.transitionGroup.size()-1);
            }
            
            if(transitionChoosed==null){
                String ex = "";
                for(int i=0;i<probArray.length;i++)
                    ex += probArray[i] + " ";
                throw new Exception("Path probability error: The choosed random do not fit the array\nRandom="+randomNum+"\nArray="+ex);
            }
        }
        
        return transitionChoosed;
    }
}
