package TimeCardSystem;

import java.io.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;

public class LeaveAdjustmentTypeSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {
	    	int iTid = Integer.parseInt(request.getParameter("Tid"));
	    	String sSQL;
	    	//construct time stamp
	    	String sDateString = request.getParameter("SelectedYear") + "-" +
	    						 request.getParameter("SelectedMonth") + "-" +
	    						 request.getParameter("SelectedDay");
	    	
	    	//calculate the pay type sum and employee status sum
			Enumeration<?> paramNames = request.getParameterNames();
    		int iPayTypesSum = 0;
    		int iStatusSum = 0;
    		while(paramNames.hasMoreElements()) {
    			String s = paramNames.nextElement().toString();
    			try{
    				int iSwitch = Integer.parseInt(s.substring(1, 2));

        			switch (iSwitch) {
    	                case 1: try{
                    				if (iPayTypesSum >= 0){
                    					iPayTypesSum = iPayTypesSum + (int)Math.pow(2, Double.parseDouble(s.substring(3)));
                    				}
                    				break;
                    			}catch(NumberFormatException ex){
                    				//all department
                    				iPayTypesSum = -1;
                    				break;
                    			}
    	                case 2: try{
    		        				if (iStatusSum >= 0){
    		        					iStatusSum = iStatusSum + (int)Math.pow(2, Double.parseDouble(s.substring(3)));
    		        				}
    		        				break;
    		        			}catch(NumberFormatException ex){
    		        				//all department
    		        				iStatusSum = -1;
    		        				break;
    		        			}
        			}
    			}catch(Exception ex){
    				//System.out.println("Not a parameter we care about at this point.");
    			}
    		}
    		
    		//get award type
    		double dAwardType = Double.parseDouble(request.getParameter("AwardType"));
    		if (dAwardType == 0){
    			dAwardType = Double.parseDouble(request.getParameter("AccrueHour"));
    		}

	    	if (iTid == 0){
	    		//new type, check to see if the supplied ID is taken or not.
		    	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Type_Info_SQL(request.getParameter("TypeID"));
		    	ResultSet rsLeaveAdjustmentTypeID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
	    		if (rsLeaveAdjustmentTypeID.next()){
	    			//<BR>the ID is not valid, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The leave adjustment type ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");	    			
	    		}else{
	    			//the selected adjustment type id is valid, save it.");
	    			if (Save_Leave_Adjustment_Type_Info(iTid,
							    					    Integer.parseInt(request.getParameter("TypeID")), 
												        request.getParameter("TypeTitle"), 
												        request.getParameter("TypeDesc"),
												        sDateString,
												        iPayTypesSum,
												        iStatusSum,
												        Double.parseDouble(request.getParameter("MinHourWorked")),
												        request.getParameter("AwardPeriod"),
												        dAwardType,
												        Integer.parseInt(request.getParameter("CarriedOver")),
												        Double.parseDouble(request.getParameter("MaxHourAllowed")),
												        Integer.parseInt(request.getParameter("PaidLeave")),
												        getServletContext(), 
												        (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    				//saving successful;
	    				out.println ("<BR>");
	    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
	    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeList'>");
	    	        	//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
					}else{
						//saving failed;
						out.println("<BR><BR>Saving pay type information failed!!<BR>");
						out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentList>Click here to return to leave adjustment type list.</A>");
					}
	    		}
		    	//clsoe resultset
	    		rsLeaveAdjustmentTypeID.close();
		    	
	    	}else{
	    		//existing department. proceed to save changes.
	    		if (Save_Leave_Adjustment_Type_Info(iTid,
												    Integer.parseInt(request.getParameter("TypeID")), 
											        request.getParameter("TypeTitle"), 
											        request.getParameter("TypeDesc"),
											        sDateString,
											        iPayTypesSum,
											        iStatusSum,
											        Double.parseDouble(request.getParameter("MinHourWorked")),
											        request.getParameter("AwardPeriod"),
											        dAwardType,
											        Integer.parseInt(request.getParameter("CarriedOver")),
											        Double.parseDouble(request.getParameter("MaxHourAllowed")),
											        Integer.parseInt(request.getParameter("PaidLeave")),
											        getServletContext(), 
											        (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
					//saving successful
					out.println ("<BR>");
					out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeList'>");
					//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed
					out.println("<BR><BR>Saving leave adjustment type information failed!!<BR>");
					out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeList>Click here to return to leave adjustment type list.</A>");
				}
	    	}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Error: " + ex.getMessage());
	    	//out.println(TimeCardUtilities.Session_Expire_Handling());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static boolean Save_Leave_Adjustment_Type_Info(int iSessionTID,
													       int iTypeID, 
													       String sTypeTitle,
													       String sTypeDesc,
													       String sEffectiveDate,
													       int iEEPType,
													       int iEEStatus,
													       double dMinHour,
													       String sAwardPeriod,
													       double dAwardType,
													       int iCarriedOver,
													       double dMaxHour,
													       int iPaidLeave,
													       ServletContext context, 
													       String conf
												    	   ){
		
		String sSQL;
		
		//	save department info
		try{
	        if (iSessionTID == 0){
	        	sSQL = TimeCardSQLs.Get_Insert_Leave_Adjustment_Type_SQL(iTypeID, 
			        												     sTypeTitle, 
			        												     sTypeDesc,
			        												     sEffectiveDate,
			        												     iEEPType,
			        												     iEEStatus,
			        												     dMinHour,
			        												     sAwardPeriod,
			        												     dAwardType,
			        												     iCarriedOver,
			        												     dMaxHour,
			        												     iPaidLeave
			        												     );
											
	        }else{
		        sSQL = TimeCardSQLs.Get_Update_Leave_Adjustment_Type_SQL(iSessionTID, 
			        												     sTypeTitle, 
												        		  	     sTypeDesc,
				       												     sEffectiveDate,
				    												     iEEPType,
				    												     iEEStatus,
				    												     dMinHour,
				    												     sAwardPeriod,
				    												     dAwardType,
				    												     iCarriedOver,
				    												     dMaxHour,
			        												     iPaidLeave);
		        	
	        }      
	        if (clsDatabaseFunctions.executeSQL(sSQL, context, conf)){ 
	        	return true;
	        }else{
	        	return false;
	        }
		}catch (Exception ex) {
	    	System.out.println("<BR><BR>Error in Save_Leave_Adjustment_Type_Info!!<BR>");
	    	System.out.println("Exception: " + ex.getMessage() + "<BR>");
	    	return false;
	    }
	}
}
