
/*********************************************************************/
// String stuff
/*********************************************************************/

String.prototype.equalIgnoreCase = function(str) {
   return (str != null &&
     typeof str === 'string' &&
     this.toUpperCase() === str.toUpperCase());
 }

/*********************************************************************/
// AJAX stuff
/*********************************************************************/

function postReloadContent(results) {
   document.getElementById("_content").innerHTML = results;
}

/*********************************************************************/

function reloadGet(query) {
   sendReloadGet(query, postReloadContent);
}

function sendReloadGet(query, callback) {

   var xmlhttp;

   if (window.XMLHttpRequest) {
      xmlhttp=new XMLHttpRequest();
   } else {
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
   }

   xmlhttp.onreadystatechange = function(){
      if (xmlhttp.readyState==4 && xmlhttp.status==200) {
         callback(xmlhttp.responseText);
      }
   };

   xmlhttp.open("GET",query,true);
   xmlhttp.send();

}

/*********************************************************************/

function reloadPost(endpoint, appid, action, typeid, itemid, attrid) {
   sendReloadPost(endpoint, appid, action, typeid, itemid, attrid, postReloadContent);
}

function sendReloadPost(endpoint, appid, action, typeid, itemid, attrid, callback) {

   var updateForm = document.forms.UpdateItemDetails;

   updateForm.action=endpoint;

   addHiddenField(updateForm, "appid", appid);
   addHiddenField(updateForm, "action", action);
   addHiddenField(updateForm, "typeid", typeid);
   addHiddenField(updateForm, "itemid", itemid);
   addHiddenField(updateForm, "attrid", attrid);

   const data = getFormData(updateForm);

   update(endpoint, data, callback);

   return false;

}

/*********************************************************************/

function addHiddenField(form, name, value) {
   var hiddenField = document.createElement("input");
   hiddenField.type = "hidden";
   hiddenField.name = name;
   hiddenField.value = value;
   form.appendChild(hiddenField);
}

/*********************************************************************/

function getFormData(form) {
   var ret = "";
   const fields = form.elements;  
   for (let i=0; i<fields.length; i++) {
      if(fields[i].name!=="") {
         if(ret.length > 0) {
            ret +="&";
         }
         if(fields[i].type =="checkbox") {
            ret += fields[i].name + "=" + encodeURIComponent(fields[i].checked);
         } else {
            ret += fields[i].name + "=" + encodeURIComponent(fields[i].value);
         }
      }
   }
   return ret;
}

/*********************************************************************/

function getFormDataArray(form) {
   var ret = [];
   const fields = form.elements;  
   for (let i=0; i<fields.length; i++) {
      if(fields[i].name.startsWith("field")) {
         if(fields[i].type == "checkbox") {
            ret.push(fields[i].checked);
         } else {
            ret.push(fields[i].value);
         }
      }
   }
   return ret;
}

/*********************************************************************/

function update(query, data, callback) {
   var xmlhttp;
   if (window.XMLHttpRequest) {
      xmlhttp=new XMLHttpRequest();
   } else {
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
   }
   xmlhttp.onreadystatechange = function(){
      if (xmlhttp.readyState==4 && xmlhttp.status==200) {
        callback(xmlhttp.responseText);
      }
   };
   xmlhttp.open("POST",query,true);
   xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
   xmlhttp.send(encodeURI(data));
}

/*********************************************************************/

function getServerQuery(action, editor, params) {

   var ret = "IDCWebEditController?action=" + action + "&editor=" + editor;

   for(let index=0; index < params.length; index++) {
      const element = params[index];
      ret += "&" + element[0] + "=" + element[1];
   }

   return ret;

}

/*********************************************************************/
// UI funtions
/*********************************************************************/

function toggleList(source) {
   var checkboxes = document.querySelectorAll('input[type="checkbox"]');
   for (var i = 0; i < checkboxes.length; i++) {
       if (checkboxes[i] != source)
           checkboxes[i].checked = source.checked;
   }
}
/*********************************************************************/

function toggleChildren(source, prefix) {
   var checkboxes = document.querySelectorAll('input[type="checkbox"][name^="' + prefix +'');
   for (var i = 0; i < checkboxes.length; i++) {
       if (checkboxes[i] != source)
           checkboxes[i].checked = source.checked;
   }
}

/*********************************************************************/

