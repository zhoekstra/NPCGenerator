/*
    NPCGenerator: A program for generating random NPC's
    Copyright (C) 2011  Zachary Hoekstra (zhoekstra@gmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * NPCGenerator.java
 * @author zach
 *
 * The GUI for the NPCGenerator. Note that on construction, takes a Generator.
 * Uses this Generator to generate NPC's, and handles all the display and saving
 */
public class NPCGenerator extends JFrame implements ActionListener, WindowListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5183908171556584362L;

	Generator generator;
	
	//Left panel of the generator
	private JPanel generator_panel_left = new JPanel(new GridLayout(0,3));
	//The generator panel
	private JPanel generator_panel = new JPanel(new GridLayout(0,2));
	
	//Labels for constant attributes
	private JLabel name_label = new JLabel("Name:");
	private JLabel gender_label = new JLabel("Gender:");
	private JLabel ethnicity_label = new JLabel("Ethnicity");
	
	//Fields for constant attributes
	private JTextField name_field = new JTextField();
	private String[] available_genders = {"Male","Female"};
	private JComboBox gender_field = new JComboBox(available_genders);
	private JComboBox ethnicity_field;
	
	//Locks for constant attributes
	private JCheckBox name_lock = new JCheckBox();
	private JCheckBox gender_lock = new JCheckBox();
	private JCheckBox ethnicity_lock = new JCheckBox();
	
	//Labels, fields, and locks for all other traits
	//TODO: Currently due to lazyness, the first 3 slots (name, gender,ethnicity)
	//are never used. Need to fix that
	private JLabel[] trait_labels;
	private JTextField[] trait_fields;
	private JCheckBox[] trait_locks;
	
	//A notes section to take notes as the game progresses
	private JTextArea notes = new JTextArea();
	
	//A table section to display saved characters
	private DefaultTableModel table_model = new DefaultTableModel(); 
	private JTable table = new JTable(table_model);
	//Buttons to generate and save
	private JButton generate = new JButton("Generate");
	private JButton save = new JButton("Save");
	
	//Output stream
	private PrintStream output;
	//We don't want to save a character twice, so use a flag to force the user to
	//change something before they save again
	private boolean already_saved = false;
	
	JMenuBar menubar = new JMenuBar();
	
	/**
	 * Contstructor: Sets up and runs the GUI for the Generator
	 * @param g the generator this GUI is using
	 * @throws FileNotFoundException when it cannot access the output file
	 */
	public NPCGenerator(final Generator g) throws FileNotFoundException{
		super("NPC Generator");
		generator = g;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(this);
		this.setLayout(new GridLayout(2,0));
		generator_panel.add(generator_panel_left);
		generator_panel.add(notes);
		add(generator_panel);
		
		for(String i : g.attributes)
			table_model.addColumn(i);
		table_model.addColumn("Notes");
		
		add(table);
		
		ethnicity_field = new JComboBox(g.EthnicList());
		trait_labels = g.traitLabels();
		trait_fields = new JTextField[trait_labels.length];
		trait_locks = new JCheckBox[trait_labels.length];
		for(int i = 0; i < trait_labels.length; ++i){
			trait_fields[i] = new JTextField();
			trait_locks[i] = new JCheckBox();
		}
		
		generator_panel_left.add(name_label);
		generator_panel_left.add(name_field);
		generator_panel_left.add(name_lock);
		
		generator_panel_left.add(gender_label);
		generator_panel_left.add(gender_field);
		generator_panel_left.add(gender_lock);
		
		generator_panel_left.add(ethnicity_label);
		generator_panel_left.add(ethnicity_field);
		generator_panel_left.add(ethnicity_lock);
		
		for(int i = 3; i < trait_labels.length; ++i){
			generator_panel_left.add(trait_labels[i]);
			generator_panel_left.add(trait_fields[i]);
			generator_panel_left.add(trait_locks[i]);
		}
		
		
		generate.addActionListener(this);
		generator_panel_left.add(generate);
		save.addActionListener(this);
		generator_panel_left.add(save);
		
		this.pack();
		this.setVisible(true);
	}
	
	/**
	 * A stub main class
	 * @param args 
	 * @throws FileNotFoundException
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws FileNotFoundException, URISyntaxException{
		if(args.length == 0){
			JFileChooser choose_world = new JFileChooser();
			if (choose_world.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				NPCGenerator generator = new NPCGenerator(new Generator(choose_world.getSelectedFile()));
			}
		}
		else if(args.length == 2){
			Generator g = new Generator(new File(args[0]));
			//PrintStream output = new PrintStream(new FileOutputStream(new File(args[1])));
			int num_generate = Integer.parseInt(args[2]);
			for(int i =0; i < num_generate; ++i){
				for(String str : g.generateCharacter())
					System.out.print("\""+str+"\",");
				System.out.println();
			}
		}
	}

	@Override
	/**
	 * The actionPerformed method: handles when one of our two buttons is pushed
	 */
	public void actionPerformed(ActionEvent arg0) {
		/**
		 * The Generate Button: Generates a character and displays it in the appropriate fields
		 */
		if(arg0.getActionCommand().equals("Generate")){
			already_saved = false;
			List<String> result = new LinkedList<String>();
			//Check for a lock on ethnicity and gender
			if (ethnicity_lock.isSelected() && gender_lock.isSelected()){
				boolean Male = false;
				boolean Female = false;
				if(gender_field.getSelectedItem().equals("Male"))
					Male = true;
				else Female = true;
				String ethnicity = (String)ethnicity_field.getSelectedItem();
				
				try {
					result = generator.generateCharacter(ethnicity, Male, Female);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//Check for lock on ethnicity only
			else if (ethnicity_lock.isSelected()){
				try {
					result = generator.generateCharacter((String)ethnicity_field.getSelectedItem());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//Check for a lock on gender only
			else if (gender_lock.isSelected()){
				boolean Male = false;
				boolean Female = false;
				if(gender_field.getSelectedItem().equals("Male"))
					Male = true;
				else Female = true;
				
				try {
					result = generator.generateCharacter(Male, Female);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//No special locks
			else{
				result = generator.generateCharacter();
			}
			/*
			 * Set all the appropriate boxes 
			 */
			if(!name_lock.isSelected())
				name_field.setText(result.get(0));
			if(!gender_lock.isSelected())
				gender_field.setSelectedItem(result.get(1));
			if(!ethnicity_lock.isSelected())
				ethnicity_field.setSelectedItem(result.get(2));
			
			for(int i = 3; i < trait_labels.length; ++i){
				if(!trait_locks[i].isSelected())
					trait_fields[i].setText(result.get(i));
			}
			notes.setText("");
			
		}
		/**
		 * Save button: Saves a displayed character to our .csv
		 */
		else if(arg0.getActionCommand().equals("Save")){
			if(!already_saved){
				already_saved = true;
				/*
				 * Add the current character to the table
				 */
				List<Object> character_list = new LinkedList<Object>();
				character_list.add(name_field.getText());
				character_list.add(gender_field.getSelectedItem());
				character_list.add(ethnicity_field.getSelectedItem());
				for(int i = 3; i < trait_fields.length; ++i)
					character_list.add(trait_fields[i].getText());
				character_list.add(notes.getText());
				table_model.addRow(character_list.toArray());
			}
		}
		else{
			/// We've found some other change in the editor. We can save again
			already_saved = false;
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	/**
	 * When our application closes, save our current table to our output file
	 */
	@Override
	public void windowClosed(WindowEvent arg0) {
		JFileChooser choose_save = new JFileChooser();
		int save_result = choose_save.showSaveDialog(this);
		
		if(save_result == JFileChooser.APPROVE_OPTION){
			try {
				output = new PrintStream(new FileOutputStream(choose_save.getSelectedFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			for(int row = 0; row < table.getRowCount(); ++row){
				List<Object> curr_row = new LinkedList<Object>();
				for(int col = 0; col < table.getColumnCount(); ++col)
					curr_row.add(table.getValueAt(row, col));
				for(Object i : curr_row)
					output.print("\""+i+"\",");
				output.println();
			}
			output.close();
		}
		
		System.exit(0);		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		
	}
}
