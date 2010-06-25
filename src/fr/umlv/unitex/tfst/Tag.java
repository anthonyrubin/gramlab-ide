/*
 * Unitex
 *
 * Copyright (C) 2001-2010 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */

package fr.umlv.unitex.tfst;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tag {

	private String inflected;
	/* If lemma is null, it means that we have a non tag token like "2" */
	private String lemma;
	private ArrayList<String> gramCodes;
	private ArrayList<String> infCodes;
	
	/**
	 * Constructs a tag from a box content either of the form "{de,.PREP}" or "2"
	 */
	public Tag(String tag) {
		if (tag.equals("{") || !tag.startsWith("{")) {
			inflected=tag;
			lemma=null;
			return;
		}
		if (!tokenize(tag)) {
			throw new IllegalArgumentException("Invalid tag: "+tag);
		}
		/*
		System.err.print(tag+": "+inflected+","+lemma);
		for (int i=0;i<gramCodes.size();i++) {
			if (i==0) System.err.print(".");
			else System.err.print("+");
			System.err.print(gramCodes.get(i));
		}
		for (int i=0;i<infCodes.size();i++) {
			System.err.print(":");
			System.err.print(infCodes.get(i));
		}
		System.err.println();*/
	}
	
	
	private boolean tokenize(String tag) {
		if (!tag.startsWith("{") || !tag.endsWith("}")) return false;
		gramCodes=new ArrayList<String>();
		infCodes=new ArrayList<String>();
		StringBuilder b=new StringBuilder();
		int i=1;
		int size=tag.length()-1;
		/* Matching inflected part */
		while (i<size && tag.charAt(i)!=',') {
			if (tag.charAt(i)=='\\') {
				i++;
				if (i==size) return false;
			}
			b.append(tag.charAt(i));
			i++;
		}
		if (i==size) return false;
		inflected=b.toString();
		/* Matching lemma part */
		b.setLength(0);
		i++;
		while (i<size && tag.charAt(i)!='.') {
			if (tag.charAt(i)=='\\') {
				i++;
				if (i==size) return false;
			}
			b.append(tag.charAt(i));
			i++;
		}
		if (i==size) return false;
		lemma=b.toString();
		if (lemma.equals("")) lemma=inflected;
		b.setLength(0);
		/* Matching grammatical code part */
		while (i!=size && tag.charAt(i)=='.' || tag.charAt(i)=='+') {
			/* We may have several codes */
			i++;
			while (i<size && tag.charAt(i)!='+' && tag.charAt(i)!=':') {
				if (tag.charAt(i)=='\\') {
					i++;
					if (i==size) return false;
				}
				b.append(tag.charAt(i));
				i++;
			}
			gramCodes.add(b.toString());
			b.setLength(0);
		}
		if (gramCodes.size()==0) {
			/* Should be at least one grammatical code */
			return false;
		}
		if (i==size) return true;
		/* Matching grammatical code part */
		while (i!=size) {
			/* We may have several codes */
			i++;
			while (i<size && tag.charAt(i)!=':') {
				if (tag.charAt(i)=='\\') {
					i++;
					if (i==size) return false;
				}
				b.append(tag.charAt(i));
				i++;
			}
			infCodes.add(b.toString());
			b.setLength(0);
		}
		return true;
	}


	/**
	 * Returns a a representation of the tag where the grammatical code list
	 * matches the given pattern, or null if none code matches the pattern.
	 */
	public String toString(TagFilter f) {
		if (lemma==null) {
			return (f!=null && f.getPattern()!=null)?null:inflected;
		}
		StringBuilder b=new StringBuilder();
		if (!inflected.equals(lemma)) {
			b.append(inflected);
			b.append(",");
		}
		b.append(lemma);
		boolean first=true;
		Pattern p=(f==null)?null:f.getPattern();
		for (int i=0;i<gramCodes.size();i++) {
			String code=gramCodes.get(i);
			if ((i==0 && (f!=null && (f.alwaysShowGramCode() || f.onlyShowGramCode()))) || matches(code,p)) {
				if (first) {
					b.append(".");
					first=false;
				} else {
					b.append("+");
				}
				b.append(code);
			}
			if (i==0 && f!=null && f.onlyShowGramCode()) break;
		}
		if (first) {
			/* No code was added ? It's a fail case */
			return null;
		}
		for (int i=0;i<infCodes.size();i++) {
			b.append(":");
			b.append(infCodes.get(i));
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return toString(null);
	}

	private boolean matches(String code, Pattern p) {
		if (p==null) return true;
		Matcher m=p.matcher(code);
		return m.matches();
	}

}