package com.indirectionsoftware.runtime.webapp;

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
import java.util.TreeMap;

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
import com.indirectionsoftware.runtime.IDCExpression;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.runtime.nlu.IDCNluResults;
import com.indirectionsoftware.runtime.nlu.nodes.IDCSentenceNode;
import com.indirectionsoftware.runtime.IDCNotificationData;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCItemId;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebApplication {
	
	private IDCApplication app;
	private String serverPath;
	
	static final String HTML_HEADERCOLOR = "<!DOCTYPE html><html><head><title>Your Data</title><script src=\"utils.js\" type=\"text/javascript\"></script><script src=\"graph.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body style=\"background-color: #d3defd;\">";
	static final String HTML_HEADER = "<!DOCTYPE html><html><head><title>Your Data</title><script src=\"utils.js\" type=\"text/javascript\"></script><script src=\"graph.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body>";
	static final String HTML_FOOTER = "</body></html>";
	
	static final String EXPLORER_TITLE = "Data Explorer";

	static final char SEP1 = ',', SEP2 = '.', SEP3 = ':';
	private static final int MAX_WIDTH = 60;
	private static final String SEARCHFIELDNAME = "searchfield";
	
	/****************************************************************************/

	public IDCWebApplication(IDCApplication app, String serverPath) {
		
		IDCUtils.traceStart("IDCWebApplication() ...");
		
		this.app = app;
		this.serverPath = serverPath;
		
		IDCUtils.traceEnd("IDCWebApplication()");
		
	}

	/****************************************************************************/

	public String process(HttpServletRequest request, IDCWebAppContext context, int action) {
		
		// Debug:
		// cd /usr/local/apache-tomcat-7.0.107/bin
		// ./catalina.sh jpda start 
		
		IDCUtils.traceStart("IDCWebApplication.process() ...");
		
		String ret = "";
		
		String errMsg = "";
		
		Map<String, IDCError> errors = new HashMap<String, IDCError>();

		if(context != null) {
			
			if(context.isUpdate) {
				
				if(action == IDCWebAppController.BACK) {
					action = IDCWebAppController.UPDATEITEMCANCEL;
				} else if(action == IDCWebAppController.UPDATESETTINGS) {
					errors = IDCWebAppSettings.updateFromHTML(this, request);
					if(errors.size() > 0) {
						action = IDCWebAppController.EDITSETTINGS;
					} else {
						action = IDCWebAppController.SETTINGS;
						context.isUpdate = false;
					}
				} else if(action == IDCWebAppController.UPDATESETTINGSCANCEL) {
					action = IDCWebAppController.SETTINGS;
					context.isUpdate = false;
				} else {
					errors = updateFromHTML(context, request, context.selectedData);
					if(errors.size() > 0 && action == IDCWebAppController.UPDATEITEMSAVE) {
						action = IDCWebAppController.UPDATEITEMREFRESH;
					}
				}

			}
			
			switch(action) {
			
				case IDCWebAppController.BACK:
				case IDCWebAppController.FORWARD:
				case IDCWebAppController.NEXTPAGE:
				case IDCWebAppController.PREVPAGE:
				case IDCWebAppController.EXPORTLIST:
					break;
					
				default:
					context.resetBrowserMap();
					break;
					
			}
			
			context.message = "";

			switch(action) {
			
				case IDCWebAppController.REPORTS:
					
					context.action = action;

					int reportId = IDCWebAppController.getIntParam(request, IDCWebAppController.TYPEID_PARM);
					if(reportId == IDCWebAppController.NA) {
						ret = getReportsHTML(context);
					} else {
						switch(reportId) {
							case 0: // Bank Statement
								ret = (String) IDCUtils.executeMethod("com.indirectionsoftware.apps.money.MoneyApp", "getBankStatement", app);
								break;
							case 1: // Forecast
								ret = (String) IDCUtils.executeMethod("com.indirectionsoftware.apps.money.MoneyApp", "getForecast", app);
								break;
						}
					}
					

					break;
					
				case IDCWebAppController.SHOWGRAPHVIEW:
					setSelectedAttr(context, request, false);
					context.stack.stackElement(context.selectedData);
					ret = getJSONGraphData(context);
					break;
					
				case IDCWebAppController.EXPORTLIST:
					setSelectedAttr(context, request, false);
					context.stack.stackElement(context.selectedData);
					ret = getCSVEntryData(context);
					break;
					
				case IDCWebAppController.IMPORT:
					
					String content = IDCWebAppController.getParam(request, IDCWebAppController.CONTENT_PARM);
		            if(content != null && content.length() > 0) {
		    			try {
		    				content = URLDecoder.decode(content,"UTF-8");
		    				app.importXML(content);
		    			} catch (UnsupportedEncodingException e) {
		    				e.printStackTrace();
		    			}
		    			
		            }
					setSelectedType(context, request);					
					setBrowser(context, context.selectedType.loadAllDataReferences(true));
					context.action = IDCWebAppController.GETTYPELIST;
					context.selectedData = null; 
					ret = getTypeListHTML(context);
					break;
					
				case IDCWebAppController.GETTYPELIST:
					
					setSelectedType(context, request);					
					setBrowser(context, context.selectedType.loadAllDataReferences(true));
					context.stack.stackElement(context.selectedType);
					context.action = action;
					context.selectedData = null; 
					ret = getTypeListHTML(context);
					break;
					
				case IDCWebAppController.GETITEMDETAILS:
					
					setSelectedData(context, request);
					context.stack.stackElement(context.selectedData);
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					if(context.selectedType.getName().equals(IDCNotificationData.NOTIFICATION_TYPE)) {
						context.selectedData.set(IDCNotificationData.NOTIFICATION_STATUS,1);
						context.selectedData.save();
					}
					break;
					
				case IDCWebAppController.UPDATEITEM:
					
					startTransaction(context);
					context.isUpdate = true;
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.UPDATEITEMREFRESH:
					
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.UPDATEITEMSAVE:
					
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
							context.action = IDCWebAppController.GETITEMDETAILS;
							context.isUpdate = false;
						}
						ret = getItemDetailsHTML(context, errors);
					}
					break;
					
				case IDCWebAppController.UPDATEITEMCANCEL:
					
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
							context.action = IDCWebAppController.GETTYPELIST;
							ret = getTypeListHTML(context);
						} else {
							context.action = IDCWebAppController.GETITEMDETAILS;
							context.selectedData.reload();
							ret = getItemDetailsHTML(context, errors);
						}
					}
					break;
					
				case IDCWebAppController.CREATEITEM:
					
					startTransaction(context); 
					context.isUpdate = true;
					context.selectedData = app.createData(context.selectedType, true);
					context.selectedType = context.selectedData.getDataType(); // in case getSuper typex
					context.stack.stackElement(context.selectedData);
					context.action = action;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.CREATECHILDITEM:
					
					setSelectedAttr(context, request);
					
					IDCWebAppContext parentContext = context;
					context = IDCWebAppContext.createChildContext(parentContext, IDCWebAppContext.CREATECHILD);
					setContext(request, context);
					
					startTransaction(context); 
					context.isUpdate = true;
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					IDCData attrData = findData(parentContext.selectedData, parentContext.selectedAttr);
					context.selectedData = app.createData(context.selectedType, attrData, parentContext.selectedAttr.getAttributeId(), true);
					context.selectedType = context.selectedData.getDataType(); // in case getSuper typex
					context.stack.stackElement(context.selectedData);
					context.action = IDCWebAppController.CREATEITEM;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.DELETEITEM:
					
					error = context.selectedData.delete(false);
					if(error != null) {
						context.message = error.getMessage();
						ret = getItemDetailsHTML(context, errors);
					} else {
						setBrowser(context, context.selectedType.loadAllDataReferences());
						context.action = IDCWebAppController.GETTYPELIST;
						context.selectedData = null; 
						ret = getTypeListHTML(context);
					}
					break;
					
				case IDCWebAppController.DELETESELECTEDITEMS:
					
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
					
				case IDCWebAppController.REMOVESELECTEDITEMS:
					
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
						
					context.action = IDCWebAppController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);

					break;
					
				case IDCWebAppController.SELECTREF:
				case IDCWebAppController.SELECTREFLIST:
					
					setSelectedAttr(context, request);
					String refListFormula = context.selectedAttr.getRefListFormula();
					parentContext = context;
					context = IDCWebAppContext.createChildContext(parentContext, IDCWebAppContext.SELECT);
					setContext(request, context);
					context.prefix = parentContext.prefix;
