package gov.nih.nci.evs.reportwriter.web.support;

import java.util.Date;

import gov.nih.nci.evs.reportwriter.web.model.ReportTemplate;

public class ReportTaskUI {
	
	private Integer id;
	private String dateCompleted;
	private String dateCreated;
	private String dateStarted;
	private String reportTemplateName;
	private Integer reportTemplateId;
	private String status;
	
	public ReportTaskUI() {
		
	}
	
	public ReportTaskUI(Integer id,String dateCompleted,String dateCreated,String dateStarted,String reportTemplateName,Integer reportTemplateId,String status) {
		this.id = id;
		this.dateCompleted = dateCompleted;
		this.dateCreated = dateCreated;
		this.dateStarted = dateStarted;
		this.reportTemplateName = reportTemplateName;
		this.reportTemplateId = reportTemplateId;
		this.status = status;
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDateCompleted() {
		return dateCompleted;
	}
	public void setDateCompleted(String dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateStarted() {
		return dateStarted;
	}
	public void setDateStarted(String dateStarted) {
		this.dateStarted = dateStarted;
	}
	public String getReportTemplateName() {
		return reportTemplateName;
	}
	public void setReportTemplateName(String reportTemplateName) {
		this.reportTemplateName = reportTemplateName;
	}
	public Integer getReportTemplateId() {
		return reportTemplateId;
	}
	public void setReportTemplateId(Integer reportTemplateId) {
		this.reportTemplateId = reportTemplateId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

}
