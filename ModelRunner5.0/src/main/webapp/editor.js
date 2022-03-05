/*********************************************************************/
// Global Variables
/*********************************************************************/

var canvas;
var canvasParent;

var dropDownCheckList = null; //document.getElementById('list1');
var dropDownItems = null; //document.getElementById('items');

var editor;

var ctx;

var popupPanel;
var isPopupPanelVisible = false;

var currentView;

var boxTop;
var boxLeft;

var canvasLeft;
var canvasTop;
var canvasWidth;
var canvasHeight;

var application = null;
var sessionId = null;

var isMouseDown = false;
var mouseDownX = -1;
var mouseDownY = -1;

var mouseOverObjectId = -1;
var selectedObjectId = -1;

var popupX,popupY;

var rootItem = null;
var objects = [];

var form = null;

var newItemX=100, newItemY=100;

var nextObjectId = 0;

var copyId = -1;

/*********************************************************************/
// Constants
/*********************************************************************/

var DEBUG = 1;

const DEFAULT_CHILD_SPACE = 20;

const ARROWHEADLEN = 15;

const OPENITEM=2, SAVEITEM=3, GETITEM=4, SAVEITEM_CLEAN=6;

// Shape styles               
const SHAPE_NAMES          = [APPLICATION_SHAPE_NAMES, WORKFLOW_SHAPE_NAMES];
const SHAPE_WIDTHS         = [APPLICATION_SHAPE_WIDTHS, WORKFLOW_SHAPE_WIDTHS];
const SHAPE_HEIGHTS        = [APPLICATION_SHAPE_HEIGHTS, WORKFLOW_SHAPE_HEIGHTS];
const SHAPE_BORDER_COLOURS = [APPLICATION_SHAPE_BORDER_COLOURS, WORKFLOW_SHAPE_BORDER_COLOURS];
const SHAPE_BORDER_WIDTHS  = [APPLICATION_SHAPE_BORDER_WIDTHS, WORKFLOW_SHAPE_BORDER_WIDTHS];
const SHAPE_BACK_COLOURS   = [APPLICATION_SHAPE_BACK_COLOURS, WORKFLOW_SHAPE_BACK_COLOURS];
const SHAPE_TEXT_COLOURS   = [APPLICATION_SHAPE_TEXT_COLOURS, WORKFLOW_SHAPE_TEXT_COLOURS];
const SHAPE_FONTS          = [APPLICATION_SHAPE_FONTS, WORKFLOW_SHAPE_FONTS];
const SHAPE_RADIUS         = [APPLICATION_SHAPE_RADIUS, WORKFLOW_SHAPE_RADIUS];

const COMMON_FIELDS = [APPLICATION_COMMON_FIELDS, WORKFLOW_COMMON_FIELDS];
const FIELDS = [APPLICATION_FIELDS, WORKFLOW_FIELDS];
const ROOT_ITEM_FIELDS = [APPLICATION_ROOT_ITEM_FIELDS, WORKFLOW_ROOT_ITEM_FIELDS];

/*********************************************************************/
// Initialization
/*********************************************************************/

function init(editParam) {

   editor = editParam;

   canvas = document.getElementById('canvas');
   canvasParent = document.getElementById("canvasParent");
   popupPanel = document.getElementById("popuppanel");

   var bb = canvas.getBoundingClientRect();
   boxLeft = bb.left;
   boxTop = bb.top;

   canvas.addEventListener('mousemove', mouseMoved, false);
   canvas.addEventListener('mousedown', mouseDown, false);
   canvas.addEventListener('mouseup', mouseUp, false);
   window.addEventListener('resize', resizeCanvas, false);
   
   // window.addEventListener('keydown',keyDown,true);
   // canvas.onkeypress = function(e) {
   //    var e = window.event || e;
   //    var code = e.charCode || e.keyCode;
   //    currentView.keyEvent(code, e.ctrlKey, e.shiftKey);
   //    return false;
   // };
   
   ctx = canvas.getContext("2d");

   resizeCanvas();

}

/*********************************************************************/

function newItem() {
   if(rootItem == null || rootItem.isSaved) {
      getNewRootItem();
      repaint();
   } else {
      alert("Please save your changes!");
   }
}

/*********************************************************************/

function openItem() {

   if(rootItem == null  || rootItem.isSaved) {
      var query = getServerQuery(OPENITEM,editor, []);
      getPopupPanel(query, 100, 100);
   } else {
      alert("Please save your changes!");
   }

}

/*********************************************************************/

function getItem(itemId) {

   var query = getServerQuery(GETITEM,editor, [["itemid", itemId]]);
   if(editor == 0) {
      sendLoadApplication(query);
   } else {
      sendReloadGet(query, postGetWorkflow);
   }

}

/*********************************************************************/

function postGetWorkflow(json) {

   hidePopupPanel();
   loadWorkflow(json);
   repaint();

}

/*********************************************************************/

function uploadItem() {

   if(rootItem == null  || rootItem.isSaved) {
      var input = document.createElement('input');
      input.type = 'file';
      input.onchange = e => { 
         input = null;
         var file = e.target.files[0]; 
         var reader = new FileReader();
         reader.readAsText(file,'UTF-8');
         reader.onload = readerEvent => {
            var content = readerEvent.target.result; 
            postUploadItem(content);
         }
      }
   
      input.click();
   } else {
      alert("Please save your changes!");
   }



}

/*********************************************************************/

function postUploadItem(data) {

   if(editor == 0) {
      var parser = new DOMParser();
      var xmlDoc = parser.parseFromString(data,"text/xml");
      postLoadApplication(xmlDoc);
   } else {
      postGetWorkflow(data);
   }

}

