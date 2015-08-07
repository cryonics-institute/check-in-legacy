package com.cryonicsinstitute.data;

public class Contact {
	public String name;
	public String number;
	
	public Contact(String name, String number) {
		this.name = name;
		
		number = number.replace(".", "");
		number = number.replace(",", "");
		number = number.replace("-", "");
		this.number = number;
	}
}
