package org.adoxx.bpmn;

import java.io.File;
import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.input.ImporterManager;
import org.adoxx.pn.modelcheckers.LOLA;
import org.adoxx.pn.output.ExporterLOLA;
import org.adoxx.utils.IOUtils;
import org.adoxx.utils.XMLUtils;

public class VerificationEngine {

    private String modelCheckerExePath = "";
    private ImporterManager importManager = new ImporterManager();
    
    public VerificationEngine() throws Exception{
        initializeLOLAEngine();
    }
    
    private void initializeLOLAEngine() throws Exception{
        boolean isWindowsOS = System.getProperty("os.name").toLowerCase().contains("windows");
        String tmpFolder = System.getProperty("java.io.tmpdir")+"/";
        if(isWindowsOS){
            if(!new File(tmpFolder + "lola-2.0-cygwin.exe").exists()){
                IOUtils.writeFile(IOUtils.toByteArray(this.getClass().getResourceAsStream("exeLola/lola-2.0-cygwin.exe")), tmpFolder+"lola-2.0-cygwin.exe", false);
                IOUtils.writeFile(IOUtils.toByteArray(this.getClass().getResourceAsStream("exeLola/cyggcc_s-1.dll")), tmpFolder+"cyggcc_s-1.dll", false);
                IOUtils.writeFile(IOUtils.toByteArray(this.getClass().getResourceAsStream("exeLola/cygstdc++-6.dll")), tmpFolder+"cygstdc++-6.dll", false);
                IOUtils.writeFile(IOUtils.toByteArray(this.getClass().getResourceAsStream("exeLola/cygwin1.dll")), tmpFolder+"cygwin1.dll", false);
            }
        } else {
            if(!new File(tmpFolder + "lola").exists()){
                IOUtils.writeFile(IOUtils.toByteArray(this.getClass().getResourceAsStream("exeLola/lola")), tmpFolder+"lola", false);
                if(!new File(tmpFolder + "lola").setExecutable(true))
                    throw new Exception("Impossible to set execution rights for " + tmpFolder + "lola");
            }
        }
        
        modelCheckerExePath = tmpFolder + ((isWindowsOS)?"lola-2.0-cygwin.exe":"lola");
    }
    
    public ImporterManager getImporterManager(){
        return importManager;
    }
    
    public String verifyDeadlock(String model, boolean checkAllDeadlock) throws Exception{
        PetriNet[] pnList = importManager.generateFromModel(model);
        String ret = "<VerificationResults>";
        for(PetriNet pn: pnList)
            if(checkAllDeadlock)
                ret += verifyAllDeadlocks(pn);
            else
                ret += verifySingleDeadlock(pn);
        
        ret += "</VerificationResults>";
        return ret;
    }
    
    public String verifyUnboundedness(String model, boolean checkAllUnboundedness) throws Exception{
        PetriNet[] pnList = importManager.generateFromModel(model);
        String ret = "<VerificationResults>";
        for(PetriNet pn: pnList)
            if(checkAllUnboundedness)
                ret += verifyUnboundedness(pn, false);
            else
                ret += verifyUnboundedness(pn, true);
        ret += "</VerificationResults>";
        return ret;
    }
    
    /*
    Attenzione: il metodo usa la full state search perciò in caso di loop nel modello genera esplosione di stati
    */
    public String verifyObjectReachability(String model, String bpObjectId, boolean inAnyCase, boolean negate) throws Exception {
        PetriNet[] pnList = importManager.generateFromModel(model);
        
        String[] pnIdObjectList = new String[0];
        PetriNet workingPn = null;
        for(PetriNet pn: pnList){
            if(pn.isEmpty())
                throw new Exception("ERROR: The provided petri net is empty\nName:"+pn.getName());
            if(BPUtils.existBPMNObject(pn, bpObjectId)){
                workingPn = pn;
                pnIdObjectList = BPUtils.getPNIdsFromBPMNId(pn, bpObjectId);
                break;
            }
            if(pn.existPlace(bpObjectId)){
                workingPn = pn;
                pnIdObjectList = new String[]{bpObjectId};
                break;
            }
        }
        if(workingPn == null || pnIdObjectList.length==0)
            throw new Exception("ERROR: Can not find the petri net objects related to the element "+bpObjectId);
        
        String ret = "<VerificationResults>";
        
        String modelToVerify = ExporterLOLA.exportTo_LOLA(workingPn);
        String propertyToVerify = ExporterLOLA.exportTo_LOLA_property_StateReachable(workingPn, pnIdObjectList, inAnyCase, negate);
        
        ArrayList<String[]> counterExampleTraceList = new ArrayList<String[]>();
        String out = LOLA.sync_getVerificationOutput(modelCheckerExePath, modelToVerify, propertyToVerify, false);
        boolean propertyVerified = false;
        if(LOLA.isPropertyVerified(out)){
            counterExampleTraceList.add(LOLA.getCounterExample(out, workingPn));
            propertyVerified = true;
        }
        
        ret += formatResult("REACHABILITY", propertyVerified, counterExampleTraceList, workingPn, "'"+BPUtils.getBPMNObjectNameById(workingPn, bpObjectId)+"' ("+bpObjectId+") is "+((inAnyCase)?"every time":"sometime")+((negate)?" not":"")+" reachable");
        
        ret += "</VerificationResults>";
        return ret;
    }
    