/*********************************************************************/

function downloadItem() {

   if(rootItem != null) {
      if(rootItem.values[0] == null || rootItem.values[0].length == 0) {
         alert("Please provide the Model's name in the Edit panel ...");
      } else {
         downloadFile(rootItem.values[0] + (editor == 0 ? ".xml" : ".json"), rootItem.getSaveData());
         rootItem.isSaved = true;
      }
   }

}

/*********************************************************************/

function publishItem(isClean) {

   if(rootItem != null) {
      if(editor == 0 && (rootItem.values[0] == null || rootItem.values[0].length == 0)) {
         alert("Please provide the Application's name in the Application Edit panel ...");
      } else {
         var queryAction = SAVEITEM;
         if(isClean) {
            queryAction = SAVEITEM_CLEAN; 
         } 
         var query = getServerQuery(queryAction, editor, [["itemid", rootItem.id]]);
         var itemData = rootItem.getSaveData();
         var data = "itemdata=" + encodeURIComponent(itemData);
         if(editor == 0) {
            data += "&tenant=Default";
         }
         update(query, data, postPublishItem)
      }
   }

}

/*********************************************************************/

function postPublishItem(res) {

   if(rootItem != null) {
      rootItem.isSaved = true;
      alert(res);
   }

}

/*********************************************************************/

function getNewRootItem() {
   if(editor == 0) {
      rootItem = new Application(-1, ["","","","","","","","","","","",""]);
      var dbRef0 = new DbRef(getNextId(), rootItem, ["Default","","","","","","", "","","",DEFAULT_DBTYPE, "", DEFAULT_DBDRIVER, "false"], 15, 15, 70, 50);
      rootItem.addChild(DBREF, dbRef0);
   } else {
      rootItem = new Workflow();
   }
   rootItem.initValues(-1, rootItem.values);
}

/*********************************************************************/

function hideRefs(id) {
   if(id == -1) {
      rootItem.setShowRefs(false);
   } else {
      objects[id].item.setShowRefs(false);
   }
   repaint();
}

/*********************************************************************/

function showRefs(id) {
   if(id == -1) {
      rootItem.setShowRefs(true);
   } else {
      objects[id].item.setShowRefs(true);
   }
   repaint();
}

/*********************************************************************/
// Repaint
/*********************************************************************/

function repaint() {

   if(rootItem != null) {
      rootItem.draw();
   } else {
      ctx.fillStyle = NO_WORKFLOW_BACKGROUND_COLOUR;
      drawRoundedRectangle(0, 0, canvas.width, canvas.height, 5, true, true);
      // ctx.fillStyle = DEFAULT_BACK_COLOUR;
      // drawRoundedRectangle(600, 300, 200, 100, 5, true, true);
   }

}

/*********************************************************************/
// Listeners
/*********************************************************************/

function resizeCanvas() {

   log(0,"Resize()");

   canvas.width = canvasParent.offsetWidth;
   canvas.height = canvasParent.offsetHeight;
   
   canvasWidth = canvas.width;
   canvasHeight = canvas.height;
   
   canvasLeft = 0; // not used for now, probably needed for scrolling
   canvasTop = 0; 

   repaint();

}

/*********************************************************************/

function mouseMoved(e) {

   var x = null;
   var y = null;

   if(rootItem != null) {
   
      if (e.layerX || e.layerX == 0) { // Firefox
         x = e.layerX;
         y = e.layerY;
      } else if (e.offsetX || e.offsetX == 0) { // Opera
         x = e.offsetX;
         y = e.offsetY;
      }
   
      if(isMouseDown) {
         var dx = x - mouseDownX;
         var dy = y - mouseDownY;
         if(selectedObjectId != -1) {
            objects[selectedObjectId].item.move(dx, dy);
            repaint();
         } else {
            canvasLeft -= dx;
            canvasTop -= dy;
            // log(0,"moved dx=" + dx + " / dy=" + dy);
            repaint();
         }
         mouseDownX = x;
         mouseDownY = y;
      
      } else {

         // log(0,"moved x=" + x + " / y=" + y);

         var id  = rootItem.checkObjects(x + canvasLeft, y + canvasTop);
         // if(id != -1) {
         //    log(0, "mouseMoved() id=" + id + "object=" + objects[id].item.values[0]);
         // } else {
         //    log(0, "mouseMoved() id=" + id);
         // }
         if(id != mouseOverObjectId) {

            if(mouseOverObjectId != -1) {
               objects[mouseOverObjectId].isMouseOver = false;
            }
      
            mouseOverObjectId = id;
            if(id != -1) {
               objects[id].isMouseOver = true;
            }

            repaint();
         
         } 
      
      }
   
   }

}

/*********************************************************************/

function mouseDown(e) {

   var x = null;
   var y = null;

   if (e.layerX || e.layerX == 0) { // Firefox
      x = e.layerX;
      y = e.layerY;
   } else if (e.offsetX || e.offsetX == 0) { // Opera
      x = e.offsetX;
      y = e.offsetY;
   }

   if(isPopupPanelVisible) {
      hidePopupPanel();
   } else {

      if(rootItem != null) {

         var isRightClick = e.which == 3;
         var isDoubleClick = event.detail > 1;
   
         var id = rootItem.checkObjects(x + canvasLeft, y + canvasTop);
         if(id != -1) {
            log(0, "mouseDown() id=" + id + "object=" + objects[id].item.values[0]);
         } else {
            log(0, "mouseDown() id=" + id);
         }
      
         if(isRightClick) {      
            if(id != -1 && selectedObjectId != id) {
               updateSelectedObject(id);
            }
            showContextMenu(id, x, y);
         } else if(isDoubleClick) {      
            if(id != -1 && selectedObjectId != id) {
               updateSelectedObject(id);
            }
            showUpdateForm(id, x, y);
         } else {
            if(isPopupPanelVisible) {
               hidePopupPanel();
            } else {
               updateSelectedObject(id);
               isMouseDown = true;
               mouseDownX = x;
               mouseDownY = y;
            }
         }
      
         log(0, "mouseDown() x=" + x + " / y=" + y);
      
         return false;
      
      }
   
   }

}

