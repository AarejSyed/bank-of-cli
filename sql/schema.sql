-- Create Account table
CREATE TABLE account (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pin TEXT NOT NULL
        CHECK(LENGTH(pin) = 4),                         -- Account PIN must be 4 characters long
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00
        CHECK(balance >= 0.00)                          -- Account balance cannot be negative
);

-- Create Bank_Transaction_Type enum type
CREATE TYPE bank_transaction_type AS ENUM(
    'Deposit',
    'Withdrawal',
    'Transfer_In',
    'Transfer_Out'
);

-- Create Bank_Transaction table
CREATE TABLE bank_transaction (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id)
        ON DELETE RESTRICT,                             -- We block attempts to delete associated account
    amount NUMERIC(15, 2) NOT NULL
        CHECK(amount > 0.00),                           -- Bank_Transaction dollar amount must be positive
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    type bank_transaction_type NOT NULL
);

-- Optimize Bank_Transaction lookups by account_id
CREATE INDEX bank_transaction_account_id_idx ON bank_transaction(account_id);