//					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					context.selectedType = null;
					List<IDCReference> refs = parentContext.selectedAttr.getReferences();
					if(refs.size() > 0) {
						context.selectedType = refs.get(0).getDataType(); 
					} else {
						
					}
					
					
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
					
				case IDCWebAppController.SELECTREFOK:
					
					setSelectedData(context, request);
					IDCWebAppContext childContext = context;
					context = context.parentContext;
					setContext(request, context);
					context.action = IDCWebAppController.UPDATEITEM;
					attrData = findData(context.selectedData, context.selectedAttr);
					if(attrData != null) {
						attrData.set(context.selectedAttr, childContext.selectedData);
					}
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.SELECTREFCANCEL:
				case IDCWebAppController.SELECTREFLISTCANCEL:
					
					childContext = context;
					context = context.parentContext;
					setContext(request, context);
					context.action = IDCWebAppController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.SELECTREFLISTOK:
					
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
						
					context.action = IDCWebAppController.UPDATEITEM;
					ret = getItemDetailsHTML(context, errors);

					break;
					
				case IDCWebAppController.RELOADREFTREE:
					setSelectedAttr(context, request);
					int nBox = IDCUtils.getJSPIntParam(request, "nbox");
					String valueStr = request.getParameter("selection");
					IDCDataRef ref = IDCDataRef.getRef(valueStr);
					ret = updateRefTree(context, context.prefix, nBox, ref);

					break;
					
				case IDCWebAppController.EXECUTEACTION:

					String actionIdStr = request.getParameter(IDCWebAppController.ACTIONID_PARM);
					if(actionIdStr != null) {
						long actionId = Long.parseLong(actionIdStr);
						IDCAction act = context.selectedType.getAction(actionId);
						if(act != null) {
							if(act.isUpload()) {
								content = IDCWebAppController.getParam(request, IDCWebAppController.CONTENT_PARM);
					            if(content != null && content.length() > 0) {
					    			try {
					    				content = URLDecoder.decode(content,"UTF-8");
					    			} catch (UnsupportedEncodingException e) {
					    				e.printStackTrace();
					    			}
					            }
							} else {
								content = null;
							}
							act.execute(context.selectedData, content);
						}
					}
					if(!context.selectedData.isNew()) {
						context.selectedData.reload();
					}
					ret = getItemDetailsHTML(context, errors);
					break;
					
				case IDCWebAppController.SETTINGS:
					
					context.action = action;
					context.stack.stackElement(new IDCURL("Settings", IDCURL.SETTINGS));
					ret = getSettingsHTML(context);
					break;
					
				case IDCWebAppController.EDITSETTINGS:
					
					context.action = action;
					context.isUpdate = true;
					context.stack.stackElement(new IDCURL("Settings", IDCURL.SETTINGS));
					ret = getSettingsHTML(context);
					break;
					
				case IDCWebAppController.TODO:
					
					context.action = action;
					context.stack.stackElement(new IDCURL("Todo", IDCURL.TODO));
					ret = getTodoHTML(context);
					break;
					
				case IDCWebAppController.TOGGLETODO:
					
					context.action = IDCWebAppController.TODO;
					context.isTodoActive = !context.isTodoActive;
					ret = getTodoHTML(context);
					break;
					
				case IDCWebAppController.BACK:
				case IDCWebAppController.FORWARD:

					IDCUtils.debug("Processing BACK/FORWARD");
					
					if(action == IDCWebAppController.BACK && context.action == IDCWebAppController.TYPESEARCH || action == IDCWebAppController.POSTSEARCH) {
						
						setBrowser(context, context.selectedType.loadAllDataReferences());
						context.action = IDCWebAppController.GETTYPELIST;

					} else {
						
						IDCEnabled data = moveStack(context, action);
						if(data != null) {
							if(data.isData()) {
								context.action = IDCWebAppController.GETITEMDETAILS;
								context.selectedType = ((IDCData)data).getDataType();
								context.selectedData = ((IDCData)data).getDataType().loadDataObject(((IDCData)data).getId());
								ret = getItemDetailsHTML(context, errors);
							} else if(data.isType()) {
								context.action = IDCWebAppController.GETTYPELIST;
								context.selectedType = (IDCType) data;
								context.selectedData = null; 
								if(context.selectedType != context.browser.type) {
									setBrowser(context, context.selectedType.loadAllDataReferences(true));
								}
								ret = getTypeListHTML(context);
							} else if(data instanceof IDCURL) {
								switch(((IDCURL) data).getType()) {
									case IDCURL.HOME:
										ret = getHomeHTML(context);
										break;
									case IDCURL.TODO:
										context.action = IDCWebAppController.TODO;
										ret = getTodoHTML(context);
										break;
									case IDCURL.SETTINGS:
										context.action = IDCWebAppController.SETTINGS;
										ret = getSettingsHTML(context);
										break;
								}
							} else if(data.isNluResults()) {
								ret = ((IDCNluResults)data).getFullHTML();
							}
						} else {

//							int nextPage=ITEMPAGE;

							switch(context.type) {

								case IDCWebAppContext.SELECT:
									setBrowser(context, context.selectedType.loadAllDataReferences());
									break;
								
								case IDCWebAppContext.CREATECHILD:
									setBrowser(context, context.parentContext.selectedData.getRefList(context.parentContext.selectedAttr.getAttributeId()));
									break;
									
							}
							
						}

					}
					break;
					
				case IDCWebAppController.SEARCH:
					IDCUtils.debug("Processing SEARCH");
					String term = IDCUtils.getJSPParam(request, SEARCHFIELDNAME);
					if(term.length() > 0) {
						context.initSearchVals(term);						
						setBrowser(context, searchFromHTML(context));
					}
					break;
					

					
				/********************************************************************************************************/					
					
				case IDCWebAppController.SORTLIST:
					int attrId = getSortAttrId(request); 
					if(context.action == IDCWebAppController.GETTYPELIST) {
						context.browser.sort(attrId);
						ret = getTypeListHTML(context);
					} else if(context.action == IDCWebAppController.GETITEMDETAILS) {
						ret = getItemDetailsHTML(context, errors);
					}
					break;
					
				case IDCWebAppController.TYPESEARCH:
					IDCUtils.debug("Processing SEARCH");
					initSearchData(context);
					setBrowser(context, context.selectedType.loadAllDataReferences());
					break;
					
				case IDCWebAppController.POSTSEARCH:
					IDCUtils.debug("Processing POSTSEARCH");
