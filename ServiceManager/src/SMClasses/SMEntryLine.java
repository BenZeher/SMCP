package SMClasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import java.math.BigDecimal;
import SMDataDefinition.SMTableentrylines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class SMEntryLine extends java.lang.Object{
	private long m_lid;
	private int m_iBatchNumber;
	private int m_iEntryNumber;
	private int m_iLineNumber;
	private String m_sDocAppliedTo;
	private String m_sGLAcct;
	private String m_sDescription;
	private BigDecimal m_dAmount;
	private String m_sComment;
	private long m_lDocAppliedToId;
	private long m_lEntryId;
	private String m_sApplyToOrderNumber;
    //============ Constructor
	public SMEntryLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber,
    		String sEntryId
        ) {
    	sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
    	sLineNumber(sLineNumber);
    	m_sDocAppliedTo = "UNAPPLIED";
    	m_sGLAcct = "";
    	m_sDescription = "INITIALIZED LINE";
    	m_dAmount = BigDecimal.valueOf(0);
    	m_sComment = "";
    	m_lDocAppliedToId = -1;
    	m_sApplyToOrderNumber = "0";
    	sEntryId(sEntryId);
        }
	public SMEntryLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber
        ) {
    	sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
    	sLineNumber(sLineNumber);
    	m_lid = -1;
    	m_sDocAppliedTo = "UNAPPLIED";
    	m_sGLAcct = "";
    	m_sDescription = "INITIALIZED LINE";
    	m_dAmount = BigDecimal.valueOf(0);
    	m_sComment = "";
    	m_lDocAppliedToId = -1;
    	m_lEntryId = -1;
    	m_sApplyToOrderNumber = "0";
        }
    //Use this constructor when adding a new line:
	public SMEntryLine(
    		String sBatchNumber,
    		String sEntryNumber
        ) {
    	sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
    	m_iLineNumber = -1;
    	m_lid = -1;
    	m_sDocAppliedTo = "UNAPPLIED";
    	m_sGLAcct = "";
    	m_sDescription = "INITIALIZED LINE";
    	m_dAmount = BigDecimal.valueOf(0);
    	m_sComment = "";
    	m_lDocAppliedToId = -1;
    	m_lEntryId = -1;
    	m_sApplyToOrderNumber = "0";
        }
	public SMEntryLine(
        ) {
    	m_iBatchNumber = -1;
    	m_iEntryNumber = -1;
    	m_iLineNumber = -1;
    	m_lid = -1;
    	m_sDocAppliedTo = "UNAPPLIED";
    	m_sGLAcct = "";
    	m_sDescription = "INITIALIZED LINE";
    	m_dAmount = BigDecimal.valueOf(0);
    	m_sComment = "";
    	m_lDocAppliedToId = -1;
    	m_lEntryId = -1;
    	m_sApplyToOrderNumber = "0";
        }
    //Methods:
    public boolean save (ServletContext context, String sDBID){
    	
    	String SQL = "";
    	if (m_iLineNumber == -1){
	    	//Add a new line:
	    	SQL = TRANSACTIONSQLs.Get_Last_Line_Number(sBatchNumber(), sEntryNumber());
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID);
		    	
		    	if (rs.next()){
		    		iLineNumber(rs.getInt(SMTableentrylines.ilinenumber) + 1);
		    	}
		    	else{
		    		iLineNumber(1);
		    	}
		    	rs.close();
	        }catch (SQLException ex){
	    		System.out.println("[1579186014] Error in " + this.toString() + "save class!!");
	    	    System.out.println("SQLException: " + ex.getMessage());
	    	    System.out.println("SQLState: " + ex.getSQLState());
	    	    System.out.println("SQL: " + ex.getErrorCode());
	    	    return false;
	    	}
	        
	        //Now add a new record:
	    	SQL = TRANSACTIONSQLs.Add_TransactionLine(
    			sBatchNumber(),  
    			sEntryNumber(),
    			sLineNumber(), 
    			sDocAppliedTo(), 
    			sGLAcct(),
    			sDescription(),
    			sAmountSQLFormat(), 
    			sComment(), 
    			sDocAppliedToId(), 
    			sEntryId(),
    			getApplyToOrderNumber()
    			);
    	}else{

    	SQL = TRANSACTIONSQLs.Update_TransactionLine(
			sBatchNumber(),  
			sEntryNumber(),
			sLineNumber(), 
			sAmountSQLFormat(),
			sDocAppliedToId(), 
			sEntryId(),
			sComment(), 
			sDescription(), 
			sDocAppliedTo(), 
			sGLAcct(),
			getApplyToOrderNumber()
			);
    	}
    	
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
	    		System.out.println("[1579186020] Could not complete update transaction - " + "entry was not updated.<BR>");
	    	}else{
	    		
	    		System.out.println("[1579186029] Successfully updated " + "entry: " + sEntryNumber() + ".");
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579186032] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
    	}
    	return true;
    }
    public boolean load (
		String sBatchNumber,
		String sEntryNumber,
		String sLineNumber,
		ServletContext context, 
		String sDBID
		){
    
	    if (! sBatchNumber(sBatchNumber)){
	    	System.out.println("[1579186038] Invalid sBatchNumber - " + sBatchNumber);
	    	return false;
	    }
	
	    if (! sEntryNumber(sEntryNumber)){
	    	System.out.println("[1579186042] Invalid sEntryNumber - " + sEntryNumber);
	    	return false;
	    }
	    
	    if (! sLineNumber(sLineNumber)){
	    	System.out.println("[1579186049] Invalid sLineNumber - " + sEntryNumber);
	    	return false;
	    }
	
	    String SQL = TRANSACTIONSQLs.Get_TransactionLine(sBatchNumber, sEntryNumber, sLineNumber);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID); 
			rs.next();

			//Load the variables:
			m_dAmount = rs.getBigDecimal(SMTableentrylines.damount);
			lDocAppliedToId(rs.getLong(SMTableentrylines.ldocappliedtoid));
			lEntryId(rs.getLong(SMTableentrylines.lentryid));
			lId(rs.getLong(SMTableentrylines.lid));
			sComment(rs.getString(SMTableentrylines.scomment));
			sDescription(rs.getString(SMTableentrylines.sdescription));
			sDocAppliedTo(rs.getString(SMTableentrylines.sdocappliedto));
			sGLAcct(rs.getString(SMTableentrylines.sglacct));
			setApplyToOrderNumber(rs.getString(SMTableentrylines.sapplytoordernumber));
			rs.close();
		}catch (SQLException ex){
	    	System.out.println("[1579186053] Error in " + this.toString()+ ".load class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        return false;
		}
    	return true;
    }
    public boolean load (
		String sID,
		ServletContext context, 
		String sDBID
		){
    
	    String SQL = TRANSACTIONSQLs.Get_TransactionLine(sID);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID); 
			rs.next();

			//Load the variables:
			iBatchNumber(rs.getInt(SMTableentrylines.ibatchnumber));
			iEntryNumber(rs.getInt(SMTableentrylines.ientrynumber));
			iLineNumber(rs.getInt(SMTableentrylines.ilinenumber));
			m_dAmount = rs.getBigDecimal(SMTableentrylines.damount);
			lDocAppliedToId(rs.getLong(SMTableentrylines.ldocappliedtoid));
			lEntryId(rs.getLong(SMTableentrylines.lentryid));
			lId(rs.getLong(SMTableentrylines.lid));
			sComment(rs.getString(SMTableentrylines.scomment));
			sDescription(rs.getString(SMTableentrylines.sdescription));
			sDocAppliedTo(rs.getString(SMTableentrylines.sdocappliedto));
			sGLAcct(rs.getString(SMTableentrylines.sglacct));
			setApplyToOrderNumber(rs.getString(SMTableentrylines.sapplytoordernumber));
			rs.close();
		}catch (SQLException ex){
	    	System.out.println("[1579186060] Error in " + this.toString()+ ".load class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        return false;
		}
    	return true;
    }
    public boolean load (
		long lID,
		ServletContext context, 
		String sDBID
		){
    
    	return load(Long.toString(lID), context, sDBID);
    }
    public void iBatchNumber (int iBatchNumber){
    	m_iBatchNumber = iBatchNumber;
    }
    public int iBatchNumber (){
    	return m_iBatchNumber;
    }
    public boolean sBatchNumber (String sBatchNumber){
    	try{
    		m_iBatchNumber = Integer.parseInt(sBatchNumber);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579186070] Error formatting batch number from string: " + sBatchNumber + ".");
    		System.out.println("Error: " + e.getMessage());
    		return false;
    	}
    }
    public String sBatchNumber (){
    	return Integer.toString(m_iBatchNumber);
    }
    public void iEntryNumber (int iEntryNumber){
    	m_iEntryNumber = iEntryNumber;
    }
    public int iEntryNumber (){
    	return m_iEntryNumber;
    }
    public boolean sEntryNumber (String sEntryNumber){
    	try{
    		m_iEntryNumber = Integer.parseInt(sEntryNumber);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579186081] Error formatting Entry number from string: " + sEntryNumber + ".");
    		System.out.println("Error: " + e.getMessage());
    		return false;
    	}
    }
    public String sEntryNumber (){
    	return Integer.toString(m_iEntryNumber);
    }

    public void iLineNumber (int iLineNumber){
    	m_iLineNumber = iLineNumber;
    }

    public int iLineNumber (){
    	return m_iLineNumber;
    }

    public boolean sLineNumber (String sLineNumber){
    	try{
    		m_iLineNumber = Integer.parseInt(sLineNumber);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579186088] Error formatting line number from string: " + sLineNumber + ".");
    		System.out.println("Error: " + e.getMessage());
    		return false;
    	}
    }
    public String sLineNumber (){
    	return Integer.toString(m_iLineNumber);
    }

    public void lEntryId (long lEntryId){
    	m_lEntryId = lEntryId;
    }

    public long lEntryId (){
    	return m_lEntryId;
    }

    public void sEntryId (String sEntryId){
    	m_lEntryId = Long.parseLong(sEntryId);
    }
    public String sEntryId (){
    	return Long.toString(m_lEntryId);
    }
    public boolean sDocAppliedTo (String sDocAppliedTo){
    	m_sDocAppliedTo = sDocAppliedTo;
    	return true;
    }
    public String sDocAppliedTo (){
    	if (m_sDocAppliedTo.equalsIgnoreCase("")){
    		return "UNAPPLIED";
    	}else {
    		return m_sDocAppliedTo;
    	}
    }
    public boolean sGLAcct (String sGLAcct){
    	m_sGLAcct = sGLAcct;
    	return true;
    }
    public String sGLAcct (){
    	return m_sGLAcct;
    }
    public boolean sDescription (String sDescription){
    	m_sDescription = sDescription;
    	return true;
    }
    public String sDescription (){
    	return m_sDescription;
    }
    public boolean dAmount (BigDecimal dAmount){
    	m_dAmount = dAmount;
    	return true;
    }
    public BigDecimal dAmount (){
    	return m_dAmount;
    }
    public boolean setAmountString (String sAmount){
    	try{
    		sAmount = sAmount.replace(",", "");
    		BigDecimal bd = new BigDecimal(sAmount);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_dAmount =  bd;
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579186100] " + this.toString() + ".setAmountString - Error converting amount from string: " + sAmount + ".");
    		System.out.println(e.getMessage());
    		return false;
    	}
    }
    public String sAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_dAmount);
    }
    public String sAmountSQLFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dAmount);
    }
    public boolean sComment (String sComment){
    	m_sComment = sComment;
    	return true;
    }
    public String sComment (){
    	return m_sComment;
    }
    public void lDocAppliedToId (long lDocAppliedToId){
    	m_lDocAppliedToId = lDocAppliedToId;
    }
    public long lDocAppliedToId (){
    	return m_lDocAppliedToId;
    }
    public boolean sDocAppliedToId (String sDocAppliedToId){
    	try{
    		m_lDocAppliedToId = Long.parseLong(sDocAppliedToId);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579186103] Error formatting DocAppliedToID from string: " + sDocAppliedToId + ".");
    		System.out.println("Error: " + e.getMessage());
    		return false;
    	}
    }
    public String sDocAppliedToId (){
    	return Long.toString(m_lDocAppliedToId);
    }
    public void lId (long lId){
    	m_lid = lId;
    }
    public long lId (){
    	return m_lid;
    }
    public void sId (String sId){
    	m_lid = Long.parseLong(sId);
    }
    public String sId (){
    	return Long.toString(m_lid);
    }
    public boolean setApplyToOrderNumber(String sApplyToOrderNumber){
    	if (sApplyToOrderNumber.length() > SMTableentrylines.sapplytoordernumberLength){
    		return false;
    	}else{
    		m_sApplyToOrderNumber = sApplyToOrderNumber;
    		return true;
    	}
    }
    public String getApplyToOrderNumber(){
    	return m_sApplyToOrderNumber;
    }
    public String read_out_debug_data(){
    	String sResult = " ** ARLine READ OUT DEBUG DATA: ";
    	sResult += "\nbatch: " + sBatchNumber();
    	sResult += "\nentry: " + sEntryNumber();
    	sResult += "\nlineno: " + sLineNumber();
    	sResult += "\namt: " + sAmountSTDFormat();
    	sResult += "\ncomment: " + sComment();
    	sResult += "\ndesc: " + sDescription();
    	sResult += "\ndocappliedto: " + sDocAppliedTo();
    	sResult += "\ndocappliedtoid: " + sDocAppliedToId();
    	sResult += "\nentryid: " + sEntryId();
    	sResult += "\nglacct: " + sGLAcct();
    	sResult += "\nlineid: " + sId();
    
	return sResult;
    }
}
