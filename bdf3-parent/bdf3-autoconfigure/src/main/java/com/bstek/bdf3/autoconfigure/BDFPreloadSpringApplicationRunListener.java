package com.bstek.bdf3.autoconfigure;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class BDFPreloadSpringApplicationRunListener implements
		SpringApplicationRunListener {
	
	private SpringApplication application;


	public BDFPreloadSpringApplicationRunListener(SpringApplication application, String[] args) {
		this.application = application;
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {

	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {

	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {

	}

	@Override
	public void starting() {
		Properties properties = new Properties();
		String basePackage = application.getClass().getPackage().getName();
		String projectName = StringUtils.substringAfterLast(basePackage, ".");
		properties.put("bdf3.projectName", projectName);
		properties.put("bdf3.basePackage", basePackage);
		properties.put("spring.mvc.staticPathPattern", "static/**");
		application.setDefaultProperties(properties);
		
	}

	@Override
	public void started(ConfigurableApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		// TODO Auto-generated method stub
		
	}

}
