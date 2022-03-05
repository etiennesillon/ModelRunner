package com.indirectionsoftware.runtime.nlu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCMetaModelEntity;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCNotificationData;
import com.indirectionsoftware.runtime.nlu.nodes.IDCNode;
import com.indirectionsoftware.runtime.nlu.nodes.IDCSentenceNode;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.runtime.webapp.IDCWebAppController;
import com.indirectionsoftware.runtime.webapp.IDCWebApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCLanguageEngineOLD {
	
	static IDCApplication app;
	
	static final String[][] SCHEMA = {
			{"sentence", "([I ([would|'d]) (just) [like|love|need|want] (to [see|get|know])|[can|may|could] I (please) [have|get]|(([could|can] you) (please)) [tell|give|get|send|show] me]) {question} ([thanks|thank you])"},
			{"question", "[who|what|where|when|why|how many|how much] {wh_query}"},
			{"question", "[does|do|did|will|have|has|can|could|will|would] {yn_query"},
			{"question", "([a|the]) (list of) (all) ([our|the]) {list_query}"},
			{"list_queryXXX", "{qualifier1} {#object} {qualifier2}"},
	};
	
	/***************************************************************************************/

	public IDCLanguageEngineOLD(IDCApplication app) {
		this.app = app;
	}
	
	/*****************************************************************************/

	public IDCNluResults processSentence(String sentence, IDCWebAppContext context) {
		
		IDCNluResults ret = new IDCNluResults(context, sentence);
		
		ret.debugNLU("IDCLanguageEngine.processSentence(): processing sentence = " + sentence);

		Map<String,String> inputMap = new HashMap<String, String>();
		inputMap.put("sentence",  IDCSentenceNode.normaliseString(sentence));
		
		Map<String, String> map = IDCSentenceNode.processSentence(SCHEMA, inputMap);
		
		String command = map.get("question");
		if(command != null) {
			
			ret.debugNLU("IDCLanguageEngine.processSentence(): trying command = " + command);
			ret.setQuery(command);
			boolean isProcessed = processDataCommand(ret);
			
			if(!isProcessed) {
				ret.debugNLU("______________________________________________________________________________________________________");
				for(String key : map.keySet()) {
					String phrase = map.get(key);
					ret.setQuery(phrase);
					ret.debugNLU("IDCLanguageEngine.processSentence(): key = " + key + " / phrase = " + phrase);
					if(key.equals("list_query")) {
						processListQuery(ret);
					} else if(key.equals("wh_query")) {
						processListQuery(ret);
//						processWHQuery(ret);
					} else if(key.equals("yn_query")) {
						processYesNoQuery(ret);
					}
				}
			}

		}
		
		return ret;

	}
	
	/*****************************************************************************/

	public void processListQuery(IDCNluResults results) {
		
		results.debugNLU("IDCLanguageEngine.processListQuery() - Start: query = " + results.query);

		List<List<IDCWord>> qualifierList = new ArrayList<List<IDCWord>>(); 
		qualifierList.add(new ArrayList<IDCWord>());
		qualifierList.add(new ArrayList<IDCWord>());

		List<IDCWord> quals = qualifierList.get(0);
		IDCConcept concept = null;		
		
		List<IDCWord> words = getWords(results.query);
		
		IDCModelData mostLikelyType = getMostLikelyQueryType(results, words);
		
//		List<IDCType> types = new ArrayList<IDCType>();
		
		for(IDCWord word : words) {

			results.debugNLU("IDCLanguageEngine.processListQuery(): word = " + word);
			
			if(word.refs.size() > 0) {
				
				boolean wordAdded = false;
				
				for(IDCEntityRef ref: word.refs) {
					
					results.debugNLU("IDCLanguageEngine.processListQuery(): ref = " + ref);

					if(ref.isConcept() && concept == null) {

						for(IDCModelData entity : ref.getConcept().modelEntities) {
							if(entity.isType() && (mostLikelyType == null || mostLikelyType.equals(entity))) {
								concept = ref.getConcept();
								quals = qualifierList.get(1);
							}
						}
						
						if(concept == null) {
							quals.add(word);
						}
						
						wordAdded = true;
					
					} else {
						
						if(concept == null && ref.getValue().attrs.size() == 2) {
							IDCAttribute refAttr = null;
							IDCAttribute nameAttr = null;
							for(IDCAttribute attr : ref.getValue().attrs) {
								if(attr.getName().equals("Name")) {
									nameAttr = attr;
								} else if(attr.getAttributeType() == IDCAttribute.REF) {
									refAttr = attr;
								} 
							}
							
							if(refAttr != null && nameAttr != null) {
								concept = new IDCConcept(refAttr.getDataType().getName());
								concept.addModelEntity(refAttr.getDataType());
								quals.add(word);
								quals = qualifierList.get(1);
							}
							
							wordAdded = true;
							
						}
					}

				}
				
				if(!wordAdded) {
					quals.add(word);
				}

			} else {
				if(!word.isCanIgnore()) {
					quals.add(word);
				} else {
					results.debugNLU("IDCLanguageEngine.processListQuery(): ignoring word = " + word);
				}
			}

		}
		
		results.debugNLU("IDCLanguageEngine.processListQuery(): concept = " + concept);
		results.debugNLU("IDCLanguageEngine.processListQuery(): pre qualifiers:");
		for(IDCWord word : qualifierList.get(0)) {
			results.debugNLU("IDCLanguageEngine.processListQuery(): word = " + word);
		}

		results.debugNLU("IDCLanguageEngine.processListQuery(): post qualifiers:");
		for(IDCWord word : qualifierList.get(1)) {
			results.debugNLU("IDCLanguageEngine.processListQuery(): word = " + word);
		}
		
		if(concept != null) {
			processListQueryConcept(results, concept, qualifierList.get(0), qualifierList.get(1)); 
		} else {
			results.setResultsHTML("<p>Sorry, I don't understand :(</p>");
		}

		results.debugNLU("IDCLanguageEngine.processListQuery() - End: query = " + results.query);

	}

	/*****************************************************************************/

	private void processYesNoQuery(IDCNluResults results) {

	}

	/*****************************************************************************/

	private void processWHQuery(IDCNluResults results) {

	}

	/*****************************************************************************/

	private IDCModelData getMostLikelyQueryType(IDCNluResults results, List<IDCWord> words) {
		
		IDCModelData ret = null;
		
		List<IDCModelData> referedTypes = getTypes(results, words);
		List<IDCModelData> referedAttrs = getAttributes(results, words);
		
		if(referedTypes.size() > 1) {
			
			int[] counts = new int[referedTypes.size()]; 
					
			int nType = 0;
			for(IDCModelData type : referedTypes) {

				for(IDCModelData attr : referedAttrs) {
					
					if(((IDCAttribute) attr).getDataType().equals(type)) {
						counts[nType]++;
					}
					
				}
				
				IDCUtils.info("type = " + type + " / count = " + counts[nType]);
				results.debugNLU("IDCLanguageEngine.getMostLikelyQueryType() - type = " + type + " / count = " + counts[nType]);
				nType++;
						
			}

			nType = 0;
			int bestCount = 0;
			for(IDCModelData type : referedTypes) {
				if(counts[nType] > bestCount) {
					ret =  type;
				}
				nType++;
			}
		} else if(referedTypes.size() == 1) {
			ret = referedTypes.get(0);
		}
			
		results.debugNLU("IDCLanguageEngine.getMostLikelyQueryType() - End: ret = " + ret);

		return ret;
		
	}

	/*****************************************************************************/

	private void processListQueryConcept(IDCNluResults results, IDCConcept concept, List<IDCWord> preList, List<IDCWord> postList) {
		
		results.debugNLU("IDCLanguageEngine.processListQueryConcept() - Start: concept = " + concept);

		IDCType type = null;

		for(IDCModelData entity : concept.modelEntities) {
			if(entity.isType()) {
				type = (IDCType) entity;
				break;
			}
		}
		
		if(type == null) {
			Map<Long, IDCType> typesMap = new HashMap<Long, IDCType>();
			for(IDCModelData entity : concept.modelEntities) {
				if(entity.isType()) {
					type = (IDCType) entity;
				} else if(entity.isAttribute()) {
					type = ((IDCAttribute) entity).getDataType();
				} 
				typesMap.put(type.getId(), type);
			}
			if(typesMap.size() == 1) {
				type = typesMap.get(typesMap.keySet().toArray()[0]);
				results.debugNLU("IDCLanguageEngine.processListQueryConcept(): type = " + type);
			} else {
				type = null;
			}
		}

		List<IDCAttributeValueDataList> attrVals = new ArrayList<IDCAttributeValueDataList>();
		List<IDCAttribute> attrs = new ArrayList<IDCAttribute>();

		if(type != null) {
			
			for(IDCWord word : preList) {

				results.debugNLU("IDCLanguageEngine.processListQueryConcept(): word = " + word);

				for(IDCEntityRef ref: word.refs) {
					
					results.debugNLU("IDCLanguageEngine.processListQueryConcept(): ref = " + ref);

					if(ref.isValue()) {
						IDCValue value = ref.getValue();
						for(IDCAttribute attr : value.attrs) {
							if(attr.getDataType().getId() == type.getId()) {
								attrVals.add(new IDCAttributeValueDataList(attr, value.value));
							}
						}
					} else {
						for(IDCModelData entity : ref.getConcept().modelEntities) {
							if(entity.isAttribute()) {
								IDCAttribute attr = (IDCAttribute) entity;
								IDCType attrParentType = attr.getDataType();
								if(attrParentType.equals(type)) {
									attrs.add((IDCAttribute) entity);
								}
							}
						}
					}

				}

			}
			
			IDCAttribute prevAttr = null;
			IDCAttributeValueDataList prevAttrVal = null;
			
			for(IDCWord word : postList) {

				results.debugNLU("IDCLanguageEngine.processListQueryConcept(): postList word = " + word + " / prevAttr = " + prevAttr + " / prevAttrVal = " + prevAttrVal);
				
				if(word.refs.size() > 0) {
					
					for(IDCEntityRef ref: word.refs) {
						
						results.debugNLU("IDCLanguageEngine.processListQueryConcept(): postList ref = " + ref);

						if(ref.isValue()) {
							IDCValue value = ref.getValue();
							for(IDCAttribute attr : value.attrs) {
								results.debugNLU("IDCLanguageEngine.processListQueryConcept(): postList value attr = " + attr);
								if(attr.getDataType().getId() == type.getId()) {
									IDCAttributeValueDataList val = new IDCAttributeValueDataList(attr, value.value);
									attrVals.add(val);
									prevAttrVal = val;
									if(prevAttr != null) {
										if(!prevAttr.equals(attr)) {
											attrs.add(prevAttr);
										}
									}
									prevAttr = null;
								}
							}
						} else {
							for(IDCModelData entity : ref.getConcept().modelEntities) {
								results.debugNLU("IDCLanguageEngine.processListQueryConcept(): postList entity = " + entity);
								if(entity.isAttribute()) {
									IDCAttribute attr = (IDCAttribute) entity;
									IDCType attrParentType = attr.getDataType();
									if(attrParentType.equals(type)) {
										IDCAttribute curAttr = (IDCAttribute) entity;
										if(prevAttrVal == null || !prevAttrVal.attr.equals(curAttr)) {
											if(prevAttr != null && prevAttr != curAttr) {
												attrs.add(prevAttr);
											}
											prevAttr = curAttr;
											prevAttrVal = null;
										}
									}
								}
							}
						}

					}
					
				} else {
					if(prevAttr != null) {
						attrVals.add(new IDCAttributeValueDataList(prevAttr, word.stem));
						prevAttr = null;
					}
				}

			}

			if(prevAttr != null) {
				attrs.add(prevAttr);
			}
			
			List<IDCData> dataList = type.search(attrVals);
			List<IDCDataRef> refList = new ArrayList<IDCDataRef>();

			if(dataList.size() > 0) {
				
				String resFull = "";

				for(IDCData data : dataList) {
					results.debugNLU("IDCLanguageEngine.processPreQualifiers(): data = " + data);
					String res = "";
					if(attrs.size() > 0) {
						for(IDCAttribute attr : attrs) {
							if(res.length() > 0) {
								res += " and ";
							}
							res += attr.getName() + " is " + data.getDisplayValue(attr); 
						}
						resFull += "<p>" + type.getName() + ": " + results.context.webApp.getURLButton(results.context, "linkbut", data.getName(), IDCWebAppContext.CONTEXTSELECTQUERY[results.context.type], data.getDataType().getId(), data.getId(), IDCWebAppController.NA, "", true, false) + " " + res + "</p>";
					} else {
						refList.add(data.getDataRef());
					}

				}
				
				if(attrs.size() == 0) {
					results.context.browser = new IDCDatabaseTableBrowser(type, refList);
					results.context.selectedType = type;
					results.context.action = IDCWebAppController.SPEAK;
					results.context.nluQuery = results.sentence;
					results.html = results.context.webApp.getTypeListHTML(results.context);
				} else {
					results.setResultsHTML(resFull);
				}

			} else {
				results.setResultsHTML("<p>Sorry, can't find any " + (attrVals.size() > 0 ? "matching " : "") + concept.name + "</p>");
			}
			
		}
		
	}
	
	/***************************************************************************************/

	public List<IDCWord> getWordsNEW(IDCNluResults results, String query) {
		
		List<IDCWord> ret = new ArrayList<IDCWord>();

		String[] tokens = query.split(" ");
		
		for(int nToken=0; nToken < tokens.length; nToken++) {
			
			String token = IDCWord.getStem(tokens[nToken]);
			
			results.debugNLU("getWords(): token = " + token);

			IDCWord word = new IDCWord(token);
			
			for(IDCDataValue value : app.searchDataValues(token, IDCData.SEARCH_STARTS_WITH)) {
				
				results.debugNLU("getWords(): found: value = " + value);

				boolean matches = true;
				int nTokenSkips = 0;
				
				if(!token.equals(value.value)) {
					String[] valueTokens = value.value.split(" ");
					for(int nValueToken=0; nValueToken < valueTokens.length && matches; nValueToken++) {
						results.debugNLU("getWords(): valueTokens = " + valueTokens[nValueToken] + " / tokens = " + tokens[nToken + nValueToken]);
						if(nToken + nValueToken >= tokens.length || !valueTokens[nValueToken].equals(tokens[nToken + nValueToken])) {
							matches = false;
						} else {
							nTokenSkips++;
						}
					}
					
				}
				
				if(matches) {
					results.debugNLU("getWords(): adding: value = " + value);
//					word.addValue(value);
					nToken += nTokenSkips;
				}

			}
			
			ret.add(word);
			
		}

		return ret;
		
	}
	
	/***************************************************************************************/

	public List<IDCWord> getWords(String query) {
		
		List<IDCWord> ret = new ArrayList<IDCWord>();

		for(String token : query.split(" ")) {
			
			IDCUtils.info("getWords(): token = " + token);
			IDCWord word = app.getOntology().getWord(IDCWord.getStem(token), IDCData.SEARCH_STARTS_WITH);
			ret.add(word);

		}

		return ret;
		
	}
	
	/***************************************************************************************/

	private List<IDCModelData> getEntities(IDCNluResults results, List<IDCWord> words, int entityType) {

		List<IDCModelData> ret = new ArrayList<IDCModelData>();
		
		Map<Long, IDCModelData> map = new HashMap<Long, IDCModelData>();
		
		for(IDCWord word : words) {

			results.debugNLU("IDCLanguageEngine.getTypes(): word = " + word);
			
			if(word.refs.size() > 0) {
				
				for(IDCEntityRef ref: word.refs) {
					
					results.debugNLU("IDCLanguageEngine.getTypes(): ref = " + ref);

					if(ref.isConcept()) {

						for(IDCModelData entity : ref.getConcept().modelEntities) {
							
							results.debugNLU("IDCLanguageEngine.getTypes(): entity = " + entity);

							if(entity.isType() && entityType == IDCModelData.TYPE || entity.isAttribute() && entityType == IDCModelData.ATTRIBUTE) {
								map.put(entity.getId(), entity);
							}
							
						}
						
					}

				}
				
			}

		}
		
		ret.addAll(map.values());
		
		return ret;
		
	}

	/***************************************************************************************/

	private List<IDCModelData> getAttributes(IDCNluResults results, List<IDCWord> words) {
		return getEntities(results, words, IDCModelData.ATTRIBUTE);
	}

	/***************************************************************************************/

	private List<IDCModelData> getTypes(IDCNluResults results, List<IDCWord> words) {
		return getEntities(results, words, IDCModelData.TYPE);
	}
	
	/*****************************************************************************/

	static final String[] ACTIONS_WORDS = {"create", "add", "insert", "select", "find", "get", "set", "update", "put", "change"};
	static final int[] ACTIONS_ACTIONS = {0, 0, 0, 1, 1, 1, 2, 2, 2, 2};

	/*****************************************************************************/

	private boolean processDataCommand(IDCNluResults results) {
		
		boolean ret = false;

		results.debugNLU("IDCLanguageEngine.processDataCommand() - Start: query = " + results.query);
		
		results.data = results.context.selectedData;
		
	      List<String> commands = new ArrayList<String>();
	      String command = "";
	   
	      String[] words = results.query.split(" ");
	   
	      for (int nWord = 0; nWord < words.length; nWord++) {
	   
	         String word = words[nWord];
	   
	         if(word.equals("and")) {
	            commands.add(command);
	            command = "";
	         } else {
	
	            if(word != "please") {
	               if(command.length() > 0) {
	                  command += " ";
	               }
	               command += word;
	            }
	   
	         }
	   
	      }
	
	      if(command.length() > 0) {
	         commands.add(command);
	      }
	   
	      for (String cmd: commands) {
	         if(processNLUCommand(results, cmd)) {
	        	 ret = true;
	         }
	      }

	      if(ret == true) {
			results.context.nluQuery = results.sentence;
			if(results.data != null) {
		      Map<String, IDCError> errors = new HashMap<String, IDCError>();
		      results.context.action = IDCWebAppController.SPEAK;
		      results.html = results.context.webApp.getItemDetailsHTML(results.context, errors);
			}
	      }


		return ret;
		
	}
	
	/**********************************/

	private boolean  processNLUCommand(IDCNluResults results, String command) {
		
		boolean ret = false;

		results.debugNLU("IDCLanguageEngine.processNLUCommand() - Start: processNLUCommand = " + command);
		
		IDCApplication app = results.context.webApp.getApplication();

	   String[] words = command.split(" ");

	   int queryAction = -1;
	   
	   IDCType queryType = null;
	   IDCAttribute queryAttribute = null;
	   
	   int nTo = -1;
	   String setToValue = "";

	   int nCalled = -1;
	   String name = "";

	   for (int nWord = 0; nWord < words.length; nWord++) {

	      String word = words[nWord];
	      
	      results.debugNLU("IDCLanguageEngine.processNLUCommand(): word = " + word);
			
	      if(word.equals("to") || word.equals("2") || word.equals("two") || word.equals("with") || word.equals("as")) {
	         nTo = nWord;
	         results.debugNLU("IDCLanguageEngine.processNLUCommand(): found 'set to' word = " + word);
	      } if(word.equals("called") || word.equals("call") || word.equals("cold") || word.equals("cold") || word.equals("gold")) {
	         nCalled = nWord;
	         results.debugNLU("IDCLanguageEngine.processNLUCommand(): found 'called' word = " + word);
	      } else if(nCalled == -1) {

	         for (int nAction = 0; nAction < ACTIONS_WORDS.length; nAction++) {
	            String action = ACTIONS_WORDS[nAction];
	            if(word.equalsIgnoreCase(action)) {
	               queryAction = ACTIONS_ACTIONS[nAction];
	            }
	         }
	   
	         for (IDCType type : app.getTypes()) {
	        	 if(word.equalsIgnoreCase(type.getDisplayName())) {
	            	queryType = type;
	            	results.debugNLU("IDCLanguageEngine.processNLUCommand(): found data type = " + queryType);
	            }
	         }
		   
	         if(results.data != null) {
	 	        for (IDCAttribute attr : results.data.getDataType().getAttributes()) {
		        	if(word.equalsIgnoreCase(attr.getDisplayName()) || attr.getDisplayName().toLowerCase().startsWith(word.toLowerCase())) {  // need better check than that !!!!!!!
		            	queryAttribute = attr;
		            	results.debugNLU("IDCLanguageEngine.processNLUCommand(): found attribute = " + queryAttribute);
		            }
		        }
	         }
	        
	      }

	    }

	    if(nTo != -1 && (nTo + 1) < words.length) {
	       for(int n = nTo +1; n < words.length; n++) {
	         if(setToValue.length() > 0) {
	            setToValue += " ";
	         }
	        setToValue += words[n];
	       }
	       setToValue = IDCUtils.capitalise(setToValue);
	       results.debugNLU("IDCLanguageEngine.processNLUCommand(): setToValue = " + setToValue);
	    }

	    if(nCalled != -1 && (nCalled + 1) < words.length) {
	      for(int n = nCalled +1; n < words.length; n++) {
	        if(name.length() > 0) {
	         name += " ";
	        }
	        name += words[n];
	      }
	      name = IDCUtils.capitalise(name);
	      results.debugNLU("IDCLanguageEngine.processNLUCommand(): name = " + name);
	   }

	    if(queryAction == 0) { // create, add, insert

	      if(queryType != null) {

	    	  results.debugNLU("IDCLanguageEngine.processNLUCommand(): create data - type = " + queryType);
	    	  results.data = queryType.getNewObject(); 
	    	  
		      if(nCalled != -1) {
		    	  results.data.set("Name", name);
		      }
		      
		      ret = true;
		      results.data.save();
		      results.context.selectedType = queryType;
		      results.context.selectedData = results.data;

	      }    

	    } if(queryAction == 1) { // select

		      if(queryType != null) {

		    	  results.debugNLU("IDCLanguageEngine.processNLUCommand(): select data - type = " + queryType + " name = " + name);
		    	  
		    	  results.data = queryType.requestDataByNameIgnoreCase(name);
		    	  if(results.data != null) {
				      ret = true;
				      results.context.selectedType = queryType;
				      results.context.selectedData = results.data;
		    	  }

		      }    

	    } else if(queryAction == 2) { // set

	      if(results.data != null && queryAttribute != null && nTo != -1) {

	    	  results.debugNLU("IDCLanguageEngine.processNLUCommand(): set attribute = " + queryAttribute + " to " + setToValue);
	    	  results.data.set(queryAttribute, setToValue);
		      ret = true;
		      results.data.save();

	      } 
	      
	    }
	    
	    return ret;

	}

}

	