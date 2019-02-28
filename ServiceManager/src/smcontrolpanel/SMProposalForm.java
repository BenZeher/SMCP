package smcontrolpanel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMProposalForm extends java.lang.Object{
	public static final int REQUEST_TYPE_PRINT = 0;
	public static final int REQUEST_TYPE_EMAIL = 1;
	
	public static final String OHD_LOGO_WIDTH = "251"; 
	public static final String OHD_LOGO_HEIGHT = "80";
	
	private boolean bDebugMode = false;
	private String sCurrentPrintDate = "";
	private String sProposalNum = "";
	public SMProposalForm(	){
	}
	public String processReport(
			Connection conn,
			String sProposalNumber,
			String sDBID,
			String sUserID,
			int iNumberOfCopies,
			int iRequestType,
			ServletContext context,
			boolean bPrintLogo
	) throws Exception{

		if (sProposalNumber == null){
			sProposalNumber = "";
		}
		if ((sProposalNumber.compareToIgnoreCase("") == 0)){
			throw new Exception("You must enter a proposal number.");
		}
		if (iNumberOfCopies < 1){
			iNumberOfCopies = 1;
		}
		String s = "";
		for (int i = 0; i < iNumberOfCopies; i++){
			try {
				if (i > 0){
					s += "<P CLASS=\"breakhere\">";
				}
				s += printProposal(
						conn,
						sProposalNumber,
						sDBID,
						sUserID,
						iRequestType, 
						context,
						bPrintLogo
				);
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
		}
		return s;
	}
	private String printProposal(
			Connection conn,
			String sProposalNumber,
			String sDBID,
			String sUserID,
			int iRequestType,
			ServletContext context,
			boolean bPrintLogo
	) throws Exception{
		String s = "";
		
		sCurrentPrintDate = clsDateAndTimeConversions.now("M/d/yyyy h:mm:ss a");
		sProposalNum = sProposalNumber;
		//s += "<B>THIS IS A GREAT PROPOSAL AT ANY PRICE.  JUST SIGN THE DAMN THING.</B>";

		String SQL = "SELECT"
			+ " * FROM"
			+ " " + SMTableorderheaders.TableName
			+ " LEFT JOIN " + SMTableproposals.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " = " + SMTableproposals.TableName + "." + SMTableproposals.strimmedordernumber
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sProposalNumber + "')"
			+ ")"	//Complete the 'where' clause
			+ " ORDER BY"
			+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
			;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - Main SQL = " + SQL);
		}
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				//Print the proposal header box:
				s += printProposalHeader(rs, sUserID, conn, context, bPrintLogo, iRequestType);
				//Print the proposal title box:
				//s += printProposalTitleBox(rs) + "<BR>";
				//Print the customer box:
				s += printCustomerTable(rs);
				//Print the 'Furnish and install' line - if there is one:
				String sFurnishAndInstallString = rs.getString(SMTableproposals.TableName + "." + SMTableproposals.sfurnishandinstallstring);
				if (sFurnishAndInstallString.compareToIgnoreCase(SMProposalEdit.FI_PHRASE_BLANK) != 0){
					s += "<p style = \" font-family: Arial; font-weight: bold; \" >"
						+ sFurnishAndInstallString
						+ "</p>"
					;
				}
				//Description body:
				s += printDescriptionBody(rs);
				
				//9/19/2013 - TJR - removed these from the form:
				//Options body:
				//s += printOptionsBody(rs);
				//Extra notes body:
				//s += printExtraNotesBody(rs);
				//Prices:
				s += printPriceTable(rs);
				//Alternates:
				s += printAlternatePricesBody(rs);
				//Terms table:
				s += printTermsTable(rs);
				//Signature table:
				s += printSalesperonsSignatureTable(rs, sProposalNumber, iRequestType, conn);
				//Customer signature:
				s += printCustomerSignatureTable(rs);
			}
			rs.close();
		}catch (Exception e){
			throw new Exception("Error reading resultset - " + e.getMessage());
		}
		
		return s;
	}
	private String printProposalHeader(
			ResultSet rsOrderAndProposal,
			String sUserID,
			Connection conn,
			ServletContext context,
			boolean bPrintLogo,
			int iRequestType
	) throws Exception{
		
		String s = "";

		s += "<TABLE style= \" width:100%; border-style:none;\" >";
		try {
			s += "<TR><TD width = 33%>" + printCompanyInformationTable(rsOrderAndProposal, conn) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing company information table - " + e1.getMessage() + ".");
		}
		try {
			s += "<TD width = 33%>" + printLogoTable(rsOrderAndProposal, context, conn, bPrintLogo, iRequestType) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing logo - " + e1.getMessage() + ".");
		}
		try {
			s += "<TD width = 33% style = \" vertical-align:text-top; \">" + printProposalInfoTable(rsOrderAndProposal) + "</TD>";
		} catch (Exception e1) {
			throw new Exception("ERROR printing proposal info table - " + e1.getMessage() + ".");
		}
		s += "</TR>";
		s += "</TABLE>";

		//Now print the line headings:
		//s += "<H3 class = \"western\" align=center>" + "THIS IS ONLY FOR TESTING-PROPOSAL HAS NOT BEEN DESIGNED YET" + "</H3>"; 
		//s += "<H4 class = \"western\" align=center>Printed: " 
		//		+ SMUtilities.now("M/d/yyyy hh:mm a") + "&nbsp;by&nbsp;" + sUser + "</H4>";
		return s;
	}
	private String printCompanyInformationTable(
			ResultSet rsOrderAndProposal, 
			Connection conn) throws Exception{
		String sCompanyAddress = "";
		
		String SQL = "SELECT * "
			+ " FROM " + SMTabledoingbusinessasaddresses.TableName
			+ " WHERE ("
			+ "(" + SMTabledoingbusinessasaddresses.lid + " = " + Integer.toString(rsOrderAndProposal.getInt(SMTableorderheaders.idoingbusinessasaddressid)) + ")"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sCompanyAddress = rs.getString(SMTabledoingbusinessasaddresses.mAddress);
			}
			rs.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
			System.out.println("[1341948940] In " + this.toString() + ".printTicketHeader - couldn't read company address - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM " + SMTablesalesperson.TableName
			+ " WHERE ("
				+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + rsOrderAndProposal.getString(SMTableorderheaders.sSalesperson) + "')"
			+ ")"
		;
		ResultSet rsSalesperson = clsDatabaseFunctions.openResultSet(SQL, conn);
		String sSalespersonFullName = "";
		String sDirectDial = "";
		String sEmail = "";
		try {
			if (rsSalesperson.next()){
				sSalespersonFullName = rsSalesperson.getString(SMTablesalesperson.sSalespersonFirstName).trim()
					+ " " + rsSalesperson.getString(SMTablesalesperson.sSalespersonLastName).trim();
				sDirectDial = rsSalesperson.getString(SMTablesalesperson.sDirectDial).trim();
				sEmail = rsSalesperson.getString(SMTablesalesperson.sSalespersonEmail).trim();
			}
			rsSalesperson.close();
		} catch (Exception e) {
			throw new Exception("Error reading salesperson info - " + e.getMessage() + ".");
		}
		
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style= \"width:45%; text-align: left;\" >"  
			+ sCompanyAddress
			+ "</TD>"
			+ "</TR>";
		
		s +="<TR>"
			+ "<TD>"
			+ "<B>Contact:</B>&nbsp;"
			+ sSalespersonFullName + "<BR>"
			+ "<B>Email:</B>&nbsp;"
			+ sEmail + "<BR>"
			+ "<B>Direct dial:</B>&nbsp;"
			+ sDirectDial
			+ "</TD>"
		;
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	
	
	public String getImage(ResultSet rsOrderAndProposal, Connection conn) throws Exception{
		String sProposalLogoFile = "";
	String SQL = "SELECT  "+SMTableproposals.TableName+"."+SMTableproposals.sdbaproposallogo
			+ " FROM " + SMTableproposals.TableName
			+ " WHERE ("
			+ "" + SMTableproposals.strimmedordernumber + " = "+rsOrderAndProposal.getString(SMTableproposals.TableName +"." + SMTableproposals.strimmedordernumber)
			+ " )"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sProposalLogoFile = rs.getString(SMTableproposals.sdbaproposallogo);
			}
			rs.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
		}
		return sProposalLogoFile;
	}
	
	
	
	
	
	
	private String printLogoTable(ResultSet rsOrderAndProposal, ServletContext context, Connection conn, boolean bPrintLogo, int iRequestType) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		if (bPrintLogo){
			s += "<TD WIDTH=100% style=\" text-align: center;\" >";
			if (iRequestType == SMProposalForm.REQUEST_TYPE_EMAIL){
				s += "<img src=\"cid:" + clsEmailInlineHTML.NAME_OF_LOGO_IMAGE + "\" width=\"" + OHD_LOGO_WIDTH + "\" height=\"" + OHD_LOGO_HEIGHT + "\" />";
			}else{
				//String sImagePath = context.getInitParameter("imagepath");
				String sImagePath = WebContextParameters.getLocalResourcesPath(context);
				if (sImagePath == null){
					sImagePath = "../images/smcontrolpanel/";
				}
				String sFile = getImage(rsOrderAndProposal, conn);
				//Get the logo file name for the proposal:
				s += "<img src=\"" + sImagePath + sFile + "\" >";
			}
			s += "</TD>";
		}else{
			s += "<TD WIDTH=100% style=\" text-align: center;\" >&nbsp;</TD>";
		}
		s += "</TR>";
		s += "</TABLE>";
		
		return s;
	}
	private String printProposalInfoTable(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-size: small;\" >";
		s += "<TR>";
		s += "<TD style=\" text-align:right; \">" 
			+ "<span style= \" font-family: Impact; font-size: 24px; font-weight:bold; \">PROPOSAL&nbsp;#"
			+ rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.strimmedordernumber)
			+ "</span>"
			+ "<BR>"
			+ "<span style=\" font-size:x-small; font-style:italic; \">(This proposal #" + sProposalNum + " printed:</B>&nbsp;"
			+ sCurrentPrintDate + ")</span>"
			+ "</TD>"
		;
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	/*
	private String printProposalTitleBox(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		s += "<TABLE style= \" width:100%; border-style:none; border-bottom:thick solid black;\" >";
		s += "<TR>";
		s += "<TD style= \" text-align:center; font-family: Impact; font-size: 42px; font-weight:bold; \">" 
			+ "<B>PROPOSAL</B>"
			+ "</TD>";
		s += "</TR>";
		s += "<TR>";
		try {
			s += "<TD style= \" text-align:center; font-family: Arial; font-size: small; \">" 
				+ "<B>Proposal date:&nbsp;</B>"
				+ SMUtilities.resultsetDateStringToString(rsOrderAndProposal.getString(SMTableproposals.TableName + "." 
						+ SMTableproposals.sdatproposaldate)) + "<BR>"
				+ "</TD>";
		} catch (Exception e) {
			throw new Exception("Error reading proposal date - " + e.getMessage() + ".");
		}
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	*/
	private String printCustomerTable(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		String sShadedCellStyle = "text-align:right; font-family: Arial; font-size: normal; background-color: LightGray;";
		String sUnShadedCellStyle = "text-align:left; font-family: Arial; font-size: normal;";
		s += "<TABLE style= \" width:100%; border-collapse:collapse; \" border = \"1\">";

		s += "<TR>";
		s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Proposed to:&nbsp" + "</TD>";
		try{
			//Bill to:
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sBillToName) + "<BR>";
			String sAddress = "";
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1).trim().compareToIgnoreCase("") !=0 ){
				sAddress += rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2).trim().compareToIgnoreCase("") !=0 ){
				sAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3).trim().compareToIgnoreCase("") !=0 ){
				sAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4).trim().compareToIgnoreCase("") !=0 ){
				sAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity).trim().compareToIgnoreCase("") !=0 ){
				sAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToState).trim().compareToIgnoreCase("") !=0 ){
				sAddress += ",&nbsp;" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToState).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToZip).trim().compareToIgnoreCase("") !=0 ){
				sAddress += "&nbsp;" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToZip).trim();
			}
			
			s += sAddress;
			s += "</TD>";
			
			s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Location:&nbsp;" + "</TD>";
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sShipToName) + "<BR>";
			String sShipAddress = "";
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += "<BR>" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += ",&nbsp;" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState).trim();
			}
			if (rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip).trim().compareToIgnoreCase("") !=0 ){
				sShipAddress += "&nbsp;" + rsOrderAndProposal.getString(
					SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip).trim();
			}
			s += sShipAddress;
			s += "</TD>";
			s += "</TR>";
			
			//contacts:
			s += "<TR>";
			s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Contact:&nbsp;" + "</TD>";
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sBillToContact);
			s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Contact:&nbsp;" + "</TD>";
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sShipToContact);
			s += "</TR>";
	
			//phones:
			s += "<TR>";
			s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Phone:&nbsp;" + "</TD>";
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sBillToPhone);
			s += "<TD style= \" " + sShadedCellStyle + " \" VALIGN=TOP>" + "Phone:&nbsp;" + "</TD>";
			s += "<TD style= \" " + sUnShadedCellStyle + " \" VALIGN=TOP>" + rsOrderAndProposal.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sShipToPhone);
		}catch (Exception e){
			throw new Exception("Error reading data for customer table - " + e.getMessage() + ".");
		}
		s += "</TR>";
		
		s += "</TABLE>";
		return s;
	}
	private String printDescriptionBody(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		s += "<p style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" >" + "DESCRIPTION:" + "</p>";
		
		try {
			s += "<font style = \" font-family: Arial; \" >" 
					+ rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.sbodydescription).replace("\n", "<br/>")
					+ "</font>";
		} catch (Exception e) {
			throw new Exception("Error printing description body - " + e.getMessage() + ".");
		}
		return s;
	}
	/*
	private String printOptionsBody(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		String sOptions = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.soptions);
		if (sOptions.trim().compareToIgnoreCase("") == 0){
			return s;
		}
		s += "<p style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" >" + "OPTIONS:" + "</p>";
		try {
			s += "<p style = \" font-family: Arial; \" >" 
					+ sOptions.replace("\n", "<BR>")
					+ "</p>";
		} catch (Exception e) {
			throw new Exception("Error printing options body - " + e.getMessage() + ".");
		}
		return s;
	}
	private String printExtraNotesBody(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		String sExtraNotes = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.sextranotes);
		if (sExtraNotes.trim().compareToIgnoreCase("") == 0){
			return s;
		}
		s += "<p style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" >" + "NOTES:" + "</p>";
		try {
			s += "<p style = \" font-family: Arial; \" >" 
					+ sExtraNotes.replace("\n", "<BR>")
					+ "</p>";
		} catch (Exception e) {
			throw new Exception("Error printing extra notes - " + e.getMessage() + ".");
		}
		return s;
	}
	*/
	private String printAlternatePricesBody(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		
		try {
			String sAlternateText1 = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.salternate1).trim().replace("\n", "<BR>");
			String sAlternateText2 = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.salternate2).trim().replace("\n", "<BR>");
			String sAlternateText3 = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.salternate3).trim().replace("\n", "<BR>");
			String sAlternateText4 = rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.salternate4).trim().replace("\n", "<BR>");
			if (
				(sAlternateText1.compareToIgnoreCase("") == 0)
				&& (sAlternateText2.compareToIgnoreCase("") == 0)
				&& (sAlternateText3.compareToIgnoreCase("") == 0)
				&& (sAlternateText4.compareToIgnoreCase("") == 0)
			){
				return s;
			}
			//s += "<TABLE style= \" width:100%; border-collapse:collapse; \" border = \"1\">";
			s += "<TABLE style= \" width:100%; border-collapse:collapse;\" >";
			String sAlternateTextStyle = "text-align:left; font-family: Arial; font-size: normal;";
			String sAlternatePriceStyle = "text-align:right; font-family: Arial; font-size: normal; font-weight: bold;";
			String sAcceptanceBoxStyle = "text-align:center; font-family: Arial; font-size: small; border-style: solid; border-width: 1px; width:5%; ";
			if (sAlternateText1.compareToIgnoreCase("") != 0){
				s += "<TR><TD style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" COLSPAN=2>" 
						+ "ALTERNATE 1:" + "</TD></TR>";
				s += "<TR>";
				s += "<TD style= \" " + sAlternateTextStyle + " \">" + sAlternateText1 + "</TD>";
				s += "<TD style= \" " + sAlternatePriceStyle + " \">" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." 
						+ SMTableproposals.salternate1price) + "</TD>";
				s += "<TD style= \" " + sAcceptanceBoxStyle + " \" VALIGN=TOP>" + "Initial:<BR><BR>" + "</TD>";
				s += "</TR>";
			}
			if (sAlternateText2.compareToIgnoreCase("") != 0){
				s += "<TR><TD style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" COLSPAN=2>" 
						+ "ALTERNATE 2:" + "</TD></TR>";
				s += "<TR>";
				s += "<TD style= \" " + sAlternateTextStyle + " \">" + sAlternateText2 + "</TD>";
				s += "<TD style= \" " + sAlternatePriceStyle + " \">" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." 
						+ SMTableproposals.salternate2price) + "</TD>";
				s += "<TD style= \" " + sAcceptanceBoxStyle + " \" VALIGN=TOP>" + "Initial:<BR><BR>" + "</TD>";
				s += "</TR>";
			}
			if (sAlternateText3.compareToIgnoreCase("") != 0){
				s += "<TR><TD style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" COLSPAN=2>" 
						+ "ALTERNATE 3:" + "</TD></TR>";
				s += "<TR>";
				s += "<TD style= \" " + sAlternateTextStyle + " \">" + sAlternateText3 + "</TD>";
				s += "<TD style= \" " + sAlternatePriceStyle + " \">" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." 
						+ SMTableproposals.salternate3price) + "</TD>";
				s += "<TD style= \" " + sAcceptanceBoxStyle + " \" VALIGN=TOP>" + "Initial:<BR><BR>" + "</TD>";
				s += "</TR>";
			}
			if (sAlternateText4.compareToIgnoreCase("") != 0){
				s += "<TR><TD style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" COLSPAN=2>" 
						+ "ALTERNATE 4:" + "</TD></TR>";
				s += "<TR>";
				s += "<TD style= \" " + sAlternateTextStyle + " \">" + sAlternateText4 + "</TD>";
				s += "<TD style= \" " + sAlternatePriceStyle + " \">" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." 
						+ SMTableproposals.salternate4price) + "</TD>";
				s += "<TD style= \" " + sAcceptanceBoxStyle + " \" VALIGN=TOP>" + "Initial:<BR><BR>" + "</TD>";
				s += "</TR>";
			}
		} catch (Exception e) {
			throw new Exception("Error printing alternates - " + e.getMessage() + ".");
		}
		s += "</TABLE>";
		return s;
	}
	private String printPriceTable(ResultSet rsOrderAndProposal)throws Exception{
		String s = "";
		if (
				(rsOrderAndProposal.getString(SMTableproposals.swrittenproposalamt).compareToIgnoreCase("") == 0)
				&& ((rsOrderAndProposal.getString(SMTableproposals.snumericproposalamt).compareToIgnoreCase("") == 0))
		){
			return s;
		}
		//s += "<BR>";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: bold; \" >";
		try {
			s += "<TR><TD style = \" text-align: left; font-style: italic; \" >"
				+ "We hereby propose to complete in accordance with above specification, for the sum of:"
				+ "</TD>"
				+ "</TR>"
				+ "<TR>"
				+ "<TD style = \" text-align: right; \" >"
				+ rsOrderAndProposal.getString(SMTableproposals.swrittenproposalamt)
				+ "&nbsp;&nbsp;"
				+ rsOrderAndProposal.getString(SMTableproposals.snumericproposalamt)
				+ "</TD>"
				+ "</TR>"
			;
		} catch (Exception e) {
			throw new Exception("Error printing price table - " + e.getMessage() + ".");
		}
		
		s += "</TABLE>";
		return s;
	}
	private String printSalesperonsSignatureTable(
			ResultSet rsOrderAndProposal, 
			String sProposalNumber,
			int iRequestType,
			Connection conn) throws Exception{
		String s = "";
		
		try {
			boolean bApproved = rsOrderAndProposal.getLong(SMTableproposals.TableName + "." + SMTableproposals.lapprovedbyuserid) != 0;
			s += "<BR>";
			s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; \" >";
			s += "<TR><TD style = \" text-align: left; \" >";
			String SQL = "SELECT"
					+ " " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sDirectDial
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonEmail
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.mSignature
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonTitle
					+ " FROM " + SMTableorderheaders.TableName + " LEFT JOIN " + SMTablesalesperson.TableName
					+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = "
					+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
					+ " WHERE ("
						+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sProposalNumber + "')"
					+ ")"
				;
			//First get the signature in a variable:
			String sSignature = "";
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sSignature = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.mSignature);
				if (sSignature == null){
					throw new Exception("No salesperson found for this order.");
				}
			}else{
				throw new Exception("No salesperson data found for this salesperson.");
			}
			//Print the signature:
			if (rsOrderAndProposal.getLong(SMTableproposals.TableName + "." + SMTableproposals.isigned) == 1 && bApproved){
				//NOTE: The email version of this has NOT been built:
				/*
				//Here's the signature pad:
				String sSignaturePadOptions = "drawOnly:true,"
					+ " displayOnly:true,"
					+ " errorMessageDraw: \"\","
					+ " lineTop:" + SMTableproposals.SIGNATURE_TOP + ","
					+ " lineColour:\"" + SMTableproposals.SIGNATURE_LINE_COLOUR + "\"," //makes the line transparent
					+ " lineWidth:1,"
					+ " lineMargin:0"
				;
				s += "\n"
					+ "<div role=main>\n"
					+ "<form method=post action=\"" + "DUMMYCLASS" + "\" class=sigPad>\n"
					//Scripts for signature:
					+ "<script src=\"scripts/jquery-signaturepad-min.js\"></script> <script>\n"
					//+ "$(document).ready(function () {\n"
					//+ "    $('.sigPad').signaturePad({" //displayOnly:true, 
					//+ " lineTop:" + SMTablesalesperson.SIG_LINE_TOP 
					//+ ", lineColour:\"#000\", lineWidth:1, lineMargin:0"
					//+ "}).regenerate(" 
			    	//+ sSignature + ");\n"
			    	//+ "});"

					+ "$(document).ready(function () {\n"
					+ "    $('.sigPad').signaturePad({" + sSignaturePadOptions + "}).regenerate(" + sSignature + ");\n"
			    	+ "});"

			    	+ "</script>\n"
			    	+ "<script src=\"scripts/json2.min.js\"></script>"
					+ "<ul class=sigNav>\n"
					+ "</ul> <div class=\"sig sigWrapper\" >\n"
					+ "<div class=typed ></div>\n"
					+ "<span style = \" vertical-align: 100%; \"><B>Signature:</B>&nbsp;</span>";
				if (iRequestType == SMProposalForm.REQUEST_TYPE_EMAIL){
					s += "<img src=\"cid:" + EmailInlineHTML.NAME_OF_SIGNATURE_IMAGE + "\" width=\"" 
						+ SMTableproposals.SIGNATURE_CANVAS_WIDTH + "\" height=\"" + SMTableproposals.SIGNATURE_CANVAS_HEIGHT + "\" />";
				}else{
					s += "<canvas class=pad width=" + SMTableproposals.SIGNATURE_CANVAS_WIDTH
						+ " height=" + SMTableproposals.SIGNATURE_CANVAS_HEIGHT + " style=\"border:0px solid  #000000;\" ></canvas>\n"
						+ "<input type=hidden name='" + "SIGNATUREOUTPUT" + "' class=output>\n";
				}		
				*/
				
				s += "<span style = \" vertical-align: 100%; \"><B>Signature:</B>&nbsp;</span>";
				
				//Display the signature:
				s += "<img "
						+  "width=\"" + SMTableproposals.SIGNATURE_CANVAS_WIDTH + "\" height=\"" + SMTableproposals.SIGNATURE_CANVAS_HEIGHT + "\""
						+ "src=\"data:image/png;base64,"
						//+ "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==\""
						//+ "\""
						+ ServletUtilities.clsBase64Functions.getbase64EncodedStringFromJSON (
							sSignature,
							Integer.parseInt(SMTableproposals.SIGNATURE_CANVAS_WIDTH),
							Integer.parseInt(SMTableproposals.SIGNATURE_CANVAS_HEIGHT),
							Integer.parseInt(SMTableproposals.SIGNATURE_PEN_WIDTH),
							SMTableproposals.SIGNATURE_PEN_R_COLOUR,
							SMTableproposals.SIGNATURE_PEN_G_COLOUR,
							SMTableproposals.SIGNATURE_PEN_B_COLOUR
							)
						+ "\""
						+ " alt=\"Signature\" />";
				
				s += "<span style = \" vertical-align: 100%; \">&nbsp;&nbsp;&nbsp;<B>Date:</B>&nbsp;<U>&nbsp;&nbsp;" 
					+ clsDateAndTimeConversions.now("M/d/yyyy") 
					+ "&nbsp;&nbsp;</U></span>";
			
				s += "<BR>"; 
				//+ "<span style = \" vertical-align: 100%; \">"
				s += getSalespersonTitle(rs);
				//+ "</span>"
				s += "</div>"
				+ "</form>\n\n</div>\n";
					
			}else{
				if (bApproved){
					s += "<B>Signature:</B>&nbsp;_______________________________________";
					s += " &nbsp;&nbsp;&nbsp;<B>Date</B>:&nbsp;___________";
					s	 += "<BR>";
				}
				s += getSalespersonTitle(rs);
			}
		} catch (Exception e) {
			throw new Exception("Error printing salesperson signature block - " + e.getMessage() + ".");
		}
		
		s += "</TD>";
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String getSalespersonTitle(ResultSet rs) throws Exception{
		String s = "";
		if (rs == null){
			return s;
		}
		try {
			s += rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName)
					+ "&nbsp;"
					+ rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName)
					+ ",&nbsp;"
					+ rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonTitle)
				;
				if (rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonEmail).compareToIgnoreCase("") != 0){
					s += "<BR><B>Email:</B>&nbsp;" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonEmail)
					;
				}
				if (rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sDirectDial).compareToIgnoreCase("") != 0){
					s += "<BR><B>Direct Dial:</B>&nbsp;" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sDirectDial);
				}
		} catch (Exception e) {
			throw new Exception("Error salesperson title - " + e.getMessage() + ".");
		}
		return s;
	}
	private String printCustomerSignatureTable(ResultSet rsOrderAndProposal) throws Exception{
		String s = "";
		boolean bApproved;
		try {
			bApproved = rsOrderAndProposal.getLong(SMTableproposals.TableName + "." + SMTableproposals.lapprovedbyuserid) != 0;
		} catch (Exception e) {
			throw new Exception("Error printing customer signature block - " + e.getMessage() + ".");
		}
		s += "<BR>";
		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; \" >";
		s += "<TR><TD style = \" text-align: left; \" >";
		s += "ACCEPTANCE: Terms, price, and specifications on all pages of this proposal are hereby accepted and the work authorized."
			+ "</TD></TR>"
			;
		if (bApproved){
			s += "<TR><TD>"
			+ "<B>Purchaser:</B>&nbsp;__________________________________"
			+     "<B>Title:</B>&nbsp;__________________________________"
			+ "</TD></TR>"
			
			// TJR - 1/29/2018 - added an additional row to force more space above the signature box.  This was requested
			// to make more room for the 'Docu-Sign' signatures:
			
			+ "<TR><TD>"
			+ "&nbsp;"
			+ "</TD></TR>"
			
			+ "<TR><TD>"
			+ "<B>Signature:</B>&nbsp;__________________________________"
			+ "<B>Date of acceptance:</B>&nbsp;________"
			+ "</TD></TR>"
			+ "<TR><TD>"
			+ "Purchaser's PO number if required for invoicing:  _________________ ."
			+ "</TD></TR>"
			;
		}else{
			s += "<TR><TD style= \" text-align:center; \" >" 
				+ "<span style= \" font-weight:bold; font-style:italic; \" >"
				+ "PLEASE NOTE: This is a DRAFT proposal and has NOT been approved for signatures."
				+ "</span></TD></TR>"
			;
		}
		s += "<TR><TD>";
		s += "<p style=\" font-size:x-small; font-style:italic; text-align:center; \">(This proposal #" + sProposalNum 
				+ " printed: " + sCurrentPrintDate + ")</p>";
		s += "</TD>";
		s += "</TR>";
		s += "</TABLE>";
		return s;
	}
	private String printTermsTable(	ResultSet rsOrderAndProposal) throws Exception{
		String s = "";

		s += "<TABLE style= \" width:100%; border-style:none; font-family: Arial; font-weight: normal; \" >";
		s += "<TR><TD style = \" font-family: Arial; font-weight: bold; font-style: italic; text-decoration:underline; \" >" 
				+ "TERMS AND CONDITIONS:" + "</TD></TR>";
		try {
			s += "<TR><TD>Payment terms to be as follows:&nbsp;" 
				+ "<B>" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.spaymentterms).replace("\n", "<BR>")
				+ "</B>"
				+ "</TD></TR>";
			s += "<TR><TD>Prices subject to change if not accepted in:&nbsp;" 
					+ "<B>" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.sdaystoaccept) + "</B>&nbsp;days."
					+ "</TD></TR>";
			s += "<TR><TD>" + rsOrderAndProposal.getString(SMTableproposals.TableName + "." + SMTableproposals.sterms).replace("\n", "<BR>")
					+ "</TD></TR>";
		} catch (Exception e) {
			throw new Exception("Error printing terms block - " + e.getMessage() + ".");
		}
		s += "</TABLE>";
		return s;
	}
}
