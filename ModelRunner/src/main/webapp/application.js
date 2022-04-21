
/*********************************************************************/
// Constants
/*********************************************************************/

const ISOLDSTYLECOORDS = false;

const STRING=0, BOOLEAN=1, NUM=2, TEXT=3, SELECT=4;
const SIMPLESELECT=0, ATTRSELECT=1, KEYSELECT=2;

const TITLE_FONT = "bold 32px sans-serif";
const SUBTITLE_FONT = "bold 24px sans-serif";

const BACKGROUND_COLOUR = "#eaf4fa";
const NO_WORKFLOW_BACKGROUND_COLOUR = "#ffffff";

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


const PACKAGE=0, TYPE=1, PANEL=2, ATTRIBUTE=3, DBREF=4, DOMAIN=5, DOMAINVALUE=6, ACTION=7, REFERENCE=8;

// Shape styles                           Package                Type                     Panel                   Attribute               DBRef                   Domain                  DomainValue            Action                  Reference
const APPLICATION_SHAPE_NAMES          = ["Package",             "Type",                  "Panel",                "Attribute",            "DatabaseRef",          "Domain",               "DomainValue",         "Action",               "Reference"];
const APPLICATION_SHAPE_WIDTHS         = [800,                   200,                     160,                    140,                    70,                     120,                    100,                   70,                     -1];
const APPLICATION_SHAPE_HEIGHTS        = [600,                   250,                     200,                    25,                     50,                     125,                    25,                    40,                     -1];
const APPLICATION_SHAPE_BORDER_COLOURS = [DEFAULT_BORDER_COLOUR, DEFAULT_BORDER_COLOUR,   DEFAULT_BORDER_COLOUR,  DEFAULT_BORDER_COLOUR,  DEFAULT_BORDER_COLOUR,  DEFAULT_BORDER_COLOUR,  DEFAULT_BORDER_COLOUR, DEFAULT_BORDER_COLOUR,  -1];
const APPLICATION_SHAPE_BORDER_WIDTHS  = [DEFAULT_BORDER_WIDTH,  DEFAULT_BORDER_WIDTH,    DEFAULT_BORDER_WIDTH,   DEFAULT_BORDER_WIDTH,   DEFAULT_BORDER_WIDTH,   DEFAULT_BORDER_WIDTH,   DEFAULT_BORDER_WIDTH,  DEFAULT_BORDER_WIDTH,   -1];
const APPLICATION_SHAPE_BACK_COLOURS   = ["#f4f4f4",             "#e0e0e0",               "#d0d0d0",              "#c0c0c0",              "#f6f6f6",              "#f8f8f8",              "#c0c0c0",             "#f8f8f8",              null];
const APPLICATION_SHAPE_TEXT_COLOURS   = [DEFAULT_TEXT_COLOUR,   DEFAULT_TEXT_COLOUR,     DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR,    DEFAULT_TEXT_COLOUR,   DEFAULT_TEXT_COLOUR,    -1];
const APPLICATION_SHAPE_FONTS          = [DEFAULT_FONT,          DEFAULT_FONT,            DEFAULT_FONT,           DEFAULT_FONT,           DEFAULT_FONT,           DEFAULT_FONT,           DEFAULT_FONT,          DEFAULT_FONT,           -1];
const APPLICATION_SHAPE_RADIUS         = [DEFAULT_SHAPE_RADIUS,  DEFAULT_SHAPE_RADIUS,    DEFAULT_SHAPE_RADIUS,   DEFAULT_SHAPE_RADIUS,   LARGE_RADIUS,           DEFAULT_SHAPE_RADIUS,   DEFAULT_SHAPE_RADIUS,  MEDIUM_RADIUS,          -1];


const APPLICATION_ROOT_ITEM_FIELDS = [["Home Page", STRING, "homePage"],["Help Page", STRING, "helpPage"]];
 
const APPLICATION_COMMON_FIELDS = [
    ["Display Name", STRING, "displayName"],
    ["Name", STRING, "name"],
    ["Is Editable Formula", STRING, "isEnabled"],
    ["Is Enabled Formula", STRING, "isViewable"],
    ["Tree Icon Name", STRING, "treeIcon"],
    ["Panel Icon Name", STRING, "panelIcon"],
    ["Description", STRING, "description"],
    ["Contact Details", STRING, "ContactDetails"],
    ["Max Test Objects", STRING, "maxTestObjects"],
    ["Concepts", STRING, "concepts"]
];

const APPLICATION_FIELDS = [
   
    // Package
   [],

    // Type
    [
      ["Is Top Level Viewable?", BOOLEAN, "isTopLevelViewable"],
      ["Is Top Level Creatable?", BOOLEAN, "isTopLevelCreatable"],
      ["Explorer SQL Filter", STRING, "explorerSQLFilter"],
      ["Explorer SQL OrderBy", STRING, "explorerSQLOrderBy"],
      ["Name Formula", STRING, "NameFormula"],
      ["Is Unique Key?", BOOLEAN, "isUniqueKey"],
      ["Default List Stylesheet", STRING, "defaultListStylesheet"],
      ["Default Details Stylesheet", STRING, "defaultDetailsStylesheet"],
      ["Default List Panel", STRING, "defaultListPanel"],
      ["Default Details Panel", STRING, "defaultDetailsPanel"],
      ["Default Edit Dialog", STRING, "defaultEditDialog"],
      ["Is Data Enabled Formula", STRING, "isDataEnabled"],
      ["Extends System Type", STRING, "extendsSystemType"],

   ],

    // Panel
    [],

    // Attribute
    [
      ["Type", SELECT, "type", ATTRSELECT], 
      ["Description", STRING, "desc"],
      ["Length", NUM, "length"],
      ["Is Mandatory?", BOOLEAN, "isMandatory"],
      ["Is Key?", BOOLEAN, "isKey"],
      ["Is Display List?", BOOLEAN, "isDisplayList"],
      ["Is Display Edit?", BOOLEAN, "isDisplayEdit"],
      ["Is Display Search?", BOOLEAN, "isDisplaySearch"],
      ["Initial Value", STRING, "initialValue"],
      ["Formula", STRING, "formula"],
      ["Reference Formula", STRING, "refFormula"],
      ["Reference List Formula", STRING, "refListFormula"],
      ["Constraint Formula", STRING, "constraintFormula"],
      ["Contraint Message", STRING, "constraintMessage"],
      ["System Reference", STRING, "__Referenced_System_Type__"]
   ],

   // DbRef
   [
    ["Type", STRING, "type"],
    ["Url", STRING, "url"],
    ["Driver", STRING, "driver"],
    ["Debug", STRING, "debug"]
   ],

    // Domain
    [],

   // DomainValue
   [
    ["Key", STRING, "key"]
    ],

   // Action
   [
    ["Is Meta Action?", BOOLEAN, "isMetaAction"],
    ["Is Pre Save?", BOOLEAN, "isPreSave"],
    ["Is Upload?", BOOLEAN, "isUpload"],
    ["Type", SELECT, "type", SIMPLESELECT, "CREATE", "UPDATE", "DELETE", "GUI", "BACKGROUND"],
    ["Formula", STRING, "formula"]
   ],

    // Reference
    [
    ["idRef", STRING, "xmi.idref"]
   ]

];
const ATTR_TYPE_VALUES = ["STRING", "INT", "BOOLEAN", "DATE", "DATETIME", "DURATION", "PHONE", "EMAIL", "PRICE", "TEXT", "BACKREF", "DOMAIN", "REF", "REFBOX", "REFTREE", "AGGREGATION", "COMPOSITION", "EXTENSION"];
const ATTR_TYPES = ["String", "Integer", "Boolean", "Date", "DateTime", "Duration", "Phone", "Email", "Price", "Text",  "Back Ref", "Domain", "Ref", "RefBox", "RefTree", "Aggregation", "Composition", "Extension"];
const ATTR_REFS = [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  1, 2, 0, 0, 0, 0, 0, 0];
const ATTR_REF_TYPES = [TYPE, ATTRIBUTE, DOMAIN];
const ATTR_SIMPLETYPES = 9;

