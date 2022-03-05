package com.indirectionsoftware.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCTriplet;
import com.indirectionsoftware.utils.IDCUtils;

/************************************************************************************************/

public class IDCFormula {
	
	IDCExpression oper;
	IDCFormula form1, form2;
	
	IDCExpression expr;
	
	IDCFormulaContext context;
	
	List<IDCDataRef> refList = new ArrayList<IDCDataRef>();
	
	/************************************************************************/

	public static void main(String[] args) throws IOException { 
    
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("Enter String");
            String exprStr = br.readLine();
    		IDCFormula form = IDCFormula.getFormula(exprStr);
    		System.out.println(form);
        }

	}
	/************************************************************************************************/

	public IDCFormula(IDCExpression expr) {
		this.expr = expr;
	}
	
	/************************************************************************************************/

	public IDCFormula(IDCFormula form1, IDCExpression oper, IDCFormula form2) {
		this.form1 = form1;
		this.oper = oper;
		this.form2 = form2;
	}
	
	/************************************************************************************************/

	public static IDCFormula getFormula(IDCExpression expr) {
		
		IDCFormula ret = null; 
		
		if(expr.isList() ) {
			ret = getFormula(expr.list);
		} else {
			ret = new IDCFormula(expr);
		}
		
		return ret;
		
		
	}
	
	/************************************************************************************************/

	public static IDCFormula getFormula(String exprStr) {
		
		IDCFormula ret = null;
		
		List<IDCExpression> exprList = IDCExpression.splitExpression(exprStr);
		
		ret = getFormula(exprList);
    	
		return ret;
	
	}
	
	/************************************************************************************************/

	private static IDCFormula getFormula(List<IDCExpression> exprList) {

		IDCFormula ret = null;
		
		if(exprList.size() == 1) {
			ret = getFormula((exprList.get(0)));
		} else {
			int nOperIndex = getNextOperatorOrder(exprList);
			if(nOperIndex == -1) {
				IDCUtils.error("Couldn't find operator in expression list :(");
			} else {
				IDCExpression oper = null;
				List<IDCExpression> leftExprList = new ArrayList<IDCExpression>();
				List<IDCExpression> rightExprList = new ArrayList<IDCExpression>();
				for(int nExpr=0; nExpr < exprList.size(); nExpr++) {
					IDCExpression expr = exprList.get(nExpr);
					if(nExpr == nOperIndex) {
						oper = expr;
					} else if(nExpr < nOperIndex) {
						leftExprList.add(expr);
					} else {
						rightExprList.add(expr);
					}
				}
				IDCFormula form1 = getFormula(leftExprList);
				IDCFormula form2 = getFormula(rightExprList);
				if(form1 != null && form2 != null) {
					ret = new IDCFormula(form1, oper, form2);
				}
			}
		}
		
		IDCUtils.debug("getFormula():");
		IDCUtils.debug(ret != null ? ret.display("") : "");

		return ret;
		
	}

	/************************************************************************************************/

	public static List<IDCFormula> getFormulas(String exprStr) {
		
		List<IDCFormula> ret = new ArrayList<IDCFormula>();
		
		List<List<IDCExpression>> exprLists = IDCExpression.getExpressionList(exprStr);
    	
		IDCUtils.debugNow("IDCFormula.getFormulas(): exprStr = " + exprStr);
		
		for(List<IDCExpression> exprList : exprLists) {
			ret.add(getFormula(exprList));
		}
		IDCUtils.debug("-------------------------------------------");

		return ret;
	
	}


	/************************************************************************************************/

	private static int getNextOperatorOrder(List<IDCExpression> exprList) {

		int ret = -1;

		int maxOperTypeInx = 99999;
		IDCUtils.debug("getNextOperator(): -------------------------------------------");
		int nExpr = 0;
		for(IDCExpression expr : exprList) {
			IDCUtils.debug("expr=" + expr);
			if(expr.exprType == IDCExpression.OPER && expr.operTypeIndx < maxOperTypeInx) {
				ret = nExpr;
				maxOperTypeInx = expr.operTypeIndx;
			}
			nExpr++;
		}

		return ret;
		
	}


    /************************************************************************************************/

	public String toString() {
		return display("");
	}
	
    /************************************************************************************************/

	public String display(String prefix) {
		
		String ret = prefix;
		if(oper == null) {
			ret += "expr = " + expr;
		} else if(form1 != null && form2 != null) {
			ret += "oper = " + oper.exprStr + "\n" + prefix + "form1 = " + form1.display(prefix + "  ") + "\n" + prefix +  "form2 = " + form2.display(prefix + "  ");
		} else {
			IDCUtils.error("Form1 and/or Form2 is null");
		}
		
		return ret;
	}
	
    /************************************************************************************************/

	public IDCEvalData evaluate(IDCFormulaContext context) {
		
		IDCEvalData ret = null;
		
		this.context = context;
		
		if(oper == null) {
			if(expr != null) {
				ret = expr.evaluate(context);
			}
		} else {
			if(oper.operTypeIndx == IDCExpression.MODEL_OPERATORS_IND) {
				ret = evaluateModelElements();
			} else {
				IDCUtils.debugTemp("IDCFormula.evaluateNumericalOperator(): form1 = " + form1 + " / form2 = " + form2);
				IDCEvalData op1 = form1.evaluate(context);
				IDCEvalData op2 = form2.evaluate(context);
				ret = evaluate(op1, op2);
			}
		}
		
		return ret;
		
	}
	
    /************************************************************************************************/

	public IDCEvalData evaluate(IDCData data) {
		
		IDCEvalData ret = null;
		
		IDCFormulaContext context = new IDCFormulaContext(data);
		ret = evaluate(context);
		
		return ret;
	}

	/************************************************************************************************/

	public IDCEvalData evaluateModelElements() {
		
		IDCEvalData ret = null;
		
		IDCUtils.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		IDCUtils.debug(display(""));
		
		IDCEvalData op1 = form1.evaluate(context);
		if(op1 != null) {
			Object val1 = op1.getValue();
			if(val1 != null) {
				if(val1 instanceof IDCData) {
					IDCFormulaContext childContext = context.getChildContext((IDCData) val1);
					ret = form2.evaluate(childContext);
				} else {
					
					List<IDCEvalData> evalDataList = new ArrayList<IDCEvalData>();
					for(Object obj1 : (List<?>) val1) {
						if(obj1 instanceof IDCData) {
							IDCFormulaContext childContext = context.getChildContext((IDCData) obj1);
							IDCEvalData op2 = form2.evaluate(childContext); 
							if(op2.isList()) {
								for(Object obj2 : (List<?>) op2.getValue()) {
									evalDataList.add((IDCEvalData)obj2);
								}
							} else {
								evalDataList.add(op2);
							}
						}
					}
					ret = new IDCEvalData(evalDataList, IDCEvalData.LIST);
				}
			}
		}
		
		
		return ret;
		
	}

	/************************************************************************************************/

	public IDCEvalData evaluate(IDCEvalData op1, IDCEvalData op2) {

		IDCEvalData ret = null;
		
		switch(oper.operTypeIndx) {
		
			case IDCExpression.COMPARE_OPERATORS_IND:
				ret = evaluateCompareOperators(op1, op2);
				break;
				
			case IDCExpression.LOGIC_OPERATORS_IND:
				ret = evaluateLogicalOperators(op1, op2);
				break;
				
			case IDCExpression.NUMERICAL_OPERATORS_IND:
				ret = evaluateNumericalOperator(op1, op2);
				break;
				
			case IDCExpression.STRING_OPERATORS_IND:
				ret = evaluateStringOperators(op1, op2);
				break;
				
			case IDCExpression.ASSIGN_OPERATORS_IND:
				ret = evaluateAssignOperators(op1, op2);
				break;
				
		}
		
    	return ret;
    	
    }

    /************************************************************************************************/

	public IDCEvalData evaluateStringOperators(IDCEvalData op1, IDCEvalData op2) {
    	
    	String ret = "";
    	
    	if(oper.operIndx == IDCExpression.CONCAT) {
    		
        	if(op1 != null) {
    			ret = "" + op1.getDisplayValue();
    		}

    		if(op2 != null) {
    			ret += op2.getDisplayValue();
    		}

		}

    	return new IDCEvalData(ret);
    	
    }
	
    /************************************************************************************************/

	public IDCEvalData evaluateAssignOperators(IDCEvalData op1, IDCEvalData op2) {
    	
		IDCEvalData ret = new IDCEvalData(null);
		
		IDCData data = (IDCData) op1.getData();
		IDCAttribute attr = op1.getAttribute();
		
		Object value = op2.getValue();
		
		switch(oper.operIndx) {
		
			case IDCExpression.ASSIGN:
	    		data.set(attr, value);    		
				break;
				
			case IDCExpression.ADDTO:
	    		if(attr.isList() || attr.isNameSpace()) {
	    			List<IDCData> listValue = data.getList(attr.getName());
	    			listValue.add((IDCData) value);
	        		data.set(attr, listValue);    		
	    		} else if(attr.isText()) {
	    			String textValue = data.getString(attr.getName());
	    			if(textValue == null || textValue.length() == 0) {
		    			textValue = "" + value;
	    			} else {
		    			textValue += "" + IDCExpression.EOL + value;
	    			}
	        		data.set(attr, textValue);    		
	    		} else if(attr.isString()) {
	    			String textValue = data.getString(attr.getName());
	    			textValue += "" + value;
	        		data.set(attr, textValue);    		
	    		}
				break;
				
		}
		
		if(context.isSave && !data.isSaving()) {
    		data.save();
		}

		return ret;
    	
    }
	
    /************************************************************************************************/
    
    public void applyUpdateAttribute(IDCData data, IDCAttribute attr, Object value, int operator) {

    	if(operator == IDCTriplet.EQUALS) {
    		IDCUtils.debug(">>> value = " + value);
    	} else if(operator == IDCTriplet.ADDTO) {
    		
    	} 
    }

    /************************************************************************************************/

	public IDCEvalData evaluateNumericalOperator(IDCEvalData op1, IDCEvalData op2) {
    	
		IDCUtils.debugTemp("IDCFormula.evaluateNumericalOperator(): op1 = " + op1 + " / op2 = " + op2);
		
		if(op2 != null && op2.getDoubleValue() == 0) {
			IDCUtils.debugTemp("IDCFormula.evaluateNumericalOperator(): op1 = " + op1 + " / op2 = " + op2);
		}
		
		Object ret = null;
		int retType = -1;
    	
		if(op1 != null && op2 != null) {

			boolean errors=false;
			
			Class op1Class = op1.getValueClass();
			Class op2Class = op2.getValueClass();
			
			if(op1Class == List.class && op2Class == List.class) {
				
				int max1 = ((List) op1.getValue()).size();
				int max2 = ((List) op2.getValue()).size();
				
				if(max1 == max2) {
					
					ret = new ArrayList<IDCEvalData>();
					List list1 = (List) op1.getValue();
					List list2 = (List) op2.getValue();
					for(int nVal=0; nVal < max1; nVal++) {
						Object val1 = list1.get(nVal);
						Object val2 = list2.get(nVal);
						op1.setValue(val1);
						op2.setValue(val2);
						IDCEvalData val = evaluateNumericalOperator(op1, op2);
						((List<IDCEvalData>)ret).add(val);
					}
				}
				
			} else if((op1Class == Long.class || op1Class == Integer.class) && (op2Class == Long.class || op2Class == Integer.class)) {
				
				Long long1 = Long.parseLong(""+op1.getValue());
				Long long2 = Long.parseLong(""+op2.getValue());
				
				retType = IDCAttribute.INTEGER;
			
				switch(oper.operIndx) {
				
					case IDCExpression.PLUS:
						ret = long1 + long2;
						break;
						
					case IDCExpression.MINUS:
						ret = long1 - long2;
						break;
						
					case IDCExpression.MULTIPLY:
						ret = long1 * long2;
						break;
						
					case IDCExpression.DIVIDE:
						ret = long1 / long2;
						break;

					default:
						errors=true;
						break;
						
				}
			
			} else { 
			
				Double double1 = null;
				Double double2 = null;
				Double doubleRet = null;
				
				retType = IDCAttribute.PRICE;
				
				try {
					
					double1 = Double.parseDouble(""+op1.getValue());
					double2 = Double.parseDouble(""+op2.getValue());

					switch(oper.operIndx) {
					
						case IDCExpression.PLUS:
							doubleRet = double1 + double2;
							break;
							
						case IDCExpression.MINUS:
							doubleRet = double1 - double2;
							break;
							
						case IDCExpression.MULTIPLY:
							doubleRet = double1 * double2;
							break;
							
						case IDCExpression.DIVIDE:
							doubleRet = double1 / double2;
							break;
							
						default:
							errors=true;
							break;
							
					}
					
					ret = doubleRet.longValue();

				} catch(Exception e) {
					errors = true;
				}
			
			
			}
			
			if(errors) {
				ret = null;
			}

		}
			
    	return new IDCEvalData(ret, retType);
    	
    }
	
    /************************************************************************************************/

	public IDCEvalData evaluateNumericalOperatorOld(IDCEvalData op1, IDCEvalData op2) {
    	
		Object ret = null;
    	
		if(op1 != null && op2 != null) {

			boolean errors=false;
			
			Double op1Val = op1.getDoubleValue();
			Object op2Val = op2.getDoubleValue();
			
				switch(oper.operIndx) {
				
					case IDCExpression.PLUS:
						ret = ((Double) op1Val) + ((Double) op2Val);
						break;
						
					case IDCExpression.MINUS:
						ret = ((Double) op1Val) - ((Double) op2Val);
						break;
						
					case IDCExpression.MULTIPLY:
						ret = ((Double) op1Val) * ((Double) op2Val);
						break;
						
					case IDCExpression.DIVIDE:
						double op2ValDouble = ((Double) op2Val).doubleValue();
						if(op2ValDouble != 0) {
							ret = ((Double) op1Val) / op2ValDouble;
						}
						break;

					default:
						errors=true;
						break;
					
			}

		}
			
    	return new IDCEvalData(ret);
    	
    }
	
    /************************************************************************************************/

	public IDCEvalData evaluateLogicalOperators(IDCEvalData op1, IDCEvalData op2) {

		boolean ret = false;
		
		if(op1 != null && op2 != null) {

			boolean b1 = (Boolean) IDCAttribute.getValue(IDCAttribute.BOOLEAN, op1.getValue());
			boolean b2 = (Boolean) IDCAttribute.getValue(IDCAttribute.BOOLEAN, op2.getValue());
			
			if(oper.operIndx == IDCExpression.AND) {
				ret = b1 && b2;
			} else if(oper.operIndx == IDCExpression.OR) {
				ret = b1 || b2;
			}
			
		}
		
    	return new IDCEvalData(new Boolean(ret));
    	
    }

    /************************************************************************************************/

	public IDCEvalData evaluateCompareOperators(IDCEvalData op1, IDCEvalData op2) {

		IDCEvalData ret = null;
		
		boolean retValue = true;
		
		if(op1 == null) {
			
			if(oper.operIndx != IDCExpression.EQUALS || op2 != null) {
				retValue = false;
			}
			
		} else if (op2 == null) {
			retValue = false;
		} else if(op1.isList() && op2.isList()) {
			
			List list1 = (List) op1.getValue();
			List list2 = (List) op2.getValue();
			
			if(list1.size() == list2.size()) {
				
				for(int i=0, max=list1.size(); i < max && retValue == true; i++) {
					Object o1 = list1.get(i);
					Object o2 = list2.get(i);
					retValue = evaluateCompareOperatorsPart2(o1, o2);
				}
			}
			
		} else {
		
			retValue = evaluateCompareOperatorsPart2(op1.getValue(), op2.getValue());
			
		}
		
		ret = new IDCEvalData(new Boolean(retValue));

    	return ret;
    	
    }

    /************************************************************************************************/

	public boolean evaluateCompareOperatorsPart2(Object o1, Object o2) {

		boolean ret = false;
		
		int comp = IDCUtils.compare(o1, o2, oper.operIndx);
		
		switch(oper.operIndx) {
			
			case IDCExpression.EQUALS:
			case IDCExpression.EQUALS_IGNORE_CASE:
			case IDCExpression.STARTS_WITH:
				ret = (comp == 0);
				break;
				
			case IDCExpression.NOTEQUALS:
				ret = (comp != 0);
				break;
				
			case IDCExpression.GREATER:
				ret = (comp == 1);
				break;

			case IDCExpression.GREATEREQUAL:
				ret = (comp == 0 || comp == 1);
				break;
				
			case IDCExpression.LESS:
				ret = (comp == -1);
				break;

			case IDCExpression.LESSEQUAL:
				ret = (comp == 0 || comp == -1);
				break;
				
		}

    	return ret;
    	
    }

    /************************************************************************************************/

	public static String getSQLFilter(String exprStr, IDCType type) {
		
		String ret = null;
		
		if(exprStr != null && exprStr.length() > 0) {
			IDCFormula form = getFormula(exprStr);
			ret = form.getSQLFilter(type);		
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public String getSQLFilter(IDCType type) {
		
		String ret = null;
		
		if(oper == null) {
			if(expr != null) {
				ret = expr.getSQLFilter(type);
			}
		} else {
			String sqlStr1 = form1.getSQLFilter(type);
			String sqlStr2 = form2.getSQLFilter(type);
			ret = getSQLFilter(sqlStr1, sqlStr2);
		}
		
		return ret;

	}
	
	/************************************************************************************************/

	public String getSQLFilter(String sqlStr1, String sqlStr2) {

		String ret = null;
		
		switch(oper.operTypeIndx) {
		
			case IDCExpression.COMPARE_OPERATORS_IND:
				ret = getSQLForCompareOperators(sqlStr1, sqlStr2);
				break;
				
			case IDCExpression.LOGIC_OPERATORS_IND:
				ret = getSQLForLogicalOperators(sqlStr1, sqlStr2);
				break;
				
			case IDCExpression.NUMERICAL_OPERATORS_IND:
				ret = getSQLForNumericalOperator(sqlStr1, sqlStr2);
				break;
				
			case IDCExpression.STRING_OPERATORS_IND:
				ret = getSQLForStringOperators(sqlStr1, sqlStr2);
				break;
				
			case IDCExpression.ASSIGN_OPERATORS_IND:
				ret = getSQLForAssignOperators(sqlStr1, sqlStr2);
				break;
				
		}
		
    	return ret;
    	
    }

	/************************************************************************************************/

	private String getSQLForCompareOperators(String sqlStr1, String sqlStr2) {
		
		String ret = null;
		
		switch(oper.operIndx) {
		
			case IDCExpression.EQUALS:
				ret = sqlStr1 + " = " + sqlStr2;
				break;
				
			case IDCExpression.NOTEQUALS:
				ret = sqlStr1 + " <> " + sqlStr2;
				break;
				
			case IDCExpression.GREATER:
				ret = sqlStr1 + " > " + sqlStr2;
				break;
	
			case IDCExpression.GREATEREQUAL:
				ret = sqlStr1 + " >= " + sqlStr2;
				break;
				
			case IDCExpression.LESS:
				ret = sqlStr1 + " < " + sqlStr2;
				break;
	
			case IDCExpression.LESSEQUAL:
				ret = sqlStr1 + " <= " + sqlStr2;
				break;
				
		}

		return ret;

	}
	
	/************************************************************************************************/

	private String getSQLForNumericalOperator(String sqlStr1, String sqlStr2) {
		return sqlStr1 + " " + oper.exprStr + " " +sqlStr2;
	}

	/************************************************************************************************/

	private String getSQLForStringOperators(String sqlStr1, String sqlStr2) {
		return sqlStr1 + " " + oper.exprStr + " " +sqlStr2;
	}

	/************************************************************************************************/

	private String getSQLForAssignOperators(String sqlStr1, String sqlStr2) {
		return sqlStr1 + " " + oper.exprStr + " " +sqlStr2;
	}
	
	/************************************************************************************************/

	private String getSQLForLogicalOperators(String sqlStr1, String sqlStr2) {
		return sqlStr1 + " " + oper.exprStr + " " +sqlStr2;
	}
	
	/************************************************************************************************/

	public List<IDCType> getReferencedTypes(IDCType type) {

		List<IDCType> ret = new ArrayList<IDCType> ();
		
		IDCAttribute attr = type.getAttribute(expr.exprStr);
		if(attr != null) {
			for(IDCReference ref : attr.getReferences()) {
				ret.add(ref.getDataType());
			}
		}
		
		
		
		
		
		return ret;
		
	}

}
