# Reporting Service

A comprehensive analytics and reporting microservice for the DentistDSS (Decision Support System) that provides advanced data analytics, multi-format report generation, and automated delivery capabilities.

## Features

### Core Functionality
- **Advanced Data Analytics**: Complex SQL queries with read replica optimization for performance
- **Multi-Format Report Generation**: PDF, Excel, CSV with professional formatting and charts
- **Automated Scheduling**: Configurable report schedules with email delivery
- **Real-time & Historical Analysis**: Support for both live data queries and pre-computed metrics
- **Performance Optimization**: Async processing, caching, connection pooling, and materialized views

### Report Types
- **Patient Analytics**: No-show rates, demographics, treatment outcomes
- **Appointment Metrics**: Utilization rates, scheduling efficiency, capacity analysis
- **Revenue Reports**: Financial analytics, billing summaries, payment tracking
- **Clinical Analytics**: Treatment completion rates, procedure statistics
- **AI Usage Statistics**: Token consumption, effectiveness metrics, cost analysis
- **Operational Reports**: Staff productivity, equipment utilization, clinic performance

### Technical Architecture
- **Dual Database Strategy**: PostgreSQL for OLTP with read replicas for analytics
- **MongoDB**: Report templates, execution metadata, and configuration storage
- **Redis Caching**: Multi-level caching for performance optimization
- **Async Processing**: Background job queues for long-running operations
- **Email Integration**: Automated report delivery with attachments

## Architecture

### Technology Stack
- **Spring Boot 3.5.0**: Core framework with WebMVC
- **Spring Data JPA**: PostgreSQL integration with Hibernate
- **Spring Data MongoDB**: Template and metadata storage
- **Spring Cache**: Multi-level caching with Caffeine and Redis
- **Spring Mail**: Email delivery integration
- **Quartz Scheduler**: Advanced job scheduling capabilities
- **iText PDF**: Professional PDF generation with charts
- **Apache POI**: Excel report generation
- **OpenCSV**: CSV export capabilities
- **JFreeChart**: Data visualization and charting

### Database Schema

#### PostgreSQL (Analytics Data)
The service connects to existing DentistDSS tables:
- `appointments` - Appointment data for utilization analysis
- `patients` - Patient demographics and statistics
- `bills` - Financial data for revenue reports
- `treatment_plans` - Clinical outcome tracking
- `chat_logs` - AI usage analytics

#### MongoDB Collections

##### report_templates
```json
{
  "_id": "ObjectId",
  "templateCode": "PATIENT_NO_SHOWS",
  "name": "Patient No-Show Analysis",
  "description": "Analyzes patient no-show patterns and trends",
  "category": "CLINICAL",
  "type": "PATIENT_ANALYTICS",
  "queryTemplate": "SELECT ... FROM appointments WHERE ...",
  "parameters": [
    {
      "name": "startDate",
      "type": "DATE",
      "required": true,
      "label": "Start Date"
    }
  ],
  "formatConfigurations": {
    "PDF": { "template": "standard", "options": {} },
    "EXCEL": { "includedColumns": ["date", "total", "no_shows"] }
  },
  "allowedRoles": ["DENTIST", "CLINIC_ADMIN"],
  "active": true
}
```

##### report_executions
```json
{
  "_id": "ObjectId",
  "templateCode": "PATIENT_NO_SHOWS",
  "requestedBy": 123,
  "clinicId": 456,
  "status": "COMPLETED",
  "parameters": { "startDate": "2024-01-01", "endDate": "2024-01-31" },
  "generatedFiles": [
    {
      "format": "PDF",
      "fileName": "patient_no_shows_20240115_143022.pdf",
      "filePath": "/app/reports/...",
      "fileSize": 245760,
      "contentType": "application/pdf"
    }
  ],
  "metrics": {
    "queryExecutionTimeMs": 1250,
    "totalExecutionTimeMs": 3400,
    "recordCount": 150
  }
}
```

## Configuration

