package COVID;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CovidServerNit  extends Thread {
BufferedReader ulazniTokOdKlijenta = null;
PrintStream izlazniTokKaKlijentu = null;
JSONObject korisnik = null;
Map<String, String> vakcina;
Socket soketZaKom = null;
CovidServerNit[] klijenti;
String username = null;
String password = null;
boolean update = false;
			
public CovidServerNit(Socket soket, CovidServerNit[] klijent) {
	this.soketZaKom = soket;
	this.klijenti = klijent;
}

@SuppressWarnings("unchecked")
private static String LogInSignIn() {
	JSONObject jo = new JSONObject();
	String message = "COVID PASS" + "\n\n" + "1.  Registracija korisnika" + "\n" + "2.  Logovanje na sistem" + "\n" + "10. Izlaz iz programa" + "\n" +"Vas izbor (1|2|10):";
    jo.put("cmd", "MENU");
    jo.put("message", message);
    jo.put("regexp", "1|2|10");
    return jo.toJSONString();	
	}

private static void save_database() {
	try {
		FileWriter fw;
		fw = new FileWriter(CovidServer.database,false);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
	    pw.write(CovidServer.jsonArray.toJSONString());
	    pw.flush();
	    pw.close();
		} 
	catch (Exception e) {
		System.out.println("Problem u kreiranju database fajla");
		System.exit(0);
		}
}

@SuppressWarnings("unchecked")
private static boolean isAccountExist(String response) {
	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
    	if (st.get("korisnik").equals(response)) 
    		return true;
    }
    return false;
}

@SuppressWarnings("unchecked")
private static boolean isJMBGduplicate(String response) {
	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
   	if (st.containsKey("jmbg")) 
    	if (st.get("jmbg").equals(response)) 
    		return true;	
    }
    return false;
}

@SuppressWarnings("unchecked")
private static String isJMBGExists(String response) {
	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
    	if (st.containsKey("jmbg")) {
    		if (st.get("jmbg").equals(response))  return st.toJSONString();
    	}
    }	
    return null;
}


@SuppressWarnings({ "unchecked", "rawtypes"})
private static String statistika3() {
	int v1=0,v2=0,v3=0,v4=0;
	    
    Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
    	if (st.get("druga_doza")!=null) {
    		Map dd = ((Map)st.get("druga_doza"));
    		
    		switch (dd.get("sifra_v").toString()){
    			case "1":
    				v1++;
    				break;
    			case "2":
    				v2++;
    			    break;
    			case "3":
    				v3++;
    				break;
    			case "4":
    				v4++;
    			    break;
    		}
    	}
  }
    JSONObject jo = new JSONObject();
    jo.put("Fajzer", v1);
    jo.put("AstraZeneka", v2);
    jo.put("Sputnjik V", v3);
    jo.put("Sinofarm", v4);		
    return jo.toJSONString();
}

@SuppressWarnings("unchecked")
private String statistika2() {
	int brojac1=0;
	int brojac2=0;
	int brojac3=0;
   
    Iterator<JSONObject> iterator1 = CovidServer.jsonArray.iterator();
    while (iterator1.hasNext()) {
    JSONObject st = iterator1.next();
    	if ((st.get("prva_doza")!=null) & (st.get("druga_doza")==null) & (st.get("treca_doza")==null)) 
    		brojac1++;
    	else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")==null)) 
    		brojac2++;	
        else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")!=null)) 
        	brojac3++;	       
    	}

	JSONObject jo = new JSONObject();
    jo.put("Ukupan broj vakcinisnih prvom dozom", brojac1);
    jo.put("Ukupan broj vakcinisnih drugom dozom", brojac2);
    jo.put("Ukupan broj vakcinisnih trecom dozom", brojac3);
    return jo.toJSONString();
}