function processSelectedListItems(query) {
   var selectedIds = "";
   var checkboxes = document.querySelectorAll('input[type="checkbox"]');
   for (var i = 0; i < checkboxes.length; i++) {
      if (checkboxes[i].name != 'master$box' && checkboxes[i].checked) {
         if(selectedIds.length > 0) {
            selectedIds += ',';
         }
         selectedIds += checkboxes[i].name;
      }
   }
   if(selectedIds.length > 0) {
      query += '&selectedids='+selectedIds;
   }
   reloadGet(query);
}

/*********************************************************************/

function processSelectedChildrenItems(query, prefix) {
   var selectedIds = "";
   var prefixLength = prefix.length;
   var checkboxes = document.querySelectorAll('input[type="checkbox"][name^="' + prefix +'"]');
   for (var i = 0; i < checkboxes.length; i++) {
      var name = checkboxes[i].name.substring(prefixLength);
      if (name != 'master$box' && checkboxes[i].checked) {
         if(selectedIds.length > 0) {
            selectedIds += ',';
         }
         selectedIds += checkboxes[i].name;
      }
   }
   if(selectedIds.length > 0) {
      query += '&selectedids='+selectedIds;
   }
   reloadGet(query);
}

/*********************************************************************/

function updateRefTree(query, label,  prefix, nBox, fieldName) {
   var field = document.querySelector('[name="' + fieldName +'"]');
   query += '&nbox='+nBox + '&attrid='+prefix + '&selection='+field.value;
   var xmlhttp;
   if (window.XMLHttpRequest) {
      xmlhttp=new XMLHttpRequest();
   } else {
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
   }
   xmlhttp.onreadystatechange = function(){
      if (xmlhttp.readyState==4 && xmlhttp.status==200) {
        document.getElementById(label).innerHTML = xmlhttp.responseText;
      }
   };
   xmlhttp.open("GET",query,true);
   xmlhttp.send();
}

/*********************************************************************/

function validateInteger(source, event) {
}

/*********************************************************************/

function validatePrice(source, event) {
}

/*********************************************************************/

function validatePhone(source, event) {
}

/*********************************************************************/

function validateEmail(source, event) {
}

/*********************************************************************/

function processChangedExtension(source, event) {
   alert(source.name);
}

/*********************************************************************/
// Show/Hide Side Bar
/*********************************************************************/

function toggleNav(source) {
   source.parentElement.querySelector(".nested").classList.toggle("active");
   source.classList.toggle("caret-down");
}

/*********************************************************************/

function openNav() {
   document.getElementById("explorer").style.width = "250px";
   document.getElementById("main").style.marginLeft = "250px";
}
 
/*********************************************************************/

function closeNav() {
   document.getElementById("explorer").style.width = "0";
   document.getElementById("main").style.marginLeft = "0";
}

/*********************************************************************/
// Utilities
/*********************************************************************/

function log(level, msg) {

   if(level < DEBUG) {
     console.log(msg);
   }

}

/*********************************************************************/

function MyMap() {

   this.keys = [];
   this.values = [];

   /*********************************************************************/

   this.put = function(key, value) {

      var ind = this.getIndex(key);
      if(ind != -1) {
         this.values[ind].push(value);
      } else {
         this.keys.push(key);
         var values = [value];
         this.values.push(values);
      }

   }
   
   /*********************************************************************/

   this.get = function(key) {

      var ret = null;

      var ind = this.getIndex(key);
      if(ind != -1) {
         ret = this.values[ind];
      }
            
      return ret;

   }
   
   /*********************************************************************/

   this.getIndex = function(key) {

      var ret = -1;

      for(var n=0; ret == -1 && n<this.keys.length; n++) {
         if(this.keys[n] == key) {
            ret = n;
         }
      }

      return ret;

   }
 
   /*********************************************************************/

   this.getSize = function() {

      var ret = 0;

      for(var n=0; n<this.values.length; n++) {
         ret += this.values[n].length;
      }

      return ret;

   }
 
}

/*********************************************************************/
// XML stuff
/*********************************************************************/

function sendLoadApplication(query) {

   var xmlhttp;
   if (window.XMLHttpRequest) {			// code for IE7+, Firefox, Chrome, Opera, Safari
      xmlhttp=new XMLHttpRequest();
   } else {								// code for IE6, IE5
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
   }

   xmlhttp.onreadystatechange = function(){
      if (xmlhttp.readyState==4 && xmlhttp.status==200) {
         postLoadApplication(xmlhttp.responseXML);
      }
   };

   xmlhttp.open("GET",query,true);
   xmlhttp.send();

}

