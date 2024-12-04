package org.adoxx.pn.input.impl;

import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.PT;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.utils.Utils;

public class ImporterUtils {

    /*
    Per ogni event gateway vengono controllati tutti i task collegati in outgoing. Se questi task hanno uno o piu messaggi in input, questi vengono staccati dalla transition del task e riattaccati alla transition del gateway. In questo modo il gateway sceglie il task da far partire sulla base del messaggio arrivato.
    Es: p0(è eventG)>t0,p0(è eventG)>t1,t0>p1,t1>p2,p1>t2,p3>t2(è messaggio) diventa p0(è eventG)>t0,p0(è eventG)>t1,t0>p1,t1>p2,p1>t2,p3>t0(è messaggio su eventG)
    */
   public static void postProcessEventGateways(PetriNet pn, String mappingNameForEventGateway, String mappingNameForMessageRelations) throws Exception{
       for(P place:pn.getPlaceList_safe()){
          if(!mappingNameForEventGateway.equals(place.additionalInfoList.get("elementType")))
              continue;
          for(T nextTransition:place.getNextList_safe()) {
              if(nextTransition.nextList.size()==1)
                  if(nextTransition.nextList.get(0).nextList.size()==1){
                      T transitionToCheck = nextTransition.nextList.get(0).nextList.get(0);
                      for(P prevOfTransitionToCheck:transitionToCheck.getPreviousList_safe())
                          if(mappingNameForMessageRelations.equals(pn.getConnection(prevOfTransitionToCheck, transitionToCheck).additionalInfoList.get("relationType"))){
                              pn.delConnection(prevOfTransitionToCheck, transitionToCheck);
                              PT conn = pn.connect(prevOfTransitionToCheck, nextTransition);
                              conn.addInfo("relationType", mappingNameForMessageRelations);
                          }
                  }
          }
      }
   }
   
   /*
    Per ogni inclusive o complex gateway aggiungo transizioni in uscita in modo da prevedere tutte le possibili combinazioni di partenza di output flow.
    FIXME: convertire il complex in parallelo. poi aggiungere per ogni path un place che valuta la condizione e skippa il percorso o meno. Va cambiato anche il hateway di chiusura per ricevere il parallelo
    problema converging: pag 437 specifica BPMN
    */

   public static void postProcessInclusiveComplexGateways(PetriNet pn, String mappingNameForInclusiveComplexGateway) throws Exception{
       for(P place:pn.getPlaceList_safe()){
           if(!mappingNameForInclusiveComplexGateway.equals(place.additionalInfoList.get("elementType")))
               continue;
           String defaultSequenceFlowId = place.additionalInfoList.get("defaultSequenceFlowId");
           if(defaultSequenceFlowId==null)
               defaultSequenceFlowId = "";

           ArrayList<T> nextTransitionList = place.getNextList_safe();
           
           if(!defaultSequenceFlowId.isEmpty()){
               for(T nextTransition:place.nextList)
                   if(defaultSequenceFlowId.equals(pn.getConnection(place, nextTransition).additionalInfoList.get("relationId")))
                       nextTransitionList.remove(nextTransition);
           }
           int[][] binMap = Utils.generateBinaryMatrix(nextTransitionList.size());
           for(int iRow=0;iRow<binMap.length;iRow++){
               int sum=0;
               for(int iColumn=0;iColumn<binMap[iRow].length;iColumn++)
                   sum+=binMap[iRow][iColumn];
               if(sum<=1)
                   continue;
               
               T newTr = pn.addTransition("t"+iRow+place.name);
               newTr.description = place.name;
               newTr.x = place.x;
               newTr.y = (Float.valueOf(place.y) + Float.valueOf(place.h)*(iRow+1) + 20) + "";
               pn.connect(place, newTr);
               
               for(int iColumn=0;iColumn<binMap[iRow].length;iColumn++){
                   if(binMap[iRow][iColumn]==0)
                       continue;
                   for(P nextPlace:nextTransitionList.get(iColumn).nextList)
                       pn.connect(newTr, nextPlace);
               }
           }
       }
    }
    
}
