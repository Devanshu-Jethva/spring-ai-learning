# Learn Spring AI

Spring Boot application using Spring AI with Ollama and PGVector for local AI chat, embeddings, semantic search, and
RAG-based applications.

---

# AI Models Used

## Chat Model

```bash
qwen2.5-coder:3b
```

### Purpose

- AI Chat
- Code Generation
- Code Explanation

---

## Embedding Model

```bash
nomic-embed-text
```

### Purpose

- Text Embeddings
- Semantic Search
- Vector Search
- RAG Applications

---

# Ollama Setup

## Install Ollama

```bash
https://ollama.com
```

---

# Ollama Commands

## Start Ollama

### Windows

```bash
ollama serve
```

---

## Stop Ollama

### Windows

```bash
taskkill /F /IM ollama.exe
```

### Linux / Mac

```bash
pkill ollama
```

---

## Pull Required Models

### Pull Chat Model

```bash
ollama pull qwen2.5-coder:3b
```

### Pull Embedding Model

```bash
ollama pull nomic-embed-text
```

---

## List Downloaded Models

```bash
ollama list
```

---

## Run Models Manually

### Run Chat Model

```bash
ollama run qwen2.5-coder:3b
```

### Run Embedding Model

```bash
ollama run nomic-embed-text
```

---

## Remove Model

```bash
ollama rm qwen2.5-coder:3b
```

---

## Check Ollama API

```bash
http://localhost:11434
http://localhost:11434/api/tags
```

---

# PGVector Setup

## Run PostgreSQL + PGVector Using Docker

```bash
docker run --name pgvector-container ^
-e POSTGRES_PASSWORD=root ^
-e POSTGRES_DB=pgvector-test ^
-e TZ=Asia/Kolkata ^
-p 5431:5432 ^
-v pgdata:/var/lib/postgresql ^
-d pgvector/pgvector:pg18
```

# Run Application

## Step 1 — Start Ollama

```bash
ollama serve
```

---

## Step 2 — Pull Required Models

```bash
ollama pull qwen2.5-coder:3b
ollama pull nomic-embed-text
```

---

## Step 3 — Start PGVector Database

```bash
docker run -d \
  --name pgvector-db \
  -e POSTGRES_DB=pgvector-test \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -p 5431:5432 \
  pgvector/pgvector:pg17
```

---