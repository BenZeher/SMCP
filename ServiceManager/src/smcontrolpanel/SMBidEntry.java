package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablebidproductamounts;
import SMDataDefinition.SMTablebidproducttypes;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTableordersources;
import SMDataDefinition.SMTablesalescontacts;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMBidEntry extends clsMasterEntry{

	public static final String ParamObjectName = "Sales Lead";

	//Particular to the specific class
	public static final String ParamID  = "lid";
	public static final String Paramssalespersoncode = "ssalespersoncode";
	public static final String Paramdatoriginationdate = "dattimeoriginationdate";
	public static final String ParamHasBidDate = "HasBidDate";
	public static final String Paramdattimebiddate = "dattimebiddate";
	public static final String Paramscustomername = "scustomername";
	public static final String Paramsprojectname = "sprojectname";
	public static final String ParamHasPlansReceivedDate = "HasPlansReceivedDate";
	public static final String Paramdatplansreceived = "dattimeplansreceived";
	public static final String ParamHasTakeoffCompleteDate = "HasTakeoffCompleteDate";
	public static final String Paramdattakeoffcomplete = "dattimetakeoffcomplete";
	public static final String ParamHasPriceCompleteDate = "HasPriceCompleteDate";
	public static final String Paramdatpricecomplete = "dattimepricecomplete";
	public static final String Parammdescription = "mdescription";
	public static final String ParamHasActualBidDate = "HasActualBidDate";
	public static final String Paramdattimeactualbiddate = "dattimeactualbiddate";
	public static final String Paramscontactname = "scontactname";
	public static final String Paramsphonenumber = "sphonenumber";
	public static final String Paramemailaddress = "emailaddress";

	public static final String Paramsstatus = "sstatus";
	public static final String Paramdapproximateamount = "dapproximateamount";
	public static final String Paramiprojecttype = "iprojecttype";
	public static final String Paramscreatedbyfullname = "screatedbyfullname";
	public static final String Paramlcreatedbyuserid = "lcreatedbyuserid";
	public static final String ParamdatCreatedTime = "datcreatedtime";
	public static final String Paramsaltphonenumber = "saltphonenumber";
	public static final String Paramsfaxnumber = "sfaxnumber";
	public static final String ParamsBidProductAmount = "sbidproductamount";
	public static final String ParamiOrderSourceID = "iordersourceid";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramisalescontactid = "isalescontactid";
	public static final String Paramsshiptoaddress1 = "sshiptoaddress1";
	public static final String Paramsshiptoaddress2 = "sshiptoaddress2";
	public static final String Paramsshiptoaddress3 = "sshiptoaddress3";
	public static final String Paramsshiptoaddress4 = "sshiptoaddress4";
	public static final String Paramsshiptocity = "sshiptocity";
	public static final String Paramsshiptostate = "sshiptostate";
	public static final String Paramsshiptozip = "sshiptozip";
	public static final String Paramstakeoffpersoncode = "stakeoffpersoncode";
	public static final String Paramspricingpersoncode = "spricingpersoncode";
	public static final String Paramscreatedfromordernumber = "screatedfromordernumber";
	public static final String Paramlsalesgroupid = "lsalesgroupid";
	
	//This value will hold the last SAVED value for the origination date, so we can tell when it's been changed on the screen:
	public static final String Paramlastsaveddatoriginationdate = "lastsaveddattimeoriginationdate";
	public static final String Paramlastsaveddattimebiddate = "lastsaveddattimebiddate";
	public static final String Paramlastsaveddatplansreceived = "lastsaveddattimeplansreceived";
	public static final String Paramlastsaveddattakeoffcomplete = "lastsaveddattimetakeoffcomplete";
	public static final String Paramlastsaveddatpricecomplete = "lastsaveddattimepricecomplete";
	public static final String Paramlastsaveddattimeactualbiddate = "lastsaveddattimeactualbiddate";
	
	private String m_sid;
	private String m_ssalespersoncode;
	private String m_datoriginationdate;
	private String m_dattimebiddate;
	private String m_scustomername;
	private String m_sprojectname;
	private String m_datplansreceived;
	private String m_dattakeoffcomplete;
	private String m_datpricecomplete;
	private String m_mdescription;
	private String m_dattimeactualbiddate;
	private String m_scontactname;
	private String m_sphonenumber;
	private String m_emailaddress;
	private String m_sstatus;
	private String m_dapproximateamount;
	private String m_iprojecttype;
	private String m_screatedbyfullname;
	private String m_lcreatedbyuserid;
	private String m_datcreatedtime;
	private String m_saltphonenumber;
	private String m_sfaxnumber;
	private String m_iordersourceid;
	private String m_sordersourcedesc;
	private String m_sgdoclink;
	private String m_isalescontactid;
	private String m_sshiptoaddress1;
	private String m_sshiptoaddress2;
	private String m_sshiptoaddress3;
	private String m_sshiptoaddress4;
	private String m_sshiptocity;
	private String m_sshiptostate;
	private String m_sshiptozip;
	private String m_stakeoffpersoncode;
	private String m_spricingpersoncode;
	private String m_screatedfromordernumber;
	private String m_lsalesgroupid;
	
	private String m_slastsaveddatoriginationdate;
	private String m_slastsaveddattimebiddate;
	private String m_slastsaveddatplansreceived;
	private String m_slastsaveddattakeoffcomplete;
	private String m_slastsaveddatpricecomplete;
	private String m_slastsaveddattimeactualbiddate;
	
	private ArrayList <String> m_arrBidProductTypeAmounts;
	private boolean bDebugMode = false;

	public SMBidEntry() {
		super();
		initBidVariables();
	}

	SMBidEntry (HttpServletRequest req){
		super(req);
		initBidVariables();

		m_sid = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.ParamID, req).trim();

		m_ssalespersoncode = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramssalespersoncode, req).trim();
		m_stakeoffpersoncode = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramstakeoffpersoncode, req).trim();
		m_spricingpersoncode = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramspricingpersoncode, req).trim();
		
		m_datoriginationdate = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramdatoriginationdate, req).trim();
		if (m_datoriginationdate.compareToIgnoreCase("") == 0){
			m_datoriginationdate = EMPTY_DATE_STRING;
		}

		if (clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.ParamHasBidDate, req).trim().compareToIgnoreCase("1") == 0){
			m_dattimebiddate = clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdattimebiddate, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							SMBidEntry.Paramdattimebiddate + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									SMBidEntry.Paramdattimebiddate + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdattimebiddate + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_dattimebiddate = m_dattimebiddate + "PM";
			}else{
				m_dattimebiddate = m_dattimebiddate + "AM";
			}
		}else{
			m_dattimebiddate = EMPTY_DATETIME_STRING;
		}
		m_scustomername = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramscustomername, req).trim();
		m_sprojectname = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsprojectname, req).trim();

		if (clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.ParamHasPlansReceivedDate, req).trim().compareToIgnoreCase("1") == 0){
			m_datplansreceived = clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdatplansreceived, req).trim();
		}else{
			m_datplansreceived = EMPTY_DATE_STRING;
		}

		if (clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.ParamHasTakeoffCompleteDate, req).trim().compareToIgnoreCase("1") == 0){
			m_dattakeoffcomplete = clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdattakeoffcomplete, req).trim();
		}else{
			m_dattakeoffcomplete = EMPTY_DATE_STRING;
		}

		if (clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.ParamHasPriceCompleteDate, req).trim().compareToIgnoreCase("1") == 0){
			m_datpricecomplete = clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdatpricecomplete, req).trim();
		}else{
			m_datpricecomplete = EMPTY_DATE_STRING;
		}

		m_mdescription = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Parammdescription, req).trim();

		if (clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.ParamHasActualBidDate, req).trim().compareToIgnoreCase("1") == 0){

			m_dattimeactualbiddate = clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdattimeactualbiddate, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							SMBidEntry.Paramdattimeactualbiddate + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									SMBidEntry.Paramdattimeactualbiddate + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					SMBidEntry.Paramdattimeactualbiddate + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_dattimeactualbiddate = m_dattimeactualbiddate + "PM";
			}else{
				m_dattimeactualbiddate = m_dattimeactualbiddate + "AM";
			}
		}else{
			m_dattimeactualbiddate = EMPTY_DATETIME_STRING;
		}
		m_scontactname = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramscontactname, req).trim();
		m_sphonenumber = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsphonenumber, req);
		m_emailaddress = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramemailaddress, req).trim();

		m_sstatus = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsstatus, req).trim();
		if (m_sstatus.compareToIgnoreCase("") == 0){
			m_sstatus = SMTablebids.STATUS_PENDING;
		}
		m_dapproximateamount = clsManageRequestParameters.get_Request_Parameter(
				SMBidEntry.Paramdapproximateamount, req).trim();
		if (m_dapproximateamount.compareToIgnoreCase("") == 0){
			m_dapproximateamount = "0.00";
		}
		m_iprojecttype = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramiprojecttype, req).trim();
		//m_sbinnumber = SMUtilities.get_Request_Parameter(SMBidEntry.Paramsbinnumber, req).trim();
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramscreatedbyfullname, req).trim();
		m_lcreatedbyuserid = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlcreatedbyuserid, req).trim();
		m_datcreatedtime = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.ParamdatCreatedTime, req).trim();
		m_saltphonenumber = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsaltphonenumber, req);
		m_sfaxnumber = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsfaxnumber, req);
		m_iordersourceid = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.ParamiOrderSourceID, req).trim();
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsgdoclink, req).trim();
		m_isalescontactid = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramisalescontactid, req).trim();
		if (m_isalescontactid.compareToIgnoreCase("0") == 0){
			m_isalescontactid = "";
		}

		m_sshiptoaddress1 = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptoaddress1, req).trim();
		m_sshiptoaddress2 = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptoaddress2, req).trim();
		m_sshiptoaddress3 = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptoaddress3, req).trim();
		m_sshiptoaddress4 = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptoaddress4, req).trim();
		m_sshiptocity = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptocity, req).trim();
		m_sshiptostate = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptostate, req).trim();
		m_sshiptozip = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramsshiptozip, req).trim();	
		m_screatedfromordernumber = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramscreatedfromordernumber, req).trim();
		m_lsalesgroupid = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlsalesgroupid, req).trim();
		
		//Get the 'last saved dates' here:
		m_slastsaveddatoriginationdate = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddatoriginationdate, req).trim();
		m_slastsaveddattimebiddate = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddattimebiddate, req).trim();
		m_slastsaveddatplansreceived = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddatplansreceived, req).trim();
		m_slastsaveddattakeoffcomplete = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddattakeoffcomplete, req).trim();
		m_slastsaveddatpricecomplete = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddatpricecomplete, req).trim();
		m_slastsaveddattimeactualbiddate = clsManageRequestParameters.get_Request_Parameter(SMBidEntry.Paramlastsaveddattimeactualbiddate, req).trim();
		
		//Load the product types from the request here:
		Enumeration<?> eParams = req.getParameterNames();
		String sParam = "";
		m_arrBidProductTypeAmounts.clear();
		while (eParams.hasMoreElements()){
			sParam = (String) eParams.nextElement();
			if (sParam.contains(ParamsBidProductAmount)){
				String sProductTypeString = sParam.substring(
						ParamsBidProductAmount.length(), sParam.length());
				m_arrBidProductTypeAmounts.add(
						sProductTypeString
						+ clsStringFunctions.PadLeft(clsManageRequestParameters.get_Request_Parameter(sParam, req), " ", 20));
				if (bDebugMode){
					System.out.println("[1579265700] In " + this.toString() 
							+ ".SMBidEntry(request), sParam = " + sParam);
					System.out.println("In " + this.toString() 
							+ ".SMBidEntry(request), sParam value = " 
							+ clsManageRequestParameters.get_Request_Parameter(sParam, req));
					System.out.println("In " + this.toString() 
							+ ".SMBidEntry(request), sProductTypeString = " + sProductTypeString);
				}
			}
		}
		Collections.sort(m_arrBidProductTypeAmounts);

	}
	public boolean load (ServletContext context, String sDBIB, String sUserID, String sUserFullName){
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + " - user: " + sUserID + " - " + sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = load (conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080411]");
		return bResult;

	}
	public boolean load (Connection conn){
		return load (m_sid, conn);
	}
	private boolean load (String sID, Connection conn){

		@SuppressWarnings("unused")
		long lID;
		try{
			lID = Long.parseLong(sID);
		}catch(NumberFormatException n){
			super.addErrorMessage("Invalid ID: '" + sID + "'");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTablebids.TableName
		+ " WHERE ("
		+ SMTablebids.lid + " = " + sID
		+ ")";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sid = sID;
				m_ssalespersoncode = rs.getString(SMTablebids.ssalespersoncode);
				m_stakeoffpersoncode = rs.getString(SMTablebids.stakeoffpersoncode);
				m_spricingpersoncode = rs.getString(SMTablebids.spricingpersoncode);
				m_datoriginationdate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablebids.dattimeoriginationdate));
				m_dattimebiddate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTablebids.dattimebiddate));
				m_scustomername = rs.getString(SMTablebids.scustomername);
				m_sprojectname = rs.getString(SMTablebids.sprojectname);
				m_datplansreceived = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablebids.dattimeplansreceived));
				m_dattakeoffcomplete = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablebids.dattimetakeoffcomplete));
				m_datpricecomplete = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablebids.dattimepricecomplete));
				m_mdescription = rs.getString(SMTablebids.mdescription);
				m_dattimeactualbiddate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTablebids.dattimeactualbiddate));
				m_scontactname = rs.getString(SMTablebids.scontactname);
				m_sphonenumber = rs.getString(SMTablebids.sphonenumber);
				m_emailaddress = rs.getString(SMTablebids.emailaddress);
				m_sstatus = rs.getString(SMTablebids.sstatus);
				m_dapproximateamount = clsManageBigDecimals.BigDecimalToFormattedString(
						"#,###,###,##0.00", rs.getBigDecimal(SMTablebids.dapproximateamount));
				m_iprojecttype = rs.getString(SMTablebids.iprojecttype);
