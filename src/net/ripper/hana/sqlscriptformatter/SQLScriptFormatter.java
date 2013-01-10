package net.ripper.hana.sqlscriptformatter;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.whiteants.util.DDLFormatter;
import net.whiteants.util.Keywords;
import net.whiteants.util.SQLFormatter;

public class SQLScriptFormatter {

	static String test2 = "create procedure collections.proc_mgr_assign_customers2(\n"
			+ "		in in_system_id varchar(5),\n"
			+ "	 	in in_client varchar(3),\n"
			+ "	 	in in_company varchar(10),\n"
			+ "	 	in in_subordinate varchar(256),\n"
			+ "	 	in in_customer_list varchar(10000),\n"
			+ "	 	out out_messages collections.TT_MESSAGE_TABLE\n"
			+ " 	) sql security invoker as \n"
			+ "\n"
			+ "	str_length integer ;\n"
			+ "	local_csv varchar (10001) ;\n"
			+ "	existing_count integer := 0 ;\n"
			+ "	already_assigned_to varchar (4) := '' ;\n"
			+ "	customer_splist_entry varchar (21) ;\n"
			+ "	specialist varchar (256) := '' ;\n"
			+ "	customer varchar (10) := '' ;\n"
			+ "	is_subordinate integer:=1; \n"
			+ "	\n"
			+ "begin \n"
			+ "		--Check if the subordinate is in managers subordinate list\n"
			+ "	call COLLECTIONS_HIERARCHY.expand_hierarchy_from(current_user,1,htree);\n"
			+ "	\n"
			+ "	sub_list=select\n"
			+ "		 		user_id,sds,sds,sdsd \n"
			+ "			from :htree \n"
			+ "			where hlevel=1;\n"
			+ "	\n"
			+ "		select\n"
			+ "			 count(*) \n"
			+ "		into is_subordinate\n"
			+ "		from :sub_list \n"
			+ "		where user_id=:in_subordinate;\n"
			+ "	 \n"
			+ "		if(:is_subordinate>0) then \n"
			+ "		--Check if the specialist already has an assignemnt from another company code \n"
			+ "	 \n"
			+ "			if (:in_subordinate <> '') then \n"
			+ "				select\n"
			+ "					count(*) \n"
			+ "				into existing_count \n"
			+ "				from collections.cm_specialist_customer_assignment \n"
			+ "				where \n"
			+ "					company <> :in_company and \n"
			+ "					user_name = :in_subordinate;\n"
			+ "	 \n"
			+ "			end if;\n"
			+ "			\n"
			+ "		 --If no such assgnment exists, proceed\n"
			+ "			if (:existing_count = 0) then -- Local variable intialization\n"
			+ "		 		local_csv := :in_customer_list||','; \n"
			+ "		 		str_length := length (:local_csv) ;\n"
			+ "		 		\n"
			+ "		 		-- Start loop and process until no more of the input string is left	\n"
			+ "		 \n"
			+ "				while :str_length > 0 do \n"
			+ "					-- Obtain the token before the first comma as a table 	\n"
			+ "		 			--  select cast(substr_before(:local_csv,',') as varchar(100)) into customer_splist_entry from sys.dummy;\n"
			+ "		 			select\n"
			+ "			 			cast(TRIM (' ' \n"
			+ "					from substr_before(:local_csv,',') ) as varchar (10)) \n"
			+ "						into customer \n"
			+ "					from collections.cm_dummy;\n"
			+ "		 			--	Assign the the string following the comma to :local_csv \n"
			+ "		 			\n"
			+ "		 			select TRIM(' ' \n"
			+ "					from substr_after(:local_csv,',')) \n"
			+ "					into local_csv \n"
			+ "					from collections.cm_dummy ;\n"
			+ "		 			--Delete customer assignment to some other user, if any, in the same level. \n"
			+ "		 			--This will ensure that only one person is assigned a particular customer \n"
			+ "		 			\n"
			+ "		 			delete \n"
			+ "		 			from collections.cm_specialist_customer_assignment \n"
			+ "					where \n"
			+ "						system_id = :in_system_id \n"
			+ "						and client = :in_client \n"
			+ "						and company = :in_company \n"
			+ "						and customer = :customer \n"
			+ "						and user_name in (select user_id from :sub_list);\n"
			+ "						\n"
			+ "		 			--Insert the new assignment \n"
			+ "		 			--(Empty specialist name means Unassign, Non-empty Specialist may mean fresh assignment or re-assignment) \n"
			+ "		 			\n"
			+ "		 			insert into collections.cm_specialist_customer_assignment \n"
			+ "		 			values (:in_system_id,\n"
			+ "			 				:in_client,\n"
			+ "			 				:in_company,\n"
			+ "			 				:customer,\n"
			+ "			 				:in_subordinate,\n"
			+ "							'');\n"
			+ "							\n"
			+ "		 			--Compute string length		\n"
			+ "		 			str_length := length (:local_csv);\n"
			+ "		 \n"
			+ "				end while;\n"
			+ "		 \n"
			+ "			else if (:existing_count = 0) then c:=c+1; end if;"
			+ "				select \n"
			+ "					top 1 distinct company \n"
			+ "				into already_assigned_to \n"
			+ "				from collections.cm_specialist_customer_assignment \n"
			+ "				where \n"
			+ "					user_name = :in_subordinate;\n"
			+ "					\n"
			+ "		 		out_messages =	select \n"
			+ "		 							'CM_MGR_100' as message_code,\n"
			+ "			 						:in_subordinate || ' has already been assigned customers from company code ' \n"
			+ "			 									|| :already_assigned_to || '. Cannot assign customers from ' || \n"
			+ "			 									:in_company || '.' as message_text \n"
			+ "								from collections.cm_dummy;\n"
			+ "		 \n"
			+ "		end if;\n"
			+ "	else\n"
			+ "	 	out_messages = 	select\n"
			+ "		 					'CM_MGR_101' as message_code,\n"
			+ "		 					:in_subordinate || ' is not subordinate to ' ||current_user|| '.' as message_text \n"
			+ "						from collections.cm_dummy;\n"
			+ "	 \n"
			+ "	end if; \n"
			+ "\n" + "end;\n" + "";
	static String testSql = "create procedure collections.proc_mgr_get_assignment_overview2 ( in in_system varchar (5),\n"
			+ "	 in in_client varchar (3),\n"
			+ "	 in in_company varchar (4),\n"
			+ "	 in in_date timestamp,\n"
			+ "	 out out_manager_overview collections.tt_manager_assignment_overview ) sql security invoker as \n"
			+ "\n"
			+ "assigned_customers integer := 0 \n"
			+ ";\n"
			+ "unassigned_customers integer := 0 \n"
			+ "; \n"
			+ "assistance_list integer := 0 \n"
			+ ";\n"
			+ "total_outstanding double := 0 \n"
			+ ";\n"
			+ "promise_to_pay double := 0 \n"
			+ ";\n"
			+ "local_currency varchar (5) := '' \n"
			+ ";\n"
			+ "user_full_name varchar (256) \n"
			+ ";\n"
			+ "count_var integer := 0 \n"
			+ "; \n"
			+ " \n"
			+ "begin \n"
			+ "/*\n"
			+ "	Modified to show only asssigned customers\n"
			+ "*/\n"
			+ "customers_under_manager = select\n"
			+ "	 customer \n"
			+ "from collections.cm_specialist_customer_assignment \n"
			+ "where system_id = :in_system \n"
			+ "and client = :in_client \n"
			+ "and company = :in_company \n"
			+ "and user_name=current_user \n"
			+ ";\n"
			+ "\n"
			+ "/*\n"
			+ "	get manager's subordinates\n"
			+ "*/\n"
			+ "call collections_hierarchy.expand_hierarchy_from(current_user,1,htree);\n"
			+ "sub_list=select user_id from :htree where hlevel=1;\n"
			+ "\n"
			+ "customers_assigned_by_manager = select\n"
			+ "	 customer \n"
			+ "from collections.cm_specialist_customer_assignment, :sub_list as subs \n"
			+ "where system_id = :in_system \n"
			+ "and client = :in_client \n"
			+ "and company = :in_company \n"
			+ "and user_name=subs.user_id;\n"
			+ "\n"
			+ "/*\n"
			+ " select\n"
			+ "	 count (distinct customer) \n"
			+ "into unassigned_customers \n"
			+ "from ( select\n"
			+ "	 customer \n"
			+ "	from :customers_under_manager minus select\n"
			+ "	 customer \n"
			+ "	from :customers_assigned_by_manager ) \n"
			+ ";\n"
			+ "\n"
			+ " select\n"
			+ "	 count (distinct customer) \n"
			+ "into assigned_customers \n"
			+ "from :customers_assigned_by_manager \n"
			+ "where customer is not null \n"
			+ ";\n"
			+ "*/\n"
			+ " assigned_invoices_with_outcomes = select\n"
			+ "	 imap.system_id,\n"
			+ "	 imap.client,\n"
			+ "	 imap.customer,\n"
			+ "	 imap.invoice,\n"
			+ "	 imap.invoice_year,\n"
			+ "	 outcome.outcome_guid,\n"
			+ "	 outcome.created_at \n"
			+ "from collections.cm_outcome_invoice_mapping as imap \n"
			+ "inner join :customers_assigned_by_manager assign on imap.customer = assign.customer \n"
			+ "inner join collections.cm_call_outcomes as outcome on imap.outcome_guid = outcome.outcome_guid \n"
			+ "where imap.system_id = :in_system \n"
			+ "and imap.client = :in_client \n"
			+ "and outcome.action_code <> '1' \n"
			+ ";\n"
			+ "\n"
			+ "last_outcome_created_at = select\n"
			+ "	 system_id,\n"
			+ "	 client,\n"
			+ "	 customer,\n"
			+ "	 invoice,\n"
			+ "	 invoice_year,\n"
			+ "	 max (created_at) as created_at \n"
			+ "from :assigned_invoices_with_outcomes \n"
			+ "group by system_id,\n"
			+ "	 client,\n"
			+ "	 customer,\n"
			+ "	 invoice,\n"
			+ "	 invoice_year \n"
			+ ";\n"
			+ " select\n"
			+ "	 count (distinct outcome.customer) \n"
			+ "into assistance_list \n"
			+ "from collections.cm_call_outcomes as outcome \n"
			+ "inner join :last_outcome_created_at as last_outcome on outcome.system_id = last_outcome.system_id \n"
			+ "and outcome.client = last_outcome.client \n"
			+ "and outcome.customer = last_outcome.customer \n"
			+ "where outcome.action_code <> '1' \n"
			+ ";\n"
			+ " customer_invoice_list= select\n"
			+ "	 bsid.system_id as system_id,\n"
			+ "	 bsid.mandt as client,\n"
			+ "	 ifnull (bsid.vbeln,\n"
			+ "	 bsid.belnr) as invoice_number,\n"
			+ "	 bsid.belnr as fin_doc_number,\n"
			+ "	 bkpf.bukrs as company_code,\n"
			+ "	 bsid.gjahr as \"YEAR\",\n"
			+ "	 bsid.kunnr as customer,\n"
			+ "	 days_between (bkpf.budat,\n"
			+ "	current_date) as days_open,\n"
			+ "	 CASE WHEN BSID.shkzg = 'S' \n"
			+ "THEN bsid.dmbtr \n"
			+ "ELSE (-1) * bsid.dmbtr \n"
			+ "END as outstanding_amount,\n"
			+ "	 t001.waers as local_currency,\n"
			+ "	 madat as last_dunned,\n"
			+ "	 manst as highest_dunning,\n"
			+ "	 current_date as reporting_date \n"
			+ "FROM \"COLLECTIONS\".\"BKPF\" bkpf \n"
			+ "INNER JOIN \"COLLECTIONS\".\"BSID\" bsid ON bsid.system_id = bkpf.system_id \n"
			+ "AND bsid.mandt = bkpf.mandt \n"
			+ "AND bsid.bukrs = bkpf.bukrs \n"
			+ "AND bsid.belnr = bkpf.belnr \n"
			+ "AND bsid.gjahr = bkpf.gjahr \n"
			+ "inner join COLLECTIONS.CM_SPECIALIST_CUSTOMER_ASSIGNMENT comp on bkpf.bukrs = comp.company \n"
			+ "and bkpf.system_id = comp.system_id \n"
			+ "AND bkpf.mandt = comp.client \n"
			+ "and bsid.kunnr = comp.customer \n"
			+ "INNER JOIN \"COLLECTIONS\".\"T001\" t001 on bkpf.system_id = t001.system_id \n"
			+ "AND bkpf.mandt = t001.mandt \n"
			+ "AND bkpf.bukrs = t001.bukrs \n"
			+ "WHERE --shkzg = 'S' and\n"
			+ " comp.user_name = current_user \n"
			+ "and comp.system_id = :in_system \n"
			+ "and comp.client = :in_client \n"
			+ "and comp.company = :in_company \n"
			+ "; 	\n"
			+ " customer_outstanding = select\n"
			+ "	 system_id,\n"
			+ "	 client,\n"
			+ "	 customer,\n"
			+ "	 sum(outstanding_amount) \n"
			+ "from :customer_invoice_list \n"
			+ "group by system_id,\n"
			+ "	 client,\n"
			+ "	 customer having sum(outstanding_amount) > 0 \n"
			+ ";\n"
			+ " select\n"
			+ "	 count(distinct customer) \n"
			+ "into assigned_customers \n"
			+ "from ( select\n"
			+ "	 distinct customer \n"
			+ "	from :customer_outstanding intersect ( select\n"
			+ "	 customer \n"
			+ "		from :customers_assigned_by_manager intersect select\n"
			+ "	 distinct customer \n"
			+ "		from :customers_under_manager ) )\n"
			+ ";\n"
			+ " select\n"
			+ "	 count(distinct customer) \n"
			+ "into unassigned_customers \n"
			+ "from ( select\n"
			+ "	 distinct customer \n"
			+ "	from :customer_outstanding intersect ( select\n"
			+ "	 customer \n"
			+ "		from :customers_under_manager minus select\n"
			+ "	 customer \n"
			+ "		from :customers_assigned_by_manager ) )\n"
			+ ";\n"
			+ "/*\n"
			+ " select\n"
			+ "	 customer \n"
			+ "from :customers_under_manager minus select\n"
			+ "	 customer \n"
			+ "from :customers_assigned_by_manager\n"
			+ ";\n"
			+ "*/\n"
			+ " select\n"
			+ "	 ifnull (sum (outstanding_amount),\n"
			+ "	0 ) \n"
			+ "into total_outstanding \n"
			+ "from :customer_invoice_list \n"
			+ ";\n"
			+ " /* select\n"
			+ "	 ifnull (sum (outcome.promise_to_pay),\n"
			+ "	0 ) \n"
			+ "into promise_to_pay \n"
			+ "from collections.cm_call_outcomes as outcome \n"
			+ "inner join :customers_assigned_by_manager assign on outcome.customer = assign.customer \n"
			+ "where outcome.system_id = :in_system \n"
			+ "and outcome.client = :in_client \n"
			+ "and by_when >= to_date (:in_date) \n"
			+ ";\n"
			+ " */ \n"
			+ " specialists_customers_and_invoices = select\n"
			+ "	 system_id,\n"
			+ "	 client,\n"
			+ "	 customer,\n"
			+ "	 fin_doc_number,\n"
			+ "	 \"YEAR\" \n"
			+ "from :customer_invoice_list \n"
			+ "group by system_id,\n"
			+ "	 client,\n"
			+ "	 customer,\n"
			+ "	 fin_doc_number,\n"
			+ "	 \"YEAR\" \n"
			+ ";\n"
			+ " promise_to_pays = select\n"
			+ "	 inv.system_id,\n"
			+ "	 inv.client,\n"
			+ "	 outcome.promise_to_pay \n"
			+ "from :specialists_customers_and_invoices as inv \n"
			+ "inner join collections.cm_outcome_invoice_mapping as imap on inv.system_id = imap.system_id \n"
			+ "and inv.client = imap.client \n"
			+ "and inv.customer = imap.customer \n"
			+ "and inv.fin_doc_number = imap.invoice \n"
			+ "and inv.\"YEAR\" = imap.invoice_year \n"
			+ "inner join collections.cm_call_outcomes as outcome on imap.outcome_guid = outcome.outcome_guid \n"
			+ "where by_when >= to_date (:in_date) \n"
			+ ";\n"
			+ " select\n"
			+ "	 ifnull (sum (promise_to_pay),\n"
			+ "	0 ) \n"
			+ "into promise_to_pay \n"
			+ "from :promise_to_pays \n"
			+ ";\n"
			+ " --select count(*) into promise_to_pay  from :customer_invoice_list;\n"
			+ " select\n"
			+ "	 top 1 waers \n"
			+ "into local_currency \n"
			+ "from collections.t001 \n"
			+ "where system_id = :in_system \n"
			+ "and mandt = :in_client \n"
			+ "and bukrs = :in_company \n"
			+ ";\n"
			+ " select\n"
			+ "	 count (*) \n"
			+ "into count_var \n"
			+ "from collections.cm_user \n"
			+ "where username = current_user \n"
			+ ";\n"
			+ " \n"
			+ "if (count_var > 0 ) \n"
			+ "then select\n"
			+ "	 first_name||' '|| last_name \n"
			+ "into user_full_name \n"
			+ "from collections.cm_user \n"
			+ "where username = current_user \n"
			+ ";\n"
			+ " \n"
			+ "else if a==b then user_full_name := current_user \n"
			+ ";\n"
			+ " \n"
			+ "end \n"
			+ "if \n"
			+ "; \n"
			+ " out_manager_overview = select\n"
			+ "	 :in_system as system_id,\n"
			+ "	 :in_client as client,\n"
			+ "	 :in_company as company,\n"
			+ "	 :unassigned_customers as unassigned_customers,\n"
			+ "	 :assigned_customers as assigned_customers,\n"
			+ "	 :assistance_list as assist_count,\n"
			+ "	 :total_outstanding as due,\n"
			+ "	 :promise_to_pay as collected,\n"
			+ "	 :local_currency as local_currency,\n"
			+ "	 :user_full_name as user_name \n"
			+ "from collections.cm_dummy \n" + ";		\n" + " \n" + "end;\n";

