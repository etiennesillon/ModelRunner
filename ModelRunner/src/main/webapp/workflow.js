/*********************************************************************/
// Constants
/*********************************************************************/

const STEP=0, WFACTION=1, CONTEXT=2, STEPPREREQ=3, WORKFLOWPREREQ=4;

// Shape styles               Step                   Action                   Context                 Step Pre Req            Workflow Pre Req
const WORKFLOW_SHAPE_NAMES          = ["Step",                "Action",                "Context",              "Pre Requisite",        "Pre Requisite"];
const WORKFLOW_SHAPE_WIDTHS         = [200,                   150,                     100,                    -1,                     100];
const WORKFLOW_SHAPE_HEIGHTS        = [50,                    50,                      50,                     -1,                     50];
const WORKFLOW_SHAPE_BORDER_COLOURS = [DEFAULT_BORDER_COLOUR, DEFAULT_BORDER_COLOUR,   DEFAULT_BORDER_COLOUR,  -1,                     DEFAULT_BORDER_COLOUR];
const WORKFLOW_SHAPE_BORDER_WIDTHS  = [DEFAULT_BORDER_WIDTH,  DEFAULT_BORDER_WIDTH,    DEFAULT_BORDER_WIDTH,   -1,                     DEFAULT_BORDER_WIDTH];
const WORKFLOW_SHAPE_BACK_COLOURS   = ["#f4f4f4",             "#e0e0e0",               "#d0d0d0",              -1,                     "#c0c0c0"];
const WORKFLOW_SHAPE_TEXT_COLOURS   = [DEFAULT_TEXT_COLOUR,   DEFAULT_TEXT_COLOUR,     DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR];
const WORKFLOW_SHAPE_FONTS          = [DEFAULT_FONT,          DEFAULT_FONT,            DEFAULT_FONT,           -1,                     DEFAULT_FONT];
const WORKFLOW_SHAPE_RADIUS         = [DEFAULT_SHAPE_RADIUS,  DEFAULT_SHAPE_RADIUS,    LARGE_RADIUS,           -1,                     LARGE_RADIUS];

const WORKFLOW_ROOT_ITEM_FIELDS = [["Name", STRING]];

const WORKFLOW_COMMON_FIELDS = [];

const WORKFLOW_FIELDS = [
   [
      ["Name", STRING,"Name"]
   ],

   [
      ["Name", STRING, "Name"],
      ["Object Formula", STRING, "Object Formula"],
      ["Action Formula", STRING, "Action Formula"]
   ],

   [
      ["Key", STRING, "Key"],
      ["Type", STRING, "Type"],
      ["Formula", STRING, "Formula"]
   ],

   [
      ["Key", SELECT, "Key", KEYSELECT],
      ["Type", STRING, "Type"],
      ["Formula", STRING, "Formula"]
   ],

   [
      ["Key", STRING, "Key"],
      ["Type", STRING, "Type"],
      ["Formula", STRING, "Formula"]
   ],

];

/*********************************************************************/
// Workflows
/*********************************************************************/

