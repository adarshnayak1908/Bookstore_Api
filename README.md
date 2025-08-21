# 📚 BookStore_Automation

BookStore_Automation is a **Cucumber + RestAssured + Extent Reports** based automation framework built for **Bookstore API Testing**.  
It provides token-based authentication, automatic token refresh, advanced HTML/PDF reporting, and seamless CI/CD integration with Jenkins.
This framework is designed to be reusable, extensible, and easy to integrate into existing projects.
---

## 🚀 Features

* BDD-style test scenarios written in Gherkin.
* **RestAssured** for making API calls.
* **Token-based authentication** with auto-refresh on expiry.
* **ExtentReports** integration for detailed HTML/PDF reports.
* Reusable **utilities** for configuration, request building, and token management.
* Jenkins-compatible with pipeline (`Jenkinsfile`) for CI/CD execution.

---
## 📋 Prerequisites

Before running this automation framework, make sure you have the following installed and configured:

1. **Java Development Kit (JDK 17 or higher)**
   - Verify installation:
     ```bash
     java -version
     ```
   - Ensure the `JAVA_HOME` environment variable is set.

2. **Maven (3.8+)**
   - Verify installation:
     ```bash
     mvn -version
     ```

3. **Git**
   - To clone the repository and manage version control.
   - Verify installation:
     ```bash
     git --version
     ```

4. **IDE (Recommended: IntelliJ IDEA / Eclipse / VS Code)**
   - IntelliJ IDEA is recommended since it has strong Maven and Cucumber integration.

5. **Cucumber Plugin for IDE (Optional but recommended)**
   - Provides Gherkin syntax highlighting and step definition navigation.

6. **Local / Remote Backend API**
   - This framework tests a BookStore API (running at `http://127.0.0.1:8000/`).
   - Ensure the API server is up before running tests, otherwise login/token steps will fail with `404` or `401`.

7. **Jenkins (Optional, for CI/CD)**
   - Installed locally or on a server/cloud instance.
   - Jenkins should have the following plugins:
      - *Pipeline*
      - *Maven Integration*
      - *Git*
      - *JUnit*
      - *HTML Publisher*

8. **Node.js & npm (Optional)**
   - Only required if you plan to extend reporting or integrate with dashboards.

---

## 🚀 Getting Started

### Clone the Repository
```bash
git clone https://github.com/your-username/BookStore_Automation.git
cd BookStore_Automation
````

---
## 📂 Project Structure

```
BookStore_Automation
├── src
│   ├── main
│   │   └── java
│   │       └── utils
│   │           ├── ConfigReader.java
│   │           ├── PropertyFileReader.java
│   │           ├── TokenManager.java
│   │           ├── AuthRetryFilter.java
│   └── test
│       ├── java
│       │   ├── stepdefs
│       │   │   ├── BookstoreSteps.java
│       │   │   └── Hooks.java
│       └── resources
│           ├── features
│           │   ├── books.feature
│           │   ├── login.feature
│           │   └── signup.feature
│           └── extent.properties
├── pom.xml
└── Jenkinsfile
```

---

## 🛠 Utilities

### `ConfigReader.java`

* Loads environment-specific configuration from property files.
* Central place to manage base URL, credentials, timeouts, etc.

### `PropertyFileReader.java`

* Simplifies reading `config.properties` and environment variables.

### `TokenManager.java`

* Handles login and token generation.
* Stores and reuses tokens until expiry.
* Auto-refreshes token if expired.
* Works seamlessly with `AuthRetryFilter` to retry failed requests.

### `AuthRetryFilter.java`

* A RestAssured filter that intercepts **401 Unauthorized** responses.
* Automatically calls `TokenManager.forceRefresh()` to get a new token.
* Retries the failed request once with a fresh token.

---

## 🔐 TokenManager – Deep Dive

The `TokenManager` class is the backbone of authentication in this framework.
It ensures every API call has a valid **Bearer token** and handles expiry seamlessly.

### How it works

* **Cached token**: Stores the token string in memory along with its expiry time.
* **Expiry calculation**:

   * If the login API provides a TTL/expiry → uses that.
   * If not → defaults to a safe TTL (e.g., 1 hour) with a small buffer.
* **Thread-safety**: Uses synchronization to prevent multiple threads from refreshing at once.
* **Pre-warming**: `Hooks.beforeAll()` can call `TokenManager.prewarm()` so the first scenario starts with a token.

### Key methods

* `getToken()` → returns a valid token if cached and not expired, else triggers login.
* `forceRefresh()` → ignores cache and always performs login (used after a `401`).
* `prewarm()` → optional early login at suite startup.

### Token lifecycle

1. First request → TokenManager checks cache.

   * If empty or expired → calls login API → caches token & expiry.
2. On subsequent requests → returns cached token until expiry.
3. If backend returns **401 Unauthorized**:

   * `AuthRetryFilter` calls `forceRefresh()`.
   * Replays the **same request once** with new token.
   * Prevents test failure due to mid-run expiry.

### Flow diagram

```mermaid
flowchart LR
    A[Request needs token] --> B{Token cached & valid?}
    B -- Yes --> C[Return cached token]
    B -- No --> D[Call login API]
    D --> E[Cache token + expiry]
    E --> C
    C --> F[Request executed]
    F --> G{Response 401?}
    G -- No --> H[Pass response]
    G -- Yes --> I[forceRefresh()]
    I --> D
    I --> J[Replay request with new token]
    J --> H
