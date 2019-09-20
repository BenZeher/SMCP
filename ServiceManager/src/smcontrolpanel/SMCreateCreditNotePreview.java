package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMInvoice;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMCreateCreditNotePreview extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private boolean bDebugMode = false;
	private String sCallingClass = "";
	
		public void doGet(HttpServletRequest request,
					HttpServletResponse response)
					throws ServletException, IOException {

		    response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			if(!SMAuthenticate.authenticateSMCPCredentials(
					request, 
					response, 
					getServletContext(), 
					SMSystemFunctions.SMCreateCreditNotes))
				{
					return;
				}

		    //Get the session info:
		    HttpSession CurrentSession = request.getSession(true);
		    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		    
		    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    	String sInvoiceNumber = clsStringFunctions.PadLeft(clsManageRequestParameters.get_Request_Parameter("INVOICENUMBER", request), " ", 8);
	    	String sCreditNoteInfo = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEINFO", request);
	    	String sCreditNoteDate = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEDATE", request);
	    	//Customized title
			out.println(SMUtilities.getMasterStyleSheetLink());
	    	String sReportTitle = "Create credit note";
	    	out.println("<HTML>" +
				        "<HEAD><TITLE>" + sReportTitle + "</TITLE></HEAD>\n<BR>" + 
					    "<BODY BGCOLOR=\""+ SMUtilities.getInitBackGroundColor(getServletContext(), (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID))+ "\">");
		    
		    out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME));
		    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
			//Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR><BR>");
		    //display warning if there is any.
		    String sWarning;
			try {
				sWarning = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter("Warning", request));
			} catch (Exception e1) {
				sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
			}
		    //System.out.println("2.WARNING = " + sWarning);
			if (!sWarning.equalsIgnoreCase("")){
				out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
			}

	    	//Retrieve information
	    	Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		sDBID, 
	    		"MySQL", 
	    		"[1336663133] " 
	    		+ SMUtilities.getFullClassName(this.toString() 
	    		+ ".doGet - user: " 
	    		+ sUserID
	    		+ " - "
	    		+ sUserFirstName
	    		+ "  "
	    		+ sUserLastName
	    		));
	    	if (conn == null){
	    		sWarning = "Unable to get data connection.";
	    		response.sendRedirect(
					"" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		);			
	        	return;
	    	}
			   
	 	    //log usage of this this report
	 	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCREATECREDITNOTEPREVIEW, "REPORT", "SMCreateCreditNotePreview", "[1376509313]");
	    	out.println ("<FORM NAME=\"MAINFORM\" ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCreateCreditNoteAction\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='smcontrolpanel.SMCreateCreditNotePreview'>");

	    	//get request parameters
	    	try{
	    		if (bDebugMode){
	    			System.out.println("Displaying invoice# " + sInvoiceNumber);
	    		}
	    		DisplayInvoiceInfo(sInvoiceNumber,
	    						   conn,
	    						   out);
	    	}catch(Exception e){
		    	//processing error
	    		sWarning = "Unable to display invoice information.<BR>" + 
	    					e.getMessage();
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080419]");
	    		response.sendRedirect(
					"" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		);			
	        	return;
		    }
    		if (bDebugMode){
    			System.out.println("Finished displaying invoice information.");
    		}

	    	out.println("<INPUT TYPE=HIDDEN NAME='INVOICENUMBER' VALUE='" + sInvoiceNumber + "'>");
	    	out.println("<BR><BR>"
	    				+ "<TABLE BORDER=0 WIDTH=100%>"
	    					+ "<TR>"
	    						+ "<TD ALIGN=LEFT VALIGN=TOP>Credit note info: " + "</TD>"
	    						+ "<TD ALIGN=LEFT COLSPAN=2 VALING=TOP>"
	    							+ "<TEXTAREA NAME=\"CREDITNOTEINFO\""
									+ " rows=\"5\""
									+ " cols=\"80\""
									+ " id = \"CREDITNOTEINFO\">");
	    				if (sCreditNoteInfo.length() > 0){
	    					out.println(sCreditNoteInfo.replace("\"", "&quot;"));
	    				}
						out.println("</TEXTAREA>"
								+"</TD>"
	    					+ "</TR><TR>"
	    						+ "<TD ALIGN=LEFT VALIGN=TOP>Credit note date: " + "</TD>"
	    						+ "<TD ALIGN=LEFT VALING=TOP>"
	    							+ "<INPUT TYPE=TEXT NAME=\"CREDITNOTEDATE\""
	    								+ " id=\"CREDITNOTEDATE\"");
				    				if (sCreditNoteDate.length() > 0){
				    		out.println(" VALUE=\"" + sCreditNoteDate + "\"");
				    				}else{
				    		out.println(" VALUE=\"\"");
									}		
				    	  out.println(" SIZE=" + "10"
				    				+ " MAXLENGTH=" + "10"
				    				+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				    				+ ">" 
				    				+ SMUtilities.getDatePickerString("CREDITNOTEDATE", getServletContext())			
						    	+ "</TD>"
						    	+ "<TD ALIGN=LEFT VALING=TOP>"
						    		+ "<input type=button value=\"----Create credit note----\"" 
						    		+ " onClick=\"confirmation()\">"
						    	+"</TD>"
		    				+ "</TR>"
	    				+ "</TABLE>"
	    				);
	    	//out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Create----\">");
	    	out.println ("</FORM>");
	    	
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080420]");
			
	    	out.println ("<NOSCRIPT>\n"
	    			   + "    <font color=red>\n"
	    			   + "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
	    			   + "    </font>\n"
	    			   + "</NOSCRIPT>\n"
					
	    			   + "<script type=\"text/javascript\">\n"
		
	    			   + "function checkDate(){\n"
	    			   + " 	var CreditDateBox = document.getElementById('CREDITNOTEDATE');"
	    			   + "    if (CreditDateBox.value == '' ) {\n"
	    			   + "        return false;\n"
	    			   + "    }else{\n"
	    			   + "		  return true;\n"
	    			   + " 	  }\n"
	    			   + "}\n"

	    			   + "function confirmation(){\n"
	    			   + "    if (confirm('Are you sure you want to credit this invoice?')) {\n"
	    			   + "    	if (checkDate()) {\n"
	    			   + "        document.MAINFORM.submit();\n"
	    			   + "    	}else{\n"
	    			   + "		  alert('Please select a credite note date.');\n"
	    			   + " 	  	}\n"
	    			   + " 	  }\n"
	    			   + "}\n"
	    			   
					+ "</script>\n");
	    	
		    out.println("</BODY></HTML>");
		}
		
		private boolean DisplayInvoiceInfo(String sInvoiceNum,
										   Connection conn,
										   PrintWriter pwOut) throws Exception{
			
			SMInvoice cInvoice = new SMInvoice();
			cInvoice.setM_sInvoiceNumber(sInvoiceNum);
			if (!cInvoice.load(conn)){
				throw new Exception("Failed to load invoice.<BR>" + 
									cInvoice.getErrorMessage());
			}
			
			//display invoice header info
			String sHeaderInfo = "<B>Invoice number:</B> " + cInvoice.getM_sInvoiceNumber() + "&nbsp;&nbsp;"
							   + " <B>Invoice date:</B> " + cInvoice.getM_datInvoiceDate() + "&nbsp;&nbsp;"
							   + " <B>Customer bill to name:</B> " + cInvoice.getM_sBillToName() + "&nbsp;&nbsp;"
							   + " <B>Customer ship to name:</B> " + cInvoice.getM_sShipToName();
			
			pwOut.println(sHeaderInfo);
			
			
			//display invoice detail info
			/*
			 * Info need from invoice (from old service manager)
			 * 
			 * 	1.	Item Line#
			 *  2.	Item Number
			 *  3.	ItemDescription
			 *  4.	Qty Returned
			 *  5.	Extended Price
			 *  6.	Item Category
			 *  7.	Unit of Measure
			 *  8.	Ruturned to Inventory
			 */
			pwOut.println("<TABLE WIDTH=100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");			
			pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Line#</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Item Number</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Item Description</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Qty. Rtd.</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Ext. Price</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Item Cat.</TD>\n");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">UOM</TD>\n");
			pwOut.println("</TR>");
			
			String sDetailRowBackgroupColor;
			for (int i=0;i<cInvoice.getM_iNumberOfLinesOnInvoice();i++){
				if (cInvoice.getDetailByIndex(i).getM_iLineNumber() % 2 == 0){
					sDetailRowBackgroupColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
				}else{
					sDetailRowBackgroupColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;			
				}
				
				pwOut.println("<TR CLASS =" + sDetailRowBackgroupColor + ">");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_iLineNumber() +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_sItemNumber() +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_sDesc() +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_dQtyShipped().setScale(2, BigDecimal.ROUND_HALF_UP) +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_dExtendedPriceAfterDiscount().setScale(2, BigDecimal.ROUND_HALF_UP) +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_sItemCategory() +"</TD>\n");
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+cInvoice.getDetailByIndex(i).getM_sUnitOfMeasure() +"</TD>\n");
				pwOut.println("</TR>");
			}
			pwOut.println("</TABLE>");
			return true;
		}
		
}
