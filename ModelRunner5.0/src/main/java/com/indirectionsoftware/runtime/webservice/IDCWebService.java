package com.indirectionsoftware.runtime.webservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.metamodel.IDCAction;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDomainValue;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCPackage;
import com.indirectionsoftware.metamodel.IDCPanel;
import com.indirectionsoftware.metamodel.IDCRefTree;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCReport;
import com.indirectionsoftware.metamodel.IDCReportFolder;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.utils.IDCItemId;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebService {
	
	private IDCApplication app;
	private String serverPath;
	
	static final String HTML_HEADERCOLOR = "<!DOCTYPE html><html><head><title>Your Data</title><script src=\"utils.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body style=\"background-color: #d3defd;\">";
	static final String HTML_HEADER = "<!DOCTYPE html><html><head><title>Your Data</title><script src=\"utils.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body>";
	static final String HTML_FOOTER = "</body></html>";
	
	static final String EXPLORER_TITLE = "Data Explorer";

	static final char SEP1 = ',', SEP2 = '.', SEP3 = ':';
	
	/****************************************************************************/

	public IDCWebService(IDCApplication app, String serverPath) {
		
		IDCUtils.traceStart("IDCWeblication() ...");
		
		this.app = app;
		this.serverPath = serverPath;
		
		IDCUtils.traceEnd("IDCWeblication()");
		
	}

	/****************************************************************************/

	public String process(HttpServletRequest request, IDCWebServiceContext context, int action) {
		
		IDCUtils.traceStart("IDCWeblication.process() ...");
		
		String ret = "";
		
		String errMsg = "";
		
		Map<String, IDCError> errors = new HashMap<String, IDCError>();

		if(context != null) {
			
			if(context.isUpdate &&  action == IDCWebServiceController.BACK) {
				action = IDCWebServiceController.UPDATEITEMCANCEL;
			}
			
			if(context.isUpdate && action != IDCWebServiceController.UPDATEITEMCANCEL) {
				errors = updateFromHTML(context, request, context.selectedData);
				if(errors.size() > 0 && action == IDCWebServiceController.UPDATEITEMSAVE) {
					action = IDCWebServiceController.UPDATEITEMREFRESH;
				}
			}

			context.message = "";

			switch(action) {
			
				case IDCWebServiceController.GETTYPELIST:
					
					setSelectedType(context, request);					
					setBrowser(context, context.selectedType.loadAllDataReferences());
					context.stack.stackElement(context.selectedType);
					context.action = action;
					context.selectedData = null; 
					ret = getTypeListHTML(context);
					break;
					
				case IDCWebServiceController.GETITEMDETAILS:
					
					setSelectedData(context, request);
					context.stack.stackElement(context.selectedData);
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.UPDATEITEM:
					
					startTransaction(context);
					context.isUpdate = true;
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.UPDATEITEMREFRESH:
					
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.UPDATEITEMSAVE:
					
					IDCError error = save(context, context.selectedData);
					if(error != null) {
						context.message = error.getMessage();
						ret = getItemDetailsHTML(context, errors);
					} else {
						endTransaction(context, true);
						if(context.isChild()) {
							context = context.parentContext;
							setContext(request, context);
						} else {
							context.action = IDCWebServiceController.GETITEMDETAILS;
							context.isUpdate = false;
						}
						ret = getItemDetailsHTML(context, errors);
					}
					break;
					
				case IDCWebServiceController.UPDATEITEMCANCEL:
					
					endTransaction(context, false);
					if(context.isChild()) {
						context = context.parentContext;
						setContext(request, context);
						if(!context.selectedData.isNew()) {
							context.selectedData.reload();
						}
						ret = getItemDetailsHTML(context, errors);
					} else {
						context.isUpdate = false;
						if(context.selectedData.isNew()) {
							context.action = IDCWebServiceController.GETTYPELIST;
							ret = getTypeListHTML(context);
						} else {
							context.action = IDCWebServiceController.GETITEMDETAILS;
							context.selectedData.reload();
							ret = getItemDetailsHTML(context, errors);
						}
					}
					break;
					
				case IDCWebServiceController.CREATEITEM:
					
					startTransaction(context); 
					context.isUpdate = true;
					context.selectedData = app.createData(context.selectedType, true);
					context.selectedType = context.selectedData.getDataType(); // in case getSuper typex
					context.stack.stackElement(context.selectedData);
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.CREATECHILDITEM:
					
					setSelectedAttr(context, request);
					
					IDCWebServiceContext parentContext = context;
					context = IDCWebServiceContext.createChildContext(parentContext, IDCWebServiceContext.CREATECHILD);
					setContext(request, context);
					
					startTransaction(context); 
					context.isUpdate = true;
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					IDCData attrData = findData(parentContext.selectedData, parentContext.selectedAttr);
					context.selectedData = app.createData(context.selectedType, attrData, parentContext.selectedAttr.getAttributeId(), true);
					context.selectedType = context.selectedData.getDataType(); // in case getSuper typex
					context.stack.stackElement(context.selectedData);
					context.action = IDCWebServiceController.CREATEITEM;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.DELETEITEM:
					
					error = context.selectedData.delete(false);
					if(error != null) {
						context.message = error.getMessage();
						ret = getItemDetailsHTML(context, errors);
					} else {
						setBrowser(context, context.selectedType.loadAllDataReferences());
						context.action = IDCWebServiceController.GETTYPELIST;
						context.selectedData = null; 
						ret = getTypeListHTML(context);
					}
					break;
					
				case IDCWebServiceController.DELETESELECTEDITEMS:
					
					List<IDCItemId> selectedItems =  getSelectedItems(request);
					for(IDCItemId itemPair : selectedItems) {
						IDCData selectedData = context.selectedType.loadDataObject(itemPair.itemId);
						if(selectedData != null) {
							error = selectedData.delete(false);
							if(error != null) {
								if(errMsg.length() > 0) {
									errMsg += " / ";
								}
								errMsg += selectedData.getName() + ": " + error.getMessage();
							}
						}
					}
						
					if(errMsg.length() > 0) {
						context.message = errMsg;
					}
					
					int nPage = context.browser.getPageNumber();
					setBrowser(context, context.selectedType.loadAllDataReferences());
					context.browser.setPageNumber(nPage);
					ret = getTypeListHTML(context);

					break;
					
				case IDCWebServiceController.REMOVESELECTEDITEMS:
					
					setSelectedAttr(context, request);

					IDCData parentData = findData(context.selectedData, context.selectedAttr);
					if(parentData != null) {
						IDCType type = null;
						selectedItems =  getSelectedItems(request);
						for(IDCItemId itemPair : selectedItems) {
							if(type == null || type.getId() != itemPair.typeId) {
								type = app.getType(itemPair.typeId);
							}
							IDCData childData = type.loadDataObject(itemPair.itemId);
							if(childData != null) {
								parentData.removeReference(context.selectedAttr, childData);
							}
						}
					}
						
					context.action = IDCWebServiceController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);

					break;
					
				case IDCWebServiceController.SELECTREF:
				case IDCWebServiceController.SELECTREFLIST:
					
					setSelectedAttr(context, request);
					String refListFormula = context.selectedAttr.getRefListFormula();
					parentContext = context;
					context = IDCWebServiceContext.createChildContext(parentContext, IDCWebServiceContext.SELECT);
					setContext(request, context);
					context.prefix = parentContext.prefix;
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					
					List<IDCDataRef> list = null;
					if(refListFormula == null ) {
						list = context.selectedType.loadAllDataReferences();
					} else {
						Object listObj = parentContext.selectedData.evaluate(refListFormula);
						if(listObj != null && listObj instanceof List) {
							list = new ArrayList<IDCDataRef>();
							for(IDCData data : ((List<IDCData>) listObj)) {
								list.add(data.getDataRef());
							}
						}
					}
					setBrowser(context, list);
					context.action = action;
					ret = getTypeListHTML(context);
					break;
					
				case IDCWebServiceController.SELECTREFOK:
					
					setSelectedData(context, request);
					IDCWebServiceContext childContext = context;
					context = context.parentContext;
					setContext(request, context);
					context.action = IDCWebServiceController.UPDATEITEM;
					attrData = findData(context.selectedData, context.selectedAttr);
					if(attrData != null) {
						attrData.set(context.selectedAttr, childContext.selectedData);
					}
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.SELECTREFCANCEL:
				case IDCWebServiceController.SELECTREFLISTCANCEL:
					
					childContext = context;
					context = context.parentContext;
					setContext(request, context);
					context.action = IDCWebServiceController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.SELECTREFLISTOK:
					
					childContext = context;
					context = context.parentContext;
					setContext(request, context);
					
					parentData = findData(context.selectedData, context.selectedAttr);
					if(parentData != null) {
						List<IDCData> children = new ArrayList<IDCData>(); 
						IDCType type = null;
						selectedItems =  getSelectedItems(request);
						for(IDCItemId itemPair : selectedItems) {
							if(type == null || type.getId() != itemPair.typeId) {
								type = app.getType(itemPair.typeId);
							}
							IDCData childData = type.loadDataObject(itemPair.itemId);
							if(childData != null) {
								children.add(childData);
							}
						}
						parentData.set(context.selectedAttr, children);
					}
						
					context.action = IDCWebServiceController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);

					break;
					
				case IDCWebServiceController.RELOADREFTREE:
					setSelectedAttr(context, request);
					int nBox = IDCUtils.getJSPIntParam(request, "nbox");
					String valueStr = request.getParameter("selection");
					IDCDataRef ref = IDCDataRef.getRef(valueStr);
					ret = updateRefTree(context, context.prefix, nBox, ref);

					break;
					
				case IDCWebServiceController.EXECUTEACTION:

					String actionIdStr = request.getParameter(IDCWebServiceController.ACTIONID_PARM);
					if(actionIdStr != null) {
						long actionId = Long.parseLong(actionIdStr);
						IDCAction act = context.selectedType.getAction(actionId);
						if(act != null) {
							act.execute(context.selectedData);
						}
					}
					if(!context.selectedData.isNew()) {
						context.selectedData.reload();
					}
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebServiceController.SETTINGS:
					
					context.action = action;
					context.stack.stackElement(new IDCURL("Settings", IDCURL.SETTINGS));
					ret = getSettingsHTML(context);
					break;
					
				case IDCWebServiceController.TODO:
					
					context.action = action;
					context.stack.stackElement(new IDCURL("Todo", IDCURL.TODO));
					ret = getTodoHTML(context);
					break;
					
				case IDCWebServiceController.TOGGLETODO:
					
					context.action = IDCWebServiceController.TODO;
					context.isTodoActive = !context.isTodoActive;
					ret = getTodoHTML(context);
					break;
					
					///////////////////////////////////////////////////////////////////////////////////////

					
				case IDCWebServiceController.BACK:
				case IDCWebServiceController.FORWARD:

					IDCUtils.debug("Processing BACK/FORWARD");
					
					if(action == IDCWebServiceController.BACK && context.action == IDCWebServiceController.SEARCH || action == IDCWebServiceController.POSTSEARCH) {
						
						setBrowser(context, context.selectedType.loadAllDataReferences());
						context.action = IDCWebServiceController.GETTYPELIST;

					} else {
						
						IDCEnabled data = moveStack(context, action);
						if(data != null) {
							if(data.isData()) {
								context.action = IDCWebServiceController.GETITEMDETAILS;
								context.selectedType = ((IDCData)data).getDataType();
								context.selectedData = ((IDCData)data).getDataType().loadDataObject(((IDCData)data).getId());
								ret = getItemDetailsHTML(context, errors);
							} else if(data.isType()) {
								context.action = IDCWebServiceController.GETTYPELIST;
								context.selectedType = (IDCType) data;
								context.selectedData = null; 
								setBrowser(context, context.selectedType.loadAllDataReferences());
								ret = getTypeListHTML(context);
							} else if(data instanceof IDCURL) {
								switch(((IDCURL) data).getType()) {
									case IDCURL.HOME:
										ret = getHomeHTML(context);
										break;
									case IDCURL.TODO:
										context.action = IDCWebServiceController.TODO;
										ret = getTodoHTML(context);
										break;
									case IDCURL.SETTINGS:
										context.action = IDCWebServiceController.SETTINGS;
										ret = getSettingsHTML(context);
										break;
								}
							}
						} else {

//							int nextPage=ITEMPAGE;

							switch(context.type) {

								case IDCWebServiceContext.SELECT:
									setBrowser(context, context.selectedType.loadAllDataReferences());
									break;
								
								case IDCWebServiceContext.CREATECHILD:
									setBrowser(context, context.parentContext.selectedData.getRefList(context.parentContext.selectedAttr.getAttributeId()));
									break;
									
							}
							
						}

					}
					break;
					
				case IDCWebServiceController.SEARCH:
					IDCUtils.debug("Processing SEARCH");
					initSearchData(context);
					setBrowser(context, context.selectedType.loadAllDataReferences());
					break;
					
				case IDCWebServiceController.POSTSEARCH:
					IDCUtils.debug("Processing POSTSEARCH");
//					error = updateFromHTML(context, request);
//					if(error != null) {
//						context.message = error.getMessage();
//					}
					setBrowser(context, searchFromHTML(context));
					break;
					
				case IDCWebServiceController.REMOVECONTEXT:
					IDCUtils.debug("Processing REMOVECONTEXT");
//					contexts.remove(contextId);
					break;
					
				case IDCWebServiceController.CLOSECONTEXT:
					IDCUtils.debug("Processing CLOSECONTEXT");
//					contexts.remove(contextId);
					break;

				case IDCWebServiceController.UPDATEDOMAIN:
					IDCUtils.debug("Processing UPDATEDOMAIN");
					setSelectedAttr(context, request);
					String selectedValue = IDCUtils.getJSPParam(request, "value");
					IDCUtils.debug("SelectedAttr = " + context.selectedAttr + " / selectedValue = " + selectedValue);
					updateFromHTML(context, context.selectedData, context.selectedAttr.getAttributeId(), selectedValue);
					break;
					
				case IDCWebServiceController.UPDATENAMESPACE:
					IDCUtils.debug("Processing UPDATENAMESPACE");
					setSelectedAttr(context, request);
					parentContext = context;
					context = IDCWebServiceContext.createChildContext(parentContext, IDCWebServiceContext.CREATECHILD);
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					setBrowser(context, parentContext.selectedData.getRefList(parentContext.selectedAttr.getAttributeId()));
					break;
					
				case IDCWebServiceController.UPDATELIST:
					IDCUtils.debug("Processing UPDATELIST");
					setSelectedAttr(context, request);
					parentContext = context;
					context = IDCWebServiceContext.createChildContext(parentContext, IDCWebServiceContext.CREATECHILD);
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					setBrowser(context, parentContext.selectedData.getRefList(parentContext.selectedAttr.getAttributeId()));
					break;
					
				case IDCWebServiceController.HELP:
					IDCUtils.debug("Processing HELP");
					break;
					
				case IDCWebServiceController.NEXTPAGE:
					IDCUtils.debug("Processing PREVPAGE");
					context.browser.setNextPage();
					ret = getTypeListHTML(context);
					break;
					
				case IDCWebServiceController.PREVPAGE:
					IDCUtils.debug("Processing PREVPAGE");
					context.browser.setPrevPage();
					ret = getTypeListHTML(context);
					break;
					
				default:
					IDCUtils.debug("Processing default ... no context.action found");
					context.message = "No context.action selected";
					ret += "<p>No context.action selected</p>";
					Enumeration paramNames = request.getParameterNames();
					while(paramNames.hasMoreElements()) {
						String paramName = (String)paramNames.nextElement();
						String[] values = request.getParameterValues(paramName);
						if (values.length == 1) {
				            String paramValue = values[0];
							ret += "<p>Name = " + paramName + " / Value = " + paramValue + "</p>";
				         } else {
				        	ret += "<p>Name = " + paramName + "</p>";
				            ret += "<ul>";
				            for(int i = 0; i < values.length; i++) {
								ret += "<li>" + values[i] + "</li>";
				            }
				            ret += "</ul>";
				         }
				    }
					break;
					
			}

		} else {
			IDCUtils.debug("Context not found ...");
		}
		
		IDCUtils.traceEnd("IDCWeblication.process()");
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getFullPage(IDCWebServiceContext context) {
		
		String ret = HTML_HEADER;
		
		ret += getNavDivHTML(context);
		
		ret += "<h1 class=\"apptitle\" style=text-align:center;>My Training App</h1>";
		
		ret += "<div id=\"main\">";

		ret += "<div id=\"_content\"><p></p>";
		ret += getHomeHTML(context);
		ret += "</div>";

		ret += "</div>";
		ret += HTML_FOOTER;
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getHomeHTML(IDCWebServiceContext context) {
		
		String ret = getButtonsDivHTML(context);
		ret += "<h1>Welcome to " + app.getDisplayName() + "</h1>";
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getTodoHTML(IDCWebServiceContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		ret += "<ul class=\"actionsbar\">";
		ret += "<li>" + getURLButton(context, "actionbut", (context.isTodoActive ? "Show All" : "Show Active"), IDCWebServiceController.TOGGLETODO, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		ret += "</ul>";


		ret += "<table><colgroup><col width='80'/><col width='150'/></colgroup>";
		
		boolean found=false;
		for(Entry<IDCType, List<IDCData>> entry : app.getTodoTypeList(context.isTodoActive, true).entrySet()) {
			ret += "<tr><td>" + IDCUtils.getPlural(entry.getKey().getDisplayName()) + "</td><td></td></tr>";
			ret += "<ul class=\"nested\">";
			for(IDCData todoData : entry.getValue()) {
				ret += "<tr><td></td><td>" + getURLButton(context, "linkbut", todoData.getName(), IDCWebServiceController.GETITEMDETAILS, entry.getKey().getId(), todoData.getId(), IDCWebServiceController.NA, "", true, true) + "</td></tr>";
				found = true;
			}
			ret += "<tr><td></td><td></td></tr>";
		}
		
		if(!found) {
			ret += "<tr><td></td><td>You're all done!</td></tr>";
		}
	
		ret += "</table></div>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";

		return ret;
		
	}

	/****************************************************************************/

	public String getSettingsHTML(IDCWebServiceContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";

		return ret;
		
	}

	/****************************************************************************/

	public String getNavDivHTML(IDCWebServiceContext context) {
		
		String ret = "<div id=\"explorer\" class=\"explorer\"><a href=\"javascript:void(0)\" class=\"closebtn\" onclick=\"closeNav()\">&times;</a>"; 
		
		ret += "<ul>" + app.getDisplayName();
		
		for(IDCPackage pack : app.getPackages(false)) {
			ret += "<li><span class=\"caret\" onclick=\"toggleNav(this);\">" + pack.getDisplayName() + "</span>";
			ret += "<ul class=\"nested\">";
			for(IDCType type : pack.getTypes()) {
				if(type.isTopLevelViewable()) {
					ret += "<li>" + getURLButton(context, null, IDCUtils.getPlural(type.getDisplayName()), IDCWebServiceController.GETTYPELIST, type.getId(), IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, true) + "</li>";
				}
			}
			ret += "</ul></li>";
		}
		
		ret += "</ul>";
		
		ret += "</div><button class=\"openbtn\" onclick=\"openNav()\">&#9776; " + EXPLORER_TITLE + "</button>\r\n"; 

		return ret;
		
	}

	/****************************************************************************/

	public String getButtonsDivHTML(IDCWebServiceContext context) {	
		
		String ret = "<ul class=\"menubar\">";

		ret += "<li>" + getURLButton(context, null, "Back", IDCWebServiceController.BACK, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", context.stack.isBackOk(context.type == IDCWebServiceContext.ROOT), false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Forward", IDCWebServiceController.FORWARD, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", context.stack.isForwardOk(), false) + "</li>";
		
		int action = -1;
		if(context.selectedData != null) {
			action = IDCWebServiceController.DELETEITEM;
		} else if(context.selectedType != null) {
			action = IDCWebServiceController.DELETESELECTEDITEMS;
		}
		if(action != -1) {
			ret += "<li>" + getURLButton(context, null, "Delete", action, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		}

		if(context.selectedType != null) {
			ret += "<li>" + getURLButton(context, null, "New", IDCWebServiceController.CREATEITEM, context.selectedType.getId(), IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		}

		if(context.selectedData != null) {
			ret += "<li>" + getURLButton(context, null, "Edit", IDCWebServiceController.UPDATEITEM, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebServiceController.NA, "", true, false) + "</li>";
		}
		
		ret += "<ul class=\"menubar-right\">";
		ret += "<li>" + getURLButton(context, null, "Todo" + getTodoNum(), IDCWebServiceController.TODO, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Settings", IDCWebServiceController.SETTINGS, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLLink(context, "Logout", IDCWebServiceController.LOGOFF, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		ret += "</ul>";

		ret += "</ul>";

		return ret;

	}

	/****************************************************************************/

	private String getTodoNum() {
		
		String ret = "";

		int nTodo=0;
		for(Entry<IDCType, List<IDCData>> entry : app.getTodoTypeList(true, true).entrySet()) {
			for(IDCData data : entry.getValue()) {
				nTodo++;
			}
		}
		
		if(nTodo != 0) {
			ret = " <span class=\"dot\">" + nTodo + "</span>";
		}
		
		return ret;
		
	}

	/****************************************************************************/

	public String getTypeListHTML(IDCWebServiceContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		if(context.browser.getMaxPageNumber() > 0) {
			ret += "<ul class=\"listbutpanel\">";
			ret += "<li>" + getURLButton(context, null, "Prev", IDCWebServiceController.PREVPAGE,  IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Next", IDCWebServiceController.NEXTPAGE,  IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, context.prefix, true, false) + "</li>";
			ret += "</ul>";
		}
		
		ret += getTableHTML(context, context.prefix, context.selectedType, context.browser, context.type == IDCWebServiceContext.ROOT || context.action == IDCWebServiceController.SELECTREFLIST);
		
		
		ret += "<ul class=\"listbutpanel\">";
		
		if(context.action == IDCWebServiceController.SELECTREFLIST) {
			ret += "<li>" + getURLButton(context, null, "Ok", IDCWebServiceController.SELECTREFLISTOK,  IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Cancel", IDCWebServiceController.SELECTREFLISTCANCEL, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		} else if(context.action == IDCWebServiceController.SELECTREF) { 
			ret += "<li>" + getURLButton(context, null, "Cancel", IDCWebServiceController.SELECTREFCANCEL, context.selectedType.getId(), IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</li>";
		}
		
		if(context.browser.getMaxPageNumber() > 0) {
			ret += "<li>" + getURLButton(context, null, "Prev", IDCWebServiceController.PREVPAGE,  IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Next", IDCWebServiceController.NEXTPAGE,  IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, context.prefix, true, false) + "</li>";
		}

		ret += "</ul>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";

		return ret;
		
	}
	
	/****************************************************************************/

	public String getTableHTML(IDCWebServiceContext context, List<Integer> attrIdList, int attrId, IDCType type, IDCDatabaseTableBrowser browser, boolean isTickNeeded) {
		return getTableHTML(context, getAttributeIdString(attrIdList, attrId), type, browser, isTickNeeded);
	}

	public String getTableHTML(IDCWebServiceContext context, String prefix, IDCType type, IDCDatabaseTableBrowser browser, boolean isTickNeeded) {
	
		String ret = null;
		
		String colheader = "<table><colgroup>";
		String header = "<tr>";
		
		List<IDCData> list = browser.getPage();
		
		if(list.size() > 0) {
			type = list.get(0).getDataType();
		}
		
		colheader += (isTickNeeded ? "<col width='20'/>" : "") + "<col width='200'/>";
		if(prefix.length() > 0) {
			header += (isTickNeeded ? (list.size() > 0 ? "<th><input name=\"" + prefix + "master$box\" type=\"checkbox\" onclick=\"toggleChildren(this,'" + prefix + "');\"></th>" : "<th></th>") : "") + "<th>Name</th>";
		} else {
			header += (isTickNeeded ? (list.size() > 0 ? "<th><input name=\"master$box\" type=\"checkbox\" onclick=\"toggleList(this);\"></th>" : "<th></th>") : "") + "<th>Name</th>";
		}

		for(IDCAttribute refAttr : type.getListAttributes()) {
			if(!refAttr.getName().equals("Name") && !refAttr.getName().equals(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME)) {
				colheader += refAttr.getHTMLWidth();
				header += "<th>" + refAttr.getDisplayName() + "</th>";
	        }
		}
        
		ret = colheader + "</colgroup><thead>" + header + "</tr></thead>";

		if(list.size() > 0) {
			
			boolean even=false;
			for(IDCData data : list) {
				
				ret += "<tr " + (even ? "style=background-color:lightgrey;" : "") + ">" + (isTickNeeded ? "<td><input name=\"" + prefix + SEP2 + data.getDataType().getId() + SEP3 + data.getId() + "\" type=\"checkbox\"></td>" : "");
				ret += "<td>" + getURLButton(context, "linkbut", data.getName(), IDCWebServiceContext.CONTEXTSELECTQUERY[context.type], data.getDataType().getId(), data.getId(), IDCWebServiceController.NA, "", true, false) + "</td>";
				for(IDCAttribute refAttr : type.getListAttributes()) {
					if(!refAttr.getName().equals("Name") && !refAttr.getName().equals(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME)) {
						ret += "<td>" + getAttributeListHTML(context, data, refAttr.getAttributeId(), refAttr) + "</td>";
					}		
				}
				ret += "</tr>";
//				even = !even;
		     }
			if(context.action == IDCWebServiceController.SELECTREF) { 
				ret += "<tr " + (even ? "style=background-color:lightgrey;" : "") + ">";
				ret += "<td>" + getURLButton(context, "linkbut", "(none)", IDCWebServiceContext.CONTEXTSELECTQUERY[context.type], IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, "", true, false) + "</td>";
			}
		} else {
			ret += "<tr>" + (isTickNeeded ? "<td></td>" : "") + "<td>No data ...</td></tr>";
		}
	
		ret += "</table></div>";

		return ret;
		
	}

	/************************************************************************************************/

    public String getAttributeListHTML(IDCWebServiceContext context, IDCData data, int attrId, IDCAttribute attr) {
    	
    	IDCUtils.traceStart("getDisplayHTML()");
    	
    	String ret = null; 
    	
    	switch(attr.getAttributeType()) {
		
			case IDCAttribute.REF:
			case IDCAttribute.REFBOX:
			case IDCAttribute.REFTREE:

				IDCUtils.debug("getDisplayHTML REF");
		    	
				IDCDataRef ref = (IDCDataRef) data.getRawValue(attrId);
				if(ref != null && ref.getItemId() != -1) {
					ret = data.getDisplayValue(attrId);
//
//					IDCType refType = app.getType(ref.getTypeId());
//					
//					IDCData refVal = refType.loadDataObject(ref.getItemId());
//					IDCUtils.debug("getDisplayHTML refVal="+refVal);
//					
//					ret = getURLButton(context,  "linkbut", refVal.getName(), IDCWebContext.CONTEXTSELECTQUERY[context.type], refVal.getDataType().getId(), refVal.getId(), IDCWebController.NA, "", true, false);
					
				} else {
					ret = "(no data)";
				}
				break;
				
			case IDCAttribute.EXTENSION:
				
				IDCData extData = data.getData(attrId);
				if(extData != null) {
					ret = data.getDisplayValue(attrId);
				} else {
					ret = "(no data)";
				}
				
				break;
	
			case IDCAttribute.DATE:
				long timestamp = data.getLong(attrId);
				ret = "";
				if(timestamp != 0) {
					ret = app.getCalendar().displayDate(timestamp);
				}
				break;
			case IDCAttribute.DATETIME:
				timestamp = data.getLong(attrId);
				ret = "";
				if(timestamp != 0) {
					ret = app.getCalendar().displayTimeDate(timestamp);
				}
				break;


			default:

				IDCUtils.debug("getDisplayHTML default");
				ret = data.getDisplayValue(attrId);
				if(ret.length() > 30) {
					ret = ret.substring(0, 30) + " ...";
				}
				break;

		}
    	
    	IDCUtils.traceEnd("getDisplayHTML()");
    	
		return ret;

    }
    
	/****************************************************************************/

	public String getItemDetailsHTML(IDCWebServiceContext context, Map<String, IDCError> errors) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		if(!context.isUpdate) {
			ret += getActionsDivHTML(context);
		}
		
		if(context.isUpdate) {
			ret += "<form name=\"UpdateItemDetails\">";
			context.onKeyPress = " onKeyPress=\"if(event.key == 'Enter') {" + getClickUpdateFunction(IDCWebServiceController.UPDATEITEMSAVE, context.selectedType.getId(), context.selectedData.getId(), true) + "}\"";
		}
		
		List<String> panelsHTML = getItemPanelsMap(context, context.selectedData, new ArrayList<Integer>(), errors);
		
		for(String html : panelsHTML) {
			ret += html;
		}
		
		if(context.isUpdate) {
			ret += "<ul class=\"detailsbutpanel\">";
			ret += "<li>" + getURLButton(context,  null, null, IDCWebServiceController.UPDATEITEMSAVE, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebServiceController.NA, "", true, false) + "</li>";
			ret += "<li>" + getURLButton(context,  null, "Cancel", IDCWebServiceController.UPDATEITEMCANCEL, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebServiceController.NA, "", true, false) + "</li>";
			ret += "</ul>";
		}
		
		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";
		
		return ret;
		
	}
	
	/****************************************************************************/

	private String getActionsDivHTML(IDCWebServiceContext context) {

		
		String ret = "<ul class=\"actionsbar\">";
		
		for(IDCAction action : context.selectedType.getGUIActions(false)) {
			ret += "<li>" + getURLButton(context, "actionbut", action.getName(), IDCWebServiceController.EXECUTEACTION, IDCWebServiceController.NA, IDCWebServiceController.NA, action.getId(), "", context.selectedData.isEditable(action), false) + "</li>";
		}

		ret += "</ul>";

		return ret;

	}

	/****************************************************************************/

	public List<String> getItemPanelsMap(IDCWebServiceContext context, IDCData data, List<Integer> attrList, Map<String, IDCError> errors) {
		
		List<String> ret = new ArrayList<String>();
		
    	List<String> temp = new ArrayList<String>();
    	
		String html = "";

		int nAttr = 0;
		for(IDCPanel panel : data.getDataType().getPanels()) {
			
			html += "<h2>" + panel.getDisplayName() + ": </h2>";
			
			html += "<table><colgroup><col width=200/><col width=*/></colgroup>";

			
			for(IDCAttribute attr : panel.getAttributes()) {
				
				if(!attr.getName().equals(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME)) {

					if(attr.isExtension() && data.getData(nAttr) == null) {
						IDCDataRef ref = data.getDataRef(nAttr);
		       			if(ref != null) {
							IDCType extType = app.getType(ref.getTypeId());
							data.set(nAttr,extType.createData(false));
		       			}
					}

					List<String> newPanelsList = null;
			    	if(context.isUpdate && data.isEditable(attr)) {
			    		newPanelsList = getAttributeUpdateHTMLMap(context, data, attrList, nAttr, attr, false, errors);
			    	} else {
			    		newPanelsList = getAttributeDisplayHTMLMap(context, data, attrList, nAttr, attr, false, errors);
			    	}
			    	boolean isFirst = true;
					for(String panelHtml : newPanelsList) {
						if(isFirst) {
							html += panelHtml; 
							isFirst = false;
						} else {
							temp.add(panelHtml);
						}
					}

				}
				
				
				nAttr++;

			}
			
			html += "</table>";
			
		}
		
		ret.add(html);
		ret.addAll(temp);
		
		return ret;
		
	}

	/************************************************************************************************/

    public List<String> getAttributeDisplayHTMLMap(IDCWebServiceContext context, IDCData data, List<Integer> attrList, int nAttr, IDCAttribute attr, boolean isSearch, Map<String, IDCError> errors) {
    	
    	IDCUtils.traceStart("getDisplayHTML()");
    	
    	List<String> ret = new ArrayList<String>();
    	
    	List<String> temp = new ArrayList<String>();
    	
    	IDCError error = errors.get(data.getAsParentRef(nAttr).toString());
    	
    	String rootHtml =  "<tr><td valign=\"top\">" + attr.getDisplayName() + "</td><td>"; 
    	
    	switch(attr.getAttributeType()) {
		
			case IDCAttribute.REF:
			case IDCAttribute.REFBOX:
			case IDCAttribute.REFTREE:

				IDCUtils.debug("getDisplayHTML REF");
		    	
				IDCDataRef ref = (IDCDataRef) data.getRawValue(nAttr);
				if(ref != null && ref.getItemId() != -1) {
					IDCType refType = app.getType(ref.getTypeId());
					
					IDCData refVal = data.getData(nAttr);
					IDCUtils.debug("getDisplayHTML refVal="+refVal);
					
					if(context.isUpdate) {
						rootHtml += refVal.getName();
					} else {
						rootHtml += getURLButton(context, "linkbut", refVal.getName(), IDCWebServiceController.GETITEMDETAILS, refVal.getDataType().getId(), refVal.getId(), IDCWebServiceController.NA, "", true, false);
					}
					
				} else {
					rootHtml += "(no data)";
				}
				break;
	
			case IDCAttribute.EXTENSION:
				
				IDCData extData = data.getData(nAttr);
				if(extData != null) {
					rootHtml += data.getDisplayValue(nAttr);
					List<Integer> attrListExt = new ArrayList<Integer>();
					attrListExt.addAll(attrList);
					attrListExt.add(nAttr);
					temp.addAll(getItemPanelsMap(context, extData, attrListExt, errors));
				} else {
					rootHtml += "(no data)";
				}
				
				break;
	
			case IDCAttribute.BACKREF:
			case IDCAttribute.LIST:
			case IDCAttribute.NAMESPACE:

				List<IDCDataRef> list = data.getRefList(nAttr);
				IDCType type = attr.getReferences().get(0).getDataType();
				IDCDatabaseTableBrowser browser = new IDCDatabaseTableBrowser(type, list);
				rootHtml += getTableHTML(context, attrList, nAttr, type, browser, false);
				
				break;
				
			case IDCAttribute.TEXT:
				rootHtml += "<textarea rows='5' cols='100' readonly>" + data.getDisplayValue(nAttr) + "</textarea>";
				break;
	
			case IDCAttribute.DATE:
				long timestamp = data.getLong(nAttr);
				if(timestamp != 0) {
					rootHtml += app.getCalendar().displayDate(timestamp);
				}
				break;
			case IDCAttribute.DATETIME:
				timestamp = data.getLong(nAttr);
				if(timestamp != 0) {
					rootHtml += app.getCalendar().displayTimeDate(timestamp);
				}
				break;

			default:

				IDCUtils.debug("getDisplayHTML default");
		    	
				rootHtml += data.getDisplayValue(nAttr);
				break;

		}
    	
		rootHtml += "</td>";
		
		if(error != null) {
			rootHtml += "<td>" + error.getMessage() + "</td>";
		}

		rootHtml += "</tr>";

		ret.add(rootHtml);
		ret.addAll(temp);
		
    	IDCUtils.traceEnd("getDisplayHTML()");
    	
		return ret;

    }
    
    /************************************************************************************************/

    public List<String> getAttributeUpdateHTMLMap(IDCWebServiceContext context, IDCData data, List<Integer> attrList, int attrId, IDCAttribute attr, boolean isSearch, Map<String, IDCError> errors) {
    	
    	IDCUtils.traceStart("getUpdateHTML()");
    	
    	List<String> ret = new ArrayList<String>();
    	
    	List<String> temp = new ArrayList<String>();
    	
    	String rootHtml = "<tr><td valign=\"top\">" + attr.getDisplayName() + "</td><td>";
    	
    	String displayVal = "" + data.getDisplayValue(attrId);
    	if(isSearch) {
    		displayVal = context.searchVals.get(attrId);
    	}
    	
    	IDCError error = errors.get(data.getAsParentRef(attrId).toString());
    	
		switch(attr.getAttributeType()) {
		
			case IDCAttribute.DOMAIN:
				IDCDomainValue keyVal = (IDCDomainValue) data.getRawValue(attrId);
				String key = null;
				if(keyVal != null) {
					key = keyVal.getKey();
				}	
					rootHtml += "<select class='dropdown' name='" + getFieldName(data, attrId) + "' onchange='selectDomainValue(\"IDCField" + context.fieldId++ + "\", " + attr.getAttributeId() + ");'>";
		            rootHtml += "<option value='(no selection)'" + (keyVal == null ? " selected='selected'" : "")+ ">(no selection)</option>";
					for(String val : attr.getRefDomainKeys()) {
			            rootHtml += "<option value='" + val + "'" + (val.equals(key) ? "selected='selected'" : "")+ ">" + val + "</option>";
					}
		            rootHtml += "</select>";
				break;

			case IDCAttribute.REFBOX:
			case IDCAttribute.REF:
       			List<IDCReference> attrRefs = attr.getReferences();
				List<IDCData> refs = null;
       			if(attrRefs != null && attrRefs.size() > 0) {
           			IDCType refType = attr.getReferences().get(0).getDataType();
    				refs = refType.loadAllDataObjects(refType.loadAllDataReferences(null, refType.getExplorerSQLFilter(), refType.getExplorerSQLOrderBy(), IDCType.NO_MAX_ROWS));
       			} else {
       				refs = new ArrayList<IDCData>();
       			}
       			
				IDCData selectedData = data.getData(attrId);
				
				if(attr.getAttributeType() == IDCAttribute.REFBOX) {
					
					rootHtml += "<select class='dropdown' name='" + getFieldName(data, attrId) + "'>";
		            rootHtml += "<option value='(no selection)'" + (selectedData == null ? " selected='selected'" : "")+ ">(no selection)</option>";
					for(IDCData refData : refs) {
			            rootHtml += "<option value='" + new IDCDataRef(refData) + "'" + (refData.equals(selectedData) ? " selected='selected'" : "")+ ">" + refData.getName() + "</option>";
					}
		            rootHtml += "</select>";
		            
				} else if(attr.getAttributeType() == IDCAttribute.REF) {
					
					IDCDataRef ref = (IDCDataRef) data.getRawValue(attrId);
					String displayValue = data.getDisplayValue(attrId); 
					
					if(displayValue.length() == 0) {
						displayValue = "(no data)";
					}
					rootHtml  += getURLButton(context, "linkbut", displayValue, IDCWebServiceController.SELECTREF, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, getAttributeIdString(attrList, attrId), true, false);
				}
				break;
	
			case IDCAttribute.REFTREE:
//				IDCDataRef ref = (IDCDataRef) data.getRawValue(nAttr);
				IDCDataRef ref = data.getDataRef(attrId);
				IDCRefTree tree = new IDCRefTree(attr, ref);
				String attrKey = getAttributeIdString(attrList, attrId);
				rootHtml += tree.getHTML(context, attrKey, getFieldName(data, attrId));
				context.refTrees.put(attrKey, tree);
				break;

			case IDCAttribute.EXTENSION:
				
				int extTypeId = -1;
				IDCData extData = data.getData(attrId);
				if(extData != null) {
					extTypeId = extData.getDataType().getEntityId();
				}
       			
				rootHtml += "<select id=IDCField" + context.fieldId++ + " onchange=\"" + getClickUpdateFunction(IDCWebServiceController.UPDATEITEMREFRESH, context.selectedType.getEntityType(), context.selectedData.getId(), true) + "\" class='dropdown' name='" + getFieldName(data, attrId) + "'>";
	            rootHtml += "<option value='(no selection)'" + (extTypeId == -1 ? " selected='selected'" : "")+ ">(no selection)</option>";
				for(IDCReference extRef : attr.getReferences()) {
					IDCType extRefType = extRef.getDataType();
		            rootHtml += "<option value='" + extRefType.getName() + "'" + (extRefType.getEntityId() == extTypeId ? " selected='selected'" : "")+ ">" + extRefType.getName() + "</option>";
				}
	            rootHtml += "</select>";

				if(extData != null) {
					List<Integer> attrListExt = new ArrayList<Integer>();
					attrListExt.addAll(attrList);
					attrListExt.add(attrId);
					temp.addAll(getItemPanelsMap(context, extData, attrListExt, errors));
				}
				
				break;
	
			case IDCAttribute.BACKREF:
			case IDCAttribute.LIST:
			case IDCAttribute.NAMESPACE:
				
				List<IDCDataRef> list = data.getRefList(attrId);
				IDCType type = attr.getReferences().get(0).getDataType();
				IDCDatabaseTableBrowser browser = new IDCDatabaseTableBrowser(type, list);
				rootHtml += getTableHTML(context, attrList, attrId, type, browser, true);
				if(attr.getAttributeType() == IDCAttribute.LIST) {
					rootHtml += "<ul class=\"insidelistbutpanel\">";
					rootHtml  += "<li>" + getURLButton(context, null, "Update", IDCWebServiceController.SELECTREFLIST, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml  += "<li>" + getURLButton(context, null, "Remove", IDCWebServiceController.REMOVESELECTEDITEMS, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</il>";
				} else if(attr.getAttributeType() == IDCAttribute.NAMESPACE) {
					rootHtml += "<div class=\"insidelistbutpanel\">";
					rootHtml  += "<li>" + getURLButton(context, null, "New", IDCWebServiceController.CREATECHILDITEM, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml  += "<li>" + getURLButton(context, null, "Delete", IDCWebServiceController.REMOVESELECTEDITEMS, IDCWebServiceController.NA, IDCWebServiceController.NA, IDCWebServiceController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</div>";
				}
				
				break;
	
			case IDCAttribute.TEXT:
				rootHtml += "<textarea id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' rows='5' cols =100'>" + data.getDisplayValue(attrId) + "</textarea>";
				break;
	
			case IDCAttribute.DATE:
				long timeStamp = data.getLong(attrId);
				String dateStr = "";
				if(timeStamp != 0) {
					dateStr = app.getCalendar().displayDateShort(timeStamp);
				}
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + dateStr + "' maxlength='20' size='15' />";
//				rootHtml += "<script language='JavaScript'>new tcal ({'formname': 'updateform','controlname': '" + attr.getName() + "'});</script>";
				break;

			case IDCAttribute.DATETIME:
				timeStamp = data.getLong(attrId);
				dateStr = "";
				if(timeStamp != 0) {
					dateStr = app.getCalendar().displayTimeDate(timeStamp);
				}
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + dateStr + "' maxlength='20' size='15' />";
//				rootHtml += "<script language='JavaScript'>new tcal ({'formname': 'updateform','controlname': '" + attr.getName() + "'});</script>";
				break;

			case IDCAttribute.BOOLEAN:
				rootHtml += "<input type=\"checkbox\" id=IDCField" + context.fieldId++ + " name='" + getFieldName(data, attrId) + "' " + (data.getBoolean(attrId) ? "checked" : "") + "/>";
				break;

			case IDCAttribute.INTEGER:
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + displayVal + "' maxlength='20' size='15' onKeyPress='return validateInteger(this, event);' />";
				break;

			case IDCAttribute.PRICE:
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + displayVal + "' maxlength='20' size='15'  onKeyPress='return validatePrice(this, event);' />";
				break;

			case IDCAttribute.PHONE:
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + displayVal + "' maxlength='20' size='15'  onKeyPress='return validatePhone(this, event);' />";
				break;

			case IDCAttribute.EMAIL:
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + displayVal + "' maxlength='" + attr.getLength() + "' size='" + attr.getLength() + "' onKeyPress='return validateEmail(this, event);' />";
				break;

			default:
				rootHtml += "<input id=IDCField" + context.fieldId++ + context.onKeyPress + " name='" + getFieldName(data, attrId) + "' class='text' type='text' value='" + displayVal + "' maxlength='" + attr.getLength() + "' size='\" + attr.getLength() + \"' />";
				break;

		}
		
		rootHtml += "</td>";
		
		if(error != null) {
			rootHtml += "<td>" + error.getMessage() + "</td>";
		}

		rootHtml += "</tr>";

		ret.add(rootHtml);
		ret.addAll(temp);

		IDCUtils.traceEnd("getUpdateHTML()");

    	return ret;

    }
    
	/************************************************************************************************/

    private String getFieldName(IDCData data, int nAttr) {
		return "IDC" + data.getDataType().getId() + "_" + data.getId() + "_" + nAttr;
	}

    /****************************************************************************/
    
	static String getURLLink(IDCWebServiceContext context, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) {
		return "<a href=\"IDCWebController?" + IDCWebServiceController.ACTION_PARM + "=" + action + "&" + IDCWebServiceController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebServiceController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebServiceController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebServiceController.ACTIONID_PARM + "=" + actionId+ "\">" + label + "</a>";
	}
		
	static String getURLButton(IDCWebServiceContext context, String className, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) throws Error {
	
		String ret = "<button ";
		if(className != null) {
			if(context.isUpdate) {
				className = "upd" + className;
			}
			ret += "class=\"" + className + "\" ";
		}

		String func = "event.preventDefault(); ";
		if(isCloseNav) {
			//func += "closeNav(); ";
		}
		String type = "";
		
		String query = "'IDCWebController?" + IDCWebServiceController.ACTION_PARM + "=" + action + "&" + IDCWebServiceController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebServiceController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebServiceController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebServiceController.ACTIONID_PARM + "=" + actionId+ "'";
				
		if(action == IDCWebServiceController.REMOVESELECTEDITEMS || action == IDCWebServiceController.SELECTREFLISTOK) {
			func += "processSelectedChildrenItems(" + query + ", '" + attrIdStr + "');";
		} else if(action == IDCWebServiceController.DELETESELECTEDITEMS) {
			func += "processSelectedListItems(" + query + ");";
		} else if(context.isUpdate) {
			if(label == null) {
				func += getClickUpdateFunction(action, typeId, itemId, isActive);
				label = "Update";
				type = "type = \"submit\"";
			} else {
				func += "reloadPost('IDCWebController'," + action + "," + typeId + "," + itemId + ",'" + attrIdStr + "'); return false;"; 
			}
		} else {
			func += "reloadGet(" + query + ");";
		}
				
		if(isActive) {
			ret += type + "onclick=\"" + func + "\""; 
		} else {
			ret += "disabled"; 
		}
		ret += ">" + label + "</button>"; 
				
		
		return ret;
		
		
	}

    /****************************************************************************/
	 
	static String getClickUpdateFunction(int action, long typeId, long itemId, boolean isActive) throws Error {
		return "event.preventDefault(); reloadPost('IDCWebController'," + action + "," + typeId + "," + itemId + "," + IDCWebServiceController.NA + "); return false;";
	}

	/****************************************************************************/

	public String getPageTitle(IDCWebServiceContext context) {

		String ret = null;
		
		switch(context.action) {
		
			case IDCWebServiceController.SETTINGS:
				ret = "Settings:";
				break;
			
			case IDCWebServiceController.TODO:
				ret = "Todo List:";
				break;
			
			case IDCWebServiceController.GETTYPELIST:
				ret = "List " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				if(context.browser.getMaxPageNumber() > 0) {
					ret += " - Page " + (context.browser.getPageNumber() + 1) + " of " + (context.browser.getMaxPageNumber() + 1);
				}
				break;
				
			case IDCWebServiceController.POSTSEARCH:
				ret = "Search Results for " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebServiceController.GETITEMDETAILS:
			case IDCWebServiceController.UPDATEITEMSAVE:
			case IDCWebServiceController.EXECUTEACTION:
				ret = "Browse " + context.selectedType.getDisplayName() + ": " + context.selectedData.getName();
				break;
				
			case IDCWebServiceController.CREATEITEM:
			case IDCWebServiceController.CREATECHILDITEM:
				ret = "New " + context.selectedType.getDisplayName();
				break;
				
			case IDCWebServiceController.UPDATEITEM:
				ret = "Update " + context.selectedType.getDisplayName() + ": " + context.selectedData.getName();
				break;
				
			case IDCWebServiceController.SEARCH:
				ret = "Search " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebServiceController.SELECTREF:
				ret = "Select a " + context.selectedType.getDisplayName();
				break;
				
			case IDCWebServiceController.SELECTREFLIST:
				ret = "Select " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebServiceController.UPDATENAMESPACE:
			case IDCWebServiceController.UPDATELIST:
				ret = "Manage " + context.parentContext.selectedAttr.getDisplayName();
				break;
				
			default:
				break;
				
	}
		
		return ret;
		
	}

	/************************************************************************************************/

    public void resetHTMLFieldId(IDCWebServiceContext context) {
    	context.fieldId = 0;
    }
    
    /************************************************************************************************/

    public String getHTMLAttributeInfo(IDCWebServiceContext context, IDCData data, int nAttr) {
    	
    	String ret = "";
    	
    	IDCType type = data.getDataType();
    	IDCAttribute attr = type.getAttribute(nAttr);
    	
    	if(attr.isMandatory()) {
    		ret += "Required";
    	}

    	switch(attr.getAttributeType()) {
    	
			case IDCAttribute.PHONE:
	    		ret += (ret.length() == 0 ? "" : ", ") +  "enter a valid 10 digit phone number";
	    		break;
	    		
			case IDCAttribute.EMAIL:
	    		ret += (ret.length() == 0 ? "" : ", ") +  "enter a valid email address";
	    		break;
	    		
	    	default:
				break;
	
		}
	
    	return ret;
    	
    }
    
    /************************************************************************************************/

    public String getSearchHTML(IDCWebServiceContext context, int nAttr) {
    	
    	String ret = null;
    	
    	IDCType type = context.selectedType;
    	IDCAttribute attr = type.getAttribute(nAttr);
    	
//  		ret = getUpdateHTML(context, context.selectedData, nAttr, attr, false, true);

    	return ret;
    	
    }
    

    /************************************************************************************************/

	public IDCError save(IDCWebServiceContext context, IDCData data) {
		
		IDCError ret = null;

		int nAttr=0;
		for(IDCAttribute attr : data.getDataType().getAttributes()) {
			if(attr.isExtension()) {
				Object val = data.getRawValue(nAttr);
				if(val instanceof IDCData) {
					ret = save(context, (IDCData) val);
					if(ret == null) {
						data.set(nAttr, ((IDCData) val).getDataRef());
					} else {
						break;
					}
				} 
			}
			nAttr++;
		}

		if(ret == null) {
			List<IDCError> errors = data.save();
			if(errors.size() > 0) {
				String errMsg = "";
				for(IDCError error : errors) {
					errMsg += "<p>" + error.getMessage() + "</p>" ;
					if(error.getAttributeId() == -1) {
					}
				}
				ret = new IDCError(-1, errMsg);
			}
		}
        
		return ret;
		
	}

    /************************************************************************************************/

	public Map<String, IDCError> updateFromHTML(IDCWebServiceContext context, HttpServletRequest request, IDCData data) {
		
		Map<String, IDCError> ret = new HashMap<String, IDCError>();

		IDCUtils.debug("IDCWebPageContext.updateFromHTML():");

		context.searchVals = new ArrayList<String>();

		List<IDCAttribute> attributes = data.getDataType().getAttributes();
		
        for(int nAttr=0; nAttr < attributes.size(); nAttr++) {
        	
        	IDCAttribute attr = attributes.get(nAttr);
        	
			IDCError error = null;

			String newVal = (String) request.getParameter(getFieldName(data,nAttr));
            if(newVal != null && newVal.length() > 0) {
    			try {
    				newVal = URLDecoder.decode(newVal,"UTF-8");
    				error = attr.checkData(newVal);
    				if(error == null) {
                        updateFromHTML(context, data, nAttr, newVal);
    				}
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
    			
            }
            
            if(error == null) {
                error = data.checkData(attributes.get(nAttr), false);
            }
            
            if(error != null) {
            	ret.put(data.getAsParentRef(nAttr).toString(), error);
            }

        	if(attributes.get(nAttr).isExtension()) {
        		IDCData extData = data.getData(nAttr);
        		if(extData != null) {
        			data.setValue(nAttr,extData);
    				updateFromHTML(context, request, extData );
        		}
        	}

        }
        
        return ret;
        
	}

    /************************************************************************************************/

	public void updateFromHTMLOLD(IDCWebServiceContext context, HttpServletRequest request, IDCData data) {
		
		IDCUtils.debug("IDCWebPageContext.updateFromHTML():");

		context.searchVals = new ArrayList<String>();
		
		int nAttr=0;
        for(IDCAttribute attr : data.getDataType().getAttributes()) {
        	
            String newVal = (String) request.getParameter(getFieldName(data,nAttr));
            if(newVal != null && newVal.length() > 0) {
            	
    			try {
    				newVal = URLDecoder.decode(newVal,"UTF-8");
                    updateFromHTML(context, data, nAttr, newVal);
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
    			
            }
            
        	if(attr.isExtension()) {
        		IDCData extData = data.getData(nAttr);
        		if(extData != null) {
        			data.setValue(nAttr,extData);
    				updateFromHTML(context, request, extData );
        		}
        	}
        	
            nAttr++;
            
        }
        
	}

    /************************************************************************************************/

    public void updateFromHTML(IDCWebServiceContext context, IDCData data, int nAttr, String valueStr) {
    	
    	IDCAttribute attr = data.getDataType().getAttribute(nAttr);
    	
		switch(attr.getAttributeType()) {
		
			case IDCAttribute.DOMAIN:
				IDCDomainValue val = attr.getRefDomain().getDomainValue(valueStr);
				data.set(nAttr, val);	
				break;

			case IDCAttribute.REF:
			case IDCAttribute.REFBOX:
			case IDCAttribute.REFTREE:
				IDCDataRef ref = IDCDataRef.getRef(valueStr);
				if(ref.getItemId() == -1) {
					ref = null;
				}
				data.set(nAttr, ref);
				break;
	
			case IDCAttribute.EXTENSION:
				IDCType extType = null;
                if(!valueStr.equals(IDCData.NOSELECTION)) {
    				extType = app.getType(valueStr);
                }
				IDCData extData = data.getData(nAttr);
				if(extType == null && extData != null || extData == null && extType != null || extType != null && extData != null && extData.getDataType().getId() != extType.getId()) {
					if(extData != null) {
						extData.delete(true);
						extData = null;
					}
					if(extType != null) {
						extData = extType.getNewObject();
					}
					data.set(nAttr, extData);
				}

				break;
	
			case IDCAttribute.BACKREF:
				break;
	
			case IDCAttribute.LIST:
			case IDCAttribute.NAMESPACE:
				break;
				
			case IDCAttribute.DATE:
			case IDCAttribute.DATETIME:
				long date = app.getCalendar().getDate(valueStr, '-');
				data.set(nAttr, date);
				break;

			default:
				data.set(nAttr, valueStr);
				break;

		}

    }
    
    /************************************************************************************************/

    public List<IDCDataRef> searchFromHTML(IDCWebServiceContext context) {
    	
    	List<IDCDataRef> ret = new ArrayList<IDCDataRef>();

    	for(IDCDataRef ref : context.selectedType.loadAllDataReferences()) {
    		
    		IDCData data = context.selectedType.loadDataRef(ref);
    		
			IDCUtils.debug("IDCWebPageContext.searchFromHTML(): data = " + data);

    		boolean isMatchFound=true;
    		
    		int nAttr=0;
    		for(IDCAttribute attr : context.selectedType.getAttributes()) {
    			
    			Object searchVal = context.selectedData.getRawValue(nAttr);
    			Object searchValObj = context.selectedData.getValue(nAttr);
    			String searchValStr = ""+searchVal;
    			
    			if(searchVal != null && searchValStr.length() > 0 && context.searchVals.get(nAttr).length() > 0) {
    				
        			Object val = data.getValue(nAttr);
        			
    				IDCUtils.debug("IDCWebPageContext.searchFromHTML(): searchVal = " + searchVal + " / val = " + val);

    				switch(attr.getAttributeType()) {
    				
    					case IDCAttribute.DOMAIN:
    						if(searchVal != val) {
    							isMatchFound = false;
    						}
    						break;
    	
    					case IDCAttribute.REFBOX:
    					case IDCAttribute.REF:
    					case IDCAttribute.REFTREE:
    						if(searchValObj != null && !((IDCData)searchValObj).equals((IDCData)val)) {
    							isMatchFound = false;
    						}
    						break;
    	
    					case IDCAttribute.EXTENSION:
    					case IDCAttribute.BACKREF:
    					case IDCAttribute.LIST:
    					case IDCAttribute.NAMESPACE:
    						break;
    			
    					default:
    						
    						String valStr = ("" + val).toUpperCase();
    						searchValStr = searchValStr.toUpperCase();
    						
    						boolean isStartsWith=false, isEndsWith=false;
    					    
    						if(searchValStr.startsWith("*")) {
    							isEndsWith = true;
    					    	searchValStr = searchValStr.substring(1);
    					    }
    					    
    					    if(searchValStr.endsWith("*")) {
    					    	isStartsWith = true;
    					    	searchValStr = searchValStr.substring(0, searchValStr.length()-1);
    					    } 
    					    
    					    if(isStartsWith && isEndsWith) {
        						if(valStr.indexOf(searchValStr) == -1) {
        							isMatchFound=false;
        						}
    					    } else if(isStartsWith) {
        						if(!valStr.startsWith(searchValStr)) {
        							isMatchFound=false;
        						}
    					    } else if(isEndsWith) {
        						if(!valStr.endsWith(searchValStr)) {
        							isMatchFound=false;
        						}
    					    } else {
        						if(!valStr.equalsIgnoreCase(searchValStr)) {
        							isMatchFound=false;
        						}
    					    }
    						break;
    	
    				}

    			}
				
				nAttr++;
				
			}
    		
			if(isMatchFound) {
				ret.add(ref);
			}
			
    	}
    	
    	return ret;
    	
    }
    
    /************************************************************************************************/

    public String getHelp() {

    	String ret = "<P>This is your help page ...</P>";
    	
    	return ret;
    	
    }
    
	/****************************************************************************/

	public String getTitle() {
		
		String ret = "Model Data Manager";
		
		if(app != null) {
			ret = app.getName();
		}
		
		return ret;
	
	}

	/****************************************************************************/

	public List<IDCType> getTypes() {

		List<IDCType> ret = new ArrayList<IDCType>();
		
		for(IDCType type : app.getTypes()) {
			if(type.isTopLevelViewable()) {
				ret.add(type);
			}
		}
		
		return ret;
		
	}

	/****************************************************************************/

	public String updateRefTree(IDCWebServiceContext context, String prefix, int nBox, IDCDataRef ref) {
		
		String ret = "";

		IDCRefTree tree = context.refTrees.get(prefix);
		if(tree != null) {
			tree.updateSelection(nBox, ref);
			IDCData attrData = findData(context.selectedData, context.selectedAttr);
			int nAttr = getAttrId(context.prefix);
			ret = tree.getHTML(context, context.prefix, getFieldName(attrData, nAttr));
		}
		
		return ret;
		
	}

	/****************************************************************************/

	public void executeAction(IDCWebServiceContext context, String actionName) {
		
		IDCAction act = context.selectedType.getAction(actionName);
		if(act != null) {
			act.execute(context.selectedData);
		}
		
	}
	
	/****************************************************************************/
	
	public IDCReport getReport(IDCWebServiceContext context, HttpServletRequest request) {
		
		IDCReport ret = null;

		String reportName = request.getParameter("reportname");
		if(reportName != null) {
			for(IDCReportFolder folder : app.getReportFolders()) {
				for(IDCReport report : folder.getReports()) {
					if(report.getReportType() == context.selectedType && report.getName().equals(reportName)) {
						report.setDirectoryName(getServerPath());
						ret = report;
						break;
					}
				}
			}
		}

		return ret;

	}

	/**************************************************************************************************/

   	public void print(IDCWebServiceContext context, PrintWriter out) {
		
   		String styleSheet;
   		
   		if(context.selectedData == null) {
   			styleSheet = context.selectedType.getDefaultListStylesheet();
   		} else {
   			styleSheet = context.selectedType.getDefaultDetailsStylesheet();
   		}

   		if(styleSheet.length() > 0) {
   	   		
			String printFileName = generatePrintFile(context);
	        
			File xmlFile = new File(printFileName);
	        File xsltFile = new File(styleSheet);
	 
	        Source xmlSource = new StreamSource(xmlFile);
	        Source xsltSource = new StreamSource(xsltFile);
	        Result result = new StreamResult(out);
	 
	        TransformerFactory transFact = TransformerFactory.newInstance(  );
	 
	        Transformer trans;
			try {
				trans = transFact.newTransformer(xsltSource);
		        trans.transform(xmlSource, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			xmlFile.delete();
			
   		} else {
   			
   	   		if(context.selectedData == null) {
   	   			app.writeXMLList(out, context.browser.getAllRefs(), true);
   	   		} else {
   	   			context.selectedData.writeXML(out, true);
   	   		}
   		}

	}

	/**************************************************************************************************/

	private String generatePrintFile(IDCWebServiceContext context) {

		String ret = "Print" + System.currentTimeMillis() + ".xml";
		
		try {
			File tempFile = new File(getServerPath() + ret);
			PrintWriter tempWriter = new PrintWriter(tempFile);
   	   		if(context.selectedData == null) {
   	   			app.writeXMLList(tempWriter, context.browser.getAllRefs(), true);
   	   		} else {
   	   		context.selectedData.writeXML(tempWriter, true);
   	   		}
			tempWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return ret;
		
	}
	
	/****************************************************************************/

	public void startTransaction(IDCWebServiceContext context) {
		context.transId = app.startTransaction();
	}

	/****************************************************************************/

	public void endTransaction(IDCWebServiceContext context, boolean isCommit) {
		if(context.transId != -1) {
			app.endTransaction(context.transId, isCommit);
			context.transId = -1;
		}
	}
	
	/****************************************************************************/

	public IDCEnabled moveStack(IDCWebServiceContext context, int action) {

		IDCEnabled ret = null;
		
		if(action == IDCWebServiceController.FORWARD) {
			ret = context.stack.moveForwardStack();
		} else {
			ret = context.stack.moveBackStack();
		}
	
		IDCUtils.debug("IDCWebContext.moveStack(): ret = " + ret);

		return ret;
	
	}

	/************************************************************************************************/

	public void setBrowser(IDCWebServiceContext context, List<IDCDataRef> list) {
		context.browser = new IDCDatabaseTableBrowser(context.selectedType, list);
	}

	/****************************************************************************/
	
	public IDCType getType(HttpServletRequest request) {
		
		IDCType ret = null;
		
		int typeId = IDCWebServiceController.getIntParam(request, IDCWebServiceController.TYPEID_PARM);
		if(typeId != IDCWebServiceController.NA) {
			ret = app.getType(typeId);
		}
			
		return ret;

	}

	/****************************************************************************/
	
	public void setSelectedType(IDCWebServiceContext context, HttpServletRequest request) {
		
		int typeId = IDCWebServiceController.getIntParam(request, IDCWebServiceController.TYPEID_PARM);
		if(typeId != IDCWebServiceController.NA) {
			IDCType type = app.getType(typeId);
			if(type != null) {
				context.selectedType = app.getType(typeId);
			} else {
				context.message = "Invalid type specified: typeId = " + typeId;
			}
		}
		
	}

	/****************************************************************************/
	
	public void setSelectedData(IDCWebServiceContext context, HttpServletRequest request) {
		
		setSelectedType(context, request);
		
		long itemId = IDCUtils.getJSPLongParam(request, IDCWebServiceController.ITEMID_PARM);
		if(itemId != IDCWebServiceController.NA) {
			IDCData data = context.selectedType.loadDataObject(itemId);
			if(data != null) {
				if(context.selectedData == null || data.getDataType().getEntityId() != context.selectedData.getDataType().getEntityId() || itemId != context.selectedData.getId()) {
					context.selectedData = data; 
				}
			} else {
				context.message = "Invalid data: type = " + context.selectedType.getName() + " /  itemId = " + itemId;
			}
		}
		
	}

	/****************************************************************************/
	
	public void setSelectedAttr(IDCWebServiceContext context, HttpServletRequest request) {
		
		setSelectedData(context, request);
		
		String attrIdStr = IDCUtils.getJSPParam(request, IDCWebServiceController.ATTRID_PARM);
		if(attrIdStr.length() > 0) {
			context.prefix = attrIdStr; 
			IDCAttribute attr = getAttribute(context.selectedData, attrIdStr);
			if(attr != null) {
				context.selectedAttr = attr; 
			} else {
				context.message = "Invalid selection attribute specified: type = " + context.selectedType.getName() + " / attrIdStr = " + attrIdStr;
			}
		}

	}
	
	/****************************************************************************/

	private List<IDCItemId> getSelectedItems(HttpServletRequest request) {
		
		List<IDCItemId> ret = new ArrayList<IDCItemId>();
		
		IDCItemId itemPair = null;
		String selectedItems = request.getParameter(IDCWebServiceController.SELECTEDIDS);
		if(selectedItems != null) {
			int startIndx=0;
			boolean looping = true;
			while (looping) {
				int indx = selectedItems.indexOf(SEP1, startIndx);
				if(indx != -1) {
					itemPair = getItemId(selectedItems.substring(startIndx, indx));
					startIndx = indx + 1;
				} else {
					itemPair = getItemId(selectedItems.substring(startIndx, selectedItems.length()));
					looping = false;
				}
				if(itemPair != null) {
					ret.add(itemPair);
					
				}
			}
		}
		
		return ret;
		
	}

	/****************************************************************************/

    private IDCItemId getItemId(String itemStr) {
    	
    	IDCItemId ret = null;
    	
		int indx = itemStr.indexOf(SEP2);
		if(indx != -1) {
			String attrStr = itemStr.substring(0, indx);
			String dataIdStr = itemStr.substring(indx+1, itemStr.length());
			indx = dataIdStr.indexOf(SEP3);
			if(indx != -1) {
				String typeIdStr = dataIdStr.substring(0, indx);
				String itemIdStr = dataIdStr.substring(indx+1, dataIdStr.length());
				int typeId = Integer.parseInt(typeIdStr);
				long itemId = Long.parseLong(itemIdStr);
				ret = new IDCItemId(attrStr, typeId, itemId);
			}
		}

		return ret;
	}


	/****************************************************************************/
	 
	static String getAttributeIdString(List<Integer> attrIdList, int attrId) {

		String ret = "";
		
		for(int extAttrId : attrIdList) {
			if(ret.length() > 0) {
				ret += SEP3;
			}
			ret += extAttrId;
		}
		
		if(attrId != IDCWebServiceController.NA) {
			if(ret.length() > 0) {
				ret += SEP3;
			}
			ret += attrId;
		}

		return ret;
		
	}
	
    /****************************************************************************/
    
	private int getAttrId(String prefix) {
		int ind = prefix.lastIndexOf(SEP3);
		String attrIdStr = prefix.substring(ind+1, prefix.length());
		int nAttr = Integer.parseInt(attrIdStr);
		return nAttr;
	}

	/****************************************************************************/

	private static IDCAttribute getAttribute(IDCData data, String attrIdStr) {

		IDCAttribute ret =  null;
		
		try {
			attrIdStr = URLDecoder.decode(attrIdStr,"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		int i = attrIdStr.indexOf(SEP3);
		if(i == -1) {
			int attrId = Integer.parseInt(attrIdStr);
			ret = data.getDataType().getAttribute(attrId);
		} else {
			String s1 = attrIdStr.substring(0, i);
			int attrId = Integer.parseInt(s1);
			IDCData extData = data.getData(attrId);
			if(extData != null) {
				String s2 = attrIdStr.substring(i+1);
				ret = getAttribute(extData, s2);
			}
		}
		
		return ret;
		

	}

	/****************************************************************************/

	private static IDCData findData(IDCData data, IDCAttribute selectedAttr) {
		
		IDCData ret = null;
		
		List<IDCData> extDataList = new ArrayList<IDCData>();
		
		int nAttr = 0;
		for(IDCAttribute attr : data.getDataType().getAttributes()) {
			if(attr == selectedAttr) {
				ret = data;
				break;
			} else if(attr.isExtension()) {
				IDCData extData = data.getData(nAttr);
				if(extData != null) {
					data.setValue(nAttr, extData);
					extDataList.add(extData);
				}
			}
			nAttr++;
		}

		int nExtData = 0;
		while(ret == null && nExtData < extDataList.size()) {
			ret = findData(extDataList.get(nExtData), selectedAttr);
			nExtData++;
		}
		
		return ret;
		
	}

	/****************************************************************************/

	public void initSearchData(IDCWebServiceContext context) {
		
		if(context.searchData == null || context.searchData.getDataType() != context.selectedType) {
			context.searchData = context.selectedType.getNewObject();
			context.searchVals = new ArrayList<String>();
			for(IDCAttribute attr : context.selectedType.getAttributes()) {
				context.searchVals.add("");
			}
			context.selectedData = context.searchData;
		}
		
	}
	
	/****************************************************************************/

	public IDCApplication getApplication() {
		return app;
	}

	/****************************************************************************/

	public String getServerPath() {
		return serverPath;
	}

	/****************************************************************************/

	public void disconnect() {
		app.disconnect();
	}
	
	/****************************************************************************/

	public void setContext(HttpServletRequest request, IDCWebServiceContext context) {
		request.getSession().setAttribute(IDCWebServiceController.SESSIONID, context);
	}
	
}