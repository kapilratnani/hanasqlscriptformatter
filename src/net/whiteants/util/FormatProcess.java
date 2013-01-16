package net.whiteants.util;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class FormatProcess {
	boolean beginLine = true;
	boolean afterBeginBeforeEnd = false;
	boolean afterByOrSetOrFromOrSelect = false;
	boolean afterValues = false;
	boolean afterOn = false;
	boolean afterBetween = false;
	boolean afterInsert = false;
	boolean afterMisc = false;
	boolean beforeMisc = false;
	boolean afterComment = false;
	// boolean afterEndClause = false;
	private static int maxCharsInLine = 80;
	private int charsInLineCount = 0;

	int inFunction = 0;
	int parentsSinceSelect = 0;
	private LinkedList<Integer> parentCounts = new LinkedList<Integer>();
	private LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<Boolean>();

	int indent = 1;

	StringBuffer result = new StringBuffer();
	StringTokenizer tokens;
	String lastToken;
	String token;
	String lcToken;

	private String initial;
	private String indentString;
	private Keywords keywords = new Keywords();

	public FormatProcess(String sql, String initial, String indentString,
			int initIndent) {
		this.initial = initial;
		this.indentString = indentString;
		this.indent = initIndent;
		tokens = new StringTokenizer(sql, "()+*/-=<>'`\"[],"
				+ keywords.getWhitespace(), true);
	}

	public String perform() {

		result.append(initial);

		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
			lcToken = token.toLowerCase();

			if (token.equals("\"") || token.equals("'")) {
				String t = "";
				for (; !t.equals("\"") && !t.equals("'"); t = tokens
						.nextToken())
					token += t;
				token += t;
				out();

				if (afterByOrSetOrFromOrSelect) {
					charsInLineCount += token.length();
				}

				continue;
			}

			if (token.equals("-")) {
				String s = tokens.nextToken();
				// i think this is for single line comments
				if (s.equals("-")) {
					token = "--";
					while (!s.equals("\n")) {
						s = tokens.nextToken();
						token += s;
					}
					afterComment = true;
				} else {
					// adding this, because it ignores strings like "-1"
					token += s;
				}

			}

			if (afterByOrSetOrFromOrSelect && ",".equals(token)
					&& inFunction <= 0) {
				commaAfterByOrFromOrSelect();
			} else if ("=".equals(token)) {
				out();
				token = token.trim();
			} else if (afterOn && ",".equals(token)) {
				commaAfterOn();
			} else if ("(".equals(token)) {
				openParent();
			} else if (")".equals(token)) {
				closeParent();
			} else if (keywords.getBegin_clauses().contains(lcToken)) {
				beginNewClause();
			} else if (keywords.getEnd_clauses().contains(lcToken)) {
				endNewClause();
			} else if ("select".equals(lcToken)) {
				select();
			} else if (keywords.getDml().contains(lcToken)) {
				updateOrInsertOrDelete();
			} else if ("values".equals(lcToken)) {
				values();
			} else if ("on".equals(lcToken)) {
				on();
			} else if (afterBetween && lcToken.equals("and")) {
				misc();
				afterBetween = false;
			} else if (keywords.getLogical().contains(lcToken)) {
				logical();
			} else if (isWhitespace(token)) {
				white();
			} else if (".".equals(token)) {
				out();
			} else if (";".equals(token)) {
				out();
				newline();
			} else if (",".equals(token)) {
				out();
			} else {
				misc();
			}

			if (!isWhitespace(token)) {
				lastToken = lcToken;
			}

		}
		return result.toString();
	}

	private void commaAfterOn() {
		out();
		indent--;
		newline();
		afterOn = false;
		afterByOrSetOrFromOrSelect = true;
	}

	private void commaAfterByOrFromOrSelect() {
		out();
		charsInLineCount++;
		if (charsInLineCount > maxCharsInLine) {
			newline();
			charsInLineCount = 0;
		}
	}

	private void logical() {
		if ("end".equals(lcToken)) {
			indent--;
		}
		newline();
		out();
		beginLine = false;
	}

	private void on() {
		indent++;
		afterOn = true;
		newline();
		out();
		beginLine = false;
	}

	private void misc() {
		afterMisc = true;
		if (afterValues) {
			newline();
			out();
			return;
		}

		if (afterByOrSetOrFromOrSelect) {
			charsInLineCount += token.length();

			if ("case".equals(lcToken)) {
				newline();
			}
		}
		out();
		if ("between".equals(lcToken)) {
			afterBetween = true;
		}
		if (afterComment) {
			afterComment = false;
			newline();
		}
		if (afterInsert) {
			newline();
			afterInsert = false;
		} else {
			beginLine = false;
			if ("case".equals(lcToken)) {
				indent++;
			}
		}
	}

	private void white() {
		if (!beginLine) {
			result.append(" ");
		}
	}

	private void updateOrInsertOrDelete() {
		out();
		indent++;
		beginLine = false;
		if ("update".equals(lcToken)) {
			newline();
		}
		if ("insert".equals(lcToken)) {
			afterInsert = true;
		}
	}

	private void select() {
		out();
		indent++;
		newline();
		parentCounts.addLast(parentsSinceSelect);
		afterByOrFromOrSelects.addLast(afterByOrSetOrFromOrSelect);
		parentsSinceSelect = 0;
		afterByOrSetOrFromOrSelect = true;
	}

	private void out() {
		result.append(token);
	}

	private void endNewClause() {
		if (afterInsert && "into".equals(lcToken)) {
			newline();
		} else if (!afterBeginBeforeEnd && inFunction <= 0) {
			indent--;
			if (afterOn) {
				indent--;
				afterOn = false;
			}
			newline();
		}
		out();
		if (inFunction == 0) {
			if (!"union".equals(lcToken)) {
				indent++;
			}
			newline();
			afterBeginBeforeEnd = false;
			afterByOrSetOrFromOrSelect = "by".equals(lcToken)
					|| "set".equals(lcToken) || "from".equals(lcToken);
		}
	}

	private void beginNewClause() {
		// if (afterMisc)
		// newline();
		if (!afterBeginBeforeEnd) {
			if (afterOn) {
				indent--;
				afterOn = false;
			}
			indent--;
			newline();
		}

		out();
		beginLine = false;
		afterBeginBeforeEnd = true;
		afterMisc = false;
	}

	private void values() {
		indent--;
		newline();
		out();
		// indent++;
		// newline();
		afterValues = true;
	}

	private void closeParent() {
		parentsSinceSelect--;
		if (parentsSinceSelect < 0) {
			indent--;
			parentsSinceSelect = parentCounts.removeLast();
			afterByOrSetOrFromOrSelect = afterByOrFromOrSelects.removeLast();
		}
		if (inFunction > 0) {
			inFunction--;
			if (afterValues)
				newline();
			indent--;
			out();
		} else {
			if (!afterByOrSetOrFromOrSelect) {
				indent--;
				newline();
			}
			out();
		}
		beginLine = false;
		afterValues = false;
	}

	private void openParent() {
		if (isFunctionName(lastToken) || inFunction > 0) {
			inFunction++;
		}
		beginLine = false;
		if (inFunction > 0) {
			out();
			indent++;
		} else {
			out();
			if (!afterByOrSetOrFromOrSelect) {
				indent++;
				newline();
				beginLine = true;
			}
		}
		parentsSinceSelect++;
	}

	private boolean isFunctionName(String tok) {
		final char begin = tok.charAt(0);
		final boolean isIdentifier = Character.isJavaIdentifierStart(begin)
				|| '"' == begin;
		return isIdentifier && !keywords.getLogical().contains(tok)
				&& !keywords.getEnd_clauses().contains(tok)
				&& !keywords.getQuantifiers().contains(tok)
				&& !keywords.getDml().contains(tok)
				&& !keywords.getMisk().contains(tok);
	}

	private boolean isWhitespace(String token) {
		return keywords.getWhitespace().indexOf(token) >= 0;
	}

	private void newline() {
		result.append("\n");
		for (int i = 0; i < indent; i++) {
			result.append(indentString);
		}
		beginLine = true;
	}
}