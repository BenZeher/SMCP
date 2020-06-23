package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMPriceLevelLabels;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICUpdateItemPricesGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICUpdateItemPrices
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    			  + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	   
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		String sWarning = "";
		String sCallingClass = "";
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Validate the entries:
	    String sStartingItem = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.STARTINGITEM_PARAM, request);
	    String sEndingItem = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.ENDINGITEM_PARAM, request);
	    String sStartingPriceList = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.STARTINGPRICELIST_PARAM, request);
	    String sEndingPriceList = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.ENDINGPRICELIST_PARAM, request);
	    String sStartingRptGrp1 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP1STARTING_PARAM, request);
	    String sEndingRptGrp1 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP1ENDING_PARAM, request);
	    String sStartingRptGrp2 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP2STARTING_PARAM, request);
	    String sEndingRptGrp2 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP2ENDING_PARAM, request);
	    String sStartingRptGrp3 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP3STARTING_PARAM, request);
	    String sEndingRptGrp3 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP3ENDING_PARAM, request);
	    String sStartingRptGrp4 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP4STARTING_PARAM, request);
	    String sEndingRptGrp4 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP4ENDING_PARAM, request);
	    String sStartingRptGrp5 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP5STARTING_PARAM, request);
	    String sEndingRptGrp5 = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.RPTGRP5ENDING_PARAM, request);
	    boolean bPriceLevel0 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL0_PARAM) != null;
	    boolean bPriceLevel1 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL1_PARAM) != null;
	    boolean bPriceLevel2 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL2_PARAM) != null;
	    boolean bPriceLevel3 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL3_PARAM) != null;
	    boolean bPriceLevel4 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL4_PARAM) != null;
	    boolean bPriceLevel5 = request.getParameter(ICUpdateItemPricesSelection.PRICELEVEL5_PARAM) != null;
	    boolean bUpdateByPercent = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.UPDATETYPE_PARAM, request).compareToIgnoreCase("0") != 0;
	    String sUpdateAmount = clsManageRequestParameters.get_Request_Parameter(
	    		ICUpdateItemPricesSelection.UPDATEAMOUNT_PARAM, request);
	    if (sUpdateAmount.compareToIgnoreCase("") == 0){
	    	sUpdateAmount = "0.00";
	    }
	    
	    //Validate entries:
	    sWarning = "";
	    if (sStartingItem.compareToIgnoreCase(sEndingItem) >0 ){
	    	sWarning += "Starting item cannot be greater than ending item.  ";
	    }
	    if (sStartingPriceList.compareToIgnoreCase(sEndingPriceList) >0 ){
	    	sWarning += "Starting price list cannot be greater than ending price list.  ";
	    }
	    if (sStartingRptGrp1.compareToIgnoreCase(sEndingRptGrp1) >0 ){
	    	sWarning += "Starting Report Group 1 cannot be greater than ending Report Group 1.  ";
	    }
	    if (sStartingRptGrp2.compareToIgnoreCase(sEndingRptGrp2) >0 ){
	    	sWarning += "Starting Report Group 2 cannot be greater than ending Report Group 2.  ";
	    }
	    if (sStartingRptGrp3.compareToIgnoreCase(sEndingRptGrp3) >0 ){
	    	sWarning += "Starting Report Group 3 cannot be greater than ending Report Group 3.  ";
	    }
	    if (sStartingRptGrp4.compareToIgnoreCase(sEndingRptGrp4) >0 ){
	    	sWarning += "Starting Report Group 4 cannot be greater than ending Report Group 4.  ";
	    }
	    if (sStartingRptGrp5.compareToIgnoreCase(sEndingRptGrp5) >0 ){
	    	sWarning += "Starting Report Group 5 cannot be greater than ending Report Group 5.  ";
	    }
	    
	    try {
			@SuppressWarnings("unused")
			BigDecimal bdAmt = new BigDecimal(sUpdateAmount);
		} catch (NumberFormatException e) {
			sWarning += "Invalid percentage/amount: " + sUpdateAmount + ".  ";
		}

		//Construct the query string in case we have to redirect back:
		String sRedirectParams =
			"&" + ICUpdateItemPricesSelection.STARTINGITEM_PARAM + "=" +sStartingItem
			+ "&" + ICUpdateItemPricesSelection.ENDINGITEM_PARAM + "=" +sEndingItem
			+ "&" + ICUpdateItemPricesSelection.STARTINGPRICELIST_PARAM + "=" +sStartingPriceList
			+ "&" + ICUpdateItemPricesSelection.ENDINGPRICELIST_PARAM + "=" +sEndingPriceList
			+ "&" + ICUpdateItemPricesSelection.RPTGRP1STARTING_PARAM + "=" +sStartingRptGrp1
			+ "&" + ICUpdateItemPricesSelection.RPTGRP1ENDING_PARAM + "=" +sEndingRptGrp1
			+ "&" + ICUpdateItemPricesSelection.RPTGRP2STARTING_PARAM + "=" +sStartingRptGrp2
			+ "&" + ICUpdateItemPricesSelection.RPTGRP2ENDING_PARAM + "=" +sEndingRptGrp2
			+ "&" + ICUpdateItemPricesSelection.RPTGRP3STARTING_PARAM + "=" +sStartingRptGrp3
			+ "&" + ICUpdateItemPricesSelection.RPTGRP3ENDING_PARAM + "=" +sEndingRptGrp3
			+ "&" + ICUpdateItemPricesSelection.RPTGRP4STARTING_PARAM + "=" +sStartingRptGrp4
			+ "&" + ICUpdateItemPricesSelection.RPTGRP4ENDING_PARAM + "=" +sEndingRptGrp4
			+ "&" + ICUpdateItemPricesSelection.RPTGRP5STARTING_PARAM + "=" +sStartingRptGrp5
			+ "&" + ICUpdateItemPricesSelection.RPTGRP5ENDING_PARAM + "=" +sEndingRptGrp5
			+ "&" + ICUpdateItemPricesSelection.UPDATEAMOUNT_PARAM + "=" +sUpdateAmount

		;
		if (bUpdateByPercent){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.UPDATETYPE_PARAM + "=1";
		}else{
			sRedirectParams += "&" + ICUpdateItemPricesSelection.UPDATETYPE_PARAM + "=0";
		}
		if (bPriceLevel0){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL0_PARAM + "=YES";
		}
		if (bPriceLevel1){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL1_PARAM + "=YES";
		}
		if (bPriceLevel2){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL2_PARAM + "=YES";
		}
		if (bPriceLevel3){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL3_PARAM + "=YES";
		}
		if (bPriceLevel4){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL4_PARAM + "=YES";
		}
		if (bPriceLevel5){
			sRedirectParams += "&" + ICUpdateItemPricesSelection.PRICELEVEL5_PARAM + "=YES";
		}
		
		//If there are NO price levels chosen, advise the user:
		if (
			!bPriceLevel0
			&& !bPriceLevel1
			&& !bPriceLevel2
			&& !bPriceLevel3
			&& !bPriceLevel4
			&& !bPriceLevel5
		){
			sWarning = "You must choose at least one price level to update.";
		}
		
		//If there's any kind of error, return with the warning:
		if(sWarning.compareToIgnoreCase("") != 0){
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sRedirectParams
    		);			
        	return;
		}
	    
    	String sReportTitle = "Preview Item Price Updates";    	
    	String sCriteria = "Starting with item '<B>" + sStartingItem + "</B>'"
    		+ ", ending with '<B>" + sEndingItem + "</B>'";
   		sCriteria = sCriteria + ", from price list '<B>" + sStartingPriceList + "</B>' to '<B>" 
   			+ sEndingPriceList + "</B>'";
   		sCriteria = sCriteria + ", with report group 1 from '<B>" + sStartingRptGrp1 + "</B>' to '<B>" 
			+ sEndingRptGrp1 + "</B>'";
   		sCriteria = sCriteria + ", with report group 2 from '<B>" + sStartingRptGrp2 + "</B>' to '<B>" 
   			+ sEndingRptGrp2 + "</B>'";
   		sCriteria = sCriteria + ", with report group 3 from '<B>" + sStartingRptGrp3 + "</B>' to '<B>" 
   			+ sEndingRptGrp3 + "</B>'";
   		sCriteria = sCriteria + ", with report group 4 from '<B>" + sStartingRptGrp4 + "</B>' to '<B>" 
   			+ sEndingRptGrp4 + "</B>'";
   		sCriteria = sCriteria + ", with report group 5 from '<B>" + sStartingRptGrp5 + "</B>' to '<B>" 
   			+ sEndingRptGrp5 + "</B>'";
   		
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				+ sRedirectParams	
    		);			
        	return;
    	}
   		
		SMPriceLevelLabels pricelevellabels = new SMPriceLevelLabels();
		try {
			pricelevellabels.load(conn);
		} catch (Exception e1) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910621]");
			out.println("<BR><B><FONT COLOR=RED> Error [1580853473] loading price level labels - " + e1.getMessage() + "</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910622]");
		
   		String sPriceLevels = "";
   		if (bPriceLevel0){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_sbaselabel() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_sbaselabel() + "</B>'";
   			}
   		}
   		if (bPriceLevel1){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_slevel1label() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_slevel1label() + "</B>'";
   			}
   		}
   		if (bPriceLevel2){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_slevel2label() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_slevel2label() + "</B>'";
   			}
   		}
   		if (bPriceLevel3){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_slevel3label() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_slevel3label() + "</B>'";
   			}
   		}
   		if (bPriceLevel4){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_slevel4label() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_slevel4label() + "</B>'";
   			}
   		}
   		if (bPriceLevel5){
   			if (sPriceLevels.compareToIgnoreCase("") == 0){
   				sPriceLevels+= "'<B>" + pricelevellabels.get_slevel5label() + "</B>'";
   			}else{
   				sPriceLevels+= ", '<B>" + pricelevellabels.get_slevel5label() + "</B>'";
   			}
   		}

   		sCriteria += ", update price levels: " + sPriceLevels;
   		
   		if (bUpdateByPercent){
   			sCriteria+= " by <B>" + sUpdateAmount + "</B> percent.";
   		}else{
   			sCriteria+= " by <B>$ " + sUpdateAmount + "</B>.";
   		}

   		String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
   		
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR=\"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICUpdateItemPricesGenerate") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICUpdateItemPrices) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
    	
		out.println(SMUtilities.getMasterStyleSheetLink());
		
		//If it's a request to PREVIEW, then preview the prices
		if(request.getParameter(ICUpdateItemPricesSelection.PREVIEWBUTTONNAME_PARAM) != null){
	    	ICUpdateItemPricesPreview rpt = new ICUpdateItemPricesPreview();
	    	if (!rpt.processReport(
				conn,
				sStartingItem,
				sEndingItem,
				sStartingPriceList,
				sEndingPriceList,
				sStartingRptGrp1,
				sEndingRptGrp1,
				sStartingRptGrp2,
				sEndingRptGrp2,
				sStartingRptGrp3,
				sEndingRptGrp3,
				sStartingRptGrp4,
				sEndingRptGrp4,
				sStartingRptGrp5,
				sEndingRptGrp5,
				bPriceLevel0,
				bPriceLevel1,
				bPriceLevel2,
				bPriceLevel3,
				bPriceLevel4,
				bPriceLevel5,
				bUpdateByPercent,
				sUpdateAmount,
    			sDBID,
    			sUserID,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
    			{
    				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081006]");
    				response.sendRedirect(
		    			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    			+ "Warning=" + rpt.getErrorMessage()
		    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    			+ sRedirectParams	
		    		);
        		return;	
	    	}
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081007]");
	    		out.println("</BODY></HTML>");
		}else{
			//If it's a request to update, check the confirming flag:
			if(request.getParameter(ICUpdateItemPricesSelection.UPDATEBUTTONNAME_PARAM) != null){
				if (request.getParameter(ICUpdateItemPricesSelection.CONFIRMUPDATE_PARAM) == null){
					sWarning = "You chose to update but did not click the 'Confirm' checkbox.";
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081008]");
    				response.sendRedirect(
			    			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			    			+ "Warning=" + sWarning
			    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    			+ sRedirectParams	
			    	);
    				return;
				}else{
					try {
						updatePrices(
								conn,
								sStartingItem,
								sEndingItem,
								sStartingPriceList,
								sEndingPriceList,
								sStartingRptGrp1,
								sEndingRptGrp1,
								sStartingRptGrp2,
								sEndingRptGrp2,
								sStartingRptGrp3,
								sEndingRptGrp3,
								sStartingRptGrp4,
								sEndingRptGrp4,
								sStartingRptGrp5,
								sEndingRptGrp5,
								bPriceLevel0,
								bPriceLevel1,
								bPriceLevel2,
								bPriceLevel3,
								bPriceLevel4,
								bPriceLevel5,
								bUpdateByPercent,
								sUpdateAmount,
								sUserFullName,
								sUserID
						);
					}catch(Exception e) {
						clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081009]");
						response.sendRedirect(
			    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			    				+ "Warning=" + e.getMessage()
			    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    				+ sRedirectParams	
			    		);			
			        	return;	
					}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081010]");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			    		+ "Status=Prices were successfully updated."
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ sRedirectParams	
			    		);
				return;
					
				}
			}
		}
	}
	private void updatePrices(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sStartingPriceList,
			String sEndingPriceList,
			String sStartingRptGrp1,
			String sEndingRptGrp1,
			String sStartingRptGrp2,
			String sEndingRptGrp2,
			String sStartingRptGrp3,
			String sEndingRptGrp3,
			String sStartingRptGrp4,
			String sEndingRptGrp4,
			String sStartingRptGrp5,
			String sEndingRptGrp5,
			boolean bUpdateLevel0,
			boolean bUpdateLevel1,
			boolean bUpdateLevel2,
			boolean bUpdateLevel3,
			boolean bUpdateLevel4,
			boolean bUpdateLevel5,
			boolean bUpdateByPercent,
			String sUpdateAmount,
			String sUserFullName,
			String sUserID
			) throws Exception{
		
		String SQL = "UPDATE "
			+ SMTableicitemprices.TableName + " LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " 
			+ SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
		
			+ " SET" 
			+ " " + SMTableicitemprices.TableName + "." + SMTableicitemprices.datLastMaintained + " = NOW()"
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.lLastEditUserID+ " = " + sUserID + ""
			;
		
		if (bUpdateByPercent){
			if (bUpdateLevel0){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdBasePrice + " = " 
					+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdBasePrice 
					+ " * (1 + (" + sUpdateAmount + " / 100))";
			}
			if (bUpdateLevel1){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel1Price + " = " 
					+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel1Price 
					+ " * (1 + (" + sUpdateAmount + " / 100))";
			}
			if (bUpdateLevel2){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel2Price + " = " 
					+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel2Price 
					+ " * (1 + (" + sUpdateAmount + " / 100))";
			}
			if (bUpdateLevel3){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel3Price + " = " 
					+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel3Price 
					+ " * (1 + (" + sUpdateAmount + " / 100))";
			}
			if (bUpdateLevel4){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel4Price + " = " 
				+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel4Price 
				+ " * (1 + (" + sUpdateAmount + " / 100))";
			}
			if (bUpdateLevel5){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel5Price + " = " 
				+ SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel5Price 
				+ " * (1 + (" + sUpdateAmount + " / 100))";
			}

		}else{
			if (bUpdateLevel0){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdBasePrice + " = "
				+ SMTableicitemprices.bdBasePrice + " + " + sUpdateAmount;
			}
			if (bUpdateLevel1){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel1Price + " = "
				+ SMTableicitemprices.bdLevel1Price + " + " + sUpdateAmount;				
			}
			if (bUpdateLevel2){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel2Price + " = "
				+ SMTableicitemprices.bdLevel2Price + " + " + sUpdateAmount;				
			}
			if (bUpdateLevel3){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel3Price + " = "
				+ SMTableicitemprices.bdLevel3Price + " + " + sUpdateAmount;				
			}
			if (bUpdateLevel4){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel4Price + " = "
				+ SMTableicitemprices.bdLevel4Price + " + " + sUpdateAmount;				
			}
			if (bUpdateLevel5){
				SQL += ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel5Price + " = "
				+ SMTableicitemprices.bdLevel5Price + " + " + sUpdateAmount;				
			}
			
		}
			
		SQL += " WHERE ("
		
			+ "(" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber + " >= '"
			+ sStartingItem + "')"
			+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber + " <= '"
			+ sEndingItem + "')"
			
			+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode + " >= '"
			+ sStartingPriceList + "')"
			+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode + " <= '"
			+ sEndingPriceList + "')"
			
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " >= '"
			+ sStartingRptGrp1 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " <= '"
			+ sEndingRptGrp1 + "')"
			
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " >= '"
			+ sStartingRptGrp2 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " <= '"
			+ sEndingRptGrp2 + "')"
			
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " >= '"
			+ sStartingRptGrp3 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " <= '"
			+ sEndingRptGrp3 + "')"

			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " >= '"
			+ sStartingRptGrp4 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " <= '"
			+ sEndingRptGrp4 + "')"

			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " >= '"
			+ sStartingRptGrp5 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " <= '"
			+ sEndingRptGrp5 + "')"

		+ ")" //End main 'where' clause
		;
		
		//System.out.println("In " + this.toString() + " - SQL = " + SQL);
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUPDATEPRICE, "Attempting to update IC prices", SQL, "[1376509414]");
		
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (SQLException e) {
			throw new Exception("Error updating prices - " + e.getMessage());
		}
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUPDATEPRICE, "Successfully updated IC prices", SQL, "[1376509415]");
	}
}
