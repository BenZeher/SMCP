/* These global variable are used, but defined outside this file.
var pickerApiLoaded = false;
var appId = "910376449199";
var scope = [ 'https://www.googleapis.com/auth/drive' ];
var clientId = "910376449199-ij2k22dulac1q590psj4psvjs1qomh6s.apps.googleusercontent.com";
var developerKey = 'AIzaSyBcA9Iryl-34pnKzGAHneuogjla29tcbBw';
var folderName = 'NEW Javascript Folder 2';
var oauthToken;
*/
var pickerApiLoaded = false;
var scope = [ 'https://www.googleapis.com/auth/drive' ];
var folderID;
var picker;
var oauthToken;

function loadPicker() {
	//If we already searched for the folder ID Just display the picker
	if(folderID){
		picker.setVisible(true);
	
	//Otherwise authorize the user and load the APIs
	}else{
		//load the auth api
		gapi.load('auth', onAuthApiLoad);
	
		//load the client.drive api
		gapi.load('client', function() {
			gapi.client.load('drive', 'v3', drivev2load);
		});
		//load the picker api
		gapi.load('picker', onPickerApiLoad);
	}
}

function drivev2load() {
	//alert('drive v3 loaded');
}

function onPickerApiLoad() {
	pickerApiLoaded = true;
	createPicker();
}

function onAuthApiLoad() {
	window.gapi.auth.authorize({
		'client_id' : clientId,
		'scope' : scope
		,'hosted_domain' : 'odcdc.com'
	}, handleAuthResult);
}

function handleAuthResult(authResult) {
	if (authResult && !authResult.error) {
		oauthToken = authResult.access_token;
		createPicker();
	}
}

function createPicker() {
	//If the api is loaded and we have authorization
	if (pickerApiLoaded && oauthToken) {
		
		//Check of the folder already exists
		gapi.client.drive.files.list({
			'q' : "name='" + folderName + "'"  
		}).then(function(response) {
			console.log(response);
			var files = response.result.files;
			//If the folder alreadt exists, do nothing...
			if (files && files.length > 0) {
				folderID = files[0].id;
				console.log(folderName + ' already exists with id: '
						+ files[0].id);
				buildPicker();
			//Otherwise, create  a new folder and save it to the database.
			} else {				
				var fileMetadata = {
						'name' : folderName,
						'mimeType' : 'application/vnd.google-apps.folder',
						'parents' : [ '0ByxluPCydkjOWnNJY1pQOTlkTWM' ]
					};
					gapi.client.drive.files.create({
						resource : fileMetadata,
					}).then(function(response) {
						switch (response.status) {
						case 200:
							var file = response.result;
							folderID = file.id;
							console.log('Created Folder Id: ' + file.id);
							buildPicker();
							break;
						default:
							break;
						}
					});		
			}
			//Always update the folder url
			console.log('Updateing data with URL: ' + 'https://drive.google.com/drive/folders/' + files[0].id);
			asyncUpdate('https://drive.google.com/drive/folders/' + files[0].id);

		});
	} 
}

function buildPicker() {
	//Setup the docs view.
	var view = new google.picker.DocsView(google.picker.ViewId.DOCS);
	view.setParent(folderID).setIncludeFolders(true);
	
	//Setup the upload view
	var uploadView = new google.picker.DocsUploadView();
	uploadView.setParent(folderID);
	
	//Create the google drive picker
	picker = new google.picker.PickerBuilder()
	//		.setTitle('Folder ' + folderName)
			.enableFeature(google.picker.Feature.MULTISELECT_ENABLED)
			.setAppId(appId)
			.setOAuthToken(oauthToken)
//			.addView(view)
			.addView(uploadView)
			.setDeveloperKey(developerKey)
			.setCallback(pickerCallback)
			.build();
	
	//Display the google drive picker
	picker.setVisible(true);
}

function pickerCallback(data) {
	if (data.action == google.picker.Action.PICKED) {
		var fileId = data.docs[0].id;
		alert('The user selected: ' + fileId);
	}
}

function asyncUpdate(folderurl) {
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function(){
		console.log(this.responseText);
	};
	xhr.open("POST", "/sm/smcontrolpanel.SMCreateGDriveFolder");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.send("asyncrequest=Y&folderURL=" + folderurl + "&recordtype=" + recordtype + "&keyvalue=" + keyvalue );
	}


