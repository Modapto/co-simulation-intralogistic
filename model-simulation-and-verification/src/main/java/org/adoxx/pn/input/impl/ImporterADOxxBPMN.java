package org.adoxx.pn.input.impl;

import javax.xml.xpath.XPathConstants;

import org.adoxx.pn.P;
import org.adoxx.pn.PT;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.input.ImporterXmlI;
import org.adoxx.pn.input.mapping.MappingHelper;
import org.adoxx.pn.input.mapping.data.GeneratedElements;
import org.adoxx.utils.Utils;
import org.adoxx.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImporterADOxxBPMN implements ImporterXmlI {
    
    @Override
    public String getName() {
        return "ADOXX-BPMN";
    }
    
    @Override
    public boolean isCompliant(Document modelXml) {
        try{
            String queryRoot = "/*/*/*[@modeltype='Business process diagram (BPMN 2.0)']";
            Node bpmnRootNode =  (Node) XMLUtils.execXPath(modelXml.getDocumentElement(), queryRoot, XPathConstants.NODE);
            if(bpmnRootNode!=null)
                return true;
        }catch(Exception e){}
        return false;
    }

    
    /**
     * Generate a list of PetriNets, one for each BPMN model (modeltype=Business process diagram (BPMN 2.0)) inside the Adoxx xml provided.
     * 
     * @param bpmnXml The Adoxx modelset containing BPMN models
     * @return The PetriNet list
     * @throws Exception
     */
    @Override
    public PetriNet[] generatePetriNet(Document adoxxXml) throws Exception{
        /* To Remember:
         * - id are unique between all the models
         * - name are unique only inside a model. Es: 2 model can have a task with the same name.
         * */

        if(!isCompliant(adoxxXml))
            throw new Exception("The model is not a BPMN in ADOxx XML format!");
                
        MappingHelper pnm = new MappingHelper();
        
        pnm.addMapping("task : p>t ; in:sequence=p, message=t ; out:sequence=t, message=t, bound=p");
        pnm.addMapping("start: p(1)>t ; in: ; out:sequence=t");
        pnm.addMapping("startMsg: p>t ; in: message=p ; out:sequence=t");
        pnm.addMapping("end|terminateEventDefinition|errorEventDefinition: p ; in: sequence=p ; out: ");
        pnm.addMapping("endMsg|terminateEventDefinitionMsg|errorEventDefinitionMsg: p0>t,t>p1 ; in: sequence=p0 ; out: message=t");
        pnm.addMapping("loop: p0>t0,t0>p1,p1>t2,p1>t1,t1>p0 ; in:sequence=p0, message=t0 ; out:sequence=t2, message=t0, bound=p0");
        pnm.addMapping("xor|inclusiveComplexG|eventG : p ; in:sequence=p ; out:sequence=p");
        pnm.addMapping("and : t ; in:sequence=t ; out:sequence=t");
        pnm.addMapping("bound : t ; in:bound=t ; out:sequence=t");

        String startQuery = "./*[@class='Start Event']";
        String endQuery = "./*[@class='End Event']";
        String anyTaskQuery = "./*[@class='Task']|./*[@class='Intermediate Event (sequence flow)']|./*[@class='Sub-Process']";
        String anyTaskLoopQuery = "./*[@name='Loop type' and text()!='Not specified']";
        String boundaryTaskQuery = "./*[@class='Intermediate Event (boundary)']";
        String andQuery = "./*[@class='Non-exclusive Gateway']|./*[@class='Non-exclusive Gateway (converging)']";
        String xorQuery = "./*[@class='Exclusive Gateway']";
        
        String eventGatewayQuery = "./*[@name='Type']";
        String inclusiveComplexGatewayQuery = "./*[@name='Gateway type']";
        String sequenceFlowQuery = "./*[@class='Subsequent']";
        String messageFlowQuery = "./*[@class='Message Flow']";
        String sequenceFlowExcludeFromToElemList = ""; //Se ci sono sequence flow da/verso una pool allora non li considero 
        String messageFlowExcludeFromToElemList = "Pool (collapsed) Lane"; //Se ci sono messaggi da/verso una pool allora non li considero in quanto per la verifica si assumerebbe che il messaggio sia inviato/ricevuto sempre perci� � come se non ci fosse; se si toglie, vanno mappati a parte un place con un token ed una transizione per ogni messaggio
        
        //NodeList bpmnModelElList =  (NodeList) XMLUtils.execXPath(adoxxXml.getDocumentElement(), "//*[@modeltype='Business process diagram (BPMN 2.0)']", XPathConstants.NODESET);
        NodeList bpmnModelElList =  (NodeList) XMLUtils.execXPath(adoxxXml.getDocumentElement(), "/*/*/*[@modeltype='Business process diagram (BPMN 2.0)']", XPathConstants.NODESET);
        
        //local-name()='repository'
        PetriNet[] ret = new PetriNet[bpmnModelElList.getLength()];
        
        for(int modelIndex=0;modelIndex<bpmnModelElList.getLength();modelIndex++) {
            
            pnm.resetProcessed();
            Node bpmnModelEl = bpmnModelElList.item(modelIndex);
            String modelId = bpmnModelEl.getAttributes().getNamedItem("id").getNodeValue();
            String modelName = bpmnModelEl.getAttributes().getNamedItem("name").getNodeValue();
            
            NodeList startEventNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, startQuery, XPathConstants.NODESET);
            for(int i=0;i<startEventNodeList.getLength();i++){
                String id = startEventNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = startEventNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                boolean isMessageStart = false;
                NodeList messagesToStartNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='Message Flow' and (./@instance="+XMLUtils.escapeXPathField(name)+")]", XPathConstants.NODESET);
                for(int x=0;x<messagesToStartNodeList.getLength();x++){
                    String sourceType = (String) XMLUtils.execXPath(messagesToStartNodeList.item(x), "./*[local-name()='FROM' or local-name()='from']/@class", XPathConstants.STRING);
                    if(!messageFlowExcludeFromToElemList.contains(sourceType))
                        isMessageStart = true;
                }
                
                String elemType = "start";
                if(isMessageStart)
                    elemType += "Msg";
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                
                //map dependent
                ge.transitionList[0].addInfo("isEntryPoint", "true");
            }
            
            NodeList endEventNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, endQuery, XPathConstants.NODESET);
            for(int i=0;i<endEventNodeList.getLength();i++){
                String id = endEventNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = endEventNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
              //FIXME: Terminate end events
                
                boolean isMessageEnd = false;
                NodeList messegesFromEndNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='Message Flow' and (./@instance="+XMLUtils.escapeXPathField(name)+")]", XPathConstants.NODESET);
                for(int x=0;x<messegesFromEndNodeList.getLength();x++){
                    String targetType = (String) XMLUtils.execXPath(messegesFromEndNodeList.item(x), "./*[local-name()='TO' or local-name()='to']/@class", XPathConstants.STRING);
                    if(!messageFlowExcludeFromToElemList.contains(targetType))
                        isMessageEnd = true;
                }
                
                String elemType = "end";
                if(isMessageEnd)
                    elemType += "Msg";
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                
                //map dependent
                if(elemType.equals("endMsg"))
                    ge.transitionList[0].addInfo("isEntryPoint", "true");
                else
                    ge.placeList[0].addInfo("isEntryPoint", "true");
            }
            
            NodeList anyTaskNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, anyTaskQuery, XPathConstants.NODESET);
            for(int i=0;i<anyTaskNodeList.getLength();i++){
                String id = anyTaskNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = anyTaskNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                String elemType = "task";
                NodeList taskLoopDefinitionNodeList =  (NodeList) XMLUtils.execXPath(anyTaskNodeList.item(i), anyTaskLoopQuery, XPathConstants.NODESET);
                if(taskLoopDefinitionNodeList.getLength()>0)
                    elemType = "loop";
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                
                //map dependent
                if(elemType.equals("task"))
                    ge.transitionList[0].addInfo("isEntryPoint", "true");
                else
                    for(T transition:ge.transitionList)
                        if(transition.name.startsWith("t0"))
                            transition.addInfo("isEntryPoint", "true");
                
                String costsString = (String)XMLUtils.execXPath(anyTaskNodeList.item(i), "./*[@name='Costs']", XPathConstants.STRING);
                String executionTimeString = (String)XMLUtils.execXPath(anyTaskNodeList.item(i), "./*[@name='Execution time']", XPathConstants.STRING);
                if(costsString.isEmpty())
                    costsString = "0";
                if(executionTimeString.isEmpty())
                    executionTimeString = "0";
                else if(Utils.isAdoxxDateTime(executionTimeString))
                    executionTimeString = "" + Utils.convertAdoxxDateTimeToMilliseconds(executionTimeString);
                /*
                double costs = 0;
                try{
                    if(!costsString.isEmpty())
                        costs = Double.parseDouble(costsString);
                }catch(Exception ex){Utils.log(ex);ex.printStackTrace();}

                long executionTime = 0;
                if(!executionTimeString.isEmpty())
                    executionTime = Utils.convertAdoxxDateTimeToMilliseconds(executionTimeString);
                */
                for(T transition:ge.transitionList)
                    if(transition.additionalInfoList.containsKey("isEntryPoint")){
                        transition.previousList.get(0).addInfo("executionTime", executionTimeString);
                        transition.previousList.get(0).addInfo("cost", costsString);
                    }
                
                NodeList boundConnectorList = (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='Intermediate Event (boundary)' and (./*[@name='Attached to']/*/@tobjname="+XMLUtils.escapeXPathField(name)+")]", XPathConstants.NODESET);
                if(boundConnectorList.getLength()>0){
                  //map dependent
                    if(elemType.equals("task"))
                        ge.ptList[0].addInfo("pathProbability", "0.8");
                    else
                        for(PT pt:ge.ptList)
                            if(pt.target.name.startsWith("t0"))
                                pt.addInfo("pathProbability", "0.8");
                }
            }
            
            NodeList andNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, andQuery, XPathConstants.NODESET);
            for(int i=0;i<andNodeList.getLength();i++){
                String id = andNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = andNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);
                
                String gatewayType =  (String) XMLUtils.execXPath(andNodeList.item(i), inclusiveComplexGatewayQuery, XPathConstants.STRING);
                GeneratedElements ge = null;
                if(gatewayType.equals("Inclusive") || gatewayType.equals("Complex")){
                    String defaultSequenceFlowId =  (String) XMLUtils.execXPath(bpmnModelEl, "./*[./*[local-name()='FROM' or local-name()='from']/@instance="+XMLUtils.escapeXPathField(name)+" and ./*[@name='Default']='Yes']/@id", XPathConstants.STRING);
                    ge = pnm.processElement(id, "inclusiveComplexG", id, xy[0], xy[1]);
                    if(ge.placeList.length==1 && ge.placeList[0]!=null)
                        ge.placeList[0].addInfo("defaultSequenceFlowId", defaultSequenceFlowId);
                    //map dependent
                    ge.placeList[0].addInfo("isEntryPoint", "true");
                }else{
                    ge = pnm.processElement(id, "and", id, xy[0], xy[1]);
                    //map dependent
                    ge.transitionList[0].addInfo("isEntryPoint", "true");
                }
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
            }
            
            NodeList xorNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, xorQuery, XPathConstants.NODESET);
            for(int i=0;i<xorNodeList.getLength();i++){
                String id = xorNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = xorNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);
                
                String gatewayType =  (String) XMLUtils.execXPath(xorNodeList.item(i), eventGatewayQuery, XPathConstants.STRING);
                GeneratedElements ge = null;
                if(gatewayType.toLowerCase().contains("event"))
                    ge = pnm.processElement(id, "eventG", id, xy[0], xy[1]);
                else
                    ge = pnm.processElement(id, "xor", id, xy[0], xy[1]);
                
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                
                //map dependent
                ge.placeList[0].addInfo("isEntryPoint", "true");
            }
            
            NodeList boundaryTaskNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, boundaryTaskQuery, XPathConstants.NODESET);
            for(int i=0;i<boundaryTaskNodeList.getLength();i++){
                String id = boundaryTaskNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = boundaryTaskNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                float[] xy = getAdoxxElementCoordinatesXY(bpmnModelEl, id);               
                String sourceName = (String) XMLUtils.execXPath(boundaryTaskNodeList.item(i), "./*[@name='Attached to']/*/@tobjname", XPathConstants.STRING);
                if(sourceName.isEmpty()) throw new Exception("ERROR: Attribute 'Attached to' not defined for the object " + id);
                
                String sourceId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name="+XMLUtils.escapeXPathField(sourceName)+"]/@id", XPathConstants.STRING);
                if(sourceId.isEmpty()) throw new Exception("ERROR: Can not find the object with name " + sourceName);

                GeneratedElements ge = pnm.processElement(id, "bound", id, xy[0], xy[1]);
                GeneratedElements ge1 = pnm.processRelation(id, "bound", sourceId, id);  
                String poolId = getAdoxxElementPool(bpmnModelEl, id);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                
                for(P place:ge1.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", name);
                }
                for(T transition:ge1.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", name);
                }
                //map dependent
                ge.transitionList[0].addInfo("isEntryPoint", "true");
            }
            
            NodeList sequenceFlowNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, sequenceFlowQuery, XPathConstants.NODESET);
            for(int i=0;i<sequenceFlowNodeList.getLength();i++){
                String id = sequenceFlowNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                
                String sourceName = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[local-name()='FROM' or local-name()='from']/@instance", XPathConstants.STRING);
                String sourceType = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[local-name()='FROM' or local-name()='from']/@class", XPathConstants.STRING);
                String targetName = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[local-name()='TO' or local-name()='to']/@instance", XPathConstants.STRING);
                String targetType = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[local-name()='TO' or local-name()='to']/@class", XPathConstants.STRING);
                
                String sourceId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name="+XMLUtils.escapeXPathField(sourceName)+"]/@id", XPathConstants.STRING);
                String targetId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name="+XMLUtils.escapeXPathField(targetName)+"]/@id", XPathConstants.STRING);

                if(sourceId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + sourceName + " for the relation " + id);
                if(targetId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + targetName + " for the relation " + id);
                
                if(sequenceFlowExcludeFromToElemList.contains(sourceType) || sequenceFlowExcludeFromToElemList.contains(targetType))
                    continue;
                
                GeneratedElements ge = pnm.processRelation(id, "sequence", sourceId, targetId);
                
                String poolId = getAdoxxElementPool(bpmnModelEl, sourceId);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", sourceName);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", sourceName);
                }
                
                //String pathProbability = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[@name='Transition probability']", XPathConstants.STRING);
                String pathProbability = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[@name='Transition condition']", XPathConstants.STRING);
                if(!pathProbability.isEmpty())
                    for(PT pt:ge.ptList)
                        pt.addInfo("pathProbability", pathProbability);
            }
            
            NodeList messageFlowNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, messageFlowQuery, XPathConstants.NODESET);
            for(int i=0;i<messageFlowNodeList.getLength();i++){
                String id = messageFlowNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                
                String sourceName = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*[local-name()='FROM' or local-name()='from']/@instance", XPathConstants.STRING);
                String sourceType = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*[local-name()='FROM' or local-name()='from']/@class", XPathConstants.STRING);
                String targetName = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*[local-name()='TO' or local-name()='to']/@instance", XPathConstants.STRING);
                String targetType = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*[local-name()='TO' or local-name()='to']/@class", XPathConstants.STRING);

                String sourceId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name="+XMLUtils.escapeXPathField(sourceName)+"]/@id", XPathConstants.STRING);
                String targetId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name="+XMLUtils.escapeXPathField(targetName)+"]/@id", XPathConstants.STRING);

                if(sourceId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + sourceName + " for the relation " + id);
                if(targetId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + targetName + " for the relation " + id);
                
                if(messageFlowExcludeFromToElemList.contains(sourceType) || messageFlowExcludeFromToElemList.contains(targetType))
                    continue;
                
                GeneratedElements genEl = pnm.processRelation(id, "message", sourceId, targetId);
                if(genEl.placeList.length>0){
                    genEl.placeList[0].excludeFromDeadlockCheck = true;
                    genEl.placeList[0].addInfo("name", sourceName);
                }
                
            }
        
            ret[modelIndex] = pnm.generatePN(modelId);
            ret[modelIndex].additionalInfoList.put("model_name", modelName);
            ImporterUtils.postProcessEventGateways(ret[modelIndex], "eventG", "message");
            ImporterUtils.postProcessInclusiveComplexGateways(ret[modelIndex], "inclusiveComplexG");
            ret[modelIndex].updateStartListCheckingFlow(); //richiamando questa funzione sistemo i bpmn fatti male che iniziano senza uno startevent necessario quando un processo inizia ad es da un intermediate event che parte da un segnale o da un altro processo
        }
        
        return ret;
    }
    
    private float[] getAdoxxElementCoordinatesXY(Node bpmnModelEl, String elementId) throws Exception{
        String position = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='Position']", XPathConstants.STRING);
        String x = position.substring(position.indexOf("x:")+2);
        x = x.substring(0, x.indexOf("cm"));
        String y = position.substring(position.indexOf("y:")+2);
        y = y.substring(0, y.indexOf("cm"));
        return new float[]{Float.valueOf(x)*29, Float.valueOf(y)*29};
    }
    
    private String getAdoxxElementPool(Node bpmnModelEl, String elementId) throws Exception{
        String elementName = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/@name", XPathConstants.STRING);
        String elementPoolName = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@class='Is inside'][(./*[local-name()='TO' or local-name()='to']/@class='Pool') or (./*[local-name()='TO' or local-name()='to']/@class='Pool (collapsed)')][./*[local-name()='FROM' or local-name()='from']/@instance=" + XMLUtils.escapeXPathField(elementName) + "]/*[local-name()='TO' or local-name()='to']/@instance", XPathConstants.STRING);
        String elementPoolId = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@name=" + XMLUtils.escapeXPathField(elementPoolName) + "]/@id", XPathConstants.STRING);
        return elementPoolId;
    }

}
