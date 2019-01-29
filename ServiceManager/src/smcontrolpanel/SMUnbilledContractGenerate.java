package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMUnbilledContractGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sReportTitle = "Unbilled Orders Report";
    
	@Override
	public void doPost(HttpServletRequest request,
					   HttpServletResponse response)throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    	String title = sReportTitle;
    	String subtitle = "";

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
	    //Selected sales groups and service types
    	ArrayList<String> alSelectedSalesGroups = new ArrayList<String>(0);
    	ArrayList<String> alSelectedServiceTypes = new ArrayList<String>(0);
    	ArrayList<String> alSelectedSalespeople = new ArrayList<String>(0);
		Enumeration<?> e = request.getParameterNames();
    	String sParam = "";
    	String sType = "";
    	String sGroup = "";
    	String sSalesperson = "";
    	while (e.hasMoreElements()){
    		sParam = (String) e.nextElement();
    		if(clsStringFunctions.StringLeft(
    				sParam, 
    				SMUnbilledContractReportSelection.SERVICE_TYPE_PARAMETER.length()).compareToIgnoreCase(
    					SMUnbilledContractReportSelection.SERVICE_TYPE_PARAMETER) == 0){
    			//selected service type
    			if (request.getParameter(sParam) != null){
    				sType = clsStringFunctions.StringRight(
    					sParam, sParam.length() - SMUnbilledContractReportSelection.SERVICE_TYPE_PARAMETER.length());
    				alSelectedServiceTypes.add(sType);
    			}
    		}else if (clsStringFunctions.StringLeft(
    				sParam, 
    				SMUnbilledContractReportSelection.SALESGROUP_PARAMETER.length()).compareToIgnoreCase(
    					SMUnbilledContractReportSelection.SALESGROUP_PARAMETER) == 0){
    			//select sales group
    			if (request.getParameter(sParam) != null){
    				sGroup = clsStringFunctions.StringRight(
    					sParam, sParam.length() - SMUnbilledContractReportSelection.SALESGROUP_PARAMETER.length());
    				alSelectedSalesGroups.add(sGroup);
    			}
			}else if (clsStringFunctions.StringLeft(
					sParam, 
					SMUnbilledContractReportSelection.SALESPERSON_PARAMETER.length()).compareToIgnoreCase(
						SMUnbilledContractReportSelection.SALESPERSON_PARAMETER) == 0){
				//select sales group
				if (request.getParameter(sParam) != null){
					sSalesperson = clsStringFunctions.StringRight(
						sParam, sParam.length() - SMUnbilledContractReportSelection.SALESPERSON_PARAMETER.length());
					alSelectedSalespeople.add(sSalesperson);
				}
			}
    	}
    	Collections.sort(alSelectedSalespeople);
    	Collections.sort(alSelectedSalesGroups);
    	String s;
    	
    	s = "<TABLE BORDER=0><TR>" +
				"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Sales Group(s):</B></FONT></TD>" 
    			+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" 
				+ getSelectedSalesGroups(alSelectedSalesGroups, sDBID) 
				+ "</FONT>" 
				+ "</TD>" 
				+ "</TR></TABLE>";
    	alCriteria.add(s);
    	
    	s = "<TABLE BORDER=0><TR>" 
    			+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Service Type(s):</B></FONT></TD>" 
    			+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>"
    			+ getSelectedServiceTypes(alSelectedServiceTypes, sDBID) 
    			+ "</FONT>"
    			+ "</TD>" 
    			+ "</TR></TABLE>";
    	alCriteria.add(s);
    	
    	//Selected Sort Order
    	//String sSortOrder = request.getParameter(SMUnbilledContractReportSelection.SORT_ORDER_PARAMETER);
    	//alCriteria.add("<FONT SIZE=2><B>Sorting by:</B>&nbsp;" + sSortOrder + "</FONT>");
        boolean bShowIndividualOrders = false;
        boolean bShowActive = false;
        boolean bShowStanding = false;
        boolean bShowWorkOrders = false;
        //boolean bShowStatistics = false;
        
	    if (request.getParameter(SMUnbilledContractReportSelection.SHOW_INDIVIDUAL_ORDERS_PARAMETER) != null){
	    	bShowIndividualOrders = true;
    	}else{
    		bShowIndividualOrders = false;
    	}
	    if (request.getParameter(SMUnbilledContractReportSelection.SHOW_WORKORDERS_PARAMETER) != null){
	    	bShowWorkOrders = true;
    	}else{
    		bShowWorkOrders = false;
    	}
	    if (request.getParameter(SMUnbilledContractReportSelection.SHOW_ACTIVE_PARAMETER) != null){
	    	bShowActive = true;
	    }else{
	    	bShowActive = false;
	    }
	    if (request.getParameter(SMUnbilledContractReportSelection.SHOW_STANDING_PARAMETER) != null){
	    	bShowStanding = true;
	    }else{
	    	bShowStanding = false;
	    }
	    //if (request.getParameter(SMUnbilledContractReportSelection.SHOW_STATISTICS_PARAMETER) != null){
	    //	bShowStatistics = true;
	    //}else{
	    //	bShowStatistics = false;
	    //}
	    String sJobStatus = "<FONT SIZE=2><B>Job Status:</B>&nbsp;";
	    if (bShowActive){
	    	sJobStatus += "Active";
	    	if (bShowStanding){
	    		sJobStatus += "/Standing";
	    	}
	    }else if (bShowStanding){
	    	sJobStatus += "Standing";
	    }
	    sJobStatus += "</FONT>";
	    alCriteria.add(sJobStatus);
	    
	    String sShowIndividualOrders = "<FONT SIZE=2><B>Show individual orders?:</B>&nbsp;";
	    if (bShowIndividualOrders){
	    	sShowIndividualOrders += "<B>YES</B>";
	    }else{
	    	sShowIndividualOrders += "<B>NO</B>";
	    }
	    sShowIndividualOrders += "</FONT>";
	    alCriteria.add(sShowIndividualOrders);
	    
	    String sShowWorkOrders = "<FONT SIZE=2><B>Show work orders?:</B>&nbsp;";
	    if (bShowWorkOrders){
	    	sShowWorkOrders += "<B>YES</B>";
	    }else{
	    	sShowWorkOrders += "<B>NO</B>";
	    }
	    sShowWorkOrders += "</FONT>";
	    alCriteria.add(sShowWorkOrders);
	    
	    //String sShowStatistics = "<FONT SIZE=2><B>Show statistics?:</B>&nbsp;";
	    //if (bShowStatistics){
	    //	sShowStatistics += "<B>YES</B>";
	    //}else{
	    //	sShowStatistics += "<B>NO</B>";
	    //}
	    //sShowStatistics += "</FONT>";
	    //alCriteria.add(sShowStatistics);
	    
	    //boolean bGenerateEmail = false;
	    //if(request.getParameter(SMUnbilledContractReportSelection.GENERATE_EMAIL_PARAMETER) != null){
	    //	bGenerateEmail = true;
	    //}
	    /*************END of PARAMETER list***************/

    	//print out report heading and selected criteria
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println("<TABLE BORDER=0 WIDTH=100%><TR>");
	    out.println("<TD ALIGN=LEFT VALIGN=TOP><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\">Return to user login</A>&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
	    out.println("</TR></TABLE><BR><BR>");
	    out.println(SMUtilities.Build_HTML_Table(
	    	3, 				//Number of Columns
	    	alCriteria,		//Criteria Array
			100,			//Width
			0,				//Border
			false,			//Equal Width?
			false)			//Vertical?
		);
	    
	    //Print the list of salespeople:
	    out.println("<FONT SIZE=2><B><U>Salespeople:</U></B></FONT>");
	    try {
			out.println(SMUtilities.Build_HTML_Table(
		    	3, 				//Number of Columns
		    	getSalespersonsWithNames(alSelectedSalespeople, sDBID),		//Criteria Array
				100,			//Width
				0,				//Border
				false,			//Equal Width?
				false)			//Vertical?
			);
		} catch (Exception e2) {
			out.println("<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>");
		}
	    out.println("<BR>");
	    SMUnbilledContractReport rpt = new SMUnbilledContractReport();
	    try {
			rpt.processReport(
				sDBID, 
				getServletContext(), 
				sUserID,
				sUserFirstName,
				sUserLastName,
				sCompanyName,
				alSelectedSalesGroups,
				alSelectedServiceTypes,
				alSelectedSalespeople,
				bShowActive,
				bShowStanding,
				bShowIndividualOrders,
				bShowWorkOrders,
				//bShowStatistics,
				out,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);

		} catch (Exception e1) {
			out.println("<BR><B><FONT COLOR=RED>Error [1429045429] printing report - " + e1.getMessage() + "</FONT></B><BR>");
			return;
		}
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
	    		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">Return to user login</A>");
	        	
	    out.println("<BR><A HREF=#> Back to Top</A>");

	    //out.println(s);
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	private String getSelectedServiceTypes(ArrayList<String> arServiceTypes, String sDBID){
		
		String sDesc = "";
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName 
			+ " ORDER BY " + SMTableservicetypes.sCode;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			String sType = "";
			while(rs.next()){
				sType = rs.getString(SMTableservicetypes.sCode);
				for (int i = 0; i < arServiceTypes.size(); i++){
					if(arServiceTypes.get(i).compareToIgnoreCase(sType) == 0){
						sDesc = sDesc + rs.getString(SMTableservicetypes.sName) + "<BR>";
					}
				}
			}
			sDesc = clsStringFunctions.StringLeft(sDesc, sDesc.length() - "<BR>".length());
			rs.close();
		}catch (SQLException e){
			sDesc = "Error [1429303811] COULD NOT READ SERVICE TYPES with SQL: '" + SQL + " - " + e.getMessage();
		}
		
		return sDesc;
	}
	private String getSelectedSalesGroups(ArrayList<String> arSalesGroups, String sDBID){
		
		String sDesc = "";
		
		String SQL = "SELECT * FROM " + SMTablesalesgroups.TableName 
			+ " ORDER BY " + SMTablesalesgroups.iSalesGroupId;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			int iGroup = 0;
			for (int i = 0; i < arSalesGroups.size(); i++){
				sDesc += arSalesGroups.get(i);
				String sSalesGroupDesc = " N/A <BR>";
				rs.beforeFirst();
				while(rs.next()){
					iGroup = rs.getInt(SMTablesalesgroups.iSalesGroupId);
					if(Integer.parseInt(arSalesGroups.get(i).toString()) == iGroup){
						sSalesGroupDesc = " " + rs.getString(SMTablesalesgroups.sSalesGroupDesc) + "<BR>";
					}
				}
				sDesc += sSalesGroupDesc;
			}
			sDesc = clsStringFunctions.StringLeft(sDesc, sDesc.length() - "<BR>".length());
			rs.close();
		}catch (SQLException e){
			sDesc = "Error [1429303812] COULD NOT READ SALES GROUPS with SQL: '" + SQL + " - " + e.getMessage();
		}
		
		return sDesc;
	}

	private ArrayList<String> getSalespersonsWithNames(ArrayList<String> arSalespersons, String sDBID) throws Exception{
		
		String sDesc = "";
		ArrayList <String>arrSalesPersonList = new ArrayList <String>(0);
		String SQL = "SELECT * FROM " + SMTablesalesperson.TableName
			+ " ORDER BY " + SMTablesalesperson.sSalespersonCode;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			for (int i = 0; i < arSalespersons.size();i++){
				rs.beforeFirst();
				sDesc = "";
				while(rs.next()){
					if (rs.getString(SMTablesalesperson.sSalespersonCode).compareToIgnoreCase(arSalespersons.get(i)) == 0){
						sDesc += "<FONT SIZE=2>" + rs.getString(SMTablesalesperson.sSalespersonCode) 
							+ " - " + rs.getString(SMTablesalesperson.sSalespersonFirstName) + " " + rs.getString(SMTablesalesperson.sSalespersonLastName)
						;
						break;
					}
				}
				if (sDesc.compareToIgnoreCase("") == 0){
					sDesc += "<FONT SIZE=2>" + arSalespersons.get(i)
						+ " - (SALESPERSON NOT FOUND)"
					;
				}
				sDesc += "</FONT>";
				arrSalesPersonList.add(sDesc);
			}
			rs.close();
		}catch (SQLException e){
			throw new Exception("Error [1430844529] COULD NOT READ SALESPERSONS with SQL: '" + SQL + " - " + e.getMessage());
		}
		return arrSalesPersonList;
	}
}