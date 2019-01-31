package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import SMDataDefinition.SMTableinvoicedetails;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;

public class SMInvoiceDetail extends clsMasterEntry{ //java.lang.Object{
	
	private int m_iIsStockItem;
	private BigDecimal m_dExtendedPrice;
	private BigDecimal m_dQtyShipped;
	private BigDecimal m_dUnitPrice;
	private int m_iDetailNumber;
	private int m_iTaxable;
	private int m_iLaborItem;
	private int m_iLineNumber;
	private String m_sDetailInvoiceComment;
	private String m_sDesc;
	private String m_sInvoiceNumber;
	private String m_sItemCategory;
	private String m_sItemNumber;
	private String m_sLocationCode;
	private String m_sUnitOfMeasure;
	private String m_sInventoryGLAcct;
	private String m_sExpenseGLAcct;
	private String m_sRevenueGLAcct;
	private BigDecimal m_dExpensedCost;
	private BigDecimal m_dExtendedCost;
	private BigDecimal m_dExtendedPriceAfterDiscount;
	private BigDecimal m_dLineSalesTaxAmount;
	private int m_iMatchingInvoiceLineNumber;
	private String m_sMechID;
	private String m_sMechInitial;
	private String m_sMechFullName;
	private String m_sLabel;
	private long m_lictransactionid;
	private int m_isuppressdetailoninvoice;

	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    //============ Constructor
	public SMInvoiceDetail(
        ) {
    	m_iIsStockItem = 0;
    	m_dExtendedPrice = new BigDecimal(0);
    	m_dQtyShipped = new BigDecimal(0);
    	m_dUnitPrice = new BigDecimal(0);
    	m_iDetailNumber = 0;
    	m_iTaxable = 0;
    	m_iLaborItem = 0;
    	m_iLineNumber = 0;
    	m_sDetailInvoiceComment = "";
    	m_sDesc = "";
    	m_sInvoiceNumber = "";
    	m_sItemCategory = "";
    	m_sItemNumber = "";
    	m_sLocationCode = "";
    	m_sUnitOfMeasure = "";
    	m_sInventoryGLAcct = "";
    	m_sExpenseGLAcct = "";
    	m_sRevenueGLAcct = "";
    	m_dExpensedCost = new BigDecimal(0);
    	m_dExtendedCost = new BigDecimal(0);
    	m_dExtendedPriceAfterDiscount = new BigDecimal(0);
    	m_dLineSalesTaxAmount = new BigDecimal(0);
    	m_iMatchingInvoiceLineNumber = 0;
    	m_sMechID = "0";
    	m_sMechInitial = "";
    	m_sMechFullName = "";
    	m_sLabel = "";
    	m_lictransactionid = 0;
    	m_isuppressdetailoninvoice = 0;

	}
	
