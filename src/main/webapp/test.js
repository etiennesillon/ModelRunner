/*********************************************************************/
// Global Variables
/*********************************************************************/

var canvas;
var canvasParent;

var data = [];
var xData = 0, yData = 1;

var xMin = null, xMax = null;
var yMin = null, yMax = null;

var xAxis0 = 100, yAxis0 = 650;
var xAxisLength = 1200, yAxisLength=600
var xAxisUnitNum = 11, yAxisUnitNum = 9;
var xAxisUnit = 100, yAxisUnit = 100;
var xAxisRatio = 100, yAxisRatio = 100;
var xAxisUnitIncr = 100, yAxisUnitIncr = 100;

var xLabels = []
var yLabels = []
var labels = {xLabels, yLabels};

/*********************************************************************/

var dropDownCheckList = null; //document.getElementById('list1');
var dropDownItems = null; //document.getElementById('items');

var editor;

var speechRec = null;
var speechToText = null;
var finalTranscript = "";

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

const BACKGROUND_COLOUR = "#eaf4fa";
const AXIS_COLOUR = "#b0b0b0";

const DEFAULT_BORDER_COLOUR = "#b0b0b0";

const DEFAULT_BACK_COLOUR = "#e1e1ea"; //"#ffe9d3";

const DEFAULT_TEXT_COLOUR = "#666";

const DEFAULT_FONT = "14px sans-serif";

const HIGHLIGHT_BORDER_COLOUR = "#666";
const HIGHLIGHT_BACK_COLOUR = "#ffe9d3";
const HIGHLIGHT_TEXT_COLOUR = "#000";
const HIGHLIGHT_FONT = "bold 14px sans-serif";

const DEFAULT_SHAPE_RADIUS = 3;
const MEDIUM_RADIUS = 15;
const LARGE_RADIUS = 28;

const ERROR_MSG_COULOUR = "#f00";

const LOW_BORDER_WIDTH = 1;
const DEFAULT_BORDER_WIDTH = 2;
const HIGHLIGHT_BORDER_WIDTH = 3;
const SHAPE_RESIZE_BORDER_WIDTH = 4;

const DEFAULT_LINK_WIDTH = 2;
const DEFAULT_LINK_COLOUR = "#999";
const DATA_LINK_WIDTH = 2;
const DATA_LINK_COLOUR = "#999";
const EDITABLE_LINK_COLOUR = "#b0b0b0";
const EDITABLE_LINK_WIDTH = 3;
const MOUSEOVER_LINK_COLOUR = "#999";
const SELECTED_LINK_COLOUR = "#c0c0c0";

/*********************************************************************/
// Initialization
/*********************************************************************/

function init() {
   initCanvas();
   initData();
   resizeCanvas();
}

/*********************************************************************/

function initCanvas() {

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
	
	ctx = canvas.getContext("2d");
 
}
 
  /*********************************************************************/

 function initData() {

	data.push(new Data(['1/1/2020', 100]));
	data.push(new Data(['1/2/2020', 200]));
	data.push(new Data(['1/3/2020', 100]));
	data.push(new Data(['1/4/2020', 300]));
	data.push(new Data(['1/5/2020', 200]));
	data.push(new Data(['1/6/2020', 300]));
	data.push(new Data(['1/7/2020', 500]));
	data.push(new Data(['1/8/2020', 500]));
	data.push(new Data(['1/9/2020', 400]));
	data.push(new Data(['1/10/2020', 300]));
	data.push(new Data(['1/11/2020', 100]));
	data.push(new Data(['1/12/2020', 1000]));

	xMin = null, xMax = null, yMin = null, yMax = null;
	for(var nPoint=0; nPoint < data.length; nPoint++) {

		var x = data[nPoint].values[xData];
		if(xMax == null || x > xMax) {
			xMax = x;
		}
		if(xMin == null || x < xMin) {
			xMin = x;
		}

		var y = data[nPoint].values[yData];
		if(yMax == null || y > yMax) {
			yMax = y;
		}
		if(yMin == null || y < yMin) {
			yMin = y;
		}

	}

	// alert("xMin = " + xMin + "\nxMax = " + xMax);
	// alert("yMin = " + yMin + "\nyMax = " + yMax);

	var xDiff = xMax - xMin;
	var yDiff = yMax - yMin;
	// alert("xDiff = " + xDiff + "\nyDiff = " + yDiff);

	xAxisRatio = xDiff/xAxisLength;
	yAxisRatio = yDiff/yAxisLength;
	// alert("xAxisRatio = " + xAxisRatio + "\nyAxisRatio = " + yAxisRatio);

	xAxisUnit = xAxisLength / xAxisUnitNum;
	yAxisUnit = yAxisLength / yAxisUnitNum;
	// alert("xAxisUnit = " + xAxisUnit + "\nyAxisUnit = " + yAxisUnit);

	xAxisUnitIncr = xAxisUnit * xAxisRatio;
	yAxisUnitIncr = yAxisUnit * yAxisRatio;
	// alert("xAxisUnitIncr = " + xAxisUnitIncr + "\nyAxisUnitIncr = " + yAxisUnitIncr);

}

