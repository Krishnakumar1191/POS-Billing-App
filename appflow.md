# POS APP - Complete Application Flow & Architecture Specification

## 1. System Overview & Architecture
**POS APP** is a modern, high-performance Android Point of Sale (POS) and Billing application built strictly with **Jetpack Compose**, **Kotlin Coroutines**, **Room (SQLite)**, and **Google Firebase**.

The app uses the **MVVM (Model-View-ViewModel)** pattern combined with an **Offline-First Dual-Sync Architecture**:
* **Local Persistence (Primary)**: All read and write operations hit the local Room SQLite database first for instant, zero-latency offline performance.
* **Cloud Mirroring (Secondary)**: Background IO coroutines (`syncScope.launch`) asynchronously mirror user profiles, products, categories, orders, and authentication states up to **Google Firebase Firestore** and **Firebase Auth**.

---

## 2. Navigation & User Flow
```
                     [ Splash Screen ]
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   (Auto-Login Valid)               (Auto-Login Failed)
            │                                 │
            │                         [ Login Screen ]
            │                                 │
            │                         ┌───────┴───────┐
            │                         ▼               ▼
            │                   [ Register ]   (Login Success)
            │                         │               │
            └─────────────────────────┼───────────────┘
                                      ▼
                             [ Dashboard Screen ]
                                      │
         ┌───────────────┬────────────┼────────────┬───────────────┐
         ▼               ▼            ▼            ▼               ▼
   [ POS Terminal ] [ Products ] [ Categories ] [ Cash/Exp ] [ Inventory ]
         │               │            │            │               │
         ▼               ▼            ▼            ▼               ▼
   [ Checkout ]    [ Edit Prod ] [ Edit Cat ]   [ Ledger ]   [ Stock Log ]
         │
         ▼
[ Thermal Print / QR ]
```

---

## 3. Screen-by-Screen Detailed Flow

### A. Authentication & Onboarding
1. **Splash Screen (`SplashScreen`)**:
   - Launches on app open with a clean Maroon branding animation.
   - Triggers `viewModel.checkAutoLogin()` via `SharedPreferences` session cache.
   - **If Logged In**: Directs immediately to `Dashboard`.
   - **If Not Logged In**: Directs to `Login`.

2. **Login Screen (`LoginScreen`)**:
   - Accepts **Email**, **Username (Full Name)**, or **Mobile Number** alongside password.
   - Looks up the user locally in Room. If logging in via Username/Phone, retrieves the associated Email and authenticates against `FirebaseAuth.getInstance().signInWithEmailAndPassword()`.
   - Directs to `Dashboard` upon success and caches the user ID locally.

3. **Register Screen (`RegisterScreen`)**:
   - Form fields: Full Name, Email Address, Mobile Number, Company Name, Password (Min 6 chars).
   - Validation: Enforces strict 10-digit mobile number validation and standard regex email validation.
   - Registers user in **Firebase Auth** + **SQLite**, creates a `/users/{uid}` document in **Firestore**, initializes the initial `ShopProfile`, and redirects to `Login`.

---

### B. Main Hub & Analytics
1. **Dashboard (`DashboardScreen`)**:
   - **Quick Overview Card**: Displays Today's Total Sales count, Today's Revenue (₹), Cash In, and Expenses.
   - **Weekly Transactions Chart**: A custom Canvas-based bar chart with horizontal grid lines and 3-letter weekday labels (`Sun`, `Mon`, `Tue`, etc.) comparing daily Sales vs. Expenses.
   - **Monthly Metrics Grid**: Displays Monthly Sales Count, Sales Revenue, Total Cash In, and Total Expenses.
   - **Low Stock Alert**: Filtered exclusively for storable products below the threshold count (< 5 items).
   - **Recent Transactions**: Segmented tab control allowing live switching between Recent Sales and Recent Expenses/Cash-Outs.

---

### C. POS Terminal & Checkout
1. **POS Sales Terminal (`PosHomeScreen`)**:
   - **Category Filter Tabs**: Dynamically filters products by Category.
   - **Search Bar**: Instant real-time text search for product names or barcodes.
   - **Product Grid**: Displays product cards with image, price, stock level, and category.
   - **Cart Drawer/Sheet**: Live calculation of Subtotal, Tax Amount (Inclusive vs. Exclusive, Percent vs. Fixed), and Total. Allows incrementing, decrementing, or clearing items.

