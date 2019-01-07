package SMDataDefinition;

public class SMTablearacctset {
	//Table Name
	public static final String TableName = "aracctset";
	
	//Field names:
	public static final String sAcctSetCode = "sAcctSetCode";
	public static final String sDescription = "sDescription";
	public static final String iActive = "iActive";
	public static final String datLastMaintained = "datLastMaintained";
	public static final String sAcctsReceivableControlAcct = "sAcctsReceivableControlAcct"; 
	public static final String sReceiptDiscountsAcct = "sReceiptDiscountsAcct"; 
	public static final String sPrepaymentLiabilityAcct = "sPrepaymentLiabilityAcct"; 
	public static final String sWriteOffAcct = "sWriteOffAcct"; 
	public static final String sRetainageAcct = "sRetainageAcct";
	public static final String sCashAcct = "sCashAcct";
	
	//Field Lengths:
	public static final int sAcctSetCodeLength = 6;
	public static final int sDescriptionLength = 60;
	public static final int sAcctsReceivableControlAcctLength = 45; 
	public static final int sReceiptDiscountsAcctLength = 45; 
	public static final int sPrepaymentLiabilityAcctLength = 45; 
	public static final int sWriteOffAcctLength = 45; 
	public static final int sRetainageAcctLength = 45; 
	public static final int sCashAcctLength = 45; 
	
	//Creation SQL:
	/*
create table aracctset (
`sAcctSetCode` varchar(6) NOT NULL default '',
`sDescription` varchar(60) NOT NULL default '',
`iActive` int(11) NOT NULL default '1',
`datLastMaintained` datetime NOT NULL default '0000-00-00 00:00:00',
`sAcctsReceivableControlAcct` varchar(45) NOT NULL default '',
`sReceiptDiscountsAcct` varchar(45) NOT NULL default '',
`sPrepaymentLiabilityAcct` varchar(45) NOT NULL default '',
`sWriteOffAcct` varchar(45) NOT NULL default '',
`sRetainageAcct` varchar(45) NOT NULL default '',
PRIMARY KEY (sacctSetCode)
) Engine=InnoDB;
	 */

}
