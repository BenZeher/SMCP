package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import ServletUtilities.clsBase64Functions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smar.ARTerms;
import smcontrolpanel.SMUtilities;

public class SMInvoicePrinter extends Object{
	
	private Connection m_conn;
	private String m_sCompanyDescription;
	private String m_sAddress;
	private String m_sRemitToAddress;
	private String m_sLogo;
	private String m_sLogoImagePath;
	private boolean m_bShowTaxBreakdown;
	private boolean m_bShowExtendedPriceForEachItem;
	private boolean m_bShowALLItemsOnInvoiceIncludingDNP;
	private boolean m_bShowLaborAndMaterialSubtotals;
	private boolean m_bSuppressDetailsPageBreak;
	private boolean m_bIsFirstInvoice;
	private int m_iCopyCounter;  //Lets the class know which copy it's printing (when printing multiple copies of an invoice)
	private BigDecimal m_bdDetailsExtendedPriceTotal;
	private SMInvoice m_invoice;
	//private int m_iInvoiceDetailsSectionColumnCount;
	private BigDecimal m_bdLaborTotal;
	private BigDecimal m_bdMaterialTotal;
	private String m_sHTMLBreakTag = "";
	private String m_InvoiceLogo;
	private String m_sBase64LogoImage;
	private String m_sLogoImageFileType;
	private ArrayList<String> m_testing = new ArrayList<String>();
	
	
	private static SimpleDateFormat m_InvoiceDateformatter;
	
	private boolean m_bEmailingInvoice;
	
	public SMInvoicePrinter(
		Connection conn, 
		String sInvoiceNumber,
		boolean bEmailingInvoice,
		boolean bShowTaxBreakdown,
		boolean bShowExtendedPriceForEachItem,
		boolean bShowALLItemsOnInvoiceIncludingDNP,
		boolean bShowLaborAndMaterialSubtotals,
		boolean bSuppressDetailsPageBreak,
		boolean bIsFirstInvoice,
		int iCopyCounter,
		HttpServletRequest request,
		ServletContext context
		) throws Exception{
		m_conn = conn;
		m_bEmailingInvoice = bEmailingInvoice;
		m_bShowTaxBreakdown = bShowTaxBreakdown;
		m_bShowExtendedPriceForEachItem = bShowExtendedPriceForEachItem;
		m_bShowALLItemsOnInvoiceIncludingDNP = bShowALLItemsOnInvoiceIncludingDNP;
		m_bShowLaborAndMaterialSubtotals = bShowLaborAndMaterialSubtotals;
		m_bSuppressDetailsPageBreak = bSuppressDetailsPageBreak;
		m_bIsFirstInvoice = bIsFirstInvoice;
		m_sLogoImagePath = getFilePath(request,context);
		//m_iInvoiceDetailsSectionColumnCount = 0;
		m_iCopyCounter = iCopyCounter;  //The first copy is '1', second copy is '2', etc.
		m_InvoiceDateformatter = new SimpleDateFormat("MM/dd/yyyy");
		
		sInvoiceNumber = clsStringFunctions.PadLeft(sInvoiceNumber.trim(), " ", SMInvoice.INVOICE_NUMBER_LENGTH_USED);
		loadInvoiceInfo(sInvoiceNumber, conn);
		loadLocationInfo(conn);
		m_bdDetailsExtendedPriceTotal = getDetailsExtendedPriceTotal(m_invoice.getM_sInvoiceNumber(), m_conn);
		m_bdLaborTotal = new BigDecimal("0.00");
		m_bdMaterialTotal = new BigDecimal("0.00");
		if (m_bEmailingInvoice){
			m_sHTMLBreakTag = "<BR></BR>";
		}else{
			m_sHTMLBreakTag = "<BR>";
		}
		m_sLogoImageFileType = SMUtilities.getImageFileGraphicsType(SMUtilities.getFileSuffix(m_sLogoImagePath));
	}
	
	public static final String getPageBreak(){
		//return"<P CLASS=\"breakhere\"></P>\n";
		return "<P style=\"page-break-before: always;\"></P>";
		//style="page-break-before: always"
	}
	
	private void loadInvoiceInfo(String sInvNumber, Connection conn) throws Exception{
		
		m_invoice = new SMInvoice();
		m_invoice.setM_sInvoiceNumber(sInvNumber);
		if (!m_invoice.load(conn)){
			throw new Exception("Error [1487087284] loading invoice - " + m_invoice.getErrorMessage());
		}
	}
	
	private void loadLocationInfo(Connection conn) throws Exception{
			
					m_sCompanyDescription = m_invoice.getsdbadescription();
					m_sAddress = m_invoice.getmdbaaddress().replaceAll("<BR>", "<BR/>").replaceAll("<br>", "<br/>");
					m_sRemitToAddress = m_invoice.getmdbaremittoaddress().replaceAll("<BR>", "<BR/>").replaceAll("<br>", "<br/>");
					m_sLogo = m_invoice.getsdbalogo();
					m_InvoiceLogo = m_invoice.getmdbainvoicelogo();
	}