    /*
     Attenzione: il metodo usa la full state search perciò in caso di loop nel modello genera esplosione di stati
     */
    public String verifyPathExistence(String model, String bpFromObjectId, String bpToObjectId, boolean inAnyCase, boolean negateFrom, boolean negateTo) throws Exception {
        PetriNet[] pnList = importManager.generateFromModel(model);
        
        String[] pnFromObjectIdList = new String[0];
        String[] pnToObjectIdList = new String[0];
        PetriNet workingPn = null;
        for(PetriNet pn: pnList){
            if(pn.isEmpty())
                throw new Exception("ERROR: The provided petri net is empty\nName:"+pn.getName());
            if(BPUtils.existBPMNObject(pn, bpFromObjectId) && BPUtils.existBPMNObject(pn, bpToObjectId)){
                workingPn = pn;
                pnFromObjectIdList = BPUtils.getPNIdsFromBPMNId(pn, bpFromObjectId);
                pnToObjectIdList = BPUtils.getPNIdsFromBPMNId(pn, bpToObjectId);
                break;
            }
            if(pn.existPlace(bpFromObjectId) && pn.existPlace(bpToObjectId)){
                workingPn = pn;
                pnFromObjectIdList = new String[]{bpFromObjectId};
                pnToObjectIdList = new String[]{bpToObjectId};
                break;
            }
        }
        if(workingPn == null || pnFromObjectIdList.length==0 || pnToObjectIdList.length==0)
            throw new Exception("ERROR: Can not find the petri net objects related to the elements "+bpFromObjectId+" and "+bpToObjectId);
        
        String ret = "<VerificationResults>";
        
        String modelToVerify = ExporterLOLA.exportTo_LOLA(workingPn);
        String propertyToVerify = ExporterLOLA.exportTo_LOLA_property_State2FollowState1(workingPn, pnFromObjectIdList, pnToObjectIdList, inAnyCase, negateFrom, negateTo);

        ArrayList<String[]> counterExampleTraceList = new ArrayList<String[]>();
        String out = LOLA.sync_getVerificationOutput(modelCheckerExePath, modelToVerify, propertyToVerify, false);
        boolean propertyVerified = false;
        if(LOLA.isPropertyVerified(out)){
            counterExampleTraceList.add(LOLA.getCounterExample(out, workingPn));
            propertyVerified = true;
        }
        
        ret += formatResult("PATH_EXISTENCE", propertyVerified, counterExampleTraceList, workingPn, ((inAnyCase)?"For every path where":"Exist a path where") + " " + ((negateFrom)?"not ":"") + "happens '" + BPUtils.getBPMNObjectNameById(workingPn, bpFromObjectId) + "' ("+bpFromObjectId+"), then "+((negateTo)?"not ":"")+"happens '"+BPUtils.getBPMNObjectNameById(workingPn, bpToObjectId) + "' ("+bpToObjectId+")");
        
        ret += "</VerificationResults>";
        return ret;
    }
    
    private String verifySingleDeadlock(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty\nName:"+pn.getName());
        
        String modelToVerify = ExporterLOLA.exportTo_LOLA(pn);
        String propertyToVerify = ExporterLOLA.exportTo_LOLA_property_DeadlockPresence(pn);
        
        ArrayList<String[]> counterExampleTraceList = new ArrayList<String[]>();
        String out = LOLA.sync_getVerificationOutput(modelCheckerExePath, modelToVerify, propertyToVerify, true);
        boolean propertyVerified = false;
        if(LOLA.isPropertyVerified(out)){
            counterExampleTraceList.add(LOLA.getCounterExample(out, pn));
            propertyVerified = true;
        }
        
        return formatResult("DEADLOCKS", !propertyVerified, counterExampleTraceList, pn, "Deadlock absence");
    }
    
    private String verifyAllDeadlocks(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty\nName:"+pn.getName());
        
        ArrayList<P> endPLList = pn.getEndList_safe();
        
        String modelToVerify = ExporterLOLA.exportTo_LOLA(pn);
        
        ArrayList<String[]> counterExampleTraceList = new ArrayList<String[]>();
        boolean propertyVerified = false;
        
        while(true){
            String propertyToVerify = ExporterLOLA.exportTo_LOLA_property_DeadlockPresence(pn);
            String out = LOLA.sync_getVerificationOutput(modelCheckerExePath, modelToVerify, propertyToVerify, true);
            boolean deadlockPresent = LOLA.isPropertyVerified(out);
            if(!deadlockPresent)
                break;
            propertyVerified = true;
            String[] counterExample = LOLA.getCounterExample(out, pn);
            counterExampleTraceList.add(counterExample);
            String[] lastCounterExampleObjList = counterExample[counterExample.length-1].split(" ");
            for(String lastCounterExampleObj: lastCounterExampleObjList)
                if(!endPLList.contains(pn.getPlace(lastCounterExampleObj)))
                    pn.getPlace(lastCounterExampleObj).excludeFromDeadlockCheck=true;
        }
        
        return formatResult("DEADLOCKS", !propertyVerified, counterExampleTraceList, pn, "Deadlock absence (checking all deadlocks)");
    }
    
