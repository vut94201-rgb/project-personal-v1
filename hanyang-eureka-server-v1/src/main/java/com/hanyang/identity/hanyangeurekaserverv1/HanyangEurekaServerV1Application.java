package com.hanyang.identity.hanyangeurekaserverv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class HanyangEurekaServerV1Application {

    public static void main(String[] args) {
        SpringApplication.run(HanyangEurekaServerV1Application.class, args);
    }

}
