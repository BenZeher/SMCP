package SMClasses;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smic.ICCategory;
import smic.ICItem;
import smic.ICOption;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMUtilities;

public class SMOrderDetail extends clsMasterEntry{
	public static final String ParamObjectName = "Order detail";
	
	public static final String ParamdUniqueOrderID = "dUniqueOrderID"; //NOT NULL default '0',
	public static final String ParamiDetailNumber = "iDetailNumber";
	public static final String ParammInvoiceComments = "mInvoiceComments";
	public static final String ParammInternalComments = "mInternalComments";
	public static final String ParammTicketComments = "mTicketComments";
	public static final String ParamiLineNumber = "iLineNumber";
	public static final String ParamsItemNumber = "sItemNumber";
	public static final String ParamsItemDesc = "sItemDesc";
	public static final String ParamsItemCategory = "sItemCategory";
	public static final String ParamsLocationCode = "sLocationCode";
	public static final String ParamdatDetailExpectedShipDate = "datDetailExpectedShipDate";
	public static final String ParamiIsStockItem = "iIsStockItem";
	public static final String ParamdQtyOrdered = "dQtyOrdered";
	public static final String ParamdQtyShipped = "dQtyShipped";
	public static final String ParamdQtyShippedToDate = "dQtyShippedToDate";
	public static final String ParamdOriginalQty = "dOriginalQty";
	public static final String ParamsOrderUnitOfMeasure = "sOrderUnitOfMeasure";
	public static final String ParamdOrderUnitPrice = "dOrderUnitPrice";
	public static final String ParamdOrderUnitCost = "dOrderUnitCost";
	public static final String ParamdExtendedOrderPrice = "dExtendedOrderPrice";
	public static final String ParamdExtendedOrderCost = "dExtendedOrderCost";
	public static final String ParamiTaxable = "iTaxable";
	public static final String ParamdatLineBookedDate = "datLineBookedDate";
	public static final String ParamdLineTaxAmount = "dLineTaxAmount";
	public static final String ParamsMechInitial = "sMechInitial";
	public static final String Paramimechid = "imechid";
	public static final String ParamsMechFullName = "sMechFullName";
	public static final String ParamsLabel = "sLabel";
	public static final String Paramstrimmedordernumber = "strimmedordernumber";
	public static final String Paramisuppressdetailoninvoice = "isuppressdetailoninvoice";
	public static final String Paramiprintondeliveryticket = "iprintondeliveryticket";
	public static final String Parambdestimatedunitcost = "bdestimatedunitcost";
	
	private String m_dUniqueOrderID;
	private String m_iDetailNumber;
	private String m_mInvoiceComments;
	private String m_mInternalComments;
	private String m_mTicketComments;
	private String m_iLineNumber;
	private String m_sItemNumber;
	private String m_sItemDesc;
	private String m_sItemCategory;
	private String m_sLocationCode;
	private String m_datDetailExpectedShipDate;
	private String m_iIsStockItem;
	private String m_dQtyOrdered;
	private String m_dQtyShipped;
	private String m_dQtyShippedToDate;
	private String m_dOriginalQty;
	private String m_sOrderUnitOfMeasure;
	private String m_dOrderUnitPrice;
	private String m_dOrderUnitCost;
	private String m_dExtendedOrderPrice;
	private String m_dExtendedOrderCost;
	private String m_iTaxable;
	private String m_datLineBookedDate;
	private String m_imechid;
	private String m_sMechInitial;
	private String m_sMechFullName;
	private String m_sLabel;
	private String m_strimmedordernumber;
	private String m_isuppressdetailoninvoice;
	private String m_iprintondeliveryticket;
	private String m_bdestimatedunitcost;
	
