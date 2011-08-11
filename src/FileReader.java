/*
    NPCGenerator: A program for generating random NPC's for role-playing games
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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public interface FileReader {
	static final Random rand = new Random();

	public List<String> getResult();

	/**
	 * NameReader
	 * 
	 * @author hoekstr Reads names from a first name file and a last name file
	 */
	public class NameReader implements FileReader {
		public final String ethnicity;
		private File firstname;
		private File lastname;
		private final int firstnamesize;
		private final int lastnamesize;

		public NameReader(File firstname, File lastname)
				throws FileNotFoundException {
			this.firstname = firstname;
			this.lastname = lastname;
			ethnicity = firstname.getName().split("\\.")[0];
			// / Get number of lines in both files
			int temp = 0;
			Scanner scan = new Scanner(firstname);
			while (scan.hasNextLine()) {
				scan.nextLine();
				++temp;
			}
			firstnamesize = temp;
			if (firstnamesize < 1)
				System.err.println("Invalid first name file:"
						+ this.firstname.getName());
			if (lastname != null) {
				temp = 0;
				scan = new Scanner(lastname);
				while (scan.hasNextLine()) {
					scan.nextLine();
					++temp;
				}
				lastnamesize = temp;
				if (lastnamesize < 1)
					System.err.println("Invalid last name file:"
							+ this.lastname.getName());
			} else {
				lastnamesize = 0;
			}
		}

		@Override
		public List<String> getResult() {
			LinkedList<String> toreturn = new LinkedList<String>();
			try {
				/*
				 * Get first name and gender
				 */
				int result = rand.nextInt(firstnamesize);
				Scanner scan = new Scanner(firstname);
				String curr_name = "";
				String temp = "";
				while (result > 0) {
					temp = scan.nextLine();
					--result;
				}
				String[] result_line = temp.split("\t");
				curr_name += result_line[0];
				String gender = "ERROR";
				try {
					gender = result_line[1];
				} catch (Exception e) {
					gender = "ERROR";
				}
				if (gender.contains("m") && gender.contains("f"))
					gender = rand.nextBoolean() ? "Male" : "Female";
				else if (gender.contains("m"))
					gender = "Male";
				else
					gender = "Female";

				/*
				 * Get last name
				 */
				if (lastname != null) {
					result = rand.nextInt(lastnamesize);
					scan = new Scanner(lastname);
					temp = "";
					while (result > 0) {
						temp = scan.nextLine();
						--result;
					}
					curr_name += " " + temp;
				}
				toreturn.add(curr_name);
				toreturn.add(gender);
				toreturn.add(ethnicity);
				return toreturn;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			toreturn.add("BADNAME");
			return toreturn;
		}

	}

	/**
	 * TraitReader
	 * 
	 * @author hoekstr reads traits from a trait file
	 */
	public class TraitReader implements FileReader {
		private File file;
		private final int max;

		public TraitReader(File f) throws FileNotFoundException {
			file = f;
			Scanner scan = new Scanner(file);
			int temp = 0;
			while (scan.hasNextLine()) {
				String[] curr_line = scan.nextLine().split("\t");
				if (curr_line.length < 2)
					temp += 1;
				else
					temp += Integer.parseInt(curr_line[1]);
			}
			max = temp;
		}

		@Override
		public List<String> getResult() {
			int result = rand.nextInt(max);
			LinkedList<String> toreturn = new LinkedList<String>();
			try {
				Scanner scan = new Scanner(file);

				int curr_val = 0;
				while (scan.hasNextLine()) {
					String[] curr_line = scan.nextLine().split("\t");
					if (curr_line.length < 2)
						curr_val += 1;
					else
						curr_val += Integer.parseInt(curr_line[1]);
					if (result < curr_val) {
						toreturn.add(curr_line[0]);
						return toreturn;
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			toreturn.add("INVALID FILE: BUG");
			return toreturn;
		}
	}

	/**
	 * ProbList
	 * 
	 * @author hoekstr holds readers along with a probability. Used for
	 *         determining random ethnicity selections
	 */
	public class ProbList implements FileReader {
		private final LinkedList<FileReader> readers = new LinkedList<FileReader>();
		private final LinkedList<Integer> probabilities = new LinkedList<Integer>();
		private int max = 0;

		public void addReader(FileReader read, int prob) {
			readers.add(read);
			probabilities.add(prob + max);
			max += prob;
		}

		@Override
		public List<String> getResult() {
			int result = rand.nextInt(max);
			for (int i = 0; i < readers.size(); ++i)
				if (result < probabilities.get(i))
					return readers.get(i).getResult();
			return readers.peek().getResult();
		}

		public String[] EthnicList() {
			String[] toreturn = new String[readers.size()];
			int index = 0;
			for (FileReader i : readers)
				toreturn[index++] = ((NameReader) i).ethnicity;
			return toreturn;
		}

		public List<String> GenerateSpecificNPC(String ethnicity, boolean Male,
				boolean Female) throws Exception {
			for (FileReader i : readers) {
				if (((NameReader) i).ethnicity.equals(ethnicity)) {
					int cannotfind = 0;
					while (cannotfind++ < 200) {
						List<String> result = i.getResult();
						if (result.get(1).equals("Male") && Male)
							return result;
						else if (result.get(1).equals("Female") && Female)
							return result;
					}
				}
			}
			throw new Exception(
					"Cannot find specified traits (Gender/Ethnicity)");
		}

		public List<String> GenerateSpecificNPC(boolean Male, boolean Female) throws Exception {
			int cannotfind = 0;
			while (cannotfind++ < 200) {
				List<String> result = getResult();
				if (result.get(1).equals("Male") && Male)
					return result;
				else if (result.get(1).equals("Female") && Female)
					return result;
			}
			throw new Exception("Cannot find specified gender traits");
		}
	}
}
