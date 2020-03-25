package com.xuecheng.testfastdfs;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

@Import(FdfsClientConfig.class)
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestFastdfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestFastdfsApplication.class, args);
    }

}
