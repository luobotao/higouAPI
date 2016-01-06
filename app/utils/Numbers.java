package utils;


public class Numbers {
	
	public static Integer parseInt(String str, Integer defaultValue){
		try{
			return Integer.parseInt(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Long parseLong(String str, Long defaultValue){
		try{
			return Long.parseLong(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	public static double parseDouble(String str, double defaultValue){
		try{
			return Double.parseDouble(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
}
