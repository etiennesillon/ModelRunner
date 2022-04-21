package com.indirectionsoftware.backend.database;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCFormula;
import com.indirectionsoftware.runtime.webapp.IDCWebAppSettings;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCDatabaseTableBrowser {
	
	/**************************************************************************************************/
	// Fields ...
	/**************************************************************************************************/
	
	public IDCType type;
	public List<IDCDataRef> refList;
	
	public String filter;
	
	private int nPage=0, maxPage=0, sortAttrId;
	
	private String htmlFooter;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDatabaseTableBrowser(IDCType type) {
		this(type, type.loadAllDataReferences());
	}

	public IDCDatabaseTableBrowser(IDCType type, List<IDCDataRef> refList) {
		this.type = type;
		setList(refList);
	}

	/**************************************************************************************************/

	public void setList(List<IDCDataRef> refList) {
		
		this.nPage=0;
		this.sortAttrId = -1;

		if(type.dateAttributes == null || (IDCWebAppSettings.startDate == -1 && IDCWebAppSettings.endDate == -1)) {
			this.refList = refList;
		} else {
			this.refList = new ArrayList<IDCDataRef>();
			for(IDCDataRef ref : refList) {
				IDCData data = type.loadDataObject(ref.getItemId());
				if(data != null && data.isInSettingsDateRange()) {
					this.refList.add(ref);
				}
			}
		}
		
		this.maxPage = this.refList.size() / IDCWebAppSettings.pageSize;
		if(this.maxPage * IDCWebAppSettings.pageSize >= this.refList.size()) {
			this.maxPage--; 
		}
		
		initFooterHTML();
		
	}

	/**************************************************************************************************/

	private void initFooterHTML() {
		
		htmlFooter = "";
		
		List<IDCAttribute> attrList = new ArrayList<IDCAttribute>();
		List<Long> priceList = new ArrayList<Long>();
		
		for(IDCAttribute attr : type.getAttributes()) {
			if(attr.getAttributeType() == IDCAttribute.PRICE) {
				attrList.add(attr);
				priceList.add(0L);
			}
		}
		
		if(attrList.size() > 0) {
			for(IDCDataRef ref : refList) {
				IDCData data = type.loadDataObject(ref.getItemId());
				if(data != null) {
					int nAttr = 0;
					for(IDCAttribute attr : attrList) {
						long price = data.getLong(attr.getAttributeId());
						long total = priceList.get(nAttr) + price;
						priceList.set(nAttr, total);
						nAttr++;
					}
				}
			}
			
			htmlFooter += "<tr><td></td>";

			int nAttr = 0;
			for(IDCAttribute attr : type.getAttributes()) {
				if(attr.getAttributeType() == IDCAttribute.PRICE) {
					htmlFooter += "<td style='text-align:right'>----------------</td>";
				} else {
					htmlFooter += "<td>&nbsp;</td>";
				}
			}
			htmlFooter += "</tr><tr><td></td>";
			
			nAttr = 0;
			for(IDCAttribute attr : type.getAttributes()) {
				if(attr.getAttributeType() == IDCAttribute.PRICE) {
					htmlFooter += "<td style='text-align:right'>" + IDCUtils.getAmountString(priceList.get(nAttr++), false) + "</td>";
				} else {
					htmlFooter += "<td>&nbsp;</td>";
				}
			}
			htmlFooter += "</tr>";
			
		}
		

	}

	/**************************************************************************************************/
	// Paging ...
	/**************************************************************************************************/
	
	public List<IDCData> getPage() {
		
		List<IDCData> ret = new ArrayList<IDCData>(); 

		for(int nRow = nPage * IDCWebAppSettings.pageSize, selectedRows=0; nRow >=0 && nRow < refList.size() && selectedRows < IDCWebAppSettings.pageSize; nRow++) {
			IDCDataRef ref = refList.get(nRow);
			IDCData data = getData(ref);
			if(data != null) {
				selectedRows++;
				ret.add(data);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public List<IDCData> getList() {
		
		List<IDCData> ret = new ArrayList<IDCData>(); 

		for(IDCDataRef ref : refList) {
			long id = ref.getItemId();
			IDCData data = getData(ref);
			if(data != null) {
				ret.add(data);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public IDCData getData(IDCDataRef ref) {
		
		IDCData ret = null; 

		long id = ref.getItemId();
		IDCData data = type.loadDataObject(id);
		boolean isOk = data.isEnabled();
		if(isOk) {
			if(filter != null && filter.length() > 0) {
				isOk = ((Boolean) data.evaluate(filter)).booleanValue();
			}
			if(isOk) {
				ret = data;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public void setPageNumber(int nPage) {
		
		if(nPage < 0) {
			nPage = 0;
		} else if(nPage > maxPage) {
			nPage = maxPage;
		}
		this.nPage = nPage;
	}

	/**************************************************************************************************/
	
	public void setNextPage() {
		setPageNumber(nPage+1);
	}

	/**************************************************************************************************/
	
	public void setPrevPage() {
		setPageNumber(nPage-1);
	}

	/**************************************************************************************************/
	// Sorting ...
	/**************************************************************************************************/
	
	public void sort(int attrId) {
		this.sortAttrId = attrId;
	}

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/
	
	public List<IDCDataRef> getAllRefs() {
		return refList;
	}

	public int getPageNumber() {
		return nPage;
	}

	public int getMaxPageNumber() {
		return maxPage;
	}

	/**************************************************************************************************/
	
	public String getFooterHTML() {
		return htmlFooter;
	}

}