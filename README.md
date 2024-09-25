# DB Proxy API

---
## Git - Branches
* master
  * Downgraded to Java 8 - Branch name: downgrade
    * Multiple vendors - Branch name: many-vendors
      * Generic SQL - Branch name: generic-sql-dto


# Supported Databases and Configuration Guide

This application is prepared to handle connections to multiple databases dynamically at runtime, supporting the following database types:

## Supported Databases

- **MySQL (DEV, QA, PROD)**: Handled via MySQL JDBC driver.
- **SingleStore (DEV, QA, PROD)**: Handled via SingleStore JDBC driver.
- **Amazon Redshift (DEV, QA, PROD)**: Handled via Amazon Redshift JDBC driver.
- **Other Databases**: Optionally supported for future extensions using the `OTHER_DEV`, `OTHER_QA`, and `OTHER_PROD` configurations.

### Enum Definition for DB Type

The `DBType` enum defines the supported database environments. It is used to determine the correct data source to be used for query execution.

```java
public enum DBType {
    MYSQL_DEV,
    MYSQL_QA,
    MYSQL_PROD,
    SINGLESTORE_DEV,
    SINGLESTORE_QA,
    SINGLESTORE_PROD,
    REDSHIFT_DEV,
    REDSHIFT_QA,
    REDSHIFT_PROD,
    OTHER_DEV,
    OTHER_QA,
    OTHER_PROD;

    public static DBType fromString(String dbType) {
        try {
            return DBType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }
    }
}
```

## Postman Header Requirement

To select the correct database connection at runtime, you must pass a custom header `DB-Type` in your HTTP requests, specifying the database environment.

### Example Usage:

In Postman, add the following header to your requests:

- **Key**: `DB-Type`
- **Value**: `MYSQL_DEV`, `SINGLESTORE_QA`, `REDSHIFT_DEV`, etc., depending on the environment you want to connect to.

### Example Request

```plaintext
POST /api/execute-query
Headers:
  - Content-Type: application/json
  - DB-Type: MYSQL_DEV
Body:
  {
    "operation": "SELECT",
    "tableName": "employees",
    "columns": ["id", "name"],
    "whereClause": [
      {
        "column": "department",
        "operator": "=",
        "value": "IT"
      }
    ]
  }
```

## Data Source Configuration

The application dynamically loads the database configurations based on the defined keys in the `application.properties` file. Each key represents a specific environment, and the corresponding configurations (URL, username, password, and driver class) must be provided.

### Configuration Structure

In the `application.properties` file, the following structure is used:

1. **Define the data source keys**:
   ```properties
   datasource.keys=mysql_dev,singlestore_dev,singlestore_qa,redshift_dev
   ```

2. **Provide a configuration for each key**:
   Each key in `datasource.keys` must have the following properties:
    - **url**: The JDBC connection string.
    - **username**: The database username.
    - **password**: The database password.
    - **driver-class-name**: The JDBC driver class name.

### Example Configuration for Multiple Data Sources

```properties
####### MySQL Data Source Configuration
datasource.mysql_dev.url=jdbc:mysql://localhost:3306/my-database
datasource.mysql_dev.username=root
datasource.mysql_dev.password=
datasource.mysql_dev.driver-class-name=com.mysql.cj.jdbc.Driver

####### SingleStore Data Source Configuration for local_stations_dev
datasource.singlestore_dev.url=jdbc:singlestore://host:3306/db_name
datasource.singlestore_dev.username=user
datasource.singlestore_dev.password=password
datasource.singlestore_dev.driver-class-name=com.singlestore.jdbc.Driver

####### SingleStore Data Source Configuration for local_stations_qa
datasource.singlestore_qa.url=jdbc:singlestore://host:3306/db_name
datasource.singlestore_qa.username=user
datasource.singlestore_qa.password=password
datasource.singlestore_qa.driver-class-name=com.singlestore.jdbc.Driver

####### Amazon Redshift Data Source Configuration
datasource.redshift_dev.url=jdbc:redshift://dbc:singlestore://host:5439/db_name
datasource.redshift_dev.username=user
datasource.redshift_dev.password=password
datasource.redshift_dev.driver-class-name=com.amazon.redshift.jdbc42.Driver
```

## Configuration Rule

For each key defined in `datasource.keys`, you **must** provide the following configurations in `application.properties`:

- `datasource.<key>.url`
- `datasource.<key>.username`
- `datasource.<key>.password`
- `datasource.<key>.driver-class-name`

If you define 5 keys in `datasource.keys`, you will need to provide these 4 properties for each of the 5 keys.

### Example:

```properties
datasource.keys=mysql_dev,mysql_qa,singlestore_dev,singlestore_prod,redshift_dev

# For mysql_dev
datasource.mysql_dev.url=jdbc:mysql://localhost:3306/dev_database
datasource.mysql_dev.username=root
datasource.mysql_dev.password=root_password
datasource.mysql_dev.driver-class-name=com.mysql.cj.jdbc.Driver

# For singlestore_prod
datasource.singlestore_prod.url=jdbc:singlestore://some-production-url
datasource.singlestore_prod.username=prod_user
datasource.singlestore_prod.password=prod_password
datasource.singlestore_prod.driver-class-name=com.singlestore.jdbc.Driver

# For redshift_dev
datasource.redshift_dev.url=jdbc:redshift://some-redshift-url
datasource.redshift_dev.username=dev_user
datasource.redshift_dev.password=dev_password
datasource.redshift_dev.driver-class-name=com.amazon.redshift.jdbc42.Driver
```