const RECT_RESIZE_WIDTH = 10;   

const DEFAULT_DBNAME = "db";
const DEFAULT_DBTYPE = "MySQL";
const DEFAULT_DBDRIVER = "com.mysql.jdbc.Driver";
const DEFAULT_DBURL1 = "jdbc:mysql://localhost/";
const DEFAULT_DBURL2 = "?useLegacyDatetimeCode=false-=AND=-useTimezone=true-=AND=-serverTimezone=UTC";

const COMMON_DISPLAYNAME=0, COMMON_NAME=1, COMMON_ISENABLED=2, COMMON_ISVIEWABLE=3, COMMON_TREEICON=4, COMMON_PANELICON=5, COMMON_DESC=6, COMMON_CONTACT=7, COMMON_MAXTESTOBJ=8;

const COMMONFIELDSLENGTH = APPLICATION_COMMON_FIELDS.length;

const TYPE_ISTLVIEWABLE=COMMONFIELDSLENGTH, TYPE_ISTLCRESTABLE=COMMONFIELDSLENGTH+1, TYPE_SQLFILTER=COMMONFIELDSLENGTH+2, TYPE_SQLORDERBY=COMMONFIELDSLENGTH+3, TYPE_NAMEFORMULA=COMMONFIELDSLENGTH+4, TYPE_ISUNIQUE=COMMONFIELDSLENGTH+5, TYPE_DEFAULTLISTSTYLESHEET=COMMONFIELDSLENGTH+6, TYPE_DEFAULTDETAILSSTYLESHEET=COMMONFIELDSLENGTH+7, TYPE_DEFAULTEDITDIALOG=COMMONFIELDSLENGTH+8, TYPE_EXTENDSSYSTYPE=COMMONFIELDSLENGTH+9;

const ATTR_TYPE=COMMONFIELDSLENGTH, ATTR_DESC=COMMONFIELDSLENGTH+1, ATTR_LENGTH=COMMONFIELDSLENGTH+2, ATTR_ISMANDATORY=COMMONFIELDSLENGTH+3, ATTR_ISKEY=COMMONFIELDSLENGTH+4, ATTR_ISDISPLAYLIST=COMMONFIELDSLENGTH+5, ATTR_ISDISPLAYEDIT=COMMONFIELDSLENGTH+6, ATTR_ISDISPLAYSEARCH=COMMONFIELDSLENGTH+7, ATTR_INITVALUE=COMMONFIELDSLENGTH+8, ATTR_FORMULA=COMMONFIELDSLENGTH+9, ATTR_REFFORMULA=COMMONFIELDSLENGTH+10, ATTR_REFLISTFORMULA=COMMONFIELDSLENGTH+11, ATTR_CONSTRFORMULA=COMMONFIELDSLENGTH+12, ATTR_CONSTRMSG=COMMONFIELDSLENGTH+13, ATTR_SYSREF=COMMONFIELDSLENGTH+14;

const DBREF_YTPE=COMMONFIELDSLENGTH, DBREF_URL=COMMONFIELDSLENGTH+1, DBREF_DRIVER=COMMONFIELDSLENGTH+2, DBREF_DEBUG=COMMONFIELDSLENGTH+3;

const REF_IDREF = COMMONFIELDSLENGTH;

const DOMAINVALUE_KEY = COMMONFIELDSLENGTH;

/*********************************************************************/
// Application
/*********************************************************************/

