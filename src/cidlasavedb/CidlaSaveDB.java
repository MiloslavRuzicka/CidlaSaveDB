/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cidlasavedb;

import database.DBInterface;
import database.MariaDB;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Milos
 */
public class CidlaSaveDB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //List<String> lines = Arrays.asList("The first line", "The second line");
        //Path file = Paths.get("/volume1/web/the-file-name.txt");
        //Files.write(file, lines, StandardCharsets.UTF_8);        
        try{
            URL url = new URL("http://10.0.0.130/synology.html");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }

                if (sb.length() > 10){
                    LocalDateTime ted = LocalDateTime.now();

                    Document doc = loadXMLFromString(sb.toString());
                    if (doc != null){
                        DBInterface db = new MariaDB();
                        db.setDatabazeProp("Cidla", "10.0.0.139", "3307");
                        db.initDriver();
                        java.sql.Connection con = db.getConnection();
                        con.setAutoCommit(false);

                        Element elem = doc.getDocumentElement();
                        if (elem != null && elem.getNodeName().toLowerCase().equals("xml")){
                            NodeList deti = elem.getChildNodes();
                            for (int i=0; i< deti.getLength(); i++){
                                Node nod = deti.item(i);
                                if (nod.getNodeName().toLowerCase().equals("teplota")){
                                    CidloList teploty = new CidloList();
                                    teploty.load(nod.getFirstChild());
                                    teploty.saveToDB(con, "T", ted);
                                }
                                if (nod.getNodeName().toLowerCase().equals("vlhkost")){
                                    CidloList vlhko = new CidloList();
                                    vlhko.load(nod.getFirstChild());
                                    vlhko.saveToDB(con, "V", ted);
                                }
                            }
                        }
                        con.commit();
                    }
                }
            }
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }
    
    public static Document loadXMLFromString(String xml)
    {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }
}
