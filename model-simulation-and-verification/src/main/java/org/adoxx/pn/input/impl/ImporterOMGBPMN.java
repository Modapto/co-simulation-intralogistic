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

public class ImporterOMGBPMN implements ImporterXmlI{

    @Override
    public String getName() {
        return "OMG-BPMN";
    }
    
    @Override
    public boolean isCompliant(Document modelXml) {
        try{
            String queryRoot = "/*[namespace-uri()='http://www.omg.org/spec/BPMN/20100524/MODEL' and local-name()='definitions']";
            Node bpmnRootNode =  (Node) XMLUtils.execXPath(modelXml.getDocumentElement(), queryRoot, XPathConstants.NODE);
            if(bpmnRootNode!=null)
                return true;
        }catch(Exception e){}
        return false;
    }

    /**
     * Generate a PetriNet from the provided OMG BPMN2 Standard.
     * Supported BPMN2 elements are: startEvent, endEvent, task, userTask, serviceTask, manualTask, businessRuleTask, receiveTask, sendTask, scriptTask, intermediateCatchEvent, intermediateThrowEvent, adHocSubProcess, subProcess, transaction, callActivity, choreographyTask, subChoreography, callChoreography, standardLoopCharacteristics, boundaryEvent, parallelGateway, exclusiveGateway, eventBasedGateway, inclusiveGateway, complexGateway, sequenceFlow, messageFlow, participant.
     * The function provide a personalized mapping for every element.
     * @param bpmnXml The BPMN2 model
     * @return The PetriNet
     * @throws Exception
     */
    
