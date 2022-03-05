package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWorkflowInstanceData extends IDCData {

	// Worfklow
	public final static String WORKFLOW_TYPE = "$$Workflow"; 
	public final static String WORKFLOW_ACTION_TYPE = "$$WorkflowAction"; 
	public final static String WORKFLOW_STEP_TYPE = "$$WorkflowStep"; 
	public final static String WORKFLOW_KFP_TYPE = "$$KeyFormulaPair"; 
	
	public final static String WORKFLOW_NAME = "Name"; 
	public final static int WORKFLOW_REQS=1, WORKFLOW_CONTEXT=2, WORKFLOW_STEPS=3,WORKFLOW_INSTANCES=4; 

	// Workflow Instance
	public final static String WORKFLOW_INSTANCE_TYPE = "$$WorkflowInstance"; 
	public final static int WORKFLOW_INSTANCE_STATUS=0, WORKFLOW_INSTANCE_STARTEDAT=1, WORKFLOW_INSTANCE_COMPLETEDAT=2, WORKFLOW_INSTANCE_ELAPSEDTIME=3, WORKFLOW_INSTANCE_WORKFLOW=4, WORKFLOW_INSTANCE_STEPS=5, WORKFLOW_INSTANCE_CONTEX=6; 
	public final static int WORKFLOW_INSTANCE_PAUSED=0, WORKFLOW_INSTANCE_RUNNING=1;	
	
	// Key Formula Pair (Workflow Context, Workflow and Workflow Step Requirement, etc)
	public final static int KEY_FORMULA_PAIR_KEY=0,KEY_FORMULA_PAIR_TYPE_NAME=1, KEY_FORMULA_PAIR_FORMULA=2, KEY_FORMULA_PAIR_MANDATORY=3, KEY_FORMULA_PAIR_MESSAGE=4; 

	// Workflow (instance) Context
	public final static String WORKFLOW_CONTEXT_TYPE = "$$WorkflowContext"; 
	public final static int WORKFLOW_CONTEXT_KEY=0, WORKFLOW_CONTEXT_REF=1; 
	public final static String WORKFLOW_CONTEXT_REF_NAME = "Reference"; 
	
	List<IDCWorkflowInstanceStep> 	steps = new ArrayList<IDCWorkflowInstanceStep>();
	
	/**************************************************************************************************/

	public static IDCData getNewWorkflow(IDCApplication appl, String name) {
		
		IDCData ret = null;
		
		IDCType workflowType = appl.getType(WORKFLOW_TYPE);
		ret = workflowType.createData();

		return ret;
		
	}
	
	/**************************************************************************************************/

	public static IDCWorkflowInstanceData getNewWorkflowInstance(IDCApplication appl, String name) {
		
		IDCWorkflowInstanceData ret = null;
		
		IDCType workflowType = appl.getType(WORKFLOW_TYPE);
		IDCData workflow = workflowType.requestSingleData(WORKFLOW_NAME + " == '" + name + "'");
		
		if(workflow != null) {
			ret = getNewWorkflowInstance(appl, workflow);
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public static IDCWorkflowInstanceData getNewWorkflowInstance(IDCApplication appl, IDCData workflow) {
		
		IDCWorkflowInstanceData ret = null;
				
		IDCType workflowInstanceType = appl.getType(WORKFLOW_INSTANCE_TYPE);
		ret = new IDCWorkflowInstanceData(workflowInstanceType);
		ret.setValue(WORKFLOW_INSTANCE_WORKFLOW, workflow.getDataRef());
		
		ret.initStepsFromWorkflow(workflow);
		ret.save(false);
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public IDCWorkflowInstanceData(IDCType workflowInstanceType) {
		super(workflowInstanceType, true);
	}
	
    /************************************************************************************************/

    public void initStepsFromWorkflow(IDCData data) {
    	
		for(IDCData workflowStep : data.getList(WORKFLOW_STEPS)) {
			addStepFromWorkflow(workflowStep);
		}
    	
    }	
    	
	/**************************************************************************************************/

	public void addStepFromWorkflow(IDCData workflowStep) {
		
		IDCData workflowStepInstance = getApplication().getType(IDCWorkflowInstanceStep.WORKFLOW_STEP_INSTANCE_TYPE).getNewObject();
		workflowStepInstance.setValue(IDCWorkflowInstanceStep.WORKFLOW_STEP_INSTANCE_STEP, workflowStep.getDataRef());
		workflowStepInstance.save();
		insertReference(WORKFLOW_INSTANCE_STEPS, workflowStepInstance);
		steps.add(new IDCWorkflowInstanceStep(this, workflowStepInstance));

	}
	
	/**************************************************************************************************/

	public List<IDCError> execute() {
		
		List<IDCError> ret = checkRequirements();

		if(ret.size() == 0) {
			
			System.out.println("Executing Workflow: " + getName());
			
			set(WORKFLOW_INSTANCE_STATUS, WORKFLOW_INSTANCE_RUNNING);
			save(false);

			for(IDCWorkflowInstanceStep step : steps) {
				step.execute();
			}

			set(WORKFLOW_INSTANCE_STATUS, WORKFLOW_INSTANCE_PAUSED);
			
			if(isCompleted()) {
				set(WORKFLOW_INSTANCE_COMPLETEDAT, System.currentTimeMillis());
			}
			save(false);

		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public List<IDCError> checkRequirements() {
		
		List<IDCError> ret = new ArrayList<IDCError>(); 

		for(IDCData req : getData(WORKFLOW_INSTANCE_WORKFLOW).getList(WORKFLOW_REQS)) {
			String key = req.getString(KEY_FORMULA_PAIR_KEY);
			String typeName = req.getString(KEY_FORMULA_PAIR_TYPE_NAME);
			boolean foundReq = false;			
			IDCData contextData = getContextData(key);			
			if(contextData != null) {
				System.out.println("found required context data key " + key);
				IDCData contextDataObject = getApplication().loadDataRef(IDCDataRef.getRef(contextData.getString(WORKFLOW_CONTEXT_REF)));
				if(contextDataObject != null && contextDataObject.getDataType().getName().equals(typeName)) {
					System.out.println("found required context data" + contextDataObject);
					foundReq = true;
				}
			}

					
			if(!foundReq) {
				System.out.println("Couldn't find required context data key=" + key);
				ret.add(new IDCError(IDCError.WORKFLOW_REQUS_NOT_MET, req.getString(KEY_FORMULA_PAIR_MESSAGE)));
			}

		}

		return ret;
		
	}

	/**************************************************************************************************/

	public boolean isCompleted() {
		
		boolean ret = true;
		
		if(getLong(WORKFLOW_INSTANCE_COMPLETEDAT) == 0) {
			for(IDCWorkflowInstanceStep step : steps) {
				if(!step.isCompleted()) {
					ret = false;
					break;
				}
			}
		};
				
		return ret;
		
	}

	/**************************************************************************************************/

	public void addContextData(String key, IDCDataRef ref) {
		
		IDCType workflowContextType = getApplication().getType(WORKFLOW_CONTEXT_TYPE);
		IDCData workflowContext = workflowContextType.getNewObject();
		workflowContext.setValue(WORKFLOW_CONTEXT_KEY, key);
		workflowContext.setValue(WORKFLOW_CONTEXT_REF, ref);
		workflowContext.save(false);
		insertReference(WORKFLOW_INSTANCE_CONTEX, workflowContext);

	}
	
	/**************************************************************************************************/

	public IDCData getContextData(String key) {
		
		IDCData ret = requestChild("Key == '" + key + "'", WORKFLOW_INSTANCE_CONTEX) ; // start by reading from this instance's context
		
		if(ret == null) {																	// if not found, look into the workflow (definition) context ;)
			ret = getData(WORKFLOW_INSTANCE_WORKFLOW).requestChild("Key == '" + key + "'", WORKFLOW_CONTEXT) ; 
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCWorkflowInstanceStep getPredecessorStepInstance(String stepRef) {
		
		IDCWorkflowInstanceStep ret = null;
		
		for(IDCWorkflowInstanceStep step : steps) {
			if(step.stepRef.equals(stepRef)) {
				ret = step;
				break;
			}
		}

		return ret;
		
	}

	/**************************************************************************************************/

	public static List<IDCWorkflowInstanceData> checkWorkflows(IDCData data) {
		
		List<IDCWorkflowInstanceData> ret = new ArrayList<IDCWorkflowInstanceData>();
		
		List<IDCWorkflowInstanceData> instances = new ArrayList<IDCWorkflowInstanceData>();
		
		IDCApplication appl = data.getApplication();
		IDCDataRef dataRef =  new IDCDataRef(data);
		String typeName = data.getDataType().getName();
		
		if(typeName.equalsIgnoreCase(WORKFLOW_TYPE) || typeName.equalsIgnoreCase(WORKFLOW_INSTANCE_TYPE)  || typeName.equalsIgnoreCase(IDCWorkflowInstanceStep.WORKFLOW_STEP_INSTANCE_TYPE)) {
			
		} else if(typeName.equalsIgnoreCase(WORKFLOW_CONTEXT_TYPE)) {
			
		} else {

			IDCUtils.debug("Checking workflow for type = " + typeName);
			
			IDCType workflowContextType = appl.getType(WORKFLOW_CONTEXT_TYPE);
			List<IDCData> contextList = workflowContextType.requestData(WORKFLOW_CONTEXT_REF_NAME + " == '" + dataRef + "'");
			for(IDCData context : contextList) {
				IDCWorkflowInstanceData instData = IDCWorkflowInstanceData.clone(context.getNamespaceParent());
				instances.add(instData);
			}

			IDCType workflowType = appl.getType(WORKFLOW_TYPE);
			for(IDCData workflow : workflowType.loadAllDataObjects()) {
				
				for(IDCData req : workflow.getList(WORKFLOW_REQS)) {
					
					String reqType = req.getString(KEY_FORMULA_PAIR_TYPE_NAME);
					if(reqType.equals(typeName)) {
						
						boolean isNeedNewnstance = true;
						for(IDCWorkflowInstanceData instData : instances) {
//							if(!instData.isCompleted() && instData.getData(WORKFLOW_INSTANCE_WORKFLOW).getId() == workflow.getId() && instData.getInt(WORKFLOW_INSTANCE_STATUS) == WORKFLOW_INSTANCE_PAUSED) {
							if(!instData.isCompleted() && instData.getData(WORKFLOW_INSTANCE_WORKFLOW).getId() == workflow.getId()) {
								isNeedNewnstance = false;
							}
						}

						if(isNeedNewnstance) {
							
							String formula = req.getString(IDCWorkflowInstanceData.KEY_FORMULA_PAIR_FORMULA);
							if(formula != null && formula.length() > 0) {
								isNeedNewnstance = data.isTrue(formula);
							} else {
								isNeedNewnstance = true;
							}
							
							if(isNeedNewnstance) {
								String key = req.getString(KEY_FORMULA_PAIR_KEY);
								IDCWorkflowInstanceData instance = getNewWorkflowInstance(appl, workflow);
								instance.addContextData(key, data.getDataRef());
								instance.set(WORKFLOW_INSTANCE_STARTEDAT, System.currentTimeMillis());
								instance.set(WORKFLOW_INSTANCE_STATUS, WORKFLOW_INSTANCE_PAUSED);
								instances.add(instance);
							}

						}
						
					}

				}
				
			}

		}
		
		for(IDCWorkflowInstanceData instance : instances) {
			if(!instance.isCompleted() && instance.getInt(WORKFLOW_INSTANCE_STATUS) == WORKFLOW_INSTANCE_PAUSED) {
				ret.add(instance);
			}
		}
		
		return ret;
	
	}

    /************************************************************************************************/

    public static IDCWorkflowInstanceData clone(IDCData data) {
    	
    	IDCWorkflowInstanceData ret = new IDCWorkflowInstanceData(data.getDataType());
    	ret.setId(data.getId());
    	ret.setNamespaceParentRef(data.getNamespaceParentRef());
    	ret.copyValues(data.getValues());
		for(IDCData workflowStep : data.getList(WORKFLOW_INSTANCE_STEPS)) {
			ret.steps.add(new IDCWorkflowInstanceStep(ret, workflowStep));
		}
    	
    	return ret;
    	
    }	
    
	/************************************************************************************************/

    public static String getWorkflowJSON(IDCData workflow) {
    	
    	String ret = "";
    	
		ret += "{\"id\":" + workflow.getId() + ", \"values\": [\"" + workflow.getName() + "\"],\"steps\":[";
		
		boolean isFirstStep = true;
		for(IDCData step : workflow.getList("Steps")) {

			ret += (isFirstStep ? "" : ",") + "{\"id\":" + step.getInt("id") + ", \"values\": [\"" + step.getName() + "\"],\"x\":" + step.getInt("x") + ",\"y\":" + step.getInt("y") + ",\"preReqs\":[";
			
			boolean isFirstPreReq = true;
			for(IDCData preReq : step.getList("Requirements")) {

				ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + preReq.getInt("id") + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
				isFirstPreReq = false;
				
			}
			
			ret += "],\"actions\":[";

			boolean isFirstAction = true;
			for(IDCData action : step.getList("Actions")) {

				ret += (isFirstAction ? "" : ",") + "{\"id\":" + action.getInt("id") + ", \"values\": [\"" + action.getName() + "\", \"" + action.getString("ObjectFormula")  + "\", \"" + action.getString("ActionFormula") + "\"],\"x\":" + action.getInt("x") + ",\"y\":" + action.getInt("y") + "}";
				isFirstAction = false;
				
			}
			
			
			ret += "]}";

			isFirstStep = false;
			
		}
		
		ret += "],\"contexts\":[";
				
		boolean isFirstContext = true;
		for(IDCData context : workflow.getList("Context")) {

			ret += (isFirstContext ? "" : ",") + "{\"id\":" + context.getInt("id") + ", \"values\": [\"" + context.getName() + "\", \"" + context.getString("TypeName")  + "\", \"" + context.getString("Formula") + "\"],\"x\":" + context.getInt("x") + ",\"y\":" + context.getInt("y") + "}";
			isFirstContext = false;
			
		}
		
		ret += "],\"preReqs\":[";
		
		boolean isFirstPreReq = true;
		for(IDCData preReq : workflow.getList("Requirements")) {

			ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + preReq.getInt("id") + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
			isFirstPreReq = false;
			
		}
		
		ret += "]}";
		
		return ret;
		
    }
    
	/************************************************************************************************/

    public static String getWorkflowJSONOLD2(IDCData workflow) {
    	
    	String ret = "";
    	
    	long id = workflow.getId();
		
		ret += "{\"id\":" + id++ + ", \"values\": [\"" + workflow.getName() + "\"],\"steps\":[";
		
		boolean isFirstStep = true;
		for(IDCData step : workflow.getList("Steps")) {

			ret += (isFirstStep ? "" : ",") + "{\"id\":" + id++ + ", \"values\": [\"" + step.getName() + "\"],\"x\":" + step.getInt("x") + ",\"y\":" + step.getInt("y") + ",\"preReqs\":[";
			
			boolean isFirstPreReq = true;
			for(IDCData preReq : step.getList("Requirements")) {

				ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + id++ + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
				isFirstPreReq = false;
				
			}
			
			ret += "],\"actions\":[";

			boolean isFirstAction = true;
			for(IDCData action : step.getList("Actions")) {

				ret += (isFirstAction ? "" : ",") + "{\"id\":" + id++ + ", \"values\": [\"" + action.getName() + "\", \"" + action.getString("ObjectFormula")  + "\", \"" + action.getString("ActionFormula") + "\"],\"x\":" + action.getInt("x") + ",\"y\":" + action.getInt("y") + "}";
				isFirstAction = false;
				
			}
			
			
			ret += "]}";

			isFirstStep = false;
			
		}
		
		ret += "],\"contexts\":[";
				
		boolean isFirstContext = true;
		for(IDCData context : workflow.getList("Context")) {

			ret += (isFirstContext ? "" : ",") + "{\"id\":" + id++ + ", \"values\": [\"" + context.getName() + "\", \"" + context.getString("TypeName")  + "\", \"" + context.getString("Formula") + "\"],\"x\":" + context.getInt("x") + ",\"y\":" + context.getInt("y") + "}";
			isFirstContext = false;
			
		}
		
		ret += "],\"preReqs\":[";
		
		boolean isFirstPreReq = true;
		for(IDCData preReq : workflow.getList("Requirements")) {

			ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + id++ + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
			isFirstPreReq = false;
			
		}
		
		ret += "]}";
		
		return ret;
		
    }
    
	/************************************************************************************************/

    public static String getWorkflowJSONOLD(IDCData workflow) {
    	
    	String ret = "";
		
		ret += "{\"id\":" + workflow.getId() + ", \"values\": [\"" + workflow.getName() + "\"],\"steps\":[";
		
		boolean isFirstStep = true;
		for(IDCData step : workflow.getList("Steps")) {

			ret += (isFirstStep ? "" : ",") + "{\"id\":" + step.getId() + ", \"values\": [\"" + step.getName() + "\"],\"x\":" + step.getInt("x") + ",\"y\":" + step.getInt("y") + ",\"preReqs\":[";
			
			boolean isFirstPreReq = true;
			for(IDCData preReq : step.getList("Requirements")) {

				ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + preReq.getId() + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
				isFirstPreReq = false;
				
			}
			
			ret += "],\"actions\":[";

			boolean isFirstAction = true;
			for(IDCData action : step.getList("Actions")) {

				ret += (isFirstAction ? "" : ",") + "{\"id\":" + action.getId() + ", \"values\": [\"" + action.getName() + "\", \"" + action.getString("ObjectFormula")  + "\", \"" + action.getString("ActionFormula") + "\"],\"x\":" + action.getInt("x") + ",\"y\":" + action.getInt("y") + "}";
				isFirstAction = false;
				
			}
			
			
			ret += "]}";

			isFirstStep = false;
			
		}
		
		ret += "],\"contexts\":[";
				
		boolean isFirstContext = true;
		for(IDCData context : workflow.getList("Context")) {

			ret += (isFirstContext ? "" : ",") + "{\"id\":" + context.getId() + ", \"values\": [\"" + context.getName() + "\", \"" + context.getString("TypeName")  + "\", \"" + context.getString("Formula") + "\"],\"x\":" + context.getInt("x") + ",\"y\":" + context.getInt("y") + "}";
			isFirstContext = false;
			
		}
		
		ret += "],\"preReqs\":[";
		
		boolean isFirstPreReq = true;
		for(IDCData preReq : workflow.getList("Requirements")) {

			ret += (isFirstPreReq ? "" : ",") + "{\"id\":" + preReq.getId() + ", \"values\": [\"" + preReq.getName() + "\", \"" + preReq.getString("TypeName")  + "\", \"" + preReq.getString("Formula") + "\"],\"x\":" + preReq.getInt("x") + ",\"y\":" + preReq.getInt("y") + "}";
			isFirstPreReq = false;
			
		}
		
		ret += "]}";
		
		return ret;
		
    }
    
    //	public final static int WORKFLOW_REQS=1, WORKFLOW_CONTEXT=2, WORKFLOW_STEPS=3; 

	/************************************************************************************************/

	public static String updateWorkflowFromJSON(IDCApplication appl, long itemId, String jsonStr) {
		
		String ret = null;
		
		IDCType workflowType = appl.getType(WORKFLOW_TYPE);
		IDCType stepType = appl.getType(WORKFLOW_STEP_TYPE);
		IDCType kvpType = appl.getType(WORKFLOW_KFP_TYPE);
		IDCType actionType = appl.getType(WORKFLOW_ACTION_TYPE);
		
		JSONObject jsonWorkflow = IDCUtils.getJSONObject(jsonStr);
		if(jsonWorkflow != null) {
			
			String wfName = IDCUtils.getStringsFromJSONArray(jsonWorkflow, "values").get(0);
			if(wfName != null) {
				
				IDCData oldWorkflow = workflowType.requestSingleData(WORKFLOW_NAME + " == '" + wfName + "'");
				
				IDCData workflow = appl.createData(workflowType, null, false);
				workflow.set("Name", wfName);	

				try {
					
					IDCDataParentRef stepsParentRef = workflow.getAsParentRef("Steps");
					
					JSONArray jsonSteps = (JSONArray) IDCUtils.getJSONValue(jsonWorkflow, "steps");
		    		for(int i=0; i<jsonSteps.length(); i++) {
		    			
		    			JSONObject jsonStepObj = jsonSteps.getJSONObject(i);
	    				IDCData stepData = appl.createData(stepType, stepsParentRef, false);
						String stepName = IDCUtils.getStringsFromJSONArray(jsonStepObj, "values").get(0);
						stepData.set("Name", stepName);	
						Integer x = (Integer) IDCUtils.getJSONValue(jsonStepObj, "x");
						stepData.set("x", x);	
						Integer y = (Integer) IDCUtils.getJSONValue(jsonStepObj, "y");
						stepData.set("y", y);	
						int id = (Integer) IDCUtils.getJSONValue(jsonStepObj, "id");
						stepData.set("id", id);	
						stepData.save(false);
						
						IDCDataParentRef actionsParentRef = stepData.getAsParentRef("Actions");
						
						JSONArray jsonActions = (JSONArray) IDCUtils.getJSONValue(jsonStepObj, "actions");
			    		for(int j=0; j<jsonActions.length(); j++) {
			    			
			    			JSONObject jsonActionObj = jsonActions.getJSONObject(j);
		    				IDCData actionData = appl.createData(actionType, actionsParentRef, false);
							List<String> values = IDCUtils.getStringsFromJSONArray(jsonActionObj, "values");
							actionData.set("Name", values.get(0));	
							actionData.set("ObjectFormula", values.get(1));	
							actionData.set("ActionFormula", values.get(2));	
							x = (Integer) IDCUtils.getJSONValue(jsonActionObj, "x");
							actionData.set("x", x);	
							y = (Integer) IDCUtils.getJSONValue(jsonActionObj, "y");
							actionData.set("y", y);	
							id = (Integer) IDCUtils.getJSONValue(jsonActionObj, "id");
							actionData.set("id", id);	
							actionData.save(false);
							
			    	    }
			    		
						IDCDataParentRef preReqstParentRef = stepData.getAsParentRef("Requirements");

						JSONArray jsonPreReqs = (JSONArray) IDCUtils.getJSONValue(jsonStepObj, "preReqs");
			    		for(int k=0; k<jsonPreReqs.length(); k++) {
			    			
			    			JSONObject jsonObj = jsonPreReqs.getJSONObject(k);
		    				IDCData data = appl.createData(kvpType, preReqstParentRef, false);
							List<String> values = IDCUtils.getStringsFromJSONArray(jsonObj, "values");
							data.set("Key", values.get(0));	
							data.set("TypeName", values.get(1));	
							data.set("Formula", values.get(2));	
							x = (Integer) IDCUtils.getJSONValue(jsonObj, "x");
							data.set("x", x);	
							y = (Integer) IDCUtils.getJSONValue(jsonObj, "y");
							data.set("y", y);	
							id = (Integer) IDCUtils.getJSONValue(jsonObj, "id");
							data.set("id", id);	
							data.save(false);
							
			    	    }
			    		
		    	    }

					IDCDataParentRef contextParentRef = workflow.getAsParentRef("Context");
					
					JSONArray jsonContexts = (JSONArray) IDCUtils.getJSONValue(jsonWorkflow, "contexts");
		    		for(int i=0; i<jsonContexts.length(); i++) {
		    			
		    			JSONObject jsonObj = jsonContexts.getJSONObject(i);
	    				IDCData data = appl.createData(kvpType, contextParentRef, false);
						List<String> values = IDCUtils.getStringsFromJSONArray(jsonObj, "values");
						data.set("Key", values.get(0));	
						data.set("TypeName", values.get(1));	
						data.set("Formula", values.get(2));	
						Integer x = (Integer) IDCUtils.getJSONValue(jsonObj, "x");
						data.set("x", x);	
						Integer y = (Integer) IDCUtils.getJSONValue(jsonObj, "y");
						data.set("y", y);	
						int id = (Integer) IDCUtils.getJSONValue(jsonObj, "id");
						data.set("id", id);	
						data.save(false);
						
		    	    }
					
					IDCDataParentRef preReqstParentRef = workflow.getAsParentRef("Requirements");

					JSONArray jsonPreReqs = (JSONArray) IDCUtils.getJSONValue(jsonWorkflow, "preReqs");
		    		for(int i=0; i<jsonPreReqs.length(); i++) {
		    			
		    			JSONObject jsonObj = jsonPreReqs.getJSONObject(i);
	    				IDCData data = appl.createData(kvpType, preReqstParentRef, false);
						List<String> values = IDCUtils.getStringsFromJSONArray(jsonObj, "values");
						data.set("Key", values.get(0));	
						data.set("TypeName", values.get(1));	
						data.set("Formula", values.get(2));	
						Integer x = (Integer) IDCUtils.getJSONValue(jsonObj, "x");
						data.set("x", x);	
						Integer y = (Integer) IDCUtils.getJSONValue(jsonObj, "y");
						data.set("y", y);	
						int id = (Integer) IDCUtils.getJSONValue(jsonObj, "id");
						data.set("id", id);	
						data.save(false);
						
		    	    }
					
					List<IDCError> errors = workflow.save(false); 
					if(errors.size() == 0) {
						if(oldWorkflow != null) {
							oldWorkflow.delete(true);
						}
						ret = "OK";
					} else {
						ret = errors.get(0).msg;
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			
		}
		

		return ret;
		
	}

}
