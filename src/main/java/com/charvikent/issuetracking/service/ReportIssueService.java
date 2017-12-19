package com.charvikent.issuetracking.service;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.charvikent.issuetracking.dao.ReportIssueDao;
import com.charvikent.issuetracking.dao.UserDao;
import com.charvikent.issuetracking.model.ReportIssue;
import com.charvikent.issuetracking.model.User;

@Service
@Transactional
public class ReportIssueService {
	
	private static final String SUBJECT_MAIL_TICKET_ISSUED = "Ticket Issued";
	@Autowired
	private UserDao userDao;

	/*@Autowired
	private ReportIssue reportIssue;*/
	@Autowired  
	private VelocityEngine velocityEngine; 

	@Autowired  
	private JavaMailSender javaMailSender; 
	
	@Autowired
	private ReportIssueDao reportIssueDao;
	private User user;
	
	public void saveReportIssue(ReportIssue reportIssue,File serverFile) throws MessagingException
	{
		reportIssueDao.saveReportIssue(reportIssue);
		sendConfirmationEmail(reportIssue,user,serverFile);
	}
	
	public List<ReportIssue> getAllReportIssues()
	{
		
		return reportIssueDao.getAllReportIssues();
	}
	
	public void sendConfirmationEmail( final ReportIssue reportIssue,User user, File serverFile) throws MessagingException {  
		try {
			
			
			int assignedUser = Integer.parseInt(reportIssue.getAssignto());
			user = userDao.find(assignedUser);

			/*String text1=reportIssue.getDescription();
			
			String text2= "hi you have have following issue";*/
			String email = user.getEmail();
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			/*helper.setTo(email);
			helper.setText(text2+text1);
			helper.setSubject("Isuue id  "+reportIssue.getId());
			helper.addAttachment("file", serverFile);
			sender.send(message);
*/
			
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("assignedName",user.getUsername());
			velocityContext.put("issueId",reportIssue.getId());
			
			velocityContext.put("severity",reportIssue.getSeverity());
			velocityContext.put("category",reportIssue.getSubject());
			velocityContext.put("description",reportIssue.getDescription());
			//Map model1 = new HashMap<>();
			//model1.put("newMessage", velocityContext);
			StringWriter stringWriter = new StringWriter();
			//@SuppressWarnings("deprecation")
			//String text2=VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "issuedEmailTemplate.vm", "UTF-8", model1);
			velocityEngine.mergeTemplate("issuedEmailTemplate.vm", "UTF-8", velocityContext, stringWriter);
			helper.setText(stringWriter.toString(), true);
			helper.setTo( email);
			helper.setSubject(SUBJECT_MAIL_TICKET_ISSUED);  
			//helper.setSubject("Hi");
			helper.addAttachment("file",serverFile);
			javaMailSender.send(message);
				
			/*MimeMessagePreparator preparator = new MimeMessagePreparator() {  
				//@Autowired(required = false)
				@SuppressWarnings({ "unchecked", "deprecation" })
				@Override  
				public void prepare(MimeMessage mimeMessage) throws Exception { 
					//String email = user.getEmail();
					
					mimeMessage = javaMailSender.createMimeMessage();  
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
					helper.setTo(user.getEmail());
					

					helper.setSubject(SUBJECT_MAIL_TICKET_ISSUED);  
					//Map model = new HashMap<>();  
					//model.put("reportIssue", reportIssue);  
					//model.put("issueId", "457896");
					VelocityContext velocityContext = new VelocityContext();
					velocityContext.put("assignedName",user.getUsername());
					velocityContext.put("issueId",reportIssue.getId());
					//velocityContext.put("category",reportIssue.getCategory());
					//velocityContext.put("assignedName", reportIssue.getAssignto());
					//velocityContext.put("dateAndTime",reportIssue.getCreatedTime());
					velocityContext.put("severity",reportIssue.getSeverity());
					velocityContext.put("category",reportIssue.getSubject());
					velocityContext.put("discription",reportIssue.getDescription());
					StringWriter stringWriter = new StringWriter();
					velocityEngine.mergeTemplate("issuedEmailTemplate.vm", "UTF-8", velocityContext, stringWriter);
					
					//velocityContext.put("user",user);
					message.setText(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine  
							, "emailtemplate.vm", CHARSET_UTF8, model), true); 
					//File file = reportIssue.getUploadfile();
					
					helper.setText(stringWriter.toString(), true);
 					//helper.addAttachment(file.getName(), file);
	
				}  
			};  
			this.javaMailSender.send(preparator);*/
		} catch (MailException e) {
			e.printStackTrace();
			System.out.println(e);
		}  
	}

	public List<ReportIssue> getIssuesByAssignBy(String id) {
		// TODO Auto-generated method stub
		return reportIssueDao.getIssuesAssignBy(id);
	}

