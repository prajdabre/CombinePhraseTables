

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author raj
 */
public class Files {

    public static String base = "/home/raj/NetBeansProjects/CombinePhraseTables/";
    public static String phrase_tables_base = "/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/";
    public static String LM_base = "/home/prajdabre/ResearchWork/smt-systems/LM/";
    public static String L1_Pivot = "phrase-table.en.cr";
    public static String Pivot_L2 = "phrase-table.cr.fr";
    public static String final_L1_l2 = "phrase-table.L1-L2";
    public static BufferedReader L1_Pivot_Reader = null;
    public static BufferedReader Pivot_L2_Reader = null;
    public static BufferedWriter final_L1_l2_Writer = null;

    
    public static void open_files() {
        try {
            L1_Pivot_Reader = new BufferedReader(new FileReader(L1_Pivot));
            Pivot_L2_Reader = new BufferedReader(new FileReader(Pivot_L2));
            final_L1_l2_Writer = new BufferedWriter(new FileWriter(final_L1_l2));


            System.out.println("Files opened for read and write");
        } catch (Exception ex) {
            Logger.getLogger(Files.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void open_files(String src, String pivot, String tgt) {
        try {

            L1_Pivot_Reader = new BufferedReader(new FileReader(phrase_tables_base + src + "-" + pivot + "/" + "phrase-table"));
            Pivot_L2_Reader = new BufferedReader(new FileReader(phrase_tables_base + pivot + "-" + tgt + "/" + "phrase-table"));

            final_L1_l2_Writer = new BufferedWriter(new FileWriter(phrase_tables_base+"/" + src+"-"+tgt+"/" + "phrase-table-" + src + "-" + pivot + "-" + tgt));


            System.out.println("Files opened for read and write");
        } catch (Exception ex) {
            Logger.getLogger(Files.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

