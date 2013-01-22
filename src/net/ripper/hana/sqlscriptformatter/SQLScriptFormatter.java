package net.ripper.hana.sqlscriptformatter;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ripper.util.StringUtil;
import net.whiteants.util.DDLFormatter;
import net.whiteants.util.Keywords;
import net.whiteants.util.SQLFormatter;

public class SQLScriptFormatter {
	private static Keywords keywords = new Keywords();

	private static Pattern assignExp = Pattern.compile(
			"(.*)\\s*:=\\s*([^;]*);", Pattern.DOTALL);
	private static Pattern selectAssignExp = Pattern.compile(
			"(.*)\\s*=\\s*select\\s+.*;", Pattern.DOTALL
					| Pattern.CASE_INSENSITIVE);
	private static Pattern queryOrDmlExp = Pattern.compile(
			"(select|delete|update|insert)\\s+[^;]*;", Pattern.DOTALL
					| Pattern.CASE_INSENSITIVE);
	private static Pattern genericAssignExp = Pattern.compile(
			"(.*)\\s*=\\s*([^;]*);", Pattern.DOTALL);
	private static Pattern createProcExp = Pattern.compile(
			"create\\s+procedure\\s+(.*)\\s+(as)", Pattern.DOTALL
					| Pattern.CASE_INSENSITIVE);
	private static DDLFormatter ddlFormatter = new DDLFormatter();
	private StringBuilder result = new StringBuilder();
	private String cString = "";
	private int indent = 0;

