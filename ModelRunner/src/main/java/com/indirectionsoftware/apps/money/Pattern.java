package com.indirectionsoftware.apps.money;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class Pattern {

	IDCData cat;
	
	String keywords[] = null;
	int keywordsLength;
	
	public static final String INCLUDES = "^", EOP = "~~~", WILDCARD = "(#)";
	
	String includes = null;
	String filter = null;
	
	/*******************************************************************************************************/
	
	public Pattern(IDCData patternData) {
		
		String pattern = patternData.getString("Pattern");
		filter = patternData.getString("Filter");
		if(filter != null && filter.length() == 0) {
			filter = null;
		}
		
		
		if(pattern.startsWith(INCLUDES)) {
			includes = pattern.substring(1, pattern.length());
		} else {
			keywords = pattern.split(" ");
			keywordsLength = keywords.length;
		}
		
		cat = patternData.getData("Category");
		
	}

	/*******************************************************************************************************/
	
	public IDCData getCategory(IDCData entry) {
		
		IDCData ret = null;
		
		String desc = entry.getString("Description");
		
		if(includes == null) {
			
			String[] temp = desc.split(" ");
			
			List<String> words = new ArrayList<String>();
			for(String word : temp) {
				if(word.length() > 0) {
					words.add(word);
				}
			}
			
			if(words.size() >= keywordsLength) {
				
				boolean matching = true;
				
				int nWord=0;
				
				for(nWord = 0; nWord < keywordsLength && matching == true; nWord++) {
					
					String keyword = keywords[nWord]; 

					if(keyword.equals(EOP)) {
						break;
						
					} else if(!keyword.equals(WILDCARD)) {
						
						String word = words.get(nWord); 
						
						int ind = keyword.indexOf(WILDCARD);
						if(ind != -1) {
							String key = keyword.substring(0, ind);
							if(!word.startsWith(key)) {
								matching = false;
							}
						} else if(!word.equals(keyword)) {
							matching = false;
						}
					}

				}
				
				if(matching) {
					ret = cat;
				}
				
			}

		} else {
			if(desc.contains(includes)) {
				ret = cat;
			}
		}
		
		if(ret != null && filter != null) {
			if(!entry.isTrue(filter)) {
				ret = null;
			}
		}
		
		return ret;
		
	}

}
