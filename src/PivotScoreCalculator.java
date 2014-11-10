/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author prajdabre
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author jyotesh
 */
public class PivotScoreCalculator {

    private class Translation {

        /**
         * Translation of sentence
         */
        String translation;
        /**
         * Scores of lexical reordering feature functions
         */
        double[] lexicalReorderingScores;
        /**
         * Score of distortion feature function
         */
        double distortionScore;
        /**
         * Score of language model feature function
         */
        double languageModelScore;
        /**
         * Score of word penalty feature function
         */
        double wordPenaltyScore;
        /**
         * Scores of translation model feature functions
         */
        double[] translationModelScores;
    }

    private class Weights {

        /**
         * Weights of lexical reordering feature functions
         */
        double[] lexicalReorderingWeights;
        /**
         * Weight of distortion feature function
         */
        double distortionWeight;
        /**
         * Weight of language model feature function
         */

        double languageModelWeight;
        /**
         * Weight of word penalty feature function
         */
        double wordPenaltyWeight;
        /**
         * Weights of translation model feature functions
         */
        double[] translationModelWeights;
    }

    /**
     * An arraylist containing arraylists of n-best translations for each input
     * sentence
     */
    private ArrayList<ArrayList<Translation>> pivotTranslations = null;

    /**
     * An arraylist containing arraylists of n-best translations for each
     * intermediate translation
     */
    private ArrayList<ArrayList<Translation>> finalTranslations = null;

    /**
     * Weights of feature functions for translations from source language to
     * intermediate form
     */
    private Weights pivotWeights = null;

    /**
     * Weights of feature functions for translations from intermediate form to
     * target language
     */
    private Weights finalWeights = null;

