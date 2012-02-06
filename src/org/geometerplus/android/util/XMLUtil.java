package org.geometerplus.android.util;

import java.io.StringWriter;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotations;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public abstract class XMLUtil {
	
	/**
	 * load an XML String of semapps into the semapps object structure
	 * @param xml
	 */
	public static SemApps loadSemAppsListFromXMLString(String xml) {
		SemApps semApps = null;
		try {
			Serializer serializer = new Persister();
    		semApps = serializer.read(SemApps.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return semApps;
	}
	
	/**
	 * load an XML String of one semapp into the semapp object structure
	 * @param xml
	 */
	public static SemApp loadSemAppFromXMLString(String xml) {
		SemApp semApp = null;
		try {
			Serializer serializer = new Persister();
    		semApp = serializer.read(SemApp.class, xml);
    		System.out.println();
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return semApp;
	}
	
	/**
	 * load an XML String of epub into the epub object structure
	 * @param xml
	 */
	public static EPub loadEPubFromXMLString(String xml) {
		EPub epub = null;
		try {
			Serializer serializer = new Persister();
			epub = serializer.read(EPub.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return epub;
	}
	
	/**
	 * load an XML String of annotations into the annotations object structure
	 * @param xml
	 */
	public static Annotation loadAnnotationFromXMLString(String xml) {
		Annotation annotation = null;
		try {
			Serializer serializer = new Persister();
			annotation = serializer.read(Annotation.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return annotation;
	}
	
	/**
	 * load an XML String of annotations into the annotations object structure
	 * @param xml
	 */
	public static SemAppsAnnotations loadSemAppsAnnotationsListFromXMLString(String xml) {
		SemAppsAnnotations saAnnotations = null;
		try {
			Serializer serializer = new Persister();
    		saAnnotations = serializer.read(SemAppsAnnotations.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return saAnnotations;
	}
	
	/**
	 * load an XML String of an annotation into a annotation object structure
	 * @param xml
	 */
	public static SemAppsAnnotation loadSemAppsAnnotationFromXMLString(String xml) {
		SemAppsAnnotation saAnnotation = null;
		try {
			Serializer serializer = new Persister();
			saAnnotation = serializer.read(SemAppsAnnotation.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
    	return saAnnotation;
	}
	
	/**
	 * Generates from the given annotation an XML String. 
	 */
	public static String saveAnnotationToString(Annotation annotation) {
		String xml = null;
		try {
    		Serializer serializer = new Persister();
    		StringWriter stringWriter = new StringWriter();
    		serializer.write(annotation, stringWriter);
    		xml = stringWriter.toString();
    	} catch (Exception e) {
    		Log.e("saveToXML", e.toString());
    	}
    	return xml;
	}
}

