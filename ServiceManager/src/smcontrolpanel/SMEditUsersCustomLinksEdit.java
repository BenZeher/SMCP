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

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecustomlinks;
import SMDataDefinition.SMTableusers;
import SMDataDefinition.SMTableuserscustomlinks;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMSystemFunctions;
;

public class SMEditUsersCustomLinksEdit  extends HttpServlet {

	public static final String TABLE_WIDTH = "930px";
	
	private static final long serialVersionUID = 1L;
	public static final String SAVE_BUTTON_LABEL = "Add Custom Link" ;
	public static final String SAVE_COMMAND_VALUE = "ADDCUSTOMLINK";
	public static final String DELETE_BUTTON_LABEL = "Delete Custom Link";
	public static final String DELETE_COMMAND_VALUE = "DELETCUSTOMLINK";

	public static final String COMMAND_FLAG = "COMMANDFLAG";
	private static final String FORM_NAME = "MAINFORM";
	public static final String USER_UPDATE_ID_MARKER = "User***Update";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//SMReminders entry = new SMReminders(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Custom Link",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditUsersCustomLinksAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditUsersCustomLinks
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditUsersCustomLinks)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
			
	    //Output Edit page HTML
	    smedit.printHeaderTable();  
		smedit.getPWOut().println("<BR>");
		smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    try {
	    smedit.getPWOut().println(clsServletUtilities.getJQueryIncludeString());
	    smedit.getPWOut().println(getStyles());
	    smedit.getPWOut().println(sCommandScripts(smedit));
	    createEditPage(
	    		getEditHTML(smedit), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit
			);
	   // smedit.getPWOut().println(sCommandScripts(smedit));
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + "Warning= Could not load Page - " + sError

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
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + sm.getCallingClass() + "\">");
		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		pwOut.println("</FORM>");
	}

	
	private String getEditHTML(SMMasterEditEntry sm) throws Exception{
		
		String s = "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">";
	
		
		//***********Add new link*************************
		s += "<TABLE BORDER=1 WIDTH=" + TABLE_WIDTH + ">"; 
		s += "\n\n<TR class=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \">"
			+ "<TD COLSPAN=3>"
			+ "<B>&nbsp;ADD LINK</B>"
			+ "</TD>"
			+ "</TR>\n"
		;
			//URL Name
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablecustomlinks.surlname,
					"", 
					SMTablecustomlinks.surlnameLength, 
					"<B>Display Name</B>:",
					"This is how the link will appear on the users main menu.",
					"40",
					""
			);		
			
			//URL
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
					SMTablecustomlinks.surl,
					"",  
					"<B>URL</B>:",
					"Enter the full url.",
					3,
					40,
					""
			);
	
			s += "</TABLE>";
			
			//Display all Users
			ArrayList<String> sUserTable = new ArrayList<String>(0);
			try{
		        String sSQL = SMMySQLs.Get_User_List_SQL(false);	       
		        ResultSet rsUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());	        
	        	while (rsUsers.next()){
	        		sUserTable.add((String) "<INPUT TYPE=CHECKBOX " + " NAME=\"" + USER_UPDATE_ID_MARKER  
	        			+  Integer.toString(rsUsers.getInt(SMTableusers.lid)) + "\">" 
	        			+ rsUsers.getString(SMTableusers.sUserFirstName) 
	        			+ " " + rsUsers.getString(SMTableusers.sUserLastName)
	        			//+ " (" + rsUsers.getString("sUserName") + ")" 
	        		);
	        	}
	        	rsUsers.close(); 	
		        s += "<div style=\"width:" + TABLE_WIDTH + ";\">";
		        s += "<input type=\"checkbox\" name=\"selectall\" id=\"selectall\" /> select all";
	        	s += SMUtilities.Build_HTML_Table(5, sUserTable, 1,true); 
		        s += "<BR>" + createSaveButton();
		        s += "</div>";
			}catch (SQLException e){
		    	throw new Exception("Error generating users list - " + e.getMessage());
			}
			s += "<BR>\n";  
		     
			//******Delete existing link *******************
			s += "<TABLE BORDER=1 WIDTH=" + TABLE_WIDTH + ">"; 
			s += "<TR class=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \"><TD COLSPAN=3>"
				+ "<B>&nbsp;REMOVE LINK</B>"
				+ "</TD></TR>"
			;
			s += "</TABLE>";
			//Get each distinct URL 
			String sSQL = " SELECT  * " 
					+ " FROM " + SMTablecustomlinks.TableName
					+ " ORDER BY " +  SMTablecustomlinks.surlname
					;	       
	        ResultSet rsCustomLinks = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());

	       while(rsCustomLinks.next()) {
				s += "\n<div class=\"link-heading\">\n";
	
				s += "<TABLE BORDER=1 WIDTH=905px" + ">";
				s += "<TR><TD>\n";
				s += "<div class=\"arrow-up\">&#9650;</div>\n"
				   + "<div class=\"arrow-down\">&#9660;</div>\n";
				s += "<B>" + rsCustomLinks.getString(SMTablecustomlinks.surlname) + "</B>"
					;
				s += createDeleteButton(rsCustomLinks.getString(SMTablecustomlinks.lid))
					+ "</TD></TR>\n";
				s += "</TABLE>\n";
				s += "</div>";
				
				//Get user that has this URL
				String SQL = " SELECT " +  SMTableuserscustomlinks.TableName + "." + SMTableuserscustomlinks.luserid
						+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName 
						+ ", " +  SMTableusers.TableName + "." + SMTableusers.sUserLastName
						+ " FROM " + SMTableuserscustomlinks.TableName
						+ " LEFT JOIN " + SMTableusers.TableName
						+ " ON " + SMTableusers.TableName + "." + SMTableusers.lid 
						+ " = " + SMTableuserscustomlinks.TableName + "." + SMTableuserscustomlinks.luserid				
						+ " WHERE (" +  SMTableuserscustomlinks.icustomlinkid + "='" + rsCustomLinks.getString(SMTablecustomlinks.lid) + "')"
						;	       
		        ResultSet rsLinkUsers = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sm.getsDBID());
	        	ArrayList<String> sLinkUsersTable = new ArrayList<String>(0);
		        while(rsLinkUsers.next()) {
		        	sLinkUsersTable.add(rsLinkUsers.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName ) 
		        			+ " " + rsLinkUsers.getString(SMTableusers.TableName + "." + SMTableusers.sUserLastName)
		        		);
		        }
		        rsLinkUsers.close();
		        //Display link Information:
		        s += "\n<div class=\"link-users\" >\n";
		        s += "<div style=\"width:" + TABLE_WIDTH + "; border:1px solid black;\">";
		        s += "<br><font size=\"2.5\"><b>&nbsp;&nbsp;URL</B><A target=\"_blank\" HREF=\"" + rsCustomLinks.getString(SMTablecustomlinks.surl) + "\">"
		        		+ rsCustomLinks.getString(SMTablecustomlinks.surl) + "</A><br>";
		        s += "<br>";
		        s += SMUtilities.Build_HTML_Table(5, sLinkUsersTable ,0,true);
		        s += "</FONT></div>";
		        s += "\n<br></div>\n";

	       }
	       rsCustomLinks.close();
	     
		return s;
	}

	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createDeleteButton( String sURLID){
		
		return "<button type=\"button\""
		+ " value=\"" +  DELETE_BUTTON_LABEL + "\""
		+ " name=\"" + DELETE_BUTTON_LABEL + "\""
		+ " style=\"float: right;\""
		+ " onClick=\"isdelete(event, \'" + sURLID + "\');\">"
		+ DELETE_BUTTON_LABEL
		+ "</button>\n";
	}
	
	
	private String getStyles() {
		// TODO Auto-generated method stub
		return "<style>\n" + 
				"\n" + 
				"  \n" + 
				"  .link-heading{padding-left:25px;margin-top:2px;cursor:pointer;}\n" + 
				"  .link-users{}\n" + 
				"  .arrow-up{margin-left:-20px;width:25px;display:inline-block;}\n" + 
				"  .arrow-down{margin-left:-20px;width:25px;display:inline-block;}\n" + 
				"  \n" + 
				"</style>";
	}
	
	private String sCommandScripts( 
			SMMasterEditEntry smmaster
			) throws Exception{
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
			
			s+= "$(document).ready(function() {\n" + 
					"    $(\".link-users\").hide();\n" + 
					"    $(\".arrow-up\").hide();\n" + 
					"    $(\".link-heading\").click(function(){\n" + 
					"            $(this).next(\".link-users\").slideToggle(600);\n" + 
					"            $(this).find(\".arrow-up, .arrow-down\").toggle();\n" + 
					"    });\n" + 
					
   					"    $('#selectall').change(function(event) {\n" + 
					"		 if(this.checked) {\n" +
					"       	$(\"input:checkbox\").each(function() {\n" + 
					"          	this.checked = true;\n" + 
					"      		});\n" + 
					"  		}\n" + 
					"  		else {\n" + 
					"    		$(\"input:checkbox\").each(function() {\n" + 
					"          	this.checked = false;\n" + 
					"      		});\n" + 
					"  		}\n" + 
					" 	});" +
					"});\n"
					;						
					
			//Delete:
			s += "function isdelete(e, sURLID){\n"
				+ "        e.stopPropagation();"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE +  "\" + sURLID;\n"
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

			//Check ALL 
	       	
	       	
			s += "</script>\n";
			
			return s;
		}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