	private static Keywords keywords = new Keywords();

	private static Pattern assignExp = Pattern.compile(
			"(.*)\\s*:=\\s*([^;]*);", Pattern.DOTALL);
	private static Pattern selectAssignExp = Pattern.compile(
			"(.*)\\s*=\\s*select\\s*.*;", Pattern.DOTALL);
	private static Pattern queryOrDmlExp = Pattern.compile(
			"(select|delete|update|insert)\\s*[^;]*;", Pattern.DOTALL);
	private static Pattern genericAssignExp = Pattern.compile(
			"(.*)\\s*=\\s*([^;]*);", Pattern.DOTALL);
	private static DDLFormatter ddlFormatter = new DDLFormatter();
	private StringBuilder result = new StringBuilder();
	private String cString = "";
	private int indent = 1;

	/**
	 * @param args
	 */
	public String format(String sqlScript) {
		StringTokenizer tokens = new StringTokenizer(sqlScript,
				"()+*//*-=<>'`\"[],;" + keywords.getWhitespace(), true);

		boolean afterCreate = false;
		boolean afterProcedure = false;
		boolean afterBegin = false;
		boolean afterQueryOrDml = false;
		boolean afterEnd = false;

		String token, lcToken, lastToken = "";
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
			lcToken = token.toLowerCase().replaceAll("[\\r\\n\\t]", " ");

			if (cString.trim().startsWith("/*")
					|| cString.trim().startsWith("--")
					|| cString.trim().endsWith("--")) {
				cString = cString.trim();
				// handle comments
				boolean dump = true;
				boolean multiline = false;
				// comments inside a query should not be dumped
				// include it with the query and send it to sql formatter
				if (cString.endsWith("--") && afterQueryOrDml)
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
			} else if ("create".equals(lcToken)) {
				afterCreate = true;
				cString += lcToken;
			} else if (afterCreate && "procedure".equals(lcToken)) {
				// format create procedure section
				afterProcedure = true;
				while (tokens.hasMoreTokens() && !"as".equals(lcToken)) {

					if (!lastToken.equals(" ") || !lastToken.equals(lcToken)) {
						cString += lcToken;
					}

					lastToken = lcToken;
					lcToken = tokens.nextToken().toLowerCase()
							.replaceAll("[\\r\\n\\t]", " ");
				}
				cString += lcToken;
				cString = ddlFormatter.format(cString);
				out();
				newline();
				cString = "";
			} else if ("begin".equals(lcToken)) {

				afterProcedure = false;
				afterCreate = false;
				indent--;
				cString = lcToken;
				out();
				newline();
				indent++;
				afterBegin = true;
				cString="";
			} else if ("end".equals(lcToken) && !afterQueryOrDml) {
				afterEnd = true;
				cString += lcToken;
			} else if (keywords.getDml().contains(lcToken)
					|| "select".equals(lcToken)) {
				afterQueryOrDml = true;
				cString += lcToken;
			} else if ("else".equals(lcToken) && !afterQueryOrDml) {
				indent--;
				indent();
				cString = lcToken;
				out();
				newline();
				cString = "";
				indent++;
				// afterElse = true;
			} else if (cString.endsWith("end if;")
					|| cString.endsWith("end if ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (cString.endsWith("end while;")
					|| cString.endsWith("end while ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (cString.endsWith("end for;")
					|| cString.endsWith("end for ;")) {
				indent--;
				indent();
				out();
				newline();
				newline();
				cString = "";
				afterEnd = false;
			} else if (cString.endsWith("end;") || cString.endsWith("end ;")) {
				indent--;
				indent();
				out();
				newline();
				cString = "";
				afterEnd = false;
			} else if ("break;".equals(cString.trim())
					|| "continue;".equals(cString.trim())) {
				indent();
				out();
				newline();
				cString = "";
			} else if ("while".equals(lcToken) && !afterEnd) {
				cString = "";
				while (tokens.hasMoreTokens() && !"do".equals(token)) {
					cString += token;
					token = tokens.nextToken().toLowerCase();
				}
				cString += token;
				indent();
				out();
				newline();
				indent++;
				cString = "";
			} else if ("for".equals(lcToken) && !afterEnd) {
				cString = "";
				while (tokens.hasMoreTokens() && !"do".equals(token)) {
					cString += token;
					token = tokens.nextToken().toLowerCase();
				}
				cString += token;
				indent();
				out();
				newline();
				indent++;
				cString = "";
			} else if (("if".equals(lcToken.trim()) || "elseif".equals(lcToken
					.trim())) && !afterEnd) {
				cString = "";
				while (tokens.hasMoreTokens() && !"then".equals(token)) {
					cString += token.replaceAll("[\\r\\n\\t]", " ");
					token = tokens.nextToken().toLowerCase();
				}
				cString += token;
				indent();
				indent++;

				out();
				newline();

				cString = "";
			} else if (afterBegin && "call".equals(lcToken)) {
				cString = token;
				lastToken = "";
				while (tokens.hasMoreTokens() && !";".equals(token)) {
					token = tokens.nextToken().replaceAll("[\r\n\t]", " ");
					if (!lastToken.equals(" ") || !lastToken.equals(token))
						cString += token;
				}

				indent();

				out();
				newline();
				newline();
				cString = "";
			} else if (afterCreate && afterProcedure && !" ".equals(lcToken)) {
				while (tokens.hasMoreTokens() && !";".equals(lcToken)) {
					cString += lcToken;
					lcToken = tokens.nextToken().toLowerCase()
							.replaceAll("[\\r\\n\\t]", " ");
				}
				cString += lcToken;
				indent();
				out();
				newline();
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

				// calculate indent here
				int initIndent = (int) (indent + Math.ceil(cString
						.indexOf("select") / 4));
				cString = new SQLFormatter(cString, 4 * indent, initIndent + 1)
						.format();
				out();
				newline();
				newline();
				cString = "";
				afterQueryOrDml = false;
			} else if (afterBegin
					&& queryOrDmlExp.matcher(cString.trim()).matches()) {
				cString = new SQLFormatter(cString, 4 * indent, indent + 1)
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
				indent();
				out();
				newline();
				newline();
				cString = "";
			} else {
				// ignore repeated whitespace characters
				if (!keywords.getWhitespace().contains(lastToken)
						|| !keywords.getWhitespace().contains(lcToken))
					cString += lcToken;
			}
			lastToken = lcToken;
		}
		if (cString.trim().length() > 0) {
			indent();
			out();
			newline();
		}
		return result.toString();
	}

	private void out() {
		result.append(cString);
	}

	private void indent() {
		for (int i = 0; i < indent; i++) {
			result.append("    ");
		}
	}

	private void newline() {
		result.append("\n");
	}

	public static void main(String[] args) {
		System.out.println(new SQLScriptFormatter().format(test2));
	}

}