//				m_sbinnumber = rs.getString(SMTablebids.sbinnumber);
				m_screatedbyfullname = rs.getString(SMTablebids.screatedbyfullname);
				m_lcreatedbyuserid= rs.getString(SMTablebids.lcreatedbyuserid);
				m_datcreatedtime = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTablebids.datcreatedtime));
				m_saltphonenumber = rs.getString(SMTablebids.saltphonenumber);
				m_sfaxnumber = rs.getString(SMTablebids.sfaxnumber);
				m_iordersourceid = rs.getString(SMTablebids.iordersourceid);
				m_sordersourcedesc = rs.getString(SMTablebids.sordersourcedesc);
				m_sgdoclink = rs.getString(SMTablebids.sgdoclink);
				m_isalescontactid = (Long.toString(rs.getLong(SMTablebids.isalescontactid)));
				if (m_isalescontactid.compareToIgnoreCase("0") == 0){
					m_isalescontactid = "";
				}
				m_sshiptoaddress1 = rs.getString(SMTablebids.sshiptoaddress1);
				m_sshiptoaddress2 = rs.getString(SMTablebids.sshiptoaddress2);
				m_sshiptoaddress3 = rs.getString(SMTablebids.sshiptoaddress3);
				m_sshiptoaddress4 = rs.getString(SMTablebids.sshiptoaddress4);
				m_sshiptocity = rs.getString(SMTablebids.sshiptocity);
				m_sshiptostate = rs.getString(SMTablebids.sshiptostate);
				m_sshiptozip = rs.getString(SMTablebids.sshiptozip);
				m_screatedfromordernumber = rs.getString(SMTablebids.screatedfromordernumber);
				m_lsalesgroupid = (Long.toString(rs.getLong(SMTablebids.lsalesgroupid)));
				
				//Update the last saved dates
				m_slastsaveddatoriginationdate = stripDateFromDateTimeString(m_datoriginationdate);
				m_slastsaveddattimebiddate = stripDateFromDateTimeString(m_dattimebiddate);
				m_slastsaveddatplansreceived = stripDateFromDateTimeString(m_datplansreceived);
				m_slastsaveddattakeoffcomplete = stripDateFromDateTimeString(m_dattakeoffcomplete);
				m_slastsaveddatpricecomplete = stripDateFromDateTimeString(m_datpricecomplete);
				m_slastsaveddattimeactualbiddate = stripDateFromDateTimeString(m_dattimeactualbiddate);
				
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for ID: '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for ID: '" + sID
					+ "' - " + e.getMessage());
			return false;
		}

		if (!loadBidProductTypes(conn)){
			return false;
		}
		
		return true;
	}

	public boolean loadBidProductTypes(Connection conn){
		//Now load the product types:
		String SQL = "SELECT"
			+ " " + SMTablebidproducttypes.TableName + "." + SMTablebidproducttypes.lID
			+ ", " + SMTablebidproducttypes.TableName + "." + SMTablebidproducttypes.sProductType
			+ ", BIDAMOUNTS." + SMTablebidproductamounts.bdAmount + " from " + SMTablebidproducttypes.TableName
			+ " LEFT JOIN (SELECT " + SMTablebidproductamounts.TableName + "." + SMTablebidproductamounts.lBidID
			+ ", " + SMTablebidproductamounts.TableName + "." + SMTablebidproductamounts.lBidProductTypeID
			+ ", " + SMTablebidproductamounts.TableName + "." + SMTablebidproductamounts.bdAmount
			+ " FROM " + SMTablebidproductamounts.TableName
			+ " WHERE ("
			+ SMTablebidproductamounts.TableName + "." + SMTablebidproductamounts.lBidID + " = " + slid()
			+ ")"
			+ ") AS BIDAMOUNTS"
			+ " ON " + SMTablebidproducttypes.TableName + "." + SMTablebidproducttypes.lID
			+ " = BIDAMOUNTS." + SMTablebidproductamounts.lBidProductTypeID
			+ " ORDER BY " + SMTablebidproducttypes.TableName + "." + SMTablebidproducttypes.lID
			;

		if (bDebugMode){
			System.out.println("[1579265707] In " +this.toString() + " SQL getting product amounts = " + SQL);
		}
		m_arrBidProductTypeAmounts.clear();
		try {
			ResultSet rsProductTypes = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsProductTypes.next()){
				String sBidProductAmount = "0.00";
				if (rsProductTypes.getBigDecimal(SMTablebidproductamounts.bdAmount) != null){
					sBidProductAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rsProductTypes.getBigDecimal(SMTablebidproductamounts.bdAmount)); 
				}
				m_arrBidProductTypeAmounts.add(
					clsStringFunctions.PadLeft(Long.toString(
						rsProductTypes.getLong(SMTablebidproducttypes.TableName + "." 
						+ SMTablebidproducttypes.lID)),"0",6)
						
					+ clsStringFunctions.PadLeft(rsProductTypes.getString(SMTablebidproducttypes.TableName 
						+ "." + SMTablebidproducttypes.sProductType)," ", SMTablebidproducttypes.sProductTypeLength)
						
					+ clsStringFunctions.PadLeft(sBidProductAmount, " ", 20)
				);
			}
			rsProductTypes.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading product amounts with SQL: " + SQL + " - " + e.getMessage() + ".");
			return false;
		}
		return true;
	}
	public boolean save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName){

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + " - user: " + sUserID + " - " + sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction.");
			return false;
		}

		boolean bResult = save_without_data_transaction (conn, sUserID, sUserFullName);

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Error committing data transaction.");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080412]");
			return false;
		}

		clsDatabaseFunctions.freeConnection(context, conn, "[1547080413]");
		return bResult;	

	}
	public boolean save_without_data_transaction (Connection conn, String sUserID, String sUserFullName){

		if (!validate_entry_fields(conn)){
			return false;
		}

		String SQL = "SELECT"
			+ " * FROM "
			+ SMTableordersources.TableName 
			+ " WHERE (" 
				+ SMTableordersources.iSourceID + "=" + m_iordersourceid
			+ ")";
		try{	
			//get order source description from database because it is not passed in
			ResultSet rsOrderSource = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsOrderSource.next()){
				m_sordersourcedesc = rsOrderSource.getString(SMTableordersources.sSourceDesc).trim();
			}else{
				m_sordersourcedesc = "";
			}
			rsOrderSource.close();
	
		}catch(SQLException ex){
			super.addErrorMessage("Error reading marketing soure : " + ex.getMessage());
			return false;
		}
		
		if(m_sid.compareToIgnoreCase("-1") == 0){
			m_screatedbyfullname = sUserFullName;
			m_lcreatedbyuserid = sUserID;
			SQL = "INSERT INTO " + SMTablebids.TableName + "("
			+ SMTablebids.dapproximateamount
			+ ", " + SMTablebids.dattimeactualbiddate
			+ ", " + SMTablebids.dattimebiddate
			+ ", " + SMTablebids.dattimeoriginationdate
			+ ", " + SMTablebids.dattimeplansreceived
			+ ", " + SMTablebids.dattimepricecomplete
			+ ", " + SMTablebids.dattimetakeoffcomplete
			+ ", " + SMTablebids.emailaddress
			+ ", " + SMTablebids.iprojecttype
			+ ", " + SMTablebids.mdescription
			+ ", " + SMTablebids.scontactname
			+ ", " + SMTablebids.screatedbyfullname
			+ ", " + SMTablebids.lcreatedbyuserid
			+ ", " + SMTablebids.datcreatedtime
			+ ", " + SMTablebids.scustomername
			+ ", " + SMTablebids.sphonenumber
			+ ", " + SMTablebids.sprojectname
			+ ", " + SMTablebids.ssalespersoncode
			+ ", " + SMTablebids.sstatus
			+ ", " + SMTablebids.saltphonenumber
			+ ", " + SMTablebids.sfaxnumber
			+ ", " + SMTablebids.iordersourceid
			+ ", " + SMTablebids.sordersourcedesc
			+ ", " + SMTablebids.sgdoclink
			+ ", " + SMTablebids.isalescontactid
			+ ", " + SMTablebids.sshiptoaddress1
			+ ", " + SMTablebids.sshiptoaddress2
			+ ", " + SMTablebids.sshiptoaddress3
			+ ", " + SMTablebids.sshiptoaddress4
			+ ", " + SMTablebids.sshiptocity
			+ ", " + SMTablebids.sshiptostate
			+ ", " + SMTablebids.sshiptozip
			+ ", " + SMTablebids.stakeoffpersoncode
			+ ", " + SMTablebids.spricingpersoncode
			+ ", " + SMTablebids.screatedfromordernumber
			+ ", " + SMTablebids.lsalesgroupid
			+ ") VALUES ("
			+ m_dapproximateamount.replace(",", "")
			+ ", '" + super.ampmDateTimeToSQLDateTime(m_dattimeactualbiddate) + "'"
			+ ", '" + super.ampmDateTimeToSQLDateTime(m_dattimebiddate) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datoriginationdate) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datplansreceived) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datpricecomplete) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_dattakeoffcomplete) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_emailaddress.trim()) + "'"
			+ ", " + m_iprojecttype
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_mdescription.trim()) + "'"

			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedbyfullname.trim()) + "'"
			+ ", " + m_lcreatedbyuserid
			+ ", NOW()"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scustomername.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sprojectname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_ssalespersoncode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sstatus.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saltphonenumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber.trim()) + "'"
			+ ", " + m_iordersourceid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sordersourcedesc) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "'"
			+ ", " + m_isalescontactid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress1.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress2.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress3.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress4.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptocity.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptostate.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptozip.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_stakeoffpersoncode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spricingpersoncode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedfromordernumber.trim()) + "'"
			+ ", " + m_lsalesgroupid + ""
			+ ")"
			;
		}else{
			SQL = "UPDATE " + SMTablebids.TableName + " SET "
			+ SMTablebids.dapproximateamount + " = "  + m_dapproximateamount.replace(",", "")
			+ ", " + SMTablebids.dattimeactualbiddate + " = '" 
			+ super.ampmDateTimeToSQLDateTime(m_dattimeactualbiddate) + "'"
			+ ", " + SMTablebids.dattimebiddate + " = '" 
			+ super.ampmDateTimeToSQLDateTime(m_dattimebiddate) + "'"
			+ ", " + SMTablebids.dattimeoriginationdate + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datoriginationdate) + "'"
			+ ", " + SMTablebids.dattimeplansreceived + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datplansreceived) + "'"
			+ ", " + SMTablebids.dattimepricecomplete + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datpricecomplete) + "'"
			+ ", " + SMTablebids.dattimetakeoffcomplete + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_dattakeoffcomplete) + "'"
			+ ", " + SMTablebids.emailaddress 
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_emailaddress.trim()) + "'"
			+ ", " + SMTablebids.iprojecttype + " = " + m_iprojecttype
			+ ", " + SMTablebids.mdescription 
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_mdescription.trim()) + "'"
			+ ", " + SMTablebids.scontactname 
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname.trim()) + "'"
			+ ", " + SMTablebids.scustomername
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scustomername.trim()) + "'"
			+ ", " + SMTablebids.sphonenumber
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber.trim()) + "'"
			+ ", " + SMTablebids.sprojectname
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sprojectname.trim()) + "'"
			+ ", " + SMTablebids.ssalespersoncode
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssalespersoncode.trim()) + "'"
			+ ", " + SMTablebids.sstatus
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sstatus.trim()) + "'"
			+ ", " + SMTablebids.saltphonenumber
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saltphonenumber.trim()) + "'"
			+ ", " + SMTablebids.sfaxnumber
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber.trim()) + "'"
			+ ", " + SMTablebids.iordersourceid + " = " + m_iordersourceid
			+ ", " + SMTablebids.sordersourcedesc
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sordersourcedesc) + "'"
			+ ", " + SMTablebids.sgdoclink
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "'"
			+ ", " + SMTablebids.isalescontactid + " = " + m_isalescontactid
			+ ", " + SMTablebids.sshiptoaddress1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress1) + "'"
			+ ", " + SMTablebids.sshiptoaddress2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress2) + "'"
			+ ", " + SMTablebids.sshiptoaddress3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress3) + "'"
			+ ", " + SMTablebids.sshiptoaddress4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptoaddress4) + "'"
			+ ", " + SMTablebids.sshiptocity + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptocity) + "'"
			+ ", " + SMTablebids.sshiptostate + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptostate) + "'"
			+ ", " + SMTablebids.sshiptozip + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshiptozip) + "'"
			+ ", " + SMTablebids.stakeoffpersoncode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_stakeoffpersoncode) + "'"
			+ ", " + SMTablebids.spricingpersoncode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spricingpersoncode) + "'"
			+ ", " + SMTablebids.screatedfromordernumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedfromordernumber) + "'"
			+ ", " + SMTablebids.lsalesgroupid + " = " + m_lsalesgroupid
			+ " WHERE ("
			+ SMTablebids.lid + " = " + m_sid
			+ ")"
			;
		}
		try{
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
				return false;
			}
		}catch(SQLException ex){
			super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
			return false;
		}

		//Update the ID if it's an insert:
		if (m_sid.compareToIgnoreCase("-1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_sid = Long.toString(rs.getLong(1));
				}else {
					m_sid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				//SMUtilities.rollback_data_transaction(conn);
				super.addErrorMessage("Could not get last ID number - " + e.getMessage());
				return false;
			}
			//If something went wrong, we can't get the last ID:
			if (m_sid.compareToIgnoreCase("0") == 0){
				//SMUtilities.rollback_data_transaction(conn);
				super.addErrorMessage("Could not get last ID number.");
				return false;
			}
			//Update the last saved dates
			m_slastsaveddatoriginationdate = stripDateFromDateTimeString(m_datoriginationdate);
			m_slastsaveddattimebiddate = stripDateFromDateTimeString(m_dattimebiddate);
			m_slastsaveddatplansreceived = stripDateFromDateTimeString(m_datplansreceived);
			m_slastsaveddattakeoffcomplete = stripDateFromDateTimeString(m_dattakeoffcomplete);
			m_slastsaveddatpricecomplete = stripDateFromDateTimeString(m_datpricecomplete);
			m_slastsaveddattimeactualbiddate = stripDateFromDateTimeString(m_dattimeactualbiddate);

		}
		
		//Re-populate the 'created by' here so we don't get a blank when we re-display:
		SQL = "SELECT"
			+ " " + SMTablebids.screatedbyfullname
			+ ", " + SMTablebids.lcreatedbyuserid
			+ " FROM " + SMTablebids.TableName
			+ " WHERE ("
				+ SMTablebids.lid + " = " + m_sid
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_screatedbyfullname = rs.getString(SMTablebids.screatedbyfullname);
				m_lcreatedbyuserid = Long.toString(rs.getLong(SMTablebids.lcreatedbyuserid));
			}else{
				super.addErrorMessage("Could not read 'Created By' from saved " + SMBidEntry.ParamObjectName + ".");
				return false;
			}
			rs.close();
		} catch (SQLException e1) {
			super.addErrorMessage("Could not read 'Created By' from saved " + SMBidEntry.ParamObjectName + " - " + e1.getMessage() + ".");
			return false;		
		}
		
		//Insert the product type amounts, if there are any:
		for (int i = 0; i < m_arrBidProductTypeAmounts.size(); i++){
			String sBidProductID = getsProductTypeID(i).trim();
			String sBidProductAmount = getsProductTypeAmount(i).replace(",", "").trim();
			SQL = "INSERT INTO " + SMTablebidproductamounts.TableName + "(" 
			+ SMTablebidproductamounts.lBidID
			+ ", " + SMTablebidproductamounts.lBidProductTypeID
			+ ", " + SMTablebidproductamounts.bdAmount
			+ ") VALUES ("
			+ slid()
			+ ", " + Long.parseLong(sBidProductID)
			+ ", " + sBidProductAmount
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablebidproductamounts.bdAmount + " = " 
				+ sBidProductAmount
			;
			if (bDebugMode){
				System.out.println("[1579265715] Inserting product amounts - SQL = " + SQL);
			}
			try {
				clsDatabaseFunctions.executeSQL(SQL, conn);
			} catch (SQLException e) {
				super.addErrorMessage("Error updating product amounts with SQL: " + SQL + " - " + e.getMessage());
				return false;
			}
		}
		return true;
	}


	public String slid (){
		return m_sid;
	}
	public boolean slid(String slid){
		try{
			m_sid = slid;
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}

	public boolean validate_entry_fields (Connection conn){
		//Validate the entries here:
		boolean bEntriesAreValid = true;
		//ID:
		long lID;
		try {
			lID = Long.parseLong(m_sid);
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		if ((lID < -1) || (lID == 0)){
			super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		//Salespersoncode
		m_ssalespersoncode = m_ssalespersoncode.trim();
		if (m_ssalespersoncode.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Salesperson code cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_ssalespersoncode.length() > SMTablebids.ssalespersoncodeLength){
			super.addErrorMessage("Salesperson code is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		//Sales group
		m_lsalesgroupid = m_lsalesgroupid.trim();
		if (m_lsalesgroupid.compareToIgnoreCase("0") == 0 
				|| m_lsalesgroupid.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Sales group cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		//Origination date:
		//REQUIRED FIELD - can't be a blank:
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datoriginationdate)){
			super.addErrorMessage("Origination date '" + m_datoriginationdate + "' is invalid.  ");
			bEntriesAreValid = false;
		}

		//Date and time:
		if (m_dattimebiddate.compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", m_dattimebiddate)){
				super.addErrorMessage("Date and time '" + m_dattimebiddate + "' is invalid.  ");
				bEntriesAreValid = false;
			}
		}

		//Customer name
		m_scustomername = m_scustomername.trim();
		if (m_scustomername.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Bill to name cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_scustomername.length() > SMTablebids.scustomernameLength){
			super.addErrorMessage("Bill to name is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		} 

		//Project Name
		m_sprojectname = m_sprojectname.trim();
		if (m_sprojectname.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Ship to name cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sprojectname.length() > SMTablebids.sprojectnameLength){
			super.addErrorMessage("Project name is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		//Date plans received:
		if (m_datplansreceived.compareTo(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datplansreceived)){
				super.addErrorMessage("Date plans received '" + m_datplansreceived + "' is invalid.  ");
				bEntriesAreValid = false;
			}        	
		}

		//Date takeoff complete:
		if (m_dattakeoffcomplete.compareTo(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_dattakeoffcomplete)){
				super.addErrorMessage("Takeoff complete date '" + m_dattakeoffcomplete + "' is invalid.  ");
				bEntriesAreValid = false;
			}        	
		}

		//Date price complete:
		if (m_datpricecomplete.compareTo(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datpricecomplete)){
				super.addErrorMessage("Price complete date '" + m_datpricecomplete + "' is invalid.  ");
				bEntriesAreValid = false;
			}        	
		}

		//Description
		m_mdescription = m_mdescription.trim();

		//Actual date and time:
		if (m_dattimeactualbiddate.compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", m_dattimeactualbiddate)){
				super.addErrorMessage("Date and time '" + m_dattimeactualbiddate + "' is invalid.  ");
				bEntriesAreValid = false;
			}
		}

		// m_scontactname 
		m_scontactname = m_scontactname.trim();
		if (m_scontactname.length() > SMTablebids.scontactnameLength){
			super.addErrorMessage("Contact name is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_sphonenumber 
		m_sphonenumber = m_sphonenumber.trim();
		if (m_sphonenumber.length() > SMTablebids.sphonenumberLength){
			super.addErrorMessage("Phone number is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_emailaddress
		m_emailaddress = m_emailaddress.trim();
		if (m_emailaddress.length() > SMTablebids.emailaddressLength){
			super.addErrorMessage("Email address is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_sstatus 
		m_sstatus = m_sstatus.trim();
		if (m_sstatus.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Status cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sstatus.length() > SMTablebids.sstatusLength){
			super.addErrorMessage("Status is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_dapproximateamount 
		m_dapproximateamount = m_dapproximateamount.replace(",", "");
		try{
			BigDecimal bdTest = new BigDecimal(m_dapproximateamount);
			if (bdTest.compareTo(BigDecimal.ZERO) < 0){
				super.addErrorMessage("Approximate amount cannot be negative: " + m_dapproximateamount + ".  ");
				bEntriesAreValid = false;
			}
		}catch(NumberFormatException e){
			super.addErrorMessage("Invalid approximate amount: '" + m_dapproximateamount + "'.  ");
			bEntriesAreValid = false;
		}

		// m_iprojecttype 
		m_iprojecttype = m_iprojecttype.trim();
		if (m_iprojecttype.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Project type cannot be blank.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_screatedby 
		m_screatedbyfullname = m_screatedbyfullname.trim();
		if (m_screatedbyfullname.length() > SMTablebids.sCreatedByLength){
			super.addErrorMessage("Created by name is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}        

		// m_saltphonenumber 
		m_saltphonenumber = m_saltphonenumber.trim();
		if (m_saltphonenumber.length() > SMTablebids.saltphonenumberLength){
			super.addErrorMessage("Alternate phone number is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_sfaxnumber 
		m_sfaxnumber = m_sfaxnumber.trim();
		if (m_sfaxnumber.length() > SMTablebids.sfaxnumberLength){
			super.addErrorMessage("Fax number is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// m_sshiptoaddress1 
		m_sshiptoaddress1 = m_sshiptoaddress1.trim();
		if (m_sshiptoaddress1.length() > SMTablebids.sshiptoaddress1Length){
			super.addErrorMessage("Ship address line 1 is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// m_sshiptoaddress2 
		m_sshiptoaddress2 = m_sshiptoaddress2.trim();
		if (m_sshiptoaddress2.length() > SMTablebids.sshiptoaddress2Length){
			super.addErrorMessage("Ship address line 2 is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// m_sshiptoaddress3 
		m_sshiptoaddress3 = m_sshiptoaddress3.trim();
		if (m_sshiptoaddress3.length() > SMTablebids.sshiptoaddress3Length){
			super.addErrorMessage("Ship address line 3 is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// m_sshiptoaddress4 
		m_sshiptoaddress4 = m_sshiptoaddress4.trim();
		if (m_sshiptoaddress4.length() > SMTablebids.sshiptoaddress4Length){
			super.addErrorMessage("Ship address line 4 is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		
		// m_sshiptocity
		m_sshiptocity = m_sshiptocity.trim();
		if (m_sshiptocity.length() > SMTablebids.sshiptocityLength){
			super.addErrorMessage("Ship to city is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_sshiptostate
		m_sshiptostate = m_sshiptostate.trim();
		if (m_sshiptostate.length() > SMTablebids.sshiptostateLength){
			super.addErrorMessage("Ship to state is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// m_sshiptozip
		m_sshiptozip = m_sshiptozip.trim();
		if (m_sshiptozip.length() > SMTablebids.sshiptozipLength){
			super.addErrorMessage("Ship to zip is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		//Sales contact ID:
		setisalescontactid(getisalescontactid().trim());
		if (getisalescontactid().compareToIgnoreCase("") == 0){
			setisalescontactid("0");
		}else{
			long lSalesContactID = 0;
			try {
				lSalesContactID = Long.parseLong(getisalescontactid());
			} catch (NumberFormatException e) {
				super.addErrorMessage("Invalid sales contact ID: '" + getisalescontactid() + "'.");
				bEntriesAreValid = false;
				return bEntriesAreValid;
			}
	
			if (lSalesContactID < 0){
				super.addErrorMessage("Invalid sales contact ID: '" + getisalescontactid() + "'.");
				bEntriesAreValid = false;
			}
			String SQL = "SELECT"
				+ " " + SMTablesalescontacts.id
				+ " FROM " + SMTablesalescontacts.TableName
				+ " WHERE ("
					+ "(" + SMTablesalescontacts.id + " = " + getisalescontactid() + ")"
				+ ")"
			;
			try {
				ResultSet rsSalesContacts = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (!rsSalesContacts.next()){
					super.addErrorMessage("Sales contact ID: '" + getisalescontactid() + "' not found.");
					bEntriesAreValid = false;
				}
				rsSalesContacts.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error [1396986883] reading sales contacts to verify ID - " + e.getMessage());
				bEntriesAreValid = false;
			}
		}
		//Validate the product amounts:
		for (int i = 0; i < m_arrBidProductTypeAmounts.size(); i++){
			if (bDebugMode){
				System.out.println("[1579265721] In " + this.toString() 
					+ " validating product amounts, amount = " + getsProductTypeAmount(i).trim());
			}
			try {
				@SuppressWarnings("unused")
				BigDecimal bdTest = new BigDecimal(getsProductTypeAmount(i).replace(",", "").trim());
			} catch (Exception e) {
				super.addErrorMessage(
						"Invalid product amount for " + getsProductType(i).trim()
						 + ": " + getsProductTypeAmount(i).trim() + ".");
				bEntriesAreValid = false;
			}
		}

		return bEntriesAreValid;
	}

	public String read_out_debug_data(){

		String sResult = "  ** SMBidEntry read out: ";
		sResult += "\nID: " + m_sid;
		sResult += "\nSalesperson code: " + this.getssalespersoncode();
		sResult += "\nOrigination date: " + this.getdatoriginationdate();
		sResult += "\nBid Date: " + this.getdattimebiddate();
		sResult += "\nBill-to name: " + this.getscustomername();
		sResult += "\nShip-to name: " + this.getsprojectname();
		sResult += "\nShip-to address 1: " + this.getsshiptoaddress1();
		sResult += "\nShip-to address 2: " + this.getsshiptoaddress2();
		sResult += "\nShip-to address 3: " + this.getsshiptoaddress3();
		sResult += "\nShip-to address 4: " + this.getsshiptoaddress4();
		sResult += "\nShip-to city: " + this.getsshiptocity();
		sResult += "\nShip-to state: " + this.getsshiptostate();
		sResult += "\nShip-to zip: " + this.getsshiptozip();
		sResult += "\nPlan received date: " + this.getdatplansreceived();
		sResult += "\nTake off date: " + this.getdattakeoffcomplete();
		sResult += "\nPrice complete date: " + this.m_datpricecomplete;
		sResult += "\nDescription: " + this.getmdescription();
		sResult += "\nActual date: " + this.m_dattimeactualbiddate;
		sResult += "\nContact name: " + this.getscontactname();
		sResult += "\nPhone number: " + this.getsphonenumber();
		sResult += "\nEmail: " + this.getemailaddress();
		sResult += "\nStatus: " + this.getsstatus();
		sResult += "\nApproximate amt: " + this.getdapproximateamount();
		sResult += "\nProject type: " + this.getiprojecttype();
//		sResult += "\nBin number: " + this.getsbinnumber();
		sResult += "\nCreated by: " + this.getscreatedbyfullname();
		sResult += "\nObject name: " + this.getObjectName();
		sResult += "\nAlternate Phone number: " + this.getsaltphonenumber();
		sResult += "\nFax number: " + this.getsfaxnumber();
		sResult += "\nMarketing source: " + this.getiordersourceid();
		sResult += "\nMarketing source desc: " + this.getsordersourcedesc();
		sResult += "\nGDoc Link: " + this.getsgdoclink();
		sResult += "\nSales Contact: " + this.getisalescontactid();
		sResult += "\nTakeoff person: " + this.getstakeoffpersoncode();
		sResult += "\nPricing person: " + this.getspricingpersoncode();
		for (int i = 0; i < m_arrBidProductTypeAmounts.size(); i++){
			sResult += "\nProduct amount for product ID " + getsProductTypeID(i).trim()
			+ " - " + getsProductTypeID(i).trim() + ": " + getsProductTypeAmount(i).trim(); 
		}

		return sResult;
	}

	public void addErrorMessage(String sMsg){
		super.addErrorMessage(sMsg);
	}

	public String getssalespersoncode() {
		return m_ssalespersoncode;
	}

	public void setssalespersoncode(String m_ssalespersoncode) {
		this.m_ssalespersoncode = m_ssalespersoncode;
	}

	public String getstakeoffpersoncode() {
		return m_stakeoffpersoncode;
	}

	public void setstakeoffpersoncode(String stakeoffpersoncode) {
		this.m_stakeoffpersoncode = stakeoffpersoncode;
	}
	
	public String getspricingpersoncode() {
		return m_spricingpersoncode;
	}

	public void setspricingpersoncode(String spricingpersoncode) {
		this.m_spricingpersoncode = spricingpersoncode;
	}
	
	public String getdatoriginationdate() {
		return m_datoriginationdate;
	}

	public void setdatoriginationdate(String m_datoriginationdate) {
		this.m_datoriginationdate = m_datoriginationdate;
	}

	public String getdattimebiddate() {
		return m_dattimebiddate;
	}

	public void setdattimebiddate(String m_dattimebiddate) {
		this.m_dattimebiddate = m_dattimebiddate;
	}

	public String getscustomername() {
		return m_scustomername;
	}

	public void setscustomername(String m_scustomername) {
		this.m_scustomername = m_scustomername;
	}

	public String getsprojectname() {
		return m_sprojectname;
	}

	public void setsprojectname(String m_sprojectname) {
		this.m_sprojectname = m_sprojectname;
	}

	public String getdatplansreceived() {
		return m_datplansreceived;
	}

	public void setdatplansreceived(String m_datplansreceived) {
		this.m_datplansreceived = m_datplansreceived;
	}

	public String getdattakeoffcomplete() {
		return m_dattakeoffcomplete;
	}

	public void setdattakeoffcomplete(String m_dattakeoffcomplete) {
		this.m_dattakeoffcomplete = m_dattakeoffcomplete;
	}

	public String getdatpricecomplete() {
		return m_datpricecomplete;
	}

	public void setdatpricecomplete(String m_datpricecomplete) {
		this.m_datpricecomplete = m_datpricecomplete;
	}

	public String getmdescription() {
		return m_mdescription;
	}

	public void setmdescription(String m_mdescription) {
		this.m_mdescription = m_mdescription;
	}

	public String getdattimeactualbiddate() {
		return m_dattimeactualbiddate;
	}

	public void setdattimeactualbiddate(String m_dattimeactualbiddate) {
		this.m_dattimeactualbiddate = m_dattimeactualbiddate;
	}

	public String getscontactname() {
		return m_scontactname;
	}

	public void setscontactname(String m_scontactname) {
		this.m_scontactname = m_scontactname;
	}

	public String getsphonenumber() {
		return m_sphonenumber;
	}

	public void setsphonenumber(String m_sphonenumber) {
		this.m_sphonenumber = m_sphonenumber;
	}

	public String getemailaddress() {
		return m_emailaddress;
	}

	public void setemailaddress(String m_emailaddress) {
		this.m_emailaddress = m_emailaddress;
	}

	public String getsstatus() {
		return m_sstatus;
	}

	public void setsstatus(String m_sstatus) {
		this.m_sstatus = m_sstatus;
	}

	public String getdapproximateamount() {
		return m_dapproximateamount;
	}

	public void setdapproximateamount(String m_dapproximateamount) {
		this.m_dapproximateamount = m_dapproximateamount;
	}

	public String getiprojecttype() {
		return m_iprojecttype;
	}

	public void setiprojecttype(String m_iprojecttype) {
		this.m_iprojecttype = m_iprojecttype;
	}

	public String getlid(){
		return m_sid;
	}

	public void setlid(String sid) {
		this.m_sid = sid;
	}

	public String getscreatedbyfullname() {
		return m_screatedbyfullname;
	}

	public void setscreatedbyfullname(String m_screatedby) {
		this.m_screatedbyfullname = m_screatedby;
	}
	
	public String getlcreatedbyuserid() {
		return m_screatedbyfullname;
	}

	public void setlcreatedbyuserid(String m_lcreatedbyuserid) {
		this.m_lcreatedbyuserid = m_lcreatedbyuserid;
	}

	public String getdatcreatedtime() {
		if (m_datcreatedtime == null){
			return SMBidEntry.EMPTY_DATETIME_STRING;
		}else{
			return m_datcreatedtime;
		}
	}

	public void setdatcreatedtime(String m_datcreatedtime) {
		this.m_datcreatedtime = m_datcreatedtime;
	}

	public String getsaltphonenumber() {
		return m_saltphonenumber;
	}

	public void setsaltphonenumber(String m_saltphonenumber) {
		this.m_saltphonenumber = m_saltphonenumber;
	}

	public String getsfaxnumber() {
		return m_sfaxnumber;
	}

	public String getiordersourceid() {
		return m_iordersourceid;
	}

	public String getsordersourcedesc() {
		return m_sordersourcedesc;
	}

	public void setsfaxnumber(String m_sfaxnumber) {
		this.m_sfaxnumber = m_sfaxnumber;
	}

	public String getsgdoclink() {
		return m_sgdoclink;
	}

	public void setsgdoclink(String m_sgdoclink) {
		this.m_sgdoclink = m_sgdoclink;
	}

	public void setisalescontactid(String s) {
		m_isalescontactid = s;
	}

	public String getisalescontactid() {
		return m_isalescontactid;
	}
	
	public String getsshiptoaddress1() {
		return m_sshiptoaddress1;
	}

	public void setsshiptoaddress1(String m_sshiptoaddress1) {
		this.m_sshiptoaddress1 = m_sshiptoaddress1;
	}
	
	public String getsshiptoaddress2() {
		return m_sshiptoaddress2;
	}

	public void setsshiptoaddress2(String m_sshiptoaddress2) {
		this.m_sshiptoaddress2 = m_sshiptoaddress2;
	}
	
	public String getsshiptoaddress3() {
		return m_sshiptoaddress3;
	}

	public void setsshiptoaddress3(String m_sshiptoaddress3) {
		this.m_sshiptoaddress3 = m_sshiptoaddress3;
	}
	
	public String getsshiptoaddress4() {
		return m_sshiptoaddress4;
	}

	public void setsshiptoaddress4(String m_sshiptoaddress4) {
		this.m_sshiptoaddress4 = m_sshiptoaddress4;
	}
	
	public String getsshiptocity() {
		return m_sshiptocity;
	}

	public void setsshiptocity(String m_sshiptocity) {
		this.m_sshiptocity = m_sshiptocity;
	}
	
	public String getsshiptostate() {
		return m_sshiptostate;
	}

	public void setsshiptostate(String m_sshiptostate) {
		this.m_sshiptostate = m_sshiptostate;
	}
	
	public String getsshiptozip() {
		return m_sshiptozip;
	}

	public void setsshiptozip(String m_sshiptozip) {
		this.m_sshiptozip= m_sshiptozip;
	}
	
	public String getscreatedfromordernumber() {
		return m_screatedfromordernumber;
	}

	public void setscreatedfromordernumber(String screatedfromordernumber) {
		this.m_screatedfromordernumber= screatedfromordernumber;
	}
	
	public String getlsalesgroupid() {
		return m_lsalesgroupid;
	}

	public void setlsalesgroupid(String lsalesgroupid) {
		this.m_lsalesgroupid = lsalesgroupid;
	}

	//Get the last saved dates:
	//Update the last saved dates
	public String getslastsaveddatoriginationdate(){
		return m_slastsaveddatoriginationdate;
	}
	public String getslastsaveddattimebiddate(){
		return m_slastsaveddattimebiddate;
	}
	public String getslastsaveddatplansreceived(){
		return m_slastsaveddatplansreceived;
	}
	public String getslastsaveddattakeoffcomplete(){
		return m_slastsaveddattakeoffcomplete;
	}
	public String getslastsaveddatpricecomplete(){
		return m_slastsaveddatpricecomplete;
	}
	public String getslastsaveddattimeactualbiddate(){
		return m_slastsaveddattimeactualbiddate;
	}
	
	public int getsProductTypeAmountsSize(){
		return m_arrBidProductTypeAmounts.size();
	}
	public String getsProductType(int iArrayIndex){
		return m_arrBidProductTypeAmounts.get(iArrayIndex).substring(6, SMTablebidproducttypes.sProductTypeLength + 6);
	}
	public String getsProductTypeID(int iArrayIndex){
		return m_arrBidProductTypeAmounts.get(iArrayIndex).substring(0, 6);
	}
	public String getsProductTypeAmount(int iArrayIndex){
		return m_arrBidProductTypeAmounts.get(iArrayIndex).substring(
			6 + SMTablebidproducttypes.sProductTypeLength, 6 + SMTablebidproducttypes.sProductTypeLength + 20);
	}
	public String getsProductTypeAmountArrayItem(int iArrayIndex){
		return m_arrBidProductTypeAmounts.get(iArrayIndex);
	}
	
	private String stripDateFromDateTimeString(String sDateTimeString){
        if (sDateTimeString.startsWith(SMBidEntry.EMPTY_DATE_STRING)){
        	return clsDateAndTimeConversions.now("M/d/yyyy");
        }
		if (sDateTimeString.indexOf(" ") == -1){
			return sDateTimeString;
		}
		return sDateTimeString.substring(0, sDateTimeString.indexOf(" "));
	}
	
	private void initBidVariables(){
		m_sid = "-1";
		m_ssalespersoncode = "";
		m_datoriginationdate = clsDateAndTimeConversions.now("M/d/yyyy");
		m_dattimebiddate = clsDateAndTimeConversions.now("M/d/yyyy hh:mm a");
		m_scustomername = "";
		m_sprojectname = "";
		m_datplansreceived = clsDateAndTimeConversions.now("M/d/yyyy");
		m_dattakeoffcomplete = clsDateAndTimeConversions.now("M/d/yyyy");
		m_datpricecomplete = clsDateAndTimeConversions.now("M/d/yyyy");
		m_mdescription = "";
		m_dattimeactualbiddate = clsDateAndTimeConversions.now("M/d/yyyy hh:mm a");
		m_scontactname = "";
		m_sphonenumber = "";
		m_emailaddress = "";
		m_sstatus = SMTablebids.STATUS_PENDING;
		m_dapproximateamount = "0.00";
		m_iprojecttype = "";
		m_screatedbyfullname = "";
		m_lcreatedbyuserid = "";
		m_saltphonenumber = "";
		m_sfaxnumber = "";
		m_iordersourceid = "0";
		m_sordersourcedesc = "";
		m_sgdoclink = "";
		m_isalescontactid = "";
		m_sshiptoaddress1 = "";
		m_sshiptoaddress2 = "";
		m_sshiptoaddress3 = "";
		m_sshiptoaddress4 = "";
		m_sshiptocity = "";
		m_sshiptostate = "";
		m_sshiptozip = "";
		m_stakeoffpersoncode = "";
		m_spricingpersoncode = "";
		m_screatedfromordernumber = "";
		m_lsalesgroupid = "0";
		
		m_slastsaveddatoriginationdate = m_datoriginationdate;
		m_slastsaveddattimebiddate = m_dattimebiddate;
		m_slastsaveddatplansreceived = m_datplansreceived;
		m_slastsaveddattakeoffcomplete = m_dattakeoffcomplete;
		m_slastsaveddatpricecomplete = m_datpricecomplete;
		m_slastsaveddattimeactualbiddate = m_dattimeactualbiddate;
		
		m_arrBidProductTypeAmounts = new ArrayList<String>(0);

		super.initVariables();
		super.setObjectName(ParamObjectName);
	}
}