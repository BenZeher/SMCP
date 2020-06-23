package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
//import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMSystemFunctions;

public class SMPrintJobFolderLabelGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMPrintJobFolderLabel
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
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    String sStartingOrderNumber = "";
	    String sEndingOrderNumber = "";
	    //String sNumberOfCopies = "";
	    sStartingOrderNumber = request.getParameter("StartingOrderNumber");
	    sEndingOrderNumber = request.getParameter("EndingOrderNumber");
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy");
	    //sNumberOfCopies = SMUtilities.get_Request_Parameter(SMEditOrderSelection.NUMBEROFWORKORDERCOPIES, request);
	    
	    //int iNumberOfCopies = 0;
	    
    	try{
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
					   "Transitional//EN\">" +
				       "<HTML>" +
				       "<HEAD><BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" 
				       		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}\n"
				       		+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
				       		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
				       		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
				       		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
				       		+ "</STYLE>"
				       + "</HEAD>");
			
	    	//Retrieve information
	    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), 
										    			sDBID, 
										    			"MySQL", 
										    			SMUtilities.getFullClassName(this.toString()) 
										    			+ " - user: " 
										    			+ sUserID
										    			+ " - "
										    			+ sUserFirstName
										    			+ " "
										    			+ sUserLastName
										    			);
	    	if (conn == null){
	    		sWarning = "Unable to get data connection.";
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ "StartingOrderNumber=" + sStartingOrderNumber
	    				+ "&EndingOrderNumber=" + sEndingOrderNumber
	    				//+ "&" + SMEditOrderSelection.NUMBEROFWORKORDERCOPIES + "=" + sNumberOfCopies
	    				+ "&Warning=" + sWarning
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);			
	        	return;
	    	}
	    	
	    	//load order info
	    	String sSQL = "SELECT * FROM "
							+ SMTableorderheaders.TableName
							+ " WHERE "
							+ SMTableorderheaders.sOrderNumber + " = '" + clsStringFunctions.PadLeft(sStartingOrderNumber.trim(), " ", 8) + "'";
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    	if (!rs.next()){
	    		//no order found. This is unlikely because in the previous step, order availability has already been checked.
	    	}else{
		    	//print folder label now
		    	out.println("<TABLE BORDER=0 WIDTH=100%>");
		    	out.println("<TR>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=5%><B>Bill To</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=25%><U>" + rs.getString(SMTableorderheaders.sBillToName) + "</U></TD>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=5%><B>Order #</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=5%><U>" + rs.getString(SMTableorderheaders.sOrderNumber) + "</U></TD>\n" + 
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=5%><B>Ship To</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=25%><U>" + rs.getString(SMTableorderheaders.sShipToName) + "</U></TD>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=10%><B>Salesman</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=5%><U>" + rs.getString(SMTableorderheaders.sSalesperson) + "</U></TD>\n" + 
		    					"<TD ALIGN=RIGHT VALIGN=TOP WIDTH=15%><B>Final Req.</B> <img src=\"./images/checkbox.jpg\"><BR><B>Wage Scale</B> <img src=\"./images/checkbox.jpg\"></TD>\n" +
		    				"</TR>\n");
		    	String sBTAddress;
		    	String sSTAddress;
		    	String sTaxTypeDesc;
		    	BigDecimal bdContractAmount = new BigDecimal(0);
		    	
		        //Build the Bill To Address:
	            sBTAddress = rs.getString(SMTableorderheaders.sBillToAddressLine1).trim();
	            //if the 2nd line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sBillToAddressLine2).trim().length() != 0){
	                sBTAddress += "<BR>" + rs.getString(SMTableorderheaders.sBillToAddressLine2).trim();
	            }
	            //if the 3rd line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sBillToAddressLine3).trim().length() != 0){
	                sBTAddress += "<BR>" + rs.getString(SMTableorderheaders.sBillToAddressLine3).trim();
	            }
	            //if the 4th line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sBillToAddressLine4).trim().length() != 0){
	                sBTAddress += "<BR>" + rs.getString(SMTableorderheaders.sBillToAddressLine4).trim();
	            }
	            //Build City-State-Zip line.
	            //first, if there is any city, state or zip, add line break and get ready to add a new line
	            if (rs.getString(SMTableorderheaders.sBillToCity).trim().length() != 0 ||
	            	rs.getString(SMTableorderheaders.sBillToState).trim().length() != 0 ||
	            	rs.getString(SMTableorderheaders.sBillToZip).trim().length() != 0){ 
	                    sBTAddress += "<BR>";
	            }
	            //'if the city is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sBillToCity).trim().length() != 0){
	                sBTAddress += rs.getString(SMTableorderheaders.sBillToCity);
	            }
	            //if the state is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sBillToState).trim().length() != 0){
	                //if the city is empty, then there will be no leading comma for state
	                if (rs.getString(SMTableorderheaders.sBillToCity).trim().length() == 0){
	                    sBTAddress += rs.getString(SMTableorderheaders.sBillToState);
	                //otherwise, add leading comma to separate city and state
	                }else{
	                    sBTAddress += ", " + rs.getString(SMTableorderheaders.sBillToState);
	                }
	            }
	            //if the zip is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sBillToZip).trim().length() != 0){
	                //if both city and state are missing then there will be no leading space for zip
	                if (rs.getString(SMTableorderheaders.sBillToCity).trim().length() == 0 &&
	                	rs.getString(SMTableorderheaders.sBillToState).trim().length() == 0){
	                    sBTAddress += rs.getString(SMTableorderheaders.sBillToZip);
	                //otherwise, add leading space to separate zip from city or state
	            	}else{
	                    sBTAddress += "  " + rs.getString(SMTableorderheaders.sBillToZip);
	            	}
	            }
	
		        //Build the Bill To Address:
	            sSTAddress = rs.getString(SMTableorderheaders.sShipToAddress1).trim();
	            //if the 2nd line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sShipToAddress2).trim().length() != 0){
	                sSTAddress += "<BR>" + rs.getString(SMTableorderheaders.sShipToAddress2).trim();
	            }
	            //if the 3rd line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sShipToAddress3).trim().length() != 0){
	                sSTAddress += "<BR>" + rs.getString(SMTableorderheaders.sShipToAddress3).trim();
	            }
	            //if the 4th line of address is not empty, then add it in.
	            if (rs.getString(SMTableorderheaders.sShipToAddress4).trim().length() != 0){
	                sSTAddress += "<BR>" + rs.getString(SMTableorderheaders.sShipToAddress4).trim();
	            }
	            //Build City-State-Zip line.
	            //first, if there is any city, state or zip, add line break and get ready to add a new line
	            if (rs.getString(SMTableorderheaders.sShipToCity).trim().length() != 0 ||
	            	rs.getString(SMTableorderheaders.sShipToState).trim().length() != 0 ||
	            	rs.getString(SMTableorderheaders.sShipToZip).trim().length() != 0){ 
	                    sSTAddress += "<BR>";
	            }
	            //'if the city is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sShipToCity).trim().length() != 0){
	                sSTAddress += rs.getString(SMTableorderheaders.sShipToCity);
	            }
	            //if the state is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sShipToState).trim().length() != 0){
	                //if the city is empty, then there will be no leading comma for state
	                if (rs.getString(SMTableorderheaders.sShipToCity).trim().length() == 0){
	                    sSTAddress += rs.getString(SMTableorderheaders.sShipToState);
	                //otherwise, add leading comma to separate city and state
	                }else{
	                    sSTAddress += ", " + rs.getString(SMTableorderheaders.sShipToState);
	                }
	            }
	            //if the zip is not empty, then add it to the address
	            if (rs.getString(SMTableorderheaders.sShipToZip).trim().length() != 0){
	                //if both city and state are missing then there will be no leading space for zip
	                if (rs.getString(SMTableorderheaders.sShipToCity).trim().length() == 0 &&
	                	rs.getString(SMTableorderheaders.sShipToState).trim().length() == 0){
	                    sSTAddress += rs.getString(SMTableorderheaders.sShipToZip);
	                //otherwise, add leading space to separate zip from city or state
	            	}else{
	                    sSTAddress += "  " + rs.getString(SMTableorderheaders.sShipToZip);
	            	}
	            }
		    	
	            //Get tax class description
	            sSQL = "SELECT * FROM "
						+ SMTabletax.TableName
						+ " WHERE "
						+ SMTabletax.lid + " = " + rs.getInt(SMTableorderheaders.itaxid); 
	            ResultSet rsTax = clsDatabaseFunctions.openResultSet(sSQL, conn);
	            if (rsTax.next()){
	            	sTaxTypeDesc = rsTax.getString(SMTabletax.staxtype);
	            }else{
	            	sTaxTypeDesc = "N/A";
	            }
	            rsTax.close();
		   

	            //Get contract amount 
	            sSQL = "SELECT * FROM" + 
	            		" " + SMTableorderdetails.TableName + 
	            		" WHERE" + 
	            		" " + SMTableorderdetails.dUniqueOrderID + " = " + rs.getString(SMTableorderheaders.dOrderUniqueifier);
	            //System.out.println("SQL = " + sSQL);
	            ResultSet rsDetails = clsDatabaseFunctions.openResultSet(sSQL, conn);
	            while(rsDetails.next()){
	            	bdContractAmount = bdContractAmount.add(new BigDecimal(rsDetails.getDouble(SMTableorderdetails.dQtyOrdered) * rsDetails.getDouble(SMTableorderdetails.dOrderUnitPrice)));
	            }
	            rsDetails.close();	
	            	
		    	out.println("<TR>\n" +
						"<TD ALIGN=LEFT VALIGN=TOP><B>Bill To Address</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP>" + sBTAddress + "</TD>\n" +
						"<TD ALIGN=LEFT VALIGN=TOP><B>Customer</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP><U>" + rs.getString(SMTableorderheaders.sCustomerCode) + "</U></TD>\n" + 
						"<TD ALIGN=LEFT VALIGN=TOP><B>Ship To Address</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP>" + sSTAddress + "</TD>\n" +
						"<TD ALIGN=LEFT VALIGN=TOP><B>Tax Class</B></TD>\n<TD  ALIGN=LEFT VALIGN=TOP COLSPAN=2><U>" + sTaxTypeDesc + "</U></TD>\n" + 
					"</TR>\n");
		    	out.println("</TABLE><BR><BR>");
		    	
		    	out.println("<TABLE BORDER=0 WIDTH=100%>");
		    	out.println("<TR>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><B>Bill To Contact</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=13%><U>" + rs.getString(SMTableorderheaders.sBillToContact) + "</U></TD>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=15%><B>Bill To Phone</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><U>" + rs.getString(SMTableorderheaders.sBillToPhone) + "</U></TD>\n" + 
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><B>Ship To Contact</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><U>" + rs.getString(SMTableorderheaders.sShipToContact) + "</U></TD>\n" +
		    					"<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><B>Ship To Phone</B></TD>\n<TD ALIGN=LEFT VALIGN=TOP WIDTH=12%><U>" + rs.getString(SMTableorderheaders.sShipToPhone) + "</U></TD>\n" + 
		    				"</TR>\n");
		    	out.println("<TR>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Bill To Fax</B></TD>\n<TD>\n<U>" + rs.getString(SMTableorderheaders.sBillToFax) + "</U></TD>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Ship To Fax</B></TD>\n<TD>\n<U>" + rs.getString(SMTableorderheaders.sShipToFax) + "</U></TD>\n" + 
								"<TD ALIGN=LEFT VALIGN=TOP><B>Email</B></TD>\n<TD COLSPAN=3><U>" + rs.getString(SMTableorderheaders.sEmailAddress) + "</U></TD>\n" + 
							"</TR>\n");
		    	out.println("<TR>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Order Creation Date</B></TD>\n<TD>\n<U>" + USDateOnlyformatter.format(rs.getDate(SMTableorderheaders.datOrderCreationDate)) + "</U></TD>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Order Created By</B></TD>\n<TD>\n<U>" + rs.getString(SMTableorderheaders.sOrderCreatedByFullName) + "</U></TD>\n" + 
							"</TR>\n");
		    	out.println("<TR>\n<TD COLSPAN=8>&nbsp;</TD>\n</TR>\n");
		    	out.println("<TR>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Terms</B></TD>\n<TD>\n<U>" + rs.getString(SMTableorderheaders.sTerms) + "</U></TD>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Contract Amount</B></TD>\n<TD>\n<U>" + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdContractAmount) + "</U></TD>\n" + 
								"<TD ALIGN=LEFT VALIGN=TOP><B>Adv. Deposit</B></TD>\n<TD>\n<U>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</U></TD>\n" +
								"<TD ALIGN=LEFT VALIGN=TOP><B>Amout Due</B></TD>\n<TD>\n<U>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</U></TD>\n" + 
							"</TR>\n");
		    	out.println("</TABLE><BR>");
		    	
		    	printChangeOrderTable(out);
		    	
	    	}
	    	rs.close();
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080615]");
    	}catch(SQLException ex){
    		System.out.println("[1579274392] SQL Error: " + ex.getErrorCode() + " - " + ex.getMessage());
    	}
    				
	    out.println("</BODY></HTML>");
	}

	private void printChangeOrderTable(PrintWriter out){
				
		out.println(
			"<style>\n"
			+ "table.parts { border-style: none; width:100%; padding:0px;  border-collapse:collapse; font-family: Arial; font-size: 12pt;}"
			+ "td.col1 { border: 1px solid black; width:9%; padding:0px; text-align:center;}"
			+ "td.col2 { border: 1px solid black; width:13%; padding:0px; text-align:center;}"
			+ "td.blankcol { border: 1px solid black; padding:0px; height:25px; }"
			
			+ "</style>\n"
		);

		out.println("<table class = \"parts\" width=100%>");
		
//		out.println("<TR>\n<TD COLSPAN=6 ALIGN=CENTER>"
//			+ "Parts used / Additional Work Performed Items</TD>\n</TR>\n");
		out.println("<TR>\n<TD  class = \"col1\"><U>C.O.#</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>C.O.Date</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>C.O.Amt.</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>Accmulated Total</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>Inv. Date</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>Inv.#</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>Amt. Invoiced</U></TD>\n");
		out.println("<TD  class = \"col2\"><U>Total Billed</U></TD>\n</TR>\n");

		for (int i = 0; i < 15; i++){
			out.println("<TR>\n");
			for (int j = 0; j < 8; j++){
				out.println("<TD class = \"blankcol\" >&nbsp;</TD>\n");
			}
			out.println("</TR>\n");
		}
		
		out.println("</table>");
	}
}
