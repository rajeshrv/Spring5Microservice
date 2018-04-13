package com.brownfield.pss.search.apigateway;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

 
import com.netflix.appinfo.AmazonInfo;
import org.springframework.cloud.commons.util.*;


@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
@CrossOrigin
@EnableSwagger2 
@EnableCircuitBreaker
public class SearchApiGateway {

	@Bean
	public CustomZuulFilter customFilter() {
		return new CustomZuulFilter();
	}
	
 	
	public static void main(String[] args) {
		SpringApplication.run(SearchApiGateway.class, args);
	}
	
}

@RestController 
class SearchAPIGatewayController {
	private static final Logger logger = LoggerFactory.getLogger(SearchAPIGatewayController.class);

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	SearchAPIGatewayComponent component;
	
	@RequestMapping("/")
	String greet(HttpServletRequest req){
		logger.info("Response for Gateway received");
		return "<H1>Search Gateway Powered By Zuul</H1>"; 
	}
	
	@RequestMapping("/hubongw")
	String getHub(HttpServletRequest req){
		logger.info(" [Hystrix enabled] Search Request in API gateway for getting Hub, forwarding to search-service - ");
		return component.getHub(); 
	} 

}

@Component	
class SearchAPIGatewayComponent { 

	@Autowired 
	RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "getDefaultHub")
	public String getHub(){
		String hub = restTemplate.getForObject("http://search-service/search/hub", String.class);
		return hub;
	}
	public String getDefaultHub(){
		return "Possibily SFO";
	}
}



@Configuration
class AppConfiguration {
    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
      return new RestTemplate();
    }
}




@Configuration
class EurekaConfig { 

	private static final Logger logger = LoggerFactory.getLogger(EurekaConfig.class);
	
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfigBean() {
    	InetUtils instance = new InetUtils(new InetUtilsProperties());
    	EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(instance);
    	try { 
	   		logger.info("Ereka Pre Configuring-3");
		   AmazonInfo info = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
		    config.setDataCenterInfo(info);
		    info.getMetadata().put(AmazonInfo.MetaDataKey.publicHostname.getName(), info.get(AmazonInfo.MetaDataKey.publicIpv4));
		    config.setHostname(info.get(AmazonInfo.MetaDataKey.localHostname));
		    
		    logger.info("hostname" + info.get(AmazonInfo.MetaDataKey.localHostname));
		    logger.info("IP" + info.get(AmazonInfo.MetaDataKey.publicIpv4));
		    
		//    config.setIpAddress(info.get(AmazonInfo.MetaDataKey.publicIpv4)); 
		   
	   		config.setNonSecurePortEnabled(true);
	        config.setNonSecurePort(0); //change this later
	    //    config.setPreferIpAddress(true);
	        
	       // config.setIpAddress("54.85.107.37");
	        config.getMetadataMap().put("instanceId",  info.get(AmazonInfo.MetaDataKey.localHostname));
	 
		   // logger.info("info" + info); 
    	}catch (Exception e){
    		e.printStackTrace();
    	}
	    return config;
	}
}