/*********************************************************************/

function mouseUp(e) {

   if(rootItem != null) {
      isMouseDown = false;     
   }

}

/*********************************************************************/

function keyDown(e){

   if(rootItem != null) {
      var e = window.event || e;
      var code = e.charCode || e.keyCode;
   }

}

/*********************************************************************/

function updateSelectedObject(id){

   if(selectedObjectId != id) {
      
      if(selectedObjectId != -1) {
         if (typeof objects[selectedObjectId] !== 'undefined') {
            objects[selectedObjectId].isSelected = false;
         }
      }

      selectedObjectId = id;
      if(id != -1) {
         objects[id].isSelected = true;
      }

      repaint();
   
   }

}

/*********************************************************************/

function deleteItem(id) {

   if(true) { //objects[id].item.deleteItem(true)) {
      objects[id].item.deleteItem(false);
      selectedObjectId = -1;
      mouseOverObjectId = -1;
      repaint();
   } else {
      alert("Can't delete referenced item.");
   }

}

/*********************************************************************/

function checkDelete(id) {

   var ret = false;

   return ret;

}

/*********************************************************************/

function deleteObject(id) {

   var lastId = objects.length -1;

   if(id != lastId) {
      objects[id] = objects[lastId];
      objects[id].id = id;
      objects[id].item.id = id;
   }

   objects.pop();
   nextObjectId--;

}

/*********************************************************************/

function deleteChild(children, id) {

   var ret = [];
   for(var nChild=0; nChild < children.length; nChild++) {
      if(children[nChild].id != id) {
         ret.push(children[nChild]);
      }
   }

   return ret;

  };
  
/*********************************************************************/

function copy(id, x, y) {
   copyId = id;
}

/*********************************************************************/

function paste(id, x, y) {

   var child = objects[copyId].item;
   dx = x - objects[copyId].x;
   dy = y - objects[copyId].y;

   var parent = rootItem;

   if(id != -1) {
      parent = objects[id].item;
   }

   var newChild = child.clone(parent, dx, dy);
   parent.addChild(newChild.obj.type, newChild);

   updateSelectedObject(newChild.id);

   copyId = -1;

   repaint();

}

/*********************************************************************/
// Context Menu
/*********************************************************************/

function showContextMenu(id, x, y) {
   var html = getContextMenu(id, x, y);
   showPopupPanel(html, x, y);
}

/*********************************************************************/

function getContextMenu(id, x, y) {

   var ret = "";

   ret = getContextMenuLink("showUpdateForm(" + id + "," + x + "," + y + ");", "Edit");

   if(id != -1) {
      ret += getContextMenuLink("copy(" + id + "," + x + "," + y + ");", "Copy");
   }
   
   if(copyId != -1 && copyId != id) {
      ret += getContextMenuLink("paste(" + id + "," + x + "," + y + ");", "Paste");
   }

   if(id == -1) {
      ret += rootItem.getContextMenu(x, y)
   } else {
      ret += getContextMenuLink("deleteItem(" + id + ");", "Delete");
      ret += objects[id].item.getContextMenu(id, x, y)
   }

   return ret;

 }

/*********************************************************************/

function getContextMenuLink(action, label) {
   return "<li><button onclick=\"event.preventDefault(); hidePopupPanel(); " + action + "\">" + label + "</button><li>";
 }

/*********************************************************************/
// Forms
/*********************************************************************/

function showNewForm(type, parentId, x, y) {

   log(0, "showNewForm()");

   form = new Form(type, null, SHAPE_NAMES[editor][type], "hidePopupPanel(); processFormNew(" + type + "," + parentId + "," + x + "," + y + ", false);", COMMON_FIELDS[editor].concat(FIELDS[editor][type]));
   showPopupPanel(form.getHTML(), x, y);

}

/*********************************************************************/

function showUpdateForm(id, x, y) {

   log(0, "showUpdateForm()");

   if(id == -1) {
      if(editor == 0) {
         form = new Form(-1, rootItem, "Application", "hidePopupPanel(); processFormUpdate(-1);", getFields(-1));
      } else {
         form = new Form(-1, rootItem, "Workflow", "hidePopupPanel(); processFormUpdate(-1);", getFields(-1));
      }
      showPopupPanel(form.getHTML(), x, y);
   } else {
      var obj = objects[id];
      var type = obj.type;
      form = new Form(obj.type, obj.item, SHAPE_NAMES[editor][type], "hidePopupPanel(); processFormUpdate(" + id + ");", getFields(type));
      showPopupPanel(form.getHTML(), x, y);
   }


}

/*********************************************************************/

function processFormNew(type, parentId, x, y) {
   rootItem.processFormNew(type, parentId, x, y);
 }

/*********************************************************************/

function processFormUpdate(id) {
   rootItem.processFormUpdate(id);
 }

/*********************************************************************/

function getFields(type) {

   var fields = null;

   if(type == -1) {
      if(editor == 0) {
         fields = COMMON_FIELDS[editor].concat(APPLICATION_ROOT_ITEM_FIELDS);
      } else {
         fields = WORKFLOW_ROOT_ITEM_FIELDS;
      }
   } else {
      fields = COMMON_FIELDS[editor].concat(FIELDS[editor][type]);
   }

   return fields;

}

