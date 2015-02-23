/*
 * $Id: GroupAlign.java 677 2008-03-23 22:58:22Z euzenat $
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2008, INRIA Rh�ne-Alpes
 * Copyright (C) 2004, Universit� de Montr�al
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.onto.OntologyCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.lang.Integer;
import java.lang.Long;
import java.util.Hashtable;
import java.util.Enumeration;

import org.xml.sax.SAXException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import org.apache.log4j.BasicConfigurator;

/** A batch class for an OWL ontology alignment processing.
    It aligns all the ontology pairs denoted 
    under subdirectory of the current directory.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.util.GroupAlign [options]
    </pre>

    where the options are:
    <pre>
        --alignment=filename -a filename Start from an XML alignment file
	--params=filename -p filename   Read the parameters in file
	--name=filename -n filename output results in filename.rdf
	--impl=className -i classname           Use the given alignment implementation.
	--renderer=className -r className       Specifies the alignment renderer
	--debug=level -d level
   </pre>

<pre>
$Id: GroupAlign.java 677 2008-03-23 22:58:22Z euzenat $
</pre>

@author Sean K. Bechhofer
@author J�r�me Euzenat
    */

public class GroupAlign {

    Parameters params = null;
    String filename = "align";
    String paramfile = null;
    String urlprefix = null;
    String source = "onto1.rdf";
    String target = "onto.rdf";
    URI uri1 = null;
    String initName = null;
    OntologyCache loadedOntologies = null;
    int debug = 0;
    String alignmentClassName = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
    String rendererClass = "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor";
    String ontoDir = null;

