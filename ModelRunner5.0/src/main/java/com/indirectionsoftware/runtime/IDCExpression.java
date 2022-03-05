package com.indirectionsoftware.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.webapp.IDCWebAppController;
import com.indirectionsoftware.runtime.webapp.IDCWebApplication;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCEmail;
import com.indirectionsoftware.utils.IDCSMS;
import com.indirectionsoftware.utils.IDCUtils;

/************************************************************************************************/

public class IDCExpression {
	
	/************************************************************************************************/

	public static final int LIST=-2, UNDEF=-1, STRING=0, SYSVAR=1, LONGNUM=2, DOUBLENUM=3, MODELELEM=4, BOOLEAN=5, OPER=6;
	
	static final String[] VARIABLE_NAMES = {"Today", "Now", "Null", "This", "NewLine", "Ref", "ExtParent", "NSParent", "Id", "User", "Context", "NextId", "Name", "Link"};
	public static final int TODAY=0, NOW=1, NULL=2, THIS=3, NEWLINE=4, REF=5, EXTPARENT=6, NSPARENT=7, THISID=8, USER=9, CONTEXT=10, NEXTID=11, NAME=12, LINK=13;
	
	static final String[] FUNCTION_NAMES = {"Filter", "Format", "If", "Sum", "Avg", "Max", "Min", "Count", "ExType", "String", "ExecMethod", "GetDays", "IsToday", "IsNow", "DateShort", "Not", "Name", "New", "Save", "NewList", "SetSystemReference", "ExecFormula", 
											"SysRef", "SendNotification", "ExecAction", "ForEach", "TypeName", "AddWorkflowContextData", "GetWorkflowContextData", "SendRequest", "RunWorkflow", "SysAppRef", "SendEmail", "AssignTask", "SendSMS", "RequestData",
											"GetNSRef", "NewListCopy"};
	public static final int FILTER=0, FORMAT=1, IF=2, SUM=3, AVG=4, MAX=5, MIN=6, COUNT=7, EXTYPE=8, STRINGFUNC=9, EXECMETHOD=10, GETDAYS=11, ISTODAY=12, ISNOW=13, DATESHORT=14, NOT=15, NAMEFUNC=16, NEW=17, SAVE=18, NEWLIST=19, SETSYSREF=20, EXECFORMULA=21, 
											SYSREF=22, SENDNOTIFICATION=23, EXECACTION=24, FOREACH=25, TYPENAME=26, WORKFLOWADDCONTEXTDATA=27, WORKFLOWGETCONTEXTDATA=28, SENDREQUEST=29, RUNWORKFLOW=30, SYSAPPREF=31, SENDEMAIL=32, ASSIGNTASK=33, SENDSMS=34, REQUESTDATA=35,
											GETNSREF=36, NEWLISTCOPY=37;
	
	static final String[][] KEYWORDS = {VARIABLE_NAMES, FUNCTION_NAMES};
	public static final int VARIABLE=0, FUNCTION=1;
	
	static final String[] COMPARE_OPERATORS = {"==", "!=", ">", "<", ">=", "<=", "~=", "=-"};
	public static final int EQUALS=0, NOTEQUALS=1, GREATER=2, LESS=3, GREATEREQUAL=4, LESSEQUAL=5, EQUALS_IGNORE_CASE=6, STARTS_WITH=7;

	static final String[] LOGIC_OPERATORS = {"AND", "OR"};
	public static final int AND=0, OR=1;

	static final String[] STRING_OPERATORS = {":"};
	public static final int CONCAT=0;

	static final String[] MODEL_OPERATORS = {"."};
	public static final int DOT=0;

	static final String[] NUMERICAL_OPERATORS = {"+", "-", "*", "/"};
	public static final int PLUS=0, MINUS=1, MULTIPLY=2, DIVIDE=3;
	
	static final String[] ASSIGN_OPERATORS = {"=", "+="};
	public static final int ASSIGN=0, ADDTO=1;
	
	static final String[][] OPERATORS = {COMPARE_OPERATORS, LOGIC_OPERATORS, STRING_OPERATORS, NUMERICAL_OPERATORS, ASSIGN_OPERATORS, MODEL_OPERATORS};
	public static final int COMPARE_OPERATORS_IND=0, LOGIC_OPERATORS_IND=1, STRING_OPERATORS_IND=2, NUMERICAL_OPERATORS_IND=3, ASSIGN_OPERATORS_IND=4, MODEL_OPERATORS_IND=5;
	
	public static final char EOL = '\n';
	private static final char SPLIT_FORMULAS_CHAR = '|';

	/************************************************************************************************/

	String exprStr;
	int exprType;
	
	int operTypeIndx, operIndx;
	
	List<IDCExpression> list;
	
	/************************************************************************************************/

	public static List<List<IDCExpression>> getExpressionList(String exprStr) {
		
		List<List<IDCExpression>> ret = split(exprStr);
		
		return ret;
	
	}

	/************************************************************************************************/

	public IDCExpression(List<IDCExpression> list) {
		exprType = LIST;
		this.list = list;
	}
	
	/************************************************************************************************/

	public IDCExpression(String exprStr) {
		this(exprStr, UNDEF, -1, -1);
	}
	
	/************************************************************************************************/

	public IDCExpression(String exprStr, int exprType) {
		this(exprStr, exprType, -1, -1);
	}
	
	/************************************************************************************************/

