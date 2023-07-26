package COVID;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.net.InetAddress; 

public class CovidKlijent implements Runnable {

private static Socket soketZaKomunikaciju = null;
private static PrintStream izlazniTokKaServeru = null;
private static BufferedReader ulazniTokOdServera = null;
private static Scanner ulazSaKonzole = null;
private static String cmd ="";
private static String regexp ="";
private static String sifra_v = "";
private static String datum_v1 = "";
private static String datum_v2 = "";
private static boolean end = false;
		
private static boolean verify(String response, String regexp) {
    if(!response.matches(regexp))
        return false;
	return true;
}
	
public static void clrscr(){
    try {
        if (System.getProperty("os.name").contains("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        else
            Runtime.getRuntime().exec("clear");
	    } 
    catch (IOException|InterruptedException e) {
    	System.out.println("Problem u funkciji clrscr()");
    }
}
	
private static boolean check_date(String datum) {
	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	format.setLenient(false);
	try {
		format.parse(datum);
	} 
	catch (java.text.ParseException e) {
		return false;
	}
    return true;
}
	
private static boolean validacija_datuma_vakcine(String datum_pd, String datum_dd, String md, int interval) {
	try {
		Date pd = new SimpleDateFormat("dd/MM/yyyy").parse(datum_pd);
		Date dd = new SimpleDateFormat("dd/MM/yyyy").parse(datum_dd); 
		Date target_date = null;
		Calendar cal = Calendar.getInstance();
		if (md.equals("MONTH")){
			cal.setTime(pd);
			cal.add(Calendar.MONTH, interval); 
		    target_date = cal.getTime();
		}
		else if (md.equals("DATE")) {
			cal.setTime(pd);
			cal.add(Calendar.DATE, interval); 
		    target_date = cal.getTime();
		}
				
		if (!target_date.after(dd))  
			return true;   
		else  
			return false;
	} 
	catch (java.text.ParseException e) {
			return false;
	} 
}
	

@SuppressWarnings("unchecked")
private static void report_1(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);

JSONObject jo = (JSONObject) obj;
System.out.println("Korisnik: " + jo.get("prezime_korisnika") + " "+ jo.get("ime_korisnika"));
System.out.println("Pol: " + jo.get("pol") + "    JMBG: "+ jo.get("jmbg"));
   	if (jo.get("prva_doza")!=null) {
	    @SuppressWarnings("rawtypes")
		Map pd = ((Map)jo.get("prva_doza"));
	    System.out.printf("%-20s %-12s %-13s %-18s %s\n", "PRVA DOZA VAKCINE:",  "Proizvodjac:", pd.get("proizvodjac_v"), "Datum vakcinacije:", pd.get("datum_v"));		
   	}
   	if (jo.get("druga_doza")!=null) {
	    @SuppressWarnings("rawtypes")
		Map pd = ((Map)jo.get("druga_doza"));
	    System.out.printf("%-20s %-12s %-13s %-18s %s\n", "DRUGA DOZA VAKCINE:",  "Proizvodjac:", pd.get("proizvodjac_v"), "Datum vakcinacije:", pd.get("datum_v"));		
   	}
   	if (jo.get("treca_doza")!=null) {
	    @SuppressWarnings("rawtypes")
		Map pd = ((Map)jo.get("treca_doza"));
	    System.out.printf("%-20s %-12s %-13s %-18s %s\n", "TRECA DOZA VAKCINE:",  "Proizvodjac:", pd.get("proizvodjac_v"), "Datum vakcinacije:", pd.get("datum_v"));		
   	}
   	
   	jo = new JSONObject();
    jo.put("cmd", "ADMIN");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_1()");
}
}

@SuppressWarnings("unchecked")
private static void report_2(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);
	JSONArray ja =  (JSONArray )obj;
	 Iterator<JSONObject> iterator = ja.iterator();
	    clrscr();
	    while (iterator.hasNext()) {
	    JSONObject st = iterator.next();
		System.out.printf("%-10s %-12s  %-12s  %-20s %s\n",  "Korisnik:" , st.get("prezime_korisnika") , st.get("ime_korisnika") , "je vakcinisan sa:", st.get("vakcina"));	
	    }

	JSONObject jo = new JSONObject();
    jo.put("cmd", "ADMIN");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_2()");
}
}

@SuppressWarnings("unchecked")
private static void report_3(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);
	JSONObject jo = (JSONObject) obj;
	
	clrscr();
	for (Object key : jo.keySet()) {
    String keyStr = (String)key;
    Object keyvalue = jo.get(keyStr);
    System.out.printf("%-40s %s\n", keyStr, keyvalue);
	}
    
  	
   	jo = new JSONObject();
    jo.put("cmd", "ADMIN");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_3()");

}
}

@SuppressWarnings("unchecked")
private static void report_5(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);
	JSONObject st = (JSONObject) obj;
	clrscr();
	System.out.printf("%-10s %-12s %-12s %-17s %s\n",  "Korisnik:" , st.get("prezime_korisnika") , st.get("ime_korisnika") , "je vakcinisan sa:", st.get("vakcina"));	
    JSONObject jo = new JSONObject();
    jo.put("cmd", "USER");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_5()");

}
}

