package smic;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smap.APTermsCalculator;
import smap.APVendor;
import smar.SMOption;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpoinvoicelines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableicvendorterms;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;

public class ICPOInvoice extends clsMasterEntry{
	public static final String ParamObjectName = "PO Invoice";

	public static final String ParamlID = "lid";
	public static final String ParamsInvoiceNumber = "sinvoicenumber";
	public static final String ParamdatInvoice = "datinvoice";
	public static final String ParamsTerms = "sterms"; 
	public static final String ParamdatDiscount = "datdiscount";
	public static final String ParamdatDue = "datdue";
	public static final String ParambdDiscount = "bddiscount"; 
	public static final String ParamlExportSequenceNumber = "lExportSequenceNumber"; 
	public static final String ParambdInvoiceTotal = "bdinvoicetotal"; 
	public static final String ParamsVendor = "svendor";
	public static final String ParamsVendorName = "svendorname";
	public static final String ParamsReceivedAmount = "bdreceivedamount";
	public static final String ParamsDescription = "sdescription";
	public static final String ParamsTaxJurisdiction = "staxjurisdiction";
	public static final String ParamdatEntered = "datentered";
	public static final String Paramitaxid = "itaxid";
	public static final String Parambdtaxrate = "bdtaxrate";
	public static final String Paramstaxtype = "staxtype";
	public static final String Paramicalculateonpurchaseorsale = "icalculateonpurchaseorsale";
	public static final String Paramiinvoiceincludestax = "iinvoiceincludestax";

	public static final String ParamsNewLineDesc = "snewlinedesc";
	public static final String ParamsNewLineExpenseAcct = "snewlineexpenseacct";
	public static final String ParamsNewLineInvoicedCost = "snewlineinvoicedcost";
	public static final String ParamsNumberOfNewLines = "sNumberOfNewLines";

	private String m_slid;
	private String m_sinvoicenumber;
	private String m_sdatinvoice;
	private String m_sterms;
	private String m_sdatdiscount;
	private String m_sdatdue;
	private String m_sdiscount;
	private String m_sexportsequencenumber;
	private String m_sinvoicetotal;
	private String m_svendor;
	private String m_svendorname;
	private String m_sNewRecord;
	private String m_sreceivedamount;
	private String m_snumberofnewlines;
	private String m_sdescription;
	private String m_staxjurisdiction;
	private String m_sdatentered;
	private String m_itaxid;
	private String m_bdtaxrate;
	private String m_staxtype;
	private String m_icalculateonpurchaseorsale;
	private String m_iinvoiceincludestax;
	private ArrayList<String> m_arrNewDescLine = new ArrayList<String>(0);
	private ArrayList<String> m_arrNewAcctLine = new ArrayList<String>(0);
	private ArrayList<String> m_arrNewAmountLine = new ArrayList<String>(0);	
	private ArrayList<ICPOInvoiceLine> m_arrLines = new ArrayList<ICPOInvoiceLine>(0);
	private boolean bDebugMode = false;

	public ICPOInvoice() {
		super();
		initInvoiceVariables();
	}

	ICPOInvoice(HttpServletRequest req){
		super(req);
		initInvoiceVariables();

		m_slid = clsManageRequestParameters.get_Request_Parameter(ICPOInvoice.ParamlID, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}

		m_sinvoicenumber = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamsInvoiceNumber, req).trim().toUpperCase().replace("&quot;", "\"");

		m_sdatinvoice = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamdatInvoice, req).trim().replace("&quot;", "\"");
		if(m_sdatinvoice.compareToIgnoreCase("") == 0){
			m_sdatinvoice = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		m_sdatentered = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamdatEntered, req).trim().replace("&quot;", "\"");
		if(m_sdatentered.compareToIgnoreCase("") == 0){
			m_sdatentered = clsDateAndTimeConversions.now("M/d/yyyy");
		}
		
		m_sterms = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamsTerms, req).trim().toUpperCase().replace("&quot;", "\"");

		m_sdatdiscount = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamdatDiscount, req).trim().replace("&quot;", "\"");
		if(m_sdatdiscount.compareToIgnoreCase("") == 0){
			m_sdatdiscount = EMPTY_DATE_STRING;
		}

		m_sdatdue = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamdatDue, req).trim().replace("&quot;", "\"");
		if(m_sdatdue.compareToIgnoreCase("") == 0){
			m_sdatdue = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		m_sdiscount = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParambdDiscount, req).trim().replace("&quot;", "\"");
		if (m_sdiscount.compareToIgnoreCase("") == 0){
			m_sdiscount = "0.00";
		}

		if(req.getParameter(ICPOInvoice.ParamlExportSequenceNumber) == null){
			m_sexportsequencenumber = "0";
		}

		m_sinvoicetotal = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParambdInvoiceTotal, req).trim().replace("&quot;", "\"");
		if (m_sinvoicetotal.compareToIgnoreCase("") == 0){
			m_sinvoicetotal = "0.00";
		}

		m_svendor = clsManageRequestParameters.get_Request_Parameter(ParamsVendor, req).trim().replace("&quot;", "\"");
		m_svendorname = clsManageRequestParameters.get_Request_Parameter(ParamsVendorName, req).trim().replace("&quot;", "\"");

		m_sreceivedamount = clsManageRequestParameters.get_Request_Parameter(
				ICPOInvoice.ParamsReceivedAmount, req).trim().replace("&quot;", "\"");
		if (m_sreceivedamount.compareToIgnoreCase("") == 0){
			m_sreceivedamount = "0.00";
		}

		m_sdescription = clsManageRequestParameters.get_Request_Parameter(ParamsDescription, req).trim().replace("&quot;", "\"");

		m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(ParamsTaxJurisdiction, req).trim().replace("&quot;", "\"");
