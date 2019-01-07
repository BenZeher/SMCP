package smar;

public class ARLineInput extends java.lang.Object{

	private String m_sLineID;
	private String m_sDocAppliedTo;
	private String m_sLineAcct;
	private String m_sDescription;
	private String m_sAmount;
	private String m_sComment;
	private String m_sDocAppliedToId;
	private String m_sApplyToOrderNumber;
	private String m_schkApplyCashTo;
	private String m_sEntryID;
	
	public static final String ParamLineID = "LineID";
	public static final String ParamDocAppliedTo = "LineDocAppliedToDocNumber";
	public static final String ParamDistAcct = "LineAcct";
	public static final String ParamLineDesc = "LineDesc";
	public static final String ParamLineAmt = "LineAmt";
	public static final String ParamLineComment = "LineComment";
	public static final String ParamLineDocAppliedToID = "LineDocAppliedToID";
	public static final String ParamLineApplyToOrderNumber = "LineApplyToOrderNumber";
	public static final String ParamLineApplyCashToChk = "chkApplyCashTo";
	public static final String ParamLineEntryID = "LineEntryID";

	ARLineInput (){
		m_sLineID = "";
		m_sDocAppliedTo = "";
		m_sLineAcct = "";
		m_sDescription = "";
		m_sAmount = "0.00";
		m_sComment = "";
		m_sDocAppliedToId = "-1";
		m_sApplyToOrderNumber = "";
		m_schkApplyCashTo = "";
		m_sEntryID = "";
	}
	
	public String getLineID(){
		return m_sLineID;
	}
	public void setLineID(String sLineID){
		m_sLineID = sLineID;
	}
	public String getDocAppliedTo(){
		return m_sDocAppliedTo;
	}
	public void setDocAppliedTo(String sDocAppliedTo){
		m_sDocAppliedTo = sDocAppliedTo;
	}
	public String getLineAcct(){
		return m_sLineAcct;
	}
	public void setLineAcct(String sLineAcct){
		m_sLineAcct = sLineAcct;
	}
	public String getDescription(){
		return m_sDescription;
	}
	public void setDescription(String sDescription){
		m_sDescription = sDescription;
	}
	public String getAmount(){
		return m_sAmount;
	}
	public void setAmount(String sAmount){
		m_sAmount = sAmount;
	}
	public String getComment(){
		return m_sComment;
	}
	public void setComment(String sComment){
		m_sComment = sComment;
	}
	public String getDocAppliedToID(){
		return m_sDocAppliedToId;
	}
	public void setDocAppliedToID(String sDocAppliedToID){
		m_sDocAppliedToId = sDocAppliedToID;
	}
	public String getApplyToOrderNumber(){
		return m_sApplyToOrderNumber;
	}
	public void setApplyToOrderNumber(String sApplyToOrderNumber){
		m_sApplyToOrderNumber = sApplyToOrderNumber;
	}
	public String getApplyCashToChk(){
		return m_schkApplyCashTo;
	}
	public void setApplyCashToChk(String sApplyCashToChk){
		m_schkApplyCashTo = sApplyCashToChk;
	}
	public String getEntryID(){
		return m_sEntryID;
	}
	public void setEntryID(String sEntryID){
		m_sEntryID = sEntryID;
	}
}