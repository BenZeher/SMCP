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
var currentlyRunning = false;

function loadPicker() {
	//prevent mutiple clicks before function is finished.
	if(currentlyRunning === true){
		return;
	}
	currentlyRunning = true;
	
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
	//Try to authorize with current user session (no prompt). 
	window.gapi.auth.authorize({
		'client_id' : clientId,
		'scope' : scope,
		'hosted_domain' : domain,
		'prompt':'none'
	}, handleAuthResult);	
}

function handleAuthResult(authResult) {
	if (authResult) {
		if(authResult.error === 'immediate_failed'){
			//If immediate login fails, prompt to sign in.
			window.gapi.auth.authorize({
				'client_id' : clientId,
				'scope' : scope,
				'hosted_domain' : domain
			}, handleAuthResult);
		}else{
			oauthToken = authResult.access_token;
			createPicker();
		}
	}
}

function createPicker() {
	//If the api is loaded and we have authorization
	if (pickerApiLoaded && oauthToken) {
		
		//Check of the folder already exists
		gapi.client.drive.files.list({
			'q' : "name='" + folderName + "' " +
				  "and parents= '" + parentfolderid + "' " +
				  "and trashed=false"  
		}).then(function(response) {
			console.log(response);
			var files = response.result.files;
			//If the folder alreadt exists, do nothing...
			if (files && files.length > 0) {
				folderID = files[0].id;
				console.log(folderName + ' already exists with id: '
						+ files[0].id);
				console.log('Updateing data with URL...');
				 //Update folder url in database.
				asyncUpdate('https://drive.google.com/drive/folders/' + files[0].id);
				buildPicker();
			//Otherwise, create  a new folder and save it to the database.
			} else {				
				var fileMetadata = {
						'name' : folderName,
						'mimeType' : 'application/vnd.google-apps.folder',
						'owners' : '',
						'parents' : [ parentfolderid ]
					};
					gapi.client.drive.files.create({
						resource : fileMetadata,
					}).then(function(response) {
						switch (response.status) {
						case 200:
							var file = response.result;
							folderID = file.id;
							console.log('Created Folder Id: ' + folderID);
	
  /*						
							var permissionListRequest = gapi.client.drive.permissions.list({
							      "fileId": folderID
							    });
								permissionListRequest.execute(function(resp) { 
									 console.log('Lising permisions: ' + resp);
							      var permissionUpdateRequest = gapi.client.drive.permissions.update({
							        'fileId': folderID,
							        'permissionId': resp.permissions[0].id, 
							        'transferOwnership': true,
							        'resource': {'role':'owner', 'emailAddress': domainaccount}
							      });
							      permissionUpdateRequest.execute(function(resp3) {
							        console.log('Updating permision to domain owner: ' + resp3);
							      });
							    });
	*/
							    //Update folder url in database.
							    console.log('Updateing data with URL...');
								asyncUpdate('https://drive.google.com/drive/folders/' + folderID);
							buildPicker();
							break;
						default:
							break;
						}
					});		
			}
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
			.enableFeature(google.picker.Feature.MULTISELECT_ENABLED)
			.setAppId(appId)
			.setOAuthToken(oauthToken)
			.addView(uploadView)
			.addView(view)
			.setDeveloperKey(developerKey)
			.setTitle(folderName)
			.setCallback(pickerCallback)
			.build();
	
	//Display the google drive picker
	picker.setVisible(true);	
}

function pickerCallback(data) {
	//If the user picked a files then display it.
	if ((data.action == google.picker.Action.PICKED) && (!data.docs[0].isNew)) {
		var fileId = data.docs[0].id;
			window.open(data.docs[0].url);
	}
	//Allow function to run again.
	currentlyRunning = false;
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


