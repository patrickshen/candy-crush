import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

/**  CandyCrush class
 *   Author: Patrick Shen
 */

public class CandyCrush {

	private final static int SIZE = 12;
	private final static int ADD_SCORE = 30;
	private final static int SCORESIZE = 5;
	private final static String CHOCOLATE = "bomb";
	private final static String SCOREFILE = "HighScore.txt";
	private static int score = 0;
	private static int movesLeft;
	private static String[][] shadowColors;
	private static Coordinate[] twoSelected = new Coordinate[2];
	private static Board b;
	private static Boolean[][] tobeDeleted;

	public static void main (String[] args) {
		String playerName;
		List<ScoreRecord> scoreRecords;
		String highScore;
		JOptionPane.showMessageDialog(null, "Welcome to CandyCrush!", "Candy Crush",JOptionPane.INFORMATION_MESSAGE);
		JOptionPane.showMessageDialog(null, "Instructions: \n Crush candies in chains of 3 or more.\n If you get a chain of 5, you recieve a chocolate. \n Chocolates can crush any other candy on the Board! \n You have 30 moves to score as high as you can. \n Good Luck!", "Instructions", JOptionPane.INFORMATION_MESSAGE);

		runGame();
		//Get player name entered by user
		playerName = JOptionPane.showInputDialog(null, "Your score is: " + score + "\nPlease enter your name:", "Enter name", JOptionPane.PLAIN_MESSAGE);

		while (playerName.contains(" ")) {
			playerName = JOptionPane.showInputDialog(null, "Your score is: " + score + "\nNo spaces please:", "Enter name", JOptionPane.PLAIN_MESSAGE);
		}
		if (playerName.equals(""))
			playerName = "ANON";

		//Get updated high score list
		scoreRecords = getHighRecordList(playerName, score);
		//Update score file
		writeHighScoreFile(scoreRecords);

		//Get high score string from score file
		highScore = readHighScoreFile();
		//Display high score list
		JOptionPane.showMessageDialog(null,highScore, "High Scores", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}

	/** begins a Candy Crush game */
	public static void runGame() {
		b = new Board (SIZE,SIZE);
		tobeDeleted  = new Boolean[SIZE][SIZE];
		shadowColors = new String[SIZE][SIZE];
		movesLeft = 30;
		firstLoadColors();
		//set tobeDeleted to false for entire board
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				tobeDeleted[row][col] = false;
			}
		}
		printOutToBoard();
		//game counter starts at 30, ends when counter reaches 0
		while (movesLeft > 0) {
			b.displayMessage("Score: " + score + "                " + "moves left: " + movesLeft);
			storeClicked();

			//switch candy location
			switchColors(twoSelected[0], twoSelected[1]);

			//candies must be beside each other, or else switch back
			if (!oneIsChocolate() && ((Math.abs(twoSelected[0].getRow() -twoSelected[1].getRow()) > 1 || Math.abs(twoSelected[0].getCol() -twoSelected[1].getCol()) > 1) ||
					!(twoSelected[0].getRow() == twoSelected[1].getRow() || twoSelected[0].getCol() == twoSelected[1].getCol()))) {
				switchColors(twoSelected[0], twoSelected[1]);
				movesLeft ++;
			}

			//result must be a chain, or else switch back
			else if (!hasChain() && !oneIsChocolate()) {
				Wait(400);
				switchColors(twoSelected[0], twoSelected[1]);
				movesLeft ++;
			}

			if (oneIsChocolate()) {
				//if both is chocolate, and you didn't select itself, or both is chocolate and beside each other, delete all
				if (bothIsChocolate() && (twoSelected[0].getRow() != twoSelected[1].getRow() && twoSelected[0].getCol() != twoSelected[1].getCol())
						|| bothIsChocolate() && (twoSelected[0].getRow() != twoSelected[1].getRow() && twoSelected[0].getCol() == twoSelected[1].getCol())
						|| bothIsChocolate() && (twoSelected[0].getRow() == twoSelected[1].getRow() && twoSelected[0].getCol() != twoSelected[1].getCol())) {
					deleteAll();
				}
				//if you selected itself, undo
				else if (twoSelected[0].getRow() == twoSelected[1].getRow() && twoSelected[0].getCol() == twoSelected[1].getCol()) {
					switchColors(twoSelected[0], twoSelected[1]);
					movesLeft ++;
				}
				//if only one is chocolate
				else {
					handleChocolate();
					Wait(400);
					loadMore();
				}
			}

			//if valid move, continue deleting applicable chains
			while (hasChain()) {
				deleteApplicable();
			}
			movesLeft --; 
		}
		b.displayMessage("Game Over. Final score: " + score);		
	}

	/** makes the game sleep for a given amount of time */
	public static void Wait(int x){
		try {
			Thread.sleep(x);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** removes candy from both the board class and the array of strings */
	public static void deleteAndSync(int row, int col) {
		shadowColors[row][col] = null;
		b.removeCandy(row, col);
		score += ADD_SCORE;
	}

	/** deletes all candies from board*/
	public static void deleteAll() {
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				deleteAndSync(row, col);

				loadMore();
			}
			Wait(80);
		}
	}

	/** creates a new game board, ensuring that chains do not occur */
	public static void firstLoadColors() {
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				do {
					shadowColors[row][col] = getCandy((int) (Math.random()*5 + 1)); //generates random color
				} while (rowLineLength(row, col)>2 || columnLineLength(row, col)>2); //keep doing to prevent chain of three on load
			}
		}	
	}

	/** deletes all candies of the color that a chocolate is switched with */
	public static void handleChocolate () {
		if (shadowColors[twoSelected[0].getRow()][twoSelected[0].getCol()] == CHOCOLATE) {
			//sets color to be deleted
			String deleteColor = shadowColors[twoSelected[1].getRow()][twoSelected[1].getCol()];
			for (int row = 0; row < SIZE; row ++) {
				for (int col = 0; col < SIZE; col ++) {
					if (shadowColors[row][col] == deleteColor) {
						deleteAndSync(row, col);
					}
				}
			}
			deleteAndSync(twoSelected[0].getRow(), twoSelected[0].getCol());
		} else if (shadowColors[twoSelected[1].getRow()][twoSelected[1].getCol()] == CHOCOLATE) {
			String deleteColor = shadowColors[twoSelected[0].getRow()][twoSelected[0].getCol()];
			for (int row = 0; row < SIZE; row ++) {
				for (int col = 0; col < SIZE; col ++) {
					if (shadowColors[row][col] == deleteColor) {
						deleteAndSync(row, col);
					}
				}
			}
			deleteAndSync(twoSelected[1].getRow(), twoSelected[1].getCol());
		}
	}

	/** checks if one of the two selected is a chocolate */
	public static boolean oneIsChocolate () {
		if (shadowColors[twoSelected[0].getRow()][twoSelected[0].getCol()] == CHOCOLATE || shadowColors[twoSelected[1].getRow()][twoSelected[1].getCol()] == CHOCOLATE)
			return true;
		return false;
	}

	/** checks if both of the two selected are chocolates */
	public static boolean bothIsChocolate () {
		if (shadowColors[twoSelected[0].getRow()][twoSelected[0].getCol()] == CHOCOLATE & shadowColors[twoSelected[1].getRow()][twoSelected[1].getCol()] == CHOCOLATE)
			return true;
		return false;
	}

	/** stores the locations of the two clicked candies */
	public static void storeClicked() {
		for (int i = 0; i < 2; i ++) {
			int row = 0, col = 0;
			Coordinate loc;

			loc = b.getClick();
			row = loc.getRow();
			col = loc.getCol();

			b.removeCandy(row, col);
			b.putCandy("black", row, col);
			twoSelected[i] = (loc);
		}
	}

	/** marks candies in chains of 3 or more for deletion, and then deletes them */
	public static void deleteApplicable() {
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				if (rowLineLength (row, col) > 2)
					storeRowChain(row, col);
				if (columnLineLength (row, col) > 2)
					storeColumnChain(row, col);
			}
		}
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				if (tobeDeleted[row][col] == true) {
					deleteAndSync(row, col);
				}
			}
		}
		Wait(200); //pause before loading more
		//load more pegs 
		loadMore();
		//prevent additional deletions
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				tobeDeleted[row][col] = false;
			}
		}
	}

	/** fills in empty spaces on the board by dropping down new candies */
	public static void loadMore() {
		boolean isAllFilled = false;
		while (!isAllFilled) {
			boolean isAnyNotFilled = false;
			//loop through all cells
			for (int row = 0; row < SIZE; row ++) {
				for (int col = 0; col < SIZE; col ++) {
					//if cell being examined is empty
					if (shadowColors[row][col] == null) {
						isAnyNotFilled = true;
						//if its the first row, create a random candy
						if (row == 0)
							shadowColors[row][col] = getCandy((int) (Math.random()*5 + 1));
						//otherwise drop down existing candies
						else if (row != 0 && shadowColors[row-1][col] != null) {
							shadowColors[row][col] = shadowColors[row-1][col];
							shadowColors[row-1][col] = null;

						}
					}
				}
			}
			if (!isAnyNotFilled)
				isAllFilled = true;
		}
		printOutToBoard();
	}

	/** prints out Candies on to board */
	public static void printOutToBoard() {
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				b.putCandy(shadowColors[row][col], row, col);
			}
		}
	}

	/** checks if there is a chain of 3 or more */
	public static boolean hasChain () {
		for (int row = 0; row < SIZE; row ++) {
			for (int col = 0; col < SIZE; col ++) {
				if (columnLineLength (row, col) > 2 || rowLineLength (row, col) > 2)
					return true;
			}
		}
		return false;
	}

	/**store vertical chains for deletion*/
	public static void storeColumnChain(int row, int col) {
		String colorToBeDeleted = shadowColors[row][col];
		int checkRow = row;
		int length = 1;
		if (colorToBeDeleted != CHOCOLATE) {
			while (sameStringIsHere(colorToBeDeleted, checkRow - 1, col)){
				tobeDeleted[checkRow][col] = true;
				//go on to next candy
				checkRow --; 
				length ++;
			}
			//reset to center of chain
			checkRow = row;
			while (sameStringIsHere(colorToBeDeleted, checkRow+1, col)) {
				tobeDeleted[checkRow][col] = true;
				// go on to next candy
				checkRow ++;
				length ++;
			}
			tobeDeleted[checkRow][col] = true;
		}
		//add a chocolate when a chain greater than 4 is formed
		if (length >=5) {
			tobeDeleted[row+2][col] = false;
			shadowColors[row+2][col] = CHOCOLATE;
			b.putCandy(CHOCOLATE, row+2, col);
		}
	}

	/**store horizontal chains for deletion*/
	public static void storeRowChain(int row, int col) {
		String colorToBeDeleted = shadowColors[row][col];
		int checkCol = col;
		int length = 1;
		if (colorToBeDeleted != CHOCOLATE) {
			while (sameStringIsHere(colorToBeDeleted, row, checkCol - 1)){
				tobeDeleted[row][checkCol] = true;
				checkCol --;
				length ++;
			}
			//reset to center
			checkCol = col;
			while (sameStringIsHere(colorToBeDeleted, row, checkCol +1)) {
				tobeDeleted[row][checkCol] = true;
				checkCol ++;
				length ++;
			}
			tobeDeleted[row][checkCol] = true;
		}
		if (length >=5) {
			tobeDeleted[row][col+2] = false;
			shadowColors[row][col+2] = CHOCOLATE;
			b.putCandy(CHOCOLATE, row, col+2);
		}
	}

	/** switch the locations of two candies when selected */
	public static void switchColors(Coordinate first ,Coordinate second) {
		int firstRow = first.getRow();
		int firstCol = first.getCol();

		int secRow = second.getRow();
		int secCol = second.getCol();

		String colorOne = shadowColors[firstRow][firstCol];
		String colorTwo = shadowColors[secRow][secCol];

		//the switch
		b.putCandy(colorOne, secRow, secCol);
		b.putCandy(colorTwo, firstRow, firstCol);
		shadowColors[firstRow][firstCol] = colorTwo;
		shadowColors[secRow][secCol] = colorOne;
	}		

	/** returns the length of a horizontal chain */
	public static int rowLineLength(int row, int col) {
		int leftLength = 0, rightLength = 0;
		int totalLength;
		String s = shadowColors[row][col];
		//start from given cell and check how far left line extends
		int checkCol = col;
		while (sameStringIsHere(s, row, checkCol - 1)){
			leftLength ++;
			checkCol --;
		}
		//reset, check how far right line extends
		checkCol = col;
		while (sameStringIsHere(s, row, checkCol +1)) {
			rightLength++;
			checkCol ++;
		}
		totalLength = leftLength + rightLength + 1;
		return totalLength;
	}	

	/** returns the length of a vertical chain */
	public static int columnLineLength(int row, int col) {

		int topLength=0,bottomLength=0;
		int totalLength;
		String s = shadowColors[row][col];
		//start from given cell and check how high line extends
		int checkRow = row;
		while (sameStringIsHere(s, checkRow-1, col)) {
			topLength ++;
			checkRow --;
		}
		//reset, check how low line extends
		checkRow = row;
		while (sameStringIsHere(s, checkRow+1, col)) {
			bottomLength ++;
			checkRow ++;
		}
		totalLength = topLength+bottomLength+1;
		return totalLength;
	}

	/** checks if a given string exists in a given cell */
	public static Boolean sameStringIsHere(String s, int row, int col) {
		//prevent indexoutofbounds errors
		if (s == null)
			return false;
		if (col < 0 || col >= SIZE || row <0 || row >= SIZE)
			return false;
		//check if exists in row
		if (shadowColors[row]==null)
			return false;
		//if it does, check if exists in cell
		if (shadowColors[row][col] == null)
			return false;
		//if it does, check if is the same
		return s.equals(shadowColors[row][col]);
	}

	/** returns a string for a given int */
	public static String getCandy (int i) {
		String c = "";
		switch (i) {
		case 1: c= "blue";break; 
		case 2: c= "green";break; 
		case 3: c= "orange";break; 
		case 4: c= "purple";break; 
		case 5: c= "red"; break;
		}
		return c;
	}

	/** reads highscore strings from highscore file and sorts in descending order */
	public static  List<ScoreRecord> getHighRecordList (String name, int score){
		List<ScoreRecord> list = new ArrayList<ScoreRecord>();
		File scoreFile = new File(SCOREFILE);
		BufferedReader reader = null;
		String line=null;
		try {
			//create file if file doesn't exist
			if (!(scoreFile.exists()))
				scoreFile.createNewFile();
			reader = new BufferedReader(new FileReader(SCOREFILE));
			//Read record line in file and add the record to the list
			while((line= reader.readLine())!=null) {
				String[] arr =line.split("\\s+");
				ScoreRecord obj = new ScoreRecord(arr[0],Integer.parseInt(arr[1]));
				list.add(obj); 
			}
			reader.close();
		} catch (IOException e) {
			System.exit(1);
		}
		//Add score of new player to the list
		ScoreRecord obj = new ScoreRecord(name, score);
		list.add(obj);
		//Sort list in score descending order
		Collections.sort(list, Collections.reverseOrder(ScoreRecord.SCORE_COMPARATOR));
		//Remove the lowest score from the list
		if(list.size() > SCORESIZE ) 
			list.remove(list.size()-1);
		return list;
	}

	/** records high scores in highscore file */
	public static void writeHighScoreFile(List<ScoreRecord> scoreRecords){
		Writer writer = null;
		//Create a new blank score file
		File scoreFile = new File(SCOREFILE);
		if (scoreFile.exists()) 
			scoreFile.delete();

		try {
			scoreFile.createNewFile();
			writer = new FileWriter(SCOREFILE, true);
		}
		catch(IOException ex) {
			System.exit(1);
		}
		//Write each record to the score file
		PrintWriter output = new PrintWriter(writer);
		for(ScoreRecord record : scoreRecords ){
			output.println(record.getName() + "   " + record.getScore());
		}
		output.close();
	}

	/** reads highscore strings from highscore file and returns high scores */
	public static String readHighScoreFile () {
		String highScore = "";
		BufferedReader reader;
		List<String> list = new ArrayList<String>();
		String line=null;
		String[] highScores;
		try{
			reader = new BufferedReader(new FileReader(SCOREFILE));
			//Concatenate scores in score file as a string to be returned
			while((line= reader.readLine())!=null) {
				highScores =line.split("\\s+");
				highScore = highScore + highScores[0] +  ": " + highScores[1] + "\n";
			}
			list.clear();
			reader.close();
		}
		catch(IOException ex) {
			System.exit(1);
		}
		return highScore;
	}	
}
