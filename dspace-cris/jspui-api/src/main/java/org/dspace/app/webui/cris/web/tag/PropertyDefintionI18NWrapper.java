package org.dspace.app.webui.cris.web.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.dspace.core.I18nUtil;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import it.cilea.osd.jdyna.model.IPropertiesDefinition;

public final class PropertyDefintionI18NWrapper implements MethodInterceptor {
	private Locale locale = null;
	private String localeString = null;
	private String simpleName = null;
	private String shortName = null;
	private int priority = 0;

	public PropertyDefintionI18NWrapper(String simpleName, String shortName, int priority, String localeString) {
		this.locale = Locale.forLanguageTag(localeString);
		//this.locale = I18nUtil.makeLocale(localeString);
		this.localeString = localeString;
		this.simpleName = simpleName;
		this.shortName = shortName;
		this.priority = priority;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (locale != null) {
			if (invocation.getMethod().getName().equals("getLabel")) {
				return getLabel(invocation);
			} else if (invocation.getMethod().getName().equals("getReal")) {
				return getWrapper((IPropertiesDefinition) invocation.proceed(), localeString);
			} else if (invocation.getMethod().getName().equals("getMask")) {
				return getMask(invocation);
			} else if (invocation.getMethod().getName().equals("getPriority")) {
				return priority;
			}
		}
		return invocation.proceed();
	}

	private Object getLabel(MethodInvocation invocation) throws Throwable {
		try {
			return I18nUtil.getMessage(simpleName + "." + shortName + ".label", locale, true);
		} catch (MissingResourceException mre) {
			return invocation.proceed();
		}
	}

	private Object getMask(MethodInvocation invocation) throws Throwable {
		List wrappedList = new ArrayList<>();
		List origList = (List) invocation.proceed();
		for (Object o : origList) {
			wrappedList.add(getWrapper((IPropertiesDefinition) o, localeString));
		}
		return wrappedList;
	}

    public static IPropertiesDefinition getWrapper(IPropertiesDefinition pd, String locale) {
        AspectJProxyFactory pf = new AspectJProxyFactory(pd);
        pf.setProxyTargetClass(true);
        pf.addAdvice(
                new PropertyDefintionI18NWrapper(pd.getAnagraficaHolderClass().getSimpleName(), pd.getShortName(), pd.getPriority(), locale));
        return pf.getProxy();
    }	
	
}