@SuppressWarnings({ "unchecked", "rawtypes" })
private static void report_6(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);
	JSONObject st = (JSONObject) obj;
	
	PrintWriter pw = null;
	try {
		pw = new PrintWriter("c:\\covid_certificate.txt", "UTF-8");
	} catch (FileNotFoundException e2) {
		System.out.println("Problem u kreiranju fajla covid_certificate.txt");
	} catch (UnsupportedEncodingException e2) {
		System.out.println("Problem u kreiranju fajla covid_certificate.txt");
	}
	pw.printf(System.getProperty("line.separator"));
	pw.printf(System.getProperty("line.separator"));
	pw.printf(System.getProperty("line.separator"));
	pw.printf("%80s", "C O V I D     S E R T I F I K A T" + System.getProperty("line.separator") + System.getProperty("line.separator"));
	pw.printf("%10s", "Ovim sertifikatom se potvrdjuje da je:" + System.getProperty("line.separator")+ System.getProperty("line.separator"));
	
	pw.printf("%10s  %s   %s", "Korisnik: ", st.get("prezime_korisnika"), st.get("ime_korisnika") + System.getProperty("line.separator"));
	pw.printf("%10s  %s", "Pol: ", st.get("pol") + System.getProperty("line.separator"));
	pw.printf("%10s  %s", "JMBG: ", st.get("jmbg") + System.getProperty("line.separator"));
	pw.printf("%10s  %s", "email: ", st.get("email") + System.getProperty("line.separator"));
	pw.printf(System.getProperty("line.separator"));
	pw.printf(System.getProperty("line.separator"));
	
	pw.printf("%10s", "Vakcinisan vakcinama sledecih proizvodjaca:" + System.getProperty("line.separator")+ System.getProperty("line.separator"));
	pw.printf(System.getProperty("line.separator"));
	
	if (st.containsKey("prva_doza")) {
		Map pd = ((Map)st.get("prva_doza"));
		pw.printf("%20s  %s" , "Proizvodjac: ", pd.get("proizvodjac_v") + System.getProperty("line.separator"));
		pw.printf("%20s  %s","Datum vakcinacije: ", pd.get("datum_v") + System.getProperty("line.separator"));
	}

	pw.printf(System.getProperty("line.separator"));
	
	if (st.containsKey("druga_doza")) {
		Map pd = ((Map)st.get("druga_doza"));
		pw.printf("%20s  %s" , "Proizvodjac: ", pd.get("proizvodjac_v") + System.getProperty("line.separator"));
		pw.printf("%20s  %s","Datum vakcinacije: ", pd.get("datum_v") + System.getProperty("line.separator"));
	}
	pw.printf(System.getProperty("line.separator"));
	
	if (st.containsKey("treca_doza")) {
		Map pd = ((Map)st.get("treca_doza"));
		pw.printf("%20s  %s" , "Proizvodjac: ", pd.get("proizvodjac_v") + System.getProperty("line.separator"));
		pw.printf("%20s  %s","Datum vakcinacije: ", pd.get("datum_v") + System.getProperty("line.separator"));
	}
	    pw.close();	    
    
    ProcessBuilder pb = new ProcessBuilder("Notepad.exe", "c:\\covid_certificate.txt");
	try {
		pb.start();
	} catch (IOException e2) {
		System.out.println("Problem u kreiranju fajla covid_certificate.txt");
	}
	
    JSONObject jo = new JSONObject();
    jo.put("cmd", "MENU_USER");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_6()");

}
}

@SuppressWarnings("unchecked")
private static void report_4(String item){
Object obj;
try {
	obj = new JSONParser().parse(item);
	JSONObject jo = (JSONObject) obj;
	
	clrscr();
	for (Object key : jo.keySet()) {
    String keyStr = (String)key;
    Object keyvalue = jo.get(keyStr);
    System.out.printf("%-12s %-25s %30s %s\n", "Proizvodjac: ", keyStr, "Broj vakcinisanih drugom dozom:", keyvalue);
	}
	
	
   	jo = new JSONObject();
    jo.put("cmd", "ADMIN");
    jo.put("response", "");
    izlazniTokKaServeru.println(jo.toJSONString());
    izlazniTokKaServeru.flush();
} catch (ParseException e) {
	System.out.println("Problem u funkciji report_4()");

}
}

