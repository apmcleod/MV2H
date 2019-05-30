package mv2h.objects;

/**
 * An <code>MV2H</code> object represents the overall evaluation score of a transcription.
 * They are ordered by decreasing {@link #mv2h}, then {@link #multiPitch}, {@link #voice},
 * {@link #meter}, {@link #value}, and {@link #harmony}, in that order.
 * 
 * @author Andrew McLeod
 */
public class MV2H implements Comparable<MV2H> {
	/**
	 * The multi-pitch F-measure.
	 */
	public final double multiPitch;
	
	/**
	 * The voice F-measure.
	 */
	public final double voice;
	
	/**
	 * The metrical F-measure.
	 */
	public final double meter;
	
	/**
	 * The note value score.
	 */
	public final double value;
	
	/**
	 * The harmony score.
	 */
	public final double harmony;
	
	/**
	 * The overall evaluation score.
	 */
	public final double mv2h;
	
	/**
	 * Create a new MV2H object.
	 * 
	 * @param mp {@link #multiPitch}
	 * @param v {@link #voice}
	 * @param m {@link #meter}
	 * @param nv {@link #value}
	 * @param h {@link #harmony}
	 */
	public MV2H(double mp, double v, double m, double nv, double h) {
		multiPitch = mp;
		voice = v;
		meter = m;
		value = nv;
		harmony = h;
		
		mv2h = (multiPitch + voice + meter + value + harmony) / 5;
	}
	
	@Override
	public int compareTo(MV2H o) {
		int result = Double.compare(mv2h, o.mv2h);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(multiPitch, o.multiPitch);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(voice, o.voice);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(meter, o.meter);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(value, o.value);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(harmony, o.harmony);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Multi-pitch: " + multiPitch + "\n");
		sb.append("Voice: " + voice + "\n");
		sb.append("Meter: " + meter + "\n");
		sb.append("Value: " + value + "\n");
		sb.append("Harmony: " + harmony + "\n");
		sb.append("MV2H: " + mv2h);
		
		return sb.toString();
	}
}
