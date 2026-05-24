package com.codingshuttle.learn_spring_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RAGServiceTest {

    @Autowired
    private RAGService ragService;

    @Test
    public void testPdfIngest() {
        ragService.ingestPdfToVectorStore();
    }

    @Test
    public void testAskAI() {
        String res = ragService.askAI("Cant view the live class, what to do?");
        System.out.println(res);
    }

    @Test
    public void testAskAIWithAdvisor() {
        String res = ragService.askAIWithAdvisors("What is my name?", "devanshu123");
        System.out.println(res);
    }
}
