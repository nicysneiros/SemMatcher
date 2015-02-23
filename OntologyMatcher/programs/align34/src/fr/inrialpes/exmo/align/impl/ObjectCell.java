/*
 * $Id: ObjectCell.java 728 2008-06-25 14:27:23Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005, 2007-2008
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

import java.net.URI;
import java.util.Enumeration;

import org.xml.sax.ContentHandler;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

/**
 * Represents an ontology alignment correspondence.
 *
 * @author J�r�me Euzenat
 * @version $Id: ObjectCell.java 728 2008-06-25 14:27:23Z euzenat $
 */

public class ObjectCell extends BasicCell {
    // JE ??: implements Comparable<ObjectCell> {

    //    public void accept( AlignmentVisitor visitor) throws AlignmentException {
    //  visitor.visit( this );
    //}

    /** Creation **/
    public ObjectCell( String id, Object ob1, Object ob2, Relation rel, double m ) throws AlignmentException {
	super( id, ob1, ob2, rel, m );
    };


    // the strength must be compared with regard to abstract types
    public boolean equals( Cell c ) {
	if ( c instanceof ObjectCell ){
	    return ( object1 == c.getObject1() && object2 == c.getObject2() && strength == c.getStrength() && (relation.equals( c.getRelation() )) );
	} else {
	    return false;
	}
    }

    /**
     * Used to order the cells in an alignment:
     * -- this > c iff this.getStrength() < c.getStrength() --
    public int compareTo( Cell c ){
	//if ( ! (c instanceof Cell) ) return 1;
	if ( c.getStrength() > getStrength() ) return 1;
	if ( getStrength() > c.getStrength() ) return -1;
	return 0;
    }
     */

    public URI getObject1AsURI( Alignment al ) throws AlignmentException {
	if ( al instanceof BasicAlignment ) {
	    Object ontology = ((BasicAlignment)al).getOntologyObject1();
	    if ( ontology instanceof LoadedOntology ) {
		return ((LoadedOntology)ontology).getEntityURI( object1 );
	    }
	};
	if ( object1 instanceof URI ) {
	    return (URI)object1;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object1 );
	}
    }
    public URI getObject2AsURI( Alignment al ) throws AlignmentException {
	if ( al instanceof BasicAlignment ) {
	    Object ontology = ((BasicAlignment)al).getOntologyObject2();
	    if ( ontology instanceof LoadedOntology ) {
		return ((LoadedOntology)ontology).getEntityURI( object2 );
	    }
	};
        if ( object2 instanceof URI ) {
	    return (URI)object2;
	} else {
	    throw new AlignmentException( "Cannot find URI for "+object2 );
	}
    }
    public Cell inverse() throws AlignmentException {
	Cell result = (Cell)new ObjectCell( (String)null, object2, object1, relation.inverse(), strength );
	if ( extensions != null ) {
	    for ( Object ext : ((BasicParameters)extensions).getValues() ){
		result.setExtension( ((String[])ext)[0], ((String[])ext)[1], ((String[])ext)[2] );
	    }
	    result.getExtensions().unsetParameter( Annotations.ALIGNNS+Annotations.ID );
	}
	// The sae should be done for the measure
	return result;
    }

    public Cell compose(Cell c) throws AlignmentException {
    	if (!object2.equals(c.getObject1()) && relation.compose(c.getRelation())==null )
    		return null;
    	Cell result = (ObjectCell)new ObjectCell( (String)null, object1, c.getObject2(), relation.compose(c.getRelation()), strength*c.getStrength() );
    	// TODO : extension...
    	return result;
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

}