    private String verifyUnboundedness(PetriNet pn, boolean onlyEndPlaces) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty\nName:"+pn.getName());
        
        String modelToVerify = ExporterLOLA.exportTo_LOLA(pn);
        String[] propertyToVerifyList = ExporterLOLA.exportTo_LOLA_property_UnboundednessPresence(pn, onlyEndPlaces);
        boolean propertyVerified = false;
        ArrayList<String[]> counterExampleTraceList = new ArrayList<String[]>();
        for(String propertyToVerify:propertyToVerifyList){
            String out = LOLA.sync_getVerificationOutput(modelCheckerExePath, modelToVerify, propertyToVerify, true);
            if(LOLA.isPropertyVerified(out)){
                counterExampleTraceList.add(LOLA.getCounterExample(out, pn));
                propertyVerified = true;
            }
        }
        return formatResult("UNBOUNDEDNESS", !propertyVerified, counterExampleTraceList, pn, "Unboundedness absence "+((onlyEndPlaces)?"only on the ending events":"in all the model"));
    }
    
    private String formatResult(String verificationType, boolean propertyVerified, ArrayList<String[]> counterExampleTraceList, PetriNet pn, String verificationDescription) throws Exception{
        /*
         <FormalVerificationResult modelId="mod.1231" modelName="test" verificationType="UNBOUNDEDNESS">
             <Status>..OK or KO..</Status>
             <Description>..detailed description of the result..</Description>
             <CounterExampleTrace>
                 <Step num="1">
                     <Object id="" name="" />
                     <Object id="" name="" />
                     <Object id="" name="" />
                 </Step>
                 <Step num="2">
                     <Object id="" name="" />
                     <Object id="" name="" />
                 </Step>
                 ...
             </CounterExampleTrace>
             <CounterExampleTrace>
                 <Step num="1">
                     <Object id="" name="" />
                 </Step>
                 ...
             </CounterExampleTrace>
             ...
         </FormalVerificationResult>
         */
        String status = (propertyVerified)?"OK":"KO";
        String description = "The property \""+verificationDescription+"\" is "+((propertyVerified)?"TRUE!":"FALSE!");
        String ret = "<FormalVerificationResult modelId=\""+XMLUtils.escapeXMLField(pn.getName())+"\" modelName=\""+XMLUtils.escapeXMLField(pn.additionalInfoList.get("model_name"))+"\" verificationType=\""+verificationType+"\"><Status>"+status+"</Status><Description>"+description+"</Description>";
        for(String[] counterExampleTrace: counterExampleTraceList){
            if(counterExampleTrace.length == 0)
                continue;
            
            ret += "<CounterExampleTrace>";
            
            for(int i=0; i<counterExampleTrace.length;i++){
                ret += "<Step num=\""+i+"\">";
                String[] objList = counterExampleTrace[i].split(" ");
                ArrayList<String> objProcessed = new ArrayList<String>();
                for(String obj: objList){
                    if(pn.getPlace(obj)==null)
                        throw new Exception("ERROR: place " + obj + " not found");
                    String objId = pn.getPlace(obj).description;
                    String objName = pn.getPlace(obj).additionalInfoList.get("name");
                    if(objProcessed.contains(objId))
                        continue;
                    objProcessed.add(objId);
                    ret += "<Object id=\""+XMLUtils.escapeXMLField(objId)+"\" name=\""+XMLUtils.escapeXMLField(objName)+"\"/>";
                }
                ret += "</Step>";
            }
            ret += "</CounterExampleTrace>";
        }
        ret += "</FormalVerificationResult>";
            
        return ret;
    }
    
    /*
    public static void main(String[] args) {    
        try {
            //Document t = XMLUtils.getXmlDocFromString("<test a=\"Analisi &amp; dell Istanza\"></test>");
            //String a = (String)XMLUtils.execXPath(t.getDocumentElement(), "/test/@a", XPathConstants.STRING);
            //a = XMLUtils.escapeXPathField(a);
            //Node b = (Node)XMLUtils.execXPath(t.getDocumentElement(), "//*[@a="+a+"]", XPathConstants.NODE);
            
            String bpmnUrl = "C:\\Users\\dfalcioni\\Desktop\\test\\EPBR-Coordinator\\LP_ME_ADOXX_MODELSET_22224\\20903.bpmn";
            String bpmnModel = new String(IOUtils.readFile(bpmnUrl));
            
            VerificationEngine engine = new VerificationEngine();
            System.out.println(engine.verifyDeadlock(bpmnModel, false));
            //System.out.println(engine.verifyUnboundedness(bpmnModel, true));
            //System.out.println(engine.verifyObjectReachability(bpmnModel, "pTask1", false, true));
            //System.out.println(engine.verifyPathExistence(bpmnModel, "StartEvent_1", "pTask1", false, false, true));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
