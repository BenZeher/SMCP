package smcontrolpanel;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smar.SMOption;
import ConnectionPool.WebContextParameters;
import SMClasses.SMDeliveryTicket;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMDeliveryTicketReceipt extends java.lang.Object{
	
	public static final int REQUEST_TYPE_EMAIL = 1;
	public static final String SIGNATURE_PREFIX = "DTSignature";
	//private boolean bDebugMode = false;
	public SMDeliveryTicketReceipt(	){
	}
	public String processReport(
			String sConf,
			String sDeliveryTicketNumber,
			String sUserName,
			String sUserID,
			String sUserFullName,
			ServletContext context,
			boolean bEmailMode
	) throws Exception{

		if (sDeliveryTicketNumber == null){
			sDeliveryTicketNumber = "";
		}
		if ((sDeliveryTicketNumber.compareToIgnoreCase("") == 0)){
			throw new Exception("Delivery ticket ID is missing");
		}
		String s = "";
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".processReport - user: " + sUserID + " - " + sUserFullName);
		
		if (conn == null){
			throw new Exception("Could not get data connection");
		}
		try {
			s += printDeliveryTicket(
					conn,
					sDeliveryTicketNumber,
					sUserName,
					sUserID,
					sConf,
					context,
					bEmailMode
				);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080431]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080432]");
		return s;
	}
	private String printDeliveryTicket(
			Connection conn,
			String sDeliveryTicketNumber,
			String sUserName,
			String sUserID,
			String sConf,
			ServletContext context,
			boolean bEmailMode
	) throws Exception{
		String s = "";
		
		SMDeliveryTicket deliveryticket = new SMDeliveryTicket();
		deliveryticket.setslid(sDeliveryTicketNumber.trim());
		
		try {
			deliveryticket.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1445609324] loading delivery ticket - " + e1.getMessage() + ".");
		}
		if (deliveryticket.getiposted().compareToIgnoreCase("1") !=0){
			throw new Exception("Unposted delivery ticket cannot be printed or emailed.");
		}
		
		String sFormString = "<FORM ID='MAINFORM' NAME='MAINFORM' ACTION='/sm/smcontrolpanel.SMEditDeliveryTicketAction'";
			sFormString	+= " METHOD='POST'";
			sFormString += " class='MAINFORM'>";
		s += sFormString;
		s+= printHeader(deliveryticket,sUserName,sUserID,sConf,conn,context);
		s += printItemsDeliveredSection(deliveryticket);
		
		s += printDeliveryInformation(deliveryticket);
		
		s += printSignature(deliveryticket, context, bEmailMode, conn);
		
		s += printTermsAndConditions(deliveryticket, conn); 
		
		s += "</FORM>";
		return s;
	}
	private String printHeader(
			SMDeliveryTicket dt,
			String sUser,
			String sUserID,
			String sConf,
			Connection conn,
			ServletContext context
	) throws Exception{
		
		String s = "";
		s += "<TABLE style= \" width:100%; text-align: left; border-style:none;\" >";

		s += "</TABLE>";
		s += "<TABLE style= \" width:100%; border-style:none;\" >";
		try {
			s += "<TR><TD width = 33%>" + printCompanyInformationTable(dt, conn) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing company information table - " + e1.getMessage() + ".");
		}
		
		try {
			s += "<TD width = 33%>" + printLogoTable(dt,sUserID,sUser,sConf, context, conn, false) + "</TD>"; //don't use logo for now
		} catch (Exception e1) {
			throw new Exception("ERROR printing logo - " + e1.getMessage() + ".");
		}
		try {
			s += "<TD width = 33% style = \" vertical-align:text-top; \">" + printOrderInfoTable(dt, sUser) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing order info table - " + e1.getMessage() + ".");
		}
		s += "</TR>";
		s += "</TABLE>";

		//Now print the Billto and Shipto info:
		s += "<TABLE style= \" width:100%; border-style:none;\" >";
		try {
			s += "<TR><TD width = 100%>" + printBilltoShiptoInformationTable(dt) + "</TD></TR>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing billto/shipto information table - " + e1.getMessage() + ".");
		}
		s += "</TABLE>";
		
		//Now print Misc info:
		s += "<TABLE style= \" width:100%; border-style:none;\">";
		try {
			s += "<TR><TD width = 100%>" + printMiscInformationTable(dt) + "</TD></TR>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing work order misc information and Ticket note table - " + e1.getMessage() + ".");
		}
		s += "</TABLE>";
		return s;
	}
	private String printCompanyInformationTable(
			SMDeliveryTicket dt,
			Connection conn) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style= \"width:45%; text-align: left;\" >" 
			+ dt.getmdbaaddress()	
		;
		s += "</TD></TR>";
		s += "</TABLE>";
		return s;
	}
	
	/* NOT USED
	private String getFileNameFromDBA (SMDeliveryTicket dt,
									   String sUserID, 
									   String sUserFullName, 
									   String sConf, 
									   ServletContext context, 
									   Connection conn) throws Exception{
		String SQL = "";
		String slid = "";
		String sLogoFileName = "";
		try {
			SQL = "SELECT "+SMTableorderheaders.TableName+"."+SMTableorderheaders.idoingbusinessasaddressid+" FROM "
					+" "+SMTableorderheaders.TableName+" WHERE "
					+" "+SMTableorderheaders.TableName+"."+SMTableorderheaders.sOrderNumber
					+ "= "+dt.getstrimmedordernumber()+"";
			
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				slid = Integer.toString(rs.getInt(SMTableorderheaders.idoingbusinessasaddressid));
			}
			rs.close();
			SQL = "SELECT "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo+" FROM "
					+" "+SMTabledoingbusinessasaddresses.TableName+" WHERE "
					+" "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.lid
					+ "= "+slid+"";
			
			
			rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sLogoFileName = rs.getString(SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1487709654] getting File Name - " + e.getMessage() + ".");
		}
		return sLogoFileName;
	}
	*/
	
	private String printLogoTable(SMDeliveryTicket dt,  
								  String sUserID, 
								  String sUserFullName, 
								  String sConf, 
								  ServletContext context, 
								  Connection conn, 
								  boolean bPrintLogo) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		
		if (bPrintLogo){
			//String sImagePath = context.getInitParameter("imagepath");
			String sImagePath = WebContextParameters.getLocalResourcesPath(context);
			if (sImagePath == null){
				sImagePath = "../images/smcontrolpanel/";
			}
			String sFileName= dt.getsdbadeliveryticketreceiptlogo();
			s += "<TD WIDTH=100% style= \"text-align: center;\"><img src=\"" + sImagePath + sFileName + "\" ></TD>";
		}else{
			s += "<TD WIDTH=100% style= \"text-align: center; font-size: 24px; font-weight:bold; \">Delivery Ticket<BR>Receipt</TD>";
		}
		s += "</TR>";
		s += "</TABLE>";
		
		return s;
	}
	private String printOrderInfoTable(SMDeliveryTicket dt, String sUserName) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style=\" text-align:right; \">" 
			+ "<span>Order Number&nbsp;:&nbsp;"
			+ dt.getstrimmedordernumber()
			+ "</span>"
			+ "<BR>"
			+ "<span style=\" font-size:x-small; font-style:italic; \">Printed:</B>&nbsp;" 
				+ clsDateAndTimeConversions.now("M/d/yyyy h:mm:ss a") 
				+ " by " + sUserName + "</span>"
			+ "</TD>"
		;
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printBilltoShiptoInformationTable(SMDeliveryTicket dt){
		String sBilltoAddress = "";
		if (dt.getsbilltoadd1().trim().length() > 0){
			sBilltoAddress += dt.getsbilltoadd1().trim() + "<BR>";
		}
		if (dt.getsbilltoadd2().trim().length() > 0){
			sBilltoAddress += dt.getsbilltoadd2().trim() + "<BR>";
		}
		if (dt.getsbilltoadd3().trim().length() > 0){
			sBilltoAddress += dt.getsbilltoadd3().trim() + "<BR>";
		}
		sBilltoAddress += dt.getsbilltocity().trim() + ", " + dt.getsbilltostate() + "&nbsp;" + dt.getsbilltozip();
		
		String sShiptoAddress = "";
		if (dt.getsshiptoadd1().trim().length() > 0){
			sShiptoAddress += dt.getsshiptoadd1().trim() + "<BR>";
		}
		if (dt.getsshiptoadd2().trim().length() > 0){
			sShiptoAddress += dt.getsshiptoadd2().trim() + "<BR>";
		}
		if (dt.getsshiptoadd3().trim().length() > 0){
			sShiptoAddress += dt.getsshiptoadd3().trim() + "<BR>";
		}
	
		sShiptoAddress += dt.getsshiptocity().trim() + ", " + dt.getsshiptostate() + "&nbsp;" + dt.getsshiptozip();
		String s = "";
		s += "<TABLE style= \"width:100%; border-style:solid; border:thin solid;\">";
		s += "<TR>";
		s += "<TD style= \" width:50%; text-align:center; vertical-align:top;\">" 
				+ "<TABLE style= \" width:100%; border-style:none; border:none; font-family: Arial; font-size: 10px;\">"
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Bill to:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsbilltoname() + "</TD></TR>" //BilltoName
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%>&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + sBilltoAddress + "</TD></TR>" //BilltoAddress
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Contact:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsbilltocontact() + "</TD></TR>" //BilltoAuthorized
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsbilltophone() + "</TD></TR>" //BilltoPhone
				+ "</TABLE>"
		   + "</TD>";
		s += "<TD style= \" width:50%; text-align:center; vertical-align:top;\" WIDTH=50%>" 
				+ "<TABLE  style= \" width:100%; border-style:none; border:none; font-family: Arial; font-size: 10px;\">"
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Ship to:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsshiptoname() + "</TD></TR>" //ShiptoName
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%>&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + sShiptoAddress + "</TD></TR>" //ShiptoAddress
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Contact:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsshiptocontact() + "</TD></TR>" //ShiptoContact
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Phone:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsshiptophone() + "</TD></TR>" //ShiptoPhone
				+ "<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=40%><B>Fax:</B>&nbsp;&nbsp;</TD><TD ALIGN=LEFT VALIGN=TOP WIDTH=60%>" + dt.getsshiptofax() + "</TD></TR>" //ShiptoFax
			+ "</TABLE>"
		  + "</TD>";
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printMiscInformationTable(SMDeliveryTicket dt){
		String s = "";
		s += "<TABLE  style= \"width:100%; border-style:solid; border:thin solid;\"><TR><TD WIDTH=100%>";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: 10px;\" >";
		s+= "<TR>"
			+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=33%><B>Order Number</B></TD>"
			+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=33%><B>PO Number</B></TD>"
			+ "<TD ALIGN=CENTER VALIGN=TOP WIDTH=33%><B>WO Number</B></TD>"
		  + "</TR>";
		String sPONumber = "N/A";
		String sWONumber = "N/A";
		if(dt.getsponumber().compareToIgnoreCase("") != 0 ){
			sPONumber = dt.getsponumber();
		}
		if(dt.getiworkorderid().compareToIgnoreCase("0") != 0 ){
			sWONumber = dt.getiworkorderid();
		}
		s+= "<TR>"
			+ "<TD ALIGN=CENTER VALIGN=TOP>" + dt.getstrimmedordernumber() + "</TD>"
			+ "<TD ALIGN=CENTER VALIGN=TOP>" + sPONumber + "</TD>"
			+ "<TD ALIGN=CENTER VALIGN=TOP>" + sWONumber + "</TD>"
		  + "</TR>";
		s += "</TABLE>";
		s += "</TD></TR></TABLE>";
		return s;
	}

	private String printItemsDeliveredSection(SMDeliveryTicket dt) throws Exception{
		
		String s = "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; \" >";
		
		s += printDeliveryTicketItemsDeliveredColumnHeader();
		s += "<TR>";
			s += "<TD ALIGN=LEFT style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 11px;\">" + dt.getsdetaillines().replace("\n", "<BR>") + "</TD>";
			s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printDeliveryInformation(SMDeliveryTicket dt) throws Exception{
		String s = "<div class='nobreak'><TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\" >";
		
		s += "<TR><TD width='5%' valign='top'>";
		s +=	"<B><U>Comments:</U></B>&nbsp;&nbsp;&nbsp;&nbsp;</TD></TD><TD ALIGN=LEFT "
				+ "style=\"vertical-align:top; font-family: Arial; font-weight: normal; font-size: 11px;\">" 
				+ dt.getmcomments().replace("\n", "<BR>") + "</TD></TR><BR>"+
			"<TR><TD COLSPAN=\"2\"><B>Delivered By:</B>&nbsp;&nbsp;&nbsp;&nbsp;" + dt.getsdeliveredby() + "</TD></TR>" +
			//"<TR><TD COLSPAN=\"2\"><B>Date of Delivery:</B>&nbsp;&nbsp;&nbsp;&nbsp;" + dt.getsdatinitiated() + 
			"</TD></TR>";
		s += "</TABLE></div>";
		return s;
	}
	
	private String printSignature(SMDeliveryTicket dt, ServletContext context, boolean bEmailMode, Connection conn) throws Exception{

		String s = "<div class='nobreak'>"
				+ "<TABLE WIDTH=100%><TR><TD style=\"vertical-align:center; border-bottom:none; font-family: Arial; font-weight: normal; font-size: small;\">";
		
		//Get the signature box width from smoptions
		String sSignatureBoxWidth = dt.getlsignatureboxwidth();
		//if the delivery ticket has not been saved from the signature screen, load default signature box dimensions
		if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
			SMOption smoptions = new SMOption();
			try {
				smoptions.load(conn);
			} catch (Exception e) {
				throw new SQLException ("Error loading smoptions for signature box size.");
			}
			sSignatureBoxWidth = smoptions.getisignatureboxwidth();
			dt.setlsignatureboxwidth(sSignatureBoxWidth);
		}
		
		int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
		String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
		
		s += "<span style = \" vertical-align: 100%; \"><B>Signature:</B>&nbsp;</span>";
		if (bEmailMode){
			s += "<img src=\"cid:" + clsEmailInlineHTML.NAME_OF_SIGNATURE_IMAGE + "\" width=\"" + SMTabledeliverytickets.SIGNATURE_CANVAS_WIDTH + "\" height=\"" + SMTabledeliverytickets.SIGNATURE_CANVAS_HEIGHT + "\" />&nbsp;&nbsp;&nbsp;&nbsp;";
			//s += "<img src=\"cid:" + EmailInlineHTML.NAME_OF_OHD_LOGO_IMAGE + "\" width=\"0\" height=\"0\" />&nbsp;&nbsp;&nbsp;&nbsp;";
		}else{
			s += "<img "
				+  "width=\"" + SMTabledeliverytickets.SIGNATURE_CANVAS_WIDTH + "\" height=\"" + SMTabledeliverytickets.SIGNATURE_CANVAS_HEIGHT + "\""
				+ "src=\"data:image/png;base64,"
				//+ "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==\""
				//+ "\""
				+ ServletUtilities.clsBase64Functions.getbase64EncodedStringFromJSON (
					dt.getmsignature(),
					Integer.parseInt(sSignatureBoxWidth),
					Integer.parseInt(sSignatureBoxHeight),
					Integer.parseInt(SMTabledeliverytickets.SIGNATURE_PEN_WIDTH),
					SMTabledeliverytickets.SIGNATURE_PEN_R_COLOUR,
					SMTabledeliverytickets.SIGNATURE_PEN_G_COLOUR,
					SMTabledeliverytickets.SIGNATURE_PEN_B_COLOUR
					)
				+ "\""
				+ " alt=\"Signature\" />";
		}
		s += "<span style = \" vertical-align: 100%; \">&nbsp;&nbsp;&nbsp;"
		+ "<B>Date:</B>&nbsp;<U>"; 
			if(dt.getsdatsigned().compareToIgnoreCase("00/00/0000") == 0){
				s+="";
			}else{
				s+=dt.getsdatsigned();
			}
		s += "</U></span><BR>";
		s += "<B>Printed name and title:</B>&nbsp;" + dt.getssignedbyname();
		s += "</TD></TR></TABLE></div>";
		
		return s;
	}
	
	private String printTermsAndConditions(SMDeliveryTicket dt, Connection conn) throws Exception{

		String s = "";
		s += "<P class=\"nobreak\" style= \"font-family: Arial; font-weight: normal; font-size: small;\">"
			+ "<B><U>Terms And Conditions: </U></B><BR>"
		;
		s += dt.getmterms().replace("\n", "<BR>");
		s += "<BR></P>";
		return s;
	}
	private String printDeliveryTicketItemsDeliveredColumnHeader(){
		return "<TR>\n" +
				"<TD COLSPAN=4 ALIGN=LEFT style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>ITEMS Delivered:</B></TD></TR><TR>" +
			 "</TR>";
	}
}