/*********************************************************************/

function initValues(type) {

   var values = [];
   var fields = getFields(type);
   for(var nField=0; nField < fields.length; nField++) {
      values.push("");
   }
   rootItem.initValues(type, values);
   return values;
}

/*********************************************************************/

function Form(type, item, title, action, fields) {

   this.type = type;
   this.item = item;
   this.title = title;
   this.action = action;
   this.fields = fields;
   if(item != null) {
      this.values = item.values;
   } else {
      this.values = initValues(type);
   }

   /*********************************************************************/

   this.getHTML = function() {

      log(0, "Form.getHTML()");

		var ret = "<div class=\"datapane\"><h1 class=\"pagetitle\">" +  title + "</h1>";
		
      ret += "<form name=\"WorkflowItemForm\">";

      ret += "<table><colgroup><col width=200/><col width=*/></colgroup>";

      for(var nField=0; nField < this.fields.length; nField++) {
         ret += this.getFieldHTML(nField);
      }

      ret += "</table>";
      ret += "<button type = \"submit\" onclick=\"event.preventDefault(); " + this.action + "\">Ok</button>";

      ret += "</form>";

      return ret;

   };

   /*********************************************************************/

   this.getFieldHTML = function(nField) {

      log(0, "Form.getFieldHTML()");

      var ret = "";

      var field = this.fields[nField];
      var value = this.values[nField];

      ret += "<tr><td valign=\"top\">" + field[0] + "</td>";

      var objId = -1;
      if(this.item != null && this.item.id != -1) {
         objId = this.item.obj.id;
      }

      if(field[1] == SELECT) {
         ret += rootItem.getSelectHTML(objId, nField, field, value);
      } else if(field[1] == BOOLEAN) {
         var checked = (value ? "checked" : "");
         ret += "<td><input name=\"field" + nField + "\" type='checkbox' " + checked + " /></td></tr>";
      } else {
         // ret += "<td><input name=\"field" + nField + "\" type='text' value=\"" + value + "\" oninput=\"rootItem.valueChanged(" + objId + ", " + nField  + ",  this);\" /></td></tr>";
         ret += "<td><input name=\"field" + nField + "\" type='text' value=\"" + value + "\" /></td></tr>";
      }

      return ret;

   };

}

/*********************************************************************/

function removeElementFromArray(array, item) {

   var ret = [];

   for(let index = 0; index < array.length; index++) {
      const element = array[index];
      if(element != item) {
         ret.push(element);
      }
   }

   return ret;

 }

/*********************************************************************/
// Link
/*********************************************************************/

function getLink(id, type, item) {

   var ret = new Link(id, type, item);
   objects[id] = ret;

   return ret;

 }

/*********************************************************************/

function Link(id, type, item) {

   this.id = id;
   this.type = type;
   this.item = item;

   this.isSelected = false;
   this.isMouseOver = false;

   /*********************************************************************/

   this.refresh = function() {

      log(0, "Link.refresh()");

      this.x1 = this.item.source.obj.xm;
      this.y1 = this.item.source.obj.ym;
      this.x2 = this.item.target.obj.xm;
      this.y2 = this.item.target.obj.ym;
      this.m = (this.y2 - this.y1) / (this.x2 - this.x1);
      this.b = this.y1 - this.m * this.x1;

   };

   /*********************************************************************/

   this.refresh();
   
   /*********************************************************************/

   this.draw = function() {

      // log(0, "Link.draw()");

      ctx.strokeStyle = EDITABLE_LINK_COLOUR;
      ctx.lineWidth = EDITABLE_LINK_WIDTH;

      if(this.isMouseOver) {
         ctx.strokeStyle = MOUSEOVER_LINK_COLOUR;
      } else if(this.isSelected) {
         ctx.strokeStyle = SELECTED_LINK_COLOUR;
      }

      drawShapeLink(this.item.source.obj, this.item.target.obj); 

      if(this.item.nLabel != -1) {
         var label = this.item.values[this.item.nLabel];
         if(label != null && label.length > 0) {
            ctx.fillStyle = SHAPE_TEXT_COLOURS[editor][this.type];
            var x = (this.x1 + this.x2) / 2 - canvasLeft;
            var y = (this.y1 + this.y2) / 2 - canvasTop;
            ctx.save();   
            ctx.translate(x, y);   
            ctx.rotate(Math.atan(this.m));   
            ctx.fillText(label, -ctx.measureText(label).width/2, -5);   
            ctx.restore();     
         }
      }

      this.item.source.obj.draw();
      this.item.target.obj.draw();

   };

   /*********************************************************************/

   this.check = function(x, y) {

      var ret = true;

      if(this.x1 < this.x2) {
         if(x < this.x1 || x > this.x2) {
            ret = false;
         }
      } else {
         if(x < this.x2 || x > this.x1) {
            ret = false;
         }
      }

      if(ret) {

         if(this.y1 < this.y2) {
            if(y < this.y1 || y > this.y2) {
               ret = false;
            }
         } else {
            if(y < this.y2 || y > this.y1) {
               ret = false;
            }
         }

      }

      if(ret) {
         yy = this.m * x + this.b;
         d = y - yy;
         if(d > 5 || d < -5) {
            ret = false;
         }
      }

      return ret;

   }

}

/*********************************************************************/
// Shapes
/*********************************************************************/

function getShape(id, type, item, x, y, w, h) {

   // var ret = new Shape(type, objects.length, item, x, y, w, h);
   var ret = new Shape(type, id, item, x, y, w, h);
   objects[id] = ret;

   return ret;

 }

