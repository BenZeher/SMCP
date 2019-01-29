package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smar.SMOption;
import ConnectionPool.WebContextParameters;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableworkorderdetails;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMWorkOrderReceipt extends java.lang.Object{
	public static final int REQUEST_TYPE_PRINT = 0;
	public static final int REQUEST_TYPE_EMAIL = 1;
	public static final String SIGNATURE_PREFIX = "WOSignature";
	//private boolean bDebugMode = false;
	private String sCurrentPrintDate = "";
	public SMWorkOrderReceipt(	){
	}
	public String processReport(
			String sDBID,
			String sWorkOrderNumber,
			String sUserName,
			String sUserID,
			String sUserFullName,
			ServletContext context,
			boolean bEmailMode
	) throws Exception{

		if (sWorkOrderNumber == null){
			sWorkOrderNumber = "";
		}
		if ((sWorkOrderNumber.compareToIgnoreCase("") == 0)){
			throw new Exception("Work order number is missing");
		}
		String s = "";
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) 
			+ ".processReport - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
		
		if (conn == null){
			throw new Exception("Could not get data connection");
		}
		try {
			s += printWorkOrder(
				conn,
				sWorkOrderNumber,
				sUserName,
				sUserID,
				sDBID,
				context,
				bEmailMode
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080688]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080688]");
		return s;
	}
	private String printWorkOrder(
			Connection conn,
			String sWorkOrderNumber,
			String sUserName,
			String sUserID,
			String sDBID,
			ServletContext context,
			boolean bEmailMode
	) throws Exception{
		String s = "";
		
		sCurrentPrintDate = clsDateAndTimeConversions.now("M/d/yyyy h:mm:ss a");
		SMWorkOrderHeader wo = new SMWorkOrderHeader();
		wo.setlid(sWorkOrderNumber.trim());
		
		try {
			if(!wo.load(conn)){
				throw new Exception("Error [1394637836] loading work order - " + wo.getErrorMessages());
			}
		} catch (Exception e1) {
			throw new Exception("Error [1394637833] loading work order - " + e1.getMessage() + ".");
		}
		if (wo.getsposted().compareToIgnoreCase("1") !=0){
			throw new Exception("Unposted work orders cannot be printed or emailed.");
		}
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(wo.getstrimmedordernumber());
		try{
			if(!order.load(conn)){
				throw new Exception("Error [1394637837] loading order header - " + order.getErrorMessages());
			}
		} catch(Exception e2){
			throw new Exception("Error [1394637835] loading order header - " + e2.getMessage() + ".");
		}
		
		String sFormString = "<FORM ID='MAINFORM' NAME='MAINFORM' ACTION='/sm/smcontrolpanel.SMWorkOrderAction'";
			sFormString	+= " METHOD='POST'";
			sFormString += " class='MAINFORM'>";
		s += sFormString;
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">\n";
		/*
		try{
			s += sCommandScripts(wo, bEmailMode);
		}catch (Exception e){
			 throw new Exception("[1394637841] error loading javascript - " + e.getMessage() + ".");
		}
		*/
		//create a FORM to hold the object.
		s += printHeader(wo, order, sUserName, sUserID, sDBID, conn, context);
		
		s += printItemsAndWorkPerformedSection(wo, conn);
		
		s += printMechanicInformation(wo);
		
		s += printAdditionalWorkRequired(wo);
		
		//s += printAcceptanceDetails();
		
		s += printSignature(wo, conn, bEmailMode);
		
		s += printTermsAndConditions(wo, conn); 
		
		s += "</FORM>";
		return s;
	}
	private String printHeader(
			SMWorkOrderHeader wo,
			SMOrderHeader order,
			String sUser,
			String sUserID,
			String sDBID,
			Connection conn,
			ServletContext context
	) throws Exception{
		
		
		String s = "";
		s += "<TABLE style= \" width:100%; text-align: left; border-style:none;\" >";
		try {
		s += "<TR><TD width = 100%; text-align: left;>" + printCommentHeadingTable(wo, order, conn) + "</TD></TR>\n";
		} catch (Exception e1) {
			throw new Exception("ERROR printing comment heading table - " + e1.getMessage() + ".");
		}
		s += "</TABLE>";
		s += "<TABLE style= \" width:100%; border-style:none;\" >";
		try {
			s += "<TR><TD width = 33%>" + printCompanyInformationTable(wo, order, conn) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing company information table - " + e1.getMessage() + ".");
		}
		
		try {
			s += "<TD width = 33%>" +  printLogoTable(wo, sUserID, sUser, sDBID, context, conn, false) + "</TD>"; //don't use logo for now
		} catch (Exception e1) {
			throw new Exception("ERROR printing logo - " + e1.getMessage() + ".");
		}
		try {
			s += "<TD width = 33% style = \" vertical-align:text-top; \">" + printOrderInfoTable(wo, sUser) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing order info table - " + e1.getMessage() + ".");
		}
		s += "</TR>";
		s += "</TABLE>";

		//Now print the Billto and Shipto info:
		s += "<TABLE style= \" width:100%; border-style:none;\" >";
		try {
			s += "<TR><TD width = 100%>" + printBilltoShiptoInformationTable(order) + "</TD></TR>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing billto/shipto information table - " + e1.getMessage() + ".");
		}
		s += "</TABLE>";
		
		//Now print Misc info and Ticket note:
		s += "<TABLE style= \" width:100%; border-style:none;\">";
		try {
			s += "<TR><TD width = 100%>" + printMiscInformationTable(order) + "</TD></TR>";
			s += "<TR><TD width = 100%>" + printTicketNote(order) + "</TD></TR>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing work order misc information and Ticket note table - " + e1.getMessage() + ".");
		}
		s += "</TABLE>";
		return s;
	}
	private String printCommentHeadingTable(
			SMWorkOrderHeader wo_header,
			SMOrderHeader order,
			Connection conn) throws Exception{
		String sComment = "";
		
		String SQL = "SELECT"
			+ " " + SMTableservicetypes.mworeceiptcomment 
			+ " FROM " + SMTableservicetypes.TableName
			+ " WHERE ("
			+ "(" + SMTableservicetypes.sCode + " = '" + order.getM_sServiceTypeCode() + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getString(SMTableservicetypes.mworeceiptcomment).trim().length() > 0){
					sComment = rs.getString(SMTableservicetypes.mworeceiptcomment).trim().replace("\n", "<BR>"); 
				}
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1341934234234] In " + this.toString() + ".printTicketHeader - couldn't read company address - " + e.getMessage());
		}
		
		
		String s = "";
