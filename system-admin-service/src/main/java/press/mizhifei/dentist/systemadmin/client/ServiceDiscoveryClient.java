package press.mizhifei.dentist.systemadmin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for Service Discovery operations
 * 
 * Provides integration with Eureka Discovery Server for service management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@FeignClient(name = "discovery-server", url = "${eureka.client.serviceUrl.defaultZone:http://localhost:8761}")
public interface ServiceDiscoveryClient {

    /**
     * Get all registered services
     */
    @GetMapping("/eureka/apps")
    Map<String, Object> getAllServices();

    /**
     * Get specific service information
     */
    @GetMapping("/eureka/apps/{serviceName}")
    Map<String, Object> getServiceInfo(@PathVariable String serviceName);

    /**
     * Get service instances
     */
    @GetMapping("/eureka/apps/{serviceName}/instances")
    List<Map<String, Object>> getServiceInstances(@PathVariable String serviceName);
}
