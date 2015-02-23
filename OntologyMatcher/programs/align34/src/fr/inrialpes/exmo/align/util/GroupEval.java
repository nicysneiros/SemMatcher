/*
 * $Id: GroupEval.java 803 2008-08-30 22:40:22Z euzenat $
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2008, INRIA Rh�ne-Alpes
 * Copyright (C) 2004, Universit� de Montr�al
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

/* This program evaluates the results of several ontology aligners in a row.
*/
package fr.inrialpes.exmo.align.util;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.onto.OntologyCache;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.lang.Integer;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Arrays; //VS
import org.xml.sax.SAXException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A basic class for synthesizing the results of a set of alignments provided by
    different algorithms. The output is a table showing various classical measures
    for each test and for each algorithm. Average is also computed as Harmonic means.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.util.GroupEval [options]
    </pre>

    where the options are:
    <pre>
    -o filename --output=filename
    -f format = prfmot (precision/recall/fallout/f-measure/overall/time) --format=prfmot
    -d debug --debug=level
    -r filename --reference=filename
    -s algo/measure
    -l list of compared algorithms
    -t output --type=output: xml/tex/html/ascii
   </pre>

   The input is taken in the current directory in a set of subdirectories (one per
   test which will be rendered by a line) each directory contains a number of
   alignment files (one per algorithms which will be renderer as a column).

    If output is requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: GroupEval.java 803 2008-08-30 22:40:22Z euzenat $
</pre>

@author Sean K. Bechhofer
@author J�r�me Euzenat
    */

public class GroupEval {

    Parameters params = null;
    String filename = null;
    String reference = "refalign.rdf";
    String format = "pr";
    int fsize = 2;
    String type = "html";
    boolean embedded = false;
    String dominant = "s";
    Vector<String> listAlgo = null;
    int debug = 0;
    String color = null;
    //Hashtable loaded = null;
    OntologyCache loaded = null;
    String ontoDir = null;

    public static void main(String[] args) {
	try { new GroupEval().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	String listFile = "";
	LongOpt[] longopts = new LongOpt[10];
	//loaded = new Hashtable();
	loaded = new OntologyCache();

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null, 'f');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("sup", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[6] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[7] = new LongOpt("color", LongOpt.OPTIONAL_ARGUMENT, null, 'c');
	longopts[8] = new LongOpt("reference", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[9] = new LongOpt("directory", LongOpt.REQUIRED_ARGUMENT, null, 'w');

	Getopt g = new Getopt("", args, "ho:a:d::l:f:t:r:w:c::", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output here */
		filename = g.getOptarg();
		break;
	    case 'r' :
		/* File name for the reference alignment */
		reference = g.getOptarg();
		break;
	    case 'f' :
		/* Sequence of results to print */
		format = g.getOptarg();
		break;
	    case 't' :
		/* Type of output (tex/html/xml/ascii) */
		type = g.getOptarg();
		break;
	    case 's' :
		/* Print per type or per algo */
		dominant = g.getOptarg();
		break;
	    case 'c' :
		/* Print colored lines */
		arg = g.getOptarg();
		if ( arg != null )  {
		    color = arg.trim();
		} else color = "lightblue";
		break;
	    case 'l' :
		/* List of filename */
		listFile = g.getOptarg();
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    case 'w' :
		/* Use the given ontology directory */
	    arg = g.getOptarg();
	    if ( arg != null ) ontoDir = g.getOptarg();
	    else ontoDir = null;
		break;
	    }
	}

	// JE: StringTokenizer is obsoleted in Java 1.4 in favor of split: to change
	listAlgo = new Vector<String>();
	StringTokenizer st = new StringTokenizer(listFile,",");
	while (st.hasMoreTokens()) {
	    listAlgo.add(st.nextToken());
	}

	params = new BasicParameters();
	if (debug > 0) params.setParameter("debug", new Integer(debug-1));

	print( iterateDirectories() );
    }

    public Vector<Object> iterateDirectories (){
	Vector<Object> result = null;
	File [] subdir = null;
	try {
		if(ontoDir == null){
		    subdir = (new File(System.getProperty("user.dir"))).listFiles(); 
		}
		else{
			subdir = (new File(ontoDir)).listFiles();
		}
	} catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}
	int size = subdir.length;
        Arrays.sort(subdir); //VS
	result = new Vector<Object>(size);
	int i = 0;
	for ( int j=0 ; j < size; j++ ) {
	    if( subdir[j].isDirectory() ) {
		if ( debug > 0 ) System.err.println("\nEntering directory "+subdir[j]);
		// eval the alignments in a subdirectory
		// store the result
		Object vect = (Object)iterateAlignments( subdir[j] );
		if ( vect != null ){
		    result.add(i, vect);
		    i++;
		}
	    }
	}
	return result;
    }

