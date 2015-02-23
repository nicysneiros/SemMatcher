/*
 * $Id: HTMLRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2006-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.renderer; 

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectCell;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Renders an alignment in HTML
 *
 * TODO:
 * - add CSS categories
 * - add resource chooser
 *
 * @author J�r�me Euzenat
 * @version $Id: HTMLRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $ 
 */

public class HTMLRendererVisitor implements AlignmentVisitor
{
    
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    public HTMLRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Parameters p ) {
	if ( p.getParameter( "embedded" ) != null 
	     && !p.getParameter( "embedded" ).equals("") ) embedded = true;
    };

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	nslist = new Hashtable<String,String>();
	nslist.put(Annotations.ALIGNNS,"align");
	nslist.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
	nslist.put("http://www.w3.org/2001/XMLSchema#","xsd");
	//nslist.put("http://www.omwg.org/TR/d7/ontology/alignment","omwg");
	// Get the keys of the parameter
	int gen = 0;
	for ( Object ext : ((BasicParameters)align.getExtensions()).getValues() ){
	    String prefix = ((String[])ext)[0];
	    String name = ((String[])ext)[1];
	    String tag = (String)nslist.get(prefix);
	    if ( tag == null ) {
		tag = "ns"+gen++;
		nslist.put( prefix, tag );
	    }
	    if ( tag.equals("align") ) { tag = name; }
	    else { tag += ":"+name; }
	    //extensionString += "  <"+tag+">"+((String[])ext)[2]+"</"+tag+">\n";
	}
	if ( embedded == false ) {
	    writer.print("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
	    writer.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n");
	}
	writer.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"");
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    writer.print("\n       xmlns:"+nslist.get(k)+"='"+k+"'");
	}
	writer.print(">\n<head><title>Alignment</title></head>\n<body>\n");
	writer.print("<h1></h1>\n");
	writer.print("<div typeof=\"align:Alignment\">\n");
	writer.print("<h2>Alignment metadata</h2>\n");
	writer.print("<table border=\"0\">\n");
	writer.print("<tr><td>onto1</td><td><div rel=\"align:onto1\"><div typeof=\"align:Ontology\" about=\""+align.getOntology1URI()+"\">");
	writer.print("<table>\n<tr><td>uri: </td><td>"+align.getOntology1URI()+"</td></tr>\n");
	if ( align.getFile1() != null )
	    writer.print("<tr><td><span property=\"align:location\" content=\""+align.getFile1()+"\"/>file:</td><td><a href=\""+align.getFile1()+"\">"+align.getFile1()+"</a></td></tr>\n" );
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject1().getFormalism() != null ) {
	    writer.print("<tr><td>type:</td><td><span rel=\"align:formalism\"><span typeof=\"align:Formalism\"><span property=\"align:name\">"+((BasicAlignment)align).getOntologyObject1().getFormalism()+"</span><span property=\"align:uri\" content=\""+((BasicAlignment)align).getOntologyObject1().getFormURI()+"\"/></span></span></td></tr>");
	}
	writer.print("</table>\n</div></div></td></tr>\n");
	writer.print("<tr><td>onto2</td><td><div rel=\"align:onto2\"><div typeof=\"align:Ontology\" about=\""+align.getOntology2URI()+"\">");
	writer.print("<table>\n<tr><td>uri: </td><td>"+align.getOntology2URI()+"</td></tr>\n");
	if ( align.getFile2() != null )
	    writer.print("<tr><td><span property=\"align:location\" content=\""+align.getFile2()+"\"/>file:</td><td><a href=\""+align.getFile2()+"\">"+align.getFile2()+"</a></td></tr>\n" );
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject2().getFormalism() != null ) {
	    writer.print("<tr><td>type:</td><td><span rel=\"align:formalism\"><span typeof=\"align:Formalism\"><span property=\"align:name\">"+((BasicAlignment)align).getOntologyObject2().getFormalism()+"</span><span property=\"align:uri\" content=\""+((BasicAlignment)align).getOntologyObject2().getFormURI()+"\"/></span></span></td></tr>");
	}
	writer.print("</table>\n</div></div></td></tr>\n");
	writer.print("<tr><td>level</td><td property=\"align:level\">"+align.getLevel()+"</td></tr>\n" );
	writer.print("<tr><td>type</td><td property=\"align:type\">"+align.getType()+"</td></tr>\n" );
	// RDFa: Get the keys of the parameter (to test)
	for ( Object ext : ((BasicParameters)align.getExtensions()).getValues() ){
	    writer.print("<tr><td>"+((String[])ext)[0]+" : "+((String[])ext)[1]+"</td><td property=\""+nslist.get(((String[])ext)[0])+":"+((String[])ext)[1]+"\">"+((String[])ext)[2]+"</td></tr>\n");
	}
	writer.print("</table>\n");
	writer.print("<h2>Correspondences</h2>\n");
	writer.print("<div rel=\"align:map\"><table><tr><td>object1</td><td>relation</td><td>strength</td><td>object2</td><td>Id</td></tr>\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("</table>\n");
	writer.print("</div></div>\n");
	writer.print("</body>\n</html>\n");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	URI u1, u2;
	// JE: I think that now these two clauses should be unified (3.4)
	//if ( cell instanceof ObjectCell ) {
	//    u1 = ((LoadedOntology)((BasicAlignment)alignment).getOntologyObject1()).getEntityURI( cell.getObject1() );
	//    u2 = ((LoadedOntology)((BasicAlignment)alignment).getOntologyObject2()).getEntityURI( cell.getObject2() );
	//} else {
	    u1 = cell.getObject1AsURI( alignment );
	    u2 = cell.getObject2AsURI( alignment );
	    //}
	writer.print(" <tr typeof=\"align:Cell\">");
	writer.print("<td rel=\"align:entity1\" href=\""+u1+"\">"+u1+"</td><td property=\"align:relation\">");
	cell.getRelation().accept( this );
	writer.print("</td><td property=\"align:measure\" datatype=\"xsd:float\">"+cell.getStrength()+"</td>");
	writer.print("<td rel=\"align:entity2\" href=\""+u2+"\">"+u2+"</td>");
	if ( cell.getId() != null ) {
	    String id = cell.getId();
	    // Would be useful to test for the Alignment URI
	    if ( id.startsWith( (String)alignment.getExtension( Annotations.ALIGNNS, Annotations.ID ) ) ){
		writer.print("<td>"+id.substring( id.indexOf( '#' ) )+"</td>");
	    } else {
		writer.print("<td>"+id+"</td>");
	    }
	} else writer.print("<td></td>");
	//if ( !cell.getSemantics().equals("first-order") )
	//	writer.print("      <semantics>"+cell.getSemantics()+"</semantics>\n");
	writer.println("</tr>");
    }
    public void visit( Relation rel ) {
	rel.write( writer );
    };
}
