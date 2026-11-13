package org.example.petstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PetStore 宠物商店 CMS 启动类。
 * 由 StoessmApplication 更名而来（原包名 org.example.stoessm 已废弃）。
 */
@SpringBootApplication
@MapperScan("org.example.petstore.mapper")
public class PetStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetStoreApplication.class, args);
    }

}
