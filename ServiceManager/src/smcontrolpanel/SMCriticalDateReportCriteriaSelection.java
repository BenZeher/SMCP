package smcontrolpanel;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import SMDataDefinition.SMTablecriticaldates;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

import java.sql.*;
import java.util.ArrayList;

	public class SMCriticalDateReportCriteriaSelection extends HttpServlet {
		
		private static final long serialVersionUID = 1L;
		private String sDBID = "";
		private String sCompanyName = "";
		private String sUserID = "";
		private String sUserFullName = "";
		
		//Params specific to report options
		public static final String ParamIncludeUnresolved = "IncludeUnresolved";
		public static final String ParamIncludeResolved = "IncludeResolved";
		public static final String ParamSelectedSortOrder = "SelectedSortOrder";
		public static final String ParamSortByDate = "SortByDate";
		public static final String ParamSortByType = "SortByType";
		
		public static final String TypeMarker = "TYPES";
		public static final String UserMarker = "USERS";
		public static final String StatusMarker = "STATUS";
		
		public void doGet(HttpServletRequest request,
					HttpServletResponse response)
					throws ServletException, IOException {
		    
		    response.setContentType("text/html");
			if (!SMAuthenticate.authenticateSMCPCredentials(
					request, 
					response, 
					getServletContext(),
					SMSystemFunctions.SMCriticaldatesreport
				)
			){
				return;
			}
		    PrintWriter out = response.getWriter();
			
		    //Get the session info:
		    HttpSession CurrentSession = request.getSession(true);
		    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
		    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		    String title = "Critical Date Report";
		    String subtitle = "listing criterias";
		    
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		    out.println(SMUtilities.getJQueryIncludeString());
			//Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		    String sSQL = "";
		    try {
	        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCriticalDateReportGenerate\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	        	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='smcontrolpanel.SMCriticalDateReportCriteriaSelection'>");
	        	

	    	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    		if (! sWarning.equalsIgnoreCase("")){
	    			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
	    		}
	        	
	        	out.println("<TABLE BORDER=10 CELLPADDING=10 WIDTH=80%>");
	        	
	        	//Resolved/Unresolved
	        	out.println("<TR><TD ALIGN=CENTER><H3>Status </H3></TD><TD>");
	        	out.println("<INPUT TYPE=\"CHECKBOX\" "
	        				+ "ID=\"" + ParamIncludeUnresolved + "\" "
	        				+ "NAME=\"" + StatusMarker + "0" + "\" "
	        				+ "CHECKED/> Unresolved ");      
	        	
	        	out.println("<INPUT TYPE=\"CHECKBOX\" "
	        				+ "ID=\"" + ParamIncludeResolved + "\" "
	        				+ "NAME=\"" + StatusMarker + "1" + "\" "
	        				+ "/> Resolved ");	        	
	        	out.println("</TD></TR>");
	        	
	        	//Type
	        	out.println("<TR><TD ALIGN=CENTER><H3>Type </H3></TD><TD>");
	        	
	        	out.println("<INPUT TYPE=\"CHECKBOX\" "
	        				+ "NAME=\"" + TypeMarker + Integer.toString(SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) + "\" "
	        				+ "CHECKED/> Orders ");
	        	
	        	//out.println("<INPUT TYPE=\"CHECKBOX\" "
        		//			+ "NAME=\"" + TypeMarker + Integer.toString(SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) + "\" "
        		//			+ "/> Sales Lead ");
	        	
	        	out.println("<INPUT TYPE=\"CHECKBOX\" "
    					+ "NAME=\"" + TypeMarker + Integer.toString(SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) + "\" "
    					+ "CHECKED /> Purchase Order ");
	        	

	        	out.println("</TD></TR>");
	        	
	        	//Select responsible users
	        	out.println("<TR><TD ALIGN=CENTER><H3>Responsible Person </H3></TD><TD>");
	        	
				out.println("<input type=\"button\" name=\"select-all\" id=\"select-all\" value=\"CHECK All users\" onclick=\"checkall()\">"); 
				out.println("&nbsp;<input type=\"button\" name=\"unselect-all\" id=\"unselect-all\" value=\"UNCHECK All users\" onclick=\"uncheckall()\"><BR><BR>"); 
				
	        	//Select statement to get all critical date users AND determine users that have ALL resolved critical dates (old users in most cases)
				sSQL = "SELECT DISTINCT " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lresponsibleuserid
						+ ", " + "UNRESOLVED." + SMTablecriticaldates.lresponsibleuserid 
					+ " FROM " + SMTablecriticaldates.TableName
					+ " LEFT JOIN ("
					+ " SELECT DISTINCT " + SMTablecriticaldates.lresponsibleuserid
					+ " FROM " + SMTablecriticaldates.TableName
					+ " WHERE " + SMTablecriticaldates.sResolvedFlag + "=" + "0"
					+ "  ) AS UNRESOLVED"
					+ " ON " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lresponsibleuserid 
					+ " = " + "UNRESOLVED." + SMTablecriticaldates.lresponsibleuserid 

					+ " ORDER BY " + SMTablecriticaldates.sresponsibleuserfullname
					;
				//out.println(sSQL);
				ResultSet rsResponsibleUsers = clsDatabaseFunctions.openResultSet(sSQL, 
														 getServletContext(),
														 sDBID,
														 "MySQL",
														 this.toString() + ".doPost - User: " + sUserID
														 + " - "
														 + sUserFullName
														 );
				ArrayList<String> sResponsibleUsersList = new ArrayList<String>(0);
				ArrayList<String> sResponsibleAllResolvedUsersList = new ArrayList<String>(0);
		
				boolean bCheckAllUsers;
				if (request.getParameter("CheckAllUsers") != null){
					bCheckAllUsers = new Boolean(request.getParameter("CheckAllUsers"));
				}else{
					bCheckAllUsers = false;
				}
				
				String sResponsibleUserID = "0";
				String sResponsibleUserFullName = "";
				boolean bAllResolved = false;
				while (rsResponsibleUsers.next()){
					bAllResolved = rsResponsibleUsers.getLong("UNRESOLVED." + SMTablecriticaldates.lresponsibleuserid) == 0;
					sResponsibleUserID = Long.toString(rsResponsibleUsers.getLong(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lresponsibleuserid));
					//Get the users full name from the user table in case it has changed
					sResponsibleUserFullName = SMUtilities.getFullNamebyUserID(
							sResponsibleUserID,  
							getServletContext(), 
							sDBID, 
							this.toString() + ".doPost - User: " + sUserID
							 + " - "
							 + sUserFullName);
					if(bAllResolved && sResponsibleUserID.compareToIgnoreCase("0") != 0) {
						sResponsibleAllResolvedUsersList.add((String) "<INPUT TYPE=CHECKBOX" + (bCheckAllUsers?" checked":"")  
			        			+ " ID=\"RESOLVEDUSER\" NAME=\"" + UserMarker 
			        			+ sResponsibleUserID 
			        			+ "\">&nbsp;" 
			        			+ sResponsibleUserFullName.trim().replace(" ", "&nbsp;")
			        			);					
					}else {
						sResponsibleUsersList.add((String) "<INPUT TYPE=CHECKBOX" + (bCheckAllUsers?" checked":"")  
			        			+ " ID=\"UNRESOLVEDUSER\" NAME=\"" + UserMarker 
			        			+ sResponsibleUserID 
			        			+ "\">&nbsp;" 
			        			+ sResponsibleUserFullName.trim().replace(" ", "&nbsp;")
			        			);
					}					
		    	}
				rsResponsibleUsers.close();			   
				
				if(sResponsibleUsersList.size() != 0) {
					out.println(SMUtilities.Build_HTML_Table(4, sResponsibleUsersList, 100, 0, true ,true));		 
				}else {
					out.println("There are no users with unresolved critical dates. ");
				}
				if(sResponsibleAllResolvedUsersList.size() != 0) {
					out.println("<div style= \"display: none;\" class=\"RESOLVEDUSERS\">");
					out.println("<hr><font size=\"1\">All Resolved </font>");
			        out.println(SMUtilities.Build_HTML_Table(4, sResponsibleAllResolvedUsersList, 100, 0, true ,true));
			        out.println("</div>");
				}
				
	    		out.println("<BR>");
	    		out.println("</TD></TR>");
	        	
	    		
	    		//Select 'Assigned By':
				sSQL = "SELECT DISTINCT " + SMTablecriticaldates.lassignedbyuserid	
						+ " FROM " + SMTablecriticaldates.TableName
						+ " ORDER BY " + SMTablecriticaldates.sassignedbyuserfullname
						;
				//out.println(sSQL);
				ResultSet rsAssignedUsers = clsDatabaseFunctions.openResultSet(sSQL, 
														 getServletContext(),
														 sDBID,
														 "MySQL",
														 this.toString() + ".doPost - User: " + sUserID
														 + " - "
														 + sUserFullName
														 );
				ArrayList<String> sAssignedByList = new ArrayList<String>(0);
				ArrayList<String> sAssignedByNameList = new ArrayList<String>(0);

				sAssignedByList.add("");
				sAssignedByNameList.add("*** ANYONE ***");
				String sAssignedUserID = "0";
				String sAssignedUserFullName = "";
				while (rsAssignedUsers.next()){
					sAssignedUserID = Long.toString(rsAssignedUsers.getLong(SMTablecriticaldates.lassignedbyuserid));
					//Get the users full name from the user table in case it has changed
					sAssignedUserFullName = SMUtilities.getFullNamebyUserID(
							sAssignedUserID,  
							getServletContext(), 
							sDBID, 
							this.toString() + ".doPost - User: " + sUserID
							 + " - "
							 + sUserFullName);
					
					sAssignedByList.add(sAssignedUserID);
					sAssignedByNameList.add(sAssignedUserFullName);
		    	}
				rsAssignedUsers.close();
		        
	        	out.println("<TR><TD ALIGN=CENTER><H3>Assigned by</H3></TD>");
	    		out.println("<TD>");
		        out.println(clsCreateHTMLFormFields.TDDropDownBox(
		        		SMCriticalDateEntry.ParamAssignedbyUserID, 
		        		sAssignedByList,
		        		sAssignedByNameList,
		        		"")
		        );
		        out.println("</TD></TR>");
		        
	        	//Select date range
	    		
	        	out.println("<TR><TD ALIGN=CENTER><H3>Date Range</H3></TD>");
	    		out.println("<TD>");
	    		out.println(clsCreateHTMLFormFields.TDTextBox(
        				"StartingDate", 
        				"1/1/2000", 
        				10, 
        				10, 
        				""
        				) 
        				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
        				);
	    		out.println("&nbsp;&nbsp;To&nbsp;&nbsp;");
	    		out.println(clsCreateHTMLFormFields.TDTextBox(
	        				"EndingDate", 
	        				clsDateAndTimeConversions.now("M/d/yyyy"), 
	        				10, 
	        				10, 
	        				""
	        				) 
	        				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
	        				);
	    		out.println("</TD></TR>");
	    		
	    		//Select sorting method
	        	out.println("<TR><TD ALIGN=CENTER><H3>Sort By </H3></TD>");
	        		out.println("<TD><SELECT NAME=\"" + ParamSelectedSortOrder + "\">");
        				out.println("<OPTION VALUE=\"" + ParamSortByDate + "\">Date ");
	        			out.println("<OPTION VALUE=\"" +ParamSortByType + "\">Record Type");
	        			out.println("</SELECT>");
	        		out.println("&nbsp;&nbsp;*All results will be sorted by the responsible person first.</TD>");
	        	out.println("</TR>");
	    		
		        out.println("</TABLE><BR><BR>");
		        
	        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	        	out.println("</FORM>");
	        	
	        	out.println(getScript());
			    
		    } catch (Exception ex) {
		        // handle any errors
		    	System.out.println("Error in SMCriticalDateReportCriteriaSelection class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        out.println("<BR>Error - in SQL: " + sSQL + " - " + ex.getMessage());
		    }
	 
		    out.println("</BODY></HTML>");
		}
		private String getScript() {
			String s = "<script>"
					+ "$( document ).ready(function() {\n" 
					+ "  		if ($(" + ParamIncludeResolved + ").is(':checked')){\n" 
					+ "    		$('.RESOLVEDUSERS').show();\n" 
					+ "		}\n"
					+ "		$('#" + ParamIncludeResolved + "').click(function(event) {   \n" 
					+ "  			$('.RESOLVEDUSERS').slideToggle('fast');                     \n" 
					+ "		});\n"
					+ "});\n"
					+ "\n"
					
					+ "function checkall() {   \n"  
					+ "        $('#RESOLVEDUSER, #UNRESOLVEDUSER').each(function() {\n" 
					+ "            this.checked = true;                        \n" 
					+ "        });\n" 
					+ "};\n"	
					
					+ "function uncheckall() {   \n"  
					+ "        $('#RESOLVEDUSER, #UNRESOLVEDUSER').each(function() {\n" 
					+ "            this.checked = false;                       \n" 
					+ "        });\n" 
					+ "    }\n" 
					
					+ "</script>";
		
			return s;
		}
	}