//		s += "<TABLE  style= \"text-align:center; border-style:none; font-family: Arial; font-size: small;\" >";
//		s += "<TR>";
		s +=  sComment;
//		s += "</TD>";
//		s += "</TR>";
//		s += "</TABLE>";
		return s;
	}
	private String printCompanyInformationTable(
			SMWorkOrderHeader wo_header,
			SMOrderHeader order,
			Connection conn) throws Exception{		
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style= \"width:45%; text-align: left;\" >"
				+ wo_header.getmdbaaddress()
			;
		s += "</TD></TR>";
		s += "</TABLE>";
		return s;
	}
	
	/* NOT USED
	private String getFileNameFromDBA (SMOrderHeader oh,
			   						   String sUserID, 
			   						   String sUserFullName, 
			   						   String sDBID, 
			   						   ServletContext context, 
			   						   Connection conn) throws Exception{
		String SQL = "";
		String sLogoFileName = "";
		try {
			SQL = "SELECT "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo+" FROM "
					+" "+SMTabledoingbusinessasaddresses.TableName+" WHERE "
					+" "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.lid
					+ "= '"+oh.getM_idoingbusinessasaddressid()+"'";
			
	
			ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
			+ " - "
			+ sUserFullName
			);
		if (rs.next()){
			sLogoFileName = rs.getString(SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo).trim();
		}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1487709654] getting File Name - " + e.getMessage() + ".");
		}
			return sLogoFileName;
		}
	*/
	private String printLogoTable(SMWorkOrderHeader wo, String sUserID, String sUserFullName, String sDBID, ServletContext context, Connection conn, boolean bPrintLogo) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		
		if (bPrintLogo){
			//String sImagePath = context.getInitParameter("imagepath");
			String sImagePath = WebContextParameters.getLocalResourcesPath(context);
			if (sImagePath == null){
				sImagePath = "../images/smcontrolpanel/";
			}
			String sFile = wo.getSdbaworkorderlogo();
			//Get the logo file name for the proposal:
			s += "<TD WIDTH=100% style= \"text-align: center;\"><img src=\"" + sImagePath + sFile + "\" ></TD>";
		}else{
			s += "<TD WIDTH=100% style= \"text-align: center; font-size: 24px; font-weight:bold; \">Work Order<BR>Receipt</TD>";
		}
		s += "</TR>";
		s += "</TABLE>";
		
		return s;
	}
	private String printOrderInfoTable(SMWorkOrderHeader wo, String sUserName) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style=\" text-align:right; \">" 
			+ "<span>Order Number&nbsp;:&nbsp;"
			+ wo.getstrimmedordernumber()
			+ "</span>"
			+ "<BR>"
			+ "<span style=\" font-size:x-small; font-style:italic; \">Printed:</B>&nbsp;" + sCurrentPrintDate + " by " + sUserName + "</span>"
			+ "</TD>"
		;
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printBilltoShiptoInformationTable(SMOrderHeader ord){
		String sBilltoAddress = "";
		if (ord.getM_sBillToAddressLine1().trim().length() > 0){
			sBilltoAddress += ord.getM_sBillToAddressLine1().trim() + "<BR>";
		}
		if (ord.getM_sBillToAddressLine2().trim().length() > 0){
			sBilltoAddress += ord.getM_sBillToAddressLine2().trim() + "<BR>";
		}
		if (ord.getM_sBillToAddressLine3().trim().length() > 0){
			sBilltoAddress += ord.getM_sBillToAddressLine3().trim() + "<BR>";
		}
		if (ord.getM_sBillToAddressLine4().trim().length() > 0){
			sBilltoAddress += ord.getM_sBillToAddressLine4().trim() + "<BR>";
		}
		sBilltoAddress += ord.getM_sBillToCity().trim() + ", " + ord.getM_sBillToState() + "&nbsp;" + ord.getM_sBillToZip();
		
		String sShiptoAddress = "";
		if (ord.getM_sShipToAddress1().trim().length() > 0){
			sShiptoAddress += ord.getM_sShipToAddress1().trim() + "<BR>";
		}
		if (ord.getM_sShipToAddress2().trim().length() > 0){
			sShiptoAddress += ord.getM_sShipToAddress2().trim() + "<BR>";
		}
		if (ord.getM_sShipToAddress3().trim().length() > 0){
			sShiptoAddress += ord.getM_sShipToAddress3().trim() + "<BR>";
		}
		if (ord.getM_sShipToAddress4().trim().length() > 0){
			sShiptoAddress += ord.getM_sShipToAddress4().trim() + "<BR>";
		}
		sShiptoAddress += ord.getM_sShipToCity().trim() + ", " + ord.getM_sShipToState() + "&nbsp;" + ord.getM_sShipToZip();
		String s = "";
		s += "<TABLE style= \"width:100%; border-style:solid; border:thin solid;\">";
		s += "<TR>";
		s += "<TD style= \" width:50%; text-align:center; vertical-align:top;\">" 
				+ "<TABLE style= \" width:100%; border-style:none; border:none; font-family: Arial; font-size: 10px;\">"
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Bill to:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sBillToName() + "</TD></TR>" //BilltoName
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%>&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + sBilltoAddress + "</TD></TR>" //BilltoAddress
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Email Address:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sEmailAddress() + "</TD></TR>" //BilltoEmail
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Authorized:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sBilltoContact() + "</TD></TR>" //BilltoAuthorized
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sBilltoPhone() + "</TD></TR>" //BilltoPhone
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>2nd Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_ssecondarybilltophone() + "</TD></TR>" //Billto2ndPhone
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Fax:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sBillToFax() + "</TD></TR>" //BilltoFax
				+ "</TABLE>"
		   + "</TD>";
		s += "<TD style= \" width:50%; text-align:center; vertical-align:top;\" WIDTH=50%>" 
				+ "<TABLE  style= \" width:100%; border-style:none; border:none; font-family: Arial; font-size: 10px;\">"
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Ship to:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sShipToName() + "</TD></TR>" //ShiptoName
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%>&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + sShiptoAddress + "</TD></TR>" //ShiptoAddress
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Map:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sShipToCountry() + "</TD></TR>" //MapCoordinates
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Job Contact:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sShiptoContact() + "</TD></TR>" //ShiptoContact
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Job Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sShiptoPhone() + "</TD></TR>" //ShiptoPhone
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>2nd Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_ssecondaryshiptophone() + "</TD></TR>" //Shipto2ndPhone
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Fax:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + ord.getM_sShipToFax() + "</TD></TR>" //ShiptoFax
			+ "</TABLE>"
		  + "</TD>";
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printMiscInformationTable(SMOrderHeader ord){
		String s = "";
		s += "<TABLE  style= \"width:100%; border-style:solid; border:thin solid;\"><TR><TD WIDTH=100%>";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: 10px;\" >";
		//first row = column title
		s+= "<TR>"
				+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=14%><B>Created By</B></TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=16%><B>PO Number</B></TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=14%><B>Customer</B></TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=14%><B>Salesperson</B></TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=14%><B>Terms</B></TD>"
		  + "</TR>";
		//second row = content
		//String sWageScale = "N";
		//if (ord.getM_sSpecialWageRate().compareToIgnoreCase("T") == 0){
		//	sWageScale = "Y";
		//}
		s+= "<TR>"
				+ "<TD ALIGN=CENTER VALIGN=TOP>" + ord.getM_sOrderCreatedByFullName() + "</TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP>" + ord.getM_sPONumber() + "</TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP>" + ord.getM_sCustomerCode() + "</TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP>" + ord.getM_sSalesperson() + "</TD>"
				+ "<TD ALIGN=CENTER VALIGN=TOP>" + ord.getM_sTerms() + "</TD>"
		  + "</TR>";
		s += "</TABLE>";
		s += "</TD></TR></TABLE>";
		return s;
	}
	private String printTicketNote(SMOrderHeader ord) throws Exception{
		String s = "";
		s += "<p style= \"font-family: Arial; font-weight: normal; font-size: small;\"><B>Work Order Note: </B>" + ord.getM_sTicketComments() + "</p>";
		return s;
	}

	private String printItemsAndWorkPerformedSection(SMWorkOrderHeader wo, Connection conn) throws Exception{
		
		String sSQL = "SELECT * FROM" +
						" " + SMTableworkorders.TableName + "," + 
						" " + SMTableworkorderdetails.TableName + " LEFT JOIN" +
						" " + SMTableicitems.TableName +
							" ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber +
					  " WHERE" +
						" " + SMTableworkorders.TableName + "." + SMTableworkorders.lid + " = " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.lworkorderid + 
					  " AND" +
						" " + SMTableworkorders.TableName + "." + SMTableworkorders.lid + " = " + wo.getlid() +
					  " ORDER BY" +
						" " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.idetailtype 
						+ ", " + SMTableicitems.TableName + "." + SMTableicitems.ilaboritem
						+ ", " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.llinenumber 
						;  
						
		ResultSet rsworkord = clsDatabaseFunctions.openResultSet(sSQL, conn);
		boolean iMaterialSectionStarted = false;
		boolean iLaborSectionStarted = false;
		boolean iWorkPerformedCodeSectionStarted = false;
		int iNUMBEROFCOLUMNS = 4;
		int iColumnCounter = 0;
		
		String s = "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; \" >";
		while (rsworkord.next()){
			if (rsworkord.getInt(SMTableworkorderdetails.idetailtype) == SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM){
				//printout work order details
				//Don't print the line if it has a zero qty:
				BigDecimal bdQtyUsed = rsworkord.getBigDecimal(SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.bdquantity);
				if (bdQtyUsed.compareTo(BigDecimal.ZERO) != 0){
					if (rsworkord.getInt(SMTableicitems.ilaboritem) == 0){
						//printout item used  
						if (!iMaterialSectionStarted){
							//print out column headers for item used
							s += printWorkOrderMaterialDetailColumnHeader();
							//set flag
							iMaterialSectionStarted = true;
						}
						//print out material item line
						s += "<TR>";
							s += "<TD ALIGN=RIGHT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getBigDecimal(SMTableworkorderdetails.bdquantity).setScale(SMTableworkorderdetails.bdquantityDecimals, BigDecimal.ROUND_HALF_UP) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sitemnumber) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sitemdesc) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sunitofmeasure) + "</TD>";
							s += "</TR>";
					}else{
						//print out hours used
						if (!iLaborSectionStarted){
							//print out column headers for hours used
							//s += "<TR><TD colspan=4>&nbsp;</TD></TR>";
							s += printWorkOrderLaborDetailColumnHeader();
							//set flag
							iLaborSectionStarted = true;
						}
						//print out the line
						s += "<TR>";
							s += "<TD ALIGN=RIGHT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getBigDecimal(SMTableworkorderdetails.bdquantity).setScale(SMTableworkorderdetails.bdquantityDecimals, BigDecimal.ROUND_HALF_UP) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sunitofmeasure) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sitemnumber) + "</TD>";
							s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sitemdesc) + "</TD>";
						s += "</TR>";
					}
				}
			}else{
				//print out work performed information.
				if (!iWorkPerformedCodeSectionStarted){
					//print out column headers for work performed code
					//s += "<TR><TD colspan=4>&nbsp;</TD></TR>";
					s += printWorkPerformedCodeColumnHeader();
					s += "<TR>";
					iColumnCounter++;
					//System.out.println("[1395951266]iColumnCounter = " + iColumnCounter);
					//set flag
					iWorkPerformedCodeSectionStarted = true;
				}
				//print out the line, 4 columns
				//System.out.println("[1395951267]iColumnCounter = " + iColumnCounter);
				//System.out.println("[1395951268]iColumnCounter % iNUMBEROFCOLUMNS = " + iColumnCounter % iNUMBEROFCOLUMNS);
				if (iColumnCounter % iNUMBEROFCOLUMNS != 0){
					s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sworkperformed) + "</TD>";
					iColumnCounter++;
				}else{
					s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 10px;\">" + rsworkord.getString(SMTableworkorderdetails.sworkperformed) + "</TD>";
					s += "</TR><TR>";
					iColumnCounter = iColumnCounter - 3;
				}
			}
		}
		s += "</TR>";
		rsworkord.close();
		s += "</TABLE>";
		return s;
	}
	private String printMechanicInformation(SMWorkOrderHeader wo) throws Exception{
		String s = "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\" >";
		s += "<TR><TD colspan=2><B><U>Description of work performed:</U></B></TD></TR>";
		s += "<TR><TD WIDTH=40%>" +
				"<B>Mechanic Name:</B>&nbsp;&nbsp;&nbsp;&nbsp;" + wo.getmechanicsname() + "<BR>" +
				"<B>Date of Work:</B>&nbsp;&nbsp;&nbsp;&nbsp;" + wo.getdattimedone() + 
			"</TD>";
		s += "<TD WIDTH=60%><B>Comment:</B> <BR>" + 
				"<pre style = \"white-space: pre-wrap; font-family: Arial; font-weight: normal; font-size: small;\">" 
				+ wo.getmcomments() + "</pre>" + 
			 "</TD></TR>";
		s += "</TABLE>";
		return s;
	}
