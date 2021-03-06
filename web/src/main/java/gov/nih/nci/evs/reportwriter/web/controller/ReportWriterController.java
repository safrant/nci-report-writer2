package gov.nih.nci.evs.reportwriter.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import gov.nih.nci.evs.reportwriter.web.model.LkGeneric;
import gov.nih.nci.evs.reportwriter.web.model.LkProperty;
import gov.nih.nci.evs.reportwriter.web.model.LookUp;
import gov.nih.nci.evs.reportwriter.web.model.ReportTask;
import gov.nih.nci.evs.reportwriter.web.model.ReportTemplate;
import gov.nih.nci.evs.reportwriter.web.model.ReportTemplateColumn;
import gov.nih.nci.evs.reportwriter.web.service.LkAssociationService;
import gov.nih.nci.evs.reportwriter.web.service.LkDisplayService;
import gov.nih.nci.evs.reportwriter.web.service.LkGroupService;
import gov.nih.nci.evs.reportwriter.web.service.LkPropertyService;
import gov.nih.nci.evs.reportwriter.web.service.LkPropertyTypeService;
import gov.nih.nci.evs.reportwriter.web.service.LkReportStatusService;
import gov.nih.nci.evs.reportwriter.web.service.LkReportTemplateStatusService;
import gov.nih.nci.evs.reportwriter.web.service.LkReportTemplateTypeService;
import gov.nih.nci.evs.reportwriter.web.service.LkSourceService;
import gov.nih.nci.evs.reportwriter.web.service.LkSubsourceService;
import gov.nih.nci.evs.reportwriter.web.service.ReportTaskService;
import gov.nih.nci.evs.reportwriter.web.service.ReportTemplateColumnService;
import gov.nih.nci.evs.reportwriter.web.service.ReportTemplateService;
import gov.nih.nci.evs.reportwriter.web.support.FileUI;
import gov.nih.nci.evs.reportwriter.web.support.ReportTaskOutput;
import gov.nih.nci.evs.reportwriter.web.support.ReportTaskUI;
import gov.nih.nci.evs.reportwriter.web.support.ReportTemplateUI;
import gov.nih.nci.evs.reportwriter.web.util.MediaTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/reportwriter")
public class ReportWriterController {
	
	
	private static final Logger log = LoggerFactory.getLogger(ReportWriterController.class);

	@Autowired  LkAssociationService lkAssociationService;
	@Autowired  LkDisplayService lkDisplayService;
	@Autowired  LkGroupService lkGroupService;
	@Autowired  LkPropertyService lkPropertyService;
	@Autowired  LkPropertyTypeService lkPropertyTypeService;
	@Autowired  LkReportStatusService lkReportStatusService;
	@Autowired  LkReportTemplateStatusService lkReportTemplateStatusService;
	@Autowired  LkReportTemplateTypeService lkReportTemplateTypeService;
	@Autowired  LkSourceService lkSourceService;
	@Autowired  LkSubsourceService lkSubsourceService;
	
	@Autowired  ReportTemplateService reportTemplateService;
	@Autowired  ReportTemplateColumnService reportTemplateColumnService;
	
	@Autowired ReportTaskService reportTaskService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody String home() {
		return "Welcome to ReportWriter";
	}