	public String printOneCopyOfInvoice(Connection conn) throws Exception{
		String s = "";

		try {
			s += printInvoiceHeader(
				m_bdDetailsExtendedPriceTotal.add(m_invoice.getM_dDiscountAmount().multiply(new BigDecimal("-1"))).add(
				m_invoice.getbdsalestaxamount()).add(m_invoice.getM_dPrePayment().multiply(new BigDecimal("-1"))),
				conn
			);
		} catch (Exception e) {
			s += m_sHTMLBreakTag + m_sHTMLBreakTag + "<FONT COLOR=RED><B>"
				+ "Error printing invoice header - " + e.getMessage() + ".";
		}
		
		//We process the details section here, just to get the totals - but we don't print it until a little later.
		String sInvoiceDetailsList = "";
		try {
			sInvoiceDetailsList = printInvoiceDetailsSection();
		} catch (Exception e) {
			s += m_sHTMLBreakTag + m_sHTMLBreakTag + "<FONT COLOR=RED><B>"
				+ "Error printing invoice details section - " + e.getMessage() + ".";
		}

		try {
			s += printInvoiceTotals();
		} catch (Exception e) {
			s += m_sHTMLBreakTag + m_sHTMLBreakTag + "<FONT COLOR=RED><B>"
				+ "Error printing invoice totals section - " + e.getMessage() + ".";
		}
		
		//page break
		if (!m_bSuppressDetailsPageBreak){
			s += getPageBreak();
		}
		//Print  out invoice details on the 2nd page.
		if (true){ //plug a possible selection here if needed.

			//create header for Second page
			s += printSecondPageHeader();
			s += sInvoiceDetailsList;
		}
		
		return s;
	}

	private String printInvoiceHeader(
			BigDecimal bdTotalAmtDue,
			Connection conn
			) throws Exception{

			String s = "";

			//Get the name of the invoice logo file:
			//SMOption opt = new SMOption();
			//if (!opt.load(m_conn)){
			//	throw new Exception("Error [1487022330] reading SMOptions data - " + opt.getErrorMessage());
			//}

			//If this is a second or third or subsequent copy, OR if it's not the first invoice printed, put a page break here:
			if (
					(m_iCopyCounter > 1)
					|| (!m_bIsFirstInvoice)

					){
				s += getPageBreak();
			}

			//Print the company letterhead (the top box on the invoice):
			
			try {
				s += printCompanyLetterhead(
					m_bEmailingInvoice,
					bdTotalAmtDue
				);
			} catch (Exception e) {
				throw new Exception("Error [1487023359] printing company letterhead - " + e.getMessage());
			}
			
			//Inner table:
			s += clsServletUtilities.createHTMLComment("Starting second table from the top, which will include a 'bill to' and a "
					+ "'ship to' table in it.");
			s += "<TABLE BORDER=\"0\" WIDTH=\"100%\" cellspacing=\"0\" style=\" title:HeaderTable02; \" >\n"
				+ "  <TR>\n" 
				+ "    <TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"50%\" style=\"border-style:solid; border-color:black; border-width:1px; \">\n"; 
			//displaying billto information
			s+= printBillToTable();

			s += "</TD>\n";

			s += "<TD VALIGN=TOP ALIGN=\"LEFT\" WIDTH=\"50%\" cellspacing=\"0\" style=\"border-style:solid; border-color:black; border-width:1px; \" >\n"; 
			
			//displaying shipto information
			s += printShipToTable();

			s +="</TD>\n"
				+ "  </TR>\n"
				+ "</TABLE>\n";
			s += clsServletUtilities.createHTMLComment("Ended second table (for Bill to and Ship to)");
			
			//TEST
			//s += getPageBreak();
			s+= printCustomerNumberTable(conn);
			
			//invoice comments
			String sNote = m_invoice.getM_mInvoiceComments().trim();
			
			if (sNote.compareTo("") != 0){
				s += clsServletUtilities.createHTMLComment("Creating table for invoice comments");
				s += "<TABLE BORDER=\"0\" WIDTH=\"100%\" cellspacing=\"0\" >\n"
					+ "  <TR>\n";
				s += "    <TD ALIGN=\"LEFT\" WIDTH=\"100%\"><FONT SIZE=\"2\"><B>Notes:</B>&nbsp;" 
					// TJR - 12/18/2018 - this filter was forcing non-breaking spaces into the field if the note had ANY special characters in it,
						// and that was preventing the note from wrapping properly on the screen.
					//+ clsStringFunctions.filter(sNote)
					+ sNote
					+ m_sHTMLBreakTag + "</FONT></TD>\n";
				s += "  </TR>\n"
					+ "</TABLE>\n";
			}else{
				s += clsServletUtilities.createHTMLComment("No invoice comments to display on this invoice");
			}
			
			return s;
		}

