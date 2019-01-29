package SMClasses;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smcontrolpanel.SMEditDeliveryTicketEdit;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;



public class SMDeliveryTicket extends clsMasterEntry{

	public static final String ITEMS_TABLE_BG_COLOR = "#FFBCA2"; //
	public static final String DETAIL_TABLE_BG_COLOR = "#F2C3FA";
	public static final String ORDERCOMMANDS_TABLE_BG_COLOR = "#99CCFF"; //
	public static final String ORDERNUMBER_TABLE_BG_COLOR = "#CCFFB2"; //
	public static final String TERMS_TABLE_BG_COLOR = "#CCFFB2"; //
	public static final String SIGNATUREBLOCK_TABLE_BG_COLOR = "#FFBCA2"; //
	public static final String COMMENTS_TABLE_BG_COLOR = "#F2C3FA"; //
	public static final String MAIN_FORM_NAME = "MAINFORM";
	public static final String NUMBER_OF_ITEM_LINES_USED = "NUMOFITEMLINESUSED";
	public static final String ITEM_LINE_QTY = "ITEMLINEQTY";
	public static final String ITEM_LINE_NUMBER = "ITEMLINENUMBER";
	public static final String ITEM_LINE_DESC = "ITEMLINEDESC";
	public static final String FORM_NAME = "MAINFORM";
	
	public static final String ParamObjectName = "Delivery Ticket";
	public static final String Paramlid= "lid";
	public static final String Paramdatinitiated= "datinitiated";
	public static final String Paramlinitiatedbyid= "linitiatedbyid";
	public static final String Paramsinitiatedbyfullname= "sinitiatedbyfullname";
	public static final String Paramstrimmedordernumber= "strimmedordernumber";
	public static final String Paramsdetaillines = "sdetaillines";
	public static final String Parammcomments = "mcomments";
	public static final String Paramdatsigneddate = "datsigneddate";
	public static final String Paramssignedbyname = "ssignedbyname";
	public static final String Parammsignature = "msignature";
	public static final String Paramsshiptocontact = "sshiptocontact";
	
	public static final String Paramstermscode = "stermscode";
	public static final String Parammterms = "mterms";
	public static final String Paramsdeliveredby = "sdeliveredby";
	public static final String Paramlsignatureboxwidth = "lsignatureboxwidth";
	
	
	
	private String m_slid;
	private String m_datinitiated;
	private String m_linitiatedbyid;
	private String m_sinitiatedbyfullname;
	private String m_sdetaillines;
	private String m_mcomments;
	private String m_strimmedordernumber;
	private String m_ssignedbyname;
	private String m_datsigneddate;
	private String m_msignature;
	private String m_mdbaaddress;
	private String m_mdbaremittoaddress;
	private String m_sdbadeliveryticketreceiptlogo;
	private String m_sbilltoname;
	private String m_sbilltoadd1;
	private String m_sbilltoadd2;
	private String m_sbilltoadd3;
	private String m_sbilltocity;
	private String m_sbilltostate;
	private String m_sbilltozip;
	private String m_sbilltocontact;
	private String m_sbilltophone;
	private String m_sponumber;
	private String m_sshiptoname;
	private String m_sshiptoadd1;
	private String m_sshiptoadd2;
	private String m_sshiptoadd3;
	private String m_sshiptocity;
	private String m_sshiptostate;
	private String m_sshiptozip;
	private String m_sshiptocountry;
	private String m_sshiptocontact;
	private String m_sshiptophone;
	private String m_sshiptofax;
	private String m_smechanicname;
	private String m_iworkorderid;
	private String m_mterms;
	private String m_stermscode;
	private String m_iposted;
	private String m_sdeliveredby;
	private String m_lsignatureboxwidth;
	private String m_sNewRecord;
	private boolean bDebugMode = false;
	
    public SMDeliveryTicket() {
		super();
		initEntryVariables();
        }
    