function Application(id, values) {

    objects = [];

    this.id = id;
    this.values = values;
    this.packages = [];
 
    this.dbRefs = [];

    this.isNew = true;
    this.isSaved = true;

    this.itemMap = new MyMap();

    this.isShowRefs = true;
 
    /*********************************************************************/
 
    this.draw = function() {
 
    //    log(0, "Application.draw()");
 
       ctx.fillStyle = BACKGROUND_COLOUR;
       drawRoundedRectangle(0, 0, canvas.width, canvas.height, 5, true, true);
    
        for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
            this.dbRefs[nChild].draw();
        }

        for(var nChild=0; nChild < this.packages.length; nChild++) {
            this.packages[nChild].draw();
        }

        for(var nChild=0; nChild < this.packages.length; nChild++) {
            this.packages[nChild].drawChildren();
        }

    };
 
    /*********************************************************************/
 
     this.addChild = function(childType, child) {

        if(childType == PACKAGE) {
            this.packages.push(child);
            this.isSaved = false;
        } else if(childType == DBREF) {
            this.dbRefs.push(child);
            this.isSaved = false;
        }

    };

    /*********************************************************************/

    this.deleteChild = function(childType, id) {

        if(childType == PACKAGE) {
            this.packages = deleteChild(this.packages, id);
        } else if(childType == DBREF) {
            this.dbRefs = deleteChild(this.dbRefs, id);
        }

    };
    
    /*********************************************************************/
 
    this.getChildrenKeys = function(attrType, curAttr) {
 
        var ret = [];

        var childType = ATTR_REFS[attrType];
        for(var nPack=0; nPack < this.packages.length; nPack++) {
            var package = this.packages[nPack];
            if(childType == 2) {
                for(var nDom=0; nDom < package.domains.length; nDom++) {
                    var domain = package.domains[nDom];
                    ret.push(domain);
                }
	            } else {
                for(var nType=0; nType < package.types.length; nType++) {
                    var type = package.types[nType];
                    if(childType == 0) {
                        ret.push(package.types[nType]);
                    } else if(childType == 1) {
                        var attributes = type.getAttributes();
                        for(var nAttr=0; nAttr < attributes.length; nAttr++) {
                            var attr = attributes[nAttr];
                            if(attr != curAttr) {
                                ret.push(attr);
                            }
                        }
                    } else {
    
                    }
                }
            }
        }

        ret.sort(function(a, b){return sortItems(childType, a, b)});

       return ret;
 
    };
 
    /*********************************************************************/

     this.getItemFromKey = function(type, value) {

        // log(0, "getItemFromKey()");
    
        var ret = -1;
    
        for(var nObject=0; ret == -1 && nObject < objects.length; nObject++) {
            var obj = objects[nObject];
            if(obj != null) {
                var item = obj.item;
                if(obj.type == type && item.getKey().equalIgnoreCase(value)) {
                    ret = nObject;
                }
            }
        }
    
        return ret;
    
    }
 
     /*********************************************************************/
 
    this.getContextMenu = function(x, y) {
 
       var ret = "";
 
       ret +=   getContextMenuLink("showNewForm(" + DBREF + ",-1," + x + "," + y + ");", "New Db Ref");
       ret +=   getContextMenuLink("showNewForm(" + PACKAGE + ",-1," + x + "," + y + ");", "New Package");
       if(this.isShowRefs) {
            ret +=   getContextMenuLink("hideRefs(-1);", "Hide References");
        } else {
            ret +=   getContextMenuLink("showRefs(-1);", "Show References");
        }   
 
       return ret;
 
    };
 
    /*********************************************************************/

    this.processFormNew = function(type, parentId, x, y, isNLU) {

        // log(0, "processFormNew()");
    

        var values
        if(isNLU) {
            values = initValues(type)
        } else {
            var updateForm = document.forms.WorkflowItemForm;
            values = this.getFormDataArray(type, updateForm);
        }
    
        var parent = null;
        
        if(parentId != -1) {
            parent = objects[parentId].item;
        }
    
        var newItem = null;
    
        if(parentId == -1) {
            if(type == PACKAGE) {
                newItem = new Package(getNextId(), rootItem, values, x, y, -1, -1);
                this.addChild(type, newItem);
            } else if(type == DBREF) {
                newItem = new DbRef(getNextId(), rootItem, values, x, y, -1, -1);
                this.addChild(type, newItem);
            }
        } else {
            parent = objects[parentId].item;
            var xx = x;
            var yy = y;
            if(x == -1 && y == -1) {
                var childCoords = parent.getChildCoords(TYPE);
                xx = childCoords[0];
                yy = childCoords[1];
            }
            if(type == TYPE) {
                newItem = new Type(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
                childCoords = newItem.getChildCoords(newItem.panels, PANEL);
                var newPanel = new Panel(getNextId(), newItem, initValues(PANEL), childCoords[0], childCoords[1], -1, -1);
                newPanel.values[COMMON_DISPLAYNAME] = "Main";
                newItem.addChild(PANEL, newPanel);
                childCoords = newPanel.getChildCoords(ATTRIBUTE );
                var newAttribute = new Attribute(getNextId(), newPanel, initValues(ATTRIBUTE), childCoords[0], childCoords[1], -1, -1);
                newAttribute.values[COMMON_DISPLAYNAME] = "Name";
                newPanel.addChild(ATTRIBUTE, newAttribute);
            } else if(type == DBREF) {
                newItem = new DbRef(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
            } else if(type == PANEL) {
                newItem = new Panel(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
            } else if(type == DOMAIN) {
                newItem = new Domain(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
            } else if(type == DOMAINVALUE) {
                newItem = new DomainValue(getNextId(), parent, values, -1, -1, -1, -1);
                parent.addChild(type, newItem);
            } else if(type == ACTION) {
                newItem = new Action(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
            } else if(type == ATTRIBUTE) {
                newItem = new Attribute(getNextId(), parent, values, -1, -1, -1, -1);
                newItem.updateReferences()
                parent.addChild(type, newItem);
            } else if(type == DBREF) {
                newItem = new DbRef(getNextId(), parent, values, xx, yy, -1, -1);
                parent.addChild(type, newItem);
            }

        }
    
        if(newItem != null) {
            updateSelectedObject(newItem.obj.id);
        }
    
        repaint();
    
    };
    
    /*********************************************************************/
    
    this.processFormUpdate = function(id) {
    
        // log(0, "processFormUpdate()");

        var item = null;
        var type = -1;
        if(id == -1) {
            item = rootItem;
        } else {
            item = objects[id].item;
            type = objects[id].type;
        }
    
        var updateForm = document.forms.WorkflowItemForm;
        item.values = this.getFormDataArray(type, updateForm);
        if(type == -1) {
            var appName = item.values[COMMON_NAME];
            if(appName.length ==0) {
                appName = rootItem.parseDisplayName(item.values[COMMON_DISPLAYNAME]);
            }
            rootItem.dbRefs[0].values[DBREF_URL] = DEFAULT_DBURL1 + appName + DEFAULT_DBURL2;
        } else if(type == ATTRIBUTE) {
            item.updateReferences()
        }
        updateSelectedObject(id);
        repaint();
    
    };

    /*********************************************************************/

    this.getFormDataArray = function(type, form) {
        var ret = [];
        const formFields = form.elements;  
        for (let i=0; i<formFields.length; i++) {
            if(formFields[i].name.startsWith("field")) {
                if(formFields[i].type == "checkbox") {
                    ret.push(formFields[i].checked);
                } else {
                    var value = formFields[i].value;
                    if(formFields[i].type == "select-one") {
                        if(value > ATTR_SIMPLETYPES) {
                            value = [value];
                            var refFieldName = "REF" + formFields[i].name;
                            var refField = null;
                            var n=0;
                            while((refField = form.elements[refFieldName + "-" + n]) != null) {
                                if(refField.checked) {
                                    value.push(refField.id);
                                }
                                n++;
                            }
                        }
                    }
                    ret.push(value);
                }
            }
        }

        return ret;

    };
 
    /*********************************************************************/

    this.valueChanged = function(id, nField, field) {

        var value = field.value;
        // log(0, "valueChanged(): id = " + id + " nField = " + nField + " value = " + value);

        if(field == COMMON_DISPLAYNAME) {

        }

    }
            
    /*********************************************************************/

    this.parseDisplayName = function(displayName) {

        var ret = "";

        for(var i=0; i< displayName.length;i++) {
            var c = displayName.charAt(i);
            if(c != " ") {
                ret += c;
            }

        }

        return ret;


    }
            
    /*********************************************************************/

    this.checkObjects = function(x, y) {

        var ret = -1;
    
        var map = new MyMap();
    
        for(var nObject=0; ret == -1 && nObject < objects.length; nObject++) {
            var obj = objects[nObject];
            if(obj != null) {
                if(obj.check(x,y)) {
                    map.put(obj.type, obj);
                 }
            }

        }
    
        for(var n=ACTION; ret == -1 && n >= PACKAGE; n--) {
           var objs = map.get(n);
           if(objs != null && objs.length > 0) {
              ret = objs[0].id;
           }
        }
    
        return ret;
    
    };
    
    /*********************************************************************/

    this.getSelectHTML = function(itemId, nField, field, value) {

        var ret = null;

        var type = field[3];

        if(type == ATTRSELECT) {
            var selectedValues = null;
            if(itemId != -1) {
                var attr = objects[itemId].item;
                selectedValues = attr.references
            }
            ret = "<td id=\"select" + nField + "\">" + this.getSelectAttrRefHTML(itemId, nField, type, value, selectedValues) + "</td></tr>";

        } else {
            ret = "<td><select class=\"dropdown\" name=\"field" + nField + "\" >";
            for(var i=4; i<field.length;i++) {
                ret += "<option value='" + field[i] + "'" + (value == field[i] ? " selected" : "")+ ">" + field[i] + "</option>";
            }
            ret += "</select></td></tr>";
        }
    
        return ret;
    
    };
    
    /*********************************************************************/

    this.getSelectAttrRefHTML = function(itemId, nField, type, value, selectedValues) {

        var ret = "<select class=\"dropdown\" onchange=\"rootItem.selectChanged(this, " + itemId + ", " + nField + ", " + type + ");\" name=\"field" + nField + "\" >";

        for(var nKey=0; nKey < ATTR_TYPES.length; nKey++) {
           var key = ATTR_TYPES[nKey];
           ret += "<option value='" + nKey + "'" + (nKey == value ? "selected='selected'" : "")+ ">" + key + "</option>";
        }


        var curAttr = null;
        if(itemId != -1) {
            curAttr = objects[itemId].item;
        }

        ret += "</select>";

        if(value > ATTR_SIMPLETYPES) {
            ret += "<div id=\"list1\" class=\"dropdown-check-list\" tabindex=\"100\"><span class=\"anchor\">Select References</span><ul id=\"items\" class=\"items\">";
            var items = rootItem.getChildrenKeys(value, curAttr);
            for(var nKey=0; nKey < items.length; nKey++) {
                var item = items[nKey];
               var key = item.getKey();
               var checked = "";
               if(selectedValues != null) {
                    for(var i=0; checked == "" && i< selectedValues.length; i++) {
                        var selectedValue = selectedValues[i].refedItem.getKey();
                        if(selectedValue == key) {
                            checked = "checked";
                        }
                    }
                }   
               ret += "<li><input id=\"" + key + "\" name=\"REFfield" + nField + "-" + nKey + "\" " + checked + " type=\"checkbox\" />" + key + "</li>";
            }
            ret += "</ul></div>";
   
        }

    return ret;
    
    };

   /*********************************************************************/

   this.selectChanged = function(field, itemId, nField, type) {
        var td = document.getElementById("select" + nField );
        td.innerHTML = this.getSelectAttrRefHTML(itemId, nField, type, field.value);
        updateRefBox();
    };

    /*********************************************************************/

	this.getSaveData = function() {

        var ret = this.getEntityXMLHeader(-1, this);

        ret += "<DatabaseRef>";
        for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
            ret += this.dbRefs[nChild].getXML();
         }
         ret += "</DatabaseRef>";
 
         if(this.packages.length > 0) {
            ret += "<Packages>";
            for(var nChild=0; nChild < this.packages.length; nChild++) {
                ret += this.packages[nChild].getXML();
             }
             ret += "</Packages>";
        }
  
          ret += this.getEntityXMLFooter(-1);

		return ret;

	};

    /*********************************************************************/

	this.getEntityXMLHeader = function(entityType, item) {

		var ret = "";

        var entityTypeName = null;
        var fields = null;

        if(entityType == -1) {
            entityTypeName = "Application";
            fields = APPLICATION_COMMON_FIELDS.concat(APPLICATION_ROOT_ITEM_FIELDS);
            ret += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        } else {
            entityTypeName = APPLICATION_SHAPE_NAMES[entityType];
            fields = APPLICATION_COMMON_FIELDS.concat(APPLICATION_FIELDS[entityType]);
        }

	    ret += "<" + entityTypeName + " xmi.id=\"" + item.id + "\" ";

        for(var nField=0; nField < fields.length; nField++) {
            var type = fields[nField][1];
            var name = fields[nField][2];
            var value = item.values[nField];
            if(type == BOOLEAN) {
                value = (value ? "true" : "false");
            }

            if(nField == COMMON_NAME && value.length == 0) {
                value = this.parseDisplayName(item.values[COMMON_DISPLAYNAME]);
            }
    
            if(entityType == DOMAINVALUE && nField == DOMAINVALUE_KEY && value.length == 0) {
                value = this.parseDisplayName(item.values[COMMON_DISPLAYNAME]);
            }
    
            ret += " " + name + "=\"" + value + "\"";
        }

        if(entityType != -1) {
            ret += " l.x=\"" + item.obj.x + "\" l.y=\"" + item.obj.y + "\" l.w=\"" + item.obj.width + "\" l.h=\"" + item.obj.height + "\" "; 
        }
        ret += ">\n";
        
		return ret;

	};

    /*********************************************************************/

	this.getEntityXMLFooter = function(entityType) {

        var entityTypeName = null;
        if(entityType == -1) {
            entityTypeName = "Application";
        } else {
            entityTypeName = APPLICATION_SHAPE_NAMES[entityType];
        }

	    return "</" + entityTypeName + ">\n";

	};

    /*********************************************************************/
 
    this.resolveLinks = function() {
        for(var nChild=0; nChild < this.packages.length; nChild++) {
            this.packages[nChild].resolveLinks();
         }
    };

    /*********************************************************************/
 
    this.setShowRefs = function(isShowRefs) {
        this.isShowRefs = isShowRefs;
        for(var nChild=0; nChild < this.packages.length; nChild++) {
            this.packages[nChild].setShowRefs(isShowRefs);
         }
    };

   /*********************************************************************/

    this.initValues = function(type, values) {

        values[COMMON_ISENABLED] = "true";

        const COMMONFIELDSLENGTH = APPLICATION_COMMON_FIELDS.length;
        
        const TYPE_ISTLVIEWABLE=COMMONFIELDSLENGTH, TYPE_ISTLCRESTABLE=COMMONFIELDSLENGTH+1, TYPE_SQLFILTER=COMMONFIELDSLENGTH+2, TYPE_SQLORDERBY=COMMONFIELDSLENGTH+3, TYPE_NAMEFORMULA=COMMONFIELDSLENGTH+4, TYPE_ISUNIQUE=COMMONFIELDSLENGTH+5, TYPE_DEFAULTLISTSTYLESHEET=COMMONFIELDSLENGTH+6, TYPE_DEFAULTDETAILSSTYLESHEET=COMMONFIELDSLENGTH+7, TYPE_DEFAULTEDITDIALOG=COMMONFIELDSLENGTH+8, TYPE_EXTENDSSYSTYPE=COMMONFIELDSLENGTH+9;
        
        const ATTR_TYPE=COMMONFIELDSLENGTH, ATTR_DESC=COMMONFIELDSLENGTH+1, ATTR_LENGTH=COMMONFIELDSLENGTH+2, ATTR_ISMANDATORY=COMMONFIELDSLENGTH+3, ATTR_ISKEY=COMMONFIELDSLENGTH+4, ATTR_ISDISPLAYLIST=COMMONFIELDSLENGTH+5, ATTR_ISDISPLAYEDIT=COMMONFIELDSLENGTH+6, ATTR_ISDISPLAYSEARCH=COMMONFIELDSLENGTH+7, ATTR_INITVALUE=COMMONFIELDSLENGTH+8, ATTR_FORMULA=COMMONFIELDSLENGTH+9, ATTR_REFFORMULA=COMMONFIELDSLENGTH+10, ATTR_SYSREF=COMMONFIELDSLENGTH+11;
        
        const DBREF_YTPE=COMMONFIELDSLENGTH, DBREF_URL=COMMONFIELDSLENGTH+1, DBREF_DRIVER=COMMONFIELDSLENGTH+2, DBREF_DEBUG=COMMONFIELDSLENGTH+3;
        
        const REF_IDREF = COMMONFIELDSLENGTH;

        if(type == TYPE) {
            values[TYPE_ISTLVIEWABLE] = "true";
            values[TYPE_ISTLCRESTABLE] = "true";
        } else if(type == ATTRIBUTE) {
            values[ATTR_ISDISPLAYLIST] = "true";
            values[ATTR_ISDISPLAYEDIT] = "true";
            values[ATTR_ISDISPLAYSEARCH] = "true";
        }

    };

    /*********************************************************************/
 
    this.getKey = function() {
        return this.values[COMMON_DISPLAYNAME];
    };
 
}

/*********************************************************************/

function sortItems(childType, item1, item2) {

    var ret = -1;

    if(childType == 1) {
        var name1 = item1.parent.parent.values[COMMON_DISPLAYNAME] + item1.values[COMMON_DISPLAYNAME];
        var name2 = item2.parent.parent.values[COMMON_DISPLAYNAME] + item2.values[COMMON_DISPLAYNAME];
        if(name1 > name2) {
            ret = 1;
        }
    } else {
        if(item1.values[COMMON_DISPLAYNAME] > item2.values[COMMON_DISPLAYNAME]) {
            ret = 1;
        }
    }

    return ret;
    
};

/*********************************************************************/

function sortAttributes(attr1, attr2) {

    var ret = -1;

    if(attr1.obj.y > attr2.obj.y) {
        ret = 1;
    }

    return ret;
    
};

/*********************************************************************/
// Package
/*********************************************************************/

function Package(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;
 
    this.obj = getShape(id, PACKAGE, this, x, y, w, h);
    this.obj.isResizable = true;
 
    this.types = [];
    this.domains = [];
    this.dbRefs = [];
 
    /*********************************************************************/

    this.addChild = function(childType, child) {

        var children = null;
        if(childType == TYPE) {
           children  = this.types;
        } else if(childType == DOMAIN) {
            children  = this.domains;
        } else if(childType == DBREF) {
            children  = this.dbRefs;
        }

        if(children != null) {
            if(child.obj.x == -1 && child.obj.y == -1) {
                var childCoords = this.getChildCoords(children, childType);
                if(childCoords.length == 2) {
                    child.obj.x = childCoords[0];
                    child.obj.y = childCoords[1];
                }
            }
        
            children.push(child);
            rootItem.isSaved = false;
        } 

    };
    
    /*********************************************************************/

    this.deleteItem = function(isCheck) {

        if(isCheck) {
            var ret = true;
            for(var nChild=0; ret && nChild < this.types.length; nChild++) {
                if(!this.types[nChild].deleteItem(isCheck)) {
                    ret = false;
                }
            }
            for(var nChild=0; ret && nChild < this.domains.length; nChild++) {
                if(!this.domains[nChild].deleteItem(isCheck)) {
                    ret = false;
                }
            }
            return ret;
        } else {
            for(var nChild=0; nChild < this.types.length; nChild++) {
                this.types[nChild].deleteItem(isCheck);
            }
            for(var nChild=0; nChild < this.domains.length; nChild++) {
                this.domains[nChild].deleteItem(isCheck);
            }
            this.parent.deleteChild(this.obj.type, this.id);
            deleteObject(this.id);
        }

    };

    /*********************************************************************/

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Package(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        for(var nChild=0; nChild < this.domains.length; nChild++) {
            ret.addChild(DOMAIN, this.domains[nChild].clone(ret, dx, dy));
        }
        for(var nChild=0; nChild < this.types.length; nChild++) {
            ret.addChild(TYPE, this.types[nChild].clone(ret, dx, dy));
        }
        for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
            ret.addChild(DBREF, this.dbRefs[nChild].clone(ret, dx, dy));
         }

        return ret;

    };

    /*********************************************************************/

    this.deleteItem = function() {

        for(var nChild=0; nChild < this.types.length; nChild++) {
            this.types[nChild].deleteItem();
        }
   
        for(var nChild=0; nChild < this.domains.length; nChild++) {
            this.domains[nChild].deleteItem();
        }

        this.parent.deleteChild(this.obj.type, this.id);

        deleteObject(this.id);

    };
    
    /*********************************************************************/

    this.deleteChild = function(childType, id) {

        if(childType == TYPE) {
            this.types = deleteChild(this.types, id);
        } else if(childType == DOMAIN) {
            this.domains = deleteChild(this.domains, id);
        } else if(childType == DBREF) {
            this.dbRefs = deleteChild(this.dbRefs, id);
        }

    };
    
    /*********************************************************************/
 
    this.getChildCoords = function(children, type) {
 
       var ret = [];
 
        var nChildren = children.length;
        var width = APPLICATION_SHAPE_WIDTHS[type] + 10;
        var x = this.obj.x + 10 + nChildren * width;
        var y = this.obj.y + 20;   
        ret.push(x);
        ret.push(y);
 
       return ret;
 
    };
 
    /*********************************************************************/
 
    this.draw = function() {
        this.obj.draw();
    };
 
    /*********************************************************************/
 
    this.drawChildren = function() {
 
        for(var nChild=0; nChild < this.types.length; nChild++) {
            this.types[nChild].draw();
         }
   
         for(var nChild=0; nChild < this.domains.length; nChild++) {
            this.domains[nChild].draw();
         }
   
         for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
            this.dbRefs[nChild].draw();
         }
   
    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
 
       this.obj.move(dx, dy);

       if(this.obj.resizePoint != 1) {
            for(var nChild=0; nChild < this.domains.length; nChild++) {
                this.domains[nChild].move(dx, dy);
            }
            for(var nChild=0; nChild < this.types.length; nChild++) {
                this.types[nChild].move(dx, dy);
            }
            for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
                this.dbRefs[nChild].move(dx, dy);
             }
        }           
 
       rootItem.isSaved = false;
 
    };
 
    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
 
       var ret = "";

       ret +=   getContextMenuLink("showNewForm(" + DBREF + "," + id + "," + x + "," + y + ");", "New Db Ref");
       ret +=   getContextMenuLink("showNewForm(" + TYPE + "," + id + "," + x + "," + y + ");", "New Type");
       ret +=   getContextMenuLink("showNewForm(" + DOMAIN + "," + id + "," + x + "," + y + ");", "New Domain");
 
       return ret;
 
    };
 
    /*********************************************************************/
 
    this.resolveLinks = function() {
        for(var nChild=0; nChild < this.types.length; nChild++) {
            this.types[nChild].resolveLinks();
         }
    };

    /*********************************************************************/
 
    this.setShowRefs = function(isShowRefs) {
        for(var nChild=0; nChild < this.types.length; nChild++) {
            this.types[nChild].setShowRefs(isShowRefs);
         }
    };

    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(PACKAGE, this);

        if(this.types.length > 0) {
            ret += "<Types>";
            for(var nChild=0; nChild < this.types.length; nChild++) {
                ret += this.types[nChild].getXML();
             }
             ret += "</Types>";
        }

        if(this.domains.length > 0) {
            ret += "<Domains>";
            for(var nChild=0; nChild < this.domains.length; nChild++) {
                ret += this.domains[nChild].getXML();
             }
             ret += "</Domains>";
        }

        if(this.dbRefs.length > 0) {
            ret += "<DatabaseRef>";
            for(var nChild=0; nChild < this.dbRefs.length; nChild++) {
                ret += this.dbRefs[nChild].getXML();
            }
            ret += "</DatabaseRef>";
        }
         ret += rootItem.getEntityXMLFooter(PACKAGE);

         return ret;
         
	};

    /*********************************************************************/
 
    this.getKey = function() {
        return this.values[COMMON_NAME];
    };
 
}
 
