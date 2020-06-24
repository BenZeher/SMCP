package smgl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglexternalcompanies;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditExternalCompaniesEdit  extends HttpServlet {

	public static final String BUTTON_LABEL_REMOVELINE = "Remove";
	public static final String COMMAND_VALUE_REMOVELINE = "RemoveLine";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String ADDING_LINE_FLAG = "ADDINGLINE";
	public static final String LID_TO_DELETE_PARAM = "LineNumberParam";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	
	public static final String TABLE_UNAPPLIED_DOCUMENTS = "APPLYTODOCSTABLE";
	public static final String UNAPPLIED_DOCUMENTS_TABLE_CONTAINER = "UNAPPLIEDDOCSTABLECONTAINER";
	public static final String TABLE_APPLIED_DOCUMENTS = "APPLIEDDOCUMENTS";
	public static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW"; //"#FF2080";
	public static final int NUMBER_PADDING_LENGTH = 11;
	public static final String BUTTON_LABEL_APPLYLINE = "Apply";
	public static final String BUTTON_LABEL_UNAPPLYLINE = "UNApply";
	public static final String BUTTON_NAME_UNAPPLYLINE = "UNApplyButton";
	public static final String COMMAND_VALUE_UNAPPLYLINE = "UnapplyLine";
	public static final String LINE_NUMBER_TO_UNAPPLY_PARAM = "UnapplyLineNumber";
	public static final String COMMAND_VALUE_APPLYTODOC = "ApplyLine";
	public static final String APPLYTODOCNUMBER_TO_APPLY_PARAM = "ApplyToDocNumber";
	
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; // U
	
	public static final String BOOKMARK_TOP_OF_TABLES = "TopOfTables";
	public static final String RETURN_TO_TABLES_BOOKMARK = "RETURNTOTABLESPARAM";
	
	public static final String PARAM_DB_PREFIX = "DATABASENAME";
	public static final String PARAM_COMPANYNAME_PREFIX = "COMPANYNAME";
	public static final int LID_PADDING_LENGTH = 6;
	
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"GL External Companies",
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLEditExternalCompaniesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLManageExternalCompanies
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLManageExternalCompanies)){
			smedit.getPWOut().println("Error [1561730106] in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		//long lStartingtime = System.currentTimeMillis();
		
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
	    smedit.printHeaderTable();
	    
	    smedit.setbIncludeDeleteButton(false);
	    smedit.setbIncludeUpdateButton(false);
	    
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\"> Return to General Ledger Main Menu</A><BR>\n");

	    try {
			smedit.createEditPage(getEditHTML(smedit), "");
		} catch (SQLException e) {
    		smedit.getPWOut().println("<BR><BR><FONT COLOR=RED><B>Error [1561739292] creating edit screen for external companies - " + e.getMessage() + "</B></FONT>");
				return;
		}
	    
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm) throws SQLException{
		String s = "";
		
		try {
			s += sCommandScript(sm);
		} catch (Exception e2) {
			s += "<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>";
		}
		
		//Store some command values here:
		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">" + "\n";
		//Store database id 
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + sm.getsDBID() + "\""
		+ "\">" + "\n";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">" + "\n";
		
		//Store which line the user has chosen to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LID_TO_DELETE_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LID_TO_DELETE_PARAM + "\""
		+ "\">" + "\n";
				
		s += clsServletUtilities.createHTMLComment("Start the details table here.");
		try {
			s += buildExternalCompanyTables(sm);
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED><B>Error [1561739821] displaying detail lines - " + e.getMessage() 
				+ "</B></FONT><BR>\n"
				;
		}
   		s += "<BR>" + createSaveButton() + "\n";
    	
		return s;
	}

	private String buildExternalCompanyTables(
		SMMasterEditEntry smmastereditentry
		) throws Exception{
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "ID#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Actual database name</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Company name</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";

		s += "  </TR>\n";
		
		String sBackgroundColor = "";
		boolean bOddRow = true;
		
		String SQL = "SELECT * FROM " + SMTableglexternalcompanies.TableName;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			smmastereditentry.getsDBID(), 
			"MySQL", 
			this.toString() + ".buildExternalCompanyTables - user: " + smmastereditentry.getFullUserName()
		);
		
		String sLineText = "";
		while (rs.next()){
			sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
			if (bOddRow){
				sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_GREY;
			}
			
			String slid = Long.toString(rs.getLong(SMTableglexternalcompanies.lid));
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
					+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
					+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
					+ ">\n"
				;
			
			//lid
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ slid 
					+ "</TD>\n";
			
			//db
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >" 
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + PARAM_DB_PREFIX 
					+ clsStringFunctions.PadLeft(slid, "0", LID_PADDING_LENGTH) 
					+ SMTableglexternalcompanies.sdbname + "\""
			    	+ " VALUE=\"" + rs.getString(SMTableglexternalcompanies.sdbname) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableglexternalcompanies.sdbnameLength)
				    + " SIZE = " + "20"
			    	+ ">"
				;
			sLineText += "</TD>\n";
			
			//company name
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >" 
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + PARAM_COMPANYNAME_PREFIX 
					+ clsStringFunctions.PadLeft(slid, "0", LID_PADDING_LENGTH) 
					+ SMTableglexternalcompanies.scompanyname + "\""
			    	+ " VALUE=\"" + rs.getString(SMTableglexternalcompanies.scompanyname) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableglexternalcompanies.scompanynameLength)
				    + " SIZE = " + "20"
			    	+ ">"
				;
			sLineText += "</TD>\n";
			
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ createRemoveLineButton(slid) + "</TD>\n";
			
			sLineText += "  </TR>\n";
			bOddRow = !bOddRow;
			
		}
		rs.close();
		
		//Add the buffer into the main string:
		s += sLineText;

		//Add one blank line so the user can add lines:
		
		sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
		if (bOddRow){
			sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_GREY;
		}

		s += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
			+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
			+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
			+ ">\n"
		;
		
		String sNewLid = "0";
		
		//lid:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "(NEW)" 
				+ "</TD>\n";
		
		//db
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "<INPUT TYPE=TEXT"
				+ " NAME=\"" + PARAM_DB_PREFIX 
				+ clsStringFunctions.PadLeft(sNewLid, "0", LID_PADDING_LENGTH) 
				+ SMTableglexternalcompanies.sdbname + "\""
		    	+ " VALUE=\"" + "" + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableglexternalcompanies.sdbnameLength)
			    + " SIZE = " + "20"
		    	+ ">"
			;
		s += "</TD>\n";
		
		//company name
		s +=  "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "<INPUT TYPE=TEXT"
				+ " NAME=\"" + PARAM_COMPANYNAME_PREFIX 
				+ clsStringFunctions.PadLeft(sNewLid, "0", LID_PADDING_LENGTH) 
				+ SMTableglexternalcompanies.scompanyname + "\""
		    	+ " VALUE=\"" + "" + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableglexternalcompanies.scompanynameLength)
			    + " SIZE = " + "20"
		    	+ ">"
			;
		s += "</TD>\n";
		
		s += "<TD>&nbsp;</TD>";
		
		s += "  </TR>\n";
		
		s += "</TABLE>\n";
		return s;
	}
	
	private String sCommandScript(SMMasterEditEntry sm) throws Exception{
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;

		s += "<script type='text/javascript'>\n";
		
		s += "window.onload = function() {\n"
			+ "    initShortcuts();\n"
			+ "    displayGLDescriptions();\n"
			+ "    setfocustofirstline();\n"
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";


		s += "\n";

		s += "function promptToSave(){\n"		
			//If the record WAS changed, then
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				//If it was anything but the 'SAVE' command that triggered this function...
			+ "        if (\n"
			+ "            (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + COMMAND_VALUE_SAVE + "\" )\n"
			+ "        ){\n"
						//Prompt to see if the user wants to continue
			+ "        return 'You have unsaved changes.';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		//Remove a detail line:
		s += "function removeLine(sLineNumber){\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before removing a line.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (confirm('Are you sure you want to remove the company with ID ' + sLineNumber + '?')){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_REMOVELINE + "\";\n"
			+ "        document.getElementById(\"" + LID_TO_DELETE_PARAM + "\").value = sLineNumber;\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
		s += "\n";
		
		
		s += "</script>\n";
		return s;
	}
	private String createRemoveLineButton(String sLineNumber){
		return "<button type=\"button\""
				+ " value=\"" + BUTTON_LABEL_REMOVELINE + "\""
				+ " name=\"" + BUTTON_LABEL_REMOVELINE + "\""
				+ " onClick=\"removeLine(" + sLineNumber + ");\">"
				+ BUTTON_LABEL_REMOVELINE
				+ "</button>\n"
				;
	}

	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ UPDATE_BUTTON_LABEL
				+ "</button>\n"
				;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