    public static void main(String[] args) {
	try { new GroupAlign().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {

	LongOpt[] longopts = new LongOpt[13];
	loadedOntologies = new OntologyCache();
	params = new BasicParameters();

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("alignment", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	longopts[6] = new LongOpt("params", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	longopts[7] = new LongOpt("name", LongOpt.REQUIRED_ARGUMENT, null, 'n');
	longopts[8] = new LongOpt("prefix", LongOpt.REQUIRED_ARGUMENT, null, 'u');
	longopts[9] = new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[10] = new LongOpt("target", LongOpt.REQUIRED_ARGUMENT, null, 't');
	// Is there a way for that in LongOpt ???
	longopts[11] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	longopts[12] = new LongOpt("directory", LongOpt.REQUIRED_ARGUMENT, null, 'w');

	Getopt g = new Getopt("", args, "ho:a:d::n:u:r:i:s:t:p:D:w:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output in given filename */
		filename = g.getOptarg();
		break;
	    case 'n' :
		arg = g.getOptarg();
		/* Use common ontology to compare */
		if(arg!=null){
		    try { uri1 = new URI(g.getOptarg());
		    } catch (Exception e) { e.printStackTrace(); }
		}
		else{uri1 = null;}
		break;
	    case 'p' :
		/* Read parameters from filename */
		paramfile = g.getOptarg();
		BasicParameters.read( params, paramfile );
		break;
	    case 'r' :
		/* Use the given class for rendering */
		rendererClass = g.getOptarg();
		break;
	    case 'i' :
		/* Use the given class for the alignment */
		alignmentClassName = g.getOptarg();
		break;
	    case 'a' :
		/* Use the given file as a partial alignment */
		initName = g.getOptarg();
		break;
	    case 'u' :
		/* Use the given url prefix for fetching the ontologies */
		urlprefix = g.getOptarg();
		break;
	    case 's' :
		/* Use the given filename for source ontology */
		source = g.getOptarg();
		break;
	    case 't' :
		/* Use the given filename for target ontology */
		target = g.getOptarg();
		break;
	    case 'D' :
		/* Parameter definition */
		arg = g.getOptarg();
		int index = arg.indexOf('=');
		if ( index != -1 ) {
		    params.setParameter( arg.substring( 0, index), 
					 arg.substring(index+1));
		} else {
		    System.err.println("Bad parameter syntax: "+g);
		    usage();
		    System.exit(0);
		}
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

	//int i = g.getOptind();

	if (debug == 0 && params.getParameter("debug") != null) {
	    debug = Integer.parseInt((String)params.getParameter("debug"));
	}
	// Exception to the parameter as string rule
	if (debug > 0) params.setParameter("debug", new Integer(debug-1));

	iterateDirectories();
    }

    public void iterateDirectories (){
	File [] subdir = null;
	try { 
		if(ontoDir == null){
		    subdir = (new File(System.getProperty("user.dir"))).listFiles(); 
		}
		else{
			subdir = (new File(ontoDir)).listFiles();
		}
	}
	catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}
	int size = subdir.length;
	for ( int i=0 ; i < size; i++ ) {
	    if( subdir[i].isDirectory() ) {
		try {
		    // Align
		    if ( debug > 0 ) System.err.println("Directory: "+subdir[i]);
		    align( subdir[i] );
		    // Unload the ontologies
		    loadedOntologies.clear();
		} catch (Exception e) { 
		    if ( debug > 1 ) e.printStackTrace(); }
	    }
	}
    }

    public void align ( File dir ) throws Exception {
	String prefix = null;
	// toURI(). is not very good
	AlignmentProcess result = null;
	Alignment init = null;

	if ( urlprefix != null ){
	    prefix = urlprefix+"/"+dir.getName()+"/";
	} else {
	    // sounds like the only way to have something portable
	    // This is the modification for acomodating the HCONE 
	    prefix = dir.toURI().toString();
	    // The problem is that is brings
	    // file:/localpath
	    // instead of
	    // file://localhost/localpath
	    // Apparently should be file:///c:/localpath
	}
	//System.err.println("Here it is "+prefix+" (end by /?)");

	BasicConfigurator.configure();
	if ( uri1 == null ) {uri1 = new URI(prefix+source);}
	if (!source.equalsIgnoreCase("onto1.rdf") && !target.equalsIgnoreCase("onto1.rdf")){uri1 = new URI(prefix+source);}
	URI uri2 = new URI(prefix+target);

	if (debug > 1) System.err.println(" Handler set");
	if (debug > 1) System.err.println(" URI1: "+uri1);
	if (debug > 1) System.err.println(" URI2: "+uri2);

	try {
	    if (initName != null) {
		AlignmentParser aparser = new AlignmentParser(debug-1);
		//init = aparser.parse( initName, loadedOntologies);
		init = aparser.parse( initName );
		uri1 = init.getFile1();
		uri2 = init.getFile2();
		if (debug > 1) System.err.println(" Init parsed");
	    }

	    // Create alignment object
	    Object[] mparams = {};
	    Class[] cparams = {};
	    Class alignmentClass = Class.forName(alignmentClassName);
	    java.lang.reflect.Constructor alignmentConstructor =
		alignmentClass.getConstructor(cparams);
	    result = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
	    result.init( uri1, uri2 );
	} catch (Exception ex) {
	    System.err.println("Cannot create alignment "+ alignmentClassName+ "\n"+ ex.getMessage());
	    throw ex;
	}

	if (debug > 1) System.err.println(" Alignment structure created");

	// Compute alignment
	long time = System.currentTimeMillis();
	result.align(init, params); // add opts
	long newTime = System.currentTimeMillis();
	result.setExtension( Annotations.ALIGNNS, Annotations.TIME, Long.toString(newTime - time) );

	if (debug > 1) System.err.println(" Alignment performed");

	// Set output file
	PrintWriter writer = new PrintWriter (
			          new BufferedWriter(
				       new OutputStreamWriter( 
                                            new FileOutputStream(dir+File.separator+filename+".rdf"), "UTF-8" )), true);
	AlignmentVisitor renderer = null;

	try {
	    Object[] mparams = { (Object)writer };
	    Class[] cparams = { Class.forName("java.io.PrintWriter") };
	    java.lang.reflect.Constructor rendererConstructor =
		Class.forName(rendererClass).getConstructor(cparams);
	    renderer = (AlignmentVisitor)rendererConstructor.newInstance(mparams);
	} catch (Exception ex) {
	    System.err.println("Cannot create renderer "+rendererClass+"\n"+ ex.getMessage());
	    throw ex;
	}

	if (debug > 1) System.err.println(" Outputing result to "+dir+File.separator+filename+".rdf");
	// Output
	result.render( renderer);

	if (debug > 1) System.err.println(" Done..."+renderer+"\n");
	// It should be useful to unload ontologies

	// JE: This instruction is very important
	writer.close();
    }

    public void usage() {
	System.err.println("usage: GroupAlign [options]");
	System.err.println("options are:");
	System.err.println("\t--name=uri -n uri\t\tUse the given uri to compare with.");
	System.err.println("\t--source=filename -s filename Source filename (default onto1.rdf)");
	System.err.println("\t--target=filename -t filename Target filename (default onto.rdf)");
	System.err.println("\t--prefix=uriprefix -u uriprefix URI prefix of the target");
	System.err.println("\t--output=filename -o filename\tOutput the alignment in filename.rdf");
	System.err.println("\t--impl=className -i classname\t\tUse the given alignment implementation.");
	System.err.println("\t--renderer=className -r className\tSpecifies the alignment renderer");
	System.err.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.err.println("\t--help -h\t\t\tPrint this message");
	System.err.print("\n"+GroupAlign.class.getPackage().getImplementationTitle()+" "+GroupAlign.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: GroupAlign.java 677 2008-03-23 22:58:22Z euzenat $)\n");
    }
}
