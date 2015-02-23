/*
 * $Id: Similarity.java 690 2008-03-31 10:26:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2004, 2006-2008
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

import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.onto.LoadedOntology;

/**
 * Represents the implementation of a similarity measure
 *
 * @author J�r�me Euzenat
 * @version $Id: Similarity.java 690 2008-03-31 10:26:08Z euzenat $ 
 */

public interface Similarity
{
    // These parameters contains usually:
    // ontology1 and ontology2
    // It would be better if they where explicit...
    // Apparently the initialize also compute the similarity

	// JE: OntoRewr: This should not be in init
    public void initialize( LoadedOntology<Object> onto1, LoadedOntology<Object> onto2 );
    public void initialize( LoadedOntology<Object> onto1, LoadedOntology<Object> onto2, Alignment align );
    public void compute( Parameters p );
    public double getClassSimilarity( Object c1, Object c2 );
    public double getPropertySimilarity( Object p1, Object p2);
    public double getIndividualSimilarity( Object i1, Object i2 );

    public void printClassSimilarityMatrix( String type );
    public void printPropertySimilarityMatrix( String type );
    public void printIndividualSimilarityMatrix( String type );

    // New implementation
    // JE: this is better as a new implementation.
    // however, currently the implementation does not follow it:
    // the abstract matrix class provides the get- accessors and the 
    // concrete classes implement measure as their computation function.
    // This is not clean. What should be done is:
	// JE: OntoRewr: to be suppressed
    public double measure( Object c1, Object c2 ) throws Exception;
    public double classMeasure( Object c1, Object c2 ) throws Exception;
    public double propertyMeasure( Object p1, Object p2) throws Exception;
    public double individualMeasure( Object i1, Object i2 ) throws Exception;
}

