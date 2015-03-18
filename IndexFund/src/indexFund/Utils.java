package indexFund;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Utils {

	public static Utils instance = null;

	// public static int nombreAppelReadFileI = 0;

	private Utils() {
	}

	// public static final Utils getInstance() {
	// if (Utils.instance == null) {
	// Utils.instance = new Utils();
	// }
	// return Utils.instance;
	// }

	private static List<String> readFile(String fichier) throws IOException {

		List<String> result = new ArrayList<String>();

		FileReader fr = new FileReader(fichier);
		BufferedReader br = new BufferedReader(fr);

		for (String line = br.readLine(); line != null; line = br.readLine()) {
			result.add(line);
		}

		br.close();
		fr.close();

		return result;
	}

	/**
	 * @deprecated
	 * @param fichier
	 * @param i
	 * @return
	 * @throws IOException
	 */
	private static String readFile(String fichier, int i) throws IOException {

		String result = "";

		FileReader fr = new FileReader(fichier);
		BufferedReader br = new BufferedReader(fr);

		int noLigne = 0;

		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (noLigne == i) {
				result = line;
				break;
			} else {
				i++;
			}
		}

		br.close();
		fr.close();

		return result;
	}

	public final static List<String[]> getCorrelations(String fichier,
			String csvSpliter) {
		List<String> lines;
		List<String[]> correlations = null;

		try {
			if (correlations == null) {
				lines = readFile(fichier);
				correlations = new ArrayList<String[]>(lines.size());
				for (String line : lines) {
					String[] oneData = line.split(csvSpliter);
					correlations.add(oneData);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return correlations;
	}

	/**
	 * 
	 * @deprecated
	 * @param fichier
	 * @param csvSpliter
	 * @param i
	 * @return
	 */
	public final static List<String> getCorrelationsLigneI(String fichier,
			String csvSpliter, int i) {
		List<String> correlationsLigneI = new ArrayList<String>();

		String line = "";

		try {
			line = readFile(fichier, i);
			correlationsLigneI.clear();
			String[] oneData = line.split(csvSpliter);
			for (int j = 0; j < oneData.length; j++) {
				correlationsLigneI.add(oneData[j]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return correlationsLigneI;
	}

	public final static List<List<Double>> generateRandomCorrelations(int n) {
		List<List<Double>> correlations = new ArrayList<List<Double>>();

		// Initialisation de la liste de listes :
		for (int i = 0; i < n; i++) {
			correlations.add(new ArrayList<Double>(n));
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) {
					correlations.get(i).add(j, new Double(1.0));
				} else if (i < j) {
					Random randomGeneratory = new Random();
					double randomNumber = randomGeneratory.nextDouble();

					correlations.get(i).add(j, new Double(randomNumber));
					correlations.get(j).add(i, new Double(randomNumber));
				}
			}
		}
		return correlations;
	}

	public final static String writeCorrelationsInFile(int n) {

		String fileName = "Correlations_n_" + n + "_"
				+ new Timestamp(System.currentTimeMillis()).getTime() + ".csv";
		try {
			FileWriter fw = new FileWriter(fileName);

			List<List<Double>> correlations = generateRandomCorrelations(n);

			for (List<Double> correlationsI : correlations) {
				for (Double correlationsIJ : correlationsI) {
					fw.append(correlationsIJ + ";");
				}
				fw.append("\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}

}
