package com.indirectionsoftware.utils;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

class IDCUtilsPDF {
	
	String fileName;
	
	int currentPage = -1;
	int maxPages = -1;
	
	PDDocument document = null;
    PDFTextStripper stripper = null;
	
	/************************************************************************************************/

    public  IDCUtilsPDF(String fileName) {
    	this.fileName = fileName;
    	this.currentPage = 1;
    }
    
	/************************************************************************************************/

    public String readNextPage() {
    	return readPage(currentPage++);
    }
    
	/************************************************************************************************/

    public String readPage(int nPage) {
    	
    	String ret = null;
    	
    	try {
    		
    		if(stripper == null) {
    			
                document = PDDocument.load(new File(fileName));
            	maxPages = document.getNumberOfPages();
                AccessPermission ap = document.getCurrentAccessPermission();
                if (!ap.canExtractContent()) {
                    System.out.println("You do not have permission to extract text");
                } else {
                    stripper = new PDFTextStripper();
                    stripper.setSortByPosition(true);
                }

    		}

    		if(stripper != null) {
    			
                if(nPage > 0 && nPage < maxPages) {
                	
                    stripper.setStartPage(nPage);
                    stripper.setEndPage(nPage);
                    String text = stripper.getText(document);
                    ret = text.trim();
                    
                }                 
                
    		}
    		
    		if(nPage == maxPages) {
                document.close();
                stripper = null;
    		}

    		
    	} catch(Exception ex) {
    		System.out.println("Exception: " + ex.getMessage());
    	}
    	
    	return ret;
    	
    
    }

	
}

