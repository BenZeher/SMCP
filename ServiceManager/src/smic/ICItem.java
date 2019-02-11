package smic;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smar.*;

public class ICItem extends Object{
	public static final String ParamAddingNewRecord = "bAddingNewRecord";
	public static final String ParamItemNumber = "ItemNumber";
	public static final String ParamItemDescription = "ItemDescription";
	public static final String ParamLastMaintainedDate = "LastMaintainedDate";
	public static final String ParamActive = "Active";
	public static final String ParamCategoryCode = "CategoryCode";
	public static final String ParamCostUnitOfMeasure = "CostUnitOfMeasure";
	public static final String ParamDefaultPriceListCode = "DefaultPriceListCode";
	public static final String ParamPickingSequence = "PickingSequence";
	public static final String ParamInactiveDate = "InactiveDate";
	public static final String ParamComment1 = "Comment1";
	public static final String ParamComment2 = "Comment2";
	public static final String ParamComment3 = "Comment3";
	public static final String ParamComment4 = "Comment4";
	public static final String ParamLastEditUserFullName = "LastEditUserFullName";
	public static final String ParamTaxable = "Taxable";
	public static final String ParamDedicatedToOrderNumber = "DedicatedToOrderNumber";
	public static final String ParamReportGroup1 = "ReportGroup1";
	public static final String ParamReportGroup2 = "ReportGroup2";
	public static final String ParamReportGroup3 = "ReportGroup3";
	public static final String ParamReportGroup4 = "ReportGroup4";
	public static final String ParamReportGroup5 = "ReportGroup5";
	public static final String ParamMostRecentCost = "MostRecentCost";
	public static final String ParamLaborItem = "LaborItem";
	public static final String ParamNonStockItem = "NonStockItem";
	public static final String ParamNumberOfLabels = "NumberOfLabels";
	public static final String ParamSuppressItemQtyLookup = "SuppressItemQtyLookup";
	public static final String ParamHideOnInvoiceDefault = "HideOnInvoiceDefault";
	public static final String Paramsworkordercomment = "sworkordercomment";
	public static final String Paramicannotbepurchased = "icannotbepurchased";
	public static final String Paramicannotbesold = "icannotbesold";
	
	//This is the length of the prefix that is used in the item number when a new item is created from the order entry system:
	public static final int DEDICATEDITEMPREFIXLENGTH = 3;

	private String m_sItemNumber;
	private String m_sItemDescription;
	private String m_sLastMaintainedDate;
	private String m_sActive;
	private String m_sCategoryCode;
	private String m_sCostUnitOfMeasure;
	private String m_sDefaultPriceListCode;
	private String m_sPickingSequence;
	private String m_sInactiveDate;
	private String m_sComment1;
	private String m_sComment2;
	private String m_sComment3;
	private String m_sComment4;
	private String m_sLastEditUserFullName;
	private String m_sTaxable;
	private String m_sDedicatedToOrderNumber;
	private String m_sReportGroup1;
	private String m_sReportGroup2;
	private String m_sReportGroup3;
	private String m_sReportGroup4;
	private String m_sReportGroup5;
	private String m_sMostRecentCost;
	private String m_sLaborItem;
	private String m_sNonStockItem;
	private String m_sNumberOfLabels;
	private String m_sNewRecord;
	private String m_sSuppressItemQtyLookup;
	private String m_sHideOnInvoiceDefault;
	private String m_sworkordercomment;
	private String m_icannotbepurchased;
	private String m_icannotbesold;
	private boolean bDebugMode = false;

	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

	public ICItem(
			String sItemNumber
	) {
		m_sItemNumber = sItemNumber;
		m_sItemDescription = "";
		m_sLastMaintainedDate = clsDateAndTimeConversions.now("MM/dd/yyyy");;
		m_sActive = "1";
		m_sCategoryCode = "";
		m_sCostUnitOfMeasure = "";
		m_sDefaultPriceListCode = "";
		m_sPickingSequence = "";
		m_sInactiveDate = "00/00/0000";
		m_sComment1 = "";
		m_sComment2 = "";
		m_sComment3 = "";
		m_sComment4 = "";
		m_sNewRecord = "1";
		m_sLastEditUserFullName = "";
		m_sTaxable = "1";
		m_sDedicatedToOrderNumber = "";
		m_sReportGroup1 = "";
		m_sReportGroup2 = "";
		m_sReportGroup3 = "";
		m_sReportGroup4 = "";
		m_sReportGroup5 = "";
		m_sMostRecentCost = "0.0000";
		m_sLaborItem = "0";
		m_icannotbepurchased = "0";
		m_icannotbesold = "0";
		m_sNonStockItem = "0";
		m_sNumberOfLabels = "1.0000";
		m_sSuppressItemQtyLookup = "0";
		m_sHideOnInvoiceDefault = "0";
		m_sworkordercomment = "";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}

