 /*
 * $Id: HTMLAServProfile.java 782 2008-08-26 17:40:56Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2006-2008.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.Annotations;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;

import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.net.URLDecoder;

import java.lang.Integer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Request;

import org.mortbay.util.MultiMap;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;

import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.servlet.MultiPartFilter;
import org.mortbay.jetty.servlet.Context.SContext;

/**
 * HTMLAServProfile: an HTML provile for the Alignment server
 * It embeds an HTTP server.
 */

public class HTMLAServProfile implements AlignmentServiceProfile {

    private int tcpPort;
    private String tcpHost;
    private int debug = 0;
    private Server server;
    private AServProtocolManager manager;
    private WSAServProfile wsmanager;

    private String myId;
    private String serverId;
    private int localId = 0;

    /**
     * Some HTTP response status codes
     */
    public static final String
	HTTP_OK = "200 OK",
	HTTP_REDIRECT = "301 Moved Permanently",
	HTTP_FORBIDDEN = "403 Forbidden",
	HTTP_NOTFOUND = "404 Not Found",
	HTTP_BADREQUEST = "400 Bad Request",
	HTTP_INTERNALERROR = "500 Internal Server Error",
	HTTP_NOTIMPLEMENTED = "501 Not Implemented";

    /**
     * Common mime types for dynamic content
     */
    public static final String
	MIME_PLAINTEXT = "text/plain",
	MIME_HTML = "text/html",
	MIME_XML = "text/xml",
	MIME_DEFAULT_BINARY = "application/octet-stream";

    public static final int MAX_FILE_SIZE = 10000;

    public static final String HEADER = "<style type=\"text/css\">body { font-family: sans-serif } button {background-color: #DDEEFF; margin-left: 1%; border: #CCC 1px solid;}</style>";
    // ==================================================
    // Socket & server code
    // ==================================================

    /**
     * Starts a HTTP server to given port.<p>
     * Throws an IOException if the socket is already in use
     */
    public void init( Parameters params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	tcpPort = Integer.parseInt( (String)params.getParameter( "http" ) );
	tcpHost = (String)params.getParameter( "host" ) ;

	/*
	try {
	    final ServerSocket ss = new ServerSocket( tcpPort );
	    Thread t = new Thread( new Runnable() {
		    public void run() {
			try { while( true ) new HTTPSession( ss.accept());
			} catch ( IOException ioe ) { ioe.printStackTrace(); }
		    }
		});
	    t.setDaemon( true );
	    t.start();
	} catch (Exception e) {
	    throw new AServException ( "Cannot launch HTTP Server" , e );
	}
	*/

	// ********************************************************************
	// JE: Jetty implementation
	server = new Server(tcpPort);

	Handler handler = new AbstractHandler(){
		public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) 
		    throws IOException, ServletException
		{
		    String method = request.getMethod();
		    //uri = URLDecoder.decode( request.getURI(), "iso-8859-1" );
		    // Should be decoded?
		    String uri = request.getPathInfo();
		    Properties params = new Properties();
		    try { decodeParms( request.getQueryString(), params ); }
		    catch ( Exception e) {};
		    // I do not decode them here because it is useless
		    // See below how it is done.
		    Properties header = new Properties();
		    Enumeration headerNames = request.getHeaderNames();
		    while(headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			header.setProperty( headerName, request.getHeader(headerName) );
		    }

		    // Get the content if any
		    // Multi part?
		    // This is supposed to be only an uploaded file
		    // We use jetty MultiPartFilter to decode this file,
		    // and only this
		    String mimetype = request.getContentType();
		    if ( mimetype != null && mimetype.startsWith("multipart/form-data") ) {
			MultiPartFilter filter = new MultiPartFilter();
			// This is in fact useless
			ParameterServletResponseWrapper dummyResponse =
			    new ParameterServletResponseWrapper( response );
			// In theory, the filter must be inited with a FilterConfig
			// filter.init( new FilterConfig);
			// This filter config must have a javax.servlet.context.tempdir attribute
			// and a ServletConxtext with parameter "deleteFiles"
			// Apparently the Jetty implementation uses System defaults
			// if no FilterConfig
			// e.g., it uses /tmp and keeps the files
			filter.doFilter( request, dummyResponse, new Chain() );
			// Extract parameters from response
			if ( request.getAttribute("content") != null )
			    params.setProperty( "filename", request.getAttribute("content").toString() );
			filter.destroy();
		    } else if ( mimetype != null && mimetype.startsWith("text/xml") ) {
			// Most likely Web service request
			int length = request.getContentLength();
			char [] mess = new char[length+1];
			try { 
			    new BufferedReader(new InputStreamReader(request.getInputStream())).read( mess, 0, length);
			} catch (Exception e) {
			    e.printStackTrace(); // To clean up
			}
			params.setProperty( "content", new String( mess ) );
		    // File attached to SOAP messages
		    } else if ( mimetype != null && mimetype.startsWith("application/octet-stream") ) {
         		File alignFile = new File(File.separator + "tmp" + File.separator + newId() +"XXX.rdf");

         		// check if file already exists - and overwrite if necessary.
         		if (alignFile.exists()) alignFile.delete();
           
               	 	FileOutputStream fos = new FileOutputStream(alignFile);
            		InputStream is = request.getInputStream();
			
           	        try {
			    byte[] buffer = new byte[4096];
			    int bytes=0; 
			    while (true) {
				bytes = is.read(buffer);
				if (bytes < 0) break;
				fos.write(buffer, 0, bytes);
			    }
			    fos.flush();
			    fos.close();
            		} catch (Exception e) {
			}
               		is.close();
			params.setProperty( "content", "" );
			params.setProperty( "filename" ,  alignFile.getAbsolutePath()  );
         	    } 

		    // Get the answer (HTTP)
		    Response r = serve( uri, method, header, params );

		    // Return it
		    response.setContentType(r.getContentType());
		    //response.setStatus(r.getStatus());
		    response.setStatus(HttpServletResponse.SC_OK);
		    response.getWriter().println(r.getData());
		    // r.getStatus(); r.getContentType; r.getData();
		    ((Request)request).setHandled(true);
		}
	    };
	server.setHandler(handler);

