/*
 * $Id: SKOSRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2004, 2006-2008
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
import java.io.PrintWriter;
import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author J�r�me Euzenat
 * @version $Id: SKOSRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $ 
 */

public class SKOSRendererVisitor implements AlignmentVisitor {
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    public SKOSRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Parameters p ) {
	if ( p.getParameter( "embedded" ) != null 
	     && !p.getParameter( "embedded" ).equals("") ) embedded = true;
    };

    // This must be considered
    public void visit( Alignment align ) throws AlignmentException {
	if ( align instanceof ObjectAlignment ) {
	    onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	    onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	}
	alignment = align;
	writer.print("<rdf:RDF\n");
	writer.print("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"); 
	writer.print("  xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n");
	writer.print("  xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("</rdf:RDF>\n");
    }

    public URI getURI2() throws AlignmentException {
	if ( onto2 != null ) {
	    return onto2.getEntityURI( cell.getObject2() );
	} else {
	    return cell.getObject2AsURI( alignment );
	}
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	if ( onto1 != null ) {
	    writer.print("  <skos:Concept rdf:about=\""+onto1.getEntityURI( cell.getObject1() )+"\">\n");
	} else {
	    writer.print("  <skos:Concept rdf:about=\""+cell.getObject1AsURI( alignment )+"\">\n");
	}
	cell.getRelation().accept( this );
	writer.print("  </skos:Concept>\n\n");
    }
    public void visit( EquivRelation rel ) throws AlignmentException {
	writer.print("    <skos:related rdf:resource=\""+getURI2()+"\"/>\n");
    }
    public void visit( SubsumeRelation rel ) throws AlignmentException {
	writer.print("    <skos:narrower rdf:resource=\""+getURI2()+"\"/>\n");
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	writer.print("    <skos:broader rdf:resource=\""+getURI2()+"\"/>\n");
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	throw new AlignmentException("Cannot translate in SKOS"+rel);
    }
    public void visit( Relation rel ) throws AlignmentException {
	// JE: I do not understand why I need this,
	// but this seems to be the case...
	try {
	    Method mm = null;
	    if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation").isInstance(rel) ){
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {rel});
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) { 
	    e.printStackTrace();
	}
    };
}