//					error = updateFromHTML(context, request);
//					if(error != null) {
//						context.message = error.getMessage();
//					}
					setBrowser(context, searchFromHTML(context));
					break;
					
				case IDCWebAppController.REMOVECONTEXT:
					IDCUtils.debug("Processing REMOVECONTEXT");
//					contexts.remove(contextId);
					break;
					
				case IDCWebAppController.CLOSECONTEXT:
					IDCUtils.debug("Processing CLOSECONTEXT");
//					contexts.remove(contextId);
					break;

				case IDCWebAppController.UPDATEDOMAIN:
					IDCUtils.debug("Processing UPDATEDOMAIN");
					setSelectedAttr(context, request);
					String selectedValue = IDCUtils.getJSPParam(request, "value");
					IDCUtils.debug("SelectedAttr = " + context.selectedAttr + " / selectedValue = " + selectedValue);
					updateFromHTML(context, context.selectedData, context.selectedAttr.getAttributeId(), selectedValue);
					break;
					
				case IDCWebAppController.UPDATENAMESPACE:
					IDCUtils.debug("Processing UPDATENAMESPACE");
					setSelectedAttr(context, request);
					parentContext = context;
					context = IDCWebAppContext.createChildContext(parentContext, IDCWebAppContext.CREATECHILD);
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					setBrowser(context, parentContext.selectedData.getRefList(parentContext.selectedAttr.getAttributeId()));
					break;
					
				case IDCWebAppController.UPDATELIST:
					IDCUtils.debug("Processing UPDATELIST");
					setSelectedAttr(context, request);
					parentContext = context;
					context = IDCWebAppContext.createChildContext(parentContext, IDCWebAppContext.CREATECHILD);
					context.selectedType = parentContext.selectedAttr.getReferences().get(0).getDataType();
					setBrowser(context, parentContext.selectedData.getRefList(parentContext.selectedAttr.getAttributeId()));
					break;
					
				case IDCWebAppController.HELP:
					IDCUtils.debug("Processing HELP");
					break;
					
				case IDCWebAppController.NEXTPAGE:
					IDCUtils.debug("Processing NEXTPAGE");
					if(context.action == IDCWebAppController.GETTYPELIST) {
						context.browser.setNextPage();
						ret = getTypeListHTML(context);
					} else {
						IDCAttribute attr = getRequestAttr(context, request);
						IDCDatabaseTableBrowser browser = context.getNextBrowserPage(attr);
						if(browser != null) {
							ret = getItemDetailsHTML(context, errors);
						}
						
					}
					break;
					
				case IDCWebAppController.PREVPAGE:
					IDCUtils.debug("Processing PREVPAGE");
					if(context.action == IDCWebAppController.GETTYPELIST) {
						context.browser.setPrevPage();
						ret = getTypeListHTML(context);
					} else {
						IDCAttribute attr = getRequestAttr(context, request);
						IDCDatabaseTableBrowser browser = context.getPrevBrowserPage(attr);
						if(browser != null) {
							ret = getItemDetailsHTML(context, errors);
						}
						
					}
					break;
					
				case IDCWebAppController.SPEAK:
					
					String query = IDCWebAppController.getParam(request, IDCWebAppController.CONTENT_PARM);
		            if(query != null && query.length() > 0) {
		    			try {
		    				query = URLDecoder.decode(query,"UTF-8");
		    			} catch (UnsupportedEncodingException e) {
		    				e.printStackTrace();
		    			}
		    			
		            }
					ret = processSpeech(context, query);
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
		
		IDCUtils.traceEnd("IDCWebApplication.process()");
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getFullPage(IDCWebAppContext context) {
		
		String ret = HTML_HEADER;
		
		ret += getNavDivHTML(context);
		
		ret += "<h1 class=\"apptitle\" style=text-align:center;>" + app.getDisplayName() + "</h1>";
		
		ret += "<div id=\"main\">";

		ret += "<div id=\"_content\"><p></p>";
		ret += getHomeHTML(context);
		ret += "</div>";

		ret += "</div>";
		ret += HTML_FOOTER;
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getHomeHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		ret += "<h1>Welcome to " + app.getDisplayName() + "</h1>";
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getTodoHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		ret += "<ul class=\"actionsbar\">";
		ret += "<li>" + getURLButton(context, "actionbut", (context.isTodoActive ? "Show All" : "Show Active"), IDCWebAppController.TOGGLETODO, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "</ul>";


		ret += "<table><colgroup><col width='80'/><col width='150'/></colgroup>";
		
		boolean found=false;
		for(Entry<IDCType, List<IDCData>> entry : app.getTodoTypeList(context.isTodoActive, true).entrySet()) {
			ret += "<tr><td>" + IDCUtils.getPlural(entry.getKey().getDisplayName()) + "</td><td></td></tr>";
			ret += "<ul class=\"nested\">";
			for(IDCData todoData : entry.getValue()) {
				ret += "<tr><td></td><td>" + getURLButton(context, "linkbut", todoData.getName(), IDCWebAppController.GETITEMDETAILS, entry.getKey().getId(), todoData.getId(), IDCWebAppController.NA, "", true, true) + "</td></tr>";
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

	public String getSettingsHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		if(context.isUpdate) {
			ret += "<form name=\"UpdateItemDetails\">";
			context.onKeyPress = " onKeyPress=\"if(event.key == 'Enter') {" + getClickUpdateFunction(app.getName(), IDCWebAppController.UPDATESETTINGS, IDCWebAppController.NA, IDCWebAppController.NA, true, "" + IDCWebAppController.NA) + "}\"";
		}
		
		ret += "<table>";
		
		ret += "<tr><td>Start Date</td><td>";
		
		String onKeyPress = " onKeyPress=\"if(event.key == 'Enter') {" + IDCWebApplication.getClickUpdateFunction(app.getApplication().getName(), IDCWebAppController.UPDATESETTINGS, IDCWebAppController.NA, IDCWebAppController.NA, true, "" + IDCWebAppController.NA) + "}\"";
		
		if(context.isUpdate) {
			
			String dateStr = "";
			if(IDCWebAppSettings.startDate != -1) {
				dateStr = IDCCalendar.getCalendar().displayDateShort(IDCWebAppSettings.startDate);
			}
			ret += "<input id=startDate" + onKeyPress + " name='startDate' class='text' type='text' value='" + dateStr + "' maxlength='20' size='15' />";
		
		} else {

			if(IDCWebAppSettings.startDate == -1) {
				ret += "none";
			} else {
				ret += IDCUtils.getDateString(IDCWebAppSettings.startDate);
			}
		
		}
		
		ret += "</td><tr><td>End Date</td><td>";
		
		if(context.isUpdate) {
			
			String dateStr = "";
			if(IDCWebAppSettings.endDate != -1) {
				dateStr = IDCCalendar.getCalendar().displayDateShort(IDCWebAppSettings.endDate);
			}
			ret += "<input id=endDate" + onKeyPress + " name='endDate' class='text' type='text' value='" + dateStr + "' maxlength='20' size='15' />";
		
		} else {

			if(IDCWebAppSettings.endDate == -1) {
				ret += "none";
			} else {
				ret += IDCUtils.getDateString(IDCWebAppSettings.endDate);
			}

		}
		
		ret += "</td><tr><td>Page Size</td><td>";
		
		if(context.isUpdate) {
			ret += "<input id=endDate" + onKeyPress + " name='pageSize' class='text' type='text' value='" + IDCWebAppSettings.pageSize + "' maxlength='20' size='15' />";
		} else {
			ret += IDCWebAppSettings.pageSize;
		}
		
		ret += "</td></tr></table>";
		
		if(context.isUpdate) {
			ret += "</form>";
		}
				
		if(context.isUpdate) {
			ret += "<ul class=\"detailsbutpanel\">";
			ret += "<li>" + getURLButton(context,  null, "Save", IDCWebAppController.UPDATESETTINGS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
			ret += "<li>" + getURLButton(context,  null, "Cancel", IDCWebAppController.UPDATESETTINGSCANCEL, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
			ret += "</ul>";
		}
		
		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";
		
		return ret;
				
	}

	/****************************************************************************/

	public String updateSettings(IDCWebAppContext context, HttpServletRequest request) {
		
		IDCWebAppSettings.updateFromHTML(this, request);
		
		String ret = getSettingsHTML(context);
		
		return ret;
		
	}

	/****************************************************************************/

	public String getReportsHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
				
		ret += "<p>" + getURLButton(context, null, "Bank Statement", IDCWebAppController.REPORTS, 0, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "<p>" + getURLButton(context, null, "Forecast", IDCWebAppController.REPORTS, 1, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";
		
		return ret;
				
	}

	/****************************************************************************/

	public String getNavDivHTML(IDCWebAppContext context) {
		
		String ret = "<div id=\"explorer\" class=\"explorer\"><a href=\"javascript:void(0)\" class=\"closebtn\" onclick=\"closeNav()\">&times;</a>"; 
		
		ret += "<ul>" + app.getDisplayName();
		
		for(IDCPackage pack : app.getPackages(false)) {
			ret += "<li><span class=\"caret\" onclick=\"toggleNav(this);\">" + pack.getDisplayName() + "</span>";
			ret += "<ul class=\"nested\">";
			for(IDCType type : pack.getTypes()) {
				if(type.isTopLevelViewable()) {
					ret += "<li>" + getURLButton(context, null, IDCUtils.getPlural(type.getDisplayName()), IDCWebAppController.GETTYPELIST, type.getId(), IDCWebAppController.NA, IDCWebAppController.NA, "", true, true) + "</li>";
				}
			}
			ret += "</ul></li>";
		}
		
		ret += "</ul>";
		
		ret += "</div><button class=\"openbtn\" onclick=\"openNav()\">&#9776; " + EXPLORER_TITLE + "</button>\r\n"; 

		return ret;
		
	}

	/****************************************************************************/

	public String getButtonsDivHTML(IDCWebAppContext context) {	
		
		String ret = "<ul class=\"menubar\">";

		ret += "<li>" + getURLButton(context, null, "Back", IDCWebAppController.BACK, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", context.stack.isBackOk(context.type == IDCWebAppContext.ROOT), false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Forward", IDCWebAppController.FORWARD, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", context.stack.isForwardOk(), false) + "</li>";
		
		int action = -1;
		if(context.selectedData != null) {
			action = IDCWebAppController.DELETEITEM;
		} else if(context.selectedType != null) {
			action = IDCWebAppController.DELETESELECTEDITEMS;
		}
		if(action != -1) {
			ret += "<li>" + getURLButton(context, null, "Delete", action, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		}

		if(context.selectedType != null) {
			ret += "<li>" + getURLButton(context, null, "New", IDCWebAppController.CREATEITEM, context.selectedType.getId(), IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		}

		
		
		if(context.action == IDCWebAppController.SETTINGS) {
			ret += "<li>" + getURLButton(context, null, "Edit", IDCWebAppController.EDITSETTINGS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		} else if(context.selectedData != null) {
			ret += "<li>" + getURLButton(context, null, "Edit", IDCWebAppController.UPDATEITEM, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebAppController.NA, "", true, false) + "</li>";
		}
		
		ret += "<ul class=\"menubar-right\">";
		ret += getSearchButton(context);
		ret += "<li>" + getURLButton(context, null, "Speak", IDCWebAppController.SPEAK, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Import", IDCWebAppController.IMPORT, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += getExportButton(context);
		ret += "<li>" + getURLButton(context, null, "Reports", IDCWebAppController.REPORTS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Todo" + getTodoNum(), IDCWebAppController.TODO, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLButton(context, null, "Settings", IDCWebAppController.SETTINGS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "<li>" + getURLLink(context, "Logout", IDCWebAppController.LOGOFF, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		ret += "</ul>";

		ret += "</ul>";

		return ret;

	}

	/****************************************************************************/

	private String getSearchButton(IDCWebAppContext context) {
		
		String ret = "<li><form name='SearchForm'><input name='" + SEARCHFIELDNAME + "' placeholder='Search ...' value='" + context.searchVal + "' onKeyPress=\"if(event.key == 'Enter') {" + getClickUpdateFunction(app.getName(), IDCWebAppController.SEARCH, IDCWebAppController.NA, IDCWebAppController.NA, true, "" + IDCWebAppController.NA) + "}\"/></form></li>";
		ret += "<li>" + getURLButton(context, null, "Search", IDCWebAppController.SEARCH, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";

		return ret;
		
	}

	
	/****************************************************************************/

	private String getExportButton(IDCWebAppContext context) {
		
		String ret = "<li>" + getURLButton(context, null, "Export", IDCWebAppController.EXPORTLIST, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";

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

	public String getGraphViewHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"canvasParent\" class=\"canvasParent\"><canvas id=\"canvas\" class=\"canvas\" width=\"1409\" height=\"715\">You're browser doesn't support HTML5 Canvas :(</canvas><ul id=\"popuppanel\" class=\"popuppanel\"></ul></div>";
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String processSpeech(IDCWebAppContext context, String query) {
		
		IDCNluResults results = app.getNLUEngine().processSentence(query, context);
		context.stack.stackElement(results);

		return results.getFullHTML();
		
	}
	
	/****************************************************************************/

	public String getTypeListHTML(IDCWebAppContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		if(context.browser.getMaxPageNumber() > 0) {
			ret += "<ul class=\"listbutpanel\">";
			ret += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, context.prefix, true, false) + "</li>";
			ret += "</ul>";
		}
		
		ret += getTableHTML(context, context.prefix, context.selectedType, context.browser, context.type == IDCWebAppContext.ROOT || context.action == IDCWebAppController.SELECTREFLIST);
		
		
		ret += "<ul class=\"listbutpanel\">";
		
		if(context.action == IDCWebAppController.SELECTREFLIST) {
			ret += "<li>" + getURLButton(context, null, "Ok", IDCWebAppController.SELECTREFLISTOK,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Cancel", IDCWebAppController.SELECTREFLISTCANCEL, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		} else if(context.action == IDCWebAppController.SELECTREF) { 
			ret += "<li>" + getURLButton(context, null, "Cancel", IDCWebAppController.SELECTREFCANCEL, context.selectedType.getId(), IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</li>";
		}
		
		if(context.browser.getMaxPageNumber() > 0) {
			ret += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, context.prefix, true, false) + "</li>";
			ret += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, context.prefix, true, false) + "</li>";
		}

		ret += "</ul>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";

		return ret;
		
	}
	
	/****************************************************************************/

	public String getTableHTML(IDCWebAppContext context, List<Integer> attrIdList, int attrId, IDCType type, IDCDatabaseTableBrowser browser, boolean isTickNeeded) {
		return getTableHTML(context, getAttributeIdString(attrIdList, attrId), type, browser, isTickNeeded);
	}

	public String getTableHTML(IDCWebAppContext context, String prefix, IDCType type, IDCDatabaseTableBrowser browser, boolean isTickNeeded) {
	
		String ret = null;
		
		String colheader = "<table><colgroup>";
		String header = "<tr>";
		
		List<IDCData> list = browser.getPage();
		
		if(list.size() > 0) {
			type = list.get(0).getDataType();
		}
		
		colheader += (isTickNeeded ? "<col width='20'/>" : "") + "<col width='200'/>";
		if(prefix.length() > 0) {
			header += (isTickNeeded ? (list.size() > 0 ? "<th><input name=\"" + prefix + "master$box\" type=\"checkbox\" onclick=\"toggleChildren(this,'" + prefix + "');\"></th>" : "<th></th>") : "");
		} else {
			header += (isTickNeeded ? (list.size() > 0 ? "<th><input name=\"master$box\" type=\"checkbox\" onclick=\"toggleList(this);\"></th>" : "<th></th>") : "");
		}

//		header += "<th>" + getURLButton(context, "linkbut", "Name", IDCWebAppController.SORTLIST, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</th>";
		header += "<th>Name</th>";


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
				ret += "<td>" + getURLButton(context, "linkbut", data.getName(), IDCWebAppContext.CONTEXTSELECTQUERY[context.type], data.getDataType().getId(), data.getId(), IDCWebAppController.NA, "", true, false) + "</td>";
				for(IDCAttribute refAttr : type.getListAttributes()) {
					if(!refAttr.getName().equals("Name") && !refAttr.getName().equals(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME)) {
						ret += (refAttr.getAttributeType() == IDCAttribute.PRICE ? "<td style='text-align:right'>" : "<td>");
						ret += getAttributeListHTML(context, data, refAttr.getAttributeId(), refAttr) + "</td>";
					}		
				}
				ret += "</tr>";
//				even = !even;
		     }
			
			if(context.action == IDCWebAppController.SELECTREF) { 
				ret += "<tr " + (even ? "style=background-color:lightgrey;" : "") + ">";
				ret += "<td>" + getURLButton(context, "linkbut", "(none)", IDCWebAppContext.CONTEXTSELECTQUERY[context.type], IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, "", true, false) + "</td>";
			} else {
				ret += browser.getFooterHTML();
			}
			
		} else {
			ret += "<tr>" + (isTickNeeded ? "<td></td>" : "") + "<td>No data ...</td></tr>";
		}
	
		ret += "</table></div>";

		return ret;
		
	}

	/************************************************************************************************/

    public String getAttributeListHTML(IDCWebAppContext context, IDCData data, int attrId, IDCAttribute attr) {
    	
    	IDCUtils.traceStart("getDisplayHTML()");
    	
    	String ret = null; 
    	
    	switch(attr.getAttributeType()) {
		
			case IDCAttribute.REF:
			case IDCAttribute.REFBOX:
			case IDCAttribute.REFTREE:

				IDCUtils.debug("getDisplayHTML REF");
				
				IDCData refVal = data.getData(attrId);
				IDCUtils.debug("getAttributeListHTML refVal="+refVal);
				
				if(refVal != null) {
					ret = data.getDisplayValue(attrId);
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
				if(ret.length() > MAX_WIDTH) {
					ret = ret.substring(0, MAX_WIDTH) + " ...";
				}
				break;

		}
    	
    	IDCUtils.traceEnd("getDisplayHTML()");
    	
		return ret;

    }
    
	/****************************************************************************/

	public String getItemDetailsHTML(IDCWebAppContext context, Map<String, IDCError> errors) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";
		
		if(!context.isUpdate) {
			ret += getActionsDivHTML(context);
		}
		
		if(context.isUpdate) {
			ret += "<form name=\"UpdateItemDetails\">";
			context.onKeyPress = " onKeyPress=\"if(event.key == 'Enter') {" + getClickUpdateFunction(app.getName(), IDCWebAppController.UPDATEITEMSAVE, context.selectedType.getId(), context.selectedData.getId(), true, "" + IDCWebAppController.NA) + "}\"";
		}
		
		List<String> panelsHTML = getItemPanelsMap(context, context.selectedData, new ArrayList<Integer>(), errors);
		
		for(String html : panelsHTML) {
			ret += html;
		}
		
		if(context.isUpdate) {
			ret += "<ul class=\"detailsbutpanel\">";
			ret += "<li>" + getURLButton(context,  null, null, IDCWebAppController.UPDATEITEMSAVE, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebAppController.NA, "", true, false) + "</li>";
			ret += "<li>" + getURLButton(context,  null, "Cancel", IDCWebAppController.UPDATEITEMCANCEL, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebAppController.NA, "", true, false) + "</li>";
			ret += "</ul>";
		}
		
		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";
		
		return ret;
		
	}
	
	/****************************************************************************/

	private String getActionsDivHTML(IDCWebAppContext context) {

		
		String ret = "<ul class=\"actionsbar\">";
		
		for(IDCAction action : context.selectedType.getGUIActions(false)) {
			int act = IDCWebAppController.EXECUTEACTION;
			if(action.isUpload()) {
				act = IDCWebAppController.EXECUTEACTIONUPLOAD;
			}
			ret += "<li>" + getURLButton(context, "actionbut", action.getName(), act, IDCWebAppController.NA, IDCWebAppController.NA, action.getId(), "", context.selectedData.isEditable(action), false) + "</li>";
		}

		ret += "</ul>";

		return ret;

	}

	/****************************************************************************/

	public List<String> getItemPanelsMap(IDCWebAppContext context, IDCData data, List<Integer> attrList, Map<String, IDCError> errors) {
		
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

    public List<String> getAttributeDisplayHTMLMap(IDCWebAppContext context, IDCData data, List<Integer> attrList, int nAttr, IDCAttribute attr, boolean isSearch, Map<String, IDCError> errors) {
    	
    	IDCUtils.traceStart("getDisplayHTML()");
    	
    	List<String> ret = new ArrayList<String>();
    	
    	List<String> temp = new ArrayList<String>();
    	
    	IDCError error = errors.get(data.getAsParentRef(nAttr).toString());
    	
    	String rootHtml =  "<tr><td valign=\"top\">" + attr.getDisplayName() + "</td><td>"; 
    	
    	switch(attr.getAttributeType()) {
		
			case IDCAttribute.REF:
			case IDCAttribute.REFBOX:
			case IDCAttribute.REFTREE:

				IDCData refVal = data.getData(nAttr);
				IDCUtils.debug("getDisplayHTML refVal="+refVal);
				
				if(refVal != null) {
					
					if(context.isUpdate) {
						rootHtml += refVal.getName();
					} else {
						rootHtml += getURLButton(context, "linkbut", refVal.getName(), IDCWebAppController.GETITEMDETAILS, refVal.getDataType().getId(), refVal.getId(), IDCWebAppController.NA, "", true, false);
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

				IDCType type = attr.getReferences().get(0).getDataType();

				IDCDatabaseTableBrowser browser = context.getBrowser(attr);
				if(browser == null) {
					List<IDCDataRef> list = data.getRefList(nAttr);
					browser = context.getBrowser(attr, type, list);
				}

				rootHtml += "<ul class=\"insidelistbutpanel\">";
				if(browser.getMaxPageNumber() > 0) {
					rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
					rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
				}
				rootHtml += "<li>" + getURLButton(context,  null, "Graph", IDCWebAppController.SHOWGRAPHVIEW, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
				rootHtml += "<li>" + getURLButton(context,  null, "Export", IDCWebAppController.EXPORTLIST, context.selectedData.getDataType().getId(), context.selectedData.getId(), IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
				rootHtml += "</ul>";


				rootHtml += getTableHTML(context, attrList, nAttr, type, browser, false);
				
				if(browser.getMaxPageNumber() > 0) {
					rootHtml += "<ul class=\"insidelistbutpanel\">";
					rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
					rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, nAttr), true, false) + "</li>";
					rootHtml += "</ul>";
				}

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

    public List<String> getAttributeUpdateHTMLMap(IDCWebAppContext context, IDCData data, List<Integer> attrList, int attrId, IDCAttribute attr, boolean isSearch, Map<String, IDCError> errors) {
    	
    	IDCUtils.traceStart("getUpdateHTML()");
    	
    	List<String> ret = new ArrayList<String>();
    	
    	List<String> temp = new ArrayList<String>();
    	
    	String rootHtml = "<tr><td valign=\"top\">" + attr.getDisplayName() + "</td><td>";
    	
    	String displayVal = "" + data.getDisplayValue(attrId, context.isUpdate);
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
    				refs = refType.loadAllDataObjects(refType.loadAllDataReferences());
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
					rootHtml  += getURLButton(context, "linkbut", displayValue, IDCWebAppController.SELECTREF, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false);
				}
				break;
	
			case IDCAttribute.REFTREE:
//				IDCDataRef ref = (IDCDataRef) data.getRawValue(nAttr);
				IDCDataRef ref = data.getDataRef(attrId);
				IDCRefTree tree = new IDCRefTree(app, attr, ref);
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
       			
				rootHtml += "<select id=IDCField" + context.fieldId++ + " onchange=\"" + getClickUpdateFunction(app.getName(), IDCWebAppController.UPDATEITEMREFRESH, context.selectedType.getEntityType(), context.selectedData.getId(), true, "" + IDCWebAppController.NA) + "\" class='dropdown' name='" + getFieldName(data, attrId) + "'>";
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
				
				IDCType type = attr.getReferences().get(0).getDataType();

				IDCDatabaseTableBrowser browser = context.getBrowser(attr);
				if(browser == null) {
					List<IDCDataRef> list = data.getRefList(attrId);
					browser = context.getBrowser(attr, type, list);
				}

				if(browser.getMaxPageNumber() > 0) {
					rootHtml += "<ul class=\"insidelistbutpanel\">";
					rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</ul>";
				}

				rootHtml += getTableHTML(context, attrList, attrId, type, browser, true);
				
				if(attr.getAttributeType() == IDCAttribute.LIST) {
					
					rootHtml += "<ul class=\"insidelistbutpanel\">";
					if(browser.getMaxPageNumber() > 0) {
						rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
						rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					}
					rootHtml  += "<li>" + getURLButton(context, null, "Update", IDCWebAppController.SELECTREFLIST, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml  += "<li>" + getURLButton(context, null, "Remove", IDCWebAppController.REMOVESELECTEDITEMS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</il>";
					
				} else if(attr.getAttributeType() == IDCAttribute.NAMESPACE) {
					rootHtml += "<div class=\"insidelistbutpanel\">";
					if(browser.getMaxPageNumber() > 0) {
						rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
						rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					}
					rootHtml  += "<li>" + getURLButton(context, null, "New", IDCWebAppController.CREATECHILDITEM, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml  += "<li>" + getURLButton(context, null, "Delete", IDCWebAppController.REMOVESELECTEDITEMS, IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</div>";
				} else {
					rootHtml += "<ul class=\"insidelistbutpanel\">";
					rootHtml += "<li>" + getURLButton(context, null, "Prev", IDCWebAppController.PREVPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "<li>" + getURLButton(context, null, "Next", IDCWebAppController.NEXTPAGE,  IDCWebAppController.NA, IDCWebAppController.NA, IDCWebAppController.NA, getAttributeIdString(attrList, attrId), true, false) + "</li>";
					rootHtml += "</ul>";
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
    
	String getURLLink(IDCWebAppContext context, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) {
		return "<a href=\"IDCWebAppController?" + IDCWebAppController.ACTION_PARM + "=" + action + "&" + IDCWebAppController.APPID_PARM + "=" + app.getName() + "&" + IDCWebAppController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebAppController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebAppController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebAppController.ACTIONID_PARM + "=" + actionId+ "\">" + label + "</a>";
	}
		
    /****************************************************************************/
    
	public String getURLButton(IDCWebAppContext context, String className, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) throws Error {
		return getURLButton(context, className, label, action, typeId, itemId, actionId, attrIdStr, isActive, isCloseNav, null);
	}	
	String getURLButton(IDCWebAppContext context, String className, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav, String imageName) throws Error {
	
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
		
		boolean isUpload = false;
		
		if(action == IDCWebAppController.EXECUTEACTIONUPLOAD) {
			action = IDCWebAppController.EXECUTEACTION;
			isUpload = true;
		}
		
		String query = "'IDCWebAppController?" + IDCWebAppController.ACTION_PARM + "=" + action + "&" + IDCWebAppController.APPID_PARM + "=" + app.getName() + "&" + IDCWebAppController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebAppController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebAppController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebAppController.ACTIONID_PARM + "=" + actionId+ "'";
				
		if(action == IDCWebAppController.REMOVESELECTEDITEMS || action == IDCWebAppController.SELECTREFLISTOK) {
			func += "processSelectedChildrenItems(" + query + ", '" + attrIdStr + "');";
		} else if(action == IDCWebAppController.DELETESELECTEDITEMS) {
			func += "processSelectedListItems(" + query + ");";
		} else if(action == IDCWebAppController.IMPORT) {
			func += "selectFile(" + query + ");";
		} else if(action == IDCWebAppController.SPEAK) {
			func += "speak(" + query + ");";
		} else if(action == IDCWebAppController.SHOWGRAPHVIEW) {
			func += "showGraph(" + query + ");";
		} else if(action == IDCWebAppController.EXPORTLIST) {
			func += "exportList(" + query + ");";
		} else if(action == IDCWebAppController.EXECUTEACTION && isUpload) {
			func += "selectFile(" + query + ");";
		} else if(context.isUpdate) {
			if(label == null) {
				func += getClickUpdateFunction(app.getName(), action, typeId, itemId, isActive, "" + IDCWebAppController.NA);
				label = "Update";
				type = "type = \"submit\"";
			} else {
//				func += "reloadPost('IDCWebAppController'," + action + "," + typeId + "," + itemId + ",'" + attrIdStr + "'); return false;"; 
				func += getClickUpdateFunction(app.getName(), action, typeId, itemId, isActive, "'" + attrIdStr + "'");
			}
		} else {
			func += "reloadGet(" + query + ");";
		}
				
		if(isActive) {
			ret += type + "onclick=\"" + func + "\""; 
		} else {
			ret += "disabled"; 
		}
		
		if(imageName != null) {
			label = "<img src=\"" + imageName + "\">";
		}
		ret += ">" + label + "</button>"; 
				
		
		return ret;
		
		
	}

    /****************************************************************************/
    
	public static String getLink(IDCApplication app, String label, long typeId, long itemId) throws Error {
		return getLink(app, label, IDCWebAppController.GETITEMDETAILS, typeId, itemId);
	}

    /****************************************************************************/
    
	public static String getLink(IDCApplication app, String label, int action, long typeId, long itemId) throws Error {
		String query = "'IDCWebAppController?" + IDCWebAppController.ACTION_PARM + "=" + action + "&" + IDCWebAppController.APPID_PARM + "=" + app.getName() + "&" + IDCWebAppController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebAppController.ITEMID_PARM + "=" + itemId + "'";
		
		return "<button class=\"linkbut\" onclick=\" event.preventDefault(); reloadGet(" + query + ");\">" + label + "</button>";

	}

    /****************************************************************************/
	 
	static String getClickUpdateFunction(String appId, int action, long typeId, long itemId, boolean isActive, String attrIdStr) throws Error {
		return "event.preventDefault(); reloadPost('IDCWebAppController', '" + appId + "', " + action + "," + typeId + "," + itemId + "," + attrIdStr + "); return false;";
	}

	/****************************************************************************/

	public String getPageTitle(IDCWebAppContext context) {

		String ret = null;
		
		switch(context.action) {
		
			case IDCWebAppController.REPORTS:
				ret = "Reports:";
				break;
			
			case IDCWebAppController.SETTINGS:
				ret = "Settings:";
				break;
			
			case IDCWebAppController.EDITSETTINGS:
				ret = "Edit Settings:";
				break;
			
			case IDCWebAppController.UPDATESETTINGS:
				ret = "Update Settings:";
				break;
			
			case IDCWebAppController.TODO:
				ret = "Todo List:";
				break;
			
			case IDCWebAppController.GETTYPELIST:
				ret = "List " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				if(context.browser.getMaxPageNumber() > 0) {
					ret += " - Page " + (context.browser.getPageNumber() + 1) + " of " + (context.browser.getMaxPageNumber() + 1);
				}
				break;
				
			case IDCWebAppController.POSTSEARCH:
				ret = "Search Results for " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebAppController.GETITEMDETAILS:
			case IDCWebAppController.UPDATEITEMSAVE:
			case IDCWebAppController.EXECUTEACTION:
				ret = "Browse " + context.selectedType.getDisplayName() + ": " + context.selectedData.getName() + " - " + context.selectedData.getDataRef();
				break;
				
			case IDCWebAppController.CREATEITEM:
			case IDCWebAppController.CREATECHILDITEM:
				ret = "New " + context.selectedType.getDisplayName();
				break;
				
			case IDCWebAppController.UPDATEITEM:
				ret = "Update " + context.selectedType.getDisplayName() + ": " + context.selectedData.getName();
				break;
				
			case IDCWebAppController.TYPESEARCH:
			case IDCWebAppController.SEARCH:
				ret = "Search " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebAppController.SELECTREF:
				ret = "Select a " + context.selectedType.getDisplayName();
				break;
				
			case IDCWebAppController.SELECTREFLIST:
				ret = "Select " + IDCUtils.getPlural(context.selectedType.getDisplayName());
				break;
				
			case IDCWebAppController.UPDATENAMESPACE:
			case IDCWebAppController.UPDATELIST:
				ret = "Manage " + context.parentContext.selectedAttr.getDisplayName();
				break;
				
			case IDCWebAppController.SPEAK:
				ret = "Results for: " + context.nluQuery;
				break;
				
			default:
				break;
				
	}
		
		return ret;
		
	}

	/************************************************************************************************/

    public void resetHTMLFieldId(IDCWebAppContext context) {
    	context.fieldId = 0;
    }
    
    /************************************************************************************************/

    public String getHTMLAttributeInfo(IDCWebAppContext context, IDCData data, int nAttr) {
    	
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

    public String getSearchHTML(IDCWebAppContext context, int nAttr) {
    	
    	String ret = null;
    	
    	IDCType type = context.selectedType;
    	IDCAttribute attr = type.getAttribute(nAttr);
    	
//  		ret = getUpdateHTML(context, context.selectedData, nAttr, attr, false, true);

    	return ret;
    	
    }
    

    /************************************************************************************************/

	public IDCError save(IDCWebAppContext context, IDCData data) {
		
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

	public Map<String, IDCError> updateFromHTML(IDCWebAppContext context, HttpServletRequest request, IDCData data) {
		
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

	public void updateFromHTMLOLD(IDCWebAppContext context, HttpServletRequest request, IDCData data) {
		
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

    public void updateFromHTML(IDCWebAppContext context, IDCData data, int nAttr, String valueStr) {
    	
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
				if(ref != null && ref.getItemId() == -1) {
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
				long date = app.getCalendar().getDate(valueStr);
				data.set(nAttr, date);
				break;

			default:
				data.set(nAttr, valueStr);
				break;

		}

    }
    
    /************************************************************************************************/

    public List<IDCDataRef> searchFromHTML(IDCWebAppContext context) {
    	
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

    public List<IDCDataRef> search(IDCWebAppContext context) {
    	
    	List<IDCDataRef> ret = new ArrayList<IDCDataRef>();

    	for(IDCType type : context.webApp.app.getTypes()) {
    		
        	for(IDCDataRef ref : type.loadAllDataReferences()) {
        		
        		IDCData data = context.selectedType.loadDataRef(ref);
        		
    			IDCUtils.debug("IDCWebApplication.search(): data = " + data);

        		boolean isMatchFound=true;
        		
        		int nAttr=0;
        		for(IDCAttribute attr : context.selectedType.getAttributes()) {
        			
        			Object searchVal = context.selectedData.getRawValue(nAttr);
        			Object searchValObj = context.selectedData.getValue(nAttr);
        			String searchValStr = ""+searchVal;
        			
        			if(searchVal != null && searchValStr.length() > 0 && context.searchVals.get(nAttr).length() > 0) {
        				
            			Object val = data.getValue(nAttr);
            			
        				IDCUtils.debug("IDCWebApplication.search(): searchVal = " + searchVal + " / val = " + val);

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

	public String updateRefTree(IDCWebAppContext context, String prefix, int nBox, IDCDataRef ref) {
		
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
	
	public IDCReport getReport(IDCWebAppContext context, HttpServletRequest request) {
		
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

   	public void print(IDCWebAppContext context, PrintWriter out) {
		
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

	private String generatePrintFile(IDCWebAppContext context) {

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

	public void startTransaction(IDCWebAppContext context) {
		context.transId = app.startTransaction();
	}

	/****************************************************************************/

	public void endTransaction(IDCWebAppContext context, boolean isCommit) {
		if(context.transId != -1) {
			app.endTransaction(context.transId, isCommit);
			context.transId = -1;
		}
	}
	
	/****************************************************************************/

	public IDCEnabled moveStack(IDCWebAppContext context, int action) {

		IDCEnabled ret = null;
		
		if(action == IDCWebAppController.FORWARD) {
			ret = context.stack.moveForwardStack();
		} else {
			ret = context.stack.moveBackStack();
		}
	
		IDCUtils.debug("IDCWebAppContext.moveStack(): ret = " + ret);

		return ret;
	
	}

	/************************************************************************************************/

	public void setBrowser(IDCWebAppContext context, List<IDCDataRef> list) {
		context.browser = new IDCDatabaseTableBrowser(context.selectedType, list);
	}

	/****************************************************************************/
	
	public IDCType getType(HttpServletRequest request) {
		
		IDCType ret = null;
		
		int typeId = IDCWebAppController.getIntParam(request, IDCWebAppController.TYPEID_PARM);
		if(typeId != IDCWebAppController.NA) {
			ret = app.getType(typeId);
		}
			
		return ret;

	}

	/****************************************************************************/
	
	public void setSelectedType(IDCWebAppContext context, HttpServletRequest request) {
		
		int typeId = IDCWebAppController.getIntParam(request, IDCWebAppController.TYPEID_PARM);
		if(typeId != IDCWebAppController.NA) {
			IDCType type = app.getType(typeId);
			if(type != null) {
				context.selectedType = type;
//				context.selectedType = app.getType(typeId);
//				context.resetBrowserMap();
			} else {
				context.message = "Invalid type specified: typeId = " + typeId;
			}
		}
		
	}

	/****************************************************************************/
	
	public void setSelectedData(IDCWebAppContext context, HttpServletRequest request) {
		
		setSelectedType(context, request);
		
		long itemId = IDCUtils.getJSPLongParam(request, IDCWebAppController.ITEMID_PARM);
		if(itemId != IDCWebAppController.NA) {
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
	
	public void setSelectedAttr(IDCWebAppContext context, HttpServletRequest request) {
		setSelectedAttr(context, request, true);
	}
	
	/****************************************************************************/
	
	public void setSelectedAttr(IDCWebAppContext context, HttpServletRequest request, boolean isSetData) {
		
		if(isSetData) {
			setSelectedData(context, request);
		}
		
		String attrIdStr = IDCUtils.getJSPParam(request, IDCWebAppController.ATTRID_PARM);
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
	
	public IDCAttribute getRequestAttr(IDCWebAppContext context, HttpServletRequest request) {
		
		IDCAttribute ret = null;
		
		String attrIdStr = IDCUtils.getJSPParam(request, IDCWebAppController.ATTRID_PARM);
		if(attrIdStr.length() > 0) {
			ret = getAttribute(context.selectedData, attrIdStr);
		}
		
		return ret;

	}
	
	/****************************************************************************/
	
	public int getSortAttrId(HttpServletRequest request) {

		int ret = -1;
		
		String attrIdStr = IDCUtils.getJSPParam(request, IDCWebAppController.ATTRID_PARM);
		ret = Integer.parseInt(attrIdStr);
		
		return ret;

	}
	
	/****************************************************************************/

	private List<IDCItemId> getSelectedItems(HttpServletRequest request) {
		
		List<IDCItemId> ret = new ArrayList<IDCItemId>();
		
		IDCItemId itemPair = null;
		String selectedItems = request.getParameter(IDCWebAppController.SELECTEDIDS);
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
		
		if(attrId != IDCWebAppController.NA) {
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

	public void initSearchData(IDCWebAppContext context) {
		
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

	public void setContext(HttpServletRequest request, IDCWebAppContext context) {
		request.getSession().setAttribute(IDCWebAppController.SESSIONID + app.getName(), context);
	}
		
	/****************************************************************************/

	private String getJSONGraphData(IDCWebAppContext context) {
		
		Map<Long, Long> map = getGraphData(context);
		
		String ret = "{\"Points\" : [";
		
		boolean isFirst = true;
		for(Long date : map.keySet()) {
			Long amount = map.get(date);
			if(isFirst) {
				isFirst = false;
			} else {
				ret += ", ";
			}
			ret += "[\"" + IDCCalendar.displayDateShortStatic(date) + "\", " + amount + "]";
		}
		
		ret += "]}";
		
		return ret;
		
	}
	
	/****************************************************************************/

	private String getCSVEntryData(IDCWebAppContext context) {
		
		String ret = null;			
		IDCDatabaseTableBrowser browser = null;
		
		if(context.selectedAttr == null) {
			ret = context.selectedType.getCSVHeader();			
			browser = context.browser;
		} else {
			IDCType refType = context.selectedAttr.getReferences().get(0).getDataType();
			ret = refType.getCSVHeader();			
			browser = context.getBrowser(context.selectedAttr);
		}		
		
		for(IDCData data : browser.getList()) {
			ret += data.getCSVString(false);
		}
		
		return ret;
		
	}
	
	/****************************************************************************/

	private String getJSONEntryData(IDCWebAppContext context) {
		
		String ret = "{\"Entries\" : [";
		
		IDCDatabaseTableBrowser browser = context.getBrowser(context.selectedAttr);
		
		boolean isFirstChild = true;
		for(IDCData data : browser.getList()) {
			ret += data.getJSONString(false, isFirstChild);
			isFirstChild = false;
		}
		
		ret += "]}";
		
		return ret;
		
	}
	
	/****************************************************************************/

	private Map<Long, Long> getGraphData(IDCWebAppContext context) {
		
		Map<Long, Long> ret = new TreeMap<Long, Long>();
		
		IDCType refType = context.selectedAttr.getReferences().get(0).getDataType();
		
		int dateAttrId = -1, valAttrId = -1;
		for(IDCAttribute attr : refType.getAttributes()) {
			if(attr.getAttributeType() == IDCAttribute.PRICE) {
				valAttrId = attr.getAttributeId();
			} else if(attr.getAttributeType() == IDCAttribute.DATE) {
				dateAttrId = attr.getAttributeId();
			}
		}
		
		IDCDatabaseTableBrowser browser = context.getBrowser(context.selectedAttr);
		
//		for(IDCData data : context.selectedData.getList(context.selectedAttr.getAttributeId())) {
		for(IDCData data : browser.getList()) {
			long date = data.getLong(dateAttrId);
			long amount = data.getLong(valAttrId);
			if(amount < 0) {
				amount *= -1;
			}
			Long curAmount = ret.get(date);
			if(curAmount != null) {
				amount += curAmount;
			}
			ret.put(date,  amount);
		}
		
		return ret;
		
	}


}