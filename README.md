My Name is Baldwin Sihle Mazibuko

SYSTEM OVERVIEW

In this assignment we build an Inventory Management system for a local pharmacy named "HealthFirst", it was designed and developed to assist the pharmacy
this digital system was made to facilitate the management of medicines, their suppliers, the pharmacy's inventory, business and sales reports along with users of the system(cashier/admin).
this application was developed using JavaSwing/AWT with the help of JDBC for database connectivity, it was made using tools such as intellij and mySQL workbench.

MAIN FEATURES OF THE SYSTEM:
1 User authentication and role-based access
2 Administrator dashboard
3 Cashier point-of-sale (POS)
4 Medicine inventory management
5 Supplier management
6 Cashier user management
7 Stock availability checking
8 Shopping cart and checkout
9 Automatic stock deduction after successful sales
10 Bill generation
11 Printing and saving customer bills
12 Sales reporting
13 Item-wise sales reporting
14 Low-stock reporting
15 Medicine expiry reporting
16 Logout functionality

USERS OF THE SYSTEM

1. Administrator (has CRUD functionality)
Administrators have access to the management and reporting functions of the system.
Administrator functions include:
 a. Managing Medicines - they can Add, View, Update and Delete medicines. 
 b. Managing Suppliers - they can Add, View, Delete and Update supplier details.
 c. Managing Users - They can Create, View, Manage and Delete cashier accounts.
 d. Access to the Reports - They can view the Sales, Item-Wise, Low Stock, and Expiry Reports

2. Cashier
Cashiers have access to functions they need to conduct and process sales.
Cashier functions include:
a. View available medicines
b. Check medicine prices and availability
c. Add medicines to the shopping cart
d. Increase or decrease quantities
e. Clear the cart
f. Complete a sale through checkout
g. Generate a customer bill
h. Print a bill
i. Save a bill as a text file
j. Log out of the system
**Cashiers cannot manage medicines, suppliers, or user accounts.

DEFAULT LOGIN CREDENTIALS
Administrator Account

Username: admin
Password: admining_2
Role: Admin

Cashier Account
Username: cashier
Password: cashing1000s
Role: Cashier

3. HOW TO START THE APPLICATION USING THE EXECUTABLE
The project includes a packaged Windows executable.
a. Navigate to: my_executable__/PIMS_exe/
b. Run: pims.exe
**The packaged application contains the required Java runtime and application files.

4. Database Setup
The application uses a MySQL-compatible database named: pims_db
The database script is provided as: my_database.sql
The script creates the database, tables, relationships, and initial sample data.
**Before running the application from the source code, ensure that the database server is running and that the database has been created using the supplied SQL script.
***********************************************
POINT OF SALE PROCESS (Cashier Functionality)
The general process is:
a. Log in using a Cashier account.
b. Select a medicine from the available stock.
c. Add the medicine to the cart.
d. Adjust the quantity if required.
e. Review the cart and total.
f. Select Checkout.
g. The system validates stock availability and expiry status.
h. The sale is saved to the database.
i. Stock quantities are updated.
j. A bill window is displayed.
k. The bill can be printed or saved as a text file.

STOCK MANAGEMENT
Administrators can manage the pharmacy's medicine inventory, the system also identifies stock that has reached or fallen below its reorder level.
Cashiers can use the Stock Check screen to quickly search for medicines.
The stock information includes:
• Medicine name
• Company
• Medicine type
• Price
• Quantity available
• Stock status
• Expiry date
This allows cashiers to check medicine availability without modifying inventory.

REPORTS
Administrators can access four reports from the Reports section.
a. Sales Report - Displays sales information recorded by the system.
b.Item-Wise Sales Report - Provides information about medicines sold and their sales quantities.
c.Low Stock Report - Displays medicines whose available stock is at or below the configured reorder level.
d.Expiry Report - Displays medicines approaching their expiry date.

BILLING
After a successful checkout, the system generates a bill containing information about the completed transaction.
The bill includes details such as:
• Sale number
• Sale date
• Cashier
• Medicine
• Quantity
• Price
• Total amount
The generated bill can be Printed or Saved as a text file

