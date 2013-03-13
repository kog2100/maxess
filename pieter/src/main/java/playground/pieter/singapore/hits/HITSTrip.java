package playground.pieter.singapore.hits;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.management.timer.Timer;

import org.matsim.api.core.v01.Coord;

public class HITSTrip implements Serializable{
	HITSPerson person;
	String h1_hhid;
	int pax_id;
	int trip_id;
	int p13d_origpcode;
	int t2_destpcode;
	Date t3_starttime_24h;
	Date t4_endtime_24h;
	String t5_placetype;
	String t6_purpose;
	double t22_lastwlktime;
	int t23_tripfreq;
	int t24_compjtime;
	int t25_estjtime;
	String t27_flextimetyp;
	String p28a_fastrbypt;
	String p28b_cheaprbypt;
	String p28c_easrtoacc;
	String p28d_needntpark;
	String p28e_lessstress;
	String p28f_othrmmbrusecar;
	String p28g_opconly;
	String p28h_envfrndly;
	String p28i_othrreasnchck;
	double tripfactorsstgfinal;
	private ArrayList<HITSStage> stages;
	
	DateFormat dfm;
	
	public HITSTrip() {
		
	}
	public HITSTrip(ResultSet trs, Connection conn, DateFormat dfm, HITSPerson person) {
		// TODO Auto-generated constructor stub
		try {
			this.person = person;
			this.h1_hhid = trs.getString("h1_hhid");
			this.pax_id = trs.getInt("pax_id");
			this.trip_id = trs.getInt("trip_id");
			this.p13d_origpcode = trs.getInt("p13d_origpcode");
			this.t2_destpcode = trs.getInt("t2_destpcode");
			this.t3_starttime_24h = (Date) trs.getObject("t3_starttime_24h");
			this.t4_endtime_24h = (Date) trs.getObject("t4_endtime_24h");
			this.t5_placetype = trs.getString("t5_placetype");
			this.t6_purpose = trs.getString("t6_purpose");
			this.t22_lastwlktime = trs.getInt("t22_lastwlktime");
			this.t23_tripfreq = trs.getInt("t23_tripfreq");
			this.t24_compjtime = trs.getInt("t24_compjtime");
			this.t25_estjtime = trs.getInt("t25_estjtime");
			this.t27_flextimetyp = trs.getString("t27_flextimetyp");
			this.p28a_fastrbypt = trs.getString("p28a_fastrbypt");
			this.p28b_cheaprbypt = trs.getString("p28b_cheaprbypt");
			this.p28c_easrtoacc = trs.getString("p28c_easrtoacc");
			this.p28d_needntpark = trs.getString("p28d_needntpark");
			this.p28e_lessstress = trs.getString("p28e_lessstress");
			this.p28f_othrmmbrusecar = trs.getString("p28f_othrmmbrusecar");
			this.p28g_opconly = trs.getString("p28g_opconly");
			this.p28h_envfrndly = trs.getString("p28h_envfrndly");
			this.p28i_othrreasnchck = trs.getString("p28i_othrreasnchck");
			this.tripfactorsstgfinal = trs.getDouble("tripfactorsstgfinal");
			this.dfm = dfm;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setStages(conn);
		calcTransients();

	}


	
	public void setStages(Connection conn) {
		ArrayList<HITSStage> s = new ArrayList<HITSStage>();
		Statement ss;
		try {
			ss = conn.createStatement();
			ss.executeQuery("select " +
					"msno_main,"+
					"h1_hhid,"+
					"pax_id,"+
					"trip_id,"+
					"stage_id,"+
					"t10_mode,"+
					"t10a_walktime,"+
					"t10i_modeother,"+
					"t11_boardsvcstn,"+
					"t12_alightstn,"+
					"t12a_paxinveh,"+
					"t13_waittime,"+
					"t14_invehtime,"+
					"t15_taxifare,"+
					"t16_taxireimb,"+
					"t17_erpcost,"+
					"t18_erpreimb,"+
					"t19_parkfee,"+
					"t19a_parkftyp,"+
					"t20_parkreimb"+
					" from hits.hitsshort where " +
					"h1_hhid = '"+ this.h1_hhid +
					"' and pax_id = " + this.pax_id + 
					" and trip_id = " + this.trip_id + ";");
			ResultSet srs = ss.getResultSet();
			while (srs.next()) {
				s.add(new HITSStage(srs,this));
			}
			srs.close();
			ss.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		s.trimToSize();
		this.stages = s;
	}

	public ArrayList<HITSStage> getStages() {
		return stages;
	}
	
//	 (calculated) fields
	 boolean transientsCalculated;
	
	 String mainmode;
	 String stageChainFull; //i.e. walk - wait - bus - walk - wait - mrt - walk
	 String stageChainSimple; // i.e. bus - mrt
	 String stageChainTransit;
	
	 int totalWalkTimeTrip;
	 int transitWalkTimeTrip;
	 int numberOfWalkStagesTrip;
	 int transitWaitTimeTrip;
	
	 int inVehTimeTrip;
	 int inVehTimeTransit;
	
	 int transitJourneyTimeBetweenFirstBoardAndLastAlight;
	 int calculatedJourneyTime;
	 int estimatedJourneyTime;
	 int subjTimeError;
	 Date transitStartTime;
	 Date transitEndTime;
	 
	 double busDistance;
	 double trainDistance;
	 double busTrainDistance;
	private double freeSpeedCarJourneyDistance;
	public double straightLineDistance;
	
	 static public final String FORMATSTRING = "%s,%d,%d," +
			"%s,%s,%s,%s," +
			"%d,%d,%d,%d," +
			"%d,%d," +
			"%d,%d,%d,%d,%s,%s," +
			"%f,%f,%f,%f"+
			"\n"; 
	 static public final String HEADERSTRING = 
		"h1_hhid,pax_id,trip_id" +
		
		",mainmode"+
		",stageChainFull"+
		",stageChainSimple"+
		",stageChainTransit"+

		",totalWalkTimeTrip"+
		",transitWalkTimeTrip"+
		",numberOfWalkStagesTrip"+
		",transitWaitTimeTrip"+

		",inVehTimeTrip"+
		",inVehTimeTransit"+

		",transitJourneyTimeBetweenFirstBoardAndLastAlight"+
		",calculatedJourneyTime"+
		",estimatedJourneyTime"+
		",subjTimeError"+
		",transitStartTime"+
		",transitEndTime"+
		",busDistance"+
		",trainDistance"+
		",busTrainDistance"+
		",freeSpeedCarJourneyDistance"+
		"\n";
	

	public String toString(){
		if(!transientsCalculated) calcTransients(); //ensures you don't run this unnecessarily
		return String.format(this.FORMATSTRING, 
				h1_hhid, pax_id, trip_id 
				
				,mainmode
				,stageChainFull 
				,stageChainSimple 
				,stageChainTransit

				,totalWalkTimeTrip
				,transitWalkTimeTrip
				,numberOfWalkStagesTrip
				,transitWaitTimeTrip

				,inVehTimeTrip
				,inVehTimeTransit

				,transitJourneyTimeBetweenFirstBoardAndLastAlight
				,calculatedJourneyTime
				,estimatedJourneyTime
				,subjTimeError
				,transitStartTime != null ? this.dfm.format(transitStartTime) : null
				,transitEndTime   != null ? this.dfm.format(transitEndTime)   : null
				,busDistance,trainDistance,busTrainDistance,freeSpeedCarJourneyDistance
		);
	}
	public void calcTransients() {
		int longest = 0; // variable to mark the longest stage
		String mm = null; //main mode of the trip
		String modeChain = ""; //motorised modes used in order
		String transitModeChain = "";
		
		int walkTimeTrip = 0; //total walk time for the trip, including last egress
		int walkTimeTransit = 0; //total walk time to, between and from  transit (if trip ends with transit)
		int walkstages = 0; // number of walk stages
		
		int inVehTime = 0; //total in vehicle time across all modes
		int inVehTimeTransit = 0; //total in vehicle time (transit only)
		
		int waitTimeTransit = 0; 
		
		int tJTBFBALL = 0;//transit journey time between first board and last alight, so wait +walk +invehtime
		int calcJourneyTime = 0; //calculated time for the journey
		
		Date tStart = null;
		Date tEnd = null;
		
		HITSStage prevStage = null; //reference to the previous stage 
		String lastMode = "";
		String lastTransit = "";
		boolean busTripSwitch=false;
		boolean trainTripSwitch=false;
		
		for (int i=0;i<this.stages.size(); i++) {
			//this initalises mm to the first mode encountered
			HITSStage stage = this.stages.get(i);
			if (stage.t14_invehtime == 0 && stage.stage_id == 1) //so invehtime is only 0 because this is the first stage
				mm = stage.t10_mode;
			//sets your invehtime and main mode if the time in that mode is longest
			if (stage.t14_invehtime > longest) {
				mm = stage.t10_mode;
				longest = stage.t14_invehtime;
			}
			inVehTime += stage.t14_invehtime;
			calcJourneyTime += stage.t10a_walktime + stage.t13_waittime + stage.t14_invehtime;
			
			// TODO this section needs work, based on exceptions in the data
			// adds walk time
			if(stage.t10_mode.equals("walk") ){
				walkTimeTrip += (int)((t4_endtime_24h.getTime() - t3_starttime_24h.getTime())/Timer.ONE_MINUTE);
				walkstages +=1;
			}else if (stage.t10a_walktime > 0){
				walkTimeTrip += stage.t10a_walktime;
				walkstages += 1;
			}
			//transit checks
			if(stage.t10_mode.equals("publBus") || stage.t10_mode.equals("lrt") || stage.t10_mode.equals("mrt")  ){
				walkTimeTransit += stage.t10a_walktime;
				waitTimeTransit += stage.t13_waittime;
				inVehTimeTransit += stage.t14_invehtime; 
				//if this is the first transit stage, add to total journey time, else check if the last stage was transit and add to total transit journey time
				if(lastTransit.equals("") && lastMode.equals("")){ //Transit is the first stage
					tStart = new Date(this.t3_starttime_24h.getTime() + (long)(stage.t10a_walktime + stage.t13_waittime)*Timer.ONE_MINUTE);
					tJTBFBALL +=  stage.t14_invehtime; // don't count the waiting time or walking time of the first trip
				}else if(lastTransit.equals("") && !lastMode.equals("")){ // 1st Transit stage, need the total time of all preceding stages to calculate its start time
					tStart = new Date(this.t3_starttime_24h.getTime() + (long)(calcJourneyTime + stage.t10a_walktime + stage.t13_waittime)*Timer.ONE_MINUTE);
					tJTBFBALL += stage.t14_invehtime;// don't count the waiting time or walking time of the first trip
				}else if(!lastTransit.equals("") && lastMode.equals(lastTransit) && stage.stage_id == prevStage.stage_id+1){
					tJTBFBALL += stage.t10a_walktime + stage.t13_waittime + stage.t14_invehtime;
				}else {
					System.out.println("special transit case");
				}
				transitModeChain += stage.t10_mode + "_";
				lastTransit = stage.t10_mode;
				if(stage.t10_mode.equals("publBus"))busTripSwitch = true;
				if(stage.t10_mode.equals("lrt") || stage.t10_mode.equals("mrt")) trainTripSwitch=true;
			}
			
			modeChain +=   (i==0?"":"_") + stage.t10_mode;
			prevStage = stage;
			lastMode = prevStage.t10_mode;
			
		}
		
		if(tJTBFBALL > 0) tEnd = new Date(tStart.getTime() + (long)tJTBFBALL*Timer.ONE_MINUTE);
		
		if (this.t22_lastwlktime>0){
			walkTimeTrip += this.t22_lastwlktime;
			walkTimeTransit += (lastMode.equals("publBus") || lastMode.equals("lrt") || lastMode.equals("mrt")   ? this.t22_lastwlktime :0);
			walkstages += 1;
			calcJourneyTime += this.t22_lastwlktime;
		}
		
//				calculate modal distance for comparison with ezlink
		Coord startCoord = HITSAnalyser.getZip2Coord(this.p13d_origpcode);
		Coord endCoord = HITSAnalyser.getZip2Coord(this.t2_destpcode);
		if(startCoord != null && endCoord != null){
			if(trainTripSwitch && !busTripSwitch){
				this.trainDistance = HITSAnalyser.getCarFreeSpeedShortestPathTimeAndDistance(startCoord, endCoord).distance;
			}else if(busTripSwitch && !trainTripSwitch){
				this.busDistance = HITSAnalyser.getCarFreeSpeedShortestPathTimeAndDistance(startCoord, endCoord).distance;
			}else if(busTripSwitch && trainTripSwitch){
				this.busTrainDistance = HITSAnalyser.getCarFreeSpeedShortestPathTimeAndDistance(startCoord, endCoord).distance;
			}else {
				this.freeSpeedCarJourneyDistance = HITSAnalyser.getCarFreeSpeedShortestPathTimeAndDistance(startCoord, endCoord).distance;
			}
			this.straightLineDistance = HITSAnalyser.getStraightLineDistance(startCoord, endCoord);
			
		}
		
		this.mainmode = mm;
		this.stageChainSimple = modeChain;
		this.stageChainTransit = transitModeChain;
		
		this.totalWalkTimeTrip = walkTimeTrip;
		this.transitWalkTimeTrip = walkTimeTransit;
		this.numberOfWalkStagesTrip = walkstages;
		this.transitWaitTimeTrip = waitTimeTransit;
		
		this.inVehTimeTrip = inVehTime;
		this.inVehTimeTransit = inVehTimeTransit;
		
		
		this.transitJourneyTimeBetweenFirstBoardAndLastAlight = tJTBFBALL;
		this.estimatedJourneyTime = (int)((t4_endtime_24h.getTime() - t3_starttime_24h.getTime())/Timer.ONE_MINUTE);
		this.calculatedJourneyTime = calcJourneyTime;
		this.subjTimeError = this.estimatedJourneyTime - this.calculatedJourneyTime;
		this.transitStartTime = tStart;
		this.transitEndTime = tEnd;
		this.transientsCalculated = true;
	}
}


