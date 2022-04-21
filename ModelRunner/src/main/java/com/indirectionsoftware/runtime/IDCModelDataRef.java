package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCModelData;

public class IDCModelDataRef {
	
	private int anchorEntityType;
	private int anchorEntityId;
	private List<IDCModelDataPath> paths;
	
    /************************************************************************************************/

	public IDCModelDataRef(int entityType, int entityId, List<IDCModelDataPath> paths) {
		this.anchorEntityType = entityType;
		this.anchorEntityId = entityId;
		this.paths = paths;
	}
	
    /************************************************************************************************/

	public IDCModelDataRef(IDCModelData anchor, List<IDCModelDataPath> paths) {
		this(anchor.getEntityType(), anchor.getEntityId(), paths);
	}
	
    /************************************************************************************************/

	public IDCModelDataRef(IDCModelData data) {
		
		this(data.getEntityType(), data.getEntityId(), null);

		IDCModelData parent = data.getParent();
		if(parent != null && !parent.isModelAnchor()) {

			List<IDCModelDataPath> paths = new ArrayList<IDCModelDataPath>();
			
			while(!parent.isApplication()) {

				IDCModelDataPath path = new IDCModelDataPath(data.getEntityType(), data.getEntityId());
				paths.add(path);

				data = parent;
				parent = parent.getParent();
				
			}

			anchorEntityType = data.getEntityType();
			anchorEntityId = data.getEntityId();

			this.paths = new ArrayList<IDCModelDataPath>();
			for(int nPath=paths.size()-1; nPath >= 0; nPath--) {
				this.paths.add(paths.get(nPath));
			}
			
		}
		
	}
	
    /************************************************************************************************/

	public static IDCModelDataRef getRef(String path) {
		
		IDCModelDataRef ret = null;

		int startAnchorInd = path.indexOf('(');
		int endAnchorInd = path.indexOf(')');
		int midAnchorInd = path.indexOf('/');
		
		if(startAnchorInd != -1 && endAnchorInd != -1 && midAnchorInd != -1) {

			int anchorEntityType = Integer.parseInt(path.substring(startAnchorInd+1, midAnchorInd));
			int anchorEntityId = Integer.parseInt(path.substring(midAnchorInd+1, endAnchorInd));
			
			List<IDCModelDataPath> paths = new ArrayList<IDCModelDataPath>();
			
			int endInd =0;
			boolean loop = true;
			while(loop) {

				int startInd = path.indexOf('<', endInd);
				if(startInd > -1) {
					endInd = path.indexOf('>', startInd);
					String refStr = path.substring(startInd, endInd+1);
					IDCModelDataPath ref = IDCModelDataPath.getPath(refStr);
					if(ref != null) {
						paths.add(ref);
					} else {
						loop = false;
					}
				} else {
					loop = false;
				}
			}

			ret = new IDCModelDataRef(anchorEntityType, anchorEntityId, paths);
			
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public String toString() {
		
		String ret = "(" + anchorEntityType + "/" + anchorEntityId + ")";
		
		if(paths != null) {
			for(IDCModelDataPath path : paths) {
				ret += path;
			}
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public int getAnchorEntityType() {
		return anchorEntityType;
	}

    /************************************************************************************************/

	public int getAnchorEntityId() {
		return anchorEntityId;
	}

	/************************************************************************************************/

	public List<IDCModelDataPath> getPaths() {
		return paths;
	}


}