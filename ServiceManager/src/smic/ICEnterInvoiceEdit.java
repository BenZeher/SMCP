package smic;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpoinvoicelines;
import SMDataDefinition.SMTableicvendorterms;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEnterInvoiceEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static String INPUT_RECEIPT_FIELD = "INPUTRECEIPTID";
	public static String ADD_RECEIPT_BUTTON_NAME = "ADDRECEIPT";
	public static String ADD_RECEIPT_BUTTON_LABEL = "Add receipt";
	//public static String FOUND_RECEIPT_PARAMETER = "FOUNDRECEIPTID";
	public static String FIND_RECEIPT_BUTTON_NAME = "FINDRECEIPT";
	public static String FIND_RECEIPT_BUTTON_LABEL = "Find receipt ID for UNINVOICED receipts";
	
	public static String UPDATE_VENDOR_BUTTON_NAME = "UPDATEVENDOR";
	public static String UPDATE_VENDOR_BUTTON_LABEL = "Update vendor to match invoice";
	public static String UPDATE_VENDOR_CONFIRM_CHECKBOX = "UPDATEVENDORCONFIRM";
	public static String UPDATE_VENDOR_CONFIRM_LABEL = "Confirm vendor update";
	
	public static String REMOVE_RECEIPT_LINE_BUTTON_NAME = "REMOVELINE";
	public static String REMOVE_RECEIPT_LINE_BUTTON_LABEL = "Delete";
	
	public static String FOUND_VENDOR_PARAMETER = "FOUND" + ICPOInvoice.ParamsVendor;
	public static String FIND_VENDOR_BUTTON_NAME = "FINDVENDOR";
	public static String FIND_VENDOR_BUTTON_LABEL = "Find vendor";
	
	public static String REFRESH_VENDOR_INFO_PARAMETER = "REFRESH" + ICPOInvoice.ParamsVendor;
	public static String REFRESH_VENDOR_INFO_BUTTON_NAME = "REFRESHVENDORINFO";
	public static String REFRESH_VENDOR_INFO_BUTTON_LABEL = "Update vendor info";
	
	public static String CALCULATE_TERMS_PARAMETER = "CALCULATETERMS";
	public static String CALCULATE_TERMS_BUTTON_NAME = "CALCULATETERMSBUTTON";
	public static String CALCULATE_TERMS_BUTTON_LABEL = "Re-calculate terms";
	
	public static String INVOICE_ALL_LINES_PARAMETER = "INVOICEALLLINESPARAM";
	public static String INVOICE_ALL_LINES_BUTTON_NAME = "INVOICEALLLINESBUTTON";
	public static String INVOICE_ALL_LINES_BUTTON_LABEL = "Invoice all receipt lines";
	
	public static String RECALCULATETOTALS_PARAMETER = "RECALCULATETOTALSPARAM";
	public static String RECALCULATETOTALS_BUTTON_NAME = "RECALCULATETOTALSBUTTON";
	public static String RECALCULATETOTALS_BUTTON_LABEL = "Recalculate line totals";

	public static String PO_INVOICE_ENTRY_OBJECT = "POINVOICEOBJECT";
	public static String NUMBER_OF_LINES = "NUMBEROFLINES";
	
	public static String FIND_EXPENSE_ACCT_PARAMETER = "FINDGLEXPENSEACCT";
	public static String FIND_EXPENSE_ACCT_LABEL = "Find GL Acct";
	
	public static String ADD_NEW_LINE_PARAMETER = "ADDNEWLINE";
	public static String ADD_NEW_LINE_LABEL = "Add New Line";
	
	public static String TAX_DROP_DOWN_PARAM = "TAXDROPDOWN";
	
	private boolean bDebugMode = false;

	//We'll use these to store the GL List, so we don't have to load it several times:
    private ArrayList<String> m_sGLValues = new ArrayList<String>();
    private ArrayList<String> m_sGLDescriptions = new ArrayList<String>();
	
    private int iNumberOfDetailColumns;
    
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		ICPOInvoice entry = new ICPOInvoice(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEnterInvoiceAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEnterInvoices
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEnterInvoices)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//If this is a 'new' entry, make sure we set the lid to -1, otherwise the user may accidentally have entered an ID and we may try
		//to create a new invoice with that ID, or edit one with that ID.
		if(smedit.getAddingNewEntryFlag()){
			entry.setM_slid("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the action class, then
		//the session will have a PO invoice entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//Record this URL so we can return to it later:
		if (entry.getM_slid().compareToIgnoreCase("-1") != 0){
			smedit.addToURLHistory("Editing invoice ID " + entry.getM_slid());
		}else{
			if (
				//If it's a request to enter a new invoice:
				(smedit.getCallingClass().compareToIgnoreCase("smic.ICEnterInvoiceSelection") == 0)
				&& (clsManageRequestParameters.get_Request_Parameter("SubmitAdd", request).compareToIgnoreCase("") != 0)
			){
				smedit.addToURLHistory("Entering a new invoice");
			}
		}
		
	    if (currentSession.getAttribute(PO_INVOICE_ENTRY_OBJECT) != null){
	    	entry = (ICPOInvoice) currentSession.getAttribute(PO_INVOICE_ENTRY_OBJECT);
	    	currentSession.removeAttribute(PO_INVOICE_ENTRY_OBJECT);

	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		if (request.getParameter("RETURNINGFROMFINDER") == null){
			    	if(!entry.load(getServletContext(), 
			    				   smedit.getsDBID(), 
			    			       smedit.getUserID(),
			    			       smedit.getFullUserName()
			    			)){
						response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
							+ "?" + ICPOInvoice.ParamlID + "=" + entry.getM_slid()
							+ "&Warning=" + entry.getErrorMessages()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
			    	}
	    		}
	    	}
	    }
	    
	    //Pick up any warnings here:
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("WARNING", request);
	    
	    //Pick up any 'status' messages here:
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("STATUS", request);
	    
	    //If we are returning from finding a vendor, update that vendor and vendor info:
	    if (request.getParameter(FOUND_VENDOR_PARAMETER) != null){
	    	entry.setM_svendor((String)request.getParameter(FOUND_VENDOR_PARAMETER));
	    	//Set the vendor, terms, PO, etc. here since the receipt ID was just set:
	    	if (!entry.loadVendorInformation(getServletContext(), smedit.getsDBID(), smedit.getUserID())){
	    		sWarning += "  Error [1478205014] loading vendor information for vendor '" + entry.getM_svendor() + "' - " + entry.getErrorMessages();
	    	}
	    }

	    //If we are returning from a request to refresh vendor info, update the vendor info:
	    if (request.getParameter(REFRESH_VENDOR_INFO_PARAMETER) != null){
	    	if (!entry.loadVendorInformation(getServletContext(), smedit.getsDBID(), smedit.getUserID())){
	    		sWarning += "  Error [1478205015] refreshing vendor information for vendor '" + entry.getM_svendor() + "' - " + entry.getErrorMessages();
	    	}
	    }
	    
	    //If we are returning from a request to re-calculate terms:
	    //System.out.println("[1473192210] discount date: " + entry.getM_sdatdiscount());
	    if (request.getParameter(CALCULATE_TERMS_PARAMETER) != null){
	    	try {
				entry.calculateDiscount(getServletContext(), smedit.getsDBID(), smedit.getUserID());
			} catch (Exception e1) {
				sWarning += "Error [1490662773] - " + e1.getMessage() + ".";
			}
	    }
	    
	   //System.out.println("[1473192211] discount date: " + entry.getM_sdatdiscount());
	    
	    //If we are returning from a request to invoice all the lines:
	    if (request.getParameter(INVOICE_ALL_LINES_PARAMETER) != null){
	    	entry.invoiceAllLines();
	    }
	    
	    //If we are returning from a request to recalculate the line totals:
	    if (request.getParameter(RECALCULATETOTALS_PARAMETER) != null){
	    	//Don't need to do anything - the totals should recalculate automatically:
	    	//entry.invoiceAllLines();
	    }
	    
    	Enumeration <String> e = request.getParameterNames();
    	String sParam = "";
    	String sRecordNumber = "";
    	String sFoundAcct = "";
    	while (e.hasMoreElements()){
    		sParam = (String) e.nextElement();
    		if (sParam.contains(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER)){
    			sRecordNumber = sParam.substring(
    			(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER).length(), sParam.length());
    			sFoundAcct = request.getParameter(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER + sRecordNumber);
    			if (bDebugMode){
    				System.out.println(
    					"In " + this.toString()
    					+ " - sRecordNumber = " + sRecordNumber
    					+ ", sFoundAcct = " + sFoundAcct
    				);
    			}
    		}
    	}

	    if (sFoundAcct.compareToIgnoreCase("") != 0){
		    //If we are returning from finding an expense account for a new additional line,
		    //update that account and account info:
	    	boolean bIsRecordNumber = true;
	    	for(int i = 0; i < Integer.parseInt(entry.getsnumberofnewlines()); i++){   	
	    		if (sRecordNumber.compareToIgnoreCase("NEW" + Integer.toString(i)) == 0){
	    			entry.setsNewLineExpenseAcct(
	    				request.getParameter(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER + "NEW" + Integer.toString(i)), i);
	    			bIsRecordNumber = false;
	    		}
	    	} 
		    //Otherwise, update the GL account on the correct line:
	    	if(bIsRecordNumber){
	    		entry.getLines().get(Integer.parseInt(sRecordNumber)).setsexpenseaccount(sFoundAcct);
	    	}    	
	    }
	    
	    smedit.printHeaderTable();
	    
	    //If there is a WARNING, print it here in red:
	    if (sWarning.compareToIgnoreCase("") != 0){
	    	smedit.getPWOut().println("<BR><FONT COLOR=RED><B>WARNING: " + sWarning + "</B></FONT><BR>");
	    }

	    //If there is a STATUS returned, print it here:
	    if (sStatus.compareToIgnoreCase("") != 0){
	    	smedit.getPWOut().println("<BR><B>WARNING: " + sStatus + "</B><BR>");
	    }

	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A>");
	    
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");
		
		boolean bEnteringInvoiceAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEnterInvoices, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		boolean bEditReceipts  = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditReceipts, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			); 
		boolean bPrintPurchaseOrders  = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			); 
		
		//Only show 'Update' and 'Delete' buttons if the invoice has NOT been exported:
		smedit.setbIncludeDeleteButton(
			entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0
		);
		smedit.setbIncludeUpdateButton(
				entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0
			);

		try {
			smedit.createEditPage(getEditHTML(smedit, entry, bEnteringInvoiceAllowed, bEditReceipts, bPrintPurchaseOrders, request), "");
		} catch (SQLException e1) {
    		String sError = "Could not create edit page - " + e1.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOInvoice.ParamlID + "=" + entry.getM_slid()
				+ "&Warning=Could not load Invoice with ID #: " + entry.getM_slid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}

		smedit.getPWOut().println(SMTableaptransactions.getInvoiceTaxLiabilityFootnote());
		
	    return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			ICPOInvoice entry, 
			boolean bEditingInvoiceAllowed,
			boolean bEditingReceiptAllowed,
			boolean bPrintPurchaseOrdersAllowed,
			HttpServletRequest req
			) throws SQLException{
		
		loadGLList(sm);
		
		String s = "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		
		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOInvoice.ParamlID + "\" VALUE=\"" + entry.getM_slid() + "\">";
		
		//New Row
		String sInvoiceID = "NEW";
		if (entry.getM_slid().compareToIgnoreCase("-1") != 0){
			sInvoiceID = entry.getM_slid();
		}
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Invoice ID:</TD>"
			+ "<TD>" 
			+ sInvoiceID
			+ "</TD>"
			;

		//Export sequence number:
        s += "<TD style=\" text-align:right; font-weight:bold; \">Export sequence number:</TD>";
        s += "<TD>" + entry.getM_sexportsequencenumber() + "</TD>";
        
        s += "</TR>";
        
        s += "<TR>";
        
		//Invoice entry date
		s += "<TD style=\" text-align:right; font-weight:bold; \">Entered:</TD>";
		s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamdatEntered + "\""
				+ " VALUE=\"" + entry.getM_sdatentered() + "\""
				+ " SIZE=10"
				+ " MAXLENGTH=10"
				//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(ICPOInvoice.ParamdatEntered, getServletContext())
				+ "</TD>"
			;
		
		//Invoice date:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Invoice date:</TD>";
		
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
		s +=
			"<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamdatInvoice + "\""
			+ " VALUE=\"" + entry.getM_sdatinvoice() + "\""
			+ " SIZE=10"
			+ " MAXLENGTH=10"
			//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(ICPOInvoice.ParamdatInvoice, getServletContext())
			+ "</TD>"
		;
		}else{
			s +=
				"<TD>"
				+ entry.getM_sdatinvoice()
				+ "</TD>"
			;
		}
		s += "</TR>";
	
		//New row:
		s += "<TR>";
		
		s += "<TD style=\" text-align:right; font-weight:bold; \">Invoice number:</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamsInvoiceNumber + "\""
	        	+ " VALUE=\"" + entry.getM_sinvoicenumber() + "\""
	        	+ " SIZE=" + "15"
	        	+ " MAXLENGTH=" + SMTableicpoinvoiceheaders.sinvoicenumberLength
	        	+ ">"
	        	+ "</TD>"
	        ;
		}else{
			s +=
				"<TD>"
				+ entry.getM_sinvoicenumber()
				+ "</TD>"
			;
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">Invoice total:</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParambdInvoiceTotal + "\""
	        	+ " VALUE=\"" + entry.getM_sinvoicetotal() + "\""
	        	+ " SIZE=" + "10"
	        	+ " MAXLENGTH=" + "13"
	        	+ ">"
	        	+ "</TD>"
	        ;
		}else{
			s += 
				"<TD>"
				+ entry.getM_sinvoicetotal()
				+ "</TD>"
			;
		}
		s += "</TR>";

		s += "<TR>";

		//Is tax already included on this invoice?
		s += "<TD style=\" text-align:right; font-weight:bold; \"><a href=\"#TAXLIABILITYNOTE\">Additional tax liability?<SUP>1</a></SUP>:&nbsp;</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>";
			s += "<SELECT NAME = \"" + ICPOInvoice.Paramiinvoiceincludestax + "\" >";
			s += "<OPTION"
				+ " VALUE=\"" 
				+ "-1" 
				+ "\">" 
				+ "** SELECT OPTION **"
				+ "</OPTION>"
			;
			s += "<OPTION";
			if (entry.getiinvoiceincludestax().compareToIgnoreCase("1") == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" 
				+ "1" 
				+ "\">" 
				+ SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.NO_ADDITIONAL_TAX_LIABILITY)
				+ "</OPTION>"
			;
	
			s += "<OPTION";
			if (entry.getiinvoiceincludestax().compareToIgnoreCase("0") == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" 
				+ "0" 
				+ "\">" 
				+ SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.ADDITIONAL_TAX_LIABILITY)
				+ "</OPTION>"
			;
			s += "</SELECT>";
			
			s += "</TD>";
		} else{
			String sTaxLiabilityLabel = SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.ADDITIONAL_TAX_LIABILITY);
			if (entry.getiinvoiceincludestax().compareToIgnoreCase("1") == 0){
				sTaxLiabilityLabel = SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.NO_ADDITIONAL_TAX_LIABILITY);
			}
			s += "<TD>" + sTaxLiabilityLabel + "</TD>";
		}
		
		//Tax type:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Tax:&nbsp;</TD>";
		
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>"
				+ "<SELECT NAME=\"" + ICPOInvoice.Paramitaxid + "\"" 
				+ ">";
			//Add a blank line to insure that the user chooses a tax type:
			s += "<OPTION"
				+ " VALUE=\"" 
				+ "0" 
				+ "\">" 
				+ "** SELECT A TAX TYPE **"
				+ "</OPTION>";
			
			String SQL = "SELECT"
				+ " " + SMTabletax.lid
				+ ", " + SMTabletax.staxjurisdiction
				+ ", " + SMTabletax.staxtype
				+ " FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.iactive + " = 1)"
					+ " AND (" + SMTabletax.ishowinaccountspayable + " = 1)"
				+ ")"
				+ " ORDER BY " + SMTabletax.staxjurisdiction + ", " + SMTabletax.staxtype
			;
			try {
				ResultSet rsTax = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + ".getEditHTML - user: " + sm.getUserName());
				while (rsTax.next()){
					s += "<OPTION";
					String sTaxID = Long.toString(rsTax.getLong(SMTabletax.lid));
					if (sTaxID.compareToIgnoreCase(entry.getitaxid()) == 0){
						s += " selected=YES ";
					}
					s += " VALUE=\"" + sTaxID + "\">" 
					+ rsTax.getString(SMTabletax.staxjurisdiction)
					+ " - " + rsTax.getString(SMTabletax.staxtype)
					+ "</OPTION>";
				}
				rsTax.close();
			} catch (SQLException e) {
				throw new SQLException("Error [1454355931] loading taxes - " + e.getMessage());
			}
	
			s += "</SELECT>";
		}else{
			String sTax = "N/A";
			String SQL = "SELECT"
				+ " " + SMTabletax.staxjurisdiction
				+ ", " + SMTabletax.sdescription
				+ ", " + SMTabletax.staxtype
				+ " FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.lid + " = " + entry.getitaxid() + ")"
				+ ")"
			;
			try {
				ResultSet rsTax = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + ".getEditHTML - user: " + sm.getUserName());
				if (rsTax.next()){
					sTax = rsTax.getString(SMTabletax.staxjurisdiction)
						+ " - " + rsTax.getString(SMTabletax.staxtype)
					;
				}
				rsTax.close();
			} catch (SQLException e) {
				throw new SQLException("Error [1454355930] loading taxes for read only - " + e.getMessage());
			}
			s += "<TD>" + sTax + "</TD>";
		}
		s += "</TD>";
        s += "</TR>";
        
        s += "<TR>";
        
        //Vendor:
        s += "<TD style=\" text-align:right; font-weight:bold; \">Vendor:</TD>";
        
        if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
	        s += "<TD><INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamsVendor + "\""
		    	+ " VALUE=\"" + entry.getM_svendor().replace("\"", "&quot;") + "\""
		    	+ " SIZE=" + "10"
		    	+ " MAXLENGTH=" + Integer.toString(SMTableicpoinvoiceheaders.svendorLength)
		    	+ ">"
		        ;
			s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + FIND_VENDOR_BUTTON_NAME + "'" 
				+ " VALUE='" + FIND_VENDOR_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
			;
	        
			//Refresh vendor information:
			s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + REFRESH_VENDOR_INFO_BUTTON_NAME + "'" 
				+ " VALUE='" + REFRESH_VENDOR_INFO_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
			;
        }else{
			s += 
				"<TD>"
				+ entry.getM_svendor()
				+ "</TD>"
			;
        }
		s += "</TD>";
		
        s += "<TD style=\" text-align:right; font-weight:bold; \">Name:</TD>"
			+ "<TD>" 
			+ entry.getsVendorName()
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOInvoice.ParamsVendorName 
			+ "\" VALUE=\"" + entry.getsVendorName() + "\">"
			+ "</TD>"
			;
        
		s += "</TR>";
		
		//New row:
		s += "<TR>";
		//Terms:
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			ArrayList<String> arrTerms = new ArrayList<String>(0);
			ArrayList<String> arrTermsDescriptions = new ArrayList<String>(0);
			String SQL = "SELECT"
				+ " " + SMTableicvendorterms.sTermsCode
				+ ", " + SMTableicvendorterms.sDescription
				+ " FROM " + SMTableicvendorterms.TableName
				+ " ORDER BY LPAD(" + SMTableicvendorterms.sTermsCode + ", " 
					+ Integer.toString(SMTableicvendorterms.sTermsCodeLength) + ", ' ')"
			;
			//System.out.println("*** SQL = " + SQL);
			//First, add a blank item so we can be sure the user chose one:
			arrTerms.add("");
			arrTermsDescriptions.add("*** Select terms ***");
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
						sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
						+ ".getEditHTML - user: " + sm.getUserName());
				while (rs.next()) {
					arrTerms.add(rs.getString(SMTableicvendorterms.sTermsCode));
					arrTermsDescriptions.add(
						rs.getString(SMTableicvendorterms.sTermsCode)
						+ " - "
						+ rs.getString(SMTableicvendorterms.sDescription)
					);
				}
				rs.close();
			} catch (SQLException e) {
				System.out.println("Error reading terms - " + e.getMessage());
				s += "<B>Error reading terms codes.</B><BR>";
			}
			
			s += "<TD style=\" text-align:right; font-weight:bold; \">Terms:</TD>";
			s +=
				"<TD>"
				+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
					ICPOInvoice.ParamsTerms, 
					arrTerms, 
					entry.getM_sterms(), 
					arrTermsDescriptions
				)
			;
	
			//Button to calculate terms:
			s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + CALCULATE_TERMS_BUTTON_NAME + "'" 
			+ " VALUE='" + CALCULATE_TERMS_BUTTON_LABEL + "'" 
			+ " STYLE='height: 0.20in'>"
			;
		}else{
			s += "<TD style=\" text-align:right; font-weight:bold; \">Terms:</TD>"
				+ "<TD>"
				+ entry.getM_sterms()
				+ "</TD>"
				;
		}
		
		s += "</TD>";
		
		//Discount amount:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Discount amt.:</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParambdDiscount + "\""
	        	+ " VALUE=\"" + entry.getM_sdiscount() + "\""
	        	+ " SIZE=" + "10"
	        	+ " MAXLENGTH=" + "13"
	        	+ ">"
	        	+ "</TD>"
	        ;
		}else{
			s += "<TD>"
				+ entry.getM_sdiscount()
				+ "</TD>"
			;
		}
		s += "</TR>";

		s += "<TR>";
		//Due date
		s += "<TD style=\" text-align:right; font-weight:bold; \">Due date:</TD>";
		
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamdatDue + "\""
				+ " VALUE=\"" + entry.getM_sdatdue() + "\""
				+ " SIZE=10"
				+ " MAXLENGTH=10"
				//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(ICPOInvoice.ParamdatDue, getServletContext())
				+ "</TD>"
			;
		}else{
			s += "<TD>"
				+ entry.getM_sdatdue()
				+ "</TD>"
			;
		}
		
		//Discount date
		s += "<TD style=\" text-align:right; font-weight:bold; \">Discount date:</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
		s += 
			"<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamdatDiscount + "\""
			+ " VALUE=\"" + entry.sgetdatdiscountShowingZeroDateAsBlank() + "\""
			+ " SIZE=10"
			+ " MAXLENGTH=10"
			//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(ICPOInvoice.ParamdatDiscount, getServletContext())
			+ "</TD>"
		;
		}else{
			s += "<TD>"
				+ entry.sgetdatdiscountShowingZeroDateAsBlank()
				+ "</TD>"
			;
		}
		s += "</TR>";
		
		//New row:
		s += "<TR>";
		//Description:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Description:</TD>";
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TD COLSPAN=3>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOInvoice.ParamsDescription + "\""
				+ " VALUE=\"" + entry.getM_sdescription() + "\""
				+ " SIZE=40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoinvoiceheaders.sdescriptionLength)
				+ " STYLE=\"width: " + "3.5" + " in; height: 0.25in\""
				+ ">"
				+ "</TD>"
			;
		}else{
			s += "<TD COLSPAN=3>"
				+ entry.getM_sdescription()
				+ "</TD>"
			;
		}
		s += "</TR>";
		
		//Record the number of lines:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + NUMBER_OF_LINES
		+ "\" VALUE=\"" + Integer.toString(entry.getLines().size()) + "\">";
		
		s += "</TABLE>";
		
		//If the invoice has already been saved, display the receipt lines:
		if (entry.getM_slid().compareToIgnoreCase("-1") != 0){
			try {
				//List the receipt lines, showing any amounts invoiced on each:
				s += listReceiptLines(sm, entry, bEditingInvoiceAllowed, bEditingReceiptAllowed, bPrintPurchaseOrdersAllowed, req);
			} catch (SQLException e) {
				throw e;
			}
		}
		
		return s;
	}
	private String listReceiptLines(
			SMMasterEditEntry sm, 
			ICPOInvoice entry, 
			boolean bEditingInvoiceAllowed,
			boolean bEditingReceiptAllowed,
			boolean bPrintPurchaseOrdersAllowed,
			HttpServletRequest req
		) throws SQLException{
		String s = "";
		s += "<BR>";
		s += "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		s += printReceivedLinesHeader();

		boolean bAdditionalLineHeadingPrinted = false;
		for (int i = 0; i < entry.getLines().size(); i++) {
			//We only want lines from the actual PO receipts in this section:
			ICPOInvoiceLine line = entry.getLines().get(i);
			String sLineNumber = Integer.toString(i + 1);
			//If the invoice has NOT been posted and exported, the invoice costs should be editable:
			String sExpenseAcctField = line.getsexpenseaccount();
			String sGLFinderButton = "&nbsp;";
			String sInvoicedCostField = line.getsinvoicedcost();
			String sRemoveLineButton = "N/A";
			
			boolean bIsStockInventory = false;
			//If it IS a real inventory item, then determine if it's stock or not:
			ICItem item = new ICItem(line.getsitemnumber());
			if (line.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
				if (!item.load(getServletContext(), sm.getsDBID())){
					throw new SQLException("Error [1519163737] - could not load item '" + line.getsitemnumber() + "' - " + item.getErrorMessageString() + ".");
				}
				bIsStockInventory = item.getNonStockItem().compareToIgnoreCase("0") == 0;
			}

			//IF this invoice has NOT YET BEEN EXPORTED - then we can edit it:
			if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
				
				sRemoveLineButton = "<INPUT TYPE=SUBMIT NAME='" + REMOVE_RECEIPT_LINE_BUTTON_NAME
					+ line.getsporeceiptlineid() + "'" 
					+ " VALUE='" + REMOVE_RECEIPT_LINE_BUTTON_LABEL + "'" 
					+ " STYLE='height: 0.24in'>"
				;
				
				sExpenseAcctField = "<INPUT TYPE=TEXT NAME=\"" 
					+ "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsexpenseaccount 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
					+ "\""
					+ " VALUE=\"" + sExpenseAcctField + "\""
					+ " SIZE=15"
					+ " MAXLENGTH=" + SMTableicpoinvoicelines.sexpenseaccountLength
					+ " STYLE=\"width: " + "1.5" + " in; height: 0.25in\""
					+ ">"
				;
				
				sGLFinderButton = "<INPUT TYPE=SUBMIT NAME='" + FIND_EXPENSE_ACCT_PARAMETER 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "'" 
					+ " VALUE='" + FIND_EXPENSE_ACCT_LABEL + "'" 
					+ " STYLE='height: 0.24in'>"
				;
				
				sInvoicedCostField = "<INPUT TYPE=TEXT NAME=\"" 
					+ "ICPOInvoiceLine" + ICPOInvoiceLine.Parambdinvoicedcost 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
					+ "\""
					+ " VALUE=\"" + sInvoicedCostField + "\""
					+ " SIZE=10"
					+ " MAXLENGTH=13"
					+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
					+ ">"
				;
			}
			//IF they are real receipt lines:
			if (entry.getLines().get(i).getsporeceiptlineid().compareToIgnoreCase("-1") != 0){
				String sInventoryItemFlag = "Y";
				
				if (line.getsnoninventoryitem().compareToIgnoreCase("1") == 0){
					sInventoryItemFlag = "N";
				}
				
				String sNonStockFlag = "N";
				if (line.getsnoninventoryitem().compareToIgnoreCase("1") == 0){
					sNonStockFlag = "(N/A)";
				}else{
					if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
						sNonStockFlag = "Y";
					}
				}
				
				s += "<TR>"
					+ "<TD style=\" text-align:right; \">"
					+ sLineNumber
					+ "</TD>"
				;
				//bEditingReceiptAllowed
				String sReceiptID = line.getsporeceiptid();
				String sPOID = getPOID(line.getsporeceiptid(), 
										sm.getsDBID(), 
										getServletContext(), 
										sm.getUserName(),
										sm.getUserID(),
										sm.getFullUserName()
						);
				String sReceiptIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICEditReceiptEdit"
				+ "?" + ICPOReceiptHeader.Paramlpoheaderid + "=" 
				+ sPOID
				+ "&" + ICPOReceiptHeader.Paramlid + "=" 
				+ line.getsporeceiptid()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() 
				+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sReceiptID) + "</A>";

				if (bEditingReceiptAllowed){
					s += "<TD style=\" text-align:right; \">"
						+ sReceiptIDLink
						+ "</TD>"
					;	
				}else{
					s += "<TD style=\" text-align:right; \">"
						+ sReceiptID
						+ "</TD>"
					;
				}
				
				String sPOIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICPrintPOGenerate"
				+ "?StartingPOID=" + sPOID
				+ "&EndingPOID=" + sPOID
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() 
				+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sPOID) + "</A>";

				if (bPrintPurchaseOrdersAllowed){
					s += "<TD style=\" text-align:right; \">"
						+ sPOIDLink
						+ "</TD>"
					;	
				}else{
					s += "<TD style=\" text-align:right; \">"
						+ sPOID
						+ "</TD>"
					;
				}
				
				s += "<TD style=\" text-align:right; \">"
					+ line.getsqtyreceived()
					+ "</TD>"
					+ "<TD>"
					+ line.getsitemnumber()
					+ "</TD>"
					+ "<TD>"
					+ line.getsitemdescription()
					+ "</TD>"
					+ "<TD>"
					+ line.getsunitofmeasure()
					+ "</TD>"
					+ "<TD>"
					;
				
				//If it's a non inventory item, OR a non-stock inventory item, allow the GL to be changed:
				if(bIsStockInventory){
					s += line.getsexpenseaccount();
				}else{
					s += sExpenseAcctField 
						+ "&nbsp;" + sGLFinderButton;
				}
				
				s += "</TD>"
					+ "<TD>"
					+ sInventoryItemFlag
					+ "</TD>"
					+ "<TD>"
					+ sNonStockFlag
					+ "</TD>"
					+ "<TD style=\" text-align:right; \">"
					+ line.getsreceivedcost()
					+ "</TD>"
				;
			//Else if they are just 'additional' lines (like freight, etc.):	
			}else{
				//If the 'additional line' headings haven't been printed yet:
				if (!bAdditionalLineHeadingPrinted){
					if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
						//Add a text box to add another receipt:
						s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns) + ">"
							+ "<INPUT TYPE=TEXT NAME=\"" + INPUT_RECEIPT_FIELD + "\""
				        	+ " VALUE=\"" 
				        	+ clsManageRequestParameters.get_Request_Parameter(INPUT_RECEIPT_FIELD, sm.getRequest()) 
				        	+ "\""
				        	+ " SIZE=" + "10"
				        	+ " MAXLENGTH=" + "12"
				        	+ ">"
				        ;
						
						//Finder for receipt IDs:
						s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + FIND_RECEIPT_BUTTON_NAME + "'" 
						+ " VALUE='" + FIND_RECEIPT_BUTTON_LABEL + "'" 
						+ " STYLE='height: 0.24in'>"
						;
						
						//Button to add the receipt:
						s += ""
							+ "&nbsp;<INPUT TYPE=SUBMIT NAME='" + ADD_RECEIPT_BUTTON_NAME + "'" 
							+ " VALUE='" + ADD_RECEIPT_BUTTON_LABEL + "'" 
							+ " STYLE='height: 0.24in'>"
						;
						
						//Button to update the vendor:
						s += ""
							+ "&nbsp;<INPUT TYPE=SUBMIT NAME='" + UPDATE_VENDOR_BUTTON_NAME + "'" 
							+ " VALUE='" + UPDATE_VENDOR_BUTTON_LABEL + "'" 
							+ " STYLE='height: 0.24in'>"
						;
						s += "&nbsp;<LABEL>" + UPDATE_VENDOR_CONFIRM_LABEL + "<INPUT TYPE=CHECKBOX NAME=\"" 
							+ UPDATE_VENDOR_CONFIRM_CHECKBOX + "\"></LABEL>";
						
						//Button to invoice all lines:
						s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + INVOICE_ALL_LINES_BUTTON_NAME + "'" 
							+ " VALUE='" + INVOICE_ALL_LINES_BUTTON_LABEL + "'" 
							+ " STYLE='height: 0.24in'>"
							+ "</TD></TR>"
						;
					}
					s += printAdditionalLinesHeader();
					bAdditionalLineHeadingPrinted = true;
				}
				s += "<TR>"
					+ "<TD style=\" text-align:right; \">"
					+ sLineNumber
					+ "</TD>"
				;
				s += "<TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns - 5) 
					+ " style=\" text-align:left; \">"
					+ line.getsitemdescription()
					+ "</TD>"
					+ "<TD>"
					+ sExpenseAcctField
					+ "</TD>"
					+ "<TD>"
					+ sGLFinderButton
					+ "</TD>"
				;
			}
			
			s += "<TD style=\" text-align:center; \">"
				+ sRemoveLineButton
				+ "</TD>"
			;
			
			s += "<TD COLSPAN=2 style=\" text-align:right; \">"
				+ sInvoicedCostField;
			
			//Store all the ineditable fields in hidden variables:
			s += storeHiddenLineVariables(line, i)
				+ "</TD>"
				+ "</TR>"
			;
		}
		
		//Add a line for additional expenses:
		//If we STILL haven't printed the 'additional line heading' then print it now:
		if (!bAdditionalLineHeadingPrinted){
			if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
				//Add a text box to add another receipt:
				s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns) + ">"
					+ "<INPUT TYPE=TEXT NAME=\"" + INPUT_RECEIPT_FIELD + "\""
		        	+ " VALUE=\"" 
		        	+ clsManageRequestParameters.get_Request_Parameter(INPUT_RECEIPT_FIELD, sm.getRequest()) 
		        	+ "\""
		        	+ " SIZE=" + "10"
		        	+ " MAXLENGTH=" + "12"
		        	+ ">"
		        ;
				
				//Finder for receipt IDs:
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + FIND_RECEIPT_BUTTON_NAME + "'" 
				+ " VALUE='" + FIND_RECEIPT_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				;
				
				//Button to add the receipt:
				s += ""
					+ "&nbsp;<INPUT TYPE=SUBMIT NAME='" + ADD_RECEIPT_BUTTON_NAME + "'" 
					+ " VALUE='" + ADD_RECEIPT_BUTTON_LABEL + "'" 
					+ " STYLE='height: 0.24in'>"
				;

				//Button to update the vendor:
				s += ""
					+ "&nbsp;<INPUT TYPE=SUBMIT NAME='" + UPDATE_VENDOR_BUTTON_NAME + "'" 
					+ " VALUE='" + UPDATE_VENDOR_BUTTON_LABEL + "'" 
					+ " STYLE='height: 0.24in'>"
				;
				s += "&nbsp;<LABEL>" + UPDATE_VENDOR_CONFIRM_LABEL + "<INPUT TYPE=CHECKBOX NAME=\"" 
						+ UPDATE_VENDOR_CONFIRM_CHECKBOX + "\"></LABEL>";

				//Button to invoice all lines:
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + INVOICE_ALL_LINES_BUTTON_NAME + "'" 
					+ " VALUE='" + INVOICE_ALL_LINES_BUTTON_LABEL + "'" 
					+ " STYLE='height: 0.24in'>"
					+ "</TD></TR>"
				;
			}
			s += printAdditionalLinesHeader();
			bAdditionalLineHeadingPrinted = true;
			
		}
		
		if((entry.getsnumberofnewlines().compareToIgnoreCase("") == 0 || entry.getsnumberofnewlines() == null) ){
			entry.setsnumberofnewlines("1");	
		}
		
		if(entry.getsNewLineInvoicedCost().isEmpty()){			
			entry.getsNewLineDescription().add("");
			entry.getsNewLineExpenseAcct().add("");
			entry.getsNewLineInvoicedCost().add("");
		}
		for(int i = 0; i < Integer.parseInt(entry.getsnumberofnewlines()) ; i++){
			if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
				s += printNewAdditionalLine(entry, i);
			}
		}
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOInvoice.ParamsNumberOfNewLines
				+ "\" VALUE=\"" + entry.getsnumberofnewlines() + "\">";
		
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
		s += "<TR><TD>"
		+ "<INPUT TYPE=SUBMIT NAME='" + ADD_NEW_LINE_PARAMETER + "'" 
		+ " VALUE='" + ADD_NEW_LINE_LABEL + "'" 
		+ " STYLE='height: 0.24in'>"
		+"</TD></TR>";
		}
		
		//Add a row to the table to show the totals:
		if (entry.getM_sexportsequencenumber().compareToIgnoreCase("0") == 0){
			s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns - 2) + " style=\" text-align:right; \">"
				+ "<INPUT TYPE=SUBMIT NAME='" + RECALCULATETOTALS_BUTTON_NAME + "'" 
				+ " VALUE='" + RECALCULATETOTALS_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				+ "</TD>"
			;
		}else{
			s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns- 2) + " style=\" text-align:right; \">"
				+ "&nbsp;"
				+ "</TD>"
			;
		}
		s += "<TD style=\" text-align:right; \">"
			+ "<B>LINE TOTAL:</B>"
			+ "</TD>"
		;

		s += "<TD style=\" text-align:right; \">"
			+ entry.getLineTotalAsString()
			+ "</TD>"
		;

		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String getPOID(
			String sReceiptID, 
			String sDBID, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
		){
		String s = "PONOTFOUND";
		ICPOReceiptHeader rec = new ICPOReceiptHeader();
		rec.setsID(sReceiptID);
		if(!rec.load(context, sDBID, sUserID, sUserFullName)){
			return s;
		}else{
			return rec.getspoheaderid();
		}
	}
	private String printNewAdditionalLine(ICPOInvoice entry, int sNewLineNumber){
		
		String s = "";
			s += "<TR>"
		
			//Line number:
			+ "<TD style=\" text-align:right; \">&nbsp;</TD>"
		
			//Description:
			+ "<TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns - 5) + " style=\" text-align:left; \">"
			+ "<INPUT TYPE=TEXT NAME=\"ICPOInvoiceNEWLine" + ICPOInvoice.ParamsNewLineDesc
				+ Integer.toString(sNewLineNumber) + "\""
				+ " VALUE=\"" + entry.getsNewLineDescription(sNewLineNumber) + "\""
				//+ " SIZE=10"
				+ " MAXLENGTH=" + SMTableicpoinvoicelines.sitemdescriptionLength
				+ " STYLE=\"width: " + "3" + " in; height: 0.25in\""
				+ ">"
			+ "</TD>"

			//Expense acct:
			
			+ "<TD style=\" text-align:left; \">"
			/*
			+ "<SELECT NAME=\"" + ICPOInvoice.ParamsNewLineExpenseAcct + "\"" + ">"
			//Add one for the 'blank':
			+ "<OPTION VALUE=\"" + "" + "\"";
			
			if (entry.getsNewLineExpenseAcct().compareToIgnoreCase("") == 0){
				s += " selected=YES ";
			}
			s += ">** Choose an expense account **"
				+ "</OPTION>";
			for (int i = 0; i < m_sGLValues.size(); i++){
				s += "<OPTION";
				if (m_sGLValues.get(i).compareToIgnoreCase(entry.getsNewLineExpenseAcct()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + m_sGLValues.get(i).toString() + "\">" + m_sGLDescriptions.get(i).toString();
				s += "</OPTION>";
			}
	    	s += "</SELECT>"
	    	+ "</TD>"
	    	*/
			
			+ "<INPUT TYPE=TEXT NAME=\"ICPOInvoiceNEWLine" 
				+ ICPOInvoice.ParamsNewLineExpenseAcct
				+  Integer.toString(sNewLineNumber) + "\""
				+ " VALUE=\"" + entry.getsNewLineExpenseAcct(sNewLineNumber) + "\""
				//+ " SIZE=10"
				+ " MAXLENGTH=" + SMTableicpoinvoicelines.sexpenseaccountLength
				+ " STYLE=\"width: " + "1.0" + " in; height: 0.25in\""
				+ ">"
				
			+ "</TD>"
	    	+ "<TD style=\" text-align:left; \">"
	    	+ "<INPUT TYPE=SUBMIT NAME='" + FIND_EXPENSE_ACCT_PARAMETER + "NEW" + Integer.toString(sNewLineNumber) + "'" 
				+ " VALUE='" + FIND_EXPENSE_ACCT_LABEL +"'" 
				+ " STYLE='height: 0.24in'>"
			
			+ "</TD>"

			//Invoiced cost:
			+ "<TD COLSPAN=2 style=\" text-align:right; \">"
			+ "<INPUT TYPE=TEXT NAME=\"ICPOInvoiceNEWLine" 
				+ ICPOInvoice.ParamsNewLineInvoicedCost
				+  Integer.toString(sNewLineNumber) + "\""
				+ " VALUE=\"" + entry.getsNewLineInvoicedCost(sNewLineNumber) + "\""
				+ " SIZE=10"
				+ " MAXLENGTH=13"
				+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
			+ "</TD>"
			+ "</TR>"
		;
		return s;
	}
	private String printReceivedLinesHeader(){
		iNumberOfDetailColumns = 0;
		String s = 
			"<TR>"
			+ "<TD style=\" text-align:right; vertical-align:bottom; font-weight:bold; \">Line&nbsp;#</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:right; vertical-align:bottom; font-weight:bold; \">Receipt&nbsp;#</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:right; vertical-align:bottom; font-weight:bold; \">PO&nbsp;#</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Qty recv'd</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Item</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Description</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">UOM</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Expense acct</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Inv. item?</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Non-stock?</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:right; vertical-align:bottom; font-weight:bold; \">Recv'd cost</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:center; vertical-align:bottom; font-weight:bold; \">Delete ?</TD>";
			iNumberOfDetailColumns++;
			s += "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Invoiced cost</TD>";
			iNumberOfDetailColumns++;
			s += "</TR>"
		;
		return s;
	}
	private String printAdditionalLinesHeader(){
		return
			"<TR>"
			+ "<TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns) + " style=\" text-align:left; font-weight:bold; \">ADDITIONAL LINES</TD>"
			+ "</TR>"
			+ "<TR>"
			+ "<TD style=\" text-align:right; font-weight:bold; \">Line #</TD>"
			+ "<TD COLSPAN=" + Integer.toString(iNumberOfDetailColumns - 5) 
				+ " style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Description</TD>"
			+ "<TD style=\" text-align:left; vertical-align:bottom; font-weight:bold; \">Expense acct</TD>"
			+ "<TD COLSPAN=3 style=\" text-align:right; vertical-align:bottom; font-weight:bold; \">Invoiced cost</TD>"
			+ "</TR>"
		;
	}
	private String storeHiddenLineVariables(ICPOInvoiceLine line, int iRowNumber){
		return
		//icpoinvoiceline ID
		"<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramlid
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getslid() + "\">"

		//qty received:
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Parambdqtyreceived
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" 
		+ line.getsqtyreceived() 
		+ "\">"

		//received cost
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Parambdreceivedcost
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" 
		+ line.getsreceivedcost()
		+ "\">"
		
		//non-inventory item
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramlnoninventoryitem
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsnoninventoryitem() + "\">"

		//invoice header ID
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramlpoinvoiceheaderid
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getspoinvoiceheaderid() + "\">"
		
		//receipt ID
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptid
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsporeceiptid() + "\">"
		
		//receipt line ID
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptlineid
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsporeceiptlineid() + "\">"

		//invoice number
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsinvoicenumber
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsinvoicenumber() + "\">"

		//item description
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemdescription
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsitemdescription().replace("\"", "&quot;") + "\">"
		
		//item number
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemnumber
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsitemnumber() + "\">"

		//location
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramslocation
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getslocation() + "\">"

		//UOM
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsunitofmeasure
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsunitofmeasure() + "\">"
		
		//Expense acct
		+ "<INPUT TYPE=HIDDEN NAME=\"" + "ICPOInvoiceLine" + ICPOInvoiceLine.Paramsexpenseaccount
		+ clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6)
		+ "\" VALUE=\"" + line.getsexpenseaccount() + "\">"
		;
	}
	private boolean loadGLList(SMMasterEditEntry smedit){
        m_sGLValues.clear();
        m_sGLDescriptions.clear();
        try{
	        String sSQL = MySQLs.Get_GL_Account_List_SQL(false);

	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	smedit.getsDBID(),
		        	"MySQL",
		        	this.toString() + ".loadGLList (1) - User: " + smedit.getUserName());
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsGLAccts.next()){
	        	m_sGLValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	m_sGLDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