    /**
     * @param args command line arguments
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        
        
        String lang = "Tel";
        if(args.length>0){
            lang = args[0];
        }
            PivotScoreCalculator scoreCalculator = new PivotScoreCalculator();
        
            scoreCalculator.parseNBestFile("/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/Jap-"+lang+"/test."+lang+"-intermediate-n-best", false);
            scoreCalculator.parseNBestFile("/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/Jap-"+lang+"/test.Hin-n-best", true);
            scoreCalculator.parseMosesConfigFile("/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/Jap-"+lang+"/mert-work/moses.ini", false);
            scoreCalculator.parseMosesConfigFile("/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/"+lang+"-Hin/mert-work/moses.ini", true);
            scoreCalculator.findBestTranslation(new int[]{1, 1, 1, 1, 1}, "/home/prajdabre/ResearchWork/smt-systems/TM/n-way-mod/Jap-"+lang+"/test.Hin-translated");
       
    }

    /**
     * Converts String array to double array
     *
     * @param arr String array
     * @return double array obtained by parsing input String array
     */
    private double[] parseDoubleArray(String[] arr) {
        double[] retArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            retArr[i] = Double.parseDouble(arr[i]);
        }
        return retArr;
    }

    /**
     * Parses n-best file and reads translations and scores
     *
     * @param nBestFile name of file containing n-best list
     * @param isFinal does this file correspond to target language n-best list
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void parseNBestFile(String nBestFile, boolean isFinal) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(nBestFile), "UTF-8"));
        ArrayList<ArrayList<Translation>> translationArray = new ArrayList<ArrayList<Translation>>();
        String line;
        while ((line = br.readLine()) != null) {
            Translation currentTranslation = new Translation();
            line = line.trim();
            String[] fields = line.split(" \\|\\|\\| ");
            int sentenceNumber = Integer.parseInt(fields[0]);
            currentTranslation.translation = fields[1].trim();
            String[] scores = fields[2].trim().split("[ ]*[A-Za-z]+0= ");
            currentTranslation.lexicalReorderingScores = parseDoubleArray(scores[1].split(" "));
            currentTranslation.distortionScore = Double.parseDouble(scores[2]);
            currentTranslation.languageModelScore = Double.parseDouble(scores[3]);
            currentTranslation.wordPenaltyScore = Double.parseDouble(scores[4]);
            currentTranslation.translationModelScores = parseDoubleArray(scores[5].split(" "));
            if (sentenceNumber == translationArray.size()) {
                translationArray.add(new ArrayList<Translation>(Arrays.asList(currentTranslation)));
            } else {
                translationArray.get(sentenceNumber).add(currentTranslation);
            }
            if(isFinal){
                finalTranslations = translationArray;
            } else {
                pivotTranslations = translationArray;
            }
        }
    }
    /**
     * Parses Moses configuration file to read weights
     *
     * @param mosesConfigFile moses.ini file
     * @param isFinal does this file correspond to target language n-best list
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void parseMosesConfigFile(String mosesConfigFile, boolean isFinal) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mosesConfigFile)));
        Weights weights = new Weights();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("WordPenalty0")) {
                weights.wordPenaltyWeight = Double.parseDouble(line.split(" ")[1]);
            } else if (line.startsWith("TranslationModel0")) {
                String[] currentWeights = line.split(" ");
                weights.translationModelWeights = parseDoubleArray(Arrays.copyOfRange(currentWeights, 1, currentWeights.length));
            } else if (line.startsWith("LexicalReordering0")) {
                String[] currentWeights = line.split(" ");
                weights.lexicalReorderingWeights = parseDoubleArray(Arrays.copyOfRange(currentWeights, 1, currentWeights.length));
            } else if (line.startsWith("Distortion0")) {
                weights.distortionWeight = Double.parseDouble(line.split(" ")[1]);
            } else if (line.startsWith("LM0")) {
                weights.languageModelWeight = Double.parseDouble(line.split(" ")[1]);
            }
        }
        br.close();
        if (isFinal) {
            finalWeights = weights;
        } else {
            pivotWeights = weights;
        }
    }

    /**
     * Finds best translation according to the score generated using given
     * feature functions
     *
                                                                                                                                                                                           151,1         73%
     *
     * @param flags array containing flags to switch on or off feature functions
     * @param filename name of output file
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     */
    public void findBestTranslation(int[] flags, String filename) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String[] bestTranslations = new String[pivotTranslations.size()];
        for (int i = 0; i < pivotTranslations.size(); i++) {
            ArrayList<Translation> currentPivotTranslations = pivotTranslations.get(i);
            String bestTranslation = "";
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < currentPivotTranslations.size(); j++) {
                Translation currentPivotTranslation = currentPivotTranslations.get(j);
                double pivotScore = 0.0;
                for (int k = 0; k < currentPivotTranslation.lexicalReorderingScores.length; k++) {
                    pivotScore += currentPivotTranslation.lexicalReorderingScores[k] * pivotWeights.lexicalReorderingWeights[k] * flags[0];
                }
                pivotScore += currentPivotTranslation.distortionScore * pivotWeights.distortionWeight * flags[1];
                pivotScore += currentPivotTranslation.languageModelScore * pivotWeights.languageModelWeight * flags[2];
                pivotScore += currentPivotTranslation.wordPenaltyScore * pivotWeights.wordPenaltyWeight * flags[3];
                for (int k = 0; k < currentPivotTranslation.translationModelScores.length; k++) {
                    pivotScore += currentPivotTranslation.translationModelScores[k] * pivotWeights.translationModelWeights[k] * flags[4];
                }
                ArrayList<Translation> currentFinalTranslations = finalTranslations.get(i * currentPivotTranslations.size() + j);
                for (int k = 0; k < currentFinalTranslations.size(); k++) {
                    Translation currentFinalTranslation = currentFinalTranslations.get(k);
                    double finalScore = 0.0;
                    for (int l = 0; l < currentFinalTranslation.lexicalReorderingScores.length; l++) {
                        finalScore += currentFinalTranslation.lexicalReorderingScores[l] * finalWeights.lexicalReorderingWeights[l] * flags[0];
                    }
                    finalScore += currentFinalTranslation.distortionScore * finalWeights.distortionWeight * flags[1];
                    finalScore += currentFinalTranslation.languageModelScore * finalWeights.languageModelWeight * flags[2];
                    finalScore += currentFinalTranslation.wordPenaltyScore * finalWeights.wordPenaltyWeight * flags[3];
                    for (int l = 0; l < currentFinalTranslation.translationModelScores.length; l++) {
                        finalScore += currentFinalTranslation.translationModelScores[l] * finalWeights.translationModelWeights[l] * flags[4];
                    }
                    double currentScore = pivotScore + finalScore;
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestTranslation = currentFinalTranslation.translation;
                    }
                }
            }
            bestTranslations[i] = bestTranslation;
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        for (String bestTranslation : bestTranslations) {
            bw.write(bestTranslation);
            bw.newLine();
        }
        bw.close();
    }

}
