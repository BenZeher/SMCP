package SMDataDefinition;

public class SMTableapchecks {
	public static final String TableName = "apchecks";
	
	public static final String lid = "lid";
	public static final String schecknumber = "schecknumber";
	public static final String lbankid = "lbankid";
	public static final String lcheckformid = "lcheckformid";
	public static final String bdamount = "bdamount";
	public static final String datcheckdate = "datcheckdate";
	public static final String lbatchnumber = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String ltransactionid = "ltransactionid";
	public static final String ivoid = "ivoid";
	public static final String iposted = "iposted";
	public static final String iprinted = "iprinted";
	public static final String lcreatedbyid = "lcreatedbyid";
	public static final String svendoracct = "svendoracct";
	public static final String svendorname = "svendorname";
	public static final String ipagenumber = "ipagenumber";
	public static final String ilastpage = "ilastpage";
	public static final String lbatchentryid = "lbatchentryid";
	public static final String dattimecreated = "dattimecreated";
	public static final String dattimeprinted = "dattimeprinted";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String lprintedbyid = "lprintedbyid";
	public static final String sprintedbyfullname = "sprintedbyfullname";
	
	//remit tos:
	public static final String sremittoname = "sremittoname";
	public static final String sremittoaddressline1 = "sremittoaddressline1";
	public static final String sremittoaddressline2 = "sremittoaddressline2";
	public static final String sremittoaddressline3 = "sremittoaddressline3";
	public static final String sremittoaddressline4 = "sremittoaddressline4";
	public static final String sremittocity = "sremittocity";
	public static final String sremittostate = "sremittostate";
	public static final String sremittocountry = "sremittocountry";
	public static final String sremittopostalcode = "sremittopostalcode";
	
	//Lengths
	public static final int schecknumberLength = 12;
	public static final int svendoracctLength = 12;
	public static final int svendornameLength = 60;
	
	public static final int sremittonameLength = 60;	 	 
	public static final int sremittoaddressline1Length = 60;	 	 	 
	public static final int sremittoaddressline2Length = 60;	 	 	 
	public static final int sremittoaddressline3Length = 60;	 	 	 
	public static final int sremittoaddressline4Length = 60;	 	 	 
	public static final int sremittocityLength = 30;	 	 	 
	public static final int sremittostateLength = 30;	 	 	 
	public static final int sremittopostalcodeLength = 20;	 	 	 
	public static final int sremittocountryLength = 30;
	public static final int lcreatedbyidLength = 11;
	public static final int screatedbyfullnameLength = 128;
	public static final int lprintedbyidLength = 11;
	public static final int sprinttedbyfullnameLength = 128;
	
	public static final int bdamountScale = 2;
}

/*

Check logic:

1) A batch of payments is created

2) A run of checks is printed, provisionally, and inspected

3) If the checks are all good, it's posted: batch is posted, checks are flagged as posted and printed

4) If some corrections need to be made, then:

5) Individual checks can be voided OR

6) Amounts, etc., can be corrected

7) A run of checks is printed, and inspected until they are correct

8) When they are correct and the check run is OK, the batch is posted, which prevents it from being re-run.


Additional rules:

1) Once a check is created, it can't be edited or deleted.  This is to prevent a person from printing a check, flagging it as 'not printed', then editing the information
	on the check and printing it again.  This would allow a person to print a check which had no corresponding record in the apchecks table, which
	could be a potential liability.

1) Once the user says that 'all checks for this batch have been printed successfully', the batch can't be edited, 
	and the check records can no longer be changed.  Until that time, payment entries can be edited, and checks
	can be flagged as printed/not printed, renumbered, etc.
	
2) Once a check has been printed, even if the whole batch wasn't printed 'successfully', then the user should not be allowed
	to make any changes to the corresponding payment entry.  If ne needs to then he needs to go to the 'Print checks' function
	and flag that check as 'NOT PRINTED'.  Only then can he edit that entry.
	
??
2) IF some checks are 'printed' and then the user edits the payment batch, what exactly has to happen?  IF the entry changes
	force the checks to change - for example if the check amount is now DIFFERENT, or if there are more or fewer lines on the
	check, then it's up to the USER to flag that check as 'NOT PRINTED' so he can reprint it, correctly, or renumber it, etc.


*/