package com.bankofcli.api;

import com.bankofcli.persistence.BankDao;
import com.bankofcli.persistence.BankDaoImpl;
import com.bankofcli.service.BankService;
import com.bankofcli.service.BankServiceImpl;

public class Main {
    public static void main(String[] args) {
        BankDao bankDao = new BankDaoImpl();
        BankService bankService = new BankServiceImpl(bankDao);
        new BankRepl(bankService).run();
    }
}
