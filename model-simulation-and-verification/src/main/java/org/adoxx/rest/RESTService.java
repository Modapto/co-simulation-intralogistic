package org.adoxx.rest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.adoxx.bpmn.BPUtils;
import org.adoxx.bpmn.SimulationEngine;
import org.adoxx.bpmn.VerificationEngine;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.input.ImporterManager;
import org.adoxx.pn.output.ExporterPNML;
import org.adoxx.utils.Utils;
import org.adoxx.utils.XMLUtils;
import org.glassfish.jersey.server.JSONP;

@Path("")
public class RESTService {
    
    @GET
    @Path("/utils/supportedmodeltypes")
    @Produces(MediaType.APPLICATION_XML)
    public String getSupportedModelTypes(){
        try{
            ArrayList<String> nameList = new SimulationEngine().getImporterManager().getAllImporterNames();
            String ret = "<Importers>";
            for(String name:nameList)
                ret += "<Importer>"+name+"</Importer>";
            ret += "</Importers>";
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/utils/showpetrinet")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String showPetriNet(String model){
        try{
            String ret = "";
            PetriNet[] petriNetList = new ImporterManager().generateFromModel(model);
            for(PetriNet petriNet:petriNetList)
                ret += ExporterPNML.exportTo_PNML(petriNet) + "\n\n";
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/utils/getmodellist")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String getObjectList(String model){
        try{
            String ret = "<Models>";
            PetriNet[] petriNetList = new ImporterManager().generateFromModel(model);
            for(PetriNet petriNet:petriNetList){
                ret += "<Model id=\"" + XMLUtils.escapeXMLField(petriNet.getName()) +"\" name=\"" + XMLUtils.escapeXMLField(petriNet.additionalInfoList.get("model_name")) + "\">";
                HashMap<String, String> objectIdNameList = BPUtils.getBPMNObjects(petriNet);
                for(Entry<String, String> entry: objectIdNameList.entrySet())
                    ret += "<Object id=\"" + XMLUtils.escapeXMLField(entry.getKey()) + "\" name=\"" + XMLUtils.escapeXMLField(entry.getValue()) + "\"/>";
                ret += "</Model>";
            }
            ret += "</Models>";
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/simulator/pathanalysis")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String startPathAnalysis(String model, @QueryParam("numExecutions") @DefaultValue("1000") int numExecutions, @QueryParam("fullResults") @DefaultValue("true") boolean fullResults){
        try{
            SimulationEngine simulationEngine = new SimulationEngine();
            simulationEngine.executePathAnalysis(model, numExecutions);
            String simRes = simulationEngine.getXMLResults(fullResults);
            return simRes;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/verificator/deadlock")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String startDeadlockVerification(String model, @QueryParam("checkAllDeadlock") @DefaultValue("true") boolean checkAllDeadlock){
        try{
            VerificationEngine verificationEngine = new VerificationEngine();
            String ret = verificationEngine.verifyDeadlock(model, checkAllDeadlock);
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/verificator/unboundness")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String startUnboundnessVerification(String model, @QueryParam("checkAllUnboundedness") @DefaultValue("false") boolean checkAllUnboundedness){
        try{
            VerificationEngine verificationEngine = new VerificationEngine();
            String ret = verificationEngine.verifyUnboundedness(model, checkAllUnboundedness);
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/verificator/reachability")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String startReachabilityVerification(String model, @QueryParam("bpObjectId") String bpObjectId, @QueryParam("inAnyCase") @DefaultValue("false") boolean inAnyCase, @QueryParam("negate") @DefaultValue("false") boolean negate){
        try{
            VerificationEngine verificationEngine = new VerificationEngine();
            String ret = verificationEngine.verifyObjectReachability(model, bpObjectId, inAnyCase, negate);
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }
    
    @POST
    @Path("/verificator/path")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public String startPathExistenceVerification(String model, @QueryParam("bpFromObjectId") String bpFromObjectId, @QueryParam("bpToObjectId") String bpToObjectId, @QueryParam("inAnyCase") @DefaultValue("false") boolean inAnyCase, @QueryParam("negateFrom") @DefaultValue("false") boolean negateFrom, @QueryParam("negateTo") @DefaultValue("false") boolean negateTo){
        try{
            VerificationEngine verificationEngine = new VerificationEngine();
            String ret = verificationEngine.verifyPathExistence(model, bpFromObjectId, bpToObjectId, inAnyCase, negateFrom, negateTo);
            return ret;
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return "<ERROR>"+XMLUtils.escapeXMLField(ex.getMessage())+"</ERROR>";
        }
    }


    @POST
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String combinedJson(String jsonData){
        /* jsonData sample:
         *   {
         *      "alg": "simulation / deadlock / unboundness / reachability / path",
         *      "params": "queryString parameters like numExecutions=100&fullResults=false"
         *      "modelB64": "..."
         *   }
         */
        try{
            JsonObject data = Json.createReader(new StringReader(jsonData)).readObject();
            String alg = data.getString("alg");
            String params = data.getString("params");
            String modelB64 = data.getString("modelB64");
            String model = new String (Base64.getDecoder().decode(modelB64), "UTF-8");
            Map<String,String> paramsMap = Utils.parseQueryString(params);
            
            String results = "";
            if (alg.equals("simulation")) {
                int numExecutions = Integer.parseInt(Utils.checkDefault(paramsMap.get("numExecutions"), "100"));
                boolean fullResults = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("fullResults"), "false"));
                
                results = startPathAnalysis(model, numExecutions, fullResults);
            } else if (alg.equals("deadlock")) {
                boolean checkAllDeadlock = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("checkAllDeadlock"), "false"));

                results = startDeadlockVerification(model, checkAllDeadlock);
            } else if (alg.equals("unboundness")) {
                boolean checkAllUnboundedness = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("checkAllUnboundedness"), "false"));
                
                results = startUnboundnessVerification(model, checkAllUnboundedness);
            } else if (alg.equals("reachability")) {
                String bpObjectId = Utils.checkEmpty(paramsMap.get("bpObjectId"), "Missing param bpObjectId");
                boolean inAnyCase = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("inAnyCase"), "false"));
                boolean negate = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("negate"), "false"));

