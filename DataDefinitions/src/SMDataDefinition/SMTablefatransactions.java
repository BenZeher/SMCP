package SMDataDefinition;

public class SMTablefatransactions {
	//Table Name
	public static final String TableName = "fa_transactions";
	
	//Field names:
	public static final String datTransactionDate = "datTransactionDate";
	public static final String dAmountDepreciated = "dAmountDepreciated";
	public static final String iFiscalYear = "iFiscalYear";
	public static final String iFiscalPeriod = "iFiscalPeriod";
	public static final String iProvisionalPosting = "iProvisionalPosting";
	public static final String sTransAccumulatedDepreciationGLAcct = "sTransAccumulatedDepreciationGLAcct";
	public static final String sTransDepreciationGLAcct = "sTransDepreciationGLAcct";
	public static final String sTransAssetNumber = "sTransAssetNumber";
	public static final String sTransactionType = "sTransactionType";
	public static final String sTransComment = "sTransComment";
	public static final String datPostingDate = "datPostingDate";
	
	/*
	+-------------------------------------+---------------+------+-----+---------+-------+
	| Field                               | Type          | Null | Key | Default | Extra |
	+-------------------------------------+---------------+------+-----+---------+-------+
	| datTransactionDate                  | datetime      | YES  |     | NULL    |       |
	| dAmountDepreciated                  | decimal(17,4) | NO   |     | 0.0000  |       |
	| iFiscalYear                         | int(11)       | YES  |     | 0       |       |
	| iFiscalPeriod                       | int(11)       | YES  |     | 0       |       |
	| iProvisionalPosting                 | int(11)       | YES  |     | 0       |       |
	| sTransAccumulatedDepreciationGLAcct | varchar(128)  | YES  |     | NULL    |       |
	| sTransDepreciationGLAcct            | varchar(128)  | YES  |     | NULL    |       |
	| sTransAssetNumber                   | varchar(50)   | NO   |     |         |       |
	| sTransactionType                    | varchar(6)    | YES  |     |         |       |
	| sTransComment                       | varchar(64)   | YES  |     |         |       |
	| datPostingDate                      | datetime      | YES  |     | NULL    |       |
	+-------------------------------------+---------------+------+-----+---------+-------+
	*/

	//Field lengths:
	public static final int sTransAccumulatedDepreciationGLAcctLength = 128;
	public static final int sTransDepreciationGLAcctLength = 128;
	public static final int sTransAssetNumberLength = 50;
	public static final int sTransactionTypeLength = 6;
	public static final int sTransCommentLength = 64;

	public static final String DEPRECIATION_FLAG = "FA_DEP";

	public static final String ADJUSTMENT_FLAG = "FA_ADJ";
}
