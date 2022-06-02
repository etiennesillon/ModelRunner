package com.indirectionsoftware.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCCSVImport {
    
	IDCApplication appl;

	BufferedReader reader;
	
	IDCType type, parentType;
	IDCAttribute parentAttr;
	
	List<Ref> refs;
	
	/************************************************************************/
    
	public IDCCSVImport(IDCApplication appl, BufferedReader reader, IDCType type, IDCType parentType, IDCAttribute parentAttr) {
		this.appl = appl;
		this.reader = reader;
		this.type = type;
		this.parentType = parentType;
		this.parentAttr = parentAttr;
	}

	/************************************************************************/
        
    public static void main(String args[]) {
    	
    	if(args.length >= 4) {
    		
    		String propsFileName = args[0];
    		String importFileName = args[1];
    		String appName = args[2];
    		String typeName = args[3];
    		
    		IDCDbManager dbManager = IDCDbManager.getIDCDbAdminManager(propsFileName, true);
    		if(dbManager != null) {
    			
    			IDCApplication appl = dbManager.getApplication(appName);
    			if(appl != null) {
    				
    				appl.connect();

    				IDCType type = appl.getType(typeName);

    				IDCType parentType = null;
    				IDCAttribute parentAttr = null;
    	    		if(args.length == 6) {
    	    			parentType = appl.getType(args[4]);
    	    			if(parentType != null) {
    	    				parentAttr = parentType.getAttribute(args[5]);
    	    			}
    	    		}

    				if(type != null) {
    					
    					BufferedReader in;
    					try {
    						in = new BufferedReader(new FileReader(new File(importFileName)));
    						IDCCSVImport imp = new IDCCSVImport(appl, in, type, parentType, parentAttr);
    						imp.process();
    					} catch (FileNotFoundException e) {
    						IDCUtils.debug("Error reading file " + importFileName);
    					}

    				} else {
    					IDCUtils.debug("Error loading type " + typeName);
    				}
    				
    			
    			} else {
    				IDCUtils.debug("Error loading application ...");
    			}
    		
    		}

		} else {
			IDCUtils.debug("Incorrect parameters ...");
    	}
    	
    }
    
	/************************************************************************/
    
    public void process() {
        	
    	String line;
		try {
			
			line = reader.readLine();
			refs = getRefs(line);

	    	int nLine = 1; 
	    	line = reader.readLine();
	    	
	    	while(line != null) {
	    		
	    		String[] cols = line.split(",");
		    	
	    		if(cols.length == refs.size()) {
		    	
	    			IDCData data = type.getNewObject();
	    			ObjRef topRef = new ObjRef(null, null, data);
	    			ObjRef objRef = topRef;
	    			
		    		int nCol = 0;
			    	for(String colValue : cols) {
			    		
			    		colValue = colValue.trim();
			    		
			    		if(colValue.startsWith("{")) {
			    			colValue = colValue.substring(1, colValue.length()-1);
			    		}

			    		String[] values = colValue.split("\\|");
			    		
			    		Ref ref = refs.get(nCol);
			    		
			    		IDCType refType = type;
			    		IDCAttribute refAttr = ref.attr;
		    			IDCData refData = data;
			    		
		    			for(String value : values) {
		    				
			    			int nSubRef=0;
			    			boolean looping = true;
				    		while(looping) {
				    			
		    					int refAttrType = (refAttr == null ? IDCAttribute.NAMESPACE : refAttr.getAttributeType());
				    			
					    		switch(refAttrType) {
					    		
						    		case IDCAttribute.REF:
						    		case IDCAttribute.LIST:
						    		case IDCAttribute.NAMESPACE:
						    			
						    			ObjRef newRef = objRef.attrsMap.get(refAttr);
						    			if(newRef == null) {
						    				refType = ref.subRefs.get(nSubRef).type;
						    				refData = refType.getNewObject();
						    				newRef = new ObjRef(objRef, refAttr, refData);
						    				objRef.attrsMap.put(refAttr, newRef);
						    				refAttr = ref.subRefs.get(nSubRef).attr;
						    			} else {
						    				refType = ref.subRefs.get(nSubRef).type;
						    				refAttr = ref.subRefs.get(nSubRef).attr;
						    				refData = newRef.data;
						    			}
						    			objRef = newRef;
						    			nSubRef++;
						    			break;
						    			
					    			default:
					    				if(nSubRef == 0 || (value != null && value.length() > 0)) {
						    				refData.set(refAttr, value);
						    				objRef.attrsMap.put(refAttr, null);
					    				} else {
					    					topRef.attrsMap.remove(ref.attr);
					    				}
					    				looping = false;
					    				break;
					    		}
				    		}

		    			}
		    			
			    		
			    		nCol++;
			    		objRef = topRef;
			    	}
			    	
			    	if(topRef.load()) {
			    		
				    	List<IDCError> errors = topRef.save();
			    		if(errors.size() > 0) {
							IDCUtils.debug("Error saving data for line " + nLine );
							for(IDCError err : errors) {
								IDCUtils.debug(">> " + err.getMessage());
							}
			    		}
				    	
			    	}
			    	
	    		} else {
					IDCUtils.debug("Ignoring line " + nLine + " ... incorrect number of columns");
		    	}
		    	nLine++;
	    		line = reader.readLine();
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
	}
    
	/************************************************************************/
    
	private List<Ref> getRefs(String header) {
        	
		List<Ref> ret = new ArrayList<Ref>();
		
    	String[] cols = header.split(",");
    	
    	for(String colValue : cols) {
    		
    		colValue = colValue.trim();
    		
    		if(colValue.indexOf('.') == -1) {
        		IDCAttribute attr = type.getAttribute(colValue.trim());
    			Ref ref = new Ref(attr);
    			ret.add(ref);
    		} else {
    			
    	    	String[] attrNames = colValue.split("\\.");
    	    	String rootAttrName = attrNames[0];

    	    	IDCAttribute attr = null;
    	    	boolean parent = false;
    	    	if(rootAttrName.equals("{Parent}")) {
    	    		parent = true;
    	    	} else {
    	    		attr = type.getAttribute(attrNames[0]);
    	    	}
    	    		
        		if(attr != null || parent) {
        			
        			Ref ref = new Ref(attr);
    				List<SubRef> subRefs = new ArrayList<SubRef>();
    				
    				for(int nAttr=1, maxAttr=attrNames.length; nAttr < maxAttr; nAttr++) {
    					
    					int attrType = (parent ? IDCAttribute.NAMESPACE : attr.getAttributeType());
    					
    		    		switch(attrType) {
    		    		
				    		case IDCAttribute.REF:
				    		case IDCAttribute.LIST:
				    		case IDCAttribute.NAMESPACE:
				    			IDCType refType = null;
				    			if(parent) {
				    				refType = parentType;
				    				parent = false;
				    			} else {
					    			refType = attr.getReferences().get(0).getDataType();
				    			}
				    			attr = refType.getAttribute(attrNames[nAttr]);
				    			if(attr != null) {
				    				SubRef subRef = new SubRef(refType, attr);
				    				subRefs.add(subRef);
				    			} else {
				    				IDCUtils.debug("Incorrect attribute definition: " + colValue + " in " + cols);
				    			}
				    			break;
				    		default:
			    				IDCUtils.debug("Incorrect multi-level attribute name for attribute type: " + colValue);
			    				break;

    		    		}
    				
    				}
    				
    				ref.subRefs = subRefs;

    				ret.add(ref);
    				
        		} else {
    				IDCUtils.debug("Error loading attribute for " + colValue);
        		}
    		}
    	
    	}
		
		return ret;
    	
	}
    
	/************************************************************************/
    
    public class Ref {
    	
    	IDCAttribute attr;
    	List<SubRef> subRefs;
    	
    	public Ref(IDCAttribute attr) {
    		this.attr = attr;
    	}
    	
    	public String toString() {
    		
    		String ret = "\nRef: attr = " + attr;
    		
    		if(subRefs != null) {
    			for(SubRef subRef : subRefs) {
    				ret += subRef;
    			}
    		}
    		
    		return ret;
    	}
    	
    }
    
	/************************************************************************/
    
    public class SubRef {
    	
    	IDCType type;
    	IDCAttribute attr;
    	
    	public SubRef(IDCType type, IDCAttribute attr) {
    		this.type = type;
    		this.attr = attr;
    	}

    	public String toString() {
    		return "\nSubRef: type = " + type + " / attr = " + attr;
    	}
    }
    
	/************************************************************************/
    
    public class ObjRef {
    	
    	IDCData data;
    	Map<IDCAttribute, ObjRef> attrsMap;
    	ObjRef parent;
    	IDCAttribute parentAttr;
    	IDCType type;
    	
    	public ObjRef(ObjRef parent, IDCAttribute parentAttr, IDCData data) {
    		this.parent = parent;
    		this.parentAttr = parentAttr;
    		this.data = data;
    		this.type = data.getDataType();
    		attrsMap = new HashMap<IDCAttribute, ObjRef>();
    	}
    	
		/************************************************************************/
        
    	public ObjRef getTopParent() {
    		return (parent == null ? this : parent.getTopParent());
		}

		/************************************************************************/
        
    	public boolean load() {
    		
    		boolean ret = true;
    		
			for(IDCAttribute attr : attrsMap.keySet()) {

				ObjRef childRef = attrsMap.get(attr);
	    		if(childRef != null) {
					ret = childRef.load();
	    		}

	    		if(ret) {
	    			
		    		switch(attr.getAttributeType()) {
		    		
			    		case IDCAttribute.REF:
			    		case IDCAttribute.REFTREE:
			    		case IDCAttribute.REFBOX:
			    			
			    			IDCDataRef ref = null;
			    			if(childRef.data != null) {
				    			ref = new IDCDataRef(childRef.data);
			    			}
			    			data.set(attr, ref);
			    			break;

			    		case IDCAttribute.LIST:
			    		case IDCAttribute.NAMESPACE:
			    			
			    			data.insertReference(attr, childRef.data);
			    			break;

		    		}

	    		} else {
	    			break;
	    		}
	    		
			}
			
			if(ret) {
				
				List<IDCData> similarData = loadSimilarData();
				
				if(similarData.size() == 1) {
		    		data = similarData.get(0);
				} else if(similarData.size() > 1) {
					IDCUtils.debug("Error loading data: too many matches ...");
					ret = false;
				}

			}
			
			return ret;
    	
    	}

    	/************************************************************************/
        
    	public List<IDCError> save() {
    		
    		List<IDCError> ret = new ArrayList<IDCError>();
			
    		/**************
			for(IDCAttribute attr : attrsMap.keySet()) {
				ObjRef childRef = attrsMap.get(attr);
	    		if(childRef != null) {
					if(attr.isNameSpace()) {
						childRef.data.setNamespaceRef(new IDCDataParentRef(type.getEntityId(), attr.getId(), data.getId()));
					}
	    			ret.addAll(childRef.save());
	    		}
			}
			**************/

    		List<IDCError> errs = data.save();
    		for(IDCError err : errs) {
    			err.msg = "" + data + " / "  + err.msg;
    		}
			ret.addAll(errs);
	    	
	    	return ret;
	    	
		}

		/************************************************************************/
        
    	private List<IDCData> loadSimilarData() {
    		
    		List<IDCData> ret = null;
    		
    		List<IDCAttribute> keys = new ArrayList<IDCAttribute>();
			for(IDCAttribute attr : attrsMap.keySet()) {
				if(attrsMap.get(attr) == null || attr.isKey()) {
					keys.add(attr);
				}
			}
    		
			ret = type.loadSimilarData(data, keys);
			
			return ret;

		}

		/************************************************************************/
        
		public String toString() {
    		
    		String ret = "\nObjRef: data = " + data + " / type = " + type;
    		
    		if(attrsMap != null) {
    			
				ret += "\n- Attribute data:";
    			for(IDCAttribute attr : attrsMap.keySet()) {
    				ObjRef childRef = attrsMap.get(attr);
    				ret += "\n" + attr + " -> " + (childRef == null ? "null" : childRef.data);
    			}
    			
    			for(IDCAttribute attr : attrsMap.keySet()) {
    				ObjRef childRef = attrsMap.get(attr);
    				if(childRef != null) {
    					ret += "\n- Attribute " + attr;
    					ret += childRef;
    				}
    			}
    		}
    		
    		return ret;
    	}
    	
    }
    
} 
