package Temporary;

import java.util.ArrayList;

import DataStyle.Records;

public class Temp_Records {
	public static int num;
	public static ArrayList<Records> tempRecordsList = new ArrayList<Records>();
	
	public static void Clear() {
		num = 0;
		tempRecordsList.clear();
	}
}
