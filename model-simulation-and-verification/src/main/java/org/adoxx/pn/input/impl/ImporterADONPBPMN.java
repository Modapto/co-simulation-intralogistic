package org.adoxx.pn.input.impl;

import javax.xml.xpath.XPathConstants;

import org.adoxx.pn.P;
import org.adoxx.pn.PT;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.input.ImporterXmlI;
import org.adoxx.pn.input.mapping.MappingHelper;
import org.adoxx.pn.input.mapping.data.GeneratedElements;
import org.adoxx.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImporterADONPBPMN implements ImporterXmlI{
    
    @Override
    public String getName() {
        return "ADONP-BPMN";
    }

    @Override
    public boolean isCompliant(Document modelXml) {
        try{
            String queryRoot = "/*[local-name()='repository']/*[local-name()='models']/*[@modeltype='MT_BUSINESS_PROCESS_DIAGRAM_BPMN_20']";
            Node bpmnRootNode =  (Node) XMLUtils.execXPath(modelXml.getDocumentElement(), queryRoot, XPathConstants.NODE);           
            if(bpmnRootNode!=null)
                return true;
        }catch(Exception e){}
        return false;
    }

    /**
     * Generate a list of PetriNets, one for each BPMN model (modeltype=MT_BUSINESS_PROCESS_DIAGRAM_BPMN_20) inside the Adonis NP light xml provided.
     * 
     * @param bpmnXml The Adonis NP light xml modelset containing BPMN models
     * @return The PetriNet list
     * @throws Exception
     */
    @Override
    public PetriNet[] generatePetriNet(Document adonpXml) throws Exception {
        /* To Remember:
         * - id are unique between all the models
         * - name are unique only inside a model. Es: 2 model can have a task with the same name.
         * */
        
        if(!isCompliant(adonpXml))
            throw new Exception("The model is not a BPMN in ADONP Light XML format!");
        
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

        String startQuery = "./*[@class='C_START_EVENT']";
        String endQuery = "./*[@class='C_END_EVENT']";
        String anyTaskQuery = "./*[@class='C_TASK']|./*[@class='C_INTERMEDIATE_EVENT']|./*[@class='C_SUB_PROCESS']";
        String anyTaskLoopQuery = "./*[@name='LOOP_TYPE' and (./*[local-name()='value']!='Not specified')]";
        String boundaryTaskQuery = "./*[@class='C_INTERMEDIATE_EVENT_BOUNDARY']";
        String andQuery = "./*[@class='C_NON_EXCLUSIVE_GATEWAY']";
        String xorQuery = "./*[@class='C_EXCLUSIVE_GATEWAY']";
        
        String eventGatewayQuery = "./*[@name='TYPE_EXCLUSIVE_GATEWAY']/*[local-name()='value']";
        String inclusiveComplexGatewayQuery = "./*[@name='TYPE_NON_EXCLUSIVE_GATEWAY']/*[local-name()='value']";
        
        String sequenceFlowQuery = "./*[@class='RC_SEQUENCE_FLOW_BPMN']";
        String messageFlowQuery = "./*[@class='RC_MESSAGE_FLOW']";
        
        String sequenceFlowExcludeFromToElemList = ""; //Se ci sono sequence flow da/verso una pool allora non li considero 
        String messageFlowExcludeFromToElemList = "C_POOL C_POOL_VERTICAL C_LANE"; //Se ci sono messaggi da/verso una pool allora non li considero in quanto per la verifica si assumerebbe che il messaggio sia inviato/ricevuto sempre perci� � come se non ci fosse; se si toglie, vanno mappati a parte un place con un token ed una transizione per ogni messaggio
        
        NodeList bpmnModelElList =  (NodeList) XMLUtils.execXPath(adonpXml.getDocumentElement(), "/*/*/*[@modeltype='MT_BUSINESS_PROCESS_DIAGRAM_BPMN_20']", XPathConstants.NODESET);
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
                NodeList messagesToStartNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='RC_MESSAGE_FLOW' and (./*/*[@name='TO']/*[local-name()='target']/@id="+XMLUtils.escapeXPathField(id)+")]", XPathConstants.NODESET);
                for(int x=0;x<messagesToStartNodeList.getLength();x++){
                    String sourceType = (String) XMLUtils.execXPath(messagesToStartNodeList.item(x), "./*/*[@name='FROM']/*[local-name()='target']/@class", XPathConstants.STRING);
                    if(!messageFlowExcludeFromToElemList.contains(sourceType))
                        isMessageStart = true;
                }
                
                String elemType = "start";
                if(isMessageStart)
                    elemType += "Msg";
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                NodeList messegesFromEndNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='RC_MESSAGE_FLOW' and (./*/*[@name='FROM']/*[local-name()='target']/@id="+XMLUtils.escapeXPathField(id)+")]", XPathConstants.NODESET);
                for(int x=0;x<messegesFromEndNodeList.getLength();x++){
                    String targetType = (String) XMLUtils.execXPath(messegesFromEndNodeList.item(x), "./*/*[@name='TO']/*[local-name()='target']/@class", XPathConstants.STRING);
                    if(!messageFlowExcludeFromToElemList.contains(targetType))
                        isMessageEnd = true;
                }
                
                String elemType = "end";
                if(isMessageEnd)
                    elemType += "Msg";
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
                
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                
                String costsString = (String)XMLUtils.execXPath(anyTaskNodeList.item(i), "./*[@name='COSTS']/*[local-name()='value']", XPathConstants.STRING);
                String executionTimeString = (String)XMLUtils.execXPath(anyTaskNodeList.item(i), "./*[@name='A_EXECUTION_TIME']/*[local-name()='value']", XPathConstants.STRING);
                if(costsString.isEmpty()) costsString = "0";
                if(executionTimeString.isEmpty()) executionTimeString = "0";
                /*
                double costs = 0;
                try{
                    if(!costsString.isEmpty())
                        costs = Double.parseDouble(costsString);
                }catch(Exception ex){Utils.log(ex);ex.printStackTrace();}
                long executionTime = 0;
                if(!executionTimeString.isEmpty())
                    executionTime = Long.parseLong(executionTimeString);
                */
                for(T transition:ge.transitionList)
                    if(transition.additionalInfoList.containsKey("isEntryPoint")){
                        transition.previousList.get(0).addInfo("executionTime", executionTimeString);
                        transition.previousList.get(0).addInfo("cost", costsString);
                    }
                
                NodeList boundConnectorList = (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='RC_ATTACHED_TO' and (./*/*[@name='FROM']/*[local-name()='target']/@class='C_INTERMEDIATE_EVENT_BOUNDARY') and (./*/*[@name='TO']/*[local-name()='target']/@id="+XMLUtils.escapeXPathField(id)+")]", XPathConstants.NODESET);
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
                
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                
                String gatewayType =  (String) XMLUtils.execXPath(andNodeList.item(i), inclusiveComplexGatewayQuery, XPathConstants.STRING);
                GeneratedElements ge = null;
                if(gatewayType.equals("Inclusive") || gatewayType.equals("Complex")){
                    String defaultSequenceFlowId =  (String) XMLUtils.execXPath(bpmnModelEl, "./*[@class='RC_SEQUENCE_FLOW_BPMN' and (./*/*[@name='FROM']/*[local-name()='target']/@id="+XMLUtils.escapeXPathField(id)+") and (./*[@name='IS_STANDARD']/*[local-name()='value']='true')]/@id", XPathConstants.STRING);
                    
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
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                
                String gatewayType =  (String) XMLUtils.execXPath(xorNodeList.item(i), eventGatewayQuery, XPathConstants.STRING);
                GeneratedElements ge = null;
                if(gatewayType.toLowerCase().contains("event"))
                    ge = pnm.processElement(id, "eventG", id, xy[0], xy[1]);
                else
                    ge = pnm.processElement(id, "xor", id, xy[0], xy[1]);
                
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                
                float[] xy = getAdoXmlLightElementCoordinatesXY(bpmnModelEl, id);
                
                String sourceId = (String) XMLUtils.execXPath(boundaryTaskNodeList.item(i), "./*[@class='RC_ATTACHED_TO']/*/*[@name='TO']/*[local-name()='target']/@id", XPathConstants.STRING);
                if(sourceId.isEmpty()) throw new Exception("ERROR: Attribute 'Attached to' not defined for the object " + id);
                
                GeneratedElements ge = pnm.processElement(id, "bound", id, xy[0], xy[1]);
                GeneratedElements ge1 = pnm.processRelation(id, "bound", sourceId, id);  
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, id);
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
                
                String sourceId = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*/*[@name='FROM']/*[local-name()='target']/@id", XPathConstants.STRING);
                String sourceName = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(sourceId) + "]/@name", XPathConstants.STRING);
                
                String targetId = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*/*[@name='TO']/*[local-name()='target']/@id", XPathConstants.STRING);
                String sourceType = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*/*[@name='FROM']/*[local-name()='target']/@class", XPathConstants.STRING);
                String targetType = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*/*[@name='TO']/*[local-name()='target']/@class", XPathConstants.STRING);
                
                if(sequenceFlowExcludeFromToElemList.contains(sourceType) || sequenceFlowExcludeFromToElemList.contains(targetType))
                    continue;
                
                GeneratedElements ge = pnm.processRelation(id, "sequence", sourceId, targetId);
                
                String poolId = getAdoXmlLightElementPool(bpmnModelEl, sourceId);
                for(P place:ge.placeList){
                    place.addInfo("poolId", poolId);
                    place.addInfo("name", sourceName);
                }
                for(T transition:ge.transitionList){
                    transition.addInfo("poolId", poolId);
                    transition.addInfo("name", sourceName);
                }
                
                String pathProbability = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[@name='TRANSITION_PROBABILITY']/*[local-name()='value']", XPathConstants.STRING);
                if(pathProbability.isEmpty())
                    pathProbability = (String) XMLUtils.execXPath(sequenceFlowNodeList.item(i), "./*[@name='TRANSITION_CONDITION']/*[local-name()='value']", XPathConstants.STRING);                    
                if(!pathProbability.isEmpty())
                    for(PT pt:ge.ptList)
                        pt.addInfo("pathProbability", pathProbability);
                    
            }
            
            NodeList messageFlowNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, messageFlowQuery, XPathConstants.NODESET);
            for(int i=0;i<messageFlowNodeList.getLength();i++){
                String id = messageFlowNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                
                String sourceId = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*/*[@name='FROM']/*[local-name()='target']/@id", XPathConstants.STRING);
                String sourceName = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(sourceId) + "]/@name", XPathConstants.STRING);
                
                String targetId = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*/*[@name='TO']/*[local-name()='target']/@id", XPathConstants.STRING);
                String sourceType = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*/*[@name='FROM']/*[local-name()='target']/@class", XPathConstants.STRING);
                String targetType = (String) XMLUtils.execXPath(messageFlowNodeList.item(i), "./*/*[@name='TO']/*[local-name()='target']/@class", XPathConstants.STRING);
                
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
    
    private float[] getAdoXmlLightElementCoordinatesXY(Node bpmnModelEl, String elementId) throws Exception{
        String x = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='POSX']/*[local-name()='value']", XPathConstants.STRING);
        String y = (String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='POSY']/*[local-name()='value']", XPathConstants.STRING);
        return new float[]{Float.valueOf(x)/20, Float.valueOf(y)/20};
    }
    
    private String getAdoXmlLightElementPool(Node bpmnModelEl, String elementId) throws Exception{
        int x = Integer.parseInt((String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='POSX']/*[local-name()='value']", XPathConstants.STRING));
        int y = Integer.parseInt((String) XMLUtils.execXPath(bpmnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='POSY']/*[local-name()='value']", XPathConstants.STRING));
        
        NodeList poolNodeList =  (NodeList) XMLUtils.execXPath(bpmnModelEl, "./*[@class='C_POOL']|./*[@class='C_POOL_VERTICAL']", XPathConstants.NODESET);
        for(int i=0;i<poolNodeList.getLength();i++){
            String id = poolNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            int pool_x = Integer.parseInt((String) XMLUtils.execXPath(poolNodeList.item(i), "./*[@name='POSX']/*[local-name()='value']", XPathConstants.STRING));
            int pool_y = Integer.parseInt((String) XMLUtils.execXPath(poolNodeList.item(i), "./*[@name='POSY']/*[local-name()='value']", XPathConstants.STRING));
            int pool_w = Integer.parseInt((String) XMLUtils.execXPath(poolNodeList.item(i), "./*[@name='WIDTH']/*[local-name()='value']", XPathConstants.STRING));
            int pool_h = Integer.parseInt((String) XMLUtils.execXPath(poolNodeList.item(i), "./*[@name='HEIGHT']/*[local-name()='value']", XPathConstants.STRING));
            
            if((x-pool_x)>=0 && (x-pool_x-pool_w)<=0 && (y-pool_y)>=0 && (y-pool_y-pool_h)<=0)
                return id;
        }
        return "";
    }
}