	// Common part
	try { server.start(); }
	catch (Exception e) {
	    throw new AServException ( "Cannot launch HTTP Server" , e );
	}
	//server.join();

	// ********************************************************************
	if ( params.getParameter( "wsdl" ) != null ){
	    wsmanager = new WSAServProfile();
	    if ( wsmanager != null ) wsmanager.init( params, manager );
	}
	myId = "LocalHTMLInterface";
	serverId = manager.serverURL()+"/html/";
	localId = 0;
    }

    /**
     * Je//: should certainly do more than that!
     */
    public void close(){
	if ( wsmanager != null ) wsmanager.close();
	if ( server != null ) {
	    try { server.stop(); }
            catch (Exception e) { e.printStackTrace(); }
	}
    }
    
    // ==================================================
    // API parts
    // ==================================================

    /**
     * Override this to customize the server.<p>
     *
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method	"GET", "POST" etc.
     * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param header	Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    public Response serve( String uri, String method, Properties header, Properties parms ) {
	if ( debug >= 1 ) System.err.println( method + " '" + uri + "' " );
	Enumeration en = header.propertyNames();
	while ( en.hasMoreElements()) {
	    String value = (String)en.nextElement();
	    //System.err.println( "  HDR: '" + value + "' = '" +
	    //			header.getProperty( value ) + "'" );
	}
	/*
	e = parms.propertyNames();
	while ( e.hasMoreElements()) {
	    String value = (String)e.nextElement();
	    //System.err.println( "  PRM: '" + value + "' = '" +parms.getProperty( value ) + "'" );
	}
	*/

	// Convert parms to parameters
	Parameters params = new BasicParameters();
	Enumeration e = parms.propertyNames();
	while ( e.hasMoreElements()) {
	    String value = (String)e.nextElement();
	    if ( debug > 1 ) System.err.println( "  PRM: '" + value + "' = '" +parms.getProperty( value ) + "'" );
	    if ( value.startsWith( "paramn" ) ){
		params.setParameter( parms.getProperty( value ),
				     parms.getProperty( "paramv"+value.substring( 6 ) ) );
	    } else if ( !value.startsWith( "paramv" ) ) {
		params.setParameter( value, parms.getProperty( value ) );
	    }
	}
	
	int start = 0;
	if ( uri.charAt(0) == '/' ) start = 1;
	int end = uri.indexOf( '/', start+1 );
	String oper = "";
	if ( end != -1 ) {
	    oper = uri.substring( start, end );
	    start = end+1;
	} else {
	    // Old implementation
	    oper = uri.substring( start );
	    start = uri.length();
	    // No '/' after the tag cause problems, send redirect
	    //uri += "/";
	    //Response r = new Response( HTTP_REDIRECT, MIME_HTML,
	    // 			       "<html><body>Redirected: <a href=\""+uri+"\">" +uri+"</a></body></html>");
	//r.addHeader( "Location", uri );
	//return r;
	}

	if ( oper.equals( "aserv" ) ){
	    if ( wsmanager != null ) {
		return new Response( HTTP_OK, MIME_HTML, wsmanager.protocolAnswer( uri, uri.substring(start), header, params ) );
	    } else {
		// This is not correct: I shoud return an error
		return new Response( HTTP_OK, MIME_HTML, "<html><head>"+HEADER+"</head><body>"+about()+"</body></html>" );
	    }
	} else if ( oper.equals( "admin" ) ){
	    return adminAnswer( uri, uri.substring(start), header, params );
	} else if ( oper.equals( "html" ) ){
	    return htmlAnswer( uri, uri.substring(start), header, params );
	} else if ( oper.equals( "alid" ) ){
	    return returnAlignment( uri );
	} else if ( oper.equals( "wsdl" ) ){
	    return wsdlAnswer(uri, uri.substring(start), header, params);
	} else {
	    //return serveFile( uri, header, new File("."), true );
	    return new Response( HTTP_OK, MIME_HTML, "<html><head>"+HEADER+"</head><body>"+about()+"</body></html>" );
	}
    }

    protected String about() {
	return "<h1>Alignment server</h1><center>"+AlignmentService.class.getPackage().getImplementationTitle()+" "+AlignmentService.class.getPackage().getImplementationVersion()+"<br />"
	    + "<center><a href=\"/html/\">Access</a></center>"
	    + "(C) INRIA Rh&ocirc;ne-Alpes, 2006-2008<br />"
	    + "<a href=\"http://alignapi.gforge.inria.fr\">http://alignapi.gforge.inria.fr</a>"
	    + "</center>";
    }

    /**
     * HTTP administration interface
     * Allows some limited administration of the server through HTTP
     */
    public Response adminAnswer( String uri, String perf, Properties header, Parameters params ) {
	if ( debug > 0 ) System.err.println("ADMIN["+perf+"]");
	String msg = "";
        if ( perf.equals("listalignments") ){
	    msg = "<h1>Available alignments</h1><ul compact=\"1\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID );
		msg += "<li><a href=\"../html/retrieve?method=fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor&id="+id+"\">"+id+"</a></li>";
	    }
	    msg += "</ul>";
	} else if ( perf.equals("listmethods") ){
	    msg = "<h1>Embedded classes</h1>\n<h2>Methods</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Renderers</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	    msg += "<h2>Services</h2><ul compact=\"1\">";
	    for( Iterator it = manager.listservices().iterator(); it.hasNext(); ) {
		msg += "<li>"+it.next()+"</li>";
	    }
	    msg += "</ul>";
	} else if ( perf.equals("prmsqlquery") ){
	    msg = "<h1>SQL query</h1><form action=\"sqlquery\">Query:<br /><textarea name=\"query\" rows=\"20\" cols=\"80\">SELECT \nFROM \nWHERE </textarea> (sql)<br /><small>An SQL SELECT query</small><br /><input type=\"submit\" value=\"Query\"/></form>";
	} else if ( perf.equals("sqlquery") ){
	    String answer = manager.query( (String)params.getParameter("query") );
	    msg = "<pre>"+answer+"</pre>";
	} else if ( perf.equals("about") ){
	    msg = about();
	} else if ( perf.equals("shutdown") ){
	    manager.shutdown();
	    msg = "<h1>Server shut down</h1>";
	} else if ( perf.equals("prmreset") ){
	    manager.reset();
	    msg = "<h1>Alignment server reset from database</h1>";
	} else if ( perf.equals("prmflush") ){
	    manager.flush();
	    msg = "<h1>Cache has been flushed</h1>";
	} else if ( perf.equals("addservice") ){
	    msg = perf;
	} else if ( perf.equals("addmethod") ){
	    msg = perf;
	} else if ( perf.equals("addrenderer") ){
	    msg = perf;
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment server administration</h1>";
	    msg += "<form action=\"listmethods\"><button title=\"List embedded plug-ins\" type=\"submit\">Embedded classes</button></form>";
	    msg += "<form action=\"prmsqlquery\"><button title=\"Query the SQL database (unavailable)\" type=\"submit\">SQL Query</button></form>";
	    msg += "<form action=\"prmflush\"><button title=\"Free memory by unloading correspondences\" type=\"submit\">Flush caches</button></form>";
	    msg += "<form action=\"prmreset\"><button title=\"Restore launching state (reload from database)\" type=\"submit\">Reset server</button></form>";
	    //	    msg += "<form action=\"shutdown\"><button title=\"Shutdown server\" type=\"submit\">Shutdown</button></form>";
	    msg += "<form action=\"..\"><button title=\"About...\" type=\"submit\">About</button></form>";
	    msg += "<form action=\"../html/\"><button style=\"background-color: lightpink;\" title=\"Back to user menu\" type=\"submit\">User interface</button></form>";
	} else {
	    msg = "Cannot understand: "+perf;
	}
	return new Response( HTTP_OK, MIME_HTML, "<html><head>"+HEADER+"</head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server administration</a></small></center></body></html>" );
    }

    /**
     * Returns the alignment in RDF/XML
     */
    public Response returnAlignment( String uri ) {
	Parameters params = new BasicParameters();
	params.setParameter("id", manager.serverURL()+uri);
	params.setParameter( "method", "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor" );
	Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	if ( answer instanceof ErrorMsg ) {
	    return new Response( HTTP_NOTFOUND, MIME_PLAINTEXT, "Alignment server: unknown alignment : "+answer.getContent() );
	} else {
	    return new Response( HTTP_OK, MIME_XML, answer.getContent() );
	}
    }

    /**
     * User friendly HTTP interface
     * uses the protocol but offers user-targeted interaction
     */
    public Response htmlAnswer( String uri, String perf, Properties header, Parameters params ) {
	//System.err.println("HTML["+perf+"]");
	String msg = "";
	if ( perf.equals("prmstore") ) {
	    msg = "<h1>Store an alignment</h1><form action=\"store\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    // JE: only those non stored please (retrieve metadata + stored)
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		params.setParameter("id", id);
		if ( !manager.storedAlignment( new Message(newId(),(Message)null,myId,serverId,"", params ) ) ){
		msg += "<option value=\""+id+"\">"+id+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" value=\"Store\"/></form>";
	} else if ( perf.equals("store") ) {
	    // here should be done the switch between store and load/store
	    String id = (String)params.getParameter("id");
	    String url = (String)params.getParameter("url");
	    if ( url != null && !url.equals("") ) { // Load the URL
		Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    id = answer.getContent();
		}
	    }
	    if ( id != null ){ // Store it
		Message answer = manager.store( new Message(newId(),(Message)null,myId,serverId,id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    msg = "<h1>Alignment stored</h1>";
		    msg += displayAnswer( answer );
		}
	    }
	} else if ( perf.equals("prmcut") ) {
	    String sel = (String)params.getParameter("id");
	    msg ="<h1>Trim alignments</h1><form action=\"cut\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		if ( sel != null && sel.equals( id ) ){
		    msg += "<option selected=\"1\" value=\""+id+"\">"+id+"</option>";
		} else {
		    msg += "<option value=\""+id+"\">"+id+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "Methods: <select name=\"method\"><option value=\"hard\">hard</option><option value=\"perc\">perc</option><option value=\"best\">best</option><option value=\"span\">span</option><option value=\"prop\">prop</option></select><br />Threshold: <input type=\"text\" name=\"threshold\" size=\"4\"/> <small>A value between 0. and 1. with 2 digits</small><br /><input type=\"submit\" name=\"action\" value=\"Trim\"/><br /></form>";
	} else if ( perf.equals("cut") ) {
	    String id = (String)params.getParameter("id");
	    String threshold = (String)params.getParameter("threshold");
	    if ( id != null && !id.equals("") && threshold != null && !threshold.equals("") ){ // Trim it
		Message answer = manager.cut( new Message(newId(),(Message)null,myId,serverId,id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    msg = "<h1>Alignment trimed</h1>";
		    msg += displayAnswer( answer );
		}
	    }
	} else if ( perf.equals("prminv") ) {
	    msg ="<h1>Inverse alignment</h1><form action=\"inv\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Invert\"/><br /></form>";
	} else if ( perf.equals("inv") ) {
	    String id = (String)params.getParameter("id");
	    if ( id != null && !id.equals("") ){ // Trim it
		Message answer = manager.inverse( new Message(newId(),(Message)null,myId,serverId,id, params) );
		if ( answer instanceof ErrorMsg ) {
		    msg = testErrorMessages( answer );
		} else {
		    msg = "<h1>Alignment inverted</h1>";
		    msg += displayAnswer( answer );
		}
	    }
	} else if ( perf.equals("prmalign") ) {
	    msg ="<h1>Match ontologies</h1><form action=\"align\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\"/> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\"/> (uri)<br /><small>These are the URL of places where to find these ontologies. They must be reachable by the server (i.e., file:// URI are acceptable if they are on the server)</small><br /><!--input type=\"submit\" name=\"action\" value=\"Find\"/><br /-->Methods: <select name=\"method\">";
	    for( Iterator it = manager.listmethods().iterator(); it.hasNext(); ) {
		String id = (String)it.next();
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />Initial alignment id:  <select name=\"id\"><option value=\"\" selected=\"1\"></option>";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "<input type=\"submit\" name=\"action\" value=\"Match\"/>";
	    msg += "  <input type=\"checkbox\" name=\"force\" /> Force <input type=\"checkbox\" name=\"async\" /> Asynchronous<br />";
	    msg += "Additional parameters:<br /><input type=\"text\" name=\"paramn1\" size=\"15\"/> = <input type=\"text\" name=\"paramv1\" size=\"65\"/><br /><input type=\"text\" name=\"paramn2\" size=\"15\"/> = <input type=\"text\" name=\"paramv2\" size=\"65\"/><br /><input type=\"text\" name=\"paramn3\" size=\"15\"/> = <input type=\"text\" name=\"paramv3\" size=\"65\"/><br /><input type=\"text\" name=\"paramn4\" size=\"15\"/> = <input type=\"text\" name=\"paramv4\" size=\"65\"/></form>";
	} else if ( perf.equals("align") ) {
	    Message answer = manager.align( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Alignment results</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmfind") ) {
	    msg ="<h1>Find alignments between ontologies</h1><form action=\"find\">Ontology 1: <input type=\"text\" name=\"onto1\" size=\"80\"/> (uri)<br />Ontology 2: <input type=\"text\" name=\"onto2\" size=\"80\"/> (uri)<br /><small>These are the URI identifying the ontologies. Not those of places where to upload them.</small><br /><input type=\"submit\" name=\"action\" value=\"Find\"/></form>";
	} else if ( perf.equals("find") ) {
	    Message answer = manager.existingAlignments( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Found alignments</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmretrieve") ) {
	    String sel = (String)params.getParameter("id");
	    msg = "<h1>Retrieve alignment</h1><form action=\"retrieve\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		if ( sel != null && sel.equals( id ) ){
		    msg += "<option selected=\"1\" value=\""+id+"\">"+id+"</option>";
		} else {
		    msg += "<option value=\""+id+"\">"+id+"</option>";
		}
	    }
	    msg += "</select><br />";
	    msg += "Rendering: <select name=\"method\">";
	    for( Iterator it = manager.listrenderers().iterator(); it.hasNext(); ) {
		String id = (String)it.next();
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Retrieve\"/></form>";
	} else if ( perf.equals("retrieve") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // Metadata not done yet
	} else if ( perf.equals("prmmetadata") ) {
	    msg = "<h1>Retrieve alignment metadata</h1><form action=\"metadata\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Get metadata\"/></form>";
	} else if ( perf.equals("metadata") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    //System.err.println("Content: "+answer.getContent());
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // render
	    // Alignment in HTML can be rendre or metadata+tuples
	} else if ( perf.equals("prmload") ) {
	    // Should certainly be good to offer store as well
	    msg = "<h1>Load an alignment</h1><form action=\"load\">Alignment URL: <input type=\"text\" name=\"url\" size=\"80\"/> (uri)<br /><small>This is the URL of the place where to find this alignment. It must be reachable by the server (i.e., file:// URI is acceptable if it is on the server).</small><br /><input type=\"submit\" value=\"Load\"/></form>";
	    //msg += "Alignment file: <form ENCTYPE=\"text/xml; charset=utf-8\" action=\"loadfile\" method=\"POST\">";
	    msg += "Alignment file: <form enctype=\"multipart/form-data\" action=\"load\" method=\"POST\">";
	    msg += " <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\""+MAX_FILE_SIZE+"\"/>";
	    msg += "<input name=\"content\" type=\"file\" size=\"35\">";
	    msg += "<br /><small>NOTE: Max file size is "+(MAX_FILE_SIZE/1024)+"KB; this is experimental but works</small><br />";
	    msg += " <input type=\"submit\" Value=\"Upload\">";
	    msg +=  " </form>";
	} else if ( perf.equals("load") ) {
	    // load
	    Message answer = manager.load( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Alignment loaded</h1>";
		msg += displayAnswer( answer );
	    }
	} else if ( perf.equals("prmtranslate") ) {
	    msg = "<h1>Translate query</h1><form action=\"translate\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br />";
	    msg += "SPARQL query:<br /> <textarea name=\"query\" rows=\"20\" cols=\"80\">PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nSELECT *\nFROM <>\nWHERE {\n\n}</textarea> (SPARQL)<br /><small>A SPARQL query (PREFIX prefix: &lt;uri&gt; SELECT variables FROM &lt;url&gt; WHERE { triples })</small><br /><input type=\"submit\" value=\"Translate\"/></form>";
	} else if ( perf.equals("translate") ) {
	    Message answer = manager.translate( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		msg = "<h1>Message translation</h1>";
		msg += "<h2>Initial message</h2><pre>"+((String)params.getParameter("query")).replaceAll("&", "&amp;").replaceAll("<", "&lt;")+"</pre>";
		msg += "<h2>Translated message</h2><pre>";
		msg += answer.HTMLString().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
		msg += "</pre>";
	    }
	} else if ( perf.equals("prmmetadata") ) {
	    msg = "<h1>Retrieve alignment metadata</h1><form action=\"metadata\">";
	    msg += "Alignment id:  <select name=\"id\">";
	    for( Enumeration e = manager.alignments(); e.hasMoreElements(); ){
		String id = ((Alignment)e.nextElement()).getExtension( Annotations.ALIGNNS, Annotations.ID);
		msg += "<option value=\""+id+"\">"+id+"</option>";
	    }
	    msg += "</select><br /><input type=\"submit\" value=\"Get metadata\"/></form>";
	} else if ( perf.equals("metadata") ) {
	    Message answer = manager.render( new Message(newId(),(Message)null,myId,serverId,"", params) );
	    //System.err.println("Content: "+answer.getContent());
	    if ( answer instanceof ErrorMsg ) {
		msg = testErrorMessages( answer );
	    } else {
		// Depending on the type we should change the MIME type
		return new Response( HTTP_OK, MIME_HTML, answer.getContent() );
	    }
	    // render
	    // Alignment in HTML can be rendre or metadata+tuples
	} else if ( perf.equals("") ) {
	    msg = "<h1>Alignment Server commands</h1>";
	    msg += "<form action=\"../admin/listalignments\"><button title=\"List of all the alignments stored in the server\" type=\"submit\">Available alignments</button></form>";
	    msg += "<form action=\"prmload\"><button title=\"Upload an existing alignment in this server\" type=\"submit\">Load alignments</button></form>";
	    msg += "<form action=\"prmfind\"><button title=\"Find existing alignements between two ontologies\" type=\"submit\">Find alignment</button></form>";
	    msg += "<form action=\"prmalign\"><button title=\"Apply matchers to ontologies for obtaining an alignment\" type=\"submit\">Match ontologies</button></form>";
	    msg += "<form action=\"prmcut\"><button title=\"Trim an alignment above some threshold\" type=\"submit\">Trim alignment</button></form>";
	    msg += "<form action=\"prminv\"><button title=\"Swap the two ontologies of an alignment\" type=\"submit\">Invert alignment</button></form>";
	    msg += "<form action=\"prmstore\"><button title=\"Persistently store an alignent in this server\" type=\"submit\" >Store alignment</button></form>";
	    msg += "<form action=\"prmretrieve\"><button title=\"Render an alignment in a particular format\" type=\"submit\">Render alignment</button></form>";
	    msg += "<form action=\"../admin/\"><button style=\"background-color: lightpink;\" title=\"Server management functions\" type=\"submit\">Server management</button></form>";
	} else {
	    msg = "Cannot understand command "+perf;
	}
	return new Response( HTTP_OK, MIME_HTML, "<html><head>"+HEADER+"</head><body>"+msg+"<hr /><center><small><a href=\".\">Alignment server</a></small></center></body></html>" );
    }

    // ===============================================
    // Util

    public Response wsdlAnswer(String uri, String perf, Properties header, Parameters params  ) {
	/*
	String msg = "";
	try {
	    FileReader fr = null;
	    String temp;
	    // JE: I would not... but absolutely not do this
	    fr = new FileReader ("WSAlignSVC.wsdl");
	    BufferedReader inFile = new BufferedReader( fr );
	    while ((temp = inFile.readLine()) != null) {
		//msg = msg + line + "\n";
		msg =msg + temp;
	    }
	    if (fr != null)  fr.close();
	} catch (IOException e) { e.printStackTrace(); }
	*/
	return new Response( HTTP_OK, MIME_XML, WSAServProfile.wsdlAnswer() );
    }	 

    private String testErrorMessages( Message answer ) {
	return "<h1>Alignment error</h1>"+answer.HTMLString();
    }

    private String displayAnswer ( Message answer ) {
	String result = answer.HTMLString();
	// Improved return
	if ( answer instanceof AlignmentId && ( answer.getParameters() == null || answer.getParameters().getParameter("async") == null ) ){
	    result += "<table><tr>";
	    // STORE
	    result += "<td><form action=\"store\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Store\"/></form></td>";
	    // TRIM (2)
	    result += "<td><form action=\"prmcut\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Trim\"/></form></td>";
	    // RETRIEVE (1)
	    result += "<td><form action=\"prmretrieve\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Show\"/></form></td>";
	    // Note at that point it is not possible to get the methods
	    // COMPARE (2)
	    // INV
	    result += "<td><form action=\"inv\"><input type=\"hidden\" name=\"id\" value=\""+answer.getContent()+"\"/><input type=\"submit\" name=\"action\" value=\"Invert\"/></form></td>";
	    result += "</tr></table>";
	}
	return result;
    }

    private int newId() { return localId++; }

    private void decodeParms( String parms, Properties p ) throws InterruptedException {
	if ( parms == null ) return;
	
	StringTokenizer st = new StringTokenizer( parms, "&" );
	while ( st.hasMoreTokens())	{
	    String next = st.nextToken();
	    int sep = next.indexOf( '=' );
	    if ( sep >= 0 ){
		try {
		    p.put( URLDecoder.decode( next.substring( 0, sep ), "iso-8859-1" ).trim(),
			   URLDecoder.decode( next.substring( sep+1 ), "iso-8859-1" ));
		} catch (Exception e) {}; //never thrown
	    }
	}
    }

    // ==================================================
    // HTTP Machinery

    /**
     * HTTP response.
     * Return one of these from serve().
     */
    public class Response {
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public Response() {
	    this.status = HTTP_OK;
	}

	/**
	 * Basic constructor.
	 */
	public Response( String status, String mimeType, InputStream data ) {
	    this.status = status;
	    this.mimeType = mimeType;
	    this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of
	 * given text.
	 */
	public Response( String status, String mimeType, String txt ) {
	    this.status = status;
	    this.mimeType = mimeType;
	    this.data = new ByteArrayInputStream( txt.getBytes());
	    // JE: Added
	    this.msg = txt;
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader( String name, String value ) {
	    header.put( name, value );
	}


	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	public String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader()
	 * to add lines.
	 */
	public Properties header = new Properties();
	// JE: Added for testing Jetty
	public String msg;
	public String getStatus() { return status; };
	public String getContentType() { return mimeType; }
	public String getData() { return msg; }

    }

    /**
     * Two private cclasses for retrieving parameters
     */
    private class ParameterServletResponseWrapper extends ServletResponseWrapper  {
	private Map parameters;

	public ParameterServletResponseWrapper( ServletResponse r ){
	    super(r);
	};

	public Map getParameterMap(){ return parameters; }
 
	public void setParameterMap( Map m ){ parameters = m; }
 
     }

    private class Chain implements FilterChain {
 
	public void doFilter( ServletRequest request, ServletResponse response)
	    throws IOException, ServletException {
	    if ( response instanceof ParameterServletResponseWrapper &&
		 request instanceof ServletRequestWrapper ) {
		((ParameterServletResponseWrapper)response).setParameterMap( ((ServletRequestWrapper)request).getParameterMap() );
	    }
         }
 
     }
}