function Workflow() {

   objects = [];

   this.id = -1;
   this.values = [""];
   this.context = [];
   this.steps = [];
   this.preReqs = [];

   this.isNew = true;
   this.isSaved = true;

   this.unresolvedPreReqs = [];
   
   /*********************************************************************/

   this.draw = function() {

      log(0, "Workflow.draw()");

      ctx.fillStyle = BACKGROUND_COLOUR;
      drawRoundedRectangle(0, 0, canvas.width, canvas.height, 5, true, true);
   
      for(var nStep=0; nStep < this.steps.length; nStep++) {
         this.steps[nStep].draw();
      }

      for(var nData=0; nData < this.context.length; nData++) {
         this.context[nData].draw();
      }

      for(var nPreReq=0; nPreReq < this.preReqs.length; nPreReq++) {
         this.preReqs[nPreReq].draw();
      }

   };

   /*********************************************************************/

    this.deleteChild = function(childType, id) {

      if(childType == STEP) {
          this.steps = deleteChild(this.steps, id);
      } else if(childType == CONTEXT) {
          this.context = deleteChild(this.context, id);
         } else if(childType == WORKFLOWPREREQ) {
            this.preReqs = deleteChild(this.preReqs, id);
        }

  };
  
/*********************************************************************/

    this.addStep = function(step) {
      this.steps.push(step);
      this.isSaved = false;
   };

   /*********************************************************************/

   this.addContext = function(context) {
      this.context.push(context);
      this.isSaved = false;
   };

   /*********************************************************************/

   this.addPreReq = function(preReq) {
      this.preReqs.push(preReq);
      this.isSaved = false;
   };

   /*********************************************************************/

   this.getChildrenKeys = function() {

      var ret = [];

      for(var nData=0; nData < this.context.length; nData++) {
         ret.push(this.context[nData]);
      }

      for(var nPreReq=0; nPreReq < this.preReqs.length; nPreReq++) {
         ret.push(this.preReqs[nPreReq]);
      }
      return ret;

   };

   /*********************************************************************/

   this.getSaveData = function() {

      var ret = {};

      ret.id = this.id;
      ret.values = this.values;

      ret.steps = [];
      for(var nStep=0; nStep < this.steps.length; nStep++) {
         ret.steps.push(getItemData(this.steps[nStep]));
      }

      ret.contexts = [];
      for(var nData=0; nData < this.context.length; nData++) {
         ret.contexts.push(getItemData(this.context[nData]));
      }

      ret.preReqs = [];
      for(var nPreReq=0; nPreReq < this.preReqs.length; nPreReq++) {
         ret.preReqs.push(getItemData(this.preReqs[nPreReq]));
      }

      return JSON.stringify(ret);
      
   };

   /*********************************************************************/

   this.getContextMenu = function(x, y) {

      var ret = "";

      ret +=   getContextMenuLink("showNewForm(" + STEP + ",-1," + x + "," + y + ");", "New Step");
      ret +=   getContextMenuLink("showNewForm(" + CONTEXT + ",-1," + x + "," + y + ");", "New Context");
      ret +=   getContextMenuLink("showNewForm(" + WORKFLOWPREREQ + ",-1," + x + "," + y + ");", "New PreReq");

      return ret;

   };

   /*********************************************************************/

   this.processFormNew = function(type, parentId, x, y) {

      log(0, "processFormNew()");

      var updateForm = document.forms.WorkflowItemForm;
      const values = getFormDataArray(updateForm);

      var parent = null;
      if(parentId != -1) {
         parent = objects[parentId].item;
      }

      var newItem = null;

      if(type == STEP) {
         newItem = new WorkflowStep(getNextId(), rootItem, values, x, y);
         this.addStep(newItem);
      } else if(type == CONTEXT) {
         newItem = new WorkflowContext(getNextId(), rootItem, values, x, y);
         this.addContext(newItem);
      } else if(type == WFACTION) {
         newItem = parent.addAction(getNextId(), values, 0 ,0);
      } else if(type == STEPPREREQ) {
         newItem = parent.addPreReq(getNextId(), values);
      } else if(type == WORKFLOWPREREQ) {
         newItem = new WorkflowPreReq(getNextId(), rootItem, values, x, y);
         this.addPreReq(newItem);
      }

      if(newItem != null) {
         updateSelectedObject(newItem.obj.id);
      }

      repaint();

   };

   /*********************************************************************/

   this.processFormUpdate = function(id) {

      log(0, "processFormUpdate()");

      var updateForm = document.forms.WorkflowItemForm;
      const values = getFormDataArray(updateForm);
      if(id == -1) {
         rootItem.values = values;
      } else {
         objects[id].item.values = values;
         if(objects[id].type == STEPPREREQ) {
            objects[id].item.updateSource(values[0])
         } else if(objects[id].type == WFACTION) {
            objects[id].item.updateLinks()
         }

      }
      updateSelectedObject(id);
      repaint();

   };

   /*********************************************************************/

   this.checkObjects = function(x, y) {

      var ret = -1;

      for(var nObject=0; ret == -1 && nObject < objects.length; nObject++) {
         var obj = objects[nObject];
         if(obj != null) {
            if(obj.check(x,y)) {
               ret = nObject;
            }
         }
      }
   
      return ret;
   
   };

   /*********************************************************************/

   this.getSelectHTML = function(itemId, nField, field, value) {

      log(0, "Workflow.getDropboxHTML()");

      var ret = "<td><select class=\"dropdown\" name=\"field" + nField + "\" >";

      var type = field[3];
    
      if(type ==  KEYSELECT) {
         var data = rootItem.getChildrenKeys();
         for(var nKey=0; nKey < data.length; nKey++) {
            var key = data[nKey].values[0];
            ret += "<option value='" + key + "'" + (key == value ? "selected='selected'" : "")+ ">" + key + "</option>";
         }
      } else {
         ret += "<option value='(no slection)'" + (value == null ? " selected='selected'" : "")+ ">(no slection)</option>";
      }

      ret += "</select></td></tr>";

      return ret;

   };

   /*********************************************************************/

   this.initValues = function(type, values) {
   };

    /*********************************************************************/

    this.valueChanged = function(id, nField, field) {
      var value = field.value;
      log(0, "valueChanged(): id = " + id + " nField = " + nField + " value = " + value);
   }
          
}

