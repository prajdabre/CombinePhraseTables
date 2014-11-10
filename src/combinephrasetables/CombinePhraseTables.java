/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combinephrasetables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raj
 */
public class CombinePhraseTables {

    /**
     * @param args the command line arguments
     */
    BiMap<String, Integer> L1_phrases_String_number = HashBiMap.create();
    BiMap<String, Integer> Pivot_phrases_String_number = HashBiMap.create();
    BiMap<String, Integer> L2_phrases_String_number = HashBiMap.create();
    BiMap<String, Integer> L1_words_String_number = HashBiMap.create();
    BiMap<String, Integer> Pivot_words_String_number = HashBiMap.create();
    BiMap<String, Integer> L2_words_String_number = HashBiMap.create();
//    IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>> L1_Pivot_Maps = new IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>>();
//    IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>> Pivot_L2_Maps = new IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>>();
//    IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>> L1_Pivot_Maps_Inverted = new IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>>();
//    IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>> Pivot_L2_Maps_Inverted = new IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>>();
//    IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>> L1_L2_Maps = new IntObjectOpenHashMap<IntObjectOpenHashMap<scores_and_alignments>>();
//    
//    IntObjectOpenHashMap<IntDoubleOpenHashMap> L1_L2_counts = new IntObjectOpenHashMap<IntDoubleOpenHashMap>();
//    IntObjectOpenHashMap<IntDoubleOpenHashMap> L2_L1_counts = new IntObjectOpenHashMap<IntDoubleOpenHashMap>();
//    IntObjectOpenHashMap<IntDoubleOpenHashMap> L1_L2_probabilities = new IntObjectOpenHashMap<IntDoubleOpenHashMap>();
//    IntObjectOpenHashMap<IntDoubleOpenHashMap> L2_L1_probabilities = new IntObjectOpenHashMap<IntDoubleOpenHashMap>();
//    
//    IntDoubleOpenHashMap L2_counts = new IntDoubleOpenHashMap();
//    IntDoubleOpenHashMap L1_counts = new IntDoubleOpenHashMap();
    HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_Pivot_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, scores_and_alignments>> Pivot_L2_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_L2_Maps = new HashMap<Integer, HashMap<Integer, scores_and_alignments>>();
    HashMap<Integer, HashMap<Integer, Double>> L1_L2_counts = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L2_L1_counts = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L1_L2_probabilities = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, HashMap<Integer, Double>> L2_L1_probabilities = new HashMap<Integer, HashMap<Integer, Double>>();
    HashMap<Integer, Double> L2_counts = new HashMap<Integer, Double>();
    HashMap<Integer, Double> L1_counts = new HashMap<Integer, Double>();
    //HashMap<Integer, HashMap<Integer, scores_and_alignments>> L1_Pivot_Maps = new HashMap<Integer, HashMap<Integer, Double>();

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
                    //L1_phrases_String_number_inverted.put(L1_phrase_Count, inphrase);
                    L1_phrase_Count++;
                }
                if (!L2_phrases_String_number.containsKey(outphrase)) {
                    L2_phrases_String_number.put(outphrase, L2_phrase_Count);
                    //L2_phrases_String_number_inverted.put(L2_phrase_Count, outphrase);
                    L2_phrase_Count++;
                }
                String words[] = inphrase.split(" ");

                for (String word : words) {
                    word = word.trim();
                    if (!L1_words_String_number.containsKey(word)) {
                        L1_words_String_number.put(word, L1_word_count);
                        //L1_words_String_number_inverted.put(L1_word_count, word);
                        L1_word_count++;
                    }
                }
//                Set<Integer> temp1 = new HashSet();
//                for (String word : words) {
//                    word = word.trim();
//                    Integer word_id = L1_words_String_number.get(word);
//                    temp1.add(word_id);
//                    Integer phrase_id = L1_phrases_String_number.get(inphrase);
//                    if (L1_words_phrases_map.containsKey(word_id)) {
//                        Set<Integer> temp = L1_words_phrases_map.get(word_id);
//                        temp.add(phrase_id);
//                        L1_words_phrases_map.put(word_id, temp);
//                    } else {
//                        Set<Integer> temp = new HashSet<Integer>();
//                        temp.add(phrase_id);
//                        L1_words_phrases_map.put(word_id, temp);
//                    }
//                }

                //L1_words_phrases_map_inverted.put(L1_words_String_number.get(inphrase), temp1);

                words = outphrase.split(" ");
                for (String word : words) {
                    word = word.trim();
                    if (!L2_words_String_number.containsKey(word)) {
                        L2_words_String_number.put(word, L2_word_count);
                        //L2_words_String_number_inverted.put(L2_word_count, word);
                        L2_word_count++;
                    }
                }