/*********************************************************************/

function Shape(type, id, item, x, y, w, h) {

   this.type = type;
   this.id = id;
   this.item = item;

   this.width = w;
   if(this.width == -1) {
      this.width = SHAPE_WIDTHS[editor][type];
   }
   this.height = h;
   if(this.height == -1) {
      this.height = SHAPE_HEIGHTS[editor][type];
   }

   this.radius = SHAPE_RADIUS[editor][type];

   this.diag = Math.sqrt(this.width * this.width + this.height * this.height);

   /*********************************************************************/

   this.moveTo = function(x, y) {
      this.x = x;
      this.y = y;
      this.x0 = this.x;
      this.y0 = this.y;
      this.x1 = this.x + this.width;
      this.y1 = this.y + this.height;
      this.xm = this.x + this.width / 2;
      this.ym = this.y + this.height / 2;
   }

   this.moveTo(x, y);

   /*********************************************************************/

   this.isSelected = false;
   this.isMouseOver = false;
   this.isMouseOverBorder = false;
   this.resizePoint = -1;

   this.isResizable = false;

   this.specialBorderColour = null;

   /*********************************************************************/

   this.draw = function() {

      ctx.fillStyle = SHAPE_BACK_COLOURS[editor][this.type];
      ctx.strokeStyle = SHAPE_BORDER_COLOURS[editor][this.type];
      ctx.font = SHAPE_FONTS[editor][this.type];
      ctx.lineWidth = SHAPE_BORDER_WIDTHS[editor][this.type];

      if(this.specialBorderColour != null) {
         ctx.strokeStyle = this.specialBorderColour;
      }
      if(this.isSelected) {
         ctx.fillStyle = HIGHLIGHT_BACK_COLOUR;
         ctx.font = HIGHLIGHT_FONT;
      }

      if(this.isMouseOverBorder) {
         ctx.lineWidth = SHAPE_RESIZE_BORDER_WIDTH;
      }
      
      if(this.isMouseOver) {
         ctx.strokeStyle = HIGHLIGHT_BORDER_COLOUR;
         ctx.font = HIGHLIGHT_FONT;
      }

      drawRoundedRectangle(this.x - canvasLeft, this.y - canvasTop, this.width,  this.height, this.radius, true, true);

      ctx.fillStyle = SHAPE_TEXT_COLOURS[editor][this.type];

      var label = "...";
      if(this.item.values != null) {
         label = this.item.values[0];
      }

      var x = 5;
      var y = 15;
      var ms = ctx.measureText(label);
      if(ms.width > this.width) {
         label = label.substr(0,10) + "...";
         ms = ctx.measureText(label);
      }

      if(editor == 1 || this.type == DBREF || this.type == ACTION) {
         x = (this.width - ms.width) / 2
         y = this.height / 2 + 5;
      }

      ctx.fillText(label, this.x + x - canvasLeft, this.y + y - canvasTop);

   };

   /*********************************************************************/

    this.move = function(dx, dy) {

      if(this.isMouseOverBorder) {
         if(this.resizePoint == 0) {
            this.x += dx;
            this.y += dy;
            this.x0 += dx;
            this.y0 += dy;
         } else {
            this.x1 += dx;
            this.y1 += dy;
         }

         this.xm += dx;
         this.ym += dy;

         this.width = Math.abs(this.x1 - this.x0);
         this.height = Math.abs(this.y1 - this.y0);      
         this.diag = Math.sqrt(this.width * this.width + this.height * this.height);
      
         this.xm = this.x + this.width / 2;
         this.ym = this.y + this.height / 2;
         
      } else {
         this.x += dx;
         this.y += dy;
         this.x0 += dx;
         this.y0 += dy;
         this.x1 += dx;
         this.y1 += dy;
         this.xm += dx;
         this.ym += dy;
      }

   };

   /*********************************************************************/

   this.check = function(x, y) {

      var ret = false;

      var onBorder = false;
      var point = -1;

      if((x > this.x0 && x < this.x1) && (y > this.y0 && y < this.y1)) {
         ret = true;
         if(this.isResizable) {
            if(x < this.x0 + RECT_RESIZE_WIDTH || y < this.y0 + RECT_RESIZE_WIDTH) {
               onBorder = true;            
               point = 0;
            }
            if(x > this.x1 - RECT_RESIZE_WIDTH || y > this.y1 - RECT_RESIZE_WIDTH) {
               onBorder = true;
               point = 1;
            }
         }
      }

      if(this.isMouseOverBorder != onBorder) {
         this.isMouseOverBorder = onBorder;
         this.resizePoint = point;
         repaint();
      }

      return ret;

   }

}

/*********************************************************************/

function getItemFromValue(nValue, value, type) {

   log(0, "getItemFromName()");

   var ret = -1;

   for(var nObject=0; ret == -1 && nObject < objects.length; nObject++) {
      var obj = objects[nObject];
      if(obj != null) {
         var item = obj.item;
         if(item.values != null && item.values[nValue] == value) {
            if(type == -1 || obj.type == type)
            ret = nObject;
         }
      }
   }

   return ret;

}

/*********************************************************************/
// Drawing
/*********************************************************************/

function draw3DRoundedRectangle(x, y, w, h, radius, stroked, filled) {

   log(9, "draw3DRoundedRectangle()");
   drawRoundedRectangle(x+2, y-2, w, h, radius, stroked, false);
   drawRoundedRectangle(x, y, w, h, radius, stroked, filled);

}

/*********************************************************************/

