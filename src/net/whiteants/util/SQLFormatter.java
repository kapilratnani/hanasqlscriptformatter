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
					"                                        cust.system_id, cust.client, cust.customer, \n" + 
					"                                        case \n" + 
					"                                            when processed.action_code = '' \n" + 
					"                                            or processed.action_code is null \n" + 
					"                                            then ifnull (remind.status, 'open') \n" + 
					"                                            else 'ok' \n" + 
					"                                        end as status,\n" + 
					"                                        ifnull (txt.days_position_text, '') as action_taken \n" + 
					"                                    from\n" + 
					"                                        :customer_company_list as cust \n" + 
					"                                    left outer join\n" + 
					"                                        :todays_reminders as remind \n" + 
					"                                            on cust.system_id = remind.system_id \n" + 
					"                                            and cust.client = remind.client \n" + 
					"                                            and cust.customer = remind.customer \n" + 
					"                                    left outer join\n" + 
					"                                        :processed_for_the_day as processed \n" + 
					"                                            on cust.system_id = processed.system_id \n" + 
					"                                            and cust.client = processed.client \n" + 
					"                                            and cust.customer = processed.customer \n" + 
					"                                    left outer join\n" + 
					"                                        collections.cm_action_text txt \n" + 
					"                                            on processed.action_code = txt.action_code \n" + 
					"                                            and txt.lang = :in_language" + 
					"", 4, 1).format());
	}
}