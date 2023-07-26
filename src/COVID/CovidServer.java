package COVID;
import java.io.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class CovidServer {

static CovidServerNit klijenti[] = new CovidServerNit[10];
static JSONArray jsonArray = new JSONArray();;
static File database =new File("C://CovidDatabase.json");
static ServerSocket serverSoket;

 private static void save_database() {
	try {
		FileWriter fw;
		fw = new FileWriter(CovidServer.database,false);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
	    pw.write(jsonArray.toJSONString());
	    pw.flush();
	    pw.close();
		} 
	catch (Exception e) {
		System.out.println("Problem u kreiranju database fajla");
		System.exit(0);
		}
}
 
@SuppressWarnings("unchecked")
private static void create_admin_account()  {
	JSONObject jo = new JSONObject();
	jo.put("korisnik", "admin");
    jo.put("lozinka", "pass");
    jsonArray.add(jo);
    try {
		save_database();
	    jo = null;
	} catch (Exception e) {
		System.out.println("Problem u kreiranju administratorskog naloga");
		System.exit(0);
	}
}

public static void main(String[] args) {
	int port = 0;
	switch (args.length) {
		case 0:
			port = 15000;
			break;
		case 1:
			
			 try {
				 port = Integer.parseInt(args[0]);
				  } 
			 catch (NumberFormatException e) {
				 System.out.println("Arguments: PortNumber");
				 System.exit(0) ;
				  }
			break;
		default:
			System.out.println("Arguments: PortNumber");
			System.exit(0) ;
	}
		
	Socket klijentSoket = null;
	   Object obj;
	   try {
			if(CovidServer.database.exists())   {
				obj = new JSONParser().parse(new FileReader(database));
				jsonArray =  (JSONArray) obj;
			}
			else
			{   
				database.createNewFile();
				create_admin_account();     	    		
			}
	   }  
	   catch (IOException|ParseException e) {
		   	 System.out.println("Problem sa ucitavanjem database fajla");
		}
	
            Thread konzola= new Thread(new Runnable() {
	                Scanner ulazSaKonzole = null;
	                @Override
	                public void run() {
	                	String unosSaKonzole;
	                    try {
	                    	ulazSaKonzole =  new Scanner(System.in);
	                        while(true){
	                        	unosSaKonzole = ulazSaKonzole.nextLine();
	                               	switch (unosSaKonzole) {
	                               		case "REG":  
	                               			int aktivni_korisnici=0;
	                               			for(int i=0; i<=9; i++) 
	                               					if (klijenti[i] != null) aktivni_korisnici++;
	                               			System.out.println("Broj aktivnih korisnika: " + aktivni_korisnici);		
	                               			break;
	                               		case "BYE":    
	                               			serverSoket.close();
	                               			System.exit(0);
	                               	}
	                        }
	                    } 
	                    catch (NoSuchElementException e) {
	            	        System.out.println("CTRL C Program stops");
	            	     }
	                    catch (Exception e) {
	                    	System.out.println("Greska u radu sa konzolom");
	                    	System.exit(0);
	                    }
	                }
	            });
            konzola.start();

      try { 
    		System.out.println("Server started");
    		serverSoket = new ServerSocket(port);		
				
			while(true) {
				klijentSoket = serverSoket.accept();
				for(int i=0; i<=9; i++) {
					if (klijenti[i] == null) {
						klijenti[i] = new CovidServerNit(klijentSoket, klijenti);
						klijenti[i].start();
						break;
					}
				}
			}
		}
	    catch (NoSuchElementException e) {
	         System.out.println("CTRL C Program stops");
	    }
        catch (SocketException e) {
   		 System.out.println("Port je zauzet");
   	     System.exit(0);
  	    }
	    catch (Exception e) {
	    	if (serverSoket.isClosed())
	    		System.out.println("Dovidjenja");
	    	else
	    		System.out.println("Greska u radu sa serverskim servisom!");
	    		System.exit(0);
       }
}
}
