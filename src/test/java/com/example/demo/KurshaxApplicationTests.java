package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KurshaxApplicationTests {

	@Test
	void contextLoads() {
	}
    @Test
    void concatTest(){
        String str1 = "abc";
        String str2 = "abc";
        assertEquals("abcabc" ,str1+str2);
    }

}
