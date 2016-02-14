package com.scheduler.booking.util

import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.ling.Sentence
import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.tagger.maxent.MaxentTagger

class TaggerDemo {

  private TaggerDemo() {}

  public static void main(String[] args) throws Exception {
    def model = "models/english-left3words-distsim.tagger"
	def input = "postagger.txt"
	
    MaxentTagger tagger = new MaxentTagger(model);
	def file = new File(input)
	println file
    List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(file)));
    for (List<HasWord> sentence : sentences) {
      List<TaggedWord> tSentence = tagger.tagSentence(sentence);
      System.out.println(Sentence.listToString(tSentence, false));
    }
  }

}
