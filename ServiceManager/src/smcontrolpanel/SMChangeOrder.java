package smcontrolpanel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMChangeOrder extends clsMasterEntry{
	public static final String ParamObjectName = "Change order";
	
	public static final String ParamiID = "id";
	public static final String ParamsJobNumber = "`Job Number`";
	public static final String ParamdChangeOrderNumber = "`Change Order Number`";
	public static final String ParamdatChangeOrderDate = "`Change Order Date`";
	public static final String ParamsDesc = "`Change Order Description`"; 
	public static final String ParamdAmount = "`Change Order Amount`"; 
	public static final String ParamdTotalMarkUp = "`Total MU`"; 
	public static final String ParamdTruckDays = "`Truck Days`"; 
	
	public String m_iID = "";
	public String m_sJobNumber = "";
	public String m_dChangeOrderNumber = "";
	public String m_datChangeOrderDate = "";
	public String m_sDesc = ""; 
	public String m_dAmount = ""; 
	public String m_dTotalMarkUp = ""; 
	public String m_dTruckDays = ""; 
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	
    public SMChangeOrder() {
		super();
		initBidVariables();
        }
    
	
	public String getM_iID() {
		return m_iID;
	}


	public void setM_iID(String mIID) {
		m_iID = mIID;
	}


	public String getM_sJobNumber() {
		return m_sJobNumber;
	}


	public void setM_sJobNumber(String mSJobNumber) {
		m_sJobNumber = mSJobNumber;
	}


	public String getM_dChangeOrderNumber() {
		return m_dChangeOrderNumber;
	}


	public void setM_dChangeOrderNumber(String mDChangeOrderNumber) {
		m_dChangeOrderNumber = mDChangeOrderNumber;
	}


	public String getM_datChangeOrderDate() {
		return m_datChangeOrderDate;
	}


	public void setM_datChangeOrderDate(String mDatChangeOrderDate) {
		m_datChangeOrderDate = mDatChangeOrderDate;
	}


	public String getM_sDesc() {
		return m_sDesc;
	}


	public void setM_sDesc(String mSDesc) {
		m_sDesc = mSDesc;
	}


	public String getM_dAmount() {
		return m_dAmount;
	}


	public void setM_dAmount(String mDAmount) {
		m_dAmount = mDAmount;
	}


	public String getM_dTotalMarkUp() {
		return m_dTotalMarkUp;
	}


	public void setM_dTotalMarkUp(String mDTotalMarkUp) {
		m_dTotalMarkUp = mDTotalMarkUp;
	}


	public String getM_dTruckDays() {
		return m_dTruckDays;
	}


	public void setM_dTruckDays(String mDTruckDays) {
		m_dTruckDays = mDTruckDays;
	}


	public ArrayList<String> getM_sErrorMessageArray() {
		return m_sErrorMessageArray;
	}


	public void setM_sErrorMessageArray(ArrayList<String> mSErrorMessageArray) {
		m_sErrorMessageArray = mSErrorMessageArray;
	}

	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nID: " + this.getM_iID();
    	sResult += "\nJob Number: " + this.getM_sJobNumber();
    	sResult += "\nChange order number: " + this.getM_dChangeOrderNumber();
    	sResult += "\nChange order date: " + this.getM_datChangeOrderDate();
    	sResult += "\nDesc: " + this.getM_sDesc();
    	sResult += "\nAmt: " + this.getM_dAmount();
    	sResult += "\nTotal MU: " + this.getM_dTotalMarkUp();
    	sResult += "\nTruck days: " + this.getM_dTruckDays();
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }

    public boolean validateEntry(){
    	
    	boolean bEntriesAreValid = true;
    	
    	m_sJobNumber = m_sJobNumber.trim().replace(" ", "");
        if (m_sJobNumber.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Order number cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_sJobNumber.length() > SMTablechangeorders.sJobNumberLength){
        	super.addErrorMessage("Order Number is too long.");
        	bEntriesAreValid = false;
        }
        
    	m_dChangeOrderNumber = m_dChangeOrderNumber.trim().replace(",", "");
        try {
			int iTest = Integer.parseInt(m_dChangeOrderNumber);
			if ((iTest <=0)){
				super.addErrorMessage("Change order number is invalid on '" + m_sDesc + "' change order.");
				bEntriesAreValid = false;
			}
        } catch (NumberFormatException e) {
        	super.addErrorMessage("'Change order number' (" + m_dChangeOrderNumber
        			+ ") is invalid.");
        	bEntriesAreValid = false;	
		}
    	
		m_datChangeOrderDate = m_datChangeOrderDate.trim();
		if(!isDateValid("Change order date", m_datChangeOrderDate)){bEntriesAreValid = false;};
		
        if (m_sDesc.length() > SMTablechangeorders.sDescriptionLength){
        	super.addErrorMessage("Description is too long.");
        	bEntriesAreValid = false;
        }
        if(!isDoubleValid("Amount", m_dAmount, SMTablechangeorders.dAmountScale)){bEntriesAreValid = false;};
        if(!isDoubleValid("Total MU", m_dTotalMarkUp, SMTablechangeorders.dTotalMarkUPScale)){bEntriesAreValid = false;};
        if(!isDoubleValid("Truck days", m_dTruckDays, SMTablechangeorders.dTruckDaysScale)){bEntriesAreValid = false;};
        
    	return bEntriesAreValid;
    }

    public boolean save_without_data_transaction (Connection conn, String sUser){

    	if (!validateEntry()){
    		return false;
    	}
    	
    	String SQL = "";

    	//First check to see if this job number already exists:
    	SQL = "SELECT"
    		+ " " + SMTableorderheaders.sOrderNumber
    		+ " FROM " + SMTableorderheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableorderheaders.sOrderNumber + " = '" 
    			+ clsStringFunctions.PadLeft(m_sJobNumber.trim(), " ", 8) + "')"
    			+ " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()) {
	    	    super.addErrorMessage("Order number" + " " + m_sJobNumber + " does not exist.");
	    	    return false;
			}
			rs.close();
		} catch (SQLException e) {
    	    super.addErrorMessage("Error checking for existing " + "order number" + ": " + e.getMessage());
    	    return false;
		}
		SQL = "INSERT INTO " + SMTablechangeorders.TableName + " ("
			+ SMTablechangeorders.dAmount
			+ ", " + SMTablechangeorders.datChangeOrderDate
			+ ", " + SMTablechangeorders.dChangeOrderNumber
			+ ", " + SMTablechangeorders.dTotalMarkUp
			+ ", " + SMTablechangeorders.dTruckDays
			+ ", " + SMTablechangeorders.sDesc
			+ ", " + SMTablechangeorders.sJobNumber
			+ ") VALUES ("
			+ m_dAmount.replace(",", "")
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datChangeOrderDate) + "'"
			+ ", " + m_dChangeOrderNumber.replace(",", "")
			+ ", " + m_dTotalMarkUp.replace(",", "")
			+ ", " + m_dTruckDays.replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDesc.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sJobNumber.trim()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTablechangeorders.dAmount
				+ " = " + m_dAmount.replace(",", "")
			+ ", " + SMTablechangeorders.datChangeOrderDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datChangeOrderDate) + "'"
			+ ", " + SMTablechangeorders.dChangeOrderNumber + " = " + m_dChangeOrderNumber.replace(",", "")
			+ ", " + SMTablechangeorders.dTotalMarkUp + " = " + m_dTotalMarkUp.replace(",", "")
			+ ", " + SMTablechangeorders.dTruckDays + " = " + m_dTruckDays.replace(",", "")
			+ ", " + SMTablechangeorders.sDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDesc.trim()) + "'"
			+ ", " + SMTablechangeorders.sJobNumber + " = '" + m_sJobNumber.trim() + "'"
			;

    	try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		System.out.println(this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}
    	}catch(SQLException ex){
    	    super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
    	    return false;
    	}
    	
    	return true;
    }

    private boolean isDateValid(String sDateLabel, String sTestDate){
        if (sTestDate.compareTo(EMPTY_DATE_STRING) == 0){
        	super.addErrorMessage("Date cannot be blank.");
        	return false;
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
				// TODO Auto-generated catch block
				super.addErrorMessage(sDoubleLabel + " value (" + sTestDouble + ") is not valid.");
				return false;
			}
        }
        return true;
    }
    private void initBidVariables(){
    	
    	m_iID = "-1";
    	m_sJobNumber = "";
    	m_dChangeOrderNumber = "";
    	m_datChangeOrderDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_sDesc = ""; 
    	m_dAmount = "0.00"; 
    	m_dTotalMarkUp = "0.00"; 
    	m_dTruckDays = "0.0000"; 
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
