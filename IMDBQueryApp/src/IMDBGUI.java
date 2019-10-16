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
import java.io.FileWriter;
import java.io.IOException;
import java.io.*; 
import java.util.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IMDBGUI {

	final String[] optionsarray = { "None", "Movie Title", "Release Year", "Actor", "Producer", "Director",
			"Average Rating", "Number of Votes", "Country (Abbreviation)", "Genre" };
	static Connection conn = null;
	static Statement stmt = null;
	public static final String NL = System.getProperty("line.separator");

	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String DB_URL = "jdbc:postgresql://db-315.cse.tamu.edu/shanepoldervaart_db";

	JFrame frame;
	JPanel queryPanel,query1Panel;
	JCheckBox toggleTextOutput,toggleTextOutput1;
	// TODO make this stuff into arraylists of these things in order to modularize
	JComboBox<String> filterOptions1, filterOptions2;
	JTextField filterParameter1, filterParmeter2, queryOutputTextField,query1OutputTextField, 
	           usernameTextField, passwordTextField,filterParameters1,filterParameters2;
	boolean outputToTextFile = false;
	boolean outputToTextFile1 = false;
	Popup connectionPopup, userInfoPopup;

	public IMDBGUI() {
		frame = new JFrame("IMDB Query App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setLayout(new GridLayout(5, 1));

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				close();
			}

		});

		prepConnectionPopup();
		prepUserInfoPopup();
		prepQueryPanel();
		prepQuery1Panel();

		JLabel headerLabel = new JLabel("Create your Movie Query", JLabel.CENTER);
		JLabel headerLabel1 = new JLabel("Question 1 Degree Of Seperation (Insert two actors in first two input text box)", JLabel.CENTER);
		frame.add(headerLabel);
		frame.add(queryPanel);
		frame.add(headerLabel1);
		frame.add(query1Panel);

		if (connectToDB("shanepoldervaart", "taeKwondo9") == 0) {
			connectionPopup.show();
			show();
		} else {
			System.out.println("Failed to connect");
		}

		// userInfoPopup.show();
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
		usernameTextField = new JTextField(20);
		passwordTextField = new JTextField(20);
		JButton submitUserInfoButton = new JButton("Submit User Info");
		submitUserInfoButton.addActionListener(new ButtonClickListener());
		submitUserInfoButton.setActionCommand("submitUserInfo");

		p.add(prompt);
		p.add(usernameTextField);
		p.add(passwordTextField);

		PopupFactory pf = new PopupFactory();
		userInfoPopup = pf.getPopup(f, p, 250, 200);

	}

	void prepQueryPanel() {
		// Query Panel
		queryPanel = new JPanel();
		queryPanel.setLayout(new FlowLayout());
		JButton submitButton = new JButton("Submit Query");
		toggleTextOutput = new JCheckBox("Output to Text File");
		JButton button = new JButton("+");
		button.setBounds(750, 100, 41, 30);
		submitButton.setActionCommand("submit");
		submitButton.addActionListener(new ButtonClickListener());
		toggleTextOutput.setActionCommand("toggleOutputFile");
		toggleTextOutput.addActionListener(new ButtonClickListener());
		filterOptions1 = new JComboBox<String>(optionsarray);
		filterOptions2 = new JComboBox<String>(optionsarray);
		filterParameter1 = new JTextField(20);
		filterParmeter2 = new JTextField(20);
		queryOutputTextField = new JTextField(50);
		queryOutputTextField.setEditable(false);
		queryPanel.add(filterOptions1);
		queryPanel.add(filterParameter1);
		queryPanel.add(filterOptions2);
		queryPanel.add(filterParmeter2);
		queryPanel.add(button);
		queryPanel.add(toggleTextOutput);
		queryPanel.add(submitButton);
		queryPanel.add(queryOutputTextField);
		


	}

	void prepQuery1Panel(){

		query1Panel = new JPanel();
		query1Panel.setLayout(new FlowLayout());
		JButton submitButton2 = new JButton("Submit");
		toggleTextOutput1 = new JCheckBox("Output to Text File");
		submitButton2.setActionCommand("submit2");
		submitButton2.addActionListener(new ButtonClickListener());
		toggleTextOutput1.setActionCommand("toggleOutputFile1");
		toggleTextOutput1.addActionListener(new ButtonClickListener());
		filterParameters1 = new JTextField(20);
		filterParameters2 = new JTextField(20);
		query1OutputTextField = new JTextField(50);
		query1OutputTextField.setEditable(false);
		query1Panel.add(filterParameters1);
		query1Panel.add(filterParameters2);
		query1Panel.add(toggleTextOutput1);
		query1Panel.add(submitButton2);
		query1Panel.add(query1OutputTextField);

	}

	int connectToDB(String USER, String PASS) {

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
		// TODO Make submit query work for all cases
		// figure out what the 2 options are
		// make a view in sql
		// order it all so that it has the multiple includes? idk how
		// if that is longer than 10 rows then output as .txt

		// EXAMPLE COMPLETED QUERY:

		// SELECT * FROM movietest WHERE name = 'Harry Beaumont'
		// AND title = (SELECT title FROM movietest WHERE name = 'Edna Flugrath');

		// not sure if I want to do this modularly. Probably a good idea
		// sql is for making the view sql2 is for making the query
		String sql = "CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name FROM team.movies ";
		String sql2 = "";

		// 2 actors funcitioning
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

		// length check
		if (queryResulStrings.size() <= 10 || outputToTextFile) {
			queryOutputTextField.setEditable(true);
			for (String s : queryResulStrings) {
				queryOutputTextField.setText(s + ", " + queryOutputTextField.getText());
				System.out.println(s);
			}
			queryOutputTextField.setEditable(false);
		} else {
			// TODO print to file
		}


	}


	private void submitQuery2() {

	        
    	    String actor1 = filterParameters1.getText();
	        String actor2 = filterParameters2.getText();
            String sql11 = String.format(
						"SELECT id FROM team.people WHERE name = $$%s$$;",
						actor1);
            String sql22 = String.format(
						"SELECT id FROM team.people WHERE name = $$%s$$;",
						actor2);
			ArrayList<String> actstr1 = new ArrayList<>();
			ArrayList<String> actstr2 = new ArrayList<>();

			try 
			{
				stmt = conn.createStatement();
				
				PreparedStatement pst = conn.prepareStatement(sql11);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					actstr1.add(rs.getString(1));
				}

				pst = conn.prepareStatement(sql22);
				rs = pst.executeQuery();
				while (rs.next()) {
					actstr2.add(rs.getString(1));
				}

			} catch (SQLException se) 
			{
				se.printStackTrace();
			}
           

            ArrayList<String> movieidact1 = new ArrayList<>();
			ArrayList<String> movieidact2 = new ArrayList<>();

			sql11 = String.format(
						"SELECT movieid FROM team.characters WHERE personid = $$%s$$;",
						actstr1.get(0));
            sql22 = String.format(
						"SELECT movieid FROM team.characters WHERE personid = $$%s$$;",
						actstr2.get(0));

			try 
			{
				stmt = conn.createStatement();
				
				PreparedStatement pst = conn.prepareStatement(sql11);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					movieidact1.add(rs.getString(1));
				}

				pst = conn.prepareStatement(sql22);
				rs = pst.executeQuery();
				while (rs.next()) {
					movieidact2.add(rs.getString(1));
				}

			} catch (SQLException se) 
			{
				se.printStackTrace();
			}



        	if(outputToTextFile1)
        	{

            try{

		      FileWriter myfile = new FileWriter("sandy.txt");
		      //myfile.write(movieidact1.get(i)+NL);
		      myfile.write(Arrays.toString(movieidact1.toArray()));
		      myfile.write(Arrays.toString(movieidact2.toArray()));



		      myfile.flush();
		      myfile.close();
		    

		    } catch (Exception se) 
			{
				se.printStackTrace();
			}
            }
		    query1OutputTextField.setText(Arrays.toString(movieidact1.toArray()) + Arrays.toString(movieidact2.toArray()));
		    
        
		
	}


	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			switch (command) {
			case "toggleOutputFile":
				outputToTextFile = !outputToTextFile;
				break;
			case "toggleOutputFile1":
				outputToTextFile1 = !outputToTextFile1;
				break;
			case "submit":
				submitQuery();
				break;
			case "acknowledge connection":
				connectionPopup.hide();
				break;
			case "submitUserInfo":
				if (connectToDB(usernameTextField.getText(), passwordTextField.getText()) == 0) {
					connectionPopup.show();
					show();
				} else {
					System.out.println("Failed to connect");
				}
				break;
			case "submit2":
				submitQuery2();
				break;
			}

		}
	}

}
