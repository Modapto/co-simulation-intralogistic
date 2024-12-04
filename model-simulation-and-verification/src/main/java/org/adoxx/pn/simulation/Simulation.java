package org.adoxx.pn.simulation;

import java.util.ArrayList;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Trace;

import org.adoxx.pn.simulation.handlers.SimulationListenerI;
import org.adoxx.pn.simulation.handlers.TransitionSelectorI;

public class Simulation {
    
    private PetriNet petriNet = null;
    private ArrayList<SimulationListenerI> simulationListenerList = new ArrayList<SimulationListenerI>();
    private TransitionSelectorI transitionSelector = null;
    
    public Simulation(PetriNet petriNet){
        this.petriNet = petriNet;
    }
    
    public Simulation(){}
    
    public void setPetriNet(PetriNet petriNet){
        this.petriNet = petriNet;
    }

    public PetriNet getPetriNet(){
        return petriNet;
    }
    
    public void addSimulationListener(SimulationListenerI simulationListener){
        simulationListenerList.add(simulationListener);
    }
    
    public ArrayList<SimulationListenerI> getDefinedListeners(){
        return simulationListenerList;
    }
    
    public void setTransitionSelector(TransitionSelectorI transitionSelector){
        this.transitionSelector = transitionSelector;
    }

    public void singleRunEvent() throws Exception{
        if(petriNet==null)
            throw new Exception("A petriNet must be specified");
        
        if(transitionSelector==null)
            throw new Exception("A TransitionSelectorI must be specified");
        
        for(SimulationListenerI simulationListener:simulationListenerList)
            simulationListener.simulationStarted(petriNet);
        
        int[] initialMark = petriNet.getCurrentMark();
        
        ArrayList<T> transitionEnabledList = new  ArrayList<T>(0);
        while(!(transitionEnabledList = petriNet.getEnabledTransitions()).isEmpty()){
            
            T transitionToFire = transitionSelector.chooseTransition(transitionEnabledList);
            if(transitionToFire==null)
                throw new Exception("Transition to fire not defined");
            
            petriNet.fireTransition(transitionToFire);
            
            for(SimulationListenerI simulationListener:simulationListenerList)
                simulationListener.transitionFiredEvent(transitionToFire);
        }
        
        for(SimulationListenerI simulationListener:simulationListenerList)
            simulationListener.simulationEnded(petriNet);
        
        petriNet.setMark(initialMark);
    }

    
    class ParallelTraceSet{
        
    }
    
    public void testMultipleUsersRun(Trace trace, int users){
        //dividere la traccia in piu in base al numero di user?
        //o associare ad ogni nodo della traccia l'utente che lo esegue?
        //se l'utente ha finito le ore a disposizione?
        //misure live? va creato un gestore a cui è possibile associare dei metodi di misurazione che viene richiamato ogni volta che si effettua un attività
    }
    
    

    /*
    public static void main(String[] args) {
        try {
            String bpmnUrl = "D:\\LAVORO\\PROGETTI\\PNToolkit\\testModels\\test_11.bpmn";
            String bpmnModel = new String(IOUtils.readFile(bpmnUrl));
            PetriNet pn = PNImport.generateFromBPMN(XMLUtils.getXmlDocFromString(bpmnModel));
            
            Simulation sim = new Simulation();
            //Trace trace = sim.singleRun(pn);
            //trace.print();
           Trace[] traceList = new Trace[1000000];
            for(int i=0;i<1000000;i++)
                traceList[i] = (sim.singleRun(pn));
            System.out.println(traceList.length);
            Scanner s = new Scanner(System.in);
            s.nextLine();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    */
}
