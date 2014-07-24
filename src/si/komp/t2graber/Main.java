package si.komp.t2graber;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
	public static String t2Url = "http://tv.t-2.net/";
    public static void main( String[] args )
    {

    	String xmlTvOut = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE tv SYSTEM \"xmltv.dtd\"><tv source-info-url=\"http://komp.si/\" source-info-name=\"T-2 Shedule\" generator-info-name=\"XMLTV/$Id: tv_grab_t2\" generator-info-url=\"http://www.xmltv.org/\">\n";
    	ArrayList<ChanalProgram> list = parseAllChanals();

    	for (ChanalProgram chanalProgram : list) {
    		xmlTvOut += chanalProgram.getChannelXmlTvRecord();
		}

    	for (ChanalProgram chanalProgram : list) {
    		xmlTvOut += chanalProgram.getProgramingXmlTvFormat();
		}
		
    	xmlTvOut += "</tv>";
    	
		try {
			new PrintWriter("t-2.xml").print(xmlTvOut);;
			System.out.println("Done!");
		} catch (FileNotFoundException e) {
			System.out.println("Could not save the xml file");
			e.printStackTrace();
		}

    }
    
    
    static ArrayList<ChanalProgram> parseAllChanals(){
    	ArrayList<ChanalProgram> listOfChanals = new ArrayList<ChanalProgram>();
    	try{
        	Document doc = Jsoup.connect(Main.t2Url+"?funcName=TVCHANNELS").get();
        	Elements tableRows = null;
        	for (Element element : doc.select("table")) {
    			if(element.attr("width").equals("421")){ //simple way to find the table
    				tableRows = element.select("tr");
    			}
    		}
        	
        	for (Element element : tableRows) {
				String chName = "";
				String sheduleUrl = "";
				String imgUrl = "";
				
				chName = element.select("b").text();
				if(chName.equals("") || chName == null){
					continue; //filter errors
				}
				for (Element element2 : element.select("a")) {
					if(element2.attr("href").contains("?TVCHUID=")){
						sheduleUrl = element2.attr("href").replace("./", "");
						break;//we found the url
					}
				}
				if(sheduleUrl.equals("")){
					ChanalProgram prog = new ChanalProgram();
					prog.setIconUrl(imgUrl);
					prog.setTitle(chName);
					listOfChanals.add(prog);
					//this chanal has no shedule but we still need to create the Chanal for the name and image so cary on...
					continue;
				}
				imgUrl = element.select("img").get(0).attr("src");
				
				ChanalProgram prog = getChanalInfo(sheduleUrl, chName, imgUrl);
				listOfChanals.add(prog);
			}
    		
    	} catch (Exception e){
    		System.out.println("ERR: Unable to connect to master chanal list");
    		e.printStackTrace();
    	}
    	return listOfChanals;
    }
    
    
    static ChanalProgram getChanalInfo(String chUrl, String presumedChName, String presumedImgUrl){
    	try {
    		
    		LocalDateTime today = LocalDateTime.now();
    		ChanalProgram program = new ChanalProgram(); 
    		
    		//get the first day for the image and title
    		ChanalProgramForDay d = new ChanalProgramForDay(chUrl, presumedChName, presumedImgUrl, today);
    		program.setIconUrl(d.getIconUrl());
    		program.setTitle(d.getTitle());
    		program.addProgramList(d.getPrograming());
    		
    		//get the program for a week
    		for(int i=1;i<=7;i++){
    			d = new ChanalProgramForDay(chUrl, presumedChName, presumedImgUrl, today.plusDays(i));
    			program.addProgramList(d.getPrograming());
    		}
			
    		return program;
    		
		} catch (Exception e) {
			System.out.println("Err: Cant create chanal object");
			e.printStackTrace();
		}
    	return null;
    }
}