	public IDCExpression(String str, int type, int typeIndx, int indx) {
		
		exprStr = str;
		exprType = type;
		operTypeIndx = typeIndx;
		operIndx = indx;
		
		if(exprType == UNDEF) {
			if(exprStr.length() > 1 && exprStr.startsWith("/") && exprStr.endsWith("/")) {
				exprType = STRING;
				exprStr = exprStr.substring(1, exprStr.length()-1);
			}
		}
		
		if(exprType == UNDEF) {
			checkOperator();
		}
		
		if(exprType == UNDEF) {

			Long l = null;
			try {
				l = new Long(exprStr);
			} catch(Exception e) {}
			if(l != null) {
				exprType = LONGNUM;
			} else {
				Double d = null;
				try {
					d = new Double(exprStr);
				} catch(Exception e) {}
				if(d != null) {
					exprType = DOUBLENUM;
				}
			}

		}
		
		if(exprType == UNDEF) {
			if(exprStr.equalsIgnoreCase("true") || exprStr.equalsIgnoreCase("false")) {
				exprType = BOOLEAN;
			}

		}
		
		if(exprType == UNDEF) {
			exprType = MODELELEM;
		}

	}
	
	/************************************************************************************************/

	public boolean isList() {
		return exprType == LIST;
	}
	
	/************************************************************************************************/

	public void checkOperator() {

		for(int nOperType=0, maxOperTypes=OPERATORS.length; nOperType < maxOperTypes && operTypeIndx==-1; nOperType++) {
			
			for(int nOper=0, maxOpers=OPERATORS[nOperType].length; nOper < maxOpers && operIndx==-1; nOper++) {
				if(exprStr.equals(OPERATORS[nOperType][nOper])) {
					operTypeIndx=nOperType;
					operIndx=nOper;
					exprType = OPER;
				}
			}
			
		}
		
	}
	
    /************************************************************************************************/

	public String toString() {
		return "IDCExpression: exprStr = " + exprStr + " / exprType = " + exprType + (exprType == OPER ? " / operTypeIndx = " + operTypeIndx + " / operIndx = " + operIndx : ""); 
	}

	/************************************************************************************************/

	private static IDCExpression getDotOperExpression() {
		return new IDCExpression(".",OPER, MODEL_OPERATORS_IND, DOT);
	}

    /************************************************************************************************/

	public static List<List<IDCExpression>> split(String text) {
		
		List<List<IDCExpression>> ret = new ArrayList<List<IDCExpression>>();
		
		List<String> lines = IDCUtils.splitLine(text, EOL);
		for(String line : lines) {
			List<String> expressions = IDCUtils.splitLine(line, SPLIT_FORMULAS_CHAR);
			for(String expr : expressions) {
				ret.add(splitExpression(expr));
			}
		}

		return ret;

	}

	/************************************************************************************************/