    //FIXME: gestire terminate & error end: basta mettere sul deadlock che c'è se valgono le condizioni originali e nessun terminate o error ha token....come gestisco il fatto che il terminate deve terminare solo i token della sua stessa pool?
    //FIXME: rimuovere i conversation dato che non saranno che non si gestisce il conversationlink?
    //FIXME: analizzare meglio i task che non continuano (transizione senza place uscenti)
    //FIXME: loop con numero specificato: fare un postprocess mettendo tanti places quanti sono i loop da fare e caricarli in parallelo all'ingresso del task e consumarne uno ad ogni loop NO-> mettere un place con dentro tanti token quanti sono i loop che entra nella transizione del loop: aggiungere p1(n)>t1 al mapping del loop con p1 escluso dal deadlock
    //FIXME: gestire i parallel event gateways!!! pag 298 eventGatewayType=parallel
    //FIXME: una sequenceflow che esce da un activity può avere una condizione, quindi partire o meno (solo se ci sono almeno 2 fresce uscenti)
    //FIXME: subprocess? per la simulazione andrebbero gestiti diversi
    @Override
    public PetriNet[] generatePetriNet(Document bpmnXml) throws Exception {
        
        if(!isCompliant(bpmnXml))
            throw new Exception("The model is not in BPMN OMG standard format!");
        
        MappingHelper pnm = new MappingHelper();
        
        pnm.addMapping("task : p>t ; in:sequence=p, message=t ; out:sequence=t, message=t, bound=p");
        //pnm.addMapping("taskBufferInfinite : pBuf>tStart,tStart>pExec,pExec>tEnd,tEnd>pIdle(1),pIdle(1)>tStart ; in:sequence=pBuf, message=tStart ; out:sequence=tEnd, message=tEnd, bound=pBuf");
        pnm.addMapping("start: p(1)>t ; in: ; out:sequence=t");
        pnm.addMapping("startMsg: p>t ; in: message=p ; out:sequence=t");
        pnm.addMapping("end|terminateEventDefinition|errorEventDefinition: p ; in: sequence=p ; out: ");
        pnm.addMapping("endMsg|terminateEventDefinitionMsg|errorEventDefinitionMsg: p0>t,t>p1 ; in: sequence=p0 ; out: message=t");
        pnm.addMapping("loop: p0>t0,t0>p1,p1>t2,p1>t1,t1>p0 ; in:sequence=p0, message=t0 ; out:sequence=t2, message=t0, bound=p0");
        pnm.addMapping("xor|inclusiveComplexG|eventG : p ; in:sequence=p ; out:sequence=p");
        pnm.addMapping("and : t ; in:sequence=t ; out:sequence=t");
        pnm.addMapping("bound : t ; in:bound=t ; out:sequence=t");
        
        String startQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='startEvent']";
        String endQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='endEvent']";
        String endSpecialQuery = "./*[local-name()='terminateEventDefinition']|./*[local-name()='errorEventDefinition']";
        String anyTaskQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='task']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='userTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='serviceTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='manualTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='businessRuleTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='receiveTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='sendTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='scriptTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='intermediateCatchEvent']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='intermediateThrowEvent']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='adHocSubProcess']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='subProcess']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='transaction']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='callActivity']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='choreographyTask']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='subChoreography']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='callChoreography']";
        //String anyTaskLoopQuery = "./*[local-name()='standardLoopCharacteristics']|./*[local-name()='multiInstanceLoopCharacteristics']";
        String anyTaskLoopQuery = "./*[local-name()='standardLoopCharacteristics']";
        String boundaryTaskQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='boundaryEvent']";
        //String andQuery = "//*[local-name()='inclusiveGateway']|//*[local-name()='eventBasedGateway']|//*[local-name()='complexGateway']|//*[local-name()='parallelGateway']";
        String andQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='parallelGateway']";
        String xorQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='exclusiveGateway']";
        String eventGatewayQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='eventBasedGateway']";
        String inclusiveComplexGatewayQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='inclusiveGateway']|"
                + "./*[local-name()='process' or local-name()='choreography']/*[local-name()='complexGateway']";
        
        String sequenceFlowQuery = "./*[local-name()='process' or local-name()='choreography']/*[local-name()='sequenceFlow']";
        String messageFlowQuery = "./*[local-name()='collaboration']/*[local-name()='messageFlow']";
        String sequenceFlowExcludeFromToElemList = "participant"; //Se ci sono sequence flow da/verso una pool allora non li considero
        String messageFlowExcludeFromToElemList = "participant"; //Se ci sono messaggi da/verso una pool allora non li considero in quanto per la verifica si assumerebbe che il messaggio sia inviato/ricevuto sempre perci� � come se non ci fosse; se si toglie, vanno mappati a parte un place con un token ed una transizione per ogni messaggio
        
        String modelId = bpmnXml.getDocumentElement().getAttribute("id");
        
        String queryRoot = "/*[namespace-uri()='http://www.omg.org/spec/BPMN/20100524/MODEL' and local-name()='definitions']";
        Node bpmnDefinition = (Node) XMLUtils.execXPath(bpmnXml.getDocumentElement(), queryRoot, XPathConstants.NODE);
        
        NodeList startEventNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, startQuery, XPathConstants.NODESET);
        for(int i=0;i<startEventNodeList.getLength();i++){
            String id = startEventNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = startEventNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            boolean isMessageStart = false;
            NodeList messagesToStartNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, "./*[local-name()='collaboration']/*[local-name()='messageFlow' and (@targetRef="+XMLUtils.escapeXPathField(id)+")]", XPathConstants.NODESET);
            for(int x=0;x<messagesToStartNodeList.getLength();x++){
                String sourceId = messagesToStartNodeList.item(x).getAttributes().getNamedItem("sourceRef").getNodeValue();
                String sourceType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(sourceId)+"]", XPathConstants.NODE)).getLocalName();
                if(!messageFlowExcludeFromToElemList.contains(sourceType))
                    isMessageStart = true;
            }
            
            String elemType = "start";
            if(isMessageStart)
                elemType += "Msg";
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            
            GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList endEventNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, endQuery, XPathConstants.NODESET);
        for(int i=0;i<endEventNodeList.getLength();i++){
            String id = endEventNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = endEventNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            boolean isTerminate = ((NodeList) XMLUtils.execXPath(endEventNodeList.item(i), endSpecialQuery, XPathConstants.NODESET)).getLength()!=0 ? true : false;

            
            boolean isMessageEnd = false;
            NodeList messagesFromEndNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, "./*[local-name()='collaboration']/*[local-name()='messageFlow' and (@sourceRef="+XMLUtils.escapeXPathField(id)+")]", XPathConstants.NODESET);
            for(int x=0;x<messagesFromEndNodeList.getLength();x++){
                String targetId = messagesFromEndNodeList.item(x).getAttributes().getNamedItem("targetRef").getNodeValue();
                String targetType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(targetId)+"]", XPathConstants.NODE)).getLocalName();
                if(!messageFlowExcludeFromToElemList.contains(targetType))
                    isMessageEnd = true;
            }
            
            String elemType = "end";
            if(isMessageEnd)
                elemType += "Msg";
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
            for(P place:ge.placeList){
                place.addInfo("poolId", poolId);
                place.addInfo("name", name);
                place.terminateAll = isTerminate;
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
        
        NodeList anyTaskNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, anyTaskQuery, XPathConstants.NODESET);
        for(int i=0;i<anyTaskNodeList.getLength();i++){
            String id = anyTaskNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = anyTaskNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            String elemType = "task";
            NodeList taskLoopDefinitionNodeList =  (NodeList) XMLUtils.execXPath(anyTaskNodeList.item(i), anyTaskLoopQuery, XPathConstants.NODESET);
            if(taskLoopDefinitionNodeList.getLength()>0)
                elemType = "loop";
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, elemType, id, xy[0], xy[1]);
            
            if(anyTaskNodeList.item(i).getAttributes().getNamedItem("calledElement")!=null){
                String calledElement = anyTaskNodeList.item(i).getAttributes().getNamedItem("calledElement").getNodeValue();
                for(P place: ge.placeList)
                    place.addInfo("calledElement", calledElement);
                for(T transition: ge.transitionList)
                    transition.addInfo("calledElement", calledElement);
            }
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
            
            //Simulation Parameters
            String costsString = getBPMNExtensionParam(bpmnDefinition, id, "Costs");
            String executionTimeString = getBPMNExtensionParam(bpmnDefinition, id, "ExecutionTime");
            
            if(costsString.isEmpty()) costsString = getBPMNExtensionParamADONP(bpmnDefinition, id, "COSTS");
            if(executionTimeString.isEmpty()) executionTimeString = getBPMNExtensionParamADONP(bpmnDefinition, id, "A_EXECUTION_TIME");

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
            
            NodeList boundConnectorList = (NodeList) XMLUtils.execXPath(bpmnDefinition, "./*[local-name()='process' or local-name()='choreography']/*[local-name()='boundaryEvent' and @attachedToRef="+XMLUtils.escapeXPathField(id)+"]", XPathConstants.NODESET);
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
        
        NodeList andNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, andQuery, XPathConstants.NODESET);
        for(int i=0;i<andNodeList.getLength();i++){
            String id = andNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = andNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, "and", id, xy[0], xy[1]);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList xorNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, xorQuery, XPathConstants.NODESET);
        for(int i=0;i<xorNodeList.getLength();i++){
            String id = xorNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = xorNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();

            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, "xor", id, xy[0], xy[1]);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList eventGNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, eventGatewayQuery, XPathConstants.NODESET);
        for(int i=0;i<eventGNodeList.getLength();i++){
            String id = eventGNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = eventGNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, "eventG", id, xy[0], xy[1]);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList inclusiveComplexGNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, inclusiveComplexGatewayQuery, XPathConstants.NODESET);
        for(int i=0;i<inclusiveComplexGNodeList.getLength();i++){
            String id = inclusiveComplexGNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = inclusiveComplexGNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            String defaultSequenceFlowId = "";
            if(inclusiveComplexGNodeList.item(i).getAttributes().getNamedItem("default")!=null)
                defaultSequenceFlowId = inclusiveComplexGNodeList.item(i).getAttributes().getNamedItem("default").getNodeValue();
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            GeneratedElements ge = pnm.processElement(id, "inclusiveComplexG", id, xy[0], xy[1]);
            if(ge.placeList.length==1 && ge.placeList[0]!=null)
                ge.placeList[0].addInfo("defaultSequenceFlowId", defaultSequenceFlowId);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList boundaryTaskNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, boundaryTaskQuery, XPathConstants.NODESET);
        for(int i=0;i<boundaryTaskNodeList.getLength();i++){
            String id = boundaryTaskNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = boundaryTaskNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            
            float[] xy = getBPMNElementCoordinatesXY(bpmnDefinition, id);
            if(boundaryTaskNodeList.item(i).getAttributes().getNamedItem("attachedToRef") == null)
                throw new Exception("ERROR: Attribute 'attachedToRef' not defined for the object " + id);
            
            if(boundaryTaskNodeList.item(i).getAttributes().getNamedItem("attachedToRef")==null)
                throw new Exception("ERROR: Attribute 'attachedToRef' not defined for the object " + id);
            String sourceId = boundaryTaskNodeList.item(i).getAttributes().getNamedItem("attachedToRef").getNodeValue();
            if(sourceId.isEmpty())
                throw new Exception("ERROR: Attribute 'attachedToRef' not defined for the object " + id);

            GeneratedElements ge = pnm.processElement(id, "bound", id, xy[0], xy[1]);
            GeneratedElements ge1 = pnm.processRelation(id, "bound", sourceId, id);    
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
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
        
        NodeList sequenceFlowNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, sequenceFlowQuery, XPathConstants.NODESET);
        for(int i=0;i<sequenceFlowNodeList.getLength();i++){
            String id = sequenceFlowNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String sourceId = sequenceFlowNodeList.item(i).getAttributes().getNamedItem("sourceRef").getNodeValue();
            String sourceName = (String) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id=" + XMLUtils.escapeXPathField(sourceId) + "]/@name", XPathConstants.STRING);
            
            String targetId = sequenceFlowNodeList.item(i).getAttributes().getNamedItem("targetRef").getNodeValue();    
            String sourceType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(sourceId)+"]", XPathConstants.NODE)).getLocalName();
            String targetType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(targetId)+"]", XPathConstants.NODE)).getLocalName();
            
            if(sourceType.isEmpty()) throw new Exception("ERROR: Can not identify the element " + sourceId);
            if(targetType.isEmpty()) throw new Exception("ERROR: Can not identify the element " + targetId);
            
            if(sequenceFlowExcludeFromToElemList.contains(sourceType) || sequenceFlowExcludeFromToElemList.contains(targetType))
                continue;
            
            GeneratedElements ge = pnm.processRelation(id, "sequence", sourceId, targetId);
            
            String poolId = getBPMNElementPool(bpmnDefinition, id);
            for(P place:ge.placeList){
                place.addInfo("poolId", poolId);
                place.addInfo("name", sourceName);
            }
            for(T transition:ge.transitionList){
                transition.addInfo("poolId", poolId);
                transition.addInfo("name", sourceName);
            }
            
            //Simulation Parameters
            String simulationPathProbability = getBPMNExtensionParam(bpmnDefinition, id, "PathProbability");
            for(PT pt:ge.ptList){
                if(!simulationPathProbability.isEmpty()) 
                    pt.addInfo("pathProbability", simulationPathProbability);
            }
        }
        
        NodeList messageFlowNodeList =  (NodeList) XMLUtils.execXPath(bpmnDefinition, messageFlowQuery, XPathConstants.NODESET);
        for(int i=0;i<messageFlowNodeList.getLength();i++){
            String id = messageFlowNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String sourceId = messageFlowNodeList.item(i).getAttributes().getNamedItem("sourceRef").getNodeValue();
            String sourceName = (String) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id=" + XMLUtils.escapeXPathField(sourceId) + "]/@name", XPathConstants.STRING);
            
            String targetId = messageFlowNodeList.item(i).getAttributes().getNamedItem("targetRef").getNodeValue();
            String sourceType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(sourceId)+"]", XPathConstants.NODE)).getLocalName();
            String targetType =  ((Node) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id="+XMLUtils.escapeXPathField(targetId)+"]", XPathConstants.NODE)).getLocalName();

            if(sourceType.isEmpty()) throw new Exception("ERROR: Can not identify the element " + sourceId);
            if(targetType.isEmpty()) throw new Exception("ERROR: Can not identify the element " + targetId);
            
            if(messageFlowExcludeFromToElemList.contains(sourceType) || messageFlowExcludeFromToElemList.contains(targetType))
                continue;
            
            GeneratedElements genEl = pnm.processRelation(id, "message", sourceId, targetId);
            if(genEl.placeList.length>0){
                genEl.placeList[0].excludeFromDeadlockCheck = true;
                genEl.placeList[0].addInfo("name", sourceName);
            }
            
        }
        
        PetriNet pn = pnm.generatePN(modelId);
        pn.additionalInfoList.put("model_name", modelId);
        ImporterUtils.postProcessEventGateways(pn, "eventG", "message");
        ImporterUtils.postProcessInclusiveComplexGateways(pn, "inclusiveComplexG");
        pn.updateStartListCheckingFlow(); //richiamando questa funzione sistemo i bpmn fatti male che iniziano senza uno startevent necessario quando un processo inizia ad es da un intermediate event che parte da un segnale o da un altro processo
        return new PetriNet[]{pn};
    }

    
    private float[] getBPMNElementCoordinatesXY(Node bpmnDefinition, String elementId) throws Exception{
        Node bpmnShapeBounds = (Node)XMLUtils.execXPath(bpmnDefinition, "./*[local-name()='BPMNDiagram']/*[local-name()='BPMNPlane']/*[local-name()='BPMNShape' and @bpmnElement=" + XMLUtils.escapeXPathField(elementId) + "]/*[local-name()='Bounds']", XPathConstants.NODE);
        String x = bpmnShapeBounds.getAttributes().getNamedItem("x").getNodeValue();
        String y = bpmnShapeBounds.getAttributes().getNamedItem("y").getNodeValue();
        if(x.isEmpty()) x = "0";
        if(y.isEmpty()) y = "0";
        return new float[]{Float.valueOf(x), Float.valueOf(y)};
    }
    
    private String getBPMNElementPool(Node bpmnDefinition, String elementId) throws Exception{
        return (String) XMLUtils.execXPath(bpmnDefinition, "./*[local-name()='process' and ./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]]/@id", XPathConstants.STRING);
    }
    
    private String getBPMNExtensionParam(Node bpmnDefinition, String elementId, String param) throws Exception{
        String ret = (String) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[local-name()='extensionElements']//@"+param, XPathConstants.STRING);
        if(ret.isEmpty())
            ret = (String) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[local-name()='extensionElements']//*[local-name()='"+param+"']", XPathConstants.STRING);
        return ret;
    }

    private String getBPMNExtensionParamADONP(Node bpmnDefinition, String elementId, String param) throws Exception{
        return (String) XMLUtils.execXPath(bpmnDefinition, "./*/*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[local-name()='extensionElements']//*[@name="+ XMLUtils.escapeXPathField(param)+"]/*[local-name()='value']", XPathConstants.STRING);
    }
}
