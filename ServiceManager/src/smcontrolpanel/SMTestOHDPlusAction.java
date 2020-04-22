package smcontrolpanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SMTestOHDPlusAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Read the entry fields from the request object:
	    //String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
		String sRequestURL = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMTestOHDPlusSelect.PARAM_REQUESTSTRING, request);
				
		String sRequestResult;
		try {
			sRequestResult = readPlusServer(sRequestURL);
		} catch (Exception e) {
			sRequestResult = "ERROR: " + e.getMessage();
		}

		out.println("Request string:"
			+ "<B><I>" + sRequestURL + "</I></B>"
		);
		
		out.print("<BR>Result of request:<BR>");
		out.print(sRequestResult);
		
		return;
	}
	private String readPlusServer(String sRequestURL) throws Exception{
		String s = "";
		
		// sample code from: https://www.baeldung.com/java-http-request
		//May need to refer back to it to add more code....
		URL url = new URL("http://example.com");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		String contentType = con.getHeaderField("Content-Type");
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		
		int status = con.getResponseCode();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine; 
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		
		con.disconnect();
			
		s = "TEST RESULT.";
		
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}