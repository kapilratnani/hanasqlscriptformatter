package net.whiteants.util;

import java.util.StringTokenizer;

public class SQLFormatter {

	private String source;
	public static String indentString = "    ";
	private String initial = "";
	private static Keywords keywords = new Keywords();
	private int initIndent = 0;

	public SQLFormatter(String sql, int initOffsetLen, int initIndent) {
		createInitialOffsetString(initOffsetLen);
		this.initIndent = initIndent;
		source = sql;
	}

	private void createInitialOffsetString(int initOffsetLen) {
		for (int i = 0; i < initOffsetLen; i++) {
			this.initial += " ";
		}
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
		System.out
				.println(new SQLFormatter(
						"select\n"
								+ "	 R.NAME AS ROLE_NAME\n"
								+ "FROM SYS.\"P_PRINCIPALS_\" P, SYS.P_PRINCIPALS_ R, SYS.P_PRINCIPALS_ G, SYS.P_ASSIGNEDROLES_ AR \n"
								+ "WHERE P.OID = AR.GRANTEEID \n"
								+ "AND G.OID = AR.GRANTERID \n"
								+ "AND AR.ROLEID = R.OID \n"
								+ "AND P.NAME = :grantee", 4, 1).format());
	}
}