	public Object getIssuesByAssignTo(String id) {
 
		return reportIssueDao.getIssuesAssignTo(id);
	}  
	
	
	public Map<Integer,List< Integer>> getGapAndCount()
	{
		Map<Integer,List< Integer>> listmap= new  HashMap<Integer,List< Integer>>();
		
	//	List<Integer> timelineDays = Arrays.asList(1,2,3,7,30,60,90,120,180,365);
		Map<Integer, Integer> issueTimelines = reportIssueDao.getGapAndCount();
		Map<Integer, Integer> issueTimelinesClosed = reportIssueDao.getGapAndCountForClosed();
		
		List<Integer> list1 =new ArrayList<Integer>();
		List<Integer> list2 =new ArrayList<Integer>();
		List<Integer> list3 =new ArrayList<Integer>();
		List<Integer> list7 =new ArrayList<Integer>();
		List<Integer> list30 =new ArrayList<Integer>();
		List<Integer> list60 =new ArrayList<Integer>();
		List<Integer> list90 =new ArrayList<Integer>();
		List<Integer> list180 =new ArrayList<Integer>();
		List<Integer> list365 =new ArrayList<Integer>();
		
		int day1Issues = 0;
		int day2Issues = 0;
		int day3Issues = 0;
		int day7Issues = 0;
		int day30Issues = 0;
		int day60Issues = 0;
		int day90Issues = 0;
		int day180Issues = 0;
		int day365Issues = 0;
		
		for(Map.Entry<Integer, Integer> entry : issueTimelines.entrySet()){
			
			if(entry.getKey() <= 1){
				day1Issues = day1Issues+entry.getValue();
			}
			if(entry.getKey() == 2  ){
				day2Issues = day2Issues+entry.getValue();
			}
			if(entry.getKey() == 3  ){
				day3Issues = day3Issues+entry.getValue();
			}
			if(entry.getKey() <=7 && entry.getKey() > 3  ){
				day7Issues = day7Issues+entry.getValue();
			}
			if(entry.getKey() <=30 && entry.getKey() > 7  ){
				day30Issues = day30Issues+entry.getValue();
			}
			
			if(entry.getKey() <=60 && entry.getKey() > 30  ){
				day60Issues = day60Issues+entry.getValue();
			}
			if(entry.getKey() <=90 && entry.getKey() > 60  ){
				day90Issues = day90Issues+entry.getValue();
			}
			if(entry.getKey() <=180 && entry.getKey() > 90  ){
				day180Issues = day180Issues+entry.getValue();
			}
			if(entry.getKey() <=365 && entry.getKey() > 180  ){
				day365Issues = day180Issues+entry.getValue();
			}
			
		}
		
		//setting values to map
		Map<Integer, Integer> gapAndCount = new HashMap<Integer, Integer>();
		gapAndCount.put(1, day1Issues);
		gapAndCount.put(2, day2Issues);
		gapAndCount.put(3, day3Issues);
		gapAndCount.put(7, day7Issues);
		gapAndCount.put(30, day30Issues);
		gapAndCount.put(60, day60Issues);
		gapAndCount.put(90, day90Issues);
		gapAndCount.put(180, day180Issues);
		gapAndCount.put(365, day365Issues);
		
		
		
		int day1Issuesclosed = 0;
		int day2Issuesclosed = 0;
		int day3Issuesclosed = 0;
		int day7Issuesclosed = 0;
		int day30Issuesclosed = 0;
		int day60Issuesclosed = 0;
		int day90Issuesclosed = 0;
		int day180Issuesclosed = 0;
		int day365Issuesclosed = 0;
		
		
		
for(Map.Entry<Integer, Integer> entry : issueTimelinesClosed.entrySet()){
			
			if(entry.getKey() <= 1){
				day1Issuesclosed = day1Issuesclosed+entry.getValue();
			}
			if(entry.getKey() == 2  ){
				day2Issuesclosed = day2Issuesclosed+entry.getValue();
			}
			if(entry.getKey() == 3  ){
				day3Issuesclosed = day3Issuesclosed+entry.getValue();
			}
			if(entry.getKey() <=7 && entry.getKey() > 3  ){
				day7Issuesclosed = day7Issuesclosed+entry.getValue();
			}
			if(entry.getKey() <=30 && entry.getKey() > 7  ){
				day30Issuesclosed = day30Issuesclosed+entry.getValue();
			}
			
			if(entry.getKey() <=60 && entry.getKey() > 30  ){
				day60Issuesclosed = day60Issuesclosed+entry.getValue();
			}
			if(entry.getKey() <=90 && entry.getKey() > 60  ){
				day90Issues = day90Issues+entry.getValue();
			}
			if(entry.getKey() <=180 && entry.getKey() > 90  ){
				day180Issues = day180Issues+entry.getValue();
			}
			if(entry.getKey() <=365 && entry.getKey() > 180  ){
				day365Issues = day180Issues+entry.getValue();
			}
			
}
		
		list1.add(day1Issues);
		list1.add(day1Issuesclosed);
		list1.add(day1Issues-day1Issuesclosed);
		
		list2.add(day2Issues);
		list2.add(day2Issuesclosed);
		list2.add(day2Issues-day2Issuesclosed);
		
		list3.add(day3Issues);
		list3.add(day3Issuesclosed);
		list3.add(day3Issues-day3Issuesclosed);
		
		list7.add(day7Issues);
		list7.add(day7Issuesclosed);
		list7.add(day7Issues-day7Issuesclosed);
		
		list30.add(day30Issues);
		list30.add(day30Issuesclosed);
		list30.add(day30Issues-day30Issuesclosed);
		
		
		list60.add(day60Issues);
		list60.add(day60Issuesclosed);
		list60.add(day60Issues-day60Issuesclosed);
		
		
		list90.add(day90Issues);
		list90.add(day90Issuesclosed);
		list90.add(day90Issues-day90Issuesclosed);
		
		
		list180.add(day180Issues);
		list180.add(day180Issuesclosed);
		list180.add(day180Issues-day180Issuesclosed);
		

		list365.add(day365Issues);
		list365.add(day365Issuesclosed);
		list365.add(day365Issues-day365Issuesclosed);
		
		listmap.put(1, list1);
		listmap.put(2,  list2);
		listmap.put(3,  list3);
		listmap.put(7,  list7);
		listmap.put(30,  list30);
		listmap.put(60,  list60);
		listmap.put(90,  list90);
		listmap.put(180,  list180);
		listmap.put(365,  list365);
		
		
		//return gapAndCount;
		
		return listmap;
	}




}
