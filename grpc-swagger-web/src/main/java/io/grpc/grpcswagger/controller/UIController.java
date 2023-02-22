package io.grpc.grpcswagger.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.grpc.grpcswagger.service.SwaggerUIService;

/**
 * @author masalinas
 */
@Controller
public class UIController {	
	@Autowired
	private SwaggerUIService swaggerUIService;
	 
	private static Logger LOG = LoggerFactory.getLogger(UIController.class);
	
	@Value("#{environment.SWAGGER_UI_TITLE}")
	private String swaggerUiTitle = "gRPC Services Documentation";
	@Value("#{environment.GRPC_HOST}")
	private String grpcHost = "localhost";
	@Value("#{environment.GRPC_PORT}")
	private Integer grpcPort = 9090;

    @RequestMapping("/")
    public String index() {
        return "redirect:ui/r.html";
    }
        
    @RequestMapping("/swagger-ui")
    public String swaggerUI(HttpServletRequest httpServletRequest) throws Exception {
    	String apiProtocol = httpServletRequest.getScheme();
    	String apiHost = httpServletRequest.getHeader("Host");
    	
    	// create swagger document from gRPC services exposed
    	LOG.info("Creating agregate gRPC swagger document ...");
    	Object swaggerUIDocumentation = swaggerUIService.getSwaggerUIDocumentation("", apiHost, swaggerUiTitle, grpcHost, grpcPort);
    	
    	// persist swagger document file
    	ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    	ObjectWriter ow = mapper.writer();
    	String json = ow.writeValueAsString(swaggerUIDocumentation);
    	
    	LOG.info("Saving agregate gRPC swagger document in temporal folder tmp ...");   	    
    	Path swaggerFolder = Paths.get("/tmp");
    	    	
    	FileWriter file = new FileWriter(swaggerFolder.toString() + "/swagger.json");
        file.write(json);
        file.close();
            
        // open swagger ui injecting swagger document
        LOG.info("Opening swagger-ui and injecting agregate gRPC swagger document ...");
        return "redirect:" + apiProtocol +"://" + apiHost + "/ui/index.html?url=" + apiProtocol + "://" + apiHost + "/ui/swagger.json";
    }
}


