package SMDataDefinition;

public class SMTabledeliverytickets {
	public static final String TableName = "deliverytickets";
	
	public static final String lid = "lid";
	public static final String datinitiated = "datinitiated";
	public static final String linitiatedbyid = "linitiatedbyid";
	public static final String sinitiatedbyfullname = "sinitiatedbyfullname";
	public static final String sdetaillines = "sdetaillines";
	public static final String mcomments = "mcomments";
	public static final String strimmedordernumber = "strimmedordernumber";
	public static final String ssignedbyname = "ssignedbyname";
	public static final String datsigneddate = "datsigneddate";
	public static final String msignature = "msignature";
	public static final String sbilltoname="sbilltoname";
	public static final String sbilltoadd1="sbilltoadd1";
	public static final String sbilltoadd2="sbilltoadd2";
	public static final String sbilltoadd3="sbilltoadd3";
	public static final String sbilltocontact="sbilltocontact";
	public static final String sbilltophone="sbilltophone";
	public static final String sponumber="sponumber";
	public static final String sshiptoname="sshiptoname";
	public static final String sshiptoadd1="sshiptoadd1";
	public static final String sshiptoadd2="sshiptoadd2";
	public static final String sshiptoadd3="sshiptoadd3";
	public static final String sshiptocountry="sshiptocountry";
	public static final String sshiptocontact="sshiptocontact";
	public static final String sshiptophone="sshiptophone";
	public static final String sshiptofax="sshiptofax";
	public static final String smechanicname="smechanicname";
	public static final String iworkorderid="iworkorderid";
	public static final String stermscode="stermscode";
	public static final String mterms= "mterms";
	public static final String iposted="iposted";
	public static final String sbilltocity = "sbilltocity";
	public static final String sbilltostate = "sbilltostate";
	public static final String sbilltozip = "sbilltozip";
	public static final String sshiptocity = "sshiptocity";
	public static final String sshiptostate = "sshiptostate";
	public static final String sshiptozip = "sshiptozip";
	public static final String sdeliveredby = "sdeliveredby";
	public static final String lsignaturboxwidth = "lsignatureboxwidth";
	public static final String mdbaaddress= "mdbaaddress";
	public static final String mdbaremittoaddress = "mdbaremittoaddress";
	public static final String sdbadeliveryticketreceiptlogo = "sdbadeliveryticketreceiptlogo";

	
	//Lengths
	public static final int linitiatedbyidlength = 11;
	public static final int sinitiatedbyfullnamelength = 128;
	public static final int sdescriptionlength = 254;
	public static final int strimmedordernumberlength = 22;
	public static final int ssignedbynamelength = 80;
	public static final int scompanynamelength = 70;
	public static final int scompanyaddressline1length = 60;
	public static final int scompanyaddressline2length = 60;
	public static final int scompanyphonelength = 20;
	public static final int sbranchofficelength = 30;
	public static final int sbranchphonelength = 30;
	public static final int sbilltonamelength = 60;
	public static final int sbilltoadd1length = 60;
	public static final int sbilltoadd2length = 60;
	public static final int sbilltoadd3length = 60;
	public static final int sbilltocontactlength = 60;
	public static final int sbilltophonelength = 30;
	public static final int sponumberlength = 20;
	public static final int sshiptonamelength =60;
	public static final int sshiptoadd1length =60;
	public static final int sshiptoadd2length =60;
	public static final int sshiptoadd3length = 60;
	public static final int sshiptocountrylength = 30;
	public static final int sshiptocontactlength = 60;
	public static final int sshiptophonelength = 30;
	public static final int sshiptofaxlength = 30;
	public static final int smechanicnamelength = 70;
	public static final int scustomerfullnamelength = 60;
	public static final int stermscodelength = 6;
	public static final int sdeliveredbylength = 128;
	public static final int lsignatureboxwidthlength = 11;
	public static final int sdbadeliveryticketreceiptlogolength = 128;
	
	//Constants for using signatures in delivery tickets:
	public static final String SIGNATURE_CANVAS_WIDTH = "200"; //Originally "200"; 
	public static final String SIGNATURE_CANVAS_HEIGHT = "53"; //Originally "53";
	public static final String SIGNATURE_PEN_WIDTH = "2";
	public static final String SIGNATURE_PEN_COLOUR = "#145394";
	public static final int SIGNATURE_PEN_R_COLOUR = 20;
	public static final int SIGNATURE_PEN_G_COLOUR = 83;
	public static final int SIGNATURE_PEN_B_COLOUR = 148;
	public static final String SIGNATURE_TOP = "52";
	public static final int SIGNATURE_LINE_R_COLOUR = 12;
	public static final int SIGNATURE_LINE_G_COLOUR = 12;
	public static final int SIGNATURE_LINE_B_COLOUR = 12;
	public static final String SIGNATURE_LINE_WIDTH = "1";
	public static final String SIGNATURE_LINE_MARGIN = "5";
	public static final String SIGNATURE_LINE_TOP = "36";


}
