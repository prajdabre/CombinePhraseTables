package combinephrasetables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
    public static String phrase_tables_base = "/home/prajdabre/ResearchWork/smt-systems/TM/n-way/";
    public static String L1_Pivot = "phrase-table.en.cr";
    public static String Pivot_L2 = "phrase-table.cr.fr";
    public static String final_L1_l2 = "phrase-table.L1-L2";
    public static BufferedReader L1_Pivot_Reader = null;
    public static BufferedReader Pivot_L2_Reader = null;
    public static BufferedWriter final_L1_l2_Writer = null;
    public static HashMap<String, BufferedWriter> final_L1_l2_Writer_Set = new HashMap<String, BufferedWriter>();
    public static String corpus_base = "/home/prajdabre/ResearchWork/corpora/Bilingual/HI-JP/n-way";
    public static String L1_Pivot_Corpus_Path = null;
    public static String Pivot_L2_Corpus_Path = null;
    public static String kanji_hanzi_map = "/home/prajdabre/Downloads/NetBeansProjects-master/CombinePhraseTables/kanji_mapping_table.txt";
    public static BufferedReader kanji_hanzi_reader = null;

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

            L1_Pivot_Reader = new BufferedReader(new FileReader(phrase_tables_base + "/" + src + "-" + pivot + "/model/" + "phrase-table"));
            Pivot_L2_Reader = new BufferedReader(new FileReader(phrase_tables_base + "/" + pivot + "-" + tgt + "/model/" + "phrase-table"));

            final_L1_l2_Writer = new BufferedWriter(new FileWriter(phrase_tables_base + "/" + src + "-" + tgt + "/" + "phrase-table-" + src + "-" + pivot + "-" + tgt + "-" + CombinePhraseTablesLowMemWithoutLangSim.cutoff.toString()));

            L1_Pivot_Corpus_Path = corpus_base + "/" + src + "-" + pivot + "/corpus." + src + "-" + pivot;
            Pivot_L2_Corpus_Path = corpus_base + "/" + pivot + "-" + tgt + "/corpus." + pivot + "-" + tgt;
            System.out.println("Files opened for read and write");
        } catch (Exception ex) {
            Logger.getLogger(Files.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void open_files_low_mem(String src, String pivot, String tgt) {
        try {

            L1_Pivot_Reader = new BufferedReader(new FileReader(phrase_tables_base + "/" + src + "-" + pivot + "/model/" + "phrase-table"));
            Pivot_L2_Reader = new BufferedReader(new FileReader(phrase_tables_base + "/" + pivot + "-" + tgt + "/model/" + "phrase-table"));

            //final_L1_l2_Writer = new BufferedWriter(new FileWriter(phrase_tables_base+"/" + src+"-"+tgt+"/" + "phrase-table-" + src + "-" + pivot + "-" + tgt+"-mod-"+CombinePhraseTablesLowMemJapPivChi.cut_off_type));
            if (CombinePhraseTablesLowMemJapPivChi.cut_off_type.equals("both")) {
                for (Double cutoff_prob : CombinePhraseTablesLowMemJapPivChi.cutoff) {
                    for (Integer cutoff_count_each : CombinePhraseTablesLowMemJapPivChi.cutoff_count) {
                        System.out.println(cutoff_prob+" "+cutoff_count_each);
                        BufferedWriter temp = new BufferedWriter(new FileWriter(phrase_tables_base + "/" + src + "-" + tgt + "/" + "phrase-table-" + src + "-" + pivot + "-" + tgt + "-" + "char-prob-" + cutoff_prob + "-" + cutoff_count_each));
                        final_L1_l2_Writer_Set.put("char-prob-" + cutoff_prob + "-" + cutoff_count_each, temp);
                    }
                }
            } else if (CombinePhraseTablesLowMemJapPivChi.cut_off_type.equals("prob")) {
                for (Double cutoff_prob : CombinePhraseTablesLowMemJapPivChi.cutoff) {
                    BufferedWriter temp = new BufferedWriter(new FileWriter(phrase_tables_base + "/" + src + "-" + tgt + "/" + "phrase-table-" + src + "-" + pivot + "-" + tgt + "-" + "prob-" + cutoff_prob));
                    final_L1_l2_Writer_Set.put("prob-" + cutoff_prob, temp);

                }
            } else if (CombinePhraseTablesLowMemJapPivChi.cut_off_type.equals("char")) {
                for (Integer cutoff_count_each : CombinePhraseTablesLowMemJapPivChi.cutoff_count) {
                    BufferedWriter temp = new BufferedWriter(new FileWriter(phrase_tables_base + "/" + src + "-" + tgt + "/" + "phrase-table-" + src + "-" + pivot + "-" + tgt + "-" + "char-" + cutoff_count_each));
                    final_L1_l2_Writer_Set.put("char-" + cutoff_count_each, temp);
                }
            }

            L1_Pivot_Corpus_Path = corpus_base + "/" + src + "-" + pivot + "/corpus." + src + "-" + pivot;
            Pivot_L2_Corpus_Path = corpus_base + "/" + pivot + "-" + tgt + "/corpus." + pivot + "-" + tgt;
            kanji_hanzi_reader = new BufferedReader(new FileReader(kanji_hanzi_map));
            System.out.println("Files opened for read and write");
        } catch (Exception ex) {
            Logger.getLogger(Files.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void close_files() throws IOException {
        L1_Pivot_Reader.close();
        Pivot_L2_Reader.close();
        //final_L1_l2_Writer.close();
        for(String s:final_L1_l2_Writer_Set.keySet()){
            final_L1_l2_Writer_Set.get(s).close();
        }
    }
}
