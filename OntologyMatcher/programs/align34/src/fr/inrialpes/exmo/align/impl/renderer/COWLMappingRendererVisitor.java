/*
 * $Id: COWLMappingRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2004, 2007-2008
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

import java.net.URI;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author J�r�me Euzenat
 * @version $Id: COWLMappingRendererVisitor.java 779 2008-08-26 17:05:38Z euzenat $ 
 */


public class COWLMappingRendererVisitor implements AlignmentVisitor
{
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;

    public COWLMappingRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Parameters p ) {}

    public void visit( Alignment align ) throws AlignmentException {
	if ( !(align instanceof ObjectAlignment) )
	    throw new AlignmentException("COWLMappingRenderer: cannot render simple alignment. Turn them into ObjectAlignment, by toObjectAlignement()");
	alignment = align;
	onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	writer.print("<rdf:RDF\n");
	writer.print("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n");
	writer.print("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
	writer.print("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" \n");
	writer.print("    xmlns:cowl=\"http://www.itc.it/cowl#\" \n");
	writer.print("    xml:base=\"http://www.itc.it/cowl#\" \n");
	writer.print("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n\n");	
	writer.print("  <cowl:Mapping rdf:ID=\"\">\n");
	writer.print("    <cowl:sourceOntology>\n");
	writer.print("      <owl:Ontology rdf:about=\""+onto1.getURI()+"\"/>\n");
	writer.print("    </cowl:sourceOntology>\n");
	writer.print("    <cowl:targetOntology>\n");
	writer.print("      <owl:Ontology rdf:about=\""+onto2.getURI()+"\"/>\n");
	writer.print("    </cowl:targetOntology>\n");
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	} //end for
	writer.print("  </cowl:Mapping>\n");
	writer.print("</rdf:RDF>\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	writer.print("    <cowl:bridgeRule>\n");
	cell.getRelation().accept( this );
	writer.print("    </cowl:bridgeRule>\n");
    }
    public void visit( EquivRelation rel ) throws AlignmentException {
	    writer.print("     <cowl:Equivalent>\n");
	    writer.print("       <cowl:source>\n");
	    printObject(cell.getObject1(),onto1);
	    writer.print("       </cowl:source>\n");
	    writer.print("       <cowl:target>\n");
	    printObject(cell.getObject2(),onto2);
	    writer.print("       </cowl:target>\n");
	    writer.print("     </cowl:Equivalent>\n");
    }

    public void visit( SubsumeRelation rel ) throws AlignmentException {
	    writer.print("     <cowl:Into>\n");
	    writer.print("       <cowl:source>\n");
	    printObject(cell.getObject1(),onto1);
	    writer.print("       </cowl:source>\n");
	    writer.print("       <cowl:target>\n");
	    printObject(cell.getObject2(),onto2);
	    writer.print("       </cowl:target>\n");
	    writer.print("     </cowl:Into>\n");
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	    writer.print("     <cowl:Onto>\n");
	    writer.print("       <cowl:source>\n");
	    printObject(cell.getObject1(),onto1);
	    writer.print("       </cowl:source>\n");
	    writer.print("       <cowl:target>\n");
	    printObject(cell.getObject2(),onto2);
	    writer.print("       </cowl:target>\n");
	    writer.print("     </cowl:Onto>\n");
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	    writer.print("     <cowl:INCOMPATIBLE>\n");
	    writer.print("       <cowl:source>\n");
	    printObject(cell.getObject1(),onto1);
	    writer.print("       </cowl:source>\n");
	    writer.print("       <cowl:target>\n");
	    printObject(cell.getObject2(),onto2);
	    writer.print("       </cowl:target>\n");
	    writer.print("     </cowl:INCOMPATIBLE>\n");
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
    }

    public void printObject( Object ob, LoadedOntology onto ) throws AlignmentException {
	if ( onto.isClass( ob )  ) {
	    writer.print("         <owl:Class rdf:about=\""+onto.getEntityURI(ob)+"\"/>\n");
	} else if ( onto.isDataProperty( ob ) ) { 
	    writer.print("         <owl:DataProperty rdf:about=\""+onto.getEntityURI(ob)+"\"/>\n");
	} else if ( onto.isObjectProperty( ob ) ) { 
	    writer.print("         <owl:ObjectProperty rdf:about=\""+onto.getEntityURI(ob)+"\"/>\n");
	} else if ( onto.isIndividual( ob ) ) {
	    writer.print("         <owl:Individual rdf:about=\""+onto.getEntityURI(ob)+"\"/>\n");
	}
    }
}