    public Vector<Object> iterateAlignments ( File dir ) {
	String prefix = dir.toURI().toString()+"/";
	Vector<Object> result = new Vector<Object>();
	boolean ok = false;
	result.add(0,(Object)dir.getName().toString());
	int i = 1;
	// for all alignments there,
	for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ; i++) {
	    // call eval
	    // store the resul in a record
	    // return the record.
	    if ( debug > 2) System.err.println("  Considering result "+i);
	    Evaluator evaluator = (Evaluator)eval( prefix+reference, prefix+(String)e.nextElement()+".rdf");
	    if ( evaluator != null ) ok = true;
	    result.add( i, evaluator );
	}
	// Unload the ontologies.
	loaded.clear();

	if ( ok == true ) return result;
	else return null;
    }

    public Evaluator eval( String alignName1, String alignName2 ) {
	Evaluator eval = null;
	try {
	    int nextdebug;
	    if ( debug < 2 ) nextdebug = 0;
	    else nextdebug = debug - 2;
	    // Load alignments
	    AlignmentParser aparser = new AlignmentParser( nextdebug );
	    Alignment align1 = aparser.parse( alignName1 );
	    if ( debug > 2 ) System.err.println(" Alignment structure1 parsed");
	    aparser.initAlignment( null );
	    Alignment align2 = aparser.parse( alignName2 );
	    if ( debug > 2 ) System.err.println(" Alignment structure2 parsed");
	    // Create evaluator object
	    eval = new PRecEvaluator( align1, align2 );
	    // Compare
	    params.setParameter( "debug", new Integer( nextdebug ) );
	    eval.eval( params, loaded ) ;
	} catch (Exception ex) {
	    if ( debug > 1 ) {
		ex.printStackTrace();
	    } else {
		System.err.println("GroupEval: "+ex);
		System.err.println(alignName1+ " - "+alignName2 );
	    }
	};
	return eval;
    }

    /**
     * This does not only print the results but compute the average as well
     */
    public void print( Vector result ) {
	if ( type.equals("html") ) printHTML( result );
	else if ( type.equals("tex") ) printLATEX( result );
	else if ( type.equals("triangle") ) printTRIANGLE( result );
    }

    public void printTRIANGLE( Vector result ) {
	// variables for computing iterative harmonic means
	int expected = 0; // expected so far
	int foundVect[]; // found so far
	int correctVect[]; // correct so far
	long timeVect[]; // time so far
	    foundVect = new int[ listAlgo.size() ];
	    correctVect = new int[ listAlgo.size() ];
	    timeVect = new long[ listAlgo.size() ];
	    for( int k = listAlgo.size()-1; k >= 0; k-- ) {
		foundVect[k] = 0;
		correctVect[k] = 0;
		timeVect[k] = 0;
	    }
	    for ( Enumeration e = result.elements() ; e.hasMoreElements() ;) {
		int nexpected = -1;
		Vector test = (Vector)e.nextElement();
		Enumeration f = test.elements();
		f.nextElement();
		for( int k = 0 ; f.hasMoreElements() ; k++) {
		    PRecEvaluator eval = (PRecEvaluator)f.nextElement();
		    if ( eval != null ){
			// iterative H-means computation
			if ( nexpected == -1 ){
			    nexpected = 0;
			    expected += eval.getExpected();
			}
			foundVect[k] += eval.getFound();
			correctVect[k] += eval.getCorrect();
			timeVect[k] += eval.getTime();
		    }
		}
	    }
	    System.out.println("\\documentclass[11pt]{book}");
	    System.out.println();
	    System.out.println("\\usepackage{pgf}");
	    System.out.println("\\usepackage{tikz}");
	    System.out.println();
	    System.out.println("\\begin{document}");
	    System.out.println("\\date{today}");
	    System.out.println("");
	    System.out.println("\n%% Plot generated by GenPlot of alignapi");
	    System.out.println("\\begin{tikzpicture}[cap=round]");
	    System.out.println("% Draw grid");
	    System.out.println("\\draw[step=1cm,very thin,color=gray] (-0.2,-0.2) grid (10.0,9.0);");
	    System.out.println("\\draw[|-|] (-0,0) -- (10,0);");
	    System.out.println("%\\draw[dashed,very thin] (0,0) -- (5,8.66) -- (10,0);");
	    System.out.println("\\draw[dashed,very thin] (10,0) arc (0:60:10cm);");
	    System.out.println("\\draw[dashed,very thin] (0,0) arc (180:120:10cm);");

	    System.out.println("\\draw (0,-0.3) node {$recall$}; ");
	    System.out.println("\\draw (10,-0.3) node {$precision$}; ");
	    //System.out.println("\\draw (0,-0.3) node {0.}; ");
	    //System.out.println("\\draw (10,-0.3) node {1.}; ");
	    System.out.println("% Plots");
	    int k = 0;
	    for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ; k++) {
		double precision = (double)correctVect[k]/foundVect[k];
		double recall = (double)correctVect[k]/expected;
		double prec2 = precision*precision;
		double a = ((prec2-(recall*recall)+1)/2);
		double b = java.lang.Math.sqrt( prec2 - (a*a) );
		a = a*10; b = b*10; //for printing scale 10.
		System.out.println("\\draw plot[mark=+,] coordinates {("+a+","+b+")};");
		System.out.println("\\draw ("+(a+.01)+","+(b+.01)+") node[anchor=south west] {"+(String)e.nextElement()+"};");
	    }
	    System.out.println("\\end{tikzpicture}");
	    System.out.println();
	    System.out.println("\\end{document}");
    }

    public void printLATEX( Vector result ) {
    }

    /* A few comments on how and why computing "weighted harmonic means"
       (J�r�me Euzenat)

Let Ai be the found alignment for test i, let Ri be the reference alignment for test i.
Let |A| be the size of A, i.e., the number of correspondences.

Let P(Ri,Ai) and R(Ri,Ai) being precision and recall respectively.

Arithmetic means is \Sum{i=1}{n} P(Ri,Ai) / n and \Sum{i=1}{n} R(Ri,Ai) / n.

Weighted harmonic means is

\Sum{i=1}{n} Wi / \Sum{i=1}{n} (Wi/P(Ri,Ai))
and
\Sum{i=1}{n} Wi / \Sum{i=1}{n} (Wi/R(Ri,Ai))

The goal of using it is that the result be the Precision and Recall of all tests (and not the average precision and recall).

If we take Wi = |Ai\cap Ri|
Then we have exactly this result:

\Sum{i=1}{n} Wi / \Sum{i=1}{n} (Wi/P(Ri,Ai))
                           = P( \cup{i=1}{n} Ri, \cup{i=1}{n} Ai )
(here no two correspondences are equivalent so \cup is a disjunct sum).

[[you can replace Wi by kilometers, Precision by kilometers-per-hour
or you can do the test by yourself to convince you that this is true]]

So our goal is to compute the weighted harmonic means with these weights because this will provide us the true precision and recall.

In fact what the algorithm does is not to compute the harmonic means! I rephrase it, it computes the harmonic means of the numbers above it but since this is equivalent to computing precision and recall, it just computes it!

How?
For each column k in the table (corresponding to an algorithm), it maintains two vectors:
correctVect[k] and foundVect[k]
which is equal to \Sum{i=1}{n} |Ai\cap Ri| and \Sim{i=1}{n} |Ai|
and it additionally stores in "expected" the size of \Sum{i=1}{n} |Ri|

So computing the average means of these columns, with the weights corresponding respectively to the size |Ai\cup Ri|, corresponds to computing:

	correctVect[k] / foundVect[k]
and
	correctVect[k] / expected

which the program does...
    */
    public void printHTML( Vector result ) {
	// variables for computing iterative harmonic means
	int expected = 0; // expected so far
	int foundVect[]; // found so far
	int correctVect[]; // correct so far
	long timeVect[]; // time so far
	PrintStream writer = null;
	fsize = format.length();
	// JE: the writer should be put out
	// JE: the h-means computation should be put out as well
	try {
	    // Print result
	    if ( filename == null ) {
		writer = (PrintStream)System.out;
	    } else {
		writer = new PrintStream(new FileOutputStream( filename ));
	    }
	    // Print the header
	    if ( embedded != true ) writer.println("<html><head></head><body>");
	    writer.println("<table border='2' frame='sides' rules='groups'>");
	    writer.println("<colgroup align='center' />");
	    // for each algo <td spancol='2'>name</td>
	    for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ;e.nextElement()) {
		writer.println("<colgroup align='center' span='"+fsize+"' />");
	    }
	    // For each file do a
	    writer.println("<thead valign='top'><tr><th>algo</th>");
	    // for each algo <td spancol='2'>name</td>
	    for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ;) {
		writer.println("<th colspan='"+fsize+"'>"+e.nextElement()+"</th>");
	    }
	    writer.println("</tr></thead><tbody><tr><td>test</td>");
	    // for each algo <td>Prec.</td><td>Rec.</td>
	    for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ;e.nextElement()) {
		for ( int i = 0; i < fsize; i++){
		    writer.print("<td>");
		    if ( format.charAt(i) == 'p' ) {
			writer.print("Prec.");
		    } else if ( format.charAt(i) == 'r' ) {
			writer.print("Rec.");
		    } else if ( format.charAt(i) == 'f' ) {
			writer.print("Fall.");
		    } else if ( format.charAt(i) == 'm' ) {
			writer.print("FMeas.");
		    } else if ( format.charAt(i) == 'o' ) {
			writer.print("Over.");
		    } else if ( format.charAt(i) == 't' ) {
			writer.print("Time");
		    }
		    writer.println("</td>");
		}
		//writer.println("<td>Prec.</td><td>Rec.</td>");
	    }
	    writer.println("</tr></tbody><tbody>");
	    foundVect = new int[ listAlgo.size() ];
	    correctVect = new int[ listAlgo.size() ];
	    timeVect = new long[ listAlgo.size() ];
	    for( int k = listAlgo.size()-1; k >= 0; k-- ) {
		foundVect[k] = 0;
		correctVect[k] = 0;
		timeVect[k] = 0;
	    }
	    // </tr>
	    // For each directory <tr>
	    boolean colored = false;
	    for ( Enumeration e = result.elements() ; e.hasMoreElements() ;) {
		int nexpected = -1;
		Vector test = (Vector)e.nextElement();
		if ( colored == true && color != null ){
		    colored = false;
		    writer.println("<tr bgcolor=\""+color+"\">");
		} else {
		    colored = true;
		    writer.println("<tr>");
		};
		// Print the directory <td>bla</td>
		writer.println("<td>"+(String)test.get(0)+"</td>");
		// For each record print the values <td>bla</td>
		Enumeration f = test.elements();
		f.nextElement();
		for( int k = 0 ; f.hasMoreElements() ; k++) {
		    PRecEvaluator eval = (PRecEvaluator)f.nextElement();
		    if ( eval != null ){
			// iterative H-means computation
			if ( nexpected == -1 ){
			    expected += eval.getExpected();
			    nexpected = 0;
			}
			foundVect[k] += eval.getFound();
			correctVect[k] += eval.getCorrect();
			timeVect[k] += eval.getTime();

			for ( int i = 0 ; i < fsize; i++){
			    writer.print("<td>");
			    if ( format.charAt(i) == 'p' ) {
				printFormat(writer,eval.getPrecision());
			    } else if ( format.charAt(i) == 'r' ) {
				printFormat(writer,eval.getRecall());
			    } else if ( format.charAt(i) == 'f' ) {
				printFormat(writer,eval.getFallout());
			    } else if ( format.charAt(i) == 'm' ) {
				printFormat(writer,eval.getFmeasure());
			    } else if ( format.charAt(i) == 'o' ) {
				printFormat(writer,eval.getOverall());
			    } else if ( format.charAt(i) == 't' ) {
				if ( eval.getTime() == 0 ){
				    writer.print("-");
				} else {
				    printFormat(writer,eval.getTime());
				}
			    }
			    writer.println("</td>");
			}
		    } else {
			writer.println("<td>n/a</td><td>n/a</td>");
		    }
		}
		writer.println("</tr>");
	    }
	    writer.print("<tr bgcolor=\"yellow\"><td>H-mean</td>");
	    // Here we are computing a sheer average.
	    // While in the column results we print NaN when the returned
	    // alignment is empty,
	    // here we use the real values, i.e., add 0 to both correctVect and
	    // foundVect, so this is OK for computing the average.
	    int k = 0;
	    for ( Enumeration<String> e = listAlgo.elements() ; e.hasMoreElements() ; k++) {
		e.nextElement();
		double precision = (double)correctVect[k]/foundVect[k];
		double recall = (double)correctVect[k]/expected;
		for ( int i = 0 ; i < fsize; i++){
		    writer.print("<td>");
		    if ( format.charAt(i) == 'p' ) {
			printFormat(writer,precision);
		    } else if ( format.charAt(i) == 'r' ) {
			printFormat(writer,recall);
		    } else if ( format.charAt(i) == 'f' ) {
			printFormat(writer,(double)(foundVect[k] - correctVect[k])/foundVect[k]);
		    } else if ( format.charAt(i) == 'm' ) {
			printFormat(writer,2 * precision * recall / (precision + recall));
		    } else if ( format.charAt(i) == 'o' ) {
			printFormat(writer,recall * (2 - (1 / precision)));
		    } else if ( format.charAt(i) == 't' ) {
			if ( timeVect[k] == 0 ){
			    writer.print("-");
			} else {
			    printFormat(writer,timeVect[k]);
			}
		    }
		    writer.println("</td>");
		};
	    }
	    writer.println("</tr>");
	    writer.println("</tbody></table>");
	    writer.println("<p><small>n/a: result alignment not provided or not readable<br />");
	    writer.println("NaN: division per zero, likely due to empty alignent.</small></p>");
	    if ( embedded != true ) writer.println("</body></html>");
	    writer.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    // Borrowed and enhanced from
    // http://acm.sus.mcgill.ca/20020323/work/acm-19/B.j
    // What a pity that it is not in Java... (wait for 1.5)
    public void printFormat(PrintStream w, double f){
	if ( f != f ) {
	    w.print("NaN");
	} else {
	    int tmp = (int)(f*100);
	    int dec = tmp%100;
	    if( (int)(f*1000)%10 >= 5 ) dec++;
	    tmp /= 100;
	    w.print("" + tmp + ".");
	    if(dec < 10) w.print("0");
	    w.print("" + dec);
	}
    }

    public void usage() {
	System.out.println("usage: GroupEval [options]");
	System.out.println("options are:");
	System.out.println("\t--format=prfmot -r prfmot\tSpecifies the output order (precision/recall/fallout/f-measure/overall/time)");
	// Apparently not implemented
	//System.out.println("\t--sup=algo -s algo\tSpecifies if dominant columns are algorithms or measure");
	System.out.println("\t--output=filename -o filename\tSpecifies a file to which the output will go");
	System.out.println("\t--reference=filename -r filename\tSpecifies the name of the reference alignment file (default: refalign.rdf)");

	System.out.println("\t--type=html|xml|tex|ascii|triangle -t html|xml|tex|ascii\tSpecifies the output format");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--color=color -c color\tSpecifies if the output must color even lines of the output");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
	System.err.print("\n"+GroupEval.class.getPackage().getImplementationTitle()+" "+GroupEval.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: GroupEval.java 803 2008-08-30 22:40:22Z euzenat $)\n");
    }
}

