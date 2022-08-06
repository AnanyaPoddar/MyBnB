package mybnb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// TODO https://gist.github.com/johnmiedema/e12e7359bcb17b03b8a0 CREDIT 
// TODO What should this actually print

//extract noun phrases from a single sentence using OpenNLP
public class NounParser {

	static String sentence = "Who is the author of The Call of the Wild?";
	static List<String> nounPhrases = new ArrayList();
	
	public static void parser (Connection conn, int listID) {

		InputStream modelInParse = null;
		String[] reviews;
		int count = 0;

		// Retrieve all the reviews of listID

		try {
            Statement stmt = conn.createStatement();

			String getCount= String.format("SELECT  count(listID) as count FROM rentersReviewListings " 
			+ "WHERE listID = %d;", listID);
            ResultSet rsCount = stmt.executeQuery(getCount);
			if(rsCount.next()){
				count = rsCount.getInt("count");
			}

			reviews = new String[count];
            String getReviews = String.format("SELECT  * FROM rentersReviewListings " 
			+ "WHERE listID = %d;", listID);
            ResultSet rs = stmt.executeQuery(getReviews);
			count = 0;
            while(rs.next()){
                System.out.print("listID: " + rs.getInt("listID"));
                System.out.println(", Review: " + rs.getString("review"));
				reviews[count] = rs.getString("review");
				count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
			return;
        }

		// If no reviews, exit
		System.out.println(count);
		if (count == 0){
			System.out.println("No reviews for this listID");
			return;
		}


		try {
			//load chunking model
			modelInParse = new FileInputStream("en-parser-chunking.bin"); //from http://opennlp.sourceforge.net/models-1.5/
			ParserModel model = new ParserModel(modelInParse);

			//create parse tree
			Parser parser = ParserFactory.create(model);

			// allParsed contains every review after being parsed
			List<Parse []> allParsed = new ArrayList();

			// Parse the first review and add it too allParsed	
			Parse topParses[] = ParserTool.parseLine(reviews[0], parser, 1);
			allParsed.add(topParses);

			// Parse every review + add it to allParsed
			for(int i = 1; i < count; i++){
				topParses = ParserTool.parseLine(reviews[i], parser, 1);
				allParsed.add(topParses);
			}
			
			// For every parsed review, getNounPhrases for EACH Parse in EACH parsed review
			//call subroutine to extract noun phrases
			for (Parse [] indivialReview : allParsed){
				for (Parse p : indivialReview){
					getNounPhrases(p);
					// System.out.println("Was called");
				}
			}
			allParsed.removeAll(allParsed);
			
			// nounPhrases is a set containing every noun phrase in all reviews for listID

			//print noun phrases
			// for (String s : nounPhrases)
			//     System.out.println(s);

		}
		catch (IOException e) {
		  e.printStackTrace();
		  return;
		}

		// 
		try {
			Statement statement = conn.createStatement();

			// delete anything from previous calls to the parser
			String npView = "DELETE FROM npReviews;";
			statement.executeUpdate(npView); 

			// Insert each noun phrase into the npView
			String npInsert = "INSERT INTO npReviews VALUES ";
			for (String s : nounPhrases)
				npInsert += String.format("('%s'), ", s);
			npInsert = npInsert.substring(0, npInsert.length()-2) + ";";
			// System.out.println(npInsert);
			statement.executeUpdate(npInsert);

			// Count each noun phrases + Display the noun phrases + their count ordered by count 
			String countNP = String.format("select *, count(nounPhrase) as count from npReviews GROUP BY nounPhrase ORDER BY count(nounPhrase) DESC;");
            ResultSet rs = statement.executeQuery(countNP);
            while(rs.next()){
                System.out.print("Frequency: " + rs.getInt("count"));
				System.out.println(", Noun Phrase: " + rs.getString("nounPhrase"));
                
            }			
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }

		finally {
			nounPhrases.removeAll(nounPhrases);
		  if (modelInParse != null) {
		    try {
		    	modelInParse.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
	
	//recursively loop through tree, extracting noun phrases
	public static void getNounPhrases(Parse p) {
			
	    if (p.getType().equals("NP")) { //NP=noun phrase
	         nounPhrases.add(p.getCoveredText());
	    }
	    for (Parse child : p.getChildren())
	         getNounPhrases(child);
	}
}
