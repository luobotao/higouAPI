package utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import play.data.validation.ValidationError;

@Named
@Singleton
public class Errors {

	public static String convert(Map<String, List<ValidationError>> errors,
			Convert convert) {
		StringBuilder sb = new StringBuilder();
		Set<Map.Entry<String, List<ValidationError>>> set = errors.entrySet();
		for (Map.Entry<String, List<ValidationError>> entry : set) {
			for (ValidationError error : entry.getValue()) {
				sb.append(convert.convert(error.message()));
			}
		}
		return sb.toString();
	}

	public static String wrapedWithComma(Map<String, List<ValidationError>> errors) {
		return convert(errors, new Convert() {

			@Override
			public String convert(String target) {
				return target + Constants.COMMA;
			}
		});
	}

	public static String wrapedWithDiv(Map<String, List<ValidationError>> errors) {
		return convert(errors, new Convert() {

			@Override
			public String convert(String target) {
				return "<div>" + target + "</div>";
			}
		});
	}

	public static String wrapedWithNL(Map<String, List<ValidationError>> errors) {
		return convert(errors, new Convert() {

			@Override
			public String convert(String target) {
				return target + Constants.NL;
			}
		});
	}

	public static String getFirst(Map<String, List<ValidationError>> errors) {
		Set<Map.Entry<String, List<ValidationError>>> set = errors.entrySet();
		for (Map.Entry<String, List<ValidationError>> entry : set) {
			for (ValidationError error : entry.getValue()) {
				return error.message();
			}
		}
		return null;
	}

	private static interface Convert {
		String convert(String target);
	}

	public static String trace(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return t.getMessage();
	}
}
