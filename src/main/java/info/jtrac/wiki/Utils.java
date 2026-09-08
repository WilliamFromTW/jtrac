package info.jtrac.wiki;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;

public class Utils {

    private static String encoding = "UTF-8";

    public static void setEncoding (String enc) {
        encoding = enc;
    }

	@SuppressWarnings("deprecation")
    public static String escape (String name) {
		if (name == null)
			return null;
		try {
			return URLEncoder.encode(name, encoding);
		} catch (UnsupportedEncodingException ueex) {
			return URLEncoder.encode(name);
		}
	}

	@SuppressWarnings("deprecation")
	public static String unescape (String name) {
		if (name == null)
			return null;
		try {
			return URLDecoder.decode(name, encoding);
		} catch (UnsupportedEncodingException ueex) {
			return URLDecoder.decode(name);
		}
	}

	// sanitize page name against XSS attacks, only allow certain characters
	// http://www.coderanch.com/t/600972/Moderators/
	public static String sanitize (String str) {
		if (str == null)
			return null;
		else
			return str.replaceAll("[^\\p{L}\\d_/%\\. -]", "");
	}

	/**
	 * Replace XML special characters with escape sequences,
	 * but not if they are escaped already.
	 */
    public static String xmlEncode (String str) {
		StringBuilder sb = new StringBuilder(str);

		int startAt = 0;
		while (startAt < sb.length()) {
			switch (sb.charAt(startAt)) {
				case '&':
					if ((startAt != sb.indexOf("&lt;", startAt))
						&& (startAt != sb.indexOf("&gt;", startAt))
						&& (startAt != sb.indexOf("&amp;", startAt)))
						sb.insert(startAt+1, "amp;");
					startAt += 4;
					break;
				case '<':
					sb.setCharAt(startAt, '&');
					sb.insert(startAt+1, "lt;");
					startAt += 3;
					break;
				case '>':
					sb.setCharAt(startAt, '&');
					sb.insert(startAt+1, "gt;");
					startAt += 3;
					break;
				default:
			}
			startAt++;
		}

		return sb.toString();
	}

	/**
	 * Un-Escapes XML special characters
	 */
    public static String xmlDecode (String str) {
		StringBuilder sb = new StringBuilder(str);

		int startAt = 0;
		while (startAt < sb.length()) {
			switch (sb.charAt(startAt)) {
				case '&':
					if (startAt == sb.indexOf("&lt;", startAt)) {
						sb.setCharAt(startAt, '<');
						sb.delete(startAt+1, startAt+4);
					} else if (startAt == sb.indexOf("&gt;", startAt)) {
						sb.setCharAt(startAt, '>');
						sb.delete(startAt+1, startAt+4);
					} else if (startAt == sb.indexOf("&amp;", startAt)) {
						sb.setCharAt(startAt, '&');
						sb.delete(startAt+1, startAt+5);
					} else if (startAt == sb.indexOf("&apos;", startAt)) {
						sb.setCharAt(startAt, '\'');
						sb.delete(startAt+1, startAt+6);
					} else if (startAt == sb.indexOf("&quot;", startAt)) {
						sb.setCharAt(startAt, '"');
						sb.delete(startAt+1, startAt+6);
					}
					break;
				default:
			}
			startAt++;
		}

		return sb.toString();
	}

	private static final String UNICODE =
		 "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"             
		+"\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" 
		+"\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" 
		+"\u00C3\u00E3\u00D1\u00F1" 
		+"\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" 
		+"\u00C5\u00E5"
		+"\u00C7\u00E7"
	;

	private static final String PLAIN_ASCII =
		  "AaEeIiOoUu"    // grave
		+ "AaEeIiOoUuYy"  // acute
		+ "AaEeIiOoUuYy"  // circumflex
		+ "AaNn"          // tilde
		+ "AaEeIiOoUuYy"  // umlaut
		+ "Aa"            // ring
		+ "Cc"            // cedilla
	;

	/**
	 * Remove accented characters from a string and replace with their ASCII equivalents.
	 * Also lowercases the string. Treats German umlauts as their non-umlaut equivalents.
	 */
	public static String normalizeText (String s) {
		if (s == null)
			return "";
		/*
		s = s.replaceAll("\\&auml;", "ae");
		s = s.replaceAll("\\&Auml;", "ae");
		s = s.replaceAll("\\&ouml;", "oe");
		s = s.replaceAll("\\&Ouml;", "oe");
		s = s.replaceAll("\\&uuml;", "ue");
		s = s.replaceAll("\\&Uuml;", "ue");
		s = s.replaceAll("\\&szlig;", "ss");
		*/

		StringBuilder sb = new StringBuilder(2 * s.length());
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
                case '\u00C4':
                case '\u00E4':
                    sb.append("ae");
                    break;
                case '\u00D6':
                case '\u00F6':
                    sb.append("oe");
                    break;
                case '\u00DC':
                case '\u00FC':
                    sb.append("ue");
                    break;
                case '\u00DF':
					sb.append("ss");
					break;
				case '\u20AC':
					// remove Euro symbol
					break;
				default:
					int pos = UNICODE.indexOf(c);
					if (pos > -1) {
						sb.append(PLAIN_ASCII.charAt(pos));
					} else {
						sb.append(c);
					}
			}
			//System.out.println(c+"("+((int)c)+")="+UNICODE.indexOf(c));
		}

		return sb.toString();
	}
}