/*	
	private String printAcceptanceDetails() throws Exception{
		String s = "";
		
			s += "<BR><p style=\"font-family: Arial; font-weight: normal; font-size: small;\">" 
				+ "This Work Order is subject to all of those Terms and Conditions set forth on the following page(s), which are expressly incorporated herein by reference."
				+ "<BR>"
				+ "<BR>"
				+ "Acceptance.  Customer(s) signature below is an acknowledgment that the Customer(s) has inspected the work performed above and said work is acceptable and satisfactory."
				
				+ "</p>"
			;
			
		return s;
	}
*/	
	private String printAdditionalWorkRequired(SMWorkOrderHeader wo) throws Exception{
		String s = "";
		
		if (wo.getmadditionalworkcomments().compareToIgnoreCase("") != 0){
			s += "<BR><p style=\"font-family: Arial; font-weight: normal; font-size: small;\"><B><U>" + "Additional Work Required:" + "</U></B><BR>";
			s += "<pre style = \"white-space: pre-wrap; font-family: Arial; font-weight: normal; font-size: small;\">" 
			+ wo.getmadditionalworkcomments() + "</pre></p>";
		}
		
		if (wo.getmadditionalworkcomments().compareToIgnoreCase("") != 0){
			s += "<B>Authorize the additional work listed above:&nbsp;&nbsp;</B><U>";
			if (wo.getsiadditionalworkauthorized().compareTo("1") == 0){
				s += "YES";
			}else{
				s += "NO";
			}
			s += "</U><BR>";
		}
		return s;
	}
	
	private String printSignature(SMWorkOrderHeader wo, Connection conn, boolean bEmailMode) throws Exception{

		String s = "<TABLE WIDTH=100%><TR><TD style=\"vertical-align:center; border-bottom:none; font-family: Arial; font-weight: normal; font-size: small;\">";
		
		String sSignatureBoxWidth = wo.getlsignatureboxwidth();
		//if the WO has not been saved from the signature screen, load default signature box dimensions
		if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
			SMOption smoptions = new SMOption();
			try {
				smoptions.load(conn);
			} catch (Exception e) {
				throw new SQLException ("Error loading smoptions for signature box size.");
			}
			sSignatureBoxWidth = smoptions.getisignatureboxwidth();
			wo.setlsignatureboxwidth(sSignatureBoxWidth);
		}
		
		int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
		String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
		
		s += "<span style = \" vertical-align: 100%; \"><B>Signature:</B>&nbsp;</span>";
		if (bEmailMode){
			s += "<img src=\"cid:" + clsEmailInlineHTML.NAME_OF_SIGNATURE_IMAGE + "\" width=\"" + SMTableworkorders.SIGNATURE_DISPLAY_WIDTH + "\" height=\"" + SMTableworkorders.SIGNATURE_DISPLAY_HEIGHT + "\" />&nbsp;&nbsp;&nbsp;&nbsp;";
			//s += "<img src=\"cid:" + EmailInlineHTML.NAME_OF_OHD_LOGO_IMAGE + "\" width=\"0\" height=\"0\" />&nbsp;&nbsp;&nbsp;&nbsp;";
		}else{
			s += "<img "
				+  "width=\"" + SMTableworkorders.SIGNATURE_DISPLAY_WIDTH  + "\" height=\"" + SMTableworkorders.SIGNATURE_DISPLAY_HEIGHT  + "\""
				+ "src=\"data:image/png;base64,"
				//+ "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==\""
				//+ "\""
				+ ServletUtilities.clsBase64Functions.getbase64EncodedStringFromJSON (
					wo.getmsignature(),
					Integer.parseInt(sSignatureBoxWidth),
					Integer.parseInt(sSignatureBoxHeight),
					Integer.parseInt(SMTableworkorders.SIGNATURE_PEN_WIDTH),
					SMTableworkorders.SIGNATURE_PEN_R_COLOUR,
		    		SMTableworkorders.SIGNATURE_PEN_G_COLOUR,
		    		SMTableworkorders.SIGNATURE_PEN_B_COLOUR
					)
				+ "\""
				+ " alt=\"Signature\" />";
			
			//TODO - print signature for HTML here:
			//s += "<canvas class=pad width=" + SMTableworkorders.SIGNATURE_CANVAS_WIDTH + " name=signaturecanvas"
		   	//	+ " height=" + SMTableworkorders.SIGNATURE_CANVAS_HEIGHT + " style=\"border:1px solid  #000000;\" ></canvas>\n"
		  	//+ "<input type=hidden name='" + SMWorkOrderHeader.Parammsignature + "' class=" + SMWorkOrderEdit.SIGNATURE_JSON_OUTPUT_FIELD_NAME + ">\n";
		}
		s += "<span style = \" vertical-align: 100%; \">&nbsp;&nbsp;&nbsp;<B>Date:</B>&nbsp;<U>&nbsp;&nbsp;" + wo.getdattimesigned() + "</U></span><BR>";
		s += "<B>Printed name and title:</B>&nbsp;" + wo.getssignedbyname();
		s += "</TD></TR></TABLE>";
		
		return s;
	}
	
	private String printTermsAndConditions(SMWorkOrderHeader wo, Connection conn) throws Exception{
		//Terms and Conditions are printed on the 2nd page.
		String sTerms = "N/A";
		String sSQL = "SELECT" + 
			" " + SMTableservicetypes.mworkorderterms + 
			" FROM" 
			+ " " + SMTableservicetypes.TableName +", " + SMTableorderheaders.TableName + ", " + SMTableworkorders.TableName 
			+ " WHERE ("
				+ " (" + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + "=" 
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + ")"
				+ " AND (" +  SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "=" 
					+ SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + ")"
				+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + "=" + wo.getlid() + ")"
			+ ")"
			;
		try {
			ResultSet rsTerms = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while (rsTerms.next()){
				sTerms = rsTerms.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.mworkorderterms);
			}
			rsTerms.close();
		} catch (Exception e) {
			throw new Exception("Error in printTermsAndConditions - " + e.getMessage());
		}
		String s = "<P CLASS=\"breakhere\">";
		s += "<p style= \"font-family: Arial; font-weight: normal; font-size: small;\">"
			+ "<B><U>Terms And Conditions Of Agreement: </U></B><BR>"
		;
		if (sTerms.trim().compareTo("") == 0){
			s += "N/A";
		}else{
			s += sTerms.replace("\n", "<BR>");
		}
		s += "<BR>";
		return s;
	}
