/*
 * $Id: Message.java 532 2007-08-04 09:37:17Z euzenat $
 *
 * Copyright (C) INRIA Rh?e-Alpes, 2006-2007
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

import org.semanticweb.owl.align.Parameters;

/**
 * Contains the messages that should be sent according to the protocol
 */

public class Message {

    int surrogate = 0;
    Message inReplyTo = null;
    String receiver = "";
    String sender = "";
    String content = "";
    Parameters parameters = null;

    public Message ( int surr, Message rep, String from, String to, String cont, Parameters param ) {
	surrogate = surr;
	inReplyTo = rep;
	receiver = to;
	sender = from;
	content = cont;
	parameters = param;
    }

    public String HTMLString(){
	return "<h1>Message</h1><dl><dt>id:</dt><dd>"+surrogate+"</dd><dt>sender:</dt><dd>"+sender+"</dd><dt>receiver:</dt><dd>"+receiver+"</dd><dt>in-reply-to:</dt><dd>"+inReplyTo+"</dd><dt>content:</dt><dd>"+content+"</dd></dl>";
    }

    public String SOAPString(){
	return "<id>"+surrogate+"</id>"+"<sender>"+sender+"</sender>" + "<receiver>"+receiver+"</receiver>" + "<in-reply-to>" + inReplyTo+ "</in-reply-to>" + "<content>" + content + "</content>";	
    }

    public int getId () {
	return surrogate;
    }

    public Message getInReplyTo() {
	return inReplyTo;
    }

    public String getReceiver() {
	return receiver;
    }

    public String getContent() {
	return content;
    }

    public String getSender() {
	return sender;
    }

    public Parameters getParameters() {
	return parameters;
    }

}
