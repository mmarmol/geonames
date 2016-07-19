package io.gromit.geonames.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * The Class Exporter.
 */
public class Exporter {

	/** The url. */
	private String url;
	
	/** The dest. */
	private String dest;
	
	/** The file name. */
	private String fileName;
	
	/** The fields. */
	private Integer[] fields;
	
	private Boolean ignoreComments = false;
	
	private Transformer transformer = new Transformer() {
		
		@Override
		public void transform(String[] row) {
		}
	};

	/**
	 * Instantiates a new exporter.
	 *
	 * @param url the url
	 * @param dest the dest
	 * @param fileName the file name
	 * @param fields the fields
	 * @param ignoreComments the ignore comments
	 */
	public Exporter(String url, String dest, String fileName, Integer[] fields, Boolean ignoreComments) {
		super();
		this.url = url;
		this.dest = dest;
		this.fileName = fileName;
		this.fields = fields;
		this.ignoreComments = ignoreComments;
	}
	
	public Exporter(String url, String dest, String fileName, Integer[] fields, Boolean ignoreComments, Transformer transformer) {
		super();
		this.url = url;
		this.dest = dest;
		this.fileName = fileName;
		this.fields = fields;
		this.ignoreComments = ignoreComments;
		this.transformer = transformer;
	}

	/**
	 * Export.
	 */
	public void export(){
		InputStream is = null;
		ZipOutputStream zipOut = null;
		CsvParserSettings settings = new CsvParserSettings();
		settings.selectIndexes(fields);
		settings.setSkipEmptyLines(true);
		settings.trimValues(true);
		CsvFormat format = new CsvFormat();
		format.setDelimiter('\t');
		format.setLineSeparator("\n");
		if(ignoreComments){
			format.setComment('\0');
		}
		format.setCharToEscapeQuoteEscaping('\0');
		format.setQuote('\0');
		settings.setFormat(format);
		CsvParser parser = new CsvParser(settings);
		CsvWriterSettings wsettings = new CsvWriterSettings();
		wsettings.setSkipEmptyLines(true);
		wsettings.trimValues(true);
		wsettings.setFormat(format);
		try {
			if(StringUtils.endsWith(url, ".zip")){
				ZipInputStream zipis = new ZipInputStream(new URL(url).openStream(), Charset.forName("UTF-8"));
				zipis.getNextEntry();
				is=zipis;
			}else{
				is=new URL(url).openStream();
			}
			zipOut = new ZipOutputStream(new FileOutputStream(dest));
			zipOut.putNextEntry(new ZipEntry(fileName));
			CsvWriter csvWriter = new CsvWriter(zipOut,wsettings);
			parser.beginParsing(new InputStreamReader(is, "UTF-8"));
			String[] row;
		    while ((row = parser.parseNext()) != null) {
		    	transformer.transform(row);
		    	csvWriter.writeRow(row);
		    }
		    csvWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
			try{is.close();}catch(Exception e){};
			try{zipOut.flush();zipOut.close();}catch(Exception e){};
		}
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String [] args){
		new Exporter("http://download.geonames.org/export/dump/cities1000.zip"
				,"export/cities.zip"
				,"cities.txt"
				,new Integer[]{0,2,4,5,8,10,11,17}
				,true).export();
		new Exporter("http://download.geonames.org/export/dump/countryInfo.txt"
				,"export/countries.zip"
				,"countries.txt"
				,new Integer[]{0,1,4,5,8,10,11,12,15,16}
				,false
				,new Transformer() {
					@Override
					public void transform(String[] row) {
						row[8]=StringUtils.substringBefore(row[8], ",");
					}
				}).export();
		new Exporter("http://download.geonames.org/export/dump/admin1CodesASCII.txt"
				,"export/subdivisions1.zip"
				,"subdivisions1.txt"
				,new Integer[]{0,2,3}
				,true).export();
		new Exporter("http://download.geonames.org/export/dump/admin2Codes.txt"
				,"export/subdivisions2.zip"
				,"subdivisions2.txt"
				,new Integer[]{0,2,3}
				,true).export();
		/*try {
			new Exporter(new File("C:\\Users\\marce\\Downloads\\allCountries.zip").toURI().toURL().toString()
					,"export/allCountries.zip"
					,"allCountries.txt"
					,new Integer[]{0,2,4,5,8,10,11,17}
					,true).export();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