function drawRoundedRectangle(x, y, w, h, radius, stroked, filled) {

   log(9, "drawRoundedRectangle()");
   ctx.beginPath();
   ctx.moveTo(x + radius, y);
   ctx.lineTo(x + w - radius, y);
   ctx.quadraticCurveTo(x + w, y, x + w, y + radius);
   ctx.lineTo(x + w, y + h - radius);
   ctx.quadraticCurveTo(x + w, y + h, x + w - radius, y + h);
   ctx.lineTo(x + radius, y + h);
   ctx.quadraticCurveTo(x, y + h, x, y + h - radius);
   ctx.lineTo(x, y + radius);
   ctx.quadraticCurveTo(x, y, x + radius, y);
   ctx.closePath();
   if(filled) {
     ctx.fill();
   }
   if(stroked) {
     ctx.stroke();
   }

}

/*********************************************************************/

function drawRectangle(x, y, w, h, stroked, filled) {

   log(9, "drawRectangle()");

   if(filled) {
     ctx.fillRect(x,y,w,h);
   }

   if(stroked) {
     ctx.strokeRect(x,y,w,h);
   }

}

/*********************************************************************/

function drawCircle(x, y, r, stroked, filled) {

   log(9, "drawCircle()");

   ctx.beginPath();
   ctx.arc(x, y, r, 0, 2 * Math.PI, false);

   if(filled) {
      ctx.fill();
   }

   if(stroked) {
      ctx.stroke();
   }

}

/*********************************************************************/

function drawRelationship(x1, y1, x2, y2) {

   log(9, "drawRelationship()");

}

/*********************************************************************/

function drawLine(x1, y1, x2, y2) {

   log(9, "drawLine()");

   ctx.beginPath();
   ctx.moveTo(x1, y1);
   ctx.lineTo(x2, y2);
   ctx.stroke();

}

/*********************************************************************/

function drawArrow(x1, y1, x2, y2, len) {

   ctx.beginPath();
   var angle = Math.atan2(y2 - y1, x2 - x1);
   ctx.moveTo(x1, y1);
   ctx.lineTo(x2, y2);
   ctx.lineTo(x2 - len * Math.cos(angle - Math.PI / 6), y2 - len * Math.sin(angle - Math.PI / 6));
   ctx.moveTo(x2, y2);
   ctx.lineTo(x2 - len * Math.cos(angle + Math.PI / 6), y2 - len * Math.sin(angle + Math.PI / 6));
   ctx.stroke();

}

/*********************************************************************/

function drawShapeLink(shape1, shape2) {

   var x1 = shape1.xm - canvasLeft;
   var y1 = shape1.ym - canvasTop;
   var x2 = shape2.xm - canvasLeft;
   var y2 = shape2.ym - canvasTop;

   var w = shape2.width / 2;
   var h = shape2.height / 2;

   var m = (y2 - y1) / (x2 - x1);
   var b = y1 - m * x1;

   var xx, yy;

   if(x1 < x2 && y1 < y2) {
      var xx = x2 - w;
      var yy = m * xx + b;
      if (yy < y2 - h) {
         yy = y2 - h;
         xx = (yy -b) / m;
      }   
   } else if(x1 > x2 && y1 > y2){
      var xx = x2 + w;
      var yy = m * xx + b;
      if (yy > y2 + h) {
         yy = y2 + h;
         xx = (yy -b) / m;
      }   
   } else if(x1 > x2 && y1 < y2){
      var xx = x2 + w;
      var yy = m * xx + b;
      if (yy < y2 - h) {
         yy = y2 - h;
         xx = (yy -b) / m;
      }   
   } else {
      var xx = x2 - w;
      var yy = m * xx + b;
      if (yy > y2 + h) {
         yy = y2 + h;
         xx = (yy -b) / m;
      }   
   }

   ctx.beginPath();
   var angle = Math.atan2(yy - y1, xx - x1);
   ctx.moveTo(x1, y1);
   ctx.lineTo(xx, yy);
   ctx.lineTo(xx - ARROWHEADLEN * Math.cos(angle - Math.PI / 6), yy - ARROWHEADLEN * Math.sin(angle - Math.PI / 6));
   ctx.moveTo(xx, yy);
   ctx.lineTo(xx - ARROWHEADLEN * Math.cos(angle + Math.PI / 6), yy - ARROWHEADLEN * Math.sin(angle + Math.PI / 6));
   ctx.stroke();

}

/*********************************************************************/

function findEdges(x1, y1, x2, y2, cutoff) {

   if (!cutoff) cutoff = 220; // alpha threshold
   var dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1),
       sx = x2 > x1 ? 1 : -1,  sy = y2 > y1 ? 1 : -1;
   var x0 = Math.min(x1,x2), y0=Math.min(y1,y2);
   var pixels = ctx.getImageData(x0,y0,dx+1,dy+1).data;
   var hits=[], over=null;
   for (x=x1,y=y1,e=dx-dy; x!=x2||y!=y2;){
     var alpha = pixels[((y-y0)*(dx+1)+x-x0)*4 + 3];
     if (over!=null && (over ? alpha<cutoff : alpha>=cutoff)){
       hits.push({x:x,y:y});
     }
     var e2 = 2*e;
     if (e2 > -dy){ e-=dy; x+=sx }
     if (e2 <  dx){ e+=dx; y+=sy  }
     over = alpha>=cutoff;
   }

   return hits;

 }

/*********************************************************************/
// Popup Panel
/*********************************************************************/

function getPopupPanel(query, x, y) {

   popupX = x;
   popupY = y;

   sendReloadGet(query, popupPanelShow);

}

/*********************************************************************/

