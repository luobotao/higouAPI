package vo.loading;


/**
 * appversion esult VO
 * @author luobotao
 * @Date 2015年5月4日
 */
public class AppVersionVO {

	public int status;
	public AppversionItem version;
	public static class AppversionItem{
		public String os;
		public String latest_version;
		public String client_version;
		public int has_new;
		public int isforced;
		public int remind;
		public String message;
		public String url;
		
	}
}



