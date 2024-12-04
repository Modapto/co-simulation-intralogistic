package org.adoxx.pn.input.mapping.data;

public class MapElement{
    
    public class FromTo{ public String from = ""; public String to = ""; public int placeToken = 0;}
    public class RelationElement{ public String relationType = ""; public String element = "";}
    
    public String[] typeList = new String[0];
    public FromTo[] mappingFromToList = new FromTo[0];
    public RelationElement[] inRelationElementList = new RelationElement[0];
    public RelationElement[] outRelationElementList = new RelationElement[0];
    
    public String getInRelationElement(String relationType) throws Exception{
        for(RelationElement relationElement: inRelationElementList)
            if(relationElement.relationType.equals(relationType))
                return relationElement.element;
        throw new Exception("ERROR: input relation of type " + relationType + " not defined for element of type " + getTypes());
    }
    
    public String getOutRelationElement(String relationType) throws Exception{
        for(RelationElement relationElement: outRelationElementList)
            if(relationElement.relationType.equals(relationType))
                return relationElement.element;
        throw new Exception("ERROR: output relation of type " + relationType + " not defined for element of type " + getTypes());
    }
    
    public String getTypes(){
        String ret = "";
        for(String type:typeList)
            ret += type+"|";
        return ret;
    }
}