    public SMDeliveryTicket(HttpServletRequest req){
		super(req);
		initEntryVariables();
		
		m_slid = clsManageRequestParameters.get_Request_Parameter(
			SMTabledeliverytickets.lid, req).trim();

		m_datinitiated = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.datinitiated, req).trim().replace("&quot;", "\"");
		   if(m_datinitiated.compareToIgnoreCase("") == 0){
			m_datinitiated = EMPTY_DATETIME_STRING;
		   }
		m_linitiatedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.linitiatedbyid, req).trim().replace("&quot;", "\"");

		m_sinitiatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sinitiatedbyfullname, req).trim().replace("&quot;", "\"");

		m_sdetaillines = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sdetaillines, req).trim().replace("&quot;", "\"");

		m_mcomments = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.mcomments, req).trim().replace("&quot;", "\"");

		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(
			SMTabledeliverytickets.strimmedordernumber, req).trim().replace("&quot;", "\"");

		setssignedbyname(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.ssignedbyname, req).trim().replace("&quot;", "\""));

		m_datsigneddate = clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.datsigneddate, req).trim().replace("&quot;", "\"");
	    if(m_datsigneddate.compareToIgnoreCase("") == 0){
	    	m_datsigneddate = EMPTY_DATE_STRING;
	    }
		setmsignature(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.msignature, req).trim().replace("&quot;", "\""));

		setmdbaaddress(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.mdbaaddress, req).trim().replace("&quot;", "\""));
		
		setmdbaremittoaddress(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.mdbaremittoaddress, req).trim().replace("&quot;", "\""));
		
		setsbilltoname(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltoname, req).trim().replace("&quot;", "\""));

		setsbilltoadd1(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltoadd1, req).trim().replace("&quot;", "\""));

		setsbilltoadd2(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltoadd2, req).trim().replace("&quot;", "\""));

		setsbilltoadd3(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltoadd3, req).trim().replace("&quot;", "\""));
		
		setsbilltocity(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltocity, req).trim().replace("&quot;", "\""));
		
		setsbilltostate(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltostate, req).trim().replace("&quot;", "\""));
		
		setsbilltozip(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltozip, req).trim().replace("&quot;", "\""));

		setsbilltocontact(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltocontact, req).trim().replace("&quot;", "\""));

		setsbilltophone(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sbilltophone, req).trim().replace("&quot;", "\""));

		setsponumber(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sponumber, req).trim().replace("&quot;", "\""));

		setsshiptoname(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptoname, req).trim().replace("&quot;", "\""));

		setsshiptoadd1(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptoadd1, req).trim().replace("&quot;", "\""));

		setsshiptoadd2(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptoadd2, req).trim().replace("&quot;", "\""));

		setsshiptoadd3(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptoadd3, req).trim().replace("&quot;", "\""));
		
		setsshiptocity(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptocity, req).trim().replace("&quot;", "\""));
		
		setsshiptostate(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptostate, req).trim().replace("&quot;", "\""));
		
		setsshiptozip(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptozip, req).trim().replace("&quot;", "\""));

		setsshiptocountry(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptocountry, req).trim().replace("&quot;", "\""));

		setsshiptocontact(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptocontact, req).trim().replace("&quot;", "\""));

		setsshiptophone(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptophone, req).trim().replace("&quot;", "\""));

		setsshiptofax(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sshiptofax, req).trim().replace("&quot;", "\""));

		setsmechanicname(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.smechanicname, req).trim().replace("&quot;", "\""));
		
		setiworkorderid(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.iworkorderid, req).trim().replace("&quot;", "\""));
		
		setmterms(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.mterms, req).trim().replace("&quot;", "\""));
		
		setstermscode(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.stermscode, req).trim().replace("&quot;", "\""));
		
		setiposted(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.iposted, req).trim().replace("&quot;", "\""));
		
		setsdeliveredby(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.sdeliveredby, req).trim().replace("&quot;", "\""));
		
		setlsignatureboxwidth(clsManageRequestParameters.get_Request_Parameter(
				SMTabledeliverytickets.lsignaturboxwidth, req).trim().replace("&quot;", "\""));

		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMEditDeliveryTicketEdit.SAVE_COMMAND_VALUE, req).trim().replace("&quot;", "\"");
		
		//Load the delivery ticket order detail lines from the request:
		//Get the number of item lines used:
		int iNumberOfItemLines = 0;
		String sPaddedLineNumber = clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicket.NUMBER_OF_ITEM_LINES_USED, req);
		if (sPaddedLineNumber.compareToIgnoreCase("") == 0){
			sPaddedLineNumber = "0";
		}
		try {
			iNumberOfItemLines = Integer.parseInt(sPaddedLineNumber);
		} catch (Exception e) {
			System.out.println("Error [1391791196] parsing number of item lines used - value '" 
				+ clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED, req) 
				+ "' is not valid -  " + e.getMessage());
		}
		for (int i = 1; i <= iNumberOfItemLines; i++){
			if ((req.getParameter(SMDeliveryTicket.ITEM_LINE_QTY + Integer.toString(i)) != null) && 
				(!req.getParameter(SMDeliveryTicket.ITEM_LINE_QTY + Integer.toString(i)).isEmpty())){
				String sFullDetailLine = req.getParameter(SMDeliveryTicket.ITEM_LINE_QTY + Integer.toString(i)) + "  "
						+ req.getParameter(SMDeliveryTicket.ITEM_LINE_NUMBER + Integer.toString(i)) + "  " ;
				try{
				sFullDetailLine +=  URLDecoder.decode(req.getParameter(SMDeliveryTicket.ITEM_LINE_DESC + Integer.toString(i)), "UTF-8");
				}catch (Exception e){
					System.out.println("Error [1451577384] decoding item line description - value '" +
					clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicket.ITEM_LINE_DESC + Integer.toString(i), req) 
					+ "' is not valid - " + e.getMessage());
				}
				if (getsdetaillines().compareToIgnoreCase("") == 0){
					setsdetaillines(getsdetaillines() + sFullDetailLine);
				}else{
					setsdetaillines(getsdetaillines() + "\n" + sFullDetailLine);
				}
			}
		}
				
    }

	public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName 
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067680]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067681]");
    }
    public void load (Connection conn) throws Exception{
    	try {
			load (m_slid, conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    }
    private void load (String sID, Connection conn) throws Exception{
    	
    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTabledeliverytickets.TableName
			+ " WHERE ("
				+ SMTabledeliverytickets.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTabledeliverytickets.lid));
				m_datinitiated = clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rs.getString(SMTabledeliverytickets.datinitiated));
				m_linitiatedbyid = Long.toString(rs.getLong(SMTabledeliverytickets.linitiatedbyid));
				m_sinitiatedbyfullname = rs.getString(SMTabledeliverytickets.sinitiatedbyfullname).trim();
				m_sdetaillines = rs.getString(SMTabledeliverytickets.sdetaillines).trim();
				m_mcomments = rs.getString(SMTabledeliverytickets.mcomments).trim();
				m_strimmedordernumber = rs.getString(SMTabledeliverytickets.strimmedordernumber).trim();
				m_ssignedbyname = rs.getString(SMTabledeliverytickets.ssignedbyname).trim();
				m_datsigneddate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTabledeliverytickets.datsigneddate));
				m_msignature = rs.getString(SMTabledeliverytickets.msignature).trim();
				m_mdbaaddress = rs.getString(SMTabledeliverytickets.mdbaaddress).trim();
				m_mdbaremittoaddress = rs.getString(SMTabledeliverytickets.mdbaremittoaddress).trim();
				m_sdbadeliveryticketreceiptlogo = rs.getString(SMTabledeliverytickets.sdbadeliveryticketreceiptlogo).trim();
				m_sbilltoname = rs.getString(SMTabledeliverytickets.sbilltoname).trim();
				m_sbilltoadd1 = rs.getString(SMTabledeliverytickets.sbilltoadd1).trim();
				m_sbilltoadd2 = rs.getString(SMTabledeliverytickets.sbilltoadd2).trim();
				m_sbilltoadd3 = rs.getString(SMTabledeliverytickets.sbilltoadd3).trim();
				m_sbilltocity = rs.getString(SMTabledeliverytickets.sbilltocity).trim();
				m_sbilltostate = rs.getString(SMTabledeliverytickets.sbilltostate).trim();
				m_sbilltozip = rs.getString(SMTabledeliverytickets.sbilltozip).trim();
				m_sbilltocontact = rs.getString(SMTabledeliverytickets.sbilltocontact).trim();
				m_sbilltophone = rs.getString(SMTabledeliverytickets.sbilltophone).trim();
				m_sponumber = rs.getString(SMTabledeliverytickets.sponumber).trim();
				m_sshiptoname = rs.getString(SMTabledeliverytickets.sshiptoname).trim();
				m_sshiptoadd1 = rs.getString(SMTabledeliverytickets.sshiptoadd1).trim();
				m_sshiptoadd2 = rs.getString(SMTabledeliverytickets.sshiptoadd2).trim();
				m_sshiptoadd3 = rs.getString(SMTabledeliverytickets.sshiptoadd3).trim();
				m_sshiptocity = rs.getString(SMTabledeliverytickets.sshiptocity).trim();
				m_sshiptostate = rs.getString(SMTabledeliverytickets.sshiptostate).trim();
				m_sshiptozip = rs.getString(SMTabledeliverytickets.sshiptozip).trim();
				m_sshiptocountry = rs.getString(SMTabledeliverytickets.sshiptocountry).trim();
				m_sshiptocontact = rs.getString(SMTabledeliverytickets.sshiptocontact).trim();
				m_sshiptophone = rs.getString(SMTabledeliverytickets.sshiptophone).trim();
				m_sshiptofax = rs.getString(SMTabledeliverytickets.sshiptofax).trim();
				m_smechanicname = rs.getString(SMTabledeliverytickets.smechanicname).trim();
				m_iworkorderid = Long.toString(rs.getInt(SMTabledeliverytickets.iworkorderid));
				m_stermscode = rs.getString(SMTabledeliverytickets.stermscode).trim();
				m_mterms = rs.getString(SMTabledeliverytickets.mterms).trim();
				m_iposted = Long.toString(rs.getInt(SMTabledeliverytickets.iposted));
				m_sdeliveredby = rs.getString(SMTabledeliverytickets.sdeliveredby).trim();
				m_lsignatureboxwidth = Integer.toString(rs.getInt(SMTabledeliverytickets.lsignaturboxwidth));
				rs.close();
			} else {
				rs.close();
				throw new Exception("Error [1445970936] Could not load delivery ticket with ID '" + sID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1445970937] reading " + ParamObjectName + " for lid : '" + sID
				+ "' - " + e.getMessage());
		}
    }
    
    public void saveFromAcceptanceScreen(
			ServletContext context, 
			String sDBIB, 
			String sUser
			) throws Exception{
			
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBIB, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".save [1445266321] - user: " + sUser);
			} catch (Exception e) {
				throw new Exception("Error [1445266319] getting connection - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.start_data_transaction(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067684]");
				throw new Exception ("Error [1445266320] - could not start data transaction.");
			}
			try {
				save_acceptance_screen_without_data_transaction(conn, sUser, context);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067685]");
				throw new Exception("Error saving delivery ticket - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067686]");
				throw new Exception("Error [1445266322] committing transaction.");
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067687]");
			return;
		}
    
    private void save_acceptance_screen_without_data_transaction (
			Connection conn, 
			String sUser,
			ServletContext context) throws Exception{

    	try{
    		validate_acceptance_screen_fields(conn);
    	}catch (Exception ex){
    		throw new Exception("Error validating " + SMDeliveryTicket.ParamObjectName + " - " + ex.getMessage());
    	}

		String SQL = " UPDATE " + SMTabledeliverytickets.TableName + " SET "
			 + SMTabledeliverytickets.datsigneddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getsdatsigned()) + "'"
			+ ", " + SMTabledeliverytickets.msignature + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmsignature()) + "'"
			+ ", " + SMTabledeliverytickets.ssignedbyname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssignedbyname()) + "'"
			+ ", " + SMTabledeliverytickets.mterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmterms()) + "'"
			+ ", " + SMTabledeliverytickets.lsignaturboxwidth + " = " + clsDatabaseFunctions.FormatSQLStatement(getlsignatureboxwidth())
			+ " WHERE (" 
				+ SMTabledeliverytickets.lid + " = " + getslid() 
			+ ")";

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1445266518] updating delivery ticket with SQL: " + SQL + " - " + e.getMessage() + ".");
			}
    	return;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1410874297] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067682]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067683]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{


    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	
    	//Since the order number has been validated; copy the order information to the delivery ticket
	    SMOrderHeader ord = new SMOrderHeader();
	    ord.setM_strimmedordernumber(m_strimmedordernumber);
	    if(!ord.load(conn)){
	    	throw new Exception("Error [1445971216] loading order number '" + m_strimmedordernumber + "' - " + ord.getErrorMessages());
	    }
	     
		m_sbilltoname = ord.getM_sBillToName();
		m_sbilltoadd1 = ord.getM_sBillToAddressLine1();
	    m_sbilltoadd2 = ord.getM_sBillToAddressLine2();
	    m_sbilltoadd3 = ord.getM_sBillToAddressLine3(); 
	    m_sbilltocity =	ord.getM_sBillToCity();  
	    m_sbilltostate = ord.getM_sBillToState();
	    m_sbilltozip = ord.getM_sBillToZip();
		m_sbilltocontact = ord.getM_sBilltoContact();
		m_sbilltophone = ord.getM_sBilltoPhone();
		m_sponumber = ord.getM_sPONumber();
	    m_sshiptoname = ord.getM_sShipToName();
		m_sshiptoadd1 = ord.getM_sShipToAddress1();
	    m_sshiptoadd2 = ord.getM_sShipToAddress2();
		m_sshiptoadd3 = ord.getM_sShipToAddress3(); 
		m_sshiptocity =  ord.getM_sShipToCity();
		m_sshiptostate = ord.getM_sShipToState();
		m_sshiptozip = ord.getM_sShipToZip();
		m_sshiptocountry = ord.getM_sShipToCountry();
	    m_sshiptocontact = ord.getM_sShiptoContact();
	    m_sshiptophone = ord.getM_sShiptoPhone();
		m_sshiptofax = ord.getM_sShipToFax();
		
		//Get Company Information
	    String SQL = "SELECT *"
	    	+ " FROM " + SMTabledoingbusinessasaddresses.TableName
	    	+ " WHERE ("
	    	+ "(" + SMTabledoingbusinessasaddresses.lid + " = '" + ord.getM_idoingbusinessasaddressid() + "')"
	    	+ ")"
	    ;
	    try {
			ResultSet rsDBA = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsDBA.next()){
				m_mdbaaddress = rsDBA.getString(SMTabledoingbusinessasaddresses.mAddress).trim();
				m_mdbaremittoaddress = rsDBA.getString(SMTabledoingbusinessasaddresses.mRemitToAddress).trim();
				m_sdbadeliveryticketreceiptlogo = rsDBA.getString(SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo).trim();
			}
			rsDBA.close();
		} catch (Exception e) {
			throw new Exception("Error [1446488011] reading doingbusinessasaddresses table for delivery ticket - " + e.getMessage());
		}
    	
    	//If it's a new record, then we need to get the user's info:
    	SQL = "";
    	long lid;
		try {
			lid = Long.parseLong(getslid());
		} catch (Exception e1) {
			throw new Exception("Error [1410874333] parsing " + ParamObjectName + " lid '" + this.getslid() + "' - " + e1.getMessage());
		}
    	if (lid < 1){
    		//It's a new record
    		setsNewRecord("1");
    	}

    	if (getsNewRecord().compareToIgnoreCase("1") == 0){
    		setlinitiatedbyid(sUserID);
    		setsinitiatedbyfullname(sUserFullName);
    	}

		//If it's a new record, do an insert:
    	if (getsNewRecord().compareToIgnoreCase("1") == 0){
    		SQL = "INSERT INTO " + SMTabledeliverytickets.TableName + " ("
				+ SMTabledeliverytickets.datinitiated
				+ ", " + SMTabledeliverytickets.linitiatedbyid
				+ ", " + SMTabledeliverytickets.sinitiatedbyfullname
				+ ", " + SMTabledeliverytickets.sdetaillines
				+ ", " + SMTabledeliverytickets.mcomments //5
				
				+ ", " + SMTabledeliverytickets.strimmedordernumber
				+ ", " + SMTabledeliverytickets.ssignedbyname
				+ ", " + SMTabledeliverytickets.msignature
				+ ", " + SMTabledeliverytickets.sbilltoname 
				+ ", " + SMTabledeliverytickets.sbilltoadd1
				+ ", " + SMTabledeliverytickets.sbilltoadd2 //25
				
				+ ", " + SMTabledeliverytickets.sbilltoadd3
				+ ", " + SMTabledeliverytickets.sbilltocity
				+ ", " + SMTabledeliverytickets.sbilltostate				
				+ ", " + SMTabledeliverytickets.sbilltozip
				+ ", " + SMTabledeliverytickets.sbilltocontact //30				
				
				+ ", " + SMTabledeliverytickets.sbilltophone
				+ ", " + SMTabledeliverytickets.sponumber
				+ ", " + SMTabledeliverytickets.sshiptoname 			
				+ ", " + SMTabledeliverytickets.sshiptoadd1			
				+ ", " + SMTabledeliverytickets.sshiptoadd2 //35
				
				+ ", " + SMTabledeliverytickets.sshiptoadd3
				+ ", " + SMTabledeliverytickets.sshiptocity
				+ ", " + SMTabledeliverytickets.sshiptostate			
				+ ", " + SMTabledeliverytickets.sshiptozip				
				+ ", " + SMTabledeliverytickets.sshiptocountry //40
				
				+ ", " + SMTabledeliverytickets.sshiptocontact
				+ ", " + SMTabledeliverytickets.sshiptophone
				+ ", " + SMTabledeliverytickets.sshiptofax 			
				+ ", " + SMTabledeliverytickets.iworkorderid			
				+ ", " + SMTabledeliverytickets.stermscode //45
				
				+ ", " + SMTabledeliverytickets.iposted
				+ ", " + SMTabledeliverytickets.smechanicname 
				+ ", " + SMTabledeliverytickets.mterms 
				+ ", " + SMTabledeliverytickets.sdeliveredby 
				+ ", " + SMTabledeliverytickets.lsignaturboxwidth //50
				
				+ ", " + SMTabledeliverytickets.mdbaaddress 
				+ ", " + SMTabledeliverytickets.mdbaremittoaddress 
				+ ", " + SMTabledeliverytickets.sdbadeliveryticketreceiptlogo
				+ ") VALUES ("
				+ "NOW()"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlinitiatedbyid()).trim() + ""
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsinitiatedbyfullname()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdetaillines()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmcomments()).trim() + "'" //5
				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssignedbyname()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmsignature()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoname()).trim() + "'" 
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd1()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd2()).trim() + "'" //25
				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd3()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltocity()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltostate()).trim() + "'" 			
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltozip()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltocontact()).trim() + "'" //30
				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltophone()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsponumber()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoname()).trim() + "'" 		
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd1()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd2()).trim() + "'" //35
				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd3()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocity()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptostate()).trim() + "'" 				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptozip()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocountry()).trim() + "'" //40
				
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocontact()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptophone()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptofax()).trim() + "'" 			
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getiworkorderid()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstermscode()).trim() + "'" //45
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getiposted()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsmechanicname()).trim() + "'" 
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmterms()).trim() + "'" 
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdeliveredby()).trim() + "'" 
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlsignatureboxwidth()).trim() //50
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmdbaaddress()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmdbaremittoaddress()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdbadeliveryticketreceiptlogo()).trim() + "'"
				+ ")"
			;

    	}else{
    		
			SQL = " UPDATE " + SMTabledeliverytickets.TableName + " SET "
			+ " " + SMTabledeliverytickets.datinitiated + " = '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getsdatinitiated()) + "'"
			+ ", " + SMTabledeliverytickets.mcomments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmcomments().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltoadd1  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd1().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltoadd2  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd2().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltoadd3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoadd3().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltocity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltocity().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltostate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltostate().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltozip + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltozip().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltocontact  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltocontact().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoname().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sbilltophone  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltophone().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sdetaillines  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdetaillines().trim()) + "'"
			+ ", " + SMTabledeliverytickets.linitiatedbyid  + " = " + clsDatabaseFunctions.FormatSQLStatement(getlinitiatedbyid().trim()) + ""
			+ ", " + SMTabledeliverytickets.sinitiatedbyfullname  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsinitiatedbyfullname().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sponumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsponumber().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptoadd1  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd1().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptoadd2  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd2().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptoadd3  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoadd3().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptocity  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocity().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptostate  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptostate().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptozip + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptozip().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptocontact  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocontact().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptocountry  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptocountry().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptofax  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptofax().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptoname  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoname().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sshiptophone  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptophone().trim()) + "'"
			+ ", " + SMTabledeliverytickets.strimmedordernumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
			+ ", " + SMTabledeliverytickets.stermscode  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstermscode().trim()) + "'"
			+ ", " + SMTabledeliverytickets.mterms  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmterms().trim()) + "'"
			+ ", " + SMTabledeliverytickets.iposted  + " = " + clsDatabaseFunctions.FormatSQLStatement(getiposted().trim()) + ""
			+ ", " + SMTabledeliverytickets.sdeliveredby  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdeliveredby().trim()) + "'"
			+ ", " + SMTabledeliverytickets.mdbaaddress  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdbaaddress().trim()) + "'"
			+ ", " + SMTabledeliverytickets.mdbaremittoaddress  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdbaremittoaddress().trim()) + "'"
			+ ", " + SMTabledeliverytickets.sdbadeliveryticketreceiptlogo  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdbadeliveryticketreceiptlogo().trim()) + "'"
			+ " WHERE ("
				+ "(" + SMTabledeliverytickets.lid + " = " + getslid() + ")"
			+ ")"
			;
    	}

		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			
			throw new Exception ("Error [1410874393] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsNewRecord().compareToIgnoreCase("1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_slid = Long.toString(rs.getLong(1));
				}else {
					m_slid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_slid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }

	public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1410874408] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067678]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067679]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";

    	//Don't need a data transaction if we are just doing a single SQL execute:
    	SQL = "DELETE FROM " + SMTabledeliverytickets.TableName
    		+ " WHERE ("
    			+ SMTabledeliverytickets.lid + " = " + this.getslid()
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception("Error [1410874430] - Could not delete " + ParamObjectName + " with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Empty the values:
		initEntryVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_slid = m_slid.trim();
    	if (m_slid.compareToIgnoreCase("") == 0){
    		m_slid = "-1";
    	}
    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_slid + "'.");
		}
    	
    	m_datinitiated = m_datinitiated.trim();
        if (m_datinitiated.compareToIgnoreCase("") == 0){
        	m_datinitiated = EMPTY_DATETIME_STRING;
        }
        if (m_datinitiated.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_datinitiated)){
        		sErrors += "Date initiated is invalid: '" + m_datinitiated + "'.";
        	}
        }
        
        m_linitiatedbyid = m_linitiatedbyid.trim();
        if (m_linitiatedbyid.length() > SMTabledeliverytickets.linitiatedbyidlength){
        	sErrors += "Initiated by ID cannot be more than " + Integer.toString(SMTabledeliverytickets.linitiatedbyidlength) + " characters.  ";
        }
        m_sinitiatedbyfullname = m_sinitiatedbyfullname.trim();
        if (m_sinitiatedbyfullname.length() > SMTabledeliverytickets.sinitiatedbyfullnamelength){
        	sErrors += "Initiated by full name cannot be more than " + Integer.toString(SMTabledeliverytickets.sinitiatedbyfullnamelength) + " characters.  ";
        }
        
        m_strimmedordernumber = m_strimmedordernumber.trim();
        if (m_strimmedordernumber.length() > SMTabledeliverytickets.strimmedordernumberlength){
        	sErrors += "Order number cannot be more than " + Integer.toString(SMTabledeliverytickets.strimmedordernumberlength) + " characters.  ";
        }
      
    	//Make sure it's a real order:
        if (m_strimmedordernumber.compareToIgnoreCase("") != 0){
	        SMOrderHeader ord = new SMOrderHeader();
	        ord.setM_strimmedordernumber(m_strimmedordernumber);
	        if (!ord.load(conn)){
	        	sErrors += "Could not load order number '" + m_strimmedordernumber + "' - " + ord.getErrorMessages() + ".  ";
	        }else if(!ord.isDBAValid(conn)){
	        	sErrors += "The 'Doing Business As Address' for this order as been deleted or is invalid.";
	        }
        }else{
        	   sErrors += "An order number is required. ";
        }
        
    	m_ssignedbyname = m_ssignedbyname.trim();
        if (m_ssignedbyname.length() > SMTabledeliverytickets.ssignedbynamelength){
        	sErrors += "Signed by name cannot be more than " + Integer.toString(SMTabledeliverytickets.ssignedbynamelength) + " characters.  ";
        }
        
        m_datsigneddate = m_datsigneddate.trim();
        if (m_datsigneddate.compareToIgnoreCase("") == 0){
        	m_datsigneddate = EMPTY_DATE_STRING;
        }
        if (m_datsigneddate.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datsigneddate)){
        		sErrors += "Date signed is invalid: '" + m_datsigneddate + "'.";
        	}
        }
        
        m_sbilltoname = m_sbilltoname.trim();
        if (m_sbilltoname.length() > SMTabledeliverytickets.sbilltonamelength){
        	sErrors += "Bill to name cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltonamelength) + " characters.  ";
        }

        m_sbilltoadd1 = m_sbilltoadd1.trim();
        if (m_sbilltoadd1.length() > SMTabledeliverytickets.sbilltoadd1length){
        	sErrors += "Bill to address line 1 cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltoadd1length) + " characters.  ";
        }

        m_sbilltoadd2 = m_sbilltoadd2.trim();
        if (m_sbilltoadd2.length() > SMTabledeliverytickets.sbilltoadd2length){
        	sErrors += "Bill to address line 3 cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltoadd2length) + " characters.  ";
        }

        m_sbilltoadd3 = m_sbilltoadd3.trim();
        if (m_sbilltoadd3.length() > SMTabledeliverytickets.sbilltoadd3length){
        	sErrors += "Bill to address line 4 cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltoadd3length) + " characters.  ";
        }

        m_sbilltocontact = m_sbilltocontact.trim();
        if (m_sbilltocontact.length() > SMTabledeliverytickets.sbilltocontactlength){
        	sErrors += "Bill to contact cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltocontactlength) + " characters.  ";
        }

        m_sbilltophone = m_sbilltophone.trim();
        if (m_sbilltophone.length() > SMTabledeliverytickets.sbilltophonelength){
        	sErrors += "Bill to phone cannot be more than " + Integer.toString(SMTabledeliverytickets.sbilltophonelength) + " characters.  ";
        }

        m_sponumber = m_sponumber.trim();
        if (m_sponumber.length() > SMTabledeliverytickets.sponumberlength){
        	sErrors += "PO Number cannot be more than " + Integer.toString(SMTabledeliverytickets.sponumberlength) + " characters.  ";
        }

        m_sshiptoname = m_sshiptoname.trim();
        if (m_sshiptoname.length() > SMTabledeliverytickets.sshiptonamelength){
        	sErrors += "Ship to name cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptonamelength) + " characters.  ";
        }

        m_sshiptoadd1 = m_sshiptoadd1.trim();
        if (m_sshiptoadd1.length() > SMTabledeliverytickets.sshiptoadd1length){
        	sErrors += "Ship to address line 1 cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptoadd1length) + " characters.  ";
        }

        m_sshiptoadd2 = m_sshiptoadd2.trim();
        if (m_sshiptoadd2.length() > SMTabledeliverytickets.sshiptoadd2length){
        	sErrors += "Ship to address line 3 cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptoadd2length) + " characters.  ";
        }
        
        m_sshiptoadd3 = m_sshiptoadd3.trim();
        if (m_sshiptoadd3.length() > SMTabledeliverytickets.sshiptoadd3length){
        	sErrors += "Ship to address line 4 cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptoadd3length) + " characters.  ";
        }

        m_sshiptocountry = m_sshiptocountry.trim();
        if (m_sshiptocountry.length() > SMTabledeliverytickets.sshiptocountrylength){
        	sErrors += "Ship to country cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptocountrylength) + " characters.  ";
        }

        m_sshiptocontact = m_sshiptocontact.trim();
        if (m_sshiptocontact.length() > SMTabledeliverytickets.sshiptocontactlength){
        	sErrors += "Ship to contact cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptocontactlength) + " characters.  ";
        }

        m_sshiptophone = m_sshiptophone.trim();
        if (m_sshiptophone.length() > SMTabledeliverytickets.sshiptophonelength){
        	sErrors += "Ship to phone cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptophonelength) + " characters.  ";
        }

        m_sshiptofax = m_sshiptofax.trim();
        if (m_sshiptofax.length() > SMTabledeliverytickets.sshiptofaxlength){
        	sErrors += "Ship to fax cannot be more than " + Integer.toString(SMTabledeliverytickets.sshiptofaxlength) + " characters.  ";
        }

        m_smechanicname = m_smechanicname.trim();
        if (m_smechanicname.length() > SMTabledeliverytickets.smechanicnamelength){
        	sErrors += "Mechanic's name cannot be more than " + Integer.toString(SMTabledeliverytickets.smechanicnamelength) + " characters.  ";
        }
        
        m_stermscode = m_stermscode.trim();
        if (m_stermscode.length() > SMTabledeliverytickets.stermscodelength){
        	sErrors += "Terms code cannot be more than " + Integer.toString(SMTabledeliverytickets.stermscodelength) + " characters.  ";
        }
        
        m_sdeliveredby = m_sdeliveredby.trim();
        if (m_sdeliveredby.length() > SMTabledeliverytickets.sdeliveredbylength){
        	sErrors += "delivered by name cannot be more than " + Integer.toString(SMTabledeliverytickets.sdeliveredbylength) + " characters.  ";
        }
        m_lsignatureboxwidth = m_lsignatureboxwidth.trim();
        if(m_lsignatureboxwidth.compareToIgnoreCase("") == 0){
        	m_lsignatureboxwidth = "0";
        }
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}
    }

   public void validate_acceptance_screen_fields (Connection conn) throws Exception{
    	
    	boolean bValid = true;
    	long lID = 0;
    	String sErrors = "";
    	
		try {
			lID = Long.parseLong(getslid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + getslid() + "'.  ";
		}
    	if (lID <= 0){
    		bValid = false;
    		sErrors += "Invalid ID: '" + getslid() + "'.  ";
    	}
    	

        if (getsdatsigned().compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getsdatsigned())){
	        	bValid = false;
	        	sErrors += "Invalid date/time signed: '" + getsdatsigned() + "  .";
	        }
        }

    	setssignedbyname(getssignedbyname().trim());
    	if (getssignedbyname().length() > SMTabledeliverytickets.ssignedbynamelength){
    		bValid = false;
    		sErrors += "Signed by name is limited to " + Integer.toString(SMTabledeliverytickets.ssignedbynamelength) + " characters.  ";
    	}

    	if (!bValid){
    		throw new Exception(sErrors);
    	}
    	return;
    }

	public String getslid() {
		return m_slid;
	}
	public void setslid(String slid) {
		m_slid = slid;
	}
	public String getsdatinitiated() {
		return m_datinitiated;
	}
	public void setsdatinitiated(String sdatinitiated) {
		m_datinitiated = sdatinitiated;
	}
	public String getlinitiatedbyid() {
		return m_linitiatedbyid;
	}
	public void setlinitiatedbyid(String linitiatedbyid) {
		m_linitiatedbyid = linitiatedbyid;
	}
	public String getsinitiatedbyfullname() {
		return m_sinitiatedbyfullname;
	}
	public void setsinitiatedbyfullname(String sinitiatedbyfullname) {
		m_sinitiatedbyfullname = sinitiatedbyfullname;
	}
	public String getsdetaillines() {
		return m_sdetaillines;
	}
	public void setsdetaillines(String sdetaillines) {
		m_sdetaillines = sdetaillines;
	}
	public String getsNewRecord() {
		return m_sNewRecord;
	}
	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	public String getstrimmedordernumber() {
		return m_strimmedordernumber;
	}
	public void setstrimmedordernumber(String strimmedordernumber) {
		m_strimmedordernumber = strimmedordernumber;
	}
	public String getssignedbyname() {
		return m_ssignedbyname;
	}

	public void setssignedbyname(String m_ssignedbyname) {
		this.m_ssignedbyname = m_ssignedbyname;
	}
	public String getsdatsigned() {
		return m_datsigneddate;
	}
	public void setsdatsigned(String sdatsigned) {
		m_datsigneddate = sdatsigned;
	}
	public String getmcomments() {
		return m_mcomments;
	}

	public void setmcomments(String mcomments) {
		m_mcomments = mcomments;
	}

	public String getmsignature() {
		return m_msignature;
	}

	public void setmsignature(String msignature) {
		m_msignature = msignature;
	}

	public String getsbilltoname() {
		return m_sbilltoname;
	}

	public void setsbilltoname(String sbilltoname) {
		m_sbilltoname = sbilltoname;
	}

	public String getsbilltoadd1() {
		return m_sbilltoadd1;
	}

	public void setsbilltoadd1(String sbilltoadd1) {
		m_sbilltoadd1 = sbilltoadd1;
	}

	public String getsbilltoadd2() {
		return m_sbilltoadd2;
	}

	public void setsbilltoadd2(String sbilltoadd2) {
		m_sbilltoadd2 = sbilltoadd2;
	}

	public String getsbilltoadd3() {
		return m_sbilltoadd3;
	}

	public void setsbilltoadd3(String sbilltoadd3) {
		m_sbilltoadd3 = sbilltoadd3;
	}

	public String getsbilltocontact() {
		return m_sbilltocontact;
	}

	public void setsbilltocontact(String sbilltocontact) {
		m_sbilltocontact = sbilltocontact;
	}

	public String getsbilltophone() {
		return m_sbilltophone;
	}

	public void setsbilltophone(String sbilltophone) {
		m_sbilltophone = sbilltophone;
	}

	public String getsponumber() {
		return m_sponumber;
	}

	public void setsponumber(String sponumber) {
		m_sponumber = sponumber;
	}

	public String getsshiptoname() {
		return m_sshiptoname;
	}

	public void setsshiptoname(String sshiptoname) {
		m_sshiptoname = sshiptoname;
	}

	public String getsshiptoadd1() {
		return m_sshiptoadd1;
	}

	public void setsshiptoadd1(String sshiptoadd1) {
		m_sshiptoadd1 = sshiptoadd1;
	}

	public String getsshiptoadd2() {
		return m_sshiptoadd2;
	}

	public void setsshiptoadd2(String sshiptoadd2) {
		m_sshiptoadd2 = sshiptoadd2;
	}

	public String getsshiptoadd3() {
		return m_sshiptoadd3;
	}

	public void setsshiptoadd3(String sshiptoadd3) {
		m_sshiptoadd3 = sshiptoadd3;
	}

	public String getsshiptocountry() {
		return m_sshiptocountry;
	}

	public void setsshiptocountry(String sshiptocountry) {
		m_sshiptocountry = sshiptocountry;
	}

	public String getsshiptocontact() {
		return m_sshiptocontact;
	}

	public void setsshiptocontact(String sshiptocontact) {
		m_sshiptocontact = sshiptocontact;
	}

	public String getsshiptophone() {
		return m_sshiptophone;
	}

	public void setsshiptophone(String sshiptophone) {
		m_sshiptophone = sshiptophone;
	}

	public String getsshiptofax() {
		return m_sshiptofax;
	}

	public void setsshiptofax(String sshiptofax) {
		m_sshiptofax = sshiptofax;
	}

	public String getsmechanicname() {
		return m_smechanicname;
	}

	public void setsmechanicname(String smechanicname) {
		m_smechanicname = smechanicname;
	}
	
	public String getiworkorderid() {
		if(m_iworkorderid.compareToIgnoreCase("") == 0){
			m_iworkorderid = "0";
		}
		return m_iworkorderid;
	}
	
	public void setiworkorderid(String iworkorderid) {
		m_iworkorderid = iworkorderid;
	}

	public String getmterms() {
		return m_mterms;
	}
	
	public void setmterms(String mterms) {
		m_mterms = mterms;
	}
	
	public String getiposted() {
		if(m_iposted.compareToIgnoreCase("") == 0){
			m_iposted = "0";
		}
		return m_iposted;
	}
	
	public void setiposted(String iposted) {	
		m_iposted = iposted;
	}

	public String getObjectName(){
		return ParamObjectName;
	}
	
	public void setsshiptozip(String sshiptozip){
		m_sshiptozip = sshiptozip;
	}
	
	public String getsshiptozip() {
		return m_sshiptozip;
	}

	public void setsshiptostate(String sshiptostate){
		m_sshiptostate = sshiptostate;
	}
	
    public String getsshiptostate() {
		return m_sshiptostate;
	}

    public void setsshiptocity(String sshiptocity){
    	m_sshiptocity = sshiptocity;
	}
    
	public String getsshiptocity() {
		return m_sshiptocity;
	}

    public void setsbilltozip(String sbilltozip){
    	m_sbilltozip = sbilltozip;
	}
    
	public String getsbilltozip() {
		return m_sbilltozip;
	}

	public void setsbilltostate(String sbilltostate){
		m_sbilltostate = sbilltostate;
	}
	   
	public String getsbilltostate() {
		return m_sbilltostate;
	}
	
	public void setsbilltocity(String sbilltocity){
		m_sbilltocity = sbilltocity;
	}

	public String getsbilltocity() {
		return m_sbilltocity;
	}
	
	public void setstermscode(String stermscode) {
		m_stermscode = stermscode;	
	}
	public String getstermscode() {
		return m_stermscode;	
	}
	public void setsdeliveredby(String sdeliveredby) {
		m_sdeliveredby = sdeliveredby;	
	}
	public String getsdeliveredby() {
		return m_sdeliveredby;	
	}
	public void setlsignatureboxwidth(String lsignatureboxwidth) {
		m_lsignatureboxwidth = lsignatureboxwidth;	
	}
	public String getlsignatureboxwidth() {
		return m_lsignatureboxwidth;	
	}
	public void setmdbaaddress(String mdbaaddress) {
		m_mdbaaddress = mdbaaddress;	
	}
	public String getmdbaaddress() {
		return m_mdbaaddress;	
	}
	public void setmdbaremittoaddress(String mdbaremittoaddress) {
		m_mdbaremittoaddress = mdbaremittoaddress;	
	}
	public String getmdbaremittoaddress() {
		return m_mdbaremittoaddress;	
	}
	
	private void initEntryVariables(){
    	m_slid = "-1";
    	m_datinitiated = EMPTY_DATETIME_STRING;
    	m_linitiatedbyid = "0";
    	m_sinitiatedbyfullname = "";
    	m_sdetaillines = "";
    	m_mcomments = "";
    	m_strimmedordernumber = "";
    	m_ssignedbyname = "";
    	m_datsigneddate = EMPTY_DATE_STRING;
    	m_msignature= "";
    	m_sbilltoname = "";
    	m_sbilltoadd1 = "";
    	m_sbilltoadd2 = "";
    	m_sbilltoadd3 = "";
    	m_sbilltocity= "";
    	m_sbilltostate = "";
    	m_sbilltozip = "";
    	m_sbilltocontact = "";
    	m_sbilltophone = "";
    	m_sponumber = "";
    	m_sshiptoname = "";
    	m_sshiptoadd1 = "";
    	m_sshiptoadd2 = "";
    	m_sshiptoadd3 = "";
    	m_sshiptocity = "";
    	m_sshiptostate = "";
    	m_sshiptozip = "";
    	m_sshiptocountry = "";
    	m_sshiptocontact = "";
    	m_sshiptophone = "";
    	m_sshiptofax = "";
    	m_smechanicname = "";
    	m_iworkorderid = "0";
    	m_mterms = "";
    	m_stermscode = "";
    	m_iposted = "0";
    	m_sdeliveredby = "";
    	m_lsignatureboxwidth = "0";
    	m_mdbaaddress = "";
    	m_mdbaremittoaddress = "";
    	m_sNewRecord = "1";
	}

	public void post_without_data_transaction(ServletContext servletContext,
			String sDBID, String userName)throws Exception{
		if(getiposted().compareToIgnoreCase("1") == 0){
			throw new Exception("This work order was already posted ");
		} 
		//Update Posting status
		String SQL = "";
    	SQL = "UPDATE " 
    		+ SMTabledeliverytickets.TableName 
    		+ " SET " + SMTabledeliverytickets.iposted + " = '1'"
    		+ " WHERE ("
    			+ "(" + SMTabledeliverytickets.lid + " = " + getslid() + ")"
    		+ ")"
    		;
    	try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				servletContext, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".post_without_data_transaction - user: " + userName);
		} catch (Exception e){
			throw new Exception("[1445459884] Error executing SQL command changing delivery ticket posting status ");
		}
	}

	public String getsdbadeliveryticketreceiptlogo() {
		return m_sdbadeliveryticketreceiptlogo;
	}

	public void setsdbadeliveryticketreceiptlogo(String m_sdbadeliveryticketreceiptlogo) {
		this.m_sdbadeliveryticketreceiptlogo = m_sdbadeliveryticketreceiptlogo;
	}
}
