# SDI AI Insurance Agent (Scraper)

## Project Overview

This is an **AI-powered automation agent** built with **Java**, **LangChain4j**, and **Selenium WebDriver** designed to automatically calculate **Civil Liability (Гражданска Отговорност) insurance prices** from the SDI Broker website.

The agent navigates the SDI online insurance calculator, fills in vehicle details, extracts the returned price offers, parses them into a structured comparison table, and sends the results via email — all automatically.

---

## Key Features Implemented

- **Browser Automation:** Uses Selenium WebDriver with Brave browser to navigate and fill the SDI insurance questionnaire automatically.
- **AI Agent Integration:** Powered by LangChain4j with the Groq API (LLaMA 3.3 70B) to orchestrate tool usage and decision-making.
- **Multi-Step Form Handling:** Completes a 2-step insurance questionnaire including vehicle type, engine volume, fuel type, power, registration year, region, driver experience, and usage type.
- **Offer Extraction:** Scrapes returned insurance offers from the results page using JavaScript DOM queries.
- **Data Parsing:** Parses raw extracted text into structured `InsuranceOffer` objects sorted by price.
- **CSV Export:** Saves extracted offers to a dated CSV file (`GO_Results_dd-MM-yyyy.csv`) and a current file (`GO_Results.csv`).
- **Email Reporting:** Sends a formatted HTML email report with the comparison table via Gmail SMTP.

---

## Technologies Used

| Category | Technology | Version / Role |
|---|---|---|
| **Backend** | Java Development Kit (JDK) | 17+ |
| **AI Framework** | LangChain4j | 0.35.0 |
| **LLM Provider** | Groq API (LLaMA 3.3 70B) | via OpenAI-compatible endpoint |
| **Browser Automation** | Selenium WebDriver | 4.21.0 |
| **Driver Management** | WebDriverManager | 5.8.0 |
| **Email** | Jakarta Mail (Gmail SMTP) | 2.0.1 |
| **Build Tool** | Apache Maven | 3+ |
| **Browser** | Brave (Chromium-based) | Latest |

---

## Project Structure

```
SDI_Insurance_Agent/
├── src/main/java/com/sirma/
│   ├── Main.java               # Entry point - orchestrates the agent
│   ├── SDITool.java            # Selenium automation - fills the form
│   ├── InsuranceAssistant.java # LangChain4j AI service interface
│   ├── ResultParser.java       # Parses CSV and builds comparison table
│   └── EmailService.java       # Sends HTML email report via Gmail
├── config.properties           # Email credentials (not committed to Git)
├── GO_Results.csv              # Latest extracted offers (auto-generated)
├── GO_Results_dd-MM-yyyy.csv   # Dated history file (auto-generated)
└── pom.xml
```

---

## Getting Started

Follow these steps to get the application up and running locally.

### 1. Prerequisites

Make sure you have the following installed:

- **Java Development Kit (JDK):** Version 17 or later
- **Apache Maven:** Installed, or use an IDE with built-in Maven support (IntelliJ IDEA recommended)
- **Brave Browser:** Installed at `C:\Program Files\BraveSoftware\Brave-Browser\Application\brave.exe`
- **Groq API Key:** Free account at [console.groq.com](https://console.groq.com)
- **Gmail Account:** With App Password enabled for SMTP

---

### 2. Configuration

#### Environment Variable (API Key)

Set the Groq API key as an environment variable.

In IntelliJ: **Run → Edit Configurations → Environment Variables**

```
GROQ_API_KEY=your_groq_api_key_here
```

#### Email Configuration

Create a `config.properties` file in the **project root** (next to `pom.xml`):

```properties
mail.username=your_gmail@gmail.com
mail.password=your_gmail_app_password
```

> **Important:** Use a Gmail **App Password**, not your regular password.
> Generate one at: Google Account → Security → 2-Step Verification → App Passwords

---

### 3. Build and Run

Build the project:

```
mvn clean install
```

Run from IntelliJ by clicking the **Run** button on `Main.java`, or via terminal:

```
mvn exec:java -Dexec.mainClass="com.sirma.Main"
```

---

## How It Works

```
1. Main.java starts the agent
        ↓
2. SDITool opens Brave browser → navigates to SDI website
        ↓
3. Fills Step 1: Vehicle type, engine, fuel, power, year, region
        ↓
4. Fills Step 2: Driver experience, usage, KASKO status
        ↓
5. Clicks "Calculate" → waits for results
        ↓
6. Extracts offer data via JavaScript DOM queries
        ↓
7. Saves to GO_Results.csv + GO_Results_dd-MM-yyyy.csv
        ↓
8. ResultParser reads CSV → builds sorted comparison table
        ↓
9. EmailService sends HTML email report via Gmail SMTP
```

---

## Code Highlights

| Feature | File | Implementation Detail |
|---|---|---|
| **Cookie Handling** | `SDITool.java` | Removes cookie consent banner via JS before interacting with the form |
| **Dynamic Selects** | `SDITool.java` | Uses `WebDriverWait` to wait for dependent dropdowns to load after AJAX |
| **Offer Extraction** | `SDITool.java` | Scans all DOM elements for text containing `лв` to capture price data |
| **Data Parsing** | `ResultParser.java` | Uses Regex patterns to extract company names, single payments and installment formulas |
| **Price Sorting** | `ResultParser.java` | Sorts `InsuranceOffer` objects by `singlePaymentEur` using Java Streams |
| **Email Formatting** | `EmailService.java` | Sends HTML email with monospace table using Jakarta Mail and Gmail SMTP |
| **Config Loading** | `EmailService.java` | Reads credentials from `config.properties` to avoid hardcoded secrets |
| **Dated CSV Export** | `SDITool.java` | Names history files `GO_Results_dd-MM-yyyy.csv` using `DateTimeFormatter` |

---

## Vehicle Configuration

The current default vehicle configuration in `SDITool.java`:

| Field | Value |
|---|---|
| Vehicle Type | Лек автомобил (Passenger Car) |
| Engine Volume | до 1600 куб. см. |
| Fuel Type | Бензин (Petrol) |
| Power | до 74 kW (101 к.с.) |
| First Registration | 2001 |
| Usage | Лично ползване (Personal use) |
| Region | КН - Кюстендил |

To change these values, edit the corresponding `selectByValue()` calls in `SDITool.java`.

---

## Security Notes

- **Never commit** `config.properties` to Git — add it to `.gitignore`
- **Never hardcode** API keys in source code — use environment variables
- The Groq API key is read from `System.getenv("GROQ_API_KEY")` at runtime

---

## Author

**Simeon Petrov**