/*		//The tax drop-down combines the tax class and the tax group, so read that here:
		String sCombinedTaxInfo = SMUtilities.get_Request_Parameter(ICEnterInvoiceEdit.TAX_DROP_DOWN_PARAM, req).trim();
		if (sCombinedTaxInfo.compareToIgnoreCase("") != 0){
			try {
				//m_staxclass = Integer.toString(Integer.parseInt(sCombinedTaxInfo.substring(0, 6)));
				m_staxjurisdiction = sCombinedTaxInfo.substring(6, sCombinedTaxInfo.length());
			} catch (NumberFormatException e) {
				//m_staxclass = "";
				m_staxjurisdiction = "";
			}
		}
*/		
		m_itaxid = clsManageRequestParameters.get_Request_Parameter(Paramitaxid, req).trim().replace("&quot;", "\"");
		m_bdtaxrate = clsManageRequestParameters.get_Request_Parameter(Parambdtaxrate, req).trim().replace("&quot;", "\"");
		m_staxtype = clsManageRequestParameters.get_Request_Parameter(Paramstaxtype, req).trim().replace("&quot;", "\"");
		m_icalculateonpurchaseorsale = clsManageRequestParameters.get_Request_Parameter(Paramicalculateonpurchaseorsale, req).trim().replace("&quot;", "\"");
		m_iinvoiceincludestax  = clsManageRequestParameters.get_Request_Parameter(Paramiinvoiceincludestax, req).trim().replace("&quot;", "\"");
		
		//Read the PO Invoice Lines:
		//Set up an arraylist to hold all the vendor items:
		String sNumberOfLines = clsManageRequestParameters.get_Request_Parameter(ICEnterInvoiceEdit.NUMBER_OF_LINES, req);
		//Size the array for the number of current vendor item records plus one for a 'new' entry:
		m_arrLines.clear();
		int iNumberOfLines = 0;
		try {
			iNumberOfLines = Integer.parseInt(sNumberOfLines);
		} catch (NumberFormatException e1) {
			//Don't hold anything up, the line count will just be zero...
		}
		for (int i = 0; i < iNumberOfLines; i++){
			ICPOInvoiceLine line = new ICPOInvoiceLine();
			m_arrLines.add(line);
		}

		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdinvoicedcost)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdinvoicedcost).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsinvoicedcost(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
				//If the invoiced cost is a blank, set it to '0.00':
				if (m_arrLines.get(Integer.parseInt(sRecordNumber))
						.getsinvoicedcost().compareToIgnoreCase("") == 0){
					m_arrLines.get(Integer.parseInt(sRecordNumber)).setsinvoicedcost("0.00");
				}
				if (bDebugMode){
					System.out.println(
							"In " + this.toString() + " invoiced cost on record number " + sRecordNumber 
							+ " = " + m_arrLines.get(Integer.parseInt(sRecordNumber)).getsinvoicedcost());
				}
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlid)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlid).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setslid(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdqtyreceived)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdqtyreceived).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsqtyreceived(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdreceivedcost)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Parambdreceivedcost).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsreceivedcost(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlnoninventoryitem)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlnoninventoryitem).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsnoninventoryitem(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}

			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptlineid)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptlineid).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsporeceiptlineid(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptid)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramlporeceiptid).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsporeceiptid(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsexpenseaccount)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsexpenseaccount).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsexpenseaccount(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsinvoicenumber)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsinvoicenumber).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsinvoicenumber(
						m_sinvoicenumber);
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemdescription)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemdescription).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsitemdescription(
						clsManageRequestParameters.get_Request_Parameter(sParam, req).trim().replace("&quot;", "\""));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemnumber)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsitemnumber).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsitemnumber(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramslocation)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramslocation).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setslocation(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			if (sParam.contains("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsunitofmeasure)){
				String sRecordNumber = sParam.substring(
						("ICPOInvoiceLine" + ICPOInvoiceLine.Paramsunitofmeasure).length(), sParam.length());
				m_arrLines.get(Integer.parseInt(sRecordNumber)).setsunitofmeasure(
						clsManageRequestParameters.get_Request_Parameter(sParam, req));
			}
			
		}

		m_snumberofnewlines = clsManageRequestParameters.get_Request_Parameter(ParamsNumberOfNewLines, req).trim().replace("&quot;", "\"");
		if(m_snumberofnewlines == null || m_snumberofnewlines.compareToIgnoreCase("") == 0){
			m_snumberofnewlines = "1";
		}
		
		for(int i= 0; i < Integer.parseInt(m_snumberofnewlines); i++){
			String sAmount = clsManageRequestParameters.get_Request_Parameter(
					"ICPOInvoiceNEWLine" + ParamsNewLineInvoicedCost + Integer.toString(i), req).trim().replace("&quot;", "\"");
			m_arrNewAmountLine.add(sAmount);
			
			String sAccount =  clsManageRequestParameters.get_Request_Parameter(
					"ICPOInvoiceNEWLine" + ParamsNewLineExpenseAcct + Integer.toString(i), req).trim().replace("&quot;", "\"");	
			m_arrNewAcctLine.add(sAccount);
			
			String sDescription =  clsManageRequestParameters.get_Request_Parameter(
					"ICPOInvoiceNEWLine" + ParamsNewLineDesc + Integer.toString(i), req).trim().replace("&quot;", "\"");
			m_arrNewDescLine.add(sDescription);
		}

	}

	public boolean load (ServletContext context, String sDBID, String sUserID, String sUserFullName){
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".load - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = load (conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080914]");
		return bResult;

	}
	public boolean load (Connection conn){
		return load (m_slid, conn);
	}
	private boolean load (String sID, Connection conn){

		sID = sID.trim();
		if (sID.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Invoice ID cannot be blank.");
			return false;
		}
		long lID;
		try {
			lID = Long.parseLong(sID);
		} catch (NumberFormatException n) {
			super.addErrorMessage("Invalid invoice ID: '" + sID + "'");
			return false;
		}

		//In case we get a negative one, that indicates that this is actually a NEW invoice, and in that
		//case it can't be loaded:
		if (lID == -1){
			super.addErrorMessage("Invalid invoice ID.");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTableicpoinvoiceheaders.TableName
		+ " WHERE ("
		+ SMTableicpoinvoiceheaders.lid + " = " + sID
		+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableicpoinvoiceheaders.lid));
				m_sinvoicenumber = rs.getString(SMTableicpoinvoiceheaders.sinvoicenumber).trim();
				m_sdatinvoice = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoinvoiceheaders.datinvoice));
				m_sdatentered = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoinvoiceheaders.datentered));
				//System.out.println("[1466175203] - m_sdatentered = '" + m_sdatentered + "' - rs.getString(SMTableicpoinvoiceheaders.datentered) = '" 
				//	+ rs.getString(SMTableicpoinvoiceheaders.datentered) + "'."	
				//);
				m_sterms = rs.getString(SMTableicpoinvoiceheaders.sterms).trim();
				m_sdatdiscount = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoinvoiceheaders.datdiscount));
				m_sdatdue = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoinvoiceheaders.datdue));
				m_sdiscount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bddiscountScale, 
						rs.getBigDecimal(SMTableicpoinvoiceheaders.bddiscount));
				m_sexportsequencenumber = Long.toString(rs.getLong(SMTableicpoinvoiceheaders.lexportsequencenumber));
				m_sinvoicetotal = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bdinvoicetotalScale, 
						rs.getBigDecimal(SMTableicpoinvoiceheaders.bdinvoicetotal));
				m_svendor = rs.getString(SMTableicpoinvoiceheaders.svendor).trim();
				m_svendorname = rs.getString(SMTableicpoinvoiceheaders.svendorname).trim();
				m_sreceivedamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bdreceivedamountScale, 
						rs.getBigDecimal(SMTableicpoinvoiceheaders.bdreceivedamount));
				m_sdescription = rs.getString(SMTableicpoinvoiceheaders.sdescription).trim();
				m_staxjurisdiction = rs.getString(SMTableicpoinvoiceheaders.staxjurisdiction).trim();
				m_itaxid = Long.toString(rs.getLong(SMTableicpoinvoiceheaders.itaxid));
				m_bdtaxrate = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bdtaxrateScale, 
						rs.getBigDecimal(SMTableicpoinvoiceheaders.bdtaxrate));
				m_staxtype = rs.getString(SMTableicpoinvoiceheaders.staxtype).trim();
				m_icalculateonpurchaseorsale = Long.toString(rs.getLong(SMTableicpoinvoiceheaders.icalculateonpurchaseorsale));
				m_iinvoiceincludestax = Long.toString(rs.getLong(SMTableicpoinvoiceheaders.iinvoiceincludestax));
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sID
					+ "' - " + e.getMessage());
			return false;
		}

		//Now load the invoice lines:
		if (!loadInvoiceLines(conn)){
			return false;
		}
		return true;    
	}
	private boolean loadInvoiceLines(Connection conn){
		m_arrLines.clear();
		//Now load the invoice lines:
		String SQL = "SELECT"
			+ " * FROM " + SMTableicpoinvoicelines.TableName
			+ " WHERE ("
			+ "(" + SMTableicpoinvoicelines.lpoinvoiceheaderid + " = " + getM_slid() + ")"
			+ ")"
			+ " ORDER BY IF (" + SMTableicpoinvoicelines.lporeceiptlineid + " = -1, 1, 0)"
			+ ", " + SMTableicpoinvoicelines.lid
			;
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load invoice lines SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			m_arrLines.clear();
			while (rs.next()) {
				ICPOInvoiceLine line = new ICPOInvoiceLine();
				//Load the variables here:
				line.setsexpenseaccount(rs.getString(SMTableicpoinvoicelines.sexpenseaccount));
				line.setsinvoicedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoicelines.bdinvoicedcostScale, 
						rs.getBigDecimal(SMTableicpoinvoicelines.bdinvoicedcost)));
				line.setsinvoicenumber(rs.getString(SMTableicpoinvoicelines.sexpenseaccount));
				line.setsinvoicenumber(this.getM_sinvoicenumber());
				line.setsitemdescription(rs.getString(SMTableicpoinvoicelines.sitemdescription));
				//System.out.println("[1474555294] rs.getString(SMTableicpoinvoicelines.sitemdescription) = '" + rs.getString(SMTableicpoinvoicelines.sitemdescription) + "'");
				line.setsitemnumber(rs.getString(SMTableicpoinvoicelines.sitemnumber));
				line.setslid(Long.toString(rs.getLong(SMTableicpoinvoicelines.lid)));
				line.setslocation(rs.getString(SMTableicpoinvoicelines.slocation));
				line.setsnoninventoryitem(Long.toString(rs.getLong(SMTableicpoinvoicelines.lnoninventoryitem)));
				line.setspoinvoiceheaderid(Long.toString(rs.getLong(SMTableicpoinvoicelines.lpoinvoiceheaderid)));
				line.setsporeceiptlineid(Long.toString(rs.getLong(SMTableicpoinvoicelines.lporeceiptlineid)));
				line.setsporeceiptid(Long.toString(rs.getLong(SMTableicpoinvoicelines.lporeceiptid)));
				line.setsqtyreceived(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoicelines.bdqtyreceivedScale, 
						rs.getBigDecimal(SMTableicpoinvoicelines.bdqtyreceived)));
				line.setsreceivedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoicelines.bdreceivedcostScale, 
						rs.getBigDecimal(SMTableicpoinvoicelines.bdreceivedcost)));
				line.setsunitofmeasure(rs.getString(SMTableicpoinvoicelines.sunitofmeasure));
				m_arrLines.add(line);
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading invoice lines for : '" + getM_slid()
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
	}
	public boolean removeReceiptLine(String sReceiptLineID){
		
		for (int i = 0; i < m_arrLines.size(); i++){
			if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase(sReceiptLineID) == 0){
				m_arrLines.remove(i);
				break;
			}
		}
		
		return true;
	}
	public boolean validate_totals(){

		if (m_arrLines.size() == 0){
			super.addErrorMessage("invoice has no lines on it.");
			return false;
		}
		
		BigDecimal bdInvoiceLineTotals = new BigDecimal(0);
		try {
			bdInvoiceLineTotals = calculateLineTotals();
		} catch (NumberFormatException e1) {
			super.addErrorMessage("Error calculating line totals - " 
					+ e1.getMessage());
			return false;
		}
		BigDecimal bdInvoiceAmount;
		try {
			bdInvoiceAmount = new BigDecimal(this.getM_sinvoicetotal().replace(",", ""));
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid invoice total - '" + this.getM_sinvoicetotal() + "' " 
					+ e.getMessage());
			return false;
		}

		if (bdInvoiceAmount.compareTo(bdInvoiceLineTotals) != 0){	
			super.addErrorMessage("Total of invoice lines (" 
					+ bdInvoiceLineTotals 
					+ ") does not match invoice amount (" 
					+ bdInvoiceAmount + ").");
			return false;
		}

		if (bdInvoiceAmount.compareTo(BigDecimal.ZERO) == 0){
			super.addErrorMessage("invoice cannot be equal to zero.");
			return false;
		}
		
		return true;
	}
	public String getLineTotalAsString(){
		String sLineTotal;
		try {
			sLineTotal = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicpoinvoicelines.bdinvoicedcostScale, calculateLineTotals());
		} catch (NumberFormatException e) {
			return "N/A";
		}

		return sLineTotal;
	}
	public boolean save_with_data_transaction (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName){

		if (
				(m_sexportsequencenumber.compareToIgnoreCase("0") != 0)
				&& (m_sexportsequencenumber.compareToIgnoreCase("-1") != 0)

		){
			super.addErrorMessage("Exported invoices cannot be updated.");
			return false;
		}

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".save_with_data_transaction - user: " + sUserID
				+ " - " + sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		if (!validate_entry_fields(conn, sUserID)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080915]");
			return false;
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction.");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080916]");
			return false;
		}

		SMLogEntry log = new SMLogEntry(sDBID, context);
		if(!save_without_data_transaction (conn, sUser, log)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080917]");
			return false;
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Error committing data transaction.");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080918]");
			return false;
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080919]");
		return true;	

	}
	private boolean save_without_data_transaction (Connection conn, String sUser, SMLogEntry log){

		//First, if there are any additional lines with zero amounts, remove them:
		if (!removeZeroAmountAdditionalLines(conn, sUser)){
			return false;
		}

		String SQL = "";

		//If it's a new record, do an insert:
		boolean bNewRecord = (m_slid.compareToIgnoreCase("-1") == 0);
		if (bNewRecord){
			SQL = "INSERT INTO " + SMTableicpoinvoiceheaders.TableName + " ("
			+ SMTableicpoinvoiceheaders.bddiscount
			+ ", " + SMTableicpoinvoiceheaders.bdinvoicetotal
			+ ", " + SMTableicpoinvoiceheaders.datdiscount
			+ ", " + SMTableicpoinvoiceheaders.datdue
			+ ", " + SMTableicpoinvoiceheaders.datinvoice
			+ ", " + SMTableicpoinvoiceheaders.datentered
			+ ", " + SMTableicpoinvoiceheaders.lexportsequencenumber
			+ ", " + SMTableicpoinvoiceheaders.sinvoicenumber
			+ ", " + SMTableicpoinvoiceheaders.sterms
			+ ", " + SMTableicpoinvoiceheaders.svendor
			+ ", " + SMTableicpoinvoiceheaders.svendorname
			+ ", " + SMTableicpoinvoiceheaders.bdreceivedamount
			+ ", " + SMTableicpoinvoiceheaders.sdescription
			+ ", " + SMTableicpoinvoiceheaders.staxjurisdiction
			+ ", " + SMTableicpoinvoiceheaders.itaxid
			+ ", " + SMTableicpoinvoiceheaders.bdtaxrate
			+ ", " + SMTableicpoinvoiceheaders.staxtype
			+ ", " + SMTableicpoinvoiceheaders.icalculateonpurchaseorsale
			+ ", " + SMTableicpoinvoiceheaders.iinvoiceincludestax
			
			+ ") VALUES ("
			+ m_sdiscount.replace(",", "")
			+ ", " + m_sinvoicetotal.replace(",", "")
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatdiscount) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatdue) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatinvoice) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatentered) + "'"
			+ ", " + m_sexportsequencenumber
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicenumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sterms.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendor.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorname.trim()) + "'"
			+ ", " + m_sreceivedamount.replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_staxjurisdiction.trim()) + "'"
			+ ", " + m_itaxid
			+ ", " + m_bdtaxrate.replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_staxtype.trim()) + "'"
			+ ", " + m_icalculateonpurchaseorsale
			+ ", " + m_iinvoiceincludestax
			+ ")"
			;
		}else{
			//If it's an existing record:
			SQL = "UPDATE " + SMTableicpoinvoiceheaders.TableName + " SET"
			+ " " + SMTableicpoinvoiceheaders.bddiscount + " = " + m_sdiscount.replace(",", "")
			+ ", " + SMTableicpoinvoiceheaders.bdinvoicetotal + " = " + m_sinvoicetotal.replace(",", "")
			+ ", " + SMTableicpoinvoiceheaders.datdiscount + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatdiscount) + "'"
			+ ", " + SMTableicpoinvoiceheaders.datdue + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatdue) + "'"
			+ ", " + SMTableicpoinvoiceheaders.datentered + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatentered) + "'"
			+ ", " + SMTableicpoinvoiceheaders.datinvoice + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatinvoice) + "'"
			+ ", " + SMTableicpoinvoiceheaders.lexportsequencenumber + " = " + m_sexportsequencenumber
			+ ", " + SMTableicpoinvoiceheaders.sinvoicenumber + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_sinvoicenumber) + "'"
			+ ", " + SMTableicpoinvoiceheaders.sterms + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_sterms) + "'"
			+ ", " + SMTableicpoinvoiceheaders.svendor + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_svendor) + "'"
			+ ", " + SMTableicpoinvoiceheaders.svendorname + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_svendorname) + "'"
			+ ", " + SMTableicpoinvoiceheaders.bdreceivedamount + " = " + m_sreceivedamount.replace(",", "")
			+ ", " + SMTableicpoinvoiceheaders.sdescription + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", " + SMTableicpoinvoiceheaders.staxjurisdiction + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_staxjurisdiction) + "'"
			+ ", " + SMTableicpoinvoiceheaders.itaxid + " = " + m_itaxid
			+ ", " + SMTableicpoinvoiceheaders.bdtaxrate + " = " + m_bdtaxrate.replace(",", "")
			+ ", " + SMTableicpoinvoiceheaders.staxtype + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_staxtype) + "'"
			+ ", " + SMTableicpoinvoiceheaders.icalculateonpurchaseorsale + " = " + m_icalculateonpurchaseorsale
			+ ", " + SMTableicpoinvoiceheaders.iinvoiceincludestax + " = " + m_iinvoiceincludestax
			+ " WHERE ("
			+ "(" + SMTableicpoinvoiceheaders.lid + " = " + m_slid + ")"
			+ ")"
			;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			//System.out.println(this.toString() + "Could not insert/update " + ParamObjectName 
			//		+ " - " + ex.getMessage() + ".<BR>");
			super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL
					+ " - " + ex.getMessage());
			return false;
		}
		//If it's a NEW record, get the last insert ID:
		if (bNewRecord){
			SQL = "SELECT LAST_INSERT_ID()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_slid = Long.toString(rs.getLong(1));
				} else {
					super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
					return false;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
				return false;
			}
			// If something went wrong, we can't get the last ID:
			if (m_slid.compareToIgnoreCase("-1") == 0) {
				super.addErrorMessage("Could not get last ID number.");
				return false;
			}
		}

		//Update all the lines, adding them as needed.  Then re-load the lines into the m_ArrLines array.

		//First delete ALL the existing lines for this invoice - we have to do this in case the user chose
		//NOT to invoice some lines from some of the receipts:
		SQL = "DELETE FROM " + SMTableicpoinvoicelines.TableName
		+ " WHERE ("
		+ "(" + SMTableicpoinvoicelines.lpoinvoiceheaderid + " = " + getM_slid() + ")"
		+ ")"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Could not delete invoice lines with : " + SQL
					+ " - " + ex.getMessage());
			return false;
		}

		//Next we'll INSERT all the lines in our array:
		for (int i = 0; i < m_arrLines.size(); i++){
			SQL = "INSERT INTO " + SMTableicpoinvoicelines.TableName + "("
			+ SMTableicpoinvoicelines.bdinvoicedcost
			+ ", " + SMTableicpoinvoicelines.bdqtyreceived
			+ ", " + SMTableicpoinvoicelines.bdreceivedcost
			+ ", " + SMTableicpoinvoicelines.lnoninventoryitem
			+ ", " + SMTableicpoinvoicelines.lpoinvoiceheaderid
			+ ", " + SMTableicpoinvoicelines.lporeceiptid
			+ ", " + SMTableicpoinvoicelines.lporeceiptlineid
			+ ", " + SMTableicpoinvoicelines.sexpenseaccount
			+ ", " + SMTableicpoinvoicelines.sinvoicenumber
			+ ", " + SMTableicpoinvoicelines.sitemdescription
			+ ", " + SMTableicpoinvoicelines.sitemnumber
			+ ", " + SMTableicpoinvoicelines.slocation
			+ ", " + SMTableicpoinvoicelines.sunitofmeasure

			+ ") VALUES ("
			+ m_arrLines.get(i).getsinvoicedcost().replace(",", "")
			+ ", " + m_arrLines.get(i).getsqtyreceived().replace(",", "")
			+ ", " + m_arrLines.get(i).getsreceivedcost().replace(",", "")
			+ ", " + m_arrLines.get(i).getsnoninventoryitem()
			+ ", " + this.getM_slid()
			+ ", " + m_arrLines.get(i).getsporeceiptid()
			+ ", " + m_arrLines.get(i).getsporeceiptlineid()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrLines.get(i).getsexpenseaccount()) + "'"
			+ ", '" + this.getM_sinvoicenumber() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrLines.get(i).getsitemdescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrLines.get(i).getsitemnumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrLines.get(i).getslocation()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrLines.get(i).getsunitofmeasure()) + "'"
			+ ")"
			;
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".save_without_data_transaction - save line SQL = " 
						+ SQL);
			}
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				System.out.println(this.toString() + "Could not insert/update invoice lines - " 
						+ ex.getMessage() + ".<BR>");
				super.addErrorMessage("Could not insert/update invoice lines with SQL : " + SQL
						+ " - " + ex.getMessage());
				return false;
			}
		}

		//If there are any NEW additional lines, add them now:
		//If there are any 'new' additional lines to be added, add them now:
		for(int i = 0; i < m_arrNewAmountLine.size(); i++){
		if (m_arrNewAmountLine.get(i).compareToIgnoreCase("") != 0){
			SQL = "INSERT INTO " + SMTableicpoinvoicelines.TableName
			+ "("
			+ " " + SMTableicpoinvoicelines.bdinvoicedcost
			+ ", " + SMTableicpoinvoicelines.bdqtyreceived
			+ ", " + SMTableicpoinvoicelines.bdreceivedcost
			+ ", " + SMTableicpoinvoicelines.lnoninventoryitem
			+ ", " + SMTableicpoinvoicelines.lpoinvoiceheaderid
			+ ", " + SMTableicpoinvoicelines.lporeceiptlineid
			+ ", " + SMTableicpoinvoicelines.sexpenseaccount
			+ ", " + SMTableicpoinvoicelines.sinvoicenumber
			+ ", " + SMTableicpoinvoicelines.sitemdescription
			+ ", " + SMTableicpoinvoicelines.sitemnumber
			+ ", " + SMTableicpoinvoicelines.slocation
			+ ", " + SMTableicpoinvoicelines.sunitofmeasure

			+ ") VALUES ("
			+ m_arrNewAmountLine.get(i).replace(",", "")
			+ ", 0.0000"
			+ ", 0.00"
			+ ", 1"
			+ ", " + this.getM_slid()
			+ ", -1"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrNewAcctLine.get(i)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getM_sinvoicenumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_arrNewDescLine.get(i)) + "'"
			+ ", ''"
			+ ", ''"
			+ ", ''"
			+ ")"
			;
			
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
				setsnumberofnewlines("1");
			}catch (Exception ex) {
				//System.out.println(this.toString() + "Could not add new additional cost line");
				super.addErrorMessage("Could not add new additional cost line with SQL: " + SQL
						+ " - " + ex.getMessage());
				return false;
			}
		}
		}

		//Now re-load the invoice lines BACK into the array:
		if (!loadInvoiceLines(conn)){
			return false;
		}

		//Update the receipt lines
		//First, clear this po invoice id from ALL the receipt lines:
		SQL = "UPDATE"
			+ " " + SMTableicporeceiptlines.TableName
			+ " SET " + SMTableicporeceiptlines.lpoinvoiceid
			+ " = " + Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET)
			+ " WHERE (" 
			+ SMTableicporeceiptlines.lpoinvoiceid + " = " + getM_slid()
			+ ")"
			;

		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			//System.out.println(this.toString() + "Could not clear receipt lines with invoice id - " 
			//		+ ex.getMessage() + ".<BR>");
			super.addErrorMessage("Could not clear receipt lines with SQL: " + SQL
					+ " - " + ex.getMessage());
			//TJR - 2/3/3-2015 - removed this line to reduce logging volume:
			//log.writeEntry(sUser, SMLogEntry.LOG_OPERATION_UPDATEINVNUMONRECEIPT, "FAILED to clear PO invoice ID on receipt lines", SQL, "[1376509408]");
			return false;
		}

		//Next, update all the po receipt lines on this invoice with the invoice id:
		if (m_arrLines.size() > 0){
			//SQL = "UPDATE"
			//	+ " " + SMTableicporeceiptlines.TableName
			//	+ " SET " + SMTableicporeceiptlines.lpoinvoiceid
			//	+ " = " + this.getM_slid()
			//	+ " WHERE " + SMTableicporeceiptlines.lid + " IN ("
			//	;
			//for (int i = 0; i < m_arrLines.size(); i++){
			//	if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase("-1") != 0){
			//		if (i == 0){
			//			SQL += m_arrLines.get(i).getsporeceiptlineid();
			//		}else{
			//			SQL += ", " + m_arrLines.get(i).getsporeceiptlineid();
			//		}
			//	}
			//}
			//SQL += ")";
			
			SQL = "UPDATE"
				+ " " + SMTableicporeceiptlines.TableName
				+ " LEFT JOIN " + SMTableicpoinvoicelines.TableName + " ON " 
				+ SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lporeceiptlineid
				+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
				+ " SET " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpoinvoiceid 
				+ " = " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid
				+ " WHERE (" 
					+ SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid + " = " + getM_slid()
				+ ")"
			;
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				//System.out.println(this.toString() + "Could not update receipt lines with invoice id - " 
				//		+ ex.getMessage() + ".<BR>");
				super.addErrorMessage("Could not update receipt lines with SQL: " + SQL
						+ " - " + ex.getMessage());
				//TJR - 2/3/3-2015 - removed this line to reduce logging volume:
				//log.writeEntry(sUser, SMLogEntry.LOG_OPERATION_UPDATEINVNUMONRECEIPT, "FAILED to update PO invoice ID on receipt lines", SQL, "[1376509518]");
				return false;
			}
		}
		//TJR - 2/3/3-2015 - removed this line to reduce logging volume:
		//log.writeEntry(sUser, SMLogEntry.LOG_OPERATION_UPDATEINVNUMONRECEIPT, "Successfully updated invoice ID's", SQL, "[1376509409]");
		return true;
	}
	public boolean addReceiptLines(String sReceiptID, ServletContext context, String sDBID, String sUser){
		
		boolean bResult = true;
		sReceiptID = sReceiptID.trim();
		if (sReceiptID.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Receipt number cannot be empty.");
			return false;
		}

		try {
			@SuppressWarnings("unused")
			long lReceiptID = Long.parseLong(sReceiptID);
		} catch (NumberFormatException e2) {
			super.addErrorMessage("Receipt number '" + sReceiptID + "' is invalid.");
			return false;
		}
		
		//First, make sure that the receipt selected is for THIS vendor:
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ " FROM " + SMTableicpoheaders.TableName + " LEFT JOIN "
			+ SMTableicporeceiptheaders.TableName + " ON "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				+ " = " + sReceiptID + ")"
			+ ")"
		;
		try {
			ResultSet rsReceipts = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".addReceiptLines, checking vendor - user: " + sUser
			);
			if (rsReceipts.next()){
				if (rsReceipts.getString(SMTableicpoheaders.TableName + "." 
					+ SMTableicpoheaders.svendor).compareToIgnoreCase(getM_svendor()) != 0){
					super.addErrorMessage("This receipt (" + sReceiptID + ") is for a different vendor.");
					rsReceipts.close();
					return false;
				}
			}else{
				super.addErrorMessage("This receipt (" + sReceiptID + ") cannot be found.");
				rsReceipts.close();
				return false;
			}
			rsReceipts.close();
		} catch (SQLException e1) {
			super.addErrorMessage("Error checking vendor on receipt with SQL: " + SQL + " - " + e1.getMessage());
			return false;
		}
		
		//We need to get any receipt lines that EITHER have NO invoice tied to them - OR any that have
		//THIS invoice tied to them, because the user may be choosing to RE-ADD this receipt to the invoice:
		SQL = "SELECT * FROM " + SMTableicporeceiptlines.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + sReceiptID + ")"
				+ " AND (" 
					+ "(" + SMTableicporeceiptlines.lpoinvoiceid + " = " + Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET) + ")"
					+ " OR (" + SMTableicporeceiptlines.lpoinvoiceid + " = " + getM_slid() + ")"
				+ ")"
			+ ")"
			+ " ORDER BY " + SMTableicporeceiptlines.llinenumber
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".addReceiptLines - user: " + sUser 
			);
			while(rs.next()){
				long lReceiptLineID = rs.getLong(SMTableicporeceiptlines.TableName + "."
					+ SMTableicporeceiptlines.lid);
				String sReceiptLineID = Long.toString(lReceiptLineID);
				for (int i = 0; i < m_arrLines.size(); i++){

					if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase(sReceiptLineID) == 0){
						m_arrLines.remove(i);
						break;
					}
				}
				ICPOInvoiceLine line = new ICPOInvoiceLine();
				line.setsexpenseaccount(rs.getString(SMTableicporeceiptlines.sglexpenseacct));
				line.setsinvoicedcost("0.00");
				line.setsinvoicenumber(getM_sinvoicenumber());
				line.setsitemdescription(rs.getString(SMTableicporeceiptlines.sitemdescription));
				line.setsitemnumber(rs.getString(SMTableicporeceiptlines.sitemnumber));
				line.setslid("-1");
				line.setslocation(rs.getString(SMTableicporeceiptlines.slocation));
				line.setsnoninventoryitem(Long.toString(rs.getLong(SMTableicporeceiptlines.lnoninventoryitem)));
				line.setspoinvoiceheaderid(getM_slid());
				line.setsporeceiptid(Long.toString(rs.getLong(SMTableicporeceiptlines.lreceiptheaderid)));
				line.setsporeceiptlineid(Long.toString(rs.getLong(SMTableicporeceiptlines.lid)));
				line.setsqtyreceived(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicpoinvoicelines.bdqtyreceivedScale, rs.getBigDecimal(SMTableicporeceiptlines.bdqtyreceived)));
				line.setsreceivedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoicelines.bdreceivedcostScale, rs.getBigDecimal(SMTableicporeceiptlines.bdextendedcost)));
				line.setsunitofmeasure(rs.getString(SMTableicporeceiptlines.sunitofmeasure));
				m_arrLines.add(line);
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading receipt lines with SQL: " + SQL + " - " + e.getMessage());
			bResult = false;
		}
		
		//Now re-sort the array of lines so that the 'additional lines' are always at the bottom:
		ArrayList<ICPOInvoiceLine> tempLines = new ArrayList<ICPOInvoiceLine>(0);
		//First, add in the lines that come from PO receipts:
		for (int i = 0; i < m_arrLines.size(); i++){
			if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase("-1") != 0){
				tempLines.add(m_arrLines.get(i));
			}
		}
		//Next, add in the lines that DON'T come from PO receipts (i.e., the 'additional' lines, like freight):
		for (int i = 0; i < m_arrLines.size(); i++){
			if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase("-1") == 0){
				tempLines.add(m_arrLines.get(i));
			}
		}
		
		//Finally, copy the temp lines back to the original array:
		m_arrLines.clear();
		for (int i = 0; i < tempLines.size(); i++){
			m_arrLines.add(tempLines.get(i));
		}
		
		return bResult;
	}
	private BigDecimal calculateLineTotals() throws NumberFormatException{

		BigDecimal bdLineInvoicedCostTotal = new BigDecimal(0);

		if (bDebugMode){
			System.out.println("In " + this.toString() + " m_arrLines.size() = " + m_arrLines.size());
		}
		for (int i = 0; i < m_arrLines.size(); i++){
			try {
				BigDecimal bdLineInvoiceCost = new BigDecimal(m_arrLines.get(i).getsinvoicedcost().replace(",", ""));
				bdLineInvoicedCostTotal = bdLineInvoicedCostTotal.add(bdLineInvoiceCost);
			} catch (NumberFormatException e) {
				throw e;
			}
		}

		//Add in any new additional costs:
		for (String sAmount : m_arrNewAmountLine){			
		if (sAmount.compareToIgnoreCase("") != 0){	
			try {
				BigDecimal bdNewLineInvoicedCost = new BigDecimal(sAmount.replace(",", ""));
				bdLineInvoicedCostTotal = bdLineInvoicedCostTotal.add(bdNewLineInvoicedCost);
			} catch (NumberFormatException e) {
				throw e;
			}
		}
		}

		return bdLineInvoicedCostTotal;
	}
	public boolean delete (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName){

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".delete - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = delete (conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080913]");
		return bResult;

	}
	public boolean delete (Connection conn){

		//Validate deletions
		//TODO - are there any conditions under which we cannot delete an invoice?
		if (
				(m_sexportsequencenumber.compareToIgnoreCase("-1") != 0)
				&& (m_sexportsequencenumber.compareToIgnoreCase("0") != 0)
		){
			super.addErrorMessage("Cannot delete an invoice that has already been exported.");
			return false;
		}

		String SQL = "";

		//Delete invoice:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start transaction when deleting invoice.");
			return false;
		}

		//Remove the invoice number from any receipt line that this invoice had been applied to:
		//TJRUPDATERECEIPT
		SQL = "UPDATE"
			+ " " + SMTableicporeceiptlines.TableName
			+ " SET " + SMTableicporeceiptlines.lpoinvoiceid + " = " + Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET)
			+ " WHERE ("
			+ "(" + SMTableicporeceiptlines.lpoinvoiceid + " = " + this.getM_slid() + ")"
			+ ")"
			;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			//System.out.println("In " + this.toString() 
			//		+ " Could not update po receipt line to remove invoice number - " + ex.getMessage());
			super.addErrorMessage("In " + this.toString() 
					+ " Could not update po receipt line to remove invoice number with SQL: " + SQL + " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		//Delete child records first:
		SQL = "DELETE FROM " + SMTableicpoinvoicelines.TableName
		+ " WHERE ("
		+ SMTableicpoinvoicelines.lpoinvoiceheaderid + " = " + m_slid
		+ ")"
		;

		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			//System.out.println("In " + this.toString() 
			//		+ " Could not delete invoice lines with header ID " + m_slid + " - " + ex.getMessage());
			super.addErrorMessage("In " + this.toString() 
					+ " Could not delete invoice lines with header ID " + m_slid + " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		//Now delete the invoice itself:
		SQL = "DELETE FROM " + SMTableicpoinvoiceheaders.TableName
		+ " WHERE ("
		+ SMTableicpoinvoiceheaders.lid + " = " + m_slid
		+ ")"
		;

		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			System.out.println("In " + this.toString() 
					+ " Could not delete invoice with ID " + m_slid + " - " + ex.getMessage());
			super.addErrorMessage("In " + this.toString() 
					+ " Could not delete invoice with ID " + m_slid + " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Could not commit data transaction while deleting invoice.");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		//Empty the values:
		initInvoiceVariables();
		return true;
	}
	
	private boolean validate_entry_lines(Connection conn){
		
		if (m_arrLines.size() == 0){
			return true;
		}
		boolean bResult = true;
		String SQL = "SELECT"
			+ " DISTINCT " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " FROM " + SMTableicporeceiptlines.TableName + " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid + " = "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ " WHERE ("
			;
			for (int i = 0; i < m_arrLines.size(); i++){
				if (i == 0){
					SQL += "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " = "
						+ m_arrLines.get(i).getsporeceiptlineid() + ")";
				}else{
					SQL += " OR (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " = "
						+ m_arrLines.get(i).getsporeceiptlineid() + ")";
				}
			}
			SQL += ")"
		;
		String sOtherVendorError = "Invoice cannot be saved because it has receipts from other vendors: ";
		String sOtherVendors = "";
		boolean bOtherVendorsFound = false;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				if (rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor).compareToIgnoreCase(
						getM_svendor()) != 0){
					sOtherVendors += " - Receipt #" + Long.toString(
							rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid))
						+ " is from "
						+ rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor)
						+ ": " + rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname);
					bOtherVendorsFound = true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading vendors from receipt lines with SQL: " + SQL + " - " + e.getMessage());
			bResult = false;
		}
		
		if (bOtherVendorsFound){
			super.addErrorMessage(sOtherVendorError + sOtherVendors);
			bResult = false;
		}
		
		//Make sure that all the receipt lines have been posted:
		SQL = "SELECT"
				+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
				+ " FROM " + SMTableicporeceiptlines.TableName
				+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName + " ON "
				+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " = "
				+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
				+ " WHERE ("
					+ "("
				;
				for (int i = 0; i < m_arrLines.size(); i++){
					if (i == 0){
						SQL += "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " = "
							+ m_arrLines.get(i).getsporeceiptlineid() + ")";
					}else{
						SQL += " OR (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " = "
							+ m_arrLines.get(i).getsporeceiptlineid() + ")";
					}
				}
				SQL += ") AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
				+ ")"
		;
				//System.out.println("[1374603765] SQL = " + SQL);
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					super.addErrorMessage("This invoice has unposted receipt lines included on it - cannot be saved.");
					bResult = false;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error checking for unposted receipt lines with SQL: " + SQL + " - " + e.getMessage());
				bResult = false;
			}
		return bResult;
	}

	public void validate_entry_fields (ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".validate_entry_fields - user: " + sUserID
			);
		} catch (Exception e) {
			throw new Exception("Error [1490661236] getting connection - " + e.getMessage());
		}
		
		try {
			validate_entry_fields(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1490661237] validating - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080920]");
		
		return;
	}
	public boolean validate_entry_fields (Connection conn, String sUserID){
		//Validate the entries here:
		boolean bEntriesAreValid = true;

		long lInvoiceID;
		//long lID;
		try {
			lInvoiceID = Long.parseLong(m_slid);
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid Invoice ID: '" + m_slid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		if (lInvoiceID < -1){
			super.addErrorMessage("Invalid Invoice ID: '" + m_slid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		m_sinvoicenumber = m_sinvoicenumber.trim();
		if (m_sinvoicenumber.length() > SMTableicpoinvoiceheaders.sinvoicenumberLength){
			super.addErrorMessage("Invoice number is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sinvoicenumber.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Invoice number cannot be blank.");
			bEntriesAreValid = false;
		}

		java.sql.Date datInvoice = null;
		if (m_sdatinvoice.compareTo(EMPTY_DATE_STRING) != 0){
			try {
				datInvoice = clsDateAndTimeConversions.StringTojavaSQLDate("MMddyy", m_sdatinvoice);
			} catch (ParseException e) {
				try {
					datInvoice = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", m_sdatinvoice);
				} catch (ParseException e1) {
					super.addErrorMessage("Invoice date '" + m_sdatinvoice + "' is invalid.");
					bEntriesAreValid = false;
					return bEntriesAreValid;
				}
			}
			m_sdatinvoice = clsDateAndTimeConversions.sqlDateToString(datInvoice, "M/d/yyyy");
		}else{
			super.addErrorMessage("Invoice date cannot be blank.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// Make sure the entry date is always within the posting date range:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	super.addErrorMessage("Error [1457652007] loading SM Options to check posting date range - " + opt.getErrorMessage() + ".");
        	bEntriesAreValid = false;
        }
        java.sql.Date datEntryDate = null;
		if (m_sdatentered.compareTo(EMPTY_DATE_STRING) != 0){
			try {
				datEntryDate = clsDateAndTimeConversions.StringTojavaSQLDate("MMddyy", m_sdatentered);
			} catch (ParseException e) {
				try {
					datEntryDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", m_sdatentered);
				} catch (ParseException e1) {
					super.addErrorMessage("Invoice date '" + m_sdatentered + "' is invalid.");
					bEntriesAreValid = false;
					return bEntriesAreValid;
				}
			}
			m_sdatentered = clsDateAndTimeConversions.sqlDateToString(datEntryDate, "M/d/yyyy");
		}else{
			super.addErrorMessage("Date entered cannot be blank.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
        try {
			opt.checkDateForPosting(clsDateAndTimeConversions.sqlDateToString(datEntryDate, "M/d/yyyy"), "PO Invoice Entry Date", conn, sUserID);
		} catch (Exception e) {
        	super.addErrorMessage("Error [1457652108] " + e.getMessage());
        	bEntriesAreValid = false;
		}

        java.sql.Date datDiscount = null;
		if (m_sdatdiscount.compareTo(EMPTY_DATE_STRING) != 0){
			try {
				datDiscount = clsDateAndTimeConversions.StringTojavaSQLDate("MMddyy", m_sdatdiscount);
			} catch (ParseException e) {
				try {
					datDiscount = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", m_sdatdiscount);
				} catch (ParseException e1) {
					super.addErrorMessage("Discount date '" + m_sdatdiscount + "' is invalid.");
					bEntriesAreValid = false;
					return bEntriesAreValid;
				}
			}
			m_sdatdiscount = clsDateAndTimeConversions.sqlDateToString(datDiscount, "M/d/yyyy");		
		}
		//else{
		//	super.addErrorMessage("Discount date cannot be blank.");
		//	bEntriesAreValid = false;
		//	return bEntriesAreValid;
		//}

		java.sql.Date datDue = null;
		if (m_sdatdue.compareTo(EMPTY_DATE_STRING) != 0){
			try {
				datDue = clsDateAndTimeConversions.StringTojavaSQLDate("MMddyy", m_sdatdue);
			} catch (ParseException e) {
				try {
					datDue = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", m_sdatdue);
				} catch (ParseException e1) {
					super.addErrorMessage("Due date '" + m_sdatdue + "' is invalid.");
					bEntriesAreValid = false;
					return bEntriesAreValid;
				}
			}
			m_sdatdue = clsDateAndTimeConversions.sqlDateToString(datDue, "M/d/yyyy");
		}else{
			super.addErrorMessage("Due date cannot be blank.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		//If the discount date is earlier than the invoice date, reject it:
		//Don't bother if there's no 'discount date':
		if (datDiscount != null){
			if (datDiscount.before(datInvoice)){
				super.addErrorMessage("The discount date cannot be earlier than the invoice date.");
				bEntriesAreValid = false;
			}
		}
		
		//If the due date is earlier than the invoice date, reject it:
		if (datDue.before(datInvoice)){
			super.addErrorMessage("The due date cannot be earlier than the invoice date.");
			bEntriesAreValid = false;
		}

		m_sterms = m_sterms.trim();
		if (m_sterms.length() > SMTableicpoinvoiceheaders.stermsLength){
			super.addErrorMessage("terms code is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sterms.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Terms cannot be blank.");
			bEntriesAreValid = false;
		}

		m_sdiscount = m_sdiscount.replace(",", "");
		if (m_sdiscount.compareToIgnoreCase("") == 0){
			m_sdiscount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicpoinvoiceheaders.bddiscountScale, BigDecimal.ZERO);
		}
		BigDecimal bdDiscount = new BigDecimal(0);
		try{
			bdDiscount = new BigDecimal(m_sdiscount);
			if (bdDiscount.compareTo(BigDecimal.ZERO) < 0){
				super.addErrorMessage("Discount must be a positive number: " + m_sdiscount + ".  ");
				bEntriesAreValid = false;
			}else{
				m_sdiscount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bddiscountScale, bdDiscount);
			}
		}catch(NumberFormatException e){
			super.addErrorMessage("Invalid discount: '" + m_sdiscount + "'.  ");
			bEntriesAreValid = false;
		}

		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_sexportsequencenumber);
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid export sequence number: '" + m_sexportsequencenumber + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		m_sinvoicetotal = m_sinvoicetotal.replace(",", "");
		if (m_sinvoicetotal.compareToIgnoreCase("") == 0){
			m_sinvoicetotal = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicpoinvoiceheaders.bdinvoicetotalScale, BigDecimal.ZERO);
		}
		BigDecimal bdInvoiceTotal = new BigDecimal(0);
		try{
			bdInvoiceTotal = new BigDecimal(m_sinvoicetotal);
			if (bdInvoiceTotal.compareTo(BigDecimal.ZERO) < 0){
				super.addErrorMessage("Invoice total must be a positive number: " + m_sinvoicetotal + ".  ");
				bEntriesAreValid = false;
			}else{
				m_sinvoicetotal = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bdinvoicetotalScale, bdInvoiceTotal);
			}
		}catch(NumberFormatException e){
			super.addErrorMessage("Invalid invoice total: '" + m_sinvoicetotal + "'.  ");
			bEntriesAreValid = false;
		}

		m_svendor = m_svendor.trim();
		if (m_svendor.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Vendor cannot be blank.");
			bEntriesAreValid = false;
		}

		APVendor ven = new APVendor();
		ven.setsvendoracct(m_svendor);
		if (!ven.load(conn)){
			super.addErrorMessage("Could not load vendor information for '" + m_svendor + "'.");
			bEntriesAreValid = false;
		}else{
			m_svendorname = ven.getsname();
		}

		m_sdescription = m_sdescription.trim();
		if (m_sdescription.length() > SMTableicpoinvoiceheaders.sdescriptionLength){
			super.addErrorMessage("description is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		//Tax fields:
		try {
			m_itaxid = clsValidateFormFields.validateLongIntegerField(m_itaxid, "Tax ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e1) {
			super.addErrorMessage(e1.getMessage());
			bEntriesAreValid = false;
		}
		
		//Use the 'Tax ID' to get all the other fields:
		String SQL = "SELECT"
			+ " * FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.lid + " = " + m_itaxid + ")"
			+ ")"
		;
		try {
			ResultSet rsTax = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsTax.next()){
				m_staxtype = rsTax.getString(SMTabletax.staxtype);
				m_staxjurisdiction = rsTax.getString(SMTabletax.staxjurisdiction);
				m_icalculateonpurchaseorsale = Long.toString(rsTax.getLong(SMTabletax.icalculateonpurchaseorsale));
				m_bdtaxrate = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpoinvoiceheaders.bdtaxrateScale, rsTax.getBigDecimal(SMTabletax.bdtaxrate)).replace(",", "");
			}else{
				super.addErrorMessage("No tax record found for ID '" + m_itaxid + "'.");
				bEntriesAreValid = false;
			}
			rsTax.close();
		} catch (SQLException e2) {
			super.addErrorMessage("Unable to read tax table with SQL '" + SQL + "' - " + e2.getMessage());
			bEntriesAreValid = false;
		}
		
		//Validate all new entry descriptions
		for (String sDesc : m_arrNewDescLine){
			sDesc.trim();
			if(sDesc.compareToIgnoreCase("") != 0){
				if (sDesc.compareToIgnoreCase("") == 0){
					super.addErrorMessage("Cannot add new line - description cannot be blank.");
					bEntriesAreValid = false;
				}
				if (sDesc.length() > SMTableicpoinvoicelines.sitemdescriptionLength){
					super.addErrorMessage(
							"Cannot add new line - description cannot be longer than " 
							+ SMTableicpoinvoicelines.sitemdescriptionLength + " characters.");
					bEntriesAreValid = false;
				}
			}		
		}
		//Validate all new entry accounts
		for (String sAcct : m_arrNewAcctLine){
			sAcct.trim();
			if(sAcct.compareToIgnoreCase("") != 0){
				if (sAcct.compareToIgnoreCase("") == 0){
					super.addErrorMessage("Cannot add new line - expense acct. cannot be blank.");
					bEntriesAreValid = false;
				}
				if (sAcct.length() > SMTableicpoinvoicelines.sexpenseaccountLength){
					super.addErrorMessage(
							"Cannot add new line - expense acct. cannot be longer than " 
							+ SMTableicpoinvoicelines.sexpenseaccountLength + " characters.");
					bEntriesAreValid = false;
				}
			}		
		}
		//Validate all new entry amounts
		BigDecimal bdNewLineInvoicedCost = new BigDecimal(0);
		for (String sAmount : m_arrNewAmountLine){
			sAmount.trim();
			if(sAmount.compareToIgnoreCase("") != 0){
				if (sAmount.compareToIgnoreCase("") == 0){
					sAmount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicpoinvoicelines.bdinvoicedcostScale, BigDecimal.ZERO);
				}
				
				try{
					BigDecimal bdTempInvoicedCost = new BigDecimal(sAmount);
					//TJR - 11/7/2016 - took out this validation, since we can save ALREADY added 'additional lines'
					//with negative costs.  So we shouldn't restrict NEW lines to 'positive only'. 
					//if (bdTempInvoicedCost.compareTo(BigDecimal.ZERO) < 0){
					//	super.addErrorMessage("Cannot add new line - invoiced cost must be a positive number: " 
					//			+ sAmount + ".");
					//	bEntriesAreValid = false;
					//}else{
						bdNewLineInvoicedCost = bdNewLineInvoicedCost.add(bdTempInvoicedCost);
						sAmount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
								SMTableicpoinvoicelines.bdinvoicedcostScale, bdNewLineInvoicedCost);
					//}

				}catch(NumberFormatException e){
					super.addErrorMessage("Cannot add new line - invalid invoiced cost: " 
							+ sAmount + ".");
					bEntriesAreValid = false;
				}
			}		
		}
		
		//Validate the 'invoice includes tax'
		if (
			(m_iinvoiceincludestax.compareToIgnoreCase("0") !=0)
			&& (m_iinvoiceincludestax.compareToIgnoreCase("1") !=0)
		){
			super.addErrorMessage("You must indicate whether this invoice includes tax or not.");
			bEntriesAreValid = false;
		}
		
		if (!validate_entry_lines(conn)){
			bEntriesAreValid = false;
		}
		
		//Finally, validate that the entered total matches the total of the lines:
		if ((m_arrLines.size() != 0) || (bdNewLineInvoicedCost.compareTo(BigDecimal.ZERO) != 0)){
			//First, get the line total:
			BigDecimal bdLineTotal = new BigDecimal("0.00");
			for (int i = 0; i < m_arrLines.size(); i++){
				try {
					bdLineTotal = bdLineTotal.add(new BigDecimal(m_arrLines.get(i).getsinvoicedcost().replace(",", "")));
				} catch (Exception e) {
					super.addErrorMessage("Invoiced cost '" + m_arrLines.get(i).getsinvoicedcost() + "' on line " + Integer.toString(i + 1) + " is not valid - " + e.getMessage());
						bEntriesAreValid = false;
				}
			}
			bdLineTotal = bdLineTotal.add(bdNewLineInvoicedCost);
			if (bdLineTotal.compareTo(bdInvoiceTotal) != 0){
				super.addErrorMessage("Invoice total (" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoiceTotal) + ")" 
					+ " does not match the total of the individual lines (" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdLineTotal) + ")." 
				);
				bEntriesAreValid = false;
			}
		}

		return bEntriesAreValid;
	}

	public boolean loadVendorInformation(ServletContext context, String sDBID, String sUserID){

		boolean bResult = true;

		String SQL = "SELECT"
			+ " " + SMTableicvendors.TableName + "." + SMTableicvendors.sterms
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname
			+ " FROM " + SMTableicvendors.TableName
			+ " WHERE ("
			+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct 
			+ " = '" + this.getM_svendor() + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID,
					"MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".loadVendorInformation - user: " + sUserID);
			if (bDebugMode){
				System.out.println("In " + this.toString() + " updating vendor info - SQL = " + SQL);
			}
			if (rs.next()) {
				m_svendorname = rs.getString(
						SMTableicvendors.TableName + "."
						+ SMTableicvendors.sname);
				if (m_svendorname == null){
					m_svendorname = "";
				}
				m_sterms = rs.getString(
						SMTableicvendors.TableName + "."
						+ SMTableicvendors.sterms);
				if (m_sterms == null){
					m_sterms = "";
				}
				if (bDebugMode){
					System.out.println("In " + this.toString() + " updating vendor info - got record, terms = " + m_sterms);
				}

			} else {
				super.addErrorMessage("Could not read records for vendor when loading vendor information.");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Could not read records for vendor when loading vendor information - " + e.getMessage());
			bResult = false;
		}

		try {
			calculateDiscount(context, sDBID, sUserID);
		} catch (Exception e) {
			super.addErrorMessage("Could not calculate terms when loading vendor information - " + e.getMessage());
			bResult = false;
		}

		return bResult;
	}
	
	public void calculateDiscount(ServletContext context, String sDBID,  String sUserID) throws Exception{
		try {
			validate_entry_fields(context, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1490662556] - " + getErrorMessages());
		}
    	try {
			APTermsCalculator calc = new APTermsCalculator(getM_sterms(), getM_sinvoicetotal(), getM_sdatinvoice());
			calc.calculateTerms(context, sDBID, sUserID);
			setM_sdatdiscount(calc.getDiscountDateString());
			setM_sdiscount(calc.getDiscountAmountString());
			setM_sdatdue(calc.getDueDateString());
		} catch (Exception e) {
			throw new Exception("Error [1490662557] calculating discount and due dates - " + e.getMessage());
		}
	}
	

	//Used ONLY FOR TESTING!!:
	public boolean TESTcalculateDiscount(Connection conn, String sUserName, String sUserID){

		boolean bResult = true;

		BigDecimal bdDiscountPercent = new BigDecimal(0);
		int iDiscountDayOfTheMonth = 0;
		int iDiscountNumberOfDays = 0;
		int iDueDayOfTheMonth = 0;
		int iDueNumberOfDays = 0;
		int iMinimumDaysAllowedForDueDayOfMonth = 0;
		int iMinimumDaysAllowedForDiscountDueDayOfMonth = 0;

		if (!validate_entry_fields(conn, sUserID)){
			return false;
		}
		
		String SQL = "SELECT"
			+ " *"
			+ " FROM " + SMTableicvendorterms.TableName
			+ " WHERE ("
			+ "(" + SMTableicvendorterms.TableName + "." + SMTableicvendorterms.sTermsCode
			+ " = '" + this.getM_sterms() + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				bdDiscountPercent = rs.getBigDecimal(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.bdDiscountPercent);
				bdDiscountPercent = bdDiscountPercent.divide(
						new BigDecimal(100), 
						6, //Using the 'discountscale' was a problem for fractions of percents, so I increased the scale here - TJR - 11/10/13
						BigDecimal.ROUND_HALF_UP
				);
				iDiscountDayOfTheMonth = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iDiscountDayOfTheMonth);
				iDiscountNumberOfDays = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iDiscountNumberOfDays);
				iDueDayOfTheMonth = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iDueDayOfTheMonth);
				iDueNumberOfDays = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iDueNumberOfDays);
				iMinimumDaysAllowedForDueDayOfMonth = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iminimumdaysallowedforduedayofmonth);
				iMinimumDaysAllowedForDiscountDueDayOfMonth = rs.getInt(
						SMTableicvendorterms.TableName + "."
						+ SMTableicvendorterms.iminimumdaysallowedfordiscountduedayofmonth);
			} else {
				super.addErrorMessage("Could not read vendor terms to calculate discounts.");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Could not read vendor terms to calculate discounts - " + e.getMessage());
			super.addErrorMessage("Could not read vendor terms to calculate discounts - " + e.getMessage());
			bResult = false;
		}

		//Now calculate the discount amount, the discount date, and the due date:

		//Discount amount:
		BigDecimal bdInvoiceTotal = new BigDecimal(0);
		try{
			bdInvoiceTotal = new BigDecimal(this.getM_sinvoicetotal().replace(",", ""));
		}catch(NumberFormatException e){
			System.out.println("Error converting invoice total '" 
					+ this.getM_sinvoicetotal().replace(",", "")
					+ "' into BigDecimal - " + e.getMessage()
			);
			super.addErrorMessage("Error converting invoice total '" 
					+ this.getM_sinvoicetotal().replace(",", "")
					+ "' into BigDecimal - " + e.getMessage());
			bResult = false;
		}
		BigDecimal bdDiscountAmount = new BigDecimal(0);
		bdDiscountAmount = bdInvoiceTotal.multiply(bdDiscountPercent);

		this.setM_sdiscount(
				clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpoinvoiceheaders.bddiscountScale, bdDiscountAmount)
		);

		//Calculate due date:
		//NOTE: The due day of the month takes precedence - if we have that, ignore the number of days due:
		java.sql.Date datInvoice = new java.sql.Date(0);
		try {
			datInvoice = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", this.getM_sdatinvoice());
		} catch (ParseException e) {
			System.out.println("Error converting invoice date '" 
					+ this.getM_sdatinvoice()
					+ "' into java.sql.Date - " + e.getMessage()
			);
			super.addErrorMessage("Error converting invoice date '" 
					+ this.getM_sdatinvoice()
					+ "' into java.sql.Date - " + e.getMessage()
			);
			bResult = false;
		}

		java.text.SimpleDateFormat sdf = 
			new java.text.SimpleDateFormat("M/d/yyyy");
		Calendar cDueDate = Calendar.getInstance(); 
		cDueDate.setTime(datInvoice);
		//If there is no 'Due date of the month', then just calculate using the number of days from the invoice date
		System.out.println("Due day of the month = " + iDueDayOfTheMonth);
		System.out.println("Due number of days = " + iDueNumberOfDays);
		if (iDueDayOfTheMonth == 0){
			//Add the number of due days to the invoice date:
			cDueDate.add(Calendar.DATE, iDueNumberOfDays);
		}else{
			//Set the due date to the next occurrence of the due date of the month:

			//We may have to move the month of the due date forward - check that here:
			//If the due date of the month is <= the invoice date, add a month to the due date:
			System.out.println("cDueDate.get(Calendar.DAY_OF_MONTH) = " + cDueDate.get(Calendar.DAY_OF_MONTH));
			if (iDueDayOfTheMonth <= cDueDate.get(Calendar.DAY_OF_MONTH)){
				cDueDate.add(Calendar.MONTH, 1);
			//If not then...
			}else{
				//...if the invoice date is only a few days before the due day of the month, see if it's in the 'grace period' and
				//kick it to the next month:
				System.out.println("cDueDate.get(Calendar.DAY_OF_MONTH) = " + cDueDate.get(Calendar.DAY_OF_MONTH));
				System.out.println("iMinimumDaysAllowedForDueDayOfMonth = " + iMinimumDaysAllowedForDueDayOfMonth);
				if ((iDueDayOfTheMonth - cDueDate.get(Calendar.DAY_OF_MONTH)) < iMinimumDaysAllowedForDueDayOfMonth){
					cDueDate.add(Calendar.MONTH, 1);
				}
			}
			//Now set the due date to the correct day of the month:
			cDueDate.set(Calendar.DAY_OF_MONTH, iDueDayOfTheMonth);
		}
		System.out.println(sdf.format(cDueDate.getTime()));
		this.setM_sdatdue(sdf.format(cDueDate.getTime()));

		//Calculate the discount date:
		Calendar cDiscountDate = Calendar.getInstance(); 
		cDiscountDate.setTime(datInvoice);
		//If there is no 'Discount date of the month', then just calculate using the number of days from the invoice date
		if (iDiscountDayOfTheMonth == 0){
			//Add the number of discount days to the invoice date:
			cDiscountDate.add(Calendar.DATE, iDiscountNumberOfDays);
		}else{
			//Set the discount due date to the next occurrence of the discount due day of the month:

			//We may have to move the month of the due date forward - check that here:
			//If the discount due date of the month is <= the invoice date, add a month to the discount due date:
			System.out.println("iDiscountDayOfTheMonth = " + iDiscountDayOfTheMonth);
			System.out.println("cDiscountDate.get(Calendar.DAY_OF_MONTH) = " + cDiscountDate.get(Calendar.DAY_OF_MONTH));
			if (iDiscountDayOfTheMonth <= cDiscountDate.get(Calendar.DAY_OF_MONTH)){
				cDiscountDate.add(Calendar.MONTH, 1);
			//If not then...
			}else{
				//...if the invoice date is only a few days before the discount day, see if it's in the 'grace period' and
				//kick it to the next month:
				if ((iDiscountDayOfTheMonth - cDiscountDate.get(Calendar.DAY_OF_MONTH)) < iMinimumDaysAllowedForDiscountDueDayOfMonth){
					cDiscountDate.add(Calendar.MONTH, 1);
				}
			}
			//Now set the due date to the correct day of the month:
			cDiscountDate.set(Calendar.DAY_OF_MONTH, iDiscountDayOfTheMonth);
		}
		//System.out.println("sdf.format(cDiscountDate.getTime()) = " + sdf.format(cDiscountDate.getTime()));
		this.setM_sdatdiscount(sdf.format(cDiscountDate.getTime()));
		return bResult;
	}
	public void invoiceAllLines(){

		//Here we want to update each line from the receipt with it's receipt cost:
		for (int i = 0; i < m_arrLines.size(); i++){
			if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase("-1") != 0){
				m_arrLines.get(i).setsinvoicedcost(m_arrLines.get(i).getsreceivedcost());
			}
		}
	}
	private boolean removeZeroAmountAdditionalLines(Connection conn, String sUser){

		for (int i = 0; i < m_arrLines.size(); i++){
			//We only look at additional lines:
			if (m_arrLines.get(i).getsporeceiptlineid().compareToIgnoreCase("-1") == 0){
				try{
					BigDecimal bdLineCost = new BigDecimal(m_arrLines.get(i).getsinvoicedcost());
					if (bdLineCost.compareTo(BigDecimal.ZERO) == 0){
						if (!deleteLine(m_arrLines.get(i).getslid(), conn, sUser)){
							return false;
						}
					}
				}catch(NumberFormatException e){
					super.addErrorMessage(
							"Could not convert invoiced cost (" + m_arrLines.get(i).getsinvoicedcost() 
							+ ") when checking to remove invoice lines - " + e.getMessage());
					return false;
				}
			}
		}
		return true;
	}
	private boolean deleteLine(String sInvoiceLineID, Connection conn, String sUser){

		String SQL = "DELETE FROM " + SMTableicpoinvoicelines.TableName
		+ " WHERE ("
		+ SMTableicpoinvoicelines.lid + " = " + sInvoiceLineID
		+ ")"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			System.out.println(this.toString() + "Could not delete invoice line with SQL: " + SQL 
					+ " - ERROR: " + ex.getMessage() + ".<BR>");
			super.addErrorMessage("Could not delete invoice line with SQL: " + SQL 
					+ " - ERROR: " + ex.getMessage());
			return false;
		}
		return true;
	}
	public String getM_slid() {
		return m_slid;
	}

	public void setM_slid(String mSlid) {
		m_slid = mSlid;
	}

	public String getM_sinvoicenumber() {
		return m_sinvoicenumber;
	}

	public void setM_sinvoicenumber(String mSinvoicenumber) {
		m_sinvoicenumber = mSinvoicenumber;
	}

	public String getM_sdatdue() {
		return m_sdatdue;
	}

	public void setM_sdatdue(String mSdatdue) {
		m_sdatdue = mSdatdue;
	}

	public String getM_sdatinvoice() {
		return m_sdatinvoice;
	}

	public void setM_sdatinvoice(String mSdatinvoice) {
		m_sdatinvoice = mSdatinvoice;
	}
	public String getM_sdatentered() {
		return m_sdatentered;
	}

	public void setM_sdatentered(String mSdatentered) {
		m_sdatentered = mSdatentered;
	}

	public String getM_sterms() {
		return m_sterms;
	}

	public void setM_sterms(String mSterms) {
		m_sterms = mSterms;
	}

	public String getM_sdatdiscount() {
		return m_sdatdiscount;
	}
	
	public String sgetdatdiscountShowingZeroDateAsBlank() {
		if (m_sdatdiscount.compareToIgnoreCase(EMPTY_DATE_STRING) == 0){
			return "";
		}else{
			return m_sdatdiscount;
		}
	}

	public void setM_sdatdiscount(String mSdatdiscount) {
		m_sdatdiscount = mSdatdiscount;
	}

	public String getM_sdiscount() {
		return m_sdiscount;
	}

	public void setM_sdiscount(String mSdiscount) {
		m_sdiscount = mSdiscount;
	}

	public String getM_sexportsequencenumber() {
		return m_sexportsequencenumber;
	}

	public void setM_sexportsequencenumber(String mSexported) {
		m_sexportsequencenumber = mSexported;
	}

	public String getM_sinvoicetotal() {
		return m_sinvoicetotal;
	}

	public void setM_sinvoicetotal(String mSinvoicetotal) {
		m_sinvoicetotal = mSinvoicetotal;
	}

	public String getM_svendor() {
		return m_svendor;
	}

	public void setM_svendor(String sVendor) {
		m_svendor = sVendor;
	}

	public String getsNewRecord() {
		return m_sNewRecord;
	}

	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}

	public String getsVendorName(){
		return m_svendorname;
	}
	public void setsVendorName(String sVendorName){
		m_svendorname = sVendorName;
	}

	public String getsReceivedAmount(){
		return m_sreceivedamount;
	}
	public void setsReceivedAmount(String sReceivedAmount){
		m_sreceivedamount = sReceivedAmount;
	}

	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}
	public void setstaxjurisdiction(String mSTaxJurisdiction) {
		m_staxjurisdiction = mSTaxJurisdiction;
	}
	public String getsNewLineDescription(int index){
		return m_arrNewDescLine.get(index);
	}
	public ArrayList<String> getsNewLineDescription(){
		return m_arrNewDescLine;
	}
	public void setsNewLineDescription(String sNewLineDescription, int index){
		m_arrNewDescLine.set(index, sNewLineDescription);
	}

	public String getsNewLineExpenseAcct(int index){
		return m_arrNewAcctLine.get(index);
	}
	public ArrayList<String> getsNewLineExpenseAcct(){
		return m_arrNewAcctLine;
	}
	public void setsNewLineExpenseAcct(String sNewLineExpenseAcct, int index){
		m_arrNewAcctLine.set(index, sNewLineExpenseAcct);
	}

	public String getsNewLineInvoicedCost(int index){
		return m_arrNewAmountLine.get(index);
	}
	public ArrayList<String> getsNewLineInvoicedCost(){
		return m_arrNewAmountLine;
	}
	public void setsNewLineInvoicedCost(String sNewLineInvoicedCost, int index){
		m_arrNewAmountLine.set(index, sNewLineInvoicedCost);;
	}

	public String getM_sdescription() {
		return m_sdescription;
	}

	public void setM_sdescription(String mSDescription) {
		m_sdescription = mSDescription;
	}

	public String getitaxid() {
		return m_itaxid;
	}

	public void setitaxid(String sTaxID) {
		m_itaxid = sTaxID;
	}
	
	public String getbdtaxrate() {
		return m_bdtaxrate;
	}

	public void setbdtaxrate(String sTaxRate) {
		m_itaxid = sTaxRate;
	}
	
	public String getstaxtype() {
		return m_staxtype;
	}

	public void setstaxtype(String sTaxType) {
		m_staxtype = sTaxType;
	}
	
	public String geticalculateonpurchaseorsale() {
		return m_icalculateonpurchaseorsale;
	}

	public void seticalculateonpurchaseorsale(String scalculateonpurchaseorsale) {
		m_icalculateonpurchaseorsale = scalculateonpurchaseorsale;
	}
	
	public String getiinvoiceincludestax() {
		return m_iinvoiceincludestax;
	}

	public void setiinvoiceincludestax(String siinvoceincludestax) {
		m_iinvoiceincludestax = siinvoceincludestax;
	}
	
	public String getsnumberofnewlines() {
		return m_snumberofnewlines;
	}

	public void setsnumberofnewlines(String snumberofnewlines) {
		m_snumberofnewlines = snumberofnewlines;
	}
	
	
	public String read_out_debug_data(){
		String sResult = "  ** " + SMUtilities.getFullClassName(toString()) + " read out: ";
		sResult += "\nID: " + getM_slid();
		sResult += "\nInvoice number: " + getM_sinvoicenumber();
		sResult += "\nInvoice date: " + getM_sdatinvoice();
		sResult += "\nEntered date: " + getM_sdatentered();
		sResult += "\nTerms: " + getM_sterms();
		sResult += "\nDiscount date: " + getM_sdatdiscount();
		sResult += "\nDue date: " + getM_sdatdue();
		sResult += "\nDiscount: " + getM_sdiscount();
		sResult += "\nExported: " + getM_sexportsequencenumber();
		sResult += "\nInvoice total: " + getM_sinvoicetotal();
		sResult += "\nVendor: " + getM_svendor();
		sResult += "\nVendor name: " + getsVendorName();
		sResult += "\nReceived amount: " + getsReceivedAmount();
		sResult += "\nObject name: " + ParamObjectName;
		sResult += "\nDescription: " + getM_sdescription();
		sResult += "\nTax jurisdiction: " + getstaxjurisdiction();
		sResult += "\nTax ID: " + getitaxid();
		sResult += "\nTax rate: " + getbdtaxrate();
		sResult += "\nTax type: " + getstaxtype();
		sResult += "\nCalculate on purchase or sale: " + geticalculateonpurchaseorsale();
		sResult += "\nInvoice includes tax: " + getiinvoiceincludestax();
		return sResult;
	}

	public void addErrorMessage(String sMsg){
		super.addErrorMessage(sMsg);
	}

	public ArrayList<ICPOInvoiceLine> getLines(){
		return m_arrLines;
	}
	private void initInvoiceVariables(){
		m_slid = "-1";
		m_sinvoicenumber = "";
		m_sdatinvoice = clsDateAndTimeConversions.now("M/d/yyyy");
		m_sdatentered = clsDateAndTimeConversions.now("M/d/yyyy");
		m_sterms = "";
		m_sdatdiscount = EMPTY_DATE_STRING;
		m_sdatdue = clsDateAndTimeConversions.now("M/d/yyyy");
		m_sdiscount = "0.00";
		m_sexportsequencenumber = "0";
		m_sinvoicetotal = "0.00";
		m_svendor = "";
		m_svendorname = "";
		m_sNewRecord = "1";
		m_sreceivedamount = "0.00";
		m_snumberofnewlines = "0";
		m_sdescription = "";
		m_staxjurisdiction = "";
		m_itaxid = "0";;
		m_bdtaxrate = "0.0000";
		m_staxtype = "";
		m_icalculateonpurchaseorsale = "0";
		m_iinvoiceincludestax = "0";
	    m_arrNewDescLine = new ArrayList<String>(0);
		m_arrNewAcctLine = new ArrayList<String>(0);
		m_arrNewAmountLine = new ArrayList<String>(0);
		m_arrLines = new ArrayList<ICPOInvoiceLine> (0);
		super.setObjectName(ParamObjectName);
	}
}


