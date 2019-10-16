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
	JPanel query3Panel, betweenPanel;
	JCheckBox toggleTextOutput;
	JComboBox<String> filterOptions1, filterOptions2;

	JTextField filterParameter1, filterParameter2, queryOutputTextField, usernameTextField, passwordTextField, startYear, endYear;
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
		prepBetweenPanel();


		JLabel headerLabel = new JLabel("Create your Movie Query", JLabel.CENTER);
		JLabel betweenLabel = new JLabel("Search between 2 years", JLabel.CENTER);
		frame.add(headerLabel);
		frame.add(query3Panel);
		frame.add(betweenLabel);
		frame.add(betweenPanel);
    
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
		submitButton.setActionCommand("submit");
		submitButton.addActionListener(new ButtonClickListener());
		toggleTextOutput.setActionCommand("toggleOutputFile");
		toggleTextOutput.addActionListener(new ButtonClickListener());
		filterOptions1 = new JComboBox<String>(optionsarray);
		filterOptions2 = new JComboBox<String>(optionsarray);
		filterParameter1 = new JTextField(20);
		filterParameter2 = new JTextField(20);
		queryOutputTextField = new JTextField(50);
		queryOutputTextField.setEditable(false);
		query3Panel.add(filterOptions1);
		query3Panel.add(filterParameter1);
		query3Panel.add(filterOptions2);
		query3Panel.add(filterParameter2);
		query3Panel.add(toggleTextOutput);
		query3Panel.add(submitButton);
		query3Panel.add(queryOutputTextField);

	}
	
	void prepBetweenPanel() {
		// Query Panel
		betweenPanel = new JPanel();
		betweenPanel.setLayout(new FlowLayout());
		JButton submitButton = new JButton("Submit Query");
		submitButton.setActionCommand("submitBetween");//might need to change
		submitButton.addActionListener(new ButtonClickListener());
		toggleTextOutput.setActionCommand("toggleOutputFile");
		toggleTextOutput.addActionListener(new ButtonClickListener());
		startYear = new JTextField(20);
		endYear = new JTextField(20);
		betweenPanel.add(startYear);
		betweenPanel.add(endYear);
		betweenPanel.add(submitButton);

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

	String[] assembleSqlQuerriesQuestion3() {
		// figure out what the 2 options are
		// make a view in sql
		// execute the query

		String[] querries = new String[2];
		// EXAMPLE COMPLETED QUERY:

		// SELECT * FROM movietest WHERE name = 'Harry Beaumont'
		// AND title = ANY(SELECT title FROM movietest WHERE name = 'Edna Flugrath');

		// sql is for making the view sql2 is for making the query
		String viewQuery = "CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name, team.ratings.avgrating ";
		String selectQuery = "";

		// if one of them equals "None" only do the basic view creation and query.
		// if both equal "None" do nothing
		// if they are the same one only need sql to have just that table.
		// if different change sql view creation and the query
		String selectedSearch1 = (String) filterOptions1.getSelectedItem();
		String selectedSearch2 = (String) filterOptions2.getSelectedItem();
		String textField1 = filterParameter1.getText();
		String textField2 = filterParameter2.getText();

		boolean areSame = selectedSearch1 == selectedSearch2;
		if (selectedSearch1 == "Actor" || selectedSearch2 == "Actor") {

			if (selectedSearch2 == "Actor") { // Used to make order of inputs not matter
				String temp = textField1;
				textField1 = textField2;
				textField2 = temp;
				temp = selectedSearch1;
				selectedSearch1 = selectedSearch2;
				selectedSearch2 = temp;
			}

			selectQuery = "SELECT title FROM movieView WHERE name = $$%s$$ ";

			// The three cases with appropriate table inclusions
			if (areSame) {
				viewQuery += "FROM team.movies ";
				selectQuery += selectQuery
						+ "AND title = ANY(SELECT title FROM movieView WHERE name = $$%s$$) ORDER BY avgrating desc;";
			} else if (selectedSearch2 == "Producer") {
				viewQuery += "team.jobs.position FROM team.movies INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id ";
				selectQuery += "AND title = ANY(SELECT title FROM movieView WHERE name=$$%s$$ AND position=\'producer\');";

			} else if (selectedSearch2 == "Director") {
				viewQuery += "team.jobs.position FROM team.movies INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id ";
				selectQuery += "AND title = ANY(SELECT title FROM movieView WHERE name=$$%s$$"
						+ "AND (position=\'director\'OR position=\'co-director\')) ORDER BY avgrating desc;";

			} else {
				selectQuery += "ORDER BY avgrating desc;";
			}

			viewQuery += " INNER JOIN team.characters ON team.characters.movieid = team.movies.id "
					+ "INNER JOIN team.people ON team.characters.personid = team.people.id;";

		} else if (selectedSearch1 == "Producer" || selectedSearch2 == "Producer") {

			if (selectedSearch2 == "Producer") { // Used to make order of inputs not matter
				String temp = textField1;
				textField1 = textField2;
				textField2 = temp;
				temp = selectedSearch1;
				selectedSearch1 = selectedSearch2;
				selectedSearch2 = temp;
			}

			viewQuery += ",team.jobs.position FROM team.movies ";
			selectQuery = "SELECT title FROM movieView WHERE (name = $$%s$$ AND position=\'producer\') ";

			if (areSame) {
				selectQuery = selectQuery
						+ "AND title = ANY(SELECT title FROM movieView WHERE (name = $$%s$$ AND position=\'producer\'));";
			} else if (selectedSearch2 == "Actor") {
				viewQuery += "INNER JOIN team.characters ON team.characters.id = team.movies.id";
				selectQuery = selectQuery + "SELECT title FROM movieView WHERE name = $$%s$$)";

			} else if (selectedSearch2 == "Director") {
				selectQuery = selectQuery
						+ "AND title = ANY(SELECT title FROM movieView WHERE (name = $$%s$$ AND (position=\'director\' OR position=\'co-director\'));";
			}
			selectQuery += " ORDER BY avgrating desc;";
			viewQuery += "INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id "
					+ "INNER JOIN team.people ON team.jobs.personid = team.people.id;";

		} else if (selectedSearch1 == "Director" || selectedSearch2 == "Director") {

			if (selectedSearch2 == "Director") { // Used to make order of inputs not matter
				String temp = textField1;
				textField1 = textField2;
				textField2 = temp;
				temp = selectedSearch1;
				selectedSearch1 = selectedSearch2;
				selectedSearch2 = temp;
			}

			viewQuery += ",team.jobs.position FROM team.movies ";
			selectQuery = "SELECT title FROM movieView WHERE (name = $$%s$$ AND (position=\'director\' OR position=\'co-director\')) ";

			if (areSame) {
				selectQuery = selectQuery
						+ "AND title = ANY(SELECT title FROM movieView WHERE (name = $$%s$$ AND position=\'producer\'));";
			} else if (selectedSearch2 == "Actor") {
				viewQuery += "INNER JOIN team.characters ON team.characters.id = team.movies.id";
				selectQuery = selectQuery + "AND title= ANY(SELECT title FROM movieView WHERE name = $$%s$$)";

			} else if (selectedSearch2 == "Producer") {
				selectQuery = selectQuery
						+ "AND title = ANY(SELECT title FROM movieView WHERE (name = $$%s$$ AND position=\'producer\'));";
			}
			viewQuery += "INNER JOIN team.jobs ON team.team.jobs.id = team.movies.id "
					+ "INNER JOIN team.people ON team.jobs.personid = team.people.id;";
		}

		querries[0] = viewQuery;
		querries[1] = String.format(selectQuery, textField1, textField2);

		return querries;
	}

	private void submitQuery3() {
		String[] querries = assembleSqlQuerriesQuestion3();
		String viewQuery = querries[0];
		String selectQuery = querries[1];
		System.out.println(viewQuery);
		System.out.println(selectQuery);
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

	private void submitBetween() {
		String sql = "CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name FROM team.movies ";
		String sql2 = "";
		ArrayList<String> queryResulStrings = new ArrayList<>();
		ArrayList<String> removeYears = new ArrayList<>();
		//only for between 2 years
		try {
			stmt = conn.createStatement();
			// Creates the view with the info
			sql = String.format("CREATE TEMPORARY VIEW movieView AS SELECT title, team.people.name, year FROM team.movies WHERE year >= $$%s$$ AND year <= $$%s$$",startYear.getText(), endYear.getText());
			sql = String.format(sql + " INNER JOIN team.characters ON team.characters.movieid = team.movies.id INNER JOIN team.people ON team.characters.personid = team.people.id;");
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			
			int index = 100;
			ResultSet rs;
			while(index != 0) {
				//gets actors name with most movies
				sql2 = "SELECT name, COUNT(name) AS vo FROM movieView GROUP BY name ORDER BY vo DESC LIMIT 1;";
				pst = conn.prepareStatement(sql2);
				rs = pst.executeQuery();
				String removeActor = rs.getString(1);
				//adds actor to output
				
				//get movie instead of name */
				sql = String.format("SELECT title FROM movieView WHERE name = $$%s$$;", removeActor);
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				
				while (rs.next()) {
					queryResulStrings.add(rs.getString(1));
				}
				//gets years that actor covers
				sql = String.format("SELECT year FROM movieView WHERE name = $$%s$$;", removeActor);
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					removeYears.add(rs.getString(1));
				}
				//removes years from view
				for(int i = 0; i < removeYears.size();i++) {
					sql = String.format("DELETE FROM movieView WHERE year = $$%s$$;", removeYears.get(i));
					pst = conn.prepareStatement(sql);
				}
				removeYears.clear();
				//gets count of view to see if it needs to continue
				sql = "SELECT COUNT(*) FROM movieView;";
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				index = rs.getInt(0);
			}
			//prevents unnecessary calls to database
			sql = "";
			sql2 = "";
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
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
			case "submitBetween":
				submitBetween();
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
