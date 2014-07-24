package si.komp.t2graber;

import java.util.ArrayList;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ChanalProgramForDay {	
	
	private Document doc = null;
	private LocalDateTime date = null;
	private String iconUrl = null;
	private String title = null;
	private ArrayList<BasicProgram> programing = null;
	
	public ChanalProgramForDay(String chUrl, String presumedChName, String presumedIconUrl, LocalDateTime wantedDate) throws Exception{
		
		//format used for date and time in the request &date=20140725
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		chUrl+="&date="+wantedDate.toString(formatter);
		
		//trying to dl the page
		try{
        	doc = Jsoup.connect(Main.t2Url+chUrl).get();
    	} catch (Exception ex) {
    		System.out.println("ERR: Unable to connect to page "+chUrl);
    		throw new Exception("ERR: Unable to connect to page "+chUrl);
    	}
		
		//EROOR CHECKS and default values
		if(presumedChName == null || presumedChName.equals("")){
			presumedChName = "Unknown";
			System.out.println("ERR: Cant get chanal name");
		}
		if(wantedDate == null || wantedDate.isBefore(LocalDateTime.now().minusDays(1))){
			wantedDate = LocalDateTime.now();
			System.out.println("ERR: Wanted date is incorrect");
		}
		if(presumedIconUrl == null || presumedIconUrl.equals("")){
			presumedIconUrl = "IMGTV/NoImage_50x50.gif";
		}
		
		//get the date from the page
    	title = getTitle(presumedChName);
    	iconUrl = imageUrl(presumedIconUrl);
    	date = date(wantedDate);
    	programing = generatePrograms();
	}
	
    private ArrayList<BasicProgram> generatePrograms(){

    	//we find try to find the table containg the shedule and save it in tableRows
    	Elements tableRows = null;
    	for (Element element : doc.select("table")) {
			if(element.attr("width").equals("421")){ //simple way to find the table
				tableRows = element.select("tr");
				continue;
			}
		}
    	
    	//if the shedule data was not found
    	if(tableRows == null){
    		//for this day we have no shedule
    		return new ArrayList<BasicProgram>();
    	}

    	ArrayList<BasicProgram> basicList = new ArrayList<BasicProgram>();
    	for (Element element : tableRows) {
    		LocalDateTime startTimeDate = null;
    		String title = "";
    		String desc = "";
    		try{
    			String startTime = element.select("tt").get(0).text(); //the start time HH:mm format
    			
    			DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        		startTimeDate = formatter.parseLocalDateTime(startTime);
        		startTimeDate = date.plusMillis(startTimeDate.getMillisOfDay()); //add date and time
    		} catch (Exception e){
    			System.out.println("WAR: Start time not found"); //if the time is not found we ignore this one
    			continue;
    		}
    		
    		try{
    			title = element.select("b").get(0).text(); //get the program title
    		} catch (Exception e){
    			System.out.println("ERR: Title text not found!");
    			title = "Unknown";
    		}
    		
    		try{
    			desc = element.select(".small-text").get(0).text(); //this is the description
    		} catch (Exception e){
    			desc = ""; // some chanals just dont hace descriptions
    		}
    		
    		//just to make sure we got the correct time
    		if(startTimeDate==null || startTimeDate.isBefore(date) || startTimeDate.isAfter(date.plusDays(30))){
    			System.out.println("ERR: Could not parse the chanals time! Or the time range is wrong.");
    			continue;
    		}
    		//make the basic program and add it to the list
			BasicProgram prog = new BasicProgram(startTimeDate, title, desc);
			basicList.add(prog);
		}
    	return basicList;
    }
    
    private LocalDateTime date(LocalDateTime presumedDate){
    	//get the date of the programing
    	try{
        	String dateText = doc.select(".CMSSuperTitle").get(0).text();
        	dateText = dateText.replace("Spored za dan: ", ""); //strip the bs
        	DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");
        	return formatter.parseLocalDateTime(dateText);
    	} catch (Exception e) {
    		return presumedDate;//someting went wrong use the date used for the url
    	}
    }
    
    private String imageUrl(String presumedImgUrl){
    	//find the img of the chanal
    	Elements allImgs = doc.select("img");
    	for (Element element : allImgs) {
			if(element.attr("src").startsWith("IMGTV") && element.attr("align").equals("right")){
				return Main.t2Url+element.attr("src");
			}
		}
    	return Main.t2Url+presumedImgUrl; // couldent find it use the one from the index
    }
    
    private String getTitle(String presumedName){
    	//get chanal title
    	try{
    		return doc.select(".ParaTitle").get(0).text();
    	} catch (Exception e){
    		return presumedName; //chanal title not found
    	}
    	
    }
    
	public LocalDateTime getDate() {
		return date;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getTitle() {
		return title;
	}

	public ArrayList<BasicProgram> getPrograming() {
		return programing;
	}
}
