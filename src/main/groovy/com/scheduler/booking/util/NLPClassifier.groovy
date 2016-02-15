package com.scheduler.booking.util

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component

import edu.stanford.nlp.ie.NERClassifierCombiner
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.*
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.time.*

@Component
public class NLPClassifier {

	/** Example usage:
	 *  java SUTimeDemo "Three interesting dates are 18 Feb 1997, the 20th
	 of july and 4 days from today."
	 *
	 *  @param args Strings to interpret
	 */

	AnnotationPipeline pipeline = null

	public static final String NER_3CLASS = DefaultPaths.DEFAULT_NER_THREECLASS_MODEL;
	public static final String NER_7CLASS = DefaultPaths.DEFAULT_NER_MUC_MODEL;
	public static final String NER_MISCCLASS = DefaultPaths.DEFAULT_NER_CONLL_MODEL;

	@PostConstruct
	def create() {
		Properties props = new Properties();
		props.setProperty("ner.model", NER_3CLASS);
		//		def crfClassifier = CRFClassifier.getClassifier("classifiers/english.all.3class.distsim.crf.ser.gz")
		NERClassifierCombiner ner = NERClassifierCombiner.createNERClassifierCombiner("ner", props);
		pipeline = new AnnotationPipeline();
		pipeline.addAnnotator(new TokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		pipeline.addAnnotator(new POSTaggerAnnotator(false));
		pipeline.addAnnotator(new NERCombinerAnnotator(ner, false, 1, -1))
		pipeline.addAnnotator(new ParserAnnotator("pa", props));
		pipeline.addAnnotator(new BinarizerAnnotator("ba", props));
		pipeline.addAnnotator(new SentimentAnnotator("sa",props))
		pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	}

	//	(String text : [
	//		"Can you find a slot for me at 10AM tomorrow?",
	//		"Hey John Vader, Mornings are not so good.",
	//			"So 11am slots are out.",
	//			"I can do at 4pm. ",
	//			"Can you find a slot for me at 4 PM day after?",
	//			"Or I can do it tomorrow."
	//	])
	def classify(texts) {
		def retList = []
		texts.each{text->
			Annotation annotation = new Annotation(text);
			annotation.set(CoreAnnotations.DocDateAnnotation.class, new Date().format("yyyy-MM-dd"));
			//	  annotation.set(CoreAnnotations.SentencesAnnotation.class, );
			//			annotation.set(CoreAnnotations.SemanticWordAnnotation.class, new Date().format("yyyy-MM-dd"));
			pipeline.annotate(annotation);
			System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
			def resultsMap = [:]
			annotation.get(TimeAnnotations.TimexAnnotations.class).each {
				println it.get(TimeExpression.Annotation.class).getTemporal()
				addToMap(resultsMap, "Temporals", it.get(TimeExpression.Annotation.class).getTemporal())
			}
			annotation.get(CoreAnnotations.SentencesAnnotation.class).each { sentence ->
				addToMap(resultsMap, "Sentiment", sentence.get(SentimentCoreAnnotations.SentimentClass.class))
				sentence.get(CoreAnnotations.TokensAnnotation.class).each { token ->
					addToMap(resultsMap, "NER", token.ner())
				}
			}
			System.out.println("--");
			retList.add(resultsMap)
		}
		println retList
		retList
	}

	static def addToMap(map, key, value) {
		map.containsKey(key)?map.get(key).add(value):map.put(key,[value])
	}

	//	static def printClassifications(list) {
	//		for (CoreMap cm : list) {
	//			List tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
	//			def temporal = cm.get(TimeExpression.Annotation.class).getTemporal()?:''
	//			def sentiment = cm.get(SentimentCoreAnnotations.SentimentClass.class)?:''
	//			println "${'Temporal:'+temporal}"
	//			println "${'Sentiment:'+sentiment}"
	//		}
	//	}

	static main(String[] args){
		String[] input = [
			'Hey Vader, Can you find me a slot on wednesday between 3PM and 5PM or next monday at 5PM?',
			"Can you find a slot for me at 10AM tomorrow?",
			"Hey John Vader, Mornings are not so good.",
			"how about after noon?.",
			"or how about in the evening?.",
			"So 11am slots are out.",
			"I can do at 4pm. ",
			"Can you find a slot for me at 4 PM day after?",
			"Or I can do it tomorrow."
		]
		def localClassifier = new NLPClassifier()
		localClassifier.create()
		localClassifier.classify(input)
	}
}