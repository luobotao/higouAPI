package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.springframework.util.Assert;

import play.Configuration;

/**
 * 文件相关信息帮助类.
 * 
 * @author luobotao
 *
 */
public class FileUtils {
	/**
	 * 路径分隔符
	 */
	public static final String SEPARATOR = "/";

	// 机算机进制基数.
	private static final int RADIX = 1024;
	// 默认的缓冲区大小:16KB.
	private static final int BUFFER_SIZE = 16 * 1024;
	// Excel的文档内容类型.
	private static final String EXCEL_2003_CONTENT_TYPE = "application/vnd.ms-excel";
	private static final String EXCEL_2007_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String EXCEL_OCTET_CONTENT_TYPE = "application/octet-stream";
	private static List<String> excelContentTypes;
	static {
		excelContentTypes = new ArrayList<String>();
		excelContentTypes.add(EXCEL_2003_CONTENT_TYPE);
		excelContentTypes.add(EXCEL_2007_CONTENT_TYPE);
		excelContentTypes.add(EXCEL_OCTET_CONTENT_TYPE);
	}

	// 时间前的分隔符 aa_1234567890123.txt
	private static final String TIME_SEPARATOR = "_";
	// 文件名与后缀名之间的分隔符.
	private static final String FILE_SEPARATOR = ".";
	// 合法的文件名.
	private static final String FILE_REGEX = "([^\\/:*?\"<>|]+)\\.([^\\/:*?\"<>|\\.]+)";
	// 带有时间标签的文件名
	private static final String TIME_FILE_REGEX = "([^\\/:*?\"<>|]+)_[1-9]\\d{12}\\.([^\\/:*?\"<>|\\.]+)";

	// 日期格式字符串.
	private static final String DATE_FORMAT = "yyyyMMdd";

	private FileUtils() {

	}

	/**
	 * 根据文件的内容类型判断此文档是否是Excel文档.
	 * 
	 * @param fileContentType
	 * @return
	 */
	public static boolean isExcelFile(String fileContentType) {
		if (excelContentTypes.contains(fileContentType)) {
			return true;
		}
		return false;
	}


	/**
	 * 返回文件的大小,单位为KB.
	 * 
	 * @param file
	 * @return 文件的大小
	 */
	public static double getSize(File file) {
		Assert.notNull(file);
		double length = file.length();
		return length / RADIX;
	}

	/**
	 * 返回文件的大小,取大于实际大小的最小double数,该值大于或等于实际大小，并且等于某个整数,单位为KB,
	 * 
	 * @param file
	 * @return 文件的大小.
	 */
	public static double getCeilSize(File file) {
		return Math.ceil(getSize(file));
	}

	/**
	 * 将一个文件名包装为一个文件对象
	 * 
	 * @param fileName
	 * @return
	 */
	public static File createFile(String fileName) {
		Assert.notNull(fileName);
		File file = new File(fileName);
		return file;
	}

	/**
	 *删除指定的文件.
	 * 
	 * @param file
	 * @return 删除成功返回true,删除失败返回false.
	 */
	public static boolean delete(File file) {
		Assert.notNull(file);
		return file.delete();
	}

	/**
	 * 将一个File对象转化为InputStream流.
	 * 
	 * @param file
	 * @return
	 */
	public static InputStream getInputStream(File file) {
		Assert.notNull(file);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {

			throw new RuntimeException("没有找到对应的文件", e);
		}
		return fis;
	}