function showPopupPanel(html, x, y) {

   popupX = x;
   popupY = y;

   popupPanelShow(html)

}

/*********************************************************************/

function popupPanelShow(html) {

   popupPanel.innerHTML = html;
   popupPanel.style.left = popupX + boxLeft + 'px';
   popupPanel.style.top = popupY + boxTop + 'px';
   popupPanel.classList.add("popuppanel--active");

   isPopupPanelVisible = true;

   updateRefBox();
   
}

/*********************************************************************/

function updateRefBox() {

   dropDownCheckList = document.getElementById('list1');
   dropDownItems = document.getElementById('items');
   
   if(dropDownCheckList != null && dropDownItems != null) {
   
      dropDownCheckList.getElementsByClassName('anchor')[0].onclick = function(evt) {
         if (dropDownItems.classList.contains('visible')) {
            dropDownItems.classList.remove('visible');
            dropDownItems.style.display = "none";
         } else {
            dropDownItems.classList.add('visible');
            dropDownItems.style.display = "block";
         }
   
      };
       
       dropDownItems.onblur = function(evt) {
         dropDownItems.classList.remove('visible');
       };
       
   }
   
}

/*********************************************************************/

function hidePopupPanel() {
   isPopupPanelVisible = false;
   popupPanel.classList.remove("popuppanel--active");
}
     
/*********************************************************************/

function getNextId() {
   return nextObjectId++;
}

/*********************************************************************/

function checkId(id) {

   if(id >= nextObjectId) {
      nextObjectId = id+1;
   }

}

/*********************************************************************/

const ACTIONS_WORDS = ['add', 'insert', 'create', 'publish', 'deploy', 'download', 'edit', 'change', 'set', 'update', 'select']
const ADD_ACTIONS_ACTIONS = [0, 0, 0, 1, 1, 2, 3, 3, 3, 3, 4]

const ARTEFACTS = ['application', 'package', 'type', 'attribute']

const ATTRIBUTES = ['name', 'type', 'reference']

const ATTRIBUTE_TYPES = ["string", "integer", "boolean", "date", "dateTime", "duration", "phone", "email", "price", "text",  "backRef", "domain", "reference", "refbox", "reftree", "aggregation", "composition", "extension"];

var nluLastPackageId = -1;
var nluLastTypeId = -1;
var nluLastAttrbuteId = -1;

var nluSelectedId = -1;

/**********************************/

function processNLUEditorQuery() {

   console.log('processNLUEditorQuery(): nluQuery = ' + nluQuery);

   if(nluQuery.length > 0) {

      var commands = [];
      var command = "";
   
      var words = nluQuery.split(' ');
   
      for (var nWord = 0; nWord < words.length; nWord++) {
   
         var word = words[nWord];
   
         console.log('processNLUEditorQuery(): word = ' + word);
   
         if(word == 'and') {
            commands.push(command);
            command = '';
         } else {

            if(word != 'please') {
               if(command.length > 0) {
                  command += ' ';
               }
               command += word;
            }
   
         }
   
      }

      if(command.length > 0) {
         commands.push(command);
      }
   
      for (var nCommand = 0; nCommand < commands.length; nCommand++) {
         processNLUCommand(commands[nCommand]);
      }
      
      nluQuery = ''
      
   }

}

/**********************************/

