package smar;
import java.sql.DriverManager;

import javax.servlet.http.HttpServlet;

import ServletUtilities.clsStringFunctions;
import smic.ICPOReceiptHeader;

public class TESTBatchExport extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public static void main(String[] args){
		
		java.sql.Connection conn = null;
		
		//Localhost settings:
		String sURL = "localhost"; //Google Cloud SQL = 35.243.211.251
		String sDBID = "servmgr1"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "smuser7sT559";
		String sPassword = "kJ26D3G9bvK8";
		
		//OHD Tampa settings:
		/*
		sURL = "23.111.150.171";
		sDBID = "smcpcontrols"; //servmgr1 - default
		sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		sUser = "smadmin";
		sPassword = "jSdy78GHk9Ygh";
		*/
		
		//OHD Daytona settings:
		/*
		String sURL = "74.50.124.130";
		String sDBID = "smdaytona"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "smuser";
		String sPassword = "smuser";
		*/
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Local string for laptop:
			conn = DriverManager.getConnection(sConnString, sUser, sPassword);
			//conn = DriverManager.getConnection("jdbc:mysql://" + "smcp001.com" + ":3306/" + "servmgr1" + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
				//+ "&allowMultiQueries=true"
				//,"smuser7sT559", "kJ26D3G9bvK8");
		}catch (Exception E) { 
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				//Local string for laptop:
				conn = DriverManager.getConnection(sConnString, sUser, sPassword);
				//conn = DriverManager.getConnection("jdbc:mysql://" + "smcp001.com" + ":3306/" + "servmgr1" + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
					//+ "&allowMultiQueries=true"
					//, "smuser7sT559", "kJ26D3G9bvK8");

			}catch(Exception F){
				System.out.println(F.getMessage() + " - " + F.getLocalizedMessage());
			}
			System.out.println(E.getMessage() + " - " + E.getLocalizedMessage());
		}
		
		
		//String sNote = "Credit \"and\" rebill as 100% customer courtesy to resolve dispute. Jeff T. has discussed with their service manager many times, and he is not going to pay this invoice even though it is billable. They called us because drive chain came off sprocket. Art S. aligned the sprocket but couldn't test the door because they had blown fuses. He took the blown fuse so we could pick up locally and return the next day, which we did. Alan arrived on site and found that the people on site had replaced the fuses in both operators themselves, but used 2A instead of the 20A fuses required. Alan verified with tech support and we returned again with the correct fuses the next day. Once he was able to test the door, he found the safety edge and sprocket in need of replacement, detailed both on 1/20. We had to get pricing from Albany, but got proposal 451845 to the customer on 01/25, which they acknowledged the same day. When we returned to replace the sprocket at the end of February we couldn't install the edge due to traffic, so they asked us to give them a price to do the work on OT hours, which we did the same day. The next day they canceled the edge, said they are going to replace the door. We were able to return the edge to TNR for full credit, and they paid the rest of that invoice. Their service manager did not dispute any of what Jeff T. said, just claimed we didn't fix the door. He also said we didn't inform him of the safety issues, which we did. Alan W. met with him in his office for 45 minutes to go over all of this once he realized they had replaced the fuses themselves, but he did not specifically note that on his work order. They never brought anything to our attention, and did not ever raise any issues upon receiving the invoice, they just didn't pay it. After numerous attempts to resolve we finally ended up sending a demand letter, to which they sent a letter in response claiming that we never provided a price for the edge, nor a quote for a new door that they had requested, both of which are demonstrably false. Jeff T. spoke with them again to let them know that not only had we sent the edge quote, but he (their service manager) had personally responded to the email thanking us and saying he would let us know once he approved. This was 5 days before the door closed on a car. Jeff T. also reviewed the email string between Jason Witter and the customer discussing new doors, which he claimed he never received. Jason Witter quoted them both doors in Oct. of 16, and follow up was consistent through December, and all this started in January. The bottom line is they know this is billable, but had someone else replace the doors and do not want to pay it. He also disputed another small invoice saying we showed up to replace the sprocket and edge without the material so he turned us away. That was again not what happened. They had called us to unjam a different door on site, which we did. He ultimately agreed to pay the smaller invoice, but refuses to pay this one. Jeff T. discussed with Dennis M., and both agreed that we will get more out of a courtesy than taking this to court, where we would win but risk losing business from other Ourisman dealerships. Jeff T. agreed to credit this out once they paid the smaller invoice, which we received this week. We will work on getting back in over here, and will make this up on future work. Jeff T. did address with Alan W. the failure to clearly note the safety issue on his work order. He said he had a 45 minute conversation with them about it, but nothing we can prove in court."
		String sNote = "123*SESSIONTAG*456";
		
		System.out.println(sNote);
		System.out.println(sNote.replaceAll("\\*SESSIONTAG\\*", ""));
		;
				
				;
		System.out.println(sNote);
		System.out.println(clsStringFunctions.filter(sNote));
		
		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
		rcpt.setsID("57578");
		rcpt.delete(conn, "TR", "6");
		
		/*
		APBatch batch = new APBatch("469");
		//clsDatabaseFunctions.start_data_transaction(conn);
		try {
			batch.loadBatch(conn);
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		clsDatabaseFunctions.rollback_data_transaction(conn);
		System.out.println("DONE");
		*/
		
		/*
		//Test GL conversion rollback:
		GLACCPACConversion conv = new GLACCPACConversion();
		try {
			conv.reverseDataChanges(conn, true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("DONE");
		*/
		/*
		APBatch batch = new APBatch("255");
		try {
			batch.load(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		clsDatabaseFunctions.start_data_transaction(conn);
		try {
			batch.post_with_connection(conn, "1", "Tom");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		System.out.println("DONE");
		*/
	}
}