/*********************************************************************/
// Type
/*********************************************************************/

function Type(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;

    this.obj = getShape(id, TYPE, this, x, y, w, h);
    this.obj.isResizable = true;
 
    this.panels = [];
    this.actions = [];

    /*********************************************************************/
 
    this.getKey = function() {
        return this.values[COMMON_DISPLAYNAME];
    };
 
    /*********************************************************************/

    this.addChild = function(childType, child) {

        var children = null;
        if(childType == PANEL) {
           children  = this.panels;
        } else if(childType == ACTION) {
            children  = this.actions;
        }

        if(children != null) {
            if(child.obj.x == -1 && child.obj.y == -1) {
                var childCoords = this.getChildCoords(children, childType);
                if(childCoords.length == 2) {
                    child.obj.x = childCoords[0];
                    child.obj.y = childCoords[1];
                }
            }
        
            children.push(child);
            rootItem.isSaved = false;
        } 

    };
    
    /*********************************************************************/

    this.deleteItem = function(isCheck) {

        if(isCheck) {
            for(var nChild=0; nChild < this.panels.length; nChild++) {
                var panel = this.panels[nChild].deleteItem(isCheck);
             }
             for(var nChild=0; nChild < this.actions.length; nChild++) {
                var action = this.actions[nChild].deleteItem(isCheck);
             }
    
        } else {
            for(var nChild=0; nChild < this.panels.length; nChild++) {
                var panel = this.panels[nChild].deleteItem(isCheck);
             }
             for(var nChild=0; nChild < this.actions.length; nChild++) {
                var action = this.actions[nChild].deleteItem(isCheck);
             }
            this.parent.deleteChild(this.obj.type, this.id);
            deleteObject(this.id);
        }

    };

    /*********************************************************************/

    this.deleteItem = function() {

        for(var nChild=0; nChild < this.panels.length; nChild++) {
            var panel = this.panels[nChild].deleteItem();
         }
   
         for(var nChild=0; nChild < this.actions.length; nChild++) {
            var action = this.actions[nChild].deleteItem();
         }

        this.parent.deleteChild(this.obj.type, this.id);

        deleteObject(this.id);

    };
    
    /*********************************************************************/

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Type(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        for(var nChild=0; nChild < this.panels.length; nChild++) {
            ret.addChild(PANEL, this.panels[nChild].clone(ret, dx, dy));
        }
   
        for(var nChild=0; nChild < this.actions.length; nChild++) {
            ret.addChild(ACTION, this.actions[nChild].clone(ret, dx, dy));
        }

        return ret;

    };

    /*********************************************************************/

    this.deleteChild = function(childType, id) {

        if(childType == PANEL) {
            this.panels = deleteChild(this.panels, id);
        } else if(childType == ACTION) {
            this.actions = deleteChild(this.actions, id);
        }

    };
    
    /*********************************************************************/
 
    this.getChildCoords = function(children, type) {
 
       var ret = [];
 
        var nChildren = children.length;
        var width = APPLICATION_SHAPE_WIDTHS[type] + 10;
        var x = this.obj.x + 10 + nChildren * width;
        var y = this.obj.y + 30;   
        ret.push(x);
        ret.push(y);
 
       return ret;
 
    };
 
    /*********************************************************************/
 
    this.draw = function() {
 
        this.obj.draw();
 
        for(var nChild=0; nChild < this.panels.length; nChild++) {
            var panel = this.panels[nChild];
            panel.draw();
         }
   
         for(var nChild=0; nChild < this.actions.length; nChild++) {
            var action = this.actions[nChild];
            action.draw();
         }
   
    };
 
    /*********************************************************************/
 
    this.getAttributes = function() {
 
        var ret = [];
        
        for(var nChild=0; nChild < this.panels.length; nChild++) {
            ret = ret.concat(this.panels[nChild].attributes);
        }
   
        return ret;

    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
 
       this.obj.move(dx, dy);
 
       if(this.obj.resizePoint != 1) {
           for(var nChild=0; nChild < this.panels.length; nChild++) {
               this.panels[nChild].move(dx, dy);
            }
        }

        if(this.obj.resizePoint != 1) {
            for(var nChild=0; nChild < this.actions.length; nChild++) {
                this.actions[nChild].move(dx, dy);
             }
         }
 
        rootItem.isSaved = false;
 
    };
 
    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
 
       var ret = "";
 
       ret +=   getContextMenuLink("showNewForm(" + PANEL + "," + id + "," + x + "," + y + ");", "New Panel");
       ret +=   getContextMenuLink("showNewForm(" + ACTION + "," + id + "," + x + "," + y + ");", "New Action");

       return ret;
 
    };
 
    /*********************************************************************/
 
    this.resolveLinks = function() {
        for(var nChild=0; nChild < this.panels.length; nChild++) {
            this.panels[nChild].resolveLinks();
         }
    };

    /*********************************************************************/
 
    this.setShowRefs = function(isShowRefs) {
        for(var nChild=0; nChild < this.panels.length; nChild++) {
            this.panels[nChild].setShowRefs(isShowRefs);
         }
    };

    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(TYPE, this);

        if(this.panels.length > 0) {
            ret += "<Panels>";
            for(var nChild=0; nChild < this.panels.length; nChild++) {
                ret += this.panels[nChild].getXML();
             }
             ret += "</Panels>";
        }

        if(this.actions.length > 0) {
            ret += "<Actions>";
            for(var nChild=0; nChild < this.actions.length; nChild++) {
                ret += this.actions[nChild].getXML();
             }
             ret += "</Actions>";
        }

        ret += rootItem.getEntityXMLFooter(TYPE);

         return ret;
         
	};

}
 
 /*********************************************************************/
