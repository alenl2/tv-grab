package si.komp.t2graber;

import java.util.ArrayList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ChanalProgram {
	private ArrayList<BasicProgram> programing = new ArrayList<BasicProgram>();
	private String iconUrl = null;
	private String title = null;
	private String chID = null;
	
	public ChanalProgram() {
	}
	public void addProgramList(ArrayList<BasicProgram> pr){
		programing.addAll(pr);
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		this.chID = title.replace(" ", "");
	}
	
	public String getChannelXmlTvRecord(){
		String chRecord = "<channel id=\"%s\"><display-name>%s</display-name><icon src=\"%s\"/></channel>";
		chRecord = String.format(chRecord, chID, title, iconUrl);
		return chRecord;
	}
	public String getProgramingXmlTvFormat(){
		String ret = "";
		String xmlTvFormat = "<programme channel=\"%s\" start=\"%s\" stop=\"%s\"><title>%s</title><desc>%s</desc></programme>";
		DateTimeFormatter timeStampFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
		
		for (int i=0;i<programing.size()-1;i++) {
			BasicProgram basicProgram = programing.get(i);
			String startTimeString = basicProgram.getStartTime().toString(timeStampFormat);
			String endTimeString = programing.get(i+1).getStartTime().toString(timeStampFormat);														//todo!!!!
			ret+=String.format(xmlTvFormat, chID, startTimeString, endTimeString, basicProgram.getProgramName(), basicProgram.getProgramDesc());
			ret+="\n";
		}
		return ret;
	}
}
