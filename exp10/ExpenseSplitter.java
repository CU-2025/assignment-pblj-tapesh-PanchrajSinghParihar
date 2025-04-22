import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class ExpenseSplitter extends JFrame {
    // Database connection
    private Connection connection;
    
    // UI Components
    private JTextField descriptionField, amountField, peopleCountField;
    private JPanel namesPanel;
    private JButton splitButton, viewRecordsButton, clearButton;
    private JPanel resultPanel;
    private List<JTextField> nameFields;
    
    // Records view components
    private JDialog recordsDialog;
    private JTable recordsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton deleteButton, editButton;
    
    public ExpenseSplitter() {
        // Initialize database
        initializeDatabase();
        
        // Set up the main frame
        setTitle("Expense Splitter");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create the main panel with a form
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create input fields
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField(20);
        formPanel.add(descriptionField);
        
        formPanel.add(new JLabel("Total Amount:"));
        amountField = new JTextField(10);
        formPanel.add(amountField);
        
        formPanel.add(new JLabel("Number of People:"));
        peopleCountField = new JTextField(5);
        peopleCountField.addActionListener(e -> generateNameFields());
        formPanel.add(peopleCountField);
        
        mainPanel.add(formPanel);
        
        // Create dynamic names panel
        namesPanel = new JPanel();
        namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
        namesPanel.setBorder(BorderFactory.createTitledBorder("People Names"));
        JScrollPane namesScrollPane = new JScrollPane(namesPanel);
        namesScrollPane.setPreferredSize(new Dimension(400, 150));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(namesScrollPane);
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        splitButton = new JButton("Split Expense");
        splitButton.addActionListener(e -> splitExpense());
        buttonPanel.add(splitButton);
        
        viewRecordsButton = new JButton("View Records");
        viewRecordsButton.addActionListener(e -> viewRecords());
        buttonPanel.add(viewRecordsButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);
        
        // Create result panel
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
        JScrollPane resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setPreferredSize(new Dimension(400, 150));
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(resultScrollPane);
        
        // Add the main panel to the frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Initialize name fields list
        nameFields = new ArrayList<>();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create a connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:expenses.db");
            System.out.println("Connected to SQLite database.");
            
            // Create tables if they don't exist
            createTables();
            
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database connection failed: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        
        // Create expenses table
        statement.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "description TEXT NOT NULL," +
                "total_amount REAL NOT NULL," +
                "people_count INTEGER NOT NULL," +
                "date TEXT NOT NULL" +
                ")");
        
        // Create participants table
        statement.execute("CREATE TABLE IF NOT EXISTS participants (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "expense_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE" +
                ")");
        
        statement.close();
    }
    
    private void generateNameFields() {
        try {
            int peopleCount = Integer.parseInt(peopleCountField.getText());
            if (peopleCount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Clear previous fields
            namesPanel.removeAll();
            nameFields.clear();
            
            // Create a panel for names with 2 columns layout
            JPanel innerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
            
            // Generate fields for each person
            for (int i = 1; i <= peopleCount; i++) {
                JLabel nameLabel = new JLabel("Person " + i + ":");
                JTextField nameField = new JTextField(15);
                innerPanel.add(nameLabel);
                innerPanel.add(nameField);
                nameFields.add(nameField);
            }
            
            namesPanel.add(innerPanel);
            namesPanel.revalidate();
            namesPanel.repaint();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void splitExpense() {
        try {
            // Validate input
            String description = descriptionField.getText().trim();
            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double totalAmount = Double.parseDouble(amountField.getText());
            if (totalAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int peopleCount = Integer.parseInt(peopleCountField.getText());
            if (peopleCount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive number of people.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate that all names are filled
            for (JTextField field : nameFields) {
                if (field.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all name fields.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Calculate split amount
            double amountPerPerson = totalAmount / peopleCount;
            
            // Save to database
            long expenseId = saveExpense(description, totalAmount, peopleCount);
            
            // Update result panel
            resultPanel.removeAll();
            
            JLabel resultLabel = new JLabel("Expense split successfully!");
            resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
            resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultPanel.add(resultLabel);
            resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            
            // Show the details
            for (int i = 0; i < peopleCount; i++) {
                String name = nameFields.get(i).getText().trim();
                JLabel personLabel = new JLabel(name + " should pay: $" + String.format("%.2f", amountPerPerson));
                detailsPanel.add(personLabel);
                
                // Save participant to database
                saveParticipant(expenseId, name, amountPerPerson);
            }
            
            resultPanel.add(detailsPanel);
            resultPanel.revalidate();
            resultPanel.repaint();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving to database: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private long saveExpense(String description, double totalAmount, int peopleCount) throws SQLException {
        // Get current date in YYYY-MM-DD format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        
        String sql = "INSERT INTO expenses (description, total_amount, people_count, date) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, description);
        pstmt.setDouble(2, totalAmount);
        pstmt.setInt(3, peopleCount);
        pstmt.setString(4, currentDate);
        
        pstmt.executeUpdate();
        
        // Get the generated expense ID
        ResultSet rs = pstmt.getGeneratedKeys();
        long expenseId = -1;
        if (rs.next()) {
            expenseId = rs.getLong(1);
        }
        rs.close();
        pstmt.close();
        
        return expenseId;
    }
    
    private void saveParticipant(long expenseId, String name, double amount) throws SQLException {
        String sql = "INSERT INTO participants (expense_id, name, amount) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setLong(1, expenseId);
        pstmt.setString(2, name);
        pstmt.setDouble(3, amount);
        
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    private void clearForm() {
        descriptionField.setText("");
        amountField.setText("");
        peopleCountField.setText("");
        namesPanel.removeAll();
        nameFields.clear();
        namesPanel.revalidate();
        namesPanel.repaint();
        
        resultPanel.removeAll();
        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    private void viewRecords() {
        // Create dialog if it doesn't exist
        if (recordsDialog == null) {
            recordsDialog = new JDialog(this, "Expense Records", false);
            recordsDialog.setSize(800, 600);
            recordsDialog.setLayout(new BorderLayout(10, 10));
            
            // Create search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            searchPanel.add(new JLabel("Search:"));
            searchField = new JTextField(20);
            searchPanel.add(searchField);
            
            searchTypeCombo = new JComboBox<>(new String[]{"Name", "Date (YYYY-MM-DD)"});
            searchPanel.add(searchTypeCombo);
            
            JButton searchButton = new JButton("Search");
            searchButton.addActionListener(e -> filterRecords());
            searchPanel.add(searchButton);
            
            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(e -> loadAllRecords());
            searchPanel.add(resetButton);
            
            recordsDialog.add(searchPanel, BorderLayout.NORTH);
            
            // Create table model
            String[] columns = {"ID", "Date", "Description", "Total Amount", "People", "Names", "Amount Per Person"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            recordsTable = new JTable(tableModel);
            recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            recordsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            recordsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            recordsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            recordsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            recordsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
            recordsTable.getColumnModel().getColumn(5).setPreferredWidth(200);
            recordsTable.getColumnModel().getColumn(6).setPreferredWidth(120);
            
            JScrollPane tableScrollPane = new JScrollPane(recordsTable);
            recordsDialog.add(tableScrollPane, BorderLayout.CENTER);
            
            // Create buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            
            editButton = new JButton("Edit Record");
            editButton.addActionListener(e -> editRecord());
            buttonsPanel.add(editButton);
            
            deleteButton = new JButton("Delete Record");
            deleteButton.addActionListener(e -> deleteRecord());
            buttonsPanel.add(deleteButton);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> recordsDialog.setVisible(false));
            buttonsPanel.add(closeButton);
            
            recordsDialog.add(buttonsPanel, BorderLayout.SOUTH);
            
            recordsDialog.setLocationRelativeTo(this);
        }
        
        // Load all records
        loadAllRecords();
        
        // Show dialog
        recordsDialog.setVisible(true);
    }
    
    private void loadAllRecords() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Query to get all expenses with their participants
            String sql = "SELECT e.id, e.date, e.description, e.total_amount, e.people_count " +
                         "FROM expenses e ORDER BY e.date DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                long expenseId = rs.getLong("id");
                String date = rs.getString("date");
                String description = rs.getString("description");
                double totalAmount = rs.getDouble("total_amount");
                int peopleCount = rs.getInt("people_count");
                
                // Get participants for this expense
                String participantsSql = "SELECT name, amount FROM participants WHERE expense_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(participantsSql);
                pstmt.setLong(1, expenseId);
                ResultSet participantsRs = pstmt.executeQuery();
                
                StringBuilder namesBuilder = new StringBuilder();
                double amountPerPerson = 0;
                
                while (participantsRs.next()) {
                    String name = participantsRs.getString("name");
                    amountPerPerson = participantsRs.getDouble("amount");
                    
                    if (namesBuilder.length() > 0) {
                        namesBuilder.append(", ");
                    }
                    namesBuilder.append(name);
                }
                
                participantsRs.close();
                pstmt.close();
                
                // Add record to table
                Object[] row = {
                    expenseId,
                    date,
                    description,
                    String.format("$%.2f", totalAmount),
                    peopleCount,
                    namesBuilder.toString(),
                    String.format("$%.2f", amountPerPerson)
                };
                
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
            if (searchField != null) {
                searchField.setText("");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(recordsDialog, 
                "Error loading records: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterRecords() {
        String searchText = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();
        
        if (searchText.isEmpty()) {
            loadAllRecords();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            
            String sql;
            PreparedStatement pstmt;
            
            if ("Name".equals(searchType)) {
                // Search by participant name
                sql = "SELECT DISTINCT e.id, e.date, e.description, e.total_amount, e.people_count " +
                      "FROM expenses e JOIN participants p ON e.id = p.expense_id " +
                      "WHERE p.name LIKE ? ORDER BY e.date DESC";
                      
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, "%" + searchText + "%");
            } else {
                // Search by date
                sql = "SELECT e.id, e.date, e.description, e.total_amount, e.people_count " +
                      "FROM expenses e WHERE e.date LIKE ? ORDER BY e.date DESC";
                      
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, "%" + searchText + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                long expenseId = rs.getLong("id");
                String date = rs.getString("date");
                String description = rs.getString("description");
                double totalAmount = rs.getDouble("total_amount");
                int peopleCount = rs.getInt("people_count");
                
                // Get participants for this expense
                String participantsSql = "SELECT name, amount FROM participants WHERE expense_id = ?";
                PreparedStatement pstmt2 = connection.prepareStatement(participantsSql);
                pstmt2.setLong(1, expenseId);
                ResultSet participantsRs = pstmt2.executeQuery();
                
                StringBuilder namesBuilder = new StringBuilder();
                double amountPerPerson = 0;
                
                while (participantsRs.next()) {
                    String name = participantsRs.getString("name");
                    amountPerPerson = participantsRs.getDouble("amount");
                    
                    if (namesBuilder.length() > 0) {
                        namesBuilder.append(", ");
                    }
                    namesBuilder.append(name);
                }
                
                participantsRs.close();
                pstmt2.close();
                
                // Add record to table
                Object[] row = {
                    expenseId,
                    date,
                    description,
                    String.format("$%.2f", totalAmount),
                    peopleCount,
                    namesBuilder.toString(),
                    String.format("$%.2f", amountPerPerson)
                };
                
                tableModel.addRow(row);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(recordsDialog, 
                "Error filtering records: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteRecord() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(recordsDialog, "Please select a record to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(recordsDialog, 
            "Are you sure you want to delete this expense record?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        long expenseId = (long) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Delete the expense (cascade will delete participants)
            String sql = "DELETE FROM expenses WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, expenseId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Refresh the table
            loadAllRecords();
            
            JOptionPane.showMessageDialog(recordsDialog, "Record deleted successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(recordsDialog, 
                "Error deleting record: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editRecord() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(recordsDialog, "Please select a record to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        long expenseId = (long) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Get expense details
            String sql = "SELECT description, total_amount, people_count, date FROM expenses WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, expenseId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String description = rs.getString("description");
                double totalAmount = rs.getDouble("total_amount");
                int peopleCount = rs.getInt("people_count");
                String date = rs.getString("date");
                
                // Get participants
                String participantsSql = "SELECT id, name FROM participants WHERE expense_id = ?";
                PreparedStatement pstmt2 = connection.prepareStatement(participantsSql);
                pstmt2.setLong(1, expenseId);
                ResultSet participantsRs = pstmt2.executeQuery();
                
                List<String> names = new ArrayList<>();
                while (participantsRs.next()) {
                    names.add(participantsRs.getString("name"));
                }
                
                participantsRs.close();
                pstmt2.close();
                
                // Create edit dialog
                showEditDialog(expenseId, description, totalAmount, peopleCount, date, names);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(recordsDialog, 
                "Error retrieving record details: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showEditDialog(long expenseId, String description, double totalAmount, 
                               int peopleCount, String date, List<String> names) {
        JDialog editDialog = new JDialog(recordsDialog, "Edit Expense", true);
        editDialog.setSize(500, 400);
        editDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Fields
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        fieldsPanel.add(new JLabel("Description:"));
        JTextField descField = new JTextField(description);
        fieldsPanel.add(descField);
        
        fieldsPanel.add(new JLabel("Total Amount:"));
        JTextField amountField = new JTextField(String.valueOf(totalAmount));
        fieldsPanel.add(amountField);
        
        fieldsPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        JTextField dateField = new JTextField(date);
        fieldsPanel.add(dateField);
        
        fieldsPanel.add(new JLabel("Number of People:"));
        JTextField countField = new JTextField(String.valueOf(peopleCount));
        countField.setEditable(false);  // Can't change number of people in edit mode
        fieldsPanel.add(countField);
        
        formPanel.add(fieldsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Names panel
        JPanel namesPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        namesPanel.setBorder(BorderFactory.createTitledBorder("People Names"));
        
        List<JTextField> nameFields = new ArrayList<>();
        
        for (int i = 0; i < peopleCount; i++) {
            JLabel nameLabel = new JLabel("Person " + (i + 1) + ":");
            JTextField nameField = new JTextField(names.size() > i ? names.get(i) : "");
            namesPanel.add(nameLabel);
            namesPanel.add(nameField);
            nameFields.add(nameField);
        }
        
        JScrollPane namesScrollPane = new JScrollPane(namesPanel);
        namesScrollPane.setPreferredSize(new Dimension(400, 150));
        formPanel.add(namesScrollPane);
        
        editDialog.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                String newDesc = descField.getText().trim();
                if (newDesc.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Description cannot be empty.", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double newAmount;
                try {
                    newAmount = Double.parseDouble(amountField.getText());
                    if (newAmount <= 0) {
                        JOptionPane.showMessageDialog(editDialog, "Amount must be positive.", 
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Invalid amount format.", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String newDate = dateField.getText().trim();
                // Validate date format
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);
                    dateFormat.parse(newDate);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Invalid date format. Use YYYY-MM-DD.", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate names
                for (JTextField field : nameFields) {
                    if (field.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(editDialog, "All name fields must be filled.", 
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Update expense in database
                updateExpense(expenseId, newDesc, newAmount, newDate, nameFields);
                
                editDialog.dispose();
                loadAllRecords();  // Refresh the records table
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editDialog, 
                    "Error updating record: " + ex.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(cancelButton);
        
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        editDialog.setLocationRelativeTo(recordsDialog);
        editDialog.setVisible(true);
    }
    
    private void updateExpense(long expenseId, String description, double totalAmount, 
                              String date, List<JTextField> nameFields) throws SQLException {
        connection.setAutoCommit(false);
        
        try {
            // Update expense
            String sql = "UPDATE expenses SET description = ?, total_amount = ?, date = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, description);
            pstmt.setDouble(2, totalAmount);
            pstmt.setString(3, date);
            pstmt.setLong(4, expenseId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Calculate new amount per person
            double amountPerPerson = totalAmount / nameFields.size();
            
            // Get existing participants
            sql = "SELECT id, name FROM participants WHERE expense_id = ?";
            PreparedStatement getStmt = connection.prepareStatement(sql);
            getStmt.setLong(1, expenseId);
            ResultSet rs = getStmt.executeQuery();
            
            List<Long> participantIds = new ArrayList<>();
            List<String> existingNames = new ArrayList<>();
            
            while (rs.next()) {
                participantIds.add(rs.getLong("id"));
                existingNames.add(rs.getString("name"));
            }
            rs.close();
            getStmt.close();
            
           // Update participants
           for (int i = 0; i < nameFields.size(); i++) {
            String name = nameFields.get(i).getText().trim();
            
            if (i < participantIds.size()) {
                // Update existing participant
                sql = "UPDATE participants SET name = ?, amount = ? WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(sql);
                updateStmt.setString(1, name);
                updateStmt.setDouble(2, amountPerPerson);
                updateStmt.setLong(3, participantIds.get(i));
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                // Add new participant if needed (shouldn't happen in this case)
                sql = "INSERT INTO participants (expense_id, name, amount) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(sql);
                insertStmt.setLong(1, expenseId);
                insertStmt.setString(2, name);
                insertStmt.setDouble(3, amountPerPerson);
                insertStmt.executeUpdate();
                insertStmt.close();
            }
        }
        
        // Delete extra participants if needed (shouldn't happen in this case)
        if (participantIds.size() > nameFields.size()) {
            for (int i = nameFields.size(); i < participantIds.size(); i++) {
                sql = "DELETE FROM participants WHERE id = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(sql);
                deleteStmt.setLong(1, participantIds.get(i));
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }
        }
        
        connection.commit();
        JOptionPane.showMessageDialog(null, "Expense updated successfully!", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
            
    } catch (SQLException e) {
        connection.rollback();
        throw e;
    } finally {
        connection.setAutoCommit(true);
    }
}

public void closeConnection() {
    if (connection != null) {
        try {
            connection.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public static void main(String[] args) {
    try {
        // Set Look and Feel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    // Create the application on the Event Dispatch Thread
    SwingUtilities.invokeLater(() -> {
        ExpenseSplitter app = new ExpenseSplitter();
        
        // Add window listener to close database connection on exit
        app.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.closeConnection();
            }
        });
    });
}
}