	//load invoice detail
    public boolean load_line(Connection conn){
    	
    	//TODO
    	boolean bResult = true;

    	String SQL = "SELECT * FROM " + SMTableinvoicedetails.TableName
    		+ " WHERE ("
    			+ "(" + SMTableinvoicedetails.sInvoiceNumber + " = '" + getM_sInvoiceNumber() + "')"
    			+ " AND (" + SMTableinvoicedetails.iDetailNumber + " = '" + getM_iDetailNumber() + "')"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
			
				m_iIsStockItem = rs.getInt(SMTableinvoicedetails.iIsStockItem);
				m_dExtendedPrice = rs.getBigDecimal(SMTableinvoicedetails.dExtendedPrice);
				m_dQtyShipped = rs.getBigDecimal(SMTableinvoicedetails.dQtyShipped);
				m_dUnitPrice = rs.getBigDecimal(SMTableinvoicedetails.dUnitPrice);
				m_iDetailNumber = rs.getInt(SMTableinvoicedetails.iDetailNumber);
				m_iTaxable = rs.getInt(SMTableinvoicedetails.iTaxable);
				m_iLaborItem = rs.getInt(SMTableinvoicedetails.ilaboritem);
				m_iLineNumber = rs.getInt(SMTableinvoicedetails.iLineNumber);
				m_sDetailInvoiceComment = rs.getString(SMTableinvoicedetails.mDetailInvoiceComment);
				m_sDesc = rs.getString(SMTableinvoicedetails.sDesc);
				m_sInvoiceNumber = rs.getString(SMTableinvoicedetails.sInvoiceNumber);
				m_sItemCategory = rs.getString(SMTableinvoicedetails.sItemCategory);
				m_sItemNumber = rs.getString(SMTableinvoicedetails.sItemNumber);
				m_sLocationCode = rs.getString(SMTableinvoicedetails.sLocationCode);
				m_sUnitOfMeasure = rs.getString(SMTableinvoicedetails.sUnitOfMeasure);
				m_sInventoryGLAcct = rs.getString(SMTableinvoicedetails.sInventoryGLAcct);
				m_sExpenseGLAcct = rs.getString(SMTableinvoicedetails.sExpenseGLAcct);
				m_sRevenueGLAcct = rs.getString(SMTableinvoicedetails.sExpenseGLAcct);
				m_dExpensedCost = rs.getBigDecimal(SMTableinvoicedetails.bdexpensedcost);
				m_dExtendedCost = rs.getBigDecimal(SMTableinvoicedetails.dExtendedCost);
				m_dExtendedPriceAfterDiscount = rs.getBigDecimal(SMTableinvoicedetails.dExtendedPriceAfterDiscount);
				m_dLineSalesTaxAmount = rs.getBigDecimal(SMTableinvoicedetails.bdlinesalestaxamount);
				m_iMatchingInvoiceLineNumber = rs.getInt(SMTableinvoicedetails.iMatchingInvoiceLineNumber);
				m_sMechID = Long.toString(rs.getLong(SMTableinvoicedetails.imechid));
				m_sMechInitial = rs.getString(SMTableinvoicedetails.sMechInitial);
				m_sMechFullName = rs.getString(SMTableinvoicedetails.sMechFullName);
				m_sLabel = rs.getString(SMTableinvoicedetails.sLabel);
				m_lictransactionid = rs.getLong(SMTableinvoicedetails.lictransactionid);
				m_isuppressdetailoninvoice = rs.getInt(SMTableinvoicedetails.isuppressdetailoninvoice);

			}else{
				super.addErrorMessage("No invoice detail record found for invoice number '" + getM_sInvoiceNumber()
					+ "', detail number '" + getM_iDetailNumber() + "'." 
				);
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading invoice detail with SQL: " + SQL + " - " + e.getMessage() + ".");
			bResult = false;
		}
    	return bResult;
    }
	
	//save invoice detail
    public boolean save_line(Connection conn){
    /*	
    	if (!validate_line(conn)){
    		return false;
    	}
    */
    	String SQL = "INSERT INTO " + SMTableinvoicedetails.TableName + "("
    		+ SMTableinvoicedetails.iIsStockItem
    		+ ", " + SMTableinvoicedetails.dExtendedPrice
    		+ ", " + SMTableinvoicedetails.dQtyShipped
    		+ ", " + SMTableinvoicedetails.dUnitPrice
    		+ ", " + SMTableinvoicedetails.iDetailNumber
    		+ ", " + SMTableinvoicedetails.iTaxable
    		+ ", " + SMTableinvoicedetails.ilaboritem
    		+ ", " + SMTableinvoicedetails.iLineNumber
    		+ ", " + SMTableinvoicedetails.mDetailInvoiceComment
    		+ ", " + SMTableinvoicedetails.sDesc
    		+ ", " + SMTableinvoicedetails.sInvoiceNumber
    		+ ", " + SMTableinvoicedetails.sItemCategory
    		+ ", " + SMTableinvoicedetails.sItemNumber
    		+ ", " + SMTableinvoicedetails.sLocationCode
    		+ ", " + SMTableinvoicedetails.sUnitOfMeasure
    		+ ", " + SMTableinvoicedetails.sInventoryGLAcct
    		+ ", " + SMTableinvoicedetails.sRevenueGLAcct
    		+ ", " + SMTableinvoicedetails.sExpenseGLAcct
    		+ ", " + SMTableinvoicedetails.bdexpensedcost
    		+ ", " + SMTableinvoicedetails.dExtendedCost
    		+ ", " + SMTableinvoicedetails.dExtendedPriceAfterDiscount
    		+ ", " + SMTableinvoicedetails.bdlinesalestaxamount
    		+ ", " + SMTableinvoicedetails.iMatchingInvoiceLineNumber
    		+ ", " + SMTableinvoicedetails.sLabel
    		+ ", " + SMTableinvoicedetails.sMechFullName
    		+ ", " + SMTableinvoicedetails.sMechInitial
    		+ ", " + SMTableinvoicedetails.imechid
    		+ ", " + SMTableinvoicedetails.lictransactionid
    		+ ", " + SMTableinvoicedetails.isuppressdetailoninvoice
    	+ ") VALUES ("
    		+ m_iIsStockItem 
    		+ ", " + m_dExtendedPrice
    		+ ", " + m_dQtyShipped
    		+ ", " + m_dUnitPrice
    		+ ", " + m_iDetailNumber
    		+ ", " + m_iTaxable
    		+ ", " + m_iLaborItem
    		+ ", " + m_iLineNumber
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDetailInvoiceComment) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDesc) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sInvoiceNumber) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemCategory) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemNumber) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLocationCode) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sUnitOfMeasure) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sInventoryGLAcct) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sRevenueGLAcct) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sExpenseGLAcct) + "'"
    		+ ", " + m_dExpensedCost
    		+ ", " + m_dExtendedCost
    		+ ", " + m_dExtendedPriceAfterDiscount
    		+ ", " + m_dLineSalesTaxAmount
    		+ ", " + m_iMatchingInvoiceLineNumber
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLabel) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sMechFullName) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sMechInitial) + "'"
    		+ ", " + m_sMechID
    		+ ", " + m_lictransactionid
    		+ ", " + m_isuppressdetailoninvoice
    	+ ")"
    	
    	//Unique key is combination of uniqueifier and detailnumber
    	+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableinvoicedetails.iIsStockItem + " = " + m_iIsStockItem
			+ ", " + SMTableinvoicedetails.dExtendedPrice + " = " + m_dExtendedPrice
			+ ", " + SMTableinvoicedetails.dQtyShipped + " = " + m_dQtyShipped
			+ ", " + SMTableinvoicedetails.dUnitPrice + " = " + m_dUnitPrice
			+ ", " + SMTableinvoicedetails.iDetailNumber + " = " + m_iDetailNumber
			+ ", " + SMTableinvoicedetails.iTaxable + " = " + m_iTaxable
			+ ", " + SMTableinvoicedetails.ilaboritem + " = " + m_iLaborItem
			+ ", " + SMTableinvoicedetails.iLineNumber + " = " + m_iLineNumber
			+ ", " + SMTableinvoicedetails.mDetailInvoiceComment + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDetailInvoiceComment) + "'"
			+ ", " + SMTableinvoicedetails.sDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDesc) + "'"
			+ ", " + SMTableinvoicedetails.sInvoiceNumber + " = " + m_sInvoiceNumber
			+ ", " + SMTableinvoicedetails.sItemCategory + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemCategory) + "'"
			+ ", " + SMTableinvoicedetails.sItemNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sItemNumber) + "'"
			+ ", " + SMTableinvoicedetails.sLocationCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLocationCode) + "'"
			+ ", " + SMTableinvoicedetails.sUnitOfMeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sUnitOfMeasure) + "'"
			+ ", " + SMTableinvoicedetails.sInventoryGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sInventoryGLAcct) + "'"
			+ ", " + SMTableinvoicedetails.sExpenseGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sExpenseGLAcct) + "'"
			+ ", " + SMTableinvoicedetails.sRevenueGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sRevenueGLAcct) + "'"
			+ ", " + SMTableinvoicedetails.bdexpensedcost + " = " + m_dExpensedCost
			+ ", " + SMTableinvoicedetails.dExtendedCost + " = " + m_dExtendedCost
			+ ", " + SMTableinvoicedetails.dExtendedPriceAfterDiscount + " = " + m_dExtendedPriceAfterDiscount
			+ ", " + SMTableinvoicedetails.bdlinesalestaxamount + " = " + m_dLineSalesTaxAmount
			+ ", " + SMTableinvoicedetails.iMatchingInvoiceLineNumber + " = " + m_iMatchingInvoiceLineNumber
			+ ", " + SMTableinvoicedetails.sLabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLabel()) + "'"
			+ ", " + SMTableinvoicedetails.sMechFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sMechFullName) + "'"
			+ ", " + SMTableinvoicedetails.sMechInitial + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sMechInitial) + "'"
			+ ", " + SMTableinvoicedetails.imechid + " = " + m_sMechID
			+ ", " + SMTableinvoicedetails.lictransactionid + " = " + m_lictransactionid
			+ ", " + SMTableinvoicedetails.isuppressdetailoninvoice + " = " + m_isuppressdetailoninvoice
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			super.addErrorMessage("Error inserting invoice detail line with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
    	
    	return true;
    }
    

	public int getM_iIsStockItem() {
		return m_iIsStockItem;
	}

	public void setM_iIsStockItem(int isStockItem) {
		m_iIsStockItem = isStockItem;
	}

	public BigDecimal getM_dExtendedPrice() {
		return m_dExtendedPrice;
	}

	public void setM_dExtendedPrice(BigDecimal extendedPrice) {
		m_dExtendedPrice = extendedPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getM_dQtyShipped() {
		return m_dQtyShipped;
	}

	public void setM_dQtyShipped(BigDecimal qtyShipped) {
		m_dQtyShipped = qtyShipped;
	}

	public BigDecimal getM_dUnitPrice() {
		return m_dUnitPrice;
	}

	public void setM_dUnitPrice(BigDecimal unitPrice) {
		m_dUnitPrice = unitPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public int getM_iDetailNumber() {
		return m_iDetailNumber;
	}

	public void setM_iDetailNumber(int detailNumber) {
		m_iDetailNumber = detailNumber;
	}

	public int get_iLaborItem() {
		return m_iLaborItem;
	}

	public void set_iLaborItem(int iLaborItem) {
		m_iLaborItem = iLaborItem;
	}

	public int getM_iTaxable() {
		return m_iTaxable;
	}

	public void setM_iTaxable(int taxable) {
		m_iTaxable = taxable;
	}

	public int getM_iLineNumber() {
		return m_iLineNumber;
	}

	public void setM_iLineNumber(int lineNumber) {
		m_iLineNumber = lineNumber;
	}

	public String getM_sDetailInvoiceComment() {
		return m_sDetailInvoiceComment;
	}

	public void setM_sDetailInvoiceComment(String detailInvoiceComment) {
		m_sDetailInvoiceComment = detailInvoiceComment;
	}

	public String getM_sDesc() {
		return m_sDesc;
	}

	public void setM_sDesc(String desc) {
		m_sDesc = desc;
	}

	public String getM_sInvoiceNumber() {
		return m_sInvoiceNumber;
	}

	public void setM_sInvoiceNumber(String invoiceNumber) {
		m_sInvoiceNumber = invoiceNumber;
	}

	public String getM_sItemCategory() {
		return m_sItemCategory;
	}

	public void setM_sItemCategory(String itemCategory) {
		m_sItemCategory = itemCategory;
	}

	public String getM_sItemNumber() {
		return m_sItemNumber;
	}

	public void setM_sItemNumber(String itemNumber) {
		m_sItemNumber = itemNumber;
	}

	public String getM_sLocationCode() {
		return m_sLocationCode;
	}

	public void setM_sLocationCode(String locationCode) {
		m_sLocationCode = locationCode;
	}

	public String getM_sUnitOfMeasure() {
		return m_sUnitOfMeasure;
	}

	public void setM_sUnitOfMeasure(String unitOfMeasure) {
		m_sUnitOfMeasure = unitOfMeasure;
	}

	public String getM_sInventoryGLAcct() {
		return m_sInventoryGLAcct;
	}

	public void setM_sInventoryGLAcct(String inventoryGLAcct) {
		m_sInventoryGLAcct = inventoryGLAcct;
	}

	public String getM_sExpenseGLAcct() {
		return m_sExpenseGLAcct;
	}

	public void setM_sExpenseGLAcct(String expenseGLAcct) {
		m_sExpenseGLAcct = expenseGLAcct;
	}

	public String getM_sRevenueGLAcct() {
		return m_sRevenueGLAcct;
	}

	public void setM_sRevenueGLAcct(String revenueGLAcct) {
		m_sRevenueGLAcct = revenueGLAcct;
	}

	public BigDecimal getM_dExtendedCost() {
		return m_dExtendedCost;
	}

	public void setM_dExtendedCost(BigDecimal extendedCost) {
		m_dExtendedCost = extendedCost.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public BigDecimal getM_dExpensedCost() {
		return m_dExpensedCost;
	}

	public void setM_dExpensedCost(BigDecimal expensedCost) {
		m_dExpensedCost = expensedCost.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getM_dExtendedPriceAfterDiscount() {
		return m_dExtendedPriceAfterDiscount;
	}

	public void setM_dExtendedPriceAfterDiscount(
			BigDecimal extendedPriceAfterDiscount) {
		m_dExtendedPriceAfterDiscount = extendedPriceAfterDiscount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getbdLineSalesTaxAmount() {
		return m_dLineSalesTaxAmount;
	}

	public void setbdLineSalesTaxAmount(BigDecimal lineSalesTaxAmount) {
		m_dLineSalesTaxAmount = lineSalesTaxAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public int getM_iMatchingInvoiceLineNumber() {
		return m_iMatchingInvoiceLineNumber;
	}

	public void setM_iMatchingInvoiceLineNumber(int matchingInvoiceLineNumber) {
		m_iMatchingInvoiceLineNumber = matchingInvoiceLineNumber;
	}

	public int getM_iSuppressDetailOnInvoice() {
		return m_isuppressdetailoninvoice;
	}

	public void setM_iSuppressDetailOnInvoice(int SuppressDetailOnInvoice) {
		m_isuppressdetailoninvoice = SuppressDetailOnInvoice;
	}

	public String getM_sMechID() {
		return m_sMechID;
	}

	public void setM_sMechID(String mechID) {
		m_sMechID = mechID;
	}

	public String getM_sMechInitial() {
		return m_sMechInitial;
	}

	public void setM_sMechInitial(String mechInitial) {
		m_sMechInitial = mechInitial;
	}

	public String getM_sMechFullName() {
		return m_sMechFullName;
	}

	public void setM_sMechFullName(String mechFullName) {
		m_sMechFullName = mechFullName;
	}

	public String getM_sLabel() {
		return m_sLabel;
	}

	public void setM_sLabel(String label) {
		m_sLabel = label;
	}
	
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
