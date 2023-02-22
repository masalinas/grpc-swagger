package io.grpc.grpcswagger.service;

import static io.grpc.grpcswagger.model.Result.error;
import static io.grpc.grpcswagger.utils.ServiceRegisterUtils.getServiceNames;
import static io.grpc.grpcswagger.utils.ServiceRegisterUtils.registerByIpAndPort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import io.grpc.grpcswagger.manager.ServiceConfigManager;
import io.grpc.grpcswagger.model.RegisterParam;
import io.grpc.grpcswagger.model.Result;
import io.grpc.grpcswagger.model.ServiceConfig;
import io.grpc.grpcswagger.openapi.v2.DefinitionType;
import io.grpc.grpcswagger.openapi.v2.InfoObject;
import io.grpc.grpcswagger.openapi.v2.PathItem;
import io.grpc.grpcswagger.openapi.v2.SwaggerV2DocumentView;
import io.grpc.grpcswagger.openapi.v2.SwaggerV2Documentation;

/**
 * swagger doc api
 * @author masalinas
 */
@Service
public class SwaggerUIService {
    @Autowired
    private DocumentService documentService;
    
    private Result<Object> registerServices(RegisterParam registerParam) {

        List<FileDescriptorSet> fileDescriptorSets = registerByIpAndPort(registerParam.getHost(), registerParam.getPort());
        if (CollectionUtils.isEmpty(fileDescriptorSets)) {
            return error("no services find");
        }
        List<String> serviceNames = getServiceNames(fileDescriptorSets);
        List<ServiceConfig> serviceConfigs = serviceNames.stream()
                .map(name -> new ServiceConfig(name, registerParam.getHostAndPortText()))
                .peek(ServiceConfigManager::addServiceConfig)
                .collect(toList());
        return Result.success(serviceConfigs);
    }
    
	public SwaggerV2DocumentView getSwaggerUIDocumentation(
			String serviceName, String apiHost, String swaggerUiTitle, String grpcHost, Integer grpcPort) {
		RegisterParam registerParam = new RegisterParam();
        registerParam.setHost(grpcHost);
        registerParam.setPort(grpcPort);
        
        // get all register services
        Result<Object> result = registerServices(registerParam);
        @SuppressWarnings("unchecked")
		List<ServiceConfig> services = (List<ServiceConfig>) result.getData();
        
        // aggregate final Swagger V2 Document created from all services from gRPC server
        SwaggerV2Documentation documentation = new SwaggerV2Documentation();
        documentation.setInfo(InfoObject.builder().title(swaggerUiTitle).build());
        documentation.setSchemes(new ArrayList<>(Arrays.asList("http")));
        documentation.setHost(apiHost + "/swagger-ui");
        documentation.setDefinitions(new HashMap<String, DefinitionType>());
        documentation.setPaths(new HashMap<String, PathItem>());
               
        for(ServiceConfig service : services) {
            SwaggerV2Documentation serviceDocumentation = documentService.getDocumentation(service.getService(), apiHost);

            documentation.getDefinitions().putAll(serviceDocumentation.getDefinitions()); 
            
            Map<String, PathItem> operations = serviceDocumentation.getPaths();
            for (Map.Entry<String, PathItem> operation : operations.entrySet()) {
            	operation.getValue().getPost().setTags(new ArrayList<>(Arrays.asList(service.getService())));
        	}
            
            documentation.getPaths().putAll(operations);
        }
        
        return new SwaggerV2DocumentView(serviceName, documentation);
    }
}
