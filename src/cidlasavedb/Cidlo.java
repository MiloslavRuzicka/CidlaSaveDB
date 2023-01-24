package cidlasavedb;

import java.sql.SQLException;
import java.time.LocalTime;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Milos
 */
class Cidlo {
    
    public String nazev;
    public float hodnota;
    public String adresa;
    
    public void load(Node node){
        NodeList deti = node.getChildNodes();
        for (int i=0; i< deti.getLength(); i++){
            Node nod = deti.item(i);
            //System.out.println("JEBAT "+ nod.getNodeName());
            if (nod.getNodeName().toLowerCase().equals("adresa") )
                adresa = nod.getTextContent();
            if (nod.getNodeName().toLowerCase().equals("nazev") )
                nazev = nod.getTextContent();
            if (nod.getNodeName().toLowerCase().equals("hodnota") )
                hodnota = Float.valueOf(nod.getTextContent());
        }
    }
    
    public void saveToDB(java.sql.Connection con, String tableSufix, int idDataDen, LocalTime cas) throws SQLException{
        System.out.println(nazev +" "+ adresa);
        int idCidlo = getIdByAdresa(con);
        if (idCidlo > -1){
            // podivam se na posledni zaznam, zda ma stejnou hodnotu
            String sql = " select idData, hodnota, cas "
                       + " from tblData"+ tableSufix 
                       + " where (idCidlo =?) and (idDataDen =?) "
                       + " order by cas desc limit 2";

            boolean canDoUpdate = false;
            int lastHodnota = 0;
            int lastIdData = 0;
            
            try(java.sql.PreparedStatement pst = con.prepareStatement(sql)){
                pst.setInt(1, idCidlo);
                pst.setInt(2, idDataDen);
                try(java.sql.ResultSet rs = pst.executeQuery()){
                    if (rs.next()) {                      
                        lastHodnota = Math.round(rs.getFloat("hodnota") * 10);
                        lastIdData  = rs.getInt("idData");

                        if (rs.next()){
                            LocalTime lastCas = rs.getTime("cas").toLocalTime();
                            
                            int diff = Math.abs(cas.toSecondOfDay() -lastCas.toSecondOfDay());
                            canDoUpdate = diff/60 < 60; // minimalne kazdou hodinu zaznam do DB
                        }
                    }

                    if ((lastHodnota == Math.round(hodnota * 10)) && (lastIdData > 0) && (canDoUpdate)){
                        // update - nove namerena hodnota ve stejny den je stejna jako predchozi
                        try(java.sql.PreparedStatement pstW = con.prepareStatement(" update tblData"+ tableSufix 
                                                                                  +" set Cas=? "
                                                                                  +" where idData=?")){
                            pstW.setTime(1, java.sql.Time.valueOf(cas));
                            pstW.setInt(2, lastIdData);
                            pstW.executeUpdate();
                            System.out.println("update");
                        }
                    }
                    else {
                        // insert - nova hodnota se lisi od predchozi
                        try(java.sql.PreparedStatement pstW = con.prepareStatement(" insert into tblData"+ tableSufix
                                                                                  +" (idData, idCidlo, idDataDen, Cas, Hodnota) "
                                                                                  +" values (?,?,?,?,?)")){
                            pstW.setNull(1, java.sql.Types.INTEGER);
                            pstW.setInt(2, idCidlo);
                            pstW.setInt(3, idDataDen);
                            pstW.setTime(4, java.sql.Time.valueOf(cas));
                            pstW.setFloat(5, hodnota);
                            pstW.execute();
                            System.out.println("insert");
                        }
                    }
                }
            }
        }
    }
    
    public int getIdByAdresa(java.sql.Connection con) throws SQLException{
        try(java.sql.PreparedStatement pst = con.prepareStatement("select idCidlo from tblCidlo where Adresa like ?")){
            pst.setString(1, adresa);
            try(java.sql.ResultSet rs = pst.executeQuery()){
                if (rs.next()){
                    return rs.getInt("idCidlo");
                }
                else{
                    return -1;
                }
            }
        }
    }
}
