# Property Management System (PMS)

A comprehensive web application built with Spring Boot and Thymeleaf for managing, leasing, and operating real estate properties (boarding houses, apartments, houses). The system connects property owners with tenants and provides tools for contracts, maintenance, payments, and AI-powered room recommendations.

---

## Technologies Used

| Layer                 | Technology                                       |
| --------------------- | ------------------------------------------------ |
| Backend               | Java, Spring Boot                                |
| Frontend              | Thymeleaf, Tailwind CSS, JavaScript, FontAwesome |
| Database              | Microsoft SQL Server (JPA / Hibernate)           |
| Build Tool            | Maven                                            |
| Payment               | VNPay (sandbox)                                  |
| Image Storage         | Cloudinary                                       |
| AI Chatbot            | Google Gemini 2.5 Flash                          |
| Geocoding             | OpenCage Geocoding API                           |
| Identity Verification | VNPT AI (eKYC: OCR + face liveness)              |

---

## User Roles

| Role               | Access                                       |
| ------------------ | -------------------------------------------- |
| **Guest (Public)** | Browse listings, search properties, register |
| **Owner**          | Full property & leasing management           |
| **Tenant**         | Browse rooms, submit maintenance requests    |
| **Staff**          | Approve posts, handle maintenance requests   |
| **Moderator**      | Review and moderate property posts           |
| **Admin**          | System-wide statistics dashboard             |

---

## Features

### Public / Guest

- Browse homepage with all active property listings
- Filter listings by category
- View property detail pages (images, amenities, pricing, location)
- Increment post view counter on detail visit
- Tenant registration with redirect to eKYC verification
- Owner registration
- Separate login pages for tenants (`/login`) and owners/staff (`/login/owner`)

#### AI Chat — Room Recommendation

- Gemini-powered chatbot widget on the homepage
- Multi-phase conversation: collects budget, location, room type, and amenities from natural language input
- Uses OpenCage Geocoding to resolve location mentions
- Ranks results by boost status, amenity match, budget proximity, location distance, and post longevity
- Rate-limited at 20 requests per 10 minutes per session; 30-minute session timeout
- Contextual quick-reply chips at each conversation phase

---

### eKYC — Identity Verification

Triggered after tenant or owner registration:

- Upload front and back of national ID (CCCD) and a face selfie
- VNPT AI performs OCR on both ID sides and extracts personal information
- Face liveness detection and face comparison (ID photo vs. selfie)
- On success: marks user as `ekycVerified`, saves `EkycSubmission` record
- Skipped automatically if user is already verified

---

### Owner

**Dashboard**

- Business performance statistics summary
- Income chart filterable by date range
- Recent activity feed

**Property Management** (`/owner/properties`)

- Create, edit, and delete properties
- Configure property name, address (province/ward via AJAX), category, rooms count, price, area, images (uploaded to Cloudinary), amenities, surroundings, and target tenants
- Geocoding via OpenCage API on create/edit

**Room Management** (`/owner/rooms`)

- List all rooms across all owned properties
- Create rooms linked to a property; AJAX loads property-specific services
- Delete rooms

**Post Management** (`/owner/posts`)

- Create posts for properties: title, slug, description → submitted for staff approval
- Edit posts (title/description) → resubmitted for moderator review
- Hide or show a post from the public marketplace
- Renew expired posts (deducted from wallet)
- View post analytics (view count, etc.)
- Post lifecycle: `PENDING_APPROVAL` → `ACTIVE` (7-day free trial) → `EXPIRED`; rejections loop back for editing

**Service Items** (`/owner/services`)

- Create and manage services (electricity, water, internet, cleaning, etc.) per property
- Delete services

**Tenant Management** (`/owner/tenants`)

- List, create, and delete tenant records linked to owned properties

**Contract Management** (`/owner/contracts`)

- List contracts with pagination and filters (status, keyword)
- Create contracts: select property → AJAX-load available rooms → assign tenant, dates
- Terminate contracts

**Maintenance Oversight** (`/owner/maintenance`)

- View all maintenance requests submitted by tenants across owned properties
- View full request detail with activity log
- Assign request to a staff member
- Reject request with reason

**Contact Inquiries** (`/owner/contact`)

- View all contact messages submitted on property posts
- Toggle read/unread status

