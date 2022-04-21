package com.indirectionsoftware.utils;

/*
 * PDFTextParser.java
 *
 * Created on January 24, 2009, 11:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author prasanna
 */

import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

import com.indirectionsoftware.runtime.nlu.IDCWord;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IDCPDF {
    
    PDFParser parser;
    String parsedText;
    PDFTextStripper pdfStripper;
    PDDocument pdDoc;
    COSDocument cosDoc;
    PDDocumentInformation pdDocInfo;
    
	public static void main(String[] args) {
		
		IDCPDF pdf = new IDCPDF();
		pdf.pdftoText("/Users/etiennesillon/Downloads/Statement_Jun 2021 (1).pdf");
		
	}
	

    
    // PDFTextParser Constructor 
    public IDCPDF() {
    }
    
    // Extract text from PDF Document
    public String pdftoText(String fileName) {
        
        //IDCUtils.debug("Parsing text from PDF file " + fileName + "....");
        File file = new File(fileName);
        
        if (!file.isFile()) {
            IDCUtils.debug("File " + fileName + " does not exist.");
            return null;
        }

        try {
            parser = new PDFParser(new FileInputStream(file));
        } catch (Exception e) {
            IDCUtils.debug("Unable to open PDF Parser.");
            return null;
        } finally {
        }
        
        try {
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setShouldSeparateByBeads(false);
            pdfStripper.setSortByPosition(true);
            parsedText = pdfStripper.getText(pdDoc); 
            System.out.println(parsedText);
        } catch (Exception e) {
            IDCUtils.debug("An exception occured in parsing the PDF Document.");
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (cosDoc != null) cosDoc.close();
                if (pdDoc != null) pdDoc.close();
            } catch (Exception e1) {
	            e1.printStackTrace();
            }
        }
        //IDCUtils.debug("Done.");
        return parsedText;
    }
    
    // Write the parsed text from PDF to a file
    void writeTexttoFile(String pdfText, String fileName) {
    	
    	IDCUtils.debug("\nWriting PDF text to output text file " + fileName + "....");
    	try {
    		PrintWriter pw = new PrintWriter(fileName);
    		pw.print(pdfText);
    		pw.close();    	
    	} catch (Exception e) {
    		IDCUtils.debug("An exception occured in writing the pdf text to file.");
    		e.printStackTrace();
    	}
    	IDCUtils.debug("Done.");
    }
    
    public static void printfPDF(String fileName, boolean isLandscape) {
    	
        try {

        	File f = new File(fileName); 
            FileInputStream fis = new FileInputStream(f); 
            FileChannel fc = fis.getChannel(); 
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()); 
            PDFFile pdfFile = new PDFFile(bb);  
            PDFPrintPage pages = new PDFPrintPage(pdfFile); 

            PrinterJob pjob = PrinterJob.getPrinterJob(); 
            PageFormat pf = PrinterJob.getPrinterJob().defaultPage(); 
            if(isLandscape) {
            	pf.setOrientation(PageFormat.LANDSCAPE);
            }
            pjob.setJobName(f.getName()); 
            Book book = new Book(); 
            book.append(pages, pf, pdfFile.getNumPages()); 
            pjob.setPageable(book); 

            pjob.print();
        
        } catch(Exception ex) {
        	
        }
        
    }
    

}