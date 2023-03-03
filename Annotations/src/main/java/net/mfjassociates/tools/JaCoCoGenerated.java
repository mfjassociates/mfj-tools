package net.mfjassociates.tools;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

public class JaCoCoGenerated {
	
	static public final String JACOCO_GENERATED_CLASS_NAME="JaCoCoGenerated";
	
	private JaCoCoGenerated() {
		throw new UnsupportedOperationException("This helper should not be instantiated");
	}
	
	public static void main(String[] args) throws IOException {
		JCodeModel codeModel=new JCodeModel();
		String packageName="dev.wonderful";
		JPackage generatableType=codeModel._package(packageName);
    	createCustomPackageRuntimeGeneratedAnnotation(generatableType);
		codeModel.build(new SingleStreamCodeWriter(System.out));

	}

	/**
	 * This will add to the codeModel associated with the passed generatableType an interface in the same
	 * package suitable for JaCoCo use  (i.e. retention policy of runtime)
	 * 
	 * @param generatableType
	 * @return
	 */
	protected static JDefinedClass createCustomPackageRuntimeGeneratedAnnotation(JPackage generatableType) {
		JDefinedClass jacocoGenerated=null;
		try {
			jacocoGenerated=generatableType._annotationTypeDeclaration(JACOCO_GENERATED_CLASS_NAME);
		} catch (JClassAlreadyExistsException e) {
			jacocoGenerated=e.getExistingClass();
		}
		// add the Documented annotation
		jacocoGenerated.annotate(Documented.class);
		// add the Retention annotation with retention policy of runtime
		JAnnotationUse use = jacocoGenerated.annotate(Retention.class);
		use.param("value", RetentionPolicy.RUNTIME);
		// add the Target annotation with the element types: type, method and constructor
		use=jacocoGenerated.annotate(Target.class);
		JAnnotationArrayMember useArray = use.paramArray("value");
		useArray.param(ElementType.TYPE);
		useArray.param(ElementType.METHOD);
		useArray.param(ElementType.CONSTRUCTOR);
		return jacocoGenerated;
	}

}