// Panel
/*********************************************************************/

function Panel(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;
 
    this.obj = getShape(id, PANEL, this, x, y, w, h);
    this.obj.isResizable = true;

    this.attributes = [];
 
    /*********************************************************************/
 
    this.draw = function() {
 
        this.obj.draw();
 
        for(var nChild=0; nChild < this.attributes.length; nChild++) {
            var attr = this.attributes[nChild];
            attr.draw();
         }
   
    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
 
       this.obj.move(dx, dy);
 
       if(this.obj.resizePoint != 1) {
            for(var nChild=0; nChild < this.attributes.length; nChild++) {
                this.attributes[nChild].move(dx, dy);
            }
        }

       rootItem.isSaved = false;
 
    };
 
    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
 
       var ret = "";
 
       ret +=   getContextMenuLink("showNewForm(" + ATTRIBUTE + "," + id + "," + x + "," + y + ");", "New Attribute");
 
       return ret;
 
    };
 
    /*********************************************************************/

    this.addChild = function(childType, child) {
        if(childType == ATTRIBUTE) {
            if(child.obj.x == -1 && child.obj.y == -1) {
                var childCoords = this.getChildCoords(childType);
                child.obj.moveTo(childCoords[0],childCoords[1]);
            }
            this.attributes.push(child);
            rootItem.isSaved = false;
        }
    };
    
    /*********************************************************************/

    this.deleteItem = function() {

   
        this.parent.deleteChild(this.obj.type, this.id);

        deleteObject(this.id);

    };
    
    /*********************************************************************/

    this.deleteItem = function(isCheck) {

        if(isCheck) {
            for(var nChild=0; nChild < this.attributes.length; nChild++) {
                var panel = this.attributes[nChild].deleteItem(isCheck);
             }
        } else {
            for(var nChild=0; nChild < this.attributes.length; nChild++) {
                var panel = this.attributes[nChild].deleteItem(isCheck);
            }
            this.parent.deleteChild(this.obj.type, this.id);
            deleteObject(this.id);
        }

    };

    /*********************************************************************/

    this.deleteChild = function(childType, id) {

        if(childType == ATTRIBUTE) {
            this.attributes = deleteChild(this.attributes, id);
        }

    };
    
    /*********************************************************************/

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Panel(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        for(var nChild=0; nChild < this.attributes.length; nChild++) {
            ret.addChild(ATTRIBUTE, this.attributes[nChild].clone(ret, dx, dy));
        }

        return ret;

    };

     /*********************************************************************/
  
     this.getChildCoords = function(type) {
  
        var ret = [];
  
        if(type == ATTRIBUTE) {
           var nChildren = this.attributes.length;
           var height = APPLICATION_SHAPE_HEIGHTS[type] + 5;
           var x = this.obj.x + 10;   
           var y = this.obj.y + 30 + nChildren * height;
            ret.push(x);
           ret.push(y);
        }
  
        return ret;
  
     };
  
    /*********************************************************************/
 
    this.resolveLinks = function() {
        for(var nChild=0; nChild < this.attributes.length; nChild++) {
            this.attributes[nChild].resolveLinks();
         }
    };

    /*********************************************************************/
 
    this.setShowRefs = function(isShowRefs) {
        for(var nChild=0; nChild < this.attributes.length; nChild++) {
            this.attributes[nChild].setShowRefs(isShowRefs);
         }
    };

    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(PANEL, this);

        this.attributes.sort(function(a, b){return sortAttributes(a, b)});

        if(this.attributes.length > 0) {
            ret += "<Attributes>";
            for(var nChild=0; nChild < this.attributes.length; nChild++) {
                ret += this.attributes[nChild].getXML();
             }
             ret += "</Attributes>";
        }
                    
 
         ret += rootItem.getEntityXMLFooter(PANEL);

         return ret;
         
	};

    /*********************************************************************/
 
    this.getKey = function() {
        return this.values[COMMON_NAME];
    };
 
}
  
 /*********************************************************************/