/*********************************************************************/
// Repaint
/*********************************************************************/

function repaint() {

	ctx.fillStyle = BACKGROUND_COLOUR;
	drawRoundedRectangle(0, 0, canvas.width, canvas.height, 5, true, true);

	drawAxis();
	drawPoints();

}

/*********************************************************************/

function drawAxis() {

	ctx.fillStyle = AXIS_COLOUR;
	drawLine(xAxis0,yAxis0,xAxis0, yAxis0-yAxisLength);
	drawLine(xAxis0,yAxis0,xAxis0+xAxisLength, yAxis0);

	var x = xAxis0;
	var xVal = xMin;
	var on = true;
	for(var nUnit=0; nUnit <= xAxisUnitNum; nUnit++) {
		drawLine(x, yAxis0, x, yAxis0 - 10);
		ctx.fillText(getDateString(xVal), x, yAxis0 + 30 + (on ? 0 : 10));   
		x+= xAxisUnit;
		xVal += xAxisUnitIncr;
		on = !on;
	}

	var y = yAxis0;
	var yVal = yMin;
	for(var nUnit=0; nUnit <= yAxisUnitNum; nUnit++) {
		drawLine(xAxis0, y, xAxis0 + 10, y);
		ctx.fillText(yVal, xAxis0 - 30, y);   
		y-= yAxisUnit;
		yVal += yAxisUnitIncr;
	}

}

/*********************************************************************/

function drawAxisLabel(x, y, label) {

	ctx.save();   
	ctx.translate(x, y);   
	// ctx.rotate(10);   
	ctx.fillText(label, -ctx.measureText(label).width/2, -5);   
	ctx.restore();     

}

/*********************************************************************/

function drawPoints() {

	ctx.fillStyle = AXIS_COLOUR;

	for(var nPoint=0; nPoint < data.length; nPoint++) {
		var x = (data[nPoint].values[xData] - xMin) / xAxisRatio;
		var y = (data[nPoint].values[yData] - yMin) / yAxisRatio;
		drawCircle(xAxis0 + x, yAxis0 - y, 5, true, true);
	}
  
}

/*********************************************************************/

function Data(values) {
	this.values = [];
	this.values.push(getTime(values[0]));
	this.values.push(values[1]);
}
 
/*********************************************************************/

function getTime(dateStr) {

	var parts = dateStr.split('/');
	return new Date(parts[2], parts[1] - 1, parts[0]).getTime(); 

}
 
/*********************************************************************/

function getDateString(time) {
	return new Date(time).toDateString(); 
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
   
   canvasLeft = 0; 
   canvasTop = 0; 

   repaint();

}

/*********************************************************************/

function mouseMoved(e) {

	var x = null;
	var y = null;

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
		} else {
			canvasLeft -= dx;
			canvasTop -= dy;
			log(0,"moved dx=" + dx + " / dy=" + dy);
		}
		repaint();
		mouseDownX = x;
		mouseDownY = y;
	
	} else {

		log(0,"moved x=" + x + " / y=" + y);

		var id  = rootItem.checkObjects(x + canvasLeft, y + canvasTop);
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

   form = new Form(type, null, SHAPE_NAMES[editor][type], "hidePopupPanel(); processFormNew(" + type + "," + parentId + "," + x + "," + y + ");", COMMON_FIELDS[editor].concat(FIELDS[editor][type]));
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

function processFormNew(type, parentId, x, y) {
   rootItem.processFormNew(type, parentId, x, y);
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

      log(0, "Link.draw()");

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

function log(level, msg) {

	if(level < DEBUG) {
	  console.log(msg);
	}
 
 }
 
 