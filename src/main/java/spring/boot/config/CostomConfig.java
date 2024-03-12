package spring.boot.config;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(value = "classpath:blog.properties")
public class CostomConfig {
	static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public VelocityEngine velocityEngine() {
		var prop = new Properties();
		prop.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		prop.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		return new VelocityEngine(prop);
	}
}
