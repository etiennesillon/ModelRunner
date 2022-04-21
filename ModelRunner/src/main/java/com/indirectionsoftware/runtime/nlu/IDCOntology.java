package com.indirectionsoftware.runtime.nlu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCOntology {
	
	private static final String[] TYPES = {"Term", "Verb", "Misc"};
	static final int UNKNOWN=-1, TERM=0, VERB=1, MISC=2; 

	IDCApplication app;
	
	Map<String, IDCConcept> conceptMap = new HashMap<String, IDCConcept>();
	Map<String, IDCWord> wordMap = new HashMap<String, IDCWord>();
	
	/*****************************************************************************/

	public IDCOntology(IDCApplication app) {
		this.app = app;
	}
	
	/*****************************************************************************/

	public IDCConcept getConcept(String conceptName) {
		
		IDCConcept ret = conceptMap.get(conceptName);

		if(ret == null) {
			ret = new IDCConcept(conceptName);
			conceptMap.put(conceptName, ret);
		}
		
		return ret;
		
	}

	/*****************************************************************************/

	public IDCConcept addModelEntity(String conceptName, IDCModelData modelEntity) {
		
		IDCUtils.debug("IDCOntology.addModelEntity(): conceptName = " + conceptName + " / modelEntity=" + modelEntity);
		
		conceptName = conceptName.toLowerCase();
		
		IDCConcept ret = getConcept(conceptName);

		if(modelEntity != null) {
			ret.addModelEntity(modelEntity);
		}

		IDCWord word = getWord(conceptName, -1);
		word.addConcept(ret);
			
		return ret;
		
	}

	/*****************************************************************************/

	public IDCConcept addModelEntity(IDCModelData modelEntity) {
		return addModelEntity(modelEntity.getName(), modelEntity);
	}

	/*****************************************************************************/

	public IDCWord getWord(String token, int searchType) {
		
		IDCWord ret = wordMap.get(token);
		
		if(ret == null) {
			
			ret = new IDCWord(token);
			if(searchType == -1) {
				wordMap.put(token, ret);
			}
			
			if(searchType != -1) {
				IDCValue value = app.searchValues(token, searchType);
				if(value.attrVals.size() > 0) {
					ret.addValue(value);
				}
			}
			
		}
		
		return ret;
		
	}

	/*****************************************************************************/

	public void loadLexicon(File lexiconFile) {
		
		for(List<String> cols : IDCUtils.parseCSVFile(lexiconFile)) {
			
			if(cols.size() > 1) {
				
				String lineType = cols.get(0);
				
				int type = -1;
				for(int nType = 0; nType < TYPES.length; nType++) {
					if(lineType.equals(TYPES[nType])) {
						type = nType;
						break;
					}
				}
				
				if(type != -1) {
					
					IDCWord word = null;
					List<IDCEntityRef> refs = new ArrayList<IDCEntityRef>();

					for(String term : cols.get(1).split("~")) {
						
						IDCUtils.debug("IDCOntology.loadLexicon(): term = " + term);
						
						if(word == null) {
							
							if(cols.size() > 2) {
								
								String concepts = cols.get(2);
								
								for(String conceptName : concepts.split("~")) {
									IDCConcept concept = getConcept(conceptName);
									if(concept != null) {
										refs.add(new IDCEntityRef(concept));
									}
								}
							}
							
							word = new IDCWord(term, type, refs);
						}

						wordMap.put(term,  word);
						
					}
					
				} else {
					IDCUtils.error("Can't process line: " + cols);
				}
					
			} else {
				IDCUtils.error("Can't process line: " + cols);
			}
			
		}

	}

	/*****************************************************************************/

	public String toString() {
		
		String ret = "Onthology {\n";
		
		ret += " Concepts {\n";
		for(IDCConcept concept : conceptMap.values()) {
			ret += "  " + concept + "\n";
		}
		ret += " }\n";

		ret += " Terms {\n";
		for(String term : wordMap.keySet()) {
			ret += "  " + term + "\n";
		}
		ret += " }\n";

		ret += "}";
		
		return ret;
	}
	
}
