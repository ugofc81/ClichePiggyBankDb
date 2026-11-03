package com.example.clichepiggybankdb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/user_db",
        "spring.datasource.username=root",
        "spring.datasource.password=secret",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
})
class ClichePiggyBankDbApplicationTests {

    @Test
    void contextLoads() {
    }

}