	public static List <IDCExpression> splitExpression(String expr) {
		
		IDCUtils.debugTemp("splitExpression(): expr = " + expr);
		
		List <IDCExpression> ret = new ArrayList<IDCExpression> ();
		
		int order = 0;
		
		String s = "";
		int openParenthesis=0;
		int openBrackets=0;
		boolean inString=false;
		int type = UNDEF;
		
		for(int nChar=0, maxChar=expr.length(); nChar < maxChar; nChar++) {
			
			char c = expr.charAt(nChar);
			
			switch(c) {
			
				case '(':
					if(inString || openBrackets>0) {
						s+=c;
					} else {
						if(openParenthesis == 0) {
							if(s.length() > 0) {
								ret.add(new IDCExpression(s, type));
								s = "";
								type = UNDEF;
							}
						} else {
							s+=c;
						}
						openParenthesis++;
					}
					break;
			
				case ')':
					if(inString || openBrackets>0) {
						s+=c;
					} else {
						if(--openParenthesis == 0) {
							ret.add(new IDCExpression(splitExpression(s)));
							s = "";
							type = UNDEF;
						} else {
							s += c;
						}
					}
					break;
			
				case '{':
					if(inString || openParenthesis > 0) {
						s+=c;
					} else {
						if(openBrackets>0) {
							s+=c;
						} else {
							if(s.length() > 0) {
								ret.add(new IDCExpression(s, type));
								s = "";
								type = SYSVAR;
							}
						}
						openBrackets++;
					}
					break;
			
				case '}':
					if(inString || openParenthesis > 0) {
						s+=c;
					} else {
						if(--openBrackets == 0) {
							ret.add(new IDCExpression(s, SYSVAR));
							s = "";
							type = UNDEF;
						} else {
							s += c;
						}
					}
					break;
			
				case ' ':
					if(inString || openParenthesis > 0 || openBrackets > 0) {
						s+=c;
					} else {
						if(s.length() > 0) {
							ret.add(new IDCExpression(s,type));
							s = "";
							type = UNDEF;
						}
					}
					break;
			
				case '\'':
					if(inString) {
						inString = false;
						ret.add(new IDCExpression(s,STRING));
						s = "";
						type = UNDEF;
					} else {
						if(openParenthesis > 0 || openBrackets>0) {
							s+=c;	
						} else {
							inString = true;
							if(s.length() > 0) {
								ret.add(new IDCExpression(s,type));
								s = "";
								type = UNDEF;
							}
						}
					}
					break;
			
				case '.':
					if(inString || openParenthesis > 0 || openBrackets>0) {
						s+=c;
					} else {

						boolean isDecimalNumber = false;
						
						if(s.length() > 0) {
							try {
								int num = Integer.parseInt(s);
								isDecimalNumber = true;
							} catch (Exception e) {
							}
						}

						if(isDecimalNumber) {
							s+=c;
						} else {
							if(s.length() > 0) {
								ret.add(new IDCExpression(s,MODELELEM));
							}
							ret.add(getDotOperExpression());
							s = "";
							type = MODELELEM;
						}
					}
					break;
					
				default: 
					s+=c;
					break;
			
			}
			
		}
		
		if(s.length() > 0) {
			ret.add(new IDCExpression(s,type));
			s = "";
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public IDCEvalData evaluate(IDCFormulaContext context) {
		
		IDCEvalData ret = null;
		
		switch(exprType) {
		
			case LONGNUM:
				ret = new IDCEvalData(new Long(exprStr));
				break;
			
			case DOUBLENUM:
				ret = new IDCEvalData(new Double(exprStr));
				break;
			
			case STRING:
				ret = new IDCEvalData(exprStr);
				break;
			
			case MODELELEM:
				ret = evaluateModelElem(context);
				break;
			
			case SYSVAR:
				ret = evaluateSysVar(context);
				break;
			
			case BOOLEAN:
				ret = new IDCEvalData(new Boolean(exprStr));
				break;
			
		}
		
		return ret;
		
	}
	
	/************************************************************************************************/

	public IDCEvalData evaluateModelElem(IDCFormulaContext context) {
		
		IDCEvalData ret = null;
		
		IDCType type = context.data.getDataType();
		IDCAttribute attr = type.getAttribute(exprStr);
		if(attr != null) {
			Object value = context.data.getValue(attr);
			ret = new IDCEvalData(value, context.data, attr);
		}
		
		return ret;
		
	}
	
    /************************************************************************************************/

	public IDCEvalData evaluateSysVar(IDCFormulaContext context) {
		
		IDCEvalData ret = null;
		Object retVal = null;
		int retType = -1;
		
		IDCSysVarEval sysEval = parseSysVar();
		
		switch(sysEval.kwType) {
		
			case VARIABLE:

				switch(sysEval.kwIndex) {
				
					case TODAY:
						retVal = new IDCCalendar().getDayStart();
						retType = IDCAttribute.DATE;
						break;
			
					case NOW:
						retVal = System.currentTimeMillis();
						retType = IDCAttribute.DATETIME;
						break;
			
					case NULL:
						retVal = null;
						break;
			
					case NEWLINE:
						retVal = "\n";
						retType = IDCAttribute.STRING;
						break;
			
					case THIS:
						retVal = context.data;
						retType = IDCAttribute.REF;
						break;
			
					case EXTPARENT:
						retVal = context.data.getExtentionParent();
						retType = IDCAttribute.REF;
						break;
			
					case NSPARENT:
						retVal = context.data.getNamespaceParent(); // is data right here ?
						retType = IDCAttribute.REF;
						break;
			
					case CONTEXT:
						retVal = context.data.getContextData();
						retType = IDCAttribute.REF;
						break;
			
					case THISID:
						retVal = context.data.getId();
						retType = IDCAttribute.INTEGER;
						break;
			
					case REF:
						retVal = context.ref;
						retType = IDCAttribute.REF;
						break;
			
					case USER:
						retVal = context.user.getUserData();
						retType = IDCAttribute.REF;
						break;
			
					case NEXTID:
						long maxRow = context.data.getDataType().getMaxRow();
						if(maxRow != -1) {
							retVal = new Long(maxRow + 1);
							retType = IDCAttribute.INTEGER;
						}
						break;
			
					case NAME:
						retVal = context.data.getName();
						retType = IDCAttribute.STRING;
						break;
						
					case LINK:
						retType = IDCAttribute.STRING;
						retVal = IDCWebApplication.getLink(context.data.getDataType().getApplication(), context.data.getName(), context.data.getDataType().getId(), context.data.getId());
						break;

			}
			break;
		
			case FUNCTION:

				switch(sysEval.kwIndex) {
				
					case FORMAT:
						
						retType = IDCAttribute.STRING;
						IDCFormula form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						Object val = form.evaluate(context).getValue();
						int len = Integer.parseInt(sysEval.funcParam.get(1));
						String fill = IDCUtils.parseQuotedString(sysEval.funcParam.get(2));
						String side = IDCUtils.parseQuotedString(sysEval.funcParam.get(3));

						String fillString = "";
						for(int nFill=0; nFill < len; nFill++) {
							fillString +=fill;
						}
						
						String retString = "";
						if(side.equals("Right")) {
							retString += val + fillString;
							retVal = retString.substring(0, len);
						} else {
							retString += fillString + val;
							int newLen=retString.length();
							retVal = retString.substring(newLen-len, newLen);
						}

						break;
			
					case EXECMETHOD:
						
						String className = IDCUtils.parseQuotedString(sysEval.funcParam.get(0));
						String methodName = IDCUtils.parseQuotedString(sysEval.funcParam.get(1));
						retVal = executeMethod(context, className, methodName);
							
						break;
			
					case GETDAYS:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						IDCEvalData eval1 = form.evaluate(context);
						
						if(eval1 != null && eval1.getType() == IDCAttribute.DATE || eval1.getType() == IDCAttribute.DATETIME || eval1.getType() == IDCAttribute.INTEGER) {
							retType = IDCAttribute.INTEGER;
							retVal = ((Long) eval1.getValue()) / IDCCalendar.DAY;
						}
							
						break;
			
					case DATESHORT:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						eval1 = form.evaluate(context);
						
						if(eval1 != null && eval1.getType() == IDCAttribute.DATE || eval1.getType() == IDCAttribute.DATETIME || eval1.getType() == IDCAttribute.INTEGER) {
							retType = IDCAttribute.STRING;								
							retVal = IDCCalendar.displayDateShortStatic((Long) eval1.getValue());
						}
							
						break;
			
					case ISTODAY:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						eval1 = form.evaluate(context);
						
						if(eval1 != null && eval1.getType() == IDCAttribute.DATE || eval1.getType() == IDCAttribute.DATETIME || eval1.getType() == IDCAttribute.INTEGER) {

							long timestamp = (Long) eval1.getValue();

							IDCCalendar cal = new IDCCalendar();
							long dayStart = cal.getDayStart();
							long dayEnd = cal.getDayEnd();
							
							retType = IDCAttribute.BOOLEAN;
							retVal = timestamp >= dayStart && timestamp <= dayEnd;
							
						}
							
						break;
			
					case ISNOW:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						eval1 = form.evaluate(context);
						
						if(eval1 != null && eval1.getType() == IDCAttribute.DATE || eval1.getType() == IDCAttribute.INTEGER) {

							long timestamp = (Long) eval1.getValue();
							long now = IDCCalendar.getTime();
							long dif = timestamp - now;
							if(dif <0) {
								dif = -dif;
							}
							
							retType = IDCAttribute.BOOLEAN;
							retVal = dif < IDCCalendar.MIN;
							
						}
							
						break;
			
					case NOT:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						eval1 = form.evaluate(context);
						
						if(eval1 != null && eval1.getType() == IDCAttribute.BOOLEAN) {
							boolean boolVal = (Boolean) eval1.getValue();
							retVal = ! boolVal;
						}
							
						break;
			
					case STRINGFUNC:
						
						retType = IDCAttribute.STRING;
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						val = form.evaluate(context).getValue();
						retVal = "'" + val + "'";
						break;
			
					case NAMEFUNC:
						retType = IDCAttribute.STRING;
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						val = form.evaluate(context).getValue();
						retVal = "";
						if(val != null) {
							retVal = ((IDCData)val).getName();
						}
						break;
			
					case IF:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						Boolean result = (Boolean) form.evaluate(context).getValue();
						if(result != null) {
							if(result.booleanValue()) {
								IDCFormula ex2 = IDCFormula.getFormula(sysEval.funcParam.get(1));
								ret = ex2.evaluate(context);
							} else {
								IDCFormula ex3 = IDCFormula.getFormula(sysEval.funcParam.get(2));
								ret = ex3.evaluate(context);
							}
						}
						
						break;
			
					case EXTYPE:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						eval1 = form.evaluate(context);
						IDCAttribute eval1Attr = eval1.getAttribute();
						if(eval1Attr != null && eval1Attr.getAttributeType() == IDCAttribute.EXTENSION) {
							Object val1 = eval1.getValue();
							if(val1 != null && val1 instanceof IDCData) {
								IDCType type = ((IDCData)val1).getDataType();
								String  val2 = IDCFormula.getFormula(sysEval.funcParam.get(1)).evaluate(context).getStringValue();
								if(val2 != null) {
									retType = IDCAttribute.BOOLEAN;
									if(val2.equals(type.getName())) {
										retVal = new Boolean(true);
									} else {
										retVal = new Boolean(false);
									}
								}
							}
						}
						
						break;
			
					case SUM:
					case AVG:
					case MIN:
					case MAX:
					case COUNT:
						
						form = IDCFormula.getFormula(sysEval.funcParam.get(0));
						IDCEvalData retData = form.evaluate(context);
						if(retData != null) {
							Object o = retData.getValue();
							if(o == null) {
								retVal = 0;
							} else if(retData.isList()) {
								ret = postProcessNumericListFunction(sysEval.kwIndex,(List<IDCEvalData>)o);
							} else if(o instanceof List && sysEval.kwIndex == COUNT){
								retVal = ((List) o).size();
							} else {
								retVal = o;
							}
						} else {
							retVal = new Double(0);
						}
						
						break;
			
					case NEW:
						
						retType = IDCAttribute.REF;
						String typeName = (String) IDCFormula.getFormula(sysEval.funcParam.get(0)).evaluate(context).getValue();
						IDCType type = context.data.getApplication().getType(typeName);
						if(type != null) {
							IDCData newData = type.createData();
							if(sysEval.funcParam.size() > 1) {
								String initParams = sysEval.funcParam.get(1);
								form = IDCFormula.getFormula(initParams);
								if(form != null) {
									IDCEvalData evalData = form.evaluate(context);
									if(evalData != null) {
										val = evalData.getValue();
										if(val instanceof IDCDataParentRef) {
											newData.setNamespaceParentRef((IDCDataParentRef) val);
											int nParam = 2;
											IDCFormulaContext newContext = context.copy();
											newContext.data = newData;
											while(nParam < sysEval.funcParam.size()) {
												initParams = sysEval.funcParam.get(nParam++);
												form = IDCFormula.getFormula(initParams);
												if(form != null) {
													form.evaluate(newContext);
												}
											}
											retVal = newData;
										}
									}
								}
							}
							
							newData.save();

						} else {
							retVal = null;
						}
						break;
				
					case NEWLIST:

						retType = IDCAttribute.LIST;
						typeName = (String) IDCFormula.getFormula(sysEval.funcParam.get(0)).evaluate(context).getValue();
						type = context.data.getApplication().getType(typeName);
						if(type != null) {
							IDCAttribute newAttr = type.getAttribute(sysEval.funcParam.get(1));
							
							form = IDCFormula.getFormula(sysEval.funcParam.get(2));
							val = form.evaluate(context).getValue();
							
							if(val != null && val instanceof List<?>) {
								
								retVal = new ArrayList<IDCData>();

								for(IDCData data : (List<IDCData>) val) {
									
									IDCData newData = type.createData();										
									newData.set(newAttr, data);
									newData.save();
									
									if(sysEval.funcParam.size() > 3) {
										String initParams = sysEval.funcParam.get(3);
										form = IDCFormula.getFormula(initParams);
										val = form.evaluate(context).getValue();            ///val????
									}
									
									((List<IDCData>)retVal).add(newData);
									
								}
								
							}

						} else {
							retVal = null;
						}
						
						break;
				
					case NEWLISTCOPY:

						retType = IDCAttribute.LIST;
						typeName = (String) IDCFormula.getFormula(sysEval.funcParam.get(0)).evaluate(context).getValue();
						type = context.data.getApplication().getType(typeName);
						if(type != null) {
							form = IDCFormula.getFormula(sysEval.funcParam.get(1));
							if(form != null) {
								val = form.evaluate(context).getValue();
								if(val != null && val instanceof List) {
									
									retVal = new ArrayList<IDCData>();

									for(IDCData data : (List<IDCData>) val) {
										
										IDCData newData = type.createData();
										for(IDCAttribute attr : data.getDataType().getAttributes()) {
											IDCAttribute newAttr = type.getAttribute(attr.getName());
											if(newAttr != null) {
												newData.set(newAttr, data.getValue(attr));
											}
											
										}
										newData.save();
										((List<IDCData>)retVal).add(newData);
									}
								}
							}
							
						} else {
							retVal = null;
						}
						
						break;
				
					case SAVE:

						retType = IDCAttribute.REF;

						String attrName = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(attrName);
						val = form.evaluate(context).getValue();
						if(val != null && val instanceof IDCData) {
							((IDCData) val).save();
						} else {
							retVal = null;
						}
						break;

					case SETSYSREF:							

						retType = IDCAttribute.REF;
						
						String sysDataFormula = sysEval.funcParam.get(0);
						String refDataFormula = sysEval.funcParam.get(1);
						
						form = IDCFormula.getFormula(sysDataFormula);
						IDCData sysData = (IDCData) form.evaluate(context).getValue();
						
						form = IDCFormula.getFormula(refDataFormula);
						IDCData refData = (IDCData) form.evaluate(context).getValue();
						
						if(sysData != null) {
							refData.setSystemReferenceData(sysData);
						} else {
							retVal = null;
						}
						break;

					case EXECFORMULA:							

						String dataFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(dataFormula);
						Object formulaDataObject = form.evaluate(context).getValue();
						List<IDCData> formulaListData = null;
						if(formulaDataObject instanceof IDCData) {
							formulaListData = new ArrayList<IDCData>();
							formulaListData.add((IDCData)formulaDataObject);
						} else {
							formulaListData = (List<IDCData>) formulaDataObject;
						}
						
						String actionFormulaFormula = sysEval.funcParam.get(1);
						
						IDCData contextData = context.data;
						if(sysEval.funcParam.size() > 2) {
							String contextFormula = sysEval.funcParam.get(2);
							form = IDCFormula.getFormula(contextFormula);
							contextData = (IDCData) form.evaluate(context).getValue();
						}
													
						boolean isSave = true;
						if(sysEval.funcParam.size() > 3) {
							String booleanFormula = sysEval.funcParam.get(3);
							form = IDCFormula.getFormula(booleanFormula);
							isSave = (Boolean) form.evaluate(context).getValue();
						}

						boolean isEvaluate = true;
						if(sysEval.funcParam.size() > 4) {
							String booleanFormula = sysEval.funcParam.get(4);
							form = IDCFormula.getFormula(booleanFormula);
							isEvaluate = (Boolean) form.evaluate(context).getValue();
						}
						
						String actionFormula = actionFormulaFormula;
						if(isEvaluate) {
							form = IDCFormula.getFormula(actionFormulaFormula);
							actionFormula = (String) form.evaluate(context).getValue();
						}

						form = IDCFormula.getFormula(actionFormula);
						IDCFormulaContext newContext = context.copy();

						for(IDCData formulaData : formulaListData) {
							newContext.data = formulaData;
							form.evaluate(newContext);
						}
						break;

					case SYSREF:	// get system record linked to this app record						

						retType = IDCAttribute.REF;
						retVal = context.data.getSystemReferenceData();
						break;

					case SENDNOTIFICATION:							

						String usersFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(usersFormula);
						IDCData userData = null;
						IDCEvalData usersEvalData = form.evaluate(context);
						if(usersEvalData != null) {
							userData = (IDCData) usersEvalData.getValue();
						}
						
						String titleString = null;
						String titleFormulaFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(titleFormulaFormula);
						IDCEvalData titleFormulaEvalData = form.evaluate(context);
						if(titleFormulaEvalData != null) {
							titleString = (String) titleFormulaEvalData.getValue();
							form = IDCFormula.getFormula(titleString);
							if(form != null) {
								IDCEvalData titleEvalData = form.evaluate(context);
								if(titleEvalData != null) {
									titleString = (String) titleEvalData.getValue();
								}
							}
						}
													
						String contentString = null;
						String contentFormulaFormula = sysEval.funcParam.get(2);
						form = IDCFormula.getFormula(contentFormulaFormula);
						String contentFormula = null;
						IDCEvalData contentFormulaEvalData = form.evaluate(context);
						if(contentFormulaEvalData != null) {
							contentString = (String) contentFormulaEvalData.getValue();
							form = IDCFormula.getFormula(contentString);
							if(form != null) {
								IDCEvalData contentEvalData = form.evaluate(context);
								if(contentEvalData != null) {
									contentString = (String) contentEvalData.getValue();
								}
							}
						}
													
						IDCData notification = IDCNotificationData.getNewNotification(context.data.getApplication(), userData, titleString, contentString);
						
						retType = IDCAttribute.STRING;
						retVal = new IDCDataRef(notification).toString();

						break;

					case EXECACTION:							

						dataFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(dataFormula);	
						formulaDataObject = form.evaluate(context).getValue();
						formulaListData = null;
						if(formulaDataObject instanceof IDCData) {
							formulaListData = new ArrayList<IDCData>();
							formulaListData.add((IDCData)formulaDataObject);
						} else {
							formulaListData = (List<IDCData>) formulaDataObject;
						}

						String actionName = sysEval.funcParam.get(1);

						contextData = context.data;
						if(sysEval.funcParam.size() > 2) {
							String contextFormula = sysEval.funcParam.get(2);
							form = IDCFormula.getFormula(contextFormula);
							contextData = (IDCData) form.evaluate(context).getValue();
						}
						
						isSave = true;
						if(sysEval.funcParam.size() > 3) {
							String booleanFormula = sysEval.funcParam.get(3);
							form = IDCFormula.getFormula(booleanFormula);
							isSave = (Boolean) form.evaluate(context).getValue();
						}
						
						for(IDCData formulaData : formulaListData) {
							formulaData.setContextData(contextData);
							formulaData.executeAction(actionName, isSave);
						}

						break;

					case FOREACH:							

						dataFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(dataFormula);
						formulaListData = (List<IDCData>) form.evaluate(context).getValue();
						
						actionFormula = sysEval.funcParam.get(1);
						
						contextData = context.data;
						if(sysEval.funcParam.size() > 2) {
							String contextFormula = sysEval.funcParam.get(2);
							form = IDCFormula.getFormula(contextFormula);
							contextData = (IDCData) form.evaluate(context).getValue();
						}

						isSave = true;
						if(sysEval.funcParam.size() > 3) {
							String booleanFormula = sysEval.funcParam.get(3);
							form = IDCFormula.getFormula(booleanFormula);
							isSave = (Boolean) form.evaluate(context).getValue();
						}

						form = IDCFormula.getFormula(actionFormula);
						newContext = context.copy();

						for(IDCData formulaData : formulaListData) {
							newContext.data = formulaData;
							form.evaluate(newContext);
						}
						break;
						
					case TYPENAME:				
						retType = IDCAttribute.STRING;
						retVal = context.data.getDataType().getName();
						break;

					case WORKFLOWADDCONTEXTDATA:
						
						String keyFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(keyFormula);
						String key = (String) form.evaluate(context).getValue();

						String refFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(refFormula);
						String ref = (String) form.evaluate(context).getValue();

						context.getWorkflowInstance().addContextData(key, IDCDataRef.getRef(ref));
						break;

					case WORKFLOWGETCONTEXTDATA:
						
						keyFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(keyFormula);
						key = (String) form.evaluate(context).getValue();

						IDCData temp = context.getWorkflowInstance().getContextData(key); // root should still be workflow instance but data could be any subformula data 
						if(temp != null) {
							if(temp.getDataType().getName().equals(IDCWorkflowInstanceData.WORKFLOW_CONTEXT_TYPE)) {
								String refString = temp.getString(IDCWorkflowInstanceData.WORKFLOW_CONTEXT_REF);
								retType = IDCAttribute.REF;
								retVal = context.data.loadDataRef(IDCDataRef.getRef(refString));
							} else {
								retType = IDCAttribute.STRING;
								retVal = temp.getString(IDCWorkflowInstanceData.KEY_FORMULA_PAIR_FORMULA);
							}
						}

						break;

					case SENDREQUEST:
						
						String userFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(userFormula);
						IDCData user = (IDCData) form.evaluate(context).getValue();

						String textFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(textFormula);
						textFormula = (String) form.evaluate(context).getValue();
						form = IDCFormula.getFormula(textFormula);
						String text = (String) form.evaluate(context).getValue();

						IDCApprovalRequestData request = IDCApprovalRequestData.getNewRequest(context.data.getApplication(), user, text);

						retType = IDCAttribute.STRING;
						retVal = new IDCDataRef(request).toString();
						
						break;
						
					case RUNWORKFLOW:
						
						String workflowName = (String) IDCFormula.getFormula(sysEval.funcParam.get(0)).evaluate(context).getValue();

						IDCWorkflowInstanceData wfi = IDCWorkflowInstanceData.getNewWorkflowInstance(context.data.getApplication(), workflowName);
						
						if(wfi != null) {
							
							int nParam = 1;
							while(nParam < sysEval.funcParam.size()) {
								String contextName = (String) IDCFormula.getFormula(sysEval.funcParam.get(nParam++)).evaluate(context).getValue();
								contextData = (IDCData) IDCFormula.getFormula(sysEval.funcParam.get(nParam++)).evaluate(context).getValue();
								if(contextData != null) {
									wfi.addContextData(contextName, contextData.getDataRef());
								} else {
									IDCUtils.error("Couldn't find Contextg Data = " + contextName);
								}
							}
							
							wfi.execute();

						} else {
							IDCUtils.error("Couldn't find Workflow name = " + workflowName);
						}

						break;
						
					case SYSAPPREF:	// get app record linked to this system record						

						retType = IDCAttribute.REF;
						retVal = context.data.getData(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME);
						break;

					case SENDEMAIL:							

						List<String> toDist = new ArrayList<String>();
						
						String recipientsFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(recipientsFormula);
						Object recipientsData = null;
						IDCEvalData receipientsEvalData = form.evaluate(context);
						if(receipientsEvalData != null) {
							recipientsData = receipientsEvalData.getValue();
							if(recipientsData instanceof String) {
								toDist.add((String) recipientsData);
							} else if(recipientsData instanceof List<?>) {
								for(String email : (List<String>) recipientsData) {
									toDist.add(email);
								}
							}
						}
						
						List<String> ccDist = new ArrayList<String>();
						
						recipientsFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(recipientsFormula);
						recipientsData = null;
						if(form != null) {
							receipientsEvalData = form.evaluate(context);
							if(receipientsEvalData != null) {
								recipientsData = receipientsEvalData.getValue();
								if(recipientsData instanceof String) {
									ccDist.add((String) recipientsData);
								} else if(recipientsData instanceof List<?>) {
									for(String email : (List<String>) recipientsData) {
										ccDist.add(email);
									}
								}
							}
						}
						
						titleString = null;
						titleFormulaFormula = sysEval.funcParam.get(2);
						form = IDCFormula.getFormula(titleFormulaFormula);
						titleFormulaEvalData = form.evaluate(context);
						if(titleFormulaEvalData != null) {
							titleString = (String) titleFormulaEvalData.getValue();
							form = IDCFormula.getFormula(titleString);
							if(form != null) {
								IDCEvalData titleEvalData = form.evaluate(context);
								if(titleEvalData != null) {
									titleString = (String) titleEvalData.getValue();
								}
							}
						}
													
						contentString = null;
						contentFormulaFormula = sysEval.funcParam.get(3);
						form = IDCFormula.getFormula(contentFormulaFormula);
						contentFormulaEvalData = form.evaluate(context);
						if(contentFormulaEvalData != null) {
							contentString = (String) contentFormulaEvalData.getValue();
							form = IDCFormula.getFormula(contentString);
							if(form != null) {
								IDCEvalData contentEvalData = form.evaluate(context);
								if(contentEvalData != null) {
									contentString = (String) contentEvalData.getValue();
								}
							}
						}
													
						IDCEmail.send(toDist, ccDist, titleString, contentString);
						break;

					case SENDSMS:							

						toDist = new ArrayList<String>();
						
						recipientsFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(recipientsFormula);
						recipientsData = null;
						receipientsEvalData = form.evaluate(context);
						if(receipientsEvalData != null) {
							recipientsData = receipientsEvalData.getValue();
							if(recipientsData instanceof String) {
								toDist.add((String) recipientsData);
							} else if(recipientsData instanceof List<?>) {
								for(Object dest : (List<Object>) recipientsData) {
									if(dest instanceof String) {
										toDist.add((String)dest);
									} else if(dest instanceof IDCEvalData) {
										dest = (((IDCEvalData)dest).getValue());
										if(dest instanceof String) {
											toDist.add((String)dest);
										}
									}									
								}
							}
						}
						
						contentString = null;
						contentFormulaFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(contentFormulaFormula);
						contentFormulaEvalData = form.evaluate(context);
						if(contentFormulaEvalData != null) {
							contentString = (String) contentFormulaEvalData.getValue();
							form = IDCFormula.getFormula(contentString);
							if(form != null) {
								IDCEvalData contentEvalData = form.evaluate(context);
								if(contentEvalData != null) {
									contentString = (String) contentEvalData.getValue();
								}
							}
						}
													
						IDCSMS.sendSMS(toDist, contentString);
						break;

					case FILTER:							

						List<IDCData> inputList = new ArrayList<IDCData>();
						
						String listFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(listFormula);
						IDCEvalData listEvalData = form.evaluate(context);
						if(listEvalData != null) {
							Object listData = listEvalData.getValue();
							if(listData instanceof IDCData) {
								inputList.add((IDCData) listData);
							} else if(listData instanceof List<?>) {
								for(Object listDataObj : (List<Object>) listData) {
									if(listDataObj instanceof IDCData) {
										inputList.add((IDCData)listDataObj);
									} else if(listDataObj instanceof IDCEvalData) {
										listDataObj = (((IDCEvalData)listDataObj).getValue());
										if(listDataObj instanceof IDCData) {
											inputList.add((IDCData) listDataObj);
										}
									}									
								}
							}
						}

						String filterFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(filterFormula);
						IDCEvalData filterFormulaEvalData = form.evaluate(context);
						if(filterFormulaEvalData != null) {
							Object filterData = filterFormulaEvalData.getValue();
							if(filterData instanceof String) {

								retType = IDCAttribute.LIST;
								retVal = new ArrayList<IDCData>();
								
								for(IDCData data : inputList) {
									if(data.isTrue((String)filterData)) {
										((List)retVal).add(data);
									}
								}
								
							}
						}


						break;

					case ASSIGNTASK:							

						userData = null;
						
						usersFormula = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(usersFormula);
						usersEvalData = form.evaluate(context);
						if(usersEvalData != null) {
							userData = (IDCData) usersEvalData.getValue();
						}
						
						titleString = null;
						titleFormulaFormula = sysEval.funcParam.get(1);
						form = IDCFormula.getFormula(titleFormulaFormula);
						titleFormulaEvalData = form.evaluate(context);
						if(titleFormulaEvalData != null) {
							titleString = (String) titleFormulaEvalData.getValue();
							form = IDCFormula.getFormula(titleString);
							if(form != null) {
								IDCEvalData titleEvalData = form.evaluate(context);
								if(titleEvalData != null) {
									titleString = (String) titleEvalData.getValue();
								}
							}
						}
													
						contentString = null;
						contentFormulaFormula = sysEval.funcParam.get(2);
						form = IDCFormula.getFormula(contentFormulaFormula);
						contentFormulaEvalData = form.evaluate(context);
						if(contentFormulaEvalData != null) {
							contentString = (String) contentFormulaEvalData.getValue();
							form = IDCFormula.getFormula(contentString);
							if(form != null) {
								IDCEvalData contentEvalData = form.evaluate(context);
								if(contentEvalData != null) {
									contentString = (String) contentEvalData.getValue();
								}
							}
						}
													
						IDCData task = IDCTaskData.getNewTask(context.data.getApplication(), userData, titleString, contentString);
						
						retType = IDCAttribute.STRING;
						retVal = new IDCDataRef(task).toString();

						break;

					case REQUESTDATA:
						
						typeName = (String) IDCFormula.getFormula(sysEval.funcParam.get(0)).evaluate(context).getValue();
						type = context.data.getApplication().getType(typeName);
						if(type != null) {
							IDCRequest req = new IDCRequest(type);
							String selectionFormula = (String) IDCFormula.getFormula(sysEval.funcParam.get(1)).evaluate(context).getValue();
							req.setSelectionFormula(selectionFormula);
							String resultFormula = null;
							if(sysEval.funcParam.size() > 2) {
								resultFormula = (String) IDCFormula.getFormula(sysEval.funcParam.get(2)).evaluate(context).getValue();
							} else {
								resultFormula = "{This}";
							}
							req.addResultFormula(resultFormula);
							retVal = new ArrayList<IDCData>(); 
							for(List<Object> res : req.execute()) {
								((List<IDCData>)retVal).add((IDCData) res.get(0));
							}
							if(((List<IDCData>)retVal).size() == 1) {
								retType = IDCAttribute.REF;
								retVal = ((List<IDCData>)retVal).get(0);
							} else {
								retType = IDCAttribute.LIST;
							}
						}
						break;
						
					case GETNSREF:

						retType = IDCAttribute.REF;
						attrName = sysEval.funcParam.get(0);
						form = IDCFormula.getFormula(attrName);
						if(form != null) {
							IDCEvalData evalData = form.evaluate(context);
							if(evalData != null) {
								val = evalData.getValue();
								if(val != null && val instanceof IDCData) {
									attrName = sysEval.funcParam.get(1);
									form = IDCFormula.getFormula(attrName);
									if(form != null) {
										evalData = form.evaluate(context);
										if(evalData != null) {
											Object val2 = evalData.getValue();
											if(val2 != null && val2 instanceof String) {
												retVal = ((IDCData) val).getAsParentRef((String) val2);
											}
										}
									}
								}
							}
						}
						break;
						
				}
				break;
			
		}

		if(ret == null) { 
			ret =  new IDCEvalData(retVal, retType);
		}

		return ret;
		
	}
	
	/************************************************************************************************/

    private IDCEvalData postProcessNumericListFunction(int function, List<IDCEvalData> dataList) {
    	
    	Long retValue = new Long(0);
    	
    	Long nVals = new Long(0);
    	
    	for(IDCEvalData evalData : dataList) {
	    	
    		Object data = evalData.getValue();
    		if(data != null) {
    			
        		switch(function) {
    	    	
			    	case SUM:
			    		retValue += getLong(data);
			    		break;
	
			    	case MIN:
			    		if((Long) data < retValue) {
			    			retValue = getLong(data);
			    		}
			    		break;
	
			    	case MAX:
			    		if((Long) data > retValue) {
			    			retValue = getLong(data);
			    		}
			    		break;
	
		    	}
	    	
    		}
    		
	    	nVals++;

    	}
    	
		switch(function) {
    	
	    	case AVG:
	    		retValue = retValue / nVals;
	    		break;

	    	case COUNT:
	    		retValue = nVals;
	    		break;

    	}
	
    	return new IDCEvalData(retValue, IDCAttribute.INTEGER);
		
	}

	/************************************************************************************************/

    private static Long getLong(Object data) {
    	
    	Long ret = null;

    	if(data.getClass() == Long.class) {
			ret = (Long) data;
    	} else {
    		Long.parseLong(""+data);
    	}
    	
    	return ret;
    }

	/************************************************************************************************/

    private static Double getDouble(Object data) {
    	return Double.parseDouble(""+data);
    }

	/************************************************************************************************/

	public IDCSysVarEval parseSysVar() {
		
		IDCSysVarEval ret = null;

		int kwType=-1;
		int kwIndex=-1;
		List<String> funcParam=null;
		
		for(int nKWType=0, maxKWTypes=KEYWORDS.length; nKWType < maxKWTypes && kwType==-1; nKWType++) {
			
			for(int nKW=0, maxKW=KEYWORDS[nKWType].length; nKW < maxKW && kwType==-1; nKW++) {

				switch(nKWType) {
				
					case VARIABLE:
						if(exprStr.equals(KEYWORDS[nKWType][nKW])) {
							kwType=nKWType;
							kwIndex=nKW;
						}
						break;
					
					case FUNCTION:
						if(exprStr.startsWith(KEYWORDS[nKWType][nKW] + "(")) {
							kwType=nKWType;
							kwIndex=nKW;
							int startParam = exprStr.indexOf('(') +1;
							int endParam = exprStr.lastIndexOf(')');
							funcParam = IDCUtils.splitLine(exprStr.substring(startParam, endParam), ',');
						}
						break;

				}
			
			}
		
		}
		
		ret = new IDCSysVarEval(kwType, kwIndex, funcParam);

		return ret;
		
	}
	
    /************************************************************************************************/

    public static Object executeMethod(IDCFormulaContext context, String className, String methodName) {
    	
        Object ret = null;
        
        try {
            Class c = Class.forName(className);
            Method method = c.getMethod(methodName, IDCApplication.class, IDCData.class, IDCFormulaContext.class);
            Object methodInstance = c.newInstance();
            ret = method.invoke(methodInstance, context.data.getDataType().getApplication(), context.data, context);
            
        } catch (Exception e) {
            IDCUtils.error(e.getMessage());
			e.printStackTrace();
		}
        
        return ret;
    	
    }
	
	/************************************************************************************************/

    public String getSQLFilter(IDCType type) {

		String ret = null;
		
		switch(exprType) {
		
			case LONGNUM:
			case DOUBLENUM:
				ret = "" + exprStr;
				break;
			
			case STRING:
				ret = "'" + exprStr + "'";
				break;
			
			case MODELELEM:
				ret = convertModelElementToSQL();
				break;
			
			case SYSVAR:
				ret = convertSysVarToSQL(type);
				break;
			
			case BOOLEAN:
				if(new Boolean(exprStr).booleanValue()) {
					ret = "1";
				} else {
					ret = "0";
				}
				break;
			
		}
		
		return ret;
		
	}
	
	/************************************************************************************************/

	private String convertModelElementToSQL() {
		
		String ret = null;
		
		String[] elems = exprStr.split("\\.");
		if(exprStr.indexOf('.') == -1) {
			ret = IDCType.getSQLName(exprStr);
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	private String convertSysVarToSQL(IDCType type) {
		
		String ret = null;
		
		IDCSysVarEval sv = parseSysVar();
		
		if(sv.kwType == FUNCTION && sv.kwIndex == EXTYPE) {
			String attrName = sv.funcParam.get(0);
			String extTypeName = sv.funcParam.get(1).substring(1, sv.funcParam.get(1).length()-1);
			IDCAttribute extAttr = type.getAttribute(attrName);
			if(extAttr != null) {
				List<IDCReference> list = extAttr.getReferences();
				int extTypeId = -1;
				for(IDCReference ext : list) {
					IDCType extType = ext.getDataType(); 
					if(extType.getName().equals(extTypeName)) {
						extTypeId = extType.getEntityId();
					}
				}
				ret = IDCType.getSQLName(attrName) + " like '(" + extTypeId + "/%'";
			}
		}

		return ret;
	
	}

 }
