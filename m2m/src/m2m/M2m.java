package m2m;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import xtext.EduquizzStandaloneSetup;

public class M2m {
	
	public static void transform(String inputPath, String outputPath) throws Exception {
		mm1.Questionnaire questionnaireMm1 = loadQuestionnaire(inputPath);
		mm2.Questionnaire questionnaireMm2 = transformQuestionnaire(questionnaireMm1);
		saveQuestionnaire(questionnaireMm2, outputPath);
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
		List<mm2.Page> pagesMm2 = new ArrayList<>();
		
		// Nom du questionnaire
		questionnaireMm2.setNom(questionnaireMm1.getNom());
		
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
		
		// Mélange des questions
		if (questionnaireMm1.isMelange()) {
			Collections.shuffle(questionsMm2);
		}
		
		// Création des pages de question
		for (mm2.Question questionMm2 : questionsMm2) {
			mm2.PageQuestion pageQuestionMm2 = mm2.Mm2Factory.eINSTANCE.createPageQuestion();
			pageQuestionMm2.setTitre("Questionnaire en cours");
			pageQuestionMm2.setQuestion(questionMm2);
			// Si il s'agit de la première question, on ne fait rien
			// Sinon, on met la page suivante de la question précédente à la page de la question actuelle
			if (!questionMm2.equals(questionsMm2.getFirst())) {
				if (questionnaireMm1.isRetourAutorise()) {
					pageQuestionMm2.setBoutonRetour(mm2.Mm2Factory.eINSTANCE.createBoutonRetour());
					pageQuestionMm2.setPagePrecedente(pagesMm2.getLast());
				}
				((mm2.PageQuestion) pagesMm2.getLast()).setPageSuivante(pageQuestionMm2);
			}
			pageQuestionMm2.setBoutonSuivant(mm2.Mm2Factory.eINSTANCE.createBoutonSuivant());
			pagesMm2.add(pageQuestionMm2);
		}
		questionnaireMm2.getPage().addAll(pagesMm2);
		
		// Création de la page de résultat
		mm2.PageResultat pageResultatMm2 = mm2.Mm2Factory.eINSTANCE.createPageResultat();
		pageResultatMm2.setTitre("Résultats du questionnaire");
		
		// Création de la page de soumission
		mm2.PageSoumission pageSoumissionMm2 = mm2.Mm2Factory.eINSTANCE.createPageSoumission();
		pageSoumissionMm2.setTitre("Soumission de vos réponses");
		pageSoumissionMm2.getQuestion().addAll(questionsMm2);
		pageSoumissionMm2.setBoutonSoumettre(mm2.Mm2Factory.eINSTANCE.createBoutonSoumettre());
		pageSoumissionMm2.setPageSuivante(pageResultatMm2);
		if (questionnaireMm1.isRetourAutorise()) {
			pageSoumissionMm2.setBoutonRetour(mm2.Mm2Factory.eINSTANCE.createBoutonRetour());
			pageSoumissionMm2.setPagePrecedente(questionnaireMm2.getPage().getLast());
		}
		((mm2.PageQuestion) questionnaireMm2.getPage().getLast()).setPageSuivante(pageSoumissionMm2); // La page suivante de la dernière question est la page de soumission
		questionnaireMm2.getPage().add(pageSoumissionMm2);
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