	@RequestMapping(value="/lkassociation",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkAssociations() {
		List lks =  lkAssociationService.findAll();	
		return convertToLookUp(lks);
	}

	
	@RequestMapping(value="/lkdisplay",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkDisplays() {
		List lks =  lkDisplayService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkgroup",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkGroups() {
		List lks =  lkGroupService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkpropertytype",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkPropertyTypes() {
		List lks =  lkPropertyTypeService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkreportstatus",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkReportStatuses() {
		List lks =  lkReportStatusService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkreporttemplatestatus",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkReportTemplateStatuses() {
		List lks =  lkReportTemplateStatusService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkreporttemplatetype",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkReportTemplateTypes() {
		List lks =  lkReportTemplateTypeService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lksource",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkSources() {
		List lks =  lkSourceService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lksubsource",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkSubsources() {
		List lks =  lkSubsourceService.findAll();	
		return convertToLookUp(lks);
	}

	@RequestMapping(value="/lkproperty",method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List <LookUp> findAllLkProperties() {
		List <LkProperty> lks =  lkPropertyService.findAll();	
		List <LookUp> lookUps = new ArrayList <LookUp> ();
		for (LkProperty lookUp: lks) {
			String label = "(" + lookUp.getCode() + ") " + lookUp.getLabel();
			lookUps.add(new LookUp(label,lookUp.getCode()));
		}
		return lookUps;
	}


	private List <LookUp> convertToLookUp(List <LkGeneric> lks) {
		List <LookUp> lookUps = new ArrayList <LookUp> ();
		for (LkGeneric lookUp: lks) {
			lookUps.add(new LookUp(lookUp.getName(),lookUp.getName()));
		}
		return lookUps;
		
	}
	
	
	//Creating a new Template	
	@RequestMapping(method = RequestMethod.POST, value = "/createTemplate", consumes = "application/json", produces = "application/json")
	public @ResponseBody String createTemplate(@RequestBody final ReportTemplateUI reportTemplate) throws JsonProcessingException {

		log.info(reportTemplate.getName());
		log.info(reportTemplate.getStatus());
		
		
     		
	   
		ReportTemplateUI reportTemplateRet = reportTemplateService.create(reportTemplate);
		
		List<ReportTemplateColumn> reportTemplateColumns =  reportTemplateColumnService.getReportColumnsByReportTemplateID(reportTemplateRet.getId());
		
				
		reportTemplateRet.setColumns(reportTemplateColumns);
		
		SimpleFilterProvider filter = new SimpleFilterProvider();
		filter.setFailOnUnknownId(false);
        ObjectMapper mapper = new ObjectMapper();
        String reportTemplateRetStr = mapper.writer(filter).writeValueAsString(reportTemplateRet);
        
       // log.info("test - " + reportTemplateRetStr);
		
		return reportTemplateRetStr;

	}
	
	//Creating a new Template	
	@RequestMapping(method = RequestMethod.POST, value = "/saveTemplate", consumes = "application/json", produces = "application/json")
	public @ResponseBody String saveTemplate(@RequestBody final ReportTemplateUI reportTemplate) throws JsonProcessingException {

			
			log.info("Id **- " + reportTemplate.getId());
			log.info(reportTemplate.getName());
			log.info(reportTemplate.getStatus());
	     		
			ReportTemplateUI reportTemplateRet = reportTemplateService.save(reportTemplate);
			
			List<ReportTemplateColumn> reportTemplateColumns =  reportTemplateColumnService.getReportColumnsByReportTemplateID(reportTemplateRet.getId());
			reportTemplateRet.setColumns(reportTemplateColumns);
			
			SimpleFilterProvider filter = new SimpleFilterProvider();
			filter.setFailOnUnknownId(false);
	        ObjectMapper mapper = new ObjectMapper();
	        String reportTemplateRetStr = mapper.writer(filter).writeValueAsString(reportTemplateRet);
	        
	        //log.info("test - " + reportTemplateRetStr);
			
			return reportTemplateRetStr;
			
			

	}
	
	// Get all Templates
	@RequestMapping(value = "/reporttemplates", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String reporttemplates() throws JsonProcessingException {
			List<ReportTemplateUI> reportTemplates = reportTemplateService.findAll();
			
			SimpleFilterProvider filter = new SimpleFilterProvider();
			filter.setFailOnUnknownId(false);
	        ObjectMapper mapper = new ObjectMapper();
	        String reportTemplatesStr = mapper.writer(filter).writeValueAsString(reportTemplates);
	        
	        //log.info("test - " + reportTemplatesStr);
			
			return reportTemplatesStr;

			
	}
	
	// Get all Templates
		@RequestMapping(value = "/reporttemplate/{reportTemplateId}", method = RequestMethod.GET, produces = "application/json")
		public @ResponseBody String reporttemplates(@PathVariable Integer reportTemplateId) throws JsonProcessingException {
				ReportTemplate reportTemplate= reportTemplateService.findOne(reportTemplateId);
				
				SimpleFilterProvider filter = new SimpleFilterProvider();
				filter.setFailOnUnknownId(false);
		        ObjectMapper mapper = new ObjectMapper();
		        String reportTemplateStr = mapper.writer(filter).writeValueAsString(reportTemplate);
		        
		        log.info("reportTemplateStr ---- " + reportTemplateStr);
				

				return reportTemplateStr;
		}
		
		@RequestMapping(value = "/reporttasks", method = RequestMethod.GET, produces = "application/json")
		public @ResponseBody List<ReportTaskUI> reporttasks() {
			List<ReportTaskUI> reportTaskUIs = reportTaskService.getAllTasksExceptDeleted();

				return reportTaskUIs;
		}
		
		@RequestMapping(value = "/reporttasksdeleted", method = RequestMethod.GET, produces = "application/json")
		public @ResponseBody List<ReportTaskUI> reporttasksdeleted() {
			List<ReportTaskUI> reportTaskUIs = reportTaskService.getAllDeletedTasks();

			return reportTaskUIs;
		}
		
		@RequestMapping(value="/reportNameByTaskId/{id}", method = RequestMethod.GET)
		  public @ResponseBody ReportTemplateUI reportNameByTaskId(@PathVariable Integer id) 
		    		throws IOException {
		    log.info("getReportTask" +  id);
		    ReportTemplateUI reportTemplateUI = reportTaskService.getReportNameByTaskId(id);		    
			return reportTemplateUI;
			
		}
		
		@RequestMapping(value="/deleteReportTask/{id}", method = RequestMethod.GET)
		  public @ResponseBody ReportTask deleteReportTask(@PathVariable Integer id) 
		    		throws IOException {
		    
			ReportTask reportTask = reportTaskService.deleteReportTask(id);
			return reportTask;
			
		}
		
		@RequestMapping(value = "/getXLSReport/{id}", method = RequestMethod.GET)
		public ModelAndView getXLSReport(@PathVariable("id") String id,
				HttpServletResponse response) throws Exception {
			log.debug("In here getDetailedReport");
			
			FileUI fileUI = reportTaskService.getDetailedReportTask(id,"xls");

			getFile(fileUI, response);
		
			return null;
		}
		
		@RequestMapping(value = "/getTxtReport/{id}", method = RequestMethod.GET)
		public ModelAndView getTxtReport(@PathVariable("id") String id,
				HttpServletResponse response) throws Exception {
			log.debug("In here getTxtReport");
			
			FileUI fileUI = reportTaskService.getDetailedReportTask(id,"txt");

			getFile(fileUI, response);
		
			return null;
		}
		

		@RequestMapping(value = "/getTemplateReport/{id}", method = RequestMethod.GET)
		public ModelAndView getTemplateReport(@PathVariable("id") String id,
				HttpServletResponse response) throws Exception {
			log.debug("In here getTxtReport");
			
			FileUI fileUI = reportTaskService.getDetailedReportTask(id,"template");

			getFile(fileUI, response);
		
			return null;
		}
		
		@RequestMapping(value = "/getLogReport/{id}", method = RequestMethod.GET)
		public ModelAndView getLogReport(@PathVariable("id") String id,
				HttpServletResponse response) throws Exception {
			log.debug("In here getLogReport");
			
			FileUI fileUI = reportTaskService.getDetailedReportTask(id,"log");

			getFile(fileUI, response);
		
			return null;
		}
		
		@RequestMapping(value="/getReportTaskData/{id}", method = RequestMethod.GET)
	    public @ResponseBody ReportTaskOutput getReportTaskData(@PathVariable String id) 
	    		throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
			
			ReportTaskOutput reportTaskOutput = reportTaskService.getReportTaskData(id);
			
			return reportTaskOutput;
			
		}
		
		@RequestMapping(value="/runReport/{id}", method = RequestMethod.GET)
	    public @ResponseBody ReportTask runReport(@PathVariable Integer id) 
	    		throws IOException { 
			
			ReportTemplate reportTemplate = reportTemplateService.findOne(id);
			List<ReportTemplateColumn> columns = reportTemplate.getColumns();
			ReportTask reportTask = new ReportTask();
			reportTask.setStatus("Pending");
			reportTask.setReportTemplate(reportTemplate);
			reportTask.setDateCreated(LocalDateTime.now());
			reportTask.setDateLastUpdated(LocalDateTime.now());
			reportTask.setCreatedBy("system");
			reportTask.setLastUpdatedBy("system");
			ReportTask reportTaskRet = reportTaskService.save(reportTask);
			log.info("TaskId" + reportTask.getId());
            reportTaskService.runReport(reportTaskRet);
            log.info("Run Report Submitted");

            return reportTaskRet;
	    }
		
		protected void getFile(FileUI file, HttpServletResponse response) throws Exception {

			OutputStream out = response.getOutputStream();
			String fileName = file.getFileName();
			//
			// set to correct media-type
			//
			response.setContentType(MediaTypes.getMediaType(fileName));

			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

			// Tell browser to validate cache
			response.addHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Cache-Control", "max-age=0");
			// Using public somehow cures IE's inability to save attachment
			// (and doesn't seem to hurt other browsers)
			response.setHeader("Pragma", "public");
			try {
				byte[] buf = new byte[16 * 1024]; // 16k buffer
				int nRead = 0;
				while ((nRead = file.getRawFileStream().read(buf)) != -1) {
					out.write(buf, 0, nRead);
				}
				out.flush();
			} finally {
				//
				// MUST ALWAYS CLOSE OPEN FILE HANDLES
				//
				if (out != null) {
					out.close();
				}
				//
				// MUST ALWAYS CLOSE OPEN FILE HANDLES
				//
				if (file.getRawFileStream() != null) {
					file.getRawFileStream().close();
				}
			}
		}

}
