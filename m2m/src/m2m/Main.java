package m2m;

import org.eclipse.uml2.uml.Model;
import umlutils.LoadUML;
import xtext.EduquizzStandaloneSetup;

import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.common.TerminalsStandaloneSetup;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

public class Main {

	public static void main(String[] args) throws Exception {
		mm1.Questionnaire questionnaireMm1 = loadQuestionnaire("model/test.quizz");
		mm2.Questionnaire questionnaireMm2 = transformQuestionnaire(questionnaireMm1);
		saveQuestionnaire(questionnaireMm2, "model/test.xmi");
	}
	
	private static mm1.Questionnaire loadQuestionnaire(String path) throws Exception {
		System.out.println("Chargement du questionnaire...");
        try {
            mm1.Mm1Package.eINSTANCE.eClass();
            Injector injector = new EduquizzStandaloneSetup().createInjectorAndDoEMFRegistration();
            XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
            resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
            Resource resource = resourceSet.createResource(URI.createURI(path));
            resource.load(resourceSet.getLoadOptions());
            mm1.Questionnaire model = (mm1.Questionnaire) resource.getContents().get(0);
            System.out.println("Questionnaire chargé avec succès !");
            return model;
        } catch (Exception e) {
            throw new Exception("Erreur lors du chargement du questionnaire : " + e.getMessage());
        }
    }
	
	private static mm2.Questionnaire transformQuestionnaire(mm1.Questionnaire questionnaireMm1) {
		System.out.println("Transformation du questionnaire...");
		mm2.Questionnaire questionnaireMm2 = mm2.Mm2Factory.eINSTANCE.createQuestionnaire();
		List<mm2.Question> questionsMm2 = new ArrayList<>();
		List<mm2.Etiquette> etiquettesMm2 = new ArrayList<>();
		
		// Création des questions
		for (mm1.Question questionMm1 : questionnaireMm1.getQuestion()) {
			mm2.Question questionMm2 = mm2.Mm2Factory.eINSTANCE.createQuestion();
			questionMm2.setEnnonce(questionMm1.getEnnonce());
			questionMm2.setDifficulte(questionMm1.getDifficulte());
			questionMm2.setReponsesMultiples(questionMm1.isReponsesMultiples());
			for (mm1.Etiquette etiquetteMm1 : questionMm1.getEtiquette()) {
				if (!etiquettesMm2.stream().anyMatch(e -> e.getNom().equals(etiquetteMm1.getNom()))) {
					mm2.Etiquette etiquetteMm2 = mm2.Mm2Factory.eINSTANCE.createEtiquette();
					etiquetteMm2.setNom(etiquetteMm1.getNom());
					etiquettesMm2.add(etiquetteMm2);
					questionMm2.getEtiquette().add(etiquetteMm2);
				} else {
					mm2.Etiquette etiquetteMm2 = etiquettesMm2.stream().filter(e -> e.getNom().equals(etiquetteMm1.getNom())).findFirst().get();
					questionMm2.getEtiquette().add(etiquetteMm2);
				}
			}
			for (mm1.Reponse reponseMm1 : questionMm1.getReponse()) {
				mm2.Reponse reponseMm2 = mm2.Mm2Factory.eINSTANCE.createReponse();
				reponseMm2.setTexte(reponseMm1.getTexte());
				reponseMm2.setEstVraie(reponseMm1.isEstVraie());
				questionMm2.getReponse().add(reponseMm2);
			}
			questionsMm2.add(questionMm2);
		}
		
		// Création des pages de question
		for (mm2.Question questionMm2 : questionsMm2) {
			mm2.PageQuestion pageQuestionMm2 = mm2.Mm2Factory.eINSTANCE.createPageQuestion();
			pageQuestionMm2.setTitre("Questionnaire en cours");
			pageQuestionMm2.setQuestion(questionMm2);
			// TODO boutonSuivant
			// TODO boutonRetour
			// TODO pageSuivante
			// TODO pagePrecedente
			questionnaireMm2.getPage().add(pageQuestionMm2);
		}
		
		// Création de la page de soumission
		mm2.PageSoumission pageSoumissionMm2 = mm2.Mm2Factory.eINSTANCE.createPageSoumission();
		pageSoumissionMm2.setTitre("Soumission de vos réponses");
		pageSoumissionMm2.getQuestion().addAll(questionsMm2);
		// TODO boutonSoumettre
		// TODO pageSuivante
		// TODO pagePrecedente
		questionnaireMm2.getPage().add(pageSoumissionMm2);
		
		// Création de la page de résultat
		mm2.PageResultat pageResultatMm2 = mm2.Mm2Factory.eINSTANCE.createPageResultat();
		pageResultatMm2.setTitre("Résultats du questionnaire");
		questionnaireMm2.getPage().add(pageResultatMm2);
		
		System.out.println("Questionnaire transformé avec succès !");
		return questionnaireMm2;
	}
	
	private static void saveQuestionnaire(mm2.Questionnaire questionnaireMm2, String path) throws Exception {
		System.out.println("Sauvegarde du questionnaire transformé...");
		try {
			Injector injector = new EduquizzStandaloneSetup().createInjectorAndDoEMFRegistration();
			XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
			Resource resource = resourceSet.createResource(URI.createURI(path));
			resource.getContents().add(questionnaireMm2);
			resource.save(null);
			System.out.println("Questionnaire sauvegardé avec succès !");
		} catch (Exception e) {
			throw new Exception("Erreur lors de la sauvegarde du questionnaire : " + e.getMessage());
		}
	}

}
