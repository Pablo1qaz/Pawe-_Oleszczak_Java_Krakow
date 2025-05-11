
## Requirements

- **Java:** Version 17 or 21 (LTS versions recommended)
- **Build Tool:** Maven or Gradle (for dependency management and packaging)
- **Dependencies:** [Jackson Databind](https://github.com/FasterXML/jackson-databind) is used for JSON serialization/deserialization

## Building the Application

### Using Maven

1. **Install Maven:** Ensure Maven is installed on your system.
2. **Build:** From the project root, run:
   ```bash
   
mvn clean package
java -jar target/Task-1.0-SNAPSHOT path/to/orders.json path/to/paymentmethods.json
	