# SQLRequestDto Usage Examples

## Overview

The `SQLRequestDto` DTO is used to dynamically build SQL queries for different operations like `SELECT`, `INSERT`, `UPDATE`, `DELETE`, and `UPSERT`. Below are examples of how to use the DTO with corresponding SQL query outputs.

## 1. SELECT Operation

### JSON Request:

```json
{
  "operation": "SELECT",
  "tableName": "MyTable",
  "columns": ["first_name", "middle_name", "last_name"],
  "whereClause": [
    {
      "column": "id",
      "operator": "=",
      "value": "1"
    }
  ]
}
```

### Generated Query:

```sql
SELECT id, first_name, middle_name, last_name FROM MyTable WHERE id = '1';
```

---

## 2. INSERT Operation

### JSON Request:

```json
{
  "operation": "INSERT",
  "tableName": "employees",
  "columns": ["id", "first_name", "last_name"],
  "values": [
    ["1", "John", "Doe"],
    ["2", "Jane", "Smith"]
  ]
}
```

### Generated Query:

```sql
INSERT INTO employees (id, first_name, last_name) VALUES ('1', 'John', 'Doe'), ('2', 'Jane', 'Smith');
```

---

## 3. UPDATE Operation

### JSON Request:

```json
{
  "operation": "UPDATE",
  "tableName": "employees",
  "columns": ["name", "email"],
  "values": [["John Doe", "john.doe@example.com"]],
  "whereClause": [
    {
      "column": "id",
      "operator": "=",
      "value": "1"
    }
  ]
}
```

### Generated Query:

```sql
UPDATE employees SET name = 'John Doe', email = 'john.doe@example.com' WHERE id = '1';
```

---

## 4. DELETE Operation

### JSON Request:

```json
{
  "operation": "DELETE",
  "tableName": "employees",
  "whereClause": [
    {
      "column": "id",
      "operator": "=",
      "value": "1"
    }
  ]
}
```

### Generated Query:

```sql
DELETE FROM employees WHERE id = '1';
```

---

## 5. UPSERT Operation (MySQL Syntax)

### JSON Request:

```json
{
  "operation": "UPSERT",
  "tableName": "employees",
  "columns": ["id", "name", "email"],
  "values": [["1", "John Doe", "john.doe@example.com"]],
  "onDuplicateUpdateColumns": ["name", "email"],
  "onDuplicateUpdateValues": ["John Doe", "john.doe@example.com"]
}
```

### Generated Query:

```sql
INSERT INTO employees (id, name, email) VALUES ('1', 'John Doe', 'john.doe@example.com') ON DUPLICATE KEY UPDATE name = 'John Doe', email = 'john.doe@example.com';
```

---

## Conclusion

By using the `SQLRequestDto`, SQL queries can be dynamically generated and executed for a variety of operations without needing to hardcode query strings. Each operation has corresponding fields and flexibility to handle complex scenarios like `UPSERT`.


By following this structure, the application will dynamically load and use the correct data source configuration at runtime based on the value of the `DB-Type` header in your requests.

## Directory Structures
* config
    * domain
        * All DAOs
    * AuditConfig
    * AuditorAwareImpl
    * DataConfig
    * DomainConfig
    * SecurityConfig
    * SwaggerConfig
    * WebMvcConfig
* core
    * Packages
        * dao
        * dto
        * mapper
        * repository
        * service
        * web
    * ApiResponse
    * Domain
    * InvalidFieldException
    * NotFoundException
    * ValidationError
* Interceptor
* Model
    * Audit
        * DateAudit
    * Entity Tables
* Security
---
## Dependencies
* Spring Web
* Thymeleaf
* Spring Boot DevTools
* Spring Security
* H2 Database
* MyBatis Framework
* Spring Data JPA
* Others:
    * MapStruct
        * Observe that, if you are using Lombok, annotationProcessorPaths must be properly configured on pom.xml
    * Lombok
    * Swagger - springfox (springfox-boot-starter)
    * commons-lang3
    * Guava
    * MySQL
    * javax.validation - validation-api
    * org.springframework.boot - spring-boot-starter-validation

## Build and Commands
### MySQL
```agsl
brew services start mysql
mysql -u root -p (or mysql -u root if you don't have password)
USE my-data-base-name;
SHOW TABLES;
SELECT * FROM my-table-name;
brew services stop mysql
```
### Build
```bash
./gradlew clean build
```

## To Execute
### Postman
#### GET
http://localhost:8080/api/generic-query/execute-query?query=SELECT * FROM TABLE_NAME;

