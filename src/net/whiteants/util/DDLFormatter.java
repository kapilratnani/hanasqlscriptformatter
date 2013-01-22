package net.whiteants.util;

import java.util.StringTokenizer;

public class DDLFormatter {

	private static String indentString = "    ";
	private static Keywords keywords = new Keywords();

	public String format(String sql) {
		if (sql.toLowerCase().startsWith("create table")) {
			return formatCreateTable(sql);
		} else if (sql.toLowerCase().startsWith("create column table")) {
			return formatCreateTable(sql);
		} else if (sql.toLowerCase().startsWith("create procedure")) {
			return formatCreateProcedure(sql);
		} else if (sql.toLowerCase().startsWith("alter table")) {
			return formatAlterTable(sql);
		} else if (sql.toLowerCase().startsWith("comment on")) {
			return formatCommentOn(sql);
		} else {
			return "\n    " + sql;
		}
	}

	private String formatCommentOn(String sql) {
		StringBuffer result = new StringBuffer().append("\n    ");
		StringTokenizer tokens = new StringTokenizer(sql, " '[]\"", true);

		boolean quoted = false;
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			result.append(token);
			if (isQuote(token)) {
				quoted = !quoted;
			} else if (!quoted) {
				if ("is".equals(token)) {
					result.append("\n       ");
				}
			}
		}

		return result.toString();
	}

	private String formatAlterTable(String sql) {
		StringBuffer result = new StringBuffer().append("\n    ");
		StringTokenizer tokens = new StringTokenizer(sql, " (,)'[]\"", true);

		boolean quoted = false;
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			if (isQuote(token)) {
				quoted = !quoted;
			} else if (!quoted) {
				if (isBreak(token)) {
					result.append("\n        ");
				}
			}
			result.append(token);
		}

		return result.toString();
	}

	private String formatCreateProcedure(String sql) {
		return formatCreateTable(sql);
	}

	private String formatCreateTable(String sql) {
		StringBuffer result = new StringBuffer().append("");
		StringTokenizer tokens = new StringTokenizer(sql, " /*-(,)'[]\"\n", true);

		int depth = 0;
		boolean quoted = false;
		boolean beginLine = true;
		String token;
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();

			if (token.equals("-")) {
				String s = tokens.nextToken();
				if (s.equals("-")) {
					token = "--";
					while (!s.equals("\n")) {
						s = tokens.nextToken();
						token += s;
					}
					result.append(token + "\n" + indentString);
					continue;
				} else {
					token += s;
				}
			}
			
			if (token.equals("/")) {
				String s = tokens.nextToken();
				if (s.equals("*")) {
					token = "/*";
					while (!token.endsWith("*/")) {
						s = tokens.nextToken();
						token += s;
					}
					result.append(token + "\n" + indentString);
					continue;
				} else {
					token += s;
				}
			}
			

			if (isQuote(token)) {
				quoted = !quoted;
				beginLine = false;
				result.append(token);
			} else if (quoted) {
				beginLine = false;
				result.append(token);
			} else {
				if (")".equals(token)) {
					depth--;
					if (depth == 0) {
						result.append("\n");
						beginLine = true;
					}
				}

				if (!" ".equals(token) || !beginLine) {
					beginLine = false;
					result.append(token);
				}
				if (",".equals(token) && depth == 1) {
					result.append("\n" + indentString);
					beginLine = true;
				}
				if ("(".equals(token)) {
					depth++;
					if (depth == 1) {
						result.append("\n" + indentString);
						beginLine = true;
					}
				}
			}
		}

		return result.toString();
	}

	private static boolean isBreak(String token) {
		return "drop".equals(token) || "add".equals(token)
				|| "references".equals(token) || "foreign".equals(token)
				|| "on".equals(token);
	}

	private static boolean isQuote(String tok) {
		return "\"".equals(tok) || "`".equals(tok) || "]".equals(tok)
				|| "[".equals(tok) || "'".equals(tok);
	}

	public static void main(String[] args) {

	}

}