package umlutils;

import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Package; // attention, ne pas utiliser la classe Package de l'introspection Java

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.AssociationClass;
import org.eclipse.uml2.uml.Class; // attention, ne pas utiliser la classe Class de l'introspection Java

public class SearchUML {
	public static List<AssociationClass> findAssociationClassesInPackage(Package p){
		List<AssociationClass> result=new ArrayList<AssociationClass>();
		for (PackageableElement pe:p.getPackagedElements()){
			if (pe instanceof AssociationClass){
				AssociationClass ac=(AssociationClass)pe;
				result.add(ac);
			} else if (pe instanceof Package) {
				Package pep=(Package)pe;
				result.addAll(findAssociationClassesInPackage(pep));
			}
		}
		return result;
	}
	
	public static Class findClassInPackage(String name, Package p){
		Class c=null;
		for (PackageableElement pd:p.getPackagedElements()){
			if (pd instanceof Class){
				Class pdc=(Class)pd;
				if (pdc.getName().equals(name)){return pdc;}
			} else if (pd instanceof Package) {
				Package pdp=(Package)pd;
				c=findClassInPackage(name, pdp);
				if (c!=null){return c;}
			}
		}
		return c;
	}
	
	public static Package findPackageInPackage(String name, Package p){
		Package result=null;
		for (PackageableElement pd:p.getPackagedElements()){
			 if (pd instanceof Package) {
				Package pdp=(Package)pd;
				if (pdp.getName().equals(name)){return pdp;}
				result=findPackageInPackage(name, pdp);
				if (result!=null){return result;}
			}
		}
		return result;
	}

}
