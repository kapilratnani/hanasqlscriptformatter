package net.whiteants.util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		StringTokenizer tokens = new StringTokenizer(sql, " (,)'[]\"", true);

		int depth = 0;
		boolean quoted = false;
		boolean beginLine = true;
		String token;
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
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
		// System.out
		// .println(new DDLFormatter()
		// .format("CREATE COLUMN TABLE \"COLLECTIONS_HIERARCHY\".\"POSITION\" (\"ID\" VARCHAR(20) NOT NULL ,\n"
		// + "	 \"NAME\" VARCHAR(100),\n"
		// + "	 PRIMARY KEY (\"ID\"));"));

		Pattern assignExp = Pattern.compile("(.*)\\s*=\\s*select\\s*.*;",
				Pattern.DOTALL);
		Matcher matcher = assignExp
				.matcher("customer_invoice_list= select bsid.system_id as system_id, bsid.mandt as client, ifnull (bsid.vbeln, bsid.belnr) as invoice_number, bsid.belnr as fin_doc_number, bkpf.bukrs as company_code, bsid.gjahr as \"year\", bsid.kunnr as customer, days_between (bkpf.budat, current_date) as days_open, case when bsid.shkzg = 's' then bsid.dmbtr else (-1) * bsid.dmbtr end as outstanding_amount, t001.waers as local_currency, madat as last_dunned, manst as highest_dunning, current_date as reporting_date from \"collections\".\"bkpf\" bkpf inner join \"collections\".\"bsid\" bsid on bsid.system_id = bkpf.system_id and bsid.mandt = bkpf.mandt and bsid.bukrs = bkpf.bukrs and bsid.belnr = bkpf.belnr and bsid.gjahr = bkpf.gjahr inner join collections.cm_specialist_customer_assignment comp on bkpf.bukrs = comp.company and bkpf.system_id = comp.system_id and bkpf.mandt = comp.client and bsid.kunnr = comp.customer inner join \"collections\".\"t001\" t001 on bkpf.system_id = t001.system_id and bkpf.mandt = t001.mandt and bkpf.bukrs = t001.bukrs where --shkzg := 'S' and\n"
						+ " comp.user_name = current_user and comp.system_id = :in_system and comp.client = :in_client and comp.company = :in_company;");
		System.out.println(matcher.matches());
		matcher.reset();

		while (matcher.find()) {
			System.out.println(matcher.group(0));
			// System.out.println(matcher.group(2));
			// System.out.println(matcher.group(1));
			// System.out.println(matcher.group(2));
		}
	}

}