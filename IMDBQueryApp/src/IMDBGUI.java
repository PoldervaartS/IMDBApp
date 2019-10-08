import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

	final String[] optionsarray = { "None", "Movie Title", "Release Year", "Actor", "Producer", "Director",
			"Average Rating", "Number of Votes", "Country (Abbreviation)", "Genre" };
	static Connection conn = null;
	static Statement stmt = null;

	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String DB_URL = "jdbc:postgresql://db-315.cse.tamu.edu/shanepoldervaart_db";

	JFrame frame;
	JPanel queryPanel;
	JCheckBox toggleTextOutput;
	// TODO make this stuff into arraylists of these things in order to modularize
	JComboBox<String> filterOptions1, filterOptions2;
	JTextField filterParameter1, filterParmeter2, queryOutputTextField;
	boolean outputToTextFile = false;
	Popup connectionPopup, userInfoPopup;

	public IMDBGUI() {
		frame = new JFrame("IMDB Query App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new GridLayout(4, 1));
		JLabel headerLabel = new JLabel("Create your Movie Query", JLabel.CENTER);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				close();
			}

		});

		prepConnectionPopup();
		prepQueryPanel();

		frame.add(headerLabel);
		frame.add(queryPanel);

		if (connectToDB() == 0) {
			connectionPopup.show();
			show();
		} else {
			System.out.println("Failed to connect");
		}
	}

	void prepConnectionPopup() {
		JFrame f = new JFrame("pop");
		JPanel p = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ButtonClickListener());
		okButton.setActionCommand("acknowledge connection");
		JLabel connectionSuccses = new JLabel("Succeeded in connecting to the database");
		p.add(connectionSuccses);
		p.add(okButton);
		f.setSize(200, 100);
		PopupFactory pf = new PopupFactory();
		connectionPopup = pf.getPopup(f, p, 180, 100);
	}

	void prepUserInfoPopup() {
		JFrame f = new JFrame("pop");
		JPanel p = new JPanel();
		JLabel prompt = new JLabel("Input username and password to connect to the database");
		JTextField username = new JTextField();
		JPasswordField passwordField = new JPasswordField();

		PopupFactory pf = new PopupFactory();

	}

	void prepQueryPanel() {
		// Query Panel
		queryPanel = new JPanel();
		queryPanel.setLayout(new FlowLayout());
		JButton submitButton = new JButton("Submit Query");
		toggleTextOutput = new JCheckBox("Output to Text File");
		submitButton.setActionCommand("submit");
		submitButton.addActionListener(new ButtonClickListener());
		toggleTextOutput.setActionCommand("toggleOutputFile");
		toggleTextOutput.addActionListener(new ButtonClickListener());
		filterOptions1 = new JComboBox<String>(optionsarray);
		filterOptions2 = new JComboBox<String>(optionsarray);
		filterParameter1 = new JTextField(20);
		filterParmeter2 = new JTextField(20);
		queryOutputTextField = new JTextField();
		queryOutputTextField.setBounds(100, 300, 700, 150);
		queryOutputTextField.setEditable(false);
		queryPanel.add(filterOptions1);
		queryPanel.add(filterParameter1);
		queryPanel.add(filterOptions2);
		queryPanel.add(filterParmeter2);
		queryPanel.add(toggleTextOutput);
		queryPanel.add(submitButton);
		queryPanel.add(queryOutputTextField);

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

	private void close() {
		System.exit(closeDBConnection());
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

	private void submitQuery() {
		// TODO Make submit query work
		// figure out what the 2 options are
		// make a view in sql
		// order it all so that it has the multiple includes? idk how
		// if that is longer than 10 rows then output as .txt
		// else just system out em?
		System.out.printf("Search 1 Type: %s\tSearch Paramater 1: %s\nSearch 2 Type: %s\tSearch Parameter2: %s\n",
				(String) filterOptions1.getSelectedItem(), filterParameter1.getText(),
				(String) filterOptions2.getSelectedItem(), filterParmeter2.getText());

		// execute
		// EXAMPLE COMPLETED QUERY:

		// SELECT * FROM movietest WHERE name = 'Harry Beaumont'
		// AND title = (SELECT title FROM movietest WHERE name = 'Edna Flugrath');

		// not sure if I want to do this modularly. Probably a good idea
		String sql = "CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name FROM team.movies ";
		String sql2 = "";
		if ((String) filterOptions1.getSelectedItem() == "Actor"
				&& (String) filterOptions2.getSelectedItem() == "Actor") {
			sql = String.format(sql + " INNER JOIN team.characters ON team.characters.movieid = team.movies.id "
					+ "INNER JOIN team.people ON team.characters.personid = team.people.id;");

			sql2 = String.format(
					"SELECT title FROM movieView WHERE name = $$%s$$ AND title = ANY(SELECT title FROM movieView WHERE name = $$%s$$);",
					filterParameter1.getText(), filterParmeter2.getText());
		}

		ArrayList<String> queryResulStrings = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			// Creates the view with the info
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();

			pst = conn.prepareStatement(sql2);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				queryResulStrings.add(rs.getString(1));
			}

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		}

		if (queryResulStrings.size() <= 10 || outputToTextFile) {
			for (String s : queryResulStrings) {
				queryOutputTextField.setText(s + ", " + queryOutputTextField.getText());
			}
		} else {
			// TODO print to file
		}
		// catch (IOException e) {
		// // File Not Found Catch
		// e.printStackTrace();
		// }

	}

	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			switch (command) {
			case "toggleOutputFile":
				outputToTextFile = !outputToTextFile;
				break;
			case "submit":
				submitQuery();
				break;
			case "acknowledge connection":
				connectionPopup.hide();
				break;
			}

		}
	}
}
