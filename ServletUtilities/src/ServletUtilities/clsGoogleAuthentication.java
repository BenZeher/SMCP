
///*
// * This class is used for testing Google APIs with OAuth2. 
// * Requires jars for this class can be found here https://developers.google.com/api-client-library/java/google-api-java-client/download
// */
//
//package ServletUtilities;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Collections;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.services.drive.DriveScopes;
//
//
//public class clsGoogleAuthentication {
//	@SuppressWarnings("unused")
//	
//	private static GoogleCredential createCredentials(String private_key_file, String email) throws Exception {
//		System.out.println("Step 2");
//		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//		
//		//File temp = new File(private_key_file);
//		//System.out.println("absolute file path: " + temp.getAbsolutePath());
//
//		JsonFactory jsonFactory = null;
//		System.out.println("Step 3: put file in " + System.getProperty("user.dir"));
//		GoogleCredential credential = null;
//		try {
//	/*
//			credential = GoogleCredential.fromStream(new FileInputStream("hypnotic-matter-188816-f8250f496614.json"))
//				    .createScoped(Collections.singleton(DriveScopes.DRIVE))
//				 ;
//	*/	
//			credential = new GoogleCredential.Builder()
//				    .setTransport(transport)
//				    .setJsonFactory(jsonFactory)
//				    .setServiceAccountId("551353444255-compute@developer.gserviceaccount.com")
//				    .setServiceAccountPrivateKeyFromP12File(new File(private_key_file))
//				    .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
//				    .setServiceAccountUser("madgcompany@odcdc.com")
//				    .build();
//		}catch (Exception e){
//			System.out.println("Step 3 FAIL");
//			System.out.println(e.getMessage());
//		}
//		
//		
//		return credential;
//	 }
//	
//	public static String sDrivePickerJavascript() throws Exception {
///*
//		GoogleCredential credential = null;
//		try {
//			System.out.println("Step 1");
//			credential = createCredentials("smcp-databases-dcf2db7344d5.p12","madgcompany@odcdc.com");
//		}catch (Exception e) {
//			System.out.println("Step 4");
//			throw new Exception("Error getting credetials: " + e.getMessage());
//		}
//*/
//		System.out.println("Step 5");
//		String s = " <script type=\"text/javascript\">\n"
//				+ "var pickerApiLoaded = false;\n"
//				+ "var appId = \"551353444255\";\n"
//				+ "   var scope = ['https://www.googleapis.com/auth/drive'];\n"
//			    + "var clientId = \"551353444255-ebef6tmsf8icu1ds6st9055l5lf7ters.apps.googleusercontent.com\";\n"
//				+ "var developerKey = 'AIzaSyBjkcilOtPued1cUdMK-jTjuqpTr_w9wJE';"
//				+ "    var oauthToken;"
//				
//				+ "  function loadPicker() {\n"
//		+ "      gapi.load('auth', {'callback': onAuthApiLoad});\n" 
//				
//				+ "      gapi.load('picker', {'callback': onPickerApiLoad});\n" + 
//				"    }\n"
//				+ ""
//				+ "  function onPickerApiLoad() {\n" + 
//				"      pickerApiLoaded = true;\n" + 
//				"      createPicker();\n" + 
//				"    }\n"
//				+ ""
//				+ "    function onAuthApiLoad() {\n" + 
//				"      window.gapi.auth.authorize(\n" + 
//				"          {\n" + 
//				"            'client_id': clientId,\n" + 
//				"            'scope': scope,\n" 
//				+ "          'hosted_domain': 'odcdc.com'" + 
//				"          },\n" + 
//				"          handleAuthResult);\n" + 
//				"    }\n"
//				+ ""
//
//				+ "    function handleAuthResult(authResult) {\n" + 
//				"      if (authResult && !authResult.error) {\n" + 
//				"        oauthToken = authResult.access_token;\n" + 
//				"        createPicker();\n" + 
//				"      }\n" + 
//				"    }\n"
//				+ ""
//				+ "    function createPicker() {\n" + 
//				"      if (pickerApiLoaded) {\n" + 
//				//"        var view = new google.picker.View(google.picker.ViewId.DOCS);\n" + 
//				"          var uploadView = new google.picker.DocsUploadView();\n" + 
//				//"        view.setMimeTypes(\"image/png,image/jpeg,image/jpg\");\n" + 
//				"        var picker = new google.picker.PickerBuilder()\n" + 
//				"            .enableFeature(google.picker.Feature.NAV_HIDDEN)\n" + 
//				"            .enableFeature(google.picker.Feature.MULTISELECT_ENABLED)\n" + 
//				"            .enableFeature(google.picker.Feature.SIMPLE_UPLOAD_ENABLED)\n" + 
//				"            .setAppId(appId)\n" + 
//				"            .setOAuthToken(" + "oauthToken" + ")\n" + 
//				"            .addView(uploadView)\n" +  
//				"            .addView(new google.picker.DocsUploadView().setParent('0ByxluPCydkjOWnNJY1pQOTlkTWM'))\n" + 
//				"            .setDeveloperKey(developerKey)\n" + 
//				"            .setCallback(pickerCallback)\n" + 
//				"            .build();\n" + 
//				"         picker.setVisible(true);\n" + 
//				"      }else{\n" + 
//				"          alert(\"oauthToken = \" + " + "oauthToken" + ");\n" + 
//				"      }\n" + 
//				"    }\n"
//				+ ""
//				+ "    function pickerCallback(data) {\n" + 
//				"      if (data.action == google.picker.Action.PICKED) {\n" + 
//				"        var fileId = data.docs[0].id;\n" + 
//				"        alert('The user selected: ' + fileId);\n" + 
//				"      }\n" + 
//				"    }\n"
//
//				+ " </script>\n";
//		
//		
//		
//		s +=  "<script type=\"text/javascript\" src=\"https://apis.google.com/js/api.js?onload=loadPicker\"></script>";
//		return s;
//	 }
//}
//
//
