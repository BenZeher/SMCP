package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMInvoice;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsManageBigDecimals;

public class SMCreateMultipleInvoicesSelection extends HttpServlet {
	
	public static final String CREATE_SINGLE_INVOICE_PARAM = "CREATESINGLEINVOICE";  
	public static final String LIST_ORDERS_TO_INVOICE_PARAM = "LISTORDERSTOBEINVOICED";
	public static final String LIST_OF_INVOICES_CREATED_PARAM = "LISTOFINVOICESCREATED";
	private static final String DARK_BG_COLOR = "DCDCDC";
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCreateInvoices)
		){
			return;
		}
		
		String sSelectedLocations = "";
		String sSelectedServiceTypes = "";
		ArrayList<String> m_alLocations = new ArrayList<String>(0);
		ArrayList<String> m_alServiceTypes = new ArrayList<String>(0);
		
		String sCallingClass = "smcontrolpanel.SMCreateMultipleInvoicesSelection";
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName =  (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    
	    boolean bCreateSingleInvoice = clsManageRequestParameters.get_Request_Parameter(CREATE_SINGLE_INVOICE_PARAM, request).compareToIgnoreCase("") != 0;
	    String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request);
	    String title = "Create multiple invoices";
	    if (bCreateSingleInvoice){
	    	title = "Create single invoice for order number " + sTrimmedOrderNumber + ".";
	    }
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    //display warning if there is any.
	    String sWarning = SMUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter("Warning", request));
		if (!sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		if (!bCreateSingleInvoice){
			out.println("<FORM ACTION=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCreateMultipleInvoicesSelection\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");        	
        	out.println("<TABLE BORDER=1 WIDTH=100%>");

        	//check boxes for locations
    		out.println("<TR><TD VALIGN=TOP ><TABLE BORDER=0 WIDTH=100%>");
    		out.println("<TR><TD><B>Include locations:<B></TD></TR>");
    		out.println("<TR><TD>");

    		String SQL = "SELECT * FROM " + SMTablelocations.TableName + " ORDER BY " + SMTablelocations.sLocation ;
				
    		try{
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    					SQL, 
    					getServletContext(), 
    					sDBID, 
    					"MySQL" ,
    					SMUtilities.getFullClassName(this.toString() + ".doGet - getting locations - user: " 
    					+ sUserID
    					+ " - "
    					+ sUserFullName
    							)
    					);
    			while(rs.next()){
    				  out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"LOCATION" + rs.getString(SMTablelocations.sLocation) + "\"");
    				  if (request.getParameter("LOCATION" + rs.getString(SMTablelocations.sLocation)) != null){
    					  out.println(" CHECKED");
    				  }
    				  out.println(" width=0.25>" + rs.getString(SMTablelocations.sLocationDescription) + "</LABEL><BR>");
    			}
    			rs.close();
    		}catch (SQLException e){
    			out.println("Could not read locations table - " + e.getMessage());
    		}
    		
    		out.println("</TD></TR>");
	    	out.println("<TR><TD COLSPAN=1><HR></TD></TR>");
    		
    		//check boxes for order types:
    		out.println("<TR><TD><B>Include order types:<B></TD></TR>");
    		out.println("<TR><TD>");

    		SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC" ;
    		try{
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				getServletContext(), 
    				sDBID, 
					"MySQL",
					SMUtilities.getFullClassName(this.toString() + ".doGet - getting locations - user: " + sUserID + " - " + sUserFullName) 
    				);
    			while(rs.next()){
    				  out.println(
    						  "<LABEL><INPUT TYPE=CHECKBOX NAME=\"SERVICETYPE" + rs.getString(SMTableservicetypes.sCode) + "\"");
    				  if (request.getParameter("SERVICETYPE" + rs.getString(SMTableservicetypes.sCode)) != null){
    					  out.println(" CHECKED");
    				  }
		    		  out.println(" width=0.25>" + rs.getString(SMTableservicetypes.sName) + "</LABEL><BR>");
    			}
    			rs.close();
    		}catch (SQLException e){
    			out.println("Could not read service types table - " + e.getMessage());
    		}
    		
    		out.println("</TD>");
    		out.println("</TR>");
	    	out.println("<TR><TD COLSPAN=1><HR></TD></TR>");
        	out.println("<TR><TD ALIGN=CENTER COLSPAN=2><INPUT NAME=\"" + LIST_ORDERS_TO_INVOICE_PARAM 
        			+ "\" TYPE=\"SUBMIT\" VALUE=\"----Refresh list----\"></TD></TR>");
        	out.println("</TABLE></TD>");
        	
        	out.println("</FORM>");
        	
		}
    	out.println("<FORM NAME=ORDERLIST ACTION=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCreateMultipleInvoicesAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	if (bCreateSingleInvoice){
    		out.println("<INPUT TYPE=HIDDEN NAME='" + CREATE_SINGLE_INVOICE_PARAM 
    			+ "' VALUE='" + "Y" + "'>");
    	}
    	if (sTrimmedOrderNumber.compareToIgnoreCase("") != 0){
    		out.println("<INPUT TYPE=HIDDEN NAME='" + SMOrderHeader.Paramstrimmedordernumber 
    			+ "' VALUE='" + sTrimmedOrderNumber + "'>");
    	}
    	
    	boolean bAllowEditOrders = SMSystemFunctions.isFunctionPermitted(
    		SMSystemFunctions.SMEditOrders, 
    		sUserID,
    		getServletContext(), 
    		sDBID,
    		(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
    	
        //invoice and deposit list.
    	//if this is returning from successfully creating invoices, display a list of created invoices.
		String sMessage= clsManageRequestParameters.get_Request_Parameter(LIST_OF_INVOICES_CREATED_PARAM, request);
		if (sMessage.trim().length() > 0){
			try{
	        	Connection conn = clsDatabaseFunctions.getConnection(
	        			getServletContext(), 
	        			sDBID, 
	        			"MySQL", 
	        			SMUtilities.getFullClassName(this.toString()) 
	        			+ ".doGet - userID: " 
	        			+ sUserID
	        			+ " - "
	        			+ sUserFullName
	        			);
	        	//Print all of the newly created invoices
	        	out.println("<TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>Invoice(s) created:<BR><HR><BR>");
	    		String[] aInvoices = sMessage.split("/");

	    		for(int i=0; i<aInvoices.length; i++){
	    			SMInvoice invoice = new SMInvoice();
	    			invoice.setM_sInvoiceNumber(aInvoices[i]);
		        	if(!invoice.load(conn)) {
		        		throw new Exception("Invalid invoice number." + invoice.getErrorMessages() );
		        	}
		        	out.println("<BR>" + invoice.getM_sInvoiceNumber() 
		        			+ " --- " + invoice.getM_sBillToName()
		        			+ " / "  + invoice.getM_sShipToName()
		        			+ ": $" + clsManageBigDecimals.BigDecimalToFormattedString("#,###,###,##0.00", invoice.getInvoiceTotalAmount())
		        			);
	    		}
	        	
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080426]");
	    		//create a link to print all invoices
	    		String sInvoiceFrom = aInvoices[0];
	    		String sInvoiceTo = aInvoices[aInvoices.length - 1];
	    		
	    		out.println("<BR><BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoiceCriteriaSelection?" 
	    						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    						+ "&InvoiceNumberFrom=" + sInvoiceFrom.trim()
	    						+ "&InvoiceNumberTo=" + sInvoiceTo.trim()
	    						+ "&PrintMultipleInvoices=true"
	    						+ "\">Print all invoice(s).</A><BR><BR>");
	    		out.println("</TD>");
        	}catch(Exception ex){
        		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + SMUtilities.URLEncode("Error parsing invoice numbers. Please print them out manually." + ex.getMessage())
				+ sSelectedLocations
				+ sSelectedServiceTypes
				;
        		if (bCreateSingleInvoice){
        			sRedirectString += CREATE_SINGLE_INVOICE_PARAM + "=Y";
        			sRedirectString += "=" + sTrimmedOrderNumber;
        		}
        		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
        		response.sendRedirect(sRedirectString);
        		return;
        	}
		}
		//if there are passed in criteria, populate the list. Otherwise, display nothing.
		else if (request.getParameter(LIST_ORDERS_TO_INVOICE_PARAM) == null){
    		//display empty grid.
    		out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=60%>No orders were found with any items shipped.</TD>");
    	}else{
    		//list all the orders with shipments.
    		String sSQL = "";
    		if (bCreateSingleInvoice){
    			//If it's a single order, read it into the list:
    			sSQL = Get_Single_Order_SQL(sTrimmedOrderNumber, sUserName);
    		}else{
    			
    	    	//Get the list of selected locations and order types:
    	    	m_alLocations.clear();
    	    	m_alServiceTypes.clear();
    	    	sSelectedLocations = "";
    	    	sSelectedServiceTypes = "";
    		    Enumeration<String> paramNames = request.getParameterNames();
    		    String sParamName = "";
    		    String sLMarker = "LOCATION";
    		    String sSTMarker = "SERVICETYPE";
    		    while(paramNames.hasMoreElements()) {
    		      sParamName = paramNames.nextElement();
    			  if (sParamName.contains(sLMarker)){
    				  m_alLocations.add(sParamName.substring(sParamName.indexOf(sLMarker) + sLMarker.length()));
    			  }else if (sParamName.contains(sSTMarker)){
    				  m_alServiceTypes.add(sParamName.substring(sParamName.indexOf(sSTMarker) + sSTMarker.length()));
    			  }
    		    }
    		    Collections.sort(m_alLocations);
    		    Collections.sort(m_alServiceTypes);
    			
    		    if (m_alLocations.size() == 0){
    	    		sWarning = "You must select at least one location.";
    		    }else{
    		    	for (int iL=0;iL<m_alLocations.size();iL++){
    		    		sSelectedLocations += "&LOCATION" + m_alLocations.get(iL);
    		    	}
    		    }
    		    if (m_alServiceTypes.size() == 0){
    	    		sWarning = "You must select at least one service type.";
    		    }else{
    		    	for (int iST=0;iST<m_alServiceTypes.size();iST++){
    		    		sSelectedServiceTypes += "&SERVICETYPE" + m_alServiceTypes.get(iST);
    		    	}
    		    }

    		    if (sWarning.compareToIgnoreCase("") != 0){
            		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + SMUtilities.URLEncode(sWarning)
    				+ sSelectedLocations
    				+ sSelectedServiceTypes
    				;
            		if (bCreateSingleInvoice){
            			sRedirectString += CREATE_SINGLE_INVOICE_PARAM + "=Y";
            			sRedirectString += "=" + sTrimmedOrderNumber;
            		}
            		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
	        		response.sendRedirect(sRedirectString);
	        		return;
    		    }
    		        		
    			sSQL = Get_Order_With_Shipment_SQL(sUserName, m_alLocations, m_alServiceTypes);
    		}
    		    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					SMUtilities.getFullClassName(this.toString() + ".doGet - getting locations - user: " + sUserID + " - " + sUserFullName) 
					);
				int iCounter = 0;
				boolean bOddRow = true;
				int iColumnCounter = 10;
				out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=75%><TABLE STYLE=\"table-layout:fixed;\" BORDER=0 WIDTH=100%>");
				out.println("<TR>"
					+ "<TD ALIGN=CENTER style = \"font-size:small; font-weight:bold\">&nbsp;</TD>" 
					+ "<TD ALIGN=CENTER style = \"font-size:small; font-weight:bold\">Order#</TD>" 
					+ "<TD ALIGN=LEFT style = \"font-size:small; font-weight:bold\">Bill&nbsp;To</TD>" 
					+ "<TD ALIGN=LEFT style = \"font-size:small; font-weight:bold\">Ship&nbsp;To</TD>"
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Total</TD>"
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Disc&nbsp;%</TD>" 
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Disc&nbsp;amt</TD>"
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Tax</TD>"
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Deposit</TD>"
					+ "<TD ALIGN=RIGHT style = \"font-size:small; font-weight:bold\">Amt&nbsp;due</TD>"
					+ "</TR>" 
					+ "<TR><TD COLSPAN=" + Integer.toString(iColumnCounter) + "><HR></TD></TR>");

				while (rs.next()){
					String sBackgroundColor = "";
					if(bOddRow){
						sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
					}else{
						sBackgroundColor = "\"#FFFFFF\"";
					}
					BigDecimal bdTotalExtendedPrice = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble("TOTALEXTENDEDPRICE"), 2).replace(",",""));
					BigDecimal bdDiscountAmt = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountAmount), 2).replace(",",""));
					BigDecimal bdTaxTotal = rs.getBigDecimal(SMTableorderheaders.TableName + "." + SMTableorderheaders.bdordertaxamount);
					BigDecimal bdDeposit = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount), 2).replace(",",""));
					BigDecimal bdAmountDue = new BigDecimal("0.00");
					bdAmountDue = ((bdTotalExtendedPrice.subtract(bdDiscountAmt)).add(bdTaxTotal)).subtract(bdDeposit);

					//Create a link for the order number:
					String sOrderNumber = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber);
					String sOrderNumberEditLink = sOrderNumber;
					if (bAllowEditOrders){
						sOrderNumberEditLink = 
							"<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMEditOrderEdit?"
							+ SMOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "\">" + sOrderNumber + "</A>"
						;
					}

					out.println("<TR bgcolor =" + sBackgroundColor + ">" +
						"<TD ALIGN=CENTER style = \"font-size:small\"><LABEL><INPUT TYPE=CHECKBOX NAME=\"ORDER\" VALUE=\"ORDER" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber) + "\" CHECKED width=0.25></TD>" +  
				  		"<TD ALIGN=CENTER style = \"font-size:small\">" + sOrderNumberEditLink + "</LABEL></TD>" +  
				  		"<TD ALIGN=LEFT style = \"font-size:small\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName) + "</TD>" +  
				  		"<TD ALIGN=LEFT style = \"font-size:small\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName) + "</TD>" +
				  		"<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, bdTotalExtendedPrice) + "</TD>" +
				  		"<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage), 2) + "</TD>" +
				  		"<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountAmount), 2) + "</TD>" +
				  		"<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, bdTaxTotal) + "</TD>" +
				  		"<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderheaders.bdOrderTaxAmountScale, rs.getBigDecimal(SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount))
				  		   + "<INPUT TYPE=HIDDEN SIZE=15 MAXLENGTH=15 NAME=\"DEPO" 
						   + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber).trim() 
						   + "\" VALUE=\"" 
						   + rs.getBigDecimal(SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount) + "\"></TD>"
						+ "<TD ALIGN=RIGHT style = \"font-size:small\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, bdAmountDue) + "</TD>"							
						+ "</TR>"
					);
					iCounter++;
					bOddRow = !bOddRow;
				}
				rs.close();
				out.println("<TR><TD COLSPAN=" + Integer.toString(iColumnCounter) + "><HR></TD></TR>");
				//invoice date
				Calendar c = Calendar.getInstance();
				//last contact date, default to today.
				c.setTimeInMillis(System.currentTimeMillis());	    	
				out.println("<TR><TD ALIGN=CENTER COLSPAN=" + Integer.toString(iColumnCounter) + ">");
				if (iCounter>1){
					out.println("<input type=\"button\" name=\"CheckAll\" value=\"Check All\" onClick=\"checkAll(document.ORDERLIST.ORDER)\">");
					out.println("<input type=\"button\" name=\"UnCheckAll\" value=\"Uncheck All\" onClick=\"uncheckAll(document.ORDERLIST.ORDER)\">");
				}else{
					out.println("&nbsp;");
				}
				out.println("<B>Invoice date:&nbsp;</B>"); 
				out.println("<INPUT TYPE=TEXT NAME=\"INVOICEDATE\""
			    			+ " VALUE=\"\""
			    			+ " SIZE=" + "10"
			    			+ " MAXLENGTH=" + "10"
			    			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			    			+ ">"
			    			+ SMUtilities.getDatePickerString("INVOICEDATE", getServletContext()));
				
				out.println("<INPUT TYPE=HIDDEN NAME='SELECTEDLOCATIONS' VALUE='" + SMUtilities.URLEncode(sSelectedLocations) + "'>");
				out.println("<INPUT TYPE=HIDDEN NAME='SELECTEDSERVICETYPES' VALUE='" + SMUtilities.URLEncode(sSelectedServiceTypes) + "'>");
				out.println(
					"&nbsp;&nbsp;&nbsp;&nbsp;<INPUT NAME=\"INVOICESELECTED\" TYPE=\"SUBMIT\" VALUE=\"----Invoice selected orders----\" ONCLICK=\"return disable();\">"
					+ "&nbsp;&nbsp;<B>Total number of orders: </B>" + iCounter
				);
				
				out.println("</TD></TR>");
				out.println("</TABLE></TD>");
				if (iCounter == 0){
					//display empty grid.
					out.println("<TR><TD COLSPAN=8>No orders found with quantities shipped.</TD></TR>");
				}
			} catch (SQLException e) {
        		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + SMUtilities.URLEncode("Error listing orders = " + e.getMessage())
				+ sSelectedLocations
				+ sSelectedServiceTypes
				;
        		if (bCreateSingleInvoice){
        			sRedirectString += CREATE_SINGLE_INVOICE_PARAM + "=Y";
        			sRedirectString += "=" + sTrimmedOrderNumber;
        		}
        		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
        		response.sendRedirect(sRedirectString);
        		return;
			}
    	}
        	
        out.println ("</TR></TABLE><BR><BR>");
       	out.println ("</FORM>");
       	
       	out.println ("<script type=\"text/javascript\">");
       	
       	out.println(javaScript());
       	
       	out.println("function checkAll(field)");
       	out.println("{");
       		out.println("for (i = 0; i < field.length; i++)");
       		out.println("field[i].checked = true;");
       	out.println("}");

       	out.println("function uncheckAll(field)");
       	out.println("{");
       		out.println("for (i = 0; i < field.length; i++)");
       		out.println("field[i].checked = false;");
       	out.println("}");
       	
       	out.println("</script>");

	    out.println("</BODY></HTML>");
	}
	
	public String javaScript(){
		String s = "";
		  s +=  "   window.addEventListener(\"beforeunload\",function(){\n"
		  +  "      document.documentElement.style.cursor = \"not-allowed\";\n "
		  +  "     document.documentElement.style.cursor = \"wait\";\n"
		  +"      });\n";

		return s;
	}
	
	private String Get_Order_With_Shipment_SQL(
		String sUserName, 
		ArrayList<String>arrLocations,
		ArrayList<String>arrServiceTypes
		){

	    String SQL = "SELECT" 
	    	+ " 'CreateMultipleOrders.Get_Order_With_Shipment_SQL' AS REPORTNAME"
	    	+ ", '" + sUserName + "' AS USERNAME"
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountAmount 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bdordertaxamount
	    	+ ", DETAILSQUERY.TOTALLINEPRICE AS TOTALEXTENDEDPRICE"
	    	+ " FROM"
	    	+ " (SELECT "
	    	+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
	    	+ ", SUM(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice + ") AS TOTALLINEPRICE"
	    	+ ", SUM(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped + ") AS TOTALQTYSHIPPED"
	    	+ " FROM " + SMTableorderdetails.TableName 
	    	+ " WHERE (orderdetails.dQtyShipped > 0.00)"
	    	+ " GROUP BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
	    	+ ")"
	    	+ " AS DETAILSQUERY"
	    	+ " LEFT JOIN " + SMTableorderheaders.TableName 
	    	+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
	    	+ " = DETAILSQUERY." + SMTableorderdetails.strimmedordernumber
	    	+ " WHERE (" 
	    		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " < '1950-01-01 00:00:00')" 
			 	+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
		 	;
	    
	    if (arrLocations.size() > 0){
	    	SQL += " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + " = '" + (String) arrLocations.get(0) + "'";
	    	for (int i=1;i<arrLocations.size();i++){
	    		SQL += " OR " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + " = '" + (String) arrLocations.get(i) + "'";
	    	}
	    	SQL += ")";
	    }else{
	    	//no location is selected, give out an empty list. Normally, this will not happen.
	    	SQL += " AND " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + " = '-1'";
	    }
	    if (arrServiceTypes.size() > 0){
	    	SQL += " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = '" + (String) arrServiceTypes.get(0) + "'";
	    	for (int i=1;i<arrServiceTypes.size();i++){
	    		SQL += " OR " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = '" + (String) arrServiceTypes.get(i) + "'";
	    	}
	    	SQL += ")";
	    }else{
	    	//no service type is selected, give out an empty list. Normally, this will not happen.
	    	SQL += " AND " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = '-1'";
	    }
	    SQL += ")";
	    SQL += " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber;
	    //System.out.println("[1410811414] SQL = " + SQL);
	    return SQL;
	    
	}
	private String Get_Single_Order_SQL(String sTrimmedOrderNumber, String sUser){
		
	    String SQL = "SELECT" 
	    	+ " 'CreateMultipleOrders.Get_Single_Order_SQL' AS REPORTNAME"
	    	+ ", '" + sUser + "' AS USERNAME"
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountAmount 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage 
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bdordertaxamount
	    	+ ", SUM(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice + ") AS TOTALEXTENDEDPRICE"
	    	+ " FROM" 
	    	+ " " + SMTableorderheaders.TableName + " LEFT JOIN " + SMTableorderdetails.TableName
	    	+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = "
	    	+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
    			+ " = '" + sTrimmedOrderNumber + "')"
    			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType 
    			+ " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
    		+ ")"
    		;
	    return SQL;
	    
	}
}
