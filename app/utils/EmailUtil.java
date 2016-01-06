package utils;


import java.util.HashMap;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailUtil {
	private Logger logger = Logger.getLogger(EmailUtil.class);
	private static EmailUtil instance ;
	
	private static JavaMailSenderImpl mailSender;
	
	//获取配置文件，初始化sender；
	public static EmailUtil getInstance()
	{
		if( mailSender == null ){
			mailSender = new JavaMailSenderImpl();
		}
		if( instance == null ){
			instance = new EmailUtil();
			mailSender.setDefaultEncoding( "GBK" );
			mailSender.setHost( "smtp.qiye.163.com");
			mailSender.setUsername( "lvcheng@neolix.cn");
			mailSender.setPassword( "nicai123");
			Properties prop1 = new Properties() ;     
			prop1.put( "mail.smtp.auth", "true" ) ;// 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确  
			prop1.put("mail.mime.charset", "GBK");
			mailSender.setJavaMailProperties( prop1 ) ; 
		}
		return instance;
	}
	
	public boolean sendMsg(String from, String to,  HashMap<String,Object> prop) {
		// 使用JavaMail的MimeMessage，支付更加复杂的邮件格式和内容
		MimeMessage msg = mailSender.createMimeMessage();
		// 创建MimeMessageHelper对象，处理MimeMessage的辅助类
		try {
			MimeMessageHelper helper = new MimeMessageHelper(msg, true,"GBK");
			// 使用辅助类MimeMessage设定参数
			helper.setFrom( mailSender.getUsername() );
			String[] tos = to.split(",");
			helper.setTo(tos);
			logger.info( "recive:"+to);
			helper.setSubject( prop.get("subject").toString() );
			helper.setText(prop.get("html").toString(),true );
			// 发送邮件
			mailSender.send(msg);
		} catch (Exception e) {
			logger.info( e.toString() );
			return false;
		}
		return true;
	}
	
	public boolean sendMsgSimple(HashMap<String,Object> prop,String to) {
		// 使用JavaMail的MimeMessage，支付更加复杂的邮件格式和内容
		MimeMessage msg = mailSender.createMimeMessage();
		// 创建MimeMessageHelper对象，处理MimeMessage的辅助类
		try {
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);
			// 使用辅助类MimeMessage设定参数
			helper.setFrom( mailSender.getUsername() );
			if(to.equals("")){
				to = mailSender.getUsername();
				helper.setTo(to);
			}else{
				String[] tos = to.split(",");
				helper.setTo(tos);
			}
			logger.info( "recive:"+to);
			helper.setSubject( prop.get("subject").toString() );
			helper.setText(prop.get("html").toString(),true );
			// 发送邮件
			mailSender.send(msg);
		} catch (Exception e) {
			logger.info( e.toString() );
			return false;
		}
		return true;
	}

    //简单下发
	public String sendEmail(String to, String content, String subject, String htmlContent) {
		HashMap<String,Object> prop=new HashMap<String,Object>();
		prop.put("subject", subject);          
		prop.put("html"   , content);  
		sendMsg(  "", to, prop);
		return "";
	}
	//试用申请发送邮件
	public String sendEmailTrial(String subject,String content,String to){
		HashMap<String,Object> prop=new HashMap<String,Object>();
		prop.put("subject", subject);          
		prop.put("html"   , content);  
		sendMsgSimple(prop,to);
		return "";
	}
	
	public static void main(String[] args) {
		EmailUtil em = EmailUtil.getInstance();
		HashMap<String,Object> prop=new HashMap<String,Object>();
		prop.put("subject", "真实库存阀值预警  "+Dates.getCurrentDay()); 
        // 正文  
        StringBuilder builder = new StringBuilder();  
        builder.append("<html><head>");  
        builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");  
        builder.append("</head><body>");  
        builder.append("<h1>Hi All：</h1><br />");  
        builder.append("<h3>此封邮件为系统自动发送，请勿回复！<h3>");  
        builder.append("<p>库存阀值预警，请相关负责人查看并处理，辛苦各位！<p>");    
        builder.append("<p>以下为预警商品详细内容，请查阅：<p><br/>");    
        builder.append("<table style='border=1 cellspacing=0 cellpadding=0' width='60%' ><thead style='align:center'><th style='width:20%'>商品ID</th><th style='width:40%'>商品名称</th><th style='width:20%'>新SKU</th><th style='width:20%'>可售库存</th></thead><tbody><tr><td>1</td><td>2safsdfag</td><td>1122</td><td>test</td></tr></tbody></table>");  
        builder.append("</body></html>");   
        String htmlContent = builder.toString(); 
		prop.put("html", htmlContent);
		em.sendMsg("", "chentaotao@higegou.com,778327382@qq.com", prop);
		System.out.println(Dates.getCurrentTime()+"发送完成");
	}

}
