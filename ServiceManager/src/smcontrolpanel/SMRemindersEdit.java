package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import SMClasses.SMReminders;
import SMDataDefinition.SMTablereminderusers;
import SMDataDefinition.SMTablereminders;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;
;

public class SMRemindersEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String SAVE_BUTTON_LABEL = "Save " + SMReminders.ParamObjectName;
	public static final String SAVE_COMMAND_VALUE = "SAVEREMINDER";
	public static final String DELETE_BUTTON_LABEL = "Delete " + SMReminders.ParamObjectName;
	public static final String DELETE_COMMAND_VALUE = "DELETREMINDER";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	private static final String FORM_NAME = "MAINFORM";
	public static final String USER_ID_MARKER = "User***Update";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMReminders entry = new SMReminders(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMRemindersEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				-1
				);
		
		//Add user list		
		if(entry.getiremindermode().compareToIgnoreCase(Integer.toString(SMTablereminders.GENERAL_REMINDER_VALUE)) == 0){
			if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditReminders)){
				smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
				return;
			}
		}else{
			if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditPersonalReminders)){
				smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
				return;
			}
		}
		

		
		//Get the command value of this page from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMRemindersEdit.COMMAND_FLAG, request);
		
		//If it's an edit, process that:
	    if(sCommandValue.compareToIgnoreCase(SMRemindersEdit.SAVE_COMMAND_VALUE) == 0){
			if(!entry.save(
					getServletContext(), 
					smedit.getsDBID(),
					smedit.getUserID(),
					smedit.getFullUserName(),
					entry.getdatstartdate())){
				smedit.getCurrentSession().setAttribute(SMReminders.ParamObjectName, entry);
				smedit.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					SMReminders.Paramlid + "=" + entry.getlid()
					+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
					);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing the primary key value
				smedit.redirectAction(
					"", 
					"Reminder saved successfully.", 
					SMReminders.Paramlid + "=" + entry.getlid()
					+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
					);
				return;			
			}		
	    }
	    
		//If it's a delete, process that:
	    if(sCommandValue.compareToIgnoreCase(SMRemindersEdit.DELETE_COMMAND_VALUE) == 0){
		    if (clsManageRequestParameters.get_Request_Parameter(SMRemindersEdit.CONFIRM_DELETE_CHECKBOX, request)
			    	.compareToIgnoreCase(SMRemindersEdit.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.getlid();
			    if (!entry.delete(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
			    	smedit.redirectAction(
			    		"Could not delete: " + entry.getErrorMessages(), 
			    		"", 
			    		SMReminders.Paramlid + "=" + entry.getlid()
			    		+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
			    		);
					return;
			    }else{				
						smedit.redirectAction(
							"", 
							entry.getObjectName() + ": " + sID + " was successfully deleted.", 
							SMReminders.Paramlid + "=-1"
							+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
						);			
					return;
			    }
	    	}else{
	    		smedit.redirectAction(
						"You chose to delete without checking the confirm before deleting checkbox.", 
						"", 
						SMReminders.Paramlid + "=" + entry.getlid()
						+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
						);
				return;
	    	}
	    }
		
		//If this is a 'resubmit',
		//the session will have a Reminder entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMReminders.ParamObjectName) != null){
	    	entry = (SMReminders) currentSession.getAttribute(SMReminders.ParamObjectName);
	    	currentSession.removeAttribute(SMReminders.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	try{
	    		entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
		    	}catch (Exception e){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMReminders.Paramlid + "=" + entry.getlid()
						+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
						+ "&Warning=" + e.getMessage()
					);
					return;
		    	}
		    	
	    	}
	    }
	    
	    smedit.printHeaderTable();  
		smedit.getPWOut().println("<BR>");
		
	    try {
	    smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
								+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n");
	    smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    createEditPage(getEditHTML(smedit, entry), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit
			);
	    smedit.getPWOut().println(sCommandScripts(entry, smedit));
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMReminders.Paramlid + "=" + entry.getlid()
				+ "&" + SMReminders.Paramiremindermode + "=" + entry.getiremindermode()
				+ "&Warning=Could not load entry ID: " + entry.getlid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm
	) throws Exception{
		//Create HTML Form
		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//Add save and delete buttons
		pwOut.println("<BR>" + createSaveButton() + "&nbsp;" +createDeleteButton());
		pwOut.println("</FORM>");
	}

	
	private String getEditHTML(SMMasterEditEntry sm, SMReminders entry) throws Exception{
		
		String s = "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\""
				+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMReminders.Paramiremindermode + "\" VALUE=\"" +entry.getiremindermode() + "\""
				+ ">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">";
		
		s += "<TABLE BORDER=1>";
		
		if (sm.getAddingNewEntryFlag() || entry.getNewRecord().compareToIgnoreCase("1") == 0 
				|| entry.getlid().compareToIgnoreCase("-1") == 0 ){
			s += "<TR><TD ALIGN=RIGHT><B>Reminder ID</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + "(NEW)"+ "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMReminders.Paramlid + "\" VALUE=\"" 
					+ "-1"+ "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}else{
			s += "<TR><TD ALIGN=RIGHT><B>Reminder ID</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getlid() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMReminders.Paramlid + "\" VALUE=\"" 
					+ entry.getlid() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}
			//Reminder code
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMReminders.Paramsschedulecode,
					entry.getsschedulecode().replace("\"", "&quot;"), 
					SMTablereminders.sschedulecodelength, 
					"<B>Reminder Name</B>:",
					"Enter unique reminder name.",
					"40",
					"flagDirty();"
			);		
			
			//Description
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
					SMReminders.Paramsdescription,
					entry.getsdescription().replace("\"", "&quot;"),  
					"<B>Description</B>:",
					"Enter a short description to be displayed as the reminder.",
					3,
					40,
					"flagDirty();"
			);
			
			
			//Frequency:
			s += "<TR style=\"background-color:grey; color:white; \"><TD COLSPAN=3>"
				+ "<B>&nbsp;FREQUENCY</B>"
				+ "</TD></TR>"
			;
			      
			//Interval:
			s += "<TR>";
			s += "<TD ALIGN=RIGHT>"
				+ "<B>Interval:</B>"
				+ "</TD>"
			;
			s += "<TD ALIGN=LEFT>";
			;
			for (int i = 1; i <= SMTablereminders.NUMBER_OF_INTERVAL_VALUES; i++){
				String sIntervalValue = Integer.toString(i);
				s += "<label>"
				+ "<INPUT TYPE=\"RADIO\""
				+ " NAME=\"" + SMReminders.Paramiinterval + "\""
				+ " ID=\"" + SMReminders.Paramiinterval + i + "\""
				+ " ONCHANGE=\"flagDirty();checkInterval();\""
				+ " VALUE=\"" + sIntervalValue + "\""; 
				if (entry.getiinterval().compareToIgnoreCase(sIntervalValue) == 0){
					s += " checked";
				}
				s += "/> " + SMTablereminders.getIntervalDescription(i) 
				+ "</label>";
			}
			
			s += "</TD>";			
			s += "<TD ALIGN=LEFT>"
				+ "Time interval for the reminder to repeat."
				+ "</TD>";
			s += "</TR>";
		
			//Month:
			s += "<tbody id=\"month\">";
			s += "<TR>";
			s += "<TD ALIGN=RIGHT>"
				+ "<B>Month:</B>"
				+ "</TD>"
			;
			s += "<TD ALIGN=LEFT>";
			s += "<SELECT NAME = \"" + SMReminders.Paramimonth + "\"" 
			+ " ID=\"" + SMReminders.Paramimonth + "\""
			+ " ONCHANGE=\"flagDirty();\""+ ">";
			for (int i = 1; i <= SMTablereminders.NUMBER_OF_MONTH_VALUES; i++){
				String sMonthValue = Integer.toString(i);
				s += "<OPTION ";
				if (entry.getimonth().compareToIgnoreCase(sMonthValue) == 0){
					s += " selected=yes";
				}
				s += " VALUE=\"" + sMonthValue + "\">" + SMTablereminders.getMonthDescription(i);
			}
			s += "</SELECT>";
			s += "</TD>";			
			s += "<TD ALIGN=LEFT>"
				+ "Select the month of the year."
				+ "</TD>";
			s += "</TR></tbody>";
		
			//Day of the Month
			s += "<tbody id=\"dayofmonth\">";
			s += "<TR>";
			s += "<TD ALIGN=RIGHT>"
				+ "<B>Day of month:</B>"
				+ "</TD>"
			;
			s += "<TD ALIGN=LEFT>";
			s += "<SELECT NAME = \"" + SMReminders.Paramidayofmonth + "\"" 
			+ " ID=\"" + SMReminders.Paramidayofmonth + "\""
			+ " ONCHANGE=\"flagDirty();\""+ ">";

			for (int i = 1; i <= 31; i++){
				String sDayOfMonthValue = Integer.toString(i);
				String sDayOfMonthLabel = Integer.toString(i);
				s += "<OPTION ";
				if (entry.getidayofmonth().compareToIgnoreCase(sDayOfMonthValue) == 0){
					s += " selected=yes";
				}
				
				if(sDayOfMonthValue.compareToIgnoreCase("31") == 0){
					sDayOfMonthLabel = "EOM";
				}
				s += " VALUE=\"" + sDayOfMonthValue + "\">" + sDayOfMonthLabel;
			}
			s += "</SELECT>";
			s += "</TD>";			
			s += "<TD ALIGN=LEFT>"
				+ "Select EOM for the last day of the month."
				+ "</TD>";
			s += "</TR></tbody>";
			
		//Days of the week
			s += "<tbody id=\"daysofweek\"><TR>";
			s += "<TD ALIGN=RIGHT>"
				+ "<B>Day(s) of Week:</B>"
				+ "</TD>"
			;
			s += "<TD>";
			s	+= "<input type=\"checkbox\" name=\"" + SMReminders.Paramisunday + "\" " + isWeekdayScheduled(entry.getisunday()) + "> Sunday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramimonday + "\" " + isWeekdayScheduled(entry.getimonday()) + "> Monday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramituesday + "\" " + isWeekdayScheduled(entry.getituesday()) + "> Tuesday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramiwednesday+ "\" " + isWeekdayScheduled(entry.getiwednesday()) + "> Wednesday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramithursday + "\" " + isWeekdayScheduled(entry.getithursday()) + "> Thursday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramifriday + "\" " + isWeekdayScheduled(entry.getifriday()) + "> Friday<br>"
				+ "<input type=\"checkbox\" name=\"" + SMReminders.Paramisaturday + "\" " + isWeekdayScheduled(entry.getisaturday()) + "> Saturday<br>"
			+ "</TD>"
					;
			s += "<TD ALIGN=LEFT>"
					+ "Select which days of the week to be reminded."
					+ "</TD>";
			s += "</TR></tbody>";
			
			//SCHEDULE USERS:
			s += "<TR style=\"background-color:grey; color:white; \"><TD COLSPAN=3>"
				+ "<B>&nbsp;SCHEDULE USERS</B>"
				+ "</TD></TR>"
			;
						
			//Start Date
			//Date; //Always stored as MM/dd/yyyy
				s += "<TR>";
				s += "<TD ALIGN=RIGHT>"
					+ "<B>Start Date:</B>"
					+ "</TD>"
				;
				
				s += "<TD><INPUT TYPE=TEXT NAME=\"" + SMReminders.Paramdatstartdate + "\""
					+ " VALUE=\"" + entry.getdatstartdate().replace("\"", "&quot;") + "\""
					+ " ID = \"" + SMReminders.Paramdatstartdate + "\""
					+ " ONCHANGE=\"flagDirty();\""
					+ " SIZE=10"
					+ " MAXLENGTH=" + "10"
					+ " STYLE=\"height: 0.25in\""
					+ ">"
					+ SMUtilities.getDatePickerString(SMReminders.Paramdatstartdate, getServletContext()) + "\n"
					+ "</TD>"
				;
				s += "<TD ALIGN=LEFT>"
					+ "Date to start reminder."
					+ "</TD>";
				s += "</TR>";
			
			boolean bAllowSchedulingOtherUsers = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditReminders, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));	
			 //If user is allowed to schedule other users generate a list of all users
			if(bAllowSchedulingOtherUsers &&(entry.getiremindermode().compareToIgnoreCase(
					Integer.toString(SMTablereminders.GENERAL_REMINDER_VALUE)) == 0)){
			ArrayList<String> sUserTable = new ArrayList<String>(0);
			try{
				//First get a list of all the users:
		        String sSQL = MySQLs.Get_User_List_SQL(false);
		       
		        ResultSet rsUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());
		        
		       sSQL = "SELECT * FROM " + SMTablereminderusers.TableName
		    		   + " WHERE(" + SMTablereminderusers.sschedulecode + "='" + entry.getsschedulecode() + "')" ;
		     
		        ResultSet rsScheduledUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());
		        
		        String sCheckedOrNot = "";
	        	while (rsUsers.next()){
	        		sCheckedOrNot = isUserScheduled(rsUsers.getString(SMTableusers.lid), rsScheduledUsers);
	        		sUserTable.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + " NAME=\"" + USER_ID_MARKER  
	        			+  rsUsers.getString(SMTableusers.lid) + "\">" 
	        			+ rsUsers.getString(SMTableusers.sUserFirstName) 
	        			+ " " + rsUsers.getString(SMTableusers.sUserLastName)
	        			+ "" 
	        		);
	        	}
	        	rsUsers.close();
	        	rsScheduledUsers.close();
	        	//Print the table:	        	
	        	s += SMUtilities.Build_HTML_Table(3, sUserTable,1,true);
        	
			}catch (SQLException e){
		    	throw new Exception("Error generating users list - " + e.getMessage());
			}
		}else{
			//Otherwise if it's a personal reminder just store the current user in a hidden field.
			s += "<INPUT TYPE=HIDDEN " + " NAME=\"" + USER_ID_MARKER + sm.getUserID() + "\""+ ">";
		}		
			s += "</TABLE>";

		return s;
	}
	
	
	private String isWeekdayScheduled(String weekdayValue){
		String checked = "";
		if(weekdayValue.compareToIgnoreCase("1") == 0){
			checked = "checked";
		}
		return checked;
	}
	
	private String isUserScheduled(String sCurrentUser, ResultSet rsScheduledUsers) throws SQLException {
		String isScheduled = "";
		rsScheduledUsers.beforeFirst();
		while(rsScheduledUsers.next()){
			if(sCurrentUser.compareToIgnoreCase(rsScheduledUsers.getString(SMTablereminderusers.luserid)) == 0){
				isScheduled = "checked";
			}
		}	
		return isScheduled;
	}

	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createDeleteButton(){
		String s = "";
		s = "<button type=\"button\""
		+ " value=\"" + DELETE_BUTTON_LABEL + "\""
		+ " name=\"" + DELETE_BUTTON_LABEL + "\""
		+ " onClick=\"isdelete();\">"
		+ DELETE_BUTTON_LABEL
		+ "</button>\n";
		
		s += "<INPUT TYPE='CHECKBOX' NAME='" + CONFIRM_DELETE_CHECKBOX 
				+ "' VALUE='" + CONFIRM_DELETE_CHECKBOX + "' > Check to confirm before deleting";
		return s;
	}
	
	private String sCommandScripts(
			SMReminders schedule, 
			SMMasterEditEntry smmaster
			) throws SQLException{
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
					
			//Prompt to save:
			s += "window.onbeforeunload = promptToSave;\n";
			
			s += "window.onload = checkInterval;\n";

			s += "function promptToSave(){\n"		
				
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + SAVE_COMMAND_VALUE + "\""
						+ " && document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + DELETE_COMMAND_VALUE + "\"){\n"
				+ "        return 'You have unsaved changes!';\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n\n"
			;
			
			//Delete:
			s += "function isdelete(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			
			//Check Reminder Interval
			s += "function checkInterval(){\n"
				+ "	 if(document.getElementById(\"" + SMReminders.Paramiinterval + Integer.toString(SMTablereminders.INTERVAL_TYPE_WEEKLY) + "\").checked){\n"
				+ "    document.getElementById(\"" + "dayofmonth"+ "\").style.display =\"none\" ;\n"
				+ "    document.getElementById(\"" + "month" + "\").style.display =\"none\" ;\n"
				+ "    document.getElementById(\"" + "daysofweek" + "\").style.display =\"table-row-group\" ;\n"
				+ "	 }else if(document.getElementById(\"" + SMReminders.Paramiinterval + Integer.toString(SMTablereminders.INTERVAL_TYPE_MONTHLY) + "\").checked){\n"
				+ "    document.getElementById(\"" + "dayofmonth"+ "\").style.display =\"table-row-group\" ;\n"
				+ "    document.getElementById(\"" + "month" + "\").style.display =\"none\" ;\n"
				+ "    document.getElementById(\"" + "daysofweek" + "\").style.display =\"none\" ;\n"
				+ "  }else{\n"
				+ "    document.getElementById(\"" + "dayofmonth"+ "\").style.display =\"table-row-group\" ;\n"
				+ "    document.getElementById(\"" + "month" + "\").style.display =\"table-row-group\" ;\n"
				+ "    document.getElementById(\"" + "daysofweek" + "\").style.display =\"none\" ;\n"
				+ "  }\n"
				+ "}\n"
			;

			//Flag page dirty:
			s += "function flagDirty() {\n"
					+ "    flagRecordChanged();\n"
					+ "}\n"
				;

			s += "function flagRecordChanged() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "}\n"
				;
			s += "</script>\n";
			return s;
		}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
