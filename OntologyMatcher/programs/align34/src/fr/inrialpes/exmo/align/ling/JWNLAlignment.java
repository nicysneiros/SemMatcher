/*
 * $Id: JWNLAlignment.java 690 2008-03-31 10:26:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005, 2007
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

package fr.inrialpes.exmo.align.ling; 

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import java.net.URI;

/**
 * This Class uses JWNLDistances to align two ontologies.
 * @author  Jerome Pierson
 * @version $Id: JWNLAlignment.java,v 1.0 2004/08/04 
 */

public class JWNLAlignment extends DistanceAlignment implements AlignmentProcess {

    final static String WNVERS = "3.0";

    protected class SynonymMatrixMeasure extends MatrixMeasure {
	protected JWNLDistances Dist = null;

	public SynonymMatrixMeasure() {
	    Dist = new JWNLDistances();
	}
	public void init() throws AlignmentException {
	    Dist.Initialize();
	}
	public void init( String wndict, String wnvers ) throws AlignmentException {
	    Dist.Initialize( wndict, wnvers );
	}
	public void init( String wndict ) throws AlignmentException {
	    Dist.Initialize( wndict, WNVERS );
	}
	public double measure( Object o1, Object o2 ) throws Exception {
	    String s1 = ontology1().getEntityName( o1 );
	    String s2 = ontology2().getEntityName( o2 );
	    if ( s1 == null || s2 == null ) return 1.;
	    return Dist.BasicSynonymDistance(s1.toLowerCase(),s2.toLowerCase());
	}
	public double classMeasure( Object cl1, Object cl2 ) throws Exception {
	    return measure( cl1, cl2 );
	}
	public double propertyMeasure( Object pr1, Object pr2 ) throws Exception {
	    return measure( pr1, pr2 );
	}
	public double individualMeasure( Object id1, Object id2 ) throws Exception {
	    if ( debug > 4 ) 
			System.err.println( "ID:"+id1+" -- "+id2);
	    return measure( id1, id2 );
	}
    }

    /** Creation **/
    public JWNLAlignment(){
	setSimilarity( new SynonymMatrixMeasure() );
	setType("**");
    };

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException {
	loadInit( alignment );
	SynonymMatrixMeasure sim = (SynonymMatrixMeasure)getSimilarity();
	String wnvers = (String)params.getParameter("wnvers");
	if ( wnvers == null ) wnvers = WNVERS;
	sim.init( (String)params.getParameter("wndict"), wnvers );
	sim.initialize( ontology1(), ontology2(), alignment );
	getSimilarity().compute( params );
      if ( params.getParameter("printMatrix") != null ) printDistanceMatrix(params);
	extract( type, params );
    }
}
	
	
	
	
	
	
			
