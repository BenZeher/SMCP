package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import SMClasses.SMAppointment;
import SMClasses.SMOption;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableappointments;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordersources;
import SMDataDefinition.SMTableprojecttypes;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditBidEntry  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static String BIDPRODUCTMARKER = "BIDPRODUCTMARKER";
	public static final String SALES_CONTACT_ID_SEARCH = "SCIDSearch";
	public static final String FIND_SALES_CONTACT_COMMAND_VALUE = "FINDSALESCONTACT";
	public static final String FIND_SALES_CONTACT_BUTTON_LABEL = "Find <B><FONT COLOR=RED>s</FONT></B>ales contact"; //S
	private static final String DATE_FIELD_WIDTH = "8";
	
	//Colors:
	private static final String BID_DATES_BG_COLOR = "#FFBCA2";
	private static final String BILL_TO_BG_COLOR = "#CCFFB2";
	private static final String SHIPTO_BG_COLOR = "#F2C3FA";
	private static final String ACTUAL_BID_DATES_BG_COLOR = "#99CCFF";
	//private static final String CRITICAL_DATES_BG_COLOR = "#99CCFF";
	private static final String BID_MEMOS_BG_COLOR = "#CCFFB2";
	private static final String BID_PRODUCTS_BG_COLOR = "#99CCFF";
	private static final String BUTTON_COMMANDS_TABLE_BG_COLOR = "#99CCFF";
	
	//Buttons:
	public static final String UPDATE_ANOTHER_BUTTON_LABEL = "Update <B><FONT COLOR=RED>a</FONT></B>nother lead"; //A
	public static final String UPDATE_ANOTHER_COMMAND_VALUE = "UPDATEANOTHER";
	public static final String UPDATE_ANOTHER_FIELD_NAME = "UPDATEANOTHERID";
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; //U
	public static final String UPDATE_COMMAND_VALUE = "UPDATELEAD";
	public static final String CLONE_BUTTON_LABEL = "<B><FONT COLOR=RED>C</FONT></B>lone"; //C
	public static final String CLONE_COMMAND_VALUE = "CLONELEAD";
	public static final String CREATE_QUOTE_BUTTON_LABEL = "Create <B><FONT COLOR=RED>q</FONT></B>uote"; //Q
	public static final String CREATE_QUOTE_COMMAND_VALUE = "CREATEQUOTE";
	public static final String CREATE_ORDER_BUTTON_LABEL = "Create <B><FONT COLOR=RED>o</FONT></B>rder"; //O
	public static final String CREATE_ORDER_COMMAND_VALUE = "CREATEORDER";
	public static final String CREATE_APPOINTMENT_COMMAND_VALUE = "CREATEAPPOINTMENT";
	public static final String CREATE_APPOINTMENT_BUTTON_LABEL = "Create Appointment";
	public static final String CREATE_CRITICALDATE_COMMAND_VALUE = "CREATECRITICALDATE";
	public static final String CREATE_CRITICALDATE_BUTTON_LABEL = "Add Critical Date";
	public static final String FIND_CUSTOMER_COMMAND_VALUE = "FINDCUSTOMER";
	public static final String FIND_CUSTOMER_BUTTON_LABEL = "<B><FONT COLOR=RED>F</FONT></B>ind customer"; //F
	public static final String CUSTOMER_SEARCH = "CUSTOMERSEARCH";
	public static final String ADD_NEW_BUTTON_LABEL = "Create <B><FONT COLOR=RED>n</FONT></B>ew lead"; //N
	public static final String ADD_NEW_COMMAND_VALUE = "CREATENEWLEAD";
	public static final String TEST_MAP_LINK_BUTTON_LABEL = "<B><FONT COLOR=RED>T</FONT></B>est map link"; //T
	public static final String TEST_MAP_LINK_COMMAND_VALUE = "TESTMAPLINK";
	//public static final String CREATE_DOCUMENT_FOLDER_BUTTON_LABEL = "C<B><FONT COLOR=RED>r</FONT></B>eate document folder in Google Drive"; //R
	//public static final String CREATE_DOCUMENT_FOLDER_COMMAND_VALUE = "CREATEDOCFOLDER";
	public static final String CREATE_UPLOAD_FILE_BUTTON_LABEL = "U<B><FONT COLOR=RED>p</FONT></B>load File (and create folder) to Google Drive"; //P
	//public static final String UPLOAD_FILE_COMMAND_VALUE = "UPLOADFILE";
	
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to <B><FONT COLOR=RED>G</FONT></B>oogle Drive"; //G
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";
	
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";

	
	//These are fields that store the saved value of the dates so we can tell if they've changed because the date picker does NOT
	//trigger the 'onchange' event:
	
	
	private boolean bDebugMode = false;
	
	private String EMPTY_DATETIME_STRING = "00/00/0000 00:00 AM";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMBidEntry entry = new SMBidEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName() + ": " + entry.getlid(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditBidAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditBids
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditBids)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		if (smedit.getAddingNewEntryFlag()){
			entry.slid("-1");
			//We need to add the blank product types to the entry now since a new sales lead won't
			//have any product amounts to add:
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					smedit.getsDBID(), 
					"MySQL", 
					this.toString() + ".doPost - user: " + smedit.getUserID() + " - " + smedit.getFullUserName());
			
			if (conn == null){
				smedit.getPWOut().println("Error getting connection to load initial product list: " + entry.getErrorMessages());
				return;
			}
			
			if (!entry.loadBidProductTypes(conn)){
				smedit.getPWOut().println("Error loading initial product list: " + entry.getErrorMessages());
				return;
			}
			
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080470]");
		}
		
		//If this is a 'resubmit', meaning it's being called by SMEditBidEntryAction, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute("BidEntry") != null){
	    	entry = (SMBidEntry) currentSession.getAttribute("BidEntry");
	    	currentSession.removeAttribute("BidEntry");
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMBidEntry.ParamID + "=" + entry.slid()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
		    	}
	    	}
	    }
	    if (smedit.getRequest().getParameter(SALES_CONTACT_ID_SEARCH) != null){
	    	//if this comes from sales contact lookup, put that value in
	    	entry.setisalescontactid(smedit.getRequest().getParameter("id"));
	    }

	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getJQueryIncludeString());
	    smedit.getPWOut().println(SMUtilities.getJQueryUIIncludeString());
	    
		//Add places API to ship to address lines. 
		String sPlacesAPIIncludeString = clsServletUtilities.getPlacesAPIIncludeString(getServletContext(), smedit.getsDBID());
		if(sPlacesAPIIncludeString.compareToIgnoreCase("") != 0) {		
			smedit.getPWOut().println(clsServletUtilities.getPlacesJavascript(
				"Enter address",
				SMBidEntry.Paramsshiptoaddress1,
				SMBidEntry.Paramsshiptocity,
				SMBidEntry.Paramsshiptostate,
				SMBidEntry.Paramsshiptozip));
			smedit.getPWOut().println(sPlacesAPIIncludeString);
		}
		
	    //If it's NOT a 'NEW' sales lead, add a link to view any sales lead documents:
		if (entry.slid().compareToIgnoreCase("-1") != 0){
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					smedit.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(
						this.toString()) + ".doPost - userID: " + smedit.getUserID());
			boolean bAllowBidView = false;
			//If the user has rights to edit orders, add a link to that here:
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMManageOrders, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			    smedit.getPWOut().println(
			    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Manage orders</A><BR>");
			}
			
			smedit.addToURLHistory("Edit Sales Lead " + entry.slid());
			smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">\nReturn to...</A><BR>");
			
			if (conn != null){
				bAllowBidView = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.SMViewOrderDocuments, 
							smedit.getUserID(), 
							conn,
							(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080471]");
			}
			if (bAllowBidView){
				SMOption smopt = new SMOption();
				if (!smopt.load(conn)){
					smedit.getPWOut().println("<BR>Error - could not load SMOptions - " + smopt.getErrorMessage() + "<BR>");
				}
				if (smopt.getBidDocsFTPUrl().compareToIgnoreCase("") !=0){
					smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListBidDocuments"
							+ "?BidNumber=" + entry.slid() 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
							+ "\">" + SMBidEntry.ParamObjectName + " Documents</A>"
		    		);
				}
			    if (entry.getsgdoclink().trim().compareToIgnoreCase("") != 0){
				    smedit.getPWOut().println("<A HREF=\"" + entry.getsgdoclink() 
						+ "\">Google Drive folder</A>&nbsp;"
				    		);
			    }
			}

		}
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry,smedit.getsDBID()), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMBidEntry.ParamID + "=" + entry.slid()
				+ "&Warning=Could not load entry ID: " + entry.slid() + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMBidEntry entry, String sDBID) throws SQLException{

		String s = "";
		s += sStyleScripts();
		s += sCommandScripts(sm, entry);
		
		boolean bUseGoogleDrivePicker = false;
		String sPickerScript = "";
			try {
			 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
						SMCreateGoogleDriveFolderParamDefinitions.SALESLEAD_RECORD_TYPE_PARAM_VALUE,
						entry.slid(),
						getServletContext(),
						sm.getsDBID());
			} catch (Exception e) {
				System.out.println("[1557921368] - Failed to load drivepicker.js - " + e.getMessage());
			}
	
			if(sPickerScript.compareToIgnoreCase("") != 0) {
				s += sPickerScript;
				bUseGoogleDrivePicker = true;
			}
			
		//Store which command button the user has chosen:
		String sCreatedBy = " originally created by " + entry.getscreatedbyfullname();
		//The sales lead ID:
		String sID = "";
		if (entry.slid().compareToIgnoreCase("-1") == 0){
			sID = "NEW";
			sCreatedBy = "";
		}else{
			sID = entry.slid();
		}
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + COMMAND_FLAG + "\""
				+ "\">";
		s += SMBidEntry.ParamObjectName 
				+ "&nbsp;" 
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditBidEntry"
				+ "?" + SMBidEntry.ParamID + "=" + sID 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID 
				+ "\"><span style = \" font-size:large; font-weight:bold; \" >" 
				+ sID + "</span></A>"  			
				+ sCreatedBy;
		
		if (entry.slid().compareToIgnoreCase("-1") != 0){
			if (entry.getdatcreatedtime().replace("\"", "&quot;").compareToIgnoreCase(
	        		SMBidEntry.EMPTY_DATETIME_STRING) != 0){
	        	s += " on " + entry.getdatcreatedtime().replace("\"", "&quot;");
			}
		}
		//Hidden values:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.ParamID + "\" VALUE=\"" + entry.getlid() + "\""
		+ " id=\"" + SMBidEntry.ParamID + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramscreatedbyfullname + "\" VALUE=\"" + entry.getscreatedbyfullname() + "\""
		+ " id=\"" + SMBidEntry.Paramscreatedbyfullname + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlcreatedbyuserid + "\" VALUE=\"" + entry.getlcreatedbyuserid() + "\""
		+ " id=\"" + SMBidEntry.Paramlcreatedbyuserid + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.ParamdatCreatedTime + "\" VALUE=\"" + entry.getdatcreatedtime() + "\""
		+ " id=\"" + SMBidEntry.ParamdatCreatedTime + "\""
		+ "\">";

		//Store the last saved values for the date fields, so we can tell if the user changed them.  The 'onchange' event won't pick this
		//up if the user changes a date with the date picker, so we have to do it this way:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddatoriginationdate + "\" VALUE=\"" + entry.getslastsaveddatoriginationdate() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddatoriginationdate + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddattimebiddate + "\" VALUE=\"" + entry.getslastsaveddattimebiddate() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddattimebiddate + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddatplansreceived + "\" VALUE=\"" + entry.getslastsaveddatplansreceived() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddatplansreceived + "\""
		+ "\">";

		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddattakeoffcomplete + "\" VALUE=\"" + entry.getslastsaveddattakeoffcomplete() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddattakeoffcomplete + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddatpricecomplete + "\" VALUE=\"" + entry.getslastsaveddatpricecomplete() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddatpricecomplete + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramlastsaveddattimeactualbiddate + "\" VALUE=\"" + entry.getslastsaveddattimeactualbiddate() + "\""
		+ " id=\"" + SMBidEntry.Paramlastsaveddattimeactualbiddate + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMBidEntry.Paramscreatedfromordernumber + "\" VALUE=\"" + entry.getscreatedfromordernumber() + "\""
		+ " id=\"" + SMBidEntry.Paramscreatedfromordernumber + "\""
		+ "\">";
		
		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">";
		
		//End hidden values
		
		s += "</B>.";
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial;\">\n";		
		
		//Create the order commands line at the bottom:
		s += "<TR><TD>\n";
		s += createCommandsTable(sm);
		s += "</TD></TR>";
		
		//Create the header table:
		s += "<TR><TD><TABLE style=\" title:GeneralHeaderArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + BILL_TO_BG_COLOR + "; \">" 
			+ createBillToTable(sm, entry) + "</TD>\n";
		s += "<TD style=\" vertical-align:top; background-color: " + SHIPTO_BG_COLOR + "; \">" 
			+ createShipToTable(sm, entry) + "</TD>\n";
		s += "</TR></TD></TABLE style=\" title:ENDGeneralHeaderArea; \">\n";

		//Create the bid dates area table:
		s += "<TR><TD><TABLE style=\" title:BidDatesArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + BID_DATES_BG_COLOR + "; \">" 
			+ createBidDatesTable(sm, entry) + "</TD>\n";
		s += "<TD style=\" vertical-align:top; background-color: " + ACTUAL_BID_DATES_BG_COLOR + "; \">" 
			+ createActualDatesTable(sm, entry) + "</TD>\n";
		s += "</TR></TD></TABLE style=\" title:ENDBidDatesArea; \">\n";
		
		//Create the order memo table:
		s += createBidMemosTable(sm, entry, bUseGoogleDrivePicker);
		
		//Create the bid products table:
		s += createBidProductsTable(sm, entry);
		
		//Create the order commands line at the bottom:
		//TODO - get this back in:
		//s += "<TR><TD>\n";
		//s += createCommandsTable();
		//s += "</TD><TR>\n";
		
		//Close the parent table:
		s += "</TABLE style=\" title:ENDParentTable; \">";
		s += "<p><FONT COLOR=RED>*</FONT>&nbsp;Required fields.</p>";
		
		//Explanations of the functions:
		s += "<span style= \" font-size:small; \" >";
		s += "<B>CLONING</B> initializes a new sales lead, with the same email address, sales contact ID, phone, alternate phone,"
			+ " fax, contact, bill-to name, and salesperson as the current " + SMBidEntry.ParamObjectName.toLowerCase() + ".";
		s += "<BR><B>CREATING A NEW ORDER/QUOTE</B> initializes a new order (or quote) with the same salesperson, order source,"
			+ " second (alternate) phone number, and ship to address information."
			+ "<BR>If you are creating a QUOTE and you have <I>NOT</I> chosen a customer, the ship-to information will be copied into the bill-to"
			+ " fields on the quote."
			+ "<BR>If you are creating an order OR a quote, and you <I>HAVE</I> chosen a customer, then the discount level, on hold status,"
			+ " bill-to adress, customer name, control account set, price list code, and terms will be copied to the new order/quote."
			+ "  The bill-to phone number, fax, and email will default to those stored with the customer, but if any are blank, then"
			+ " the ones from the sales lead will be used."
			+ "<BR><B>SPECIAL CASE:</B>If you are creating an order OR a quote and you HAVE chosen a customer, but that customer has NO address information,"
			+ " then the ship-to information from the sales lead will be copied into the bill-to on the order/quote.  (This is done"
			+ " because the system assumes that this customer is a 'template' customer used for multiple accounts.)"
		;
		s += "</span>";
		return s;
	}
	private String createBillToTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:BillTo; \">\n";

		//Salesperson:
	    ArrayList<String> sValues = new ArrayList<String>();
	    ArrayList<String> sDescriptions = new ArrayList<String>();
    	try{
    		//Salesperson
        	ResultSet rsUserInfo = clsDatabaseFunctions.openResultSet(
        		MySQLs.Get_User_By_Username(sm.getUserName()),
        		getServletContext(),
        		sm.getsDBID(),
        		"MySQL",
        		this.toString() + " reading user info - user: " + sm.getUserID()
        		+ " - "
        		+ sm.getFullUserName()
        		);
        	rsUserInfo.close();
    	}catch(SQLException e){
    		throw e;
    	}
    	//Select list:
	    try{
	        ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
	        		MySQLs.Get_Salesperson_List_SQL(),
	        		getServletContext(),
	        		sm.getsDBID(),
	        		"MySQL",
	        		this.toString() + " reading salespersons - user: " + sm.getUserID()
	        		+ " - "
	        		+ sm.getFullUserName()
	        		);
	        while (rsSalespersons.next()){
	        	sValues.add((String) rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim());
	        	sDescriptions.add(
	        			(String) (rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim() 
	        			+ " - " + rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName).trim()
	        			+ " " + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName).trim())
	        	);
	        }
	        rsSalespersons.close();
	    }catch(SQLException e){
	    	throw e;
	    }
	    
		s += "<TR>" + "<TD class=\" fieldlabel \"><B>Salesperson<FONT COLOR=RED>*</FONT>:</B></TD>";
		String sFirstOption = "-- Select a salesperson --";
		//If there are NO salespersons:
		if (sValues.size() == 0){
			sFirstOption = "** (NO SALESPERSONS) **";
		}
		s += "<TD class=\"fieldcontrol\" COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<SELECT ID= \"" + SMBidEntry.Paramssalespersoncode + "\""
			+ " onchange=\"flagDirty();\""
			+ " NAME=\"" + SMBidEntry.Paramssalespersoncode + "\"" + ">";
		s += "<OPTION VALUE=\"" + "" + "\">" + sFirstOption + "</OPTION>";
		for (int i = 0; i < sValues.size(); i++){
			s += "<OPTION";
			if (entry.getssalespersoncode().compareToIgnoreCase(sValues.get(i)) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + sValues.get(i) + "\">" + sDescriptions.get(i) + "</OPTION>";
		}
		s += "</SELECT>";
		s += "</TD></TR>";
		
		
		
		//Origination date
        String sDefaultDate = "";
        if (entry.getdatoriginationdate().replace("\"", "&quot;").compareToIgnoreCase(
        		SMBidEntry.EMPTY_DATE_STRING) == 0){
        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy");
        }else{
        	sDefaultDate = entry.getdatoriginationdate().replace("\"", "&quot;");
        }
        s += "<TR>" + "<TD class=\" fieldlabel \"><B>Origination date<FONT COLOR=RED>*</FONT>:</B></TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramdatoriginationdate + "\""
			+ " VALUE=\"" + sDefaultDate + "\""
			+ " id = \"" + SMBidEntry.Paramdatoriginationdate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + DATE_FIELD_WIDTH
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(SMBidEntry.Paramdatoriginationdate, getServletContext())
		;
		
    	//Project Type
        sValues.clear();
        sDescriptions.clear();
        //First, add a blank to make sure the user selects one:
        sValues.add("");
        sDescriptions.add("-- Select a project type --");
    	String smSQL = "SELECT * FROM " + SMTableprojecttypes.TableName;
        try{
			ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(
					smSQL,
	        		getServletContext(),
	        		sm.getsDBID(),
	        		"MySQL",
	        		this.toString() + " reading project types - user: " + sm.getUserID()
	        		+ " - "
	        		+ sm.getFullUserName()
			);
			while (rsProjectTypes.next()){
				sValues.add(rsProjectTypes.getString(SMTableprojecttypes.iTypeId));
				sDescriptions.add(rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc));
			}
			rsProjectTypes.close();
        } catch(SQLException e){
	    	throw e;
        }
		s += "&nbsp;<B>Project type<FONT COLOR=RED>*</FONT>:</B>";
		s += "<SELECT ID= \"" + SMBidEntry.Paramiprojecttype + "\""
			+ " onchange=\"flagDirty();\""
			+ " NAME=\"" + SMBidEntry.Paramiprojecttype + "\"" + ">";
		for (int i = 0; i < sValues.size(); i++){
			s += "<OPTION";
			if (entry.getiprojecttype().compareToIgnoreCase(sValues.get(i)) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + sValues.get(i) + "\">" + sDescriptions.get(i) + "</OPTION>";
		}
		s += "</SELECT>";
		s += "</TD></TR>";

		//Sales Group:
				s += "<TR>";
				s += "<TD class=\" fieldlabel \">Sales group<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
					+ "<TD class=\" fieldcontrol \">"
					+ "<SELECT NAME=\"" + SMBidEntry.Paramlsalesgroupid + "\"" 
					+ " id = \"" + SMBidEntry.Paramlsalesgroupid + "\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					+ "<OPTION VALUE=\"" + "0" + "\"> -- Select a sales group --</OPTION>";
				String SQL = "SELECT"
					+ " " + SMTablesalesgroups.iSalesGroupId
					+ ", " + SMTablesalesgroups.sSalesGroupCode
					+ ", " + SMTablesalesgroups.sSalesGroupDesc
					+ " FROM " + SMTablesalesgroups.TableName
					+ " ORDER BY " + SMTablesalesgroups.sSalesGroupCode
				;
				try {
					ResultSet rsSalesgroups = clsDatabaseFunctions.openResultSet(
							SQL, 
							getServletContext(), 
							sm.getsDBID(), 
							"MySQL", 
							this.toString() + " [1524061396] SQL: " + SQL);
					while (rsSalesgroups.next()){
						String sSalesGroupID = Long.toString(rsSalesgroups.getLong(SMTablesalesgroups.iSalesGroupId));
						s += "<OPTION";
						if (sSalesGroupID.compareToIgnoreCase(entry.getlsalesgroupid()) == 0){
							s += " selected=YES ";
						}
						s += " VALUE=\"" + sSalesGroupID + "\">" 
						+ rsSalesgroups.getString(SMTablesalesgroups.sSalesGroupCode).trim()
						+ " " + rsSalesgroups.getString(SMTablesalesgroups.sSalesGroupDesc).trim()
						+ "</OPTION>";
					}
					rsSalesgroups.close();
				} catch (SQLException e) {
					throw new SQLException("Error loading sales groups with SQL: " + SQL + " - " + e.getMessage());
				}

				s += "</SELECT>";
				s += "</TD></TR>";
				
		//Order source:
    	sValues.clear();
        sDescriptions.clear();
        //First, add a blank to make sure the user selects one:
        sValues.add("0");
        sDescriptions.add("-- Select a marketing source --");
        try{ 
		    //order source list
	        String sSQL = MySQLs.Get_OrderSource_List_SQL();
	        ResultSet rsOrderSources = clsDatabaseFunctions.openResultSet(sSQL, 
				 getServletContext(), 
				 sm.getsDBID(),
				 "MySQL",
				 this.toString() + " reading order sources - user: " + sm.getUserID()
				 + " - "
				 + sm.getFullUserName()
				 );
	    	while (rsOrderSources.next()){
				sValues.add(rsOrderSources.getString(SMTableordersources.iSourceID));
				sDescriptions.add(rsOrderSources.getString(SMTableordersources.sSourceDesc));
			}
	    	rsOrderSources.close();
        } catch(SQLException e){
	    	throw e;
        }

		s += "<TR>" + "<TD class=\" fieldlabel \"><B>Marketing source:</B></TD>";
		s += "<TD class=\"fieldcontrol\" COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<SELECT ID= \"" + SMBidEntry.ParamiOrderSourceID + "\""
			+ " onchange=\"flagDirty();\""
			+ " NAME=\"" + SMBidEntry.ParamiOrderSourceID + "\"" + ">";
		for (int i = 0; i < sValues.size(); i++){
			s += "<OPTION";
			if (entry.getiordersourceid().compareToIgnoreCase(sValues.get(i)) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + sValues.get(i) + "\">" + sDescriptions.get(i) + "</OPTION>";
		}
		s += "</SELECT>";
		s += "</TD></TR>";
		
		String sContactIDFinderButton = "<button type=\"button\""
			+ " value=\"" + FIND_SALES_CONTACT_COMMAND_VALUE + "\""
			+ " name=\"" + FIND_SALES_CONTACT_COMMAND_VALUE + "\""
			+ " onClick=\"findsalescontact();\">"
			+ FIND_SALES_CONTACT_BUTTON_LABEL
			+ "</button>\n";

		//Sales Contact ID:
		s += "<TR>"	+ "<TD class=\" fieldlabel \"><B>Sales contact ID:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramisalescontactid + "\""
			+ " VALUE=\"" + entry.getisalescontactid() + "\""
			+ " id = \"" + SMBidEntry.Paramemailaddress + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=8"
			+ " MAXLENGTH=13"
			+ ">"
			+ "&nbsp;&nbsp;" + sContactIDFinderButton
			+ "</TD>";
		s += "</TD></TR>";
		
		//Status
		String sChecked = "";
		sValues.clear();
		sDescriptions.clear();
        sValues.add(SMTablebids.STATUS_PENDING);
        sValues.add(SMTablebids.STATUS_SUCCESSFUL);
        sValues.add(SMTablebids.STATUS_UNSUCCESSFUL);
        sValues.add(SMTablebids.STATUS_INACTIVE);
        sDescriptions.add("<B>Pending</B>&nbsp;&nbsp;");
        sDescriptions.add("<B>Successful</B>&nbsp;&nbsp;");
        sDescriptions.add("<B>Unsuccessful</B>&nbsp;&nbsp;");
        sDescriptions.add("<B>Inactive</B>");

		s += "<TR>"	+ "<TD class=\" fieldlabel \">" + SMBidEntry.ParamObjectName + " status:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		for (int i = 0; i < sValues.size(); i ++){
			if (sValues.get(i).compareToIgnoreCase(entry.getsstatus()) == 0){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			s += "<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.Paramsstatus + "\""
				+ " VALUE=" + sValues.get(i) + " " + sChecked
				+ " id = \"" + SMBidEntry.Paramsstatus + "\""
				+ " onchange=\"flagDirty();\""
				+ " >" + sDescriptions.get(i)
				+ "</LABEL>"
			;
		}
		s += "</TD></TR>";
		
		//Bill-to name
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Bill-to name<FONT COLOR=RED>*</FONT>:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramscustomername + "\""
			+ " VALUE=\"" + entry.getscustomername().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramscustomername + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.scustomernameLength)
			+ ">"
		;
		s += "</TD></TR>";
			
		//Bill to contact:
		s += "<TR>"
			+ "<TD class=\" fieldlabel \"><B>Contact:</B></TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramscontactname + "\""
			+ " VALUE=\"" + entry.getscontactname().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramscontactname + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.scontactnameLength)
		;
		s += "</TD></TR>";
		
		//Bill to phone:
		if(entry.getsphonenumber().replace("\"", "&quot;").compareToIgnoreCase("") == 0) {

			s += "<TR>"
					+ "<TD class=\" fieldlabel \"><B>Phone:</B></TD>";
			s += "<TD>"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsphonenumber + "\""
					+ " VALUE=\"" + entry.getsphonenumber().replace("\"", "&quot;") + "\""
					+ " id = \"" + SMBidEntry.Paramsphonenumber + "\""
					+ " onchange=\"flagDirty();\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTablebids.sphonenumberLength)
					+ ">"
					;
		}else {
			s +=  "  <TR>\n"
					+ "<TD class=\" fieldlabel \"><A HREF=\"tel:" +entry.getsphonenumber()  + "\"><B>Phone</B></A>:</TD>\n";
			s += "<TD>"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsphonenumber + "\""
					+ " VALUE=\"" + entry.getsphonenumber().replace("\"", "&quot;") + "\""
					+ " id = \"" + SMBidEntry.Paramsphonenumber + "\""
					+ " onchange=\"flagDirty();\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTablebids.sphonenumberLength)
					+ ">"
					;
		}
		
		//Alternate phone:
		if(entry.getsaltphonenumber().replace("\"", "&quot;").compareToIgnoreCase("") == 0) {
			s += "&nbsp;<B>Alternate phone:</B>"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsaltphonenumber + "\""
					+ " VALUE=\"" + entry.getsaltphonenumber().replace("\"", "&quot;") + "\""
					+ " id = \"" + SMBidEntry.Paramsaltphonenumber + "\""
					+ " onchange=\"flagDirty();\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTablebids.saltphonenumberLength)
					+ ">"
				;
		} else {
			s += "&nbsp;<A HREF=\"tel:" +entry.getsphonenumber()  + "\"><B>Alternate phone</B></A>:\n"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsaltphonenumber + "\""
					+ " VALUE=\"" + entry.getsaltphonenumber().replace("\"", "&quot;") + "\""
					+ " id = \"" + SMBidEntry.Paramsaltphonenumber + "\""
					+ " onchange=\"flagDirty();\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTablebids.saltphonenumberLength)
					+ ">"
				;
		}
		
		//Fax:
		s += "&nbsp;<B>Fax:</B>&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsfaxnumber + "\""
			+ " VALUE=\"" + entry.getsfaxnumber().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsfaxnumber + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sfaxnumberLength)
			+ ">"
		;
		
		s += "</TD></TR>";
		
		//Email:
		s += "<TR>"
				+ "<TD class=\" fieldlabel \"><B>Email:</B>&nbsp;</TD>";
			s += "<TD class=\" fieldcontrol \">"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramemailaddress + "\""
				+ " VALUE=\"" + entry.getemailaddress().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMBidEntry.Paramemailaddress + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTablebids.emailaddressLength)
			;
			s += "</TD></TR>";
			
		//Close the table:
		s += "</TABLE style=\" title:ENDBillTo; \">\n";
		return s;
	}
	private String createShipToTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:CustomerShipTo; \">\n";
		
		//The ship-to name:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Ship-to name<FONT COLOR=RED>*</FONT>:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsprojectname + "\""
			+ " VALUE=\"" + entry.getsprojectname().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsprojectname + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sprojectnameLength)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to address 1:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Address:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptoaddress1 + "\""
			+ " VALUE=\"" + entry.getsshiptoaddress1().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptoaddress1 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptoaddress1Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to address 2:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptoaddress2 + "\""
			+ " VALUE=\"" + entry.getsshiptoaddress2().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptoaddress2 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptoaddress2Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to address 3:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptoaddress3 + "\""
			+ " VALUE=\"" + entry.getsshiptoaddress3().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptoaddress3 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptoaddress3Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to address 4:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">" + createTestMapLinkButton() + "</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptoaddress4 + "\""
			+ " VALUE=\"" + entry.getsshiptoaddress4().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptoaddress4 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptoaddress4Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to city:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">City:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptocity + "\""
			+ " VALUE=\"" + entry.getsshiptocity().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptocity + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptocityLength)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to state:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">State:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptostate + "\""
			+ " VALUE=\"" + entry.getsshiptostate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptostate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptostateLength)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Ship-to zip:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Postal code:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsshiptozip + "\""
			+ " VALUE=\"" + entry.getsshiptozip().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsshiptozip + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptozipLength)
			+ ">"
		;
		s += "</TD></TR>";
		
		//Close the table:
		s += "</TABLE style=\" title:ENDCustomerShipTo; \">\n";
		return s;
	}
	private String createBidDatesTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:BidDatesTable; \">\n";
		
		ArrayList<String>sValues = new ArrayList<String>(0);
		ArrayList<String>sDescriptions = new ArrayList<String>(0);
        sValues.add("0");
        sValues.add("1");
        sDescriptions.add("N/A&nbsp;&nbsp;");
        String sDefaultDate = "";
        if (entry.getdattimebiddate().replace("\"", "&quot;").compareToIgnoreCase(
        		EMPTY_DATETIME_STRING) == 0){
        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy hh:mm a");
        }else
        	sDefaultDate = entry.getdattimebiddate().replace("\"", "&quot;");
        sDescriptions.add(Create_Edit_Form_DateTime_Input_Field(
			SMBidEntry.Paramdattimebiddate, 
			sDefaultDate,
			getServletContext()
    		)
        );
        
        String sDefaultValue = "";
        //If the lead HAS a lead date, then indicate that and display the date:
		if (entry.getdattimebiddate().startsWith(SMBidEntry.EMPTY_DATE_STRING)){
			sDefaultValue = "0";
		}else{
			sDefaultValue = "1";		
		}   
    	
		s += "<TR>"	+ "<TD class=\" fieldlabel \">Bid date:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		String sChecked = "";
		for (int i = 0; i < sValues.size(); i ++){
			if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			s += "<INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.ParamHasBidDate + "\""
				+ " VALUE=" + sValues.get(i) + " " + sChecked 
				+ " id = \"" + SMBidEntry.ParamHasBidDate + "\""
				+ " onchange=\"flagDirty();\""
				+ " >" + sDescriptions.get(i)
			;
		}
		s += "</TD></TR>";
		
		//Plans received date:
        sValues.clear();
        sDescriptions.clear();
        sValues.add("0");
        sValues.add("1");
        sDescriptions.add("N/A&nbsp;&nbsp;");
        if (entry.getdatplansreceived().replace("\"", "&quot;").compareToIgnoreCase(
        		SMBidEntry.EMPTY_DATE_STRING) == 0){
        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy");
        }else{
        	sDefaultDate = entry.getdatplansreceived().replace("\"", "&quot;");
        }
        
        sDescriptions.add(Create_Edit_Form_Date_Input_Field(
				SMBidEntry.Paramdatplansreceived, 
				sDefaultDate,
				getServletContext())
        );
        sDefaultValue = "";
        //If it HAS a date, then indicate that and display the date:
		if (entry.getdatplansreceived().startsWith(SMBidEntry.EMPTY_DATE_STRING)){
			sDefaultValue = "0";
		}else{
			sDefaultValue = "1";		
		}   
    	
		s += "<TR>"	+ "<TD class=\" fieldlabel \">Plans received:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		sChecked = "";
		for (int i = 0; i < sValues.size(); i ++){
			if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			s += "<INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.ParamHasPlansReceivedDate + "\""
				+ " VALUE=" + sValues.get(i) + " " + sChecked 
				+ " id = \"" + SMBidEntry.ParamHasPlansReceivedDate + "\""
				+ " onchange=\"flagDirty();\""
				+ " >" + sDescriptions.get(i)
			;
		}
		s += "</TD></TR>";
		
		//Takeoff complete date:
        sValues.clear();
        sDescriptions.clear();
        sValues.add("0");
        sValues.add("1");
        sDescriptions.add("N/A&nbsp;&nbsp;");
        if (entry.getdattakeoffcomplete().replace("\"", "&quot;").compareToIgnoreCase(
        		SMBidEntry.EMPTY_DATE_STRING) == 0){
        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy");
        }else{
        	sDefaultDate = entry.getdattakeoffcomplete().replace("\"", "&quot;");
        }
        sDescriptions.add(Create_Edit_Form_Date_Input_Field(
				SMBidEntry.Paramdattakeoffcomplete, 
				sDefaultDate,
				getServletContext())
        );
        sDefaultValue = "";
        //If it HAS a date, then indicate that and display the date:
		if (entry.getdattakeoffcomplete().startsWith(SMBidEntry.EMPTY_DATE_STRING)){
			sDefaultValue = "0";
		}else{
			sDefaultValue = "1";		
		}   
    	
		s += "<TR>"	+ "<TD class=\" fieldlabel \">Takeoff complete:</TD>"
				+ "<TD class=\" fieldcontrol \" COLSPAN=" 
				+ Integer.toString(iNumberOfColumns -1) + ">";
			sChecked = "";
			for (int i = 0; i < sValues.size(); i ++){
				if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				s += "<INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.ParamHasTakeoffCompleteDate + "\""
					+ " VALUE=" + sValues.get(i) + " " + sChecked
					+ " id = \"" + SMBidEntry.ParamHasTakeoffCompleteDate + "\""
					+ " onchange=\"flagDirty();\""
					+ " >" + sDescriptions.get(i)
				;
			}
			
			//Salesperson:
		    ArrayList<String> sSalespersonValues = new ArrayList<String>();
		    ArrayList<String> sSalespersonDescriptions = new ArrayList<String>();

	    	//Select list:
		    try{
		        ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
		        		MySQLs.Get_Salesperson_List_SQL(),
		        		getServletContext(),
		        		sm.getsDBID(),
		        		"MySQL",
		        		this.toString() + " reading salespersons - user: " + sm.getUserID()
		        		+ " - "
		        		+ sm.getFullUserName()
		        		);
		        while (rsSalespersons.next()){
		        	sSalespersonValues.add((String) rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim());
		        	sSalespersonDescriptions.add(
		        			(String) (rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim() 
		        			+ " - " + rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName).trim()
		        			+ " " + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName).trim())
		        	);
		        }
		        rsSalespersons.close();
		    }catch(SQLException e){
		    	throw e;
		    }
		    
			s += "&nbsp;<B>Responsibility:</B>";
			String sFirstOption = "-- Select a takeoff person --";
			//If there are NO salespersons:
			if (sSalespersonValues.size() == 0){
				sFirstOption = "** (NO SALESPERSONS) **";
			}
			s += "<SELECT ID= \"" + SMBidEntry.Paramstakeoffpersoncode + "\""
				+ " onchange=\"flagDirty();\""
				+ " NAME=\"" + SMBidEntry.Paramstakeoffpersoncode + "\"" + ">";
			s += "<OPTION VALUE=\"" + "" + "\">" + sFirstOption + "</OPTION>";
			for (int i = 0; i < sSalespersonValues.size(); i++){
				s += "<OPTION";
				if (entry.getstakeoffpersoncode().compareToIgnoreCase(sSalespersonValues.get(i)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sSalespersonValues.get(i) + "\">" + sSalespersonDescriptions.get(i) + "</OPTION>";
			}
			s += "</SELECT>";
			s += "</TD></TR>";
			
			
			
			//Price complete date
	        sValues.clear();
	        sDescriptions.clear();
	        sValues.add("0");
	        sValues.add("1");
	        sDescriptions.add("N/A&nbsp;&nbsp;");
	        if (entry.getdatpricecomplete().replace("\"", "&quot;").compareToIgnoreCase(
	        		SMBidEntry.EMPTY_DATE_STRING) == 0){
	        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy");
	        }else{
	        	sDefaultDate = entry.getdatpricecomplete().replace("\"", "&quot;");
	        }
	        sDescriptions.add(Create_Edit_Form_Date_Input_Field(
					SMBidEntry.Paramdatpricecomplete, 
					sDefaultDate,
					getServletContext())
	        );
	        sDefaultValue = "";
	        //If it HAS a date, then indicate that and display the date:
			if (entry.getdatpricecomplete().startsWith(SMBidEntry.EMPTY_DATE_STRING)){
				sDefaultValue = "0";
			}else{
				sDefaultValue = "1";		
			}   
	    	
			s += "<TR>"	+ "<TD class=\" fieldlabel \">Pricing complete:</TD>"
				+ "<TD class=\" fieldcontrol \" COLSPAN=" 
				+ Integer.toString(iNumberOfColumns -1) + ">";
			sChecked = "";
			for (int i = 0; i < sValues.size(); i ++){
				if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				s += "<INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.ParamHasPriceCompleteDate + "\""
					+ " VALUE=" + sValues.get(i) + " " + sChecked
					+ " id = \"" + SMBidEntry.ParamHasPriceCompleteDate + "\""
					+ " onchange=\"flagDirty();\""
					+ " >" + sDescriptions.get(i)
				;
			}
			s += "&nbsp;<B>Responsibility:</B>";
			sFirstOption = "-- Select a pricing person --";
			//If there are NO salespersons:
			if (sSalespersonValues.size() == 0){
				sFirstOption = "** (NO SALESPERSONS) **";
			}
			s += "<SELECT ID= \"" + SMBidEntry.Paramspricingpersoncode + "\""
				+ " onchange=\"flagDirty();\""
				+ " NAME=\"" + SMBidEntry.Paramspricingpersoncode + "\"" + ">";
			s += "<OPTION VALUE=\"" + "" + "\">" + sFirstOption + "</OPTION>";
			for (int i = 0; i < sSalespersonValues.size(); i++){
				s += "<OPTION";
				if (entry.getspricingpersoncode().compareToIgnoreCase(sSalespersonValues.get(i)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sSalespersonValues.get(i) + "\">" + sSalespersonDescriptions.get(i) + "</OPTION>";
			}
			s += "</SELECT>";
			s += "</TD></TR>";
			
		//Close the table:
		s += "</TABLE style=\" title:ENDBidDatesTable; \">\n";
		return s;
	}
	private String createActualDatesTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:ActualDates; \">\n";
		
	    //Actual lead date:
        ArrayList<String>sValues = new ArrayList<String>(0);
        ArrayList<String>sDescriptions = new ArrayList<String>(0);
        String sDefaultDate = "";
        String sDefaultValue = "";
        sValues.add("0");
        sValues.add("1");
        sDescriptions.add("N/A&nbsp;&nbsp;");
        if (entry.getdattimeactualbiddate().replace("\"", "&quot;").compareToIgnoreCase(
        		EMPTY_DATETIME_STRING) == 0){
        	sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy hh:mm a");
        }else{
        	sDefaultDate = entry.getdattimeactualbiddate().replace("\"", "&quot;");
        }
        sDescriptions.add(Create_Edit_Form_DateTime_Input_Field(
				SMBidEntry.Paramdattimeactualbiddate, 
				sDefaultDate,
				getServletContext()
        		)
        );
        
        //If it HAS a date, then indicate that and display the date:
		if (entry.getdattimeactualbiddate().startsWith(SMBidEntry.EMPTY_DATE_STRING)){
			sDefaultValue = "0";
		}else{
			sDefaultValue = "1";		
		}   
    	
		s += "<TR>"	+ "<TD class=\" fieldlabel \">Actual bid date:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		String sChecked = "";
		for (int i = 0; i < sValues.size(); i ++){
			if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			s += "<INPUT TYPE=\"RADIO\" NAME=\"" + SMBidEntry.ParamHasActualBidDate + "\""
				+ " VALUE=" + sValues.get(i) + " " + sChecked
				+ " id = \"" + SMBidEntry.ParamHasActualBidDate + "\""
				+ " onchange=\"flagDirty();\""
				+ ">" + sDescriptions.get(i)
			;
		}
		s += "</TD></TR>";
		
		//Approximate amount	
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Proposed amt:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramdapproximateamount + "\""
			+ " VALUE=\"" + entry.getdapproximateamount().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramdapproximateamount + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + Integer.toString(SMTablebids.sshiptostateLength)
			+ ">"
		;
		s += "</TD></TR>";

		//Close the table:
		s += "</TABLE style=\" title:ENDActualdates; \">\n";
		return s;
	}
	private String createBidMemosTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry,
			boolean bUseGoogleDrivePicker) throws SQLException{
		String s = "";
		int iRows = 4;
		int iNumberOfColumns = 2;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:BidMemos; background-color: "
			+ BID_MEMOS_BG_COLOR + "; \" width=100% >\n";		
		
		//Description:
		s += "<TR>";
		s += "<TD class=\" fieldheading \" ><B>Description</B>&nbsp;</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" WIDTH=50%>"
			+ "<TEXTAREA NAME=\"" + SMBidEntry.Parammdescription + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style = \" width: 100%; \""
			+ " id = \"" + SMBidEntry.Parammdescription + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getmdescription().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Add a field for the 'Google Docs Link and Buttons:
		String sCreateAndUploadButton = "";
		if (SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMCreateGDriveSalesLeadFolders, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
				&& !sm.getAddingNewEntryFlag()){
			
				sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
			}
		
		s += "<TR><TD COLSPAN=" + iNumberOfColumns + "><U><B>Document folder link</B></U>" + sCreateAndUploadButton + "</TD></TR>";
		s += "<TR><TD COLSPAN=" + iNumberOfColumns + " class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMBidEntry.Paramsgdoclink + "\""
			+ " VALUE=\"" + entry.getsgdoclink().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMBidEntry.Paramsgdoclink + "\""
			+ " onchange=\"flagDirty();\""
			//+ " SIZE=" + "90"
			+ " style = \" width:100% \""
			+ " MAXLENGTH=" + "254"
			+ ">"
		;
		s += "</TD></TR>";
		
		//Display order that generated this follow up sales lead
		String sCreatedFromOrderNumberLink = "";
		String sCreatedFromOrderNumber = entry.getscreatedfromordernumber().trim();
		if(sCreatedFromOrderNumber.compareToIgnoreCase("") != 0){
			sCreatedFromOrderNumberLink = "<A href=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMDisplayOrderInformation"
						+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sCreatedFromOrderNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sCreatedFromOrderNumber + "</A>";
		}			
		s += "<TR>";
		s += "<TD ALIGN=LEFT COLSPAN=" + Integer.toString(iNumberOfColumns) + "><B><U>" + "Lead created from order number:</U></B>&nbsp;"  + sCreatedFromOrderNumberLink + " </TD>";
		s += "</TR>";
		
		//List any associated quotes:
        String sOrderNumberLink = "";
        boolean bAllowOrderViewing = SMSystemFunctions.isFunctionPermitted(
        		SMSystemFunctions.SMViewOrderInformation, 
        		sm.getUserID(), 
        		getServletContext(), 
        		sm.getsDBID(),
        		(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		s += "<TR>";
		s += "<TD ALIGN=LEFT COLSPAN=" + Integer.toString(iNumberOfColumns) + "><B><U>" + "Generated quotes/orders"  + " </U></B></TD>";
		s += "<TR>";
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.strimmedordernumber
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.squotedescription
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.lbidid + " = " + entry.slid() + ")"
				+ " AND (" + SMTableorderheaders.datOrderCanceledDate + " = '0000-00-00')"
			+ ")"
		;
        try {
			ResultSet rsOrderHeaders = clsDatabaseFunctions.openResultSet(SQL, 
					 getServletContext(), 
					 sm.getsDBID(),
					 "MySQL",
					 this.toString() + " reading quote desc from order header - user: " + sm.getUserID()
					 + " - "
					 + sm.getFullUserName()
					 );

			while (rsOrderHeaders.next()){
				String strimmedordernumber = rsOrderHeaders.getString(SMTableorderheaders.strimmedordernumber);
				if (bAllowOrderViewing){
					sOrderNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMDisplayOrderInformation"
						+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + strimmedordernumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + strimmedordernumber + "</A>"
					;
						
				}else{
					sOrderNumberLink = strimmedordernumber;
				}
				s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfColumns) + ">"
					+ "<span style = \" font-style: italic; font-size: small; \">"
					+ sOrderNumberLink
					+ "&nbsp;"
					+ rsOrderHeaders.getString(SMTableorderheaders.sBillToName)
					+ "/"
					+ rsOrderHeaders.getString(SMTableorderheaders.sShipToName)
					+ " - "
					+ rsOrderHeaders.getString(SMTableorderheaders.squotedescription)
					+ "</span>"
					+ "</TD></TR>"
				;
			}
			rsOrderHeaders.close();
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED>WARNING - could not read order headers to get quote description - " + e.getMessage() + ".</FONT>";
		}
        
		//List any associated Appointments:
        String sAppointmentLink = "";
        boolean bAllowAppointmentEditing = SMSystemFunctions.isFunctionPermitted(
        		SMSystemFunctions.SMEditAppointmentCalendar, 
        		sm.getUserID(), 
        		getServletContext(), 
        		sm.getsDBID(),
        		(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		s += "<TR>";
		s += "<TD ALIGN=LEFT COLSPAN=" + Integer.toString(iNumberOfColumns) + "><B><U>" + "Appointments"  + " </U></B></TD>";
		s += "<TR>";
		SQL = "SELECT"
			+ " " + SMTableappointments.lid
			+ ", " + SMTableappointments.datentrydate
			+ ", " + SMTableappointments.luserid
			+ ", " + SMTableappointments.mcomment
			+ " FROM " + SMTableappointments.TableName
			+ " WHERE ("
				+ "(" + SMTableappointments.ibidid + " = " + entry.slid() + ")"
			+ ")"
			+ " ORDER BY " +  SMTableappointments.datentrydate
		;
        try {
			ResultSet rsAppointments = clsDatabaseFunctions.openResultSet(SQL, 
					 getServletContext(), 
					 sm.getsDBID(),
					 "MySQL",
					 this.toString() + " reading appointments associated with sales lead - user: " 
					 + sm.getUserID()
					 + " - "
					 + sm.getFullUserName()
					 );

			while (rsAppointments.next()){
				String sAppointmentID = rsAppointments.getString(SMTableappointments.lid);
				if (bAllowAppointmentEditing){
					sAppointmentLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditAppointmentEdit"
						+ "?" + SMAppointment.Paramlid + "=" + sAppointmentID 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sAppointmentID + "</A>"
					;
						
				}else{
					sAppointmentLink = sAppointmentID;
				}
				s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfColumns) + ">"
					+ "<span style = \" font-style: italic; font-size: small; \">"
					+ sAppointmentLink
					+ "&nbsp;"
					+ clsDateAndTimeConversions.resultsetDateStringToString(rsAppointments.getString(SMTableappointments.datentrydate))
					+ "  "
					+ SMUtilities.getFullNamebyUserID(rsAppointments.getString(SMTableappointments.luserid), getServletContext(), sm.getsDBID(), this.toString())
					+ " - "
					+ rsAppointments.getString(SMTableappointments.mcomment)
					+ "</span>"
					+ "</TD></TR>"
				;
			}
			rsAppointments.close();
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED>WARNING - could not read appointments associated with sales lead - " + e.getMessage() + ".</FONT>";
		}
     
        //list critical dates
        if (entry.slid().compareToIgnoreCase("-1") != 0){
            s += "<TR><TD COLSPAN=" + Integer.toString(iNumberOfColumns) + ">"
                    + SMCriticalDateEntry.listCriticalDates(
    				SMTablecriticaldates.SALES_LEAD_RECORD_TYPE,
    				entry.getlid(),
    				"1100px",
    				"#F2C3FA",
    				getServletContext(), 
    				sm.getsDBID(), 
    				sm.getUserID(),
    				true
    				)
                    + "<BR>" 
    				+ SMCriticalDateEntry.addNewCriticalDateLink(Integer.toString(SMTablecriticaldates.SALES_LEAD_RECORD_TYPE)
                    		, entry.getlid(), 
                    		sm.getUserID(), 
                    		getServletContext(), 
                    		sm.getsDBID())
                 + "</TD></TR>";
        }

        
		s += "</TABLE style=\" title:ENDBidMemos; \">\n";
		return s;
	}
	private String createBidProductsTable(
			SMMasterEditEntry sm, 
			SMBidEntry entry) throws SQLException{
		String s = "";
		
		if (entry.getsProductTypeAmountsSize() > 0){
			//Create the table:
			s += "<TABLE class = \" innermost \" style=\" title:BidProducts; background-color: "
				+ BID_PRODUCTS_BG_COLOR + "; \" width=100% >\n";		
			s += "<TR><TD>";
			s += "<B><U>Product Type Amounts</U></B>";
			s += "</TD><TD>&nbsp;</TD></TR>";
			for (int i = 0; i < entry.getsProductTypeAmountsSize(); i++){
				if (bDebugMode){
					System.out.println("[1579268856] In " + this.toString() 
						+ " reading producttypesarray - string = " + entry.getsProductType(i).trim());
					System.out.println("insmeditbidentry - producttypeID = " + entry.getsProductTypeID(i));
				}
				s += "<TR><TD class=\" fieldlabel \">" + entry.getsProductType(i) + ":</TD>"
					+ "<TD class=\" fieldcontrol \" >";
				
				s += "<INPUT TYPE=TEXT NAME=\"" 
					+ SMBidEntry.ParamsBidProductAmount + entry.getsProductTypeID(i) + entry.getsProductType(i) + "\""
					+ " VALUE=\"" + entry.getsProductTypeAmount(i).trim() + "\""
					+ " SIZE=40"
					+ " MAXLENGTH=13" 
				;
				s += "</TD></TR>";
			}
	    }
		//Close the table:
		s += "</TABLE style=\" title:ENDBidProducts; \">\n";
		return s;
	}
	private String createCommandsTable(SMMasterEditEntry smedit){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:ButtonCommands; background-color: "
			+ BUTTON_COMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";

		s += createUpdateButton();
		s += createAddNewButton();
		s += createCloneButton();
		s += createUpdateAnotherButton();
		s +="&nbsp;ID#:&nbsp;<INPUT TYPE=TEXT NAME=\"" + UPDATE_ANOTHER_FIELD_NAME + "\" SIZE=9 MAXLENGTH=13" 
			+ " STYLE=\"width: .75in; \">&nbsp;";
		if (SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMCreateQuotes, 
			smedit.getUserID(), 
			getServletContext(), 
			smedit.getsDBID(),
			(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += createCreateQuoteButton();
		}
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditOrders, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += createCreateOrderButton();
		}
		//If there was a search for the customer, enter the result here:
		String sCustomerNumber = "";
	    if (smedit.getRequest().getParameter(CUSTOMER_SEARCH) != null){
	    	sCustomerNumber = (smedit.getRequest().getParameter(SMOrderHeader.ParamsCustomerCode));
	    }
		s +="for customer:&nbsp;<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsCustomerCode + "\""
			+ " VALUE = \"" + sCustomerNumber + "\""
			+ " MAXLENGTH=" 
			+ Integer.toString(SMTableorderheaders.sCustomerCodeLength) 
			+ " STYLE=\"width: .9in; \">&nbsp;";
		s += createFindCustomerButton();
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditAppointmentCalendar, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += "&nbsp;" + createAppointmentButton();
		}

		s += "</TD></TR>";
		s += "</TABLE style=\" title:ENDButtonCommands; \">\n";
		return s;
	}
	private String createUpdateAnotherButton(){
		return "<button type=\"button\""
			+ " value=\"" + UPDATE_ANOTHER_BUTTON_LABEL + "\""
			+ " name=\"" + UPDATE_ANOTHER_BUTTON_LABEL + "\""
			+ " onClick=\"updateanotherlead();\">"
			+ UPDATE_ANOTHER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createUpdateButton(){
		return "<button type=\"button\""
			+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " onClick=\"update(this);\">"
			+ UPDATE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createCloneButton(){
		return "<button type=\"button\""
			+ " value=\"" + CLONE_BUTTON_LABEL + "\""
			+ " name=\"" + CLONE_BUTTON_LABEL + "\""
			+ " onClick=\"clone();\">"
			+ CLONE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createCreateQuoteButton(){
		return "<button type=\"button\""
			+ " value=\"" + CREATE_QUOTE_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_QUOTE_BUTTON_LABEL + "\""
			+ " onClick=\"createquote();\">"
			+ CREATE_QUOTE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createCreateOrderButton(){
		return "<button type=\"button\""
			+ " value=\"" + CREATE_ORDER_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_ORDER_BUTTON_LABEL + "\""
			+ " onClick=\"createorder();\">"
			+ CREATE_ORDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createFindCustomerButton(){
		return "<button type=\"button\""
			+ " value=\"" + FIND_CUSTOMER_BUTTON_LABEL + "\""
			+ " name=\"" + FIND_CUSTOMER_BUTTON_LABEL + "\""
			+ " onClick=\"findcustomer();\">"
			+ FIND_CUSTOMER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createAddNewButton(){
		return "<button type=\"button\""
			+ " value=\"" + ADD_NEW_BUTTON_LABEL + "\""
			+ " name=\"" + ADD_NEW_BUTTON_LABEL + "\""
			+ " onClick=\"addnew();\">"
			+ ADD_NEW_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	private String createTestMapLinkButton(){
		return "<button type=\"button\""
			+ " value=\"" + TEST_MAP_LINK_BUTTON_LABEL + "\""
			+ " name=\"" + TEST_MAP_LINK_BUTTON_LABEL + "\""
			+ " onClick=\"testmaplink();\">"
			+ TEST_MAP_LINK_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createAndUploadFolderButton(boolean bUseGoogleDrivePicker){
		String sOnClickFunction = "createanduploadfolder()";
		if(bUseGoogleDrivePicker) {
			sOnClickFunction = "loadPicker()";
		}
		
		return "<button type=\"button\""
			+ " value=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " onClick=\""+ sOnClickFunction +"\">"
			+ CREATE_UPLOAD_FOLDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createAppointmentButton(){
		return "<button type=\"button\""
			+ " value=\"" + CREATE_APPOINTMENT_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_APPOINTMENT_BUTTON_LABEL + "\""
			+ " onClick=\"createappointment();\">"
			+ CREATE_APPOINTMENT_BUTTON_LABEL
			+ "</button>\n";
	}
	
	//This function creates a field for editing a date field on a form with a date picker:
	private String Create_Edit_Form_Date_Input_Field (
			String sFieldName,
			String sValue,
			ServletContext context
	){

		String s = "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
		if (sValue != null){
			s += " VALUE=\"" + sValue + "\"";
		}
		else{
			s += " VALUE=\"\"";
		}
		s +=  "ID=\"" + sFieldName + "\"";
		s += "SIZE=8";
		s += " MAXLENGTH=10";
		s += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		s += SMUtilities.getDatePickerString(sFieldName, context);
		return s;
	}
	//This function creates a field in a table for editing a date AND time field on a form with a date picker:
	private String Create_Edit_Form_DateTime_Input_Field (
			String sDateFieldName,
			String sValue,
			ServletContext context
	){

		//The value of sValue should look something like this: "12/1/2010 03:59 PM"
		String sDatePortion = sValue.substring(0, sValue.indexOf(" ")).trim();
		String sTimePortion = sValue.substring(sValue.indexOf(" "), sValue.length()).trim();

		//System.out.println("In Create_Edit_Form_DateTime_Input_Field - sTimePortion = " + sTimePortion);

		String s = "<INPUT TYPE=TEXT NAME=\"" + sDateFieldName + "\"";
		s += " VALUE=\"" + sDatePortion + "\"";
		s +=  "ID=\"" + sDateFieldName + "\"";
		s += "SIZE=8";
		s += " MAXLENGTH=10";
		s += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		s += SMUtilities.getDatePickerString(sDateFieldName, context) + "&nbsp;";

		int iMinute = Integer.parseInt(
				sTimePortion.substring(sTimePortion.indexOf(":") + 1, sTimePortion.indexOf(":") + 3));
		int iAMPM = 0;
		if (clsStringFunctions.StringRight(sTimePortion, 2).compareToIgnoreCase("AM") == 0){
			iAMPM = 0;
		}else{
			iAMPM = 1;
		}
		String sHour = sTimePortion.substring(0, sTimePortion.indexOf(":"));
		int iHour = Integer.parseInt(sHour);
		//if (iHour == 0 && iAMPM == 1){
		if (iHour == 0){
			iHour = 12;
		}
		s += "<B>Time:</B>&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedHour\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		for (int i=1; i<=12;i++){
			if (i == iHour){
				s += "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s += "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s += "</SELECT>";
		s += "<B>:</B>&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedMinute\"";
		s += " onchange=\"flagDirty();\"";		
		s += ">";
		for (int i=0; i<=59;i++){
			String sMinute = clsStringFunctions.PadLeft(Integer.toString(i), "0", 2);
			if (i == iMinute){
				s += "<OPTION SELECTED VALUE = " 
					+ sMinute + ">" + sMinute;
			}else{
				s += "<OPTION VALUE = " + sMinute + ">" + sMinute;
			}
		}
		
		s += "</SELECT>";	
		s += "&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedAMPM\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		for (int i=Calendar.AM; i<=Calendar.PM;i++){
			if (i == iAMPM){
				if (i == Calendar.AM){
					s+= "<OPTION SELECTED VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION SELECTED VALUE = " + Calendar.PM + ">" + "PM";
				}		
			}else{
				if (i == Calendar.AM){
					s += "<OPTION VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION VALUE = " + Calendar.PM + ">" + "PM";
				}
			}
		}
		s += "</SELECT>";

		return s;
	}
	private String sCommandScripts(SMMasterEditEntry sm, SMBidEntry entry){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		s += "<script type=\"text/javascript\">\n";
		
		
		s += "window.onbeforeunload = prompttosave;\n";
		
		s += "function prompttosave(){\n"
			
			//First check to see if the date fields were changed, and if so, flag the record was changed field:
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddatoriginationdate + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdatoriginationdate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"			
			
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddattimebiddate + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdattimebiddate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"			
			
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddatplansreceived + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdatplansreceived + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"	
			
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddattakeoffcomplete + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdattakeoffcomplete + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"
			
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddatpricecomplete + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdatpricecomplete + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"
			
			+ "    if (document.getElementById(\"" + SMBidEntry.Paramlastsaveddattimeactualbiddate + "\").value != " 
				+ "document.getElementById(\"" + SMBidEntry.Paramdattimeactualbiddate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"


			//Don't prompt on updates, sales contact finds, or customer finds:
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + UPDATE_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + FIND_CUSTOMER_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + FIND_SALES_CONTACT_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "       return 'You have unsaved changes - are you sure you want to leave this page?';\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n"
		;

		//Update another lead:
		s += "function updateanotherlead(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + UPDATE_ANOTHER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Clone:
		s += "function clone(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ CLONE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;


		
		//Find customer
		s += "function findcustomer(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + FIND_CUSTOMER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Add new:
		s += "function addnew(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ ADD_NEW_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Create order:
		s += "function createorder(){\n"
			//+ "    alert('This function is not completed yet.');\n"
			//+ "    return;\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ CREATE_ORDER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Create quote:
		s += "function createquote(){\n"
			//+ "    alert('This function is not completed yet.');\n"
			//+ "    return;\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ CREATE_QUOTE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Create Appointment
		s += "function createappointment(){\n"
				+ "prompttosave();\n"
				+ "window.open(\""
					+ SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit?" 
					+ SMAppointment.Paramlid + "=-1"
					//TODO Get the user name from the salesperson code.
					//+ "&" + SMAppointment.Paramsuser + "=" + SMUtilities.encodeURL(sDefautUser)
					+ "&" + SMAppointment.Paramsemail + "=" + clsServletUtilities.URLEncode(entry.getemailaddress())
	    			+ "&" + SMAppointment.Paramsphone + "=" + clsServletUtilities.URLEncode(entry.getsphonenumber())
	    			+ "&" + SMAppointment.Paramsbilltoname + "=" + clsServletUtilities.URLEncode(entry.getscustomername())
	    			+ "&" + SMAppointment.Paramsshiptoname + "=" + clsServletUtilities.URLEncode(entry.getsprojectname())
	    			+ "&" + SMAppointment.Paramscontactname + "=" + clsServletUtilities.URLEncode(entry.getscontactname())
	    			+ "&" + SMAppointment.Paramsaddress1 + "=" + clsServletUtilities.URLEncode(entry.getsshiptoaddress1())
	    			+ "&" + SMAppointment.Paramsaddress2 + "=" + clsServletUtilities.URLEncode(entry.getsshiptoaddress2())
	    			+ "&" + SMAppointment.Paramsaddress3 + "=" + clsServletUtilities.URLEncode(entry.getsshiptoaddress3())
	    			+ "&" + SMAppointment.Paramsaddress4 + "=" + clsServletUtilities.URLEncode(entry.getsshiptoaddress4())
	    			+ "&" + SMAppointment.Paramscity + "=" + clsServletUtilities.URLEncode(entry.getsshiptocity())
	    			+ "&" + SMAppointment.Paramsstate + "=" + clsServletUtilities.URLEncode(entry.getsshiptostate())
	    			+ "&" + SMAppointment.Paramszip + "=" + clsServletUtilities.URLEncode(entry.getsshiptozip())
	    			+ "&" + SMAppointment.Paramibidid + "=" + clsServletUtilities.URLEncode(entry.getlid())
	    			+ "&" + SMAppointment.Paramdatentrydate + "=" + SMUtilities.EMPTY_DATE_VALUE
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
	    			
				+ "\");\n"
				+ "}\n"
			;
		
		//Create critical date
		s += "function addcriticaldate(){\n"
				+ "prompttosave();\n"
				+ "window.open(\""
					+ SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCriticalDateEdit?" 
					+ SMCriticalDateEntry.ParamID + "=-1"
					+ "&" + SMCriticalDateEntry.ParamDocNumber + "=" + clsServletUtilities.URLEncode(entry.getlid())
	    			+ "&" + SMCriticalDateEntry.ParamiType + "=" + clsServletUtilities.URLEncode(Integer.toString(SMTablecriticaldates.SALES_LEAD_RECORD_TYPE))
	    			+ "&" + SMCriticalDateEntry.ParamAssignedbyUserID + "=" + clsServletUtilities.URLEncode(sm.getUserID())
	    			+ "&" + SMCriticalDateEntry.ParamResponsibleUserID + "=" + clsServletUtilities.URLEncode(sm.getUserID())
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
				+ "\");\n"
				+ "}\n"
			;

		//Create folder and/or upload file
		s += "function createanduploadfolder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Test map link:
		s += "function testmaplink(){\n"
			+ "    var sLinkAddress = '';\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptoaddress1 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptoaddress2 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptoaddress3 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptoaddress4 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptocity + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMBidEntry.Paramsshiptostate + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
				
			+ "    sLinkAddress = 'https://maps.google.com/maps?hl=en&geocode=&q=' + escape(sLinkAddress);\n"
			+ "    window.open(sLinkAddress, 'newWindow');\n"
			+ "}\n"
		;
		
		//Update:
		s += "function update(param){\n"
				+ "param.disabled=true;\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ UPDATE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Find sales contact:
		s += "function findsalescontact(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ FIND_SALES_CONTACT_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+a\",function() {\n";
		s += "        updateanotherlead();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+c\",function() {\n";
		s += "        clone();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        findcustomer();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        createanduploadfolder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+n\",function() {\n";
		s += "        addnew();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+o\",function() {\n";
		s += "        createorder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+q\",function() {\n";
		s += "        createquote();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        createnewfolder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        findsalescontact();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+t\",function() {\n";
		s += "        testmaplink();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+u\",function() {\n";
		s += "        update();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        uploadfile();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "}\n";
		s += "\n";
		
		s += "window.onload = function(){\n"
			+ "    initShortcuts();\n"
			+ "}\n\n"
		;
		

		
		s += "</script>\n";
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//TJR - 5/13/2011 - I left all these comments in to use as samples here or elsewhere:
		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";
		
		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";
		
		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";
		
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

		s += "</style>"
			+ "\n"
			;

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