```

---

## 📊 ExtentReports – Test Reporting

The framework integrates with **ExtentReports** (via `extentreports-cucumber7-adapter`) for rich, interactive reports.

### What it shows

* **Scenario & step status** (pass/fail/skip)
* **Step logs** – request payloads, response codes, response bodies
* **Timestamps & duration** per step
* **Categorization** by feature, tag, or scenario
* **Visuals** (charts of pass/fail trends)

### Report formats

* **HTML Spark Report** → modern interactive UI with collapsible steps
* **PDF Report** (if enabled) → portable version for sharing
* **Cucumber JSON/HTML** → standard reports for CI/CD

### Location

By default, reports are generated in:

```
target/extent-reports/ExtentSpark.html
target/extent-reports/ExtentPdf.pdf
```

*(You can adjust in `extent.properties` and `extent-config.xml`)*

### Example (HTML Spark)

* Home page: summary stats, scenario counts, pass/fail pie chart
* Each feature → expandable scenario list
* Each step → log details + HTTP request/response nicely formatted
* Failures → stack traces, error logs highlighted

---

## ⚙️ Execution Flow

1. **JVM Startup** → Cucumber runner loads feature files.
2. **Hooks.beforeAll()** → initializes ExtentReports, prewarms token.
3. **Scenario Execution** → step definitions from `BookstoreSteps` run.
4. **TokenManager** provides tokens for API calls.
5. **AuthRetryFilter** retries requests automatically on `401`.
6. **ExtentReports Adapter** logs all steps, requests, and responses.
7. **Reports Generated** in `target/extent-reports` after test run.

---

## ▶️ Running Tests

### Using Maven

```bash
mvn clean test
```

### Using Feature File Directly (IntelliJ IDEA)

* Right-click a `.feature` file → `Run`.
* TokenManager + Hooks ensure login is triggered automatically.

---
## 📦 POJO Classes – How They’re Used

### `LoginRequest`

Represents the body sent to the login endpoint.

```java
public class LoginRequest {
  private int id;        // or String, depending on API
  private String email;
  private String password;
  // getters/setters (or Lombok @Data)
}
```

**Usage:**

```java
LoginRequest req = new LoginRequest();
req.setId(Integer.parseInt(ConfigReader.get("id")));
req.setEmail(ConfigReader.get("email"));
req.setPassword(ConfigReader.get("password"));

Response res = given()
  .baseUri(ConfigReader.base())
  .contentType(ContentType.JSON)
  .body(req)                    // POJO → JSON (Jackson)
  .post(ConfigReader.loginPath());
```

### `LoginResponse`

Represents the response from the login endpoint.

```java
public class LoginResponse {
  private String token;   // field names should match JSON keys
  private Long   ttl;     // optional: seconds until expiry
  private Long   exp;     // optional: epoch seconds
  // getters/setters (or Lombok @Data)
}
```

**Usage:**

```java
LoginResponse lr = res.then().statusCode(200)
  .extract().as(LoginResponse.class);  // JSON → POJO (Jackson)