// Attribute
/*********************************************************************/

function Attribute(id, parent, values, x, y, w, h) {

    this.id = id;
    this.parent = parent;
    this.values = values;

    if(h < APPLICATION_SHAPE_HEIGHTS[ATTRIBUTE]) {
        h = APPLICATION_SHAPE_HEIGHTS[ATTRIBUTE];
    }
    this.obj = getShape(id, ATTRIBUTE, this, x, y, w, h);
 
    this.references = [];

    this.isShowRefs = true;
 
    /*********************************************************************/
 
    this.getKey = function() {
        return this.parent.parent.values[COMMON_DISPLAYNAME] + "." + this.values[COMMON_DISPLAYNAME];
    };
 
    /*********************************************************************/
 
    this.draw = function() {
 
        if(this.isShowRefs) {
            for(var nChild=0; nChild < this.references.length; nChild++) {
                var refedItem = this.references[nChild].refedItem;
                if(refedItem != null && refedItem.obj != null) {
                    ctx.strokeStyle = DEFAULT_LINK_COLOUR;
                    ctx.lineWidth = DEFAULT_LINK_WIDTH;
                    drawShapeLink(this.obj, refedItem.obj);
                }
            }
            this.obj.specialBorderColour = null;
        } else if(this.values[ATTR_TYPE] > ATTR_SIMPLETYPES) {
            this.obj.specialBorderColour = "#fff";
        }

        this.obj.draw();

    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
 
       this.obj.move(dx, dy);
       rootItem.isSaved = false;
 
    };
 
    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {

        ret = "";

        if(this.values[ATTR_TYPE] > ATTR_SIMPLETYPES) {
            if(this.isShowRefs) {
                ret +=   getContextMenuLink("hideRefs(" + id + ");", "Hide References");
            } else {
                ret +=   getContextMenuLink("showRefs(" + id + ");", "Show References");
            }   
        }

        return ret;
    };
 
    /*********************************************************************/

    this.addChild = function(childType, child) {
        if(childType == REFERENCE) {
            this.references.push(child);
            rootItem.isSaved = false;
        }
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

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Attribute(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        for(var nChild=0; nChild < this.references.length; nChild++) {
            ret.addChild(REFERENCE, this.references[nChild].clone());
        }

        return ret;

    };

    /*********************************************************************/

    this.resolveLinks = function() {
        for(var nChild=0; nChild < this.references.length; nChild++) {
            this.references[nChild].resolveLinks();
        }
    };

    /*********************************************************************/
 
    this.setShowRefs = function(isShowRefs) {
        this.isShowRefs = isShowRefs;
    };

    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(ATTRIBUTE, this);

        if(this.references.length > 0) {
            ret += "<References>";
            for(var nChild=0; nChild < this.references.length; nChild++) {
                ret += this.references[nChild].getXML();
             }
             ret += "</References>";
        }
 
         ret += rootItem.getEntityXMLFooter(ATTRIBUTE);

         return ret;

	};

    /*********************************************************************/

    this.updateReferences = function() {
        var attrType = this.values[REF_IDREF][0];
        if(attrType > ATTR_SIMPLETYPES) {
            var refType = ATTR_REF_TYPES[ATTR_REFS[attrType]];
            this.references = [];
            for(var i=1; i < this.values[REF_IDREF].length; i++) {
                var key = this.values[REF_IDREF][i];
                var sourceId = rootItem.getItemFromKey(refType, key);
                if(sourceId != -1) {
                    var newRef = new Reference(-1, sourceId);
                    newRef.refedItem = objects[sourceId].item;
                   this.references.push(newRef);
                }
            }
        }
        this.values[REF_IDREF] = attrType;
    };
     
  }
  
 /*********************************************************************/
