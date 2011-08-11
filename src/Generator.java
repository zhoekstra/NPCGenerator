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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JLabel;


public class Generator {
	///A list of attributes that this generator provides, in order of their appearence in the list given by generateCharacter()
	public final List<String> attributes = new LinkedList<String>();
	
	private static File root_dir = null; // new File(Generator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
	private static File[] first_name_files = null;//new File("./Data/name/first/").listFiles();
	private static File[] last_name_files = null;//new File("./Data/name/last/").listFiles();
	private static File[] trait_files = null;//new File("./Data/trait/").listFiles();
	
	private FileReader.ProbList names = new FileReader.ProbList();
	private LinkedList<FileReader> traits = new LinkedList<FileReader>();
	/**
	 * Load a world file and prepare the appropriate files
	 * @param world_path The path to the world file to load
	 * @throws FileNotFoundException If the world file or any of the files indicated by the world file is not found
	 * @throws URISyntaxException If the root file cannot be found.
	 */
	public Generator(File world_path) throws FileNotFoundException, URISyntaxException{
		if(root_dir == null){
			root_dir = new File(Generator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
			System.out.println(root_dir.getPath());
			first_name_files = new File(root_dir.getPath()+"/Data/name/first/").listFiles();
			//System.out.println(first_name_files.getPath());
			last_name_files = new File(root_dir.getPath()+"/Data/name/last/").listFiles();
			//System.out.println(last_name_files.getPath());
			trait_files = new File(root_dir.getPath()+"/Data/trait/").listFiles();
			//System.out.println(root_dir.getPath());
		}
		Scanner world_file = new Scanner(world_path);
		String temp = "";
		/*
		 * Load Name files
		 */
		while(true){
			temp = world_file.nextLine();
			String[] curr_line = temp.split("\t+");
			if(curr_line[0].equals("TRAITS:")) break;
			curr_line[0]+=".txt";
			int prob = curr_line.length > 1 ? Integer.parseInt(curr_line[1]) : 1;
			for(File i : first_name_files){
				if (i.getName().equals(curr_line[0])){
					File first = i;
					File second = null;
					for(File j : last_name_files)
						if(j.getName().equals(curr_line[0])){
							second = j;
							break;
						}
					names.addReader(new FileReader.NameReader(first, second), prob);
				}
			}
		}
		attributes.add("Name");
		attributes.add("Gender");
		attributes.add("Ethnicity");
		/*
		 * Load Trait files 
		 */
		while(world_file.hasNext()){
			temp =world_file.next();
			attributes.add(temp);
			temp+=".txt";
			for(File i : trait_files){
				if(i.getName().equals(temp)){
					traits.add(new FileReader.TraitReader(i));
					break;
				}
			}
		}
	}
	/**
	 * generate a NPC
	 * @return a list of Strings containing all attributes of this NPC
	 */
	public List<String> generateCharacter(){
		List<String> toreturn = names.getResult();
		for (FileReader file : traits)
			toreturn.addAll(file.getResult());
		return toreturn;
	}
	
	public List<String> generateCharacter(String ethnicity) throws Exception{
		List<String> toreturn = names.GenerateSpecificNPC(ethnicity, true, true);
		for (FileReader file : traits)
			toreturn.addAll(file.getResult());
		return toreturn;
	}
	
	public List<String> generateCharacter(boolean Male, boolean Female) throws Exception{
		List<String> toreturn = names.GenerateSpecificNPC(Male, Female);
		for (FileReader file : traits)
			toreturn.addAll(file.getResult());
		return toreturn;		
	}
	
	public List<String> generateCharacter(String ethnicity, boolean Male, boolean Female) throws Exception{
		List<String> toreturn = names.GenerateSpecificNPC(ethnicity, Male, Female);
		for (FileReader file : traits)
			toreturn.addAll(file.getResult());
		return toreturn;		
	}
	
	
	/*
	public static void main(String[] args) throws FileNotFoundException{
		Generator world = new Generator(args[0]);
		for(int i = 0; i < 100; ++i){
			for(String str:world.generateCharacter())
				System.out.println(str);
			System.out.println("------------------------------");
		}
	}
	*/
	/****************************
	 * Helper methods for the GUI
	 ****************************/
	final public String[] EthnicList(){
		return names.EthnicList();
	}
	public JLabel[] traitLabels(){
		JLabel[] labels = new JLabel[attributes.size()];
		for(int i = 0; i < labels.length; ++i)
			labels[i] = new JLabel(attributes.get(i));
		return labels;
	}
}
