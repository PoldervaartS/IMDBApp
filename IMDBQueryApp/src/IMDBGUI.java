import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IMDBGUI {

	final String[] optionsarray = { "None", "Actor", "Producer", "Director" };
	static Connection conn = null;
	static Statement stmt = null;

	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String DB_URL = "jdbc:postgresql://db-315.cse.tamu.edu/shanepoldervaart_db";

	JFrame frame;
	JPanel query3Panel;
	JCheckBox toggleTextOutput;
	// TODO make this stuff into arraylists of these things in order to modularize
	JComboBox<String> filterOptions1, filterOptions2;
	JTextField filterParameter1, filterParmeter2, queryOutputTextField, usernameTextField, passwordTextField;
	boolean outputToTextFile = false;
	Popup connectionPopup, userInfoPopup;

	public IMDBGUI() {
		frame = new JFrame("IMDB Query App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new GridLayout(4, 1));

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				close();
			}

		});

		prepConnectionPopup();
		prepUserInfoPopup();
		prepQuery3Panel();

		JLabel headerLabel = new JLabel("Create your Movie Query", JLabel.CENTER);
		frame.add(headerLabel);
		frame.add(query3Panel);

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

	void prepQuery3Panel() {
		// Query Panel
		query3Panel = new JPanel();
		query3Panel.setLayout(new FlowLayout());
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
		query3Panel.add(filterOptions1);
		query3Panel.add(filterParameter1);
		query3Panel.add(filterOptions2);
		query3Panel.add(filterParmeter2);
		query3Panel.add(button);
		query3Panel.add(toggleTextOutput);
		query3Panel.add(submitButton);
		query3Panel.add(queryOutputTextField);

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

	private void submitQuery3() {
		// figure out what the 2 options are
		// make a view in sql
		// execute the query

		// EXAMPLE COMPLETED QUERY:

		// SELECT * FROM movietest WHERE name = 'Harry Beaumont'
		// AND title = ANY(SELECT title FROM movietest WHERE name = 'Edna Flugrath');

		// not sure if I want to do this modularly. Probably a good idea
		// sql is for making the view sql2 is for making the query
		String viewQuery = "CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name, team.ratings.avgrating ";
		// TODO make the ORDER BY avgrating desc; last
		String tableJoin = "INNER JOIN team.ratings ON team.ratings.movieid = team.movies.id ";
		String selectQuery = "";

		// if one of them equals "None" only do the basic view creation and query.
		// if both equal "None" do nothing
		// if they are the same one only need sql to have just that table.
		// if different change sql view creation and the query
		// TODO modularity and logic things.
		String textField1 = (String) filterOptions1.getSelectedItem();
		String textField2 = (String) filterOptions2.getSelectedItem();
		boolean areSame = textField1 == textField2;
		if (textField1 == "Actor" || textField2 == "Actor") {

			
			if (textField2 == "Actor") { // Used to make order of inputs not matter
				String temp = textField1;
				textField1 = textField2;
				textField2 = temp;
			}

			selectQuery = String.format("SELECT title FROM movieView WHERE name = $$%s$$ ", textField1);

			if (areSame) {
				viewQuery += "FROM team.movies "; 
				selectQuery += String.format( selectQuery +
					"AND title = ANY(SELECT title FROM movieView WHERE name = $$%s$$) ORDER BY avgrating desc;",
					textField2);
			} else if (textField2 == "Producer") {
				viewQuery += "team.jobs.position FROM team.movies INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id ";
				selectQuery += "AND title = ANY(SELECT title FROM movieView WHERE name=$$%s$$ AND position=\'producer\');";

			} else if (textField2 == "Director") {
				viewQuery += "team.jobs.position FROM team.movies INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id ";
				selectQuery += String.format("AND title = ANY(SELECT title FROM movieView WHERE name=$$%s$$"
					+ "AND (position=\'director\'OR position=\'co-director\')) ORDER BY avgrating desc;", textField2);

			}else{
				selectQuery+="ORDER BY avgrating desc;";
			}

			viewQuery += " INNER JOIN team.characters ON team.characters.movieid = team.movies.id "
				+ "INNER JOIN team.people ON team.characters.personid = team.people.id;";

		} else if (textField1 == "Producer") {

			viewQuery += "team.jobs.position FROM team.movies INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id ";
			if (textField2 == "Producer") { // Used to make order of inputs not matter
				String temp = textField1;
				textField1 = textField2;
				textField2 = temp;
			}

			if (areSame) {
				viewQuery += " INNER JOIN team.jobs ON team.jobs.movieid = team.movies.id "
						+ "INNER JOIN team.people ON team.jobs.personid = team.people.id;";

				selectQuery = String.format(
						"SELECT title FROM movieView WHERE name = $$%s$$ AND title = ANY(SELECT title FROM movieView WHERE name = $$%s$$);",
						filterParameter1.getText(), filterParmeter2.getText());
			} else if (textField2 == "Actor") {

			} else if (textField2 == "Director") {

			}

		} else if (textField1 == "Director") {

		}

		// 2 actors funcitioning
		if ((String) filterOptions1.getSelectedItem() == "Actor"
				&& (String) filterOptions2.getSelectedItem() == "Actor") {
			viewQuery = String
					.format(viewQuery + " INNER JOIN team.characters ON team.characters.movieid = team.movies.id "
							+ "INNER JOIN team.people ON team.characters.personid = team.people.id;");

			selectQuery = String.format(
					"SELECT title FROM movieView WHERE name = $$%s$$ AND title = ANY(SELECT title FROM movieView WHERE name = $$%s$$);",
					filterParameter1.getText(), filterParmeter2.getText());
		}

		ArrayList<String> queryResulStrings = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			// Creates the view with the info
			PreparedStatement pst = conn.prepareStatement(viewQuery);
			pst.execute();

			pst = conn.prepareStatement(selectQuery);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				queryResulStrings.add(rs.getString(1));
			}

			// Drop view
			pst = conn.prepareStatement("DROP VIEW movieView;");
			pst.execute();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		}

		// if result longer than 10 rows or the toggle is on then output as .txt
		if (queryResulStrings.size() <= 10 && !outputToTextFile) {
			queryOutputTextField.setEditable(true);
			for (String s : queryResulStrings) {
				queryOutputTextField.setText(s + ", " + queryOutputTextField.getText());
				System.out.println(s);
			}
			queryOutputTextField.setEditable(false);
		} else {
			try {
				FileWriter fileOut = new FileWriter("a.txt");
				fileOut.write("Hello\n");
				fileOut.write("There");
				fileOut.flush();
				fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			switch (command) {
			case "toggleOutputFile":
				outputToTextFile = !outputToTextFile;
				break;
			case "submit":
				submitQuery3();
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
			}

		}
	}
}
