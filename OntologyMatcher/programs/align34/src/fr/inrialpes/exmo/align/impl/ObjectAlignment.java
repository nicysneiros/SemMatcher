/*
 * $Id: ObjectAlignment.java 756 2008-07-17 15:30:07Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2008
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

package fr.inrialpes.exmo.align.impl;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.net.URI;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.xml.sax.SAXException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.onto.OntologyFactory;
import fr.inrialpes.exmo.align.onto.OntologyCache;
import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information about those
 * objects.
 *
 * @author J�r�me Euzenat
 * @version $Id: ObjectAlignment.java 756 2008-07-17 15:30:07Z euzenat $
 */

public class ObjectAlignment extends BasicAlignment {

    protected ObjectAlignment init = null;

    public ObjectAlignment() {}

    public void init(Object onto1, Object onto2) throws AlignmentException {
	init( onto1, onto2, (OntologyCache)null );
    }

    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
	OntologyCache cache = null;
	if ( ontologies instanceof OntologyCache ) cache = (OntologyCache)ontologies;
	else cache = (OntologyCache)null;
	if ( (o1 instanceof LoadedOntology && o2 instanceof LoadedOntology) ){
	    super.init( o1, o2, ontologies );
	} else if ( o1 instanceof URI && o2 instanceof URI ) {
		super.init( loadOntology( (URI)o1, cache ),
			    loadOntology( (URI)o2, cache ) );
	} else {
	    throw new AlignmentException("Arguments must be LoadedOntology or URI");
	};
    }

    public LoadedOntology<Object> ontology1(){
	return (LoadedOntology<Object>)onto1;
    }

    public LoadedOntology<Object> ontology2(){
	return (LoadedOntology<Object>)onto2;
    }

    public void loadInit( Alignment al ) throws AlignmentException {
	loadInit( al, (OntologyCache)null );
    }

    public void loadInit( Alignment al, OntologyCache ontologies ) throws AlignmentException {
	if ( al instanceof URIAlignment ) {
	    try { init = toObjectAlignment( (URIAlignment)al, ontologies );
	    } catch (SAXException e) { e.printStackTrace(); }
	} else if ( al instanceof ObjectAlignment ) {
	    init = (ObjectAlignment)al;
	}
    }

    public URI getOntology1URI() { return onto1.getURI(); };

    public URI getOntology2URI() { return onto2.getURI(); };

    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return new ObjectCell( id, ob1, ob2, relation, measure);
    }

    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement
    // It has two difficulties
    // - it should call the current init() and not that of BasicAlignement
    // - it should catch the AlignmentException that it is supposed to raise
    public Object clone() {
	ObjectAlignment align = new ObjectAlignment();
	try {
	    align.init( onto1, onto2 );
	} catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Object ext : ((BasicParameters)extensions).getValues() ){
	    align.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	}
	align.setExtension( Annotations.ALIGNNS, "method", "http://exmo.inrialpes.fr/align/impl/ObjectAlignment#clone" );
	String oldid = align.getExtension( Annotations.ALIGNNS, "id" );
	if ( oldid != null && !oldid.equals("") ) {
	    align.setExtension( Annotations.ALIGNNS, "derivedFrom", oldid );
	    align.getExtensions().unsetParameter( Annotations.ALIGNNS+"id" );
	}
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { ex.printStackTrace(); }
	return align;
    }

    /**
     * This is a clone with the URI instead of Object objects
     *
     */
    public URIAlignment toURIAlignment() throws AlignmentException {
	URIAlignment align = new URIAlignment();
	align.init( getOntology1URI(), getOntology2URI() );
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( Object ext : ((BasicParameters)extensions).getValues() ){
	    align.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	}
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    try {
		align.addAlignCell( c.getId(), c.getObject1AsURI(this), c.getObject2AsURI(this), c.getRelation(), c.getStrength() );
	    } catch (AlignmentException aex) {
		// Sometimes URIs are null, this is ignore
	    }
	};
	return align;
    }

    static public ObjectAlignment toObjectAlignment( URIAlignment al, OntologyCache ontologies ) throws AlignmentException, SAXException {
	ObjectAlignment alignment = new ObjectAlignment();
	alignment.init( al.getFile1(), al.getFile2(), ontologies );
	alignment.setType( al.getType() );
	alignment.setLevel( al.getLevel() );
	for ( Object ext : ((BasicParameters)al.getExtensions()).getValues() ){
	    alignment.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	}
	LoadedOntology<Object> o1 = (LoadedOntology<Object>)alignment.getOntologyObject1(); // [W:unchecked]
	LoadedOntology<Object> o2 = (LoadedOntology<Object>)alignment.getOntologyObject2(); // [W:unchecked]
	for (Enumeration e = al.getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    alignment.addAlignCell( c.getId(), 
				    o1.getEntity( c.getObject1AsURI(alignment) ),
				    o2.getEntity( c.getObject2AsURI(alignment) ),
				    c.getRelation(), 
				    c.getStrength(),
				    c.getExtensions() );
	};
	return alignment;
    }

    static LoadedOntology loadOntology( URI ref, OntologyCache ontologies ) throws AlignmentException {
	OntologyFactory factory = OntologyFactory.newInstance();
	return factory.loadOntology( ref, ontologies );
    }
}

