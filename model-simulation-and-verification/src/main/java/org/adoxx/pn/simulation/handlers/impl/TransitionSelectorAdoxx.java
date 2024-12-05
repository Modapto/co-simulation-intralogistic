package org.adoxx.pn.simulation.handlers.impl;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.adoxx.pn.PetriNet;

public class TransitionSelectorAdoxx extends TransitionSelectorDefault {

    private ScriptEngine scriptEngine = null;
    
    public TransitionSelectorAdoxx(PetriNet pn) {
        super(pn);
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        scriptEngine.put("petrinet", pn);
        //TODO: pass other variables to the scriptengine?
        //idea: create an attribute collector class that contain for each object id/name of the adoxx/bpmn, all its attributes with value and pass that as paramenter 
    }
    
    public TransitionSelectorAdoxx() {
        super();
        //scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        scriptEngine = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory().getScriptEngine();

    }
    
    @Override
    public void setPetriNet(PetriNet pn){
        super.setPetriNet(pn);
        scriptEngine.put("petrinet", pn);
    }

    @Override
    protected double processProbabilityString(String pathProbabilityString) throws Exception{
        if(pathProbabilityString!=null && pathProbabilityString.contains("return")){
            double pathProbability=0;
            String script = "function probability() { " + pathProbabilityString + " }";
            try{
                scriptEngine.eval(script);
            } catch(Exception ex) {
                throw new Exception("The provided script contains some errors!\n"+ex.getMessage());
            }
            Object probObj = ((Invocable) scriptEngine).invokeFunction("probability");
            
            if(probObj instanceof Double){
                pathProbability = (Double) probObj;
            } else if(probObj instanceof String){
                pathProbability = Double.parseDouble((String) probObj);
            } else {
                throw new Exception("The script returned an unrecognized object;\nScript:\n" + pathProbabilityString + "\nReturned Type: "+probObj.getClass()+"\nReturned Val: "+probObj.toString());
            }
            
            return pathProbability;
        } else {
            return super.processProbabilityString(pathProbabilityString);
        }
    }
    
    /*
    public static void main(String[] args){
        try {    
            //ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
            ScriptEngine engine = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory().getScriptEngine();
            
            Boolean isOk = true;
            engine.put("ok", isOk);
            String script = "function probability() { if(ok){ return Math.random();} else { return ok;} }";
            engine.eval(script);
            Object valObj = ((Invocable) engine).invokeFunction("probability");
            double pathProbability=0;
            if(valObj instanceof Double){
                pathProbability = (Double) valObj;
            } else if(valObj instanceof String){
                pathProbability = Double.parseDouble((String) valObj);
            } else {
                throw new Exception("The script returned an unrecognized object;\nScript: " + script + "\nReturned Type: "+valObj.getClass()+"\nReturned Val: "+valObj.toString());
            }
            System.out.println(pathProbability);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