// Action
/*********************************************************************/

function Action(id, parent, values, x, y, w, h) {

    this.id = id;
    this.parent = parent;
    this.values = values;
 
    this.obj = getShape(id, ACTION, this, x, y, w, h);
 
    /*********************************************************************/
 
    this.draw = function() {
        this.obj.draw();
    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
       this.obj.move(dx, dy);
       rootItem.isSaved = false;
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

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Action(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        return ret;

    };

    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
        return "";
    };
 
    /*********************************************************************/

	this.getXML = function() {

        var ret = rootItem.getEntityXMLHeader(ACTION, this);

        ret += rootItem.getEntityXMLFooter(ACTION);

        return ret;

	};

    /*********************************************************************/
 
      this.getKey = function() {
        return this.values[COMMON_NAME];
    };
 
}
  
 /*********************************************************************/
// DbRef
/*********************************************************************/

function DbRef(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;
 
    this.obj = getShape(id, DBREF, this, x, y, w, h);
 
    /*********************************************************************/
 
    this.draw = function() {
        this.obj.draw();
    };
 
    /*********************************************************************/
 
    this.move = function(dx, dy) {
       this.obj.move(dx, dy);
       rootItem.isSaved = false;
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

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new DbRef(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        return ret;

    };

    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
        return "";
    };
 
    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(DBREF, this);

        ret += rootItem.getEntityXMLFooter(DBREF);

        return ret;

	};

    /*********************************************************************/
 
      this.getKey = function() {
        return this.values[DBREF_URL];
    };
 
}
  
 /*********************************************************************/
// Domain
/*********************************************************************/

function Domain(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;
 
    this.obj = getShape(id, DOMAIN, this, x, y, w, h);
    this.obj.isResizable = true;

    this.domainValues = [];
 
    /*********************************************************************/
 
    this.draw = function() {
 
        this.obj.draw();
 
        for(var nChild=0; nChild < this.domainValues.length; nChild++) {
            this.domainValues[nChild].draw();
         }
   
    };
 
    /*********************************************************************/

    this.deleteItem = function(isCheck) {

        if(isCheck) {
            var ret = true;
            return ret;
        } else {
            for(var nChild=0; nChild < this.domainValues.length; nChild++) {
                this.domainValues[nChild].deleteItem(isCheck);
            }
            this.parent.deleteChild(this.obj.type, this.id);
            deleteObject(this.id);
        }

    };

    /*********************************************************************/

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new Domain(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        for(var nChild=0; nChild < this.domainValues.length; nChild++) {
            ret.addChild(DOMAINVALUE, this.domainValues[nChild].clone(ret, dx, dy));
        }

        return ret;

    };

    /*********************************************************************/
 
    this.move = function(dx, dy) {
 
       this.obj.move(dx, dy);
 
       if(this.obj.resizePoint != 1) {
            for(var nChild=0; nChild < this.domainValues.length; nChild++) {
                this.domainValues[nChild].move(dx, dy);
            }
        }

       rootItem.isSaved = false;
 
    };
 
    /*********************************************************************/
 
    this.getContextMenu = function(id, x, y) {
 
       var ret = "";
 
       ret +=   getContextMenuLink("showNewForm(" + DOMAINVALUE + "," + id + "," + x + "," + y + ");", "New Value");
 
       return ret;
 
    };
 
    /*********************************************************************/

    this.addChild = function(childType, child) {
        if(childType == DOMAINVALUE) {
            if(child.obj.x == -1 && child.obj.y == -1) {
                var childCoords = this.getChildCoords(childType);
                child.obj.moveTo(childCoords[0],childCoords[1]);
            }
            this.domainValues.push(child);
            rootItem.isSaved = false;
        }
    };
    
    /*********************************************************************/

    this.deleteChild = function(childType, id) {

        if(childType == DOMAINVALUE) {
            this.attributes = domainValues(this.attributes, id);
        }

    };
    
     /*********************************************************************/
  
     this.getChildCoords = function(type) {
  
        var ret = [];
  
        if(type == DOMAINVALUE) {
           var nChildren = this.domainValues.length;
           var height = APPLICATION_SHAPE_HEIGHTS[type] + 5;
           var x = this.obj.x + 10;   
           var y = this.obj.y + 30 + nChildren * height;
            ret.push(x);
           ret.push(y);
        }
  
        return ret;
  
     };
  
    /*********************************************************************/

	this.getXML = function() {

        if(this.id == -1) {
            this.id = getNextId();
        }

        var ret = rootItem.getEntityXMLHeader(DOMAIN, this);

        if(this.domainValues.length > 0) {
            ret += "<DomainValues>";
            for(var nChild=0; nChild < this.domainValues.length; nChild++) {
                ret += this.domainValues[nChild].getXML();
             }
             ret += "</DomainValues>";
        }
                    
         ret += rootItem.getEntityXMLFooter(DOMAIN);

         return ret;
         
	};

    /*********************************************************************/
 
    this.getKey = function() {
        return this.values[COMMON_DISPLAYNAME];
    };
 
}
  
 /*********************************************************************/
