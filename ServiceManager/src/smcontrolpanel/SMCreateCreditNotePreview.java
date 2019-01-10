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
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMCreateCreditNotePreview extends HttpServlet{

	private static final long serialVersionUID = 1L;

	//formats
//	private static final SimpleDateFormat InvoiceDateformatter = new SimpleDateFormat("MM/dd/yyyy");
	private static final String DARK_BG_COLOR = "DCDCDC";
	
	private boolean bDebugMode = false;
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	private String sUserID = "0";
	
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
		    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		    sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		    sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		    
		    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    	String sInvoiceNumber = clsStringFunctions.PadLeft(clsManageRequestParameters.get_Request_Parameter("INVOICENUMBER", request), " ", 8);
	    	String sCreditNoteInfo = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEINFO", request);
	    	String sCreditNoteDate = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEDATE", request);
	    	//Customized title
	    	String sReportTitle = "Create credit note";
	    	out.println("<HTML>" +
				        "<HEAD><TITLE>" + sReportTitle + "</TITLE></HEAD>\n<BR>" + 
					    "<BODY BGCOLOR=\""+ SMUtilities.getInitBackGroundColor(getServletContext(), (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID))+ "\">");
		    
	    	out.println(sStyleScripts());
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
	 	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_CREATECREDITNOTEPREVIEW, "REPORT", "SMCreateCreditNotePreview", "[1376509313]");
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
			
			pwOut.println("<TABLE border=0 width=100% cellspacing=0 cellpadding=1><TR>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Line#</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Item Number</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Item Description</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Qty. Rtd.</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Ext. Price</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>Item Cat.</TH>");
			pwOut.println("<TH class=\"orderlineheading\"><B>UOM</TH>");
			pwOut.println("<TH class=\"orderlineheading\">Rt. to I.</TH>");
			pwOut.println("</TR>");
			
			String sDetailRowBackgroupColor;
			for (int i=0;i<cInvoice.getM_iNumberOfLinesOnInvoice();i++){
				if (cInvoice.getDetailByIndex(i).getM_iLineNumber() % 2 == 0){
					sDetailRowBackgroupColor = DARK_BG_COLOR;
				}else{
					sDetailRowBackgroupColor = "#FFFFFF";			
				}
				
				pwOut.println("<TR bgcolor =" + sDetailRowBackgroupColor + ">");
				pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_iLineNumber() + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_sItemNumber() + "</FONT></TD>");
				pwOut.println("<TD ALIGN=LEFT style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_sDesc() + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_dQtyShipped().setScale(2, BigDecimal.ROUND_HALF_UP) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_dExtendedPriceAfterDiscount().setScale(2, BigDecimal.ROUND_HALF_UP) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_sItemCategory() + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>" + cInvoice.getDetailByIndex(i).getM_sUnitOfMeasure() + "</FONT></TD>");
				/*if (cInvoice.getDetailByIndex(i).getM_iReturnToInventory() == 0){
					pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>NO</FONT></TD>");
				}else{
					pwOut.println("<TD ALIGN=CENTER style=\"border-style:none;\"><FONT SIZE=2>YES</FONT></TD>");
				}*/
				pwOut.println("</TR>");
			}
			pwOut.println("</TABLE>");
			return true;
		}
		
		private String sStyleScripts(){
			String s = "";
			String sBorderSize = "0";
			String sRowHeight = "22px";
			s += "<style type=\"text/css\">\n";

			//Layout table:
			s +=
				"table.innermost {"
				+ "border-width: " + sBorderSize + "px; "
				+ "border-spacing: 2px; "
				//+ "border-style: outset; "
				+ "border-style: none; "
				+ "border-color: white; "
				+ "border-collapse: separate; "
				+ "width: 100%; "
				+ "font-size: " + "small" + "; "
				+ "font-family : Arial; "
				+ "color: black; "
				//+ "background-color: white; "
				+ "}"
				+ "\n"
				;

			//s +=
			//	"table.main th {"
			//	+ "border-width: " + sBorderSize + "px; "
			//	+ "padding: 2px; "
			//	//+ "border-style: inset; "
			//	+ "border-style: none; "
			//	+ "border-color: white; "
			//	+ "background-color: white; "
			//	+ "color: black; "
			//	+ "font-family : Arial; "
			//	+ "vertical-align: text-middle; "
			//	//+ "height: 50px; "
			//	+ "}"
			//	+ "\n"
			//	;

			//s +=
			//	"tr.d0 td {"
			//	+ "background-color: #FFFFFF; "
			//	+"}"
			//	;
			//s +=
			//	"tr.d1 td {"
			//	+ "background-color: #EEEEEE; "
			//	+ "}"
			//	+ "\n"
			//	;

			//This is the def for a left aligned field:
			s +=
				"td.fieldleftaligned {"
				+ "height: " + sRowHeight + "; "
				+ "font-weight: bold; "
				+ "text-align: left; "
				+ "}"
				+ "\n"
				;
			
			//This is the def for a label field:
			s +=
				"td.fieldlabel {"
				+ "height: " + sRowHeight + "; "
				//+ "border-width: " + sBorderSize + "px; "
				//+ "padding: 2px; "
				//+ "border-style: none; "
				//+ "border-color: white; "
				//+ "vertical-align: text-middle;"
				//+ "background-color: black; "
				+ "font-weight: bold; "
				+ "text-align: right; "
				//+ "color: black; "
				//+ "height: 50px; "
				+ "}"
				+ "\n"
				;

			//This is the def for a control on the screen:
			s +=
				"td.fieldcontrol {"
				+ "height: " + sRowHeight + "; "
				//+ "border-width: " + sBorderSize + "px; "
				//+ "padding: 2px; "
				//+ "border-style: none; "
				//+ "border-color: white; "
				//+ "vertical-align: text-middle;"
				//+ "background-color: black; "
				+ "text-align: left; "
				//+ "color: black; "
				//+ "height: 50px; "
				+ "}"
				+ "\n"
				;
			
			//This is the def for an underlined heading on the screen:
			s +=
				"td.fieldheading {"
				+ "height: " + sRowHeight + "; "
				+ "font-weight: bold; "
				+ "text-align: left; "
				+ "text-decoration:underline; "
				+ "}"
				+ "\n"
				;
			
			//This is the def for the order lines heading:
			s +=
				"th.orderlineheading {"
				+ "height: " + sRowHeight + "; "
				//+ "border-width: " + sBorderSize + "px; "
				//+ "padding: 2px; "
				//+ "border-style: none; "
				//+ "border-color: white; "
				+ "vertical-align: text-bottom;"
				+ "background-color: #708090; "
				+ "font-weight: bold; "
				+ "font-size: small; "
				+ "text-align: center; "
				+ "color: white; "
				+ "}"
				+ "\n"
				;

			s += "tbody.scrolling {\n" 
				+ " height:4em;\n"
				+ " overflow:scroll;\n"
				+ "}"
			;
				
			s += "</style>"
				+ "\n"
				;

			return s;
		}
}
