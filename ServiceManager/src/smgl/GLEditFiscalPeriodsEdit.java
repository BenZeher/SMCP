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
import SMDataDefinition.SMTableglfiscalperiods;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;


public class GLEditFiscalPeriodsEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String OBJECT_NAME = GLFiscalYear.ParamObjectName;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update fiscal year";
	private static final String ROW_BACKGROUND_DEFAULT_COLOR = "#C2E0FF";
	public static final String SEGMENT_ID_VALUE = "SEGMENTNAMEVALUE";
	private static final String MAIN_FORM_NAME = "MAIN";
	private static final String FISCAL_PERIODS_TABLE = "USEDSEGMENTSTABLE";
	private static final String CALLING_CLASS = "GLEditFiscalPeriodsSelect";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		GLFiscalYear entry = new GLFiscalYear(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				GLFiscalYear.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLEditFiscalPeriodsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditFiscalPeriods
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditFiscalPeriods, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(GLEditFiscalPeriodsSelect.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(GLEditFiscalPeriodsSelect.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.get_sifiscalyear().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
					+ "&Warning=You must select a " + OBJECT_NAME + " to delete."
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
        					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
        					+ "&Warning=Error deleting " + OBJECT_NAME + " - cannot get connection."
        				);
    						return;
		    	}
		    	
		    	try{
		    		entry.delete(entry.get_sifiscalyear(), conn);
		    	}catch (Exception e){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080749]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
    					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
    					+ "&Warning=Error deleting " + OBJECT_NAME + " " + entry.get_sifiscalyear() + " - " + e.getMessage()
    				);
					return;
		    	}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080750]");
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
					+ "&Status=Successfully deleted " + OBJECT_NAME + " - " + entry.get_sifiscalyear() + "."
				);
				return;
		    }
	    }
		
		//If this is a 'resubmit', meaning it's being called by the action class, then
		//the session will have an object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(OBJECT_NAME) != null){
			entry = (GLFiscalYear) currentSession.getAttribute(OBJECT_NAME);
			currentSession.removeAttribute(OBJECT_NAME);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			
			//If it's a request to add a NEW fiscal year:
		    //If coming from Add button of select screen; set as new record and clear ID
			if(request.getParameter(GLEditFiscalPeriodsSelect.SUBMIT_ADD_BUTTON_NAME) != null){
			  	//Need a connection for the 'load':
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
        					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
        					+ "&Warning=Error deleting " + OBJECT_NAME + " - cannot get connection."
        				);
    						return;
		    	}
				String SQL= "SELECT " + SMTableglfiscalperiods.ifiscalyear + ""
						+ " FROM "+SMTableglfiscalperiods.TableName
						+" ORDER BY " + SMTableglfiscalperiods.ifiscalyear + " DESC "
						+ " LIMIT 1";
				int maxYear = 0;
		    	try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if(rs.next()) {
						maxYear = rs.getInt( SMTableglfiscalperiods.ifiscalyear);
					}
					entry.set_sifiscalyear(Integer.toString(maxYear));
					entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
					entry.set_snewrecord(GLFiscalYear.ADDING_NEW_RECORD_PARAM_VALUE_TRUE);
					entry.NextYear();
					entry.set_sifiscalyear(Integer.toString(maxYear+1));
					entry.set_slasteditedbyuserfullname("");
					entry.set_slasteditedbyuserid("");
					entry.set_sdattimelastedited("");
					
				} catch (Exception e) {
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1566411638]");
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + CALLING_CLASS
        					+ "?" + SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
        					+ "&Warning=Error retreving " + OBJECT_NAME + " " + entry.get_sifiscalyear() + " - " + e.getMessage()
        				);
    					return;
				}
		    	
			//But if it's NOT in the session, and we're NOT adding a new fiscal year, then try to load it:
			}else{
				entry.set_snewrecord(GLFiscalYear.ADDING_NEW_RECORD_PARAM_VALUE_FALSE);
				try {
					entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", "");
					return;
				}
			}
		}
		//Get company name from session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1531162890] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit General Ledger Fiscal Years";

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
		
	    //Print a link to GL main menu:
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
			GLFiscalYear entry
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
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");
	}
	
	private String getEditHTML(SMMasterEditEntry smedit, GLFiscalYear entry) throws Exception	{
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
	    + "    <TD ALIGN=RIGHT><B>" + "Fiscal year:"  + " </B></TD>\n"
	    + "    <TD ALIGN=LEFT>";
	      if(entry.get_snewrecord().compareToIgnoreCase(GLFiscalYear.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
	    	  s += "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.ifiscalyear + "\""
				+ " VALUE=\"" + entry.get_sifiscalyear().replace("\"", "&quot;") + "\""
				+ " MAXLENGTH=" + "4"
				+ " STYLE=\"height: 0.25in\""
				+ "></TD>\n"
			;
	      }else{
	         s+= entry.get_sifiscalyear()
	         + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglfiscalperiods.ifiscalyear + "\" VALUE='" + entry.get_sifiscalyear() + "'>\n";
	      }
	    s+= "</TD>\n"
	    + "    <TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>\n"
	    + "  </TR>\n"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + GLFiscalYear.ParamsNewRecord+ "\" VALUE='" + entry.get_snewrecord() + "'>\n"
	    ;  	

	    //Last edited by:
        s += "  <TR>\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Last edited by:"  + " </B></TD>\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "User ID: " + entry.get_slasteditedbyuserid() + " - " + entry.get_slasteditedbyuserfullname() + " on " + entry.get_sdattimelastedited() + "\n"
    	  + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglfiscalperiods.ilasteditedbyuserid + "\" VALUE='" + entry.get_slasteditedbyuserid() + "'>\n"
          + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglfiscalperiods.slasteditedbyfullusername + "\" VALUE='" + entry.get_slasteditedbyuserfullname() + "'>\n"
          + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglfiscalperiods.datlastediteddateandtime + "\" VALUE='" + entry.get_sdattimelastedited() + "'>\n"
    	  + "</TD>\n"
    	  + "    <TD ALIGN=LEFT>" 
    	  + "" 
    	  + "</TD>\n"
    	  + "  </TR>\n"
    	  ;
        
        //Number of periods:
        s += "  <TR>\n"
          	  + "    <TD ALIGN=RIGHT><B>" + "Number of periods:"  + " </B></TD>\n"
          	  + "    <TD ALIGN=LEFT>"
          	  + "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.inumberofperiods + "\""
          	  + " VALUE=\"" + entry.get_sinumberofperiods().replace("\"", "&quot;") + "\""
          	  + " MAXLENGTH=" + "2"
          	  + " STYLE=\"height: 0.25in\""
          	  + "></TD>\n"
          	  + "    <TD ALIGN=LEFT>" 
          	  + "Fiscal years would normally have 12 or possibly 13 fiscal periods per year. " 
          	  + "</TD>\n"
          	  + "  </TR>\n"
          	  ;

        //Active?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTableglfiscalperiods.iactive, 
			Integer.parseInt(entry.get_siactive()), 
			"Active?", 
			"<I>Setting to inactive prevents this fiscal year from being closed.</I>"
			)
		;

	    /* TJR - 11/8/2019 - Not sure we need this field at all:
	    //Lock adjustments?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTableglfiscalperiods.ilockadjustmentperiod, 
			Integer.parseInt(entry.get_silockadjustmentperiod()), 
			"Lock adjustments for this year?", 
			"<I>Check to prevent adjustments to this fiscal year.</I>"
			)
		;
		*/
	    
	    /*
	    //Lock closings?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTableglfiscalperiods.ilockclosingperiod, 
			Integer.parseInt(entry.get_silockclosingperiod()), 
			"Lock closing for this year?", 
			"<I>Check to prevent closing to this fiscal year.</I>"
			)
		;
	    */
       //We need a row to hold our starting and ending dates:
        s += buildRowForFiscalPeriodDates(smedit, entry);
        
        s += clsServletUtilities.createHTMLComment("End the outermost table:");
        s += "</TABLE>\n";
        s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_UPDATE_BUTTON_NAME 
            	+ "' VALUE='" + SUBMIT_UPDATE_BUTTON_VALUE + "' STYLE='height: 0.24in'></P>";
		return s;
	}
	
	private String buildRowForFiscalPeriodDates(SMMasterEditEntry smedit, GLFiscalYear entry) throws Exception{
		String s = "";
        s += clsServletUtilities.createHTMLComment("Start a row to contain the dates table here:");
        
        s += "  <TR>\n";
        
        s += "    <TD COLSPAN=3 >\n";
        
        s += buildDatesTable(smedit, entry);
        
        s += "    </TD>\n";
        
        s += "  </TR>\n";
        
        s += clsServletUtilities.createHTMLComment("End the row to contain the dates table here:");
        
		return s;
	}
	
	private String buildDatesTable(SMMasterEditEntry smedit, GLFiscalYear entry) throws Exception{
		String s = "";
		
		s += "            <TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " style = \" width:100%; \" "
			+ " ID = \"" + FISCAL_PERIODS_TABLE + "\""
			+ ">\n";
		
		//Headings:
		s += "              <TR>\n";
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "&nbsp;"
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 1"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 2"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 3"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 4"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 5"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 6"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 7"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 8"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 9"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 10"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 11"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 12"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: white; background-color: black; \" >"
				+ "PERIOD 13"
				+ "</TD>\n"
			;
		s += "              </TR>\n"; 
		
		//Starting dates:
		s += "              <TR>\n";
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: black; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "BEGINNING:&nbsp;"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod1 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod1() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod1, getServletContext())
				+ "</TD>\n"
			;	
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod2 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod2() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod2, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod3 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod3() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod3, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod4 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod4() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod4, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod5 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod5() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod5, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod6 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod6() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod6, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod7 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod7() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod7, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod8 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod8() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod8, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod9 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod9() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod9, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod10 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod10() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod10, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod11 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod11() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod11, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod12 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod12() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod12, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datbeginningdateperiod13 + "\""
	    		+ " VALUE=\"" + entry.get_sdatbeginningdateperiod13() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datbeginningdateperiod13, getServletContext())
				+ "</TD>\n"
			;
		
		s += "              </TR>\n";
		
		//Ending dates:
		s += "              <TR>\n";
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; color: black; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "ENDING:&nbsp;"
				+ "</TD>\n"
			;
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod1 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod1() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod1, getServletContext())
				+ "</TD>\n"
			;	
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod2 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod2() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod2, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod3 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod3() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod3, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod4 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod4() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod4, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod5 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod5() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod5, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod6 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod6() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod6, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod7 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod7() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod7, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod8 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod8() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod8, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod9 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod9() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod9, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod10 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod10() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod10, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod11 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod11() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod11, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod12 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod12() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod12, getServletContext())
				+ "</TD>\n"
			;
		
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\""
				+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableglfiscalperiods.datendingdateperiod13 + "\""
	    		+ " VALUE=\"" + entry.get_sdatendingdateperiod13() + "\""
	    		+ " MAXLENGTH=" + "10"
	    		+ " SIZE = " + "7"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableglfiscalperiods.datendingdateperiod13, getServletContext())
				+ "</TD>\n"
			;
		
		s += "              </TR>\n";
		
		//Period locking:
		s += "              <TR>\n";
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; color: black; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "LOCKED?:&nbsp;"
			+ "</TD>\n"
		;
		String sChecked = "";
		if(entry.get_siperiod1locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod1locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;	
		
		if(entry.get_siperiod2locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod2locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod3locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod3locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod4locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod4locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod5locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod5locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod6locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod6locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod7locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod7locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod8locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod8locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod9locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod9locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod10locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod10locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod11locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod11locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod12locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod12locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;
		
		if(entry.get_siperiod13locked().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		s += "                <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + "\""
			+ " style = \" font-weight:bold; color: white; background-color: " + ROW_BACKGROUND_DEFAULT_COLOR + "; \" >"
			+ "<INPUT TYPE=CHECKBOX + " + sChecked
			+ " NAME=\"" + SMTableglfiscalperiods.iperiod13locked + "\" width=0.25"
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>\n"
		;

		s += "              </TR>\n";
		
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