String token = lr.getToken();
```

> 📌 If the API uses different JSON field names, annotate with `@JsonProperty("json_name")` or align your POJO names.

### Why POJOs?

* Type‑safe request/response mapping
* Cleaner code than building raw Maps/Strings
* Works seamlessly with RestAssured’s object mapper (Jackson)

### Integration with `TokenManager`

* `TokenManager` builds a `LoginRequest` from `config.properties`/`-D` props.
* Sends it to the login endpoint with `ContentType.JSON`.
* Parses the `LoginResponse` using `.as(LoginResponse.class)`.
* Caches `token` and computes `expiryEpochSeconds` from `ttl/exp` if available; otherwise uses a default TTL + small skew.
* On `401` (caught by `AuthRetryFilter`), calls `forceRefresh()` to repeat the above and updates the cache.

---


## ☁️ Jenkins Integration

* A `Jenkinsfile` is provided at the root of the repo.
* Clone repo in Jenkins → configure **Pipeline** → set `Script Path = Jenkinsfile`.
* Jenkins will:

   1. Checkout repo
   2. Run `mvn clean test`
   3. Archive HTML/PDF ExtentReports for visualization.

---

---

## 🧷 Jenkins – CI/CD Details

### Prerequisites

* **Plugins:** Pipeline, Git, **HTML Publisher**, Credentials Binding (optional), Timestamper (optional).
* **Global Tool Configuration:**

   * **JDK** named `jdk17` (or update the Jenkinsfile if you use a different name).
   * **Maven** named `Maven3`.
* **Agents:** Linux recommended (uses `sh`). On Windows agents replace `sh 'mvn ...'` with `bat 'mvn ...'`.

### Job Types

* **Pipeline job (from SCM)**

   * Definition: *Pipeline script from SCM*
   * SCM: your Git repo URL
   * Branch: `main`
   * **Script Path:** `Jenkinsfile` (at repo root)
* **Multibranch Pipeline**

   * Indexes all branches/PRs with a `Jenkinsfile`.
   * Enable GitHub webhooks for auto-builds on push/PR.

### Credentials & Private Repos

* Add Git credentials in *Manage Jenkins → Credentials*.
* Select them in the job’s SCM section.

### Environment Overrides (Per-Job)

You can override API settings in Jenkins without editing files:

```
MAVEN_OPTS: -Dapi.base=http://127.0.0.1:8000 -Dapi.login.path=/login -Dapi.user=user@example.com -Dapi.pass=secret -Dapi.id=1
```

Or parametrize the pipeline:

```groovy
parameters {
  string(name: 'API_BASE', defaultValue: 'http://127.0.0.1:8000')
  string(name: 'API_LOGIN', defaultValue: '/login')
  string(name: 'API_USER',  defaultValue: 'user@example.com')
  password(name: 'API_PASS', defaultValue: 'secret')
  string(name: 'API_ID',    defaultValue: '1')
}

stages {
  stage('Build & Test') {
    steps {
      sh "mvn clean test -Dapi.base=${params.API_BASE} -Dapi.login.path=${params.API_LOGIN} -Dapi.user=${params.API_USER} -Dapi.pass=${params.API_PASS} -Dapi.id=${params.API_ID}"
    }
  }
}
```

### Reports in Jenkins

* **HTML Publisher** serves `target/extent-reports/ExtentSpark.html` as *Extent HTML Report* in the sidebar.
* **Artifacts** are archived: `target/extent-reports/**` so you can download HTML/PDF later.
* **JUnit** results: `target/surefire-reports/*.xml` for trend charts and test counts.

### Good-to-have Hardening

* **Timeouts:** `options { timeout(time: 30, unit: 'MINUTES') }`
* **Retry flaky stages:** `retry(2) { ... }`
* **Workspace cleanup:** add a `post { always { cleanWs() } }`
* **Parallelization:** keep `TokenManager` synchronized; avoid over-parallelizing against rate‑limited APIs.

---

