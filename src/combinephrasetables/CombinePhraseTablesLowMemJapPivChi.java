/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combinephrasetables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raj
 */
public class CombinePhraseTablesLowMemJapPivChi {

    /**
     * @param args the command line arguments
     */
    BiMap<String, Integer> L1_phrases_String_number = HashBiMap.create();
    BiMap<String, Integer> Pivot_phrases_String_number = HashBiMap.create();
    BiMap<String, Integer> L2_phrases_String_number = HashBiMap.create();
    BiMap<Integer, String> L1_phrases_number_String = HashBiMap.create();
    BiMap<Integer, String> Pivot_phrases_number_String = HashBiMap.create();
    BiMap<Integer, String> L2_phrases_number_String = HashBiMap.create();
    BiMap<String, Integer> L1_words_String_number = HashBiMap.create();
    BiMap<String, Integer> Pivot_words_String_number = HashBiMap.create();
    BiMap<String, Integer> L2_words_String_number = HashBiMap.create();
    HashMap<String, ArrayList<String>> Kanji_to_Hanzi_Map = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> Hanzi_to_Kanji_Map = new HashMap<String, ArrayList<String>>();

    HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_Pivot_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, scores_and_alignments>> Pivot_L2_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_L2_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, Double>> L1_L2_counts = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L2_L1_counts = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L1_L2_probabilities = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L2_L1_probabilities = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, Double> L2_counts = new HashMap<Integer, Double>();
    HashMap<Integer, Double> L1_counts = new HashMap<Integer, Double>();
    HashSet<String> all_kanji = new HashSet<String>();
    HashSet<String> all_hanzi = new HashSet<String>();
    CrossLangSimilarityCalculator clsc = new CrossLangSimilarityCalculator();
    static Double cutoff[] = new Double[]{0.001};
    static Integer cutoff_count[] = new Integer[]{20};
    static String[] pivots = new String[]{"Eng"};
    static String cut_off_type = "";
    int i = 0; // to make sure that formatting does not screw up
    static String src = "";
    static String tgt = "";