@SuppressWarnings("unchecked")
public static void main(String[] args) {
	String konzola;
	String hostName;
	int port = 0;
	InetAddress ip = null;
	JSONObject jo = null;
	
	try {
		switch (args.length) {
			case 0:
					hostName="localhost";
					ip = InetAddress.getByName(hostName);
					port = 15000;
				break;
			case 1:
					hostName = args[0];
					ip = InetAddress.getByName(hostName);
					port = 15000;
				break;
			case 2:
					hostName = args[0];
					ip = InetAddress.getByName(hostName);
					port = Integer.parseInt(args[1]);
				break;
			default:
				System.out.println("Arguments: IPaddress PortNumber");
				System.exit(0) ;
		 }
		
		soketZaKomunikaciju = new Socket(ip, port);
		ulazniTokOdServera = new BufferedReader (new InputStreamReader(soketZaKomunikaciju.getInputStream()));
		izlazniTokKaServeru = new PrintStream(soketZaKomunikaciju.getOutputStream());     
		ulazSaKonzole =  new Scanner(System.in);
		
		new Thread(new CovidKlijent()).start();
		while (!end) {
			konzola = ulazSaKonzole.nextLine();
			
			if (!verify(konzola,regexp)) {
				System.out.println("Pogresan unos, pokusajte ponovo!");
			    continue;
			}
			else {
				    //dodatna validacija datuma
					if (regexp.equals("^(3[01]|[12][0-9]|0[1-9])\\/(1[0-2]|0[1-9])\\/2021$")) {
						if (!check_date(konzola)) {
							System.out.println("Pogresan unos:");
					    	continue;
						}				
					} //proizvodjac prve i druge doze vakcine mora biti isti
					if (cmd.equals("MENU1.12")) {
						if (!konzola.equals(sifra_v)) {
							System.out.println("Pogresan unos:");
					    	continue;
						}
					} //validacija vremena izmedju prve i druge doze vakcine
					if (cmd.equals("MENU1.13")) {
						if (!validacija_datuma_vakcine(datum_v1, konzola, "DATE", 21)) {
							System.out.println("Pogresan unos:");
					    	continue;
						}
					} //validacija vremena izmedju druge i trece doze vakcine
					if (cmd.equals("MENU1.16")) {
						if (!validacija_datuma_vakcine(datum_v2, konzola, "MONTH", 6)) {
							System.out.println("Pogresan unos:");
					    	continue;
						}
					}
					if (cmd.equals("BYE")) {
						end=true;
					    continue;
					}
					
					jo = new JSONObject();
				    jo.put("cmd", cmd);
				    jo.put("response", konzola);
				    izlazniTokKaServeru.println(jo.toJSONString());
				    izlazniTokKaServeru.flush();
				    jo=null;
			}
		}
		soketZaKomunikaciju.close();
	}
	catch (NumberFormatException e) {
		 System.out.println("Arguments: IPaddress PortNumber");
		 System.exit(0) ;
		  }	
	catch (UnknownHostException e) {
		 System.out.println("Arguments: IPaddress PortNumber");
		 System.exit(0) ;
	}
	catch (NoSuchElementException e) {
         System.out.println("CTRL C Program stops");
    }
	catch (ConnectException e) {
		 System.out.println("Komunikaciju nije moguce ostvariti");
		 System.exit(0) ;
	} 
	catch (Exception e) {
		 System.out.println("Greska u radu programa");
		 System.exit(0) ;
	
	}

	}
	
@SuppressWarnings("unchecked")
public void run() {
		String odgovorOdServera;
		Object obj = null;
		JSONObject jo = null;
	try {
		while ((odgovorOdServera = ulazniTokOdServera.readLine()) != null) {
			obj = new JSONParser().parse(odgovorOdServera);
			jo = (JSONObject) obj;
	        String msg = (String) jo.get("message");
	        cmd = (String) jo.get("cmd");
	        regexp = (String) jo.get("regexp");
	        if (cmd.equals("MENU1.12")) sifra_v = (String) jo.get("sifra_v"); else sifra_v="";
	        if (cmd.equals("MENU1.13")) datum_v1 = (String) jo.get("datum_v"); else datum_v1="";
	        if (cmd.equals("MENU1.16")) datum_v2 = (String) jo.get("datum_v"); else datum_v2="";
	        jo=null;
	        switch (cmd) {
		    	case "BYE":
		    		System.exit(0);
		    		break;
		    	case "ADMIN.1":
		    	    report_1(msg);
		    	    break;
		    	case "ADMIN.2":
		    	    report_2(msg);
		    	    break;
		    	case "ADMIN.3":
		    	    report_3(msg);
		    	    break;
		    	case "ADMIN.4":
		    	    report_4(msg);
		    	    break;
		    	case "USER.1":
		    	    report_5(msg);
		    	    break;
		    	case "USER.2":
		    		if (msg.equals("NO_COVID_PASS")) {
		    		clrscr();
		    		System.out.println("Korisnik nije vakcinisan potreban broj puta");
		    		jo = new JSONObject();
		    	    jo.put("cmd", "USER");
		    	    jo.put("response", "");
		    	    izlazniTokKaServeru.println(jo.toJSONString());
		    	    izlazniTokKaServeru.flush();
		    		}
		    		else
		    		report_6(msg);
		    	    break;
		    	case "MENU_ADMIN":
		    		System.out.println(msg);
		    	    break;
		    	case "MENU_USER":
		    		System.out.println(msg);
		    	    break;
		    	default:
		    		clrscr();
		    		System.out.println(msg);
                break;       
	        }
		}
	}
	catch (IOException e) {
		System.out.println("Problem u radu sa klijentom");
	} 
	catch (ParseException e) {
		System.out.println("Problem u radu sa klijentom");

	}
}
}


