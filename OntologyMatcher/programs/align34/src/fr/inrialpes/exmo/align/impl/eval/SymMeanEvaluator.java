/*
 * $Id: SymMeanEvaluator.java 690 2008-03-31 10:26:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2004-2005, 2007-2008
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

package fr.inrialpes.exmo.align.impl.eval; 

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicEvaluator;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.onto.LoadedOntology;

import java.lang.Math;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;

/**
 * Evaluate proximity between two alignments.
 * This function implements a simple weighted symetric difference.
 * The highest the value the closest are the alignments:
 * 1: the alignments are exactly the same, with the same strenghts
 * 0: the alignments do not share a single cell
 *
 * The result is 2*w(A\cap B)/|A|+|B|
 * in which w(.) is the sum of the complement of the strength diference between same cells
 * i.e., \Sum_{c\in A, c'\in R; c=c'} (1 - | c.strength - c'.strength |)
 *
 * @author Jerome Euzenat
 * @version $Id: SymMeanEvaluator.java 690 2008-03-31 10:26:08Z euzenat $ 
 */

public class SymMeanEvaluator extends BasicEvaluator {
    private double classScore = 0.;
    private double propScore = 0.;
    private double indScore = 0.;

    /** Creation **/
    public SymMeanEvaluator( Alignment align1, Alignment align2 ) throws AlignmentException {
	super(align1,align2);
	if ( !( align1 instanceof ObjectAlignment ) ||
	     !( align2 instanceof ObjectAlignment ) )
	    throw new AlignmentException( "Alignments should be ObjectAlignments, try to " );
    }

    public double eval(Parameters params) throws AlignmentException {
	return eval( params, (Object)null );
    }
    public double eval(Parameters params, Object cache) throws AlignmentException {
	int nbClassCell = 0;
	int nbPropCell = 0;
	int nbIndCell = 0;
	result = 0.;
	classScore = 0.;
	propScore = 0.;
	indScore = 0.;
	LoadedOntology onto1 = (LoadedOntology)((ObjectAlignment)align1).getOntologyObject1();
	LoadedOntology onto2 = (LoadedOntology)((ObjectAlignment)align2).getOntologyObject1();
	
	//for ( Cell c1 : align1.getElements() ){
	for (Enumeration e = align1.getElements() ; e.hasMoreElements() ;) {
	    Cell c1 = (Cell)e.nextElement();
	    if ( onto1.isClass( c1.getObject1() ) ) nbClassCell++;
	    else if ( onto1.isProperty( c1.getObject1() ) ) nbPropCell++;
	    else nbIndCell++;
	    Set<Cell> s2 = (Set<Cell>)align2.getAlignCells1( c1.getObject1() );
	    if( s2 != null ){
		for ( Cell c2: s2 ){
		    if ( c1.getObject2() == c2.getObject2() ) {
			if ( onto2.isClass( c1.getObject2() ) ) {
			    classScore = classScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			} else if ( onto2.isProperty( c1.getObject2() ) ) {
			    propScore = propScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());
			} else {
			    indScore = indScore + 1 - Math.abs(c2.getStrength() - c1.getStrength());}}}}}
		
	//for( Cell c2: align2.getElements() ) {
	for (Enumeration e = align2.getElements() ; e.hasMoreElements() ;) {
	    Cell c2 = (Cell)e.nextElement();
	    if ( onto1.isClass( c2.getObject1() ) ) nbClassCell++ ;
	    else if ( onto1.isProperty( c2.getObject1() ) ) nbPropCell++;
	    else nbIndCell++;
	}
		
	// Beware, this must come first
	result = 2*(classScore+propScore+indScore) / (nbClassCell+nbPropCell+nbIndCell);
	classScore = 2*classScore / nbClassCell;
	propScore = 2*propScore / nbPropCell;
	indScore = 2*indScore / nbIndCell;
	return(result);
    }

    public void write( PrintWriter writer ) throws java.io.IOException {
	writer.print("<rdf:RDF>\n  <Evaluation class=\"SymMeanEvaluator\">\n    <class>");
 	writer.print(classScore);
	writer.print("</class>\n    <properties>");
 	writer.print(propScore);
	writer.print("</properties>\n    <individuals>");
 	writer.print(indScore);
	writer.print("</individuals>\n    <result>");
 	writer.print(result);
 	writer.print("</result>\n  </Evaluation>\n</rdf:RDF>\n");
    }

}
