package mv2h.objects.meter;

import java.io.IOException;

public class Tatum implements Comparable<Tatum> {
	public final int time;

	public Tatum(int time) {
		this.time = time;
	}
	
	public String toString() {
		return "Tatum " + time;
	}

	@Override
	public int compareTo(Tatum o) {
		int result = Integer.compare(time, o.time);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}

	public static Tatum parseTatum(String line) throws IOException {
		String[] tatumSplit = line.split(" ");
		
		if (tatumSplit.length != 2) {
			throw new IOException("Error parsing Tatum: " + line);
		}
		
		int time;
		try {
			time = Integer.parseInt(tatumSplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Tatum. Time not recognised: " + line);
		}
		
		return new Tatum(time);
	}
}
