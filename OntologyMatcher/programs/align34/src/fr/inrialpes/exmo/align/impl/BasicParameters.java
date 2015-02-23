/*
 * $Id: BasicParameters.java 655 2008-02-28 20:43:51Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2004-2005, 2008
 * Copyright (C) University of Montr�al, 2004
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl; 

// import java classes
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collection;
import java.io.PrintStream;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.semanticweb.owl.align.Parameters;

/**
  *
  * Standard parameter list structure to be used everywhere.
  * By default and for means of communication, parameter names and values 
  * are Strings (even if their type is Object).
  * 
  * @author J�r�me Euzenat
  * @version $Id: BasicParameters.java 655 2008-02-28 20:43:51Z euzenat $ 
 */

public class BasicParameters implements Parameters {
 
  /** The list of unlinked out  XML_Port */
  Hashtable<String,Object> parameters = null;
    
  public BasicParameters() {
      parameters = new Hashtable<String,Object>();
  }
  
  public void setParameter(String name, Object value){
    parameters.put(name,value);
  }

  public void unsetParameter(String name){
    parameters.remove(name);
  }

  public Object getParameter(String name){
    return parameters.get(name);
  }

  public Enumeration<String> getNames(){
    return parameters.keys();
  }

  public Collection getValues(){
    return parameters.values();
  }

    public void write(){
	System.out.println("<?xml version='1.0' ?>");
	System.out.println("<Parameters>");
	for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
	    String k = (String)e.nextElement();
	    System.out.println("  <param name='"+k+"'>"+parameters.get(k)+"</param>");
	}
	System.out.println("</Parameters>");
    }

    /**
     * displays the current parameters (debugging)
     */
    public void displayParameters( PrintStream stream ){
	stream.println("Parameters:");
	for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
	    String k = (String)e.nextElement();
	    stream.println("  "+k+" = "+parameters.get(k));
	}
    }

    public static Parameters read(String filename){
	return read(new BasicParameters(), filename);
    }

    public static Parameters read(Parameters p, String filename){
	try {
	    // open the stream
	    DocumentBuilderFactory docBuilderFactory =
		DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc = docBuilder.parse(new File(filename));

	    // normalize text representation
	    doc.getDocumentElement().normalize();

	    // Get the params
	    NodeList paramList = doc.getElementsByTagName("param");
	    int totalParams = paramList.getLength();
	    for (int s = 0; s < totalParams; s++) {
		Element paramElement = (Element)paramList.item(s);
		String paramName = paramElement.getAttribute("name");
		NodeList paramContent = paramElement.getChildNodes();
		String paramValue =((Node)paramContent.item(0)).getNodeValue().trim();
		p.setParameter(paramName, paramValue); 
	    }
	} catch (SAXParseException err) {
	    System.err.println("** Parsing error: ["+ err.getLineNumber()+"]: "+err.getSystemId()); 
	    System.err.println(" " + err.getMessage());
	} catch (SAXException e) {
	    Exception x = e.getException();
	    ((x == null) ? e : x).printStackTrace();
	} catch (Throwable t) {	t.printStackTrace(); }

	return p;
    }
}