/*********************************************************************/

function getItemData(item) {

   var ret = {};

   ret.id = item.id;
   ret.values = item.values;
   ret.type = item.obj.type;
   ret.x = item.obj.x;
   ret.y = item.obj.y;

   if(item.obj.type == STEP) {

      ret.preReqs = [];
      for(var nPreReq=0; nPreReq < item.preReqs.length; nPreReq++) {
         ret.preReqs.push(getItemData(item.preReqs[nPreReq]));
      }

      ret.actions = [];
      for(var nChild=0; nChild < item.actions.length; nChild++) {
         ret.actions.push(getItemData(item.actions[nChild]));
      }

   }

   return ret;

};

/*********************************************************************/

function loadWorkflow(json) {

   rootItem = new Workflow();

   var data = JSON.parse(json);

   rootItem.id = data.id;
   checkId(data.id);
   rootItem.values[0] = data.values[0];

   for(var nData=0; nData < data.contexts.length; nData++) {
      rootItem.context.push(loadWorkflowContext(data.contexts[nData]));
   }

   for(var nPreReq=0; nPreReq < data.preReqs.length; nPreReq++) {
      rootItem.preReqs.push(loadWorkflowPreReq(data.preReqs[nPreReq]));
   }

   for(var nStep=0; nStep < data.steps.length; nStep++) {
      rootItem.steps.push(loadWorkflowStep(data.steps[nStep]));
   }

   for(var nPreReq=0; nPreReq < rootItem.unresolvedPreReqs.length; nPreReq++) {
      var preReqData = rootItem.unresolvedPreReqs[nPreReq];
      preReqData[0].addPreReq(preReqData[1], preReqData[2]);
   }

   rootItem.isNew = false;
   rootItem.isSaved = true;

}

/*********************************************************************/

function addWorflowContext(name) {

   var ret = null;

   var newContextValues = ["","",""];
   newContextValues[0] = name;
   var ret = new WorkflowContext(getNextId(), rootItem, newContextValues, newItemX, newItemY);
   rootItem.addContext(ret);

   newItemX += 100;
   newItemY += 30;

   return ret;

}

/*********************************************************************/
// Workflow Step
/*********************************************************************/