function processNLUCommand(command) {

   console.log('processNLUCommand(): command = ' + command);

   var words = command.split(' ');

   var queryAction = -1;
   var queryArtefact = -1;
   var queryAttribute = -1;
   var queryAttributeType = -1;

   var nTo = -1;
   var setToValue = '';
   var setToValue2 = '';

   var nCalled = -1;
   var name = '';
   var name2 = '';

   var isClean = false;

   for (var nWord = 0; nWord < words.length; nWord++) {

      var word = words[nWord];

      if(word == 'to' || word == '2'  || word == 'two') {
         nTo = nWord;
      } if(word == 'called' || word == 'call'  || word == 'cold') { // don't add code because it breaks the zip code attribute!
         nCalled = nWord;
      } else if(word == 'clean') {
         isClean = true;
      } else if(nCalled == -1) {

         for (var nAction = 0; nAction < ACTIONS_WORDS.length; nAction++) {
            var action = ACTIONS_WORDS[nAction];
            if(word.equalIgnoreCase(action)) {
               queryAction = ADD_ACTIONS_ACTIONS[nAction];
            }
         }
   
         for (var nArtefact = 0; nArtefact < ARTEFACTS.length; nArtefact++) {
            var artefact = ARTEFACTS[nArtefact];
            if(word.equalIgnoreCase(artefact)) {
               queryArtefact = nArtefact;
            }
         }
   
         for (var nAttribute = 0; nAttribute < ATTRIBUTES.length; nAttribute++) {
            var attribute = ATTRIBUTES[nAttribute];
            if(word.equalIgnoreCase(attribute)  && nTo == -1) {
               queryAttribute = nAttribute;
            }
         }

         for (var nAttributeType = 0; nAttributeType < ATTRIBUTE_TYPES.length; nAttributeType++) {
            var attributeType = ATTRIBUTE_TYPES[nAttributeType];
            if(word.equalIgnoreCase(attributeType)) {
               queryAttributeType = nAttributeType;
            }
         }

      }

    }

    if(nTo != -1 && (nTo + 1) < words.length) {
       for(var n = nTo +1; n < words.length; n++) {
         if(setToValue.length > 0) {
            setToValue += ' ';
         }
        setToValue += words[n];
        setToValue2 += words[n];
       }
       console.log('processNLUCommand(): setToValue = ' + setToValue)
    }

    if(nCalled != -1 && (nCalled + 1) < words.length) {
      for(var n = nCalled +1; n < words.length; n++) {
        if(name.length > 0) {
         name += ' ';
        }
        name += words[n];
        name2 += words[n];
      }
      console.log('processNLUCommand(): name = ' + name)
   }

   if(queryAction == 1) { // deploy

      console.log('processNLUCommand(): Deploying application');

      if(rootItem == null) {
         alert("Please create an Application first");
      } else {
         publishItem(isClean);
      }

   } else if(queryAction == 2) { // download

      console.log('processNLUCommand(): Downloading application');

      if(rootItem == null) {
         alert("Please create an Application first");
      } else {
         downloadItem();
      }

   } else if(queryAction == 0) { // create, add, insert

      updateForm = null;

      if(queryArtefact == 0) { // application

         console.log('processNLUCommand(): create application');

         if(rootItem == null) {
            newItem() 
            nluSelectedId = -1;
         } else {
            alert("Please save the current Application first");
         }

      } else if(queryArtefact == 1) { // package

         console.log('processNLUCommand(): create package');

         if(rootItem == null) {
            alert("Please create an Application first");
         } else {
            var x = 100 + 850 * rootItem.packages.length;
            rootItem.processFormNew(PACKAGE, -1, x, 100, true)
            nluLastPackageId = selectedObjectId;
            nluSelectedId = selectedObjectId;
         }

      } else if(queryArtefact == 2) { // type

         console.log('processNLUCommand(): create type');

         if(nluLastPackageId == -1) {
            alert("Please create a Package first");
         } else {
            var package = objects[nluLastPackageId];
            var nTypes = package.item.types.length;
            var xMult = nTypes;
            var yMult = 0;
            if(nTypes > 2) {
               xMult = nTypes - 3;
               yMult = 1;
            }
            var x = package.x + 50 + 250 * xMult;
            var y = package.y + 50 + yMult * 280;
            rootItem.processFormNew(TYPE, nluLastPackageId, x, y, true)
            nluLastTypeId = selectedObjectId;
            nluSelectedId = selectedObjectId;
         }

      } else if(queryArtefact == 3) { // attribute

         console.log('processNLUCommand(): create attribute');

         if(nluLastTypeId == -1) {
            alert("Please create a Type first");
         } else {
            var panel = objects[objects[nluLastTypeId].id].item.panels[0];
            rootItem.processFormNew(ATTRIBUTE, panel.id, 150, 150, true)
            nluLastAttrbuteId = selectedObjectId;
            nluSelectedId = selectedObjectId;

            if(queryAttributeType != -1) {
               item = objects[nluSelectedId].item;
               item.values[10] = queryAttributeType  // set attribute type
            }

         }

      }

      if(queryArtefact != -1 && nCalled != -1) {

         var item
         if(nluSelectedId == -1) {
            item = rootItem
         } else {
            item = objects[nluSelectedId].item;
         }
   
         item.values[0] = capitalise(name)  // set name
         item.values[1] = capitalise(name2)  // set display name

         if(nluSelectedId == -1) { // need to update the DBRef URL if updating the Application name
            rootItem.dbRefs[0].values[DBREF_URL] = DEFAULT_DBURL1 + capitalise(name2) + DEFAULT_DBURL2;
         }

      }

   }  else if(queryAction == 3) { // edit

      if(queryAttribute != -1) {

         var item
         if(nluSelectedId == -1) {
            item = rootItem
         } else {
            item = objects[nluSelectedId].item;
         }

         if(queryAttribute == 0) { // name
            item.values[0] = capitalise(setToValue)  // set name
            item.values[1] = capitalise(setToValue2)  // set display name
            if(nluSelectedId == -1) { // need to update the DBRef URL if updating the Application name
               rootItem.dbRefs[0].values[DBREF_URL] = DEFAULT_DBURL1 + capitalise(setToValue2) + DEFAULT_DBURL2;
            }
         } else if(queryAttribute == 1) { // attribute type
            item.values[10] = queryAttributeType  // set attribute type
         } else if(queryAttribute == 2) { // reference

            var targetId = rootItem.getItemFromKey(TYPE, setToValue);
            if(targetId != -1) {
               item.references = [];
               var newRef = new Reference(-1, targetId);
               newRef.refedItem = objects[targetId].item;
               item.references.push(newRef);
            }
         }

      }
      
   }  else if(queryAction == 4) { // select

      var key = '';
      for(var n = 1; n < words.length; n++) {
         if(key.length > 0) {
            key += ' ';
         }
         key += words[n];
       }
 
      nluSelectedId = rootItem.getItemFromKey(TYPE, key);
      if(nluSelectedId == -1) {
         nluSelectedId = rootItem.getItemFromKey(PACKAGE, key);
      }

      if(nluSelectedId == -1) {
         alert("Sorry, couldn't find a Type or Package called " + key);
      } else {

         updateSelectedObject(nluSelectedId);

         var shape = objects[nluSelectedId];
         if(shape.type == PACKAGE) {
            nluLastPackageId = nluSelectedId;
         } else if(shape.type == TYPE) {
            nluLastTypeId = nluSelectedId;
         }
   
      }

   }    

   repaint()

   setToValue = '';

}


/**********************************/

function capitalise(str) {

   var ret = "";

   if(str === undefined) {

   } else if(str.length > 0 ) {

      var words = str.split(' ');
      for (var nWord = 0; nWord < words.length; nWord++) {
   
         var word = words[nWord];
   
         if(ret.length > 0) {
            ret += ' ';
         }
         
         ret += word[0].toUpperCase() + word.substring(1);
   
     }

   }

   return ret;

}
          