package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglaccountsegments;
import SMDataDefinition.SMTableglaccountstructures;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;


public class GLAccountStructureEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String OBJECT_NAME = GLAccountStructure.ParamObjectName;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + OBJECT_NAME;
	private static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW";
	private static final String ROW_BACKGROUND_DEFAULT_COLOR = "#C2E0FF";
	public static final String SEGMENT_ID_VALUE = "SEGMENTNAMEVALUE";
	private static final String MAIN_FORM_NAME = "MAIN";
	private static final String USED_SEGMENTS_TABLE = "USEDSEGMENTSTABLE";
	private static final String UNUSED_SEGMENTS_TABLE = "UNUSEDSEGMENTSTABLE";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		GLAccountStructure entry = new GLAccountStructure(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLAccountStructureAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditAccountStructures
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditAccountStructures, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(GLAccountStructureSelect.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(GLAccountStructureSelect.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
					+ "&Warning=You must select an " + OBJECT_NAME + " to delete."
				);
				return;
		    }
		    
		    else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		smedit.getsDBID(),
		    		"MySQL",
		    		this.toString() + ".doPost - User: " + smedit.getUserID()
		    		+ " - "
		    		+ smedit.getFullUserName()
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
        					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
        					+ "&Warning=Error deleting " + OBJECT_NAME + " - cannot get connection."
        				);
    						return;
		    	}
		    	
		    	try{
		    		entry.delete(entry.getlid(), conn);
		    	}catch (Exception e){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
    					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
    					+ "&Warning=Error deleting " + OBJECT_NAME + " " + entry.getsdescription()
    				);
					return;
		    	}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
					+ "&Status=Successfully deleted " + OBJECT_NAME + " - " + entry.getsdescription() + "."
				);
				return;
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(GLAccountStructureSelect.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(GLAccountStructureSelect.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getFullUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountStructureSelect"
					+ "?" + SMTableglaccountstructures.lid + "=" + entry.getlid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
	    	
		}
		
		//If this is a 'resubmit', meaning it's being called by the action class, then
		//the session will have an object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(OBJECT_NAME) != null){
			entry = (GLAccountStructure) currentSession.getAttribute(OBJECT_NAME);
			currentSession.removeAttribute(OBJECT_NAME);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			try {
				entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
			} catch (Exception e) {
				smedit.redirectAction(e.getMessage(), "", "");
				return;
			}
		}
		//Get company name from session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1524595059] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit General Ledger " + OBJECT_NAME + "s";

	    smedit.getPWOut().println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), sCompanyName));
	    smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    
	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		if (sStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B>" + sStatus + "</B><BR>");
		}
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to Main Menu</A><BR>");
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				MAIN_FORM_NAME,
				smedit.getPWOut(),
				smedit,
			entry);
	} catch (Exception e) {
		String sError = "Could not create edit page - " + e.getMessage();
		smedit.getPWOut().println(sError);
		return;
	}

}
	
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm,
			GLAccountStructure entry
	) throws Exception	{

		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SEGMENT_ID_VALUE + "' ID = '" + SEGMENT_ID_VALUE + "' VALUE=''>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid1 + "' ID = '" + SMTableglaccountstructures.lsegmentid1 + "' VALUE='" + entry.getssegmentid1() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid2 + "' ID = '" + SMTableglaccountstructures.lsegmentid2 + "' VALUE='" + entry.getssegmentid2() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid3 + "' ID = '" + SMTableglaccountstructures.lsegmentid3 + "' VALUE='" + entry.getssegmentid3() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid4 + "' ID = '" + SMTableglaccountstructures.lsegmentid4 + "' VALUE='" + entry.getssegmentid4() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid5 + "' ID = '" + SMTableglaccountstructures.lsegmentid5 + "' VALUE='" + entry.getssegmentid5() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid6 + "' ID = '" + SMTableglaccountstructures.lsegmentid6 + "' VALUE='" + entry.getssegmentid6() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid7 + "' ID = '" + SMTableglaccountstructures.lsegmentid7 + "' VALUE='" + entry.getssegmentid7() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid8 + "' ID = '" + SMTableglaccountstructures.lsegmentid8 + "' VALUE='" + entry.getssegmentid8() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid9 + "' ID = '" + SMTableglaccountstructures.lsegmentid9 + "' VALUE='" + entry.getssegmentid9() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTableglaccountstructures.lsegmentid10 + "' ID = '" + SMTableglaccountstructures.lsegmentid10 + "' VALUE='" + entry.getssegmentid10() + "'>");
		
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");
	}
	
	private String getEditHTML(SMMasterEditEntry smedit, GLAccountStructure entry) throws Exception	{
		String s = "";
		
		try {
			s += sCommandScript();
		} catch (Exception e2) {
			s += "<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>";
		}
		
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//ID:
	    s +="  <TR>\n"
	    + "    <TD ALIGN=RIGHT><B>" + "ID:"  + " </B></TD>\n"
	    + "    <TD ALIGN=LEFT>";
	      if(entry.getNewRecord().compareToIgnoreCase("1") == 0){
	         s+= "NEW";
	      }else{
	         s+= entry.getlid();
	      }
	    s+= "</TD>\n"
	    + "    <TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>\n"
	    + "  </TR>\n"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglaccountstructures.lid + "\" VALUE='" + entry.getlid() + "'>\n"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + GLAccountStructure.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>\n"
	    ;  	

	    //Structure ID:
        s += "  <TR>\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Structure ID:"  + " </B></TD>\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTableglaccountstructures.sstructureid + "\""
    	  + " VALUE=\"" + entry.getsstructureid().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableglaccountstructures.sstructureidlLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>\n"
    	  + "    <TD ALIGN=LEFT>" 
    	  + "Every " + OBJECT_NAME + " must have a unique ID. " 
    	  + "</TD>\n"
    	  + "  </TR>\n"
    	  ;
	    
	    //Description:
        s += "  <TR>\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Description:"  + " </B></TD>\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTableglaccountstructures.sdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableglaccountstructures.sdescriptionLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>\n"
    	  + "    <TD ALIGN=LEFT>" 
    	  + "Every " + OBJECT_NAME + " must have a unique description. " 
    	  + "</TD>\n"
    	  + "  </TR>\n"
    	  ;
	    		
       //We need a row to hold our used and unused tables:
        s += buildRowForUsedAndUnusedTables(smedit, entry);
        
        s += clsServletUtilities.createHTMLComment("End the outermost table:");
        s += "</TABLE>\n";
        s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_UPDATE_BUTTON_NAME 
            	+ "' VALUE='" + SUBMIT_UPDATE_BUTTON_VALUE + "' STYLE='height: 0.24in'></P>";
		return s;
	}
	
	private String buildRowForUsedAndUnusedTables(SMMasterEditEntry smedit, GLAccountStructure entry) throws Exception{
		String s = "";
        s += clsServletUtilities.createHTMLComment("Start a row to contain the 'Unused' and 'Used' tables here:");
        
        s += "  <TR>\n";
        
        s += clsServletUtilities.createHTMLComment("This cell is for both the 'Unused' and 'Used' tables:");
        s += "    <TD COLSPAN=3 >\n";
        
        //Instructions:
        s += "<div style = \" font-size:small; font-style:italic; \">" + "\n"
        	+ "The table on the left lists GL segments that are NOT used in this Account Structure; the table on the right lists the"
        	+ " segments that ARE currently used in this Account Structure.  "
        	+ "The USED segments will appear in the Account Structure in the order you see in the 'USED' table." + "\n"
        	+ "</div>" + "\n"
        	
        	+ "<BR/><B>1) To ADD a segment to the Account Structure:</I></B> click the selected"
        	+ " segment in the 'UNUSED' table."
        	+ "<BR/><B>2) To REMOVE a segment from the Account Structure:</I></B> click the selected segment in the 'USED'"
        	+ " table."
        ;
        
        s += buildContainerTableForUsedAndUnusedTables (smedit, entry);
        
        s += "    </TD>\n";
        
        s += "  </TR>\n";
		return s;
	}
	
	private String buildContainerTableForUsedAndUnusedTables (SMMasterEditEntry smedit, GLAccountStructure entry) throws Exception{
		String s = "";
        s += "      <TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + " \""
        	+ " style = \" width:100%; \""
        	+ " >\n";
        
        s += "        <TR>\n";
        
        s += "          <TD style = \" width:50%; vertical-align: top; \" >"
        	+ buildUnusedSegmentsTable(smedit, entry);
        s += "          </TD>\n";
        
        s += "\n";

        s += "          <TD style = \" width:50%; vertical-align: top; \" >"
           	+ buildUsedSegmentsTable(smedit, entry);
        s += "          </TD>\n";

        s += "        </TR>\n";
        
        s += "      </TABLE>\n";
        return s;
	}
	
	private String buildUnusedSegmentsTable(SMMasterEditEntry smedit, GLAccountStructure entry) throws Exception{
		String s = "";
		
		s += clsServletUtilities.createHTMLComment("Start the 'Unused' table here:");
		s += "            <TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " style = \" width:100%; \" "
			+ " ID = \"" + UNUSED_SEGMENTS_TABLE + "\""
			+ ">\n";
		
		s += "              <TR>\n";
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
			+ "UNUSED SEGMENTS"
			+ "</TD>\n"
		;
		s += "              </TR>\n";
		
		//s += "\n" + "<div style= \"cursor: crosshair; \" >" + "\n";

		String SQL = "SELECT"
			+ " " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.sdescription
			+ ", " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength
			+ ", " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid
			+ " FROM " + SMTableglaccountsegments.TableName
			+ " WHERE ("
				+ "(" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid1() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid2() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid3() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid4() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid5() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid6() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid7() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid8() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid9() + ")"
				+ " AND (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " != " + entry.getssegmentid10() + ")"
			+ ") ORDER BY " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.sdescription;
		
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			smedit.getsDBID(), 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".buildUnusedSegmentsTable - user: "
			+ smedit.getUserID()
			+ " - "
			+ smedit.getFullUserName());

		while(rs.next()){
			String sCharacterString = "characters";
			if (rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength) == 1L){
				sCharacterString = "character";
			}

			s += "              <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" "
				+ " onmouseout=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_DEFAULT_COLOR + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ " onclick=\"addSegment(this);\""
				+ ">\n"
				+ "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\""
					+ " style = \" cursor: pointer; \" "
					+ " ID = \"" + rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid) + "\""
					+ ">"
				//+ Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid)) + " "
				+ rs.getString(SMTableglaccountsegments.sdescription) 
					+ " (" + Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)) + " " + sCharacterString + ")"
				+ "    </TD>\n"
				+ "              </TR>\n"
			;
		}
		rs.close();
		
		//s += "</div>" + "\n";
		
		s += clsServletUtilities.createHTMLComment("End of the 'Unused' table");
		s += "            </TABLE>\n";
		
		return s;
	}

	private String buildUsedSegmentsTable(SMMasterEditEntry smedit, GLAccountStructure entry) throws Exception{
		String s = "";
		
		s += clsServletUtilities.createHTMLComment("Start the 'Used' table here:");
		s += "            <TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " style = \" width:100%; \" "
			+ " ID = \"" + USED_SEGMENTS_TABLE + "\""
			+ ">\n";
		
		s += "              <TR>\n";
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
			+ "USED SEGMENTS"
			+ "</TD>\n"
		;
		s += "              </TR>\n";

		String SQL = "SELECT"
			+ " " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.sdescription
			+ ", " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength
			+ ", " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid
			+ " FROM " + SMTableglaccountsegments.TableName
			+ " WHERE ("
				+ "(" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid1() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid2() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid3() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid4() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid5() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid6() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid7() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid8() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid9() + ")"
				+ " OR (" + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid + " = " + entry.getssegmentid10() + ")"
			+ ") ORDER BY " + SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.sdescription;
		
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			smedit.getsDBID(), 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".buildUnusedSegmentsTable - user: "
			+ smedit.getUserID()
			+ " - "
			+ smedit.getFullUserName());
		
		
		while(rs.next()){
			s += "              <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" "
				+ " onmouseout=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_DEFAULT_COLOR + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ " onclick=\"removeSegment(this);\""
				+ ">\n"
				+ "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\""
					+ " style = \" cursor: pointer; \" "
					+ " ID = \"" + rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid) + "\""
					+ ">"
				//+ Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid)) + " "
				+ rs.getString(SMTableglaccountsegments.sdescription) + " (" + Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)) + " characters)"
				+ "    </TD>\n"
				+ "              </TR>\n"
			;
		}
		rs.close();
		s += clsServletUtilities.createHTMLComment("End of the 'Used' table");
		s += "            </TABLE>\n";
		
		return s;
	}
	
	private String sCommandScript () throws Exception{
		
		String s = "";
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		s += "<script type='text/javascript'>\n";
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		//Function for REMOVING a segment:
		s += "function removeSegment(usedrow) { \n"
			
			+ "    //Get the row properties so we can add it to the UNUSED table: \n"
			+ "    var usedrownumber = usedrow.rowIndex;"
			+ "    var usedtable = document.getElementById(\"" + USED_SEGMENTS_TABLE + "\"); \n"
			+ "    var usedcell = usedrow.cells[0]; \n"
			+ "    var segmentID = usedcell.id; \n"

			+ "    //And add a NEW row to the UNUSED table: \n"
			+ "    var unusedtable = document.getElementById(\"" + UNUSED_SEGMENTS_TABLE + "\"); \n"
			+ "    var nextrownumber = unusedtable.rows.length; \n"
			+ "    var unusedrow = unusedtable.insertRow(nextrownumber); \n"
			+ "    unusedrow.style.backgroundColor = \"" + ROW_BACKGROUND_DEFAULT_COLOR + "\"; \n"
			
			+ "    //Add the events to the row: \n"
			+ "    unusedrow.onmousemove = function() { \n"
			+ "         setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "'); \n"
			+ "    }; \n"
			+ "    unusedrow.onmouseout = function() { \n"
			+ "         setRowBackgroundColor(this, '" + ROW_BACKGROUND_DEFAULT_COLOR + "'); \n"
			+ "    }; \n"
			+ "    unusedrow.onclick = function() { \n"
			+ "         addSegment(this); \n"
			+ "    }; \n"
			
			+ "    var unusedcell = unusedrow.insertCell(0); \n"
			+ "    unusedcell.className = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\"; \n"
			+ "    unusedcell.innerHTML = usedcell.innerHTML; \n"
			+ "    unusedcell.id = segmentID; \n"
			+ "    //Now remove the line from the USED table: \n"
			+ "    document.getElementById(\"" + USED_SEGMENTS_TABLE + "\").deleteRow(usedrownumber); \n"
			+ "    updateSegmentIDs(); \n"
    		+ "} \n"
		;
		
		//Function for ADDING a segment:
		s += "function addSegment(unusedrow) { \n"
			+ "    //Get the row properties so we can add it to the USED table: \n"
			+ "    var unusedrownumber = unusedrow.rowIndex; \n"
			+ "    var unusedtable = document.getElementById(\"" + UNUSED_SEGMENTS_TABLE + "\"); \n"
			+ "    var unusedcell = unusedrow.cells[0]; \n"
			+ "    var segmentID = unusedcell.id; \n"

			+ "    //And add a NEW row to the USED table: \n"
			+ "    var usedtable = document.getElementById(\"" + USED_SEGMENTS_TABLE + "\"); \n"
			+ "    var nextrownumber = usedtable.rows.length; \n"
			+ "    var usedrow = usedtable.insertRow(nextrownumber);"
			+ "    usedrow.style.backgroundColor = \"" + ROW_BACKGROUND_DEFAULT_COLOR + "\"; \n"
			
			+ "    //Add the events to the row: \n"
			+ "    usedrow.onmousemove = function() { \n"
			+ "         setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "'); \n"
			+ "    }; \n"
			+ "    usedrow.onmouseout = function() { \n"
			+ "         setRowBackgroundColor(this, '" + ROW_BACKGROUND_DEFAULT_COLOR + "'); \n"
			+ "    }; \n"
			+ "    usedrow.onclick = function() { \n"
			+ "         removeSegment(this); \n"
			+ "    }; \n"
			
			+ "    var usedcell = usedrow.insertCell(0); \n"
			+ "    usedcell.className = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\"; \n"
			+ "    usedcell.innerHTML = unusedcell.innerHTML; \n"
			+ "    usedcell.id = segmentID; \n"

			//+ "    alert ('usedcell.id = ' + usedcell.id); \n"
			
			+ "    //Now remove the line from the USED table: \n"
			+ "    document.getElementById(\"" + UNUSED_SEGMENTS_TABLE + "\").deleteRow(unusedrownumber); \n"
			+ "    updateSegmentIDs(); \n"

    		+ "} \n"
    		
    		+ "function updateSegmentIDs(){ \n"
    		+ "    //First, clear all the hidden segment IDs: \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid1 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid2 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid3 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid4 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid5 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid6 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid7 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid8 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid9 + "\").value = '0'; \n"
    		+ "    document.getElementById(\"" + SMTableglaccountstructures.lsegmentid10 + "\").value = '0'; \n"
    		+ "\n"
    		+ "   //Now set any segment IDs in the 'Used' table: \n"
    		+ "    var usedtable = document.getElementById(\"" + USED_SEGMENTS_TABLE + "\"); \n"
    		+ "    var iRowIndex; \n"
    		
    		+ "    for (iRowIndex = 1; iRowIndex < usedtable.rows.length; iRowIndex++) { \n"
    		+ "        var cellsarray = document.getElementById(\"" + USED_SEGMENTS_TABLE + "\").rows[iRowIndex].cells;"
    		+ "        if (iRowIndex == 1){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid1 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 2){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid2 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 3){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid3 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 4){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid4 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 5){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid5 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 6){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid6 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 7){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid7 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 8){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid8 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 9){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid9 + "\").value = cellsarray[0].id;} \n"
    		+ "        if (iRowIndex == 10){ document.getElementById(\"" + SMTableglaccountstructures.lsegmentid10 + "\").value = cellsarray[0].id;} \n"
			+ "    } \n"
    		
			+ "    /* \n"
			+ "    alert( \n"
			+ "        'SegmentID 1 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid1 + "\").value \n"
			+ "        + ',SegmentID 2 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid2 + "\").value \n"
			+ "        + ',SegmentID 3 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid3 + "\").value \n"
			+ "        + ',SegmentID 4 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid4 + "\").value \n"
			+ "        + ',SegmentID 5 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid5 + "\").value \n"
			+ "        + ',SegmentID 6 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid6 + "\").value \n"
			+ "        + ',SegmentID 7 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid7 + "\").value \n"
			+ "        + ',SegmentID 8 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid8 + "\").value \n"
			+ "        + ',SegmentID 9 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid9 + "\").value \n"
			+ "        + ',SegmentID 10 = ' + document.getElementById(\"" + SMTableglaccountstructures.lsegmentid10 + "\").value \n"
			+ "    ); \n"
			+ "    */ \n"
			
			+ "} \n"
		;
		
		s += "\n";
		s += "</script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}