@SuppressWarnings("unchecked")
private String statistika1() {
	JSONArray ja = new JSONArray();
	JSONObject jo = null;

	Iterator<JSONObject> iterator1 = CovidServer.jsonArray.iterator();
    while (iterator1.hasNext()) {
    JSONObject st = iterator1.next();
    if (st.containsKey("prezime_korisnika")) {
    	jo = new JSONObject();
    	jo.put("ime_korisnika", st.get("ime_korisnika"));
    	jo.put("prezime_korisnika", st.get("prezime_korisnika"));
    	if ((st.get("prva_doza")!=null) & (st.get("druga_doza")==null) & (st.get("treca_doza")==null)) 
    		jo.put("vakcina", "sa jednom dozom");
    	else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")==null)) 
    		jo.put("vakcina", "sa dvije doze");
        else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")!=null)) 
        	jo.put("vakcina", "sa tri doze"); 
        else
        	jo.put("vakcina", "nije vakcinisan"); 
    	ja.add(jo);
    	}
    }
    return ja.toJSONString();
}

@SuppressWarnings({ "unchecked", "null" })
private String isCovidPass() {
	JSONObject jo = null;

	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
    if (st.get("korisnik").equals(username)) {
    	jo = new JSONObject();
    	jo.put("ime_korisnika", st.get("ime_korisnika"));
    	jo.put("prezime_korisnika", st.get("prezime_korisnika"));
    	if ((st.get("prva_doza")!=null) & (st.get("druga_doza")==null) & (st.get("treca_doza")==null)) 
    		jo.put("vakcina", "sa jednom dozom  NO COVID PASS");
    	else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")==null)) 
    		jo.put("vakcina", "sa dvije doze  COVID PASS");
        else if ((st.get("prva_doza")!=null) & (st.get("druga_doza")!=null) & (st.get("treca_doza")!=null)) 
        	jo.put("vakcina", "sa tri doze  COVID PASS"); 
        else 
           	jo.put("vakcina", "nije vakcinisan NO COVID PASS"); 
    	return jo.toJSONString();
    	}
    }
    return null;
}


@SuppressWarnings("unchecked")
private String CovidPass() {
	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    while (iterator.hasNext()) {
    JSONObject st = iterator.next();
    if (st.get("korisnik").equals(username))
    	if (st.get("druga_doza")!=null)
    	     return st.toJSONString();
        else
    	     return null;
    }
    return null;
    
}

