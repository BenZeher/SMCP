package smic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ACCPACDataDefinition.ICITEM;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelabelprinters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import smcontrolpanel.SMUtilities;

public class ICPrintUPCItemLabel extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	private boolean bTestPrintMode = false;
	private int MAX_QTY_OF_LABELS = 10000;
	private static String UPC_FILE_PREFIX = "UPC";
	public static final String LABELPRINTER_LIST = "LABELPRINTER_LIST";

	public ICPrintUPCItemLabel(
	){
		m_sErrorMessage = "";
	}
	public boolean printLabels(
			Connection conn,
			String sDatabaseType,
			ArrayList <String> sItems,
			ArrayList <Integer> iQuantities,
			ArrayList <Integer> iPieceQuantities,
			String sLabelPrinterID,
			boolean bPrintToPrinter,
			boolean bPrintTwoPerRow,
			PrintWriter out,
			HttpServletRequest req,
			ServletContext context
	){

		//This module assumes the following:
		/*
		 * The two required jar files are found in the class path somewhere on the tomcat server:
		 * barbecue-1.5-beta1.jar AND
		 * jdom.jar
		 * 
		 * And there is a path for writing the temporary jpg files under the tomcat 'ROOT' folder
		 * like this: . . . sm/images/barcodes - it must have permissions set to allow the
		 * tomcat user to create and delete files in it
		 */

		//This line is needed to allow the 'barbecue' bar code tools function when there is
		//no X11 server running on the web server.  The error appears in the web browser to the
		//user as an error in the java.awt class, referencing X11, something like this:
		// 'java.lang.NoClassDefFoundError: Could not initialize class sun.awt. . . .
		System.setProperty("java.awt.headless", "true");

		String sBarCodeImagePath = SMUtilities.getAbsoluteSMTempPath(req, context);
		//sBarCodeImagePath = getAbsoluteBarcodeImagePath(req, context);

		//try to delete any existing bar code files here:
		if (!deleteCurrentBarCodeFiles(sBarCodeImagePath)){
			return false;
		}

		boolean bSuppressBarCodesOnNonStockItems = false;
		ICOption icopt = new ICOption();
		if (!icopt.load(conn)){
			m_sErrorMessage = "Error [1539107826] loading IC options to determine whether to suppress bar codes on non-stock items - " 
				+ icopt.getErrorMessage() + ".";
			return false;
		}
		bSuppressBarCodesOnNonStockItems = icopt.getSuppressBarCodesOnNonStockItems() == 1L;
		
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "LabelPrinter", "Number of items to print = " + sItems.size());
		}

		if (bPrintToPrinter){
			try {
				if (!printLabelsToPrinter(
						sItems,
						iQuantities,
						iPieceQuantities,
						sLabelPrinterID,
						conn,
						sDatabaseType,
						out,
						context
				)
				){
					return false;
				}
			} catch (SocketTimeoutException e) {
				m_sErrorMessage = "Error printing - " + e.getMessage();
				return false;
			} catch (UnknownHostException e) {
				m_sErrorMessage = "Error printing - " + e.getMessage();
				return false;
			} catch (IOException e) {
				m_sErrorMessage = "Error printing - " + e.getMessage();
				return false;
			}
		}else{
			if (bPrintTwoPerRow){
				out.println("<TABLE>");
			}
			for (int i = 0; i < sItems.size(); i++){
				if ( bPrintTwoPerRow && ((i % 2) == 0)){
				    out.println("<TR>");
				}
				if (!printLabel(
						sItems.get(i),
						iQuantities.get(i),
						iPieceQuantities.get(i),
						sBarCodeImagePath,
						conn,
						sDatabaseType,
						out,
						context,
						bPrintTwoPerRow,
						bSuppressBarCodesOnNonStockItems
				)
				){
					if (bDebugMode){
						clsServletUtilities.sysprint(
								this.toString(), 
								"LabelPrinter", 
								"Error printing label on item = " + sItems.get(i)
						);
					}
					return false;
				}else{
					if (bDebugMode){
						clsServletUtilities.sysprint(
								this.toString(), 
								"LabelPrinter", 
								"NO Error printing label on item = " + sItems.get(i)
						);
					}
				}
				if ( bPrintTwoPerRow && ((i % 2) == 1)){
				    out.println("</TR>");
				}
			}
			if (bPrintTwoPerRow){
				out.println("</TABLE>");
			}
		}
		return true;
	}

	private boolean deleteCurrentBarCodeFiles(String sBarCodeImagePath){

		boolean bDeletionSuccessful = true;

		File dir = new File(sBarCodeImagePath);
		if (!dir.exists()) {
			m_sErrorMessage = "Bar code directory does not exist: " + sBarCodeImagePath;
			//System.out.println("In " + this.toString() + " sWarning = " + m_sErrorMessage);
			return true;
		}

		String[] info = dir.list();

		for (int i = 0; i < info.length; i++) {
			File n = new File(sBarCodeImagePath + info[i]);
			if (!n.isFile()) { // skip ., .., other directories, etc.
				continue;
			}
			//System.out.println("removing " + n.getPath());
			if (info[i].startsWith(UPC_FILE_PREFIX)){
				if (!n.delete()){
					m_sErrorMessage = m_sErrorMessage + "Unable to delete " + sBarCodeImagePath + info[i] + "\n";
					bDeletionSuccessful = false;
				}
			}
		}
		return bDeletionSuccessful;
	}

	private boolean printLabel(
			String sItemNum, 
			int iQty,
			int iPiecesQty,
			String sImagePath,
			Connection conn,
			String sDataBaseType,
			PrintWriter pwOut,
			ServletContext context,
			boolean bPrintTwoPerRow,
			boolean bSuppressBarCodesOnNonStockItems){

		if(sItemNum.compareToIgnoreCase("") == 0){
			return true;
		}

		if ((iQty == 0) || (iPiecesQty == 0)){
			return true;
		}

		if (
				(iQty < 0)
				|| (iQty > MAX_QTY_OF_LABELS)

		){
			pwOut.println("Cannot print - quantity requested is " + Integer.toString(iQty) 
					+ " and the program cannot"
					+ " print more than " + Integer.toString(MAX_QTY_OF_LABELS) + " labels."
			);
			return true;
		}

		String sUnitOfMeasure = "";
		String sDescription = "";
		boolean bIsNonStockItem = false;
		String sReportGroup1 = "";
		String sDedicatedToNumber = "";
		String SQL = "";
		if (sDataBaseType.compareToIgnoreCase("MySQL") == 0){
			SQL = "SELECT "
				+ SMTableicitems.sItemDescription + " AS ITEMDESCRIPTION"
				+ ", " + SMTableicitems.sCostUnitOfMeasure + " AS UNITOFMEASURE"
				+ ", " + SMTableicitems.inonstockitem + " AS NONSTOCKITEM"
				+ ", " + SMTableicitems.sreportgroup1 + " AS REPORTGROUP1"
				+ ", " + SMTableicitems.sDedicatedToOrderNumber + " AS DEDICATEDTOORDERNUMBER"
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + sItemNum + "'"
				+ ")"
				;
		}else{
			SQL = "SELECT "
				+ "\"" + ICITEM.sItemDesc + "\" AS ITEMDESCRIPTION"
				+ ", " + ICITEM.sStockUnitMeasure + " AS UNITOFMEASURE"
				+ ", 0 AS NONSTOCKITEM"
				+ ", '' AS REPORTGROUP1"
				+ ", '' AS DEDICATEDTOORDERNUMBER"
				+ " FROM " + ICITEM.TableName
				+ " WHERE ("
				+ ICITEM.sItemNumber + " = '" + sItemNum + "'"
				+ ")"
				;
		}

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sUnitOfMeasure = rs.getString("UNITOFMEASURE");
				sDescription = rs.getString("ITEMDESCRIPTION");
				bIsNonStockItem = rs.getLong("NONSTOCKITEM") == 1;
				sReportGroup1 = rs.getString("REPORTGROUP1");
				sDedicatedToNumber = rs.getString("DEDICATEDTOORDERNUMBER");
				rs.close();
			}else{
				pwOut.println("<B>Item '" + sItemNum + "' not found.</B><BR><BR>");
				rs.close();
				return true;
			}

		}catch (SQLException e){
			pwOut.println("Error reading data for '" + sItemNum + "' - " + e.getMessage() + ".<BR><BR>");
			//System.out.println("In " + this.toString() + ".printLabel - caught SQL error: " + e.getMessage());
			return true;
		}

		String sOutPut = "";

		for (int j = 0; j < iQty; j++){

			for (int i = 1; i <= iPiecesQty; i++){

				Barcode barcode = null;
				String sBarcodeImageURLPath = "";
				
				boolean bPrintBarCode = false;
				
				//If it's the FIRST of the pieces, 
				if (i == 1){
					//Then if it's a STOCK item, print the bar code
					if (!bIsNonStockItem){
						bPrintBarCode = true;
					//If it's a NON-STOCK item,
					}else{
						//Then is the system is flagged to NOT suppress bar codes on non-stock items, print a bar code:
						if (!bSuppressBarCodesOnNonStockItems){
							bPrintBarCode = true;
						}
					}
				}
				
				if (bPrintBarCode){
					try {
						barcode = BarcodeFactory.createCode128A(sItemNum);
						barcode.setDrawingText(false);
						barcode.setBarHeight(27);
						//System.out.println("Bar height = " + barcode.getHeight());
						//System.out.println("Bar width = " + barcode.getWidth());
					} catch (BarcodeException e1) {
						e1.printStackTrace();
						pwOut.println("Error generating code " + sItemNum + ".");
						//System.out.println("In " + this.toString() + ".printLabel - caught barcode exception: " + e1.getMessage());
						return false;
					}
	
					//System.out.println("In " + this.toString() + ".printLabel - going into create file");
	
					File f = null;
					try {
						f = new File(
								sImagePath + UPC_FILE_PREFIX + sItemNum + ".jpg"
						);
					}catch (NullPointerException e){
						//System.out.println("Error creating new file - " + e.getMessage());
						pwOut.println("Error creating new file - " + e.getMessage());
						//System.out.println("In " + this.toString() + ".printLabel - caught null pointer: " + e.getMessage());
					}
					//System.out.println("In " + this.toString() + ".printLabel - trying to save jpg");
					try{
						BarcodeImageHandler.saveJPEG(barcode, f);
					} catch (Exception e) {
						// Error handling here
						//System.out.println("In " + this.toString() + ".printLabel - error saving jpg: " + e.getMessage());
						pwOut.println("Error [1416590300] generating file " + e.getMessage() + ".");
					}
	
					sBarcodeImageURLPath = ".." + WebContextParameters.getsmtempfolder(context);
					System.out.println("[202011427520] " + "sBarcodeImageURLPath = '" + sBarcodeImageURLPath + "'.");
				}
				if (bPrintTwoPerRow){
					sOutPut +=
						"<TD>"
						+ "<B><FONT SIZE=2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + sDescription.trim() + "</FONT></B><BR>"
						+ "<img src=\"" + sBarcodeImageURLPath + UPC_FILE_PREFIX + sItemNum + ".jpg\" height=24 width=270 alt=\"" 
						+ sItemNum + "\"/>"
						+ "<BR><B><FONT SIZE=2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + sItemNum 
						+ "&nbsp;&nbsp;" + sUnitOfMeasure.trim() + "&nbsp;&nbsp;" + sReportGroup1.trim() + "&nbsp;&nbsp;" + sDedicatedToNumber.trim()
						+ "</FONT></B><BR>"
						+ "</TD>"
						;
				}else{
					sOutPut +=
						"<B><FONT SIZE=2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + sDescription.trim() + "</FONT></B><BR>"
						;
						if (bPrintBarCode){
							sOutPut += "<img src=\"" + sBarcodeImageURLPath + UPC_FILE_PREFIX + sItemNum + ".jpg\" height=24 width=270 alt=\"" 
							+ sItemNum + "\"/>";
						}
						sOutPut += "<BR><B><FONT SIZE=2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + sItemNum 
						+ "&nbsp;&nbsp;" + sUnitOfMeasure.trim() + "&nbsp;&nbsp;" + sReportGroup1.trim() + "&nbsp;&nbsp;" + sDedicatedToNumber.trim()
						//+ "&nbsp;&nbsp;" + sComment.trim()
						+ "</FONT></B><BR>"
						+ "<BR>"
						+ "<P CLASS=\"breakhere\">"
						;
				}
			}
		}

		pwOut.println(sOutPut);
		return true;
	}
	@SuppressWarnings("resource")
	private boolean printLabelsToPrinter(
			ArrayList<String> arrItemNum, 
			ArrayList<Integer> arrQty,
			ArrayList<Integer> arrPiecesQty,
			String sLabelPrinterID,
			Connection conn,
			String sDataBaseType,
			PrintWriter pwOut,
			ServletContext context)throws UnknownHostException, IOException, SocketTimeoutException{

		//First get the information for the printer:
		String SQL = "SELECT * FROM " + SMTablelabelprinters.TableName
		+ " WHERE ("
		+ "(" + SMTablelabelprinters.lid + " = " + sLabelPrinterID + ")"
		+ ")"
		;
		String sHost = "";
		int iPort = 0;
		int iTopMargin = 0;
		int iLeftMargin = 0;
		String sFont = "";
		int iBarCodeHeight = 0;
		int iBarCodeWidth = 0;
		int iDarkness = 0;
		int iPrinterLanguage = 0;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sHost = rs.getString(SMTablelabelprinters.sHost);
				iPort = rs.getInt(SMTablelabelprinters.iport);
				iTopMargin = rs.getInt(SMTablelabelprinters.iTopMargin);
				iLeftMargin = rs.getInt(SMTablelabelprinters.iLeftMargin);
				sFont = rs.getString(SMTablelabelprinters.sFont);
				iBarCodeHeight = rs.getInt(SMTablelabelprinters.iBarCodeHeight);
				iBarCodeWidth = rs.getInt(SMTablelabelprinters.iBarCodeWidth);
				iDarkness = rs.getInt(SMTablelabelprinters.iDarkness);
				iPrinterLanguage = rs.getInt(SMTablelabelprinters.iprinterlanguage);
			}else{
				pwOut.println("Could not get printer data for printer with ID: " + sLabelPrinterID);
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e1) {
			addToErrorMessage("Error reading printer data for printer with ID: " 
					+ sLabelPrinterID + " - " + e1.getMessage());
			pwOut.println("Error reading printer data for printer with ID: " 
					+ sLabelPrinterID + " - " + e1.getMessage());
			return false;
		}

		//Determine whether non-stock items should get bar codes on their labels:
		boolean bSuppressBarCodesOnNonStockItems = false;
		ICOption icopt = new ICOption();
		if (!icopt.load(conn)){
			addToErrorMessage("Error [1539105863] loading IC options to determine whether to suppress bar codes on non-stock item labels:"
				+ icopt.getErrorMessage() + ".");
			pwOut.println("Error [1539105863] loading IC options to determine whether to suppress bar codes on non-stock item labels:"
					+ icopt.getErrorMessage() + ".");
			return false;
		}
		bSuppressBarCodesOnNonStockItems = icopt.getSuppressBarCodesOnNonStockItems() == 1;
		//System.out.println("[1539107170] - bSuppressBarCodesOnNonStockItems = " + bSuppressBarCodesOnNonStockItems);
		
		// Create an unbound socket
		Socket echoSocket = new Socket();
		PrintWriter socketout = null;
		BufferedReader in = null;
		try {
			InetAddress addr = InetAddress.getByName(sHost);
			SocketAddress sockaddr = new InetSocketAddress(addr, iPort);

			//echoSocket = new Socket(sHost, iPort);
			echoSocket = new Socket();
			//echoSocket.setSoTimeout(10*1000);
			echoSocket.connect(sockaddr, 10*1000);
			socketout = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					echoSocket.getInputStream()));
		} catch (SocketTimeoutException e){
			addToErrorMessage("Timeout trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			//System.out.println("In " + this.toString() + " - " 
			//		+ "Timeout trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			throw new SocketTimeoutException ("Timeout trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
		} catch (UnknownHostException e) {
			addToErrorMessage("Unknown host trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			//System.out.println("In " + this.toString() + " - " 
			//		+ "Unknown host trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			throw new UnknownHostException ("Can't find host " + sHost + " - " + e.getMessage());
		} catch (IOException e) {
			addToErrorMessage("IOException trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			//System.out.println("In " + this.toString() + " - " 
			//		+ "IOException trying to open socket at " + sHost + " on port " + iPort + " - " + e.getMessage());
			throw new IOException ("Can't get IO  using host " + sHost + " on port " + iPort + " - " + e.getMessage());
		}

		String sTestOutPut = "";
		if (bTestPrintMode){
			System.out.println("[1493916886] - label printing is in TEST mode - printer to printer '" + sLabelPrinterID + "'.");
		}
		
		for (int i = 0; i < arrItemNum.size(); i++){
			//System.out.println("Int i = " + i + ", item = " + arrItemNum.get(i) + ", qty = " + arrQty.get(i)
			//		+ ", pcs = " + arrPiecesQty.get(i));
			//If the item number is blank, or if the qty or number of pieces is zero
			if(
					(arrItemNum.get(i).compareToIgnoreCase("") == 0)
					|| (arrQty.get(i) == 0) 
					|| (arrPiecesQty.get(i) == 0)
			){
				//Do nothing
				//System.out.println("****Ignored");
			}else{
				//System.out.println("****Going into print");
				if (
						//(arrQty.get(i) < 0)
						//|| 
						(arrQty.get(i) > MAX_QTY_OF_LABELS)
				){
					pwOut.println("Cannot print - quantity requested for item number " + arrItemNum.get(i) 
						+ " is " + Integer.toString(arrQty.get(i)) 
						+ " and the program cannot"
						+ " print less than zero or more than " + Integer.toString(MAX_QTY_OF_LABELS) + " labels."
					);
					addToErrorMessage("Qty for item " + arrItemNum.get(i) + "=" + Integer.toString(arrQty.get(i))
							+ "-max is " + Integer.toString(MAX_QTY_OF_LABELS) + ".");
					return false;
				}

				String sUnitOfMeasure = "";
				String sDescription = "";
				boolean bIsNonStockItem = false;
				String sReportGroup1 = "";
				String sDedicatedToOrderNumber = "";
				SQL = "";
				if (sDataBaseType.compareToIgnoreCase("MySQL") == 0){
					SQL = "SELECT "
						+ SMTableicitems.sItemDescription + " AS ITEMDESCRIPTION"
						+ ", " + SMTableicitems.sCostUnitOfMeasure + " AS UNITOFMEASURE"
						+ ", " + SMTableicitems.inonstockitem + " AS NONSTOCKITEM"
						+ ", " + SMTableicitems.sreportgroup1 + " AS REPORTGROUP1"
						+ ", " + SMTableicitems.sDedicatedToOrderNumber + " AS DEDICATEDTOORDERNUMBER"
						+ " FROM " + SMTableicitems.TableName
						+ " WHERE ("
						+ SMTableicitems.sItemNumber + " = '" + arrItemNum.get(i) + "'"
						+ ")"
						;
				}else{
					SQL = "SELECT "
						+ "\"" + ICITEM.sItemDesc + "\" AS ITEMDESCRIPTION"
						+ ", " + ICITEM.sStockUnitMeasure + " AS UNITOFMEASURE"
						+ ", 0 AS NONSTOCKITEM"
						+ ", '' AS REPORTGROUP1"
						+ ", '' AS DEDICATEDTOORDERNUMBER"
						+ " FROM " + ICITEM.TableName
						+ " WHERE ("
						+ ICITEM.sItemNumber + " = '" + arrItemNum.get(i) + "'"
						+ ")"
						;
				}

				try{
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						sUnitOfMeasure = rs.getString("UNITOFMEASURE");
						sDescription = rs.getString("ITEMDESCRIPTION");
						if (rs.getLong("NONSTOCKITEM") == 1){
							bIsNonStockItem = true;
						}
						sReportGroup1 = rs.getString("REPORTGROUP1");
						sDedicatedToOrderNumber = rs.getString("DEDICATEDTOORDERNUMBER");
						rs.close();
					}else{
						pwOut.println("<B>Item '" + arrItemNum.get(i) + "' not found.</B><BR><BR>");
						//System.out.println("Item '" + arrItemNum.get(i) + "' not found.");
						rs.close();
						addToErrorMessage("Item '" + arrItemNum.get(i) + "' not found.");
						return false;
					}

				}catch (SQLException e){
					addToErrorMessage("Error reading data for '" + arrItemNum.get(i) + "' - " + e.getMessage() + ".");
					pwOut.println("Error reading data for '" + arrItemNum.get(i) + "' - " + e.getMessage() + ".<BR><BR>");
					System.out.println("In " + this.toString() + ".printLabel - caught SQL error: " + e.getMessage());
					return true;
				}

				String sOutPut = "";

				//First set the darkness:
				sOutPut = "~SD" + clsStringFunctions.PadLeft(Long.toString(iDarkness), "0", 2);
				socketout.println(sOutPut);
				for (int j = 0; j < arrQty.get(i); j++){

					for (int k = 1; k <= arrPiecesQty.get(i); k++){

						//Format the label here:
						//^XA - this starts the format
						//^FO10,10^AU^FDPulley, RDB Drive With Sprocket^FS - 10 is the X position, 10 is the Y position
						//^A is the font code, U is the actual font, ^FD starts the field, ^FS ends it 
						//^FO10,70^BCN,75,N,N,Y,N^FDSEP12060^FS - ^BC indicates Code 128 UPC, N indicates normal
						//positioning, 75 is the height of the code in dots, etc.
						//^FO10,150^AU^FDELP0146  EA^FS
						//^XZ - this ends the format

						//This worked on the standard printers before I added margin settings:
						/*
					sOutPut += 
						"^XA"
						//Label Home:
						+ "^LH40,0"
						+ "^FO10,10^AU^FD" + sDescription + "^FS" //Description:
						+ "^FO10,70" //Position, x, then y
						+ "^BY4" // 'Module width' in dots (width of bars in bar code)
						+ "^BCN,75,N,N,Y,N^FD" + sItemWithPieceSequence + "^FS" //Bar code - 128A
						+ "^FO10,150^AU^FD" + sItemWithPieceSequence + "   " + sUnitOfMeasure.trim() + "^FS"
						+ "^XZ"
						;
						 */
						/*
						SAMPLE EPL CODE - this prints a description on the first line, then a bar code, then a last line with item number and unit of measure
						N
						A26,26,0,3,1,1,N,"ITEM DESCRIPTION"
						B26,56,0,1A,2,2,40,N,"ABC123"
						A253,86,0,3,1,1,N,"ABC123     EA"
						P1,1
						
						Explanation of EPL codes:
						Start the sequence with an empty line, then an 'N' - this clears the buffer and tells the printer a new job is coming
						
						NOTE - normally a command follows which sets the height and width of the label:
						1) Label height - not used - let the autosensor determine the height of the labels
						2) Label width - not used
						
						Bar code parameters (the line starting with 'B'):
						1) Starting x
						2) Starting y
						3) Rotation (0)
						4) Bar code selection (1A for 128 A)
						5) Narrow bar width
						6) Wide bar width
						7) Bar code height
						8) Print human readable (B for yes, N for no)
						9) TEXT TO CODE
						
						Text parameters (the lines starting with 'A'):
						1) Starting x
						2) Starting y
						3) Rotation (0)
						4) Font (1 - 5)
						5) Horizontal multiplier (1-6, 8)
						6) Vertical multiplier (1-9)
						7) Reverse image (N for no)
						8) TEXT
						
						Last line, which commits the label to print:
						'P1, 1' prints one label set, one copy
						 */

						// So if it's the FIRST label of the set
						boolean bPrintBarCode = false;
						if (k == 1){
							// And if it's a STOCK item:
							if (!bIsNonStockItem){
								bPrintBarCode = true;
							//If it's NOT a stock item...
							}else{
								//Then if the system is NOT flagged to suppress bar codes on non-stock item labels, print the code:
								if (!bSuppressBarCodesOnNonStockItems){
									bPrintBarCode = true;
								}
							}
						}
						if (bPrintBarCode){
							//Print WITH the bar code:
							if (iPrinterLanguage==SMTablelabelprinters.PRINTER_LANGUAGE_ZPL){
								sOutPut = 
										"^XA"
										//Label Home:
										//Setting the margins to 50 left, 10 top for the standard printers
										+ "^LH" + Integer.toString(iLeftMargin) + "," + Integer.toString(iTopMargin)
										+ "^FO0,0^A" + sFont + "^FD" + sDescription + "^FS" //Description:
										+ "^FO0,60" //Position, x, then y
										+ "^BY" + Integer.toString(iBarCodeWidth) // 'Module width' in dots (width of bars in bar code)
										+ "^BCN," + Integer.toString(iBarCodeHeight) + ",N,N,Y,N^FD" + arrItemNum.get(i) + "^FS" //Bar code - 128A
										+ "^FO0,140^A" + sFont + "^FD" 
											+ arrItemNum.get(i) 
											+ "   " + sUnitOfMeasure.trim() 
											+ "   " + sReportGroup1.trim()
											+ "   " + sDedicatedToOrderNumber.trim()
											+ "^FS"
										+ "^XZ"
										;
								
							}
							if (iPrinterLanguage==SMTablelabelprinters.PRINTER_LANGUAGE_EPL){
								sOutPut = 
										"\n"
										+ "N\n"
										+ "A" + Integer.toString(iLeftMargin) + "," + Integer.toString(iTopMargin) 
											+ ",0," + sFont + ",1,1,N,\"" + sDescription.replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n"
										+ "B" + Integer.toString(iLeftMargin) + ",56,0,1A," + Integer.toString(iBarCodeWidth)
											+ "," + Integer.toString(iBarCodeWidth) + "," + Integer.toString(iBarCodeHeight)
											+ ",N,\"" + arrItemNum.get(i) + "\"\n"
										+ "A" + Integer.toString(iLeftMargin) + "," + "140" 
											+ ",0," + sFont + ",1,1,N,\"" + (
												arrItemNum.get(i) 
													+ "   " + sUnitOfMeasure.trim() 
													+ "   " + sReportGroup1.trim()
													+ "   " + sDedicatedToOrderNumber.trim()
												).replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n"										
										+ "P1,1\n"
										;
								//System.out.println("[1370022294] EPL print output with bar code = " + sOutPut);
							}
						}else{
							//Print WITHOUT the bar code:
							if (iPrinterLanguage==SMTablelabelprinters.PRINTER_LANGUAGE_ZPL){
								sOutPut = 
									"^XA"
									//Label Home:
									//Setting the margins to 50 left, 10 top for the standard printers
									+ "^LH" + Integer.toString(iLeftMargin) + "," + Integer.toString(iTopMargin)
									+ "^FO0,0^A" + sFont + "^FD" + sDescription + "^FS" //Description:
									//+ "^FO0,60" //Position, x, then y
									//+ "^BY" + Integer.toString(iBarCodeWidth) // 'Module width' in dots (width of bars in bar code)
									//+ "^BCN," + Integer.toString(iBarCodeHeight) + ",N,N,Y,N^FD" + arrItemNum.get(i) + "^FS" //Bar code - 128A
									+ "^FO0,140^A" + sFont + "^FD" 
										+ arrItemNum.get(i) 
										+ "   " + sUnitOfMeasure.trim() 
										+ "   " + sReportGroup1.trim() 
										+ "   " + sDedicatedToOrderNumber.trim()
										+ "^FS"
									+ "^XZ"
									;
								}
							if (iPrinterLanguage==SMTablelabelprinters.PRINTER_LANGUAGE_EPL){
								sOutPut = 
									"\n"
									+ "N\n"
									+ "A" + Integer.toString(iLeftMargin) + "," + Integer.toString(iTopMargin) 
										+ ",0," + sFont + ",1,1,N,\"" + sDescription.replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n"
									+ "A" + Integer.toString(iLeftMargin) + "," + "140" 
										+ ",0," + sFont + ",1,1,N,\"" + (
											arrItemNum.get(i) 
											+ "   " + sUnitOfMeasure.trim()
											+ "   " + sReportGroup1.trim()
											+ "   " + sDedicatedToOrderNumber.trim()
											).replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n"										
									+ "P1,1\n"
									;
									//System.out.println("[1370022295] EPL print output w/o bar code = " + sOutPut);
								}
						}
						
						//Send the labels for this item to the printer:
						//System.out.println(sOutPut);
						//if (iPrinterLanguage!=SMTablelabelprinters.PRINTER_LANGUAGE_EPL){
						
						//TEST:
						if(bTestPrintMode){
							sTestOutPut += sOutPut;
						}else{
							socketout.println(sOutPut);
						}
						//}
					}
				}
				//Send the labels for this item to the printer:
				//System.out.println(sOutPut);
				//socketout.println(sOutPut);
			}
		}
		try {
			socketout.close();
			in.close();
			echoSocket.close();
		} catch (IOException e) {
			addToErrorMessage("Error closing socket on " + sHost + " on port " + iPort + " - " + e.getMessage());
			throw new IOException ("Error closing socket on " + sHost + " on port " + iPort + " - " + e.getMessage());
		}
		if (sTestOutPut.compareToIgnoreCase("") != 0){
			System.out.println("[1493917035] Label test output:\n" + sTestOutPut);
		}
		return true;
	}
	private void addToErrorMessage(String sMsg){
		if (m_sErrorMessage.length() < 800){
			m_sErrorMessage += "-" + clsServletUtilities.URLEncode(sMsg);
		}
	}
	public String getErrorMessage(){
		return m_sErrorMessage;
	}
}