/*********************************************************************/

function postLoadApplication(serverResponse) {

   hidePopupPanel();

   if(serverResponse != null) {
 	  
 	  var applNode = serverResponse.getElementsByTagName("Application")[0];
 	  if(applNode != null) {
         rootItem = getModelObjectFromXMLNode(applNode, null);
         rootItem.resolveLinks();
         rootItem.isNew = false;
         rootItem.isSaved = true;
         repaint();
      }

   } else {
 	  alert("Error loading model.");
   }

}

/*********************************************************************/

function downloadFile(filename, data) {

   var element = document.createElement('a');

   element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(data));
   element.setAttribute('download', filename);
   element.style.display = 'none';

   document.body.appendChild(element);

   element.click();

   document.body.removeChild(element);

}

/*********************************************************************/

function selectFile(query) {

   var input = document.createElement('input');
   input.type = 'file';
   input.onchange = e => { 
      input = null;
      var file = e.target.files[0]; 
      var reader = new FileReader();
      reader.readAsText(file,'UTF-8');
      reader.onload = readerEvent => {
         var content = readerEvent.target.result; 
         postSelectFile(query, content);
      }
   }
   input.click();
}

/*********************************************************************/

function postSelectFile(query, content) {
   data = "content=" + encodeURIComponent(content);
   update(query, data, postReloadContent);
}

/*********************************************************************/

function showGraph(query) {
   sendReloadGet(query, postShowGraph);
}
/*********************************************************************/

function postShowGraph(results) {

   var graphHTML = '<div id="canvasParent" class="canvasParent"><canvas id="canvas" class="canvas" width="1409" height="715">HTML5 Canvas not supported :(</canvas> <ul id="popuppanel" class="popuppanel"></ul></div>';
   document.getElementById("_datapane").innerHTML = graphHTML;
   
   var graphData = JSON.parse(results);

   initGraph(graphData);

}

/*********************************************************************/

function exportList(query) {
   alert("exportList" + query);
   sendReloadGet(query, postExportList);
}

/*********************************************************************/

function postExportList(results) {

   alert("postExportList" + results);

   downloadFile("test.csv", results);

}

/*********************************************************************/
// Speech rec
/*********************************************************************/

var nluQuery = '';
var recognizing = false;
var ignore_onend;

var nluServerQuery;

if (!('webkitSpeechRecognition' in window)) {
} else {
  var recognition = new webkitSpeechRecognition();
  recognition.continuous = true;
  recognition.interimResults = true;

  recognition.onstart = function() {
    recognizing = true;
  };

  recognition.onerror = function(event) {
    if (event.error == 'no-speech') {
      console.log('info_no_speech');
      ignore_onend = true;
    }
    if (event.error == 'audio-capture') {
      console.log('info_no_microphone');
      ignore_onend = true;
    }
    if (event.error == 'not-allowed') {
      if (event.timeStamp - start_timestamp < 100) {
         console.log('info_blocked');
      } else {
         console.log('info_denied');
      }
      ignore_onend = true;
    }
  };

  recognition.onend = function() {
    recognizing = false;
    if (ignore_onend) {
      return;
    }
    if (!final_transcript) {
      console.log('info_start');
      return;
    }
  };

  recognition.onresult = function(event) {
    var interim_transcript = '';
    for (var i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
         nluQuery += event.results[i][0].transcript;
      } else {
        interim_transcript += event.results[i][0].transcript;
      }
    }
    if (nluQuery || interim_transcript) {
      console.log('interim_transcript: ' + interim_transcript);
      console.log('nluQuery: ' + nluQuery);
      if(editor === undefined) {
         processNLUServerQuery();
      } else {
         processNLUEditorQuery();
      }
    }
  };
}

   function speak(query) {

      nluServerQuery = query;

      if (recognizing) {
      recognition.stop();
      console.log('stopping recognition');
      } else {
      final_transcript = '';
      recognition.lang = 'en-US';
      recognition.start();
      ignore_onend = false;
      console.log('starting recognition');
   }

}

/*********************************************************************/

function processNLUServerQuery() {

   console.log('processNLUQuery: nluQuery = ' + nluQuery);

   if(nluQuery.length > 0) {

      data = "content=" + encodeURIComponent(nluQuery);
      update(nluServerQuery, data, postReloadContent);

      nluQuery = ''
   
   }

   
}

     

