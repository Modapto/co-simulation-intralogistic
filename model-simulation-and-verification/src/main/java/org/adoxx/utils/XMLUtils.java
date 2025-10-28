package org.adoxx.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.saxon.xpath.XPathFactoryImpl;

public class XMLUtils {
    
    public static Document getXmlDocFromString(String xml) throws Exception{
        return getXmlDocFromIS(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
    
    public static Document getXmlDocFromURI(String xmlFile) throws Exception{
        if(xmlFile.startsWith("http"))
            return getXmlDocFromIS(new URL(xmlFile).openStream());
        else
            return getXmlDocFromIS(new FileInputStream(new File(xmlFile)));
    }
    
    public static Document getXmlDocFromIS(InputStream is) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    return new InputSource(new StringReader(""));
                }
            });
        return builder.parse(is);
    }
    
    public static String getStringFromXmlDoc(org.w3c.dom.Node node) throws Exception{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
    
    public static Document createNewDocument() throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        //dbf.setIgnoringElementContentWhitespace(true);
        return dbf.newDocumentBuilder().newDocument();
    }
    /*
    public static String escapeXMLField(String field){
        if(field.contains("&")){
            int index = 0;
            do{
                index = field.indexOf("&", index);
                if(index != -1 && !field.substring(index).startsWith("&amp;"))
                    field = field.substring(0, index) + "&amp;" + field.substring(index + 1, field.length());
                if(index != -1)
                    index++;
            }while(index!=-1);
        }
        field = field.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
        return field;
    }
    */
    
    public static String escapeXMLField(String data){
        return escapeXMLField(data, true);
    }
    
    public static String escapeXMLField(String data, boolean onlyUnescapedCharacter){
        if(onlyUnescapedCharacter)
            return data.replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&", "&amp;")
                    .replace(">", "&gt;")
                    .replace("<", "&lt;")
                    .replace("'", "&apos;")
                    .replace("\"", "&quot;");
        else
            return data.replace("&", "&amp;")
                       .replace("\"", "&quot;")
                       .replace("'", "&apos;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;");
    }
    
    public static String escapeXPathField(String field) {
        Matcher matcher = Pattern.compile("['\"]").matcher(field);
        StringBuilder buffer = new StringBuilder("concat(");
        int start = 0;
        while (matcher.find()) {
            buffer.append("'").append(field.substring(start, matcher.start())).append("',");
            buffer.append("'".equals(matcher.group()) ? "\"'\"," : "'\"',");
            start = matcher.end();
        }
        if (start == 0)
            return "'" + field + "'";
        return buffer.append("'").append(field.substring(start)).append("'").append(")").toString();
    }
    
    //private static XPath xPath = XPathFactory.newInstance().newXPath();
    
    public static Object execXPath(org.w3c.dom.Node node, String pattern, QName xPathConstantsType) throws Exception{
        //XPathFactory factory = XPathFactory.newInstance();
        XPathFactory factory = new XPathFactoryImpl();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        XPath xPath = factory.newXPath();
        return xPath.compile(pattern).evaluate(node, xPathConstantsType);
    }
    
    public static Object execXPath(org.w3c.dom.Node node, XPathExpression expression, QName xPathConstantsType) throws Exception{
        return expression.evaluate(node, xPathConstantsType);
    }
    
    public static XPathExpression createXPathQuery(String pattern) throws Exception{
        //XPathFactory factory = XPathFactory.newInstance();
        XPathFactory factory = new XPathFactoryImpl();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        XPath xPath = factory.newXPath();
        return xPath.compile(pattern);
    }
}
