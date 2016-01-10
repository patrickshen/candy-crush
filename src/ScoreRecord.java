import java.util.Comparator;

/**  Score record class
 *   Author: Patrick Shen
 *   Created for CandyCrush
 */
public class ScoreRecord {

	private String name; 
	private int score;
	/** sorts ScoreRecords by score */
	public static final Comparator<ScoreRecord> SCORE_COMPARATOR =	new Comparator<ScoreRecord>() {
		public int compare(ScoreRecord s1, ScoreRecord s2) {
			Integer score1 = s1.getScore();
			Integer score2 = s2.getScore();
			return score1.compareTo(score2);
		}
	};


	/** Constructor */
	public ScoreRecord (String inName, int inScore) {
		name = inName;
		score = inScore;
	}
	/** returns name */
	public String getName () {
		return name;
	}
	/** returns score */
	public int getScore (){
		return score;
	}

}



