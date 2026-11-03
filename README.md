# POS APP - Android Point of Sale & Billing Solution

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-green.svg)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room%20SQLite-v11-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%26%20Auth-FFCA28.svg)](https://firebase.google.com)

**POS APP** is a full-featured, offline-first Point of Sale (POS) and inventory management Android application built for retail stores, restaurants, and small businesses. It combines modern Jetpack Compose UI with dual-layer data persistence (offline Room SQLite + real-time Firebase Cloud sync) and thermal receipt printing capabilities.

---

## 🌟 Key Features

* 🛒 **POS Sales Terminal**: Fast product catalog browsing, search bar, category tabs, and live shopping cart subtotal/tax/total computation.
* 💳 **Multi-Payment & Dynamic UPI QR**: Supports Cash, Bank, and UPI payments with auto-generated QR codes based on total bill amount and shop UPI ID.
* 🧾 **Thermal ESC/POS Printing**:
  * Bluetooth & Network Wi-Fi printing on Port 9100.
  * Paper width formatting for **58mm (Mobile)** and **80mm (e-POS)** printers.
  * Automatic paper cutter command (`GS V A 0`) for 80mm thermal e-POS printers.
  * Built-in Wi-Fi Subnet Scanner to automatically discover printers on local network.
  * In-app "Test Print" connection testing.
* 📦 **Inventory & Stock Management**:
  * Product types: `Storable Product`, `Consumable`, and `Service`.
  * Services do **not** maintain stock or trigger low stock alerts.
  * Stock adjustment tool with reason logging (*"Physical count"*, *"Damaged"*).
  * Stock movement ledger showing timeline history of sales (+/-) and manual adjustments.
* 💰 **Cash & Expense Management**:
  * Track Cash In, Cash Out, and Operational Expenses.
  * Includes a **Daily Ledger Table** showing day-by-day cash flow history.
* 📊 **Analytics Dashboard**:
  * Quick Today's Overview cards (Sales, Revenue, Cash In, Expense).
  * Canvas-based weekly sales vs. expenses bar chart with grid lines and 3-letter weekday labels (`Sun`, `Mon`, etc.).
  * Low stock alerts and monthly metric summaries.
* 👤 **Customer Management**:
  * Walk-in customer default or select/register new customers on the fly with 10-digit phone validation.
  * Customer details printed on thermal receipts.
* 🏷️ **Tax Configuration**:
  * Full GST/VAT support (Percentage/Fixed, Inclusive/Exclusive, Sale/Purchase scope).
* ☁️ **Firebase Sync & Offline First**:
  * 100% functional offline using local Room SQLite.
  * Background synchronization with **Firebase Firestore** and **Firebase Auth**.
* 🔐 **Security & Backup**:
  * Local database backup & restore functionality.
  * Session management with auto-login and complete backstack clearance on logout.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: [Kotlin](https://kotlinlang.org) (v2.1.0)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
* **Architecture**: MVVM Pattern + Repository Pattern
* **Local Database**: [Room SQLite](https://developer.android.com/training/data-storage/room) (Database Version 11)
* **Cloud Sync**: [Google Firebase BoM](https://firebase.google.com/docs/android/setup) (Firestore & Auth)
* **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/)
* **QR Code Engine**: [ZXing Core](https://github.com/zxing/zxing)
* **Asynchronous Execution**: Kotlin Coroutines (`Dispatchers.IO`) & StateFlow

---

## 🗄️ Database Schema & Room Migrations

The database (`pos_database`) uses explicit migrations from version 5 through 11:

| Table Name | Description | Key Fields |
| :--- | :--- | :--- |
| `products` | Catalog items | `id`, `name`, `price`, `costPrice`, `stock`, `type`, `imageUri`, `saleTaxId`, `purchaseTaxId` |
| `categories` | Product categories | `id`, `name`, `imageUri` |
| `orders` | Sales records | `id`, `orderNumber`, `timestamp`, `totalAmount`, `customerName`, `customerPhone` |
| `order_lines` | Individual cart items | `id`, `orderId`, `productId`, `quantity`, `unitPrice`, `lineTotal` |
| `customers` | Client directory | `id`, `name`, `phone` |
| `stock_logs` | Inventory ledger | `id`, `productId`, `changeAmount`, `type`, `reason`, `timestamp` |
| `cash_transactions`| Cash flow/Expenses | `id`, `type` (`CASH_IN`/`CASH_OUT`/`EXPENSE`), `amount`, `reason`, `timestamp` |
| `taxes` | Tax rules | `id`, `name`, `amount`, `amountType`, `typeTaxUse`, `priceInclude` |
| `local_users` | Offline user accounts| `id`, `email`, `mobileNumber`, `name`, `passwordHash`, `businessName` |

---

## 🚀 Getting Started & Setup

### Prerequisites
* **Android Studio**: Ladybug / 2024.2.1 or newer.
* **JDK**: Version 17.
* **Min SDK**: 26 (Android 8.0 Oreo).
* **Target SDK**: 35 (Android 15).

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/POSAPP.git
cd POSAPP
```

### 2. Add Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a project and add an Android App with package name: `com.example.posapp`.
3. Download the `google-services.json` file.
4. Place `google-services.json` inside the `app/` folder directory (`POSAPP/app/google-services.json`).
5. In Firebase Console, enable **Authentication** (Email/Password) and **Firestore Database** (Test Mode).

### 3. Build & Run
Open the project in Android Studio, allow Gradle to sync dependencies, and click **Run** (`Shift + F10`) on an emulator or physical Android device.

---

## 🖨️ Printer Configuration Guide

1. Navigate to **Settings > Printer Configuration** in the app.
2. Choose **Connection Type**: `Bluetooth` or `Wi-Fi`.
3. Choose **Paper Width**:
   * **58 mm (Mobile)**: Formats receipt to 32 characters per line.
   * **80 mm (e-POS)**: Formats receipt to 48 characters per line and sends an ESC/POS partial paper-cut command upon print completion.
4. Tap **SCAN** to discover nearby Bluetooth devices or scan your local Wi-Fi subnet for printers listening on port `9100`.
5. Tap **TEST PRINTER CONNECTION** to verify the hardware setup.

---

## 📄 License & Documentation
For detailed screen flows, architecture diagrams, and navigation specs, check [appflow.md](appflow.md).