	private String printBillToTable(){
		String s = "";
		s+= clsServletUtilities.createHTMLComment("Beginning BILL TO table");
		s +="<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" >\n";
		s += "  <TR>\n"
			//+ "    <TD style = \" width:20%; text-align: right; vertical-align: top; font-size: small; \" ><B>Bill To:</B></TD>\n"
			+ "    <TD WIDTH=\"20%\" ALIGN=\"right\" VALIGN=\"top\" ><FONT SIZE=\"2\" ><B>Bill To:&nbsp;</B></FONT></TD>\n"
			+ "    <TD WIDTH=\"80%\" ALIGN=\"LEFT\" ><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sBillToName()) + m_sHTMLBreakTag;
		int iEmptyLineCounter = 0;
		if (m_invoice.getM_sBillToAddressLine1().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sBillToAddressLine1()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sBillToAddressLine2().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sBillToAddressLine2()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sBillToAddressLine3().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sBillToAddressLine3()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sBillToAddressLine4().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sBillToAddressLine4()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		s += clsStringFunctions.filter(m_invoice.getM_sBillToCity()) + ", " 
			+ clsStringFunctions.filter(m_invoice.getM_sBillToState()) + " " 
			+ clsStringFunctions.filter(m_invoice.getM_sBillToZip());
		s += "</FONT>";
		for (int i=0; i<iEmptyLineCounter;i++){
			s += m_sHTMLBreakTag;
		}
		s += "</TD>\n"
			+ "</TR>\n";
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"right\" VALIGN= \"top\" ><B><FONT SIZE=\"2\">Authorized:&nbsp;</FONT></B></TD>\n" 
			+ "    <TD ALIGN=\"left\" ><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sBillToContact()) 
				+ "</FONT></TD>\n</TR>\n";
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"right\" VALIGN=\"top\" ><B><FONT SIZE=\"2\">Phone:&nbsp;</FONT></B></TD>\n" 
			+ "    <TD ALIGN=\"left\" VALIGN=\"top\" ><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sBillToPhone()) 
				+ "</FONT></TD>\n"
			+ "  </TR>\n";
		s += "</TABLE>\n";
		s+= clsServletUtilities.createHTMLComment("Ended BILL TO table");
		return s;
	}
	private String printShipToTable(){
		String s = "";
		s+= clsServletUtilities.createHTMLComment("Beginning SHIP TO table");
		int iEmptyLineCounter = 0;
		s += "<TABLE BORDER=\"0\" WIDTH=\"100%\"  CELLPADDING=\"1\" >\n";
		s += "  <TR>\n"
			+ "    <TD WIDTH=\"20%\" ALIGN=\"RIGHT\" VALIGN=\"TOP\"><FONT SIZE=\"2\"><B>Ship To:&nbsp;</B></FONT></TD>\n" 
			+ "    <TD WIDTH=\"80%\" ALIGN=\"LEFT\"><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sShipToName()) + m_sHTMLBreakTag
//			+ "    <TD WIDTH=\"80%\" ALIGN=\"LEFT\"><FONT SIZE=\"2\">" + m_invoice.getM_sShipToName() + m_sHTMLBreakTag
		;
		iEmptyLineCounter = 0;
		if (m_invoice.getM_sShipToAddress1().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sShipToAddress1()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sShipToAddress2().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sShipToAddress2()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sShipToAddress3().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sShipToAddress3()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		if (m_invoice.getM_sShipToAddress4().trim().compareTo("") != 0){
			s += clsStringFunctions.filter(m_invoice.getM_sShipToAddress4()) + m_sHTMLBreakTag;
		}else{
			iEmptyLineCounter++;
		}
		s += clsStringFunctions.filter(m_invoice.getM_sShipToCity()) + ", " 
			+ clsStringFunctions.filter(m_invoice.getM_sShipToState()) + " " 
			+ clsStringFunctions.filter(m_invoice.getM_sShipToZip());
		for (int i=0; i<iEmptyLineCounter;i++){
			s += m_sHTMLBreakTag;
		}
		s += "</FONT></TD>\n"
			+ "  </TR>\n";

		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" VALIGN=\"TOP\"><FONT SIZE=\"2\"><B>Map:&nbsp;</B></FONT></TD>\n" 
			+ "    <TD ALIGN=\"LEFT\"><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sShipToCountry()) + "</FONT></TD>\n"
			+ "  </TR>\n";
		s +="  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" VALIGN=\"TOP\"><FONT SIZE=\"2\"><B>Job Contact:&nbsp;</B></FONT></TD>\n" 
			+ "    <TD ALIGN=\"LEFT\"><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sShipToContact()) + "</FONT></TD>\n"
			+ "  </TR>\n";
		s +="  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" VALIGN=\"TOP\"><FONT SIZE=\"2\"><B>Phone:&nbsp;</B></FONT></TD>\n" 
			+ "    <TD ALIGN=\"LEFT\"><FONT SIZE=\"2\">" + clsStringFunctions.filter(m_invoice.getM_sShipToPhone()) + "</FONT></TD>\n"
			+ "  </TR>\n";
		s +="</TABLE>\n";
		s+= clsServletUtilities.createHTMLComment("Ended SHIP TO table");
		return s;
	}
	private String printCustomerNumberTable(Connection conn){
		
		ARTerms terms = new ARTerms("");
		try {
			terms.load(m_invoice.getM_sTerms().trim(), conn);
		} catch (Exception e) {
			//Just drop out quietly:
			System.out.println("Error [1523041514] - could not read AR Terms - " + e.getMessage());
		}
		
		return
			clsServletUtilities.createHTMLComment("Starting third table from the top, for customer number, order number, etc.")
			+ "<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" >\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"CENTER\" VALIGN=\"TOP\" WIDTH=\"15%\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"1\"><B>Customer Number</B>" + m_sHTMLBreakTag 
			+ clsStringFunctions.filter(m_invoice.getM_sCustomerCode().trim()) + "</FONT></TD>\n"
			+ "    <TD ALIGN=\"CENTER\" VALIGN=\"TOP\" WIDTH=\"20%\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"1\"><B>Order Number</B>" + m_sHTMLBreakTag 
			+ clsStringFunctions.filter(m_invoice.getM_strimmedordernumber()) + "</FONT></TD>\n"
			+ "    <TD ALIGN=\"CENTER\" VALIGN=\"TOP\" WIDTH=\"20%\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"1\"><B>Order Date</B>" + m_sHTMLBreakTag 
			+ m_InvoiceDateformatter.format(m_invoice.getM_datOrderDate()) + "</FONT></TD>\n"
			+ "    <TD ALIGN=\"CENTER\" VALIGN=\"TOP\" WIDTH=\"30%\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"1\"><B>PO Number</B>" + m_sHTMLBreakTag 
			+ clsStringFunctions.filter(m_invoice.getM_sPONumber().trim()) + "</FONT></TD>\n"
			+ "    <TD ALIGN=\"CENTER\" VALIGN=\"TOP\" WIDTH=\"15%\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"1\"><B>Terms</B>" + m_sHTMLBreakTag 
			+ clsStringFunctions.filter(m_invoice.getM_sTerms().trim()) + " - " + terms.getM_sDescription() + "</FONT></TD>\n"
			+ "  </TR>\n</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending customer number table");
	}
	private String printInvoiceDetailsHeader(String sInvoiceDetailHeaderFontSize){
		String s = "";
		//invoice details column header
		s += clsServletUtilities.createHTMLComment("Printing invoice details header line");
		//m_iInvoiceDetailsSectionColumnCount = 0;
		s += "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"1\" >\n";
		s += "<TR>\n";
		s += "<TD style=\" text-align: right; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Line&nbsp;</B></TD>\n";
		//m_iInvoiceDetailsSectionColumnCount++;
		s += "<TD style=\" text-align: right; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Qty&nbsp;Ordered&nbsp;</B></TD>\n";
		//m_iInvoiceDetailsSectionColumnCount++;
		s += "<TD style=\" text-align: left; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Item&nbsp;Number</B></TD>\n";
		//m_iInvoiceDetailsSectionColumnCount++;
		s += "<TD style=\" text-align: left; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Description</B></TD>\n";
		//m_iInvoiceDetailsSectionColumnCount++;

		//Show tax detail?
		if (m_bShowTaxBreakdown){
			s += "<TD style=\" text-align: center; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Taxable?</B></TD>\n";
			//m_iInvoiceDetailsSectionColumnCount++;
			s += "<TD style=\" text-align: right; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Tax&nbsp;</B></TD>\n";
			//m_iInvoiceDetailsSectionColumnCount++;
		}

		if (m_bShowExtendedPriceForEachItem){
			s += "<TD style=\" text-align: right; border-style:solid; border-color:black; border-width:1px; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Extended&nbsp;Price&nbsp;</B></TD>\n";
			//m_iInvoiceDetailsSectionColumnCount++;
		}
		s += "</TR>\n";
		s += clsServletUtilities.createHTMLComment("Ended invoice details header line");

//		s += "<TR>\n";
//		s += "<TD style=\" text-align: right; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Line&nbsp;</B></TD>\n";
//		s += "<TD style=\" text-align: right; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Qty&nbsp;Ordered&nbsp;</B></TD>\n";
//		s += "<TD style=\" text-align: left; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Item&nbsp;Number</B></TD>\n";
//		s += "<TD style=\" text-align: left; font-size: " + sInvoiceDetailHeaderFontSize + "; \"><B>Description</B></TD>\n";
//		s += "</TR>\n";
		return s;
	}
	
	private String printCompanyLetterhead(
			boolean bEmailingInvoice,
			BigDecimal bdTotalAmtDue
			) throws Exception{
			String sCompanyLetterHead = "";
			
			/*
			String sTest = "<TABLE style = \" "
				+ "border-width: " + "1" + "px; "
				+ "border-spacing: 0px; "
				//+ "border-style: outset; "
				+ "border-style: none; "
				+ "border-color: black; "
				+ "border-collapse: separate; "
				+ "padding-top: 1px;"
				+ "padding-right: 1px;"
				+ "padding-bottom: 1px;"
				+ "padding-left: 1px;"
				+ "width: 100%; "
				//+ "font-size: " + "small" + "; "
				//+ "font-family : Arial; "
				+ "color: black; "
				//+ "background-color: white; "
				+ "line_height: 100%;"
				+ " \" >\n" 
				+ "<TR>"
				+ "<TD style= \" text-align: center; font-size: large; border-style:solid; border-color:black; border-width:1px; \">"
				+ "THIS IS A TEST"
				+ "</TD>"
				+ "</TR>"
				+ "</TABLE>"
			
			;
			*/
			//sCompanyLetterHead += "<TABLE WIDTH=100% BORDER=0 CELLPADDING=1 >\n" 
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Starting 'company letterhead' table at the top of the page");
			sCompanyLetterHead += "<TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"1\" cellspacing=\"0\" >\n" 
				+ "  <TR>\n";

			//display company logo
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Upper left corner (logo area) of top table");
			if (
				(m_sLogo.toUpperCase().compareTo("YES") == 0)
				//&& (m_sBase64LogoImage.compareToIgnoreCase("") != 0)
			 ){	
				try {
					m_sBase64LogoImage = clsBase64Functions.loadFileIntoBase64Image(m_InvoiceLogo, m_sLogoImageFileType);
				} catch (Exception e) {
					//Base 64 encoding failed for some reason - set the image to an empty string:
					m_sBase64LogoImage = "";
				}
				
				if (bEmailingInvoice){
					sCompanyLetterHead += "    <TD style= \"width:25%; text-align:center; \" >" 
						+ "<div>"
						+ "<img src=\""
						+ "data:image/" + m_sLogoImageFileType + ";base64," 
						+ m_sBase64LogoImage
						+ "\""
						+ " />"
						+ "</div>"
					+ "</TD>\n"
					;
				}else{
					//String sLogoImage = m_sLogoImagePath+m_InvoiceLogo;
					//sCompanyLetterHead += "    <TD style= \"width:25%; text-align:center; \" ><img src=\"" + m_sLogoImagePath + opt.getInvoiceLogoFileName() + "\""
					//	+ " alt=\"" + opt.getInvoiceLogoFileName() + " \" ></TD>\n";
					sCompanyLetterHead += "    <TD style= \"width:25%; text-align:center; \" >" 
					+ "<img src=\""
							+ImageConcatWithFilePath (m_InvoiceLogo, m_sLogoImagePath)
							+ "\""
							+ " alt=\"" + "INVOICE LOGO IMAGE" + "\""
							+ " width=\"" + SMTableinvoiceheaders.EMAILED_LOGO_WIDTH + "\""
							+ " height=\"" + SMTableinvoiceheaders.EMAILED_LOGO_HEIGHT + "\""
							+ "/>";
					for(int i = 0; i < m_testing.size(); i++) {
						sCompanyLetterHead += "<p>"+m_testing.get(i)+"</p>";
					}
					//+ "(LOGO)"
					sCompanyLetterHead += "</TD>\n"
					;
				}
		}else{
			sCompanyLetterHead += "    <TD style= \"width:25%; \" >&nbsp;</TD>\n";
		}
			

			//display invoice title - company information
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Center cell (for the company info) of top table");
			sCompanyLetterHead += "    <TD style= \"width:45%; text-align: center; \" >" ;
			sCompanyLetterHead +=  m_sAddress;

			sCompanyLetterHead += "</TD>\n";
			//display invoice information
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Rightmost cell of top table - this will include a sub table");
			sCompanyLetterHead += "    <TD style= \"width:30%; text-align: center; vertical-align: top; \" >\n";
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Sub table for the invoice label, number, amount, etc., which will appear at the top right of the invoice");
			sCompanyLetterHead += "      <TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"1\" cellspacing=\"0\" >\n"
				+ "        <TR>\n";
			sCompanyLetterHead += "          <TD COLSPAN=\"3\" style=\"border-style:solid; border-color:black; border-width:1px; text-align: center; font-size: x-large; font-weight: bold; \">";
			String sDocLabel = "";
			if (m_invoice.getM_iTransactionType() == 0){
				sCompanyLetterHead += "INVOICE";
				sDocLabel = "Invoice Number";
			}else{
				sCompanyLetterHead += "CREDIT&nbsp;NOTE";
				sDocLabel = "Credit Note Number";
			}
			
			sCompanyLetterHead += "</TD>\n"
				+ "        </TR>\n"
				+ "        <TR>\n";
			sCompanyLetterHead += "          <TD style=\"border-style:solid; border-color:black; border-width:1px; text-align: center; font-size: xx-small; \">Date:" + m_sHTMLBreakTag 
				+ m_InvoiceDateformatter.format(m_invoice.getM_datInvoiceDate()) 
				+ "</TD>\n";

			sCompanyLetterHead += "          <TD style=\"border-style:solid; border-color:black; border-width:1px; text-align: center; font-size: xx-small; \">" + sDocLabel + m_sHTMLBreakTag 
				+ m_invoice.getM_sInvoiceNumber().trim() 
				+ "</TD>\n";

			sCompanyLetterHead += "          <TD style=\"border-style:solid; border-color:black; border-width:1px; text-align: center; font-size: xx-small; font-weight: bold; \">AMOUNT&nbsp;DUE" +m_sHTMLBreakTag 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmtDue.setScale(2, BigDecimal.ROUND_HALF_UP)) 
				+ "</TD>\n";

			sCompanyLetterHead += "        </TR>\n"
				+ "      </TABLE>\n";
			
			if(m_sRemitToAddress.trim().compareToIgnoreCase("") != 0) {
				sCompanyLetterHead += clsServletUtilities.createHTMLComment("Remit to address under invoice label, number, amount, etc.");
				sCompanyLetterHead += "<TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"5\" cellspacing=\"0\">\n"
						+ "<TR>\n<TD style= \"width:45%; text-align: center;white-space: nowrap;\">\n"
						+ "<div style = \" font-size: medium; font-weight: bold;\">Remit To</div>\n"
						+ m_sRemitToAddress
						+ "\n</TD>\n</TR>\n"
						+ "</TABLE>\n"
						;
			}
			
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Upper right corner table ended");
			sCompanyLetterHead += "    </TD>\n"
				+ "  </TR>\n"
				+ "</TABLE>\n"
				+ m_sHTMLBreakTag + m_sHTMLBreakTag;
			sCompanyLetterHead += clsServletUtilities.createHTMLComment("Top table on the invoice ended");
			
			return sCompanyLetterHead;
		}
	