/*
	private String sCommandScripts(SMWorkOrderHeader workorder, boolean bEmailMode) throws Exception{
		String s = "";
		
		if (!bEmailMode){
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		}
		s += "<script type='text/javascript'>\n";

		//SIG
		//displayOnly
		String sSignaturePadOptions = "drawOnly:true,"
			+ " output:\"." + SMWorkOrderEdit.SIGNATURE_JSON_OUTPUT_FIELD_NAME + "\","
			+ " errorMessageDraw: \"\","
			+ " lineTop:" + SMTableworkorders.SIGNATURE_TOP + ","
			+ " penWidth:" + SMTableworkorders.SIGNATURE_PEN_WIDTH + ","
			+ " penColour:" + "\"" + SMTableworkorders.SIGNATURE_PEN_COLOUR + "\","
			+ " lineColour:\"" + SMTableworkorders.SIGNATURE_LINE_COLOUR + "\","
			+ " lineWidth:" + SMTableworkorders.SIGNATURE_LINE_WIDTH + ","
			+ " lineMargin:" + SMTableworkorders.SIGNATURE_LINE_MARGIN + "," 
			+ " displayOnly:true"
		;
				
		s += "window.onload = function() {\n"
			+ "\n"
			//Display signature:
			+ "    $(document).ready("
			+ "        function () {\n"
			+ "            $('.MAINFORM').signaturePad({" + sSignaturePadOptions + "});\n"
		;
	    if (workorder.getmsignature().compareToIgnoreCase("") != 0){
	    	s += "        $('.MAINFORM').signaturePad({" + sSignaturePadOptions + "}).regenerate(" + workorder.getmsignature()  + ");\n";
	    }
	    s += "        }"
	    	+ "    );\n";
		s += "\n"
			+ "}\n";
		s += "</script>\n";
		
		s += "<script src=\"scripts/jquery-signaturepad-min-01.js\"></script>\n";
	    s += "<script src=\"scripts/json2.min.js\"></script>\n";
	    s += "<script src=\"scripts/flashcanvas.js\"></script>\n"
	    ;
		
		return s;
	}
*/
	private String printWorkOrderMaterialDetailColumnHeader(){
		return "<TR>\n" +
				"<TD COLSPAN=4 ALIGN=LEFT style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>ITEMS USED:</B></TD></TR><TR>" +
				"<TD ALIGN=RIGHT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Item Qty</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Item #</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Description</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Unit of Measure</B></TD>" +
			 "</TR>";
	}
	private String printWorkOrderLaborDetailColumnHeader(){
		return "<TR><TD COLSPAN=4 ALIGN=LEFT style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>HOURS USED:</B></TD></TR><TR>" +
				"<TD ALIGN=RIGHT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Qty</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Unit of Measure</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Labor ID</B></TD>" +
				"<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Description</B></TD>" +
			 "</TR>";
	}
	
	private String printWorkPerformedCodeColumnHeader(){
		return "<TR><TD COLSPAN=4 ALIGN=LEFT style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>WORK PERFORMED CODES:</B></TD></TR>";
	}

}