function WorkflowStep(id, parent, values, x, y) {

   this.id = id;
   this.values = values;
   this.parent = parent;

   this.obj = getShape(id, STEP, this, x, y, -1, -1);

   this.actions = [];
   this.preReqs = [];

   /*********************************************************************/

   this.addAction = function(id, values, x, y) {

      var ret = null;

      if(x == 0 && y == 0) {
         var childCoords = this.getChildCoords(WFACTION);
         if(childCoords.length == 2) {
            x = childCoords[0];
            y = childCoords[1];
         }
      }

      ret = new WorkflowAction(id, this, values, x, y);
      this.actions.push(ret);

      rootItem.isSaved = false;

      return ret;

   };

   /*********************************************************************/

   this.addPreReq = function(id, values) {

      var ret = null;

      var sourceId = getItemFromValue(0, values[0], -1);
      if(sourceId == -1) {
         alert("req not found for " + values[0]);
         var newContext = addWorflowContext(values[0]);
         sourceId = newContext.obj.id;
      }

      if(sourceId != -1) {
         var source = objects[sourceId].item;
         ret = new StepPreReq(id, this, values, source, this);
         this.preReqs.push(ret);
         source.linkedPreReqs.push(ret);
         rootItem.isSaved = false;
      }
            
      return ret;

   };

   /*********************************************************************/

   this.getChildCoords = function(type) {

      var ret = [];

      var children = null;
      if(type == WFACTION) {
         children  = this.actions;
      }
      if(children != null) {
         var nChildren = children.length;
         var width = WORKFLOW_SHAPE_WIDTHS[type] + DEFAULT_CHILD_SPACE;
         var x = this.obj.x + nChildren * width;
         var y = this.obj.y + this.obj.height * 2;   
         ret.push(x);
         ret.push(y);
      }

      return ret;

   };

   /*********************************************************************/

   this.draw = function() {

      for(var nChild=0; nChild < this.actions.length; nChild++) {
         var action = this.actions[nChild];
         action.draw();
         ctx.strokeStyle = DEFAULT_LINK_COLOUR;
         ctx.lineWidth = DEFAULT_LINK_WIDTH;
         drawShapeLink(this.obj, action.obj);
      }

      this.obj.draw();

      for(var nChild=0; nChild < this.preReqs.length; nChild++) {
         this.preReqs[nChild].draw();
      }

   };

    /*********************************************************************/

    this.deleteItem = function(isCheck) {

      if(isCheck) {
          return true;
      } else {
         for(var nChild=0; nChild < this.preReqs.length; nChild++) {
            this.preReqs[nChild].delete(isCheck);
         }
         this.parent.deleteChild(this.obj.type, this.id);
         deleteObject(this.id);
      }

  };
  
   /*********************************************************************/

   this.deleteChild = function(childType, id) {

      if(childType == WFACTION) {
          this.actions = deleteChild(this.actions, id);
      } else if(childType == STEPPREREQ) {
         this.preReqs = deleteChild(this.preReqs, id);
      }

  };
  
   /*********************************************************************/

   this.move = function(dx, dy) {

      this.obj.move(dx, dy);

      for(var nChild=0; nChild < this.actions.length; nChild++) {
         this.actions[nChild].move(dx, dy);
      }

      for(var nChild=0; nChild < this.preReqs.length; nChild++) {
         this.preReqs[nChild].obj.refresh();
      }

      rootItem.isSaved = false;

   };

   /*********************************************************************/

   this.getContextMenu = function(id, x, y) {

      var ret = "";

      ret +=   getContextMenuLink("showNewForm(" + WFACTION + "," + id + "," + x + "," + y + ");", "New Action");
      ret +=   getContextMenuLink("showNewForm(" + STEPPREREQ + ","+ id + "," + x + "," + y + ");", "New PreReq");

      return ret;

   };

}

/*********************************************************************/

function loadWorkflowStep(data) {

   checkId(data.id);

   var ret = new WorkflowStep(data.id, rootItem, data.values, data.x, data.y);

   for(var nPreReq=0; nPreReq < data.preReqs.length; nPreReq++) {
      var preReq = data.preReqs[nPreReq];
      checkId(preReq.id);
      rootItem.unresolvedPreReqs.push([ret, preReq.id, preReq.values]);
   }

   for(var nAction=0; nAction < data.actions.length; nAction++) {
      var action = data.actions[nAction];
      checkId(action.id);
      ret.addAction(action.id, action.values, action.x, action.y);
   }

   return ret;

}

/*********************************************************************/
// Workflow Action
/*********************************************************************/

function WorkflowAction(id, parent, values, x, y) {

   this.id = id;
   this.values = values;
   this.parent = parent;

   this.obj = getShape(id, WFACTION, this, x, y, -1, -1);

   this.inputs = [];
   this.outputs = [];

   /*********************************************************************/

   this.updateLinks2 = function(links, form, term) {

      var ind = 0;
      var looping = true;
      while(looping && (ind = form.indexOf(term, ind)) != -1) {
         var startInd = form.indexOf("'", ind);
         var endInd = form.indexOf("'", startInd + 1);
         if(startInd != -1 && endInd != -1) {
            var key = form.substring(startInd +1, endInd);
            log(0, "key = " + key);
            var id = getItemFromValue(0, key, -1);
            if(id == -1) {
               var newContext = addWorflowContext(key);
               id = newContext.obj.id;
            }
            links.push(objects[id]);
            ind = endInd;

         } else {
            alert("error looking for context in formula = " + form);
            looping = false;
         }

      }
   
   };

   /*********************************************************************/

   this.updateLinks = function() {

      this.inputs = [];
      this.outputs = [];
   
      this.updateLinks2(this.inputs, this.values[1], "{GetWorkflowContextData('");
      this.updateLinks2(this.outputs, this.values[2], "{AddWorkflowContextData('");

   };

   this.updateLinks();

   /*********************************************************************/

   this.draw = function() {
      
      ctx.strokeStyle = DATA_LINK_COLOUR;
      ctx.lineWidth = DATA_LINK_WIDTH;

      for(var n=0; n<this.inputs.length; n++) {
         drawShapeLink(this.inputs[n], this.obj);
      }

      for(var n=0; n<this.outputs.length; n++) {
         log(0,"WorkflowAction: " + this.values[0] + " -> " + this.outputs[n].item.values[0]);
         drawShapeLink(this.obj, this.outputs[n]);
      }

      this.obj.draw();

   };

    /*********************************************************************/

    this.deleteItem = function(isCheck) {

      if(isCheck) {
          return true;
      } else {
          this.parent.deleteChild(this.obj.type, this.id);
          deleteObject(this.id);
      }

  };
  
   /*********************************************************************/

   this.move = function(dx, dy) {
      this.obj.move(dx, dy);
      rootItem.isSaved = false;
   };

   /*********************************************************************/

   this.getContextMenu = function(id, x, y) {
      return "";
   };

}