	private String printInvoiceDetailsSection() throws Exception{
		String s = "";
		String sInvoiceLineFontSize = "small";
		//Start counting the invoice detail columns here:
		//m_iInvoiceDetailsSectionColumnCount = 0;
		s += printInvoiceDetailsHeader(sInvoiceLineFontSize);
		
		s += clsServletUtilities.createHTMLComment("Looping through details to create invoice detail lines, and totalling material.labor, etc.");
		
		String sInvoiceLines = "";
		int iLineCounter = 0;
		for (int iLineIndex = 0; iLineIndex < m_invoice.getInvoiceDetailArray().size(); iLineIndex++){
			SMInvoiceDetail detail = m_invoice.getInvoiceDetailArray().get(iLineIndex);
			
			boolean bDisplayItemOnInvoice = true;
			if (detail.getM_sDesc().trim().length() >= 3){
				if ((detail.getM_sDesc().trim().substring(0, 3).toUpperCase().compareTo("DNP") == 0)){
					bDisplayItemOnInvoice = false;
				}
			}
			if (detail.getM_iSuppressDetailOnInvoice() == 1){
				bDisplayItemOnInvoice = false;
			}

			if (m_bShowALLItemsOnInvoiceIncludingDNP || bDisplayItemOnInvoice){
				iLineCounter++;
				sInvoiceLines += "  <TR>\n"
					+ "    <TD style=\" text-align: right; font-size: " + sInvoiceLineFontSize + "; \">" + Integer.toString(iLineCounter) + "&nbsp;</TD>\n"
					+ "    <TD style=\" text-align: right; font-size: " + sInvoiceLineFontSize + "; \">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(4, detail.getM_dQtyShipped()) + "&nbsp;</TD>\n"
					+ "    <TD style=\" text-align: left; font-size: " + sInvoiceLineFontSize + "; \"><B>&nbsp;" + clsStringFunctions.filter(detail.getM_sItemNumber().trim()) + "</B></TD>\n"
					+ "    <TD style=\" text-align: left; font-size: " + sInvoiceLineFontSize + "; \">" + clsStringFunctions.filter(detail.getM_sDesc().trim()).replaceAll("&nbsp;", " ") + "</TD>\n"
				;

				//Show tax detail?
				if (m_bShowTaxBreakdown){
					String sTaxable = "N";
					if (detail.getM_iTaxable() == 1){
						sTaxable = "Y";
					}
					//Taxable?
					sInvoiceLines += "    <TD style=\" text-align: center; font-size: " + sInvoiceLineFontSize + "; \"><B>" + sTaxable + "</B></TD>\n";
					//Tax amount:
					sInvoiceLines += "    <TD style=\" text-align: right; font-size: " + sInvoiceLineFontSize + "; \"><B>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(detail.getbdLineSalesTaxAmount().setScale(SMTableinvoicedetails.bdlinetaxamountscale, BigDecimal.ROUND_HALF_UP))
						+ "</B></TD>\n";
				}

				if (m_bShowExtendedPriceForEachItem){
					sInvoiceLines += "    <TD style=\" text-align: right; font-size: " + sInvoiceLineFontSize + "; \"><B>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(detail.getM_dExtendedPrice().setScale(2, BigDecimal.ROUND_HALF_UP)) + "</B></TD>\n";
				}
				sInvoiceLines += "  </TR>\n";
			}
			//accumulate labor and material totals
			if (detail.get_iLaborItem() == 1){
				m_bdLaborTotal = m_bdLaborTotal.add(detail.getM_dExtendedPrice());
			}else{
				m_bdMaterialTotal = m_bdMaterialTotal.add(detail.getM_dExtendedPrice());
			}

			//display invoice detail comment if there is any.
			if (detail.getM_sDetailInvoiceComment().trim().length() > 0){
				String[] sInvoiceComments = detail.getM_sDetailInvoiceComment().trim().split("\n");
				for (int i = 0; i < sInvoiceComments.length; i++){
					sInvoiceLines += "  <TR>\n"
						+ "    <TD COLSPAN=\"3\" style = \" text-align:right; font-size:" + sInvoiceLineFontSize +"; \" >&nbsp;</TD>\n"
						+ "    <TD style=\" text-align: left; font-size: " + sInvoiceLineFontSize + "; \">" + clsStringFunctions.filter(sInvoiceComments[i]).replaceAll("&nbsp;", " ") + "</TD>\n"
						+ "  </TR>\n"
					;
				}
			}
		}
		//Add the invoice lines to the string:
		s += sInvoiceLines;
		s += "</TABLE>\n";
		s += clsServletUtilities.createHTMLComment("Ended invoice details table");
		
		s += clsServletUtilities.createHTMLComment("Printing small table for 'END OF DETAILS' label");
		
		s += "<TABLE WIDTH=\"100%\" BORDER=\"0\" CELLPADDING=\"1\" cellspacing=\"0\" >\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"CENTER\"><span style = \" text-align:center; font-weight:bold; \" >**** END OF DETAILS ****</span></TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n";
		s += clsServletUtilities.createHTMLComment("Ended small table for 'END OF DETAILS' label");
		return s;
	}
	
