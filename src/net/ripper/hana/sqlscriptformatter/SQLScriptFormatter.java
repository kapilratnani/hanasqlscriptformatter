package net.ripper.hana.sqlscriptformatter;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ripper.util.StringUtil;
import net.whiteants.util.DDLFormatter;
import net.whiteants.util.Keywords;
import net.whiteants.util.SQLFormatter;

public class SQLScriptFormatter {

	static String test2 = "CREATE PROCEDURE EDIT_NETWORK_STATUS ( --used while updating the status of network like - activating, blocking, closing or draft\n"
			+ " \n"
			+ "              IN networkId VARCHAR(32),\n"
			+ "              IN networkStatus VARCHAR(15),\n"
			+ "              IN networkModifiedBy VARCHAR(32),\n"
			+ "            OUT networkModifiedId VARCHAR(32),\n"
			+ "            OUT statusMaxSeverity TINYINT,\n"
			+ "            OUT statusLog VARCHAR (3000))\n"
			+ "             \n"
			+ "LANGUAGE SQLSCRIPT AS\n"
			+ "           \n"
			+ "            v_count INTEGER;\n"
			+ "            v_ownerId VARCHAR(32);\n"
			+ "            v_ownerType VARCHAR(32);\n"
			+ "            v_timestamp TIMESTAMP;\n"
			+ "           \n"
			+ "BEGIN\n"
			+ " \n"
			+ "            statusLog := '' ;\n"
			+ "            statusMaxSeverity := 0;\n"
			+ " \n"
			+ "--check if the network is present in the database ?\n"
			+ " \n"
			+ "IF :networkId IS NULL OR :networkId =''\n"
			+ "THEN\n"
			+ "            CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog, statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_REQUIRED', 3);\n"
			+ "            return;\n"
			+ "ELSE\n"
			+ "                        select count(id) into v_count from network as n  where n.id=:networkId;\n"
			+ "                        IF :v_count < 1\n"
			+ "                        THEN\n"
			+ "                                    CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog,statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_INVALID', 3);\n"
			+ "                                    return;\n"
			+ "                        END IF;\n"
			+ "END IF;\n"
			+ " \n"
			+ "--check if the Network modifier user id is valid (pernissions are checked later in this procedure) ?\n"
			+ " \n"
			+ "IF :networkModifiedBy IS NULL OR :networkModifiedBy =''\n"
			+ "THEN\n"
			+ "            CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog, statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_MODIFIED_BY_USER_REQUIRED', 3);\n"
			+ "            return;\n"
			+ "ELSE\n"
			+ "                        select count(uuid) into v_count from profile as p where p.uuid=:networkModifiedBy;\n"
			+ "                        IF :v_count < 1\n"
			+ "                        THEN\n"
			+ "                                    CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog,statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_MODIFIED_BY_USER_INVALID', 3);\n"
			+ "                                    return;\n"
			+ "                        END IF;\n"
			+ "END IF;           \n"
			+ " \n"
			+ "--get the ownerId and ownertype of the edited network\n"
			+ " \n"
			+ "select owner_id,owner_type into v_ownerId,v_ownerType from network where id = :networkId;\n"
			+ " \n"
			+ "--check if the modifier of the network is admin or moderator of the owner_community if ownertype is community?\n"
			+ " \n"
			+ "IF :v_ownerType = 'COMMUNITY' or :v_ownerType = 'community'\n"
			+ "THEN\n"
			+ "            select count(id) into v_count from membership as m where m.community_id = :v_ownerId and m.user_id = :networkModifiedBy and (m.type = 20 or m.type=10) and m.status=20;\n"
			+ "            IF :v_count < 1\n"
			+ "            THEN\n"
			+ "                        CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog,statusMaxSeverity, 'Network.Edit_Status.ERROR_EDIT_PERMISSION_REQUIRED', 3);\n"
			+ "                        return;\n"
			+ "            END IF;\n"
			+ "END IF;\n"
			+ " \n"
			+ "--check if the network status is from valid domain and not empty ?\n"
			+ " \n"
			+ "IF :networkStatus='' or :networkStatus IS NULL\n"
			+ "THEN\n"
			+ "            CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog,statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_STATUS_REQUIRED', 3);\n"
			+ "            return;\n"
			+ "ELSE\n"
			+ "            select count(*) into v_count from dummy where :networkStatus in ('ACTIVE','DRAFT','BLOCKED','CLOSED','active','draft','blocked','closed');\n"
			+ "            IF :v_count=0\n"
			+ "            THEN\n"
			+ "                        CALL CHARITRA_RO._APPEND_STATUSLOG (statusLog,statusMaxSeverity, 'Network.Edit_Status.ERROR_NETWORK_STATUS_VALUE_INVALID', 3);\n"
			+ "                        return;\n"
			+ "            END IF;\n"
			+ "END IF;\n"
			+ " \n"
			+ "-- call update query\n"
			+ " \n"
			+ "v_timestamp := CURRENT_UTCTIMESTAMP;\n"
			+ " \n"
			+ "UPDATE NETWORK SET STATUS=:networkStatus, MODIFIED_BY = :networkModifiedBy, MODIFIED_ON=:v_timestamp where Id = networkId;  \n"
			+ " \n" + "END;\n" + " ";
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

	public static void main(String[] args) {
		System.out.println(new SQLScriptFormatter().format(test2));
	}
}