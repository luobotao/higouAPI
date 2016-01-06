package utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import play.Logger;

public class BeanUtils {
    private static final Logger.ALogger LOGGER = Logger.of(BeanUtils.class);
    
    /**
     * 传入一个bean获取其属性值为空的属性集合
     * @param source
     * @return
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null)
                emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 传入一个bean获取其属性值不为空的属性集合
     * @param source
     * @return
     */
    public static String[] getNotNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue != null)
                emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
    
    public static void copyProperties(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target,
                getNullPropertyNames(source));
    }

    public static void copyProperties(Object source, Object target,
                                      String[] ingore) {
        List<String> list = new LinkedList<>(Arrays.asList(ingore));
        list.addAll(Arrays.asList(getNullPropertyNames(source)));
        String[] all = new String[list.size()];
        list.toArray(all);
        org.springframework.beans.BeanUtils.copyProperties(source, target, all);
    }

    public static void setValue(Object object, String fieldName, Object newValue){
        Field field;
        Object oldValue;
        try {
            field  = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            oldValue = field.get(object);
            if((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue))){
                field.set(object,newValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("set value error ", e);
        }
    }
    
	/**
	 * 传入一个对象与要获取属性值的名称，返回该属性值
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static Object getPropertyValue(Object object, String fieldName) {
		final BeanWrapper src = new BeanWrapperImpl(object);
		Object value = src.getPropertyValue(fieldName);
		return value;
	}

    public static <T> List<T> castEntity(List<Object[]> list, Class<T> clazz) {
        List<T> returnList = new ArrayList<>();
        if(list.size() > 0 ) {
            Object[] co = list.get(0);
            Class[] c2 = new Class[co.length];

            for (int i = 0; i < co.length; i++) {
                c2[i] = co[i].getClass();
            }

            for (Object[] o : list) {
                Constructor<T> constructor;
                try {
                    constructor = clazz.getConstructor(c2);
                    returnList.add(constructor.newInstance(o));
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    LOGGER.error("构建" + clazz.getName() + "失败", e);
                }
            }
        }
        return returnList;
    }
}