	private String printInvoiceTotals() throws Exception{
		String s = "";
		
		//round all totals here
		m_bdDetailsExtendedPriceTotal = m_bdDetailsExtendedPriceTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
		m_bdMaterialTotal = m_bdMaterialTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
		m_bdLaborTotal = m_bdLaborTotal.setScale(2, BigDecimal.ROUND_HALF_UP);

		s += clsServletUtilities.createHTMLComment("Printing invoice totals table");
		
		// 'Labor and material lines
		if (m_bShowLaborAndMaterialSubtotals){
			s += printLaborAndMaterialSubtotals();
		} //else{
		//	s += m_sHTMLBreakTag;
		//}
		
		// 'Item Total line' - total extended prices before discount and tax:
		s += clsServletUtilities.createHTMLComment("Printing actual totals table");
		s += "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n";
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"80%\"><B><FONT SIZE=\"2\">"
				//label:
				+ "Item Total:&nbsp;</FONT></B></TD>\n"
			+ "    <TD WIDTH=\"20%\">\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for extended price")
			+"<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdDetailsExtendedPriceTotal.setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for extended price")
			+ "    </TD>\n"
			+ "  </TR>\n";
		
//else{
		//	s += "<TR>\n"
		//		+ "<TD ALIGN=\"RIGHT\" COLSPAN=\"2\" style=\"border-style:none;\"><B>&nbsp;</B></TD>\n"
		//		+ "</TR>\n";
		//}
		
		// Discount line:
		if (m_invoice.getM_dDiscountAmount().compareTo(BigDecimal.ZERO) != 0){
			s += printDiscountLine();
		}
		
		// Sales tax:
		s += "<TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><B><FONT SIZE=\"2\">" + "Total Sales Tax:" + "</FONT>&nbsp;</B></TD>\n"
			+ "    <TD>\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for sales tax")
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_invoice.getbdsalestaxamount().setScale(2, BigDecimal.ROUND_HALF_UP)) + "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for sales tax")
			+ "    </TD>\n"
			+ "  </TR>\n";
		
		// Total amount:
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><B><FONT SIZE=\"2\">"
				//label
				+ "Total Amount Including Tax:</FONT>&nbsp;</B></TD>\n"
			+ "    <TD>\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for total amt")
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdDetailsExtendedPriceTotal.add(m_invoice.getM_dDiscountAmount().multiply(new BigDecimal("-1"))).add(m_invoice.getbdsalestaxamount()).setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for total amount")
			+ "    </TD>\n"
			+ "  </TR>\n";
		
		// Deposit:
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><B><FONT SIZE=\"2\">" + "Deposit:</FONT>&nbsp;</B></TD>\n"
			+ "    <TD>\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for customer deposit")
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_invoice.getM_dPrePayment().multiply(new BigDecimal("-1")).setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for customer deposit")
			+ "    </TD>\n"
			+ "  </TR>\n";
		
		// Amount due:
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><B><FONT SIZE=\"2\">"
				//label
				+ "Amount Due:"
			+ "</FONT>&nbsp;</B></TD>"
			+ "<TD>\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for final amt due")
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdDetailsExtendedPriceTotal.add(m_invoice.getM_dDiscountAmount().multiply(new BigDecimal("-1"))).add(
					m_invoice.getbdsalestaxamount()).add(m_invoice.getM_dPrePayment().multiply(new BigDecimal("-1"))).setScale(2, BigDecimal.ROUND_HALF_UP)) + "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for final amt due")
			+ "    </TD>\n"
			+ "  </TR>\n";

		//sOut += "</TR>\n";
		s += "</TABLE>\n";
		s += clsServletUtilities.createHTMLComment("Ending actual totals table");
		return s;
	}
	private String printDiscountLine(){
		String s = "";
		
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"80%\"><B><FONT SIZE=\"2\">"
				//label:
				+ clsStringFunctions.filter(m_invoice.getM_sDiscountDesc()) + ":&nbsp;"
			+ "</FONT></B></TD>\n"
			+ "    <TD WIDTH=\"20%\">\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for discount")
			+"<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_invoice.getM_dDiscountAmount().multiply(new BigDecimal("-1")).setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for discount")
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"80%\"><B><FONT SIZE=\"2\">"
				//label:
				+ "Subtotal After Discount:&nbsp;" 
			+ "</FONT></B></TD>\n"
			+ "    <TD WIDTH=\"20%\">\n"
			+ clsServletUtilities.createHTMLComment("Printing subtable for discount")
			+"<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" style=\"border-style:solid; border-color:black; border-width:1px;\"><FONT SIZE=\"2\">" 
				//label
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdDetailsExtendedPriceTotal.subtract(m_invoice.getM_dDiscountAmount()).setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ clsServletUtilities.createHTMLComment("Ending subtable for discount")
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		return s;
	}
	private String printLaborAndMaterialSubtotals(){
		String s = "";
		//how to determine labor or material?
		s += clsServletUtilities.createHTMLComment("Printing table for material and labor breakdowns");
		s += "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n";
		
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"80%\" ><B><FONT SIZE=\"2\">"
				//label:
				+ "Labor Subtotals:"
			+ "</FONT>&nbsp;</B></TD>\n" 
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"20%\" style=\"border-style:solid; border-color:black; border-width:1px;\">\n"
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><FONT SIZE=\"2\">"
				//label:
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdLaborTotal.setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n    </TD>\n  </TR>\n";
		
		s += "  <TR>\n    <TD ALIGN=\"RIGHT\" WIDTH=\"80%\" ><B><FONT SIZE=\"2\">"
				//Label:
				+ "Material Subtotals:"
			+ "</FONT>&nbsp;</B></TD>\n" 
			+ "    <TD ALIGN=\"RIGHT\" WIDTH=\"20%\" style=\"border-style:solid; border-color:black; border-width:1px;\">\n"
			+ "<TABLE border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n  <TR>\n"
			+ "    <TD ALIGN=\"RIGHT\"><FONT SIZE=\"2\">"
				//Label:
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdMaterialTotal.setScale(2, BigDecimal.ROUND_HALF_UP)) 
			+ "</FONT>&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>\n"
			+ "    </TD>\n  </TR>\n";
		
		s +="</TABLE>\n";
		s += clsServletUtilities.createHTMLComment("Ending material and labor breakdown table");
		return s;
	}
	private BigDecimal getDetailsExtendedPriceTotal(String sInvoiceNumber, Connection conn) throws Exception{
		
		BigDecimal bdInvoiceTotal = new BigDecimal("0.00");
		String sSQL =
			"SELECT SUM(" + SMTableinvoicedetails.dExtendedPrice + ") AS INVOICETOTAL"
				+ " FROM " + SMTableinvoicedetails.TableName
				+ " WHERE ("
				+ SMTableinvoicedetails.sInvoiceNumber + " = '" 
				+ clsStringFunctions.PadLeft(sInvoiceNumber, " ", 8) + "'"
				+ ")"
			;
		try {
			ResultSet rsInvoiceTotal = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rsInvoiceTotal.next()){
				bdInvoiceTotal = BigDecimal.valueOf(rsInvoiceTotal.getDouble("INVOICETOTAL"));
			}else{
				rsInvoiceTotal.close();
				throw new Exception("Error [1487021194] getting invoice total for invoice number '" + sInvoiceNumber.trim() + "'.");
			}
			rsInvoiceTotal.close();
		} catch (SQLException e1) {
			throw new Exception("Error [1487021195] reading invoice total with SQL: '" + sSQL + "' - " + e1.getMessage());
		}
		return bdInvoiceTotal;
	}

	private String printSecondPageHeader(){
		
		String s = clsServletUtilities.createHTMLComment("Printing second page header table") 
			+ "<TABLE border=\"0\" width=\"100%\" cellpadding=\"0\">\n";
		BigDecimal bdAmountDue = m_bdDetailsExtendedPriceTotal.add(m_invoice.getM_dDiscountAmount().multiply(new BigDecimal("-1"))).add(
			m_invoice.getbdsalestaxamount()).add(m_invoice.getM_dPrePayment().multiply(new BigDecimal("-1")));
 
		s += "  <TR>\n"
			+ "    <TD ALIGN=\"LEFT\" ><B>\n<FONT SIZE=\"3\"><B>" + m_sCompanyDescription + "</B></FONT></B></TD>\n"
			+ "    <TD ALIGN=\"CENTER\">Invoice:&nbsp;<B>" + m_invoice.getM_sInvoiceNumber().trim() + "</B></TD>\n"
			+ "    <TD ALIGN=\"RIGHT\" >Amount Due:&nbsp;<B>" 
			+ bdAmountDue.setScale(2, BigDecimal.ROUND_HALF_UP) 
			+ "</B></TD>\n" 
			+ "  </TR>\n";
		s += "</TABLE>\n" 
			+ m_sHTMLBreakTag
			+ clsServletUtilities.createHTMLComment("Ending second page header table");
		return s;
	}
	
	
	public String getFilePath(HttpServletRequest request, ServletContext context) {
		String sFullLogoImageFilePath = "";
		sFullLogoImageFilePath += System.getProperty("file.separator");
		
		m_testing.add("[1547154104] "+sFullLogoImageFilePath);
		
		if (WebContextParameters.getLocalResourcesPath(context).startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context).substring(1);
			m_testing.add("[1547154198] "+sFullLogoImageFilePath);
		}else{
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context);
			m_testing.add("[1547154186] "+sFullLogoImageFilePath);
		}
		return sFullLogoImageFilePath;
	}
	
	public String ImageConcatWithFilePath (String sFileName, String sFullLogoImageFilePath) {
		if (sFileName.startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += sFileName.substring(1);
			m_testing.add("[1547154160] "+sFullLogoImageFilePath);
		
		}else{
			sFullLogoImageFilePath += sFileName;
			m_testing.add("[1547154149] "+sFullLogoImageFilePath);
			
		}
		return sFullLogoImageFilePath;
	}
	
	

	
	public static String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.theme01 {"
			+ "border-width: " + "0" + "px; "
			+ "border-spacing: 0px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "padding-top: 1px;"
			+ "padding-right: 1px;"
			+ "padding-bottom: 1px;"
			+ "padding-left: 1px;"
			+ "width: 100%; "
			//+ "font-size: " + "small" + "; "
			//+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "line_height: 100%;"
			+ "}"
			+ "\n"
			;
		
		s +=
				"table.theme02withborder {"
				+ "border-width: " + "1" + "px; "
				+ "border-spacing: 0px; "
				//+ "border-style: outset; "
				+ "border-style: none; "
				+ "border-color: black; "
				+ "border-collapse: separate; "
				+ "padding-top: 1px;"
				+ "padding-right: 1px;"
				+ "padding-bottom: 1px;"
				+ "padding-left: 1px;"
				+ "width: 100%; "
				//+ "font-size: " + "small" + "; "
				//+ "font-family : Arial; "
				+ "color: black; "
				//+ "background-color: white; "
				+ "line_height: 100%;"
				+ "}"
				+ "\n"
				;
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

		//This is the def for a left aligned field:
		s +=
			"td.fieldleftaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right aligned field:
		s +=
			"td.fieldrightaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: right; "
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
		
		//This is the def for a read only field, left justified:
		s +=
			"td.readonlyleftfield {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: normal; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a read only field, right justified:
		s +=
			"td.readonlyrightfield {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: normal; "
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
		
		//This is the def for an underlined left-aligned heading on the screen:
		s +=
			"td.fieldleftheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "text-decoration:underline; "
			+ "}"
			+ "\n"
			;

		//This is the def for an underlined right-aligned heading on the screen:
		s +=
			"td.fieldrightheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: right; "
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

		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
	//End SMInvoicePrinter
}