//                temp1 = new HashSet();
//
//                for (String word : words) {
//                    word = word.trim();
//                    Integer word_id = L2_words_String_number.get(word);
//                    temp1.add(word_id);
//                    Integer phrase_id = L2_phrases_String_number.get(inphrase);
//                    if (L2_words_phrases_map.containsKey(word_id)) {
//                        Set<Integer> temp = L2_words_phrases_map.get(word_id);
//                        temp.add(phrase_id);
//                        L2_words_phrases_map.put(word_id, temp);
//                    } else {
//                        Set<Integer> temp = new HashSet<Integer>();
//                        temp.add(phrase_id);
//                        L2_words_phrases_map.put(word_id, temp);
//                    }
//                }

                //L2_words_phrases_map_inverted.put(L2_words_String_number.get(outphrase), temp1);

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

//                if (L1_L2_Maps_Inverted.containsKey(L2_id)) {
//                    HashMap<Integer, scores_and_alignments> temp = L1_L2_Maps_Inverted.get(L2_id);
//                    temp.put(L1_id, saa);
//                    L1_L2_Maps_Inverted.put(L2_id, temp);
//                } else {
//                    HashMap<Integer, scores_and_alignments> temp = new HashMap<Integer, scores_and_alignments>();
//                    temp.put(L1_id, saa);
//                    L1_L2_Maps_Inverted.put(L2_id, temp);
//                }


            }
        } catch (IOException ex) {
            Logger.getLogger(CombinePhraseTables.class.getName()).log(Level.SEVERE, null, ex);
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
        //System.out.println("Here");
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

    public void read_tables_into_memory() {
        System.out.println("Reading L1-Pivot table into memory");
        read_table(Files.L1_Pivot_Reader, L1_words_String_number, Pivot_words_String_number, L1_phrases_String_number, Pivot_phrases_String_number, L1_Pivot_Maps, 0, 0, 0, 0);
        System.out.println("Reading Pivot-L2 table into memory");
        read_table(Files.Pivot_L2_Reader, Pivot_words_String_number, L2_words_String_number, Pivot_phrases_String_number, L2_phrases_String_number, Pivot_L2_Maps, Pivot_phrases_String_number.size(), 0, Pivot_words_String_number.size(), 0);
        System.out.println(L1_words_String_number.size() + " " + Pivot_words_String_number.size() + " " + L2_words_String_number.size() + " " + L1_Pivot_Maps.size() + " " + Pivot_L2_Maps.size());

        System.out.println("Tables read into memory");
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
            //System.out.println("At phrase:"+counter +" Out of "+ L1_Pivot_Maps.size() +"Common pivots: "+Pivot_keys_common.size());
            
//            if(counter%100==0){
//                System.out.println(counter+" Phrases processed");
//                
//            }
//            counter++;
            HashMap<Integer, scores_and_alignments> tgt_scores_and_alignments = new HashMap<Integer, scores_and_alignments>();
            for (Integer piv_key : Pivot_keys_common) {
                HashMap<Integer, scores_and_alignments> piv_tgt_scores = Pivot_L2_Maps.get(piv_key);
                for (Integer l2_key : piv_tgt_scores.keySet()) {
                    if (tgt_scores_and_alignments.containsKey(l2_key)) {
                        scores_and_alignments temp = tgt_scores_and_alignments.get(l2_key);
                        temp.alignments_map = induce_or_update_alignment_map(L1_Pivot_Maps.get(L1_key).get(piv_key).alignments_map, Pivot_L2_Maps.get(piv_key).get(l2_key).alignments_map, temp.alignments_map);
                        temp.scores[0] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[0] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[0];
                        temp.scores[2] += L1_Pivot_Maps.get(L1_key).get(piv_key).scores[2] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[2];
                        tgt_scores_and_alignments.put(l2_key, temp);
                    } else {
                        scores_and_alignments temp = new scores_and_alignments();
                        temp.alignments_map = induce_or_update_alignment_map(L1_Pivot_Maps.get(L1_key).get(piv_key).alignments_map, Pivot_L2_Maps.get(piv_key).get(l2_key).alignments_map, new HashMap<Integer, HashMap<Integer, Integer>>());
                        temp.scores[0] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[0] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[0];
                        temp.scores[2] = L1_Pivot_Maps.get(L1_key).get(piv_key).scores[2] * Pivot_L2_Maps.get(piv_key).get(l2_key).scores[2];
                        tgt_scores_and_alignments.put(l2_key, temp);
                    }

                }

            }
            Iterator it3 = tgt_scores_and_alignments.entrySet().iterator();
            while (it3.hasNext()) {
                Map.Entry pairs3 = (Map.Entry) it3.next();
                Integer temp_key = (Integer) pairs3.getKey();
                scores_and_alignments sal_temp = (scores_and_alignments) pairs3.getValue();
                if (sal_temp.scores[0] < 0.01) {
                    it3.remove();
                }
            }
            L1_L2_Maps.put(L1_key, tgt_scores_and_alignments);

        }

        L1_Pivot_Maps = null;
        Pivot_L2_Maps = null;
        System.gc();
        System.out.println("Probabilities Calculated");
    }

    public void compute_L1_L2_lexical_counts() {
        System.out.println("Generating L1-L2 and L2-L1 lexical counts");
        Iterator it = L1_L2_Maps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Integer L1_key = (Integer) pairs.getKey();
            HashMap<Integer, scores_and_alignments> L2_alignments_and_scores = (HashMap<Integer, scores_and_alignments>) pairs.getValue();
            Iterator it2 = L2_alignments_and_scores.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pairs2 = (Map.Entry) it2.next();
                Integer L2_key = (Integer) pairs2.getKey();
                scores_and_alignments s_a_l = (scores_and_alignments) pairs2.getValue();
                Double p_f_e = s_a_l.scores[0];
                Double p_e_f = s_a_l.scores[2];
                String inphrase[] = L1_phrases_String_number.inverse().get(L1_key).split(" ");
                String outphrase[] = L2_phrases_String_number.inverse().get(L2_key).split(" ");
                for (int i = 0; i < inphrase.length; i++) {
                    Integer L1_word_id = L1_words_String_number.get(inphrase[i]);
                    if (s_a_l.alignments_map.containsKey(i)) {
                        for (Integer s : s_a_l.alignments_map.get(i).keySet()) {
                            Integer L2_word_id = L2_words_String_number.get(outphrase[s]);
                            if (L1_L2_counts.containsKey(L1_word_id)) {
                                HashMap<Integer, Double> L2_count = L1_L2_counts.get(L1_word_id);
                                if (L2_count.containsKey(L2_word_id)) {
                                    L2_count.put(L2_word_id, L2_count.get(L2_word_id) + p_f_e);
                                } else {
                                    L2_count.put(L2_word_id, p_f_e);
                                }
                                L1_L2_counts.put(L1_word_id, L2_count);
                            } else {
                                HashMap<Integer, Double> L2_count = new HashMap<Integer, Double>();
                                L2_count.put(L2_word_id, p_f_e);
                                L1_L2_counts.put(L1_word_id, L2_count);
                            }

                            if (L2_L1_counts.containsKey(L2_word_id)) {
                                HashMap<Integer, Double> L1_count = L2_L1_counts.get(L2_word_id);
                                if (L1_count.containsKey(L1_word_id)) {
                                    L1_count.put(L1_word_id, L1_count.get(L1_word_id) + p_e_f);
                                } else {
                                    L1_count.put(L1_word_id, p_e_f);
                                }
                                L2_L1_counts.put(L2_word_id, L1_count);
                            } else {
                                HashMap<Integer, Double> L1_count = new HashMap<Integer, Double>();
                                L1_count.put(L1_word_id, p_e_f);
                                L2_L1_counts.put(L2_word_id, L1_count);
                            }
                        }
                    }
                }


            }

        }
        System.out.println(L1_L2_counts.size());
        System.out.println(L2_L1_counts.size());
    }

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

    public void compute_L1_L2_phrase_lexical_probabilities() {
        System.out.println("Generating L1-L2 phrase lexical probabilities");
        Iterator it = L1_L2_Maps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Integer L1_key = (Integer) pairs.getKey();
            HashMap<Integer, scores_and_alignments> L2_alignments_and_scores = (HashMap<Integer, scores_and_alignments>) pairs.getValue();
            Iterator it2 = L2_alignments_and_scores.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pairs2 = (Map.Entry) it2.next();
                Integer L2_key = (Integer) pairs2.getKey();
                scores_and_alignments s_a_l = (scores_and_alignments) pairs2.getValue();
                HashMap<Integer, HashMap<Integer, Integer>> maps = s_a_l.alignments_map;
                String inphrase[] = L1_phrases_String_number.inverse().get(L1_key).split(" ");
                String outphrase[] = L2_phrases_String_number.inverse().get(L2_key).split(" ");
                Double full_weight = 1.0;
                for (Integer i : maps.keySet()) {
                    int size = maps.get(i).size();
                    Integer l1_word_id = L1_words_String_number.get(inphrase[i]);
                    Double curr_weight = 0.0;
                    for (Integer j : maps.get(i).keySet()) {
                        Integer l2_word_id = L2_words_String_number.get(outphrase[j]);
                        curr_weight += L1_L2_probabilities.get(l1_word_id).get(l2_word_id);
                    }
                    full_weight *= curr_weight / size;
                }
                s_a_l.scores[1] = full_weight;
                maps = invert_alignments(s_a_l.alignments_map);
                full_weight = 1.0;
                for (Integer i : maps.keySet()) {
                    int size = maps.get(i).size();
                    Integer l2_word_id = L2_words_String_number.get(outphrase[i]);
                    Double curr_weight = 0.0;
                    for (Integer j : maps.get(i).keySet()) {
                        Integer l1_word_id = L1_words_String_number.get(inphrase[j]);
                        curr_weight += L2_L1_probabilities.get(l2_word_id).get(l1_word_id);
                    }
                    full_weight *= curr_weight / size;
                }
                s_a_l.scores[3] = full_weight;
                pairs2.setValue(s_a_l);
            }
            pairs.setValue(L2_alignments_and_scores);
        }
    }

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
                    String alignments = convert_alignments_to_strings(current.alignments_map);
                    String counts = Integer.toString((int) (1.0 / current.scores[0])) + " " + Integer.toString((int) (1.0 / current.scores[2])) + " 1";
                    if (i == 0) {
                        i = 1;
                    } else {
                        Files.final_L1_l2_Writer.write("\n");
                    }
                    Files.final_L1_l2_Writer.write(inphrase + " ||| " + outphrase + " ||| " + scores + " ||| " + alignments + " ||| " + counts);
                    Files.final_L1_l2_Writer.flush();


                } catch (IOException ex) {
                    Logger.getLogger(CombinePhraseTables.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("New Phrase Table Written.\nDone, dona done!");
    }

    public static void main(String[] args) {
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
        Files.phrase_tables_base = args[0];
        
        String pivots[] = new String[]{"Chi","Esp","Kan","Kor","Mar","Pai","Tel"};

        for (String s : pivots) {
            System.out.println("Processing tables Jap to " + s + " and " + s + " to Hin");
            Files.open_files("Jap", s, "Hin");
            CombinePhraseTables cpt = new CombinePhraseTables();
            cpt.read_tables_into_memory();
            cpt.compute_L1_L2_phrase_probabilities_via_pivot();

            cpt.compute_L1_L2_lexical_counts();
            cpt.compute_L2_lexical_counts();
            cpt.compute_Li_Lj_lexical_probabilities();
            cpt.compute_L1_L2_phrase_lexical_probabilities();
            cpt.write_L1_L2_phrase_table();
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

    class scores_and_alignments {

        double scores[];
        HashMap<Integer, HashMap<Integer, Integer>> alignments_map;

        public scores_and_alignments(double[] scores, HashMap<Integer, HashMap<Integer, Integer>> alignments_map) {
            this.scores = scores;
            this.alignments_map = alignments_map;

        }

        private scores_and_alignments() {
            this.scores = new double[4];
            this.scores[0] = this.scores[1] = this.scores[2] = this.scores[3] = 0.0;


            this.alignments_map = new HashMap<Integer, HashMap<Integer, Integer>>();
        }
    }
}