	public String format(String sqlScript) {
		StringTokenizer tokens = new StringTokenizer(sqlScript + "\n",
				"()+*//*-=<>'`\"[],;" + keywords.getWhitespace(), true);

		boolean afterBegin = false;
		boolean afterQueryOrDml = false;
		boolean afterEnd = false;

		String token, lastToken = "";
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken().replaceAll("[\\r\\n\\t]", " ");

			if (cString.trim().startsWith("/*")
					|| cString.trim().startsWith("--")
					|| cString.trim().endsWith("--")) {
				cString = cString.trim();
				// handle comments
				boolean dump = true;
				boolean multiline = false;
				// comments inside a query should not be dumped
				// include it with the query and send it to sql formatter
				if (cString.endsWith("--") && (afterQueryOrDml || !afterBegin))
					dump = false;
				// don't format comments, just paste them as is
				if (cString.startsWith("/*")) {
					multiline = true;
					while (tokens.hasMoreTokens()) {
						cString += token;
						token = tokens.nextToken();
						if (cString.endsWith("*/"))
							break;
					}
				} else {
					while (tokens.hasMoreTokens() && !"\n".equals(token)
							&& !"\r".equals(token)) {
						cString += token;
						token = tokens.nextToken();
					}
					cString += token;
				}

				if (dump) {
					if (!multiline) {
						indent();
					}
					out();
					newline();
					cString = "";
				}
			} else if (token.equals("\"") || token.equals("'")) {
				while (tokens.hasMoreTokens()) {
					cString += token;
					token = tokens.nextToken();
					if (token.equals("\"") || token.equals("'"))
						break;
				}
				cString += token;
			} else if ("begin".equalsIgnoreCase(token)) {
				indent--;
				cString = token;
				out();
				newline();
				indent++;
				afterBegin = true;
				cString = "";
			} else if ("end".equalsIgnoreCase(token) && !afterQueryOrDml) {
				afterEnd = true;
				cString += token;
			} else if (keywords.getDml().contains(token)
					|| "select".equalsIgnoreCase(token)) {
				afterQueryOrDml = true;
				cString += token;
			} else if ("else".equalsIgnoreCase(token) && !afterQueryOrDml) {
				indent--;
				indent();
				cString = token;
				out();
				newline();
				cString = "";
				indent++;
				// afterElse = true;
			} else if (StringUtil.endsWithIgnoreCase(cString, "end if;")
					|| StringUtil.endsWithIgnoreCase(cString, "end if ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (StringUtil.endsWithIgnoreCase(cString, "end while;")
					|| StringUtil.endsWithIgnoreCase(cString, "end while ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (StringUtil.endsWithIgnoreCase(cString, "end for;")
					|| StringUtil.endsWithIgnoreCase(cString, "end for ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (StringUtil.endsWithIgnoreCase(cString, "end;")
					|| StringUtil.endsWithIgnoreCase(cString, "end ;")) {
				indent--;
				indent();
				out();
				newline();
				cString = "";
				afterEnd = false;
				afterBegin = false;
			} else if ("break;".equalsIgnoreCase(cString.trim())
					|| "continue;".equalsIgnoreCase(cString.trim())) {
				indent();
				out();
				newline();
				cString = "";
			} else if (!afterEnd && "while".equalsIgnoreCase(token)) {
				cString = token;
				lastToken = "";
				while (tokens.hasMoreTokens() && !"do".equalsIgnoreCase(token)) {
					token = tokens.nextToken().replaceAll("[\r\n\t]", " ");
					if (!lastToken.equals(" ")
							|| !lastToken.equalsIgnoreCase(token))
						cString += token;
					lastToken = token;
				}
				indent();
				out();
				newline();
				indent++;
				cString = "";
			} else if (!afterEnd && "for".equalsIgnoreCase(token)) {
				cString = token;
				lastToken = "";
				while (tokens.hasMoreTokens()
						&& !"then".equalsIgnoreCase(token)) {
					token = tokens.nextToken().replaceAll("[\r\n\t]", " ");
					if (!lastToken.equals(" ") || !lastToken.equals(token))
						cString += token;
					lastToken = token;
				}
				indent();
				out();
				newline();
				indent++;
				cString = "";
			} else if (!afterEnd
					&& ("if".equalsIgnoreCase(token.trim()) || "elseif"
							.equalsIgnoreCase(token.trim()))) {
				cString = token;
				lastToken = "";
				while (tokens.hasMoreTokens()
						&& !"then".equalsIgnoreCase(token)) {
					token = tokens.nextToken().replaceAll("[\r\n\t]", " ");
					if (!lastToken.equals(" ") || !lastToken.equals(token))
						cString += token;
					lastToken = token;
				}
				indent();
				indent++;

				out();
				newline();

				cString = "";
			} else if (afterBegin && "call".equalsIgnoreCase(token)) {
				cString = token;
				lastToken = "";
				while (tokens.hasMoreTokens() && !";".equalsIgnoreCase(token)) {
					token = tokens.nextToken().replaceAll("[\r\n\t]", " ");
					if (!lastToken.equals(" ")
							|| !lastToken.equalsIgnoreCase(token))
						cString += token;
					lastToken = token;
				}

				indent();

				out();
				newline();
				cString = "";
			} else if (!afterBegin
					&& createProcExp.matcher(cString.trim()).matches()) {
				cString = ddlFormatter.format(cString.trim());
				out();
				newline();
				indent++;
				cString = "";
			} else if (afterBegin
					&& assignExp.matcher(cString.trim()).matches()) {
				Matcher matcher = assignExp.matcher(cString);
				indent();

				matcher.find();

				cString = matcher.group(1).trim() + " := "
						+ matcher.group(2).trim() + ";";
				out();
				newline();
				cString = "";
				afterQueryOrDml = false;
			} else if (afterBegin
					&& selectAssignExp.matcher(cString.trim()).matches()) {

				Matcher matcher = selectAssignExp.matcher(cString.trim());
				matcher.find();

				indent();

				// calculate indent and offset here
				int initIndent = (int) (indent + 1 + Math.ceil((matcher
						.group(1).trim().length() + 2) / 4));
				int initOffset = (int) (initIndent * 4 - (indent * 4
						+ matcher.group(1).trim().length() + 2));

				cString = matcher.group(1).trim()
						+ " ="
						+ new SQLFormatter(cString.substring(cString
								.indexOf("select")), initOffset, initIndent)
								.format();
				out();
				newline();
				newline();
				cString = "";
				afterQueryOrDml = false;
			} else if (afterBegin
					&& queryOrDmlExp.matcher(cString.trim()).matches()) {
				cString = new SQLFormatter(cString.trim(), 4 * indent, indent)
						.format();
				out();
				newline();
				newline();
				cString = "";
				afterQueryOrDml = false;
			} else if (afterBegin
					&& genericAssignExp.matcher(cString.trim()).matches()) {
				Matcher matcher = genericAssignExp.matcher(cString);
				indent();

				matcher.find();

				cString = matcher.group(1).trim() + " := "
						+ matcher.group(2).trim() + ";";
				out();
				newline();
				cString = "";
				afterQueryOrDml = false;
			} else if (cString.endsWith(";")) {
				cString = cString.trim();
				indent();
				out();
				newline();
				cString = "";
			} else {
				// ignore repeated whitespace characters
				if (!keywords.getWhitespace().contains(lastToken)
						|| !keywords.getWhitespace().contains(token))
					cString += token;
			}
			lastToken = token;
		}
		return result.toString();
	}

	private void out() {
		result.append(cString);
	}

	private void indent() {
		for (int i = 0; i < indent; i++) {
			result.append(SQLFormatter.indentString);
		}
	}

	private void newline() {
		result.append("\n");
	}
}