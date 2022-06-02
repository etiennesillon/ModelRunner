package com.indirectionsoftware.metamodel;

import javax.swing.*;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.runtime.webapp.IDCWebAppController;

import java.awt.event.*;

import java.util.*;

/************************************************************************************************/

    public class IDCRefTree {
    	
    	private IDCApplication app;
    	private JPanel comp;
    	private JLabel label;
    	private IDCAttribute selAttr;
    	
		public IDCSelectBox top, bot;
		public IDCType type;

		/************************************************************************************************/

       	public IDCRefTree(IDCApplication app, IDCAttribute attr, IDCDataRef itemRef) {
            	
   			type = attr.getReferences().get(0).getDataType();
      		
    		this.app = app;
    		this.selAttr = attr;
    		List<IDCAttribute> parentAttrs = attr.getNamespaceTree();
    		List<IDCDataRef> parentRefs = getAllParentRefs(itemRef, parentAttrs);
    		comp = new JPanel();
    		IDCSelectBox parent=null;
    		top = null;
    		
    		for(int nParent = parentAttrs.size()-1; nParent >= 0; nParent--) {
    			
    			IDCAttribute parentAttr = parentAttrs.get(nParent);

    			IDCSelectBox box = new IDCSelectBox(parentAttr);
    			comp.add(box.box);
    			
    			if(top == null) {
    				top = box;
    			}
    			
    			if(parent != null) {
    				parent.child = box;
    			}
    			
    			parent = box;

    		}

    		bot = new IDCSelectBox(type);
			comp.add(bot.box);
			if(parent != null) {
				parent.child = bot;
				top.setModel(null);
				if(parentRefs != null) {
					top.setSelected(parentRefs, 0);
				}
			} else {
				bot.setModel(null);
			}
    		
    		label = new JLabel(attr.getDisplayName());
    		//label.setForeground(attr.getLabelColor());

    	}
    	
		/************************************************************************************************/

        private List<IDCDataRef> getAllParentRefs(IDCDataRef ref, List<IDCAttribute> parentAttrs) {
        	
        	List<IDCDataRef> ret = null;
        	
        	if(ref != null) {
            	List<IDCDataRef> temp = new ArrayList<IDCDataRef>();
        		temp.add(ref);
        		IDCData data = type.loadDataRef(ref);
        		if(data != null) {
            		IDCDataParentRef parentRef = data.getNamespaceParentRef();
            		while(parentRef != null) {
                		ref = new IDCDataRef(parentRef);
                		temp.add(ref);
            			data = type.loadDataRef(ref);
            			parentRef = data.getNamespaceParentRef();
            		}

            		ret = new ArrayList<IDCDataRef>();
            		for(int nRef=temp.size()-1; nRef >= 0; nRef--) {
            			ref = temp.get(nRef);
            			ret.add(ref);
            		}
        		}
        	}
        	
        	return ret;
		
        }

       	/************************************************************************************************/

       	public JComponent getComponent() { return comp;}

    	/************************************************************************************************/

    	public JLabel 	  getLabel() { return label;}

    	public boolean    isTable() { return false; }
   	
		/************************************************************************************************/

		public void  setValue(Object value) {
    		if(value instanceof Integer) {
    			//box.setSelectedIndex(getIndex(((Integer)value).intValue()));
    		}
		}

		/************************************************************************************************/

		public IDCAttribute getAttribute() {
			return selAttr;
		}

		/************************************************************************************************/

		public void setEditable(boolean isEditable) {
		}
    	
    	/************************************************************************************************/
		
    	public Object getValue(boolean check) {
    		return bot.getValue(check);
    	}
    	
		/************************************************************************************************/

		class IDCSelectBox implements ItemListener {
			
			IDCAttribute attr;
			IDCType type;
			JComboBox box;
			int selectedIndex = -1;
			
			IDCSelectBox child = null;
			
	    	private String keys[];
	    	private long ids[];
	    	
	    	/************************************************************************************************/

			public IDCSelectBox(IDCType type) {
				attr = selAttr;
				this.type = type;
				box = new JComboBox();
	    		box.addItemListener(this);
			}

			/************************************************************************************************/

			public IDCSelectBox(IDCAttribute attr) {
				this.attr = attr;
				this.type = attr.getDataType();
				box = new JComboBox();
	    		box.addItemListener(this);
			}

			/************************************************************************************************/

			public void itemStateChanged(ItemEvent arg0) {
				
				if(child != null) {
					
					Object val = getValue(false);
					if(val == null) {
						child.setModel(new ArrayList<IDCData>());
					} else if(val instanceof IDCDataRef) {
						long id = ((IDCDataRef)val).getItemId();
						IDCDataParentRef parentRef = new IDCDataParentRef(type.getEntityId(), attr.getId(), id);
						child.setModel(child.type.loadAllDataObjects(child.type.loadAllDataReferences(parentRef, child.type.getExplorerSQLFilter(), child.type.getExplorerSQLOrderBy(), IDCType.NO_MAX_ROWS)));
					}
				}
				
			}
			
	    	/************************************************************************************************/

			public void setSelected(List<IDCDataRef> parentRefs, int nRef) {

				IDCDataRef ref = parentRefs.get(nRef);
				selectedIndex = getIndex(ref.getItemId());
				box.setSelectedIndex(selectedIndex);

				if(child != null) {
					IDCDataParentRef parentRef = new IDCDataParentRef(ref.getTypeId(), attr.getId(), ref.getItemId());
					child.setModel(child.type.loadAllDataObjects(child.type.loadAllDataReferences(parentRef, child.type.getExplorerSQLFilter(), child.type.getExplorerSQLOrderBy(), IDCType.NO_MAX_ROWS)));
					child.setSelected(parentRefs, ++nRef);
				}
				
			}

	        /************************************************************************************************/

	       	public void setModel(List<IDCData> list) {
	       		
	       		selectedIndex = -1;
	       		
	       		if(list == null) {
	       			list = type.loadAllDataObjects();
	       		}
	       		
	       		int len = list.size();
			
	    		keys = new String[len+1];
	    		ids = new long[len+1];
	    		
	    		keys[0] = IDCData.NOSELECTION;
	    		ids[0] = -1;
	    		
	    		for(int i=0; i< len; i++) {
	    			IDCData data = (IDCData) list.get(i);
	    			keys[i+1] = data.getName();
	    			ids[i+1] = data.getId();
	    		}
	    		
	    		box.setModel(new DefaultComboBoxModel(keys));
	    		
	    		if(child != null) {
	    			child.setModel(new ArrayList<IDCData>());
	    		}
	    		
	       	}
		
	    	/************************************************************************************************/
			
	    	public Object     getValue(boolean check) {

	    		Object ret = null;
	    		
	    		int index = box.getSelectedIndex();

	    		if(selAttr.isMandatory() && index == 0) {
	    			if(check) {
	        			ret = new IDCError(IDCError.MANDATORYATTRIBUTE, "Please select a " + attr.getDesc());
	    			}
	   			} else {
	   				long id = -1;
	   	   			if(index != -1){
	   	   				id = ids[index];
	   	   				if(id != -1) {
	   	   	   	   			ret = new IDCDataRef(type.getEntityId(), id);
	   	   				}
	   				}
	   			}
	    			
	            if(ret == null) {
					comp.requestFocus();
	            }
	            
	    		return ret;
	    		
	    	}
	    	
	        /**********************************************************************/

	        public long getId(String key) {

	            long id = IDCData.NA;

	            for(int i=0; i<keys.length && id == IDCData.NA;i++) {
	               if(key.equals(keys[i])) {
	                  id = ids[i];
	               }
	            }

	            return id;

	        }

	        /**********************************************************************/

	        public int getIndex(long id) {

	            int ind = -1;

	            for(int i=0; i<ids.length && ind == -1;i++) {
	               if(id == ids[i]) {
	                  ind = i;
	               }
	            }

	            return ind;

	        }

	        /**********************************************************************/

			public String getHTML(IDCWebAppContext context, String labelName, String prefix, String fieldName, int nBox, boolean isFilled, boolean isLast) {
				
				String ret = "<select id=IDCField" + context.fieldId++ + " onChange=\"return updateRefTree('IDCWebAppController?" + IDCWebAppController.ACTION_PARM + "=" + IDCWebAppController.RELOADREFTREE  + "&" + IDCWebAppController.CONTEXTID_PARM + "=" + context.contextId + "', '" + labelName + "', '" + prefix + "', " + nBox + ", '" + fieldName + "');\" class='dropdown' name='" + fieldName + "'>";
	    		for(int i=0, max=(isFilled ? keys.length : 1); i<max ; i++) {
		            ret += "<option value='" + new IDCDataRef(type.getEntityId(), ids[i]) + "'" + (i == selectedIndex ? " selected" : "")+ ">" + keys[i] + "</option>";
	    		}
	            ret += "</select>";
	            
	            return ret;

			}

	    	/************************************************************************************************/

			public void setSelectedId(long id) {
				selectedIndex = getIndex(id);
				box.setSelectedIndex(selectedIndex);
				itemStateChanged(null);
			}
	        
		}

    	/************************************************************************************************/

		public String getHTML(IDCWebAppContext context, String prefix, String fieldName) {
			
			String ret = "<label id='";
			String labelName = "REFTREE_" + prefix + fieldName;
			ret += labelName + "'>";
			
			boolean isFilled = true;
			
			if(top != null) {
				IDCSelectBox box = top;
				int nBox = 0;
				do {
					ret += box.getHTML(context, labelName, prefix, fieldName + (box.child == null ? "" : nBox), nBox++, isFilled, (box.child == null));
					if(box.box.getSelectedIndex() == -1) {
						isFilled = false;
					}
					box = box.child;
				} while(box != null);
			} else {
				ret = bot.getHTML(context, labelName, prefix, fieldName, 0, isFilled, true);
			}
			
			ret += "</label>";
			
			return ret;
		}

    	/************************************************************************************************/

		public void updateSelection(int nBox, IDCDataRef ref) {
			
			IDCSelectBox box = (top == null ? bot : top);
			
			while(nBox-- > 0) {
				box = box.child;
			}
			
			if(box != null) {
				box.setSelectedId(ref.getItemId());
			}
			
		}

    }