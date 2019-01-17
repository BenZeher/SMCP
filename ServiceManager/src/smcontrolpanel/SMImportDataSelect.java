package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableicvendorterms;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableproposalterms;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMImportDataSelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String PARAM_EXECUTESTRING = "EXECUTESTRING";

	private String sCompanyName = "";
	private String sDBID = "";
	String sFormName = "IMPORT";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMImportData
		)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		//Values on the Dropdown List
		String [] sSQLtables = {
				"Select Table",
				"GL Accounts",
				"Mechanics",
				"Proposal Terms",
				"Users","IC items",
				"IC items Prices",
				"IC Vendor Terms",
				"IC Vendors",
				"AR Customers",
				"AR Customers Ship To",
				"Custom"};
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String title = "SM Import Data";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		try{
			out.println(getJavaScript(sSQLtables));
		}catch(Exception e){
			
		}
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMImportData) 
				+ "\">Summary</A><BR><BR>");
		//Link to the data definitions mapping:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayDataDefs?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Display data definitions</A>"
				);
		
		String sExecuteString = clsManageRequestParameters.get_Request_Parameter(PARAM_EXECUTESTRING, request);
		if (sExecuteString.compareToIgnoreCase("") == 0){
			sExecuteString = "";
		}
		
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMImportDataAction\" method = \"post\" ENCTYPE = \"multipart/form-data\" >");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME= CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		
		//Drop Down to show selected tables with pre loaded commands
		out.println("<TR><TD ALIGN=RIGHT><B><U> Import table:</U></B></TD>");
		out.println("<TD><select id =\"list\" onchange =\"getSelectValue();\">\n");
		for(int i = 0; i < sSQLtables.length; i++){
			out.println("<option value = \""+sSQLtables[i]+"\">"+sSQLtables[i]+"</option>");
		}
		out.println("</select></TD></TR>\n");
		out.println("<TR><TD ALIGN=RIGHT><B>Fields :</B></TD>");
		out.println("<TD><div id = \"fields\"></div></TD></TR>");
		out.println("<TR><TD ALIGN=RIGHT><B>Include headers? </B></TD>");
		out.println("<TD><input type =\"checkbox\" id =\"header\" value=\"\" onclick = \"return createHeaderCommand();\"></input> Include the field names of the table from the CSV File</TD></TR>");
		out.println("</TABLE>");
		out.println("<BR>");
		//Text box for the SQL Command
		out.println("<B><U> Enter SQL command below:</U></B><BR>");
		out.println("<TEXTAREA ID = \""+PARAM_EXECUTESTRING+"\" NAME=\"" + PARAM_EXECUTESTRING + "\""
				+ " rows=\"" + "20" + "\""
				+ " cols=\"" + "120" + "\""
				+ ">" + sExecuteString + "</TEXTAREA>"
		);
		out.println("<BR>");
		
		//Choose file button
		out.println("<input type =\"file\" name=\"file\"><BR>\n");
		out.println("<TABLE border=1 >\n");
		out.println("<TR><TD style = \"color: red\"><B>OPERATION ?</B></TD>");
		out.println("<TD><input type =\"radio\" name = \"operation\" id = \"operation\" value=\"UPDATE\">Update</input>");
		out.println("<input type =\"radio\" name = \"operation\" id = \"operation\" value =\"INSERT/UPDATE\">Insert/Update</input>");
		out.println("<input type =\"radio\" name = \"operation\" id = \"operation\" value =\"INSERT\">Insert</input><br></TD></TR>");
		out.println("</TABLE>\n");
		out.println("<BR>");
		//Submit button
		out.println("<INPUT TYPE=\"SUBMIT\" onClick=\"return submitCommand();\" VALUE=\"----Execute command----\">");
		out.println("</FORM>");
		
		//Javascript to get the preloaded commands of the tables shown in the drop down
		
		out.println("</BODY></HTML>");
	}
	private String getJavaScript(String [] sSQLtables) throws Exception{
		String s = "<script>\n";
		
		   s += "function selectAll(source){\n"
					+ "var items = document.getElementsByName(\"fields\");\n"
					+ "for(var i = 0; i < items.length; i++){\n"
					+ "if(items[i].type == \"checkbox\")\n"
					+ "    items[i].checked = source.checked;\n  "
					+ "}\n"
					+"generateCommand();\n"
				+ "}\n";
		   
		   s+= "function createHeaderCommand(){\n"
			   + "var SQLcommand = document.getElementById('EXECUTESTRING').value;\n"
			   + "var dropDown = document.getElementById('list').value;\n"
			   + "if(SQLcommand == '' || dropDown == 'Select Table'){\n"
			   + " return false;\n"
			   + " }\n"
			   +" generateCommand();\n"
			   +" return true;\n"
			   +" }\n";

		   
				s += " function getSelectValue () {\n"
				 + " var dropDown = document.getElementById(\"list\").value;\n"
				 + " switch(dropDown){\n";
		//int numberOfCheckBox = 0;
				 for(int i = 0; i < sSQLtables.length; i++){
					 s +="  case \""+sSQLtables[i]+"\":\n";
							String table = getTableAndColumns(sSQLtables[i],sDBID);
							s += "var child = document.getElementById(\"fields\");\n"
								+"while(child.hasChildNodes()){\n"
								+ " child.removeChild(child.lastChild);\n"
								+ "}\n";
							s += "var field = document.getElementById(\"fields\");\n";
							if(table.equals("")){
								s += "field.innerHTML = field.innerHTML + \"\";\n" ;
							}else{
								s+=  "field.innerHTML += "+table+";\n" ;
							}
								
							
					 //s +="   document.getElementById(\""+PARAM_EXECUTESTRING+"\").value = "+getTableAndColumns(sSQLtables[i],sConfFile)+";\n"
					  s+= "   break;\n";
				 }
			   s += "     }\n"
				 + "  }\n";
			   
		
			   
				 s += "function generateCommand(){\n"
						 + " var dropDown = document.getElementById(\"list\").value;\n"
						 + " switch(dropDown){\n";
						 for(int i = 0; i < sSQLtables.length; i++){
							 s +="  case \""+sSQLtables[i]+"\":\n";
							 		String tableName = getTable(sSQLtables[i],sDBID);
							 		String sCommand = "";
							 		sCommand = " \"INTO TABLE "+tableName+"\\n\""
									 + "+ \"FIELDS TERMINATED BY \',\'\\n\""
									 + "+ \"OPTIONALLY ENCLOSED BY '\\\"' \\n\""
									 + "+ \"LINES TERMINATED BY '\\\\n' \\n\"";
							 		s +=  " var a = "+sCommand+";\n"
							         +"if(document.getElementById(\"header\").checked == false){\n"
							         +"   a += \"IGNORE 1 LINES \\n\"\n "
									 + "}\n "
									 + " a += \"(\"\n"
									 + " var array = [];\n"
				    + " for(var i = 0; i < document.getElementById(\"total\").value; i++){\n"
					+ "        if(document.getElementById(\"id\"+i).checked == true){\n"
					+ "                  array.push(document.getElementById(\"id\"+i).value);\n"
					+ "        }\n"
					+ "   }\n"
					+ " for(var j = 0; j < array.length; j++){\n"
					+ "             if(j == array.length - 1){\n"
					+ "                 a += array[j] + \"\\n\"\n"
					+ "             }else{\n"
					+ "               a += array[j] +\",\\n\"\n"
					+ "              }\n"
					+ "   }\n"
					+ "   a += \")\\n\";\n"
					+ "   document.getElementById(\""+PARAM_EXECUTESTRING+"\").value = a;\n"
					+ "   break;\n";
						 }
				s	+= "     }\n"
				 + "       }\n"
				 + " function submitCommand(){" 
				 + " var sqlCommand = document.getElementById('EXECUTESTRING').value;\n"
				 + " var operation = document.getElementsByName('operation');\n"
				 + " if(sqlCommand == ''){\n"
				 + "  alert('Please enter SQL Command Below');\n"
				 + "  return false;\n"
				 + "  }\n"
				 + "if(sqlCommand.includes(\";\") === false){\n"
				 + " alert('Please add a semi colon at the end of the query');\n"
				 + " return false;\n"
				 + " }\n"
				 + " var isChecked = false;\n"
				 + " for ( var i = 0; i < operation.length; i++){\n"
				 +"  if(operation[i].checked){\n"
				 +"   isChecked = true;\n"
				 +"   }\n"
				 +"  }\n"
				 +"  if(isChecked === false){\n"
				 +"   alert('Please select an operation');\n"
				 +"   return false;\n"
				 +"  }\n"
				 + "  return true;\n"
				 +" }\n"
				 + "</script>\n"
				 ;
		return s;
	}
	/**
	 * getTableAndColumns
	 * Grabs the columns of the table 
	 * @param sSelectedTable
	 * @param DATABASEID
	 * @return
	 * @throws Exception
	 */
	private String getTableAndColumns(String sSelectedTable, String DATABASEID) throws Exception{
		String tableAndColumn = "";
		switch (sSelectedTable){
		case "GL Accounts":
				tableAndColumn = createTableAndColumns(SMTableglaccounts.TableName,DATABASEID);
				break;
		case "Mechanics":
			tableAndColumn = createTableAndColumns(SMTablemechanics.TableName,DATABASEID);
			break;
		case "Proposal Terms":
			tableAndColumn = createTableAndColumns(SMTableproposalterms.TableName,DATABASEID);
			break;
		case "Users":
			tableAndColumn = createTableAndColumns(SMTableusers.TableName,DATABASEID);
			break;
		case "IC items":
			tableAndColumn = createTableAndColumns(SMTableicitems.TableName,DATABASEID);
			break;
		case "IC items Prices":
			tableAndColumn = createTableAndColumns(SMTableicitemprices.TableName,DATABASEID);
			break;
		case "IC Vendor Terms":
			tableAndColumn = createTableAndColumns(SMTableicvendorterms.TableName,DATABASEID);
			break;
		case "IC Vendors":
			tableAndColumn = createTableAndColumns(SMTableicvendors.TableName,DATABASEID);
			break;
		case "Custom":
			tableAndColumn = createTableAndColumns("Custom",DATABASEID);
			break;
		case "AR Customers":
			tableAndColumn = createTableAndColumns(SMTablearcustomer.TableName,DATABASEID);
			break;
		case "AR Customers Ship To":
			tableAndColumn = createTableAndColumns(SMTablearcustomershiptos.TableName,DATABASEID);
			break;
		default:
			tableAndColumn = "";
			break;
		
		}
		return tableAndColumn;
	}
	
	private String getTable(String sSelectedTable, String DATABASEID) throws Exception{
		String sTableName = "";
		switch (sSelectedTable){
		case "GL Accounts":
				sTableName = SMTableglaccounts.TableName;
				break;
		case "Mechanics":
			sTableName = SMTablemechanics.TableName;
			break;
		case "Proposal Terms":
			sTableName = SMTableproposalterms.TableName;
			break;
		case "Users":
			sTableName = SMTableusers.TableName;
			break;
		case "IC items":
			sTableName = SMTableicitems.TableName;
			break;
		case "IC items Prices":
			sTableName = SMTableicitemprices.TableName;
			break;
		case "IC Vendor Terms":
			sTableName = SMTableicvendorterms.TableName;
			break;
		case "IC Vendors":
			sTableName = SMTableicvendors.TableName;
			break;
		case "Custom":
			sTableName = "Custom";
			break;
		case "AR Customers":
			sTableName = SMTablearcustomer.TableName;
			break;
		case "AR Customers Ship To":
			sTableName = SMTablearcustomershiptos.TableName;
			break;
		
		}
		return sTableName;
	}
	
	/**
	 * SQLTableAndColumns
	 * Runs the command to get the columns of the table 
	 * @param sTableName
	 * @param DATABASEID
	 * @return
	 * @throws Exception
	 */

	private String createTableAndColumns(String sTableName, String DATABASEID) throws Exception{
		int checkboxcount = 0;
		String s = " \"<TABLE WIDTH=100% CELLPADDING=5 border=4 STYLE = \'font-size:small;\'>\"\n+"
				 + "\"<TR>\"\n +"
				 + "\"<TD><input type = \\'checkbox\\'  id= \\'checkall\\' onclick= \\'selectAll(this)\\'></TD>\"\n+"
				 + "\"<TD><B>NAME</B></TD>\"\n+"
				 + "\"<TD><B>TYPE</B></TD>\"\n+"
				 + "\"<TD><B>KEY</B></TD>\"\n+"
				 + "\"<TD><B>DEFAULT VALUE</B></TD>\"\n+"
				 + "\"<TD><B>EXTRA</B></TD>\"\n+";
	  if(!sTableName.equals("Custom")){
		  s += "\"</TR>\"\n+";
		  String  sSQL =  "DESCRIBE "+sTableName+";";
		  java.sql.ResultSet rsTableColumns;
		try {
			rsTableColumns = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), DATABASEID);
			  while(rsTableColumns.next()){
				  	s += "\"<TR>\"\n+"
				  	  +   "\"<TD><input type = \\'checkbox\\' name = \\'fields\\'  value = \\'"+rsTableColumns.getString("Field")+"\\' id = \\'id"+checkboxcount+"\\' onclick = 'generateCommand()' ></TD>\"\n+"
				  	  +   "\"<TD>"+rsTableColumns.getString("Field")+"</TD>\"\n+"
				  	  +   "\"<TD>"+rsTableColumns.getString("Type")+"</TD>\"\n+"
				  	  +   "\"<TD>"+rsTableColumns.getString("Key")+"</TD>\"\n+"
				  	  +   "\"<TD>"+rsTableColumns.getString("Default")+"</TD>\"\n+"
				  	  +   "\"<TD>"+rsTableColumns.getString("Extra")+"</TD>\"\n+"
				  	  + "\"</TR>\"\n+";
				  	checkboxcount++;	  
			  }
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
		
	  }else{
		  s += "\"</TR>\"+";
	  }
	  s += "\"</TABLE>\"\n+"
			  + "\"<input type = \\'hidden\\' name = \\'name\\' value = \\'"+checkboxcount+"\\' id = \\'total\\'>\"\n";
	  return s;
	  
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}