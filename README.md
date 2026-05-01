📦 Distributed File System (DFS) – Spring Boot

An AI-inspired Distributed File Storage System that supports:

📤 Chunked file uploads
🔁 Data replication across nodes
📥 File reconstruction & download
🔐 Secure access with JWT
🔗 File sharing between users

📌 Project Overview

This system simulates a distributed storage architecture where:

Files are split into chunks
Chunks are stored across multiple nodes
Each chunk is replicated for fault tolerance
Files are reconstructed during download

👉 Core file handling logic:


🚀 Key Features

📤 Chunked File Upload

Files split into smaller chunks
Stored across multiple storage nodes
Improves scalability & performance

🔁 Replication System

Each chunk stored in multiple nodes
Ensures fault tolerance
Prevents data loss

📥 File Reconstruction

Combines chunks during download
Uses first available replica

🔐 JWT Authentication

Secure login system
Stateless authentication
Token-based authorization

🔗 File Sharing

Share files with other users
Access control enforced

📊 File Tracking

Track:
Uploaded files
Shared files
Download history

🧾 Task Management

Users can create personal tasks
Linked to authenticated users

🏗️ System Architecture

Client (Frontend)
        ↓
Spring Boot Backend
        ↓
JWT Authentication Layer
        ↓
File Service (Metadata)
        ↓
Chunk Service (Split & Replicate)
        ↓
Storage Nodes (Local Directories)

⚙️ Tech Stack

Backend: Spring Boot
Security: Spring Security + JWT
Database: MySQL / H2 (JPA)
ORM: Hibernate (JPA)
File Handling: Java IO
API Testing: Postman

📁 Project Structure

src/main/java/com/yespackage/dfs/
│
├── config/
│   └── CorsConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── FileController.java
│   └── TaskController.java
│
├── dto/
│   ├── AuthRequest.java
│   └── AuthResponse.java
│
├── entity/
│   ├── User.java
│   ├── FileEntity.java
│   ├── FileChunk.java
│   ├── FileShare.java
│   ├── FileDownload.java
│   └── Task.java
│
├── repository/
│   ├── UserRepository.java
│   ├── FileRepository.java
│   ├── FileChunkRepository.java
│   ├── FileShareRepository.java
│   ├── FileDownloadRepository.java
│   └── TaskRepository.java
│
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConfig.java
│   └── CustomUserDetailsService.java
│
├── service/
│   ├── FileService.java
│   └── FileChunkService.java
│
└── DistributedFilesystem1Application.java

📡 API Endpoints

🔐 Authentication

POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout

📤 File Operations

POST   /api/files/upload-chunked
GET    /api/files/download/{fileId}
DELETE /api/files/{fileId}
GET    /api/files/list

🔗 File Sharing

POST /api/files/share/{fileId}?recipientUsernameOrEmail=
GET  /api/files/shared-by-me

🧾 Task Management

POST /api/tasks/create

🔐 Authentication Flow

User logs in
JWT token generated
Sent in:
Authorization: Bearer <token>
Verified in JwtAuthenticationFilter

🧠 Core Concepts

🔁 Chunking Algorithm

File split into chunks (e.g., 1MB each)
Each chunk assigned index

🔁 Replication Strategy

Each chunk stored in 2 nodes
Nodes selected randomly

📥 Reconstruction Logic

Group chunks by index
Pick first available replica
Merge into original file

🔐 Security

JWT validation
Stateless sessions
Protected endpoints

🗄️ Database Schema

users

id
username
email
password
role

files

id
filename
checksum
uploadedBy
uploadDate

file_chunks

fileId
chunkIndex
nodeName
chunkPath

file_shares

fileId
sharedBy
sharedWith

file_downloads

fileId
userId
timestamp

🚀 Setup Instructions

1️⃣ Clone Repository
git clone https://github.com/your-username/distributed-filesystem.git
cd distributed-filesystem

2️⃣ Configure Database

Update application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/dfs
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update

3️⃣ Run Application
mvn spring-boot:run

4️⃣ Access API
http://localhost:8080

📂 Storage Nodes

Simulated local directories:

C:/dfs_nodes/node1
C:/dfs_nodes/node2
C:/dfs_nodes/node3

📊 Example Workflow

Upload file
File split into chunks
Chunks replicated across nodes
Metadata stored in DB
Download reconstructs file

📈 Future Enhancements

☁️ Cloud storage integration (AWS S3)
⚡ Load balancing across nodes
🔍 Search files
📊 File analytics dashboard
🔔 Real-time notifications
👨‍💻 Author

Developed as part of a Distributed Systems + Backend Engineering Project

📜 License

MIT License

⭐ Support

If you like this project:

⭐ Star the repo
🍴 Fork it
🚀 Build something bigger