	public void loadFromHTTPRequest(HttpServletRequest req){

		m_sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamItemNumber, req).trim().replace(" ", "").replace("&quot;", "\"");
		m_sItemDescription = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamItemDescription, req).trim().replace("&quot;", "\"");
		m_sLastMaintainedDate = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamLastMaintainedDate, req).trim().replace("&quot;", "\"");
		if(m_sLastMaintainedDate.compareToIgnoreCase("") == 0){
			m_sLastMaintainedDate = "00/00/0000";
		}
		if(req.getParameter(ICItem.ParamActive) == null){
			m_sActive = "0";
		}else{
			m_sActive = "1";
		}
		m_sCategoryCode = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamCategoryCode, req).trim().replace("&quot;", "\"");
		m_sCostUnitOfMeasure = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamCostUnitOfMeasure, req).trim().replace("&quot;", "\"");
		m_sDefaultPriceListCode = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamDefaultPriceListCode, req).trim().replace("&quot;", "\"");
		m_sPickingSequence = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamPickingSequence, req).trim().replace("&quot;", "\"");
		m_sInactiveDate = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamInactiveDate, req).trim().replace("&quot;", "\"");
		if(m_sInactiveDate.compareToIgnoreCase("") == 0){
			m_sInactiveDate = "00/00/0000";
		}
		m_sComment1 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamComment1, req).trim().replace("&quot;", "\"");
		m_sComment2 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamComment2, req).trim().replace("&quot;", "\"");
		m_sComment3 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamComment3, req).trim().replace("&quot;", "\"");
		m_sComment4 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamComment4, req).trim().replace("&quot;", "\"");
		m_sDedicatedToOrderNumber = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamDedicatedToOrderNumber, req).trim().replace("&quot;", "\"");
		m_sReportGroup1 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamReportGroup1, req).trim().replace("&quot;", "\"");
		m_sReportGroup2 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamReportGroup2, req).trim().replace("&quot;", "\"");
		m_sReportGroup3 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamReportGroup3, req).trim().replace("&quot;", "\"");
		m_sReportGroup4 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamReportGroup4, req).trim().replace("&quot;", "\"");
		m_sReportGroup5 = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamReportGroup5, req).trim().replace("&quot;", "\"");
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamAddingNewRecord, req).trim().replace("&quot;", "\"");
		m_sMostRecentCost = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamMostRecentCost, req).trim().replace("&quot;", "\"");
		m_sLastEditUserFullName = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamLastEditUserFullName, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ICItem.ParamTaxable) == null){
			m_sTaxable = "0";
		}else{
			m_sTaxable = "1";
		}
		if(req.getParameter(ICItem.ParamLaborItem) == null){
			m_sLaborItem = "0";
		}else{
			m_sLaborItem = "1";
		}
		if(req.getParameter(ICItem.Paramicannotbepurchased) == null) {
			m_icannotbepurchased = "0";
		}else {
			m_icannotbepurchased = "1";
		}
		if(req.getParameter(ICItem.Paramicannotbesold) == null) {
			m_icannotbesold = "0";
		}else {
			m_icannotbesold = "1";
		}
		if(req.getParameter(ICItem.ParamNonStockItem) == null){
			m_sNonStockItem = "0";
		}else{
			m_sNonStockItem = "1";
		}
		m_sNumberOfLabels = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamNumberOfLabels, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ICItem.ParamSuppressItemQtyLookup) == null){
			m_sSuppressItemQtyLookup = "0";
		}else{
			m_sSuppressItemQtyLookup = "1";
		}		
		if(req.getParameter(ICItem.ParamHideOnInvoiceDefault) == null){
			m_sHideOnInvoiceDefault = "0";
		}else{
			m_sHideOnInvoiceDefault = "1";
		}
		m_sworkordercomment = clsManageRequestParameters.get_Request_Parameter(ICItem.Paramsworkordercomment, req).trim().replace("&quot;", "\"");
	}
	private boolean load(
			String sItemNumber,
			ServletContext context, 
			String sDBID
	){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ SMTableicitems.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".load");
			if (loadFromResultSet(rs)){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		}catch (SQLException ex){
			m_sErrorMessageArray.add("Error [1519147880] - couldn't load item = " + ex.getMessage());
			return false;
		}
	}
	//Need this one with the connection:
	private boolean load(
			String sItemNumber,
			Connection conn
	){
		m_sErrorMessageArray.clear();
		String sSQL = "";
		try{
			//Get the record to edit:
			sSQL = "SELECT * FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ SMTableicitems.sItemNumber + " = '" + sItemNumber + "'"
			+	")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (loadFromResultSet(rs)){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}

		}catch (SQLException ex){
			m_sErrorMessageArray.add("Error [1549305132] - couldn't load item with SQL: '" + sSQL + "' - " + ex.getMessage());
			return false;
		}
	}

	private boolean loadFromResultSet(ResultSet rs){
		
		try{
			if (rs.next()){
				m_sItemNumber = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sItemNumber));
				m_sItemDescription = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sItemDescription));
				String sDate = rs.getString(SMTableicitems.datLastMaintained);
				m_sLastMaintainedDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				m_sActive = Integer.toString(rs.getInt(SMTableicitems.iActive));
				m_sCategoryCode = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sCategoryCode));
				m_sCostUnitOfMeasure = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sCostUnitOfMeasure));
				m_sDefaultPriceListCode = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sDefaultPriceListCode));
				m_sPickingSequence = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sPickingSequence));
				sDate = rs.getString(SMTableicitems.datInactive);
				m_sInactiveDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				m_sComment1 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sComment1));
				m_sComment2 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sComment2));
				m_sComment3 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sComment3));
				m_sComment4 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sComment4));
				m_sLastEditUserFullName = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sLastEditUserFullName));
				m_sDedicatedToOrderNumber = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sDedicatedToOrderNumber));
				m_sReportGroup1 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sreportgroup1));
				m_sReportGroup2 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sreportgroup2));
				m_sReportGroup3 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sreportgroup3));
				m_sReportGroup4 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sreportgroup4));
				m_sReportGroup5 = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sreportgroup5));
				m_sTaxable = Integer.toString(rs.getInt(SMTableicitems.iTaxable));
				m_sMostRecentCost = clsManageBigDecimals.BigDecimalToFormattedString("########0.0000",
						rs.getBigDecimal(SMTableicitems.bdmostrecentcost));
				m_sLaborItem = Integer.toString(rs.getInt(SMTableicitems.ilaboritem));
				m_icannotbepurchased = Integer.toString(rs.getInt(SMTableicitems.icannotbepurchased));
				m_icannotbesold = Integer.toString(rs.getInt(SMTableicitems.icannotbesold));
				m_sNonStockItem = Integer.toString(rs.getInt(SMTableicitems.inonstockitem));
				m_sNumberOfLabels = clsManageBigDecimals.BigDecimalToFormattedString("########0.0000",
						rs.getBigDecimal(SMTableicitems.bdnumberoflabels));
				m_sNewRecord = "0";
				m_sSuppressItemQtyLookup = Integer.toString(rs.getInt(SMTableicitems.isuppressitemqtylookup));
				m_sHideOnInvoiceDefault = Integer.toString(rs.getInt(SMTableicitems.ihideoninvoicedefault));
				m_sworkordercomment = ARUtilities.checkStringForNull(rs.getString(SMTableicitems.sworkordercomment));
				rs.close();
				return true;
			}
			else{
				rs.close();
				return false;
			}
		}catch(SQLException ex){
			m_sErrorMessageArray.add("Error [1549305232] - couldn't load item - " + ex.getMessage());
			return false;
		}
	}
	public boolean load(
			ServletContext context, 
			String sDBID
	){

		return load(m_sItemNumber, context, sDBID);
	}
	public boolean load(
			Connection conn
	){

		return load(m_sItemNumber, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save (String sUserFullName, String sUserID, Connection conn){
		m_sErrorMessageArray.clear();
		
		//First, validate the item code:
		if (!validateNewCode()){
			return false;
		}
		
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableicitems.TableName
		+ " WHERE ("
		+ SMTableicitems.sItemNumber + " = '" + m_sItemNumber + "'"
		+ ")"
		;

		//We'll use this variable to see if the item is being changed to a 'non-stock' item:
		int iCurrentlyNonStockItem = 0;
		//We'll use this variable to see if the item is being changed to an 'inactive' item:
		int iCurrentlyActiveItem = 0;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

			//Update the record:
			String sInactiveDate = "";
			if(m_sActive.compareToIgnoreCase("1") == 0){
				sInactiveDate = "0000-00-00";
				m_sInactiveDate = "00/00/0000";
			}else{
				try {
					sInactiveDate = clsDateAndTimeConversions.utilDateToString(
							clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sInactiveDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					m_sErrorMessageArray.add("Error:[1423844687] Invalid inactive date: '"
						+ m_sInactiveDate + "' - " + e.getMessage());
					return false;
				}
			}

			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - item already exists.");
					rs.close();
					return false;
				}
				//Remember whether this item was a non-stock item before we saved:
				iCurrentlyNonStockItem = rs.getInt(SMTableicitems.inonstockitem);
				//Remember whether this item was an active item before we saved:
				iCurrentlyActiveItem = rs.getInt(SMTableicitems.iActive);
				
				rs.close();

				//If we are changing this item from a stock item to a non-stock item, we have
				//to verify that there are no costs, entries, etc., preventing the change:
				if (
						(iCurrentlyNonStockItem == 0)
						&& (getNonStockItem().compareToIgnoreCase("1") == 0)
				){
					if (!permitChangeToNonStock(conn)){
						return false;
					}else{
						//Remove all the item statistics for this item, because we don't want to carry
						//any of those for a 'non-stock' item:
						if (!removeProcessingHistory(conn)){
							return false;
						}
					}
				}

				//If we are changing this item from an active item to an inactive item, we have
				//to verify that there are no costs, entries, etc., preventing the change:
				if (
						(iCurrentlyActiveItem == 1)
						&& (getActive().compareToIgnoreCase("0") == 0)
				){
					if (!permitChangeToInactive(conn)){
						return false;
					}
				}

				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				setLastMaintainedDate(clsDateAndTimeConversions.nowSqlFormat());
				
				SQL = "UPDATE " + SMTableicitems.TableName + " SET "
				+ SMTableicitems.datInactive + " = '" + sInactiveDate + "'"
				+ ", " + SMTableicitems.datLastMaintained + " = '" + m_sLastMaintainedDate + "'"
				+ ", " + SMTableicitems.iActive + " = " + m_sActive
				+ ", " + SMTableicitems.ilaboritem + " = " + m_sLaborItem
				+ ", " + SMTableicitems.icannotbepurchased + " = " + m_icannotbepurchased
				+ ", " + SMTableicitems.icannotbesold + " = " + m_icannotbesold
				+ ", " + SMTableicitems.inonstockitem + " = " + m_sNonStockItem
				+ ", " + SMTableicitems.isuppressitemqtylookup + " = " + m_sSuppressItemQtyLookup
				+ ", " + SMTableicitems.ihideoninvoicedefault + " = " + m_sHideOnInvoiceDefault
				+ ", " + SMTableicitems.bdnumberoflabels + " = " + m_sNumberOfLabels.replace(",", "")
				+ ", " + SMTableicitems.iTaxable + " = " + m_sTaxable
				+ ", " + SMTableicitems.sCategoryCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCategoryCode) + "'"
				+ ", " + SMTableicitems.sComment1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment1) + "'"
				+ ", " + SMTableicitems.sComment2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment2) + "'"
				+ ", " + SMTableicitems.sComment3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment3) + "'"
				+ ", " + SMTableicitems.sComment4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment4) + "'"
				+ ", " + SMTableicitems.sDefaultPriceListCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDefaultPriceListCode) + "'"
				+ ", " + SMTableicitems.sItemDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemDescription) + "'"
				+ ", " + SMTableicitems.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				+ ", " + SMTableicitems.lLastEditUserID + " = " + sUserID + ""
				+ ", " + SMTableicitems.sPickingSequence + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPickingSequence) + "'"
				+ ", " + SMTableicitems.sCostUnitOfMeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCostUnitOfMeasure) + "'"
				+ ", " + SMTableicitems.sDedicatedToOrderNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDedicatedToOrderNumber) + "'"
				+ ", " + SMTableicitems.sreportgroup1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup1) + "'"
				+ ", " + SMTableicitems.sreportgroup2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup2) + "'"
				+ ", " + SMTableicitems.sreportgroup3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup3) + "'"
				+ ", " + SMTableicitems.sreportgroup4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup4) + "'"
				+ ", " + SMTableicitems.sreportgroup5 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup5) + "'"
				+ ", " + SMTableicitems.sworkordercomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sworkordercomment) + "'"
				+ ", " + SMTableicitems.bdmostrecentcost + " = " + m_sMostRecentCost.replace(",", "")

				+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemNumber) + "'"
				+ ")"
				;
				setLastMaintainedDate(m_sLastMaintainedDate.substring(5, 7) + "/" + m_sLastMaintainedDate.substring(8, 10) + "/" + m_sLastMaintainedDate.substring(0, 4));
				if (bDebugMode){
					clsServletUtilities.sysprint(this.toString(), sUserFullName, "UPDATE SQL = " + SQL);
				}
				try{
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				}catch (SQLException e){
					m_sErrorMessageArray.add("Cannot execute UPDATE sql - " + e.getMessage() + ".");
					return false;
				}
				m_sNewRecord = "0";
				return true;
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing item.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:

				if(!validateEntries()){
					return false;
				}

				if (!clsDatabaseFunctions.start_data_transaction(conn)){
					m_sErrorMessageArray.add("Could not start data transaction for item insert.");
					return false;
				}

				SQL = "INSERT INTO " + SMTableicitems.TableName + " ("
				+ SMTableicitems.datInactive
				+ ", " + SMTableicitems.datLastMaintained
				+ ", " + SMTableicitems.iActive
				+ ", " + SMTableicitems.ilaboritem 
				+ ", " + SMTableicitems.icannotbepurchased
				+ ", " + SMTableicitems.icannotbesold
				+ ", " + SMTableicitems.inonstockitem
				+ ", " + SMTableicitems.isuppressitemqtylookup
				+ ", " + SMTableicitems.ihideoninvoicedefault
				+ ", " + SMTableicitems.bdnumberoflabels
				+ ", " + SMTableicitems.iTaxable
				+ ", " + SMTableicitems.sCategoryCode
				+ ", " + SMTableicitems.sComment1
				+ ", " + SMTableicitems.sComment2
				+ ", " + SMTableicitems.sComment3
				+ ", " + SMTableicitems.sComment4
				+ ", " + SMTableicitems.sDefaultPriceListCode
				+ ", " + SMTableicitems.sItemDescription
				+ ", " + SMTableicitems.sItemNumber
				+ ", " + SMTableicitems.sLastEditUserFullName
				+ ", " + SMTableicitems.lLastEditUserID
				+ ", " + SMTableicitems.sPickingSequence
				+ ", " + SMTableicitems.sCostUnitOfMeasure
				+ ", " + SMTableicitems.sDedicatedToOrderNumber
				+ ", " + SMTableicitems.sreportgroup1
				+ ", " + SMTableicitems.sreportgroup2
				+ ", " + SMTableicitems.sreportgroup3
				+ ", " + SMTableicitems.sreportgroup4
				+ ", " + SMTableicitems.sreportgroup5
				+ ", " + SMTableicitems.sworkordercomment
				+ ", " + SMTableicitems.bdmostrecentcost

				+ " ) VALUES ("
				+ "'" + sInactiveDate + "'" 
				+ ", '" + clsDateAndTimeConversions.nowSqlFormat() + "'" 
				+ ", " + m_sActive
				+ ", " + m_sLaborItem
				+ ", " + m_icannotbepurchased
				+ ", " + m_icannotbesold
				+ ", " + m_sNonStockItem
				+ ", " + m_sSuppressItemQtyLookup
				+ ", " + m_sHideOnInvoiceDefault
				+ ", " + m_sNumberOfLabels.replace(",", "")
				+ ", " + m_sTaxable
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCategoryCode) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment1) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment2) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment3) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment4) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDefaultPriceListCode) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemNumber) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserFullName) + "'"
				+ ", " + sUserID + ""
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPickingSequence) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCostUnitOfMeasure) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDedicatedToOrderNumber) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup1) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup2) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup3) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup4) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sReportGroup5) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sworkordercomment) + "'"
				+ ", " + m_sMostRecentCost.replace(",", "")
				+ ")"
				;
				try{
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				}catch (SQLException e){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot execute item INSERT sql - " + e.getMessage() + ".");
					return false;
				}

				if (!clsDatabaseFunctions.commit_data_transaction(conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot commit data transaction.");
					return false;
				}

				//If we get to here, we've succeeded:
				m_sNewRecord = "0";
				return true;				
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error saving item - " + e.getMessage());
			return false;
		}
	}

	private boolean permitChangeToInactive(Connection con){
		return permitCheck("n in-active", con);
	}

	private boolean permitChangeToNonStock(Connection con){
		return permitCheck(" non-stock", con);
	}
	private boolean permitCheck(String smegSubject, Connection con){
		//This function checks to make sure we can change the part from stock to non-stock:

		String SQL = "";
		ResultSet rs = null;
		//Check to make sure there are no quantities or costs for this item in iccosts:
		SQL = "SELECT"
			+ " " + SMTableiccosts.sItemNumber
			+ ", SUM(" + SMTableiccosts.bdCost + ") AS NETCOST"
			+ ", SUM(" + SMTableiccosts.bdQty + ") AS NETQTY"
			+ " FROM " + SMTableiccosts.TableName
			+ " WHERE ("
			+ "(" + SMTableiccosts.sItemNumber + " = '" + getItemNumber() + "')"
			+ ")"
			+ " GROUP BY " + SMTableiccosts.sItemNumber
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				if (
					(rs.getBigDecimal("NETCOST").compareTo(BigDecimal.ZERO) !=0)
					|| (rs.getBigDecimal("NETQTY").compareTo(BigDecimal.ZERO) !=0)
				){
					
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it has"
						+ " costs/quantities still on it.");
				rs.close();
				return false;
			}
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking IC costs/quantities with SQL: " + SQL + " - " 
					+ e.getMessage());
			return false;
		}

		//Check to make sure this item isn't in any unposted batches anywhere:
		SQL = "SELECT"
			+ " " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lid
			+ " FROM " + SMTableicentrylines.TableName
			+ " LEFT JOIN " + ICEntryBatch.TableName
			+ " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber
			+ " = " + ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
			+ " WHERE ("
			+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber
			+ " = '" + getItemNumber() + "')" 
			+ " AND (" 
			+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
			+ " OR (" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
			+ ")"
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it is"
						+ " still in some unposted batches.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking unposted batches with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}

		//Check to make sure this item isn't in any physical counts waiting to be posted:
		SQL = "SELECT"
			+ " " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lid
			+ " FROM " + SMTableicphysicalcountlines.TableName
			+ " LEFT JOIN " + SMTableicphysicalinventories.TableName
			+ " ON " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lphysicalinventoryid
			+ " = " + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid
			+ " WHERE ("
			+ "(" + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sitemnumber
			+ " = '" + getItemNumber() + "')" 
			+ " AND (" + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.istatus 
			+ " = " + SMTableicphysicalinventories.STATUS_ENTERED + ")"
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it is"
						+ " still in some open physical inventories.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking physical inventory lines with SQL: " + " - " + e.getMessage());
			return false;
		}

		//Check to make sure it's not in any locations:
		SQL = "SELECT"
			+ " " + SMTableicitemlocations.sItemNumber
			+ " FROM " + SMTableicitemlocations.TableName
			+ " WHERE ("
			+ "(" + SMTableicitemlocations.sItemNumber + " = '" + getItemNumber() + "')"
			+ " AND ("
			+ "(" + SMTableicitemlocations.sQtyOnHand + " != 0.0000)"
			+ " OR (" + SMTableicitemlocations.sTotalCost + " != 0.00)"
			+ ")"
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it still has"
						+ " costs/quantities in item locations.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking IC costs/quantities with SQL: " + " - " + e.getMessage());
			return false;
		}

		//Check to make sure it's not on any unposted po lines
		SQL = "SELECT"
			+ " " + SMTableicpolines.TableName + "." + SMTableicpolines.lid
			+ " FROM " + SMTableicpolines.TableName
			+ " LEFT JOIN " + SMTableicpoheaders.TableName
			+ " ON " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid
			+ " = " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ " WHERE ("
			+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
			+ " = '" + getItemNumber() + "')" 
			+ " AND (" 
			+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
			+ SMTableicpoheaders.STATUS_ENTERED + ")"
			+ " OR ("+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
			+ SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
			+ ")"
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it is"
						+ " still in some open POs.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking PO lines with SQL: " + " - " + e.getMessage());
			return false;
		}

		//Check to make sure it's not on any unposted receipt lines
		SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
			+ " FROM " + SMTableicporeceiptlines.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
			+ " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " WHERE ("
			+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ " = '" + getItemNumber() + "')"
			+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
			+ " != 0.000)"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " = " 
			+ SMTableicporeceiptheaders.STATUS_ENTERED + ")"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic 
			+ " != 1)"
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it is"
						+ " still on some open receipts.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking PO lines with SQL: " + " - " + e.getMessage());
			return false;
		}

		//Check to make sure it's not on any unposted PO invoice lines
		SQL = "SELECT"
			+ " " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lid
			+ " FROM " + SMTableicpoinvoicelines.TableName
			+ " LEFT JOIN " + SMTableicpoinvoiceheaders.TableName
			+ " ON " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid
			+ " = " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
			+ " WHERE ("
			+ "(" + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.sitemnumber
			+ " = '" + getItemNumber() + "')"
			+ " AND (" + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.bdqtyreceived
			+ " != 0.000)"
			+ " AND (" + SMTableicpoinvoiceheaders.TableName + "." 
			+ SMTableicpoinvoiceheaders.lexportsequencenumber + " < 1)" 
			+ ")"
			;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				m_sErrorMessageArray.add("This item cannot be changed to a" + smegSubject + " item because it is"
						+ " still on some open PO invoices.");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error checking PO lines with SQL: " + " - " + e.getMessage());
			return false;
		}

		return true;

	}
	private boolean removeProcessingHistory(Connection con){
		String SQL = "DELETE FROM " + SMTableicitemstatistics.TableName
		+ " WHERE ("
		+ SMTableicitemstatistics.sItemNumber + " = '" + getItemNumber() + "'"
		+ ")"
		;

		try {
			Statement stmt = con.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error deleting item statistics - " + e.getMessage());
			return false;
		}

		SQL = "DELETE FROM " + SMTableiccosts.TableName
		+ " WHERE ("
		+ SMTableiccosts.sItemNumber + " = '" + getItemNumber() + "'"
		+ ")"
		;

		try {
			Statement stmt = con.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error deleting IC costs - " + e.getMessage());
			return false;
		}

		return true;
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sItemNumber = m_sItemNumber.toUpperCase();

		if(!clsStringFunctions.validateStringCharacters(m_sItemNumber, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in item code");
			return false;
		}else{
			return true;
		}

	}
	private boolean validateEntries(){

		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
		if (m_sItemNumber.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessageArray.add("item number cannot be blank");
			bEntriesAreValid = false;
		}

		m_sItemNumber = m_sItemNumber.replace("-", "");
		m_sItemNumber = m_sItemNumber.replace("'", "");
		m_sItemNumber = m_sItemNumber.replace(" ", "");
		m_sItemNumber = m_sItemNumber.toUpperCase();
		if (m_sItemNumber.length() > SMTableicitems.sItemNumberLength){
			m_sErrorMessageArray.add("item number cannot be longer than " 
					+ SMTableicitems.sItemNumberLength + " characters.");
			bEntriesAreValid = false;
		}

		if (m_sItemDescription.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessageArray.add("item description cannot be blank");
			bEntriesAreValid = false;
		}
		if (m_sItemDescription.length() > SMTableicitems.sItemDescriptionLength){
			m_sErrorMessageArray.add("item description cannot be longer than " 
					+ SMTableicitems.sItemDescriptionLength + " characters.");
			bEntriesAreValid = false;
		}

		if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sLastMaintainedDate)){
			//Allow an empty date:
			if (m_sLastMaintainedDate.compareToIgnoreCase("00/00/0000") == 0){
			}else{
				m_sErrorMessageArray.add("Invalid last maintained date: " + m_sLastMaintainedDate); 
				bEntriesAreValid = false;
			}
		}
		//TJR - 11/7/2016 - commented this out because no place in the system really requires this category code:
		//if (m_sCategoryCode.trim().compareToIgnoreCase("") == 0){
		//	m_sErrorMessageArray.add("category cannot be blank");
		//	bEntriesAreValid = false;
		//}
		if (m_sCategoryCode.length() > SMTableicitems.sCategoryCodeLength){
			m_sErrorMessageArray.add("category code cannot be longer than " 
					+ SMTableicitems.sCategoryCodeLength + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sCostUnitOfMeasure.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessageArray.add("cost unit of measure cannot be blank");
			bEntriesAreValid = false;
		}
		if (m_sCostUnitOfMeasure.length() > SMTableicitems.sCostUnitOfMeasureLength){
			m_sErrorMessageArray.add("cost unit of measure cannot be longer than " 
					+ SMTableicitems.sCostUnitOfMeasureLength + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sDefaultPriceListCode.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessageArray.add("default price list code cannot be blank");
			bEntriesAreValid = false;
		}
		if (m_sDefaultPriceListCode.length() > SMTableicitems.sDefaultPriceListCodeLength){
			m_sErrorMessageArray.add("default price list code cannot be longer than " 
					+ SMTableicitems.sDefaultPriceListCodeLength + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sPickingSequence.length() > SMTableicitems.sPickingSequenceLength){
			m_sErrorMessageArray.add("picking sequence cannot be longer than " 
					+ SMTableicitems.sPickingSequenceLength + " characters.");
			bEntriesAreValid = false;
		}
		if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sInactiveDate)){
			//Allow an empty date:
			if (m_sInactiveDate.compareToIgnoreCase("00/00/0000") == 0){
			}else{
				m_sErrorMessageArray.add("Invalid inactive date: " + m_sInactiveDate); 
				bEntriesAreValid = false;
			}
		}
		if (m_sComment1.length() > SMTableicitems.sComment1Length){
			m_sErrorMessageArray.add("comment 1 cannot be longer than " 
					+ SMTableicitems.sComment1Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sComment2.length() > SMTableicitems.sComment2Length){
			m_sErrorMessageArray.add("comment 2 cannot be longer than " 
					+ SMTableicitems.sComment2Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sComment3.length() > SMTableicitems.sComment3Length){
			m_sErrorMessageArray.add("comment 3 cannot be longer than " 
					+ SMTableicitems.sComment3Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sComment4.length() > SMTableicitems.sComment4Length){
			m_sErrorMessageArray.add("comment 4 cannot be longer than " 
					+ SMTableicitems.sComment4Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sLastEditUserFullName.length() > SMTableicitems.sLastEditUserLength){
			m_sErrorMessageArray.add("Last Edit User cannot be longer than " 
					+ SMTableicitems.sLastEditUserLength + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sDedicatedToOrderNumber.length() > SMTableicitems.sDedicatedToOrderNumberLength){
			m_sErrorMessageArray.add("Dedicated to order number cannot be longer than " 
					+ SMTableicitems.sDedicatedToOrderNumberLength + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sReportGroup1.length() > SMTableicitems.sreportgroup1Length){
			m_sErrorMessageArray.add("report group 1 cannot be longer than " 
					+ SMTableicitems.sreportgroup1Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sReportGroup2.length() > SMTableicitems.sreportgroup2Length){
			m_sErrorMessageArray.add("report group 2 cannot be longer than " 
					+ SMTableicitems.sreportgroup2Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sReportGroup3.length() > SMTableicitems.sreportgroup3Length){
			m_sErrorMessageArray.add("report group 3 cannot be longer than " 
					+ SMTableicitems.sreportgroup3Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sReportGroup4.length() > SMTableicitems.sreportgroup4Length){
			m_sErrorMessageArray.add("report group 4 cannot be longer than " 
					+ SMTableicitems.sreportgroup4Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sReportGroup5.length() > SMTableicitems.sreportgroup5Length){
			m_sErrorMessageArray.add("report group 5 cannot be longer than " 
					+ SMTableicitems.sreportgroup5Length + " characters.");
			bEntriesAreValid = false;
		}
		if (m_sworkordercomment.length() > SMTableicitems.sworkordercommentLength){
			m_sErrorMessageArray.add("work order comment cannot be longer than " 
					+ SMTableicitems.sworkordercommentLength + " characters.");
			bEntriesAreValid = false;
		}
		
		if ((m_icannotbepurchased.compareToIgnoreCase("0") != 0) && (m_icannotbepurchased.compareToIgnoreCase("1") != 0)){
			m_sErrorMessageArray.add("'cannot be purchased' value ('" + m_icannotbepurchased + "' is invalid.");
			bEntriesAreValid = false;
		}
		if ((m_icannotbesold.compareToIgnoreCase("0") != 0) && (m_icannotbesold.compareToIgnoreCase("1") != 0)){
			m_sErrorMessageArray.add("'cannot be sold' value ('" + m_icannotbesold + "' is invalid.");
			bEntriesAreValid = false;
		}
		
		if (m_sMostRecentCost.trim().compareToIgnoreCase("") == 0){
			m_sMostRecentCost = "0.0000";
		}else{
			try{
				m_sMostRecentCost = m_sMostRecentCost.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sMostRecentCost);
				bd = bd.setScale(SMTableicitems.bdmostrecentcostScale, BigDecimal.ROUND_HALF_UP);
				m_sMostRecentCost =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicitems.bdmostrecentcostScale,bd);
			}catch (NumberFormatException e){
				m_sErrorMessageArray.add("invalid most recent cost.");
				bEntriesAreValid = false;
			}
		}

		if (m_sNumberOfLabels.trim().compareToIgnoreCase("") == 0){
			m_sNumberOfLabels = "1.0000";
		}else{
			try{
				m_sNumberOfLabels = m_sNumberOfLabels.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sNumberOfLabels);
				bd = bd.setScale(SMTableicitems.bdnumberoflabelsScale, BigDecimal.ROUND_HALF_UP);
				m_sNumberOfLabels =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicitems.bdnumberoflabelsScale,bd);
			}catch (NumberFormatException e){
				m_sErrorMessageArray.add("invalid number of labels.");
				bEntriesAreValid = false;
			}
		}
		return bEntriesAreValid;
	}
	public String getQueryString(){

		String sQueryString = "";
		sQueryString += ParamAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_sNewRecord);
		sQueryString += "&" + ParamItemNumber + "=" + clsServletUtilities.URLEncode(m_sItemNumber);
		sQueryString += "&" + ParamItemDescription + "=" + clsServletUtilities.URLEncode(m_sItemDescription);
		sQueryString += "&" + ParamLastMaintainedDate + "=" + clsServletUtilities.URLEncode(m_sLastMaintainedDate);
		if (m_sActive.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamActive + "=" + m_sActive;
		}
		if (m_sTaxable.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamTaxable + "=" + m_sTaxable;
		}
		if (m_icannotbepurchased.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + Paramicannotbepurchased + "=" + m_icannotbepurchased;
		}
		if (m_icannotbesold.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + Paramicannotbesold + "=" + m_icannotbesold;
		}
		if (m_sLaborItem.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamLaborItem + "=" + m_sLaborItem;
		}
		if (m_sNonStockItem.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamNonStockItem + "=" + m_sNonStockItem;
		}
		if (m_sSuppressItemQtyLookup.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamSuppressItemQtyLookup + "=" + m_sSuppressItemQtyLookup;
		}
		if (m_sHideOnInvoiceDefault.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ParamHideOnInvoiceDefault + "=" + m_sHideOnInvoiceDefault;
		}
		sQueryString += "&" + ParamCategoryCode + "=" + clsServletUtilities.URLEncode(m_sCategoryCode);
		sQueryString += "&" + ParamCostUnitOfMeasure + "=" + clsServletUtilities.URLEncode(m_sCostUnitOfMeasure);
		sQueryString += "&" + ParamDefaultPriceListCode + "=" + clsServletUtilities.URLEncode(m_sDefaultPriceListCode);
		sQueryString += "&" + ParamPickingSequence + "=" + clsServletUtilities.URLEncode(m_sPickingSequence);
		sQueryString += "&" + ParamInactiveDate + "=" + clsServletUtilities.URLEncode(m_sInactiveDate);
		sQueryString += "&" + ParamComment1 + "=" + clsServletUtilities.URLEncode(m_sComment1);
		sQueryString += "&" + ParamComment2 + "=" + clsServletUtilities.URLEncode(m_sComment2);
		sQueryString += "&" + ParamComment3 + "=" + clsServletUtilities.URLEncode(m_sComment3);
		sQueryString += "&" + ParamComment4 + "=" + clsServletUtilities.URLEncode(m_sComment4);
		sQueryString += "&" + ParamLastEditUserFullName + "=" + clsServletUtilities.URLEncode(m_sLastEditUserFullName);
		sQueryString += "&" + ParamDedicatedToOrderNumber + "=" + clsServletUtilities.URLEncode(m_sDedicatedToOrderNumber);
		sQueryString += "&" + ParamReportGroup1 + "=" + clsServletUtilities.URLEncode(m_sReportGroup1);
		sQueryString += "&" + ParamReportGroup2 + "=" + clsServletUtilities.URLEncode(m_sReportGroup2);
		sQueryString += "&" + ParamReportGroup3 + "=" + clsServletUtilities.URLEncode(m_sReportGroup3);
		sQueryString += "&" + ParamReportGroup4 + "=" + clsServletUtilities.URLEncode(m_sReportGroup4);
		sQueryString += "&" + ParamReportGroup5 + "=" + clsServletUtilities.URLEncode(m_sReportGroup5);
		sQueryString += "&" + Paramsworkordercomment + "=" + clsServletUtilities.URLEncode(m_sworkordercomment);
		sQueryString += "&" + ParamMostRecentCost + "=" + clsServletUtilities.URLEncode(m_sMostRecentCost);
		sQueryString += "&" + ParamNumberOfLabels + "=" + clsServletUtilities.URLEncode(m_sNumberOfLabels);
		return sQueryString;
	}
	//Requires connection since it is used as part of a data transaction in places:
	public boolean delete(String sItemNumber, Connection conn){

		m_sErrorMessageArray.clear();

		//First, check that the item exists:
		String SQL = "SELECT * FROM " + SMTableicitems.TableName
		+ " WHERE ("
		+ SMTableicitems.sItemNumber + " = '" + sItemNumber + "'"
		+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}

		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1421098500] checking item " + sItemNumber + " to delete - " + e.getMessage());
			return false;
		}

		//Cost buckets (iccosts)
		SQL = "SELECT "
			+ " " + SMTableiccosts.sItemNumber
			+ " FROM " + SMTableiccosts.TableName
			+ " WHERE ("
			+ "(" + SMTableiccosts.sItemNumber + " = '" + sItemNumber + "')"
			+ " AND ("
			+ "(" + SMTableiccosts.bdCost + " != 0.0000)"
			+ " OR (" + SMTableiccosts.bdQty + " != 0.0000)"
			+ ")"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber 
						+ " still has quantities/cost in some cost buckets.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1421098499] checking iccost records for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Pending transactions
		SQL = "SELECT "
			+ " " + SMTableicentrylines.sitemnumber
			+ " FROM " + SMTableicentrylines.TableName + " LEFT JOIN " + ICEntryBatch.TableName
			+ " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber + " = "
			+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
			+ " WHERE ("
			+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber 
			+ " = '" + sItemNumber + "')"
			+ " AND (" 
			+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus + " = " 
			+ SMBatchStatuses.ENTERED + ")"
			+ " OR (" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus 
			+ " = " + SMBatchStatuses.IMPORTED + ")"
			+ ")"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber 
						+ " is included in pending IC batches.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking pending batch records for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098498] checking pending batch records for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Pending physical inventory with this item
		SQL = "SELECT "
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ " FROM " + SMTableicinventoryworksheet.TableName + " LEFT JOIN " + SMTableicphysicalinventories.TableName
			+ " ON " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid + " = "
			+ SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid
			+ " WHERE ("
			+ "(" + SMTableicinventoryworksheet.TableName + "." 
			+ SMTableicinventoryworksheet.sitemnumber + " = '" + sItemNumber + "')"
			+ " AND (" + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.istatus
			+ " = " + Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED) + ")"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber 
						+ " is included in an unprocessed physical inventory.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking physical inventory records for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098497] reading physical inventory records for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Open (unreceived) PO's:
		SQL = "SELECT"
			+ " " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
			+ " FROM " + SMTableicpolines.TableName + " LEFT JOIN " + SMTableicpoheaders.TableName
			+ " ON " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ " WHERE ("
			+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = '" + sItemNumber + "')"
			+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem + " = 0)"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " != " 
			+ Integer.toString(SMTableicpoheaders.STATUS_COMPLETE) + ")"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " != " 
			+ Integer.toString(SMTableicpoheaders.STATUS_DELETED) + ")"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber + " is included on some open purchase orders.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking purchase orders for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098496] checking purchase orders for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//PO Receipts:
		SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ " FROM " + SMTableicporeceiptlines.TableName + " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " WHERE ("
			//If the receipts have EITHER not been processed into batches OR have not been invoiced,
			//we want them listing in this query:
			+ "("
			+ "(" + SMTableicporeceiptheaders.TableName + "." 
			+ SMTableicporeceiptheaders.lpostedtoic + " = 0)"
			+ " OR (" + SMTableicporeceiptlines.TableName + "." 
			+ SMTableicporeceiptlines.lpoinvoiceid + " = " 
				+ Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET) + ")"
			+ ")"
			+ " AND (" + SMTableicporeceiptlines.TableName + "." 
			+ SMTableicporeceiptlines.lnoninventoryitem + " = 0)"
			+ " AND (" + SMTableicporeceiptlines.TableName + "." 
			+ SMTableicporeceiptlines.sitemnumber + " = '" + sItemNumber + "')"
			+ " AND ( " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != 1)"
			+ ")"
			;

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber 
						+ " is included on some purchase order receipts that have EITHER not been processed into"
						+ " batches OR have not been invoiced yet.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking purchase order receipts for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098495] checking purchase order receipts for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//No unprocessed invoices:
		SQL = "SELECT"
			+ " " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.sitemnumber
			+ " FROM " + SMTableicpoinvoicelines.TableName + " LEFT JOIN " + SMTableicpoinvoiceheaders.TableName
			+ " ON " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid + " = "
			+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
			+ " WHERE ("
			+ "(" + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.sitemnumber
			+ " = '" + sItemNumber + "')"
			+ " AND (" + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lnoninventoryitem
			+ " = 0)"
			+ " AND (" 
			+ "(" + SMTableicpoinvoiceheaders.TableName + "." 
			+ SMTableicpoinvoiceheaders.lexportsequencenumber + " = 0)"
			+ " OR (" + SMTableicpoinvoiceheaders.TableName + "." 
			+ SMTableicpoinvoiceheaders.lexportsequencenumber + " = -1)"
			+ ")"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber 
						+ " is included on some IC invoices that have not been exported into"
						+ " Accounts Payable yet.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking IC invoices for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098494] checking IC invoices for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//No open orders in SM
		SQL = "SELECT " 
			+ SMTableorderheaders.sOrderNumber
			+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName
			+ " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " WHERE ("
			+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber 
			+ " = '" + sItemNumber + "')"

			+ " AND (" + SMTableorderdetails.TableName + "." 
			+ SMTableorderdetails.dQtyOrdered + " != 0.00)"

			+ " AND ("
			+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
			+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
			+ ")"

			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber + " is included on some open orders or quotes.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking open orders for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098493] checking open orders for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//No unexported invoices in SM
		SQL = "SELECT " 
			+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
			+ " FROM " + SMTableinvoiceheaders.TableName + " LEFT JOIN " + SMTableinvoicedetails.TableName
			+ " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = "
			+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
			+ " WHERE ("
			+ "(" + SMTableinvoicedetails.sItemNumber + " = '" + sItemNumber + "')"
			+ " AND ("
				+ "(" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
				+ " OR (" + SMTableinvoiceheaders.iExportedToIC + " != 1)"
			+ ")"
			+ ")"
			;

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber + " is on invoices that have not yet been exported to AR or IC batches.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			//System.out.println("Error checking unexported invoices for " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098492] checking unexported invoices for " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//No unposted work orders:
		SQL = "SELECT " 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.lid
			+ " FROM " + SMTableworkorderdetails.TableName + " LEFT JOIN " + SMTableworkorders.TableName
			+ " ON " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.lworkorderid + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.lid
			+ " WHERE ("
			+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.iposted + " = 0)"
			+ " AND (" + SMTableworkorderdetails.sitemnumber + " = '" + sItemNumber + "')"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Item " + sItemNumber + " is on some work orders that have not been posted.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1421098479] checking unposted work orders for " + sItemNumber + " - " + e.getMessage());
			return false;
		}
		
		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1421098480] beginning data transaction to delete item " + sItemNumber + "");
			return false;
		}

		//Cost buckets (iccosts)
		try{
			SQL = "DELETE FROM " + SMTableiccosts.TableName
			+ " WHERE ("
			+ SMTableiccosts.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1421098481] deleting iccost buckets for item " + sItemNumber);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			//System.out.println("Error deleting iccost buckets for item " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098482] deleting iccost buckets for item " + sItemNumber + " - " + e.getMessage());
			return false;
		}
		
		//Statistics
		try{
			SQL = "DELETE FROM " + SMTableicitemstatistics.TableName
			+ " WHERE ("
			+ SMTableicitemstatistics.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1421098483] deleting statistics for item " + sItemNumber);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			//System.out.println("Error deleting statistics for item " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098484] deleting statistics for item " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Item locations (icilocs)
		try{
			SQL = "DELETE FROM " + SMTableicitemlocations.TableName
			+ " WHERE ("
			+ SMTableicitemlocations.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1421098485] deleting item locations for item " + sItemNumber);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			//System.out.println("Error deleting item locations for item " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098486] deleting item locations for item " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Item prices
		try{
			SQL = "DELETE FROM " + SMTableicitemprices.TableName
			+ " WHERE ("
			+ SMTableicitemprices.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1421098487] deleting prices for item " + sItemNumber);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			//System.out.println("Error deleting prices for item " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098488] deleting prices for item " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		//Vendor items
		SQL = "DELETE FROM " + SMTableicvendoritems.TableName
		+ " WHERE ("
		+ "(" + SMTableicvendoritems.sItemNumber + " = '" + sItemNumber + "')"
		+ ")"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			m_sErrorMessageArray.add("Error [1421098489] deleting vendor item with SQL: " + SQL + " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		//The items themselves:
		try{
			SQL = "DELETE FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ SMTableicitems.sItemNumber + " = '" + sItemNumber + "'"
			+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1421098490] deleting item " + sItemNumber);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			//System.out.println("Error deleting item " + sItemNumber + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1421098491] deleting item " + sItemNumber + " - " + e.getMessage());
			return false;
		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		return true;
	}
	public String getNextDedicatedItemNumberForOrder(Connection conn, String sTrimmedOrderNumber) throws Exception{
		
		int iItemCounter = 1; 
		//Get the next unused item 'counter' for this order:
		String SQL = "SELECT"
				+ " LEFT(" + SMTableicitems.sItemNumber + ", " + Integer.toString(DEDICATEDITEMPREFIXLENGTH) + ") AS HIGHESTPREFIX"
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
					+ "(RIGHT (" + SMTableicitems.sItemNumber + ", LENGTH(" 
					+ SMTableicitems.sItemNumber + ") - "+ Integer.toString(DEDICATEDITEMPREFIXLENGTH) + ") = '" + sTrimmedOrderNumber + "')"
					+ " AND (LEFT(" + SMTableicitems.sItemNumber + ", " + Integer.toString(DEDICATEDITEMPREFIXLENGTH) + ") REGEXP '^[0-9]+$')"
					//+ " AND (" + SMTableicitems.sDedicatedToOrderNumber + " = '" + sTrimmedOrderNumber + "')"
				+ ") ORDER BY LEFT(" + SMTableicitems.sItemNumber + ", " + Integer.toString(DEDICATEDITEMPREFIXLENGTH) + ") DESC"
				+ " LIMIT 1"
			;
		
		
		try {
			ResultSet rsPrefix = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsPrefix.next()){
				try {
					iItemCounter = Integer.parseInt(rsPrefix.getString("HIGHESTPREFIX")) + 1;
				} catch (NumberFormatException e) {
					rsPrefix.close();
					throw new SQLException("Error parsing item number prefix: '" + rsPrefix.getString("HIGHESTPREFIX") + "'.");
				}
			}
			rsPrefix.close();
		} catch (Exception e) {
			throw new SQLException("Error reading highest item number prefix with SQL: " + SQL + " - " + e.getMessage());
		}

		//Return the new item number:
		return clsStringFunctions.PadLeft(Integer.toString(iItemCounter), "0", DEDICATEDITEMPREFIXLENGTH) + sTrimmedOrderNumber;
	}
	public String getItemNumber(){
		return m_sItemNumber;
	}
	public void setItemNumber(String sItemNumber){
		m_sItemNumber = sItemNumber.trim();
	}
	public String getItemDescription(){
		return m_sItemDescription;
	}
	public void setItemDescription(String sItemDescription) {
		m_sItemDescription = sItemDescription.trim();
	}
	public String getLastMaintainedDate() {
		return m_sLastMaintainedDate;
	}
	public void setLastMaintainedDate(String sLastMaintainedDate) {
		m_sLastMaintainedDate = sLastMaintainedDate;
	}
	public String getActive() {
		return m_sActive;
	}
	public String getCannotBePurchasedFlag() {
		return m_icannotbepurchased;
	}
	public void setCannotBePurchasedFlag(String sCannotBePurchased) {
		m_icannotbepurchased = sCannotBePurchased;
	}
	public String getCannotBeSoldFlag() {
		return m_icannotbesold;
	}
	public void setCannotBeSoldFlag(String sCannotBeSold) {
		m_icannotbesold = sCannotBeSold;
	}
	public void setActive(String sActive) {
		m_sActive = sActive;
	}
	public String getTaxable() {
		return m_sTaxable;
	}
	public void setTaxable(String sTaxable) {
		m_sTaxable = sTaxable;
	}
	public String getLaborItem() {
		return m_sLaborItem;
	}
	public void setLaborItem(String sLaborItem) {
		m_sLaborItem = sLaborItem;
	}
	public String getNonStockItem() {
		return m_sNonStockItem;
	}
	public void setNonStockItem(String sNonStockItem) {
		m_sNonStockItem = sNonStockItem;
	}
	public String getSuppressItemQtyLookup() {
		return m_sSuppressItemQtyLookup;
	}
	public void setSuppressItemQtyLookup(String sSuppressItemQtyLookup) {
		m_sSuppressItemQtyLookup = sSuppressItemQtyLookup;
	}
	public String getHideOnInvoiceDefault() {
		return m_sHideOnInvoiceDefault;
	}
	public void setHideOnInvoiceDefault(String sHideOnInvoiceDefault) {
		m_sHideOnInvoiceDefault = sHideOnInvoiceDefault;
	}
	public String getCategoryCode() {
		return m_sCategoryCode;
	}
	public void setCategoryCode(String sCategoryCode) {
		m_sCategoryCode = sCategoryCode.trim();
	}
	public String getCostUnitOfMeasure() {
		return m_sCostUnitOfMeasure;
	}
	public void setCostUnitOfMeasure(String sCostUnitOfMeasure) {
		m_sCostUnitOfMeasure = sCostUnitOfMeasure.trim();
	}
	public String getDefaultPriceListCode() {
		return m_sDefaultPriceListCode;
	}
	public void setDefaultPriceListCode(String sDefaultPriceListCode) {
		m_sDefaultPriceListCode = sDefaultPriceListCode.trim();
	}
	public String getPickingSequence() {
		return m_sPickingSequence;
	}
	public void setPickingSequence(String sPickingSequence) {
		m_sPickingSequence = sPickingSequence.trim();
	}
	public String getInactiveDate() {
		return m_sInactiveDate;
	}
	public void setInactiveDate(String sInactiveDate) {
		m_sInactiveDate = sInactiveDate.trim();
	}
	public String getComment1() {
		return m_sComment1;
	}
	public void setComment1(String sComment1) {
		m_sComment1 = sComment1.trim();
	}
	public String getComment2() {
		return m_sComment2;
	}
	public void setComment2(String sComment2) {
		m_sComment2 = sComment2.trim();
	}
	public String getComment3() {
		return m_sComment3;
	}
	public void setComment3(String sComment3) {
		m_sComment3 = sComment3.trim();
	}
	public String getComment4() {
		return m_sComment4;
	}
	public void setComment4(String sComment4) {
		m_sComment4 = sComment4.trim();
	}
	public String getLastEditUserFullName() {
		return m_sLastEditUserFullName;
	}
	public void setLastEditUserFullName(String sLastEditUserFullName) {
		m_sLastEditUserFullName = sLastEditUserFullName.trim();
	}
	public String getDedicatedToOrderNumber() {
		return m_sDedicatedToOrderNumber;
	}
	public void setDedicatedToOrderNumber(String sDedicatedToOrderNumber) {
		m_sDedicatedToOrderNumber = sDedicatedToOrderNumber.trim();
	}
	public String getReportGroup1() {
		return m_sReportGroup1;
	}
	public void setReportGroup1(String sReportGroup1) {
		m_sReportGroup1 = sReportGroup1.trim();
	}
	public String getReportGroup2() {
		return m_sReportGroup2;
	}
	public void setReportGroup2(String sReportGroup2) {
		m_sReportGroup2 = sReportGroup2.trim();
	}
	public String getReportGroup3() {
		return m_sReportGroup3;
	}
	public void setReportGroup3(String sReportGroup3) {
		m_sReportGroup3 = sReportGroup3.trim();
	}
	public String getReportGroup4() {
		return m_sReportGroup4;
	}
	public void setReportGroup4(String sReportGroup4) {
		m_sReportGroup4 = sReportGroup4.trim();
	}
	public String getReportGroup5() {
		return m_sReportGroup5;
	}
	public void setReportGroup5(String sReportGroup5) {
		m_sReportGroup5 = sReportGroup5.trim();
	}
	public String getworkordercomment() {
		return m_sworkordercomment;
	}
	public void setworkordercomment(String sworkordercomment) {
		m_sworkordercomment = sworkordercomment.trim();
	}
	public String getMostRecentCost() {
		return m_sMostRecentCost;
	}
	public void setMostRecentCost(String sMostRecentCost) {
		m_sMostRecentCost = sMostRecentCost.trim().replace(",", "");
	}
	public String getNumberOfLabels() {
		return m_sNumberOfLabels;
	}
	public void setNumberOfLabels(String sNumberOfLabels) {
		m_sNumberOfLabels = sNumberOfLabels.trim().replace(",", "");
	}

	public String getNewRecord() {
		return m_sNewRecord;
	}
	public void setNewRecord(String newRecord) {
		m_sNewRecord = newRecord;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
