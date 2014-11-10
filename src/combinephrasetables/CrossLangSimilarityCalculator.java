/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package combinephrasetables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.fluent.Dictionary.on;
import static org.grep4j.core.Grep4j.grep;
import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;

/**
 *
 * @author prajdabre
 */
public class CrossLangSimilarityCalculator {

    public static Profile L1_Pivot_Corpus = null;
    public static Profile Pivot_L2_Corpus = null;
    public static GrepResults L1_P_Results = null;
    public static GrepResults P_L2_Results = null;
    public static Set<String> piv_words = new HashSet<String>();
    public static HashMap<String, Integer> f_piv_count = new HashMap<String, Integer>();
    public static HashMap<String, Integer> piv_e_count = new HashMap<String, Integer>();

    public void load_corpora_for_grepping(String L1_Pivot_Path, String Pivot_L2_Path) {
        L1_Pivot_Corpus = ProfileBuilder.newBuilder().name("L1_Pivot_Corpus for grepping").filePath(L1_Pivot_Path).onLocalhost().build();
        Pivot_L2_Corpus = ProfileBuilder.newBuilder().name("Pivot_L2_Corpus for grepping").filePath(Pivot_L2_Path).onLocalhost().build();
    }

    public GrepResults load_L1_L2_grep(String p1, String p2, Profile L1_L2_Corpus, Set<String> piv_words, HashMap<String, Integer> f_e_count, int direction){
        GrepResults L1_L2_Results = grep(constantExpression(" "+p1+" "), on(L1_L2_Corpus));
        L1_L2_Results = L1_L2_Results.filterBy(constantExpression(" "+p2+" "));
        //System.out.println(L1_L2_Results);
        if(L1_L2_Results.isEmpty()){
            return null;
        } else {
            for (GrepResult single_result : L1_L2_Results) {
            //System.out.println(single_result);
            String piv_sents[] = single_result.getText().split("\n");
            for (String piv_sent : piv_sents) {
                piv_sent = piv_sent.trim().split("\t")[direction].trim().replaceAll(" "+p2+" ", "").replaceAll("[ ][ ]*", " ").trim();
                if (piv_sent.length() == 0 || piv_sent.equals(" ")) {
                    continue;
                }
                for (String piv_word : piv_sent.split(" ")) {
                    piv_words.add(piv_word);
                    if (f_e_count.containsKey(piv_word)) {
                        f_e_count.put(piv_word, f_e_count.get(piv_word) + 1);
                    } else {
                        f_e_count.put(piv_word, 1);
                    }
                }
            }
        }
            return L1_L2_Results;
        }
    }
    
    public Double generate_cross_language_similarities() {
        

        if ( L1_P_Results==null || P_L2_Results==null) {
            return 0.0;
        }

       
        Double f_piv_norm = Generate_Norm(new ArrayList<Integer>(f_piv_count.values()));
        Double piv_e_norm = Generate_Norm(new ArrayList<Integer>(piv_e_count.values()));
        Double similarity = 0.0;
        for (String piv_word : piv_words) {
            Integer count1 = 0;
            Integer count2 = 0;
            if (f_piv_count.containsKey(piv_word)) {
                count1 = f_piv_count.get(piv_word);
            }
            if (piv_e_count.containsKey(piv_word)) {
                count2 = piv_e_count.get(piv_word);
            }

            similarity += count1 * count2;

        }

//        System.out.println(similarity);
//        System.out.println(piv_e_norm);
        if (similarity == 0 || f_piv_norm == 0 || piv_e_norm == 0) {
            return 0.0;
        } else {
            return similarity / (f_piv_norm * piv_e_norm);
        }

    }

    public Double Generate_Norm(ArrayList<Integer> counts) {
        Double norm = 0.0;
        for (Integer val : counts) {
            norm += val * val;
        }
        return Math.sqrt(norm);
    }

    public static void main(String args[]) {
        CrossLangSimilarityCalculator clsc = new CrossLangSimilarityCalculator();
        clsc.load_corpora_for_grepping("/home/prajdabre/ResearchWork/corpora/Bilingual/HI-JP/n-way/Jap-Mar/corpus.Jap-Mar", "/home/prajdabre/ResearchWork/corpora/Bilingual/HI-JP/n-way/Mar-Hin/corpus.Mar-Hin");

        //clsc.load_L1_L2_grep(null, null, L1_P_Results, L1_Pivot_Corpus);
        //GrepResults results = grep(constantExpression("दुसऱ्या दिवशी"), on(L1_Pivot_Corpus));
        L1_P_Results=clsc.load_L1_L2_grep("ところが 、 わたし", "गरोदर",  L1_Pivot_Corpus, piv_words, f_piv_count,1);
        //System.out.println(L1_P_Results);
        P_L2_Results=clsc.load_L1_L2_grep("गर्भवती", "गरोदर",  Pivot_L2_Corpus, piv_words, piv_e_count,0);
        //System.out.println(P_L2_Results);
        //System.out.println(f_piv_count);
        
        System.out.println(clsc.generate_cross_language_similarities());

    }

}
