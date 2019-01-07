package ServletUtilities;

import java.io.Serializable;

public class clsURLRecord implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String sTitle = "";
	private String sAddress = "";
	
	public String Title(){
		return sTitle;
	}
	public void Title(String s){
		sTitle = s;
	}
	
	public String Address(){
		return sAddress;
	}
	public void Address(String s){
		sAddress = s;
	}
	
	public clsURLRecord(){
		sTitle = "";
		sAddress = "";
	}
	
	public clsURLRecord(String sT, String sA){
		sTitle = sT;
		sAddress = sA;
	}

}
