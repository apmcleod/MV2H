package mv2h.objects;

public class MV2H implements Comparable<MV2H> {
	public final double multiPitch;
	public final double voice;
	public final double meter;
	public final double value;
	public final double harmony;
	public final double mv2h;
	
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