2. **Checkout (`CheckoutScreen`)**:
   - **Customer Card**:
     - Defaults to **"Walk-in Customer"**.
     - Dropdown menu to select existing customers saved in Room/Firestore.
     - **"+ Create New Customer"** button opens a dialog to register a new customer (Name + 10-digit validated phone number) on the fly.
   - **Payment Method Selection**: Cash, Bank, or UPI.
     - **UPI Payment**: Generates a live QR Code dynamically via ZXing based on the Shop's UPI ID and total bill amount.
   - **Receipt Printing**: Calls `PrinterHelper` to format ESC/POS receipt data and send it to the configured Bluetooth or Wi-Fi printer.

---

### D. Inventory & Stock Management
1. **Product Management (`ProductsScreen` & `ProductEditScreen`)**:
   - Lists all products with image, sale price, cost price, and available stock.
   - **Product Types**:
     - `Storable Product`: Full stock tracking, low stock alerts, and inventory logs.
     - `Consumable`: Basic product without strict stock warnings.
     - `Service`: **Does not maintain stock**. Excluded from low stock alerts and stock deductions during checkout.
   - Supports photo upload (saved locally to `context.filesDir`), Barcode, Category mapping, Sales Tax, and Purchase Tax assignment.

2. **Inventory Screen (`InventoryScreen`)**:
   - Shows storable items with live stock counts and low-stock warnings in red.
   - **Stock Adjustments**: Tap the Edit button on any item to set a new stock level and enter a reason (e.g., *"Physical Count"*, *"Damaged"*).
   - **Stock Movement History Ledger**: Tap the History button on any item to view a timeline popup showing every stock increase (`+` Green) and decrease (`-` Red) with reasons and timestamps.

3. **Category Management (`CategoriesScreens`)**:
   - Category list and editor with photo upload capability.

---

### E. Cash & Expense Management
1. **Cash Management Screen (`CashManagementScreen`)**:
   - **Tabs**:
     - `Cash In`: For adding change floats or opening balances.
     - `Cash Out`: For non-expense cash removals (e.g., safe drops, bank deposits).
     - `Expenses`: For operational costs paid directly from the cash drawer (e.g., vendor payments, tea, supplies).
     - `Ledger Table`: A daily report table summarizing Date | Cash In | Cash Out | Expenses day-by-day.

---

### F. Taxes & Configuration
1. **Tax Configuration (`TaxScreens`)**:
   - Full CRUD for tax rules.
   - Options: Percentage vs. Fixed Amount, Included in Price (Inclusive) vs. Excluded (Exclusive), Scope (Sale, Purchase, None).

2. **Printer Configuration (`PrinterSettingsScreen`)**:
   - **Connection**: Bluetooth SPP or Wi-Fi (Network IP).
   - **Paper Width**: 58mm (Mobile Printer, 32 chars/line) vs. 80mm (e-POS Thermal Printer, 48 chars/line).
   - **Network Scanner**: Scans local Wi-Fi subnet (e.g. `192.168.1.*`) in parallel on port `9100` to automatically discover thermal printers.
   - **Auto-Cutter**: Sends ESC/POS partial paper cut command (`GS V A 0`) for 80mm e-POS thermal printers.
   - **Test Print**: Button to test printing directly from settings.

3. **Shop Profile (`ProfileSettingsScreen`)**:
   - Configures Shop Name, Address, Phone, Email, Bank Details, and UPI ID for QR code generation.

4. **Backup & Restore (`BackupRestoreScreen`)**:
   - Database backup to local storage and restoration via file picker.

5. **Logout Flow**:
   - Clears `SharedPreferences` session cache, resets `_currentUser` state, and flushes the entire navigation backstack to prevent returning to protected screens.

---

## 4. Technical Specifications Summary
* **Language**: Kotlin 2.1.0
* **UI**: Jetpack Compose with Material 3 (Forced Light Theme)
* **Database**: Room v2.6.1 (SQLite Database Version 11)
* **Cloud**: Firebase BoM 33.7.0 (Firestore + Auth)
* **Image Loading**: Coil Compose 2.6.0
* **QR Generation**: ZXing Core 3.5.3
* **Printing**: Custom ESC/POS engine over Bluetooth RFCOMM Socket / TCP Socket (Port 9100)
