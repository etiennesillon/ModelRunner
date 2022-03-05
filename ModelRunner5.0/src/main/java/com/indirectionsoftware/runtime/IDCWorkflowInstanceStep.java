package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWorkflowInstanceStep {
	
	// Workflow Step Instance
	public final static String WORKFLOW_STEP_INSTANCE_TYPE = "$$WorkflowStepInstance"; 
	public final static int WORKFLOW_STEP_INSTANCE_STATUS=0, WORKFLOW_STEP_INSTANCE_STARTEDAT=1, WORKFLOW_STEP_INSTANCE_COMPLETEDAT=2, WORKFLOW_STEP_INSTANCE_ELAPSEDTIME=3, WORKFLOW_STEP_INSTANCE_STEP=4; 
	public final static int WORKFLOW_STEP_INSTANCE_PAUSED=0, WORKFLOW_STEP_INSTANCE_RUNNING=1;	

	// Workflow Step
	public final static int WORKFLOW_STEP_NAME=0, WORKFLOW_STEP_REQS=1, WORKFLOW_STEP_PREDECESSORS=2, WORKFLOW_STEP_ACTIONS=3; 

	// Workflow Action
	public final static int WORKFLOW_ACTION_NAME=0, WORKFLOW_ACTION_OBJECT_FORMULA=1, WORKFLOW_ACTION_ACTION_FORMULA=2; 

	// Workflow Step Predecessor
	public final static int WORKFLOW_STEP_PREDECESSOR_STEPS=0, WORKFLOW_STEP_PREDECESSOR_TYPE=1; 

	IDCData data;
	String stepRef;
	IDCWorkflowInstanceData workflowInstance;
		
	/**************************************************************************************************/

	public IDCWorkflowInstanceStep(IDCWorkflowInstanceData workflowInstance, IDCData data) {
		this.workflowInstance = workflowInstance;
		this.data = data;
		this.stepRef = new IDCDataRef(getStep()).toString();
	}

	/**************************************************************************************************/

	public void execute() {
		
		if(!isCompleted()) {
			
			System.out.println("Preparing to execute Step = " + data.getName());
			
			IDCData workflowStep = getStep(); 
			
			boolean isReady = checkPredecessors(workflowStep);
			
			if(isReady) {
				isReady = checkRequirements(workflowStep);
			}
			
			if(isReady) {

				data.set(WORKFLOW_STEP_INSTANCE_STATUS, 1);
				data.set(WORKFLOW_STEP_INSTANCE_STARTEDAT, System.currentTimeMillis());
				data.save(false);

				System.out.println("Executing Step = " + data.getName());
				
				for(IDCData  action : workflowStep.getList(WORKFLOW_STEP_ACTIONS)) {
					
					String objFormula = action.getString(WORKFLOW_ACTION_OBJECT_FORMULA);
					String actionFormula = action.getString(WORKFLOW_ACTION_ACTION_FORMULA);
					System.out.println("Executing objFormula = " + objFormula + " actionFormula = "+ actionFormula);
					
					if(objFormula != null && objFormula.length() > 0) {
						
						Object formulaDataObject = workflowInstance.evaluate(objFormula);
						if(formulaDataObject != null) {

							List<IDCData> formulaListData = null;
							if(formulaDataObject instanceof IDCData) {
								formulaListData = new ArrayList<IDCData>();
								formulaListData.add((IDCData)formulaDataObject);
							} else {
								formulaListData = (List<IDCData>) formulaDataObject;
							}

							for(IDCData formulaData : formulaListData) {
								formulaData.applyUpdates(workflowInstance, actionFormula, null, true);								
							}
							
						} else {
							IDCUtils.error("Could not evaluate Object Formula: " + objFormula);
						}
						
					} else {
						workflowInstance.evaluate(actionFormula);
					}
					
				}
				
				data.set(WORKFLOW_STEP_INSTANCE_STATUS, 0);
				data.set(WORKFLOW_STEP_INSTANCE_COMPLETEDAT, System.currentTimeMillis());
				data.save();									

			}
			
		} else {
			System.out.println("Step = " + data.getName() + " already completed!");
		}

	}
	
	/**************************************************************************************************/

	public boolean checkPredecessors(IDCData workflowStep) {
		
		boolean ret = true;
		
		for(IDCData step : workflowStep.getList(WORKFLOW_STEP_PREDECESSORS)) {
			
			System.out.println("Found Predecessor Type=" + step.getDisplayValue(WORKFLOW_STEP_PREDECESSOR_TYPE));
			boolean foundPredecessorStepCompleted = false;
			boolean foundPredecessorStepNotCompleted = false;
			for(IDCData predStep : step.getList(WORKFLOW_STEP_PREDECESSOR_STEPS)) {
				System.out.println("Predecessor Step = " + predStep.getName());
				IDCWorkflowInstanceStep predStepInstance = workflowInstance.getPredecessorStepInstance(new IDCDataRef(predStep).toString());
				if(predStepInstance != null && predStepInstance.isCompleted()) {
					System.out.println("Found Completed Predecessor Step Instance = " + predStepInstance.data.getName());
					foundPredecessorStepCompleted = true;
				} else {
					System.out.println("Found Not Completed Predecessor Step Instance = " + predStepInstance.data.getName());
					foundPredecessorStepNotCompleted = true;
				}
			}
			if(step.getString(WORKFLOW_STEP_PREDECESSOR_TYPE).contentEquals("AND")) { // AND
				if(foundPredecessorStepNotCompleted) {
					ret = false;
				}
			} else {																  // OR
				if(!foundPredecessorStepCompleted) {
					ret = false;
				}
			}
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public boolean checkRequirements(IDCData workflowStep) {
		
		boolean ret = true;
		
		for(IDCData req : workflowStep.getList(WORKFLOW_STEP_REQS)) {
			
			System.out.println("Requirement Key = " + req.getName());
			
			String key = req.getString(IDCWorkflowInstanceData.KEY_FORMULA_PAIR_KEY);
			String typeName = req.getString(IDCWorkflowInstanceData.KEY_FORMULA_PAIR_TYPE_NAME);
			String formula = req.getString(IDCWorkflowInstanceData.KEY_FORMULA_PAIR_FORMULA);
			boolean foundReq = false;			
			IDCData contextData = workflowInstance.getContextData(key);			
			if(contextData != null) {
				String ref = contextData.getString(IDCWorkflowInstanceData.WORKFLOW_CONTEXT_REF);
				if(ref != null && ref.length() > 0) {
					System.out.println("found required context data key " + key);
					IDCData contextDataObject = workflowInstance.getApplication().loadDataRef(IDCDataRef.getRef(contextData.getString(IDCWorkflowInstanceData.WORKFLOW_CONTEXT_REF)));
					if(contextDataObject != null && contextDataObject.getDataType().getName().equals(typeName)) {
						System.out.println("found required context data" + contextDataObject);
						if(formula != null && formula.length() > 0) {
							foundReq = contextDataObject.isTrue(formula);
							System.out.println("formula = " + formula + " foundReq = " + foundReq);						
						} else {
							foundReq = true;
							System.out.println("No formula foundReq = " + foundReq);						
						}
					}
				}
			}
					
			if(!foundReq) {
				System.out.println("Couldn't find required context data key=" + key);
				ret = false;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCData getStep() {
		return data.getData(WORKFLOW_STEP_INSTANCE_STEP);
	}

	/**************************************************************************************************/

	public boolean isCompleted() {
		return data.getLong(WORKFLOW_STEP_INSTANCE_COMPLETEDAT) != 0;
	}

}
