package net.ripper.hana.sqlscriptformatter;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.whiteants.util.DDLFormatter;
import net.whiteants.util.Keywords;
import net.whiteants.util.SQLFormatter;

public class SQLScriptFormatter {

	static String test6="create procedure collections.proc_mgr_assign_customers2(\n" + 
			"		in in_system_id varchar(5),\n" + 
			"	 	in in_client varchar(3),\n" + 
			"	 	in in_company varchar(10),\n" + 
			"	 	in in_subordinate varchar(256),\n" + 
			"	 	in in_customer_list varchar(10000),\n" + 
			"	 	out out_messages collections.TT_MESSAGE_TABLE\n" + 
			" 	) sql security invoker as \n" + 
			"\n" + 
			"	str_length integer ;\n" + 
			"	local_csv varchar (10001) ;\n" + 
			"	existing_count integer := 0 ;\n" + 
			"	already_assigned_to varchar (4) := '' ;\n" + 
			"	customer_splist_entry varchar (21) ;\n" + 
			"	specialist varchar (256) := '' ;\n" + 
			"	customer varchar (10) := '' ;\n" + 
			"	is_subordinate integer:=1; \n" + 
			"	\n" + 
			"begin \n" + 
			"		--Check if the subordinate is in managers subordinate list\n" + 
			"	call COLLECTIONS_HIERARCHY.expand_hierarchy_from(current_user,1,htree);\n" + 
			"	\n" + 
			"	sub_list=select\n" + 
			"		 		user_id \n" + 
			"			from :htree \n" + 
			"			where hlevel=1;\n" + 
			"	\n" + 
			"		select\n" + 
			"			 count(*) \n" + 
			"		into is_subordinate\n" + 
			"		from :sub_list \n" + 
			"		where user_id=:in_subordinate;\n" + 
			"	 \n" + 
			"		if(:is_subordinate>0) then \n" + 
			"		--Check if the specialist already has an assignemnt from another company code \n" + 
			"	 \n" + 
			"			if (:in_subordinate <> '') then \n" + 
			"				select\n" + 
			"					count(*) \n" + 
			"				into existing_count \n" + 
			"				from collections.cm_specialist_customer_assignment \n" + 
			"				where \n" + 
			"					company <> :in_company and \n" + 
			"					user_name = :in_subordinate;\n" + 
			"	 \n" + 
			"			end if;\n" + 
			"			\n" + 
			"		 --If no such assgnment exists, proceed\n" + 
			"			if (:existing_count = 0) then -- Local variable intialization\n" + 
			"		 		local_csv := :in_customer_list||','; \n" + 
			"		 		str_length := length (:local_csv) ;\n" + 
			"		 		\n" + 
			"		 		-- Start loop and process until no more of the input string is left	\n" + 
			"		 \n" + 
			"				while :str_length > 0 do \n" + 
			"					-- Obtain the token before the first comma as a table 	\n" + 
			"		 			--  select cast(substr_before(:local_csv,',') as varchar(100)) into customer_splist_entry from sys.dummy;\n" + 
			"		 			select\n" + 
			"			 			cast(TRIM (' ' \n" + 
			"					from substr_before(:local_csv,',') ) as varchar (10)) \n" + 
			"						into customer \n" + 
			"					from collections.cm_dummy;\n" + 
			"		 			--	Assign the the string following the comma to :local_csv \n" + 
			"		 			\n" + 
			"		 			select TRIM(' ' \n" + 
			"					from substr_after(:local_csv,',')) \n" + 
			"					into local_csv \n" + 
			"					from collections.cm_dummy ;\n" + 
			"		 			--Delete customer assignment to some other user, if any, in the same level. \n" + 
			"		 			--This will ensure that only one person is assigned a particular customer \n" + 
			"		 			\n" + 
			"		 			delete \n" + 
			"		 			from collections.cm_specialist_customer_assignment \n" + 
			"					where \n" + 
			"						system_id = :in_system_id \n" + 
			"						and client = :in_client \n" + 
			"						and company = :in_company \n" + 
			"						and customer = :customer \n" + 
			"						and user_name in (select user_id from :sub_list);\n" + 
			"						\n" + 
			"		 			--Insert the new assignment \n" + 
			"		 			--(Empty specialist name means Unassign, Non-empty Specialist may mean fresh assignment or re-assignment) \n" + 
			"		 			\n" + 
			"		 			insert into collections.cm_specialist_customer_assignment \n" + 
			"		 			values (:in_system_id,\n" + 
			"			 				:in_client,\n" + 
			"			 				:in_company,\n" + 
			"			 				:customer,\n" + 
			"			 				:in_subordinate,\n" + 
			"							'');\n" + 
			"							\n" + 
			"		 			--Compute string length		\n" + 
			"		 			str_length := length (:local_csv);\n" + 
			"		 \n" + 
			"				end while;\n" + 
			"		 \n" + 
			"			else \n" + 
			"				select \n" + 
			"					top 1 distinct company \n" + 
			"				into already_assigned_to \n" + 
			"				from collections.cm_specialist_customer_assignment \n" + 
			"				where \n" + 
			"					user_name = :in_subordinate;\n" + 
			"					\n" + 
			"		 		out_messages =	select \n" + 
			"		 							'CM_MGR_100' as message_code,\n" + 
			"			 						:in_subordinate || ' has already been assigned customers from company code ' \n" + 
			"			 									|| :already_assigned_to || '. Cannot assign customers from ' || \n" + 
			"			 									:in_company || '.' as message_text \n" + 
			"								from collections.cm_dummy;\n" + 
			"		 \n" + 
			"		end if;\n" + 
			"	else\n" + 
			"	 	out_messages = 	select\n" + 
			"		 					'CM_MGR_101' as message_code,\n" + 
			"		 					:in_subordinate || ' is not subordinate to ' ||current_user|| '.' as message_text \n" + 
			"						from collections.cm_dummy;\n" + 
			"	 \n" + 
			"	end if; \n" + 
			"\n" + 
			"end;\n" + 
			"";
	static String test5 = "create procedure collections.proc_GET_USER_ROLES \n"
			+ "	(in grantee NVARCHAR (256), OUT USER_ROLE_DATA collections.tt_ROLE\n"
			+ "	) LANGUAGE SQLSCRIPT READS SQL DATA WITH RESULT VIEW collections.USER_ROLE_VIEW AS \n"
			+ "BEGIN \n"
			+ "USER_ROLE_DATA = select\n"
			+ "	 R.NAME AS ROLE_NAME\n"
			+ "FROM SYS.\"P_PRINCIPALS_\" P, SYS.P_PRINCIPALS_ R, SYS.P_PRINCIPALS_ G, SYS.P_ASSIGNEDROLES_ AR \n"
			+ "WHERE P.OID = AR.GRANTEEID \n" + "AND G.OID = AR.GRANTERID \n"
			+ "AND AR.ROLEID = R.OID \n" + "AND P.NAME = :grantee\n" + ";\n"
			+ "END;";
	static String test4 = "create procedure collections.proc_util_split_csv\n"
			+ "(\n"
			+ "      in in_csv_string varchar(2000),\n"
			+ "      out out_values_tab collections.tt_string_index_tab\n"
			+ ")\n"
			+ "as\n"
			+ "str_length integer;\n"
			+ "local_csv varchar(2001);\n"
			+ "idx integer;\n"
			+ "begin\n"
			+ "--  Local variable intialization\n"
			+ "      local_csv := :in_csv_string||','; \n"
			+ "      str_length := length(:local_csv);\n"
			+ "      idx := 1;   \n"
			+ "      \n"
			+ "      string_tab = select top 0 0 as row_index, cast('' as varchar(100)) as string_value from sys.dummy;\n"
			+ "      \n"
			+ "--  Start loop and process until no more string is left     \n"
			+ "      while :str_length > 0 do\n"
			+ "      \n"
			+ "--          Obtain the token before the first comma as a table    \n"
			+ "            single_value = select idx as row_index, cast(substr_before(:local_csv,',') as varchar(100)) as string_value from sys.dummy;\n"
			+ "            \n"
			+ "--          Assign the the string following the comma to :local_csv \n"
			+ "            select TRIM(' ' from substr_after(:local_csv,',')) into local_csv from sys.dummy;\n"
			+ "            \n"
			+ "--      Accumulate entries in the table         \n"
			+ "            string_tab = select * from :string_tab union all select * from :single_value;\n"
			+ "            \n"
			+ "--          Compute string length         \n"
			+ "            str_length := length(:local_csv);\n"
			+ "            idx := :idx + 1;\n"
			+ "      end while;\n"
			+ "      \n"
			+ "--    return values in table form, so that consumers can loop over table to fetch the results     \n"
			+ "      out_values_tab = select * from :string_tab where length(string_value) > 0;\n"
			+ "end; ";
	static String test3 = "create procedure collections_hierarchy.add_child(\n"
			+ "    in parent_id varchar(30),\n"
			+ "    in child_id varchar(30) \n"
			+ ") language sqlscript as\n"
			+ "     username varchar(100);\n"
			+ "     role varchar(30);\n"
			+ "begin\n"
			+ "/*This will throw no data found exception if users don't exist*/\n"
			+ "/*check if child id exists*/\n"
			+ "    select\n"
			+ "        username \n"
			+ "    into\n"
			+ "        username \n"
			+ "    from\n"
			+ "        collections.cm_user \n"
			+ "    where\n"
			+ "        username  =  :child_id;\n"
			+ "/*check if parent id exists*/\n"
			+ "    select\n"
			+ "        username \n"
			+ "    into\n"
			+ "        username \n"
			+ "    from\n"
			+ "        collections.cm_user \n"
			+ "    where\n"
			+ "        username  =  :parent_id;\n"
			+ "/*check if parent is a manager*/\n"
			+ "    select\n"
			+ "        role_name \n"
			+ "    into\n"
			+ "        role \n"
			+ "    from\n"
			+ "        \"sys\".\"granted_roles\"\n"
			+ "    where\n"
			+ "        grantee  =  :parent_id \n"
			+ "        and role_name  =  'cm_collman';\n"
			+ "        \n"
			+ "    insert \n"
			+ "    into\n"
			+ "        collections_hierarchy.hierarchy\n"
			+ "        select\n"
			+ "           ancestor,  :child_id as descendent,  depth+1 as depth \n"
			+ "        from\n"
			+ "            collections_hierarchy.hierarchy \n"
			+ "        where\n" + "            descendent  =  :parent_id \n"
			+ "        union\n" + "        select\n"
			+ "            :child_id,  :child_id,  0 \n" + "        from\n"
			+ "            dummy;\n" + "end;";
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
			+ "\n" + "end;";
	static String test1 = "drop procedure collections.proc_mgr_get_all_customers_summary2;\n"
			+ "create procedure collections.proc_mgr_get_all_customers_summary2 (\n"
			+ "    in in_system_id varchar (5),\n"
			+ "    in in_client varchar (3),\n"
			+ "    in in_company varchar (4),\n"
			+ "    in in_language varchar (1),\n"
			+ "    out out_customer_summary_list collections.tt_mgr_customer_summary \n"
			+ ") sql security invoker as\n"
			+ "begin\n"
			+ "/*\n"
			+ "	Modified to only show customers which are assigned to the manager\n"
			+ "*/\n"
			+ "    call collections.proc_customer_contact_address_data_with_client_system (:in_language,  :in_system_id,  :in_client,  kna) ;\n"
			+ "\n"
			+ "    address  =  select\n"
			+ "                    kna.system_id,  kna.client,  kna.customer,  kna.\"customer_name\",  kna.\"telephone_number\",  kna.\"city\",  kna.\"region\",  kna.\"country\" \n"
			+ "                from\n"
			+ "                    :kna as kna ;\n"
			+ "                    \n"
			+ "\n"
			+ "    call collections_hierarchy.expand_hierarchy_from(current_user,1,h_tree);\n"
			+ "\n"
			+ "    sub_list  =  select\n"
			+ "                        user_id \n"
			+ "                    from\n"
			+ "                        :h_tree \n"
			+ "                    where\n"
			+ "                        hlevel  =  1;\n"
			+ "\n"
			+ "/*\n"
			+ "	get all assignments to manager,\n"
			+ "	as well as to whom the manager has assigned\n"
			+ "*/\n"
			+ "/*\n"
			+ "assignment_list = select\n"
			+ "	 mgr_asmnt.system_id,\n"
			+ "	 mgr_asmnt.client,\n"
			+ "	 mgr_asmnt.company,\n"
			+ "	 mgr_asmnt.customer,\n"
			+ "	 mgr_asmnt.user_name as manager_name,\n"
			+ "	 spl_asmnt.user_name as specialist_name \n"
			+ "from COLLECTIONS.CM_SPECIALIST_CUSTOMER_ASSIGNMENT as mgr_asmnt\n"
			+ "left outer join COLLECTIONS.CM_SPECIALIST_CUSTOMER_ASSIGNMENT as spl_asmnt on\n"
			+ "				mgr_asmnt.system_id=spl_asmnt.system_id and\n"
			+ "				mgr_asmnt.client=spl_asmnt.client and\n"
			+ "				mgr_asmnt.company=spl_asmnt.company and\n"
			+ "				mgr_asmnt.customer=spl_asmnt.customer and\n"
			+ "				mgr_asmnt.user_name<>spl_asmnt.user_name\n"
			+ "where mgr_asmnt.system_id=:in_system_id and mgr_asmnt.client=:in_client and mgr_asmnt.user_name=current_user;							 \n"
			+ "*/\n"
			+ "    spl_asmnt  =  select\n"
			+ "                        system_id,  client,  company,  customer,  user_name \n"
			+ "                    from\n"
			+ "                        collections.cm_specialist_customer_assignment as cm_asmnt,  :sub_list as subs \n"
			+ "                    where\n"
			+ "                        user_name  =  subs.user_id \n"
			+ "                        and system_id  =  :in_system_id \n"
			+ "                        and client  =  :in_client;\n"
			+ "\n"
			+ "    assignment_list  =  select\n"
			+ "                            mgr_asmnt.system_id,  mgr_asmnt.client,  mgr_asmnt.company,  mgr_asmnt.customer,  mgr_asmnt.user_name as manager_name,\n"
			+ "                            spl_asmnt.user_name as specialist_name \n"
			+ "                        from\n"
			+ "                            collections.cm_specialist_customer_assignment as mgr_asmnt \n"
			+ "                        left outer join\n"
			+ "                            :spl_asmnt as spl_asmnt \n"
			+ "                                on mgr_asmnt.system_id  =  spl_asmnt.system_id \n"
			+ "                                and mgr_asmnt.client  =  spl_asmnt.client \n"
			+ "                                and mgr_asmnt.company  =  spl_asmnt.company \n"
			+ "                                and mgr_asmnt.customer  =  spl_asmnt.customer \n"
			+ "                                and mgr_asmnt.user_name<>spl_asmnt.user_name \n"
			+ "                        where\n"
			+ "                            mgr_asmnt.system_id  =  :in_system_id \n"
			+ "                            and mgr_asmnt.client  =  :in_client \n"
			+ "                            and mgr_asmnt.user_name  =  current_user;\n"
			+ "\n"
			+ "    --Build the list of customers and invoices\n"
			+ "\n"
			+ "    --Source of customers is the assignment table \n"
			+ "\n"
			+ "    customer_invoice_list  =  select\n"
			+ "                                    bsid.system_id as system_id,  bsid.mandt as client,  ifnull (bsid.vbeln, bsid.belnr) as invoice_number,  bsid.belnr as fin_doc_number,  bkpf.bukrs as company_code,  bsid.gjahr as \"year\",  bsid.kunnr as customer,  days_between (bkpf.budat, current_date) as days_open,\n"
			+ "                                    case \n"
			+ "                                        when bsid.shkzg  =  's' \n"
			+ "                                        then bsid.dmbtr \n"
			+ "                                        else (-1) * bsid.dmbtr \n"
			+ "                                    end as outstanding_amount,  t001.waers as local_currency,  madat as last_dunned,  manst as highest_dunning,  current_date as reporting_date,  comp.specialist_name as specialist_name,\n"
			+ "                                    bsid.shkzg \n"
			+ "                                from\n"
			+ "                                    \"collections\".\"bkpf\"bkpf \n"
			+ "                                inner join\n"
			+ "                                    \"collections\".\"bsid\"bsid \n"
			+ "                                        on bsid.system_id  =  bkpf.system_id \n"
			+ "                                        and bsid.mandt  =  bkpf.mandt \n"
			+ "                                        and bsid.bukrs  =  bkpf.bukrs \n"
			+ "                                        and bsid.belnr  =  bkpf.belnr \n"
			+ "                                        and bsid.gjahr  =  bkpf.gjahr \n"
			+ "                                inner join\n"
			+ "                                    :assignment_list comp \n"
			+ "                                        on bkpf.bukrs  =  comp.company \n"
			+ "                                        and bkpf.system_id  =  comp.system_id \n"
			+ "                                        and bkpf.mandt  =  comp.client \n"
			+ "                                        and bsid.kunnr  =  comp.customer \n"
			+ "                                inner join\n"
			+ "                                    \"collections\".\"t001\"t001 \n"
			+ "                                        on bkpf.system_id  =  t001.system_id \n"
			+ "                                        and bkpf.mandt  =  t001.mandt \n"
			+ "                                        and bkpf.bukrs  =  t001.bukrs \n"
			+ "                                where\n"
			+ "                                    bsid.system_id  =  :in_system_id \n"
			+ "                                    and bsid.mandt  =  :in_client \n"
			+ "                                    and bsid.bukrs  =  :in_company ;\n"
			+ "                                    \n"
			+ "\n"
			+ "    --	Get the list of distinct Customer and Company Code, needed for Credit Information\n"
			+ "\n"
			+ "    customer_company_list  =  select\n"
			+ "                                    distinct system_id,  client,  customer,  company_code,  local_currency \n"
			+ "                                from\n"
			+ "                                    :customer_invoice_list ;\n"
			+ "                                    \n"
			+ "\n"
			+ "    customer_company_count  =  select\n"
			+ "                                    distinct system_id ,  client ,  customer ,  company_code ,  fin_doc_number,  \"year\" \n"
			+ "                                from\n"
			+ "                                    :customer_invoice_list \n"
			+ "                                where\n"
			+ "                                    shkzg  =  's' ;\n"
			+ "                                    \n"
			+ "\n"
			+ "    customer_company_count_fin  =  select\n"
			+ "                                        distinct system_id ,  client ,  customer ,  company_code ,  count (fin_doc_number) as inv_count \n"
			+ "                                    from\n"
			+ "                                        :customer_company_count \n"
			+ "                                    group by\n"
			+ "                                        system_id,  client,  customer ,  company_code ;\n"
			+ "                                        \n"
			+ "\n"
			+ "    --  Part1 : Build Customer Position	- sum of amounts, max of open days, count of documents\n"
			+ "\n"
			+ "    customer_position  =  select\n"
			+ "                                bsid.system_id,  bsid.client,  bsid.customer,  bsid.local_currency,  bsid.specialist_name as specialist_name,  sum (bsid.outstanding_amount) as outstanding_amount,\n"
			+ "                                cast (max (days_open) as integer ) as oldest_open_invoice_age,  count (bsid.fin_doc_number) as open_invoices_count \n"
			+ "                            from\n"
			+ "                                :customer_invoice_list bsid \n"
			+ "                            group by\n"
			+ "                                bsid.system_id,  bsid.client,  bsid.customer,  bsid.local_currency,\n"
			+ "                                bsid.specialist_name \n"
			+ "                            having\n"
			+ "                                sum (bsid.outstanding_amount) > 0 ;\n"
			+ "                                \n"
			+ "\n"
			+ "    --select * from :customer_position order by customer;\n"
			+ "\n"
			+ "    --	Part 2: \n"
			+ "\n"
			+ "    --	Step 1: Identify customers' highest dunning level		\n"
			+ "\n"
			+ "    customer_max_dunning_level  =  select\n"
			+ "                                        list.system_id,  list.client,  list.customer,  max (list.highest_dunning) as highest_dunning \n"
			+ "                                    from\n"
			+ "                                        :customer_invoice_list as list \n"
			+ "                                    inner join\n"
			+ "                                        :customer_position as total \n"
			+ "                                            on list.system_id  =  total.system_id \n"
			+ "                                            and list.client  =  total.client \n"
			+ "                                            and list.customer  =  total.customer \n"
			+ "                                    group by\n"
			+ "                                        list.system_id,  list.client,  list.customer ;\n"
			+ "                                        \n"
			+ "\n"
			+ "    --	Part 2: \n"
			+ "\n"
			+ "    --	Step 2: Among the highest levels dunned, find the oldest due dated document	\n"
			+ "\n"
			+ "    customer_dunning_date  =  select\n"
			+ "                                    dun.system_id,  dun.client,  dun.customer,  dun.highest_dunning,  max (inv.last_dunned) as last_dunned \n"
			+ "                                from\n"
			+ "                                    :customer_max_dunning_level as dun \n"
			+ "                                inner join\n"
			+ "                                    :customer_invoice_list as inv \n"
			+ "                                        on dun.system_id  =  inv.system_id \n"
			+ "                                        and dun.client  =  inv.client \n"
			+ "                                        and dun.customer  =  inv.customer \n"
			+ "                                        and dun.highest_dunning  =  inv.highest_dunning \n"
			+ "                                group by\n"
			+ "                                    dun.system_id,  dun.client,\n"
			+ "                                    dun.customer,  dun.highest_dunning ;\n"
			+ "                                    \n"
			+ "\n"
			+ "    --select * from :customer_dunning_date order by customer;\n"
			+ "\n"
			+ "    --Part 3: Get Credit Information	\n"
			+ "\n"
			+ "    customer_credit_information  =  select\n"
			+ "                                        cust.system_id,  cust.client,  cust.customer,  knkk.klimk as credit_limit,  knkk.dbrtg as credit_rating \n"
			+ "                                    from\n"
			+ "                                        :customer_company_list as cust \n"
			+ "                                    inner join\n"
			+ "                                        collections.t001 as t001 \n"
			+ "                                            on cust.system_id  =  t001.system_id \n"
			+ "                                            and cust.client  =  t001.mandt \n"
			+ "                                            and cust.company_code  =  t001.bukrs \n"
			+ "                                    left outer join\n"
			+ "                                        collections.knkk as knkk \n"
			+ "                                            on cust.system_id  =  knkk.system_id \n"
			+ "                                            and cust.client  =  knkk.mandt \n"
			+ "                                            and cust.customer  =  knkk.kunnr \n"
			+ "                                            and knkk.kkber  =  t001.kkber ;\n"
			+ "                                            \n"
			+ "\n"
			+ "    --select * from :customer_credit_information order by customer;\n"
			+ "\n"
			+ "    -- 	Part 4\n"
			+ "\n"
			+ "    todays_reminders  =  select\n"
			+ "                            system_id,  client,  customer,  by_when,  case \n"
			+ "                                when count (*)> 0 \n"
			+ "                                then 'remind' \n"
			+ "                                else '' \n"
			+ "                            end as status \n"
			+ "                        from\n"
			+ "                            collections.cm_call_outcomes \n"
			+ "                        where\n"
			+ "                            by_when  =  current_date \n"
			+ "                            and action_code  =  '1' \n"
			+ "                        group by\n"
			+ "                            system_id,  client,  customer,  by_when ;\n"
			+ "                            \n"
			+ "\n"
			+ "    processed_for_the_day  =  select\n"
			+ "                                    outcome.system_id,  outcome.client,  outcome.customer,  ifnull (max (outcome.action_code), '' ) as action_code \n"
			+ "                                from\n"
			+ "                                    collections.cm_call_outcomes as outcome \n"
			+ "                                where\n"
			+ "                                    outcome_date  =  current_date \n"
			+ "                                group by\n"
			+ "                                    outcome.system_id,  outcome.client,\n"
			+ "                                    outcome.customer ;\n"
			+ "                                    \n"
			+ "\n"
			+ "    customer_promises_for_today  =  select\n"
			+ "                                        cust.system_id,  cust.client,  cust.customer,  case \n"
			+ "                                            when processed.action_code  =  '' \n"
			+ "                                            or processed.action_code is null \n"
			+ "                                            then ifnull (remind.status, 'open') \n"
			+ "                                            else 'ok' \n"
			+ "                                        end as status,  ifnull (txt.days_position_text, '') as action_taken \n"
			+ "                                    from\n"
			+ "                                        :customer_company_list as cust \n"
			+ "                                    left outer join\n"
			+ "                                        :todays_reminders as remind \n"
			+ "                                            on cust.system_id  =  remind.system_id \n"
			+ "                                            and cust.client  =  remind.client \n"
			+ "                                            and cust.customer  =  remind.customer \n"
			+ "                                    left outer join\n"
			+ "                                        :processed_for_the_day as processed \n"
			+ "                                            on cust.system_id  =  processed.system_id \n"
			+ "                                            and cust.client  =  processed.client \n"
			+ "                                            and cust.customer  =  processed.customer \n"
			+ "                                    left outer join\n"
			+ "                                        collections.cm_action_text txt \n"
			+ "                                            on processed.action_code  =  txt.action_code \n"
			+ "                                            and txt.lang  =  :in_language ;\n"
			+ "                                            \n"
			+ "\n"
			+ "    --select * from :customer_promises_for_today order by customer;\n"
			+ "\n"
			+ "    --	Bring together the four parts into a single result\n"
			+ "\n"
			+ "    out_customer_summary_list  =  select\n"
			+ "                                        cust.\"system_id\",  cust.\"client\",  cust.\"customer\",  kna.\"customer_name\",  kna.\"telephone_number\",  kna.\"city\",  kna.\"region\",  kna.\"country\",  prio.\"priority\",  cust.\"outstanding_amount\",  cust.\"oldest_open_invoice_age\",  cnt.inv_count as open_invoices_count,\n"
			+ "                                        dunn.last_dunned as \"last_dunned\",  dunn.highest_dunning as \"highest_dunning\",  ifnull(credit.credit_limit, 0) as \"credit_limit\",  ifnull (credit.credit_rating, '') as \"credit_rating\",  cust.\"local_currency\",  outcome.\"status\",  outcome.\"action_taken\",  ifnull (users.first_name||' '||users.last_name, cust.specialist_name ) as assigned_to \n"
			+ "                                    from\n"
			+ "                                        :customer_position as cust \n"
			+ "                                    inner join\n"
			+ "                                        :customer_company_count_fin as cnt \n"
			+ "                                            on cust.system_id  =  cnt.system_id \n"
			+ "                                            and cust.client  =  cnt.client \n"
			+ "                                            and cust.customer  =  cnt.customer \n"
			+ "                                    inner join\n"
			+ "                                        :customer_dunning_date as dunn \n"
			+ "                                            on cust.system_id  =  dunn.system_id \n"
			+ "                                            and cust.client  =  dunn.client \n"
			+ "                                            and cust.customer  =  dunn.customer \n"
			+ "                                    inner join\n"
			+ "                                        :customer_credit_information as credit \n"
			+ "                                            on cust.system_id  =  credit.system_id \n"
			+ "                                            and cust.client  =  credit.client \n"
			+ "                                            and cust.customer  =  credit.customer \n"
			+ "                                    inner join\n"
			+ "                                        :customer_promises_for_today as outcome \n"
			+ "                                            on cust.system_id  =  outcome.system_id \n"
			+ "                                            and cust.client  =  outcome.client \n"
			+ "                                            and cust.customer  =  outcome.customer \n"
			+ "                                    inner join\n"
			+ "                                        collections.cm_invoice_age_priority prio \n"
			+ "                                            on cust.oldest_open_invoice_age >=  prio.interval_start \n"
			+ "                                            and cust.oldest_open_invoice_age <=  prio.interval_end \n"
			+ "                                    left join\n"
			+ "                                        :address as kna \n"
			+ "                                            on cust.system_id  =  kna.system_id \n"
			+ "                                            and cust.client  =  kna.client \n"
			+ "                                            and cust.customer  =  kna.customer \n"
			+ "                                    left outer join\n"
			+ "                                        collections.cm_user as users \n"
			+ "                                            on cust.specialist_name  =  users.username ;\n"
			+ "                                            \n"
			+ "\n"
			+ "end ;\n" + "";
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
		StringTokenizer tokens = new StringTokenizer(sqlScript + "\n",
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
			} else if (token.equals("\"") || token.equals("'")) {
				while (tokens.hasMoreTokens()) {
					cString += token;
					token = tokens.nextToken();
					if (token.equals("\"") || token.equals("'"))
						break;
				}
				cString += token;
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
				cString = "";
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
		System.out.println(new SQLScriptFormatter().format(test6));
	}

}