// DomainValue
/*********************************************************************/

function DomainValue(id, parent, values, x, y, w, h) {

    this.id = id;
    this.values = values;
    this.parent = parent;
 
    if(h < APPLICATION_SHAPE_HEIGHTS[DOMAINVALUE]) {
        h = APPLICATION_SHAPE_HEIGHTS[DOMAINVALUE];
    }

    this.obj = getShape(id, DOMAINVALUE, this, x, y, w, h);
 
    /*********************************************************************/
 
    this.draw = function() {
        this.obj.draw();
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

    this.clone = function(parent, dx, dy) {

        xx = this.obj.x + dx;
        yy = this.obj.y + dy;

        var newVals = JSON.parse(JSON.stringify(this.values));
        var ret = new DomainValue(getNextId(), parent, newVals, xx, yy, this.obj.width, this.obj.height);

        return ret;

    };

    /*********************************************************************/

	this.getXML = function() {

        var ret = rootItem.getEntityXMLHeader(DOMAINVALUE, this);
        ret += rootItem.getEntityXMLFooter(DOMAINVALUE);

        return ret;

	};

  }
  
/*********************************************************************/
// Reference
/*********************************************************************/

function Reference(id, idRef) {

    this.id = id;
    this.idRef = idRef;
    this.refedItem = null;
 
    /*********************************************************************/

    this.resolveLinks = function() {
        var items = rootItem.itemMap.get(this.idRef);
        if(items != null && items.length > 0) {
            this.refedItem = items[0];
        }
    };
 
    /*********************************************************************/

	this.getXML = function() {

        return "<Reference xmi.id=\"" + this.id + "\" xmi.idref=\"" + this.idRef+ "\"></Reference>";

	};

    /*********************************************************************/

    this.clone = function() {
        var ret = new Reference(getNextId(), this.idRef);
        ret.refedItem = this.refedItem;
        return ret;
    };

 }
  
/*********************************************************************/
// XML
/*********************************************************************/

function getModelObjectFromXMLNode(node, parent) {

    var ret = null;
    
    // log(0, "-------------------------------------------------------------------------------------");
    // log(0, "Item type = " + node.nodeName);
	
	if(node.nodeName != "#text" && node.attributes != null) {

        var typeIndex = getTypeIndex(node.nodeName); 
        // log(0, "Item typeIndex = " + typeIndex);
        if(typeIndex != -99) {

            var map = new MyMap();
            for(var nAttr = 0; nAttr < node.attributes.length; nAttr++) {
                
                var attr = node.attributes[nAttr];
                var attrName = attr.name;
                var attrValue = attr.value;
                // log(0, "From Node     " + nAttr + " : " + attrName + "=" + attrValue);
                map.put(attrName, attrValue);
            }
    
            var values = [];
    
            var fields = null;
            if(typeIndex == -1) {
                fields = APPLICATION_COMMON_FIELDS.concat(APPLICATION_ROOT_ITEM_FIELDS);
            } else {
                fields = APPLICATION_COMMON_FIELDS.concat(APPLICATION_FIELDS[typeIndex]);
            }
    
            for(var nField=0; nField < fields.length; nField++) {
                var type = fields[nField][1];
                var name = fields[nField][2];
                var value = map.get(name);
                if(value == null) {
                    value = "";
                } else {
                    value = value[0];
                }
                if(type == BOOLEAN) {
                    value = value == "true";
                } else if(type ==  NUM) {
                    if(value.length > 0) {
                        value = parseInt(value);
                    }
                } else if(type ==  SELECT) {
                    var found = false;
                    for(var nType = 0; !found && nType < ATTR_TYPE_VALUES.length; nType++) {
                        if(value == ATTR_TYPE_VALUES[nType]) {
                            found = true;
                            value = nType;
                        }
                    }
                }
                values.push(value);
                // log(0, "From Map     " + nField + " : " + name + "=" + value);
            }
            
            var id = parseInt(map.get("xmi.id"));
            checkId(id);
    
            if(typeIndex == -1) { 
                ret = new Application(id, values);
                rootItem = ret;
            } else {
                // if(!values[3]) { ///////////// CHECKIT
                    var xs = map.get("l.x");
                    var x = parseInt(xs);
                    var y = parseInt(map.get("l.y"));
                    var w = parseInt(map.get("l.w"));
                    var h = parseInt(map.get("l.h"));
                    if(ISOLDSTYLECOORDS && parent != null && parent != rootItem) {
                        x += parent.obj.x;
                        y += parent.obj.y;
                    }
                    ret = getNewApplicationItem(typeIndex, id, parent, values, x, y, w, h);
                    if(ret != null) {
                        parent.addChild(typeIndex, ret);
                    }
                // }
            }
    
            if(ret != null) {
                rootItem.itemMap.put(id, ret);
                for(var nChild = 0; nChild < node.childNodes.length; nChild++) {
                    var childNode = node.childNodes[nChild];
                    for(var nGrandChild = 0; nGrandChild < childNode.childNodes.length; nGrandChild++) {
                        getModelObjectFromXMLNode(childNode.childNodes[nGrandChild], ret);
                    }
                }
            }		
    
        }
	}
	
	return ret;
   
}

/*********************************************************************/

function getNewApplicationItem(type, id, parent, values, x, y, w, h) {

    var ret = null;

    if(type == PACKAGE) {
        ret = new Package(id, parent, values, x, y, w, h);
    } else if(type == TYPE) {
        ret = new Type(id, parent, values, x, y, w, h);
    } else if(type == PANEL) {
        ret = new Panel(id, parent, values, x, y, w, h);
    } else if(type == ATTRIBUTE) {
        ret = new Attribute(id, parent, values, x, y, w, h);
    } else if(type == DBREF) {
        ret = new DbRef(id, parent, values, x, y, w, h);
    } else if(type == DOMAIN) {
        ret = new Domain(id, parent, values, x, y, w, h);
    } else if(type == DOMAINVALUE) {
        ret = new DomainValue(id, parent, values, x, y, w, h);
    } else if(type == ACTION) {
        ret = new Action(id, parent, values, x, y, w, h);
    } else if(type == REFERENCE) {
        ret = new Reference(id, values[REF_IDREF]);
    }

    return ret;
    
}

/*********************************************************************/

function getTypeIndex(name) {

    var ret = -99;
    
    if(name == "Application") {
        ret = -1;
    } else {
        for(var nType = 0; nType < APPLICATION_SHAPE_NAMES.length; nType++) {
            if(APPLICATION_SHAPE_NAMES[nType] == name) {
                ret = nType;
            }
        }
    }
	

	return ret;
	
}

