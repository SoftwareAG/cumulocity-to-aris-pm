package c8y.to.aris.ms;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

@MicroserviceApplication
public class App {
	protected static final Logger logger = LoggerFactory.getLogger(App.class);
	static ConfigurableApplicationContext appCtx;
	
    public static void main (String[] args) {
    	appCtx = SpringApplication.run(App.class, args);
    }
    
    public static void shutdownMicroservice()
	{
    	logger.info("You can now connect to the ARIS Process Mining tenant to configure the tables and visualize the Cumulocity processes.");
		logger.info("This Microservice will now auto-shutdown.");
		SpringApplication.exit(appCtx, new ExitCodeGenerator() {
			
			@Override
			public int getExitCode() {
				// TODO Auto-generated method stub
				return 0;
			}
		});
	}
}