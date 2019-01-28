package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMInvoicePrinter;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMPrintInvoice extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMPrintInvoice)){
			return;
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sWarning = "";
		String sInvoiceNumberFrom = clsManageRequestParameters.get_Request_Parameter("InvoiceNumberFrom", request);
		boolean bPrintMultipleInvoices = false;
		if (request.getParameter("PrintMultipleInvoices") != null){
			bPrintMultipleInvoices = true;
			//System.out.println("Print multiple invoice flag is set true");
		}
		String sInvoiceNumberTo = clsManageRequestParameters.get_Request_Parameter("InvoiceNumberTo", request);

		//Customized title
		String sReportTitle = "Invoice";
		out.println();
		out.println(
				//SMUtilities.DOCTYPE
				"<!DOCTYPE html>\n"
				+ "<HTML>\n" 
				+ "<HEAD>\n" 

					//+ "<STYLE TYPE=\"text/css\" media=\"print\">P.breakhere {page-break-before: always;}\n"
					+ "<STYLE TYPE=\"text/css\" media=\"print\">\n"
					+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
					+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
					+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
					+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
					+ "</STYLE>\n"

		       		+ "<TITLE>" + sReportTitle + "</TITLE>\n"
		       		+ "</HEAD>\n" 
		       		+ "<BODY BGCOLOR=\"" + "#FFFFFF" + "\">\n"

				);
		out.println(SMInvoicePrinter.sStyleScripts());

		//Retrieve information
		Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", this.toString() 
				+ " - user: "
				+ sUserID
				+ " - "
				+ sUserFullName
				);
		if (conn == null){
			sWarning = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass 
							+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "Warning=" + sWarning
					);			
			return;
		}

		//log usage of this this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_PRINTINVOICE, "REPORT", "SMPrintInvoice", "[1376509335]");

		//get request parameters
		int iNoofInvoiceCopies = 1;
		try{
			iNoofInvoiceCopies = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter("NOOFINVOICECOPIES", request));
		}catch(NumberFormatException e){
			iNoofInvoiceCopies = 1;
		}
		boolean bShowExtendedPriceForEachItem;
		if (request.getParameter("ShowExtendedPriceForEachItem") != null){
			if (Integer.parseInt(request.getParameter("ShowExtendedPriceForEachItem")) == 1){
				bShowExtendedPriceForEachItem = true;
			}else{
				bShowExtendedPriceForEachItem = false;
			}
		}else{
			bShowExtendedPriceForEachItem = false;
		}

		boolean bShowLaborAndMaterialSubtotals;
		if (request.getParameter("ShowLaborAndMaterialSubtotals") != null){
			//System.out.println("In " + this.toString() + "ShowLaborAndMaterialSubtotals != null");
			if (Integer.parseInt(request.getParameter("ShowLaborAndMaterialSubtotals")) == 1){
				//System.out.println("In " + this.toString() + "ShowLaborAndMaterialSubtotals = 1");
				bShowLaborAndMaterialSubtotals = true;
			}else{
				//System.out.println("In " + this.toString() + "ShowLaborAndMaterialSubtotals != 1");
				bShowLaborAndMaterialSubtotals = false;
			}
		}else{
			//System.out.println("In " + this.toString() + "ShowLaborAndMaterialSubtotals == null");
			bShowLaborAndMaterialSubtotals = false;
		}

		boolean bSuppressDetailsPageBreak;
		if (request.getParameter("SuppressDetailsPageBreak") != null){
			if (Integer.parseInt(request.getParameter("SuppressDetailsPageBreak")) == 1){
				bSuppressDetailsPageBreak = true;
			}else{
				bSuppressDetailsPageBreak = false;
			}
		}else{
			bSuppressDetailsPageBreak = false;
		}

		boolean bShowALLItemsOnInvoiceIncludingDNP;
		if (request.getParameter("ShowALLItemsOnInvoiceIncludingDNP") != null){
			if (Integer.parseInt(request.getParameter("ShowALLItemsOnInvoiceIncludingDNP")) == 1){
				bShowALLItemsOnInvoiceIncludingDNP = true;
			}else{
				bShowALLItemsOnInvoiceIncludingDNP = false;
			}
		}else{
			bShowALLItemsOnInvoiceIncludingDNP = false;
		}

		boolean bShowTaxBreakdown;
		if (request.getParameter("ShowTaxBreakdown") != null){
			if (Integer.parseInt(request.getParameter("ShowTaxBreakdown")) == 1){
				bShowTaxBreakdown = true;
			}else{
				bShowTaxBreakdown = false;
			}
		}else{
			bShowTaxBreakdown = false;
		}
		if (!bPrintMultipleInvoices){
			//System.out.println("Printing single invoice....");
			out.println(PrintInvoice(sInvoiceNumberFrom, 
					bShowExtendedPriceForEachItem,
					bShowLaborAndMaterialSubtotals,
					bShowALLItemsOnInvoiceIncludingDNP,
					bSuppressDetailsPageBreak,
					bShowTaxBreakdown,
					true,
					iNoofInvoiceCopies,
					conn,
					request,
					getServletContext(),
					false
					)
					);
		}else{
			//System.out.println("Printing multiple invoices....");
			out.println(PrintMultipleInvoices(sInvoiceNumberFrom, 
					sInvoiceNumberTo,
					bShowExtendedPriceForEachItem,
					bShowLaborAndMaterialSubtotals,
					bShowALLItemsOnInvoiceIncludingDNP,
					bSuppressDetailsPageBreak,
					bShowTaxBreakdown,
					iNoofInvoiceCopies,
					conn,
					request,
					getServletContext(),
					false)
					);
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080604]");

		out.println("</BODY></HTML>");
	}

	/* - Not used
	private HashMap<String, String> getMultipleLogo(String sInvoiceNumberFrom, String sInvoiceNumberTo,
			HttpServletRequest request, ServletContext servletContext, Connection conn) throws Exception{
		
		HashMap<String,String> map = new HashMap<String, String>();
		String sSQL =  Get_Invoice_Numbers(sInvoiceNumberFrom,sInvoiceNumberTo);
		ArrayList <String>sInvoiceNumbers = new ArrayList<String>();
		try {
			ResultSet rsInvoiceNumbers = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rsInvoiceNumbers.next()) {
				String sCurrentInvoice = rsInvoiceNumbers.getString(SMTableinvoiceheaders.sInvoiceNumber);
				sInvoiceNumbers.add(sCurrentInvoice);
			}
			for(int i = 0; i < sInvoiceNumbers.size(); i++) {
				String sImagePath = getInvoiceLogoFileFromDBA(sInvoiceNumbers.get(i),request, getServletContext(),conn);
				map.put(sInvoiceNumbers.get(i), sImagePath);
			}
		}catch(Exception e ) {
			throw new Exception("ERROR [1545316628] "+e.getMessage());
		}
		return map;
	}
	*/
	public String PrintInvoice(String sInvoiceNum,
			boolean bShowExtendedPriceForEachItem,
			boolean bShowLaborAndMaterialSubtotals,
			boolean bShowALLItemsOnInvoiceIncludingDNP,
			boolean bSuppressDetailsPageBreak,
			boolean bShowTaxBreakdown,
			boolean bIsFirstInvoice,
			int iNoofCopies,
			Connection conn,
			HttpServletRequest request,
			ServletContext context,
			boolean bEmailingInvoice){
		String sOut = "";
		
		String sSQL = "SELECT * FROM "
				+ SMTableinvoiceheaders.TableName + ", " + SMTableinvoicedetails.TableName
				+ " WHERE "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = '" 
				+ clsStringFunctions.PadLeft(sInvoiceNum, " ", 8) + "'"
				+ " AND "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = "
				+ SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber;

		sSQL = "SELECT * FROM "
				+ SMTableinvoiceheaders.TableName + ", " + SMTableinvoicedetails.TableName
				+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
				+ "=" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
				+ " WHERE "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = '" 
				+ clsStringFunctions.PadLeft(sInvoiceNum, " ", 8) + "'"
				+ " AND "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = "
				+ SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber;

		try{
			ResultSet rsInvoice = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if(rsInvoice.next()){
				for (int iCopy=1;iCopy<=iNoofCopies;iCopy++){
					SMInvoicePrinter prninv = null;
					try {
						prninv = new SMInvoicePrinter(
							conn, 
							rsInvoice.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber),
							bEmailingInvoice,
							bShowTaxBreakdown,
							bShowExtendedPriceForEachItem,
							bShowALLItemsOnInvoiceIncludingDNP,
							bShowLaborAndMaterialSubtotals,
							bSuppressDetailsPageBreak,
							bIsFirstInvoice,
							iCopy,
							request,
							context
						);
					} catch (Exception e1) {
						sOut += "<BR><BR><FONT COLOR=RED><B>"
							+ "Error [1487083134] printing invoice '" 
							+ rsInvoice.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber)
							+ "' - " + e1.getMessage()
							+ "</B></FONT><BR><BR>"
						;
						return sOut;
					}
					try {
						sOut += prninv.printOneCopyOfInvoice(conn);
					} catch (Exception e) {
						sOut += "<BR><BR><FONT COLOR=RED><B>"
							+ "Error [1487020301] printing copy of invoice - " + e.getMessage() + ".";
					}
				} //end for loop
			}else{
				sOut += "Invoice not found.";
				return sOut;
			}
			rsInvoice.close();
			return sOut;
		}catch (SQLException e){
			sOut += "Error opening invoice query: " + e.getMessage();
			return sOut;
		}
	}
	String PrintMultipleInvoices(String sInvoiceNumFrom,
			String sInvoiceNumTo,
			boolean bShowExtendedPriceForEachItem,
			boolean bShowLaborAndMaterialSubtotals,
			boolean bShowALLItemsOnInvoiceIncludingDNP,
			boolean bSuppressDetailsPageBreak,
			boolean bShowTaxBreakdown,
			int iNoofInvoiceCopies,
			Connection conn,
			HttpServletRequest request,
			ServletContext context,
			boolean bEmailingInvoices){
		String sOut = "";
		try{
			String sSQL = Get_Invoice_Numbers(sInvoiceNumFrom, sInvoiceNumTo);
			ResultSet rsInvoiceNumbers = clsDatabaseFunctions.openResultSet(sSQL, conn);
			int iInvoicesPrinted = 0;
			boolean bIsFirstInvoice = true;

			while (rsInvoiceNumbers.next()){
				String sCurrentInvoice = rsInvoiceNumbers.getString(SMTableinvoiceheaders.sInvoiceNumber);
				//System.out.println("Processing invoice number " + sCurrentInvoice);
				if (iInvoicesPrinted > 0){
					bIsFirstInvoice = false;
				}

				sOut += PrintInvoice(sCurrentInvoice, 
						bShowExtendedPriceForEachItem,
						bShowLaborAndMaterialSubtotals,
						bShowALLItemsOnInvoiceIncludingDNP,
						bSuppressDetailsPageBreak,
						bShowTaxBreakdown,
						bIsFirstInvoice,
						iNoofInvoiceCopies,
						conn,
						request,
						context,
						bEmailingInvoices);
				iInvoicesPrinted++;
			}
		}catch(SQLException ex){
			sOut += "Error formatting invoice number: " + ex.getMessage();
			return sOut;
		}
		return sOut;
	}


	String Get_Invoice_Numbers(String sFromNumber, String sToNumber){

		String SQL =  "SELECT" + 
				" " + SMTableinvoiceheaders.sInvoiceNumber + 
				" FROM" + 
				" " + SMTableinvoiceheaders.TableName + 
				" WHERE" + 
				" " + SMTableinvoiceheaders.sInvoiceNumber + " >= LPAD('" + sFromNumber.trim() + "'," 
				+ Integer.toString(SMTableorderheaders.sOrderNumberPaddedLength) + ",' ')" + 
				" AND" +
				" " + SMTableinvoiceheaders.sInvoiceNumber + " <= LPAD('" + sToNumber.trim() + "'," + 
				Integer.toString(SMTableorderheaders.sOrderNumberPaddedLength) + ",' ')";
		return SQL;
	}
	
	/* TJR - 1/28/2019 - NOT USED...?
	public String getInvoiceLogoFileFromDBA(String sInvoiceNumber,
											HttpServletRequest request,
											ServletContext context,
											Connection conn,
											String sDBID,
											String sUserID,
											String sUserFullName) throws Exception{
		String SQL = "";
		String sDescription = "";
		String sLogoFileName = "";
		SQL = "SELECT "+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sdbadescription+" FROM "+SMTableinvoiceheaders.TableName
				+" WHERE "+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sInvoiceNumber+" = "+sInvoiceNumber;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sDescription = rs.getString(SMTableinvoiceheaders.sdbadescription).trim();
				}
			rs.close();
			SQL = "SELECT "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sInvoiceLogo+" FROM "
					+" "+SMTabledoingbusinessasaddresses.TableName+" WHERE "
					+" "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sDescription
					+ "= '"+sDescription+"'";

			rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sLogoFileName = rs.getString(SMTabledoingbusinessasaddresses.sInvoiceLogo).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1487709654] getting File Name - " + e.getMessage() + ".");
		}
		sLogoFileName = getFilePath(sLogoFileName,request,context);

		return sLogoFileName;
	}
	
	public String getFilePath(String sFileName, HttpServletRequest request, ServletContext context) {
		String sFullLogoImageFilePath = SMUtilities.getAbsoluteRootPath(request, context);
		
		sFullLogoImageFilePath = sFullLogoImageFilePath.replace(WebContextParameters.getInitWebAppName(context), "");
		while (sFullLogoImageFilePath.endsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath = sFullLogoImageFilePath.substring(0, sFullLogoImageFilePath.length() - 1);
		}
		
		sFullLogoImageFilePath = sFullLogoImageFilePath + System.getProperty("file.separator");
		
		
		if (WebContextParameters.getLocalResourcesPath(context).startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context).substring(1);
		}else{
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context);
		}

		while (sFullLogoImageFilePath.endsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath = sFullLogoImageFilePath.substring(0, sFullLogoImageFilePath.length() - 1);
		}

		sFullLogoImageFilePath = sFullLogoImageFilePath + System.getProperty("file.separator");

		if (sFileName.startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += sFileName.substring(0);
		
		}else{
			sFullLogoImageFilePath += sFileName;
			
		}
		return sFullLogoImageFilePath;
	}
	*/
}
