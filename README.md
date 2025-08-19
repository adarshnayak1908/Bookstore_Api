# 📚 BookStore_Automation

BookStore_Automation is a **Cucumber + RestAssured + Extent Reports** based automation framework built for **Bookstore API Testing**.  
It provides token-based authentication, automatic token refresh, advanced HTML/PDF reporting, and seamless CI/CD integration with Jenkins.

---

## ✨ Key Features
- ✅ **BDD Style** → Uses **Cucumber + Gherkin** for readable scenarios
- ✅ **API Automation** → Powered by **RestAssured**
- ✅ **Token Management** → Centralized `TokenManager` with **auto-refresh**
- ✅ **Reusable Filters** → Retry on expired tokens
- ✅ **Extent Reports** → Interactive **HTML & PDF reports** with request/response logs
- ✅ **CI/CD Ready** → Jenkins pipeline integration (local/cloud)
- ✅ **Custom Properties Loader** → Externalized `config.properties` for easy environment config

---

## ⚡ Prerequisites

Java → JDK 17+
RestAssured → 5.0+
Cucumber → 7.0+
Extent Reports → 5.0+
JUnit → 5.0+
Maven → 3.8+
Local/Remote Bookstore API running (base URL in config.properties)

## Running the Tests
1. Run `mvn clean test` to execute all tests
2. Run a specific feature:
   ```bash
   mvn test -Dcucumber.options="src/test/resources/features/books.feature"
   ```
      

## 📂 Project Structure
```bash
BookStore_Automation/
│── src/
│   ├── test/
│   │   ├── java/
│   │   │   ├── stepdefs/         # Step Definitions (Books, Login, Signup)
│   │   │   ├── utils/            # TokenManager, Filters, Config Reader
│   │   │   ├── pojo/             # Request/Response POJOs
│   │   └── resources/
│   │       ├── features/         # Cucumber Feature Files
│   │       │   ├── books.feature
│   │       │   ├── login.feature
│   │       │   └── signup.feature
│   │       ├── config.properties # Environment configs
│   │       ├── extent.properties # Extent report properties
│   │       └── extent-config.xml # Extent report config (theme, charts, etc.)
│── target/                       # Build output & Reports
│── pom.xml                       # Maven Dependencies
│── Jenkinsfile                   # CI/CD Pipeline config
│── README.md                     # Documentation


