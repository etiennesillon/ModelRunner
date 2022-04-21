package com.indirectionsoftware.apps.money;

import com.indirectionsoftware.backend.database.IDCData;

public class Account {

	long balance;
	IDCData account;
	
	public Account(IDCData account) {
		this.account = account;
		this.balance = account.getLong("InitialBalance");
	}
	
}

