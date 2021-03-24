package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class SQLManager {

	private static Connection conn = null;
	private List<String> customer_name_list;
	private List<String> staff_name_list;

	public SQLManager(String[] args) {
		// MySQLとの接続
		String database_name = args[2];
		try{
			conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/"+database_name+"?useSSL=false",
					args[0], args[1]);
			System.out.println(database_name+"に接続できました．");
		}catch(SQLException e){
			System.out.println(database_name+"に接続できませんでした．");
		}

		//テーブル構造の確認
        showTableStructure("staff_list");

		//SQLの実行と表示
		Statement stm = makeStatement();
        String sql = "select name from customer_list order by ID";
        ResultSet rs = null;
		try {
			rs = stm.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLクエリの実行に失敗しました．");
		}

		customer_name_list = new ArrayList<>();
        try {
			while(rs.next()){
				customer_name_list.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("カーソル位置にデータがありません．");
		}

        sql = "select name from staff_list";
        rs = null;
		try {
			rs = stm.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLクエリの実行に失敗しました．");
		}

		staff_name_list = new ArrayList<>();
        try {
			while(rs.next()){
				staff_name_list.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("カーソル位置にデータがありません．");
		}

	}

	public Statement makeStatement() {
		try {
			return conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Statementの作成に失敗しました．");
			return null;
		}
	}

	public void showTableStructure(String table_name) {
		Statement stm = makeStatement();
        String sql = "desc " + table_name;
        ResultSet rs = null;
		try {
			rs = stm.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLクエリの実行に失敗しました．");
		}
		String[] columns = {"Field", "Type", "Null", "Key", "Default", "Extra"};
        try {
			for(String c: columns) {
			    System.out.print("| " + c + " ");
			}
		    System.out.println("|");
			while(rs.next()){
				for(String c: columns) {
				    System.out.print("| " + rs.getString(c) + " ");
				}
			    System.out.println("|");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("カーソル位置にデータがありません．");
		}
	}

	public List<String> getCustomerNameList(){
		return this.customer_name_list;
	}

	public List<String> getStaffNameList(){
		return this.staff_name_list;
	}
}
