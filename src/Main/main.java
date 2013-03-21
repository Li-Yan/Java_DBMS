package Main;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import qifeng.db.DataBase;
import qifeng.db.DbTable;
import qifeng.lowlevel.BufferManager;
import qifeng.schema.Schema;
import qifeng.schema.SchemaBuilder;
import qifeng.schema.Symbol;
import qifeng.schema.Type;
import qifeng.schema.TypeHelper;

import DataStyle.Records;
import GUI.GUI;
import GUI.Login;
import Semant.Semant;
import Tools.Tools;
import Exec_Tree.Exec_Exp;
import Exec_Tree.Exec_Expr;

public class main {

	public static DataBase database = null;
	public static BufferManager bm = null;
	public static GUI gui = null;
	public static int insertcnt = 0;
	public static ArrayList<Records> excutePlan (String text) {
		ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes()); 
		ErrorMsg.ErrorMsg errMsg = new ErrorMsg.ErrorMsg("test.txt");
		Parse.Parse p = null;
		try {
			p = new Parse.Parse(inp, errMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Absyn.StmList stmList = p.GetExp();
		Semant semant = new Semant(errMsg);
		Execute.Execute exec = new Execute.Execute();
		ArrayList<Records> records = new ArrayList<Records>();
		while(stmList != null) {
			Exec_Expr exec_Expr = semant.transStm(stmList.head);
			try {
				if (exec_Expr != null)
					if (exec_Expr instanceof Exec_Exp) {
						Records r = exec.executeExp((Exec_Exp) exec_Expr);
						if (r != null)
							records.add(r);
					} else {
					}
			} catch (NullPointerException e) {
				
			} finally {
				stmList = stmList.tail;
				bm.sync();
			}
		}
		return records;
	}
	
	public static int InitDatabase() throws IOException, ClassNotFoundException {
		File file = new File("Database.db");
		if (!file.exists()) {
			//Create database file
			try {
				qifeng.db.DataBase.createEmptyFile("Database.db", 50*1024*1024);
			} catch (IOException e) {
				System.err.println("Create Database Error!");
				e.printStackTrace();
				return 0;
			}
		}
		try {
			database = DataBase.open("Database.db");
		} catch (IOException e) {
			System.err.println("Open Database Error!");
			e.printStackTrace();
			return 0;
		}
		bm = BufferManager.getDefaultBufferManager();
		bm.sync();
		return 1;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Login dialog = new Login(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}
	
	public static void AppendGuiOutput(String s) {
		gui.AppendOutput(s);
	}
	public static void RefreshTree() {
		gui.RefreshTree();
	}
}
