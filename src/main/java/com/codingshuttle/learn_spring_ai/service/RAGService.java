package com.codingshuttle.learn_spring_ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RAGService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:faq.pdf")
    Resource faqPdfResource;

    public List<Document> springAiDocs() {
        return List.of(
                new Document(
                        "Spring AI provides abstractions like ChatClient, ChatModel, and EmbeddingModel to interact with LLMs.",
                        Map.of("topic", "basics")
                ),
                new Document(
                        "A VectorStore is used to persist embeddings and perform similarity search for retrieval augmented generation.",
                        Map.of("topic", "vectorstore")
                ),
                new Document(
                        "Retrieval Augmented Generation combines vector similarity search with prompt augmentation to reduce hallucinations.",
                        Map.of("topic", "rag")
                ),
                new Document(
                        "PgVectorStore stores embeddings inside PostgreSQL using the pgvector extension.",
                        Map.of("topic", "pgvector")
                ),
                new Document(
                        "ChatClient provides a fluent API to send prompts to language models like OpenAI or Ollama.",
                        Map.of("topic", "chat")
                )
        );
    }

    public void ingestPdfToVectorStore() {
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(faqPdfResource);
        List<Document> documents = pagePdfDocumentReader.read();

        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMinChunkSizeChars(50)
                .build();
        List<Document> chunks = tokenTextSplitter.apply(documents);
        vectorStore.add(chunks);
    }

    /*
        ========================================
        RAG (Retrieval Augmented Generation) Flow
        ========================================

        1. User asks a question
           Example:
           "What is the refund policy?"

        2. Convert user query into embedding vector
           Using embedding model:
           -> nomic-embed-text

           Query:
           "What is the refund policy?"

           becomes:
           [0.23, -0.91, 0.44, ...]

        3. Perform semantic similarity search in PGVector
           - Compare query vector with stored document chunk vectors
           - Return most semantically similar chunks

           SearchRequest:
           - query(prompt)
           - similarityThreshold(...)
           - topK(...)
           - metadata filters

        4. Retrieve top matching document chunks
           Example:
           Chunk 1 -> Refund policy details
           Chunk 2 -> Cancellation rules
           Chunk 3 -> Payment information

        5. Extract text from retrieved chunks
           docs.stream()
               .map(Document::getText)

        6. Combine all chunk text into single context string
           Context becomes:

           "Refunds are allowed within 30 days...

            Customers can cancel subscriptions..."

        7. Inject retrieved context into prompt template
           Replace:
           {context}

           with:
           actual retrieved chunk text

        8. Create final SYSTEM prompt
           This tells LLM:
           - behave as assistant
           - use ONLY provided context
           - avoid hallucination
           - answer conversationally

        9. Send USER question separately
           USER:
           "What is the refund policy?"

        10. Final request sent to LLM

           SYSTEM:
           Instructions + Retrieved Context

           USER:
           Actual question

        11. LLM generates answer ONLY using retrieved context

        12. Return generated response to client


        ========================================
        Overall Architecture
        ========================================

        User Query
            ↓
        Embedding Model
            ↓
        Vector Similarity Search (PGVector)
            ↓
        Retrieve Relevant Chunks
            ↓
        Inject Chunks into Prompt
            ↓
        LLM Generates Context-Aware Answer


        ========================================
        Important Notes
        ========================================

        - LLM does NOT directly query database
        - Application orchestrates retrieval flow
        - Vector DB provides memory/knowledge
        - LLM provides natural language response
        - similarityThreshold too low can return irrelevant chunks
        - Better chunking = better retrieval quality
        - Embedding model used during ingestion and querying
          MUST remain same


        ====================================================

        Q) Why are we doing vector similarity search before calling the LLM?
        Why not directly send the user question to the LLM?

        Answer:
        The LLM does NOT know our private documents, PDFs, FAQs, databases,
        or latest uploaded data.

        It only knows:
        - its training knowledge
        - public/general information
        - whatever is present in the current prompt

        So if user asks:

        "What is your refund policy?"

        the LLM cannot answer correctly unless we first provide
        the relevant information from our own documents.

        That is the purpose of vector search.


        ------------------------------------------------------------


        Q) What does vector search actually do?

        Answer:
        Vector search acts like a "memory retrieval system".

        It searches the vector database and finds the most semantically
        relevant document chunks related to the user's question.

        Example:

        User Question:
        "What is the refund policy?"

        Stored Document:
        "Customers may request reimbursement within 45 days."

        Even though words are different:
        - refund
        - reimbursement

        embedding vectors understand semantic meaning and can still
        find the correct chunk.


        ------------------------------------------------------------


        Q) Why not send the entire PDF/document to the LLM?

        Answer:
        Because:
        - documents can be huge
        - token limits are limited
        - expensive and slow
        - irrelevant information confuses the model

        Instead, vector search retrieves ONLY the most relevant chunks
        and sends them to the LLM.


        ------------------------------------------------------------


        Q) Why do we send BOTH:
        1. Retrieved context
        2. Original user question

        to the LLM?

        Answer:
        Because both have different responsibilities.


        1) Retrieved Context:
        Provides factual information / knowledge source.

        Example:
        "Refunds are allowed within 45 days."


        2) User Question:
        Tells the model what the user wants.

        Example:
        "What is the refund policy?"


        Without context:
        LLM may hallucinate or guess.

        Without user question:
        LLM does not know what to answer.


        ------------------------------------------------------------


        Q) What is the complete RAG flow?

        Answer:

        User Question
              ↓
        Convert question into embedding vector
              ↓
        Perform semantic similarity search in PGVector
              ↓
        Retrieve most relevant document chunks
              ↓
        Combine retrieved chunks into context
              ↓
        Inject context into prompt
              ↓
        Send:
           - SYSTEM prompt (rules + context)
           - USER question
        to LLM
              ↓
        LLM generates grounded/context-aware answer


        ------------------------------------------------------------


        Q) What is the role of each component?

        Answer:

        Embedding Model:
        Converts text into vectors representing semantic meaning.

        PGVector:
        Stores embeddings and performs similarity search.

        Vector Search:
        Finds relevant knowledge from documents.

        LLM:
        Generates natural language response.

        RAG Pipeline:
        Connects retrieval system with LLM.


        ------------------------------------------------------------


        Q) What is the biggest advantage of RAG?

        Answer:
        RAG allows the LLM to answer using:
        - private data
        - latest documents
        - company knowledge
        - dynamic information

        WITHOUT retraining or fine-tuning the model.

        This is why most modern AI applications use RAG architecture.
    */
    public String askAI(String prompt) {

        var docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(prompt)
                .similarityThreshold(0.2)
                .filterExpression("file_name == 'faq.pdf'")
                .topK(4)
                .build());

        var context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String template = """
                You are an AI assistant called Cody
                
                Rules:
                - Use ONLY the information provided in the context
                - You MAY rephrase, summarize, and explain in natural language
                - Do NOT introduce new concepts or facts
                - If multiple context sections are relevant, combine them into a single explanation.
                - If the answer is not present, say "I don't know"
                
                Context:
                {context}
                
                Answer in a friendly, conversational tone.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        String stuffedPrompt = promptTemplate.render(Map.of("context", context));

        return chatClient.prompt()
                .system(stuffedPrompt)
                .user(prompt)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
    }
}
