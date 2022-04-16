package de.litona.dienstplanToCalendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

public final class DienstplanToCalendar {

	public static void main(String... args) {
		Tesseract instance = new Tesseract();
		instance.setDatapath(System.getProperty("user.dir") + "\\tessdata");
		try(FileOutputStream fos = new FileOutputStream("Dienstplan.ics")) {
			Calendar cal = new Calendar();
			cal.getProperties().add(new ProdId("-//Litona//DienstplanToCalendar 1.0//EN"));
			cal.getProperties().add(Version.VERSION_2_0);
			cal.getProperties().add(CalScale.GREGORIAN);
			String[] lines;
			if(args.length == 2) {
				String fromImage = "";
				for(String a : args)
					fromImage = fromImage + "\n" + instance.doOCR(new File(a));
				lines = fromImage.trim().split("\n");
			} else
				lines = args[0].split("\\+");
			for(String l : lines)
				if(l.startsWith("Montag") || l.startsWith("Dienstag") || l.startsWith("Mittwoch") || l.startsWith("Donnerstag") || l.startsWith("Freitag")
					|| l.startsWith("Samstag") || l.startsWith("Sonntag")) {
					System.out.println(l);
					String[] split = l.split("((,\\s*)|(\\s+))");
					System.out.println(split.length);
					if(split.length >= 3) {
						String day = split[1];
						String title;
						if(split.length >= 4) {
							String time = split[2].replace("00:00", "08:00");
							title = split[3] + " " + split[4].replaceAll("22", "Z2").replaceAll("\\$", "S") + " " + time;
						} else
							title = split[2].equals("Urlaub") ? "Urlaub" : "Frei";
						String[] daySplit = day.split("\\.");
						Date date = new Date("2022" + daySplit[1] + daySplit[0]);
						VEvent event = new VEvent(date, date, title);
						event.getProperties().add(new Uid("dienst" + day.replaceAll("\\.", "") + "2022"));
						cal.getComponents().add(event);
					}
				}
			new CalendarOutputter().output(cal, fos);
		} catch(TesseractException | ParseException | IOException e) {
			e.printStackTrace();
		}
	}
}