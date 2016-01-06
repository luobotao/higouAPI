package utils;

public class Htmls {
	private static final String OPTION = "<option value=\"%d\">%s</option>";

	private static final String SELECTED_OPTION = "<option selected value=\"%d\">%s</option>";

	public static String generateOption(Object key, Object value) {
		return String.format(OPTION, key, value);
	}

	public static String generateSelectedOption(Object key, Object value) {
		return String.format(SELECTED_OPTION, key, value);
	}

}
