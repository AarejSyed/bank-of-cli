-- Create Account table
CREATE TABLE account (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    pin TEXT NOT NULL
        CHECK(pin ~* '^[0-9]{4}$'),                     -- Account PIN must be 4 digits long
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00
        CHECK(balance >= 0.00)                          -- Account balance cannot be negative
);

-- Create Bank_Transaction_Type_Enum type
CREATE TYPE bank_transaction_type_enum AS ENUM (
    'deposit',
    'withdrawal',
    'transfer_in',
    'transfer_out'
);

-- Create Bank_Transaction table
CREATE TABLE bank_transaction (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id)
        ON DELETE RESTRICT,                             -- We block attempts to delete associated account
    amount NUMERIC(15, 2) NOT NULL
        CHECK(amount > 0.00),                           -- Bank_Transaction dollar amount must be positive
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    type bank_transaction_type_enum NOT NULL
);

-- Optimize Bank_Transaction lookups by account_id
CREATE INDEX bank_transaction_account_id_idx ON bank_transaction(account_id);