	/**
	 * 将文件src复制到文件dest中.
	 * 
	 * @param src
	 *            源文件.
	 * @param dest
	 *            目标文件.
	 */
	public static void copy(File src, File dest) {
		Assert.notNull(src, "src file cannot be null");
		Assert.notNull(dest, "dest file cannot be null");
		// 当原文件为空文件的处理方式.

		try {

			InputStream in = null;
			OutputStream out = null;
			try {
				in = new BufferedInputStream(new FileInputStream(src),
						BUFFER_SIZE);
				out = new BufferedOutputStream(new FileOutputStream(dest),
						BUFFER_SIZE);
				// -----1.
				// 当可读字节数大于缓冲字节数，以缓冲字节数为单位读入写入.
				byte[] buffer = new byte[BUFFER_SIZE];
				while (in.available() >= BUFFER_SIZE) {

					in.read(buffer);
					out.write(buffer);
				}
				// 当可读字节数小于缓冲字节数,用可读字节数长度的数组读入写入. example: available=10500,
				// buffer=1000.
				if (in.available() < BUFFER_SIZE) {
					buffer = new byte[in.available()];
					in.read(buffer);
					out.write(buffer);
				}
			 
				/*-------2.单字节读入写入.
				 int i = -1;
				 while ((i = in.read()) != -1) {
				 out.write(i);
				 }
				 */

			} finally {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	/**
	 * 将in中的复制到out.
	 * @param in
	 * @param out
	 * @return 复制的字节数.
	 */
	public static int copy(InputStream in,OutputStream out) {
		Assert.notNull(in,"in must not be null.");
		Assert.notNull(out,"out must not be null.");
		//总的字节数.
		int byteCount = 0;
		//每次读入到缓存的实际字节数.
		int byteRead = -1;
		byte[] b = new byte[BUFFER_SIZE];
		try {
			while ((byteRead = in.read(b)) != -1) {
				out.write(b, 0, byteRead);
				byteCount += byteRead;
			}
		} catch (IOException e) {
			 
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			try {
				out.close();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return byteCount;
	}

	/**
	 * 将一个文件名拆分为文件名和后缀名，存入到一个数组中. 例如aa.txt 拆分为String[]{"aa","txt"}. aa.sh.xml
	 * 拆分为String[]{"aa.sh","xml"}
	 * 
	 * @param fileName
	 * @return
	 * @throws 传入的字符串不是一个文件名.抛出RuntimeException.
	 */
	public static String[] splitFileName(String fileName) {
		int lastIndex = fileName.lastIndexOf(".");
		if(fileName.lastIndexOf(".")>-1){
			lastIndex = fileName.lastIndexOf(".");
		}
		String[] array = new String[2];
		array[0] = fileName.substring(0, lastIndex);
		array[1] = fileName.substring((lastIndex + 1), fileName.length());
		return array;
	}

	/**
	 * 在文件名上加上当前时间标签. 例如:file.txt 转换为 file_234235234.txt.
	 * 
	 * @param fileName
	 * @return
	 */
	public static String addTimeTagFileName(String fileName) {
		return addTimeTagFileName(fileName, new Date().getTime());
	}

	/**
	 * 将时间标签去掉. 由"aaa_1263879003750.xls" 转换为 "aaa.xls"
	 * 
	 * @param fileName
	 * @return
	 */
	public static String removeTimeTagFileName(String fileName) {
		return getOriginalFileName(fileName) + FILE_SEPARATOR
				+ splitFileName(fileName)[1];
	}

	/**
	 * 在文件名上加上时间标签.
	 * 
	 * @param fileName
	 * @param time
	 * @return
	 */
	public static String addTimeTagFileName(String fileName, long time) {
		String[] array = FileUtils.splitFileName(fileName);
		if (array.length > 1){
			return array[0] + TIME_SEPARATOR + UUID.randomUUID().toString()  + FILE_SEPARATOR+array[1];
		}else{
			return array[0] + TIME_SEPARATOR + UUID.randomUUID().toString();
		}
	}

	/**
	 * 取出形如"aa2_1263879003750.xls"的文件名的"aa2"部分.
	 * 
	 * @param timeFileName
	 *            带有时间标志的文件名.
	 * @return 原文件名(去掉时间标签的文件名).
	 */
	public static String getOriginalFileName(String timeFileName) {
		int index = timeFileName.lastIndexOf(TIME_SEPARATOR);
		String orginal = timeFileName.substring(0, index);
		return orginal;
	}

	/**
	 * 将"aa2_1263879003750.xls"字符串的时间部分取出.
	 * 
	 * @param timeFileName
	 * @return
	 */
	public static long getTimePart(String timeFileName) {
		int beginIndex = timeFileName.lastIndexOf(TIME_SEPARATOR);
		int endIndex = timeFileName.lastIndexOf(FILE_SEPARATOR);
		String timeString = timeFileName.substring((beginIndex + 1), endIndex);
		long time = Long.parseLong(timeString);
		return time;
	}

	/**
	 * 用新文件名替换原文件名. 将"aa2_1263879003750.xls"改为"newName_1263879003750.xls";
	 * 
	 * @param fileName
	 *            将要被替换的文件名.
	 * @param newName
	 *            替换的文件名.
	 * @return 替换后的文件名.
	 * @throws
	 */
	public static String replaceFileName(String fileName, String newName) {
		Assert.notNull(newName);
		return newName + TIME_SEPARATOR + getTimePart(fileName)
				+ FILE_SEPARATOR + splitFileName(fileName)[1];
	}

	/**
	 * 重命名文件,文件夹.
	 * 
	 * @param oldFile
	 * @param newFile
	 * @return 操作成功返回true,否则返回false
	 */
	public static boolean replaceFile(File oldFile, File newFile) {
		return oldFile.renameTo(newFile);
	}

	/**
	 * 重命名文件,文件夹.
	 * 
	 * @param oldFileName
	 * @param newFileName
	 * @return 操作成功返回true,否则返回false
	 */
	public static boolean replaceFile(String oldFileName, String newFileName) {
		Assert.notNull(oldFileName);
		Assert.notNull(newFileName);
		File oldFile = createFile(oldFileName);
		File newFile = createFile(newFileName);
		return replaceFile(oldFile, newFile);
	}

	/**
	 * 将指定的日期格式化为指定的字符串格式.
	 * 
	 * @param date
	 *            日期.
	 * @param formatString
	 *            格式化字符串.
	 * @return 日期格式化字符串.
	 */
	private static String format(Date date, String formatString) {
		DateFormat df = new SimpleDateFormat(formatString);
		return df.format(date);
	}

	/**
	 * 将当前日期格式化为形如"yyyyMMdd"的字符串.
	 * 
	 * @return
	 */
	public static String formatToday() {
		return format(new Date(), DATE_FORMAT);
	}

	/**
	 * 根据给定的路径信息,创建指定文件夹.
	 * 
	 * @param path
	 * @return 如果成功创建返回true,如果已存在返回false.
	 */
	public static boolean createFolder(String path) {
		return createFile(path).mkdir();
	}

	/**
	 * 判断后缀名是否在集合里面
	 * 
	 * @param arrFileExt
	 * @param fileExt
	 * @return
	 */
	public static boolean isValidFile(String[] arrFileExt, String fileExt) {
		List<String> listFileExt = new ArrayList<String>();
		for (String o : arrFileExt) {
			listFileExt.add(o);
		}
		if (listFileExt.contains(fileExt)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获得当天上传文件路径的物理路径 
	 * 
	 * @param savePath
	 * @return
	 */
	public static String getTodyUploadPath(String savePath) {
		String path = Configuration.root().getString("export.path") + savePath + FileUtils.SEPARATOR
				+ FileUtils.formatToday();
		FileUtils.createFolder(path);
		return path + FileUtils.SEPARATOR;
	}

	/**
	 * 获得上传文件的物理路径
	 * 
	 * @param savePath
	 * @return
	 */
	public static String getUploadPath() {
		return Configuration.root().getString("uploadPath");
	}

	/**
	 * 获得配置文件的上传物理路径
	 * 
	 * @param savePath
	 * @return
	 */
	public static String getConfigurePath() {
		return Configuration.root().getString("configurePath");
	}

	
	/**
	 * 文件流输出拷贝
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void copyFile(File file, FileOutputStream fos)
			throws IOException {
		FileInputStream fis = new FileInputStream(file);
		int len = 0;
		byte[] buffer = new byte[1024];
		while ((len = fis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
	}

	/**
	 * 得到文件的后缀名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getStrFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf("."), fileName.length()).toLowerCase();
	}
	
	/**
	 * 解压（.gz）文件，并将解压后的文件返回
	 * @param inFileName
	 * @return
	 */
	public static File doUncompressFile(File inFile) {
		String filePath = inFile.getAbsolutePath();
		File file = null;
		try {
			if (!getStrFileExt(filePath).equalsIgnoreCase(".gz")) {
				return null;
			}
			GZIPInputStream in = new GZIPInputStream(new FileInputStream(filePath));

			String outFilePath = getUnzipFilePath(filePath);
			FileOutputStream out = new FileOutputStream(outFilePath);

			byte[] buf = new byte[10240];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
			file = new File(outFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}
	
	/**
	 * 获取解压后的文件路径
	 * @param f
	 * @return
	 */
	public static String getUnzipFilePath(String f) {
		String fname = "";
		int i = f.lastIndexOf('.');

		if (i > 0 && i < f.length() - 1) {
			fname = f.substring(0, i);
		}
		return fname;
	}
	/**
	 * 将网络图片保存至本地服务器，并将该文件返回
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static File getFileFromUrl(String url){
		File file = new File(System.currentTimeMillis()+getStrFileExt(url));  
		try {
			 //new一个URL对象  
	        URL urlHttp = new URL(url);  
	        //打开链接  
	        HttpURLConnection conn = (HttpURLConnection)urlHttp.openConnection();  
	        //设置请求方式为"GET"  
	        conn.setRequestMethod("GET");  
	        //超时响应时间为5秒  
	        conn.setConnectTimeout(5 * 1000);  
	        //通过输入流获取数据  
	        InputStream inStream = conn.getInputStream();  
	        //得到二进制数据，以二进制封装得到数据，具有通用性  
	        byte[] data = readInputStream(inStream);  
	        //new一个文件对象用来保存网络文件，默认保存当前工程根目录  
	        //创建输出流  
	        FileOutputStream outStream = new FileOutputStream(file);  
	        //写入数据  
	        outStream.write(data);  
	        //关闭输出流  
	        outStream.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(file.getAbsolutePath());
        return file;
	}
	 /**
	  * 将输入流转成byte[]
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception{  
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
	        //创建一个Buffer字符串  
	        byte[] buffer = new byte[1024];  
	        //每次读取的字符串长度，如果为-1，代表全部读取完毕  
	        int len = 0;  
	        //使用一个输入流从buffer里把数据读取出来  
	        while( (len=inStream.read(buffer)) != -1 ){  
	            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度  
	            outStream.write(buffer, 0, len);  
	        }  
	        //关闭输入流  
	        inStream.close();  
	        //把outStream里的数据写入内存  
	        return outStream.toByteArray();  
	    }  
	
	
	public static File writeToFile(String fileName, String log) {
		//在application.conf中配置的路径
		String path = Configuration.root().getString("export.path");
		File file = new File(path);
		file.mkdir();//判断文件夹是否存在，不存在就创建
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path+fileName,true);
			out.write(log.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new File(path+fileName);
	}
	public static void main(String[] args) {
		try {
			String fileName="D:\\sumFile.log.gz";
			File fileTemp = new File(fileName);
			File file = doUncompressFile(fileTemp);
			System.out.println(file==null?"====":file.length());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("error !");
		}
		
	}
}
