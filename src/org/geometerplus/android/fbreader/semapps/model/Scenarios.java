package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import android.os.Parcel;
import android.os.Parcelable;

@Element
public class Scenarios implements Parcelable {
	
	@Attribute
	protected String type;
	@ElementList(required=false, inline=true, name="scenarios", entry="scenario")
	protected ArrayList<Scenario> scenarios;
	
	/**
	 * Standard empty constructor
	 */
	public Scenarios(){
		scenarios = new ArrayList<Scenario>();
    }
	
	public Scenarios(Parcel in) {
		readFromParcel(in);
	}
	
	public Scenario addScenario(
			int id,
			int semapp_id,
			int epub_id,
			String name,
			String created_at,
			String updated_at) {

		Scenario scenario = new Scenario();
		
		scenario.setId(id);
		scenario.setSemAppId(semapp_id);
		scenario.setEPubId(epub_id);
		scenario.setName(name);
		scenario.setCreated_at(created_at);
		scenario.setUpdated_at(updated_at);
		
		scenarios.add(scenario);
		
		return scenario;
	}

	public ArrayList<Scenario> getScenarios() {
		return scenarios;
	}
	
	public Scenario getScenarioById(int id) {
		for (Scenario scenario : scenarios) {
			if (scenario.getId() == id) {
				return scenario;
			}
		}
		return null;
	}
	
	public Scenario getScenarioByEPubId(int epub_id) {
		for (Scenario scenario : scenarios) {
			if (scenario.getId() == epub_id) {
				return scenario;
			}
		}
		return null;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		if (scenarios == null) {
			scenarios = new ArrayList();
		}
		in.readTypedList(scenarios, Scenario.CREATOR);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(scenarios);
	}
	
	public static final Parcelable.Creator<Scenarios> CREATOR = new Parcelable.Creator<Scenarios>() {
		public Scenarios createFromParcel(Parcel in) {
			return new Scenarios(in);
		}

		public Scenarios[] newArray(int size) {
			return new Scenarios[size];
		}
	};
	
}