                results = startReachabilityVerification(model, bpObjectId, inAnyCase, negate);
            } else if (alg.equals("path")) {
                String bpFromObjectId = Utils.checkEmpty(paramsMap.get("bpFromObjectId"), "Missing param bpFromObjectId");
                String bpToObjectId = Utils.checkEmpty(paramsMap.get("bpToObjectId"), "Missing param bpToObjectId");
                boolean inAnyCase = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("inAnyCase"), "false"));
                boolean negateFrom = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("negateFrom"), "false"));
                boolean negateTo = Boolean.parseBoolean(Utils.checkDefault(paramsMap.get("negateTo"), "false"));

                results = startPathExistenceVerification(model, bpFromObjectId, bpToObjectId, inAnyCase, negateFrom, negateTo);
            } else if (alg.equals("getmodellist")) {
                results = getObjectList(model);
            } else if (alg.equals("showpetrinet")) {
                results = showPetriNet(model);
            } else if (alg.equals("supportedmodeltypes")) {
                results = getSupportedModelTypes();
            } else {
                throw new Exception("Invalid alg value provided: " + alg + ". Supported are 'simulation', 'deadlock', 'unboundness', 'reachability', 'path', 'getmodellist', 'showpetrinet', 'supportedmodeltypes'.");
            }
            String resultsB64 = Base64.getEncoder().encodeToString(results.getBytes("UTF-8"));
            return Json.createObjectBuilder().add("resultsB64", resultsB64).build().toString();
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
            return Json.createObjectBuilder().add("error", ex.getMessage()).build().toString();
        }
    }
}
