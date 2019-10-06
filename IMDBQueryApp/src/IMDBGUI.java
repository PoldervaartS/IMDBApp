import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IMDBGUI {

	static Connection conn = null;
	static Statement stmt = null;

	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String DB_URL = "jdbc:postgresql://db-315.cse.tamu.edu/shanepoldervaart_db";

	JFrame frame;
	JPanel panel;

	public IMDBGUI(){
		frame = new JFrame("IMDB Query App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new GridLayout(3, 1));
		JLabel headerLabel = new JLabel("Create your Movie Query",JLabel.CENTER );

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
			   System.exit(0);
			}        
		 });   
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Press");
		JCheckBox toggleTextOutput = new JCheckBox("Output to Text File");
		
		frame.add(headerLabel);
		frame.add(toggleTextOutput);
		frame.add(panel);
		frame.add(button);

		show();

		if (connectToDB() == 0) {

			closeDBConnection();
		} else {
			System.out.println("Failed to connect");
		}
	}

	int connectToDB() {

		// Get username & password
		String USER;
		String PASS;
		Scanner input = new Scanner(System.in);
		System.out.println("Input the username: ");
		USER = input.next();
		System.out.println("Input the password: ");
		PASS = input.next();
		input.close();

		try {

			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Opened database successfully");
			stmt = conn.createStatement();

		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
			return 1;
		}

		return 0;
	}

	public static void main(String[] args) {
		IMDBGUI userInterface = new IMDBGUI();
		userInterface.show();
	}

	private void show() {

		frame.setVisible(true);
	}

	int closeDBConnection() {

		// finally block used to close resources
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException se2) {
		} // nothing we can do
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} // end finally try
			// end try
		System.out.println(" Goodbye!");
		return 0;
	}

	void sqlThings() {

		String sql;
		StringTokenizer st;
		try {
			BufferedReader TSVFile = new BufferedReader(new FileReader("./name.basics.tsv"));

			String dataRow = TSVFile.readLine(); // Read first line.

			dataRow = TSVFile.readLine();
			while (dataRow != null) {
				st = new StringTokenizer(dataRow, "\t");
				List<String> dataArray = new ArrayList<String>();
				while (st.hasMoreElements()) {
					dataArray.add(st.nextElement().toString());
				}

				// sql =String.format( "INSERT INTO team.ratings (movieid,avgrating,numvotes) "
//	            + "VALUES (\'%s\',%s,%s);",dataArray.get(0), dataArray.get(1),Integer.parseInt(dataArray.get(2)));

				if (dataArray.get(3).compareTo("\\N") != 0 && dataArray.get(2).compareTo("\\N") != 0) {
					sql = String.format(
							"INSERT INTO team.people (id, name, birthdate, deathdate) "
									+ "VALUES ( $$%s$$, $$%s$$, %d, %d);",
							dataArray.get(0), dataArray.get(1), Integer.parseInt(dataArray.get(2)),
							Integer.parseInt(dataArray.get(3)));
				} else if (dataArray.get(3).compareTo("\\N") == 0 && dataArray.get(2).compareTo("\\N") != 0) {
					sql = String.format(
							"INSERT INTO team.people (id, name, birthdate) " + "VALUES ( $$%s$$, $$%s$$, %d);",
							dataArray.get(0), dataArray.get(1), Integer.parseInt(dataArray.get(2)));
				} else if (dataArray.get(3).compareTo("\\N") != 0 && dataArray.get(2).compareTo("\\N") == 0) {
					sql = String.format(
							"INSERT INTO team.people (id, name, deathdate) " + "VALUES ( $$%s$$, $$%s$$, %d);",
							dataArray.get(0), dataArray.get(1), Integer.parseInt(dataArray.get(3)));
				} else {
					sql = String.format("INSERT INTO team.people (id, name) " + "VALUES ( $$%s$$, $$%s$$);",
							dataArray.get(0), dataArray.get(1));
				}
				stmt.executeUpdate(sql);
				dataRow = TSVFile.readLine(); // Read next line of data.
			}
			// Close the file once all data has been read.
			TSVFile.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (IOException e) {
			// File Not Found Catch
			e.printStackTrace();
		}
	}

}