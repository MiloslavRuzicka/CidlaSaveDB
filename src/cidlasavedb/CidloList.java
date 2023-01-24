/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cidlasavedb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Milos
 */
public class CidloList extends ArrayList<Cidlo> {
    
    public void load(Node node){
        this.clear();
        NodeList deti = node.getChildNodes();
        for(int i=0; i<deti.getLength(); i++){
            Cidlo cidlo = new Cidlo();
            cidlo.load(deti.item(i));
            this.add(cidlo);
        }
    }
    
    public void saveToDB(java.sql.Connection con, String tableSufix, LocalDateTime ted) throws SQLException{
        int idDataDen = getDateDenId(con, tableSufix, ted.toLocalDate());
        if (idDataDen > -1){
            // vlozim vlastni data cidel
            for (Cidlo cidlo: this){
                cidlo.saveToDB(con, tableSufix, idDataDen, ted.toLocalTime());
            }
        }
    }
    
    private int getDateDenId(java.sql.Connection con, String tableSufix, LocalDate datum) throws SQLException{
        PreparedStatement pst = con.prepareStatement( "select idDataDen from tblDataDen"+ tableSufix +" where datum =?");
        java.sql.Date datumSql = java.sql.Date.valueOf(datum);
        pst.setDate(1, datumSql);
        java.sql.ResultSet rs = pst.executeQuery();
        if (rs.next()){
            return rs.getInt("idDataDen");
        }
        else{
            pst = con.prepareStatement("insert into tblDataDen"+ tableSufix +" (idDataDen, datum) values (?,?)", Statement.RETURN_GENERATED_KEYS);
            pst.setNull(1, java.sql.Types.INTEGER);
            pst.setDate(2, datumSql);
            pst.execute();
            rs = pst.getGeneratedKeys();
            if (rs.next()){
                int id = rs.getInt(1);   
                //System.out.println("Auto Generated Primary Key " + id);
                return id;
            }            
        }
        return -1;
    }
}
