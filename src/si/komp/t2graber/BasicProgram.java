package si.komp.t2graber;

import org.joda.time.LocalDateTime;;

public class BasicProgram {
	LocalDateTime startTime;
	String programName;
	String programDesc;
	
	public BasicProgram(LocalDateTime startTime, String programName, String programDesc) {
		super();
		this.startTime = startTime;
		this.programName = programName;
		this.programDesc = programDesc;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public String getProgramName() {
		return programName;
	}

	public String getProgramDesc() {
		return programDesc;
	}
}
