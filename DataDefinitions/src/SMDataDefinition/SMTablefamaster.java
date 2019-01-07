package SMDataDefinition;

public class SMTablefamaster {
	//Table Name
	public static final String TableName = "fa_master";

	//Field names:
	public static final String sId  = "id";
	public static final String sTruckNumber  = "sTruckNumber";
	public static final String sNotePayableGLAcct = "sNotePayableGLAcct";
	public static final String sState = "sState";
	public static final String sAssetNumber = "sAssetNumber";
	public static final String sDescription = "sDescription";
	public static final String datAcquisitionDate= "datAcquisitionDate";
	public static final String bdAcquisitionAmount= "dAcquisitionAmount";
	public static final String sClass = "sClass";
	public static final String sSerialNumber = "sSerialNumber";
	public static final String sLicenseTagNumber = "sLicenseTagNumber";
	public static final String sLocation = "sLocation";
	public static final String datDateSold = "datDateSold";
	public static final String sGaragedLocation = "sGaragedLocation";
	public static final String sLossOrGainGL = "sLossOrGainGL";
	public static final String sDepreciationType = "sDepreciationType";
	public static final String bdCurrentValue = "dCurrentValue";
	public static final String sComment = "sComment";
	public static final String bdAmountSoldFor = "dAmountSoldFor";
	public static final String sDepreciationGLAcct = "sDepreciationGLAcct";
	public static final String sAccumulatedDepreciationGLAcct = "sAccumulatedDepreciationGLAcct";
	public static final String bdAccumulatedDepreciation = "dAccumulatedDepreciation";
	//public static final String bdYTDDepreciation = "dYTDDepreciation";
	public static final String bdSalvageValue = "dSalvageValue";
	//public static final String bdYTDDisposedAmount = "dYTDDisposedAmount";
	//public static final String bdYTDPurchaseAmount = "dYTDPurchaseAmount";
	public static final String sdriver = "sdriver";
	public static final String scomment1 = "scomment1";
	public static final String scomment2 = "scomment2";
	public static final String scomment3 = "scomment3";
	public static final String sgdoclink = "sgdoclink";
	
	/*
	+--------------------------------+---------------+------+-----+---------+-------+
	| Field                          | Type          | Null | Key | Default | Extra |
	+--------------------------------+---------------+------+-----+---------+-------+
	| sTruckNumber                   | varchar(32)   | YES  |     | NULL    |       |
	| sNotePayableGLAcct             | varchar(128)  | YES  |     | NULL    |       |
	| sState                         | varchar(32)   | YES  |     | NULL    |       |
	| sAssetNumber                   | varchar(50)   | NO   |     |         |       |
	| sDescription                   | varchar(255)  | YES  |     | NULL    |       |
	| datAcquisitionDate             | datetime      | YES  |     | NULL    |       |
	| dAcquisitionAmount             | decimal(17,4) | NO   |     | 0.0000  |       |
	| sClass                         | varchar(6)    | YES  |     | NULL    |       |
	| sSerialNumber                  | varchar(128)  | YES  |     | NULL    |       |
	| sLicenseTagNumber              | varchar(32)   | YES  |     | NULL    |       |
	| sLocation                      | varchar(32)   | YES  |     | NULL    |       |
	| datDateSold                    | datetime      | YES  |     | NULL    |       |
	| sGaragedLocation               | varchar(32)   | YES  |     | NULL    |       |
	| sLossOrGainGL                  | varchar(128)  | YES  |     | NULL    |       |
	| sDepreciationType              | varchar(12)   | YES  |     | NULL    |       |
	| dCurrentValue                  | decimal(17,4) | NO   |     | 0.0000  |       |
	| sComment                       | varchar(254)  | YES  |     | NULL    |       |
	| dAmountSoldFor                 | decimal(17,4) | NO   |     | 0.0000  |       |
	| sDepreciationGLAcct            | varchar(128)  | YES  |     | NULL    |       |
	| sAccumulatedDepreciationGLAcct | varchar(128)  | YES  |     | NULL    |       |
	| dAccumulatedDepreciation       | decimal(17,4) | NO   |     | 0.0000  |       |
	| dYTDDepreciation               | decimal(17,4) | NO   |     | 0.0000  |       |
	| dSalvageValue                  | decimal(17,4) | NO   |     | 0.0000  |       |
	| dYTDDisposedAmount             | decimal(17,4) | NO   |     | 0.0000  |       |
	| dYTDPurchaseAmount             | decimal(17,4) | NO   |     | 0.0000  |       |
	| scomment1                      | varchar(254)  | NO   |     |         |       |
	| scomment2                      | varchar(254)  | NO   |     |         |       |
	| scomment3                      | varchar(254)  | NO   |     |         |       |
	| sgdoclink                      | text          | NO   |     | NULL    |       |
    | id                             | mediumint(9)  | NO   | PRI | NULL    | auto_increment |
	+--------------------------------+---------------+------+-----+---------+-------+
	 */
	//Field lengths:
	public static final int sTruckNumberLength = 32;
	public static final int sNotePayableGLAcctLength = 128;
	public static final int sStateLength = 32;
	public static final int sAssetNumberLength = 50;
	public static final int sDescriptionLength = 255;
	public static final int sClassLength = 6;
	public static final int sSerialNumberLength = 128;
	public static final int sLicenseTagNumberLength = 32;
	public static final int sLocationLength = 32;
	public static final int sGaragedLocationLength = 32;
	public static final int sLossOrGainGLLength = 128;
	public static final int sDepreciationTypeLength = 12;
	public static final int sCommentLength = 254;
	public static final int sDepreciationGLAcctLength = 128;
	public static final int sAccumulatedDepreciationGLAcctLength = 128;
	public static final int sdriverLength = 72;
	public static final int sComment1Length = 254;
	public static final int sComment2Length = 254;
	public static final int sComment3Length = 254;
	
	public static final int bdAcquisitionAmountScale = 4;
	public static final int bdCurrentValueScale = 4;
	public static final int bdAmountSoldForScale = 4;
	public static final int bdAccumulatedDepreciationScale = 4;
	//public static final int bdYTDDepreciationScale = 4;
	public static final int bdSalvageValueScale = 4;
	//public static final int bdYTDDisposedAmountScale = 4;
	//public static final int bdYTDPurchaseAmountScale = 4;
}
