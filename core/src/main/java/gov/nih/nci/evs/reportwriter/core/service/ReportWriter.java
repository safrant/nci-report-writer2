package gov.nih.nci.evs.reportwriter.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gov.nih.nci.evs.reportwriter.core.model.evs.EvsConcept;
import gov.nih.nci.evs.reportwriter.core.model.report.Report;
import gov.nih.nci.evs.reportwriter.core.model.report.ReportColumn;
import gov.nih.nci.evs.reportwriter.core.model.report.ReportRow;
import gov.nih.nci.evs.reportwriter.core.model.template.Template;
import gov.nih.nci.evs.reportwriter.core.model.template.TemplateColumn;
import gov.nih.nci.evs.reportwriter.core.util.RWUtils;

@Service
/**
 * Generates a report based on a report template file.
 * 
 */
public class ReportWriter {
	
	private static final Logger log = LoggerFactory.getLogger(ReportWriter.class);
	
    @Autowired
    SparqlQueryManagerService sparqlQueryManagerService;

    @Autowired
    RWUtils rwUtils;

    public ReportWriter() {}
	
    /**
     * Run a report using template file.
     * 
     * @param templateFile Template file name.
     * @param outputFile Output file name.
     * @param conceptFile Concept file name.
     * @return Returns a string of either "success" or "failure".
     * 
     * <p>
     * This methods supports 2 basic types of template definitions.
     * <dl>
     * <dt>Association</dt>
     * <dd>The types of associations supported are either "Concept_In_Subset" or "Subclass".</dd>
     * <dt>ConceptList</dt>
     * <dd>For this type the conceptFile must be specified and should
     *                  contain one concept code per line.</dd>
     * </dl>
     * 
     * <p>
     * The templateFile format is YAML, which allows for the simple marshalling of the content
     * into the ReportTemplate class.
     * 
     */
	public String runReport(String templateFile, String outputFile, String conceptFile) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Template reportTemplate = null;
        PrintWriter logFile = null;
        try {
            reportTemplate = mapper.readValue(new File(templateFile), Template.class);
            String logOutputFile = outputFile + ".log";
            logFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(logOutputFile)),StandardCharsets.UTF_8),true);
        } catch (Exception ex) {
        	System.err.println(ex);
        	return "failure";
        }
        
        log.info("Template Information");
        log.info("********************");
        log.info(reportTemplate.toString());
        System.out.println("Template Information");
        System.out.println("********************");
        System.out.println(reportTemplate.toString());
        logFile.println("Started: " + LocalDateTime.now());
        logFile.println("********************************");
        logFile.println("");
        logFile.println("Template Information");
        logFile.println("********************************");
        logFile.println(reportTemplate.toString());
        
        String rootConceptCode = reportTemplate.getRootConceptCode();
        EvsConcept rootConcept = sparqlQueryManagerService.getEvsConceptDetailShort(rootConceptCode);

        // The conceptHash is used to improve performance, especially in the cases for reports that
        // are looking for parents concepts.  By first looking in the hash for the concept, time
        // can be saved by not repeating the SPARQL queries.
        HashMap <String,EvsConcept> conceptHash = new <String,EvsConcept> HashMap();
        
        int currentLevel = 0;
        int maxLevel = reportTemplate.getLevel();
        String templateType = reportTemplate.getType();
        Report reportOutput = new Report();
        if (templateType.equals("Association")) {
        	if (reportTemplate.getAssociation().equals("Concept_In_Subset")) {
                rwUtils.processConceptInSubset(reportOutput, rootConcept, conceptHash, reportTemplate.getColumns(),logFile);
                rwUtils.processConceptSubclasses(reportOutput, rootConcept, conceptHash, reportTemplate.getColumns(),logFile);
        	} else if (reportTemplate.getAssociation().equals("Subclass")) {
                rwUtils.processConceptSubclassesOnly(reportOutput, rootConcept, conceptHash, reportTemplate.getColumns(), currentLevel, maxLevel,logFile);
        	} else {
        		System.err.println("Invalid Association Type: " + reportTemplate.getAssociation());
        		return "failure";
        	}
        } else if (templateType.equals("ConceptList")) {
                rwUtils.processConceptList(reportOutput, conceptHash, reportTemplate.getColumns(), conceptFile,logFile);
        } else {
        	System.err.println("Invalid Template Type: " + templateType);
        	return "failure";
        }

        
        /*
         * Print out tab separated and Excel output files
         */
        String outputFileText = outputFile + ".txt";
        String outputFileExcel = outputFile + ".xls";
        PrintWriter pw = null;
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("report");
        sheet.createFreezePane(0, 1);
        int rowIndex = 0;
        
        Font headerFont = wb.createFont();
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle headerStyle = createBorderedStyle(wb);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
		try {
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFileText)),StandardCharsets.UTF_8),true);
			ArrayList <String> columnHeadings = new ArrayList <String>();
	       	Row excelRow = sheet.createRow(rowIndex++);
	       	int cellIndex = 0;
        	for (TemplateColumn templateColumn: reportTemplate.getColumns()) {
		       	columnHeadings.add(templateColumn.getLabel());
		       	Cell cell = excelRow.createCell(cellIndex++);
		        cell.setCellValue(templateColumn.getLabel());
		        cell.setCellStyle(headerStyle);
        	}
            pw.write(String.join("\t",columnHeadings) + "\n");
            
            /*
             * Sort the rows based on the template sortColumn specification
             */
            Integer sortColumnTemp = 0;
            if (reportTemplate.getSortColumn() != null) {
            	sortColumnTemp = reportTemplate.getSortColumn() - 1;
            }
            final Integer sortColumn = sortColumnTemp;
            ArrayList <ReportRow> reportRows = reportOutput.getRows();
            Collections.sort(reportRows,(row1, row2) -> row1.getColumns().get(sortColumn).getValue().compareTo(row2.getColumns().get(sortColumn).getValue()));

            for (ReportRow row: reportRows) {
            	excelRow = sheet.createRow(rowIndex++);
        	    ArrayList <String> values = new ArrayList <String>();
        	    cellIndex = 0;
        	    for (ReportColumn column: row.getColumns()) {
                    values.add(column.getValue());
                    Cell cell =  excelRow.createCell(cellIndex++);
		            cell.setCellValue(column.getValue());
        	    }
                pw.write(String.join("\t",values) + "\n");
            }
            
            for (int i = 0; i < reportTemplate.getColumns().size(); i++) {
            	sheet.autoSizeColumn(i);
            }

            OutputStream fos = new FileOutputStream(new File(outputFileExcel));
            wb.write(fos);
    	    fos.close();
        } catch (FileNotFoundException e) {
        	System.err.println("File Not Found Exception");
        	return "failure";
        } catch (IOException e) {
        	System.err.println("IOException");
        	return "failure";
        } finally {
    	    if (pw != null) {
    		    pw.close();
    	    }
        } 
		
        logFile.println("");
        logFile.println("********************************");
        logFile.println("Completed: " + LocalDateTime.now());
		logFile.close();
        
		return "success";
		
	}
	
	private static CellStyle createBorderedStyle(Workbook wb){
	        BorderStyle thin = BorderStyle.THIN;
	        short black = IndexedColors.BLACK.getIndex();
	        
	        CellStyle style = wb.createCellStyle();
	        style.setBorderRight(thin);
	        style.setRightBorderColor(black);
	        style.setBorderBottom(thin);
	        style.setBottomBorderColor(black);
	        style.setBorderLeft(thin);
	        style.setLeftBorderColor(black);
	        style.setBorderTop(thin);
	        style.setTopBorderColor(black);
	        return style;
	    }
	
}