    public void read_table(BufferedReader br, BiMap<String, Integer> L1_words_String_number, BiMap<String, Integer> L2_words_String_number, BiMap<String, Integer> L1_phrases_String_number, BiMap<String, Integer> L2_phrases_String_number, HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_L2_Maps, Integer L1_phrase_Count, Integer L2_phrase_Count, Integer L1_word_count, Integer L2_word_count) {

        try {
            String line = "";
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String components[] = line.split("\\|\\|\\|");
                String inphrase = components[0].trim();
                String outphrase = components[1].trim();
                String scores[] = components[2].trim().split(" ");
                Double p_f_e = Double.parseDouble(scores[0]);
                Double l_f_e = Double.parseDouble(scores[1]);
                Double p_e_f = Double.parseDouble(scores[2]);
                Double l_e_f = Double.parseDouble(scores[3]);

                String alignment_str = components[3].trim();
                HashMap<Integer, HashMap<Integer, Integer>> alignments = generate_alignment_map(alignment_str);
                String counts[] = components[4].trim().split(" ");

                double scores_arr[] = new double[4];
                scores_arr[0] = p_f_e;
                scores_arr[1] = l_f_e;
                scores_arr[2] = p_e_f;
                scores_arr[3] = l_e_f;

                if (!L1_phrases_String_number.containsKey(inphrase)) {
                    L1_phrases_String_number.put(inphrase, L1_phrase_Count);

                    L1_phrase_Count++;
                }
                if (!L2_phrases_String_number.containsKey(outphrase)) {
                    L2_phrases_String_number.put(outphrase, L2_phrase_Count);

                    L2_phrase_Count++;
                }
                String words[] = inphrase.split(" ");

                for (String word : words) {
                    word = word.trim();
                    if (!L1_words_String_number.containsKey(word)) {
                        L1_words_String_number.put(word, L1_word_count);

                        L1_word_count++;
                    }
                }

                words = outphrase.split(" ");
                for (String word : words) {
                    word = word.trim();
                    if (!L2_words_String_number.containsKey(word)) {
                        L2_words_String_number.put(word, L2_word_count);

                        L2_word_count++;
                    }
                }

                Integer L1_id = L1_phrases_String_number.get(inphrase);
                Integer L2_id = L2_phrases_String_number.get(outphrase);
                scores_and_alignments saa = new scores_and_alignments(scores_arr, alignments);
                if (L1_L2_Maps.containsKey(L1_id)) {
                    HashMap<Integer, scores_and_alignments> temp = L1_L2_Maps.get(L1_id);
                    temp.put(L2_id, saa);
                    L1_L2_Maps.put(L1_id, temp);
                } else {
                    HashMap<Integer, scores_and_alignments> temp = new HashMap<Integer, scores_and_alignments>();
                    temp.put(L2_id, saa);
                    L1_L2_Maps.put(L1_id, temp);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(CombinePhraseTablesLowMemJapPivChi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<Integer, HashMap<Integer, Integer>> generate_alignment_map_advanced(String alignment_str, String inphrase, String outphrase, HashMap<String, Integer> L1_word_string_number, HashMap<String, Integer> L2_word_string_number) {
        HashMap<Integer, HashMap<Integer, Integer>> alignments = new HashMap<Integer, HashMap<Integer, Integer>>();
        String components[] = alignment_str.split(" ");
        for (String s : components) {
            Integer src = Integer.parseInt(s.split("-")[0]);
            Integer tgt = Integer.parseInt(s.split("-")[1]);
            if (alignments.containsKey(src)) {
                HashMap<Integer, Integer> tgts = alignments.get(src);
                tgts.put(tgt, 1);
                alignments.put(src, tgts);
            } else {
                HashMap<Integer, Integer> tgts = new HashMap<Integer, Integer>();
                tgts.put(tgt, 1);
                alignments.put(src, tgts);
            }
        }
        return alignments;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> generate_alignment_map(String alignment_str) {
        HashMap<Integer, HashMap<Integer, Integer>> alignments = new HashMap<Integer, HashMap<Integer, Integer>>();
        String components[] = alignment_str.split(" ");
        for (String s : components) {
            Integer src = Integer.parseInt(s.split("-")[0]);
            Integer tgt = Integer.parseInt(s.split("-")[1]);
            if (alignments.containsKey(src)) {
                HashMap<Integer, Integer> tgts = alignments.get(src);
                tgts.put(tgt, 1);
                alignments.put(src, tgts);
            } else {
                HashMap<Integer, Integer> tgts = new HashMap<Integer, Integer>();
                tgts.put(tgt, 1);
                alignments.put(src, tgts);
            }
        }
        return alignments;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> induce_or_update_alignment_map(HashMap<Integer, HashMap<Integer, Integer>> alignment_L1_Pivot, HashMap<Integer, HashMap<Integer, Integer>> alignment_Pivot_L2, HashMap<Integer, HashMap<Integer, Integer>> alignment_L1_L2) {

        HashMap<Integer, HashMap<Integer, Integer>> new_alignments = alignment_L1_L2;
        for (Integer src : alignment_L1_Pivot.keySet()) {
            HashMap<Integer, Integer> pivot_maps = alignment_L1_Pivot.get(src);
            for (Integer pivot_map : pivot_maps.keySet()) {
                if (alignment_Pivot_L2.containsKey(pivot_map)) {
                    HashMap<Integer, Integer> tgts = alignment_Pivot_L2.get(pivot_map);
                    for (Integer tgt : tgts.keySet()) {
                        if (new_alignments.containsKey(src)) {
                            HashMap<Integer, Integer> temp = new_alignments.get(src);
                            if (temp.containsKey(tgt)) {
                                temp.put(tgt, temp.get(tgt) + 1);
                            } else {
                                temp.put(tgt, 1);
                            }
                            new_alignments.put(src, temp);
                        } else {
                            HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
                            temp.put(tgt, 1);
                            new_alignments.put(src, temp);
                        }
                    }

                }
            }
        }
        return new_alignments;
    }

    public void read_tables_and_corpora_into_memory() {
        System.out.println("Reading L1-Pivot table into memory");
        read_table(Files.L1_Pivot_Reader, L1_words_String_number, Pivot_words_String_number, L1_phrases_String_number, Pivot_phrases_String_number, L1_Pivot_Maps, 0, 0, 0, 0);
        System.out.println("Reading Pivot-L2 table into memory");
        read_table(Files.Pivot_L2_Reader, Pivot_words_String_number, L2_words_String_number, Pivot_phrases_String_number, L2_phrases_String_number, Pivot_L2_Maps, Pivot_phrases_String_number.size(), 0, Pivot_words_String_number.size(), 0);
        System.out.println(L1_words_String_number.size() + " " + Pivot_words_String_number.size() + " " + L2_words_String_number.size() + " " + L1_Pivot_Maps.size() + " " + Pivot_L2_Maps.size());

        L1_phrases_number_String = L1_phrases_String_number.inverse();
        L2_phrases_number_String = L2_phrases_String_number.inverse();
        Pivot_phrases_number_String = Pivot_phrases_String_number.inverse();

        System.out.println("Tables read into memory");

        System.out.println("Reading Kanji-Hanzi Map... all hail ChenHui Chu");
        read_hanzi_kanji();
        System.out.println("Done Reading Kanji-Hanzi Map...");
        //clsc.load_corpora_for_grepping(Files.L1_Pivot_Corpus_Path, Files.Pivot_L2_Corpus_Path);
        //System.out.println("Corpora read into memory for cross language similarity");
    }

    public int count_common() {
        System.out.println("Calculating common phrases");
        int count = 0;
        for (Integer i : L1_Pivot_Maps.keySet()) {
            Set<Integer> keySet = L1_Pivot_Maps.get(i).keySet();
            keySet.retainAll(Pivot_L2_Maps.keySet());
            count += keySet.size();
        }
        return count;
    }

    public void read_hanzi_kanji() {
        try {
            String line = "";
            while ((line = Files.kanji_hanzi_reader.readLine()) != null) {
                if (line.startsWith("#") || line.contains("N/A")) {
                    continue;
                } else {
                    String components[] = line.split("\t");
                    String kanji = components[0].trim();
                    ArrayList<String> hanzi = new ArrayList<String>(Arrays.asList(components[1].trim().split(",")));
                    Kanji_to_Hanzi_Map.put(kanji, hanzi);
                    for (String hanz : hanzi) {
                        if (Hanzi_to_Kanji_Map.containsKey(hanz)) {
                            ArrayList<String> temp = Hanzi_to_Kanji_Map.get(hanz);
                            temp.add(kanji);
                            Hanzi_to_Kanji_Map.put(hanz, temp);
                        } else {
                            ArrayList<String> temp = new ArrayList<String>();
                            temp.add(kanji);
                            Hanzi_to_Kanji_Map.put(hanz, temp);
                        }
                    }
                }
            }
            all_kanji.addAll(Kanji_to_Hanzi_Map.keySet());
            all_hanzi.addAll(Hanzi_to_Kanji_Map.keySet());
        } catch (IOException ex) {
            Logger.getLogger(CombinePhraseTablesLowMemJapPivChi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void compute_L1_L2_phrase_probabilities_via_pivot() {
        System.out.println("Generating L1-L2 phrase probabilities via pivot");
        Iterator it = L1_Pivot_Maps.entrySet().iterator();
        int counter = 0;
        //System.out.println("Number of source phrases: "+L1_Pivot_Maps.size());
        while (it.hasNext()) {
            counter++;
            Map.Entry pairs = (Map.Entry) it.next();
            Integer L1_key = (Integer) pairs.getKey();
            HashMap<Integer, scores_and_alignments> Pivot_maps = (HashMap<Integer, scores_and_alignments>) pairs.getValue();
            Set<Integer> Pivot_keys_common = Pivot_maps.keySet();
            Pivot_keys_common.retainAll(Pivot_L2_Maps.keySet());
            if (Pivot_keys_common.isEmpty()) {
                continue;
            }

            String src_phrase = L1_phrases_number_String.get(L1_key);
            String piv_phrase = "";
            String tgt_phrase = "";
            System.out.println("At phrase:" + counter + " Out of " + L1_Pivot_Maps.size() + " Common pivots: " + Pivot_keys_common.size());

            HashMap<Integer, scores_and_alignments> tgt_scores_and_alignments = new HashMap<Integer, scores_and_alignments>();
            for (Integer piv_key : Pivot_keys_common) {
                HashMap<Integer, scores_and_alignments> piv_tgt_scores = Pivot_L2_Maps.get(piv_key);
                piv_phrase = Pivot_phrases_number_String.get(piv_key);
                //CrossLangSimilarityCalculator.L1_P_Results = clsc.load_L1_L2_grep(src_phrase, piv_phrase, CrossLangSimilarityCalculator.L1_Pivot_Corpus, CrossLangSimilarityCalculator.piv_words, CrossLangSimilarityCalculator.f_piv_count,1);
                for (Integer l2_key : piv_tgt_scores.keySet()) {
                    //System.out.println("Done");
                    tgt_phrase = L2_phrases_number_String.get(l2_key);
                    //Double lang_sim = 0.000000000001;
                    //if(!(CrossLangSimilarityCalculator.L1_P_Results==null)){
                    //    CrossLangSimilarityCalculator.P_L2_Results = clsc.load_L1_L2_grep(tgt_phrase, piv_phrase, CrossLangSimilarityCalculator.Pivot_L2_Corpus, CrossLangSimilarityCalculator.piv_words, CrossLangSimilarityCalculator.piv_e_count,1);
                    //}
                    //if(!(CrossLangSimilarityCalculator.P_L2_Results==null)){
                    //    lang_sim = clsc.generate_cross_language_similarities();
                    //}
                    if (tgt_scores_and_alignments.containsKey(l2_key)) {
                        scores_and_alignments temp = tgt_scores_and_alignments.get(l2_key);
                        //temp.alignments_map = induce_or_update_alignment_map(L1_Pivot_Maps.get(L1_key).get(piv_key).alignments_map, Pivot_L2_Maps.get(piv_key).get(l2_key).alignments_map, temp.alignments_map);
                        temp.scores[0] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[0] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[0];
                        temp.scores[2] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[2] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[2];
                        temp.scores[1] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[1] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[1];
                        temp.scores[3] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[3] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[3];

                        tgt_scores_and_alignments.put(l2_key, temp);
                    } else {
                        scores_and_alignments temp = new scores_and_alignments();
                        //temp.alignments_map = induce_or_update_alignment_map(L1_Pivot_Maps.get(L1_key).get(piv_key).alignments_map, Pivot_L2_Maps.get(piv_key).get(l2_key).alignments_map, new HashMap<Integer, HashMap<Integer, Integer>>());
                        temp.scores[0] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[0] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[0];
                        temp.scores[2] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[2] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[2];
                        temp.scores[1] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[1] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[1];
                        temp.scores[3] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[3] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[3];
                        tgt_scores_and_alignments.put(l2_key, temp);
                    }

                }

            }

            if (cut_off_type.equals("both") || cut_off_type.equals("char")) {
                System.out.println("Before pruning: Number of Phrase Pairs were "+tgt_scores_and_alignments.size());
                //System.out.println(tgt_scores_and_alignments);
                tgt_scores_and_alignments = prune_by_kanji_hanzi(src_phrase, tgt_scores_and_alignments, cutoff_count[cutoff_count.length - 1]);
                System.out.println("After pruning: Number of Phrase Pairs were "+tgt_scores_and_alignments.size());
                //System.out.println(tgt_scores_and_alignments);
                //System.exit(1);
            }

            write_translations_for_phrase(src_phrase, tgt_scores_and_alignments);
            //L1_L2_Maps.put(L1_key, tgt_scores_and_alignments);

        }

        L1_Pivot_Maps = null;
        Pivot_L2_Maps = null;
        System.gc();
        System.out.println("Probabilities Calculated");
    }

//    public void compute_L1_L2_lexical_counts() {
//        System.out.println("Generating L1-L2 and L2-L1 lexical counts");
//        Iterator it = L1_L2_Maps.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry) it.next();
//            Integer L1_key = (Integer) pairs.getKey();
//            HashMap<Integer, scores_and_alignments> L2_alignments_and_scores = (HashMap<Integer, scores_and_alignments>) pairs.getValue();
//            Iterator it2 = L2_alignments_and_scores.entrySet().iterator();
//            while (it2.hasNext()) {
//                Map.Entry pairs2 = (Map.Entry) it2.next();
//                Integer L2_key = (Integer) pairs2.getKey();
//                scores_and_alignments s_a_l = (scores_and_alignments) pairs2.getValue();
//                Double p_f_e = s_a_l.scores[0];
//                Double p_e_f = s_a_l.scores[2];
//                String inphrase[] = L1_phrases_String_number.inverse().get(L1_key).split(" ");
//                String outphrase[] = L2_phrases_String_number.inverse().get(L2_key).split(" ");
//                for (int i = 0; i < inphrase.length; i++) {
//                    Integer L1_word_id = L1_words_String_number.get(inphrase[i]);
//                    if (s_a_l.alignments_map.containsKey(i)) {
//                        for (Integer s : s_a_l.alignments_map.get(i).keySet()) {
//                            Integer L2_word_id = L2_words_String_number.get(outphrase[s]);
//                            if (L1_L2_counts.containsKey(L1_word_id)) {
//                                HashMap<Integer, Double> L2_count = L1_L2_counts.get(L1_word_id);
//                                if (L2_count.containsKey(L2_word_id)) {
//                                    L2_count.put(L2_word_id, L2_count.get(L2_word_id) + p_f_e);
//                                } else {
//                                    L2_count.put(L2_word_id, p_f_e);
//                                }
//                                L1_L2_counts.put(L1_word_id, L2_count);
//                            } else {
//                                HashMap<Integer, Double> L2_count = new HashMap<Integer, Double>();
//                                L2_count.put(L2_word_id, p_f_e);
//                                L1_L2_counts.put(L1_word_id, L2_count);
//                            }
//
//                            if (L2_L1_counts.containsKey(L2_word_id)) {
//                                HashMap<Integer, Double> L1_count = L2_L1_counts.get(L2_word_id);
//                                if (L1_count.containsKey(L1_word_id)) {
//                                    L1_count.put(L1_word_id, L1_count.get(L1_word_id) + p_e_f);
//                                } else {
//                                    L1_count.put(L1_word_id, p_e_f);
//                                }
//                                L2_L1_counts.put(L2_word_id, L1_count);
//                            } else {
//                                HashMap<Integer, Double> L1_count = new HashMap<Integer, Double>();
//                                L1_count.put(L1_word_id, p_e_f);
//                                L2_L1_counts.put(L2_word_id, L1_count);
//                            }
//                        }
//                    }
//                }
//
//            }
//
//        }
//        System.out.println(L1_L2_counts.size());
//        System.out.println(L2_L1_counts.size());
//    }
    public void compute_L2_lexical_counts(HashMap<Integer, Double> L2_counts, HashMap<Integer, HashMap<Integer, Double>> L1_L2_counts) {
        System.out.println("Generating L2 lexical counts from L1-L2 Lexical counts");
        Iterator it = L1_L2_counts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Integer L1_key = (Integer) pairs.getKey();
            HashMap<Integer, Double> L2_id_and_counts = (HashMap<Integer, Double>) pairs.getValue();
            Iterator it2 = L2_id_and_counts.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pairs2 = (Map.Entry) it2.next();
                Integer L2_key = (Integer) pairs2.getKey();
                Double counts = (Double) pairs2.getValue();
                if (L2_counts.containsKey(L2_key)) {
                    L2_counts.put(L2_key, L2_counts.get(L2_key) + counts);
                } else {
                    L2_counts.put(L2_key, counts);
                }
            }
        }
    }

    public void compute_L1_L2_lexical_probabilities(HashMap<Integer, HashMap<Integer, Double>> L1_L2_probabilities, HashMap<Integer, HashMap<Integer, Double>> L1_L2_counts, HashMap<Integer, Double> L2_counts) {
        System.out.println("Generating L1-L2 lexical probabilities from the L1-L2 counts and L2 counts");
        Iterator it = L1_L2_counts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Integer L1_key = (Integer) pairs.getKey();
            HashMap<Integer, Double> L2_alignments_and_scores = (HashMap<Integer, Double>) pairs.getValue();
            Iterator it2 = L2_alignments_and_scores.entrySet().iterator();
            HashMap<Integer, Double> l2_ids_and_probs = new HashMap<Integer, Double>();
            while (it2.hasNext()) {
                Map.Entry pairs2 = (Map.Entry) it2.next();
                Integer L2_key = (Integer) pairs2.getKey();
                Double l1_l2_count = (Double) pairs2.getValue();
                Double l2_count = (Double) L2_counts.get(L2_key);
                l2_ids_and_probs.put(L2_key, l1_l2_count / l2_count);
            }
            L1_L2_probabilities.put(L1_key, l2_ids_and_probs);
        }
    }

//    public void compute_L1_L2_phrase_lexical_probabilities() {
//        System.out.println("Generating L1-L2 phrase lexical probabilities");
//        Iterator it = L1_L2_Maps.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry) it.next();
//            Integer L1_key = (Integer) pairs.getKey();
//            HashMap<Integer, scores_and_alignments> L2_alignments_and_scores = (HashMap<Integer, scores_and_alignments>) pairs.getValue();
//            Iterator it2 = L2_alignments_and_scores.entrySet().iterator();
//            while (it2.hasNext()) {
//                Map.Entry pairs2 = (Map.Entry) it2.next();
//                Integer L2_key = (Integer) pairs2.getKey();
//                scores_and_alignments s_a_l = (scores_and_alignments) pairs2.getValue();
//                HashMap<Integer, HashMap<Integer, Integer>> maps = s_a_l.alignments_map;
//                String inphrase[] = L1_phrases_String_number.inverse().get(L1_key).split(" ");
//                String outphrase[] = L2_phrases_String_number.inverse().get(L2_key).split(" ");
//                Double full_weight = 1.0;
//                for (Integer i : maps.keySet()) {
//                    int size = maps.get(i).size();
//                    Integer l1_word_id = L1_words_String_number.get(inphrase[i]);
//                    Double curr_weight = 0.0;
//                    for (Integer j : maps.get(i).keySet()) {
//                        Integer l2_word_id = L2_words_String_number.get(outphrase[j]);
//                        curr_weight += L1_L2_probabilities.get(l1_word_id).get(l2_word_id);
//                    }
//                    full_weight *= curr_weight / size;
//                }
//                s_a_l.scores[1] = full_weight;
//                maps = invert_alignments(s_a_l.alignments_map);
//                full_weight = 1.0;
//                for (Integer i : maps.keySet()) {
//                    int size = maps.get(i).size();
//                    Integer l2_word_id = L2_words_String_number.get(outphrase[i]);
//                    Double curr_weight = 0.0;
//                    for (Integer j : maps.get(i).keySet()) {
//                        Integer l1_word_id = L1_words_String_number.get(inphrase[j]);
//                        curr_weight += L2_L1_probabilities.get(l2_word_id).get(l1_word_id);
//                    }
//                    full_weight *= curr_weight / size;
//                }
//                s_a_l.scores[3] = full_weight;
//                pairs2.setValue(s_a_l);
//            }
//            pairs.setValue(L2_alignments_and_scores);
//        }
//    }
    public void write_L1_L2_phrase_table() {
        System.out.println("Writing New Phrase Table");
        int i = 0;
        for (Integer l1_key : L1_L2_Maps.keySet()) {
            HashMap<Integer, scores_and_alignments> L2_scores_and_alignments = L1_L2_Maps.get(l1_key);
            for (Integer l2_key : L2_scores_and_alignments.keySet()) {
                try {
                    String inphrase = L1_phrases_String_number.inverse().get(l1_key);
                    String outphrase = L2_phrases_String_number.inverse().get(l2_key);
                    scores_and_alignments current = L2_scores_and_alignments.get(l2_key);
                    if (current.scores[0] < 0.01) {
                        continue;
                    }
                    String scores = Double.toString(current.scores[0]) + " "
                            + Double.toString(current.scores[1]) + " "
                            + Double.toString(current.scores[2]) + " "
                            + Double.toString(current.scores[3]) + " "
                            + Double.toString(2.718);
                    //String alignments = convert_alignments_to_strings(current.alignments_map);
                    String alignments = "0-0";
                    String counts = Integer.toString((int) (1.0 / current.scores[0])) + " " + Integer.toString((int) (1.0 / current.scores[2])) + " 1";
                    if (i == 0) {
                        i = 1;
                    } else {
                        Files.final_L1_l2_Writer.write("\n");
                    }
                    Files.final_L1_l2_Writer.write(inphrase + " ||| " + outphrase + " ||| " + scores + " ||| " + alignments + " ||| " + counts);
                    Files.final_L1_l2_Writer.flush();

                } catch (IOException ex) {
                    Logger.getLogger(CombinePhraseTablesLowMemJapPivChi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("New Phrase Table Written.\nDone, dona done!");
    }

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
//        Files.open_files();
//        CombinePhraseTables cpt = new CombinePhraseTables();
//        cpt.read_tables_into_memory();
//        cpt.compute_L1_L2_phrase_probabilities_via_pivot();
//
//        cpt.compute_L1_L2_lexical_counts();
//        cpt.compute_L2_lexical_counts();
//        cpt.compute_Li_Lj_lexical_probabilities();
//        cpt.compute_L1_L2_phrase_lexical_probabilities();
//        cpt.write_L1_L2_phrase_table();
        //Files.phrase_tables_base = args[0];

        //String pivots[] = new String[]{"Chi", "Esp", "Kan", "Kor", "Mar", "Pai", "Tel", "Tha"};
        if (args.length > 0) {
            if (args[0].equals("help")) {
                System.out.println("To run this program type:");
                System.out.print("java -Xmx[memory to use] -cp '[path to]/grep4j-with-dependencies.jar:[path to]/guava-17.0.jar:[path to the package where this program is]'");
                System.out.println(" [path to the base of the phrase tables] [path to the base of the corpus (not used for now) ");
                System.out.println("[path to the japanese-chinese character map file] [source language] [target language]");
                System.out.println("[cutoff probability in floating point number (0.001 is default)] [cutoff count as in integer (20 is the default)]");
                System.out.println("[space separated sequence of pivot language initials (for now only English between Jap and Chi)]");

                //System.exit(1);
            } else {
                Files.phrase_tables_base = args[0];
                Files.corpus_base = args[1];
                Files.kanji_hanzi_map = args[2];
                src = args[3];
                tgt = args[4];
                String temp[] = args[5].split("_");
                cutoff = new Double[temp.length];

                for (int i = 0; i < cutoff.length; i++) {
                    cutoff[i] = Double.parseDouble(temp[i]);
                }

                temp = args[6].split("_");

                cutoff_count = new Integer[temp.length];

                for (int i = 0; i < cutoff_count.length; i++) {
                    cutoff_count[i] = Integer.parseInt(temp[i]);
                }
                cut_off_type = args[7];

                temp = args[8].split("_");
                pivots = new String[temp.length];

                for (int i = 0; i < pivots.length; i++) {
                    pivots[i] = temp[i];
                }
            }

        }

        for (String s : pivots) {
            System.out.println("Processing tables " + src + " to " + s + " and " + s + " to " + tgt);

            Files.open_files_low_mem(src, s, tgt);
            CombinePhraseTablesLowMemJapPivChi cpt = new CombinePhraseTablesLowMemJapPivChi();
            cpt.read_tables_and_corpora_into_memory();
            cpt.compute_L1_L2_phrase_probabilities_via_pivot();

            Files.close_files();

        }

//        for (String src : pivots) {
//            for (String tgt : pivots) {
//                if (src.equals(tgt)) {
//                    continue;
//                }
//                System.out.println("Language Pair: "+src+"-"+tgt);
//                for (String piv : pivots) {
//                    if (src.equals(piv) || piv.equals(tgt)) {
//                        continue;
//                    }
//                    System.out.println("Processing tables "+src+" to " + piv + " and " + piv + " to "+tgt);
//                    File f = new File(Files.phrase_tables_base+"/" + src+"-"+tgt+"/" + "phrase-table-" + src + "-" + piv + "-" + tgt);
//                    File f1 = new File(Files.phrase_tables_base+"/" + src+"-"+tgt+"/" + "phrase-table-" + src + "-" + piv + "-" + tgt+".gz");
//                    if(f.exists() || f1.exists()){
//                        System.out.println("Phrase table already exists for this pair and choice of pivot");
//                        continue;
//                    }
//                    Files.open_files(src, piv, tgt);
//                    CombinePhraseTables cpt = new CombinePhraseTables();
//                    cpt.read_tables_into_memory();
//                    cpt.compute_L1_L2_phrase_probabilities_via_pivot();
//
//                    cpt.compute_L1_L2_lexical_counts();
//                    cpt.compute_L2_lexical_counts();
//                    cpt.compute_Li_Lj_lexical_probabilities();
//                    cpt.compute_L1_L2_phrase_lexical_probabilities();
//                    cpt.write_L1_L2_phrase_table();
//                }
//            }
//        }
        //System.out.println("Number of phrases gained by bridging are:" + cpt.count_common());
//        Files.open_files("mr", "en", "hi");
//        CombinePhraseTables cpt = new CombinePhraseTables();
//        cpt.read_tables_into_memory();
//        cpt.compute_L1_L2_phrase_probabilities_via_pivot();
//
//        cpt.compute_L1_L2_lexical_counts();
//        cpt.compute_L2_lexical_counts();
//        cpt.compute_Li_Lj_lexical_probabilities();
//        cpt.compute_L1_L2_phrase_lexical_probabilities();
//        cpt.write_L1_L2_phrase_table();
//        
//        
//        Files.open_files("ta", "bn", "ur");
//        cpt = new CombinePhraseTables();
//        cpt.read_tables_into_memory();
//        cpt.compute_L1_L2_phrase_probabilities_via_pivot();
//
//        cpt.compute_L1_L2_lexical_counts();
//        cpt.compute_L2_lexical_counts();
//        cpt.compute_Li_Lj_lexical_probabilities();
//        cpt.compute_L1_L2_phrase_lexical_probabilities();
//        cpt.write_L1_L2_phrase_table();
//        
//        Files.open_files("ur", "en", "pa");
//        cpt = new CombinePhraseTables();
//        cpt.read_tables_into_memory();
//        cpt.compute_L1_L2_phrase_probabilities_via_pivot();
//
//        cpt.compute_L1_L2_lexical_counts();
//        cpt.compute_L2_lexical_counts();
//        cpt.compute_Li_Lj_lexical_probabilities();
//        cpt.compute_L1_L2_phrase_lexical_probabilities();
//        cpt.write_L1_L2_phrase_table();
    }

    private String convert_alignments_to_strings(HashMap<Integer, HashMap<Integer, Integer>> alignments_map) {
        ArrayList<Integer> keys = new ArrayList<Integer>(alignments_map.keySet());
        String alignments = "";
        Collections.sort(keys);
        for (Integer i : keys) {
            ArrayList<Integer> values = new ArrayList<Integer>(alignments_map.get(i).keySet());
            Collections.sort(values);
            for (Integer j : values) {
                alignments = alignments + (Integer.toString(i) + "-" + Integer.toString(j) + " ");
            }
        }
        alignments = alignments.trim();
        return alignments;

    }

    public void compute_L2_lexical_counts() {
        compute_L2_lexical_counts(L2_counts, L1_L2_counts);
        compute_L2_lexical_counts(L1_counts, L2_L1_counts);
    }

    public void compute_Li_Lj_lexical_probabilities() {
        compute_L1_L2_lexical_probabilities(L1_L2_probabilities, L1_L2_counts, L2_counts);
        compute_L1_L2_lexical_probabilities(L2_L1_probabilities, L2_L1_counts, L1_counts);
    }

    public HashMap<Integer, HashMap<Integer, Integer>> invert_alignments(HashMap<Integer, HashMap<Integer, Integer>> alignments_map) {
        HashMap<Integer, HashMap<Integer, Integer>> inverted_alignments = new HashMap<Integer, HashMap<Integer, Integer>>();
        for (Integer i : alignments_map.keySet()) {
            for (Integer j : alignments_map.get(i).keySet()) {
                if (inverted_alignments.containsKey(j)) {
                    HashMap<Integer, Integer> inner_invert = inverted_alignments.get(j);
                    inner_invert.put(i, alignments_map.get(i).get(j));
                    inverted_alignments.put(j, inner_invert);
                } else {
                    HashMap<Integer, Integer> inner_invert = new HashMap<Integer, Integer>();
                    inner_invert.put(i, alignments_map.get(i).get(j));
                    inverted_alignments.put(j, inner_invert);
                }
            }
        }
        return inverted_alignments;
    }

    private void write_translations_for_phrase(String src_phrase, HashMap<Integer, scores_and_alignments> tgt_scores_and_alignments) {

        int i = 0;
        HashMap<String, Integer> top_done_per_cutoff = new HashMap<String, Integer>();
        if (cut_off_type.equals("both")) {

            for (Double cutoff_prob : CombinePhraseTablesLowMemJapPivChi.cutoff) {
                for (Integer cutoff_count_each : CombinePhraseTablesLowMemJapPivChi.cutoff_count) {
                    top_done_per_cutoff.put("char-prob-" + cutoff_prob + "-" + cutoff_count_each, 0);
                }
            }
        }
        for (Integer l2_key : tgt_scores_and_alignments.keySet()) {
            try {
                String inphrase = src_phrase;
                String outphrase = L2_phrases_number_String.get(l2_key);
                scores_and_alignments current = tgt_scores_and_alignments.get(l2_key);

                String scores = Double.toString(current.scores[0]) + " "
                        + Double.toString(current.scores[1]) + " "
                        + Double.toString(current.scores[2]) + " "
                        + Double.toString(current.scores[3]);
                //String alignments = convert_alignments_to_strings(current.alignments_map);
                String alignments = "0-0";
                String counts = Integer.toString((int) (1.0 / current.scores[0])) + " " + Integer.toString((int) (1.0 / current.scores[2])) + " 1";
                BufferedWriter bw = null;
                if (cut_off_type.equals("both")) {

                    for (Double cutoff_prob : CombinePhraseTablesLowMemJapPivChi.cutoff) {
                        for (Integer cutoff_count_each : CombinePhraseTablesLowMemJapPivChi.cutoff_count) {
                            if (top_done_per_cutoff.get("char-prob-" + cutoff_prob + "-" + cutoff_count_each) < cutoff_count_each && current.scores[0] > cutoff_prob) {
                                bw = Files.final_L1_l2_Writer_Set.get("char-prob-" + cutoff_prob + "-" + cutoff_count_each);
                                bw.write(inphrase + " ||| " + outphrase + " ||| " + scores + " ||| " + alignments + " ||| " + counts + " ||| " + " ||| \n");
                                bw.flush();
                                top_done_per_cutoff.put("char-prob-" + cutoff_prob + "-" + cutoff_count_each, top_done_per_cutoff.get("char-prob-" + cutoff_prob + "-" + cutoff_count_each) + 1);
                            }
                        }
                    }
                } else if (cut_off_type.equals("prob")) {
                    for (Double cutoff_prob : CombinePhraseTablesLowMemJapPivChi.cutoff) {
                        if (current.scores[0] > cutoff_prob) {
                            bw = Files.final_L1_l2_Writer_Set.get("prob-" + cutoff_prob);
                            bw.write(inphrase + " ||| " + outphrase + " ||| " + scores + " ||| " + alignments + " ||| " + counts + " ||| " + " ||| \n");
                            bw.flush();
                        }
                    }
                } else if (cut_off_type.equals("char")) {
                    for (Integer cutoff_count_each : CombinePhraseTablesLowMemJapPivChi.cutoff_count) {
                        if (i < cutoff_count_each) {
                            bw = Files.final_L1_l2_Writer_Set.get("char-" + cutoff_count_each);
                            bw.write(inphrase + " ||| " + outphrase + " ||| " + scores + " ||| " + alignments + " ||| " + counts + " ||| " + " ||| \n");
                            bw.flush();
                        }
                    }
                    i++;
                }

            } catch (IOException ex) {
                Logger.getLogger(CombinePhraseTablesLowMemJapPivChi.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        //System.out.println(top_done_per_cutoff);
        //System.exit(1);
    }

    public HashMap<Integer, scores_and_alignments> prune_by_kanji_hanzi(String src_phr, HashMap<Integer, scores_and_alignments> tgt_scores_and_alignments, int cutoff_count) {
        //TODO: Decide logic to prune the shit out of this list of potential phrases
        HashSet<String> hanzi_chars = new HashSet<String>(Arrays.asList(src_phr.split("")));
        hanzi_chars.retainAll(all_hanzi);

        HashMap<Integer, Double> overlap_measure = new HashMap<Integer, Double>();

        Iterator it3 = tgt_scores_and_alignments.entrySet().iterator();
        while (it3.hasNext()) {
            Map.Entry pairs3 = (Map.Entry) it3.next();
            Integer temp_key = (Integer) pairs3.getKey();
            HashSet<String> kanji_chars = new HashSet<String>(Arrays.asList(L2_phrases_number_String.get(temp_key).split("")));
            kanji_chars.retainAll(all_kanji);
            HashSet<String> h2k_mapped = new HashSet<String>();
            HashSet<String> k2h_mapped = new HashSet<String>();
            for (String s : hanzi_chars) {
                if (Hanzi_to_Kanji_Map.containsKey(s)) {
                    h2k_mapped.addAll(Hanzi_to_Kanji_Map.get(s));
                }
            }

            for (String s : kanji_chars) {
                if (Kanji_to_Hanzi_Map.containsKey(s)) {
                    k2h_mapped.addAll(Kanji_to_Hanzi_Map.get(s));
                }
            }

            h2k_mapped.retainAll(kanji_chars);
            k2h_mapped.retainAll(hanzi_chars);
            overlap_measure.put(temp_key, 1.0 * h2k_mapped.size() * k2h_mapped.size());

        }

        overlap_measure = sortHashMapByValues(overlap_measure, cutoff_count);

        it3 = tgt_scores_and_alignments.entrySet().iterator();
        while (it3.hasNext()) {
            Map.Entry pairs3 = (Map.Entry) it3.next();
            Integer temp_key = (Integer) pairs3.getKey();
            if (!overlap_measure.containsKey(temp_key)) {
                it3.remove();
            }
        }
        return tgt_scores_and_alignments;

    }

    public LinkedHashMap sortHashMapByValues(HashMap passedMap, int cutoff_count) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();
        int i = 0;
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((Integer) key, (Double) val);
                    i++;
                    break;
                }

            }

            if (i == cutoff_count) {
                break;
            }

        }
        return sortedMap;
    }

    class scores_and_alignments {

        double scores[];
        //HashMap<Integer, HashMap<Integer, Integer>> alignments_map; // This is now kinda obsolete.... screw this off

        public scores_and_alignments(double[] scores, HashMap<Integer, HashMap<Integer, Integer>> alignments_map) {
            this.scores = scores;
            //this.alignments_map = alignments_map;

        }

        private scores_and_alignments() {
            this.scores = new double[4];
            this.scores[0] = this.scores[1] = this.scores[2] = this.scores[3] = 0.0;

            //this.alignments_map = new HashMap<Integer, HashMap<Integer, Integer>>();
        }
    }
}