/*********************************************************************/
// Workflow Context
/*********************************************************************/

function WorkflowContext(id, parent, values, x, y) {

   this.id = id;
   this.values = values;
   this.parent = parent;

   this.obj = getShape(id, CONTEXT, this, x, y, -1, -1);

   this.linkedPreReqs = [];

   /*********************************************************************/

   this.draw = function() {
      this.obj.draw();
   };

    /*********************************************************************/

    this.deleteItem = function(isCheck) {

      if(isCheck) {
          return true;
      } else {
         this.parent.deleteChild(this.obj.type, this.id);
         deleteObject(this.id);
      }

  };
  
   /*********************************************************************/

   this.move = function(dx, dy) {
      this.obj.move(dx, dy);
      for(var nObject=0; nObject < this.linkedPreReqs.length; nObject++) {
         this.linkedPreReqs[nObject].obj.refresh();
      }
      rootItem.isSaved = false;
   };
   
   /*********************************************************************/

   this.getContextMenu = function(id, x, y) {
      return "";
   };

}

/*********************************************************************/

function loadWorkflowContext(data) {
   checkId(data.id);
   var ret = new WorkflowContext(data.id, rootItem, data.values, data.x, data.y);
   return ret;
}

/*********************************************************************/
// Workflow PreReq
/*********************************************************************/

function WorkflowPreReq(id, values, x, y) {

   this.obj = getShape(id, WORKFLOWPREREQ, this, x, y, -1, -1);

   this.id = id;
   this.values = values;
   this.linkedPreReqs = [];

   /*********************************************************************/

   this.draw = function() {
      this.obj.draw();
   };

    /*********************************************************************/

    this.deleteItem = function(isCheck) {

      if(isCheck) {
          return true;
      } else {
         this.parent.deleteChild(this.obj.type, this.id);
         deleteObject(this.id);
      }

  };
  
   /*********************************************************************/

   this.move = function(dx, dy) {
      this.obj.move(dx, dy);
      for(var nObject=0; nObject < this.linkedPreReqs.length; nObject++) {
         this.linkedPreReqs[nObject].obj.refresh();
      }
      rootItem.isSaved = false;
   };
   
   /*********************************************************************/

   this.getContextMenu = function(id, x, y) {
      return "";
   };

}

/*********************************************************************/

function loadWorkflowPreReq(data) {
   checkId(data.id);
   var ret = new WorkflowPreReq(data.id, rootItem, data.values, data.x, data.y);
   return ret;
}

/*********************************************************************/
// Step PreReq
/*********************************************************************/

function StepPreReq(id, parent, values, source, target, x, y) {

   this.id = id;
   this.values = values;
   this.parent = parent;
   this.source = source;
   this.target = target;

   if(source == rootItem) {
      this.obj = getShape(id, WORKF-LOWPREREQ, this, x, y, -1, -1);
   } else {
      this.obj = getLink(id, STEPPREREQ, this);
      this.nLabel = 2;
   }

   /*********************************************************************/

   this.draw = function() {
      this.obj.draw();
   };

    /*********************************************************************/

    this.deleteItem = function(isCheck) {

      if(isCheck) {
          return true;
      } else {
          this.parent.deleteChild(this.obj.type, this.id);
          deleteObject(this.id);
      }

  };
  
 /*********************************************************************/

   this.move = function(dx, dy) {
      // this.obj.move(dx, dy);
   };

    /*********************************************************************/

   this.updateSource = function(key) {
      var sourceId = getItemFromValue(0, key, CONTEXT);
      if(sourceId != -1) {
         this.source.linkedPreReqs = removeElementFromArray(this.source.linkedPreReqs, this);
         this.source = objects[sourceId].item;
         this.source.linkedPreReqs.push(this);
         this.obj.refresh();
         rootItem.isSaved = false;
      }
   };
   
   /*********************************************************************/

   this.getContextMenu = function(id, x, y) {
      return "";
   };

}