**Wallet** (`/owner/wallet`)

- Dashboard: balance, total deposited, total spent, monthly spending, last 10 transactions
- Top-up via VNPay (10,000 – 50,000,000 VNĐ)
- Full paginated transaction history

**Subscription Plans** (`/owner/management-plans`)

- View all management plans with current subscription status
- Subscribe, upgrade, downgrade (with impact confirmation), or cancel
- Automatic plan expiration handled by a background scheduler (every 5 minutes)

**Posting Packages** (`/owner/posting-packages`)

- Browse available posting packages (duration bundles)
- Purchase packages via VNPay checkout

---

### Tenant

- Browse all available rooms (`/tenant/home`)
- Request to rent a room (validated: user must have a phone number, room must be available; creates a `Contact` entry for the owner)
- View list of currently rented rooms
- Room detail view
- Create maintenance requests for rented rooms: category, description, optional image upload
- View own maintenance request list and status
- View maintenance request detail

**Maintenance Request Categories:** Plumbing, Electrical, Air Conditioning, Furniture, Door Lock, Cleaning, Pest Control, Other

---

### Staff

- **Post Approval** (`/staff/posts`): list all pending posts, approve (starts 7-day free trial) or reject with reason
- **Maintenance Handling** (`/staff/maintenance`): list assigned requests, mark as In Progress, complete with mandatory resolution note

---

### Moderator

- Dashboard with counts of pending and under-review posts
- List and review `PENDING_APPROVAL` and `PENDING_REVISION` posts
- Approve or reject posts with mandatory reason
- Badge counts injected on every moderator page

---

### Admin

- System-wide statistics dashboard (`AdminStatisticsDTO`)
- API usage statistics table per endpoint

---

## Data Initialization

On first startup the application automatically seeds sample data via `DataInitializer`:

- Sample user accounts for each role
- Initial categories, management plans, and posting packages

---

## Prerequisites

1. **JDK** — Java 21 or later
2. **Maven** — for dependency management and build
3. **Microsoft SQL Server** — primary database

---

## Installation & Setup

### 1. Clone the Project

```bash
git clone <your-repo-url>
cd "Property Management System"
```

### 2. Configure the Database

1. Create a database in SQL Server (e.g., `PropertyManagementDB`).
2. Open `src/main/resources/application.properties` and update the connection settings:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=PropertyManagementDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=<YOUR_USERNAME>
spring.datasource.password=<YOUR_PASSWORD>
```

### 3. Configure External Services (optional)

Set the following keys in `application.properties` or as environment variables to enable optional services:

```properties
# Cloudinary (image uploads)
cloudinary.cloud-name=...
cloudinary.api-key=...
cloudinary.api-secret=...

# OpenCage (geocoding)
opencage.api.key=...

# Gemini AI (chatbot)
gemini.api.key=...

# VNPT AI (eKYC)
vnpt.ai.token=...
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

_The first run downloads Maven dependencies and initializes the database schema via Hibernate._

### 5. Access the Application

| URL                                 | Description                             |
| ----------------------------------- | --------------------------------------- |
| `http://localhost:8080`             | Public homepage                         |
| `http://localhost:8080/login`       | Tenant / user login                     |
| `http://localhost:8080/login/owner` | Owner / staff / moderator / admin login |

**Sample credentials** (seeded by `DataInitializer`):

| Role      | Username     | Password |
| --------- | ------------ | -------- |
| Owner     | `owner1`     | `123`    |
| Staff     | `staff1`     | `123`    |
| Moderator | `moderator1` | `123`    |
| Admin     | `admin1`     | `123`    |

---

## Project Structure

```
src/main/java/com/pms/propertymanagement
├── config/          # App configuration, schedulers, data initializer
├── controller/      # MVC & REST controllers per role
├── dto/             # Data transfer objects
├── entity/          # JPA entities (32 total)
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic (30+ services)
└── PropertyManagementSystemApplication.java
```

---

## Notes

- **Default Port:** `8080`
- **Database schema** is auto-created on first run (`spring.jpa.hibernate.ddl-auto=create`). Change to `update` on subsequent runs to preserve data.
- The **subscription expiration scheduler** runs every 5 minutes to expire plans and sync post statuses automatically.