	private boolean bDebugMode = false;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	
    public SMOrderDetail() {
		super();
		initDetailVariables();
        }
	public SMOrderDetail (HttpServletRequest req){
		super(req);
		initDetailVariables();
		
		//PrintWriter pwOut = new PrintWriter(System.out, true);
		//SMUtilities.printRequestParameters(pwOut, req);
		
		m_dUniqueOrderID = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdUniqueOrderID, req).trim();
		m_iDetailNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamiDetailNumber, req).trim();
		m_mInvoiceComments = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParammInvoiceComments, req).trim();
		m_mInternalComments = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParammInternalComments, req).trim();
		m_mTicketComments = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParammTicketComments, req).trim();
		m_iLineNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamiLineNumber, req).trim();
		m_sItemNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsItemNumber, req).trim();
		m_sItemDesc = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsItemDesc, req).trim();
		m_sItemCategory = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsItemCategory, req).trim();
		m_sLocationCode = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsLocationCode, req).trim();
		m_datDetailExpectedShipDate = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdatDetailExpectedShipDate, req).trim();
		m_iIsStockItem = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamiIsStockItem, req).trim();
		m_dQtyOrdered = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdQtyOrdered, req).trim();
		m_dQtyShipped = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdQtyShipped, req).trim();
		m_dQtyShippedToDate = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdQtyShippedToDate, req).trim();
		m_dOriginalQty = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdOriginalQty, req).trim();
		m_sOrderUnitOfMeasure = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsOrderUnitOfMeasure, req).trim();
		m_dOrderUnitPrice = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdOrderUnitPrice, req).trim();
		m_dOrderUnitCost = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdOrderUnitCost, req).trim();
		m_dExtendedOrderPrice = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdExtendedOrderPrice, req).trim();
		m_dExtendedOrderCost = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdExtendedOrderCost, req).trim();
		if (req.getParameter(SMOrderDetail.ParamiTaxable) == null){
			m_iTaxable = "0";
		}else{
			m_iTaxable = "1";
		}
		m_datLineBookedDate = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamdatLineBookedDate, req).trim();
		m_imechid = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.Paramimechid, req).trim();
		m_sMechInitial = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsMechInitial, req).trim();
		m_sMechFullName = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsMechFullName, req).trim();
		m_sLabel = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsLabel, req).trim();
		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.Paramstrimmedordernumber, req).trim();
		if (req.getParameter(SMOrderDetail.Paramisuppressdetailoninvoice) == null){
			m_isuppressdetailoninvoice = "0";
		}else{
			m_isuppressdetailoninvoice = "1";
		}
		if (req.getParameter(SMOrderDetail.Paramiprintondeliveryticket) == null){
			m_iprintondeliveryticket = "0";
		}else{
			m_iprintondeliveryticket = "1";
		}
		m_bdestimatedunitcost = clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.Parambdestimatedunitcost, req).trim();
	}
    public boolean validate_line(Connection conn) {
    	boolean bLineIsValid = true;
    	String SQL = "";
    	SMOrderHeader order = new SMOrderHeader();
    	order.setM_strimmedordernumber(getM_strimmedordernumber());
    	if (!order.load(conn)){
    		super.addErrorMessage("Could not load order/quote number '" + getM_strimmedordernumber() + "' - " + order.getErrorMessages());
    		return false;
    	}
    	
        /*
        TODO - add this validation at some point?
       'Check the extended prices:
       If Common_Round(Me.OrderDetailDoubleFieldValue(dExtendedOrderPrice, i), 2) <> _
           Common_Round( _
               Me.OrderDetailDoubleFieldValue(dOrderUnitPrice, i) * Me.OrderDetailDoubleFieldValue(dQtyShipped, i), _
               2) Then
           If MsgBox("The extended price on line " & i & " doesn't equal the unit price times the qty shipped.  Do you want to save the order like this anyway?", vbYesNo, cGlobalSettings.ProgramName) = vbNo Then
               LoggingClassEntry cn, enumLogErrorSave, "Extended price is not equal to price times qty shipped on line " & i & " item number: " & Me.OrderDetailStringFieldValue(sItemNumber, i) & " - user chose to correct it.", "", False
               GoTo CLEAR_ALL
           Else
               LoggingClassEntry cn, enumLogOther, "Extended price is not equal to price times qty shipped on line " & i & " item number: " & Me.OrderDetailStringFieldValue(sItemNumber, i) & " - user chose NOT to correct it.", "", False
           End If
       End If
         */

    	if (!isLongValid("Unique order ID", m_dUniqueOrderID, true)){bLineIsValid = false;}
    	if (!isLongValid("Detail number", m_iDetailNumber, true)){bLineIsValid = false;}
    	
    	m_mInvoiceComments = m_mInvoiceComments.trim();
    	m_mInternalComments = m_mInternalComments.trim();
    	m_mTicketComments = m_mTicketComments.trim();

    	//m_iLineNumber;
    	if (!isLongValid("Line number", m_iLineNumber, true)){bLineIsValid = false;}
    	
    	//m_sItemNumber;
     	m_sItemNumber = m_sItemNumber.trim();
     	//We only validate item information if the qty ordered is greater than zero.  Otherwise, the user will 
     	//get an error every time he tries to save a line which may have a deleted item.  We allow deleted items
     	//on orders as long as the qty ordered is zero.
     	BigDecimal bdQtyOrdered = new BigDecimal("0.0000");
     	try {
			bdQtyOrdered = new BigDecimal(this.getM_dQtyOrdered().replace(",", ""));
		} catch (Exception e1) {
			bdQtyOrdered = new BigDecimal("0.0000");
		}
     	
     	if (bdQtyOrdered.compareTo(BigDecimal.ZERO) > 0){
		 	ICItem item = new ICItem(m_sItemNumber);
	        if (!item.load(conn)){
	        	super.addErrorMessage("Invalid item number: '" + m_sItemNumber + "'.");
	        	return false;
	        }
	        if (item.getNonStockItem().compareToIgnoreCase("0") == 0){
	        	m_iIsStockItem = "1";
	        }else{
	        	m_iIsStockItem = "0";
	        }
	        
	        //Make sure that it is a 'sellable' item:
	        if (item.getCannotBeSoldFlag().compareToIgnoreCase("1") == 0){
	        	super.addErrorMessage("Item number: '" + m_sItemNumber + "  is not configured as a sellable item.");
	        	return false;
	        }
     	}
     	if (
     			(m_iIsStockItem.compareToIgnoreCase("0") != 0)
     			&& (m_iIsStockItem.compareToIgnoreCase("1") != 0)
     	){
     		super.addErrorMessage("Stock item '" + m_iIsStockItem + "' is not valid.");
     		bLineIsValid = false;
     	}
        m_sItemDesc = m_sItemDesc.trim();
        if (!isStringValid("Item description", m_sItemDesc, SMTableorderdetails.sItemDescLength, false)){
        	bLineIsValid = false;}
    	//m_sItemCategory;
        m_sItemCategory = m_sItemCategory.trim();
	 	ICCategory cat = new ICCategory(m_sItemCategory);
        if (!cat.load(conn)){
   	       	super.addErrorMessage("Invalid category code: '" + m_sItemCategory + "'.");
   	       	bLineIsValid = false;
        }
        //m_sLocationCode;
        m_sLocationCode = m_sLocationCode.trim();
        SQL = "SELECT " + SMTablelocations.sLocation
        	+ " FROM " + SMTablelocations.TableName
        	+ " WHERE ("
        		+ "(" + SMTablelocations.sLocation + " = '" + m_sLocationCode + "')"
        	+ ")"
        ;
        try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
			   	super.addErrorMessage("Invalid location: '" + m_sLocationCode + "'.");
			   	bLineIsValid = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error checking location: '" + m_sLocationCode + "' - " + e.getMessage());
		   	bLineIsValid = false;
		}
    	//m_datDetailExpectedShipDate;
        if (!isDateValid("Expected ship date", m_datDetailExpectedShipDate, true)){bLineIsValid = false;}
    	m_dQtyOrdered = m_dQtyOrdered.trim();
        if (!isDoubleValid("Qty ordered", m_dQtyOrdered, 4)){bLineIsValid = false;}
    	m_dQtyShipped = m_dQtyShipped.trim();
        if (!isDoubleValid("Qty shipped", m_dQtyShipped, 4)){bLineIsValid = false;}

        //If the qty shipped is greater than zero, check:
        BigDecimal bdQtyShipped = new BigDecimal(m_dQtyShipped.replace(",", ""));
        if ((bdQtyShipped.compareTo(BigDecimal.ZERO) > 0) && (m_iIsStockItem.compareToIgnoreCase("1") == 0)){
	        //If the IC option to disallow shipping more than the number on hand is active, then return false:
	        ICOption icoptions = new ICOption();
	        if (!icoptions.load(conn)){
	        	super.addErrorMessage("Error loading icoptions - " + icoptions.getErrorMessage());
			   	bLineIsValid = false;
	        }
	        if (
	        		(icoptions.getAllowNegativeQtys() == 0)
	        		&& (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0)
	        ){
	        	//See if the qty shipped is greater than the qty on hand:
	        	SQL = "SELECT"
	        		+ " " + SMTableicitemlocations.sQtyOnHand
	        		+ " FROM " + SMTableicitemlocations.TableName
	        		+ " WHERE ("
	        			+ "(" + SMTableicitemlocations.sItemNumber + " = '" + m_sItemNumber + "')"
	        			+ " AND (" + SMTableicitemlocations.sLocation + " = '" + m_sLocationCode + "')"
	        		+ ")"
	        	;
	        	try {
					ResultSet rsitemlocation = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsitemlocation.next()){
						BigDecimal bdQtyOnHand = rsitemlocation.getBigDecimal(SMTableicitemlocations.sQtyOnHand);
						rsitemlocation.close();
						if (bdQtyShipped.compareTo(bdQtyOnHand) > 0){
					    	super.addErrorMessage("You are attempting to ship " + m_dQtyShipped + " of item number '" + m_sItemNumber 
					        		+ "' but there are only " 
					    			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyOrderedScale, bdQtyOnHand) 
					    			+ " on hand at this location.");
							   	bLineIsValid = false;
						}
					}else{
						rsitemlocation.close();
						super.addErrorMessage("You are attempting to ship " + m_dQtyShipped + " of Item number '" + m_sItemNumber 
							+ "' but there are none on hand at this location.");
					   	bLineIsValid = false;
					}
				} catch (SQLException e) {
					super.addErrorMessage("Error trying to read qty on hand with SQL: '" + SQL + "' - " + e.getMessage());
					   	bLineIsValid = false;
				}
	        }
        }
        m_dQtyShippedToDate = m_dQtyShippedToDate.trim();
        if (!isDoubleValid("Qty shipped to date", m_dQtyShippedToDate, 4)){bLineIsValid = false;}
    	m_dOriginalQty = m_dOriginalQty.trim();;
        if (!isDoubleValid("Original qty", m_dOriginalQty, 4)){bLineIsValid = false;}
        //m_sOrderUnitOfMeasure;
        m_sOrderUnitOfMeasure = m_sOrderUnitOfMeasure.trim();
        if (!isStringValid("Unit of measure", m_sOrderUnitOfMeasure, SMTableorderdetails.sOrderUnitOfMeasureLength, true)){
        	bLineIsValid = false;}
        //m_dOrderUnitPrice;
        m_dOrderUnitPrice = m_dOrderUnitPrice.trim();
        if (!isDoubleValid("Unit price", m_dOrderUnitPrice, 2)){bLineIsValid = false;}
        //m_dOrderUnitCost;
        m_dOrderUnitCost = m_dOrderUnitCost.trim();
        if (!isDoubleValid("Unit cost", m_dOrderUnitCost, 2)){bLineIsValid = false;}
    	//m_dExtendedOrderPrice;
        m_dExtendedOrderPrice = m_dExtendedOrderPrice.trim();
        if (!isDoubleValid("Extended price", m_dExtendedOrderPrice, 2)){bLineIsValid = false;}
    	//m_dExtendedOrderCost;
        m_dExtendedOrderCost = m_dExtendedOrderCost.trim();
        if (!isDoubleValid("Extended cost", m_dExtendedOrderCost, 2)){bLineIsValid = false;}
    	//m_iTaxable;
        m_iTaxable = m_iTaxable.trim();
        if (!isBooleanIntValid("Item taxable", m_iTaxable)){bLineIsValid = false;}
    	//m_datLineBookedDate;
        m_datLineBookedDate = m_datLineBookedDate.trim();
        if (!isDateValid("Line booked date", m_datLineBookedDate, true)){bLineIsValid = false;}
    	//m_dLineTaxAmount;
        //if (!isDoubleValid("Line tax amount", m_dLineTaxAmount, 2)){bLineIsValid = false;}
    	//m_sMechInitial;
        m_sMechInitial = m_sMechInitial.trim();
        if (!isStringValid("Mechanic Initials", m_sMechInitial, SMTableorderdetails.sMechInitialLength, true)){
        	bLineIsValid = false;}
    	//m_sMechFullName;
        m_sMechFullName = m_sMechFullName.trim();
        if (!isStringValid("Mechanic Full Name", m_sMechFullName, SMTableorderdetails.sMechFullNameLength, true)){
        	bLineIsValid = false;}
        //m_sLabel;
        m_sLabel = m_sLabel.trim();
        if (!isStringValid("Label", m_sLabel, SMTableorderdetails.sLabelLength, true)){
        	bLineIsValid = false;}
        //m_strimmedordernumber;
        m_strimmedordernumber = m_strimmedordernumber.trim();
        if (!isLongValid("Trimmed order number", m_strimmedordernumber, true)){bLineIsValid = false;}
    	
    	if (
    			(m_isuppressdetailoninvoice.compareToIgnoreCase("0") != 0)
    			&& (m_isuppressdetailoninvoice.compareToIgnoreCase("1") != 0)
    	){
    		bLineIsValid = false;
    	}
    	if (
    			(m_iprintondeliveryticket.compareToIgnoreCase("0") != 0)
    			&& (m_iprintondeliveryticket.compareToIgnoreCase("1") != 0)
    	){
    		bLineIsValid = false;
    	}
    	
    	//Validate mechanic info:
    	if (m_sMechInitial.compareToIgnoreCase("") != 0){
	    	SQL = "SELECT * FROM " + SMTablemechanics.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTablemechanics.sMechInitial + " = '" + m_sMechInitial + "')"
	    		+ ")"
	    	;
	    	try {
				ResultSet rsMechanic = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsMechanic.next()){
					m_imechid = Long.toString((rsMechanic.getLong(SMTablemechanics.lid)));
					m_sMechFullName = rsMechanic.getString(SMTablemechanics.sMechFullName);
				}else{
					//super.addErrorMessage("Mechanic with initials '" + m_sMechInitial + "' was not found.");
					//bLineIsValid = false;
				}
				rsMechanic.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error reading mechanics with SQL: " + SQL + " - " + e.getMessage());
				bLineIsValid = false;
			}
    	}else{
    		m_imechid = "0";
			m_sMechInitial = "N/A";
			m_sMechFullName = "N/A";
    	}
    	if (!isBigDecimalValid("Estimated unit cost", m_bdestimatedunitcost, 4, true)){bLineIsValid = false;}
    	return bLineIsValid;
    }
    private boolean isDateValid(String sDateLabel, String sTestDate, boolean bAllowEmptyDate){
        if (sTestDate.compareTo(EMPTY_DATE_STRING) == 0){
        	if (!bAllowEmptyDate){
	        	super.addErrorMessage(sDateLabel + " cannot be blank.");
	        	return false;
        	}
        }else{
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sTestDate)){
	        	super.addErrorMessage(sDateLabel + " '" + sTestDate + "' is invalid.  ");
	        	return false;
	        }
        }
        return true;
    }
    private boolean isDoubleValid(String sDoubleLabel, String sTestDouble, int iScale){
    	@SuppressWarnings("unused")
        Double dTest;
        if (sTestDouble.compareToIgnoreCase("") == 0){
        	sTestDouble = "0." + clsStringFunctions.PadLeft("", "0", iScale);
        }else{
        	try {
				dTest = Double.parseDouble(sTestDouble.replace(",", ""));
			} catch (NumberFormatException e) {
				super.addErrorMessage(sDoubleLabel + " value (" + sTestDouble + ") is not valid.");
				return false;
			}
        }
        return true;
    }
    private boolean isBooleanIntValid(String sBooleanLabel, String sBooleanInt){
    	sBooleanInt = sBooleanInt.trim();
        if (
        	(sBooleanInt.compareToIgnoreCase("0") != 0)
        	&& (sBooleanInt.compareToIgnoreCase("1") != 0)
        ){
  	       	super.addErrorMessage(sBooleanLabel + " '" + sBooleanInt + "' is not valid.");
   	       	return false;
        }
        return true;
    }
    private boolean isLongValid(String sLongLabel, String sLong, boolean bMustBeZeroOrGreater){
    	sLong = sLong.trim();
    	if (sLong.compareToIgnoreCase("") == 0){
    		super.addErrorMessage(sLongLabel + " is blank.");
    		return false;
    	}
    	long lTestLong = 0;
    	try {
			lTestLong = Long.parseLong(sLong);
		} catch (NumberFormatException e) {
    		super.addErrorMessage(sLongLabel + " (" + sLong + ") is invalid.");
    		return false;
		}
    	
		if (bMustBeZeroOrGreater){
	    	if (lTestLong < 0){
	    		super.addErrorMessage(sLongLabel + " is less than zero.");
	    		return false;
	    	}
		}
		return true;
    }
    private boolean isStringValid(String sStringLabel, String sString, int iMaxLength, boolean bAllowBlank){
    	sString = sString.trim();
        if (!bAllowBlank){
        	if (sString.compareToIgnoreCase("") == 0){
   	       		super.addErrorMessage(sStringLabel + " cannot be blank.");
   	       		return false;
        	}
        }
        if (sString.length() > iMaxLength){
   	       	super.addErrorMessage(sStringLabel + " is too long.");
   	       	return false;
        }
        return true;

    }
    private boolean isBigDecimalValid (String sStringLabel, String sDecimal, int iScale, boolean bMustBeZeroOrGreater){
    	@SuppressWarnings("unused")
        BigDecimal bdTest;
        if (sDecimal.compareToIgnoreCase("") == 0){
        	sDecimal = "0." + clsStringFunctions.PadLeft("", "0", iScale);
        }else{
        	try {
				bdTest = new BigDecimal(sDecimal.replace(",", ""));
			} catch (NumberFormatException e) {
				super.addErrorMessage(sStringLabel + " value (" + sDecimal + ") is not valid.");
				return false;
			}
        }
        return true;
    }
    public boolean load_line(Connection conn){
    	
    	boolean bResult = true;
    	//We need the trimmed order number and the detail number to identify a line:
    	if (!isLongValid("Trimmed order number", m_strimmedordernumber, true)){
    		return false;
    	}
    	
    	if (!isLongValid("Detail number", m_iDetailNumber, true)){
    		return false;
    	}
    	
    	String SQL = "SELECT * FROM " + SMTableorderdetails.TableName
    		+ " WHERE ("
    			+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + getM_strimmedordernumber() + "')"
    			+ " AND (" + SMTableorderdetails.iDetailNumber + " = " + getM_iDetailNumber() + ")"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setM_datDetailExpectedShipDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datDetailExpectedShipDate)));
				setM_datLineBookedDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datLineBookedDate)));
				setM_dExtendedOrderCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderCost))));
				setM_dExtendedOrderPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderPrice))));
				setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderdetails.bdEstimatedUnitCostScale, new BigDecimal(rs.getDouble(SMTableorderdetails.bdEstimatedUnitCost))));
				setM_dOrderUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitCost))));
				setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitPrice))));
				setM_dOriginalQty(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dOriginalQty))));
				setM_dQtyOrdered(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyOrdered))));
				setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShipped))));
				setM_dQtyShippedToDate(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShippedToDate))));
				setM_dUniqueOrderID(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					0, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyOrdered))));
				
				double dUniqueOrderID = rs.getDouble(SMTableorderdetails.dUniqueOrderID);
				long lUniqueOrderID = (new Double(dUniqueOrderID)).longValue();
				setM_dUniqueOrderID(Long.toString(lUniqueOrderID));
				setM_iDetailNumber(Long.toString(rs.getLong(SMTableorderdetails.iDetailNumber)));
				setM_iIsStockItem(Long.toString(rs.getLong(SMTableorderdetails.iIsStockItem)));
				setM_iLineNumber(Long.toString(rs.getLong(SMTableorderdetails.iLineNumber)));
				setM_iTaxable(Long.toString(rs.getLong(SMTableorderdetails.iTaxable)));
				setM_mInvoiceComments(rs.getString(SMTableorderdetails.mInvoiceComments));
				setM_mInternalComments(rs.getString(SMTableorderdetails.mInternalComments));
				setM_mTicketComments(rs.getString(SMTableorderdetails.mTicketComments));
				setM_sItemCategory(rs.getString(SMTableorderdetails.sItemCategory));
				setM_sItemDesc(rs.getString(SMTableorderdetails.sItemDesc));
				setM_sItemNumber(rs.getString(SMTableorderdetails.sItemNumber));
				setM_sLabel(rs.getString(SMTableorderdetails.sLabel));
				setM_sLocationCode(rs.getString(SMTableorderdetails.sLocationCode));
				setM_sMechFullName(rs.getString(SMTableorderdetails.sMechFullName));
				setM_sMechInitial(rs.getString(SMTableorderdetails.sMechInitial));
				setM_sMechID(Long.toString(rs.getLong(SMTableorderdetails.imechid)));
				setM_sOrderUnitOfMeasure(rs.getString(SMTableorderdetails.sOrderUnitOfMeasure));
				setM_strimmedordernumber(rs.getString(SMTableorderdetails.strimmedordernumber));
				setM_isuppressdetailoninvoice(Integer.toString(rs.getInt(SMTableorderdetails.isuppressdetailoninvoice)));
				setM_iprintondeliveryticket(Integer.toString(rs.getInt(SMTableorderdetails.iprintondeliveryticket)));
			}else{
				super.addErrorMessage("No order detail record found for order number '" + getM_strimmedordernumber()
					+ "', detail number '" + getM_iDetailNumber() + "'." 
				);
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading order detail with SQL: " + SQL + " - " + e.getMessage() + ".");
			bResult = false;
		}
    	return bResult;
    }
    public boolean load_line_using_line_number(Connection conn){
    	
    	boolean bResult = true;
    	//We need the trimmed order number and the detail number to identify a line:
    	if (!isLongValid("Trimmed order number", m_strimmedordernumber, true)){
    		return false;
    	}
    	
    	if (!isLongValid("Line number", m_iLineNumber, true)){
    		return false;
    	}
    	
    	String SQL = "SELECT * FROM " + SMTableorderdetails.TableName
    		+ " WHERE ("
    			+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + getM_strimmedordernumber() + "')"
    			+ " AND (" + SMTableorderdetails.iLineNumber + " = " + getM_iLineNumber() + ")"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setM_datDetailExpectedShipDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datDetailExpectedShipDate)));
				setM_datLineBookedDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datLineBookedDate)));
				setM_dExtendedOrderCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderCost))));
				setM_dExtendedOrderPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderPrice))));
				setM_dOrderUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitCost))));
				setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitPrice))));
				setM_dOriginalQty(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dOriginalQty))));
				setM_dQtyOrdered(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyOrdered))));
				setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShipped))));
				setM_dQtyShippedToDate(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShippedToDate))));
				setM_dUniqueOrderID(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					0, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyOrdered))));
				setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderdetails.bdEstimatedUnitCostScale, new BigDecimal(rs.getDouble(SMTableorderdetails.bdEstimatedUnitCost))));
				double dUniqueOrderID = rs.getDouble(SMTableorderdetails.dUniqueOrderID);
				long lUniqueOrderID = (new Double(dUniqueOrderID)).longValue();
				setM_dUniqueOrderID(Long.toString(lUniqueOrderID));
				setM_iDetailNumber(Long.toString(rs.getLong(SMTableorderdetails.iDetailNumber)));
				setM_iIsStockItem(Long.toString(rs.getLong(SMTableorderdetails.iIsStockItem)));
				setM_iLineNumber(Long.toString(rs.getLong(SMTableorderdetails.iLineNumber)));
				setM_iTaxable(Long.toString(rs.getLong(SMTableorderdetails.iTaxable)));
				setM_mInvoiceComments(rs.getString(SMTableorderdetails.mInvoiceComments));
				setM_mInternalComments(rs.getString(SMTableorderdetails.mInternalComments));
				setM_mTicketComments(rs.getString(SMTableorderdetails.mTicketComments));
				setM_sItemCategory(rs.getString(SMTableorderdetails.sItemCategory));
				setM_sItemDesc(rs.getString(SMTableorderdetails.sItemDesc));
				setM_sItemNumber(rs.getString(SMTableorderdetails.sItemNumber));
				setM_sLabel(rs.getString(SMTableorderdetails.sLabel));
				setM_sLocationCode(rs.getString(SMTableorderdetails.sLocationCode));
				setM_sMechFullName(rs.getString(SMTableorderdetails.sMechFullName));
				setM_sMechInitial(rs.getString(SMTableorderdetails.sMechInitial));
				setM_sMechID(Long.toString(rs.getLong(SMTableorderdetails.imechid)));
				setM_sOrderUnitOfMeasure(rs.getString(SMTableorderdetails.sOrderUnitOfMeasure));
				setM_strimmedordernumber(rs.getString(SMTableorderdetails.strimmedordernumber));
				setM_isuppressdetailoninvoice(Integer.toString(rs.getInt(SMTableorderdetails.isuppressdetailoninvoice)));
				setM_iprintondeliveryticket(Integer.toString(rs.getInt(SMTableorderdetails.iprintondeliveryticket)));
			}else{
				super.addErrorMessage("No order detail record found for order number '" + getM_strimmedordernumber()
					+ "', line number '" + getM_iLineNumber() + "'." 
				);
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading order detail using line number with SQL: " + SQL + " - " + e.getMessage() + ".");
			bResult = false;
		}
    	return bResult;
    }
    public boolean save_line_and_update_order(
			ServletContext context, 
			String sDBIB, 
			String sUserID,
			String sUserFullName,
			String sCompany,
			String sCallingClass,
			String sInsertLineAboveDetailNumber
    		){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    		context, 
    		sDBIB, 
    		"MySQL", 
    		this.toString() + ".save_line_and_update_order - user: " + sUserID + " - " + sUserFullName);
    	if (conn == null){
    		super.addErrorMessage("Error getting data connection.");
    		return false;
    	}
    	
    	//If it's a NEW detail being added, do that now:
    	if (this.getM_iDetailNumber().compareToIgnoreCase("-1") == 0){
    		SMOrderHeader order = new SMOrderHeader();
    		order.setM_strimmedordernumber(this.getM_strimmedordernumber());
    		if (!order.load(conn)){
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067712]");
    			super.addErrorMessage(order.getErrorMessages());
    			return false;
    		}
    		if (!order.addNewDetail(this, sUserID, sUserFullName, conn, sInsertLineAboveDetailNumber)){
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067713]");
    			super.addErrorMessage(order.getErrorMessages());
    			return false;
    		}else{
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067714]");
    			return true;
    		}
    	}
    	
    	//Otherwise, if we are updating an existing line, we go from here:
    	//Start a data transaction:
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067715]");
    		super.addErrorMessage("Error beginning data transaction - " + e.getMessage() + ".");
    		return false;
		}
    	
		//First, save the line:
		try {
			save_line(conn);
		} catch (SQLException e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067716]");
			super.addErrorMessage(e1.getMessage());
    		return false;
		}
		//Now load and save the entire order to update all the calculations:
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(getM_strimmedordernumber());
		if (!order.load(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067717]");
			super.addErrorMessage("Cannot load order - " + order.getErrorMessages());
    		return false;
		}
		
		if (!order.loadDetailLines(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067718]");
			super.addErrorMessage("Cannot load detail lines on order - " + order.getErrorMessages());
    		return false;
		}
		
		if (!order.save_order_without_data_transaction(conn, sDBIB, context, sUserID, sUserFullName, false, false, "EDITEDLINE")){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067719]");
			super.addErrorMessage("Cannot load detail lines on order - " + order.getErrorMessages());
    		return false;
		}
		
    	//Commit the data transaction:
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067720]");
    		super.addErrorMessage("Error committing data transaction - " + e.getMessage() + ".");
    		return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067721]");
    	return true;
    }
    public void save_line(Connection conn) throws SQLException{
    	if (!validate_line(conn)){
    		throw new SQLException(this.getErrorMessages());
    	}
    	String SQL = "INSERT INTO " + SMTableorderdetails.TableName + "("
    		+ SMTableorderdetails.datDetailExpectedShipDate
    		+ ", " + SMTableorderdetails.bdEstimatedUnitCost
    		+ ", " + SMTableorderdetails.datLineBookedDate
    		+ ", " + SMTableorderdetails.dExtendedOrderCost
    		+ ", " + SMTableorderdetails.dExtendedOrderPrice
    		+ ", " + SMTableorderdetails.dOrderUnitCost
    		+ ", " + SMTableorderdetails.dOrderUnitPrice
    		+ ", " + SMTableorderdetails.dOriginalQty
    		+ ", " + SMTableorderdetails.dQtyOrdered
    		+ ", " + SMTableorderdetails.dQtyShipped
    		+ ", " + SMTableorderdetails.dQtyShippedToDate
    		+ ", " + SMTableorderdetails.dUniqueOrderID
    		+ ", " + SMTableorderdetails.iDetailNumber
    		+ ", " + SMTableorderdetails.iIsStockItem
    		+ ", " + SMTableorderdetails.iLineNumber
    		+ ", " + SMTableorderdetails.imechid
    		+ ", " + SMTableorderdetails.iTaxable
    		+ ", " + SMTableorderdetails.mInvoiceComments
    		+ ", " + SMTableorderdetails.mInternalComments
    		+ ", " + SMTableorderdetails.mTicketComments
    		+ ", " + SMTableorderdetails.sItemCategory
    		+ ", " + SMTableorderdetails.sItemDesc
    		+ ", " + SMTableorderdetails.sItemNumber
    		+ ", " + SMTableorderdetails.sLabel
    		+ ", " + SMTableorderdetails.sLocationCode
    		+ ", " + SMTableorderdetails.sMechFullName
    		+ ", " + SMTableorderdetails.sMechInitial
    		+ ", " + SMTableorderdetails.sOrderUnitOfMeasure
    		+ ", " + SMTableorderdetails.strimmedordernumber
    		+ ", " + SMTableorderdetails.isuppressdetailoninvoice
    		+ ", " + SMTableorderdetails.iprintondeliveryticket
    	+ ") VALUES ("
    		+ "'" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datDetailExpectedShipDate()) + "'"
    		+ ", " + getM_bdEstimatedUnitCost().replace(",", "")
    		+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datLineBookedDate()) + "'"
    		+ ", " + getM_dExtendedOrderCost().replace(",", "")
    		+ ", " + getM_dExtendedOrderPrice().replace(",", "")
    		+ ", " + getM_dOrderUnitCost().replace(",", "")
    		+ ", " + getM_dOrderUnitPrice().replace(",", "")
    		+ ", " + getM_dQtyOrdered().replace(",", "") //When the detail is first inserted, the original
    													// qty ordered should be equal to the ordered qty
    		+ ", " + getM_dQtyOrdered().replace(",", "")
    		+ ", " + getM_dQtyShipped().replace(",", "")
    		+ ", " + getM_dQtyShippedToDate().replace(",", "")
    		+ ", " + getM_dUniqueOrderID()
    		+ ", " + getM_iDetailNumber()
    		+ ", " + getM_iIsStockItem()
    		+ ", " + getM_iLineNumber()
    		+ ", " + getM_sMechID()
    		+ ", " + getM_iTaxable()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInvoiceComments()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInternalComments()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_mTicketComments()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemCategory()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemDesc()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemNumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLabel()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLocationCode()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sMechFullName()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sMechInitial()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sOrderUnitOfMeasure()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_strimmedordernumber()) + "'"
    		+ ", " + clsDatabaseFunctions.FormatSQLStatement(getM_isuppressdetailoninvoice())
    		+ ", " + clsDatabaseFunctions.FormatSQLStatement(getM_iprintondeliveryticket())
    	+ ")"
    	
    	//Unique key is combination of uniqueifier and detailnumber
    	+ " ON DUPLICATE KEY UPDATE "
			+ SMTableorderdetails.datDetailExpectedShipDate + " = '" 
				+ clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datDetailExpectedShipDate()) + "'"
			+ ", " + SMTableorderdetails.datLineBookedDate + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datLineBookedDate()) + "'"
			+ ", " + SMTableorderdetails.bdEstimatedUnitCost + " = " + getM_bdEstimatedUnitCost().replace(",", "")
			+ ", " + SMTableorderdetails.dExtendedOrderCost + " = " + getM_dExtendedOrderCost().replace(",", "")
			+ ", " + SMTableorderdetails.dExtendedOrderPrice + " = " + getM_dExtendedOrderPrice().replace(",", "")
			+ ", " + SMTableorderdetails.dOrderUnitCost + " = " + getM_dOrderUnitCost().replace(",", "")
			+ ", " + SMTableorderdetails.dOrderUnitPrice + " = " + getM_dOrderUnitPrice().replace(",", "")
			+ ", " + SMTableorderdetails.dOriginalQty + " = " + getM_dOriginalQty().replace(",", "")
			+ ", " + SMTableorderdetails.dQtyOrdered + " = " + getM_dQtyOrdered().replace(",", "")
			+ ", " + SMTableorderdetails.dQtyShipped + " = " + getM_dQtyShipped().replace(",", "")
			+ ", " + SMTableorderdetails.dQtyShippedToDate + " = " + getM_dQtyShippedToDate().replace(",", "")
			+ ", " + SMTableorderdetails.dUniqueOrderID + " = " + getM_dUniqueOrderID()
			+ ", " + SMTableorderdetails.iDetailNumber + " = " + getM_iDetailNumber()
			+ ", " + SMTableorderdetails.iIsStockItem + " = " + getM_iIsStockItem()
			+ ", " + SMTableorderdetails.iLineNumber + " = " + getM_iLineNumber()
			+ ", " + SMTableorderdetails.imechid + " = " + getM_sMechID()
			+ ", " + SMTableorderdetails.iTaxable + " = " + getM_iTaxable()
			+ ", " + SMTableorderdetails.mInvoiceComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInvoiceComments()) + "'"
			+ ", " + SMTableorderdetails.mInternalComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInternalComments()) + "'"
			+ ", " + SMTableorderdetails.mTicketComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_mTicketComments()) + "'"
			+ ", " + SMTableorderdetails.sItemCategory + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemCategory()) + "'"
			+ ", " + SMTableorderdetails.sItemDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemDesc()) + "'"
			+ ", " + SMTableorderdetails.sItemNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sItemNumber()) + "'"
			+ ", " + SMTableorderdetails.sLabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLabel()) + "'"
			+ ", " + SMTableorderdetails.sLocationCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLocationCode()) + "'"
			+ ", " + SMTableorderdetails.sMechFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sMechFullName()) + "'"
			+ ", " + SMTableorderdetails.sMechInitial + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sMechInitial()) + "'"
			+ ", " + SMTableorderdetails.sOrderUnitOfMeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sOrderUnitOfMeasure()) + "'"
			+ ", " + SMTableorderdetails.strimmedordernumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_strimmedordernumber()) + "'"
			+ ", " + SMTableorderdetails.isuppressdetailoninvoice + " = " 
				+ clsDatabaseFunctions.FormatSQLStatement(getM_isuppressdetailoninvoice())
			+ ", " + SMTableorderdetails.iprintondeliveryticket + " = " 
				+ clsDatabaseFunctions.FormatSQLStatement(getM_iprintondeliveryticket())
    	;
    	if (bDebugMode){
    		System.out.println("[1332266358]: " + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + " " + SQL);
    		System.out.println("In " + this.toString() + ".save SQL = " + SQL + ".");
    	}
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new SQLException("Error inserting order detail line with SQL: " + SQL + " - " + e.getMessage());
		}
    }
    
	public String read_out_debug_data(){
		
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nDetail expected ship date: " + getM_datDetailExpectedShipDate();
    	sResult += "\nLine booked date: " + getM_datLineBookedDate();
    	sResult += "\nExtended order cost: " + getM_dExtendedOrderCost();
    	sResult += "\nExtended order price: " + getM_dExtendedOrderPrice();
    	//sResult += "\nLine tax amount: " + getM_dLineTaxAmount();
    	sResult += "\nOrder unit cost: " + getM_dOrderUnitCost();
    	sResult += "\nOrder unit price: " + getM_dOrderUnitPrice();
    	sResult += "\nOriginal qty: " + getM_dOriginalQty();
    	sResult += "\nQty ordered: " + getM_dQtyOrdered();
    	sResult += "\nQty shipped: " + getM_dQtyShipped();
    	sResult += "\nQty shipped to date: " + getM_dQtyShippedToDate();
    	sResult += "\nUnique order ID: " + getM_dUniqueOrderID();
    	sResult += "\nDetail number: " + getM_iDetailNumber();
    	sResult += "\nIs stock item: " + getM_iIsStockItem();
    	sResult += "\nLinenumber: " + getM_iLineNumber();
    	sResult += "\nTaxable: " + getM_iTaxable();
    	sResult += "\nInvoice comments: " + getM_mInvoiceComments();
    	sResult += "\nInternal comments: " + getM_mInternalComments();
    	sResult += "\nTicket comments: " + getM_mTicketComments();
    	sResult += "\nItem category: " + getM_sItemCategory();
    	sResult += "\nItem description: " + getM_sItemDesc();
    	sResult += "\nItem number: " + getM_sItemNumber();
    	sResult += "\nLabel: " + getM_sLabel();
    	sResult += "\nLocation: " + getM_sLocationCode();
    	sResult += "\nMechanic full name: " + getM_sMechFullName();
    	sResult += "\nMechanic initials: " + getM_sMechInitial();
    	sResult += "\nMech ID: " + this.getM_sMechID();
    	sResult += "\nOrder unit of measure: " + getM_sOrderUnitOfMeasure();
    	sResult += "\nTrimmed order number: " + getM_strimmedordernumber();
    	sResult += "\nSuppress line on invoice: " + getM_isuppressdetailoninvoice();
    	sResult += "\nPrint on delivery ticket: " + getM_iprintondeliveryticket();
    	sResult += "\nEstimated unit cost: " + getM_bdEstimatedUnitCost();
    	sResult += "\nObject name: " + ParamObjectName;
    	return sResult;
    	
    }

    public String getM_dUniqueOrderID() {
		return m_dUniqueOrderID;
	}
	public void setM_dUniqueOrderID(String mDUniqueOrderID) {
		m_dUniqueOrderID = mDUniqueOrderID;
	}
	public String getM_iDetailNumber() {
		return m_iDetailNumber;
	}
	public void setM_iDetailNumber(String mIDetailNumber) {
		m_iDetailNumber = mIDetailNumber;
	}
	
	
	public String getM_mInvoiceComments() {
		return m_mInvoiceComments;
	
	}
	public void setM_mInvoiceComments(String mMInvoiceComments) {
		
		if (mMInvoiceComments == null){
			m_mInvoiceComments = "";
		}else{
			m_mInvoiceComments = mMInvoiceComments;
		}
	}
	
	public String getM_mInternalComments() {
		return m_mInternalComments;
		
	}
	public void setM_mInternalComments(String mMInternalComments) {
		if (mMInternalComments == null){
			m_mInternalComments = "";
		}else{
			m_mInternalComments = mMInternalComments;
		}
	}
	
	/*public String getM_mInternalComments() {
		return m_mInternalComments;
	}
	public void setM_mInternalComments(String m_mInternalComments) {
		this.m_mInternalComments = m_mInternalComments;
	}
	*/
	public String getM_mTicketComments() {
		return m_mTicketComments;
	}
	public void setM_mTicketComments(String mMTicketComments) {
		if (mMTicketComments == null){
			m_mTicketComments = "";
		}else{
			m_mTicketComments = mMTicketComments;
		}
	}
	
	public String getM_iLineNumber() {
		return m_iLineNumber;
	}
	public void setM_iLineNumber(String mILineNumber) {
		m_iLineNumber = mILineNumber;
	}
	public String getM_sItemNumber() {
		return m_sItemNumber;
	}
	public void setM_sItemNumber(String mSItemNumber) {
		if (mSItemNumber == null){
			m_sItemNumber = "";
		}else{
			m_sItemNumber = mSItemNumber;
		}
	}
	public String getM_sItemDesc() {
		return m_sItemDesc;
	}
	public void setM_sItemDesc(String mSItemDesc) {
		if (mSItemDesc == null){
			m_sItemDesc = "";
		}else{
			m_sItemDesc = mSItemDesc;
		}
	}
	public String getM_sItemCategory() {
		return m_sItemCategory;
	}
	public void setM_sItemCategory(String mSItemCategory) {
		if (mSItemCategory == null){
			m_sItemCategory = "";
		}else{
			m_sItemCategory = mSItemCategory;
		}
	}
	public String getM_sLocationCode() {
		return m_sLocationCode;
	}
	public void setM_sLocationCode(String mSLocationCode) {
		if (mSLocationCode == null){
			m_sLocationCode = "";
		}else{
			m_sLocationCode = mSLocationCode;
		}
	}
	public String getM_datDetailExpectedShipDate() {
		return m_datDetailExpectedShipDate;
	}
	public void setM_datDetailExpectedShipDate(String mDatDetailExpectedShipDate) {
		m_datDetailExpectedShipDate = mDatDetailExpectedShipDate;
	}
	public String getM_iIsStockItem() {
		return m_iIsStockItem;
	}
	public void setM_iIsStockItem(String mIIsStockItem) {
		m_iIsStockItem = mIIsStockItem;
	}
	public String getM_dQtyOrdered() {
		return m_dQtyOrdered;
	}
	public void setM_dQtyOrdered(String mDQtyOrdered) {
		m_dQtyOrdered = mDQtyOrdered;
	}
	public String getM_dQtyShipped() {
		return m_dQtyShipped;
	}
	public void setM_dQtyShipped(String mDQtyShipped) {
		m_dQtyShipped = mDQtyShipped;
	}
	public String getM_dQtyShippedToDate() {
		return m_dQtyShippedToDate;
	}
	public void setM_dQtyShippedToDate(String mDQtyShippedToDate) {
		m_dQtyShippedToDate = mDQtyShippedToDate;
	}
	public String getM_dOriginalQty() {
		return m_dOriginalQty;
	}
	public void setM_dOriginalQty(String mDOriginalQty) {
		m_dOriginalQty = mDOriginalQty;
	}
	public String getM_sOrderUnitOfMeasure() {
		return m_sOrderUnitOfMeasure;
	}
	public void setM_sOrderUnitOfMeasure(String mSOrderUnitOfMeasure) {
		if (mSOrderUnitOfMeasure == null){
			m_sOrderUnitOfMeasure = "";
		}else{
			m_sOrderUnitOfMeasure = mSOrderUnitOfMeasure;
		}
	}
	public String getM_dOrderUnitPrice() {
		return m_dOrderUnitPrice;
	}
	public void setM_dOrderUnitPrice(String mDOrderUnitPrice) {
		m_dOrderUnitPrice = mDOrderUnitPrice;
	}
	public String getM_dOrderUnitCost() {
		return m_dOrderUnitCost;
	}
	public void setM_dOrderUnitCost(String mDOrderUnitCost) {
		m_dOrderUnitCost = mDOrderUnitCost;
	}
	public String getM_dExtendedOrderPrice() {
		return m_dExtendedOrderPrice;
	}
	public void setM_dExtendedOrderPrice(String mDExtendedOrderPrice) {
		m_dExtendedOrderPrice = mDExtendedOrderPrice;
	}
	public String getM_dExtendedOrderCost() {
		return m_dExtendedOrderCost;
	}
	public void setM_dExtendedOrderCost(String mDExtendedOrderCost) {
		m_dExtendedOrderCost = mDExtendedOrderCost;
	}
	public String getM_iTaxable() {
		return m_iTaxable;
	}
	public void setM_iTaxable(String mITaxable) {
		m_iTaxable = mITaxable;
	}
	public String getM_datLineBookedDate() {
		return m_datLineBookedDate;
	}
	public void setM_datLineBookedDate(String mDatLineBookedDate) {
		m_datLineBookedDate = mDatLineBookedDate;
	}
	public String getM_sMechID() {
		return m_imechid;
	}
	public void setM_sMechID(String sMechID) {
		if (sMechID == null){
			m_imechid = "0";
		}else{
			m_imechid = sMechID;
		}
	}
	public String getM_sMechInitial() {
		return m_sMechInitial;
	}
	public void setM_sMechInitial(String mSMechInitial) {
		if (mSMechInitial == null){
			m_sMechInitial = "";
		}else{
			m_sMechInitial = mSMechInitial;
		}
	}
	public String getM_sMechFullName() {
		return m_sMechFullName;
	}
	public void setM_sMechFullName(String mSMechFullName) {
		if (mSMechFullName == null){
			m_sMechFullName = "";
		}else{
			m_sMechFullName = mSMechFullName;
		}
	}
	public String getM_sLabel() {
		return m_sLabel;
	}
	public void setM_sLabel(String mSLabel) {
		if (mSLabel == null){
			m_sLabel = "";
		}else{
			m_sLabel = mSLabel;
		}
	}
	public String getM_strimmedordernumber() {
		return m_strimmedordernumber;
	}
	public void setM_strimmedordernumber(String mStrimmedordernumber) {
		if (mStrimmedordernumber == null){
			m_strimmedordernumber = "-1";
		}else{
			m_strimmedordernumber = mStrimmedordernumber;
		}
	}
	public String getM_isuppressdetailoninvoice(){
		return m_isuppressdetailoninvoice;
	}
	public void setM_isuppressdetailoninvoice(String sSuppressdetailoninvoice){
		m_isuppressdetailoninvoice = sSuppressdetailoninvoice;
	}
	public String getM_iprintondeliveryticket(){
		return m_iprintondeliveryticket;
	}
	public void setM_iprintondeliveryticket(String sPrintOnDeliveryTicket){
		m_iprintondeliveryticket = sPrintOnDeliveryTicket;
	}
	public ArrayList<String> getM_sErrorMessageArray() {
		return m_sErrorMessageArray;
	}
	public void setM_sErrorMessageArray(ArrayList<String> mSErrorMessageArray) {
		m_sErrorMessageArray = mSErrorMessageArray;
	}

	public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getM_bdEstimatedUnitCost() {
		return m_bdestimatedunitcost;
	}
	public void setM_bdEstimatedUnitCost(String sEstimatedUnitCost) {
		m_bdestimatedunitcost = sEstimatedUnitCost;
	}
    private void initDetailVariables(){
    	m_dUniqueOrderID = "-1";
    	m_iDetailNumber = "-1";
    	m_mInvoiceComments = "";
    	m_mInternalComments = "";
    	m_mTicketComments = "";
    	m_iLineNumber = "-1";
    	m_sItemNumber = "";
    	m_sItemDesc = "";
    	m_sItemCategory = "";
    	m_sLocationCode = "";
    	m_datDetailExpectedShipDate = "00/00/0000";
    	m_iIsStockItem = "1";
    	m_dQtyOrdered = "0.00";
    	m_dQtyShipped = "0.00";
    	m_dQtyShippedToDate = "0.00";
    	m_dOriginalQty = "0.00";
    	m_sOrderUnitOfMeasure = "EA";
    	m_dOrderUnitPrice = "0.00";
    	m_dOrderUnitCost = "0.00";
    	m_dExtendedOrderPrice = "0.00";
    	m_dExtendedOrderCost = "0.00";
    	m_iTaxable = "1";
    	m_datLineBookedDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	//m_dLineTaxAmount = "0.00";
    	m_sMechInitial = "N/A";
    	m_imechid = "0";
    	m_sMechFullName = "N/A";
    	m_sLabel = "";
    	m_strimmedordernumber = "";
    	m_isuppressdetailoninvoice = "0";
    	m_iprintondeliveryticket = "0";
    	m_bdestimatedunitcost = "0.0000";
    	m_sErrorMessageArray = new ArrayList<String> (0);
    	super.setObjectName(ParamObjectName);
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	
}
