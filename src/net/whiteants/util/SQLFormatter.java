package net.whiteants.util;

import java.util.Arrays;
import java.util.StringTokenizer;

public class SQLFormatter {

	private String source;
	private String indentString = "    ";
	private String initial = "";
	private static Keywords keywords = new Keywords();
	private int initIndent = 0;

	public SQLFormatter(String sql, int initOffsetLen, int initIndent) {
		createInitialOffsetString(initOffsetLen);
		this.initIndent = initIndent;
		source = sql;
	}

	private void createInitialOffsetString(int initOffsetLen) {
		char[] csp = new char[initOffsetLen];
		Arrays.fill(csp, ' ');
		this.initial = new String(csp);
	}

	public String format() {
		return new FormatProcess(source, initial, indentString, initIndent)
				.perform();
	}

	public void setIndentString(String indent) {
		indentString = indent;
	}

	public void setInitial(String initial) {
		this.initial = initial;
	}

	public void toLowerCase() {
		StringTokenizer tokens = new StringTokenizer(source,
				"()+*//*-=<>'`\"[]," + keywords.getWhitespace(), true);
		source = "";
		for (String token = ""; tokens.hasMoreTokens(); token = tokens
				.nextToken())
			if (keywords.contains(token.toLowerCase()))
				source += token.toLowerCase();
			else
				source += token;
	}

	public void toUpperCase() {
		StringTokenizer tokens = new StringTokenizer(source,
				"()+*//*-=<>'`\"[]," + keywords.getWhitespace(), true);
		source = "";
		for (String token = ""; tokens.hasMoreTokens(); token = tokens
				.nextToken())
			if (keywords.contains(token.toLowerCase()))
				source += token.toUpperCase();
			else
				source += token;
	}

	public static void main(String[] args) {
			System.out.println(new SQLFormatter("select\n" + 
					"			ancestor,nasdj,dajsdksm,dajsdks,dkasjd,ajsdkasd,kjdashd,akjsdh,kajshd,kjashd,kajsdh,jkashd,jasd,kajsdh,dlakjs,asijd,lasjd,sldjasd,lasdj,alskjd,\n" + 
					"			:child_id \n" + 
					"		as\n" + 
					"			descendent,depth+1 \n" + 
					"		as\n" + 
					"			depth \n" + 
					"		from\n" + 
					"			collections_hierarchy.hierarchy \n" + 
					"		where\n" + 
					"			descendent=:parent_id \n" + 
					"		union\n" + 
					"		select\n" + 
					"			:child_id,\n" + 
					"			:child_id,\n" + 
					"			0 \n" + 
					"		from\n" + 
					"			dummy;", 0, 1).format());
	}
}