### Application Properties
```yaml
# Database Configuration
spring:
  datasource:
    primary:
      url: jdbc:postgresql://localhost:5432/dentistdss
      username: dentistdss
      password: ${POSTGRES_PASSWORD}
    replica:
      url: jdbc:postgresql://replica:5432/dentistdss
      username: dentistdss_readonly
      password: ${POSTGRES_READONLY_PASSWORD}

# Reporting Configuration
reporting:
  async:
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 100
  export:
    temp-directory: /app/temp
    max-file-size: 50MB
    cleanup-after-hours: 24
  email:
    from: reports@dentistdss.com
    max-recipients: 50
    attachment-size-limit: 25MB
```

### Environment Variables
- `POSTGRES_PASSWORD`: Primary database password
- `POSTGRES_READONLY_PASSWORD`: Read replica password
- `MONGO_INITDB_ROOT_PASSWORD`: MongoDB password
- `REDIS_PASSWORD`: Redis password
- `MAIL_HOST`: SMTP server host
- `MAIL_USERNAME`: SMTP username
- `MAIL_PASSWORD`: SMTP password

## API Endpoints

### Report Generation
```bash
# Generate report asynchronously
POST /reports/generate/async
{
  "templateCode": "PATIENT_NO_SHOWS",
  "parameters": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "clinicId": 123
  },
  "requestedFormats": ["PDF", "EXCEL"],
  "emailDelivery": true,
  "emailRecipients": ["admin@clinic.com"]
}

# Generate report synchronously
POST /reports/generate/sync
{
  "templateCode": "REVENUE_ANALYSIS",
  "parameters": { "month": "2024-01" },
  "requestedFormats": ["CSV"]
}
```

### Execution Tracking
```bash
# Get execution status
GET /reports/executions/{executionId}

# Get user's executions
GET /reports/executions?page=0&size=20

# Get clinic executions
GET /reports/executions/clinic/{clinicId}
```

### Template Management
```bash
# Get available templates
GET /reports/templates

# Get specific template
GET /reports/templates/{templateCode}
```

### File Download
```bash
# Download generated file
GET /reports/files/{executionId}/{fileName}
```

## Usage Examples

### Patient No-Show Analysis
```bash
curl -X POST http://localhost:8092/reports/generate/async \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "templateCode": "PATIENT_NO_SHOWS",
    "parameters": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "clinicId": 123
    },
    "requestedFormats": ["PDF", "EXCEL"],
    "emailDelivery": true,
    "emailRecipients": ["manager@clinic.com"]
  }'
```

### Revenue Analysis
```bash
curl -X POST http://localhost:8092/reports/generate/sync \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "templateCode": "REVENUE_ANALYSIS",
    "parameters": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "clinicId": 123
    },
    "requestedFormats": ["PDF"]
  }'
```

## Development

### Running Tests
```bash
./gradlew test
```

### Building the Service
```bash
./gradlew bootJar
```

### Running Locally
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Deployment

### Docker Build
```bash
docker build -t reporting-service .
docker run -p 8092:8092 reporting-service
```

### Environment Setup
The service requires:
- PostgreSQL database with read replica (optional)
- MongoDB for metadata storage
- Redis for caching
- SMTP server for email delivery

## Security & Compliance

### Access Control
- **Role-based Authorization**: Templates restricted by user roles
- **Clinic Data Isolation**: Users can only access their clinic's data
- **Audit Logging**: All report generation and access logged

### Data Protection
- **HIPAA Compliance**: Secure handling of patient data
- **Data Anonymization**: Configurable anonymization for aggregate reports
- **Secure File Storage**: Temporary files with automatic cleanup
- **Encrypted Email**: Secure delivery of sensitive reports

## Monitoring

### Actuator Endpoints
- `/actuator/health`: Service health status
- `/actuator/metrics`: Performance metrics
- `/actuator/caches`: Cache statistics
- `/actuator/scheduledtasks`: Scheduled job status

### Key Metrics
- Report generation time and success rate
- Cache hit ratios and performance
- Database query performance
- Email delivery success rate
- File storage usage

## Performance Optimization

### Database Optimization
- Read replica usage for analytical queries
- Connection pooling with HikariCP
- Query timeout and fetch size optimization
- Materialized views for frequently accessed data

### Caching Strategy
- L1 Cache: Caffeine (in-memory, fast access)
- L2 Cache: Redis (distributed, larger datasets)
- Template caching for quick access
- Result caching for expensive queries

### Async Processing
- Background job queues for report generation
- Separate thread pools for different operations
- Non-blocking email delivery
- Configurable timeout and retry policies