#### POST
http://localhost:8080/api/generic-query/execute-query
```json
{
    "query": "SELECT * FROM TABLE_NAME;"
}
```
### Command Line
```bash
java -jar build/libs/mysql_proxy-0.0.1-SNAPSHOT.jar
```
### Profile
```bash
java -jar build/libs/mysql_proxy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
### External Configuration
```bash
java -jar build/libs/mysql_proxy-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/application.properties,/path/to/external/application.properties
```
### Export Profile
```bash 
echo "export SPRING_PROFILES_ACTIVE=prod"
```
### Swagger
http://localhost:8080/swagger-ui.html


# Deploying a Spring Boot Application on a Linux Server

To deploy your Spring Boot application as a JAR file on a Linux server, you need to follow these steps:

### 1. **Build the JAR File**

First, you need to generate the JAR file for your Spring Boot application using Gradle.

#### Steps:
- Open your terminal or command line in the project root directory (where the `build.gradle` file is located).
- Run the following command to build the project and generate the JAR file:

```bash
./gradlew clean build
```

This will generate the JAR file in the `build/libs/` directory. The file will be named something like:

```
your-application-name-0.0.1-SNAPSHOT.jar
```

### 2. **Prepare the Linux Server**

Before deploying your application on a Linux server, make sure that:
- You have access to the server (via SSH).
- The server has **Java** installed (preferably Java 17 or higher, depending on your project configuration).

#### Check Java Version:
You can check the installed Java version on the server by running:

```bash
java -version
```

If Java is not installed, you can install it:

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

(Replace `openjdk-17-jdk` with the version required by your application.)

### 3. **Transfer the JAR File to the Server**

You need to transfer the JAR file from your local machine to the Linux server. You can do this using **`scp`** (secure copy) or any other file transfer tool (like `rsync` or `FTP`).

#### Using `scp`:

```bash
scp /path/to/your-application-name-0.0.1-SNAPSHOT.jar username@server-ip:/path/to/deployment-directory/
```

- Replace `/path/to/your-application-name-0.0.1-SNAPSHOT.jar` with the actual path to your JAR file.
- Replace `username@server-ip` with your server username and IP address.
- Replace `/path/to/deployment-directory/` with the directory on the server where you want to deploy your application.

### 4. **Run the Spring Boot Application on the Server**

Once the JAR file is on the server, you can run the Spring Boot application using the `java -jar` command.

#### Steps:
1. **SSH into the server**:

   ```bash
   ssh username@server-ip
   ```

2. **Navigate to the deployment directory**:

   ```bash
   cd /path/to/deployment-directory/
   ```

3. **Run the JAR file**:

   ```bash
   java -jar your-application-name-0.0.1-SNAPSHOT.jar
   ```

   This will start the Spring Boot application, and it will be available on the default port `8080` (or the port specified in your `application.properties`).

#### Optional: Run the Application in the Background
If you want to run the application in the background, you can use `nohup` to prevent it from being stopped when you close the SSH session:

```bash
nohup java -jar your-application-name-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

- **`nohup`**: Runs the process in the background.
- **`> app.log 2>&1 &`**: Redirects the output to a log file (`app.log`) and runs the process in the background (`&`).

You can check if the application is running by viewing the log:

```bash
tail -f app.log
```

### 5. **Configure a Reverse Proxy with NGINX (Optional)**

If you want your application to be accessible via a domain name or over port `80` (HTTP), you can set up a **reverse proxy** using **Nginx** or **Apache**.

#### Example using Nginx:
1. **Install Nginx**:

   ```bash
   sudo apt update
   sudo apt install nginx
   ```

2. **Configure Nginx** to proxy requests to your Spring Boot application:

   ```bash
   sudo nano /etc/nginx/sites-available/your-domain.conf
   ```

   Add the following content:

   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

3. **Enable the configuration**:

   ```bash
   sudo ln -s /etc/nginx/sites-available/your-domain.conf /etc/nginx/sites-enabled/
   ```

4. **Restart Nginx**:

   ```bash
   sudo systemctl restart nginx
   ```

Your Spring Boot application will now be accessible via `http://your-domain.com`.

### 6. **Monitoring and Managing the Application**

You may want to use a process management tool like **`systemd`** or **`pm2`** (for Node.js) to manage the application lifecycle. For `systemd`:

1. **Create a systemd service file**:

   ```bash
   sudo nano /etc/systemd/system/myapp.service
   ```

2. **Add the following content**:

   ```ini
   [Unit]
   Description=My Spring Boot Application
   After=syslog.target
   After=network.target

   [Service]
   User=your-username
   ExecStart=/usr/bin/java -jar /path/to/your-application-name-0.0.1-SNAPSHOT.jar
   SuccessExitStatus=143
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

3. **Start and enable the service**:

   ```bash
   sudo systemctl start myapp
   sudo systemctl enable myapp
   ```

This will ensure that your application starts automatically when the server restarts.

### Summary:

1. **Build** the Spring Boot JAR file using Gradle (`./gradlew build`).
2. **Transfer** the JAR file to the Linux server using `scp` or another method.
3. **Run** the application with `java -jar` or use `nohup` to run it in the background.
4. Optionally, set up **Nginx** or another reverse proxy for easy access.
5. Optionally, use **`systemd`** to manage the application as a service.
