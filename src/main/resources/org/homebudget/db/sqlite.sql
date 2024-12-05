-- budget definition

CREATE TABLE "budget" (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	budgetDate DATE NOT NULL,
	CONSTRAINT UNIQUE_BUDGET UNIQUE ( budgetDate )
);

-- income definition

CREATE TABLE "income" (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	name TEXT(100) NOT NULL,
	budgetedPay NUMERIC, 
	payFrequency TEXT(100), 
	nextPayDate DATE);
	
-- payee definition

CREATE TABLE "payee" (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	name TEXT(100) NOT NULL,
	url TEXT(200),
	username TEXT(100),
	password TEXT(100), 
	budgetedPayment NUMERIC, 
	due_on INTEGER, 
	balance NUMERIC, 
    income_id int,
	constraint payee_income_FK foreign key ( income_id ) references "income"
   );	
   
-- payday definition

CREATE TABLE "payday" (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	budget_id int not null,
	income_id int not null, 
	payDate DATE, amount float,
	constraint PAYDAY_INCOME_FK foreign key ( income_id ) references "income",
	constraint PAYDAY_BUDGET_FK foreign key ( budget_id ) references "budget"
);

-- budgetItem definition

CREATE TABLE "budgetItem" (
	id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	payday_id int not null,
	payee_id int not null, 
	amount float,
	payed boolean,
	payDate DATE, 
	constraint BUDGETITEM_PAYDAY_FK foreign key ( payday_id ) references "payday",
	constraint BUDGETITEM_PAYEE_FK foreign key ( payee_id ) references "payee"
);