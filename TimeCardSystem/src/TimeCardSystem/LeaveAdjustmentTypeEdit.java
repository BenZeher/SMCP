package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

/* Servlet that insert In-Time records into the the time entry table.*/

public class LeaveAdjustmentTypeEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Leave Type Info";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {
	    	
	    	String sSQL;

	    	/*
				+--------------------------+-------------+------+-----+--------------+-------+
				| Field                    | Type        | Null | Key | Default      | Extra |
				+--------------------------+-------------+------+-----+--------------+-------+
				| iTypeID                  | int(11)     |      | PRI | 0            |       |
				| sTypeTitle               | varchar(20) | YES  |     |              |       |
				| sTypeDesc                | text        | YES  |     | NULL         |       |
				| dtEffectiveDate          | date        | YES  |     | 0000-00-00   |       |
				| iEligibleEmployeePayType | int(11)     | YES  |     | 0            |       |
				| iEligibleEmploeeStatus   | int(11)     | YES  |     | 0            |       |
				| dMinimumHourWorked       | double      | YES  |     | 0            |       |
				| sAwardPeriod             | varchar(12) | YES  |     | calendaryear |       |
				| dAwardType               | double      | YES  |     | 0            |       |
				| iCarriedOver             | int(1)      | YES  |     | 0            |       |
				| dMaximumHourAvailable    | double      | YES  |     | 0            |       |
				+--------------------------+-------------+------+-----+--------------+-------+
 			*/
	    	if (Integer.parseInt(request.getParameter("Type")) == 0){
	    		//new Leave Adjustment Type
	        	out.println ("<BR><FONT SIZE=4>Leave Type Information: <B>New Type</B></FONT><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Leave Adjustment Type ID
	        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3> Choose an integer between 1 - 16 only</TD></TR>");
	        	//Leave Adjustment Type Title
	        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3> Up to 3 alpha-numerics</TD></TR>");
	        	//Leave Adjustment Type Description
	        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=40 MAXLENGTH=150> Up to 150 alpha-numerics</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"0\">");
	        	
	        	//Effective Date
	        	out.println("<TR><TD><B> Effective Date: </B></TD>");
	        	Calendar c = Calendar.getInstance();
	        	c.setTimeInMillis(System.currentTimeMillis());
	        	
	        	out.println("<TD>");
        		out.println("Month <SELECT NAME=\"SelectedMonth\">");
	        		for (int i=1; i<=12;i++){
	        			if (i == c.get(Calendar.MONTH) + 1){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
		        	
	        	out.println("Day <SELECT NAME=\"SelectedDay\">");
		    		for (int i=1; i<=31;i++){
	        			if (i == c.get(Calendar.DAY_OF_MONTH)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("Year <SELECT NAME=\"SelectedYear\">");
		    		for (int i=1970; i<=2069;i++){
	        			if (i == c.get(Calendar.YEAR)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("</TD>");
	        	
	        	//Eligible Pay Type
	        	out.println ("<TR><TD><B>Eligible Pay Types</B></TD>");
		        sSQL = TimeCardSQLs.Get_Pay_Type_Info_SQL();
		        ResultSet rsPayTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        	out.println("<TD>");
	        	
	        	ArrayList<String> alEmployeePayTypes = new ArrayList<String>(0);
	        	//add one entry for "all department"
	        	alEmployeePayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!ALLTypes VALUE=0><B>All Types</B>");
	        	while (rsPayTypes.next()){
	        		alEmployeePayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt("iTypeID") + " VALUE=0>" + rsPayTypes.getString("sTypeTitle") + " - " + rsPayTypes.getString("sTypeDesc"));
	        	}
	        	rsPayTypes.close();
	        	out.println(TimeCardUtilities.Build_HTML_Table(3, alEmployeePayTypes, 0, false));
	        	out.println("</SELECT></TD></TR>");

	        	//Eligible Employee Status
	        	out.println("<TR><TD><H3>Eligible Employee Status</H3></TD>");
	        	//get recordset of employee status
	        	sSQL = TimeCardSQLs.Get_Employee_Status_Info_SQL();	
	        	ResultSet rsEmployeeStatus = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        	out.println ("<TD>");
	        	ArrayList<String> alEmployeeStatus = new ArrayList<String>(0);
	        	//add one entry for "all department"
	        	alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!ALLStatus VALUE=0><B>All Statuses</B>");
	        	while (rsEmployeeStatus.next()){
	        		alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt("iStatusID") + " VALUE=0>" + rsEmployeeStatus.getString("sStatusTitle") + " - " + rsEmployeeStatus.getString("sStatusDesc"));
	        	}
	        	rsEmployeeStatus.close();
	        	out.println(TimeCardUtilities.Build_HTML_Table(3, alEmployeeStatus, 0, false));
		        out.println ("</TD></TR>");
		        
		        //Minimum hour worked
		        out.println ("<TR><TD><B>Minimum Hour Worked</B></TD><TD>" + 
		  	      "<INPUT TYPE=TEXT NAME=\"MinHourWorked\" SIZE=7 MAXLENGTH=7 VALUE=\"0.00\"> Number only, 0 for no minimum</TD></TR>");
		        
		        //Award Period
		        out.println ("<TR><TD><B>Award Period</B></TD><TD><SELECT NAME=\"AwardPeriod\">");
		        out.println("<OPTION VALUE=\"calendaryear\"> Calendar Year");		        
		        out.println("<OPTION VALUE=\"fiscalyear\"> Fiscal Year");
		        out.println("<OPTION VALUE=\"employeeyear\"> Employee Year");
		        out.println("</SELECT></TD></TR>");
		        
		        //Award type
		        out.println ("<TR><TD><B>Award Type</B></TD><TD>");
		        out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"-1\"> Lump Sum <BR>");		        
		        out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"0\"> Accrue " +
		        			"<INPUT TYPE=TEXT NAME=\"AccrueHour\" SIZE=7 MAXLENGTH=7 VALUE=0> Hour(s)");
		        out.println("</SELECT></TD></TR>");

		        //Carried Over?
	        	out.println ("<TR><TD><B>Carried Over?</B></TD><TD>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=1>Yes<BR>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=0 CHECKED>No<BR>");
	        	
		        //Max Hours
		        out.println ("<TR><TD><B>Maximum Hour Allowed</B></TD><TD>" + 
		  	      "<INPUT TYPE=TEXT NAME=\"MaxHourAllowed\" SIZE=7 MAXLENGTH=7 VALUE=\"0\"> Number only, 0 for unlimited</TD></TR>");

		        //Paid Leave?
	        	out.println ("<TR><TD><B>Paid Leave?</B></TD><TD>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=1>Yes<BR>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=0 CHECKED>No<BR>");
	        	
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing type, get Leave Adjustment type info
		        sSQL = TimeCardSQLs.Get_Leave_Adjustment_Type_Info_SQL(request.getParameter("Type"));
		        ResultSet rsTypeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        if (rsTypeInfo.next()){
			        out.println ("<BR><FONT SIZE=4>Leave Type Information: <B>" + rsTypeInfo.getString("sTypeTitle") + " - " + rsTypeInfo.getString("sTypeDesc") + "</B></FONT><BR>");
		        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeSave\">");
		        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	//Leave Adjustment Type ID, not editable.
		        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3 VALUE=" + rsTypeInfo.getInt("iTypeID")+ "></TD></TR>");
		        	//Leave Adjustment Type Title
		        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3 VALUE=\"" + rsTypeInfo.getString("sTypeTitle")+ "\">Up to 3 alpha-numerics</TD></TR>");
		        	//Leave Adjustment Type Description
		        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
		        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=40 MAXLENGTH=150 VALUE=\"" + rsTypeInfo.getString("sTypeDesc")+ "\">Up to 150 alpha-numerics</TD></TR>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");

		        	//Effective Date
		        	out.println("<TR><TD><B> Effective Date: </B></TD>");
		        	Calendar c = Calendar.getInstance();
		        	if (rsTypeInfo.getString("dtEffectiveDate").compareTo("0000-00-00") == 0){
		        		c.setTimeInMillis(System.currentTimeMillis());
		        	}else{
		        		c.setTime(rsTypeInfo.getDate("dtEffectiveDate"));
		        	}
		        	out.println("<TD>");
		        	
	        		out.println("Month <SELECT NAME=\"SelectedMonth\">");
		        		for (int i=1; i<=12;i++){
		        			if (i == c.get(Calendar.MONTH) + 1){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
		        		}
		        	out.println("</SELECT>");
			        	
		        	out.println("Day <SELECT NAME=\"SelectedDay\">");
			    		for (int i=1; i<=31;i++){
		        			if (i == c.get(Calendar.DAY_OF_MONTH)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("Year <SELECT NAME=\"SelectedYear\">");
			    		for (int i=1970; i<=2069;i++){
		        			if (i == c.get(Calendar.YEAR)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("</TD></TR>");
		        	
		        	//Eligible Employee Types
		        	out.println("<TR><TD><B>Eligible Pay Types</B></TD>");
		        	//get recordset of employee types
		        	sSQL = TimeCardSQLs.Get_Pay_Type_Info_SQL();	
		        	ResultSet rsPayTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        	
		        	//display available status. 
		        	out.println ("<TD>");
		        	ArrayList<String> alPayTypes = new ArrayList<String>(0);
		        	//add one entry for "all department"
		        	if (rsTypeInfo.getInt("iEligibleEmployeePayType") < 0){
		        		alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!ALLTypes VALUE=\"0\" CHECKED><B>All Types</B>");
		        		//fill in all the other options with "NOT CHECKED"
		        		while (rsPayTypes.next()){
			        		alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt("iTypeID") + " VALUE=0>" + rsPayTypes.getString("sTypeTitle") + " - " + rsPayTypes.getString("sTypeDesc"));
			        	}
		        	}else{
		        		alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!ALLTypes VALUE=\"0\"><B>All Types</B>");
		        		while (rsPayTypes.next()){
			        		if (TimeCardUtilities.TypeCombinationCheck(rsTypeInfo.getInt("iEligibleEmployeePayType"), rsPayTypes.getInt("iTypeID"))){
			        			alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt("iTypeID") + " VALUE=0 CHECKED>" + rsPayTypes.getString("sTypeTitle") + " - " + rsPayTypes.getString("sTypeDesc"));
			        		}else{
			        			alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt("iTypeID") + " VALUE=0>" + rsPayTypes.getString("sTypeTitle") + " - " + rsPayTypes.getString("sTypeDesc"));
			        		}
			        	}
		        	}
		        	rsPayTypes.close();
		        	out.println(TimeCardUtilities.Build_HTML_Table(3, alPayTypes, 0, false));
			        out.println ("</TD></TR>");
		        	
		        	
		        	//Eligible Employee Status
		        	out.println("<TR><TD><B>Eligible Employee Status</B></TD>");
		        	//get recordset of employee status
		        	sSQL = TimeCardSQLs.Get_Employee_Status_Info_SQL();	
		        	ResultSet rsEmployeeStatus = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        	
		        	//display available status. 
		        	out.println ("<TD>");
		        	ArrayList<String> alEmployeeStatus = new ArrayList<String>(0);
		        	//add one entry for "all department"
		        	if (rsTypeInfo.getInt("iEligibleEmployeeStatus") < 0){
		        		alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!ALLStatus VALUE=0 CHECKED><B>All Statuses</B>");
		        		while (rsEmployeeStatus.next()){
			        		alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt("iStatusID") + " VALUE=0>" + rsEmployeeStatus.getString("sStatusTitle") + " - " + rsEmployeeStatus.getString("sStatusDesc"));
			        	}
		        	}else{
			        	alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!ALLStatus VALUE=0><B>All Statuses</B>");
			        	while (rsEmployeeStatus.next()){
			        		if (TimeCardUtilities.TypeCombinationCheck(rsTypeInfo.getInt("iEligibleEmployeeStatus"), rsEmployeeStatus.getInt("iStatusID"))){
			        			alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt("iStatusID") + " VALUE=0 CHECKED>" + rsEmployeeStatus.getString("sStatusTitle") + " - " + rsEmployeeStatus.getString("sStatusDesc"));
			        		}else{
			        			alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt("iStatusID") + " VALUE=0>" + rsEmployeeStatus.getString("sStatusTitle") + " - " + rsEmployeeStatus.getString("sStatusDesc"));
			        		}
			        	}
		        	}
		        	
		        	rsEmployeeStatus.close();
		        	
		        	out.println(TimeCardUtilities.Build_HTML_Table(3, alEmployeeStatus, 0, false));
			        out.println ("</TD></TR>");
			        
			        //Minimum hour worked
			        out.println ("<TR><TD><B>Minimum Hour Worked</B></TD><TD>" + 
			  	      "<INPUT TYPE=TEXT NAME=\"MinHourWorked\" SIZE=7 MAXLENGTH=7 VALUE=" + TimeCardUtilities.RoundHalfUp(rsTypeInfo.getDouble("dMinimumHourWorked"), 2) + "> Number only, 0 for no minimum</TD></TR>");
			        
			        //Award Period
			        out.println ("<TR><TD><B>Award Period</B></TD><TD><SELECT NAME=\"AwardPeriod\">");
			        if (rsTypeInfo.getString("sAwardPeriod").compareTo("calendaryear") == 0){
			        	out.println("<OPTION VALUE=\"calendaryear\" SELECTED> Calendar Year");
			        }else{
			        	out.println("<OPTION VALUE=\"calendaryear\"> Calendar Year");
			        }
			        if (rsTypeInfo.getString("sAwardPeriod").compareTo("fiscalyear") == 0){
			        	out.println("<OPTION VALUE=\"fiscalyear\" SELECTED> Fiscal Year");
			        }else{
			        	out.println("<OPTION VALUE=\"fiscalyear\"> Fiscal Year");
			        }
			        if (rsTypeInfo.getString("sAwardPeriod").compareTo("employeeyear") == 0){
			        	out.println("<OPTION VALUE=\"employeeyear\" SELECTED> Employee Year");
			        }else{
			        	out.println("<OPTION VALUE=\"employeeyear\"> Employee Year");
			        }
			        out.println("</SELECT></TD></TR>");
			        
			        //Award Period
			        out.println ("<TR><TD><B>Award Type</B></TD><TD>");
			        
			        if (rsTypeInfo.getDouble("dAwardType") < 0){
			        	out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"-1\" CHECKED> Lump Sum <BR>");		        
				        out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"0\"> Accrue " +
				        			"<INPUT TYPE=TEXT NAME=\"AccrueHour\" SIZE=7 MAXLENGTH=7 VALUE=0> Hour(s)");
			        }else{
			        	out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"-1\"> Lump Sum <BR>");		        
				        out.println("<INPUT TYPE=\"RADIO\" NAME=\"AwardType\" VALUE=\"0\" CHECKED> Accrue " +
				        			"<INPUT TYPE=TEXT NAME=\"AccrueHour\" SIZE=7 MAXLENGTH=7 VALUE=" + rsTypeInfo.getDouble("dAwardType") + "> Hour(s)");
			        }
			        out.println("</SELECT></TD></TR>");
			        
			        //Carried Over?
		        	out.println ("<TR><TD><B>Carried Over?</B></TD><TD>");
		        	if (rsTypeInfo.getInt("iCarriedOver") == 1){
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=1 CHECKED>Yes<BR>");
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=0>No<BR>");
		        	}else{
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=1>Yes<BR>");
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"CarriedOver\" VALUE=0 CHECKED>No<BR>");
		        	}
		        	
			        //Max Hours
			        out.println ("<TR><TD><B>Maximum Hour Allowed</B></TD><TD>" + 
					  	      "<INPUT TYPE=TEXT NAME=\"MaxHourAllowed\" SIZE=7 MAXLENGTH=7 VALUE=" + TimeCardUtilities.RoundHalfUp(rsTypeInfo.getDouble("dMaximumHourAvailable"), 2) + "> Number only, 0 for unlimited</TD></TR>");

			        //Paid Leave?
		        	out.println ("<TR><TD><B>Paid Leave?</B></TD><TD>");
		        	if (rsTypeInfo.getInt("iPaidLeave") == 1){
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=1 CHECKED>Yes<BR>");
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=0>No<BR>");
		        	}else{
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=1>Yes<BR>");
			        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"PaidLeave\" VALUE=0 CHECKED>No<BR>");
		        	}
					        
		        	out.println ("</Table>");
		        	
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println ("</FORM>");
		        	//Option to	delete current record.
		        	//hardwire "vacation" in the program.
		        	if (Integer.parseInt(request.getParameter("Type")) != 1){
			        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeRemove\">");
			        	out.println("<INPUT TYPE=HIDDEN NAME=\"Type\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");
			        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
			        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this pay type.");
			        	out.println("</FORM>");
		        	}
		        }else{
		        	//no leave adjustment type returned.
		        }
		        rsTypeInfo.close();
	    	}
	    } catch (SQLException ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	        out.println("SQLState: " + ex.getSQLState() + "<BR>");
	        out.println("SQL: " + ex.getErrorCode() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}