@SuppressWarnings({ "unchecked", "rawtypes" })
private String commands(String odgovor_klijenta) {

	try {
		Object obj = new JSONParser().parse(odgovor_klijenta);
		JSONObject jo = (JSONObject) obj;
		String response = (String) jo.get("response");
		//System.out.println(response);
		String cmd = (String) jo.get("cmd");
		//System.out.println(cmd);
		String message;
		jo=null;	
		
		switch (cmd) {
        case "MENU":  
        	if (response.equals("1")) {
        		update = false;
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Molimo Vas unesite podatke vezane za registraciju!" + "\n" + "Unesite Vase korisnicko ime:";
        	    jo.put("cmd", "MENU1.1");
        	    jo.put("message", message);
        	    jo.put("regexp", "[a-zA-Z.]*");
        	    return jo.toJSONString();	
        	}
        	else if (response.equals("2")) {
        		update = false;
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Molimo Vas unesitevase kredencijale za pristup sistemu!" + "\n" + "Unesite Vase korisnicko ime:";
        	    jo.put("cmd", "MENU2.1");
        	    jo.put("message", message);
        	    jo.put("regexp", "[a-zA-Z.]*");
        	    return jo.toJSONString();	
        	}
        	else if (response.equals("3")) {
     	    	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
    	        while (iterator.hasNext()) {
    	        korisnik = iterator.next();
    	        if (korisnik.get("korisnik").equals(username) & korisnik.get("lozinka").equals(password)) {
    	        	if (korisnik.get("prva_doza")==null) {
    	        		update = true;
  	        		    jo = new JSONObject();
  	    	        	message = "COVID PASS" + "\n\n" + "Da li ste primili prvu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
  	    	        	jo.put("cmd", "MENU1.8");
  	    	        	jo.put("message", message);
  	    	        	jo.put("regexp", "D|N");           
  	    	    	    return jo.toJSONString();
  	        	    }
    	        	else if (korisnik.get("druga_doza")==null) {
    	        		update = true;
	        		    jo = new JSONObject();
	    	        	message = "COVID PASS" + "\n\n" + "Da li ste primili drugu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
	    	        	jo.put("cmd", "MENU1.11");
	    	        	jo.put("message", message);
	    	        	jo.put("regexp", "D|N");           
	    	    	    return jo.toJSONString();
	        	    }  
    	        	else if (korisnik.get("treca_doza")==null) {
    	        	     	update = true;
    	        		    jo = new JSONObject();
    	    	        	message = "COVID PASS" + "\n\n" + "Da li ste primili trecu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
    	    	        	jo.put("cmd", "MENU1.14");
    	    	        	jo.put("message", message);
    	    	        	jo.put("regexp", "D|N");           
    	    	    	    return jo.toJSONString();
    	            }
    	        	else {
    	        		update = false;
    	        		jo = new JSONObject();
    	        	    message = "Primili ste sve tri doze vakcine " + "\n\n" + "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" + "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
    	        	    jo.put("cmd", "MENU");
    	        	    jo.put("message", message);
    	        	    jo.put("regexp", "3|4|5|10");
    	                return jo.toJSONString();
    	        	}		
    	        }
    	        }
        	}
        	else if (response.equals("4")) {
        		update = false;
              	jo = new JSONObject();
    	        message = isCovidPass();
    	        jo.put("cmd", "USER.1");
    	        jo.put("message", message);
            	jo.put("regexp", "");
            	return jo.toJSONString();
        	}
        	else if (response.equals("5")) {
        		update = false;
              	jo = new JSONObject();
    	        message = CovidPass();
    	        if (message == null) message ="NO_COVID_PASS";
    	        jo.put("cmd", "USER.2");
    	        jo.put("message", message);
            	jo.put("regexp", "");
            	return jo.toJSONString();
        	}
        	else if (response.equals("6")) {
        		update = false;
          		jo = new JSONObject();
	        	message = "COVID PASS" + "\n\n" + "Unesite maticni broj korisnika!";
	        	jo.put("cmd", "MENU6.1");
	        	jo.put("message", message);
        	    jo.put("regexp", "[0-9]{13}");
        	    return jo.toJSONString();	
        	}
        	else if (response.equals("7")) {
        		update = false;
              	jo = new JSONObject();
    	        message = statistika1();
    	        jo.put("cmd", "ADMIN.2");
    	        jo.put("message", message);
            	jo.put("regexp", "");
            	return jo.toJSONString();	
        	}
            else if (response.equals("8")) {
            	update = false;
               	jo = new JSONObject();
        	    message = statistika2();
        	    jo.put("cmd", "ADMIN.3");
        	    jo.put("message", message);
              	jo.put("regexp", "");
              	return jo.toJSONString();	
        	}
            else if (response.equals("9")) {
        		update = false;
              	jo = new JSONObject();
    	        message = statistika3();
    	        jo.put("cmd", "ADMIN.4");
    	        jo.put("message", message);
            	jo.put("regexp", "");
            	return jo.toJSONString();	
            }
        	else if (response.equals("10")) {
        		update = false;
        		jo = new JSONObject();
        	    jo.put("cmd", "BYE");
        	    jo.put("message", "");
        	    jo.put("regexp", "");
        	    return jo.toJSONString();	
        	}
        break;
        case "MENU1.1":
        	if (!isAccountExist(response)) {
        		korisnik = new JSONObject();
        		username =response;
        		korisnik.put("korisnik", response);
        		message = "COVID PASS" + "\n\n" + "Unesite Vasu korisnicku lozinku:";
        		jo = new JSONObject();
        		jo.put("cmd", "MENU1.2");
        		jo.put("message", message);
        		jo.put("regexp", "[a-zA-Z.]*");
        		return jo.toJSONString();
        	}
        	else {
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Korisnicko ime vec postoju u sistemu!" + "\n\n" + "1.  Registracija korisnika" + "\n" + "2.  Logovanje na sistem" + "\n" + "10. Izlaz iz programa" + "\n" +"Vas izbor (1|2|10):";
        	    jo.put("cmd", "MENU");
        	    jo.put("message", message);
        	    jo.put("regexp", "1|2|10");
        	    return jo.toJSONString();	
        	}
        case "MENU1.2": 
        	password =response;
        	korisnik.put("lozinka", response);
        	jo = new JSONObject();
    		message = "COVID PASS" + "\n\n" + "Unesite Vase ime:";
    	    jo.put("cmd", "MENU1.3");
    	    jo.put("message", message);
    	    jo.put("regexp", "[a-zA-Z.]*");
    	    return jo.toJSONString();	
        case "MENU1.3":  
        	korisnik.put("ime_korisnika", response);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Unesite Vase prezime:";
    	    jo.put("cmd", "MENU1.4");
    	    jo.put("message", message);
    	    jo.put("regexp", "[a-zA-Z.]*");
    	    return jo.toJSONString();
        case "MENU1.4":    
        	korisnik.put("prezime_korisnika", response);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Unesite Vas JMBG:";
    	    jo.put("cmd", "MENU1.5");
    	    jo.put("message", message);
    	    jo.put("regexp", "[0-9]{13}");
    	    return jo.toJSONString();
        case "MENU1.5":   
        	if (!isJMBGduplicate(response)) {
         		korisnik.put("jmbg", response);
            	jo = new JSONObject();
            	message = "COVID PASS" + "\n\n" + "Unesite Pol (M|Z):";
        	    jo.put("cmd", "MENU1.6");
        	    jo.put("message", message);
        	    jo.put("regexp", "M|Z");
        	    return jo.toJSONString();     		
        	}
        	else {
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "JMBG vec postoji u sistemuu!" + "\n\n" + "Unesite Vas JMBG:";
        		jo.put("cmd", "MENU1.5");
        	    jo.put("message", message);
        	    jo.put("regexp", "[0-9]{13}");
        	    return jo.toJSONString();	
        	} 	
        case "MENU1.6":   
        	korisnik.put("pol", response);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Unesite Vas e-mail:";
    	    jo.put("cmd", "MENU1.7");
    	    jo.put("message", message);
    	    jo.put("regexp", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    	    return jo.toJSONString();
        case "MENU1.7":   
    	    korisnik.put("email", response);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Da li ste primili prvu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
    	    jo.put("cmd", "MENU1.8");
    	    jo.put("message", message);
    	    jo.put("regexp", "D|N");   
    	    return jo.toJSONString();
        case "MENU1.8":   
        	if (response.equals("D")) {
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Izaberite proizvodjaca vakcine!" + "\n" + "1. Fajzer" + "\n" + "2. AstraZeneka " + "\n" + "3. Sputnjik V" + "\n"+ "4. Sinofarm" + "\n\n"  + "Vas izbor (1|2|3|4):";
        		jo.put("cmd", "MENU1.9");
        		jo.put("message", message);
        		jo.put("regexp", "1|2|3|4");   
        		return jo.toJSONString();
        	}
        	else if (response.equals("N")) {
        		if (update == false) {
        			CovidServer.jsonArray.add(korisnik);
        		}
        		korisnik=null;
                jo = new JSONObject();
        	    message = "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
        	    jo.put("cmd", "MENU");
        	    jo.put("message", message);
        	    jo.put("regexp", "3|4|5|10");
        	    
        	    save_database();
                return jo.toJSONString();
           	}
        case "MENU1.9":   
        	    vakcina = new LinkedHashMap(2);
        		if (response.equals("1")) {
        			vakcina.put("sifra_v", "1");	
        			vakcina.put("proizvodjac_v", "Fajzer");
        		}
        		else if (response.equals("2")) {
        			vakcina.put("sifra_v", "2");
            		vakcina.put("proizvodjac_v", "AstraZeneka");
        		}
        		else if (response.equals("3")) {
        			vakcina.put("sifra_v", "3");
            		vakcina.put("proizvodjac_v", "Sputnjik V");
        		}
        		else if (response.equals("4")) {
        			vakcina.put("sifra_v", "4");
            		vakcina.put("proizvodjac_v", "Sinofarm");
        		}

        	jo = new JSONObject();
            message = "COVID PASS" + "\n\n" + "Unesite datum vakcinacije u formatu dd/mm/yyyy?";
        	jo.put("cmd", "MENU1.10");
        	jo.put("message", message);
        	jo.put("regexp", "^(3[01]|[12][0-9]|0[1-9])\\/(1[0-2]|0[1-9])\\/2021$");   
        	return jo.toJSONString();
        case "MENU1.10":   
        	vakcina.put("datum_v", response);
        	korisnik.put("prva_doza", vakcina);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Da li ste primili drugu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
        	jo.put("cmd", "MENU1.11");
        	jo.put("message", message);
        	jo.put("regexp", "D|N");    
    	    return jo.toJSONString();
        case "MENU1.11":   
        	if (response.equals("D")) {
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Izaberite proizvodjaca vakcine!" + "\n" + "1. Fajzer" + "\n" + "2. AstraZeneka " + "\n" + "3. Sputnjik V" + "\n"+ "4. Sinofarm" + "\n\n"  + "Vas izbor (1|2|3|4):";
        		jo.put("cmd", "MENU1.12");
        		jo.put("message", message);
        		jo.put("regexp", "1|2|3|4");
        		Map pds = ((Map) korisnik.get("prva_doza"));
        		jo.put("sifra_v", pds.get("sifra_v"));
        		return jo.toJSONString();
        	}
        	else if (response.equals("N")) {
        		if (update == false) {
        			CovidServer.jsonArray.add(korisnik);
        		}
        		korisnik=null;
                jo = new JSONObject();
        	    message = "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
        	    jo.put("cmd", "MENU");
        	    jo.put("message", message);
        	    jo.put("regexp", "3|4|5|10");
        	    save_database();
                return jo.toJSONString();
        	}
        case "MENU1.12":   
        	    vakcina = new LinkedHashMap(2);
        		if (response.equals("1")) {
        			vakcina.put("sifra_v", "1");	
        			vakcina.put("proizvodjac_v", "Fajzer");
        		}
        		else if (response.equals("2")) {
        			vakcina.put("sifra_v", "2");
            		vakcina.put("proizvodjac_v", "AstraZeneka");
        		}
        		else if (response.equals("3")) {
        			vakcina.put("sifra_v", "3");
            		vakcina.put("proizvodjac_v", "Sputnjik V");
        		}
        		else if (response.equals("4")) {
        			vakcina.put("sifra_v", "4");
            		vakcina.put("proizvodjac_v", "Sinofarm");
        		}

        	jo = new JSONObject();
            message = "COVID PASS" + "\n\n" + "Unesite datum vakcinacije u formatu dd/mm/yyyy?";
        	jo.put("cmd", "MENU1.13");
        	jo.put("message", message);
        	jo.put("regexp", "^(3[01]|[12][0-9]|0[1-9])\\/(1[0-2]|0[1-9])\\/2021$");   
        	Map pdd = ((Map) korisnik.get("prva_doza"));
            jo.put("datum_v", pdd.get("datum_v"));
        	return jo.toJSONString();
        case "MENU1.13":   
        	vakcina.put("datum_v", response);
        	korisnik.put("druga_doza", vakcina);
        	jo = new JSONObject();
        	message = "COVID PASS" + "\n\n" + "Da li ste primili trecu dozu vakcine?" + "\n" + "Vas izbor (D|N):";
        	jo.put("cmd", "MENU1.14");
        	jo.put("message", message);
        	jo.put("regexp", "D|N");           
    	    return jo.toJSONString();    	
        case "MENU1.14":   
        	if (response.equals("D")) {
        		jo = new JSONObject();
        		message = "COVID PASS" + "\n\n" + "Izaberite proizvodjaca vakcine!" + "\n" + "1. Fajzer" + "\n" + "2. AstraZeneka " + "\n" + "3. Sputnjik V" + "\n"+ "4. Sinofarm" + "\n\n"  + "Vas izbor (1|2|3|4):";
        		jo.put("cmd", "MENU1.15");
        		jo.put("message", message);
        		jo.put("regexp", "1|2|3|4");
        		return jo.toJSONString();
        	}
        	else if (response.equals("N")) {
        		if (update == false) {
        			CovidServer.jsonArray.add(korisnik);
        		}	
        		korisnik=null;
                jo = new JSONObject();
        	    message = "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
        	    jo.put("cmd", "MENU");
        	    jo.put("message", message);
        	    jo.put("regexp", "3|4|5|10");
        	    save_database();
                return jo.toJSONString();
        	}
        case "MENU1.15":   
        	vakcina = new LinkedHashMap(2);
        		if (response.equals("1")) {
        			vakcina.put("sifra_v", "1");	
        		    vakcina.put("proizvodjac_v", "Fajzer");
        		}
        		else if (response.equals("2")) {
        			vakcina.put("sifra_v", "2");
            		vakcina.put("proizvodjac_v", "AstraZeneka");
        		}
        		else if (response.equals("3")) {
        			vakcina.put("sifra_v", "3");
            		vakcina.put("proizvodjac_v", "Sputnjik V");
        		}
        		else if (response.equals("4")) {
        			vakcina.put("sifra_v", "4");
            		vakcina.put("proizvodjac_v", "Sinofarm");
        		}
        	jo = new JSONObject();
            message = "COVID PASS" + "\n\n" + "Unesite datum vakcinacije u formatu dd/mm/yyyy?";
        	jo.put("cmd", "MENU1.16");
        	jo.put("message", message);
        	jo.put("regexp", "^(3[01]|[12][0-9]|0[1-9])\\/(1[0-2]|0[1-9])\\/2021$");   
        	Map ddd = ((Map) korisnik.get("druga_doza"));
            jo.put("datum_v", ddd.get("datum_v"));
        	return jo.toJSONString();
        case "MENU1.16":   
        	vakcina.put("datum_v", response);
        	korisnik.put("treca_doza", vakcina);
        	jo = new JSONObject();
    	    message = "COVID PASS" + "\n\n" + "3. Unos statusa vakcinacije" + "\n" + "4. Provjera kovid propusnice" + "\n" + "5. Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
    	    jo.put("cmd", "MENU");
    	    jo.put("message", message);
    	    jo.put("regexp", "3|4|5|10");
    	    if (update == false) {
    	    	CovidServer.jsonArray.add(korisnik);
    	    }
    	    korisnik=null;
            save_database();
    	    return jo.toJSONString();
        case "MENU2.1":
        	username =response;
    		message = "COVID PASS" + "\n\n" + "Unesite Vasu korisnicku lozinku:";
    		jo = new JSONObject();
    	    jo.put("cmd", "MENU2.2");
    	    jo.put("message", message);
    	    jo.put("regexp", "[a-zA-Z.]*");
    	    return jo.toJSONString();	
        case "MENU2.2":
        	password =response;
        	Iterator<JSONObject> iterator = CovidServer.jsonArray.iterator();
	        while (iterator.hasNext()) {
	          JSONObject value = iterator.next();
	          if (value.get("korisnik").equals(username) & value.get("lozinka").equals(password)) {
	        	    jo = new JSONObject();  
	        	    if (username.equals("admin")) {
	 	        	    message = "COVID PASS" + "\n\n" + "Pregled statistike o vakcinaciji" + "\n" + "6.  Provjera statusa korisnika" + "\n" + "7.  Lista svih korisnika i nihov status" + "\n" + "8.  Pregled ukupnog broja vakcinisanih (I,II,III doza)" + "\n" + "9.  Pregled ukupnog broja vakcinisanih po proizvodjacu vakcine" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (6|7|8|9|10|):";
	        	    	jo.put("regexp", "6|7|8|9|10");
	        	    }
	        	    else {
		        	    message = "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
		        	    jo.put("regexp", "3|4|5|10");
	        	    }
	        	    jo.put("cmd", "MENU");
	        	    jo.put("message", message);
	        	    return jo.toJSONString();	  
	           }
	        }
	        		message = "COVID PASS" + "\n\n" +"Neuspjesno logovanje" + "\n\n" + "1.  Registracija korisnika" + "\n" + "2.  Logovanje na sistem" + "\n" + "10. Izlaz iz programa" + "\n" +"Vas izbor (1|2|10):";
	        	    jo = new JSONObject();  
	        	    jo.put("cmd", "MENU");
	        	    jo.put("message", message);
	        	    jo.put("regexp", "1|2|10");
	        	    return jo.toJSONString();	
        case "MENU6.1":
        	if ((isJMBGExists(response))!=null) {
        		jo = new JSONObject();  
        		message = isJMBGExists(response);
        	    jo.put("cmd", "ADMIN.1");
        	    jo.put("message", message);
        	    jo.put("regexp", "");
        	    return jo.toJSONString();	
        	}	
        	else {
        		jo = new JSONObject();  
	        	message = "COVID PASS" + "\n\n" + "Ne postoji korisnik sa ovim JMBG!" + "\n\n" + "Pregled statistike o vakcinaciji" + "\n" + "6.  Provjera statusa korisnika" + "\n" + "7.  Lista svih korisnika i nihov status" + "\n" + "8.  Pregled ukupnog broja vakcinisanih (I,II,III doza)" + "\n" + "9.  Pregled ukupnog broja vakcinisanih po proizvodjacu vakcine" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (6|7|8|9|10|):";
        		jo.put("cmd", "MENU");
        		jo.put("message", message);
        		jo.put("regexp", "6|7|8|9|10");
        		return jo.toJSONString();
        	}
        case "MENU_ADMIN":
         		jo = new JSONObject();  
	        	message = "COVID PASS" + "\n\n" + "Pregled statistike o vakcinaciji" + "\n" + "6.  Provjera statusa korisnika" + "\n" + "7.  Lista svih korisnika i nihov status" + "\n" + "8.  Pregled ukupnog broja vakcinisanih (I,II,III doza)" + "\n" + "9.  Pregled ukupnog broja vakcinisanih po proizvodjacu vakcine" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (6|7|8|9|10|):";
    	    	jo.put("cmd", "MENU");
        	    jo.put("message", message);
        	    jo.put("regexp", "6|7|8|9|10");
        	    return jo.toJSONString();       
        case "MENU_USER":
     		jo = new JSONObject();  
     		message = "COVID PASS" + "\n\n" + "3.  Unos statusa vakcinacije" + "\n" + "4.  Provjera kovid propusnice" + "\n" + "5.  Generisanje kovid propusnice" + "\n" +  "10. Izlaz iz programa" + "\n" + "Vas izbor (3|4|5|10|):";
  	    	jo.put("cmd", "MENU");
    	    jo.put("message", message);
    	    jo.put("regexp", "3|4|5|10");
    	    return jo.toJSONString();       
        case "ADMIN":
     		jo = new JSONObject();  
        	message = "PRESS ENTER";
	    	jo.put("cmd", "MENU_ADMIN");
    	    jo.put("message", message);
    	    jo.put("regexp", "");
    	    return jo.toJSONString();   
        case "USER":
     		jo = new JSONObject();  
        	message = "PRESS ENTER";
	    	jo.put("cmd", "MENU_USER");
    	    jo.put("message", message);
    	    jo.put("regexp", "");
    	    return jo.toJSONString();   
        case "BYE":  
              
        	System.exit(0) ;
		}
		
		
	}
	catch (ParseException e) {
		System.out.println("Problem u funkciji commands()");
	}
return null;

}



public void run() {
String odgovor_klijenta;

try {
	ulazniTokOdKlijenta = new BufferedReader (new InputStreamReader(soketZaKom.getInputStream()));
	izlazniTokKaKlijentu = new PrintStream(soketZaKom.getOutputStream());     
	izlazniTokKaKlijentu.println(LogInSignIn());

	while (true) {
		odgovor_klijenta = ulazniTokOdKlijenta.readLine();
	    	izlazniTokKaKlijentu.println(commands(odgovor_klijenta));
		}
	}
    catch (SocketException e) {
		 for (int i = 0; i <=9; i++) {
				if (klijenti[i]==this) {
					klijenti[i]=null;
					break;
				}	
			}
    }
	catch (NoSuchElementException e) {
        System.out.println("CTRL C Program stops");
        for (int i = 0; i <=9; i++) {
			if (klijenti[i]==this) {
				klijenti[i]=null;
				break;
			}	
		}
    }
	catch (IOException e) {
	System.out.println(e);
	System.out.println("Dovidjenja!");
}
}
}





